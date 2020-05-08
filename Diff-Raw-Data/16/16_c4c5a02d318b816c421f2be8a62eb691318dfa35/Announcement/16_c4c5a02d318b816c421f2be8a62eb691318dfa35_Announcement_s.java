 package org.svfc57.entities;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.apache.tapestry5.beaneditor.NonVisual;
 import org.apache.tapestry5.beaneditor.Validate;
 
 @Entity(name="Announcement")
 public class Announcement {
 
 	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@NonVisual
 	public long id;
 	
 	@NonVisual
 	@Temporal(TemporalType.DATE)
 	public Date createDate;
 	
 	@NonVisual
 	@Temporal(TemporalType.DATE)
 	public Date modifiedDate;
 
 	@NonVisual
 	public long createdBy;
 	
 	@NonVisual
 	public long modifiedBy;
 
 	@Validate("required")
 	public String title;
 
 	@Validate("required")
 	public String text;
 
 	@Validate("required")
 	public boolean active;
 	
 	@Validate("required")
 	@Temporal(TemporalType.DATE)
 	public Date showAfterDate;
 	
 	@Validate("required")
 	@Temporal(TemporalType.DATE)
 	public Date showBeforeDate;
 	
 	@NonVisual
 	public boolean deleted;
 	
 }
