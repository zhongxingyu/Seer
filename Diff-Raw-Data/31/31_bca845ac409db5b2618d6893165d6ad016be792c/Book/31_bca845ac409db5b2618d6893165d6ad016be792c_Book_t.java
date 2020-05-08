 package org.googolplex.javalib.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
import java.util.Set;
 
 /**
  * Date: 17.03.12
  * Time: 16:45
  *
  * @author Vladimir Matveev
  */
 @Entity
 @Table(name = "books")
 public class Book {
     @Id
     @NotNull
     private Long id;

    @NotNull
    private String name;

    @ManyToMany
    private Set<Author> author;
 }
