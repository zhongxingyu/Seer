 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pack;
 
 import java.util.ArrayList;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  *
  * @author Dominique Ménard
  */
 public class DocumentXmlValidOutputFile {
 
     private DocumentXml document;
     private NodeList formulaire;
     private Document outputXmlFile;

    public Document getOutputXmlFile() {
        return outputXmlFile;
    }
     private ArrayList<IndividualReclamationXmlNode> clientReclamationList;
     private Element remboursementsWrittenInOutputXmlFile;
     private String[] argsFromMain;
 
     public DocumentXmlValidOutputFile(String[] argsFromMain) {
         this.argsFromMain = argsFromMain;
     }
 
     private DocumentXml loadXmlDocumentFromArg0(String[] argsFromMain) throws Exception {
         document = new DocumentXml(argsFromMain[0]);
         return document;
     }
 
     public void createElementsInXmlOutputFile() throws Exception {
         loadXmlDocumentFromArg0(argsFromMain);
         setRootNodeInXmlDocument("reclamations");
         setReclamationNodeListInXmlDocument("reclamation");
         remboursementsWrittenInOutputXmlFile = ElementXml.creationElementXmlRoot(outputXmlFile, "remboursements");
         insertChildElementsOfRemboursementElement();
     }
 
     private void setRootNodeInXmlDocument(String rootName) {
         formulaire = document.getNodesByName(rootName);
     }
 
     private void setReclamationNodeListInXmlDocument(String nodeName) {
         clientReclamationList = DocumentXml.createListOfIndividualReclamationXmlNode(nodeName, document);
     }
 
     private void insertChildElementsOfRoot() {
         Element dossierWrittenInOutputXmlFile = ElementXml.creationElementXmlChild(outputXmlFile, "dossier", remboursementsWrittenInOutputXmlFile);
         Text textDossierWrittenInOutputXmlFile = outputXmlFile.createTextNode(document.obtainNodeContent(formulaire.item(0), "dossier"));
         dossierWrittenInOutputXmlFile.appendChild(textDossierWrittenInOutputXmlFile);
 
         Element moisWrittenInOutputXmlFile = ElementXml.creationElementXmlChild(outputXmlFile, "mois", remboursementsWrittenInOutputXmlFile);
         Text textMoisWrittenInOutputXmlFile = outputXmlFile.createTextNode(document.obtainNodeContent(formulaire.item(0), "mois"));
         moisWrittenInOutputXmlFile.appendChild(textMoisWrittenInOutputXmlFile);
     }
     
     private void insertChildElementsOfRemboursementElement(){
         for (int i = 0; i < clientReclamationList.size(); i++) {
                 Element remboursementWrittenInOutputXmlFile = ElementXml.creationElementXmlChild(outputXmlFile, "remboursement", remboursementsWrittenInOutputXmlFile);
 
                 Element soinWrittenInOutputXmlFile = ElementXml.creationElementXmlChild(outputXmlFile, "soin", remboursementWrittenInOutputXmlFile);
                 Text textSoinWrittenInOutputXmlFile = outputXmlFile.createTextNode(clientReclamationList.get(i).getSoin());
                 soinWrittenInOutputXmlFile.appendChild(textSoinWrittenInOutputXmlFile);
 
                 Element dateWrittenInOutputXmlFile = ElementXml.creationElementXmlChild(outputXmlFile, "date", remboursementWrittenInOutputXmlFile);
                 Text textDateWrittenInOutputXmlFile = outputXmlFile.createTextNode(clientReclamationList.get(i).getDate());
                 dateWrittenInOutputXmlFile.appendChild(textDateWrittenInOutputXmlFile);
 
                 Element montantWrittenInOutputXmlFile = ElementXml.creationElementXmlChild(outputXmlFile, "montant", remboursementWrittenInOutputXmlFile);
                 String montantToReimburse = ElementXml.calculateAmountToReinburseInString(clientReclamationList, i);
                    
                 Text textMontant = outputXmlFile.createTextNode(montantToReimburse);
                 montantWrittenInOutputXmlFile.appendChild(textMontant);
             }
     }
 }
