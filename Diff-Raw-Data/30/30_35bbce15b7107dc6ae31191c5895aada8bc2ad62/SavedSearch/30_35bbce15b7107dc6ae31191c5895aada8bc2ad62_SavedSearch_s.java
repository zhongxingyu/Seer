 package wad.timetables.domain;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /* @author mhaanran */
 @Entity
 public class SavedSearch implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     private String searchName;
     
     private String stopNumber;
     
     private String stopName;
 
     public SavedSearch() {
    }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getSearchName() {
         return searchName;
     }
 
     public void setSearchName(String searchName) {
         this.searchName = searchName;
     } 
 
     public String getStopNumber() {
         return stopNumber;
     }
 
     public void setStopNumber(String stopNumber) {
         this.stopNumber = stopNumber;
     }
 
     public String getStopName() {
         return stopName;
     }
 
     public void setStopName(String stopName) {
         this.stopName = stopName;
     }
     
     
 }
