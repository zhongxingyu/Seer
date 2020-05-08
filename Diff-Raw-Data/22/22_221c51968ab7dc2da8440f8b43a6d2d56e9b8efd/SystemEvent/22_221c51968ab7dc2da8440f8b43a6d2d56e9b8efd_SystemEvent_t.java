 package velo.entity;
 
 import java.util.Date;
 
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 /*@Entity
 @Table(name="VL_SYSTEM_EVENT")
 @SequenceGenerator(name="SystemEventIdSeq",sequenceName="SYSTEM_EVENT_ID_SEQ")*/
 
 @Table(name="VL_SYSTEM_EVENT")
 @Entity
 //@DiscriminatorValue("SYSTEM_EVENT")
 
 @NamedQueries( {
 	@NamedQuery(name = "systemEvent.findByUniqueName", query = "SELECT se FROM SystemEvent se WHERE se.uniqueName = :uniqueName")
 } )
 //@DiscriminatorValue("SYSTEM_EVENT")
 public class SystemEvent extends Event {
	public static final String EVENT_PRE_USER_CREATION = "USER_CREATION_PRE";
	public static final String EVENT_POST_USER_CREATION = "USER_CREATION_POST";
 	public static final String EVENT_RESOURCE_RECONCILIATION_POST = "RESOURCE_RECONCILIATION_POST";
 	
 	
 	public SystemEvent() {
 		
 	}
 	
 	public SystemEvent(String uniqueName, String displayName, String description) {
 		super(uniqueName, displayName, description);
 		setCreationDate(new Date());
 	}
 	
 	
 	//private Long systemEventId;
 
 	/*@Id
     @GeneratedValue(strategy=GenerationType.AUTO,generator="SystemEventIdSeq")
 	public Long getSystemEventId() {
 		return systemEventId;
 	}
 
 	public void setSystemEventId(Long systemEventId) {
 		this.systemEventId = systemEventId;
 	}*/
 }
