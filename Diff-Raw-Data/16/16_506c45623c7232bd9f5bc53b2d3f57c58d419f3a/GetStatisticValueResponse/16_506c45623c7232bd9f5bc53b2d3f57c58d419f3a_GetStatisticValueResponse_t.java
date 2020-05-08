 
package com.fluidtime.exampleMVC.statistic_client.org.tempuri;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 
 /**
  * <p>Java class for anonymous complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType>
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="getStatisticValueResult" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "", propOrder = {
     "getStatisticValueResult"
 })
 @XmlRootElement(name = "getStatisticValueResponse")
 public class GetStatisticValueResponse {
 
     protected Double getStatisticValueResult;
 
     /**
      * Gets the value of the getStatisticValueResult property.
      * 
      * @return
      *     possible object is
      *     {@link Double }
      *     
      */
     public Double getGetStatisticValueResult() {
         return getStatisticValueResult;
     }
 
     /**
      * Sets the value of the getStatisticValueResult property.
      * 
      * @param value
      *     allowed object is
      *     {@link Double }
      *     
      */
     public void setGetStatisticValueResult(Double value) {
         this.getStatisticValueResult = value;
     }
 
 }
