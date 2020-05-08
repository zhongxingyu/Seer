 package no.hials.muldvarpweb.domain;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 
 /**
  *
  * @author Lena
  */
 @Entity
 public class Person implements Serializable { 
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue
     private Long id;
     
     @Column(nullable=false)
     String name;
     
//    String username;
//    String password;
//    public enum Roles {
//        STUDENT,TEACHER
//    }
     
     @ManyToMany(mappedBy = "teachers")
     private List<Course> courses;
     
     String affiliation; //f.eks. Faglig ledelse
     String position;    //f.eks. Dekan
     String department;  //f.eks. Internasjonal markedsføring
     String email;       //f.eks. per@hials.no
     String location;    //f.eks. A214 eller kunnskapsparken eller Oslo
 
     public String getAffiliation() {
         return affiliation;
     }
 
     public void setAffiliation(String affiliation) {
         this.affiliation = affiliation;
     }
 
     public String getDepartment() {
         return department;
     }
 
     public void setDepartment(String department) {
         this.department = department;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getLocation() {
         return location;
     }
 
     public void setLocation(String location) {
         this.location = location;
     }
 
     public int getPhone() {
         return phone;
     }
 
     public void setPhone(int phone) {
         this.phone = phone;
     }
 
     public String getPosition() {
         return position;
     }
 
     public void setPosition(String position) {
         this.position = position;
     }
     int phone;          //f.eks. 70161310
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
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
         if (!(object instanceof Person)) {
             return false;
         }
         Person other = (Person) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "no.hials.muldvarpweb.domain.Person[ id=" + id + " ]";
     }
     
 }
