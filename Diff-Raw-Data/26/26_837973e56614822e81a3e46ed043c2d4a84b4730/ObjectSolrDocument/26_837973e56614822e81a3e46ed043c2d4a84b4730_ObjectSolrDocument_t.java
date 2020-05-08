 package org.aptrust.ingest.ips.solr;
 
 import org.aptrust.common.solr.AptrustSolrDocument;
 import org.aptrust.common.solr.SolrField;
 import org.aptrust.ingest.api.IngestPackage;
 
 /**
  * A simple class that wraps an IngestPackage and and object pid and exposes
  * annotated methods that allow for easy creation of Solr Documents for
  * "object" records.
  * 
  * @see org.aptrust.common.solr.AptrustSolrDocument#createValidSolrDocument(Object)
  */
 public class ObjectSolrDocument {
 
     private IngestPackage p;
 
     private String pid;
 
     private String title;
 
     public ObjectSolrDocument(String objectPid, IngestPackage ingestPackage, String title) {
         p = ingestPackage;
         pid = objectPid;
         this.title = title;
     }
 
     @SolrField(name=AptrustSolrDocument.RECORD_TYPE)
     public String getRecordType() {
         return "object";
     }
 
     @SolrField(name=AptrustSolrDocument.INSTITUTION_ID)
     public String getInstitutionId() {
         return p.getMetadata().getInstitution();
     }
 
     @SolrField(name=AptrustSolrDocument.ID)
     public String getId() {
        return p.getMetadata().getInstitution() + "-object:" + pid;
     }
 
     @SolrField(name=AptrustSolrDocument.PACKAGE_ID)
     public String getPackageId() {
         return p.getMetadata().getId();
     }
 
     @SolrField(name=AptrustSolrDocument.TITLE)
     public String getTitle() {
         return title;
     }
 }
