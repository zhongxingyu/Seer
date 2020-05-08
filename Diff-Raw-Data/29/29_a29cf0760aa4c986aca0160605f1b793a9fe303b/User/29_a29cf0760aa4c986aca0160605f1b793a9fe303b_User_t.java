 package edu.chl.dat076.foodfeed.model.entity;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 @Entity
@Table(name = "FoodfeedUser") // User is a reserved keyword in many databases
 public class User implements Serializable, IEntity<String> {
 
    @Id
    @NotNull
    @Size(min = 1, message = "The username must consist of at least 1 character")
     private String username;
    @NotNull
    @Size(min = 6, message = "The password must consist of at least 6 characters")
    @Pattern(regexp = "[a-zA-Z0-9_\\s\\W]")
     private String password;
    @OneToMany
     private List<Recipe> recipes;
 
     @Override
     public String getId() {
         return this.username;
     }
 
    @Override
     public void setId(String id) {
         this.username = id;
     }
 
    public String getUsername() {
        return username;
    }

     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public List<Recipe> getRecipes() {
         return recipes;
     }
 
     public void setRecipes(List<Recipe> recipes) {
         this.recipes = recipes;
     }
 }
