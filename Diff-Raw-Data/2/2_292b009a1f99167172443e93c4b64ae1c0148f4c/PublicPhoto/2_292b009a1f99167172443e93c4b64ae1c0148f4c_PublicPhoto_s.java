 package ai.ilikeplaces.entities;
 
 import ai.ilikeplaces.doc.*;
 import ai.ilikeplaces.util.EntityLifeCycleListener;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.UUID;
 
 /**
  * @author Ravindranath Akila
  */
 
 // @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Entity
 @EntityListeners(EntityLifeCycleListener.class)
 @OK
 public class PublicPhoto implements Serializable {
 
     @NOTE(note = "Pre persisted entities will have null ids. hence using pre persisted ids is not practical.")
     final private UUID uUID = UUID.randomUUID();
 
     private static final long serialVersionUID = 1L;
 
     public Long publicPhotoId = null;
 
     public String publicPhotoFilePath;
 
     @FieldPreamble(description = "The path should be very random as it will be exposed to the www." +
             "Also make sure this supports good SEO.")
     public String publicPhotoURLPath;
 
     public String publicPhotoName;
 
     public String publicPhotoDescription;
 
     @FieldPreamble(description = "Required to calculate ranking")
     public Date publicPhotoUploadDate;
 
     @FieldPreamble(description = "Required to calculate rank position")
     public Date publicPhotoTakenDate;
 
     @FieldPreamble(description = "Required to calculate rank position")
     public Long publicPhotoRankUnits;
 
     @FieldPreamble(description = "Required to calculate rank position")
     public Long publicPhotoRankTurns;
 
     @FieldPreamble(description = "Required when rebuilding a database from scratch someday." +
             "Since the whole concept of ilikeplaces relies on content richness, preserving this in this table important.")
     public Location location;
     final static public String locationCOL = "location";
 
     @FieldPreamble(description = "Who uploaded this image? Will he request to delete it?")
     public HumansPublicPhoto humansPublicPhoto;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getPublicPhotoId() {
         return publicPhotoId;
     }
 
     public void setPublicPhotoId(Long publicPhotoId) {
         this.publicPhotoId = publicPhotoId;
     }
 
     public String getPublicPhotoFilePath() {
         return publicPhotoFilePath;
     }
 
     public void setPublicPhotoFilePath(String publicPhotoFilePath) {
         this.publicPhotoFilePath = publicPhotoFilePath;
     }
 
     public String getPublicPhotoName() {
         return publicPhotoName;
     }
 
     public void setPublicPhotoName(String publicPhotoName) {
         this.publicPhotoName = publicPhotoName;
     }
 
     @Column(length = 1000)
     public String getPublicPhotoDescription() {
         return publicPhotoDescription;
     }
 
     public void setPublicPhotoDescription(final String publicPhotoDescription) {
         this.publicPhotoDescription = publicPhotoDescription;
     }
 
     @WARNING(warning = "The cascade types of HumansPublicPhoto, Location and this method, are very CascadeType sensitive. Any mistake will trigger rollbacks.")
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
     public HumansPublicPhoto getHumansPublicPhoto() {
         return humansPublicPhoto;
     }
 
     public void setHumansPublicPhoto(HumansPublicPhoto humansPublicPhoto) {
         this.humansPublicPhoto = humansPublicPhoto;
     }
 
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     public Long getPublicPhotoRankTurns() {
         return publicPhotoRankTurns;
     }
 
     public void setPublicPhotoRankTurns(Long publicPhotoRankTurns) {
         this.publicPhotoRankTurns = publicPhotoRankTurns;
     }
 
     public Long getPublicPhotoRankUnits() {
         return publicPhotoRankUnits;
     }
 
     public void setPublicPhotoRankUnits(Long publicPhotoRankUnits) {
         this.publicPhotoRankUnits = publicPhotoRankUnits;
     }
 
     @Temporal(javax.persistence.TemporalType.DATE)
     public Date getPublicPhotoTakenDate() {
         return publicPhotoTakenDate;
     }
 
     public void setPublicPhotoTakenDate(Date publicPhotoTakenDate) {
         this.publicPhotoTakenDate = publicPhotoTakenDate;
     }
 
     public String getPublicPhotoURLPath() {
         return publicPhotoURLPath;
     }
 
     public void setPublicPhotoURLPath(String publicPhotoURLPath) {
         this.publicPhotoURLPath = publicPhotoURLPath;
     }
 
     @Temporal(javax.persistence.TemporalType.DATE)
     public Date getPublicPhotoUploadDate() {
         return publicPhotoUploadDate;
     }
 
     public void setPublicPhotoUploadDate(Date publicPhotoUploadDate) {
         this.publicPhotoUploadDate = publicPhotoUploadDate;
     }
 
     @Override
     public int hashCode() {
         return (int) (this.getPublicPhotoId() != null ? this.getPublicPhotoId() : uUID.hashCode());
     }
 
     @Override
     public boolean equals(Object obj) {
         boolean returnVal = false;
         equals:
         {
             if (obj == null) {
                 returnVal = false;
                 break equals;
             }
             if (getClass() != obj.getClass()) {
                 returnVal = false;
                 break equals;
             }
             final PublicPhoto other = (PublicPhoto) obj;
             /*Are both not null?*/
             if (this.getPublicPhotoId() != null && other.getPublicPhotoId() != null) {
                 if (this.getPublicPhotoId().equals(other.getPublicPhotoId())) {
                     returnVal = true;
                     break equals;
                 }
             } else if (this.getPublicPhotoId() == null && other.getPublicPhotoId() == null) {/*Are both null?*/
                 /*i.e. Still not persisted. Lets compare UUIDs*/
                 if (this.uUID.equals(other.uUID)) {
                     returnVal = true;
                     break equals;
                 } else {
                     returnVal = false;
                     break equals;
                 }
             } else {/*Only one is null*/
                 returnVal = false;
                 break equals;
             }
 
         }
         return returnVal;
     }
 
     /**
      * @return
      */
     @Override
     public String toString() {
         return "PublicPhoto{" +
                 ", publicPhotoId=" + publicPhotoId +
                 '}';
     }
 
 }
