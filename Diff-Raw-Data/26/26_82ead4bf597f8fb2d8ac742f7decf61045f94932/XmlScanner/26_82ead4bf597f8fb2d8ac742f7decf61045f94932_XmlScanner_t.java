 package goDiet.Utils;
 
 import goDiet.Controller.*;
 import goDiet.Model.*;
 
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.*;
 import org.w3c.dom.*;
 
 /*
  * @author hdail
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
     
     private class Config {
         public Config() {};
         public String server = null;
         public String binary = null;
         public int traceLevel = -1;
         public boolean haveTraceLevel = false;
     }
     
     private class EnvDesc {
         public EnvDesc() {};
         public String path = null;
         public String ldLibraryPath = null;
     }
     
     private class EndPoint {
         public EndPoint() {};
         public String contact = null;
         public int startPort = -1;
         public int endPort = -1;
     }
     
     /**
      * Create new SimpleScanner with org.w3c.dom.Document.
      */
     public XmlScanner() {
         System.out.println("XmlScanner constructor");
     }
            
     /* Implementation of ErrorHandler for parser */
     public void error(org.xml.sax.SAXParseException sAXParseException) 
         throws org.xml.sax.SAXException {
         System.err.println ("XML Parse Error:  " + sAXParseException);
         throw sAXParseException;
     }
     
     /* Implementation of ErrorHandler for parser */
     public void fatalError(org.xml.sax.SAXParseException sAXParseException) 
         throws org.xml.sax.SAXException {
         System.err.println ("XML Parse Fatal Error:  " + sAXParseException);
         throw sAXParseException;
     }
     
     /* Implementation of ErrorHandler for parser */
     public void warning(org.xml.sax.SAXParseException sAXParseException) {    
         System.err.println ("XML Parse Warning:  " + sAXParseException);
     }  
     
     public boolean buildDietModel(String xmlFile, 
                                   DietPlatformController controller) 
         throws IOException {
             
         Document doc1;
         mainController = controller;
         
         try {
             doc1 = readXml(xmlFile);
         } catch (IOException ioe) {
             throw ioe;
         }
         
         visitDocument(doc1);
         return true;
     }
     
     public Document readXml(String xmlFile) throws IOException {
         Document doc;
         
         DocumentBuilderFactory factory =
                 DocumentBuilderFactory.newInstance();
         factory.setValidating(true);
         //factory.setNamespaceAware(true);
   
         try {
             DocumentBuilder builder = factory.newDocumentBuilder();
             builder.setErrorHandler(this);
             doc = builder.parse( xmlFile );
         } catch (SAXException sxe) {
             // Error generated during parsing)
             /*Exception  x = sxe;
             if (sxe.getException() != null)
                 x = sxe.getException();*/
             sxe.printStackTrace();
             throw new IOException("Parsing of " + xmlFile + " failed. Parser error." );
 
          } catch (ParserConfigurationException pce) {
             // Parser with specified options can't be built
             pce.printStackTrace();
             throw new IOException("Parsing of " + xmlFile + " failed. Parser configuration error." );
 
         } catch (IOException ioe) {
             // I/O error
             ioe.printStackTrace();
             throw new IOException("Parsing of " + xmlFile + " failed.  I/O error.");
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
     
     void visitElement_diet_configuration(org.w3c.dom.Element element) { // <diet_configuration>
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
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
     
     void visitElement_goDiet(org.w3c.dom.Element element) {
         RunConfig runCfg = mainController.getRunConfig();
         String tempStr;
         int tempInt;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("debug")) { 
                 tempInt = (new Integer(attr.getValue())).intValue();
                 if(tempInt < 0) { runCfg.debugLevel = 0;
                 } else if(tempInt > 2) { runCfg.debugLevel = 2;
                 } else { runCfg.debugLevel = tempInt; }
             }
             if (attr.getName().equals("saveStdOut")) {
                 tempStr = attr.getValue();
                 if(tempStr.equals("no")){
                     runCfg.saveStdOut = false;
                 } else if(tempStr.equals("yes")){
                     runCfg.saveStdOut = true;
                 }   // else leave default in place
             }
             if (attr.getName().equals("saveStdErr")) {
                 tempStr = attr.getValue();
                 if(tempStr.equals("no")){
                     runCfg.saveStdErr = false;
                 } else if(tempStr.equals("yes")){
                     runCfg.saveStdErr = true;
                 }
             }
             if (attr.getName().equals("useUniqueDirs")) {
                 tempStr = attr.getValue();
                 if(tempStr.equals("no")){
                     runCfg.useUniqueDirs = false;
                 } else if(tempStr.equals("yes")){
                     runCfg.useUniqueDirs = true;
                 }
             }
         }
         mainController.addRunConfig(runCfg);
     }
         
     void visitElement_resources(org.w3c.dom.Element element) {
         String scratchDir = null;
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("scratch")) {
                     scratchDir = visitElement_scratch(nodeElement);
                     mainController.addLocalScratchBase(scratchDir);
                 }
                 if (nodeElement.getTagName().equals("storage")) {
                     visitElement_storage(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("compute")) {
                     visitElement_compute(nodeElement);
                 }
             }
         }
     }
     
     void visitElement_storage(org.w3c.dom.Element element) { // <storage>
         String resourceLabel = null;
         String scratchDir = null;
         String serverLabel = null;
         AccessMethod scpAccess = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("label")) { // <storage label="???">
                 resourceLabel = attr.getValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("scratch")) {
                     scratchDir = visitElement_scratch(nodeElement);
                 }
                 if (nodeElement.getTagName().equals("scp")) {
                     scpAccess = visitElement_scp(nodeElement);
                 }
             }
         }
         if(resourceLabel == null) {
             System.err.println("Problem retrieving storage element resource label from XML.");
             System.err.println("Check for changes in DTD.  Exiting.");
             System.exit(1);
         } else if(scratchDir == null){
             System.err.println("Problem retrieving scratch dir for " + resourceLabel + 
                 " from XML. Exiting.");
             System.exit(1);
         } else if(scpAccess == null){
             System.err.println("No access method available for " + resourceLabel + ". Exiting.");
             System.exit(1);
         }
         
         StorageResource storRes = new StorageResource(resourceLabel, scratchDir);
         storRes.addAccessMethod(scpAccess);
         mainController.addStorageResource(storRes);
     }
 
     String visitElement_scratch(org.w3c.dom.Element element) { // <scratch>
         String directory = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("dir")) { // <scratch dir="???">
                 directory = attr.getValue();
             }
         }
         return directory;
     }
 
     void visitElement_compute(org.w3c.dom.Element element) { // <compute>
         String resourceLabel = null;
         String scratchDir = null;
         String diskLabel = null;
         AccessMethod sshAccess = null;
         EnvDesc envCfg = null;
         EndPoint endPoint = null;
         ComputeResource compRes = null;
 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("label")) { // <compute label="???">
                 resourceLabel = attr.getValue();
             }
             if (attr.getName().equals("disk")) { // <compute disk="???">
                 diskLabel = attr.getValue();
             }
         }
         StorageResource storRes = mainController.getStorageResource(diskLabel);
         if(storRes == null){
             System.err.println("Compute resource " + resourceLabel +
                " has invalid storage reference " + diskLabel + ". Not added.");
             return;
         }
         
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("ssh")) {
                     sshAccess = visitElement_ssh(nodeElement);
                 } else if (nodeElement.getTagName().equals("env")) {
                     envCfg = visitElement_env(nodeElement);
                 } else if (nodeElement.getTagName().equals("end_point")) {
                     endPoint = visitElement_end_point(nodeElement);
                 }
             }
         }
         compRes = new ComputeResource(resourceLabel, storRes);
         if(sshAccess != null){
             compRes.addAccessMethod(sshAccess);
         }
         if(envCfg != null){
             compRes.setEnvPath(envCfg.path);
             compRes.setEnvLdLibraryPath(envCfg.ldLibraryPath);
         }
         if(endPoint != null){
             if(endPoint.contact != null){
                 compRes.setEndPointContact(endPoint.contact);
             }
            if(endPoint.startPort != -1){
                 compRes.setEndPointRange(endPoint.startPort,endPoint.endPort);
             }
         }
         mainController.addComputeResource(compRes); 
     }
     
     AccessMethod visitElement_scp(org.w3c.dom.Element element) { // <scp>
         AccessMethod scpAccess = null;
         String login = null, server = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("login")) { // <ssh login="???">
                 login = attr.getValue();
             }
             if (attr.getName().equals("server")) { // <ssh server="???">
                 server = attr.getValue();
             }
         }
         if(login != null) {
             scpAccess = new AccessMethod("scp", server, login);
         } else {
             scpAccess = new AccessMethod("scp", server);
         }
         return scpAccess;
     }
     
     AccessMethod visitElement_ssh(org.w3c.dom.Element element) { 
         AccessMethod sshAccess = null;
         String login = null, server = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("login")) { 
                 login = attr.getValue();
             }
             if (attr.getName().equals("server")) { 
                 server = attr.getValue();
             }
         }
         if(login != null) {
             sshAccess = new AccessMethod("ssh", server, login);
         } else {
             sshAccess = new AccessMethod("ssh", server);
         }
         return sshAccess;
     }
     
     EndPoint visitElement_end_point(org.w3c.dom.Element element) { 
         EndPoint endPoint = new EndPoint();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("contact")) { 
                 endPoint.contact = attr.getValue();
             } else if (attr.getName().equals("startPort")){
                 endPoint.startPort = (new Integer(attr.getValue())).intValue();
             } else if (attr.getName().equals("endPort")){
                 endPoint.endPort = (new Integer(attr.getValue())).intValue();
             }
         }
         return endPoint;
     }
     
     EnvDesc visitElement_env(org.w3c.dom.Element element) { // <env>
         EnvDesc env = new EnvDesc();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("path")) { // <env path="???">
                 env.path = attr.getValue();
             }
             if (attr.getName().equals("LD_LIBRARY_PATH")) { // <env LD_LIBRARY_PATH="???">
                 env.ldLibraryPath = attr.getValue();
             }
         }
         return env;
     }
     
     void visitElement_diet_services(org.w3c.dom.Element element) { // <diet_services>
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("omni_names")) {
                     visitElement_omni_names(nodeElement);
                 } else if (nodeElement.getTagName().equals("log_central")) {
                     visitElement_service(nodeElement);
                 } else if (nodeElement.getTagName().equals("test_tool")) {
                     visitElement_service(nodeElement);
                 }
             }
         }
     }
     
     void visitElement_omni_names(org.w3c.dom.Element element) { // <omni_names>
         OmniNames omniNames = null;
         Config config = new Config();
         int port = -1;
         String contact = null;
         ComputeResource compRes = null;
         
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if(attr.getName().equals("contact")) { 
                 contact = attr.getValue();
             } else if(attr.getName().equals("port")) { 
                 port = (new Integer(attr.getValue())).intValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                 }
             }
         }  
         compRes = mainController.getComputeResource(config.server);
         if(compRes == null){
             System.err.println("Definition of omni_names " +
                 "incorrect.  Host label " + config.server + " does not refer" +
                 "to valid compute resource.");
             System.exit(1);
         }
         if(port != -1){
             omniNames = new OmniNames("OmniNames",compRes,config.binary,
                 contact,port);
         } else {
             omniNames = new OmniNames("OmniNames",compRes,config.binary,
                 contact);
         }
         omniNames.setCfgFileName("omniORB4.cfg");
         mainController.addOmniNames(omniNames);
     }    
     void visitElement_service(org.w3c.dom.Element element) { // <omni_names>
         Services service = null;
         Config config = new Config();
         ComputeResource compRes = null;
         
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                 }
             }
         }
         
         compRes = mainController.getComputeResource(config.server);
         if(compRes == null){
             System.err.println("Definition of " + element.getTagName() +
                 "incorrect.  Host label " + config.server + " does not refer" +
                 "to valid compute resource.");
             System.exit(1);
         }
         if (element.getTagName().equals("log_central")) {
             service = new Services("LogCentral",compRes,config.binary);
             service.setCfgFileName("config.cfg");
             mainController.addLogCentral(service);
         } else if (element.getTagName().equals("test_tool")) {
             service = new Services("TestTool",compRes,config.binary);
             mainController.addTestTool(service);
         }
     }
     
     void visitElement_diet_hierarchy(org.w3c.dom.Element element) { // <diet_hierarchy>
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("master_agent")) {
                     visitElement_master_agent(nodeElement);
                 }
             }
         }
     }
     
     void visitElement_master_agent(org.w3c.dom.Element element) { // <master_agent>
         MasterAgent newMA = null;
         String maName = "";
         int port = -1;
         Config config = new Config();
                      
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("label")) { // <master_agent label="???">
                 maName = attr.getValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes = 
                         mainController.getComputeResource(config.server);
                     if(compRes == null){
                         System.err.println("Definition of " + maName +
                         " incorrect.  Host label " + config.server + 
                         " does not refer to valid compute resource.");
                         System.exit(1);
                     }
                    
                     newMA = new MasterAgent(maName,compRes,config.binary);
                     if( config.haveTraceLevel ) {
                         newMA.setTraceLevel(config.traceLevel);
                     }
                     mainController.addMasterAgent(newMA);
                     
                 }
                 if( newMA != null ) {
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
     
     void visitElement_local_agent(org.w3c.dom.Element element,
                                   Agents parentAgent) { // <local_agent>
         LocalAgent newLA = null;
         String laName="";
         Config config = new Config();
         
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("label")) { // <local_agent label="???">
                 laName = attr.getValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes = 
                         mainController.getComputeResource(config.server);
                     if(compRes == null){
                         System.err.println("Definition of " + laName +
                         "incorrect.  Host label " + config.server + 
                         " does not refer to valid compute resource.");
                         System.exit(1);
                     }    
                     newLA = new LocalAgent(laName, compRes,
                                            config.binary, parentAgent);
                     if (config.haveTraceLevel) {
                         newLA.setTraceLevel(config.traceLevel);
                     }
                     mainController.addLocalAgent(newLA, parentAgent);
                 }
                 if( newLA != null ) {
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
     
     void visitElement_SeD(org.w3c.dom.Element element,
                           Agents parentAgent) { // <SeD>
         ServerDaemon newSeD = null;
         String sedName = "";
         Config config = new Config();
         String parameters = null;
                 
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("label")) { // <SeD label="???">
                 sedName = attr.getValue();
             }
         }
         org.w3c.dom.NodeList nodes = element.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             org.w3c.dom.Node node = nodes.item(i);
             if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                 if (nodeElement.getTagName().equals("config")) {
                     config = visitElement_config(nodeElement);
                     ComputeResource compRes = 
                         mainController.getComputeResource(config.server);
                     if(compRes == null){
                         System.err.println("Definition of " + sedName +
                         " incorrect.  Host label " + config.server + 
                         " does not refer to valid compute resource.");
                         System.exit(1);
                     }
                     newSeD = new ServerDaemon(sedName,compRes,
                                 config.binary,parentAgent);
                     if (config.haveTraceLevel) {
                         newSeD.setTraceLevel(config.traceLevel);
                     }
                     mainController.addServerDaemon(newSeD, parentAgent);
                 }
                 if( newSeD != null ) {
                     if (nodeElement.getTagName().equals("parameters")) {
                         parameters = visitElement_parameters(nodeElement);
                         newSeD.setParameters(parameters);
                     }
                 }
             }
         }
     }
     
     Config visitElement_config(org.w3c.dom.Element element) { // <config>
         Config config = new Config();
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
             if (attr.getName().equals("remote_binary")) { // <config remote_binary="???">
                 config.binary = attr.getValue();
             }
             if (attr.getName().equals("server")) { // <config server="???">
                 config.server = attr.getValue();
             }
             if (attr.getName().equals("trace_level")) { // <config trace_level="???">
                 config.traceLevel = (new Integer(attr.getValue())).intValue();
                 config.haveTraceLevel = true;
             }
         }
         return config;
     }
     
     String visitElement_parameters(org.w3c.dom.Element element) { // <parameters>        
         String parameters = null;
         org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++) {
             org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
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
 
         if( args.length != 1 ) {
             System.err.println(USAGE);
             System.exit(1);
         }
         
         DietPlatformController mainController = 
                 new DietPlatformController();
 
         xmlFile = args[0];
         mainController.parseXmlFile(xmlFile);
         
         System.out.println("XmlScanner unit test finished.");
     } 
 }
