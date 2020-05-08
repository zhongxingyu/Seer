 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.domain;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.Pattern;
 import org.springframework.data.domain.Persistable;
 
 /**
  * A place holds measurements that have been added to it, it has a name and a description
  * @author tonykovanen
  */
 @Entity
 public class Place implements Serializable, Persistable<Long> {
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private Long id;
     @Column(unique = true)
     /**
      * Name is validated to only contain letters a-zA-Z0-9äöüÄÖÜ'-
      */
    @Pattern(regexp="^[a-zA-Z0-9äöüÄÖÜ'- ]+$", message="The name should only contains characters a-z, A-Z, 0-9, äöüÄÖÜ'-")
     private String name;
     /**
      * Description is validated to not contain characters <>%$
      */
     @Pattern(regexp="^[^<>%$]*$", message="The description should not contain <, >, % or $ characters")
     private String description;
     
     
     @OneToMany(mappedBy = "place", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
     private List<Measurement> measurements;
 
     public Long getId() {
         return id;
     }
     
     public List<Measurement> getMeasurements() {
         return measurements;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setMeasurements(List<Measurement> measurements) {
         this.measurements = measurements;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Override
     public boolean isNew() {
         return id == null;
     }
     
    
     
 }
