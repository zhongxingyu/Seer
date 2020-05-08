 package cn.edu.sdu.oopSort.bean;
 
 import java.io.Serializable;
 
 /**
  * 
  * @author Yonggang Yuan
 *
  */
 
 public class Person implements Serializable {
 
     private static final long serialVersionUID = 667645502175806738L;
 
     /** Fields **/
 
     /* Name of a person[Full name] */
     private String name;
 
     /* Age of a person[years old] */
     private int age;
 
     /* Height of a person[cm] */
     private double height;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     /** constructions **/
 
     public Person() {}
 
     public Person(String name) {
         this.name = name;
     }
     
     public Person(String name, int age, double height) {
         this.name = name;
         this.age = age;
         this.height = height;
     }
 
     /** Setters and getters **/
 
     public int getAge() {
         return age;
     }
 
     public void setAge(int age) {
         this.age = age;
     }
 
     public double getHeight() {
         return height;
     }
 
     public void setHeight(double height) {
         this.height = height;
     }
 
     /** Methods **/
 
     /**
      * To judge if this is older than given person
      * @param person : Given person.
      * @return : Return true if this is older than given person.
      */
     public boolean older(Person person) {
         return this.age > person.age;
     }
 
     /** Override hashCode and equals using name **/
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (obj == null) return false;
         if (getClass() != obj.getClass()) return false;
         Person other = (Person) obj;
         if (name == null) {
             if (other.name != null) return false;
         } else if (!name.equals(other.name)) return false;
         return true;
     }
 
     @Override
     public String toString() {
         return this.name + "\t|\t" + this.age + "\t|\t" + this.height;
     }
 
 }
