 package interiores.utils;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author hector
  */
 @XmlRootElement
 public class Range {
     @XmlAttribute
     public int min;
     
     @XmlAttribute
     public int max;
     
     public Range() {
         this(0, 0);
     }
     
     public Range(int min, int max) {
         this.min = min;
         this.max = max;
     }
     
     @Override
     public String toString() {
        return "min=" + min + ", max=" + max;
     }
 }
