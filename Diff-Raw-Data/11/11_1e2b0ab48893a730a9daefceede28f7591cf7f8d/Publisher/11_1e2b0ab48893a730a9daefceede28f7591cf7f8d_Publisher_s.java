 package Chapter.Four.pojo;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 //Title: Pro Hibernate 3
 //Authors: Dave Minter, Jeff Linwood
 //Chapter 4 : Using Annotations with Hibernate
 //Page 73
 
 @Entity
 public class Publisher {
   protected Set<Book> books = new HashSet<Book>();
 
   @OneToMany(mappedBy = "publisher")
   public Set<Book> getBooks() {
     return books;
   }
 
   public void setBooks(Set<Book> books) {
     this.books = books;
   }
 
   protected String name;
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   protected int id;
 
  @Id(generate = GeneratorType.AUTO)
   public int getId() {
     return id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 }
