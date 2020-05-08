 package net.canang.cfi.core.dd.model.impl;
 
 import net.canang.cfi.core.dd.model.CfStateCode;
 import net.canang.cfi.core.so.model.CfMetadata;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 
 /**
  * todo: comment
  *
  * @author canang.technologies
  */
 @Entity(name = "CfStateCode")
 @Table(name = "CF_STTE_CODE")
 public class CfStateCodeImpl implements CfStateCode, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @Column(name = "ID", nullable = false)
     @GeneratedValue(generator = "SEQ_CF_STTE_CODE")
     @SequenceGenerator(name = "SEQ_CF_STTE_CODE", sequenceName = "SEQ_STTE_CITY_CODE", allocationSize = 1)
     private Long id;
 
     @NotNull
     @Column(name = "CODE", nullable = false)
     private String code;
 
     @NotNull
     @Column(name = "ALIAS", nullable = false)
     private String alias;
 
     @NotNull
     @Column(name = "DESCRIPTION")
     private String description;
 
    @OneToOne(targetEntity = CfStateCodeImpl.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "STATE_CODE_ID")
    private CfStateCode stateCode;
 
     @Embedded
     private CfMetadata metadata;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public String getAlias() {
         return alias;
     }
 
     public void setAlias(String alias) {
         this.alias = alias;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
    public CfStateCode getStateCode() {
        return stateCode;
     }
 
    public void setStateCode(CfStateCode stateCode) {
        this.stateCode = stateCode;
     }
 
     public CfMetadata getMetadata() {
         return metadata;
     }
 
     public void setMetadata(CfMetadata metadata) {
         this.metadata = metadata;
     }
 
     @Override
     public String getSummary() {
         return code + Character.DASH_PUNCTUATION + getDescription();
     }
 
 }
