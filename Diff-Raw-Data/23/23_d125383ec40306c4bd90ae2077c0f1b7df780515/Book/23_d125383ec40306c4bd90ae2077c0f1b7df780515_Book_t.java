 package org.googolplex.javalib.domain;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.util.Set;
 
 @Entity
 @Table(name = "books")
 public class Book {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @NotNull
     private String name;
 
     @ManyToMany(
         targetEntity = Author.class,
         cascade = {CascadeType.PERSIST, CascadeType.MERGE}
     )
     @JoinTable(
         name = "books_authors_map",
         joinColumns = @JoinColumn(name = "book_id"),
         inverseJoinColumns = @JoinColumn(name = "author_id")
     )
     private Set<Author> authors;
 
     @ManyToMany(
         targetEntity = Series.class,
         cascade = {CascadeType.PERSIST, CascadeType.MERGE}
     )
     @JoinTable(
         name = "books_series_map",
         joinColumns = @JoinColumn(name = "book_id"),
         inverseJoinColumns = @JoinColumn(name = "series_id")
     )
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
 
     public Set<Author> getAuthors() {
         return authors;
     }
 
     public void setAuthors(Set<Author> authors) {
         this.authors = authors;
     }
 
     public Set<Series> getSeries() {
         return series;
     }
 
     public void setSeries(Set<Series> series) {
         this.series = series;
     }
 }
