 package ru.spbau.bioinf.tagfinder;
 
import org.jdom.Element;
import ru.spbau.bioinf.tagfinder.util.XmlUtil;

 public class Protein {
     private int proteinId;
    private String sequence;
     private String simplifiedAcids = null;
     private String name;
 
    public Protein(int proteinId, String sequence, String name) {
         this.proteinId = proteinId;
        this.sequence = sequence;
         this.name = name;
     }
 
    public Element toXml() {
        Element protein = new Element("protein");
        XmlUtil.addElement(protein, "protein-id", proteinId);
        XmlUtil.addElement(protein, "protein-name", name);
        XmlUtil.addElement(protein, "protein-sequence", sequence);
        return protein;
    }

     public int getProteinId() {
         return proteinId;
     }
 
    public String getSequence() {
        return sequence;
     }
 
     public String getName() {
         return name;
     }
 
     public String getSimplifiedAcids() {
         if (simplifiedAcids == null) {
            simplifiedAcids = sequence.replaceAll("L", "I").replaceAll("Z", "Q").replaceAll("B", "E").replaceAll("X", "I");
         }
         return simplifiedAcids;
     }
 }
