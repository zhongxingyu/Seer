 package ua.dp.primat.domain.lesson;
 
 import java.io.Serializable;
import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import ua.dp.primat.domain.workload.Discipline;
 import ua.dp.primat.domain.Lecturer;
 import ua.dp.primat.domain.StudentGroup;
 
 /**
  * Entity, which contains common information about lesson for one group,
  * one semester. Every lessonType has its own LessonDescription
  * @author fdevelop
  */
 @Entity
 public class LessonDescription implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue
     private Long id;
     @ManyToOne
     private Discipline discipline;
     @ManyToOne
     private StudentGroup studentGroup;
     private Long semester;
     private LessonType lessonType;
     @ManyToOne
     private Lecturer lecturer;
     @ManyToOne
     private Lecturer assistant;
 
     public LessonDescription() {
     }
 
     public LessonDescription(Discipline discipline, StudentGroup studentGroup, Long semester, LessonType lessonType, Lecturer lecturer, Lecturer assistant) {
         this.discipline = discipline;
         this.studentGroup = studentGroup;
         this.semester = semester;
         this.lessonType = lessonType;
         this.lecturer = lecturer;
         this.assistant = assistant;
     }
 
     public Lecturer getAssistant() {
         return assistant;
     }
 
     public void setAssistant(Lecturer assistant) {
         this.assistant = assistant;
     }
 
     public Discipline getDiscipline() {
         return discipline;
     }
 
     public void setDiscipline(Discipline discipline) {
         this.discipline = discipline;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Lecturer getLecturer() {
         return lecturer;
     }
 
     public void setLecturer(Lecturer lecturer) {
         this.lecturer = lecturer;
     }
 
     public LessonType getLessonType() {
         return lessonType;
     }
 
     public void setLessonType(LessonType lessonType) {
         this.lessonType = lessonType;
     }
 
     public Long getSemester() {
         return semester;
     }
 
     public void setSemester(Long semester) {
         this.semester = semester;
     }
 
     public StudentGroup getStudentGroup() {
         return studentGroup;
     }
 
     public void setStudentGroup(StudentGroup studentGroup) {
         this.studentGroup = studentGroup;
     }
 
     /**
      * Returns a string representation of lecturers names for this lesson.
      * @return string value like "Name A.A., Name2 B.B."
      */
     public String getLecturerNames() {
         if (lecturer == null) {
             return "";
         }
         if (assistant == null) {
             return lecturer.getShortName();
         } else {
             return lecturer.getShortName() + ", " + assistant.getShortName();
         }
     }
 }
