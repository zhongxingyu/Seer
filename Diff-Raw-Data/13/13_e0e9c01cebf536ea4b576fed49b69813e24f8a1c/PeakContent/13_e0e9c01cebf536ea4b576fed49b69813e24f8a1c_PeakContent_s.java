 package ru.spbau.bioinf.tagfinder.view;
 
 import org.jdom.Element;
 import ru.spbau.bioinf.tagfinder.Peak;
 import ru.spbau.bioinf.tagfinder.util.XmlUtil;
 
 public class PeakContent implements Content {
     private Peak peak;
 
     public PeakContent(Peak peak) {
         this.peak = peak;
     }
 
     @Override
     public Element toXml() {
         Element ans = new Element("peak");
         XmlUtil.addElement(ans, "value", peak.getValue());
         XmlUtil.addElement(ans, "mass", peak.getMass());
         XmlUtil.addElement(ans, "type", peak.getPeakType().name());
         return ans;
     }
 }
