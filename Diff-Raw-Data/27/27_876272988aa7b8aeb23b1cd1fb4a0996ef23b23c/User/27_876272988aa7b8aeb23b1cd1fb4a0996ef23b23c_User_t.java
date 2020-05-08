 package me.donnior.sparkle.demo;
 
import me.donnior.eset.Accessible;

 
 public class User {
 
     
    @Accessible private String name;
    @Accessible private String mail;
    @Accessible private int age;
     
     public User(){
         this(null, null);
     }
     
     public User(String name, String mail) {
         this.name = name;
         this.mail = mail;
     }
     
     public String getMail() {
         return mail;
     }
     
     public String getName() {
         return name;
     }
 
     public int getAge() {
         return age;
     }
     
     public String toString() {
         return name + ":"+ mail + ":" + age;
     }
 }
