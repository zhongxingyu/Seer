 package org.jboss.pressgang.ccms.rest.v1.entities;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTFileCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBasePrimaryEntityV1;
 
 public class RESTFileV1 extends RESTBasePrimaryEntityV1<RESTFileV1, RESTFileCollectionV1, RESTFileCollectionItemV1> {
     public static final String DESCRIPTION_NAME = "description";
     public static final String FILE_PATH_NAME = "filePath";
     public static final String FILE_NAME = "fileName";
     public static final String EXPLODE_ARCHIVE_NAME = "explodeArchive";
     public static final String LANGUAGE_FILES_NAME = "languageFiles_OTM";
 
     private String description = null;
     private String fileName = null;
     private String filePath = null;
     private Boolean explodeArchive = null;
     private RESTLanguageFileCollectionV1 languageFiles_OTM = null;
     /**
      * A list of the Envers revision numbers
      */
     private RESTFileCollectionV1 revisions = null;
 
     @Override
     public RESTFileCollectionV1 getRevisions() {
         return revisions;
     }
 
     @Override
     public void setRevisions(final RESTFileCollectionV1 revisions) {
         this.revisions = revisions;
     }
 
 
     @Override
     public RESTFileV1 clone(boolean deepCopy) {
         final RESTFileV1 retValue = new RESTFileV1();
 
         this.cloneInto(retValue, deepCopy);
 
         return retValue;
     }
 
     public void cloneInto(final RESTFileV1 clone, final boolean deepCopy) {
         super.cloneInto(clone, deepCopy);
 
         clone.description = this.description;
         clone.fileName = this.fileName;
         clone.filePath = this.filePath;
 
         if (deepCopy) {
             if (this.languageFiles_OTM != null) {
                 clone.languageFiles_OTM = new RESTLanguageFileCollectionV1();
                 this.languageFiles_OTM.cloneInto(clone.languageFiles_OTM, deepCopy);
             }
 
             if (this.getRevisions() != null) {
                 clone.revisions = new RESTFileCollectionV1();
                 this.revisions.cloneInto(clone.revisions, deepCopy);
             }
         } else {
             clone.languageFiles_OTM = this.languageFiles_OTM;
             clone.revisions = this.revisions;
         }
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(final String description) {
         this.description = description;
     }
 
     public void explicitSetDescription(final String description) {
         this.description = description;
         this.setParameterToConfigured(DESCRIPTION_NAME);
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(final String fileName) {
         this.fileName = fileName;
     }
 
     public void explicitSetFileName(final String fileName) {
         this.fileName = fileName;
         this.setParameterToConfigured(FILE_NAME);
     }
 
     public String getFilePath() {
         return filePath;
     }
 
     public void setFilePath(final String filePath) {
         this.filePath = filePath;
     }
 
     public void explicitSetFilePath(final String filePath) {
         this.filePath = filePath;
         this.setParameterToConfigured(FILE_PATH_NAME);
     }
 
     public RESTLanguageFileCollectionV1 getLanguageFiles_OTM() {
         return languageFiles_OTM;
     }
 
     public void setLanguageFiles_OTM(RESTLanguageFileCollectionV1 languageFiles_OTM) {
         this.languageFiles_OTM = languageFiles_OTM;
     }
 
     public void explicitSetLanguageFiles_OTM(RESTLanguageFileCollectionV1 languageFiles_OTM) {
         this.languageFiles_OTM = languageFiles_OTM;
         this.setParameterToConfigured(LANGUAGE_FILES_NAME);
     }
 
     public Boolean getExplodeArchive() {
         return explodeArchive;
     }
 
     public void setExplodeArchive(Boolean explodeArchive) {
         this.explodeArchive = explodeArchive;
     }
 
     public void explicitSetExplodeArchive(Boolean explodeArchive) {
         this.explodeArchive = explodeArchive;
         setParameterToConfigured(EXPLODE_ARCHIVE_NAME);
     }
 
     @Override
     public boolean equals(final Object other) {
         if (other == null) return false;
         if (this == other) return true;
         if (!(other instanceof RESTFileV1)) return false;
 
         return super.equals(other);
     }
 }
