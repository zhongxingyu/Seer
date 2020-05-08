 package net.kurochenko.ispub.author.form;
 
 import java.io.Serializable;
 import java.util.Set;
 import javax.persistence.*;
 
 import net.kurochenko.ispub.department.form.Department;
 import net.kurochenko.ispub.department.form.DepartmentFormat;
 import net.kurochenko.ispub.source.form.Source;
 import net.kurochenko.ispub.source.form.SourceFormat;
 
 
 /**
  *
  * @author Andrej Kuročenko <kurochenko@gmail.com>
  */
 @Entity
 @Table(name = "author")
 public class Author implements Serializable {
 
     @Id
     @Column(name = "idauthor")
     @GeneratedValue
     private Integer idAuthor;
     
     @Column(name = "me_id")
     private String meId;
 
     @Column(name = "name")
     private String name;
     
     @Column(name = "surname")
     private String surname;
     
     @Column(name = "note")
     private String note;
 
     @DepartmentFormat
    @ManyToOne(cascade = CascadeType.ALL,optional = true)
     private Department department;
 
     @ManyToMany(
             targetEntity = Source.class,
             cascade = {CascadeType.PERSIST, CascadeType.MERGE   },
             fetch = FetchType.EAGER
     )
     @JoinTable(
             name="author_source",
             joinColumns = @JoinColumn(name = "authorId"),
             inverseJoinColumns = @JoinColumn(name = "sourceId")
     )
     private Set<Source> sources;
 
     public Department getDepartment() {
         return department;
     }
 
     public void setDepartment(Department department) {
         this.department = department;
     }
 
     public Integer getIdAuthor() {
         return idAuthor;
     }
 
     public void setIdAuthor(Integer idAuthor) {
         this.idAuthor = idAuthor;
     }
 
     public String getMeId() {
         return meId;
     }
 
     public void setMeId(String meId) {
         this.meId = meId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getNote() {
         return note;
     }
 
     public void setNote(String note) {
         this.note = note;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public Set<Source> getSources() {
         return sources;
     }
 
     public void setSources(Set<Source> sources) {
         this.sources = sources;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Author author = (Author) o;
 
         if (department != null ? !department.equals(author.department) : author.department != null) return false;
         if (idAuthor != null ? !idAuthor.equals(author.idAuthor) : author.idAuthor != null) return false;
         if (meId != null ? !meId.equals(author.meId) : author.meId != null) return false;
         if (name != null ? !name.equals(author.name) : author.name != null) return false;
         if (note != null ? !note.equals(author.note) : author.note != null) return false;
         if (sources != null ? !sources.equals(author.sources) : author.sources != null) return false;
         if (surname != null ? !surname.equals(author.surname) : author.surname != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = idAuthor != null ? idAuthor.hashCode() : 0;
         result = 31 * result + (meId != null ? meId.hashCode() : 0);
         result = 31 * result + (name != null ? name.hashCode() : 0);
         result = 31 * result + (surname != null ? surname.hashCode() : 0);
         result = 31 * result + (note != null ? note.hashCode() : 0);
         result = 31 * result + (department != null ? department.hashCode() : 0);
         result = 31 * result + (sources != null ? sources.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "Author{" + "name=" + name + ", surname=" + surname + '}';
     }
 }
