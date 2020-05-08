 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package reserv.entity;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author muody
  */
 @Entity
 @Table(name = "reservation")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Reservation.findAll", query = "SELECT r FROM Reservation r"),
     @NamedQuery(name = "Reservation.findById", query = "SELECT r FROM Reservation r WHERE r.id = :id"),
     @NamedQuery(name = "Reservation.findByFirstName", query = "SELECT r FROM Reservation r WHERE r.firstName = :firstName"),
     @NamedQuery(name = "Reservation.findByLastName", query = "SELECT r FROM Reservation r WHERE r.lastName = :lastName"),
     @NamedQuery(name = "Reservation.findByPhoneNumber", query = "SELECT r FROM Reservation r WHERE r.phoneNumber = :phoneNumber"),
    @NamedQuery(name = "Reservation.findBySeanceId", query = "SELECT r FROM Reservation r WHERE r.seance.id = :seanceId"),
     @NamedQuery(name = "Reservation.findByPlace", query = "SELECT r FROM Reservation r WHERE r.place = :place")})
 public class Reservation implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @Basic(optional = false)
     @NotNull
     @Column(name = "id", nullable = false)
     private Integer id;
     @Size(max = 45)
     @Column(name = "first_name", length = 45)
     private String firstName;
     @Size(max = 45)
     @Column(name = "last_name", length = 45)
     private String lastName;
     @Size(max = 45)
     @Column(name = "phone_number", length = 45)
     private String phoneNumber;
     @Column(name = "place")
     private Integer place;
     @JoinColumn(name = "seance_id", referencedColumnName = "id", nullable = false)
     @ManyToOne(optional = false, fetch = FetchType.EAGER)
     private Seance seance;
 
     public Reservation() {
     }
 
     public Reservation(Integer id) {
         this.id = id;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public Integer getPlace() {
         return place;
     }
 
     public void setPlace(Integer place) {
         this.place = place;
     }
 
     public Seance getSeance() {
         return seance;
     }
 
     public void setSeance(Seance seance) {
         this.seance = seance;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Reservation)) {
             return false;
         }
         Reservation other = (Reservation) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "reserv.entity.Reservation[ id=" + id + " ]";
     }
     
 }
