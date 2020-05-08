 package org.emamotor.javase.utility.common;
 
 /**
  * @author emag
  */
 public class Human implements Comparable<Human> {
 
     private String name;
     private int age;
 
     public Human(String name, int age) {
         this.name = name;
         this.age = age;
     }
 
     String getName() {
         return this.name;
     }
 
     void setName(String name) {
         this.name = name;
     }
 
     int getAge() {
         return this.age;
     }
 
     void setAge(int age) {
         this.age = age;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Human human = (Human) o;
 
         if (age != human.age) return false;
         if (name != null ? !name.equals(human.name) : human.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + age;
         return result;
     }
 
     @Override
     public String toString() {
         return "Human{" +
                ", name='" + name + '\'' +
                 "age=" + age +
                 '}';
     }
 
     @Override
     public int compareTo(Human other) {
 
         int nameComparison = this.name.compareTo(other.getName());
 
         if (nameComparison > 0) return  1;
         if (nameComparison < 0) return -1;
 
         if (this.age > other.getAge()) return  1;
         if (this.age < other.getAge()) return -1;
 
         return 0;
 
     }
 }
