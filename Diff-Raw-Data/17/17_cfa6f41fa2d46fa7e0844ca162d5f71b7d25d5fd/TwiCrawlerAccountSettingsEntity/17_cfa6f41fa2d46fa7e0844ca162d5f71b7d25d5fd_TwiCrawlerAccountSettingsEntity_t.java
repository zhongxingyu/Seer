 package crawlers.twi;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import java.util.ArrayList;
 
@JsonIgnoreProperties(ignoreUnknown = true)
 public class TwiCrawlerAccountSettingsEntity {
     public static class SleepTimeInfo {
         public Boolean enabled;
         public String end_time;
         public String start_time;
     }
 
     public static class TimeZoneInfo {
         public String name;
         public String tzinfo_name;
         public Integer utc_offset;
     }
 
     public static class TrendLocationInfo {
         public static class PlaceTypeInfo {
             public Integer code;
             public String name;
         }
 
         public String country;
         public String countryCode;
         public String name;
         public Integer parent_id;
         public PlaceTypeInfo place_type;
         public String url;
         public Integer woeid;
     }
 
     public Boolean always_use_https;
     public Boolean discoverable_by_email;
     public Boolean geo_enabled;
     public String language;
 
     @JsonProperty("protected")
     public Boolean is_protected;
 
     public String screen_name;
     public Boolean show_all_inline_media;
     public SleepTimeInfo sleep_time;
     public TimeZoneInfo time_zone;
     public ArrayList<TrendLocationInfo> trend_location;
     public Boolean use_cookie_personalization;
     public Boolean discoverable_by_mobile_phone;
     public Boolean display_sensitive_media;
 
 //    public Boolean getProtected() { return m_protected; }
 //    public void setM_protected(Boolean is_protected) { m_protected = is_protected; }
 }
