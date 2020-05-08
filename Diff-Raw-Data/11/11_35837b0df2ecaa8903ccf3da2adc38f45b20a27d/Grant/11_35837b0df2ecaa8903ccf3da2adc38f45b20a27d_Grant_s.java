 package net.straininfo2.grs.idloader.bioproject.domain;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name="ProjectGrant")
 public class Grant {
 
     private long id;
 
     private String title;
 
     private String agencyName;
 
     private String agencyAbbr;
 
     private String grantId;
 
     private BioProject bioProject;
 
     @Id
     @GeneratedValue
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     @Column(length = 512)
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
        this.title = title;
     }
 
     @Column(length = 256)
     public String getAgencyName() {
         return agencyName;
     }
 
     public void setAgencyName(String agencyName) {
         this.agencyName = agencyName;
     }
 
     @Column(length = 64)
     public String getAgencyAbbr() {
         return agencyAbbr;
     }
 
     public void setAgencyAbbr(String agencyAbbr) {
         this.agencyAbbr = agencyAbbr;
     }
 
     @Column(length = 64)
     public String getGrantId() {
         return grantId;
     }
 
     public void setGrantId(String grantId) {
         this.grantId = grantId;
     }
 
     @ManyToOne(optional =false)
     public BioProject getBioProject() {
         return bioProject;
     }
 
     public void setBioProject(BioProject bioProject) {
         this.bioProject = bioProject;
     }
 }
