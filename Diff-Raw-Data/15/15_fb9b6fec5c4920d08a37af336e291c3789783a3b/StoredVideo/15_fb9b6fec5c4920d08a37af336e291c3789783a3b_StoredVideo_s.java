 package net.kuehldesign.backuptube.app.common.stored;
 
 public class StoredVideo {
     private String publishedOn;
     private String description;
     private String downloadedOn;
     private String title;
     private StoredVideoSiteInfo siteInfo;
     private String category;
     private StoredVideoResponseInfo videoResponse;
     private String tags;
     private String uploader;
     private boolean hasBeenDeleted;
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getDownloadedOn() {
         return downloadedOn;
     }
 
     public void setDownloadedOn(String downloadedOn) {
         this.downloadedOn = downloadedOn;
     }
 
    public boolean isHasBeenDeleted() {
         return hasBeenDeleted;
     }
 
     public void setHasBeenDeleted(boolean hasBeenDeleted) {
         this.hasBeenDeleted = hasBeenDeleted;
     }
 
     public String getPublishedOn() {
         return publishedOn;
     }
 
     public void setPublishedOn(String publishedOn) {
         this.publishedOn = publishedOn;
     }
 
     public StoredVideoSiteInfo getSiteInfo() {
         return siteInfo;
     }
 
     public void setSiteInfo(StoredVideoSiteInfo siteInfo) {
         this.siteInfo = siteInfo;
     }
 
     public String getTags() {
         return tags;
     }
 
     public void setTags(String tags) {
         this.tags = tags;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getUploader() {
         return uploader;
     }
 
     public void setUploader(String uploader) {
         this.uploader = uploader;
     }
 
     public StoredVideoResponseInfo getVideoResponse() {
         return videoResponse;
     }
 
     public void setVideoResponse(StoredVideoResponseInfo videoResponse) {
         this.videoResponse = videoResponse;
     }
 
     public StoredVideo() {
 
     }
 }
