 // Copyright (C) 2012, The SAVI Project.
 package ca.savi.camel.model;
 
 import java.math.BigInteger;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 
 /**
  * <p>
  * Java class for anonymous complex type.
  * <p>
  * @author Eliot J. Kang <eliot@savinetwork.ca>
  * @version 0.4
  */
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "authentication", "duration", "xmlString",
    "qinqTag" })
 @XmlRootElement(name = "getRequest")
 public class GetRequest {
   @XmlElement(required = true)
   protected SecurityObject authentication;
   @XmlSchemaType(name = "unsignedLong")
   protected BigInteger duration;
   protected String xmlString;
   @XmlSchemaType(name = "unsignedInt")
   protected long qinqTag;
 
   /**
    * Gets the value of the authentication property.
    * @return possible object is {@link SecurityObject }
    */
   public SecurityObject getAuthentication() {
     return authentication;
   }
 
   /**
    * Sets the value of the authentication property.
    * @param value allowed object is {@link SecurityObject }
    */
   public void setAuthentication(SecurityObject value) {
     this.authentication = value;
   }
 
   /**
    * Gets the value of the duration property.
    * @return possible object is {@link BigInteger }
    */
   public BigInteger getDuration() {
     return duration;
   }
 
   /**
    * Sets the value of the duration property.
    * @param value allowed object is {@link BigInteger }
    */
   public void setDuration(BigInteger value) {
     this.duration = value;
   }
 
   /**
    * Gets the value of the xmlString property.
    * @return possible object is {@link String }
    */
   public String getXmlString() {
     return xmlString;
   }
 
   /**
    * Sets the value of the xmlString property.
    * @param value allowed object is {@link String }
    */
   public void setXmlString(String value) {
     this.xmlString = value;
   }
 
   /**
    * Gets the value of the qinqTag property.
    */
   public long getQinqTag() {
     return qinqTag;
   }
 
   /**
    * Sets the value of the qinqTag property.
    */
   public void setQinqTag(long value) {
     this.qinqTag = value;
   }
 }
