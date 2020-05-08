 package pl.agh.enrollme.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Entity
 public class Subject implements Serializable {
     @Transient
     private static final long serialVersionUID = -5771235478609230476L;
 
     @Id
     @GeneratedValue
     private Integer SubjectID=0;
 
     @ManyToOne(fetch = FetchType.LAZY)
     private Enroll enroll;
 
     @ManyToMany(mappedBy = "subjects", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     private List<Person> persons = new ArrayList<>();
 
     @Column(unique = true, nullable = false)
    private String name = "";
 
     @Column(nullable = false)
     private Integer teamsCapacity;
 
     @Column(nullable = false)
     private String color;
 
     @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
     private Teacher teacher;
 
     public Subject() {
         teacher = new Teacher("","","","");
     }
 
     public Subject(Enroll enroll, List<Person> persons, String name, Integer teamsCapacity, String color,
                    Teacher teacher) {
         this.enroll = enroll;
         this.persons = persons;
         this.name = name;
         this.teamsCapacity = teamsCapacity;
         this.color = color;
         this.teacher = teacher;
     }
 
     public void addPerson(Person person) {
         persons.add(person);
     }
 
     public void setSubjectID(Integer subjectID) {
         SubjectID = subjectID;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Enroll getEnroll() {
         return enroll;
     }
 
     public List<Person> getPersons() {
         return persons;
     }
 
     public void setTeamsCapacity(Integer teamsCapacity) {
         this.teamsCapacity = teamsCapacity;
     }
 
     public void setEnroll(Enroll enroll) {
         this.enroll = enroll;
     }
 
     public void setPersons(List<Person> persons) {
         this.persons = persons;
     }
 
     public void setColor(String color) {
         this.color = color;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public String getColor() {
         return color;
     }
 
     public Integer getTeamsCapacity() {
         return teamsCapacity;
     }
 
     public String getName() {
         return name;
     }
 
     public Integer getSubjectID() {
         return SubjectID;
     }
 
     @Override
     public String toString() {
         return "Subject{" +
                 "SubjectID=" + SubjectID +
                 ", name='" + name + '\'' +
                 '}';
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Subject)) return false;
 
         Subject subject = (Subject) o;
 
         if (!name.equals(subject.name)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 }
