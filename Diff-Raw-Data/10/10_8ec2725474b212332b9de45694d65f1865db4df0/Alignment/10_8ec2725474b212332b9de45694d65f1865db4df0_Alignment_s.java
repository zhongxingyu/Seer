 package ru.spbau.bioinf.palign;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.jdom.Element;
 import ru.spbau.bioinf.tagfinder.Peak;
 import ru.spbau.bioinf.tagfinder.Protein;
 import ru.spbau.bioinf.tagfinder.Scan;
 import ru.spbau.bioinf.tagfinder.util.XmlUtil;
 
 public class Alignment {
     private Scan scan;
     private Protein protein;
 
     public Alignment(Scan scan, Protein protein) {
         this.scan = scan;
         this.protein = protein;
     }
 
     private List<Cleavage> cleavages = new ArrayList<Cleavage>();
 
     public void addCleavage(Cleavage cleavage) {
         cleavages.add(cleavage);
     }
 
     public List<Cleavage> getCleavages() {
         return cleavages;
     }
 
     public Element toXml() {
         Element xml = new Element("alignment");
         Set<Peak> used = new HashSet<Peak>();
         for (Cleavage cleavage : cleavages) {
             for (PeptideSupport support : cleavage.getSupports()) {
                 for (PeptideModification modification : support.getModifcations()) {
                     used.add(modification.getPeak());
                 }
             }
         }
         XmlUtil.addElement(xml, "peaks-described", used.size());
         XmlUtil.addElement(xml, "peaks-total", scan.getPeaks().size());
         xml.addContent(protein.toXml());
         xml.addContent(scan.toXml());
         for (Cleavage cleavage : cleavages) {
             xml.addContent(cleavage.toXml());
         }
         return xml;
     }
 }
