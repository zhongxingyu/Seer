 package com.topsy.jmxproxy;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.HttpConfiguration;
 import com.yammer.dropwizard.json.JsonSnakeCase;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 public class JMXProxyConfiguration extends Configuration {
     @JsonSnakeCase
     public static class JMXProxyServiceConfiguration {
         @Min(1)
         @JsonProperty
         private int cleanInterval = 1;
 
         @Min(0)
         @JsonProperty
         private int cacheDuration = 5;
 
         @Min(1)
         @JsonProperty
         private int accessDuration = 30;
 
         @JsonProperty
         private List<String> allowedEndpoints = new ArrayList<String>();
 
         public int getCleanInterval() {
             return cleanInterval;
         }
         public void setCleanInterval(int cleanInterval) {
             this.cleanInterval = cleanInterval;
         }
 
         public int getCacheDuration() {
             return cacheDuration;
         }
         public void setCacheDuration(int cacheDuration) {
             this.cacheDuration = cacheDuration;
         }
 
         public int getAccessDuration() {
             return accessDuration;
         }
         public void setAccessDuration(int accessDuration) {
             this.accessDuration = accessDuration;
         }
 
         public List<String> getAllowedEndpoints() {
             return allowedEndpoints;
         }
         public void setAllowedEndpoints(List<String> allowedEndpoints) {
             this.allowedEndpoints = allowedEndpoints;
         }
     }
 
    public static class JMXProxyHttpConfiguration extends HttpConfiguration {
        @NotNull
        @JsonProperty
        private String rootPath = "/jmxproxy/*";

        @Override
        public String getRootPath() {
            return rootPath;
        }
        @Override
        public void setRootPath(String path) {
            this.rootPath = path;
        }
    }

    @Valid
    @NotNull
    @JsonProperty(value="http")
    private JMXProxyHttpConfiguration httpConfig = new JMXProxyHttpConfiguration();

     @Valid
     @NotNull
     @JsonProperty(value="jmxproxy")
     private JMXProxyServiceConfiguration serviceConfig = new JMXProxyServiceConfiguration();
 
    public JMXProxyHttpConfiguration getHttpConfiguration() {
        return httpConfig;
    }
    public void setHttpConfiguration(JMXProxyHttpConfiguration httpConfig) {
        this.httpConfig = httpConfig;
    }

     public JMXProxyServiceConfiguration getServiceConfiguration() {
         return serviceConfig;
     }
     public void setServiceConfiguration(JMXProxyServiceConfiguration serviceConfig) {
         this.serviceConfig = serviceConfig;
     }
 }
