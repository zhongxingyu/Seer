 package osf.poc.model;
 
 import java.io.Serializable;
 
 /**
  * Main model class representing a configuration property
  */
 public class Property implements Serializable {
    private static final long serialVersionUID = 1L;
     
     private String name;
     private String value;
     
     public Property() {
         // Nothing to do
     }
     
     public Property(String name, String value) {
         this.name = name;
         this.value = value;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 }
