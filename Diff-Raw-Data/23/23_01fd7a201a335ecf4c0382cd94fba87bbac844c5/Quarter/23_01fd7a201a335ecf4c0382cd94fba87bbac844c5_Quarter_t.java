 package model;
 
 import javax.persistence.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: acottrill
  * Date: 3/1/13
  * Time: 11:52 AM
  * To change this template use File | Settings | File Templates.
  */
 
@Table(name = "Quarter")
 @Entity
 public class Quarter {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "name", nullable = false, length = 40)
     private String name;
 
     public String getName(){
         return name;
     }
 
     public String toString(){
         return name;
     }
 }
