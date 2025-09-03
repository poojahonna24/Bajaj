package com.example.bajaj;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

@Component
class Runner implements CommandLineRunner {

    private final WebClient web = WebClient.builder().build();

    @Value("${bfhl.name}")  String name;
    @Value("${bfhl.regNo}") String regNo;
    @Value("${bfhl.email}") String email;

    private static final String GEN_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private static final String SQL_Q1 = "SELECT ...";  // (Q1 SQL here)
    private static final String SQL_Q2 = "SELECT ...";  // (Q2 SQL here)

    record GenerateReq(String name, String regNo, String email) {}
    record GenerateRes(String webhook, String accessToken) {}
    record SubmitReq(String finalQuery) {}

    @Override
    public void run(String... args) {
        GenerateRes gen = web.post()
                .uri(GEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new GenerateReq(name, regNo, email))
                .retrieve()
                .bodyToMono(GenerateRes.class)
                .block();

        int lastTwo = Integer.parseInt(regNo.replaceAll("\\D", "")
                .substring(regNo.length() - 2));
        String sql = (lastTwo % 2 == 0) ? SQL_Q2 : SQL_Q1;

        String resp = web.post()
                .uri(gen.webhook())
                .header("Authorization", gen.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SubmitReq(sql))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Submission response: " + resp);
    }
}
