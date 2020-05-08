 package no.niths.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
 import no.niths.domain.signaling.AccessField;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity
 @Table(name = AppConstants.ROOMS)
 @XmlRootElement
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 public class Room implements Serializable {
 
 	@Transient
 	private static final long serialVersionUID = -664567726655902624L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     @Column(name = "room_name", unique = true)
     private String roomName;
 
     @ManyToMany(fetch = FetchType.LAZY)
     @Cascade(CascadeType.ALL)
    @JoinTable(
    		name="rooms_access_fields")
     private List<AccessField> accessFields = new ArrayList<AccessField>();
 
     public Room(String roomName) {
 		setRoomName(roomName);
 	}
 
 	public Room() {
 		this(null);
 	}
 
 	public void setId(Long id) {
         this.id = id;
     }
 
     public Long getId() { 
         return id;
     }
 
     public void setRoomName(String roomName) {
         this.roomName = roomName;
     }
 
     public String getRoomName() {
         return roomName;
     }
 
     public void setAccessFields(List<AccessField> accessFields) {
         this.accessFields = accessFields;
     }
 
     public List<AccessField> getAccessFields() {
         return accessFields;
     }
     
     @Override
     public String toString() {
     	return String.format("[%s][%s]", id, roomName);
     }
 }
