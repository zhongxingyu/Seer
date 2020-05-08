 /**
  * Config.java implements methods, which are needed to change programm settings
  */
 package config;
 
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.OutputKeys;
  
 import org.w3c.dom.*;
 
 public class Config {
 
   private static final String CONFIG_FILE = "config.xml";
   private static int id;
   private wordlists.IWordList[] wordList;
   
   private Object dictObject;
 
   public Config(){
     }
     
   public Config(Object dicts) {
     this.dictObject = dicts;
     }
     
   
   public void save() {
     try {
       DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    
       // Create XML roots
       Document doc = docBuilder.newDocument();
       Element rootElement = doc.createElement("wordlists");
       doc.appendChild(rootElement);
       
       Element wl = doc.createElement("wordlist");
       rootElement.appendChild(wl);
       
       // Value list attribute
       Attr attr = doc.createAttribute("id");
       attr.setValue(Integer.toString(id));
       wl.setAttributeNode(attr);
       id++;
       
       // Word List file path
       Element wlFilePath = doc.createElement("wlfilepath");
       wlFilePath.appendChild(doc.createTextNode("tests/test.dwa"));
       wl.appendChild(wlFilePath);
       
       // Word List description
       Element wlFileDesc = doc.createElement("wlfiledesc");
       wlFileDesc.appendChild(doc.createTextNode("Testinis failas"));
       wl.appendChild(wlFileDesc);
       
       // write the content into XML file
       TransformerFactory transformerFactory = TransformerFactory.newInstance();
       Transformer transformer = transformerFactory.newTransformer();
       transformer.setOutputProperty(OutputKeys.INDENT, "yes");
       transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
       DOMSource source = new DOMSource(doc);
       StreamResult result =  new StreamResult(new File(CONFIG_FILE));
       transformer.transform(source, result);
       }
     catch(ParserConfigurationException pce) {
       pce.printStackTrace();
       }
     catch(TransformerException tfe) {
       tfe.printStackTrace();
       };
 
     }
 
   public wordlists.IWordList[] load() {
     try {
  
       File fXmlFile = new File(CONFIG_FILE);
       DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
       Document doc = dBuilder.parse(fXmlFile);
       doc.getDocumentElement().normalize();
    
       doc.getDocumentElement().getNodeName();
       NodeList nList = doc.getElementsByTagName("wordlist");
       wordList = new wordlists.GSFMemory[nList.getLength()];
 
       for (int temp = 0; temp < nList.getLength(); temp++) {
 
         Node nNode = nList.item(temp);
         if (nNode.getNodeType() == Node.ELEMENT_NODE) {
           Element eElement = (Element) nNode;
 
           wordList[temp] = new wordlists.GSFMemory();
           // FIXME: Check if IWordListFileRead before executing.
           ((wordlists.IWordListFileRead) wordList[temp]
             ).load(getTagValue("wlfilepath",eElement));
           /* FIXME: Object must be created outside the class
            * wordList = new wordlists.DWAMemory();
            * wordList.load(getTagValue("wlfilepath",eElement));
            */
 
           }
         }
       }
     catch (Exception e) {
       System.out.println("Exception: " + e.getMessage());
      System.out.println("System is now exiting. :/");
       System.exit(0);
       }
     
     return this.wordList;
     }
  
   private static String getTagValue(String sTag, Element eElement){
     NodeList nlList= eElement.getElementsByTagName(sTag).item(0).getChildNodes();
     Node nValue = (Node) nlList.item(0); 
  
     return nValue.getNodeValue();
     }
 
   }
