 package org.jcouchdb.document;
 
 import org.jcouchdb.util.Base64Util;
 import org.svenson.JSONProperty;
 
 /**
  * Represents a couchdb document attachment.
  * The attachment mechanism mirrors the functionality present in couchdb itself.
  * You will only be able to indirectly use the data property contained in {@link Attachment} to create
  * attachments inlined with the document. When you query a document with attachments, the attachments will
  * have a <code>null</code> data property and the stub property will be set to <code>true</code>.
  *
  * This limitation has its origin in the way couchdb works and is deliberately kept that way to not introduce additional queries.
  * 
  * @author fforw at gmx dot de
  *
  */
 public class Attachment
 {
     private String contentType, data;
     private long length;
     private boolean stub;
    private String revPos;
     
     public Attachment()
     {
 
     }
 
     public Attachment(String contentType, byte[] data)
     {
         this.contentType = contentType;
         this.data = Base64Util.encodeBase64(data);
     }
 
 
     /**
      * Returns the media type of this attachment.
      * @return media type 
      */
     @JSONProperty("content_type")
     public String getContentType()
     {
         return contentType;
     }
 
     /**
      * Sets the media type for this attachment.
      * @return media type 
      */
     public void setContentType(String contentType)
     {
         this.contentType = contentType;
     }
 
     /**
      * Returns the Base64 encoded representation of this attachment's content. <em>note that you cannot read attachment data this way</em>. the property is 
      * only used internally to create inlined attachments and is set to <code>null</code> when reading documents.
      * 
      * This limitation has its origin in the way couchdb works and is deliberately kept that way to not introduce additional queries.
      * @return
      */
     public String getData()
     {
         return data;
     }
 
     public void setData(String data)
     {
         this.data = data;
     }
 
     /**
      * Returns the length of the attachment in bytes.
      * @return
      */
     public long getLength()
     {
         return length;
     }
 
     /**
      * Sets the length of the attachments in bytes
      * @param length
      */
     public void setLength(long length)
     {
         this.length = length;
     }
 
     /**
      * This is set to <code>true</code> by couchdb to signal that the attachment instance is just a stub without any data.
      * @return
      */
     public boolean isStub()
     {
         return stub;
     }
 
     public void setStub(boolean stub)
     {
         this.stub = stub;
     }
 
     /**
      * Returns the revision position of this attachment that will change if the attachment gets updated.
      * @param revPos
      */
    public String getRevPos()
     {
         return revPos;
     }
 
     /**
      * Sets the revision position of this attachment.
      * @param revPos
      */
     @JSONProperty("revpos")
    public void setRevPos(String revPos)
     {
         this.revPos = revPos;
     }
     
     
 }
