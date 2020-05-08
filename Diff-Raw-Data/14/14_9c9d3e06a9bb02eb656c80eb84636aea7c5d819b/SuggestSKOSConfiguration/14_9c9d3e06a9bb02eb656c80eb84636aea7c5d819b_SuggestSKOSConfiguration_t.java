 package pt.unl.fct.di.suggestskos;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import com.yammer.dropwizard.config.Configuration;
 
 public class SuggestSKOSConfiguration extends Configuration {
   @NotEmpty
   @JsonProperty
   private String fileName;
 
   @JsonProperty
   private String languages;
   
   public String getLanguages() {
     return languages;
   }
   
   public String getFileName() {
     return fileName;
   }
 }
