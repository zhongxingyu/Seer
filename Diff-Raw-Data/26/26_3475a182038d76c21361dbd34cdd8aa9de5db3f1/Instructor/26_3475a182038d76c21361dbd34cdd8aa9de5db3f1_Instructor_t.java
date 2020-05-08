 package models;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Instructor extends User {
 
     public String positionSki;
     public String positionSnowboard;
     // @Enumerated(EnumType.STRING)
     // public Lang languages;
 
     @ManyToMany(mappedBy = "instructors")
     public Set<Lesson> lessons;
 
     @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
     public Set<Availability> availabilities;
 
     public static Instructor getByEmail(String userName) {
         return find("byEmail", userName).first();
     }
 
     public Instructor(String email, String password, String firstname, String surname, String phone) {
         super(email, password, firstname, surname, phone);
         this.availabilities = new HashSet<Availability>();
         this.lessons = new HashSet<Lesson>();
     }
 
     public void addAvailability(Availability availability) {
         availability.save();
         availabilities.add(availability);
         save();
     }
 
     public void addLesson(Lesson lesson) {
        //lesson.save();
         lessons.add(lesson);
        //save();
     }
 
 }
