 package models;
 
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 
 import play.data.validation.Email;
 import play.data.validation.Phone;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class Student extends Model {
 
     @Required
     public String fullname;
     @Email
     public String email;
     @Phone
     public String phone;
 
     @ManyToMany(mappedBy = "students")
     public Set<Lesson> lessons;
 
     public Student(String fullname, String email, String phone) {
         this.fullname = fullname;
         this.email = email;
         this.phone = phone;
     }
 
     public void addLesson(Lesson lesson) {
        lesson.save();
         lessons.add(lesson);
        save();
     }
 
 }
