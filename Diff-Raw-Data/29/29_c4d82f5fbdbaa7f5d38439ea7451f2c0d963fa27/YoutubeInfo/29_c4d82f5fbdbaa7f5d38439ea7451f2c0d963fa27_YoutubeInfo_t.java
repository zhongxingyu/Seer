 package com.centretown.tuba.client.youtube.request;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import com.google.gwt.core.shared.GWT;
 
 public class YoutubeInfo {
 
   private Map<String, String> properties = new TreeMap<String, String>();
 
   private List<YoutubeVideoSource> sources = new ArrayList<YoutubeVideoSource>();
 
   private String videoId = "";
 
   private String title = "";
 
   private String thumbnail = "";
 
   private String poster = "";
 
   private String author = "";
 
   private Integer duration = 0;
 
   private Integer viewCount = 0;
 
   private String status = "";
 
   public YoutubeInfo(String response) {
     decode(response);
   }
 
   private void decode(String response) {
     DecodeUtils.decodeQueryString(response, properties);
 
     status = properties.get("status");
     if (status != null) {
       if (status.equalsIgnoreCase("fail")) {
         title = properties.get("reason");
         thumbnail = "images/tuba.png";// TODO add fail thumbnail
       } else if (status.equalsIgnoreCase("ok")) {
         title = properties.get("title");
         videoId = properties.get("video_id");
         author = properties.get("author");
 
         thumbnail = properties.get("thumbnail_url");
         poster = properties.get("iurlmaxres");
         if (poster == null)
           poster = properties.get("iurlsd");
         if (poster == null) {
           dumpProperties();
           poster = parseStoryboard(properties.get("storyboard_spec"));
         }
 
         if (poster == null)
           poster = thumbnail;
 
         try {
           duration = Integer.parseInt(properties.get("length_seconds"));
         } catch (Exception e) {
         }
         try {
           viewCount = Integer.parseInt(properties.get("view_count"));
         } catch (Exception e) {
         }
 
         sources.clear();
         String value = properties.get("url_encoded_fmt_stream_map");
         if (value != null)
           DecodeUtils.decodeStreams(this, value, sources);
       }
     }
   }
 
   private String parseStoryboard(String storyboard) {
     String url = null;
     String base = null;
     if (storyboard != null && storyboard.contains(".jpg")) {
       String options[] = storyboard.split("\\|");
       if (options.length > 0) {
         base = options[0];
         if (options.length > 1) {
           // best quality if more than one
           base = options[0].split("\\$")[0] + "2/M";
          String[] c = options[options.length-1].split("#");
           String sigh = c[c.length - 1];
           url = base + "0" + ".jpg?sigh=" + sigh;
           //
           // Integer quality = options.length - 2;
           // String[] parms = options[options.length - 1].split("#");
           // if (parms.length == 8) {
           // String sigh = parms[7];
           // base = base.replace("$L", quality.toString()).replace("$N",
           // parms[6]);
           // base = base.replace("$M", "0" + "?sigh=" + sigh);
           // GWT.log(base);
           // }
         }
       }
     }
     return url;
   }
 
   private void dumpProperties() {
     for (Entry<String, String> entry : properties.entrySet())
       GWT.log(entry.getKey() + "::>" + entry.getValue());
   }
 
   /**
    * @return the author
    */
   public String getAuthor() {
     return author;
   }
 
   /**
    * @return the duration
    */
   public Integer getDuration() {
     return duration;
   }
 
   /**
    * @return the poster
    */
   public String getPoster() {
     return poster;
   }
 
   /**
    * @return the video properties
    */
   public Map<String, String> getProperties() {
     return properties;
   }
 
   /**
    * @return the sources
    */
   public List<YoutubeVideoSource> getSources() {
     return sources;
   }
 
   /**
    * @return the status
    */
   public String getStatus() {
     return status;
   }
 
   /**
    * @return the thumbnail
    */
   public String getThumbnail() {
     return thumbnail;
   }
 
   /**
    * @return the title
    */
   public String getTitle() {
     return title;
   }
 
   /**
    * @return the videoId
    */
   public String getVideoId() {
     return videoId;
   }
 
   /**
    * @return the viewCount
    */
   public Integer getViewCount() {
     return viewCount;
   }
 }
