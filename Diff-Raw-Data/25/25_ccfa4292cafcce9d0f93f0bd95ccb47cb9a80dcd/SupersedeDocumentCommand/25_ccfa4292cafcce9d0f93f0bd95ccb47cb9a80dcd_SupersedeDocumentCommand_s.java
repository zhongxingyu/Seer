 package com.mlj.documents;
 
 public class SupersedeDocumentCommand {
 
    private String documentId;
     private String documentNo;
     private String title;
     private String revision;
 

    public String getDocumentId() {
        return documentId;
    }

     public String getDocumentNo() {
         return documentNo;
     }
 
     public void setDocumentNo(String documentNo) {
         this.documentNo = documentNo;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getRevision() {
         return revision;
     }
 
     public void setRevision(String revision) {
         this.revision = revision;
     }
 
 }
