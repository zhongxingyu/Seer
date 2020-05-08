 
 package com.janpix.webclient.repodoc;
 
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 
 /**
  * <p>Java class for fileAttributesDTO complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType name="fileAttributesDTO">
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *         &lt;element name="repositoryId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="filename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="mimeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *         &lt;element name="creationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
  *         &lt;element name="fileHash" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "fileAttributesDTO", propOrder = {
     "uuid",
     "repositoryId",
     "filename",
     "mimeType",
     "creationTime",
     "fileHash",
     "size"
 })
 public class FileAttributesDTO {
 
     @XmlElement(required = true)
     protected String uuid;
     protected String repositoryId;
     protected String filename;
     @XmlElement(required = true)
     protected String mimeType;
     @XmlSchemaType(name = "dateTime")
     protected Date creationTime;
     protected String fileHash;
     protected Long size;
 
     /**
      * Gets the value of the uuid property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getUuid() {
         return uuid;
     }
 
     /**
      * Sets the value of the uuid property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setUuid(String value) {
         this.uuid = value;
     }
 
     /**
      * Gets the value of the repositoryId property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getRepositoryId() {
         return repositoryId;
     }
 
     /**
      * Sets the value of the repositoryId property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setRepositoryId(String value) {
         this.repositoryId = value;
     }
 
     /**
      * Gets the value of the filename property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getFilename() {
         return filename;
     }
 
     /**
      * Sets the value of the filename property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setFilename(String value) {
         this.filename = value;
     }
 
     /**
      * Gets the value of the mimeType property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getMimeType() {
         return mimeType;
     }
 
     /**
      * Sets the value of the mimeType property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setMimeType(String value) {
         this.mimeType = value;
     }
 
     /**
      * Gets the value of the creationTime property.
      * 
      * @return
      *     possible object is
      *     {@link XMLGregorianCalendar }
      *     
      */
    public XMLGregorianCalendar getCreationTime() {
         return creationTime;
     }
 
     /**
      * Sets the value of the creationTime property.
      * 
      * @param value
      *     allowed object is
      *     {@link XMLGregorianCalendar }
      *     
      */
    public void setCreationTime(XMLGregorianCalendar value) {
         this.creationTime = value;
     }
 
     /**
      * Gets the value of the fileHash property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getFileHash() {
         return fileHash;
     }
 
     /**
      * Sets the value of the fileHash property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setFileHash(String value) {
         this.fileHash = value;
     }
 
     /**
      * Gets the value of the size property.
      * 
      * @return
      *     possible object is
      *     {@link Long }
      *     
      */
     public Long getSize() {
         return size;
     }
 
     /**
      * Sets the value of the size property.
      * 
      * @param value
      *     allowed object is
      *     {@link Long }
      *     
      */
     public void setSize(Long value) {
         this.size = value;
     }
 
 }
