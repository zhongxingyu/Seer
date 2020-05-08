 package model;
 
 public class Picture {
 
     private String thumbUrl;
     private String largeUrl;
     private String tag;
     private String id;
    private String unixDate;
 
     public Picture(String url, String thumb) {
         this.largeUrl = url;
         this.thumbUrl = thumb;
     }
 
     /** 
      * @param thumbUrl the thumbUrl to set
      */
     public void setThumbUrl(String thumbUrl) {
         this.thumbUrl = thumbUrl;
     }
 
     /**
      * @param largeUrl the largeUrl to set
      */
     public void setLargeUrl(String largeUrl) {
         this.largeUrl = largeUrl;
     }
 
     /**
      * @return the thumbUrl
      */
     public String getThumbUrl() {
         return thumbUrl;
     }
 
     /**
      * @return the largeUrl
      */
     public String getLargeUrl() {
         return largeUrl;
     }
 
     /**
      * @return the tag
      */
     public String getTag() {
         return tag;
     }
 
     /**
      * @param tag the tag to set
      */
     public void setTag(String tag) {
         this.tag = tag;
     }
 
     /**
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**
      * @return the unixDate
      */
    public String getUnixDate() {
         return unixDate;
     }
 
     /**
      * @param unixDate the unixDate to set
      */
    public void setUnixDate(String unixDate) {
         this.unixDate = unixDate;
     }
     
     public boolean equals(Picture pic){
         return pic.getLargeUrl().equals(this.getLargeUrl());
     }
 
 }
