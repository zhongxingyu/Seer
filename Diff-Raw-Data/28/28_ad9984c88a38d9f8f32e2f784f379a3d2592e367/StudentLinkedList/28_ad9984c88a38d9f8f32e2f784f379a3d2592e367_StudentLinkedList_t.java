 package student.list;
 
 import student.Student;
 
 import java.util.LinkedList;
 
 public class StudentLinkedList extends LinkedList<Student> {
     @Override
     public String toString() {
         StringBuilder out = new StringBuilder();
 
         for(Student s : this) {
             out.append(s.toString());
            out.append("||");
         }
 
         int length = out.length();
 
        if(length > 1) {
            out.setLength(length - 2);
         }
 
         return out.length() == 0 ? "Nothing found." : out.toString();
     }
 }
