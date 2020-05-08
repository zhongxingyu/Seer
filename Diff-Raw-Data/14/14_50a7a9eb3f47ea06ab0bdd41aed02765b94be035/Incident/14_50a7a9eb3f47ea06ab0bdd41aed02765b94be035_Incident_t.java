 package models;
  
 import java.util.*;
 import javax.persistence.*;
 
 import org.apache.commons.lang.time.DateFormatUtils;
 
 import controllers.Application;
  
 import play.db.jpa.*;
  
 @Entity
 public class Incident extends Model {
     
     @OneToOne
     public IncidentCategory incidentCategory;
     
     public String incidentTitle;
     public String description;
     public Date incidentDate;
     public long duration;
     @OneToOne
     public Location location;
     public String direction;
     
     @OneToOne
     public User User;
     
     
     public Incident(IncidentCategory aIncidentCategory, String incidentTitle, String description,
             Date incidentDate, long duration,
             Location location, String direction,
             User user) 
     {
         super();
         this.incidentCategory = aIncidentCategory;
         this.incidentTitle = incidentTitle;
         this.description = description;
         this.incidentDate = incidentDate;
         this.duration = duration;
         this.location = location;
         this.direction = direction;
         User = user;
     }
     
     public Date getIncidentDate()
     {
         return this.incidentDate;
 	}
     
    public String getIncidentDateForUshahidi()
     {
    	return DateFormatUtils.format(incidentDate, "M/dd/yyyy");
    }
	

    public String getIncidentDateFormated()
    {
        return incidentDate.toString();
     }
     
     
     @PostPersist
     public void test()
     {
         Application.USHAHIDI.storeIncident(this);        
     }
 }
