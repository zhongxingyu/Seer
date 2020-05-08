 package no.ntnu.fp.model;
 
 import no.ntnu.fp.Client;
 
 public class Room {
     private String title;
     private int capacity;
     
     public Room(String title, int capacity) {
         this.title = title;
         this.capacity = capacity;
     }
     
     public static Room loadRoom(int id) {
        return Client.f1;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public int getCapacity() {
         return capacity;
     }
 
     public void setCapacity(int capacity) {
         this.capacity = capacity;
     }
 }
