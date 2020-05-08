 package org.opennms.rancid;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 import org.w3c.dom.NodeList;
 
 import java.io.IOException;
 
 import org.restlet.Client;
 //import org.restlet.data.CharacterSet;
 import org.restlet.data.Form;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Request;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.data.Response;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.w3c.dom.Document;
 
 /**
  * This class is the main client for the Rancid API.
  * 
  * 
  * @author <a href="mailto:guglielmoincisa@gmail.com">Guglielmo Incisa </a>
  * @author <a href="http://www.opennms.org/">OpenNMS </a>
  * 
  */
 
 class RWSResourceListImpl implements RWSResourceList {
     
     public RWSResourceListImpl(){
     }
     
     public List<String> ResourcesList;    
     
     public String getResource(int i) {
         //if ....TODO
         return ResourcesList.get(i);
     }
     public List<String> getResource() {
         //if ....TODO
         return ResourcesList;
     }    
 }
 
 public class RWSClientApi {
    
     private static Client client=new Client(Protocol.HTTP);
     
     private static boolean inited=false;
  
     public static void init(){
 
         try {
             client.start();
             inited = true;
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
     
     //Test encoding againts test server http://www.rionero.com/cgi-bin/cgitest
     public static void encode_test() throws RancidApiException {
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         Form form = new Form();
         form.add("deviceType", "test 123");
         form.add("state", "rotto al 99%");
         form.add("comment", "test commento");
                 
         Representation rep = form.getWebRepresentation();
 
         //rep.setMediaType(MediaType.APPLICATION_XHTML_XML);
         // Launch the request
         Reference rwsTest= new Reference("http://www.rionero.com/cgi-bin/cgitest");
         Response response = client.post(rwsTest,rep);
         try {
             response.getEntity().write(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     //***************************************************************************
     //***************************************************************************
     // check if server is busy
     public static boolean isRWSAvailable(ConnectionProperties cp) throws RancidApiException {
 
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
 
         String url = cp.getUrl()+cp.getDirectory() + "/";
         Response response=getMethodRWS(cp, url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         try {
             Document doc = dmr.getDocument();
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 return false;
             }
             else
                 return true;
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
         }
     }
     
     //***************************************************************************
     //***************************************************************************
     //LISTS    
 
     // Generic list of resources
     public static RWSResourceList getRWSResourceList(ConnectionProperties cp, String subUri ) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp, subUri);
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceList(String baseUri, String subUri ) throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"",30);
         return getRWSResourceList(cp, subUri);
     }
     
     //***************************************************************************
     // Services list
     public static RWSResourceList getRWSResourceServicesList(ConnectionProperties cp) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp , "/");
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceServicesList(String baseUri ) throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceServicesList(cp);
     }
 
     //***************************************************************************
     // RancidApi resource list
     public static RWSResourceList getRWSResourceRAList(ConnectionProperties cp) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp, "/rancid/");
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceRAList(String baseUri) throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceRAList(cp);
     }
     //***************************************************************************
     // Group list
     public static RWSResourceList getRWSResourceGroupsList(ConnectionProperties cp) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp,"/rancid/groups/");
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceGroupsList(String baseUri) throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceGroupsList(cp);
     }
     
     //***************************************************************************
     // Device list
     public static RWSResourceList getRWSResourceDeviceList(ConnectionProperties cp, String group) throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp,"/rancid/groups/"+group+"/");
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceDeviceList(String baseUri, String group) throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceDeviceList(cp, group);
     }
 
     //***************************************************************************
     // Login Pattern List
     public static RWSResourceList getRWSResourceLoginPatternList(ConnectionProperties cp)  throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp, "/rancid/clogin/");
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceLoginPatternList(String baseUri)  throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceLoginPatternList(cp);
     }
     //***************************************************************************
     // DeviceType Pattern List
     public static RWSResourceList getRWSResourceDeviceTypesPatternList(ConnectionProperties cp)  throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp, "/rancid/devicetypes/");
         return rwsImpl;
     }
     public static RWSResourceList getRWSResourceDeviceTypesPatternList(String baseUri)  throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceDeviceTypesPatternList(cp);
     }
 
     //***************************************************************************
     // Version List
     public static RWSResourceList getRWSResourceConfigList(ConnectionProperties cp, String group, String deviceName)  throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp, "/rancid/groups/"+group+"/"+deviceName+"/configs/");
         return rwsImpl;
     }
      
     public static RWSResourceList getRWSResourceConfigList(String baseUri, String group, String deviceName)  throws RancidApiException {
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSResourceConfigList(cp, group, deviceName);
     }
      
     //***************************************************************************
     //***************************************************************************
     // getInfo
     // gets generic list of items
     private static List<String> getInfo(ConnectionProperties cp, String listUri) throws RancidApiException{
 
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
 
         String url = cp.getUrl()+cp.getDirectory()+listUri;
         Response response=getMethodRWS(cp, url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         List<String> data = new ArrayList<String>();
         
         try {
             Document doc = dmr.getDocument();
             //dmr.write(System.out);
             for (int ii = 0; ii < doc.getElementsByTagName("Resource").getLength() ; ii++) {
                 String tmp = doc.getElementsByTagName("Resource").item(ii).getTextContent();
                 //System.out.println("Element " + tmp);
                 data.add(tmp);
             }
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
         }
         return data;
     }
     
     //***************************************************************************
     //***************************************************************************
     //Rancid Node Info retrieve 
     //
     // TLO = Top Level Only, no config is queried from server
     public static RancidNode getRWSRancidNodeTLO(ConnectionProperties cp ,String group, String devicename) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
 
         String url = cp.getUrl() + cp.getDirectory()+"/rancid/groups/" + group + "/" + devicename;
         Response response =getMethodRWS(cp, url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         RancidNode rn = new RancidNode();
        
         try {
             Document doc = dmr.getDocument();
 
             rn.setDeviceName(devicename);
             rn.setDeviceType(doc.getElementsByTagName("deviceType").item(0).getTextContent());
             rn.setStateUp(doc.getElementsByTagName("state").item(0).getTextContent().compareTo("up") == 0);
             rn.setComment(doc.getElementsByTagName("comment").item(0).getTextContent());
             rn.setGroup(group);
             
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
        }
         return rn;
     }
 
     
     public static RancidNode getRWSRancidNode(ConnectionProperties cp ,String group, String devicename) throws RancidApiException{
 
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         String url = cp.getUrl() + cp.getDirectory()+"/rancid/groups/" + group + "/" + devicename;
         Response response =getMethodRWS(cp, url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         RancidNode rn = new RancidNode();
        
         try {
             Document doc1 = dmr.getDocument();
 
             rn.setDeviceName(devicename);
             rn.setDeviceType(doc1.getElementsByTagName("deviceType").item(0).getTextContent());
             rn.setStateUp(doc1.getElementsByTagName("state").item(0).getTextContent().compareTo("up") == 0);
             rn.setComment(doc1.getElementsByTagName("comment").item(0).getTextContent());
             rn.setGroup(group);
             
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
         }
 
         String url2 = cp.getUrl() + cp.getDirectory()+"/rancid/groups/" + group + "/" + devicename+"/configs";
         Response response2 = getMethodRWS(cp, url2);
         DomRepresentation dmr2 = response2.getEntityAsDom();
    
         try {
             Document doc2 = dmr2.getDocument();
 
             rn.setRootConfigurationUrl(doc2.getElementsByTagName("UrlViewVC").item(0).getTextContent());
             rn.setTotalRevisions(doc2.getElementsByTagName("TotalRevisions").item(0).getTextContent());
             rn.setHeadRevision(doc2.getElementsByTagName("HeadRevision").item(0).getTextContent());        
         }
             catch( IOException e){
                 throw(new RancidApiException("Error: IOException Method GET: URL:" +url2, RancidApiException.OTHER_ERROR));
             }
         return rn;
     }
         
     public static RancidNode getRWSRancidNode(String baseUri ,String group, String devicename) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSRancidNode(cp, group, devicename );
     }
     
     
     //***************************************************************************
     //Rancid Group provisioning
     // incomplete
     //TODO
     
 //    public static void createRWSGroup(String baseUri, String group) throws RancidApiException{
 //        
 //        if (!inited){
 //            throw(new RancidApiException("Error: Api not initialized"));
 //        }
 //        
 //        Form form = new Form();
 //        form.add("Resource", group );
 //                
 //        Representation rep = form.getWebRepresentation();
 //
 //        Response response = putMethodRWS(cp, baseUri+"/rws/rancid/groups/" + group,rep);
 //        try {
 //            response.getEntity().write(System.out);
 //        } catch (IOException e) {
 //            // TODO Auto-generated catch block
 //            e.printStackTrace();
 //        }
 //
 //    }
     
     //***************************************************************************
     //***************************************************************************
     //Rancid Node Info provisioning
     
     public static void create_TEST_RWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
     }
     
     public static void createRWSRancidNode(ConnectionProperties cp, RancidNode rnode) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         Form form = new Form();
         form.add("deviceType", rnode.getDeviceType());
         form.add("state", rnode.getState());
         form.add("comment", rnode.getComment());
                 
         Representation rep = form.getWebRepresentation();
 
         String url = cp.getUrl()+cp.getDirectory()+"/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName();
         Response response = putMethodRWS(cp, url,rep);
         
         DomRepresentation dmr2 = response.getEntityAsDom();
         
         try {
             Document doc = dmr2.getDocument();
 
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
             } else if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrExists") == 0){
                 throw(new RancidApiException("Error: Resource Already Exists", RancidApiException.RWS_RESOURCE_EXISTS));
             } 
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method PUT: URL:" +url, RancidApiException.OTHER_ERROR));
         }
     }
 
     public static void createRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         createRWSRancidNode(cp, rnode);
     }
     
     //***************************************************************************
     
     public static void updateRWSRancidNode(ConnectionProperties cp, RancidNode rnode) throws RancidApiException{
 
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         Form form = new Form();
         form.add("deviceType", rnode.getDeviceType());
         form.add("state", rnode.getState());
         form.add("comment", rnode.getComment());
                 
         Representation rep = form.getWebRepresentation();
 
         String url = cp.getUrl()+cp.getDirectory()+"/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName();
 
         Response response = postMethodRWS(cp, url,rep);
         
         DomRepresentation dmr2 = response.getEntityAsDom();
         
         try {
             Document doc = dmr2.getDocument();
 
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
             } else if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrNotFound") == 0) {
             	throw(new RancidApiException("Error: Resource not found",RancidApiException.RWS_RESOURCE_NOT_FOUND));
             }
          }      
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method POST: URL:" +url, RancidApiException.OTHER_ERROR));
         }
     }
 
     public static void updateRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         updateRWSRancidNode(cp, rnode);
     }
     
     //***************************************************************************
     public static void createOrUpdateRWSRancidNode(ConnectionProperties cp, RancidNode rnode) throws RancidApiException{
      
         try {
             //RancidNode rnx = getRWSRancidNodeTLO( cp, rnode.getGroup(), rnode.getDeviceName());
             // no exception here so it exist so update it
             updateRWSRancidNode(cp, rnode);
         }
         catch (RancidApiException re){
             if (re.getRancidCode() == RancidApiException.RWS_RESOURCE_NOT_FOUND){
                 // does not exist create it
                 createRWSRancidNode(cp, rnode);
             }
             else {
                 //other kind of error must rethrow it
                 throw(re);
             }
         }
     }
 
 
     //***************************************************************************
     public static void deleteRWSRancidNode(ConnectionProperties cp, RancidNode rnode) throws RancidApiException{
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         Form form = new Form();
         form.add("deviceType", rnode.getDeviceType());
         form.add("state", rnode.getState());
         form.add("comment", rnode.getComment());
                 
 //        Representation rep = form.getWebRepresentation();
 
         //rep.setMediaType(MediaType.APPLICATION_XHTML_XML);
         // Launch the request
 
         String url = cp.getUrl()+cp.getDirectory()+"/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName();
         Response response = deleteMethodRWS(cp, url);
         
         DomRepresentation dmr2 = response.getEntityAsDom();
         
         try {
             Document doc = dmr2.getDocument();
 
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
             }        
         } catch( IOException e){
             throw(new RancidApiException("Error: IOException Method DELETE: URL:" +url, RancidApiException.OTHER_ERROR));
         }
     }
     public static void deleteRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         deleteRWSRancidNode(cp, rnode);
     }
     
     //***************************************************************************
 
 
 
     //***************************************************************************
     //***************************************************************************
     //Inventory Node info retrieve 
     public static InventoryNode getRWSInventoryNode(ConnectionProperties cp, RancidNode rancidNode, String version) throws RancidApiException{
     
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         String url = cp.getUrl() + cp.getDirectory()+"/rancid/groups/" + rancidNode.getGroup() + "/" + rancidNode.getDeviceName()+"/configs/"+version;
         Response response = getMethodRWS(cp,  url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         InventoryNode in = new InventoryNode(rancidNode);
                
         //TODO get inventory too
         
         try {
             Document doc = dmr.getDocument();
             // 2008/11/13 13:54:35 UTC
             SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d H:m:s z");
             //System.out.println("DATA "+doc.getElementsByTagName("Date").item(0).getTextContent());
             Date date = format.parse(doc.getElementsByTagName("Date").item(0).getTextContent());
             in.setCreationDate(date);
             in.setConfigurationUrl(doc.getElementsByTagName("UrlViewVC").item(0).getTextContent());
             
             in.setVersionId(version);
                       
         
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
         }
         catch (ParseException e){
             throw(new RancidApiException("Error: ParseException Method GET: URL:" +url + " ParseException" + e, RancidApiException.OTHER_ERROR));
         }
         return in;
 
     }
     public static InventoryNode getRWSInventoryNode(RancidNode rancidNode, String baseUri,  String version ) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSInventoryNode(cp , rancidNode, version);
     }
 
     //***************************************************************************
     //***************************************************************************
     //Inventory Node info retrieve 
     public static RancidNode getRWSRancidNodeInventory(ConnectionProperties cp, String group, String deviceName) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         RWSResourceList versions = getRWSResourceConfigList(cp, group, deviceName);
         RancidNode rn = getRWSRancidNode(cp, group, deviceName);
         
         List<String> configlist = versions.getResource();
         Iterator<String> iter1 = configlist.iterator();
         String vstmp;
         
         while (iter1.hasNext()) {
             vstmp = iter1.next();
             //System.out.println("Version " + tmpg1);
             InventoryNode in = getRWSInventoryNode(cp, rn, vstmp);
             rn.addInventoryNode(vstmp, in);
         }
         
         return rn;
 
     }
 
     public static RancidNode getRWSRancidNodeInventory(String baseUri, String group, String deviceName) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSRancidNodeInventory(cp, group, deviceName);
     }
 
     
     //***************************************************************************
     // Inventory element
     public static InventoryElement getRWSRancidNodeInventoryElement(ConnectionProperties cp, RancidNode rancidNode, String version) throws RancidApiException {
 
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         InventoryElement ie = new InventoryElement();
 
         String url = cp.getUrl() + cp.getDirectory()+"/rancid/groups/" + rancidNode.getGroup() + "/" + rancidNode.getDeviceName() + "/configs/" + version + "/inventory";
         
         Response response = getMethodRWS(cp, url);
 
         DomRepresentation dmr = response.getEntityAsDom();
 
         try {
             Document doc = dmr.getDocument();
             
             //Global part flat nodes
             System.out.println("**************");
             System.out.println("Chasis " + doc.getElementsByTagName("Chassis").item(0).getTextContent());
             System.out.println("**************");
             System.out.println("CPU " + doc.getElementsByTagName("CPU").item(0).getTextContent());
             System.out.println("**************");
             System.out.println("Processor-ID " + doc.getElementsByTagName("Processor-ID").item(0).getTextContent());
             System.out.println("**************");
             
             ie.setChassis(doc.getElementsByTagName("Chassis").item(0).getTextContent());
             ie.setCpu(doc.getElementsByTagName("CPU").item(0).getTextContent());
             ie.setProcessorId(doc.getElementsByTagName("Processor-ID").item(0).getTextContent());
             
             NodeList memoryNL = doc.getDocumentElement().getElementsByTagName("Memory");
             System.out.println("Memory " + memoryNL.getLength());
             System.out.println("Memory/DRAM/Size " + memoryNL.item(0).getChildNodes().item(1).getChildNodes().item(1).getTextContent());
             System.out.println("Memory/NVRAM/Size " + memoryNL.item(0).getChildNodes().item(3).getChildNodes().item(1).getTextContent());
             System.out.println("Memory/BootFlash/Size " + memoryNL.item(0).getChildNodes().item(5).getChildNodes().item(1).getTextContent());
             System.out.println("Memory/PCMCIA-ATA/Name " + memoryNL.item(0).getChildNodes().item(7).getChildNodes().item(1).getTextContent());
             System.out.println("Memory/PCMCIA-ATA/Size " + memoryNL.item(0).getChildNodes().item(7).getChildNodes().item(3).getTextContent());
             
             ie.setDRam(memoryNL.item(0).getChildNodes().item(1).getChildNodes().item(1).getTextContent());
             ie.setNvRam(memoryNL.item(0).getChildNodes().item(3).getChildNodes().item(1).getTextContent());
             ie.setBootFlash(memoryNL.item(0).getChildNodes().item(5).getChildNodes().item(1).getTextContent());
             ie.setPcmciaName(memoryNL.item(0).getChildNodes().item(7).getChildNodes().item(1).getTextContent());
             ie.setPcmciaSize(memoryNL.item(0).getChildNodes().item(7).getChildNodes().item(3).getTextContent());
             
             NodeList powerNL = doc.getDocumentElement().getElementsByTagName("Power");
             System.out.println("Power " + powerNL.getLength());            
             
             List<Tuple> power = new ArrayList<Tuple>();
             boolean has_power = false;
             for (int i = 1 ; i < powerNL.item(0).getChildNodes().getLength() ; i++) {
                 System.out.println("Power/item0/Name " + powerNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                 System.out.println("Power/item0/Desc " + powerNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent());
 
                 Tuple pw = new Tuple( powerNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent(),
                                       powerNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent()); 
                 
                 power.add(pw);
                 has_power = true;
                 i++;
             }
             if (has_power){
                 ie.setPower(power);
             }            
             
             NodeList softNL = doc.getDocumentElement().getElementsByTagName("Software");
             System.out.println("Software " + softNL.getLength());
             System.out.println("Software/OperatingSystem " + softNL.item(0).getChildNodes().item(1).getTextContent());
             System.out.println("Software/ROM-Bootstrap " + softNL.item(0).getChildNodes().item(3).getTextContent());
             System.out.println("Software/Bootloader " + softNL.item(0).getChildNodes().item(5).getTextContent());
 
             ie.setOs(softNL.item(0).getChildNodes().item(1).getTextContent());
             ie.setRomBootstarp(softNL.item(0).getChildNodes().item(3).getTextContent());
             ie.setBootLoader(softNL.item(0).getChildNodes().item(5).getTextContent());
             
             NodeList intfNL = doc.getDocumentElement().getElementsByTagName("Interfaces");
             System.out.println("Interfaces " + intfNL.getLength());
 
             List<Tuple> intflist = new ArrayList<Tuple>();
             boolean has_intf = false;
             for (int i = 1 ; i < intfNL.item(0).getChildNodes().getLength() ; i++) {
                 System.out.println("Interfaces/item"+i+"/Name " + intfNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                 System.out.println("Interfaces/item"+i+"/Desc " + intfNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent());
 
                 Tuple intf = new Tuple( intfNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent(),
                                         intfNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent()); 
                 
                 intflist.add(intf);
                 has_intf = true;
                 i++;
             }
             if (has_intf){
                 ie.setNwInterface(intflist);
             }            
             
             NodeList slotsNL = doc.getDocumentElement().getElementsByTagName("Slots");
             System.out.println("Slots " + slotsNL.getLength());
 
             List<InventorySlot> slots = new ArrayList<InventorySlot>();
             boolean has_slots = false;
             for (int i = 1 ; i < slotsNL.item(0).getChildNodes().getLength() ; i++) {
                 
                 if ( slotsNL.item(0).getChildNodes().item(i).getChildNodes().getLength() == 11){
                     System.out.println("Slots/item"+i+"/Name " +        slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item0/type " +  slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item1/hvers " + slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(5).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item2/part " +  slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item2/sn " +    slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(3).getTextContent());
     
                     InventorySlot slot = new InventorySlot(slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent(),
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getChildNodes().item(1).getTextContent(),
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(5).getChildNodes().item(1).getTextContent(),
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(1).getTextContent(),
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(3).getTextContent()); 
                     
                     slots.add(slot);
                 }
                 if ( slotsNL.item(0).getChildNodes().item(i).getChildNodes().getLength() == 9){
                     System.out.println("Slots/item"+i+"/Name " +        slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item1/hvers " + slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item2/part " +  slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(1).getTextContent());
                     System.out.println("Slots/item"+i+"/item2/sn " +    slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(3).getTextContent());
     
                     InventorySlot slot = new InventorySlot(slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent(),
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getChildNodes().item(1).getTextContent(),
                                                                          "",
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(1).getTextContent(),
                                                                          slotsNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getChildNodes().item(3).getTextContent()); 
                     
                     slots.add(slot);
                 }
 
                 has_slots = true;
                 i++;
             }
             if (has_slots){
                 ie.setSlot(slots);
             }            
 
             
             NodeList invNL = doc.getDocumentElement().getElementsByTagName("Inventory");
             System.out.println("Inventory " + invNL.item(0).getChildNodes().getLength());
 
             List<InventoryItem> invits = new ArrayList<InventoryItem>();
             boolean has_invits = false;
             for (int i = 1 ; i < invNL.item(0).getChildNodes().getLength(); i++) {
 
                 System.out.println("Inventory " + i +" " + invNL.item(0).getChildNodes().item(i).getChildNodes().getLength());
                 if (invNL.item(0).getChildNodes().item(i).getChildNodes().getLength() == 5){
                     System.out.println("Inventory/item"+i+"/Name " +  invNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                     System.out.println("Inventory/item"+i+"/Desc " +  invNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent());
                     
                     InventoryItem invit = new InventoryItem(invNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent(),
                                                             invNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent(),"","","");
                     invits.add(invit);
                 }
                 if (invNL.item(0).getChildNodes().item(i).getChildNodes().getLength() == 11){
                     System.out.println("Inventory/item"+i+"/Name " +  invNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                     System.out.println("Inventory/item"+i+"/Desc " +  invNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent());
                     System.out.println("Inventory/item"+i+"/pid " +   invNL.item(0).getChildNodes().item(i).getChildNodes().item(5).getTextContent());
                     System.out.println("Inventory/item"+i+"/vid " +   invNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getTextContent());
                     System.out.println("Inventory/item"+i+"/sn " +    invNL.item(0).getChildNodes().item(i).getChildNodes().item(9).getTextContent());
                     InventoryItem invit = new InventoryItem(invNL.item(0).getChildNodes().item(i).getChildNodes().item(1).getTextContent(),
                                                             invNL.item(0).getChildNodes().item(i).getChildNodes().item(3).getTextContent(),
                                                             invNL.item(0).getChildNodes().item(i).getChildNodes().item(5).getTextContent(),
                                                             invNL.item(0).getChildNodes().item(i).getChildNodes().item(7).getTextContent(),
                                                             invNL.item(0).getChildNodes().item(i).getChildNodes().item(9).getTextContent());
                     invits.add(invit);
                 }
                 has_invits = true;
                 i++;
             }
             if (has_invits){
                 ie.setInventoryItem(invits);
             }            
 
                   
             return ie;
             
             
         }
         catch( IOException e){
             throw(new RancidApiException("Error getRWSRancidNodeInventoryElement: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
         }
     }
     
     //***************************************************************************
     //***************************************************************************
     //Authentication info retrieve
     public static RancidNodeAuthentication getRWSAuthNode(ConnectionProperties cp, String devicename) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         String url = cp.getUrl() + cp.getDirectory()+"/rancid/clogin/" + devicename;
         Response response = getMethodRWS(cp, url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         RancidNodeAuthentication rna = new RancidNodeAuthentication();
                
         try {
             Document doc = dmr.getDocument();
             // dmr.write(System.out);
             rna.setDeviceName(devicename);
             rna.setUser(doc.getElementsByTagName("user").item(0).getTextContent());
             rna.setPassword(doc.getElementsByTagName("password").item(0).getTextContent());
             rna.setEnablePass(doc.getElementsByTagName("enablepassword").item(0).getTextContent());
             rna.setConnectionMethod(doc.getElementsByTagName("method").item(0).getTextContent());
             try {
                 rna.setAutoEnable(doc.getElementsByTagName("autoenable").item(0).getTextContent().compareTo("1") == 0);
             }
             catch (Exception e) {
                 System.out.println("optional auto enable field not found");
             }
             //rna.setAuthType(doc.getElementsByTagName("authType").item(0).getTextContent());
             //System.out.println("nel metodo "+ doc.getElementsByTagName("method").item(0).getTextContent());
 
 
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url, RancidApiException.OTHER_ERROR));
         }
         return rna;
     }
     
     public static RancidNodeAuthentication getRWSAuthNode(String baseUri, String devicename) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSAuthNode(cp, devicename);
     }
     
     public static void createRWSAuthNode(ConnectionProperties cp, RancidNodeAuthentication rnodea) throws RancidApiException{
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         Form form = new Form();
         form.add("user", rnodea.getUser());
         form.add("password", rnodea.getPassword());
         form.add("enablepassword", rnodea.getEnablePass());
         String autoenable = "0";
         if (rnodea.isAutoEnable()){
             autoenable="1";
         }
         form.add("autoenable", autoenable);
 //        form.add("authType", rnodea.getAuthType());
         form.add("method", rnodea.getConnectionMethodString());
         
                 
         Representation rep = form.getWebRepresentation();
 
         String url = cp.getUrl() + cp.getDirectory() + "/rancid/clogin/" +rnodea.getDeviceName();
         Response response = putMethodRWS(cp, url,rep);
         
         DomRepresentation dmr2 = response.getEntityAsDom();
         
         try {
             Document doc = dmr2.getDocument();
 
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
             }  else if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrExists") == 0){
                 throw(new RancidApiException("Error: Resource Already Exists", RancidApiException.RWS_RESOURCE_EXISTS));
             }
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method PUT: URL:" +url, RancidApiException.OTHER_ERROR));
         }
 
     }
 
     public static void updateRWSAuthNode(ConnectionProperties cp, RancidNodeAuthentication rnodea) throws RancidApiException{
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         Form form = new Form();
         form.add("user", rnodea.getUser());
         form.add("password", rnodea.getPassword());
         form.add("enablepassword", rnodea.getEnablePass());
         String autoenable = "0";
         if (rnodea.isAutoEnable()){
             autoenable="1";
         }
         form.add("autoenable", autoenable);
 //        form.add("authType", rnodea.getAuthType());
         form.add("method", rnodea.getConnectionMethodString());
         
                 
         Representation rep = form.getWebRepresentation();
 
         String url = cp.getUrl() + cp.getDirectory() + "/rancid/clogin/" +rnodea.getDeviceName();
 
         Response response = postMethodRWS(cp, url,rep);
         
         DomRepresentation dmr2 = response.getEntityAsDom();
         
         try {
             Document doc = dmr2.getDocument();
 
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
             } else if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrNotFound") == 0) {
             	throw(new RancidApiException("Error: Resource not found",RancidApiException.RWS_RESOURCE_NOT_FOUND));
             }
         }      
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method POST: URL:" +url, RancidApiException.OTHER_ERROR));
         }
     }
 
     //***************************************************************************
     //***************************************************************************
     //RancidNodeAuthetication provisioning
     public static void createOrUpdateRWSAuthNode(ConnectionProperties cp, RancidNodeAuthentication rnodea) throws RancidApiException{
         
         try {
             //RancidNode rnx = getRWSRancidNodeTLO( cp, rnode.getGroup(), rnode.getDeviceName());
             // no exception here so it exist so update it
             updateRWSAuthNode(cp, rnodea);
         }
         catch (RancidApiException re){
             if (re.getRancidCode() == RancidApiException.RWS_RESOURCE_NOT_FOUND){
                 // does not exist create it
                 createRWSAuthNode(cp, rnodea);
             }
             else {
                 //other kind of error must rethrow it
                 throw(re);
             }
         }
 
     }
 
     public static void createOrUpdateRWSAuthNode(String baseUri, RancidNodeAuthentication rnodea) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         createOrUpdateRWSAuthNode(cp, rnodea);
         return;
 
     }   
     
     public static void deleteRWSAuthNode(ConnectionProperties cp, RancidNodeAuthentication rnodea)throws RancidApiException{
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         Form form = new Form();
         form.add("user", rnodea.getUser());
         form.add("password", rnodea.getPassword());
         form.add("enablepassword", rnodea.getEnablePass());
 //        form.add("autoEnable", rnodea.isAutoEnable());
 //        form.add("authType", rnodea.getAuthType());
         form.add("method", rnodea.getConnectionMethodString());
 //        form.add("user", rnodea.getUser());
         
 //        Representation rep = form.getWebRepresentation();
 
         //rep.setMediaType(MediaType.APPLICATION_XHTML_XML);
         // Launch the request
         String url = cp.getUrl() + cp.getDirectory() + "/rancid/clogin/" +rnodea.getDeviceName();
         Response response = deleteMethodRWS(cp, url);
         DomRepresentation dmr2 = response.getEntityAsDom();
         
         try {
             Document doc = dmr2.getDocument();
 
             if (doc.getElementsByTagName("Code").item(0).getTextContent().compareTo("ErrBusy") == 0){
                 throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
             }        
         } catch( IOException e){
             throw(new RancidApiException("Error: IOException Method DELETE: URL:" +url, RancidApiException.OTHER_ERROR));
         }
         return;
     }
 
     
     
     public static void deleteRWSAuthNode(String baseUri, RancidNodeAuthentication rnodea)throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         deleteRWSAuthNode(cp, rnodea);
         return;
     }
     
     //*********************************************************************************************
     // RWS METHODS
     static Response getMethodRWS(ConnectionProperties cp, String uriReference) throws RancidApiException {
         
         client.setConnectTimeout(cp.getTimeout());
         
         Request request = new Request(Method.GET, uriReference);
         
         if(cp.getUserName() != null){
             
             ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
             ChallengeResponse authentication = new ChallengeResponse(scheme,cp.getUserName(), cp.getPassword());
             
 
             request.setChallengeResponse(authentication);
         }
         else {
             
         }
         Response response = client.handle(request); 
         
         if (response.getStatus().isSuccess()) {
             return response;
         } else if (response.getStatus() == Status.CLIENT_ERROR_REQUEST_TIMEOUT){
             throw(new RancidApiException("Error: RWS GET request timeout for URL:" + uriReference));
         }else if (response.getStatus() == Status.CLIENT_ERROR_UNAUTHORIZED){
             throw(new RancidApiException("Error: RWS GET authentication failed for URL:" + uriReference));
         }else if (response.getStatus().getCode() == 404){
             throw(new RancidApiException("Error: RWS GET resource not found for URL:" + uriReference, RancidApiException.RWS_RESOURCE_NOT_FOUND));
         } else {
             throw(new RancidApiException("Error: RWS GET request failed for URL: "+ uriReference+ " Status: "+ response.getStatus()));
         }
         
     }
     static Response postMethodRWS(ConnectionProperties cp, String uriReference, Representation form) throws RancidApiException {
         
         client.setConnectTimeout(cp.getTimeout());
         
         Request request = new Request(Method.POST, uriReference, form);
         
         if(cp.getAuthOn()){
             
             ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
             ChallengeResponse authentication = new ChallengeResponse(scheme,cp.getUserName(), cp.getPassword());
             
 
             request.setChallengeResponse(authentication);
         }
         else {
             
         }
         Response response = client.handle(request); 
        
         if (response.getStatus().isSuccess()) {
             return response;
         } else if (response.getStatus() == Status.CLIENT_ERROR_REQUEST_TIMEOUT){
             throw(new RancidApiException("Error: RWS POST request timeout "));
         }else if (response.getStatus() == Status.CLIENT_ERROR_UNAUTHORIZED){
             throw(new RancidApiException("Error: RWS POST authentication failed"));
         } else {
            throw(new RancidApiException("Error: RWS POST request failed: "+ response.getStatus()));
         }
         
     }
     static Response putMethodRWS(ConnectionProperties cp, String uriReference, Representation form) throws RancidApiException {
         
         client.setConnectTimeout(cp.getTimeout());
         
         Request request = new Request(Method.PUT, uriReference, form);
         
         if(cp.getAuthOn()){
             
             ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
             ChallengeResponse authentication = new ChallengeResponse(scheme,cp.getUserName(), cp.getPassword());
             
 
             request.setChallengeResponse(authentication);
         }
         else {
             
         }
         Response response = client.handle(request); 
         
         if (response.getStatus().isSuccess()) {
             return response;
         } else if (response.getStatus() == Status.CLIENT_ERROR_REQUEST_TIMEOUT){
             throw(new RancidApiException("Error: RWS PUT request timeout "));
         }else if (response.getStatus() == Status.CLIENT_ERROR_UNAUTHORIZED){
             throw(new RancidApiException("Error: RWS PUT authentication failed"));
         } else {
            throw(new RancidApiException("Error: RWS PUT request failed: "+ response.getStatus()));
         }
         
     }
     static Response deleteMethodRWS(ConnectionProperties cp, String uriReference) throws RancidApiException {
         
         client.setConnectTimeout(cp.getTimeout());
         
         Request request = new Request(Method.DELETE, uriReference);
         
         if(cp.getAuthOn()){
             
             ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
             ChallengeResponse authentication = new ChallengeResponse(scheme,cp.getUserName(), cp.getPassword());
             
 
             request.setChallengeResponse(authentication);
         }
         else {
             
         }
         Response response = client.handle(request); 
         
         if (response.getStatus().isSuccess()) {
             return response;
         } else if (response.getStatus() == Status.CLIENT_ERROR_REQUEST_TIMEOUT){
             throw(new RancidApiException("Error: RWS DELETE request timeout "));
         }else if (response.getStatus() == Status.CLIENT_ERROR_UNAUTHORIZED){
             throw(new RancidApiException("Error: RWS DELETE authentication failed"));
         } else {
            throw(new RancidApiException("Error: RWS DELETE request failed: "+ response.getStatus()));
         }
         
     }
     
     //private  String BaseUri;
     
 //user password
     /*
     # // Prepare the request  
     # Request request = new Request(Method.GET, "http://localhost:8182/");  
     #   
     # // Add the client authentication to the call  
     # ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;  
     # ChallengeResponse authentication = new ChallengeResponse(scheme,  
     #         "scott", "tiger");  
     # request.setChallengeResponse(authentication);  
     #   
     # // Ask to the HTTP client connector to handle the call  
     # Client client = new Client(Protocol.HTTP);  
     # Response response = client.handle(request);  
     #   
     # if (response.getStatus().isSuccess()) {  
     #     // Output the response entity on the JVM console  
     #     response.getEntity().write(System.out);  
     # } else if (response.getStatus()  
     #         .equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {  
     #     // Unauthorized access  
     #     System.out  
     #             .println("Access authorized by the server, " +  
     #                     "check your credentials");  
     # } else {  
     #     // Unexpected status  
     #     System.out.println("An unexpected status was returned: "  
     #             + response.getStatus());  
     # } 
     */ 
   
  
 }
 
 
 
     
