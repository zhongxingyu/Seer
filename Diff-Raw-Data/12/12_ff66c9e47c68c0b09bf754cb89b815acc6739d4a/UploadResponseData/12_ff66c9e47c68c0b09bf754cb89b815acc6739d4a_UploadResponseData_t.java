 package com.bullyapp.imgur;
 
 public class UploadResponseData {
 
     private String id;
     private String  title;
     private String description;
     private Long datetime;
     private String type;
     private Boolean animated;
     private Long width;
     private Long height;
     private Long size;
     private Integer views;
     private Long bandwidth;
     private Boolean favorite;
     private String nsfw;
     private String section;
    private String deletehash;
     private String link;
     
     public String getId() {
         return id;
     }
     public void setId(String id) {
         this.id = id;
     }
     public String getTitle() {
         return title;
     }
     public void setTitle(String title) {
         this.title = title;
     }
     public String getDescription() {
         return description;
     }
     public void setDescription(String description) {
         this.description = description;
     }
     public Long getDatetime() {
         return datetime;
     }
     public void setDatetime(Long datetime) {
         this.datetime = datetime;
     }
     public String getType() {
         return type;
     }
     public void setType(String type) {
         this.type = type;
     }
     public Boolean getAnimated() {
         return animated;
     }
     public void setAnimated(Boolean animated) {
         this.animated = animated;
     }
     public Long getWidth() {
         return width;
     }
     public void setWidth(Long width) {
         this.width = width;
     }
     public Long getHeight() {
         return height;
     }
     public void setHeight(Long height) {
         this.height = height;
     }
     public Long getSize() {
         return size;
     }
     public void setSize(Long size) {
         this.size = size;
     }
     public Integer getViews() {
         return views;
     }
     public void setViews(Integer views) {
         this.views = views;
     }
     public Long getBandwidth() {
         return bandwidth;
     }
     public void setBandwidth(Long bandwidth) {
         this.bandwidth = bandwidth;
     }
     public Boolean getFavorite() {
         return favorite;
     }
     public void setFavorite(Boolean favorite) {
         this.favorite = favorite;
     }
     public String getNsfw() {
         return nsfw;
     }
     public void setNsfw(String nsfw) {
         this.nsfw = nsfw;
     }
     public String getSection() {
         return section;
     }
     public void setSection(String section) {
         this.section = section;
     }
    public String getDeletehash() {
        return deletehash;
     }
    public void setDeleteHash(String deletehash) {
        this.deletehash = deletehash;
     }
     public String getLink() {
         return link;
     }
     public void setLink(String link) {
         this.link = link;
     }
     
 }
