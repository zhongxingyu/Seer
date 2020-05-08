 package charityHibernateEntities;
 
 import java.sql.Timestamp;
 
 import javax.persistence.Entity;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.hibernate.envers.Audited;
 
 @Entity
 @Audited
 @XmlRootElement(name = "userType")
 public class UserType {
 
 	private Integer userTypeId;
 	private String userType;
 	private String description;
 	private Boolean isActive;
	private Timestamp timestamp;
 //	private User user;
 	
 /*	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}*/
 	@XmlElement
 	public Integer getUserTypeId() {
 		return userTypeId;
 	}
 	public void setUserTypeId(Integer userTypeId) {
 		this.userTypeId = userTypeId;
 	}
 	@XmlElement
 	public String getUserType() {
 		return userType;
 	}
 	public void setUserType(String userType) {
 		this.userType = userType;
 	}
 	@XmlElement
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	@XmlElement
 	public Boolean getIsActive() {
 		return isActive;
 	}
 	public void setIsActive(Boolean isActive) {
 		this.isActive = isActive;
 	}
	@XmlElement
 	public Timestamp getTimestamp() {
 		return timestamp;
 	}
 	public void setTimestamp(Timestamp timestamp) {
 		this.timestamp = timestamp;
	}
 	
 }
