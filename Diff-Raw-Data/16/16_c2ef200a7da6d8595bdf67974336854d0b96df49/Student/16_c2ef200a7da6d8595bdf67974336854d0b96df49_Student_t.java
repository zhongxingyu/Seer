 import java.util.List;
 
 /**
  */
 public class Student {
     private final String name;
     private final String group;
     private final List<String> task;
 
     public Student(String name, String group, List<String> task) {
         this.name = name;
         this.group = group;
         this.task = task;
     }
 
     public List<String> getTask() {
         return task;
     }
 
     @Override
     public String toString() {
         return name + " " + group;
     }
 }
