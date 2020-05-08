 package com.testdroid.api;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 /**
  *
 * @author Sławomir
  */
 @XmlRootElement
 @JsonIgnoreProperties(value = {"id", "hasId"})
 public class APIArray<T> extends APIEntity {
 
     private T[] items;
 
     public APIArray() {
     }
 
     public APIArray(T[] values) {
         this.items = values;
     }
 
     @XmlElement(name = "item")
     public T[] getItems() {
         return items;
     }
 
     public void setItems(T[] items) {
         this.items = items;
     }
 
 }
