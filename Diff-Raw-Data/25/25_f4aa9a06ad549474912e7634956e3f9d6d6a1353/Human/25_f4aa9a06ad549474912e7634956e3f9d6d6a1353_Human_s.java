 package net.wissenswerft.test.maven;
 
 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.Setter;
 import lombok.extern.log4j.Log4j;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Admin
  * Date: 02.09.13
  * Time: 15:41
  * To change this template use File | Settings | File Templates.
  */
 @Log4j
@AllArgsConstructor
 @Getter
 @Setter
 public class Human {
 
     private String firstname;
 
     private String lastName;
 
 }
