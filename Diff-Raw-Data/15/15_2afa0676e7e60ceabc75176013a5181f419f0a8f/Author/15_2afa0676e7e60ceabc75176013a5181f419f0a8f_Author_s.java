 package net.kurochenko.ispub.author.form;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
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
     private Long id;
     
    @Column(name = "me_id")
     private String meId;
 
     @Column(name = "name")
     private String name;
     
     @Column(name = "surname")
     private String surname;
     
     @Column(name = "note")
     private String note;
 
     @DepartmentFormat
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(name = "author_department",
         joinColumns = { @JoinColumn(name = "author")},
         inverseJoinColumns = { @JoinColumn(name = "department")})
     private List<Department> departments;
 
     @SourceFormat
     @ManyToMany(fetch= FetchType.EAGER)
     @JoinTable(name = "author_source",
         joinColumns = { @JoinColumn(name = "author")},
         inverseJoinColumns = { @JoinColumn(name = "source")})
     private List<Source> sources = new ArrayList<Source>();
 
     public List<Department> getDepartments() {
         return departments;
     }
 
     public void setDepartments(List<Department> departments) {
         this.departments = departments;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
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
 
     public List<Source> getSources() {
         return sources;
     }
 
     public void setSources(List<Source> sources) {
         this.sources = sources;
     }
     
     public void addSource(Source source) {
         this.sources.add(source);
         source.getAuthors().add(this);
     }
     
     public void removeSource(Source source) {
         this.sources.remove(source);
         source.getAuthors().remove(this);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Author author = (Author) o;
 
         if (id != null ? !id.equals(author.id) : author.id != null) return false;
         if (meId != null ? !meId.equals(author.meId) : author.meId != null) return false;
         if (name != null ? !name.equals(author.name) : author.name != null) return false;
         if (note != null ? !note.equals(author.note) : author.note != null) return false;
         if (surname != null ? !surname.equals(author.surname) : author.surname != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (meId != null ? meId.hashCode() : 0);
         result = 31 * result + (name != null ? name.hashCode() : 0);
         result = 31 * result + (surname != null ? surname.hashCode() : 0);
         result = 31 * result + (note != null ? note.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "Author{" + "name=" + name + ", surname=" + surname + '}';
     }
 }
