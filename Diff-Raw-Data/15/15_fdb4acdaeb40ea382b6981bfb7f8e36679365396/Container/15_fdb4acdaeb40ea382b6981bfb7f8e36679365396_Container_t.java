 package interiores.data.objects;
 
import java.util.ArrayList;
 import java.util.List;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author hector
  */
 @XmlRootElement
 public class Container
 {
     @XmlElement
     private Simple a;
     
     @XmlElementWrapper
     @XmlElement(name = "simple")
     private List<Simple> list;
     
     public Container() {
        a = new Simple();
        list = new ArrayList();
     }
     
     public Container(Simple a, List<Simple> list) {
         this.a = a;
         this.list = list;
     }
     
     public boolean equals(Container c) {
         if(! a.equals(c.a))
             return false;
         
         for(int i = 0; i < list.size(); i++) {
             if(! list.get(i).equals(c.list.get(i)))
                 return false;
         }
                 
         return true;
     }
 }
