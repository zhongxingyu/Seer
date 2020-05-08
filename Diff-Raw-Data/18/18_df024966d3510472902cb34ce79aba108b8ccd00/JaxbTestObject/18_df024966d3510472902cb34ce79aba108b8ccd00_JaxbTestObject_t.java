 package de.openknowledge.extensions.jsf.jaxb;
 
 import static org.apache.commons.lang3.Validate.notNull;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement
 public class JaxbTestObject {
 
   @XmlAttribute
   private String testAttribute;
  @XmlElement
   private List<Integer> testValues;
 
   protected JaxbTestObject() {
     // for JAXB
   }
 
   public JaxbTestObject(String attribute, List<Integer> values) {
     testAttribute = notNull(attribute);
     testValues = Collections.unmodifiableList(new ArrayList<Integer>(values));
   }
 
   public String getTestAttribute() {
     return testAttribute;
   }
 
   public List<Integer> getTestValues() {
     return testValues;
   }
 }
