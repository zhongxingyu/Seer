 // Copyright (C) 2012, The SAVI Project.
 package ca.savi.camel.model;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 /**
  * <p>
  * Java class for anonymous complex type.
  * <p>
  * @author Eliot J. Kang <eliot@savinetwork.ca>
  * @version 0.4
  */
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "networkPort", "successful", "error",
    "xmlString" })
 @XmlRootElement(name = "connectToNetworkResponse")
 public class ConnectToNetworkResponse {
  protected int networkPort;
   protected boolean successful;
   protected String error;
   protected String xmlString;
 
   /**
   * Gets the value of the networkPort property.
   */
  public int getNetworkPort() {
    return networkPort;
  }

  /**
   * Sets the value of the networkPort property.
   */
  public void setNetworkPort(int networkPort) {
    this.networkPort = networkPort;
  }

  /**
    * Gets the value of the successful property.
    */
   public boolean isSuccessful() {
     return successful;
   }
 
   /**
    * Sets the value of the successful property.
    */
   public void setSuccessful(boolean value) {
     this.successful = value;
   }
 
   /**
    * Gets the value of the error property.
    * @return possible object is {@link String }
    */
   public String getError() {
     return error;
   }
 
   /**
    * Sets the value of the error property.
    * @param value allowed object is {@link String }
    */
   public void setError(String value) {
     this.error = value;
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
 }
