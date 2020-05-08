 package org.openmrs.module.mirebalais.smoke.helper;
 
 public class SmokeTestProperties {
 
     public String getWebAppUrl() {
        return envOrDefault("WEBAPP_URL", "http://localhost:8080/openmrs_1.9");
        //return envOrDefault("WEBAPP_URL", "http://bamboo.pih-emr.org:8080/mirebalais");
     }
 
     public String getDatabaseUrl() {
        return envOrDefault("DATABASE_URL", "jdbc:mysql://localhost:3306/openmrs_1_9");
        //return envOrDefault("DATABASE_URL", "jdbc:mysql://bamboo.pih-emr.org:3306/openmrs");
     }
 
     public String getDatabaseUsername() {
        return envOrDefault("DATABASE_USERNAME", "openmrs_1_9_user");
        //return envOrDefault("DATABASE_USERNAME", "openmrs_user");
     }
 
     public String getDatabasePassword() {
        return envOrDefault("DATABASE_PASSWORD", "c65Ln9e^.di1");
        //return envOrDefault("DATABASE_PASSWORD", "c65Ln9e^.di1");
     }
 
     public String getDatabaseDriverClass() {
         return "com.mysql.jdbc.Driver";
     }
 
     private String envOrDefault(String environmentVariable, String defaultValue) {
         return System.getenv(environmentVariable) != null ? System.getenv(environmentVariable) : defaultValue;
     }
 }
