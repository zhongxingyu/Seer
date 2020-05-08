 package org.motechproject.care.reporting.migration.util;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 @Component
 public class MotechAPIHttpClient {
 
    private static final Logger logger = LoggerFactory.getLogger(CommcareAPIHttpClient.class);
 
     private HttpClient httpClient;
     private final Properties platformProperties;
 
     @Autowired
     public MotechAPIHttpClient(@Qualifier("motechHttpClient") HttpClient httpClient, @Qualifier("platformProperties") Properties platformProperties) {
         this.httpClient = httpClient;
         this.platformProperties = platformProperties;
     }
 
     public void postForm(String form) {
         postContet(form, new PostMethod(getFormUpdateUrl()));
     }
 
     public void postCase(String aCase) {
         postContet(aCase, new PostMethod(getCaseUpdateUrl()));
     }
 
     void postContet(String form, PostMethod postMethod) {
         try {
             postMethod.setRequestEntity(new StringRequestEntity(form, "text/xml; charset=UTF-8", "UTF-8"));
             httpClient.executeMethod(postMethod);
 
             int statusCode = postMethod.getStatusCode();
 
             String response = readResponse(postMethod);
 
             if (statusCode != HttpStatus.SC_OK) {
                 BadResponseException e = new BadResponseException(postMethod.getURI().toString(), statusCode, response);
                 logger.error(e.getMessage(), e);
                 throw e;
             }
         } catch (IOException e) {
             logger.error("IO exception while sending request to motech", e);
             throw new RuntimeException(e);
         }
     }
 
     private String readResponse(PostMethod postMethod) throws IOException {
         InputStream responseStream = postMethod.getResponseBodyAsStream();
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseStream));
         StringBuffer sb = new StringBuffer();
         String line;
         while ((line = bufferedReader.readLine()) != null) {
             sb.append(line);
         }
         return sb.toString();
     }
 
     private String getFormUpdateUrl() {
         return String.format("%s/%s", platformProperties.getProperty("app.url"), platformProperties.getProperty("app.form.endpoint"));
     }
 
     private String getCaseUpdateUrl() {
         return String.format("%s/%s", platformProperties.getProperty("app.url"), platformProperties.getProperty("app.case.endpoint"));
     }
 }
