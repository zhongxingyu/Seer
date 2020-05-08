 package at.tuwien.inso.ase.demo;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class HelloWorld {
 
     public String helloWorld() {
         return "Hello World";
     }
 
     public String hello(String user) {
        return "Hello " + user + "! What can web do for you today?";
     }
 }
