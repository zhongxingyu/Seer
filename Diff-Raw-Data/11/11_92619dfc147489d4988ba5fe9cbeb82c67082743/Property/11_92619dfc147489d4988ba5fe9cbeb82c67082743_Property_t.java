 package osf.poc.vaadin.model;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement
 public class Property {
     private String name;
    private String data;
     
     public Property(){
         super();
         
         //Nothing
     }
     
     public Property(String name, String value){
         super();
         
         this.name = name;
        this.data = value;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getValue() {
        return data;
     }
 
     public void setValue(String value) {
        this.data = value;
     }
 }
