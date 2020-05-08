 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.domain;
 import java.io.Serializable;
 import javax.persistence.*;
 
 @Entity
 public class Viite implements Serializable {
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long        id;
     private String      referenceId;
     private String      author;
     private String      title;
     private String      bookTitle;
     private String      itemYear;
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getReferenceId() {
         return referenceId;
     }
 
     public void setReferenceId(String referenceId) {
         this.referenceId = referenceId;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getBookTitle() {
         return bookTitle;
     }
 
     public void setBookTitle(String bookTitle) {
         this.bookTitle = bookTitle;
     }
 
     public String getItemYear() {
         return itemYear;
     }
 
     public void setItemYear(String year) {
         this.itemYear = year;
     }
     
     public String toStringBiBTex() {
         String bibtex = "@inproceedings{";
        bibtex += this.viiteId + ",\n";
         bibtex += "author = {" + bibtexCharReplace(this.author) + "},\n";
         bibtex += "title = {" + bibtexCharReplace(this.title) + "},\n";
         bibtex += "booktitle = {" + bibtexCharReplace(this.bookTitle) + "},\n";
         bibtex += "year = {" + bibtexCharReplace(this.itemYear) + "},\n";
         bibtex += "}\n";
         return bibtex;
     }
 
     private String bibtexCharReplace(String s) {
         s = s.replace("ä", "\\" + "\"" + "{a}");
         s = s.replace("ö", "\\" + "\"" + "{o}");
         s = s.replace("Ä", "\\" + "\"" + "{A}");
         s = s.replace("Ö", "\\" + "\"" + "{O}");
         s = s.replace("å", "\\" + "aa");
         s = s.replace("Å", "\\" + "AA");
         return s;
     }
 }
