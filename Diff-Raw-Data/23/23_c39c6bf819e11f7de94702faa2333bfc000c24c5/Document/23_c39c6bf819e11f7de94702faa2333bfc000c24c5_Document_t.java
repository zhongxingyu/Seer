 package com.sunnyd.models;
 
 import java.util.Date;
 import java.util.HashMap;
 import com.sunnyd.Base;
 import com.sunnyd.IModel;
 import com.sunnyd.annotations.ActiveRelationHasOne;
 import com.sunnyd.annotations.ActiveRecordField;
 import com.sunnyd.database.Manager;
 
 public class Document extends Base implements IModel {
     public static final String tableName = "documents";
 
     @ActiveRecordField
     private String docName;
     @ActiveRecordField
     private String thumbnailPath;
     @ActiveRecordField
     private Date lastModifiedDate;
     @ActiveRecordField
     private Date creationDate;
     @ActiveRelationHasOne
     private Peer peer;
     @ActiveRecordField
     private Integer peerId;
     
     
     public Document() {
         super();
     }
 
     public Document(HashMap<String, Object> HM) {
         super(HM);
     }
 
     public String getDocName() {
         return docName;
     }
 
     public Peer getPeer(){
         initRelation("peer");
 //        if(peer == null){
 //            HashMap<String, Object> foundPeer = Manager.find(peerId, "peers");
 //            this.peer = new Peer(foundPeer);
 //        }
         return peer;
     }
     
     public void setPeerId(Integer peerId){
         this.peerId = peerId;
     }
     
     public int getPeerId(){
         return this.peerId;
     }
     
     public void setDocName(String docName) {
         this.docName = docName;
         setUpdateFlag(true);
     }
 
     public String getThumbnailPath() {
         return thumbnailPath;
     }
 
     public void setThumbnailPath(String thumbnailPath) {
         this.thumbnailPath = thumbnailPath;
         setUpdateFlag(true);
     }
     
     public static void main(String[] args) {
        Document a = Document.find(2);
        System.out.println(a.getPeer().getFirstName());
        System.out.println(a.getPeer().getLastName());
     }
 
     public Date getLastModifiedDate() {
         return lastModifiedDate;
     }
 
     public void setLastModifiedDate(Date lastModifiedDate) {
         this.lastModifiedDate = lastModifiedDate;
         setUpdateFlag(true);
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
         setUpdateFlag(true);
     }
     
     
 
 }
