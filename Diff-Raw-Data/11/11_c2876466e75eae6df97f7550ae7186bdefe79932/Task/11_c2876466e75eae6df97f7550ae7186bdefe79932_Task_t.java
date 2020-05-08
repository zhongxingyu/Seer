 package au.gov.nsw.records.digitalarchives.dashboard.model;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import com.google.gson.annotations.Expose;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 public class Task {
 	@Id
   @Expose
 	@GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;
 
   @Expose
   private String description;
 
   @ManyToOne
   private Project project;
    
   @ManyToOne
   private Person assignedTo;
 
   @Expose
   @Temporal(TemporalType.TIMESTAMP)
   @DateTimeFormat(style = "M-")
   private Date created;
 
   @ManyToOne
   private Person createdBy;
   
   @Expose
   @Temporal(TemporalType.TIMESTAMP)
   @DateTimeFormat(style = "M-")
   private Date due;
   
   @Enumerated(EnumType.STRING)
   private TaskStatusType status;
   
   
 //  @OneToMany
 //  private List<RelationshipWithOtherRecords> relationship;
 //  
   
}
