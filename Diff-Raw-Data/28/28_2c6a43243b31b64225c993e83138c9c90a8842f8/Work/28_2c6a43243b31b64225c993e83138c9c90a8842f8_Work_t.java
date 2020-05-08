 package com.worksnet.model;
 
 import com.worksnet.model.workdetails.WorkDetail;
 
 import javax.persistence.*;
 import java.util.List;
 
 /**
  * @author maxim.levicky
  *         Date: 2/27/13
  *         Time: 3:04 PM
  */
 @Entity
 @Table(name = "work")
 public class Work extends BaseModel {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "id", nullable = false)
     protected int id;
 
     @Column(name = "type", nullable = false)
     protected int type;
 
     @Column(name = "name", nullable = false)
     protected String name;
 
     @Column(name = "description")
     protected String description;
 
     @Column(name = "owner_id", nullable = false, updatable = false)
     protected int ownerId;
 
    @OneToMany(fetch = FetchType.LAZY)
     @JoinColumn(name = "work_id")
     protected List<WorkDetail> details;
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public int getType() {
         return type;
     }
 
     public void setType(int type) {
         this.type = type;
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
 
     public int getOwnerId() {
         return ownerId;
     }
 
     public void setOwnerId(int ownerId) {
         this.ownerId = ownerId;
     }
 
     public List<WorkDetail> getDetails() {
         return details;
     }
 
     public void setDetails(List<WorkDetail> details) {
         this.details = details;
     }
 }
