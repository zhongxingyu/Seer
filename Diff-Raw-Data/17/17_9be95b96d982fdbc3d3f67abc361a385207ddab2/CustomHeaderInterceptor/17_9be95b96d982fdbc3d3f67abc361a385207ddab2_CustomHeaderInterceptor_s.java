 
 package com.github.arekolek.knurek.sync;
 
 import com.googlecode.androidannotations.annotations.EBean;
 
 import org.springframework.http.HttpRequest;
 import org.springframework.http.client.ClientHttpRequestExecution;
 import org.springframework.http.client.ClientHttpRequestInterceptor;
 import org.springframework.http.client.ClientHttpResponse;
 
 import java.io.IOException;
 
 @EBean
 public class CustomHeaderInterceptor implements ClientHttpRequestInterceptor {
 
     private String identifier;
 
     @Override
     public ClientHttpResponse intercept(HttpRequest request, byte[] data,
             ClientHttpRequestExecution execution) throws IOException {
         if (identifier != null) {
             request.getHeaders().add("Identifier", identifier);
         }
         return execution.execute(request, data);
     }
 
     public void setIdentifier(String identifier) {
         this.identifier = identifier;
     }
 
 }
