 package pl.agh.enrollme.model;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import pl.agh.enrollme.utils.EnrollmentMode;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Entity
 public class Enroll implements Serializable {
 
     @Transient
     private static final long serialVersionUID = -577123547860923047L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Enroll.class.getName());
 
     @Id
     @GeneratedValue
     private Integer enrollID;
 
     private String name;
     private EnrollmentMode enrollmentMode;
 
     @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private EnrollConfiguration enrollConfiguration;
 
     @OneToMany(mappedBy = "enroll", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private List<Subject> subjects = new ArrayList<>();
 
     @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
     private List<Person> persons;
 
     public EnrollConfiguration getEnrollConfiguration() {
         return enrollConfiguration;
     }
 
     public Enroll(String name, EnrollmentMode enrollmentMode, EnrollConfiguration enrollConfiguration, List<Subject> subjects) {
         this.enrollmentMode = enrollmentMode;
         this.enrollConfiguration = enrollConfiguration;
         this.subjects = subjects;
         enrollID = 0;
         this.name = name;
     }
 
     public void addSubject(Subject subject) {
        LOGGER.debug("I am trying to add subject: " + subject);
         if(subjects.contains(subject)) {
            LOGGER.debug("Subject is already persisted, do not persist it again!");
             return;
         }
        LOGGER.debug("Adding subject ( " + subject + " ) ended with success");
         subjects.add(subject);
     }
 
     /**
      * @param enrollmentMode defines either enroll is completed, closed or currently open
      */
     public void setEnrollmentMode(EnrollmentMode enrollmentMode) {
         this.enrollmentMode = enrollmentMode;
     }
 
     public EnrollmentMode getEnrollmentMode() {
         return enrollmentMode;
     }
 
     /**
      * @param subjects subjects available for this enrollment
      */
     public void setSubjects(List<Subject> subjects) {
         this.subjects = subjects;
     }
 
     public List<Subject> getSubjects() {
         return subjects;
     }
 
     /**
      * @param enrollID primary key
      */
     public void setEnrollID(Integer enrollID) {
         this.enrollID = enrollID;
     }
 
     public List<Person> getPersons() {
         return persons;
     }
 
     /**
      * @param persons list of people who are allowed for this enrollment
      */
     public void setPersons(List<Person> persons) {
         this.persons = persons;
     }
 
     /**
      * @param enrollConfiguration object enrollConfiguration - configuration of this enrollment
      */
     public void setEnrollConfiguration(EnrollConfiguration enrollConfiguration) {
         this.enrollConfiguration = enrollConfiguration;
     }
 
     public Enroll() {
         enrollID = 0;
         name = "";
     }
 
     /**
      * @param name name of the enrollment
      */
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getEnrollID() {
 
         return enrollID;
     }
 
     public String getName() {
         return name;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Enroll)) return false;
 
         Enroll enroll = (Enroll) o;
 
         if (enrollID != null ? !enrollID.equals(enroll.enrollID) : enroll.enrollID != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return enrollID != null ? enrollID.hashCode() : 0;
     }

    @Override
    public String toString() {
        return "Enroll{" +
                "enrollID=" + enrollID +
                ", name='" + name + '\'' +
                '}';
    }
 }
