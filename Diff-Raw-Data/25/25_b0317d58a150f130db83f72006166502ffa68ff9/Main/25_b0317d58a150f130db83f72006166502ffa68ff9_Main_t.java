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
 
         RichSequenceIterator aRsIter = s.fromGenBankFile("test\\test_files\\BFa_8.15.gb");
        Library aBioFABlib = s.fromRichSequenceIter(aRsIter);
 
        System.out.println("aBioFABlib json: \n" + s.toJson(aBioFABlib));
        System.out.println("aBioFABlib RDF: \n"+ s.toRDF(aBioFABlib));
        
     }
 }
