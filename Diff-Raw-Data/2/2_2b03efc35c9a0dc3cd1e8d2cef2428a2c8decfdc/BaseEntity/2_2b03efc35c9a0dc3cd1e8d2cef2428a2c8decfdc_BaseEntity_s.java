 package com.amee.persist;
 
 import com.amee.base.domain.DatedObject;
 import com.amee.base.utils.UidGen;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.annotations.NaturalId;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * An abstract base class for persistent entities. Provides common base properties and
  * methods. The properties cover identity (id, uid) and auditing (created, modified).
  */
 @MappedSuperclass
 public abstract class BaseEntity implements DatedObject, Serializable {
 
     @Transient
     protected final Log log = LogFactory.getLog(getClass());
 
     // The exact size of all UID fields.
     public final static int UID_SIZE = 12;
 
     /**
      * The unique ID, within the table, of the entity.
      */
     @Id
     @GeneratedValue
     @Column(name = "ID")
     private Long id;
 
     /**
      * The unique UID of the entity.
      */
     @NaturalId
     @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
     private String uid = "";
 
     /**
      * Timestamp of when the entity was created. Set by onCreate().
      */
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "CREATED", nullable = false)
     private Date created = null;
 
     /**
      * Timestamp of when the entity was modified. Set by onModify().
      */
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "MODIFIED", nullable = false)
     private Date modified = null;
 
     public BaseEntity() {
         super();
         setUid(UidGen.INSTANCE_12.getUid());
     }
 
     /**
      * Two BaseEntity instances are considered equal if their UID matches, along with standard
      * object identity matching.
      *
      * @param o object to compare
      * @return true if the supplied object matches this object
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
        if (!BaseEntity.class.isAssignableFrom(o.getClass())) return false;
         BaseEntity baseEntity = (BaseEntity) o;
         return getUid().equals(baseEntity.getUid());
     }
 
     /**
      * Returns a hash code for an BaseEntity. Internally uses the hash code of the uid property.
      *
      * @return the hash code
      */
     @Override
     public int hashCode() {
         return getUid().hashCode();
     }
 
     /**
      * Copy values from this instance to the supplied instance.
      *
      * @param o Object to copy values to
      */
     protected void copyTo(BaseEntity o) {
         o.id = id;
         o.uid = uid;
         o.created = (created != null) ? (Date) created.clone() : null;
         o.modified = (modified != null) ? (Date) modified.clone() : null;
     }
 
     /**
      * Called by the JPA persistence provider when a persistent entity is created. Updates created and
      * modified with the current time.
      */
     @PrePersist
     public void onCreate() {
         // Update created and modified.
         Date now = new Date();
         setCreated(now);
         setModified(now);
     }
 
     /**
      * Called by the JPA persistence provider when a persistent entity is updated. Updates modified with
      * the current time.
      */
     @PreUpdate
     public void onModify() {
         setModified(new Date());
     }
 
     /**
      * Fetch the entity ID.
      *
      * @return the entity ID
      */
     public Long getId() {
         return id;
     }
 
     /**
      * Sets the entity ID.
      *
      * @param id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Get the entity UID.
      *
      * @return the entity UID
      */
     public String getUid() {
         return uid;
     }
 
     /**
      * Set the entity UID.
      *
      * @param uid to set
      */
     public void setUid(String uid) {
         if (uid == null) {
             uid = "";
         }
         this.uid = uid;
     }
 
     public String getIdentityValue() {
         return getUid();
     }
 
     public void setIdentityValue(String value) {
         setUid(value);
     }
 
     /**
      * Fetch the created timestamp.
      *
      * @return the created timestamp
      */
     public Date getCreated() {
         return created;
     }
 
     /**
      * Set the created timestamp.
      *
      * @param created timestamp to set
      */
     public void setCreated(Date created) {
         this.created = created;
     }
 
     /**
      * Fetch the modified timestamp.
      *
      * @return modified timestamp to set
      */
     public Date getModified() {
         return modified;
     }
 
     /**
      * Set the modified timestamp.
      *
      * @param modified timestamp to set
      */
     public void setModified(Date modified) {
         this.modified = modified;
     }
 }
