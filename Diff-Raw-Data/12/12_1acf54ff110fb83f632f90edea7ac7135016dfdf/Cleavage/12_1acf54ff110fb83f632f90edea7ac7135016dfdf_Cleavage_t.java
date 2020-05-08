 package ru.spbau.bioinf.palign;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jdom.Element;
 import ru.spbau.bioinf.tagfinder.util.XmlUtil;
 
 public class Cleavage {
     
     private int position;
     private int direction;
 
     private double modification = 0;
 
     public Cleavage(int position, int direction, double modification) {
         this.position = position;
         this.direction = direction;
         this.modification = modification;
     }
 
     private List<PeptideSupport> supports = new ArrayList<PeptideSupport>();
 
     public void addSupport(PeptideSupport support) {
         supports.add(support);
     }
 
     public List<PeptideSupport> getSupports() {
         return supports;
     }
 
    public int getPosition() {
        return position;
    }

    public int getDirection() {
        return direction;
    }

    public double getModification() {
        return modification;
    }

     public Element toXml() {
         Element xml = new Element("cleavage");
         XmlUtil.addElement(xml, "position", position);
         XmlUtil.addElement(xml, "direction", direction);
         XmlUtil.addElement(xml, "modification", modification);        
         for (PeptideSupport support : supports) {
             xml.addContent(support.toXml());
         }
         return xml;
     }
 }
