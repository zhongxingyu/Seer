 package models;
  
 import java.util.*;
 import javax.persistence.*;
import javax.xml.bind.annotation.*;
  
 import play.db.jpa.*;
import play.modules.resteasy.crud.CRUDField;
  
 @Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Task extends GenericModel {
	@Id
	@GeneratedValue
	@XmlAttribute
	public Long id;

	@CRUDField(editable = true)
     public String value;
	
	@CRUDField
     public Date createdAt;
     
     @ManyToOne
	@XmlTransient
     public User user;
     
     public Task(User user, String value) {
         this.user = user;
         this.value = value;
         this.createdAt = new Date();
     }
 }
