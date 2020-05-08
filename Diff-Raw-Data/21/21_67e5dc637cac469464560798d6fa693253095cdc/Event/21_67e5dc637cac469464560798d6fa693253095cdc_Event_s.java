 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.radioaine.domain;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import javax.persistence.*;
 import ohtu.radioaine.tools.Time;
 
 /**
  *
  * @author Richard Moneybags
  */
 @Entity
 public class Event implements Serializable {
     
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private int id;
    @Column
     String happening;
    @Column
     String info;
     @Column
     private Timestamp timestamp;
     
     public Event() {
         timestamp = Time.getTimestamp();
     }
 
     public String getHappening() {
         return happening;
     }
 
     public String getInfo() {
         return info;
     }
 
     public void setInfo(String info) {
         this.info = info;
     }
 
     public void setHappening(String happening) {
         this.happening = happening;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public Timestamp getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Timestamp timestamp) {
         this.timestamp = timestamp;
     }
     
 }
