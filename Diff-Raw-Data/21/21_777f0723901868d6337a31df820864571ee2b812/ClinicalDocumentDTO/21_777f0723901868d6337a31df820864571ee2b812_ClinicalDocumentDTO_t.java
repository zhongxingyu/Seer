 
 package com.janpix.webclient.repodoc;
 
 import java.util.Date;
 
 import javax.activation.DataHandler;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlMimeType;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 
 /**
  * <p>Java class for clinicalDocumentDTO complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType name="clinicalDocumentDTO">
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="uniqueId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *         &lt;element name="fileAttributes" type="{jpx:ar.com.janpix}fileAttributesDTO"/>
  *         &lt;element name="documentCreationStarted" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
  *         &lt;element name="documentCreationEnded" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
  *         &lt;element name="patientId" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *         &lt;element name="author" type="{jpx:ar.com.janpix}authorDTO"/>
  *         &lt;element name="stateDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
  *         &lt;element name="typeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="formatName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="availabilityStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="option" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="binaryData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "clinicalDocumentDTO", propOrder = {
     "uniqueId",
     "name",
     "fileAttributes",
     "documentCreationStarted",
     "documentCreationEnded",
     "patientId",
     "author",
     "stateDescription",
     "comments",
     "language",
     "typeId",
     "typeName",
     "formatName",
     "availabilityStatus",
     "option",
     "binaryData"
 })
 public class ClinicalDocumentDTO {
 
     protected String uniqueId;
     @XmlElement(required = true)
     protected String name;
     @XmlElement(required = true)
     protected FileAttributesDTO fileAttributes;
     @XmlElement(required = true)
     @XmlSchemaType(name = "dateTime")
     protected Date documentCreationStarted;
     @XmlSchemaType(name = "dateTime")
     protected Date documentCreationEnded;
     @XmlElement(required = true)
     protected String patientId;
     @XmlElement(required = true)
     protected AuthorDTO author;
     protected String stateDescription;
     protected String comments;
     protected String language;
     protected Long typeId;
     protected String typeName;
     protected String formatName;
     protected String availabilityStatus;
     protected String option;
     @XmlMimeType("application/octet-stream")
     protected DataHandler binaryData;
 
     /**
      * Gets the value of the uniqueId property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getUniqueId() {
         return uniqueId;
     }
 
     /**
      * Sets the value of the uniqueId property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setUniqueId(String value) {
         this.uniqueId = value;
     }
 
     /**
      * Gets the value of the name property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the value of the name property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setName(String value) {
         this.name = value;
     }
 
     /**
      * Gets the value of the fileAttributes property.
      * 
      * @return
      *     possible object is
      *     {@link FileAttributesDTO }
      *     
      */
     public FileAttributesDTO getFileAttributes() {
         return fileAttributes;
     }
 
     /**
      * Sets the value of the fileAttributes property.
      * 
      * @param value
      *     allowed object is
      *     {@link FileAttributesDTO }
      *     
      */
     public void setFileAttributes(FileAttributesDTO value) {
         this.fileAttributes = value;
     }
 
     /**
      * Gets the value of the documentCreationStarted property.
      * 
      * @return
      *     possible object is
      *     {@link XMLGregorianCalendar }
      *     
      */
    public Date getDocumentCreationStarted() {
         return documentCreationStarted;
     }
 
     /**
      * Sets the value of the documentCreationStarted property.
      * 
      * @param value
      *     allowed object is
      *     {@link XMLGregorianCalendar }
      *     
      */
    public void setDocumentCreationStarted(Date value) {
         this.documentCreationStarted = value;
     }
 
     /**
      * Gets the value of the documentCreationEnded property.
      * 
      * @return
      *     possible object is
      *     {@link XMLGregorianCalendar }
      *     
      */
    public Date getDocumentCreationEnded() {
         return documentCreationEnded;
     }
 
     /**
      * Sets the value of the documentCreationEnded property.
      * 
      * @param value
      *     allowed object is
      *     {@link XMLGregorianCalendar }
      *     
      */
    public void setDocumentCreationEnded(Date value) {
         this.documentCreationEnded = value;
     }
 
     /**
      * Gets the value of the patientId property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getPatientId() {
         return patientId;
     }
 
     /**
      * Sets the value of the patientId property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setPatientId(String value) {
         this.patientId = value;
     }
 
     /**
      * Gets the value of the author property.
      * 
      * @return
      *     possible object is
      *     {@link AuthorDTO }
      *     
      */
     public AuthorDTO getAuthor() {
         return author;
     }
 
     /**
      * Sets the value of the author property.
      * 
      * @param value
      *     allowed object is
      *     {@link AuthorDTO }
      *     
      */
     public void setAuthor(AuthorDTO value) {
         this.author = value;
     }
 
     /**
      * Gets the value of the stateDescription property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getStateDescription() {
         return stateDescription;
     }
 
     /**
      * Sets the value of the stateDescription property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setStateDescription(String value) {
         this.stateDescription = value;
     }
 
     /**
      * Gets the value of the comments property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getComments() {
         return comments;
     }
 
     /**
      * Sets the value of the comments property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setComments(String value) {
         this.comments = value;
     }
 
     /**
      * Gets the value of the language property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getLanguage() {
         return language;
     }
 
     /**
      * Sets the value of the language property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setLanguage(String value) {
         this.language = value;
     }
 
     /**
      * Gets the value of the typeId property.
      * 
      * @return
      *     possible object is
      *     {@link Long }
      *     
      */
     public Long getTypeId() {
         return typeId;
     }
 
     /**
      * Sets the value of the typeId property.
      * 
      * @param value
      *     allowed object is
      *     {@link Long }
      *     
      */
     public void setTypeId(Long value) {
         this.typeId = value;
     }
 
     /**
      * Gets the value of the typeName property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getTypeName() {
         return typeName;
     }
 
     /**
      * Sets the value of the typeName property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setTypeName(String value) {
         this.typeName = value;
     }
 
     /**
      * Gets the value of the formatName property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getFormatName() {
         return formatName;
     }
 
     /**
      * Sets the value of the formatName property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setFormatName(String value) {
         this.formatName = value;
     }
 
     /**
      * Gets the value of the availabilityStatus property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getAvailabilityStatus() {
         return availabilityStatus;
     }
 
     /**
      * Sets the value of the availabilityStatus property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setAvailabilityStatus(String value) {
         this.availabilityStatus = value;
     }
 
     /**
      * Gets the value of the option property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getOption() {
         return option;
     }
 
     /**
      * Sets the value of the option property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setOption(String value) {
         this.option = value;
     }
 
     /**
      * Gets the value of the binaryData property.
      * 
      * @return
      *     possible object is
      *     {@link DataHandler }
      *     
      */
     public DataHandler getBinaryData() {
         return binaryData;
     }
 
     /**
      * Sets the value of the binaryData property.
      * 
      * @param value
      *     allowed object is
      *     {@link DataHandler }
      *     
      */
     public void setBinaryData(DataHandler value) {
         this.binaryData = value;
     }
 
 }
