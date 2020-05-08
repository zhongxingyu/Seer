 package fr.adrienbrault.notetonsta.entity;
 
 import javax.persistence.*;
 
 import java.util.List;
 
 /**
  * Author: Adrien Brault
  * Date: 12/12/11
  * Time: 13:15
  */
 
 @Entity
@Table(name = "speaker")
 public class Speaker {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     protected Integer id;
 
     @Basic
     protected String email;
 
     @Basic
     protected String password;
 
     @Basic
     protected String salt;
 
     @Basic
     protected String firstName;
 
     @Basic
     protected String lastName;
 
     @OneToMany(mappedBy = "speaker", cascade = CascadeType.ALL)
     protected List<Intervention> interventions;
 
     public Speaker() {
 
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getSalt() {
         return salt;
     }
 
     public void setSalt(String salt) {
         this.salt = salt;
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
 
     public List<Intervention> getInterventions() {
         return interventions;
     }
 
     public void setInterventions(List<Intervention> interventions) {
         this.interventions = interventions;
     }
 
 }
