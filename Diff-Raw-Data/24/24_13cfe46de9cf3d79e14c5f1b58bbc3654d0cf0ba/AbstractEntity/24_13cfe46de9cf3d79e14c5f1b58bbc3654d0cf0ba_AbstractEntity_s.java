 package com.aciertoteam.common.entity;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Version;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import com.aciertoteam.common.dto.AbstractDTO;
 import com.aciertoteam.common.interfaces.IAbstractEntity;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @MappedSuperclass
 public abstract class AbstractEntity implements IAbstractEntity {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @Version
     private Integer version;
 
     @Column
     private Date validFrom;
 
     @Column
     private Date validThru;
 
     @Column
     private Date timestamp;
 
     protected AbstractEntity() {
         this.validFrom = new Date();
         this.timestamp = new Date();
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Date getValidFrom() {
         return validFrom;
     }
 
     public void setValidFrom(Date validFrom) {
         this.validFrom = validFrom;
     }
 
     public Date getValidThru() {
         return validThru;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     public boolean isDeleted() {
         return this.validThru != null;
     }
 
     public void closeEndPeriod() {
         validThru = new Date();
     }
 
    public <DTO extends AbstractDTO> void inject(DTO dto) {
        dto.setId(getId());
        dto.setTimestamp(getTimestamp());
        dto.setValidFrom(getValidFrom());
        dto.setValidThru(getValidThru());
    }

     @JsonIgnore
     public IAbstractEntity getEntity() {
         return this;
     }
 
     public boolean isNew() {
         return getId() == null;
     }
 
     /**
      * Additional options that will be added to the json object
      * 
      * @return String[]
      */
     public String[] getSelectItemOptions() {
         return new String[0];
     }
 
     public void check() {
        /*By default do not check the internal state of the current entity*/
     }
 }
