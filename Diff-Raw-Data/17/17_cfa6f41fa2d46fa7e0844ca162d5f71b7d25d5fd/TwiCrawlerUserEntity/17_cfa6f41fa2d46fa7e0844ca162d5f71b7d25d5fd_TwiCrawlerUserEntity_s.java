 package crawlers.twi;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import java.util.ArrayList;
 
/**
 * Created by pavel on 08/12/13.
 */
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class TwiCrawlerUserEntity {
     public Boolean contributors_enabled;
     public String created_at;
     public Boolean default_profile;
     public Boolean default_profile_image;
     public String description;
     public Integer favourites_count;
     public Integer followers_count;
     public Integer friends_count;
     public Boolean geo_enabled;
     public Long id;
     public String id_str;
     public Boolean is_translator;
     public String lang;
     public Integer listed_count;
     public String location;
     public String name;
     public Boolean notifications;
     public String profile_background_color;
     public String profile_background_image_url;
     public String profile_background_image_url_https;
     public Boolean profile_background_tile;
     public String profile_banner_url;
     public String profile_image_url;
     public String profile_image_url_https;
     public String profile_link_color;
     public String profile_sidebar_border_color;
     public String profile_sidebar_fill_color;
     public String profile_text_color;
     public Boolean profile_use_background_image;
 
     @JsonProperty("protected")
     public Boolean is_protected;
 
     public String screen_name;
     public Boolean show_all_inline_media;
     public TwiCrawlerGetTimelineTweet status;
     public Integer statuses_count;
     public String time_zone;
     public String url;
     public Integer utc_offset;
     public Boolean verified;
     public String withheld_in_countries;
     public String withheld_scope;
 }
