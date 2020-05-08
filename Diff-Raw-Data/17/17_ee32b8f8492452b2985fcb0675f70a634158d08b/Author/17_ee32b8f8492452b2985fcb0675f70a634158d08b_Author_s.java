 package Chapter.Four.pojo;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 //Title: Pro Hibernate 3
 //Authors: Dave Minter, Jeff Linwood
 //Chapter 4 : Using Annotations with Hibernate
 //Page 79 & 80
 
 @Entity
 public class Author {
 
   @ManyToMany(mappedBy = "authors")
   public Set<Book> getBooks() {
     return books;
   }
 
  protected Set<Book> books = new HashSet<Book>();

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
 
   protected String email;
 
   public String getEmail() {
     return email;
   }
 
   public void setEmail(String email) {
     this.email = email;
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
