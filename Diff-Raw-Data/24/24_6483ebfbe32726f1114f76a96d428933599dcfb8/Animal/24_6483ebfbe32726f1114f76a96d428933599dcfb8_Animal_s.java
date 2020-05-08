 package lesson1.ams;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 04.11.11
  * Time: 22:29
  */
 
 public abstract class Animal {
     
     private String name;
 
     public Animal(String name){
         this.name = name;
     }
 
     public abstract String moves();
     public abstract String sound();
 
     public String output(){
        return name + " " + this.getClass().toString() ;
     }
 
 }
