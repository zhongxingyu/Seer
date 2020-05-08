 package edu.chl.dat076.foodfeed.model.entity;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 @Entity
 public class User implements Serializable, IEntity<String> {
 
     private String username;
     private String password;
     private List<Recipe> recipes;
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    @Size(min = 1, message = "The username must consist of at least 1 character")
     @Override
     public String getId() {
         return this.username;
     }
 
     public void setId(String id) {
         this.username = id;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
    @NotNull
    @Size(min = 6, message = "The password must consist of at least 6 characters")
    @Pattern(regexp = "[a-zA-Z0-9_\\s\\W]")
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
    @OneToMany
     public List<Recipe> getRecipes() {
         return recipes;
     }
 
     public void setRecipes(List<Recipe> recipes) {
         this.recipes = recipes;
     }
 }
