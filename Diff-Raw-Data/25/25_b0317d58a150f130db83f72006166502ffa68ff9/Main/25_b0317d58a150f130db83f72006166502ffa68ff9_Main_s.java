 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 //package libSBOLjUseExample;
 
 import org.biojava.bio.BioException;
 import org.biojavax.bio.seq.RichSequenceIterator;
 import org.sbolstandard.libSBOLj.SBOLutil;
 import org.sbolstandard.libSBOLj.Library;
 
 /**
  *
  * @author mgaldzic
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws BioException {
         System.out.println("createDnaComponent");
         SBOLutil s = new SBOLutil();
        Library aBioFABlib = null;

 
         RichSequenceIterator aRsIter = s.fromGenBankFile("test\\test_files\\BFa_8.15.gb");
        aBioFABlib = s.fromRichSequenceIter(aRsIter);
 
        System.out.println("aBioFABpart json" + s.toJson(aBioFABlib));
        System.out.println("aBioFABpart RDF" + s.toRDF(aBioFABlib));
        //DnaComponent result = s.createDnaComponent(displayId, name, description, isCircular, type);
     }
 }
