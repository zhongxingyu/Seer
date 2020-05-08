 package pl.agh.enrollme.model;
 
import org.jboss.netty.example.localtime.LocalTimeProtocol;
 import pl.agh.enrollme.utils.Color;
 import pl.agh.enrollme.utils.DayOfWeek;
 import pl.agh.enrollme.utils.StupidDate;
 
 import javax.persistence.*;
 import java.io.Serializable;
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
 
     @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     private List<Person> persons = new ArrayList<>();
 
     private String name;
     private Integer teamsCapacity;
     private Color color;
     private String room;
 
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private Teacher teacher;
 
     private DayOfWeek dayOfWeek;
 
     @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     private StupidDate timeStart;
 
     @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     private StupidDate timeEnd;
 
     public Subject() {
         teacher = new Teacher("","","","");
     }
 
     public Subject(Enroll enroll, List<Person> persons, String name, Integer teamsCapacity, Color color, String room,
                    Teacher teacher, DayOfWeek dayOfWeek, StupidDate timeStart, StupidDate timeEnd) {
         this.enroll = enroll;
         this.persons = persons;
         this.name = name;
         this.teamsCapacity = teamsCapacity;
         this.color = color;
         this.room = room;
         this.teacher = teacher;
         this.dayOfWeek = dayOfWeek;
         this.timeStart = timeStart;
         this.timeEnd = timeEnd;
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
 
     public void setColor(Color color) {
         this.color = color;
     }
 
     public void setRoom(String room) {
         this.room = room;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public void setDayOfWeek(DayOfWeek dayOfWeek) {
         this.dayOfWeek = dayOfWeek;
     }
 
     public void setTimeStart(StupidDate timeStart) {
         this.timeStart = timeStart;
     }
 
     public void setTimeEnd(StupidDate timeEnd) {
         this.timeEnd = timeEnd;
     }
 
     public StupidDate getTimeEnd() {
 
         return timeEnd;
     }
 
     public StupidDate getTimeStart() {
         return timeStart;
     }
 
     public DayOfWeek getDayOfWeek() {
         return dayOfWeek;
     }
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public String getRoom() {
         return room;
     }
 
     public Color getColor() {
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
 }
