import java.util.Date;

 /**
  * Simple contrived Person class.
  */
 public class Person {
     private String name;
     private int age;
     private int numChildren;
 
     public Person(final String name, final int age, final int numChildren) {
         if (name == null || "".equals(name)) {
             throw new IllegalArgumentException("name cannot be null");
         }
 
         if (age <= 0) {
             throw new IllegalArgumentException("age cannot be less or equal to zero");
         }
 
         if (numChildren < 0) {
             throw new IllegalArgumentException("cannot have negative children, you're stuck with them");
         }
 
         this.name = name;
         this.age = age;
         this.numChildren = numChildren;
     }
 
     public String getName() {
         return name;
     }
 
     public int getAge() {
         return age;
     }
 
     public int getNumChildren() {
         return numChildren;
     }
 }
 
