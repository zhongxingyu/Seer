 package net.kurochenko.ispub.source.form;
 
 import net.kurochenko.ispub.author.form.Author;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: kurochenko
  * Date: 7/5/11
  * Time: 1:19 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name = "source")
 public class Source implements Serializable {
     @Id
    @Column(name = "sourceId")
     @GeneratedValue
     private Long id;
 
     @Column(name = "name",unique = true)
     private String name;
 
     @ManyToMany(mappedBy = "sources")
             @JoinTable(name = "author_source",
         joinColumns = { @JoinColumn(name = "source")},
         inverseJoinColumns = { @JoinColumn(name = "author")})
     private List<Author> authors = new ArrayList<Author>();
 
     public Source() {}
 
     public Source(String name) {
         this.name = name;
     }
 
    public Long getID() {
         return id;
     }
 
    public void setID(Long sourceId) {
         this.id = sourceId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Author> getAuthors() {
         return authors;
     }
 
     public void setAuthors(List<Author> authors) {
         this.authors = authors;
     }
     
     public void addAuthor(Author author) {
         this.authors.add(author);
         author.getSources().add(this);
     }
     
     public void removeAuthor(Author author) {
         this.authors.remove(author);
         author.getSources().remove(this);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Source source = (Source) o;
 
         if (name != null ? !name.equals(source.name) : source.name != null) return false;
         if (id != null ? !id.equals(source.id) : source.id != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (name != null ? name.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return name;
     }
 }
