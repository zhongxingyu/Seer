 package edu.entitymaster.logic;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Антон
  * Date: 10.11.12
  * Time: 16:09
  * To change this template use File | Settings | File Templates.
  */
 public class Client {
     String name;
     boolean deleted;
     int id;
 
    public Client(String name){
         this.name = name;
    }
 
     public Client(int id, String name, Boolean deleted) {
         this.id = id;
         this.name = name;
         this.deleted = deleted;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public Boolean getDeleted() {
         return deleted;
     }
 
     public void setDeleted(Boolean deleted) {
         this.deleted = deleted;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String read() {
         return id + "," + name + "," + deleted;       //+ number + etc
     }
 }
