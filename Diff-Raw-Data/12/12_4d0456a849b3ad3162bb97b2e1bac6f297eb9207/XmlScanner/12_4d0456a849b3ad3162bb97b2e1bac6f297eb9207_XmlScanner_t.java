 /*@GODIET_LICENSE*/
 package goDiet.Utils;
 
 import goDiet.Controller.*;
 import goDiet.Model.*;
 
 import java.io.IOException;
 
 import java.util.Vector;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.*;
 import org.w3c.dom.*;
 
 /*
  * @author hdail & rbolze
  *
  * @see org.w3c.dom.Document
  * @see org.w3c.dom.Element
  * @see org.w3c.dom.NamedNodeMap
  */
 public class XmlScanner implements ErrorHandler {
 
     org.w3c.dom.Document document;
     private static final String USAGE =
             "USAGE: XmlImporter <file.xml>\n";
     private goDiet.Controller.DietPlatformController mainController;
     private goDiet.Controller.ConsoleController consoleCtrl;
     private OptionChecker opt_checker;
     /* helper class, only use here */
     private class Config {
 
         public String server = null;
         public String binary = null;
     }    
     /* helper class, only use here */
 
     private class EndPoint {
 
         public String contact = null;
         public int startPort = -1;
         public int endPort = -1;
     }    
 
     /**
      * Create new SimpleScanner with org.w3c.dom.Document.
      */
     public XmlScanner() {
         //System.out.println("XmlScanner constructor");
         this.opt_checker = new OptionChecker();        
         //opt_checker.check("ServerDeamon", "batchName");
     }
 
     /* Implementation of ErrorHandler for parser */
     public void error(org.xml.sax.SAXParseException sAXParseException)
             throws org.xml.sax.SAXException {
         System.err.println("XML Parse Error:  " + sAXParseException);
         throw sAXParseException;
     }
 
     /* Implementation of ErrorHandler for parser */
     public void fatalError(org.xml.sax.SAXParseException sAXParseException)
             throws org.xml.sax.SAXException {
         System.err.println("XML Parse Fatal Error:  " + sAXParseException);
         throw sAXParseException;
     }
 
     /* Implementation of ErrorHandler for parser */
     public void warning(org.xml.sax.SAXParseException sAXParseException) {
         System.err.println("XML Parse Warning:  " + sAXParseException);
     }
 
     public boolean buildDietModel(String xmlFile,
             DietPlatformController mainController,
             ConsoleController consoleCtrl)
             throws IOException {
 
         Document doc1;
         this.mainController = mainController;
         this.consoleCtrl = consoleCtrl;
         
         try {
             doc1 = readXml(xmlFile);
         } catch (IOException ioe) {
             ioe.getMessage();
             throw ioe;
         }
 
         visitDocument(doc1);
         return true;
     }
 
     public Document readXml(String xmlFile) throws IOException {
         Document doc = null;
 
         DocumentBuilderFactory factory =
                 DocumentBuilderFactory.newInstance();
         factory.setValidating(true);
         //factory.setNamespaceAware(true);
 
         try {
             DocumentBuilder builder = factory.newDocumentBuilder();
             builder.setErrorHandler(this);
             doc = builder.parse(xmlFile);
         } catch (SAXException sxe) {
             // Error generated during parsing)
             /*Exception  x = sxe;
             if (sxe.getException() != null)
             x = sxe.getException();*/
             sxe.getMessage();
             throw new IOException("Parsing of " + xmlFile + " failed. Parser error.");
 
         } catch (ParserConfigurationException pce) {
             // Parser with specified options can't be built
             pce.getMessage();
             throw new IOException("Parsing of " + xmlFile + " failed. Parser configuration error.");
 
         } catch (IOException ioe) {
             // I/O error
             throw new IOException("Parsing of " + xmlFile + " failed.  I/O error: " + ioe.getMessage());
         }
         return doc;
     }
 
     public void visitDocument(Document document) {
         // Initialize counters for unique labels on components seen in document        
         
         org.w3c.dom.Element element = document.getDocumentElement();
         if ((element != null) && element.getTagName().equals("diet_configuration")) {
             visitElement_diet_configuration(element);
         }
     }
 
    public void visitElement_diet_configuration(org.w3c.dom.Element element) { // <diet_configuration>
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("goDiet")) {
                     visitElement_goDiet(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("resources")) {
                     visitElement_resources(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("diet_services")) {
                     visitElement_diet_services(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("diet_hierarchy")) {
                     visitElement_diet_hierarchy(nodeElement);
                 }
             }
         }
     }
 
   public  void visitElement_goDiet(org.w3c.dom.Element element) {
         RunConfig runCfg = consoleCtrl.getRunConfig();
         String tempStr;
         int tempInt;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("debug")) {
                 tempInt = (new Integer(attr.getValue())).intValue();
                 if (tempInt < 0) {
                     runCfg.debugLevel = 0;
                 } else {
                     runCfg.debugLevel = tempInt;
                 }
             }
             if (attr.getName().equals("saveStdOut")) {
                 tempStr = attr.getValue();
                 if (tempStr.equals("no")) {
                     runCfg.saveStdOut = false;
                 } else if (tempStr.equals("yes")) {
                     runCfg.saveStdOut = true;
                 }   // else leave default in place
             }
             if (attr.getName().equals("saveStdErr")) {
                 tempStr = attr.getValue();
                 if (tempStr.equals("no")) {
                     runCfg.saveStdErr = false;
                 } else if (tempStr.equals("yes")) {
                     runCfg.saveStdErr = true;
                 }
             }
             if (attr.getName().equals("useUniqueDirs")) {
                 tempStr = attr.getValue();
                 if (tempStr.equals("no")) {
                     runCfg.useUniqueDirs = false;
                 } else if (tempStr.equals("yes")) {
                     runCfg.useUniqueDirs = true;
                 }
             }
             if (attr.getName().equals("log")) {
                 tempStr = attr.getValue();
                 if (tempStr.equals("yes")) {
                     consoleCtrl.setLogFile("GoDIET.log");
                 }
             }
         }
     }
 
    public void visitElement_resources(org.w3c.dom.Element element) {
         String scratchDir = null;
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("scratch")) {
                     scratchDir = visitElement_scratch(nodeElement);
                     //mainController.addLocalScratchBase(scratchDir);
                     consoleCtrl.getRunConfig().setLocalScratchBase(scratchDir);
                 }
                 if (nodeElement.getTagName().equals("storage")) {
                     visitElement_storage(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("compute")) {
                     visitElement_compute(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("cluster")) {
                     visitElement_cluster(nodeElement);
                 }
             }
         }
     }
 
    public void visitElement_storage(org.w3c.dom.Element element) { // <storage>
         String resourceLabel = null;
         String scratchDir = null;
         String serverLabel = null;
         AccessMethod scpAccess = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <storage label="???">
                 resourceLabel = attr.getValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("scratch")) {
                     scratchDir = visitElement_scratch(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("scp")) {
                     scpAccess = visitElement_scp(nodeElement);
                 }
             }
         }
         if (resourceLabel == null) {
             System.err.println("Problem retrieving storage element resource label from XML.");
             System.err.println("Check for changes in DTD.  Exiting.");
             System.exit(1);
         } else if (scratchDir == null) {
             System.err.println("Problem retrieving scratch dir for " +
                     resourceLabel + " from XML. Exiting.");
             System.exit(1);
         } else if (scpAccess == null) {
             System.err.println("No access method available for " +
                     resourceLabel + ". Exiting.");
             System.exit(1);
         }
 
         StorageResource storRes = new StorageResource(resourceLabel);
         storRes.setScratchBase(scratchDir);
         storRes.addAccessMethod(scpAccess);
         mainController.addStorageResource(storRes);
     }
 
    public String visitElement_scratch(org.w3c.dom.Element element) { // <scratch>
         String directory = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("dir")) { // <scratch dir="???">
                 directory = attr.getValue();
             }
         }
         return directory;
     }
 
    public void visitElement_compute(org.w3c.dom.Element element) { // <compute>
         String resourceLabel = null;
         String diskLabel = null;
         String login = null;
         AccessMethod sshAccess = null;
         Vector envVars = null;
         EndPoint endPoint = null;
         ComputeResource compRes = null;
         ComputeCollection compColl = null;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <compute label="???">
                 resourceLabel = attr.getValue();
             }
             if (attr.getName().equals("disk")) { // <compute disk="???">
                 diskLabel = attr.getValue();
             }
             if (attr.getName().equals("login")) {
                 login = attr.getValue();
             }
         }
         StorageResource storRes = mainController.getStorageResource(diskLabel);
         if (storRes == null) {
             System.err.println("Compute resource " + resourceLabel +
                     " has invalid storage reference " + diskLabel + ". Not added.");
             return;
         }
 
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("ssh")) {
                     sshAccess = visitElement_ssh(nodeElement, login);
                 } else if (nodeElement.getTagName().equals("env")) {
                     envVars = visitElement_env(nodeElement);
                 } else if (nodeElement.getTagName().equals("end_point")) {
                     endPoint = visitElement_end_point(nodeElement);
                 }
             }
         }
         compColl = new ComputeCollection(resourceLabel);
         compRes = new ComputeResource(resourceLabel, compColl);
         if (sshAccess != null) {
             compRes.addAccessMethod(sshAccess);
         }
         if (endPoint != null) {
             if (endPoint.contact != null) {
                if (endPoint.contact.equals("localhost"))
                    endPoint.contact = "127.0.0.1";
                 compRes.setEndPointContact(endPoint.contact);
             }
             if (endPoint.startPort != -1) {
                 compRes.setBegAllowedPorts(endPoint.startPort);
                 compRes.setEndAllowedPorts(endPoint.endPort);
             }
         }
         compColl.addComputeResource(compRes);
         compColl.addStorageResource(storRes);
 
         if (envVars != null) {
             compColl.setEnvVars(envVars);
         }
         mainController.addComputeCollection(compColl);
     }
 
    public void visitElement_cluster(org.w3c.dom.Element element) {
         String clusLabel = null;
         String diskLabel = null;
         String login = null;
         Vector envVars = null;
         //ComputeResource compRes = null;
         ComputeCollection compColl = null;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <compute label="???">
                 clusLabel = attr.getValue();
             }
             if (attr.getName().equals("disk")) { // <compute disk="???">
                 diskLabel = attr.getValue();
             }
             if (attr.getName().equals("login")) {
                 login = attr.getValue();
             }
         }
         StorageResource storRes = mainController.getStorageResource(diskLabel);
         if (storRes == null) {
             System.err.println("Compute resource " + clusLabel +
                     " has invalid storage reference " + diskLabel + ". Not added.");
             return;
         }
 
         /** First retrieve env parameters, and create collection */
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("env")) {
                     envVars = visitElement_env(nodeElement);
                     break;
                 }
             }
         }
         compColl = new ComputeCollection(clusLabel);
         compColl.addStorageResource(storRes);
         if (!envVars.isEmpty()) {
             compColl.setEnvVars(envVars);
         }
 
         /** Next find all resources that go in this collection */
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("node")) {
                     visitElement_node(nodeElement, compColl, login);
                 }
             }
         }
         mainController.addComputeCollection(compColl);
     }
 
    public void visitElement_node(org.w3c.dom.Element element,
             ComputeCollection collection,
             String clusLogin) {
         String resourceLabel = null;
         AccessMethod sshAccess = null;
         EndPoint endPoint = null;
         ComputeResource compRes = null;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) {
                 resourceLabel = attr.getValue();
             }
         }
         compRes = new ComputeResource(resourceLabel, collection);
 
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("ssh")) {
                     sshAccess = visitElement_ssh(nodeElement, clusLogin);
                 } else if (nodeElement.getTagName().equals("end_point")) {
                     endPoint = visitElement_end_point(nodeElement);
                 }
             }
         }
 
         if (sshAccess != null) {
             compRes.addAccessMethod(sshAccess);
         } else {
             System.err.println("Resource " + resourceLabel +
                     " does not have recognizable ssh access. " +
                     "Not added!");
             return;
         }
         if (endPoint != null) {
             if (endPoint.contact != null) {
                if (endPoint.contact.equals("localhost"))
                    endPoint.contact = "127.0.0.1";
                 compRes.setEndPointContact(endPoint.contact);
             }
             if (endPoint.startPort != -1) {
                 compRes.setBegAllowedPorts(endPoint.startPort);
                 compRes.setEndAllowedPorts(endPoint.endPort);
             }
         }
         collection.addComputeResource(compRes);
         return;
     }
 
    public AccessMethod visitElement_scp(org.w3c.dom.Element element) { // <scp>
         AccessMethod scpAccess = null;
         String login = null, server = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("login")) { // <ssh login="???">
                 login = attr.getValue();
             }
             if (attr.getName().equals("server")) { // <ssh server="???">
                 server = attr.getValue();
             }
         }
         if (login != null) {
             scpAccess = new AccessMethod("scp", server, login);
         } else {
             scpAccess = new AccessMethod("scp", server);
         }
         return scpAccess;
     }
 
    public AccessMethod visitElement_ssh(org.w3c.dom.Element element, String clusLogin) {
         AccessMethod sshAccess = null;
         String login = null, server = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("login")) {
                 login = attr.getValue();
             }
             if (attr.getName().equals("server")) {
                 server = attr.getValue();
             }
         }
         if (login != null) {
             sshAccess = new AccessMethod("ssh", server, login);
         } else if (clusLogin != null) {
             sshAccess = new AccessMethod("ssh", server, clusLogin);
         } else {
             sshAccess = new AccessMethod("ssh", server);
         }
         return sshAccess;
     }
 
    public EndPoint visitElement_end_point(org.w3c.dom.Element element) {
         EndPoint endPoint = new EndPoint();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("contact")) {
                 endPoint.contact = attr.getValue();
             } else if (attr.getName().equals("startPort")) {
                 endPoint.startPort = (new Integer(attr.getValue())).intValue();
             } else if (attr.getName().equals("endPort")) {
                 endPoint.endPort = (new Integer(attr.getValue())).intValue();
             }
         }
         return endPoint;
     }
 
    public Vector visitElement_env(org.w3c.dom.Element element) { // <env>
         Vector envVars = new Vector();
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("var")) {
                     EnvVar v = visitElement_var(nodeElement);
                     envVars.add(v);
                 }
             }
         }
         return envVars;
     }
 
    public EnvVar visitElement_var(org.w3c.dom.Element element) {
         EnvVar v = new EnvVar();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("name")) {
                 v.setName(attr.getValue());
             }
             if (attr.getName().equals("value")) {
                 v.setValue(attr.getValue());
             }
         }
         return v;
     }
 
    public Option visitElement_option(org.w3c.dom.Element element) {        
         Option o = new Option();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("name")) {
                 o.setName(attr.getValue());
             }
             if (attr.getName().equals("value")) {
                 o.setValue(attr.getValue());
             }
         }
         return o;
     }
 
    public void visitElement_diet_services(org.w3c.dom.Element element) { // <diet_services>
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("omni_names")) {
                     visitElement_omni_names(nodeElement);
                 } else if (nodeElement.getTagName().equals("log_central")) {
                     visitElement_log_central(nodeElement);
                 } else if (nodeElement.getTagName().equals("log_tool")) {
                     visitElement_log_tool(nodeElement);
                 } else if (nodeElement.getTagName().equals("diet_statistics")) {
                     mainController.setUseDietStats(true);
                 }
             }
         }
     }
 
    public void visitElement_omni_names(org.w3c.dom.Element element) { // <omni_names>
         OmniNames omniNames = null;
         Config config = new Config();
         int port = -1;
         String contact = null;
         ComputeResource compRes = null;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("contact")) {
                 contact = attr.getValue();
             } else if (attr.getName().equals("port")) {
                 port = (new Integer(attr.getValue())).intValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     compRes = mainController.getComputeResource(config.server);
                     if (compRes == null) {
                         System.err.println("Definition of omni_names " +
                                 "incorrect.  Host label " + config.server + " does not refer" +
                                 "to valid compute resource.");
                         System.exit(1);
                     }
                     if (port != -1) {
                         omniNames = new OmniNames("OmniNames", compRes, config.binary,
                                 contact, port);
                     } else {
                         omniNames = new OmniNames("OmniNames", compRes, config.binary,
                                 contact);
                     }
                 }                
                 if (nodeElement.getTagName().equals("cfg_options")) {                    
                     visitElement_cfg_options(nodeElement, omniNames);
                 }
             }
         }
         omniNames.setCfgFileName("omniORB4.cfg");
         mainController.addOmniNames(omniNames);
     }
 
    public void visitElement_log_central(org.w3c.dom.Element element) { // <log_central>
         Config config = new Config();
         ComputeResource compRes = null;
         boolean connectDuringLaunch = true;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("connectDuringLaunch")) {
                 String tempStr = attr.getValue();
                 if (tempStr.equals("no")) {
                     connectDuringLaunch = false;
                 } else if (tempStr.equals("yes")) {
                     connectDuringLaunch = true;
                 }
             }
         }
 
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                 }
             }
         }
 
         compRes = mainController.getComputeResource(config.server);
         if (compRes == null) {
             System.err.println("Definition of " + element.getTagName() +
                     "incorrect.  Host label " + config.server + " does not refer" +
                     "to valid compute resource.");
             System.exit(1);
         }
 
         LogCentral logger = new LogCentral(
                 "LogCentral", compRes, config.binary, connectDuringLaunch);
         logger.setCfgFileName("config.cfg");
         mainController.addLogCentral(logger);
     }
 
     public void visitElement_log_tool(org.w3c.dom.Element element) { // <test_tool>
         Config config = new Config();
         ComputeResource compRes = null;
 
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                 }
             }
         }
 
         compRes = mainController.getComputeResource(config.server);
         if (compRes == null) {
             System.err.println("Definition of " + element.getTagName() +
                     "incorrect.  Host label " + config.server + " does not refer" +
                     "to valid compute resource.");
             System.exit(1);
         }
 
         Services service = new Services("TestTool", compRes, config.binary);
         mainController.addTestTool(service);
     }
 
     public void visitElement_diet_hierarchy(org.w3c.dom.Element element) { // <diet_hierarchy>
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("master_agent")) {
                     visitElement_master_agent(nodeElement);
                 }
             }
         }
     }
 
     public void visitElement_cfg_options(org.w3c.dom.Element element, goDiet.Model.Elements el) { // <cfg_options>               
         org.w3c.dom.NodeList nodes = element.getChildNodes();        
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("option")) {
                     Option o = visitElement_option(nodeElement);
                     String message = el.getElementCfg().addOption(o);                    
                     String message2=opt_checker.check(OptionChecker.getObjectClass(el.getClass().getName()), o.getName());
                     if(!message.equals("")){
                         this.consoleCtrl.printOutput(message);
                     }
                     if (!message2.equals("")){
                         this.consoleCtrl.printOutput(message2);
                     }
                 }
             }
         }
     }
 
     void visitElement_master_agent(org.w3c.dom.Element element) { // <master_agent>
         MasterAgent newMA = null;
         String maName = "";
         int port = -1;
         Config config = new Config();
         int useDietStats = -1;
         //long initRequestID = -1;        
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <master_agent label="???">
                 maName = attr.getValue();
             } else if (attr.getName().equals("useDietStats")) {
                 int val = -1;
                 try {
                     val = (new Integer(attr.getValue())).intValue();
                 } catch (NumberFormatException e) {
                     System.err.println("In " + element.getTagName() + " attribut " + attr.getName() +
                             " has invalid value \"" + attr.getValue() +
                             "\", it must be 0 or 1");
                     System.exit(1);
                 }
                 if (val != 0 && val != 1) {
                     System.err.println("In " + element.getTagName() + ", useDietStats value \"" + val + "\" is invalid.  Choose 0 or 1 ");
                     System.exit(1);
                 } else {
                     useDietStats = val;
                 }
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;                
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes =
                             mainController.getComputeResource(config.server);
                     if (compRes == null) {
                         System.err.println("Definition of " + maName +
                                 " incorrect.  Host label " + config.server +
                                 " does not refer to valid compute resource.");
                         System.exit(1);
                     }
 
                     newMA = new MasterAgent(maName, compRes, config.binary);
                     /*if( config.haveTraceLevel ) {
                     newMA.setTraceLevel(config.traceLevel);
                     }*/
                     if (useDietStats == -1 // the user don't give any value
                             && mainController.getUseDietStats()) {// the user set the option for all
                         newMA.setUseDietStats(mainController.getUseDietStats());
                     } else { // the user give a value to useDietStats ( 0 or 1)
                         newMA.setUseDietStats((useDietStats == 1));
                     }
                     mainController.addMasterAgent(newMA);
                 }
                 if (newMA != null) {
                     if (nodeElement.getTagName().equals("cfg_options")) {
                         visitElement_cfg_options(nodeElement, newMA);
                     }
                     if (nodeElement.getTagName().equals("ma_dag")) {
                         visitElement_ma_dag(nodeElement, newMA);
                     }
                     if (nodeElement.getTagName().equals("local_agent")) {
                         visitElement_local_agent(nodeElement, newMA);
                     }
                     if (nodeElement.getTagName().equals("SeD")) {
                         visitElement_SeD(nodeElement, newMA);
                     }
                 }
             }
         }
     }
 
    public void visitElement_local_agent(org.w3c.dom.Element element,
             Agents parentAgent) { // <local_agent>
         LocalAgent newLA = null;
         String laName = "";
         Config config = new Config();
         int useDietStats = -1;
 
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <local_agent label="???">
                 laName = attr.getValue();
             } else if (attr.getName().equals("useDietStats")) {
                 int val = -1;
                 try {
                     val = (new Integer(attr.getValue())).intValue();
                 } catch (NumberFormatException e) {
                     System.err.println("In " + element.getTagName() + " attribut " + attr.getName() +
                             " has invalid value \"" + attr.getValue() +
                             "\", it must be 0 or 1");
                     System.exit(1);
                 }
                 if (val != 0 && val != 1) {
                     System.err.println("In " + element.getTagName() + " useDietStats value \"" + val + "\" is invalid.  Choose 0 or 1 ");
                     System.exit(1);
                 } else {
                     useDietStats = val;
                 }
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes =
                             mainController.getComputeResource(config.server);
                     if (compRes == null) {
                         System.err.println("Definition of " + laName +
                                 "incorrect.  Host label " + config.server +
                                 " does not refer to valid compute resource.");
                         System.exit(1);
                     }
                     newLA = new LocalAgent(laName, compRes,
                             config.binary, parentAgent);
                     /*if (config.haveTraceLevel) {
                     newLA.setTraceLevel(config.traceLevel);
                     }*/
                     //newLA.setUseDietStats(mainController.getUseDietStats());
                     if (useDietStats == -1 // the user don't give any value
                             && mainController.getUseDietStats()) {// the user set the option for all
                         newLA.setUseDietStats(mainController.getUseDietStats());
                     } else { // the user give a value to useDietStats ( 0 or 1)
                         newLA.setUseDietStats((useDietStats == 1));
                     }
                     mainController.addLocalAgent(newLA, parentAgent);
                 }
                 if (nodeElement.getTagName().equals("cfg_options")) {
                     visitElement_cfg_options(nodeElement, newLA);
                 }
                 if (newLA != null) {
                     if (nodeElement.getTagName().equals("local_agent")) {
                         visitElement_local_agent(nodeElement, newLA);
                     }
                     if (nodeElement.getTagName().equals("SeD")) {
                         visitElement_SeD(nodeElement, newLA);
                     }
                 }
             }
         }
     }
 
    public void visitElement_ma_dag(org.w3c.dom.Element element,
             Agents parentAgent) { // <local_agent>
         Ma_dag ma_dag = null;
         String ma_dagName = "";
         Config config = new Config();
         int useDietStats = -1;
         String parameters = null;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <local_agent label="???">
                 ma_dagName = attr.getValue();
             } else if (attr.getName().equals("useDietStats")) {
                 int val = -1;
                 try {
                     val = (new Integer(attr.getValue())).intValue();
                 } catch (NumberFormatException e) {
                     System.err.println("In " + element.getTagName() + " attribut " + attr.getName() +
                             " has invalid value \"" + attr.getValue() +
                             "\", it must be 0 or 1");
                     System.exit(1);
                 }
                 if (val != 0 && val != 1) {
                     System.err.println("In " + element.getTagName() + " useDietStats value \"" + val + "\" is invalid.  Choose 0 or 1 ");
                     System.exit(1);
                 } else {
                     useDietStats = val;
                 }
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes =
                             mainController.getComputeResource(config.server);
                     if (compRes == null) {
                         System.err.println("Definition of " + ma_dagName +
                                 "incorrect.  Host label " + config.server +
                                 " does not refer to valid compute resource.");
                         System.exit(1);
                     }
                     ma_dag = new Ma_dag(ma_dagName, compRes,
                             config.binary, parentAgent);
                     /*if (config.haveTraceLevel) {
                     ma_dag.setTraceLevel(config.traceLevel);
                     }*/
                     //newLA.setUseDietStats(mainController.getUseDietStats());
                     if (useDietStats == -1 // the user don't give any value
                             && mainController.getUseDietStats()) {// the user set the option for all
                         ma_dag.setUseDietStats(mainController.getUseDietStats());
                     } else { // the user give a value to useDietStats ( 0 or 1)
                         ma_dag.setUseDietStats((useDietStats == 1));
                     }
                     mainController.addMa_dag(ma_dag);
                 }
                 if (nodeElement.getTagName().equals("parameters")) {
                         parameters = visitElement_parameters(nodeElement);
                         ma_dag.setParameters(parameters);
                 }
                 if (nodeElement.getTagName().equals("cfg_options")) {
                     visitElement_cfg_options(nodeElement, ma_dag);
                 }
             }
         }
     }
 
     public void visitElement_SeD(org.w3c.dom.Element element,
             Agents parentAgent) { // <SeD>
         ServerDaemon newSeD = null;
         String sedName = "";
         Config config = new Config();
         int useDietStats = -1;      
         String parameters = null;
 
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("label")) { // <SeD label="???">
                 sedName = attr.getValue();
             }
             if (attr.getName().equals("useDietStats")) {
                 int val = -1;
                 try {
                     val = (new Integer(attr.getValue())).intValue();
                 } catch (NumberFormatException e) {
                     System.err.println("In " + element.getTagName() + " attribut " + attr.getName() +
                             " has invalid value \"" + attr.getValue() +
                             "\", it must be 0 or 1");
                     System.exit(1);
                 }
                 if (val != 0 && val != 1) {
                     System.err.println("In " + element.getTagName() + " useDietStats value \"" + val + "\" is invalid.  Choose 0 or 1 ");
                     System.exit(1);
                 } else {
                     useDietStats = val;
                 }
             }
         }
 
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes =
                             mainController.getComputeResource(config.server);
                     if (compRes == null) {
                         System.err.println("Definition of " + sedName +
                                 " incorrect.  Host label " + config.server +
                                 " does not refer to valid compute resource." + node.toString());
                         System.exit(1);
                     }
                     newSeD = new ServerDaemon(sedName, compRes,
                             config.binary, parentAgent);
                     if (useDietStats == -1 // the user don't give any value
                             && mainController.getUseDietStats()) {// the user set the option for all
                         newSeD.setUseDietStats(mainController.getUseDietStats());
                     } else { // the user give a value to useDietStats ( 0 or 1)
                         newSeD.setUseDietStats((useDietStats == 1));
                     }                 
                     mainController.addServerDaemon(newSeD, parentAgent);
                 }
                 if (newSeD != null) {
                     if (nodeElement.getTagName().equals("parameters")) {
                         parameters = visitElement_parameters(nodeElement);
                         newSeD.setParameters(parameters);
                     }
                     if (nodeElement.getTagName().equals("cfg_options")) {
                         visitElement_cfg_options(nodeElement, newSeD);
                     }
                 //newSeD.setParameters(parameters);
                     /*if (!batchName.equals("")){
                 newSeD.setBatchName(batchName);
                 }
                 if (!batchQueue.equals("")){
                 newSeD.setBatchQueue(batchQueue);
                 }
                 if (!pathToNFS.equals("")){
                 newSeD.setPathToNFS(pathToNFS);
                 }
                 if (!pathToTmp.equals("")){
                 newSeD.setPathToTmp(pathToTmp);
                 }*/
                 }
             }
         }
     }
 
    public Config visitElement_config(org.w3c.dom.Element element) { // <config>
         Config config = new Config();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("remote_binary")) { // <config remote_binary="???">
                 config.binary = attr.getValue();
             }
             if (attr.getName().equals("server")) { // <config server="???">
                 config.server = attr.getValue();
             }
         /*if (attr.getName().equals("trace_level")) { // <config trace_level="???">
         config.traceLevel = (new Integer(attr.getValue())).intValue();
         config.haveTraceLevel = true;
         }*/
         }
         return config;
     }
 
    public String visitElement_parameters(org.w3c.dom.Element element) { // <parameters>
         String parameters = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
             if (attr.getName().equals("string")) { // <parameters string="???">
                 parameters = attr.getValue();
             }
         }
         return parameters;
     }
 
     public static void main(String args[]) {
         String xmlFile = "";
         XmlScanner scanner = null;
 
         System.out.println("Running unit test for XmlImporter.");
 
         if (args.length != 1) {
             System.err.println(USAGE);
             System.exit(1);
         }
 
         xmlFile = args[0];
         ConsoleController consoleController = new ConsoleController();
         consoleController.loadXmlFile(xmlFile);
 
         System.out.println("XmlScanner unit test finished.");
     }
 }
