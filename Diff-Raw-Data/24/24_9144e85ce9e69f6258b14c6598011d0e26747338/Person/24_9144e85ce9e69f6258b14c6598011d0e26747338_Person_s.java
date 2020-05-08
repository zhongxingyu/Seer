 package com.sunnyd.models;
 
 import com.sunnyd.Base;
 import com.sunnyd.IModel;
 import com.sunnyd.annotations.*;
 
 import java.util.HashMap;
 
 public class Person extends Base implements IModel {
     public static final String tableName = "persons";
 
     @tableAttr
     private String firstName;
 
     @tableAttr
     private String lastName;
 
     private String status;
 
     public Person(){
         super();
     }
     public Person(HashMap<String, Object> HM) {
         super(HM);
     }
 
     public static void main(String[] args) {
 
         Person a = new Person();
        a.setFirstName("bobby  ");
         a.setLastName("yit");
        int id = a.save();
        
        Person b = Person.find(id);
           
 //        Person a = Person.find(1);
 //
 //        System.out.println(a.getId());
 //        System.out.println(a.getFirstName());
 //        System.out.println(a.getLastName());
 //        a.setFirstName("john");
 //
 //        Person b = Person.find(2);
 //
        System.out.println(b.getId());
        System.out.println(b.getFirstName());
        System.out.println(b.getLastName());
 //        b.setFirstName("oaisjdoaijdoa");
 //
 //        b.update();
 //        a.update();
         // System.out.println(a.Destroy());
 
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
         setUpdateFlag(true);
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
         setUpdateFlag(true);
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
 }
