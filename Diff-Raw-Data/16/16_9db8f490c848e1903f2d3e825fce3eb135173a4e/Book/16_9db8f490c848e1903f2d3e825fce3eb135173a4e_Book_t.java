 package edu.sjsu.cmpe.library.domain;
 import java.util.Date;
 import java.util.List;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 public class Book {
     private long isbn;
     private String title;
    @JsonProperty("publication-date") 
     private String pubDate;
     private String language;
     @JsonProperty("num-pages")
     private int numPages;
     private String status = "available";
     @JsonProperty("authors")
     private List<Author> author;
     /*add more fields here
     ISBN (Key) # You will generate this key.
    Title (Required field)
  Publication Date (Required field)
 Language (Optional field)
 Number of Pages (Optional field)
 Status {available, checked-out, in-queue, or lost} # default to ‘available’
      */
     /**
      * @return the isbn
      */
     public long getIsbn() {
 	return isbn;
     }
 
     /**
      * @param isbn
      *            the isbn to set
      */
     public void setIsbn(long isbn) {
 	this.isbn = isbn;
     }
 
     /**
      * @return the title
      */
     public String getTitle() {
 	return title;
     }
 
     /**
      * @param title
      *            the title to set
      */
     public void setTitle(String title) {
 	this.title = title;
     }
     
     public String getPublicationDate() {
     	return pubDate;
         }
     
     public void setPublicationDate(String date) {
     	this.pubDate = date;
         }
     
     public String getLanguage() {
     	return language;
         }
 
         /**
          * @param title
          *            the title to set
          */
         public void setLanguage(String language) {
     	this.language = language;
         }
         public int getNumPages() {
         	return numPages;
             }
 
             /**
              * @param isbn
              *            the isbn to set
              */
             public void setNumPages(int numPages) {
         	this.numPages = numPages;
             }
             
             public String getStatus() {
             	return status;
                 }
 
                 /**
                  * @param title
                  *            the title to set
                  */
                 public void setStatus(String status) {
             	this.status = status;
                 }
 
 				public List<Author> getAuthor() {
 					return author;
 				}
 
 				public void setAuthor(List<Author> author) {
 					this.author = author;
 				}
 }
