 package interiores.data.objects;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author hector
  */
 @XmlRootElement
 public class Simple {
     @XmlAttribute
     private int a;
     
     @XmlAttribute
     private int b;
     
     @XmlAttribute
     private String c;
     
     public Simple() {
        a = b = 0;
        c = "Default value!";
     }
     
     public Simple(int a, int b, String c) {
         this.a = a;
         this.b = b;
         this.c = c;
     }
     
     public boolean equals(Simple s) {
         return (s.a == a && s.b == b && s.c.equals(c));
     }
 }
