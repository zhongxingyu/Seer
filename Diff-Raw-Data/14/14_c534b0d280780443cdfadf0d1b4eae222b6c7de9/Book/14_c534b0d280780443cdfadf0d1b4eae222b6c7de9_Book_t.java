 package Chapter.Four.pojo;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 
import org.hibernate.annotations.AccessType;

 //Title: Pro Hibernate 3
 //Authors: Dave Minter, Jeff Linwood
 //Chapter 4 : Using Annotations with Hibernate
 //Page 68, 77 - 79
 
@Entity
@AccessType("property")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 public class Book {
   protected int pages;
 
   public int getPages() {
     return pages;
   }
 
   public void setPages(int pages) {
     this.pages = pages;
   }
 
   protected String title;
 
   @Column(name = "working_title", length = 200, nullable = false)
   public String getTitle() {
     return title;
   }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   protected int id;
 
   @Id
   @GeneratedValue
   public int getId() {
     return id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 
   protected Date publicationDate;
 
   @Transient
   public Date getPublicationDate() {
     return publicationDate;
   }
 
   public void setPublicationDate(Date publicationDate) {
     this.publicationDate = publicationDate;
   }
 
   protected Publisher publisher;
 
   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "publisher_id")
   public Publisher getPublisher() {
     return publisher;
   }
 
   public void setPublisher(Publisher publisher) {
     this.publisher = publisher;
   }
 
   protected Set<Author> authors = new HashSet<Author>();
 
   @ManyToMany
   public Set<Author> getAuthors() {
     return authors;
   }
 
   public void setAuthors(Set<Author> authors) {
     this.authors = authors;
   }

  // Convenience method, added by Todd Gerspacher
  public boolean addAuthor(Author author) {
    return authors.add(author);
  }
 }
