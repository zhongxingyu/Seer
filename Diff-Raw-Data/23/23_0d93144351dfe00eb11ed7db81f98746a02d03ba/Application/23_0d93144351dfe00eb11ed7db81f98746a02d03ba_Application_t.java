 package com.hawkware.apollo.client.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement(name = "application")
 public class Application {
     private List<Property> properties = new ArrayList<Property>();
 
     @XmlElements(value = { @XmlElement(name = "property", type = Property.class) })
     public List<Property> getProperties() {
 	return properties;
     }
 
     public void setProperties(List<Property> properties) {
 	this.properties = properties;
     }
 
     @Override
     public int hashCode() {
 	final int prime = 31;
 	int result = 1;
 	result = prime * result + ((properties == null) ? 0 : properties.hashCode());
 	return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 	if (this == obj)
 	    return true;
 	if (obj == null)
 	    return false;
 	if (getClass() != obj.getClass())
 	    return false;
 	Application other = (Application) obj;
 	if (properties == null) {
 	    if (other.properties != null)
 		return false;
 	} else if (!properties.equals(other.properties))
 	    return false;
 	return true;
     }
 
     @Override
     public String toString() {
 	return "Application [properties=" + properties + "]";
     }
 
 }
