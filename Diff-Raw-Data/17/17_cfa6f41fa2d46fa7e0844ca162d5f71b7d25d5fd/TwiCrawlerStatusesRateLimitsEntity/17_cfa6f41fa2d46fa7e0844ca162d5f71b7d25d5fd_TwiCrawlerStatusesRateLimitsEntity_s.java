 package crawlers.twi;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
/**
 * Created by pavel on 09/12/13.
 */
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class TwiCrawlerStatusesRateLimitsEntity {
     public static class RateEntity {
         public Long remaining;
         public Long reset;
         public Long limit;
     }
 
     @JsonProperty("/statuses/show/:id")
     public RateEntity requests_limit;
 }
