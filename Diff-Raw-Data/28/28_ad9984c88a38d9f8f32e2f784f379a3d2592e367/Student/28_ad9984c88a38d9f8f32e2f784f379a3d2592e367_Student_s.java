 package student;
 
 public class Student {
     private Integer id;
     private String name;
     private String lastName;
     private String school;
 
     public Student(String name, String lastName, String school) {
         this.id = -1;
         this.name = name;
         this.lastName = lastName;
         this.school = school;
     }
 
     public int hashCode() {
         return id;
     }
 
     public String toString() {
         return String.format(
 //            "Name:\t%s\nLast Name:\t%s\nSchool:\t%s\nId:\t%d",
            "%s:%s:%s:%d",
             name, lastName, school, id);
     }
 
     public boolean registerId(int id) {
         if (this.id == -1) {
             this.id = id;
             return true;
         }
         return false;
     }
 
     public Integer getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public String getSchool() {
         return school;
     }
 }
 
