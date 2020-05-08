 package fr.cg95.cvq.business.users.external;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cascade;
 
 @Entity
 @Table(name="home_folder_mapping")
 public class HomeFolderMapping implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
     
     @Column(name="external_service_label")
     private String externalServiceLabel;
 
     @Column(name="home_folder_id")
     private Long homeFolderId;
 
     @Column(name="external_capdemat_id")
     private String externalCapDematId;
 
     @Column(name="external_id")
     private String externalId;
 
     @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY, mappedBy="homeFolderMapping")
     @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OrderColumn(name="home_folder_mapping_index")
     private List<IndividualMapping> individualsMappings;
 
     
     public HomeFolderMapping() {
     }
 
     public HomeFolderMapping(String externalServiceLabel, Long homeFolderId,  String externalId) {
         this.externalServiceLabel = externalServiceLabel;
         this.homeFolderId = homeFolderId;
         this.externalCapDematId = UUID.randomUUID().toString();
         this.externalId = externalId;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getExternalServiceLabel() {
         return externalServiceLabel;
     }
 
     public void setExternalServiceLabel(String externalServiceLabel) {
         this.externalServiceLabel = externalServiceLabel;
     }
 
     public Long getHomeFolderId() {
         return homeFolderId;
     }
 
     public void setHomeFolderId(Long homeFolderId) {
         this.homeFolderId = homeFolderId;
     }
 
     public String getExternalCapDematId() {
         return externalCapDematId;
     }
 
     public void setExternalCapDematId(String externalCapDematId) {
         this.externalCapDematId = externalCapDematId;
     }
 
     public String getExternalId() {
         return externalId;
     }
 
     public void setExternalId(String externalId) {
         this.externalId = externalId;
     }
 
     public List<IndividualMapping> getIndividualsMappings() {
         if (individualsMappings == null)
             individualsMappings = new ArrayList<IndividualMapping>();
         return individualsMappings;
     }
 
     public void setIndividualsMappings(List<IndividualMapping> individualsMappings) {
         this.individualsMappings = individualsMappings;
     }
 
 }
