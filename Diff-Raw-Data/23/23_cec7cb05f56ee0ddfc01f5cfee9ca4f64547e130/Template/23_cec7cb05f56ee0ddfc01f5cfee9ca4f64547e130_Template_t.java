 package models;
 
 import java.util.*;
 import javax.persistence.*;
  
 import play.db.jpa.*;
 
 
 @Entity
 public class Template extends Model {
 
     public String Content;
     public String Name;
     
    public Template(String name) {
         this.Name = name;
     }
     
 }
