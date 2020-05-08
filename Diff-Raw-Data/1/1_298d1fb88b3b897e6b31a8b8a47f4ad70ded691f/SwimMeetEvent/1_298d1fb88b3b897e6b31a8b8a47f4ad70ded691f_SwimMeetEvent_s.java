 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entity;
 
 import java.io.Serializable;
 import java.util.List;
 import javassist.tools.rmi.ObjectNotFoundException;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import org.usa_swimming.xsdif.CourseType;
 import org.usa_swimming.xsdif.Gender;
 import org.usa_swimming.xsdif.StrokeType;
 
 /**
  *
  * @author nhorman
  */
 @Entity
 public class SwimMeetEvent extends PersistingObject implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     private StrokeType stroke;
     private int distance;
     private CourseType course;
     private Gender gender;
     private String minAge;
     private String maxAge;
     private Boolean isRelay;
     private String minTimeClass;
     private String maxTimeClass;
     private int eventNumber;
     
     //Note - This is a list to point to all Athletes registered for 
     //for this event
     @OneToMany
    @JoinColumn
     private List<SwimMeetAthlete> swimmers;
     
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public static SwimMeetEvent getEventByEventNumber(Integer eventNumber) {
         List<SwimMeetEvent> results;
         String myQuery = "SELECT * FROM SwimMeetEvent WHERE SwimMeetEvent.eventNumber = " + eventNumber.toString();
         results = SwimMeetEvent.queryClassObjects(myQuery, SwimMeetEvent.class);
         if (results.isEmpty())
             return null;
         if (results.size() > 1) {
             return null;
         }
         return results.get(0);
     }
     
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (getId() != null ? getId().hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof SwimMeetEvent)) {
             return false;
         }
         SwimMeetEvent other = (SwimMeetEvent) object;
         if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entity.SwimMeetEvent[ id=" + getId() + " ]";
     }
 
     /**
      * @return the minAge
      */
     public String getMinAge() {
         return minAge;
     }
 
     /**
      * @param minAge the minAge to set
      */
     public void setMinAge(String minAge) {
         this.minAge = minAge;
     }
 
     /**
      * @return the maxAge
      */
     public String getMaxAge() {
         return maxAge;
     }
 
     /**
      * @param maxAge the maxAge to set
      */
     public void setMaxAge(String maxAge) {
         this.maxAge = maxAge;
     }
 
     /**
      * @return the isRelay
      */
     public Boolean getIsRelay() {
         return isRelay;
     }
 
     /**
      * @param isRelay the isRelay to set
      */
     public void setIsRelay(Boolean isRelay) {
         this.isRelay = isRelay;
     }
 
     /**
      * @return the minTimeClass
      */
     public String getMinTimeClass() {
         return minTimeClass;
     }
 
     /**
      * @param minTimeClass the minTimeClass to set
      */
     public void setMinTimeClass(String minTimeClass) {
         this.minTimeClass = minTimeClass;
     }
 
     /**
      * @return the maxTimeClass
      */
     public String getMaxTimeClass() {
         return maxTimeClass;
     }
 
     /**
      * @param maxTimeClass the maxTimeClass to set
      */
     public void setMaxTimeClass(String maxTimeClass) {
         this.maxTimeClass = maxTimeClass;
     }
 
     /**
      * @return the eventNumber
      */
     public Integer getEventNumber() {
         return eventNumber;
     }
 
     /**
      * @param eventNumber the eventNumber to set
      */
     public void setEventNumber(Integer eventNumber) {
         this.eventNumber = eventNumber;
     }
 
     /**
      * @return the stroke
      */
     public StrokeType getStroke() {
         return stroke;
     }
 
     /**
      * @param stroke the stroke to set
      */
     public void setStroke(StrokeType stroke) {
         this.stroke = stroke;
     }
 
     /**
      * @return the distance
      */
     public int getDistance() {
         return distance;
     }
 
     /**
      * @param distance the distance to set
      */
     public void setDistance(int distance) {
         this.distance = distance;
     }
 
     /**
      * @return the course
      */
     public CourseType getCourse() {
         return course;
     }
 
     /**
      * @param course the course to set
      */
     public void setCourse(CourseType course) {
         this.course = course;
     }
 
     /**
      * @return the gender
      */
     public Gender getGender() {
         return gender;
     }
 
     /**
      * @param gender the gender to set
      */
     public void setGender(Gender gender) {
         this.gender = gender;
     }
 
     /**
      * @return the swimmers
      */
     public List<SwimMeetAthlete> getSwimmers() {
         return swimmers;
     }
 
     /**
      * @param swimmers the swimmers to set
      */
     public void setSwimmers(List<SwimMeetAthlete> swimmers) {
         this.swimmers = swimmers;
     }
     
     public void addSwimmer(SwimMeetAthlete newswimmer) throws Exception {
         
         if (getSwimmers().contains(newswimmer)) {
                 throw new Exception("Swimmer already registered for this event");
         }
         getSwimmers().add(newswimmer);
     }
     
     public void removeSwimmer(SwimMeetAthlete swimmer) throws ObjectNotFoundException {
         if (getSwimmers().contains(swimmer)) {
             getSwimmers().remove(swimmer);
         }
         
     }
 }
