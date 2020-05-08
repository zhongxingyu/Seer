 package com.example.stocks.portfolio;
 
 import com.example.stocks.infrastructure.HttpServer;
 import com.github.tomakehurst.wiremock.WireMockServer;
 import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
 import com.github.tomakehurst.wiremock.client.UrlMatchingStrategy;
 import com.github.tomakehurst.wiremock.client.VerificationException;
 import com.github.tomakehurst.wiremock.client.WireMock;
 import com.google.code.tempusfugit.concurrency.Callable;
 
 import static com.example.stocks.infrastructure.UrlMatchingStrategies.urlEndingWith;
 import static com.github.tomakehurst.wiremock.client.WireMock.*;
 import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
 import static com.google.code.tempusfugit.condition.Conditions.assertion;
 import static com.google.code.tempusfugit.temporal.Duration.millis;
 import static com.google.code.tempusfugit.temporal.Timeout.timeout;
 import static com.google.code.tempusfugit.temporal.WaitFor.waitFor;
 import static org.hamcrest.Matchers.is;
 
 public class FakeHttpServer implements HttpServer {
 
     private final Integer port;
     private final WireMockServer server;
 
     public FakeHttpServer(Integer port) {
         this.port = port;
         this.server = new WireMockServer(wireMockConfig().port(this.port));
     }
 
     @Override
     public void start() {
         configureFor("localhost", port);
         server.start();
     }
 
     public void stub(UrlMatchingStrategy urlMatchingStrategy, ResponseDefinitionBuilder response) {
         stubFor(get(urlMatchingStrategy).willReturn(response));
     }
 
     public void verify(UrlMatchingStrategy url) throws InterruptedException, AssertionError {
         waitFor(assertion(verificationOf(url), is(true)), timeout(millis(500)));
     }
 
     private static Callable<Boolean, RuntimeException> verificationOf(final UrlMatchingStrategy urlMatchingStrategy) {
         return new Callable<Boolean, RuntimeException>() {
             @Override
             public Boolean call() throws RuntimeException {
                 try {
                     WireMock.verify(getRequestedFor(urlMatchingStrategy));
                     return true;
                 } catch (VerificationException e) {
                     System.err.println(e.getMessage());
                     return false;
                 }
             }
         };
     }
 
     @Override
     public void stop() {
         server.stop();
     }
 
     public static void main(String[] args) {
         FakeHttpServer server = new FakeHttpServer(8000);
         server.start();
         server.stub(urlEndingWith("/portfolio/0001"), aResponse()
                 .withHeader("Access-Control-Allow-Origin", "*")
                 .withBody("10999.99"));
        server.stub(urlEndingWith("/portfolio/0002"), aResponse()
                .withHeader("Access-Control-Allow-Origin", "*")
                .withStatus(404));
     }
 }
