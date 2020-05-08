 package org.motechproject.care.reporting.ft.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import static java.lang.String.format;
 
 public class TestEnvironment {
     private final Properties properties;
 
     public TestEnvironment() {
         properties = new Properties();
         InputStream resourceAsStream = getClass().getResourceAsStream(format("/environment/%s/test.properties", getEnv()));
         try {
             properties.load(resourceAsStream);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public String getProperty(String key) {
         return properties.getProperty(key);
     }
 
     private String getEnv() {
         String envFromSystem = System.getenv().get("TEST_ENV");
         return envFromSystem != null ? envFromSystem : "local";
     }
 

     public String getDbConnectionString() {
         return format("jdbc:postgresql://%s:%s/%s",
                 properties.getProperty("postgres.host"),
                 properties.getProperty("postgres.port"),
                 properties.getProperty("postgres.dbname"));
     }
 
     public String getFormUpdateEnpoint() {
         return String.format("%s/%s", properties.getProperty("app.baseurl"), properties.getProperty("app.update.endpoint.form"));
     }
 
     public String getCaseUpdateEnpoint() {
         return String.format("%s/%s", properties.getProperty("app.baseurl"), properties.getProperty("app.update.endpoint.case"));
     }
 
     public String updateFakeTimeEndPoint() {
         return String.format("%s/%s", properties.getProperty("app.baseurl"), properties.getProperty("app.update.endpoint.faketime"));
     }
 
     public String getFakeTimeEndPoint() {
         return String.format("%s/%s", properties.getProperty("app.baseurl"), properties.getProperty("app.get.endpoint.faketime"));
     }
 }
