 package org.jboss.pressgang.ccms.model.contentspec;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.NotNull;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "CSMetaDataToCSTranslatedString", uniqueConstraints = @UniqueConstraint(columnNames = { "CSMetaDataID",
         "CSTranslatedStringID" }))
 public class CSMetaDataToCSTranslatedString extends AuditedEntity<CSMetaDataToCSTranslatedString> implements
         java.io.Serializable {
     private static final long serialVersionUID = -7516063608506037594L;
 
     private Integer csMetaDataToCSTranslatedStringId;
     private ContentSpecToCSMetaData contentSpecToCSMetaData;
     private CSTranslatedString csTranslatedString;
 
     public CSMetaDataToCSTranslatedString() {
     }
 
     public CSMetaDataToCSTranslatedString(final ContentSpecToCSMetaData csMetaDataMapping,
             final CSTranslatedString csTranslatedString) {
         this.contentSpecToCSMetaData = csMetaDataMapping;
         this.csTranslatedString = csTranslatedString;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "CSMetaDataToCSTranslatedStringID", unique = true, nullable = false)
     public Integer getCSMetaDataToCSTranslatedStringId() {
         return this.csMetaDataToCSTranslatedStringId;
     }
 
     public void setCSMetaDataToCSTranslatedStringId(final Integer csMetaDataToCSTranslatedStringId) {
         this.csMetaDataToCSTranslatedStringId = csMetaDataToCSTranslatedStringId;
     }
 
     @ManyToOne
     @JoinColumn(name = "ContentSpecToCSMetaDataID", nullable = false)
     @NotNull
     public ContentSpecToCSMetaData getContentSpecToCSMetaData() {
         return this.contentSpecToCSMetaData;
     }
 
     public void setContentSpecToCSMetaData(final ContentSpecToCSMetaData csMetaData) {
         this.contentSpecToCSMetaData = csMetaData;
     }
 
     @ManyToOne
     @JoinColumn(name = "CSTranslatedStringID", nullable = false)
     @NotNull
     public CSTranslatedString getCSTranslatedString() {
         return this.csTranslatedString;
     }
 
     public void setCSTranslatedString(final CSTranslatedString csTranslatedString) {
         this.csTranslatedString = csTranslatedString;
     }
 
     @Override
     @Transient
     public Integer getId() {
         return this.csMetaDataToCSTranslatedStringId;
     }
 
 }
