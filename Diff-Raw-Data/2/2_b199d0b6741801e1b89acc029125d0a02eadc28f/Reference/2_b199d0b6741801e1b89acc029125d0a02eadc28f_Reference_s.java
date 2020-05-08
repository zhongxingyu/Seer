 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.missingfeatures.bibtext.models;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class Reference implements Serializable {
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     private List<String> authors;
     
     private String title;
     
     @Column(name="pubyear")
     private int year;
     
     private String bibtextID;
     private String type;
     private String booktitle;
     private String publisher;
     private String pages;
     private String address;
     private String journal;
     private int volume;
     private int number;
 
     public Reference() {}
 
    public Reference(String[] author, String title, int year, String bibtextID, 
             String type, String booktitle, String publisher, String pages, 
             String address, String journal, int volume, int number) {
         
         this.authors = new ArrayList<String>();
         this.authors.add(author);
         this.title = title;
         this.year = year;
         this.bibtextID = bibtextID;
         this.type = type;
         this.booktitle = booktitle;
         this.publisher = publisher;
         this.pages = pages;
         this.address = address;
         this.journal = journal;
         this.volume = volume;
         this.number = number;
     }
     
 
     
     /**
      * @return the id
      */
     public Long getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
     
     public void setType(String type){
         this.type=type;
     }
     
     public String getType(){
         return type;
     }
 
     /**
      * @return the author
      */
     public List<String> getAuthors() {
         return authors;
     }
 
     public void setAuthors(List<String> authors) {
         this.authors = authors;
     }
     
     public void addAuthor(String author) {
         this.authors.add(type);
     }
     
     public void deleteAuthor(String author) {
         this.authors.remove(author);
     }
 
     /**
      * @return the title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title the title to set
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * @return the booktitle
      */
     public String getBooktitle() {
         return booktitle;
     }
 
     /**
      * @param booktitle the booktitle to set
      */
     public void setBooktitle(String booktitle) {
         this.booktitle = booktitle;
     }
 
     /**
      * @return the publisher
      */
     public String getPublisher() {
         return publisher;
     }
 
     /**
      * @param publisher the publisher to set
      */
     public void setPublisher(String publisher) {
         this.publisher = publisher;
     }
 
     /**
      * @return the pubYear
      */
     public int getYear() {
         return year;
     }
 
     /**
      * @param pubYear the pubYear to set
      */
     public void setYear(int year) {
         this.year = year;
     }
 
     /**
      * @return the bibtextID
      */
     public String getBibtextID() {
         return bibtextID;
     }
 
     /**
      * @param bibtextID the bibtextID to set
      */
     public void setBibtextID(String bibtextID) {
         this.bibtextID = bibtextID;
     }
 
     /**
      * @return the pages
      */
     public String getPages() {
         return pages;
     }
 
     /**
      * @param pages the pages to set
      */
     public void setPages(String pages) {
         this.pages = pages;
     }
 
     /**
      * @return the address
      */
     public String getAddress() {
         return address;
     }
 
     /**
      * @param address the address to set
      */
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getJournal() {
         return journal;
     }
 
     public void setJournal(String journal) {
         this.journal = journal;
     }
 
     public int getVolume() {
         return volume;
     }
 
     public void setVolume(int volume) {
         this.volume = volume;
     }
 
     public int getNumber() {
         return number;
     }
 
     public void setNumber(int number) {
         this.number = number;
     }
  
 }
