 package org.aptrust.client.impl;
 
 import org.aptrust.common.solr.AptrustSolrDocument;
 import org.aptrust.common.solr.SolrField;
 
 /**
  * Data describing an object associated with a package.
  * @author Daniel Bernstein
  *
  */
 public class ObjectDescriptor {
     private String id;
     private String title;
 
     public ObjectDescriptor() {
     }
 
     public ObjectDescriptor(String id) {
         this(id, id);
     }
 
     public ObjectDescriptor(String id, String title) {
         this.id = id;
         this.title = title;
     }
     
     public String getId() {
         return id;
     }
 
     @SolrField(name=AptrustSolrDocument.ID)
     public void setId(String id) {
         this.id = id;
     }
     
     public String getLocalId() {
        return id.substring(id.indexOf(':') + 1);
     }
 
     public String getTitle() {
         return title;
     }
 
     @SolrField(name=AptrustSolrDocument.TITLE)
     public void setTitle(String title) {
         this.title = title;
     }
 }
