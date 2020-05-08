 /*
  * @author Dominique Ménard
  * equipe11
  */
 package pack;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import pack.validation.ContractLetter;
 
 public class TP1AGILE {
     
     private static DocumentXml document;
    private static Document outputXmlFile;
     
     public static DocumentXml loadXmlDocumentFromArg0(String[] args) throws Exception {
         document = new DocumentXml(args[0]);
         return document;
     }
     
     public static void consoleMessageOutput(String[] args) {
         System.out.println("Le fichier entree est : " + args[0]);
         System.out.println("Le fichier a ete enregistre : " + args[1]);
     }
 
     public static void main(String[] args) throws Exception {
         document = loadXmlDocumentFromArg0(args);
         
         if (!ValidationExecution.exexValid(document)) {
             DocumentXml.createErrorFile(args[1]);
         } else {
             DocumentXmlValidOutputFile xmlOutputFile = new DocumentXmlValidOutputFile(args);
             xmlOutputFile.createElementsInXmlOutputFile();
             DocumentXml.saveXmlDocument(args[1], xmlOutputFile.getOutputXmlFile());
             consoleMessageOutput(args);
         }
     }
 }
