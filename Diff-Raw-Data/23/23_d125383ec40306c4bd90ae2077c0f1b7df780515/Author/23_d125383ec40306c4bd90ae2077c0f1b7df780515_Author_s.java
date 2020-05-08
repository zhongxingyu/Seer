 package org.googolplex.javalib.domain;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.util.Set;
 
 @Entity
 @Table(name = "authors")
 public class Author {
     @Id
     private Long id;
 
     @NotNull
     private String name;
 
     @ManyToMany(
         targetEntity = Book.class,
         cascade = {CascadeType.PERSIST, CascadeType.MERGE},
         mappedBy = "authors"
     )
     private Set<Book> books;
 
     @Transient // Temporarily transient
     private Set<Series> series;
 
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
 }
