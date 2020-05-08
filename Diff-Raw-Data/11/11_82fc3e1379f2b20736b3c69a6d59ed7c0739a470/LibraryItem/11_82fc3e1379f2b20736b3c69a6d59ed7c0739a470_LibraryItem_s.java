 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.domain;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 import no.hials.muldvarpweb.fragments.Fragment;
 
 /**
  *
  * @author Nospherus
  */
 @Entity
 @Table (name="Library")
 @XmlRootElement
 public class LibraryItem implements Serializable {
     
     @Id
     @GeneratedValue
     @Column (name = "id")
     Long id;
     
     @Column(name="title")
     String title;
     
     @Column(name="alternateTitle")
     String alternateTitle;
     
     @Column(name="author")
     String author;
     
     @Column(name="coAuthor")
     String coAuthor;
     
     @Column(name="published")
     String published;
     
     @Column(name="uploaded")
     String uploaded;
     
     @Column(name="pageNo")
     String pageNo;
     
     @Column(name="summary")
     String summary;
     
     @Column(name="iconURL")
     String iconURL;
     
     @Column(name="thumbURL")
     String thumbURL;
     
     @Column(name="URL")
     String URL;
     
     @ManyToMany(mappedBy = "documents")
     List<Fragment> fragments;
 
     public LibraryItem() {
         Calendar cal = new GregorianCalendar();
         int d = cal.get(Calendar.DATE);
         int m = cal.get(Calendar.MONTH);
         int y = cal.get(Calendar.YEAR);
         setUploaded(d + "." + m + "." + y);
     }
 
     public LibraryItem(String title, String alternateTitle, String author, String coAuthor, String published, String pageNo, String summary, String iconURL, String thumbURL, String URL) {
         this.title = title;
         this.alternateTitle = alternateTitle;
         this.author = author;
         this.coAuthor = coAuthor;
         this.published = published;
         this.pageNo = pageNo;
         this.summary = summary;
         this.iconURL = iconURL;
         this.thumbURL = thumbURL;
         this.URL = URL;
         Calendar cal = new GregorianCalendar();
         int d = cal.get(Calendar.DATE);
         int m = cal.get(Calendar.MONTH);
         int y = cal.get(Calendar.YEAR);
         setUploaded(d + "." + m + "." + y);
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
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public List<Fragment> getFragments() {
         return fragments;
     }
 
     public void setFragments(List<Fragment> fragments) {
         this.fragments = fragments;
     }
     
 }
