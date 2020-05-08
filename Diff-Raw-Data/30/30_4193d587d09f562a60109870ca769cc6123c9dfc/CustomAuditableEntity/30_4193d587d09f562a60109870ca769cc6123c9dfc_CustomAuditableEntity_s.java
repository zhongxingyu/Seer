 /*
  * Copyright (c) 2013.
  * All rights reserved.
  */
 package com.project.commons.audit.entity.support;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.MappedSuperclass;
 
 import org.hibernate.annotations.Parameter;
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 import org.springframework.data.annotation.CreatedBy;
 import org.springframework.data.annotation.CreatedDate;
 import org.springframework.data.annotation.LastModifiedBy;
 import org.springframework.data.annotation.LastModifiedDate;
 import org.springframework.data.jpa.domain.AbstractPersistable;
 
 /**
  * @author mhua
  */
 @MappedSuperclass
 public class CustomAuditableEntity<PK extends Serializable> extends AbstractPersistable<PK> {
 
     /**
     * @
      */
     @Column(name = "CREATED_BY")
     @CreatedBy
     private String createdBy;
 
     /**
     * @
      */
     @Column(name = "CREATED_DATETIME")
     @CreatedDate
     @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime",
             parameters = { @Parameter(name = "databaseZone", value = "jvm") })
     private DateTime createdDateTime;
 
     /**
     * @
      */
     @Column(name = "LAST_UPDATED_BY")
     @LastModifiedBy
     private String lastUpdatedBy;
 
     /**
     * @
      */
     @Column(name = "LAST_UPDATED_DATETIME")
     @LastModifiedDate
     @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime",
             parameters = { @Parameter(name = "databaseZone", value = "jvm") })
     private DateTime lastUpdatedDateTime;
 
     /**
     * @return
      */
     public String getCreatedBy() {
         return createdBy;
     }
 
     /**
      * @param createdBy
      */
     public void setCreatedBy(String createdBy) {
         this.createdBy = createdBy;
     }
 
     /**
     * @return
      */
     public DateTime getCreatedDateTime() {
         return createdDateTime;
     }
 
     /**
      * @param createdDateTime
      */
     public void setCreatedDateTime(DateTime createdDateTime) {
         this.createdDateTime = createdDateTime;
     }
 
     /**
     * @return
      */
     public String getLastUpdatedBy() {
         return lastUpdatedBy;
     }
 
     /**
      * @param lastUpdatedBy
      */
     public void setLastUpdatedBy(String lastUpdatedBy) {
         this.lastUpdatedBy = lastUpdatedBy;
     }
 
     /**
     * @return
      */
     public DateTime getLastUpdatedDateTime() {
         return lastUpdatedDateTime;
     }
 
     /**
      * @param lastUpdatedDateTime
      */
     public void setLastUpdatedDateTime(DateTime lastUpdatedDateTime) {
         this.lastUpdatedDateTime = lastUpdatedDateTime;
     }
 
 }
