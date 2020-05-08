 package org.jboss.aerogear.blog;
 
 import org.yaml.snakeyaml.Yaml;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author edewit
  */
 public class Feeds {
  public static final String BLOGS_RESOURCE = "/blogs.yml";
   private String resourceName;
   private Yaml yaml = new Yaml();
 
   public Feeds() {
     resourceName = BLOGS_RESOURCE;
   }
 
   protected Feeds(String resourceName) {
     this.resourceName = resourceName;
   }
 
   @SuppressWarnings("unchecked")
   public List<Feed> getFeeds() {
     List<Map> feeds = (List<Map>) yaml.load(getClass().getResourceAsStream(resourceName));
 
     List<Feed> result = new ArrayList<>();
     for (Map<String, String> feed : feeds) {
       try {
         result.add(new Feed(feed.get("email"), feed.get("url")));
       } catch (MalformedURLException e) {
         throw new RuntimeException("malformed url in yaml resource file " + resourceName);
       }
     }
 
     return result;
   }
 
 
 }
