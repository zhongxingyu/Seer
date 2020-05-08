 package models;
 
 import java.util.*;
 import javax.persistence.*;
  
 import play.db.jpa.*;
 
 
 @Entity
 public class Template extends Model {
 
     public String Content;
     public String Name;
    public Integer ID;
     
    public Template(String name, Integer id) {
         this.Name = name;
        this.ID = id;
     }
     
 }
