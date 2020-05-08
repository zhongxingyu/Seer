 package models;
 
 import java.util.List;
 
 import play.db.jpa.*;
 import play.data.validation.Min;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PrePersist;
 import javax.persistence.Query;
 
 
 /**
  * Base class for JPA model objects Automatically provide a @Id Long id field
  */
 @MappedSuperclass
 public class NestedSet extends GenericModel {
     
     @Id
     @GeneratedValue
     public Long    id;
     
     public String  name;
     
     @Min(1)
     private Long   lft = 1l;
     @Min(2)
     private Long   rgt = 2l;
     protected Long parent;
     
     
     public Long getId() {
     
         return id;
     }
     
     @Override
     public Object _key() {
     
         return getId();
     }
     
     @PrePersist
     public void insertNode() {
     
         // Get Parent Node
         if (this.parent != null) {
             NestedSet parentNode = JPA.em().find(this.getClass(), this.parent);
             
             // If Parent Node Found
             if (parentNode != null) {
                 
                 Query q;
                 
                 // Update All Changed Columns
                 q = JPA.em().createQuery("UPDATE " + this.getClass().getSimpleName() + " SET rgt = rgt + 2, lft = lft + 2 WHERE rgt > ?");
                 q.setParameter(1, parentNode.rgt);
                 q.executeUpdate();
                 
                 this.lft = parentNode.rgt;
                 this.rgt = this.lft + 1;
                 
                 parentNode.rgt = parentNode.rgt + 2;
                 parentNode.save();
             }
         }
     }
     
    public <T extends NestedSet> List<T> getChildren() {
     
         Query q = JPA.em().createQuery("SELECT ns FROM " + this.getClass().getSimpleName() + " ns WHERE lft > ? AND lft < ?");
         q.setParameter(1, this.lft);
         q.setParameter(2, this.rgt);
         
         return q.getResultList();
     }
 }
