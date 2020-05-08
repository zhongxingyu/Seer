 package org.opennms.rancid;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 import java.io.IOException;
 import java.io.InputStream;
 
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
 import org.opennms.rancid.RWSBucket.BucketItem;
 import org.restlet.Client;
 
 import org.restlet.data.Form;
 import org.restlet.data.Protocol;
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
     
      // check if server is busy
     public static boolean isRWSAvailable(ConnectionProperties cp) throws RancidApiException {
 
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
 
         String url = cp.getUrl()+cp.getDirectory() + "/";
 
         
         try {
         	getMethodRWS(cp, url);
         }
         catch( RancidApiException e){
         	if (e.getRancidCode() == RancidApiException.RWS_BUSY) {
         		return false;
         	} else {
         		throw(e);
         	}
         }
         return true;
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
 
             for (int ii = 0; ii < doc.getElementsByTagName("Resource").getLength() ; ii++) {
                 String tmp = doc.getElementsByTagName("Resource").item(ii).getTextContent();
 
                 data.add(tmp);
             }
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url + ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
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
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url + ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
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
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url + ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
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
                 throw(new RancidApiException("Error: IOException Method GET: URL:" +url2 + ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
             }
         return rn;
     }
         
     public static RancidNode getRWSRancidNode(String baseUri ,String group, String devicename) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         return getRWSRancidNode(cp, group, devicename );
     }
     
 
     public static void createRWSGroup(ConnectionProperties cp, String group) throws RancidApiException {
     	throw new RancidApiException("Create RWS Group: operation not supported");
     }
     
     //***************************************************************************
     //***************************************************************************
     //Rancid Node Info provisioning
     
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
         putMethodRWS(cp, url,rep);
         
     }
 
     public static void createRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         createRWSRancidNode(cp, rnode);
     }
         
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
 
         postMethodRWS(cp, url,rep);
         
     }
 
     public static void updateRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         updateRWSRancidNode(cp, rnode);
     }
     
     public static void createOrUpdateRWSRancidNode(ConnectionProperties cp, RancidNode rnode) throws RancidApiException{
      
         try {
             updateRWSRancidNode(cp, rnode);
         }
         catch (RancidApiException re){
             if (re.getRancidCode() == RancidApiException.RWS_RESOURCE_NOT_FOUND){
                 createRWSRancidNode(cp, rnode);
             } else {
                 throw(re);
             }
         }
     }
 
     public static void deleteRWSRancidNode(ConnectionProperties cp, RancidNode rnode) throws RancidApiException{
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
                         
         String url = cp.getUrl()+cp.getDirectory()+"/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName();
         deleteMethodRWS(cp, url);
     }
     
     public static void deleteRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         deleteRWSRancidNode(cp, rnode);
     }
     
 
 
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
     
             Date date = format.parse(doc.getElementsByTagName("Date").item(0).getTextContent());
             in.setCreationDate(date);
             in.setConfigurationUrl(doc.getElementsByTagName("UrlViewVC").item(0).getTextContent());
             
             in.setVersionId(version);
                       
         
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url+ ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
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
     // Inventory element list
     // for backward compatibility
     public static InventoryElement getRWSRancidNodeInventoryElement(ConnectionProperties cp, RancidNode rancidNode, String version) throws RancidApiException {
         throw(new RancidApiException("Error: Api is deprecated"));
     }
     //***************************************************************************
     // Inventory element list
 
     public static List<InventoryElement2> getRWSRancidNodeInventoryElement2(ConnectionProperties cp, RancidNode rancidNode, String version) throws RancidApiException {
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         String url = cp.getUrl() + cp.getDirectory()+"/rancid/groups/" + rancidNode.getGroup() + "/" + rancidNode.getDeviceName() + "/configs/" + version + "/inventory";
         
         Response response = getMethodRWS(cp, url);
 
         DomRepresentation dmr = response.getEntityAsDom();
         
         List<InventoryElement2> invlist = new ArrayList<InventoryElement2>();
 
 
         try {
             Document doc = dmr.getDocument();
                         
             int j;
             for (j = 0 ; j < doc.getElementsByTagName("Item").getLength() ; j++){
 
                 InventoryElement2 tee = new InventoryElement2();
                 
                 int i;
                 for (i = 1 ; i < doc.getElementsByTagName("Item").item(j).getChildNodes().getLength() ; i++){
                                   
                     if(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getNodeName().compareTo("Memory") == 0){
                         
                         InventoryMemory im = new InventoryMemory();
                         im.setType(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                         im.setSize(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getChildNodes().item(3).getTextContent());
                         
                         tee.addMemory(im);
     
                     }
                     else if(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getNodeName().compareTo("Software") == 0){
     
                         InventorySoftware im = new InventorySoftware();
                         im.setType(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getChildNodes().item(1).getTextContent());
                         im.setVersion(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getChildNodes().item(3).getTextContent());
                         
                         tee.addSoftware(im);
 
                     }
                     else {
                         
                         Tuple im = new Tuple(doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getNodeName(), 
                                              doc.getElementsByTagName("Item").item(j).getChildNodes().item(i).getTextContent());
                         
                         tee.addTuple(im);
                     }
                     i++;
                 }
                 invlist.add(tee);
             }
             return invlist;
 
         }
         catch( IOException e){
             throw(new RancidApiException("Error getRWSRancidNodeInventoryElement: IOException Method GET: URL:" +url+ ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
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
 
             rna.setDeviceName(devicename);
             rna.setUser(doc.getElementsByTagName("user").item(0).getTextContent());
             rna.setPassword(doc.getElementsByTagName("password").item(0).getTextContent());
             rna.setEnablePass(doc.getElementsByTagName("enablepassword").item(0).getTextContent());
             rna.setConnectionMethod(doc.getElementsByTagName("method").item(0).getTextContent());
             try {
                 rna.setAutoEnable(doc.getElementsByTagName("autoenable").item(0).getTextContent().compareTo("1") == 0);
             }
             catch (Exception e) {
             	rna.setAutoEnable(false);
             }
         }
         catch( IOException e){
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url+ ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
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
         form.add("method", rnodea.getConnectionMethodString());
         
                 
         Representation rep = form.getWebRepresentation();
 
         String url = cp.getUrl() + cp.getDirectory() + "/rancid/clogin/" +rnodea.getDeviceName();
         putMethodRWS(cp, url,rep);        
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
         form.add("method", rnodea.getConnectionMethodString());
         
                 
         Representation rep = form.getWebRepresentation();
 
         String url = cp.getUrl() + cp.getDirectory() + "/rancid/clogin/" +rnodea.getDeviceName();
 
         postMethodRWS(cp, url,rep);        
     }
 
     //***************************************************************************
     //***************************************************************************
     //RancidNodeAuthetication provisioning
     public static void createOrUpdateRWSAuthNode(ConnectionProperties cp, RancidNodeAuthentication rnodea) throws RancidApiException{
         
         try {
             updateRWSAuthNode(cp, rnodea);
         }
         catch (RancidApiException re){
             if (re.getRancidCode() == RancidApiException.RWS_RESOURCE_NOT_FOUND){
                 createRWSAuthNode(cp, rnodea);
             }
             else {
                 throw(re);
             }
         }
 
     }
 
     public static void createOrUpdateRWSAuthNode(String baseUri, RancidNodeAuthentication rnodea) throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         createOrUpdateRWSAuthNode(cp, rnodea);
     }   
     
     public static void deleteRWSAuthNode(ConnectionProperties cp, RancidNodeAuthentication rnodea)throws RancidApiException{
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         String url = cp.getUrl() + cp.getDirectory() + "/rancid/clogin/" +rnodea.getDeviceName();
         deleteMethodRWS(cp, url);
     }
 
     
     
     public static void deleteRWSAuthNode(String baseUri, RancidNodeAuthentication rnodea)throws RancidApiException{
         ConnectionProperties cp = new ConnectionProperties("","",baseUri,"/rws",30);
         deleteRWSAuthNode(cp, rnodea);
     }
     
     public static RWSResourceList getBuckets(ConnectionProperties cp) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(cp,"/storage/buckets/");
         return rwsImpl;
     }
     
     public static RWSBucket getBucket(ConnectionProperties cp, String bucketName ) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         String url = cp.getUrl() + cp.getDirectory()+"/storage/buckets/" + bucketName;
         Response response = getMethodRWS(cp, url);
         DomRepresentation dmr = response.getEntityAsDom();
         
         RWSBucket bucket = new RWSBucket(bucketName);
         try {
             Document doc = dmr.getDocument();
             
             int fileNumber = doc.getElementsByTagName("File").getLength();
         	
             bucket.setBucketItem(new ArrayList<BucketItem>(fileNumber));
             
             for (int j = 0 ; j < fileNumber ; j++){
                String itemName="";
                int itemSize=0;
                Date itemDate=null;
                for (int i = 0; i < doc.getElementsByTagName("File").item(j).getChildNodes().getLength(); i++) {
                    log().debug("Item:" + i +" NodeName: " + doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getNodeName());
                    log().debug("Item:" + i +" NodeTextContent: " + doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getTextContent());

                    if ("Name".equals(doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getNodeName())) {
                        itemName  = doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getTextContent();                        
                    } else if ("Size".equals(doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getNodeName())) {
                        itemSize  = Integer.parseInt(doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getTextContent());                        
                    } else if ("LastModified".equals(doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getNodeName())) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d H:m:s z");
                        itemDate = format.parse(doc.getElementsByTagName("File").item(j).getChildNodes().item(i).getTextContent());
                    }
                }                
                 bucket.setBucket(j, itemName, itemSize, itemDate);               
             }
         
         
         } catch (IOException e) {
             throw(new RancidApiException("Error: IOException Method GET: URL:" +url+ ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
 		} catch (ParseException pe) {
             throw(new RancidApiException("Error: ParseException Method GET: URL:" +url+ ":" + pe.getMessage(), RancidApiException.OTHER_ERROR));
 			
 		}
         
         
         return bucket;
 
     }
     
     public static byte[] getBucketItem(ConnectionProperties cp, String bucketName, String filename) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         String url = cp.getUrl() + cp.getDirectory()+"/storage/buckets/" + bucketName + "?filename=" + filename;
         Response response = getMethodRWS(cp, url);
         response.getEntity();
         throw new RancidApiException("getBucketItem: not implemented");
 //        response.getEntity().getMediaType().equals(MediaType.APPLICATION_OCTET_STREAM);
 //        FileRepresentation fr ;
 //        fr.
 //        response.getEntityAsObject()
         //response.getRe
         //InputStream is = null;
 //        try {
 //			is = response.getEntityAsForm().getWebRepresentation().getAvailableSize();
 //		} catch (IOException e) {
 //            throw(new RancidApiException("Error: IOException Method GET: URL:" +url+ ":" + e.getMessage(), RancidApiException.OTHER_ERROR));
 //		}
 //		return null;
     }
     
     public static void createBucket(ConnectionProperties cp, String bucketName) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         String url = cp.getUrl() + cp.getDirectory()+"/storage/buckets/" + bucketName;
         putMethodRWS(cp, url, new Form().getWebRepresentation());    	
     }
 
     public static void deleteBucket(ConnectionProperties cp, String bucketName) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         String url = cp.getUrl() + cp.getDirectory()+"/storage/buckets/" + bucketName;
         deleteMethodRWS(cp, url);    	
     }
 
     public static void forceDeleteBucket(ConnectionProperties cp, String bucketName) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         String url = cp.getUrl() + cp.getDirectory()+"/storage/buckets/" + bucketName + "?mode=forced";
         deleteMethodRWS(cp, url);    	
     }
     
     public static void updateBucket(ConnectionProperties cp, String bucketName, String fileName, InputStream io) throws RancidApiException {
     	throw new RancidApiException("updateBucket: Not implemented");
     }
 
     public static void deleteBucketItem(ConnectionProperties cp, String bucketName, String fileName) throws RancidApiException {
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         String url = cp.getUrl() + cp.getDirectory()+"/storage/buckets/" + bucketName + "?filename=" + fileName;
         deleteMethodRWS(cp, url);    	
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
         } else {
         	throw(handleException(response, "GET" ,uriReference));
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
         } else {
         	throw(handleException(response, "POST" ,uriReference));
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
         } else {
         	throw(handleException(response, "PUT" ,uriReference));
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
         } else {
         	throw(handleException(response, "DELETE" ,uriReference));
         }
         
     }
     
     static RancidApiException handleException(Response response, String method, String uriReference ) throws RancidApiException {
     
 	    if (response.getStatus() == Status.CLIENT_ERROR_REQUEST_TIMEOUT){
 	        return new RancidApiException("Error: RWS "+ method + " failed for URL:" + uriReference + " Status: "+ response.getStatus(), 
 	        		RancidApiException.RWS_TIMEOUT);
 	    } else if (response.getStatus() == Status.CLIENT_ERROR_UNAUTHORIZED){
 	        return new RancidApiException("Error: RWS "+ method + " failed for URL:" + uriReference + " Status: "+ response.getStatus(),
 	        		RancidApiException.RWS_AUTH_FAILES);
 	    } else if (response.getStatus().getCode() == 404){
 	        return new RancidApiException("Error: RWS "+ method + " failed for URL:" + uriReference + " Status: "+ response.getStatus(), 
 	        		RancidApiException.RWS_RESOURCE_NOT_FOUND);
 	    } else if (response.getStatus().getCode() == 409){
 	        return new RancidApiException("Error: RWS "+ method + " failed for URL:" + uriReference + " Status: "+ response.getStatus(), 
 	        		RancidApiException.RWS_RESOURCE_EXISTS);
 	    } else if (response.getStatus().getCode() == 503){
 	        return new RancidApiException("Error: RWS "+ method + " failed for URL:" + uriReference + " Status: "+ response.getStatus(), 
 	        		RancidApiException.RWS_BUSY);
 	    } else {    	
 	        return new RancidApiException("Error: RWS "+ method + " failed for URL: "+ uriReference + " Status: "+ response.getStatus(), 
 	        		RancidApiException.OTHER_ERROR);
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
    private static Category log() {
        return Logger.getLogger("Rancid");
    }

  
 }
 
 
 
     
