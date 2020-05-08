 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Enriko Käsper
  */
 package eionet.webq.dto;
 
 import java.util.Date;
 
 /**
  * Data transfer object to pass uploaded file data across application.
  */
 public class UploadedXmlFile {
     /**
      * File id in data storage.
      */
     private int id;
     /**
      * File name.
      */
     private String name;
     /**
      * File content bytes.
      */
     private byte[] content;
     /**
      * Xml schema name extracted during conversion.
      * @see eionet.webq.converter.MultipartFileConverter
      */
     private String xmlSchema;
     /**
      * File size in bytes.
      */
     private long sizeInBytes;
     /**
      * File upload date.
      */
     private Date created;
     /**
      * Last change date.
      */
     private Date updated;
 
     public int getId() {
         return id;
     }
 
     /**
      * Id setter.
      * @param id
      *            file id
      * @return current object for method call chaining
      */
     public UploadedXmlFile setId(int id) {
         this.id = id;
         return this;
     }
 
     public String getName() {
         return name;
     }
 
     /**
      * File name setter.
      * @param name
      *            file name
      * @return current object for method call chaining
      */
     public UploadedXmlFile setName(String name) {
         this.name = name;
         return this;
     }
 
     public byte[] getContent() {
         return content;
     }
 
     /**
      * File content in bytes setter.
      * @param content
      *            file content
      * @return current object for method call chaining
      */
     public UploadedXmlFile setContent(byte[] content) {
         this.content = content;
         return this;
     }
 
     public long getSizeInBytes() {
         return sizeInBytes;
     }
 
     /**
      * File size in bytes.
      * @param sizeInBytes
      *            file size in bytes
      * @return current object for method call chaining
      */
     public UploadedXmlFile setSizeInBytes(long sizeInBytes) {
         this.sizeInBytes = sizeInBytes;
         return this;
     }
 
     /**
      * @return the xmlSchema
      */
     public String getXmlSchema() {
         return xmlSchema;
     }
 
     /**
      * @param xmlSchema
      *            the xmlSchema to set
      * @return current object for method call chaining
      * */
     public UploadedXmlFile setXmlSchema(String xmlSchema) {
         this.xmlSchema = xmlSchema;
         return this;
     }
 
     public Date getCreated() {
         return created;
     }
 
     /**
      * Created date setter.
      * @param created
      *            date file was uploaded
      * @return current object for method call chaining
      */
     public UploadedXmlFile setCreated(Date created) {
         this.created = created;
         return this;
     }
 
     public Date getUpdated() {
         return updated;
     }
 
     /**
      * Updated date setter.
      * @param updated
      *            date when file was updated last time
      * @return current object for method call chaining
      */
     public UploadedXmlFile setUpdated(Date updated) {
         this.updated = updated;
         return this;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("UploadedXmlFile{");
         sb.append("id=").append(id);
         sb.append(", name='").append(name).append('\'');
        sb.append(", contentLength=").append(content != null ? content.length : null);
         sb.append(", xmlSchema='").append(xmlSchema).append('\'');
         sb.append(", sizeInBytes=").append(sizeInBytes);
         sb.append(", created=").append(created);
         sb.append(", updated=").append(updated);
         sb.append('}');
         return sb.toString();
     }
 }
