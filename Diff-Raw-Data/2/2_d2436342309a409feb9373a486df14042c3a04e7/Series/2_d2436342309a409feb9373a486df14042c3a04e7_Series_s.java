 package org.googolplex.javalib.domain;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.util.Set;
 
 @Entity
@Table(name = "entities")
 public class Series {
     @Id
     private Long id;
 
     @NotNull
     @Column(unique = true)
     private String name;
 
     @ManyToMany(
         cascade = {CascadeType.PERSIST, CascadeType.MERGE},
         targetEntity = Book.class,
         mappedBy = "series"
     )
     private Set<Book> books;
 
     @Transient // Temporarily transient
     private Set<Author> authors;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Set<Book> getBooks() {
         return books;
     }
 
     public void setBooks(Set<Book> books) {
         this.books = books;
     }
 
     public Set<Author> getAuthors() {
         return authors;
     }
 
     public void setAuthors(Set<Author> authors) {
         this.authors = authors;
     }
 }
