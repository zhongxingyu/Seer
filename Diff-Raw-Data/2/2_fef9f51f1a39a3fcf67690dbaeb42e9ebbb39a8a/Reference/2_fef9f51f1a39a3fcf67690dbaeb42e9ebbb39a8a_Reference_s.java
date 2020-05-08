 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.domain;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 
 /**
  *
  * @author tonykova
  */
 
 @Entity
 public class Reference implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private Long id;
     private ReferenceType type;
    @Pattern(regexp="[a-zA-Z'-åöäüÅÄÖÜ ]+", message="There has to be an author and it should contain only letters a-zA-Z'-åöäüÅÄÖÜ")
     private String author;
     @NotNull
     @Pattern(regexp="[^<>$%]+", message="There should be a title and it should not contain <>$%")
     private String title;
     @Pattern(regexp="([0-9]{4})?", message="Year should contain exactly 4 numbers")
     private String publishingYear;
     @Pattern(regexp="[^<>$%]*", message="Publisher should not contain <>$%")
     private String publisher;
     @Pattern(regexp="([0-9]+-{1}[0-9]+)?", message="Pages should consist of a number, a line and a number")
     private String pages;
     @Pattern(regexp="[^<>$%]*", message="Address should not contain <>$%")
     private String address;
     @Pattern(regexp="[^<>$%]*", message="Booktitle should not contain <>$%")
     private String booktitle;
     @Pattern(regexp="[^<>$%]*", message="Journal should not contain <>$%")
     private String journal;
     
     private int volume;
     private int number;
     
     public boolean equals(Object o) {
         if (o instanceof Reference) {
             Reference r = (Reference) o;
             if (r.getId() == this.id) {
                 return true;
             }
             return false;
         }
         return false;
     }
     
     public Reference() {
         
     }
     
     public void setAddress(String address) {
         this.address = address;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public void setBooktitle(String booktitle) {
         this.booktitle = booktitle;
     }
 
     public void setJournal(String journal) {
         this.journal = journal;
     }
 
     public void setNumber(int number) {
         this.number = number;
     }
 
     public void setPages(String pages) {
         this.pages = pages;
     }
 
     public void setPublisher(String publisher) {
         this.publisher = publisher;
     }
 
     public void setPublishingYear(String publishingYear) {
         this.publishingYear = publishingYear;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setType(ReferenceType type) {
         this.type = type;
     }
 
     public void setVolume(int volume) {
         this.volume = volume;
     }
 
     public String getAddress() {
         return address;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public String getBooktitle() {
         return booktitle;
     }
 
     public String getJournal() {
         return journal;
     }
 
     public int getNumber() {
         return number;
     }
 
     public String getPages() {
         return pages;
     }
 
     public String getPublisher() {
         return publisher;
     }
 
     public String getPublishingYear() {
         return publishingYear;
     }
 
     public String getTitle() {
         return title;
     }
 
     public ReferenceType getType() {
         return type;
     }
 
     public int getVolume() {
         return volume;
     }
     
     
     public void setId(Long id) {
         this.id = id;
     }
 
    
 
     public Long getId() {
         return id;
     }
 
 }
