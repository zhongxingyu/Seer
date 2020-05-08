 package pl.agh.enrollme.webflow.model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.io.Serializable;
 
 @Entity(name="enrollment")
 public class Enrollment implements Serializable {
 
     @Id
    private int id;
 
     private String name;
 
     public Enrollment() {
 
     }
 
    public int getId() {
        return id;
     }
 
    public void setId(int id) {
        this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
