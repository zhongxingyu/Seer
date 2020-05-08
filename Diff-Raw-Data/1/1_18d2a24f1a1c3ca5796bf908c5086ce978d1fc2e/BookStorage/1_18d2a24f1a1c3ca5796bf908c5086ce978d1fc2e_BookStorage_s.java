 package edu.exigen.server.storage;
 
 import edu.exigen.client.entities.Book;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.List;
 
 /**
  * @author O. Tedikova
  * @version 1.0
  */
 
 @XmlRootElement(name = "bookStorage")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class BookStorage {
 
     @XmlElement(name = "book")
     private List<Book> books;
 
    @XmlElement(name = "reader")
     public List<Book> getElements() {
         return books;
     }
 
 
     public void setElements(List<Book> books) {
         this.books = books;
     }
 }
