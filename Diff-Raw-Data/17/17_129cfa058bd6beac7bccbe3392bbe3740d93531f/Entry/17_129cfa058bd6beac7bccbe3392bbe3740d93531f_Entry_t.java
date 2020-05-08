 package org.ict4h.atomfeed.spring.resource;
 
 public class Entry {
     String title;
     private String content;
     private String url;
    private String category;
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getContent() {
         return this.content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public String getUrl() {
         return this.url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
 }
