 package ru.spbau.bioinf.mzpeak;
 
import org.jdom.Element;
import ru.spbau.bioinf.tagfinder.util.XmlUtil;

 public class MzPoint implements Comparable<MzPoint> {
 
     private double mass;
     private double intencity;
 
     public MzPoint(double mass, double intencity) {
         this.mass = mass;
         this.intencity = intencity;
     }
 
     public double getMass() {
         return mass;
     }
 
     public double getIntencity() {
         return intencity;
     }
 
     public int compareTo(MzPoint o) {
         double diff = mass - o.mass;
         if (diff < 0) {
             return -1;
         } else if (diff > 0) {
             return 1;
         }
         return 0;
     }
    
    public Element toXml() {
        Element xml = new Element("mzpeak");
        XmlUtil.addElement(xml, "mass", mass);
        XmlUtil.addElement(xml, "intencity", intencity);
        return xml;
    }
 }
