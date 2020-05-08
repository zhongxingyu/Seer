 package edu.mit.wi.haploview;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMNode;
 import org.apache.axiom.om.impl.llom.OMNavigator;
 import org.apache.axiom.om.impl.builder.StAXOMBuilder;
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 
 import java.io.*;
 import java.util.Iterator;
 import java.net.URL;
 import javax.xml.stream.*;
 
 /**
  * Connects with the Genecruiser server via HTTP request for obtaining an XML file with relevant data. Also parses said XML data.
  * @author Jesse Whitworth
  */
 
 public class GeneCruiser {
 
     //Default settings
     int chromMark, startMark, endMark;
     boolean chromCollected, startCollected, endCollected;
 
     String origNamespace = "http://service.genecruiser.org/xsd";
     String dataNamespace = "service.genecruiser.org";
     String host = "genecruiser.broad.mit.edu/genecruiser3_services";
     String firstResult = "0";
     String email = "haploview@broad.mit.edu";
    Logger logger = Logger.getLogger(GeneCruiser.class);
 
 
     /**
      * Gets the GeneCruiser data and returns it as an array of ints.
      * @param selectType Type of query, validated with if statements
      * @param inputId String literal of the user's query
      * @return Int[] containing [chromosome, start position, end position]
      * @throws HaploViewException If there are problems with data collection
      */
 
     public int[] getData(int selectType, String inputId)throws HaploViewException {
 
         BasicConfigurator.configure();
         logger.setLevel(Level.OFF);
         String address;
 
         //Make sure that the user has put in at least some request.
         if (inputId.length() > 0){
 
                 if (selectType == 0){//For ENSMBL Searches        ENSG00000114784
                     address = "http://" + host + "/rest/variation/byGenomicId?idType=ensembl_gene_stable_id&id=" + inputId + "&firstResult=" + firstResult + "&email=" + email;
 
                 }else if (selectType == 1){  //For HUGO Searches      1100
                     address = "http://" + host + "/rest/variation/byGenomicId?idType=HUGO&id=" + inputId + "&firstResult=" + firstResult + "&email=" + email;
 
                 }else if (selectType ==2){//For SNP Searches    rs5004340
                     address = "http://" + host + "/rest/variation/byName?name=" + inputId + "&firstResult=" + firstResult + "&email=" + email;
 
                 }else{
                     throw new HaploViewException("Please select an Id Type for Genecruiser");
                 }
             //System.out.println(address);
             try {
 
                 //Initialize the URL connection
                 URL inURL = new URL(address);
                 inURL.openConnection();
 
                 //Load the XML file into the parser
                 XMLStreamReader parser =
                         XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(inURL.openStream()));
                 StAXOMBuilder builder = new StAXOMBuilder(parser);
 
                 //Create the Document and iterate it.
                 OMElement docElement =  builder.getDocumentElement();
 
                 OMNavigator navigator = new OMNavigator(docElement);
                 OMNode node;
                 OMElement nodeElements;
 
                 while (navigator.isNavigable()) {
 
                     node = navigator.next();
                     //Check to make sure that the node can be converted to an element
                     if (node.getType() == 1){
                         nodeElements = (OMElement)node;
                         Iterator iter = nodeElements.getChildren();
                         NamespaceNav(iter);
                     }
                 }
             }catch(IOException ioe){
                 throw new HaploViewException("Error connecting to GeneCruiser database.");
             }catch(XMLStreamException xmls){
                 throw new HaploViewException("Error reading GeneCruiser data.");
             }
 
             //Check to make sure that everything has been located
             if (chromCollected &&
                     startCollected &&
                     endCollected){
                 return new int[]{chromMark, startMark, endMark};
             }
         }else{
             throw new HaploViewException("Please enter a search query");
         }
         throw new HaploViewException("Could not locate the requested information");
     }
 
     /**
      * Navigates through the XML tree, allowing for long XML files to be handled correctly
      * Created to be a recursive function, very large XML docs may take some time.
      * @param iter A series of nodes that need to be navigated and validated
      */
 
     public void NamespaceNav(Iterator iter){
 
         OMElement tempNode;
         OMNode tempNoder;
         Iterator tempIter;
         String childValue;
 
         while (iter.hasNext()) {
 
             tempNoder = (OMNode)iter.next();
 
             //Validate that the node is of a useable type, if not 1 then the tree is not fully broken down
             if(tempNoder.getType()==1){
                 tempNode = (OMElement) tempNoder;
                 tempIter = tempNode.getChildElements();
 
                 int tempInt;
 
                 //Check to see that this is section comes from GeneCrusier
                 if (tempNode.getNamespace().getNamespaceURI().equalsIgnoreCase(dataNamespace)){
 
                     childValue = tempNode.getText();
 
                     //Check the node to learn what data it contains
                     if (tempNode.getLocalName().equalsIgnoreCase("chromosome")){
 
                         chromMark = Integer.parseInt(childValue.trim());
                         chromCollected = true;
 
                     }else if (tempNode.getLocalName().equalsIgnoreCase("start")){
 
                         tempInt = Integer.parseInt(childValue.trim());
                         if (tempInt < startMark){
                             startMark = tempInt;
                         }
                         if(startMark == 0){
                             startMark = tempInt;
                         }
                         startCollected = true;
 
                     }else if (tempNode.getLocalName().equalsIgnoreCase("end")){
 
                         tempInt = Integer.parseInt(childValue.trim());
                         if (tempInt > endMark){
                             endMark = tempInt;
                         }
                         endCollected = true;
                     }
                 }
                 while (tempIter.hasNext()){
                     NamespaceNav(tempIter);
                 }
             }
         }
     }
 }
