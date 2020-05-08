 package kabbadi.domain;
 
 import lombok.Getter;
 
 @Getter
 public enum Location {
    BANGALORE("Bangalore"), CHENNAI("Chennai"), PUNE("Pune");
     private String location;
 
     Location(String location) {
         this.location = location;
     }
 


 }
