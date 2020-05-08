 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.entities;
 
 import java.io.Serializable;
 
 /**
  *
  * @author Nospherus
  */
 
 public class LibraryItem implements Serializable {
     
     private Long id;
     
     String title;
     
     String alternateTitle;
     
     String author;
     
     String coAuthor;
     
     String published;
     
     String uploaded;
 
     String pageNo;
 
     String summary;
 
     String iconURL;
 
     String thumbURL;
 
    String URL;
 
     public LibraryItem() {
     }
 
     public LibraryItem(String title, String alternateTitle, String author, String coAuthor, String published, String uploaded, String pageNo, String summary, String iconURL, String thumbURL, String URL) {
         this.title = title;
         this.alternateTitle = alternateTitle;
         this.author = author;
         this.coAuthor = coAuthor;
         this.published = published;
         this.uploaded = uploaded;
         this.pageNo = pageNo;
         this.summary = summary;
         this.iconURL = iconURL;
         this.thumbURL = thumbURL;
        this.URL = URL;
     }
     
     public LibraryItem(String title, String summary){
         this.title = title;
         this.summary = summary;
     }
 
     public String getURL() {
        return URL;
     }
 
     public void setURL(String URL) {
        this.URL = URL;
     }
 
     public String getAlternateTitle() {
         return alternateTitle;
     }
 
     public void setAlternateTitle(String alternateTitle) {
         this.alternateTitle = alternateTitle;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getCoAuthor() {
         return coAuthor;
     }
 
     public void setCoAuthor(String coAuthor) {
         this.coAuthor = coAuthor;
     }
 
     public String getIconURL() {
         return iconURL;
     }
 
     public void setIconURL(String iconURL) {
         this.iconURL = iconURL;
     }
 
     public String getPageNo() {
         return pageNo;
     }
 
     public void setPageNo(String pageNo) {
         this.pageNo = pageNo;
     }
 
     public String getPublished() {
         return published;
     }
 
     public void setPublished(String published) {
         this.published = published;
     }
 
     public String getSummary() {
         return summary;
     }
 
     public void setSummary(String summary) {
         this.summary = summary;
     }
 
     public String getThumbURL() {
         return thumbURL;
     }
 
     public void setThumbURL(String thumbURL) {
         this.thumbURL = thumbURL;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getUploaded() {
         return uploaded;
     }
 
     public void setUploaded(String uploaded) {
         this.uploaded = uploaded;
     }
     
 }
