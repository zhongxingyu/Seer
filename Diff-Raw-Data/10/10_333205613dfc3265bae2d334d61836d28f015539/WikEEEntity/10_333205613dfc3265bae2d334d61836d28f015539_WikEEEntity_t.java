 package cz.cvut.wikee.model.persistence.entity;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by
  * User: Vaclav Cokrt
  * Email: beziks@gmail.com
  * Date: 20.1.13
  * Time: 16:43
  *
  * Parent class for all entities in app.
  * Dates are auto populated, use only getters
  */
 @Entity
 public abstract class WikeeEntity implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Integer id;
 
    private Date created;
    private Date updated;
 
     @ManyToOne
    protected User creator;
 
     @ManyToMany(
             mappedBy = "contains",
             cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
     private List<WikeeEntity> partOf;
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
     private List<WikeeEntity> contains;
 
     public WikeeEntity(){
         Date now = new Date();
         setCreated(now);
         setUpdated(now);
         this.contains = new ArrayList<WikeeEntity>();
         this.partOf = new ArrayList<WikeeEntity>();
     }
 
     public WikeeEntity(User creator){
         this();
         this.creator = creator;
     }
 
     public abstract String type();
 
     protected void propertyChanged(Serializable oldValue, Serializable newValue) {
         if(oldValue == null && newValue == null){
             return;
         }
         if(oldValue == null){
             Serializable tmp = oldValue;
             oldValue = newValue;
             newValue = oldValue;
         }
         if(!oldValue.equals(newValue)){
             return;
         }
 
         setUpdated(new Date());
     }
 
     public Integer getId() {
         return id;
     }
 
     private void setId(Integer id) {
         this.id = id;
     }
 
     public Date getCreated() {
         return created;
     }
 
     protected void setCreated(Date created) {
         this.created = created;
     }
 
     public Date getUpdated() {
         return updated;
     }
 
     protected void setUpdated(Date updated) {
         this.updated = updated;
     }
 
     public User getCreator() {
         return creator;
     }
 
     public void setCreator(User creator) {
         propertyChanged(this.creator, creator);
         this.creator = creator;
     }
 
     public List<WikeeEntity> getPartOf() {
         return Collections.unmodifiableList(partOf);
     }
 
     protected void setPartOf(List<WikeeEntity> partOf) {
         this.partOf = partOf;
     }
 
     public List<WikeeEntity> getContains() {
         return Collections.unmodifiableList(contains);
     }
 
     protected void setContains(List<WikeeEntity> contains) {
         this.contains = contains;
     }
 
     public void addPartOf(WikeeEntity entity){
         if(!partOf.contains(entity)){
             partOf.add(entity);
         }
         if(!entity.contains.contains(this)){
             entity.contains.add(this);
         }
     }
 
     public void removePartOf(WikeeEntity entity){
         if(!partOf.contains(entity)){
             partOf.remove(entity);
         }
         if(!entity.contains.contains(this)){
             entity.contains.remove(this);
         }
     }
 
     public void addContains(WikeeEntity entity){
         if(!contains.contains(entity)){
             contains.add(entity);
         }
         if(!entity.partOf.contains(this)){
             entity.partOf.add(this);
         }
     }
 
     public void removeContains(WikeeEntity entity){
         if(!contains.contains(entity)){
             contains.remove(entity);
         }
         if(!entity.partOf.contains(this)){
             entity.partOf.remove(this);
         }
     }
 }
