 package org.aptrust.common.solr;
 
 import java.util.Date;
 
 import org.aptrust.common.solr.AptrustSolrDocument;
 import org.aptrust.common.solr.SolrField;
 
 public class ContentSolrDocument {
 
     private String institutionId;
 
     private String packageId;
 
     private String objectId;
 
     private String contentId;
 
     private boolean failed;
 
     private Date healthCheckDate;
 
     public ContentSolrDocument() {
     }
 
     public ContentSolrDocument(String institutionId, String packageId, String objectId, String contentId, boolean passed, Date date) {
         this.institutionId = institutionId;
         this.packageId = packageId;
         this.objectId = objectId;
         this.contentId = contentId;
         failed = !passed;
         healthCheckDate = date;
     }
 
     @SolrField(name=AptrustSolrDocument.RECORD_TYPE)
     public String getRecordType() {
         return "content";
     }
 
     @SolrField(name=AptrustSolrDocument.INSTITUTION_ID)
     public String getInstitutionId() {
         return institutionId; 
     }
 
     @SolrField(name=AptrustSolrDocument.INSTITUTION_ID)
     public void setInstitutionId(String id) {
         institutionId = id;
     }
 
     @SolrField(name=AptrustSolrDocument.PACKAGE_ID)
     public String getPackageId() {
         return packageId;
     }
 
     @SolrField(name=AptrustSolrDocument.PACKAGE_ID)
     public void setPackageId(String id) {
         packageId = id;
     }
 
     @SolrField(name=AptrustSolrDocument.OBJECT_ID)
     public String getObjectId() {
         return objectId;
     }
 
     @SolrField(name=AptrustSolrDocument.OBJECT_ID)
     public void setObjectId(String id) {
         objectId = id;
     }
 
     public String getContentId() {
         return contentId;
     }
 
     @SolrField(name=AptrustSolrDocument.ID)
     public String getId() {
         return getId(institutionId, contentId);
     }
 
     @SolrField(name=AptrustSolrDocument.ID)
     public void setId(String id) {
        contentId = id.substring(id.indexOf('-') + 1);
     }
 
     @SolrField(name=AptrustSolrDocument.FAILED_HEALTH_CHECK)
     public String getFailedHealthCheck() {
         return String.valueOf(failed);
     }
 
     @SolrField(name=AptrustSolrDocument.FAILED_HEALTH_CHECK)
     public void setFailedHealthCheck(String value) {
         failed = Boolean.parseBoolean(value);
     }
 
     @SolrField(name=AptrustSolrDocument.LAST_HEALTH_CHECK_DATE)
     public Date getHealthCheckDate() {
         return healthCheckDate;
     }
 
     @SolrField(name=AptrustSolrDocument.LAST_HEALTH_CHECK_DATE)
     public void setHealthCheckDate(Date date) {
         healthCheckDate = date;
     }
 
     public static String getId(String institutionId, String contentId) {
        return institutionId + "-" + contentId;
     }
 }
