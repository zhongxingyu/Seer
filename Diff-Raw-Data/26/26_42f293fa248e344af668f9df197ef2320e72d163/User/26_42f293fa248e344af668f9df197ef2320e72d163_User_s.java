 package com.cloudbees.gasp.models;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import java.util.Collection;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 @Entity
 public class User {
     @JsonProperty
     @Id
     @GeneratedValue(strategy= GenerationType.AUTO)
     private int id;
 
     /**
      * Human readable name of the restaurant.
      */
     @JsonProperty
     @Column
     private String name;
 
    @OneToMany(mappedBy= "restaurant")
     private Collection<Review> reviews;
 
     public User() {
     }
 
     public User(int id) {
         this.id = id;
     }
 
     public int getId() {
         return id;
     }
 
     public String getUrl() {
         return "/users/"+id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Collection<Review> getReviews() {
         return reviews;
     }
 }
