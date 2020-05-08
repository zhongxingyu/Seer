 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nl.sogeti.jdc.demo.jee6.banking.config;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.InjectionPoint;
 
 /**
  * @author kanteriv
  */
 @Startup
 @Singleton
 public class Configuration {
 
    private Map<String, String> configuration;
    private Set<String> unconfiguredFields;
 
    @PostConstruct
    public void fetchConfiguration() {
 
       // default values...
       this.configuration = new HashMap<String, String>();
       this.configuration.put("numberOfRows", "10");
 
       this.unconfiguredFields = new HashSet<String>();
       mergeWithCustomConfiguration();
    }
 
    private void mergeWithCustomConfiguration() {
       // TODO Auto-generated method stub
    }
 
    @Produces
    public String getString(InjectionPoint point) {
 
       String fieldName = point.getMember().getName();
       String valueForFieldName = this.configuration.get(fieldName);
       if (valueForFieldName == null) {
          this.unconfiguredFields.add(fieldName);
       }
       return valueForFieldName;
    }
 
    @Produces
    public int getInteger(InjectionPoint point) {
 
       String stringValue = getString(point);
       if (stringValue == null) {
          return 0;
       }
       return Integer.parseInt(stringValue);
    }
 
 }
