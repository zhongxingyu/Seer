 package no.niths.domain;
 
 import java.io.Serializable;
 import java.util.List;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity
 @Table(name = AppConstants.ROOMS)
 @XmlRootElement
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 public class Room implements Serializable {
 
     @Transient
     private static final long serialVersionUID = 1L;
 
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "room_name")
     private String roomName;
 
     @OneToMany(fetch = FetchType.EAGER)
     @Cascade(CascadeType.ALL)
     private List<AccessField> accessFields;
 
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
 }
