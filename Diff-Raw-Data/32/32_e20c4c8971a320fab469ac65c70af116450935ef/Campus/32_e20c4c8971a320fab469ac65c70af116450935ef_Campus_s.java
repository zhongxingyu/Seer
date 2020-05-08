 package fr.adrienbrault.notetonsta.entity;
 
 import javax.persistence.*;
 import java.util.List;
 
 /**
  * Author: Adrien Brault
  * Date: 12/12/11
  * Time: 13:15
  */
 
 @Entity
 public class Campus {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     protected Integer id;
 
     @Basic
     protected String name;
 
     @OneToMany(mappedBy = "campus", cascade = CascadeType.ALL)
     protected List<Intervention> interventions;
 
     public Campus() {
 
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Intervention> getInterventions() {
         return interventions;
     }
 
     public void setInterventions(List<Intervention> interventions) {
         this.interventions = interventions;
     }
 
 }
