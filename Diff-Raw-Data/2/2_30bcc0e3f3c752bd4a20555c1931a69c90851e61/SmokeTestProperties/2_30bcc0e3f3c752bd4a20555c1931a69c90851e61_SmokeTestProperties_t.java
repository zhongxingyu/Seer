 package org.openmrs.module.mirebalais.smoke.helper;
 
 public class SmokeTestProperties {
 
    public static final int IMPLICIT_WAIT_TIME = 5;

     public String getWebAppUrl() {
         return envOrDefault("WEBAPP_URL", "http://localhost:8080/openmrs");
     }
 
     public String getDatabaseUrl() {
         return envOrDefault("DATABASE_URL", "jdbc:mysql://localhost:3306/openmrs");
     }
 
     public String getDatabaseUsername() {
         return envOrDefault("DATABASE_USERNAME", "openmrs");
     }
 
     public String getDatabasePassword() {
         return envOrDefault("DATABASE_PASSWORD", "openmrs");
     }
 
     public String getDatabaseDriverClass() {
         return "com.mysql.jdbc.Driver";
     }
 
     private String envOrDefault(String environmentVariable, String defaultValue) {
         return System.getenv(environmentVariable) != null ? System.getenv(environmentVariable) : defaultValue;
     }
 }
