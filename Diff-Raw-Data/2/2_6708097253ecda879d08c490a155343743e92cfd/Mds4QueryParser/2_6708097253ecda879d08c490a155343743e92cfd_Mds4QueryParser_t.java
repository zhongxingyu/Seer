 /* -------------------------------------------------------------------------- */
 /* Copyright 2002-2006 GridWay Team, Distributed Systems Architecture         */
 /* Group, Universidad Complutense de Madrid                                   */
 /*                                                                            */
 /* Licensed under the Apache License, Version 2.0 (the "License"); you may    */
 /* not use this file except in compliance with the License. You may obtain    */
 /* a copy of the License at                                                   */
 /*                                                                            */
 /* http://www.apache.org/licenses/LICENSE-2.0                                 */
 /*                                                                            */
 /* Unless required by applicable law or agreed to in writing, software        */
 /* distributed under the License is distributed on an "AS IS" BASIS,          */
 /* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   */
 /* See the License for the specific language governing permissions and        */
 /* limitations under the License.                                             */
 /* -------------------------------------------------------------------------- */
 
 /**
  * Mds4QueryParser parses an XML file that contains information 
  * provided by the Globus Index Service (MDS4). 
  * <p>
  * @author      Katia Leal (ASD)
  */
 
 import java.io.*; 
 import java.util.*;
 // JAXP packages
 import javax.xml.parsers.*;
 import org.xml.sax.* ;
 import org.w3c.dom.*;
 import java.net.*;
 
 class Mds4QueryParser{
     /**
      * Last known error
      */
     private String err;
         
     /**
      * Tree of XML nodes
      */
     private Document doc;
     
     /**
      * List of hosts names
      */
     private Vector hostsNames;
     
     /**
      * Creates an empty Mds4QueryParser
      */
     public Mds4QueryParser(){
         err = "";
         hostsNames = new Vector();
     }
     
     /**
      * Creates a tree of elements that representing the nodes of 
      * an XML file.
      * @param   xmlFile     name of the XML file
      * @return           a boolean indicating that the file was properly parsed
      */
     public boolean createTree(String xmlFile){
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = null;
         try{
             db = dbf.newDocumentBuilder();
         } 
         catch (ParserConfigurationException pce){
             err = pce.getMessage();
             return false;
         }
         OutputStreamWriter errorWriter = new OutputStreamWriter(System.err);
         db.setErrorHandler(new MyErrorHandler(new PrintWriter(errorWriter, true)));
         
         try{
             doc = db.parse(new File(xmlFile));
         }         
         catch (SAXParseException spe){ 
             err = "\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId();
             err += "   " + spe.getMessage();
             return false;
         } 
         catch (SAXException se){ 
             err = se.getMessage();        
             return false;
         } 
         catch (IOException ioe){
             err = ioe.getMessage();
             return false;
         }
         return true ;
     }
     
     /**
      *  Error handler to report errors and warnings
      */
     private static class MyErrorHandler implements ErrorHandler{
         private PrintWriter out;
 
         MyErrorHandler(PrintWriter out){
             this.out = out;
         }
 
         /**
          * Returns a string describing parse exception details
          */
         private String getParseExceptionInfo(SAXParseException spe){
             String systemId = spe.getSystemId();
             if (systemId == null) 
                 systemId = "null";
     
             String info = "URI=" + systemId +
                 " Line=" + spe.getLineNumber() +
                 ": " + spe.getMessage();
                 
             return info;
         }
 
         public void warning(SAXParseException spe) throws SAXException{
             out.println("Warning: " + getParseExceptionInfo(spe));
         }
         
         public void error(SAXParseException spe) throws SAXException{
             String message = "Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
 
         public void fatalError(SAXParseException spe) throws SAXException{
             String message = "Fatal Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
     }
     
     /**
      * Determines whether the 'addr' is an IP or not.
      */
     private boolean isIP(String addr){
     	int value;
     	try{
     		value = (new Integer(addr.substring(0,1))).intValue();
     	}catch (NumberFormatException e){
     		return false;
     	}
     	return (value >= 0 || value <= 9);
     }
     
     /**
      * Determines the name of a host, given the IP address.
      */
     private String iPToName(String addr){
     	InetAddress inetD = null;
     	int i = 0;
     	String[] ip_parts = addr.split("\\.");
     	List ip = new Vector();
     	for (i = 0; i< ip_parts.length; i++)
     		ip.add(ip_parts[i]);
         byte[] ip_num = {0, 0, 0, 0};
         Iterator it = ip.iterator();
         i = 0;
         while (it.hasNext()){
             String num = (String)it.next();
             ip_num[i++] = (new Integer(num)).byteValue();
         }
     	try{
     		inetD = InetAddress.getByAddress(ip_num);
     	}catch(UnknownHostException e){
     		return null;
     	}
     	return inetD.getHostName();
     }
     
     
     /**
      * Returns the last known error.
      */
     public String getErr(){
         return err;
     }
     
     /**
      * Resets err value.
      */
     public void resetErr(){
         err = "";
     }
 
     
     /**
      * Displays XML tree of nodes 
      */    
 /*    public void displayTree(){
         DOMTree tree = new DOMTree();
         tree.processNode(doc);
     }*/
 
     /**
      * Returns an string with the names of the hosts obtained from the XML file.
      * @return     a String formatted.
      */
     private String displayHostsNames(){
         StringBuffer out = new StringBuffer();
         Iterator it = hostsNames.iterator();
         
         while (it.hasNext()){
             String h = (String)it.next();
             out.append(h);
             if (it.hasNext())
                 out.append("\n");
         }
         return out.toString();    
     }
     
     /**
      * Obtains the names of the different hosts in the XML file. 
      * @param   node    next node to process
      */    
     public void getHostsNames(Node node){
         if (node == null){
             err = "Nothing to do, node is null";
             return;
         }    
         String tag = node.getNodeName();
         String https;
         int pos = tag.lastIndexOf(":");       
         if (tag.substring(pos+1).equals("Address")){
             NodeList list = node.getChildNodes();
             if (list == null)
                 return;
             try{
             	https = list.item(0).getNodeValue();
             }         
             catch (DOMException dome){
                 return;
             }
             pos = https.lastIndexOf(":");
             String addr = https.substring(8, pos);
             if (isIP(addr))
             	addr = iPToName(addr);
             if (addr != null && !hostsNames.contains(addr))
                 hostsNames.add(addr);
             return;
         }
         NodeList children = node.getChildNodes();
         if (children != null){
             int len = children.getLength();
             for (int i = 0; i < len; i++)
                 getHostsNames(children.item(i));
         }
     }
     
     public Document getDoc(){
         return doc;
     }
     
     public Vector getHostsNames(){
         return hostsNames;
     }
     
     /**
      * Obtains queue information.
      * @param   node    next node to process
      * @param   q     to store queue information
      */    
     private void qComputingElementNode(Node node, Queue q){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "qComputingElementNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("Name")){
                 q.setName(attrNode.getNodeValue());
                 return;
             }
         }
         return;
     }
     
     /**
      * Obtains queue information.
      * @param   node    next node to process
      * @param   q     to store queue information
      */    
     private void qInfoNode(Node node, Queue q){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "qInfoNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("TotalCPUs")){
                 q.setNode(attrNode.getNodeValue());
                 return;
             }
         }
         return;
     }
     
     /**
      * Obtains queue state information.
      * @param   node    next node to process
      * @param   q     to store queue information
      */    
     private void qStateNode(Node node, Queue q){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "qStateNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("FreeCPUs"))
                 q.setFreeNode(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("Status"))
                 q.setStatus(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("WaitingJobs"))
                 q.setMaxJobsWoutCpu(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("WorstResponseTime"))
                 q.setMaxTime(attrNode.getNodeValue());
         }
         return;
     }
 
     /**
      * Obtains queue policy information.
      * @param   node    next node to process
      * @param   q     to store queue information
      */    
     private void qPolicyNode(Node node, Queue q){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "qPolicyNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("MaxCPUTime"))
                 q.setMaxCpuTime(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("MaxRunningJobs"))
                 q.setMaxRunningJobs(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("Priority"))
                 q.setPriority(attrNode.getNodeValue());
         }
         return;
     }
     
     
     /**
      * Obtains processor node information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void processorNode(Node node, Host host){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "processorNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("ClockSpeed"))
                 host.setCpuMhz(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("InstructionSet")){
                 host.setCpuModel(attrNode.getNodeValue());
                 if (host.getArch().equals("NULL"))
                     host.setArch(attrNode.getNodeValue());
             }    
         }
         return;
     }
     
     /**
      * Obtains main memory node information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void mainMemoryNode(Node node, Host host){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "mainMemoryNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("RAMAvailable"))
                 host.setFreeMemMB(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("RAMSize"))
                 host.setSizeMemMB(attrNode.getNodeValue());
         }
         return;
     }
     
     /**
      * Obtains operating system node information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void osNode(Node node, Host host){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "osNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("Name"))
                 host.setOs(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("Release"))
                 host.setOsVersion(attrNode.getNodeValue());
         }
         return;
     }
     
     /**
      * Obtains architecture information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void archNode(Node node, Host host){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "archNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("SMPSize"))
                 host.setCpuSmp(attrNode.getNodeValue());
         }
         return;
     }
     
     /**
      * Obtains file system information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void fileSystemNode(Node node, Host host){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "fileSystemNode: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("AvailableSpace"))
                 host.setFreeDiskMB(attrNode.getNodeValue());
             else if (tag.substring(pos+1).equals("Size"))
                 host.setSizeDiskMB(attrNode.getNodeValue());
         }
         return;
     }
     
     /**
      * Obtains processor load information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void processorLoad(Node node, Host host){
         NamedNodeMap attrList = node.getAttributes();
         if(attrList == null){
             err += err + "processorLoad: no child nodes\n";
             return;
         }
         
         int len = attrList.getLength();
         int value = 0;
         for(int i = 0; i < len; i++){
             Node attrNode = attrList.item(i);
             String tag = attrNode.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("Last1Min"))
                 value = (new Integer(attrNode.getNodeValue())).intValue();
         }
         int smp = (new Integer(host.getCpuSmp())).intValue();
         int free = 100*smp - value;
         if (free < 0)
             free = 0;
         host.setFreeCpu(String.valueOf(free));
         return;
     } 
     
     /**
      * Determines only queues information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void queuesInfoNode(Node node, Host host){
         NodeList children = node.getChildNodes();
         if (children == null){
             err += err + "queuesInfoNode: no child nodes\n";
             return;
         }
         
         Queue q = new Queue();
         qComputingElementNode(node, q);
         int len = children.getLength();
         for (int i = 0; i < len; i++){
             Node n = children.item(i);
             String tag = n.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("Info"))
                 qInfoNode(n, q);
             else if (tag.substring(pos+1).equals("State"))
                 qStateNode(n, q);
             else if (tag.substring(pos+1).equals("Policy"))
                 qPolicyNode(n, q);
         }
         if (q.getNodes() > host.getNodeCount())
             host.setNodeCount(String.valueOf(q.getNodes()));
         host.add(q);
         return;
     }
     
     /**
      * Determines cpu type information.
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     private void cpuTypeNode(Node node, Host host){
         NodeList children = node.getChildNodes();
         if (children == null){
             err += err + "cpuTypeNode: no child nodes\n";
             return;
         }
         String arch;
         try{
             arch = children.item(0).getNodeValue();
         }         
         catch (DOMException dome){
             err += err + "cpuTypeNode: no node value\n";
             return;
         }
         host.setArch(arch);
         return;
     }
 
     /**
      * Determines only host information.
      * @param   node    next node to process
      * @param   host     to store host information
      * @return    a boolean indicating a valid host id
      */    
     private boolean hostInfoNode(Node node, Host host){
         NodeList children = node.getChildNodes();
         if (children == null){
             err += err + "hostInfoNode: no child nodes\n";
             return false;
         }
         
         int len = children.getLength();
         //Iterate on NodeList of child nodes.
         for (int i = 0; i < len; i++){
             Node n = children.item(i);
             String tag = n.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("Processor"))
                 processorNode(n, host);
             else if (tag.substring(pos+1).equals("MainMemory"))
                 mainMemoryNode(n, host);
             else if (tag.substring(pos+1).equals("OperatingSystem"))
                 osNode(n, host);
             else if (tag.substring(pos+1).equals("Architecture"))
                 archNode(n, host);
             else if (tag.substring(pos+1).equals("FileSystem"))
                 fileSystemNode(n, host);
             else if (tag.substring(pos+1).equals("ProcessorLoad"))
                 processorLoad(n, host);
         }
         return true;
     }
 
     /**
      * Determines host information, including queues information.
      * @param   node    next node to process
      * @param   host     to store host information
      * @return    a boolean indicating a valid host id
      */    
     private boolean processHostNode(Node node, Host host){
         String tag = node.getNodeName();
         int pos = tag.lastIndexOf(":");
         
         if (tag.substring(pos + 1).equals("hostCPUType")){
             cpuTypeNode(node, host);
         }
         else if (!host.isUpToDate() && tag.substring(pos + 1).equals("Host")){
             if (hostInfoNode(node, host))
             	host.setUpToDate(true);
         }
         else if (tag.substring(pos + 1).equals("ComputingElement")){
             queuesInfoNode(node, host);
         }
         else{
         	NodeList children = node.getChildNodes();
         	if (children != null){
         		int len = children.getLength();
         		for (int i = 0; i < len; i++)
         			processHostNode(children.item(i), host);
         	}
         }
         return true;
     }
     
     /**
      * Determines job manager type
      * @param   node    next node to process
      * @param   host     to store host information
      * @return    a boolean indicating a valid job manager was found
      */    
     private boolean jmName(Node node, Host host){
         NodeList children = node.getChildNodes();
         if (children == null){
             err += err + "jmName: no child nodes\n";
             return false;
         }
         
         for (int i = 0; i < children.getLength(); i++){
             Node n = children.item(i);
             String tag = n.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos+1).equals("ResourceID")){
                 NodeList list = n.getChildNodes();
                 if (list == null)
                     return false;
                 String job;
                 try{
                     job = list.item(0).getNodeValue();
                 }         
                 catch (DOMException dome){
                     return false;
                 }
                 if (job.toUpperCase().equals("FORK")){
                     host.setForkName("Fork");
                     return true;
                 }
                 else if (!job.toUpperCase().equals("MULTI")){
                    host.setLrmsName(job);
                     host.setLrmsType(job.toLowerCase());
                     host.removeQueues();
                     return true;
                 }
             }
         }
         return false;
     }
     
     /**
      * Process an Content node 
      * @param   node    next node to process
      * @param   host     to store host information
      * @return    a boolean indicating a valid host id
      */    
     public boolean processContentNode(Node node, Host host){
         NodeList list = node.getChildNodes();
         if (list == null){
             err += err + "processContentNode: no child nodes\n";
             return false;
         }
         
         for (int j = 0; j < list.getLength(); j++){
             Node c = list.item(j);
             String tag = c.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos + 1).equals("AggregatorData"))
                 if (!processHostNode(c, host))
                     return false;
         }
         return false;
     }
     
     /**
      * Process an MemberServiceEPR node 
      * @param   node    next node to process
      * @param   hostId    IP address of the host
      * @param   host     to store host information
      * @return    a boolean indicating a valid MemberServiceEPR node
      */    
     public boolean processMemberServiceEPRNode(Node node, Host host){
         NodeList list = node.getChildNodes();
         if (list == null)
             return false;
 
         for (int j = 0; j < list.getLength(); j++){
             Node c = list.item(j);
             String tag = c.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos + 1).equals("ReferenceProperties"))
                 return (jmName(c, host));
         }
         return false;
     }
     
     /**
      * Process an Entry node 
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     public void processEntryNode(Node node, Host host){
         NodeList children = node.getChildNodes();
         if (children == null)
             return;
 
         for (int i = 0; i < children.getLength(); i++){
             Node n = children.item(i);
             String tag = n.getNodeName();
             int pos = tag.lastIndexOf(":");
             if (tag.substring(pos + 1).equals("MemberServiceEPR")){
                 if (!processMemberServiceEPRNode(n, host))
                     return;
             }
             else if (tag.substring(pos + 1).equals("Content")){
                 if (!processContentNode(n, host))
                     return;
             }
         }
         return;
     }
     
     /**
      * Obtains the names of the different hosts in the XML file. 
      * @param   node    next node to process
      * @param   host     to store host information
      */    
     public void getHostInfo(Node node, Host host){
         if (node == null){
             err = "Nothing to do, node is null";
             return;
         }
         String tag = node.getNodeName();
         int pos = tag.lastIndexOf(":");
         if (tag.substring(pos+1).equals("Entry")){
             processEntryNode(node, host);
             return;
         }
         NodeList children = node.getChildNodes();
         if (children != null){
             int len = children.getLength();
             for (int i = 0; i < len; i++){
                 if (!host.getLrmsName().toUpperCase().equals("NULL"))
                     break;
                 getHostInfo(children.item(i), host);
             }
         }
     }
     
     public static void main(String[] args){
         Mds4QueryParser parser = new Mds4QueryParser();
         
         switch(args.length){
             case 2:{ //hosts names list
                 if (!args[0].equals("-l")){
                     System.err.println("java Mds4QueryParser [-l] [-i hostname] file");
                     return;
                 }
                 if (!parser.createTree(args[1])){
                     System.err.println("Error while procesing the file: " + parser.getErr());
                     return;
                 }
                 //parser.displayTree();
                 parser.getHostsNames(parser.getDoc());
                 if (parser.getHostsNames().size() == 0)
                     System.err.println("Error while obtaining hosts names: " + parser.getErr());
                 else
                     System.out.println(parser.displayHostsNames());
                 break;
             }
             case 3:{ //host information
                 if (!args[0].equals("-i")){
                     System.err.println("java Mds4QueryParser [-l] [-i hostname] file");
                     return;
                 }
                 if (!parser.createTree(args[2])){
                     System.err.println("Error while procesing the file: " + parser.getErr());
                     return;
                 }
                 Host h = new Host();
                 parser.resetErr();
                 parser.getHostInfo(parser.getDoc(), h);
                 if (h.getForkName().equals("NULL")){
                     h.setForkName("Fork");
                 }
                 if (h.getLrmsName().equals("NULL")){
                     h.setLrmsName("Fork");
                     h.setLrmsType("fork");
                 }
                 h.setName(args[1]);
                 System.out.println(h.info());
                 System.out.println(parser.getErr());
                 break;
             }
             default:{
                 System.err.println("java Mds4QueryParser [-l] [-i hostname] file");
                 break;
             }
         }
     }
 } //End Mds4QueryParser
