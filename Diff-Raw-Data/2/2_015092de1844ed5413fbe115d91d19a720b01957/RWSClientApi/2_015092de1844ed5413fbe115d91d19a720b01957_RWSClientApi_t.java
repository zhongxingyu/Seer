 package org.opennms.rancid;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 
 import java.io.IOException;
 
 import org.restlet.Client;
 //import org.restlet.data.CharacterSet;
 import org.restlet.data.Form;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Response;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.w3c.dom.Document;
 
 /**
  * This class is the main client for the Rancid API.
  * 
  * <strong>Note: </strong>The class is instatiated with the main Rancid
  * url base Uri.
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
         
     //***************************************************************************
     //***************************************************************************
     //LISTS    
 
     // Generic list of resources
     public static RWSResourceList getRWSResourceList(String baseUri, String subUri ) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+subUri);
         return rwsImpl;
     }
     
     // Services list
     public static RWSResourceList getRWSResourceServicesList(String baseUri ) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+"/rws/");
         return rwsImpl;
     }
 
     // RancidApi resource list
     public static RWSResourceList getRWSResourceRAList(String baseUri) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+"/rws/rancid/");
         return rwsImpl;
     }
 
     // Group list
     public static RWSResourceList getRWSResourceGroupsList(String baseUri) throws RancidApiException {
 
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+"/rws/rancid/groups/");
         return rwsImpl;
     }
     
     // Device list
     public static RWSResourceList getRWSResourceDeviceList(String baseUri, String group) throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+"/rws/rancid/groups/"+group+"/");
         return rwsImpl;
     }
 
     // Login Pattern List
     public static RWSResourceList getRWSResourceLoginPatternList(String baseUri)  throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+"/rws/rancid/clogin/");
         return rwsImpl;
     }
     
     // Version List
     public static RWSResourceList getRWSResourceConfigList(String baseUri, String group, String deviceName)  throws RancidApiException {
         RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
         rwsImpl.ResourcesList = getInfo(baseUri+"/rws/rancid/groups/"+group+"/"+deviceName+"/configs/");
         return rwsImpl;
     }
      
 //    //FUTURE
 //    public RWSResourceList getRWSResourceDeviceVersionList(String Group, String device){
 //        RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
 //        rwsImpl.ResourcesList = getInfo(BaseUri+"/rws/rancid/groups/"+Group+"/"+device+"/");
 //        return rwsImpl;
 //    }
     
     
 //    // To be deleted
 //    public RWSResourceList getRWSResourceList(String group){
 //        RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
 //        rwsImpl.ResourcesList = getInfo(BaseUri+"/rws/rancid/groups/"+group+"/");
 //        return rwsImpl;
 //    }
 
 //    // To be deleted
 //    public RWSResourceList getRWSResourceList(String Group, String device){
 //        RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
 //        String Node;
 //        rwsImpl.ResourcesList = getInfo(BaseUri+"/rws/rancid/groups/"+Group+"/"+device+"/");
 //        return rwsImpl;
 //    }
 
 //    // To be deleted
 //    public RWSResourceList getRWSResourceList(int Type){
 //        
 //        RWSResourceListImpl rwsImpl = new RWSResourceListImpl();
 //        String Node="";
 //    
 //        switch(Type) {
 //        case 1 :
 //            Node = "/rws/";
 //            break;
 //        case 2 :
 //            Node = "/rws/rancid/";
 //            break;
 ////        case 3 :
 ////            Node = "/rws/rancid/groups/";
 ////            break;
 ////        case 6 :
 ////            Node = "/rws/rancid/clogin/";
 ////            break;
 //        }
 //        rwsImpl.ResourcesList = getInfo(BaseUri+Node);
 //        return rwsImpl;
 //    }
     
     private static List<String> getInfo(String fullUri) throws RancidApiException{
         
     	if (!inited){
     		throw(new RancidApiException("Error: Api not initialized"));
     	}
     	
         Reference rwsTest= new Reference(fullUri);
         Response response=client.get(rwsTest);
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
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return data;
     }
     
     //***************************************************************************
     //***************************************************************************
     //Rancid Node Info retrieve
     
     public static RancidNode getRWSRancidNode(String baseUri ,String group, String devicename) throws RancidApiException{
         
     	if (!inited){
     		throw(new RancidApiException("Error: Api not initialized"));
     	}
     	
         Reference rwsTest = new Reference(baseUri + "/rws/rancid/groups/" + group + "/" + devicename);
         Response response =client.get(rwsTest);
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
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         //http://www.rionero.com/rws-current/rws/rancid/groups/laboratorio/7206PED.wind.lab/configs
         //Get the following
 //        <TotalRevisions>18</TotalRevisions>
 //        <HeadRevision>1.18</HeadRevision>
 //        <UrlViewVC>
 //        http://www.rionero.com/viewvc/laboratorio/configs/7206PED.wind.lab?view=log
 //        </UrlViewVC>
         Reference rwsTest2 = new Reference(baseUri + "/rws/rancid/groups/" + group + "/" + devicename+"/configs");
         Response response2 =client.get(rwsTest2);
         DomRepresentation dmr2 = response2.getEntityAsDom();
    
         try {
             Document doc = dmr2.getDocument();
 
             rn.setRootConfigurationUrl(doc.getElementsByTagName("UrlViewVC").item(0).getTextContent());
             rn.setTotalRevisions(doc.getElementsByTagName("TotalRevisions").item(0).getTextContent());
             rn.setHeadRevision(doc.getElementsByTagName("HeadRevision").item(0).getTextContent());        
         }
         catch( IOException e){
             // TODO Auto-generated catch block
             //Silent
         }
         return rn;
 
     }
     
 //    public static RancidNodeAggregate getRancidNodeAggregate(String baseUri,String deviceName) throws RancidApiException {
 //        
 //        if (!inited){
 //            throw(new RancidApiException("Error: Api not initialized"));
 //        }
 //        
 //        try {
 //            RWSResourceList groups = getRWSResourceGroupsList(baseUri);
 //
 //    
 //            List<String> groupList = groups.getResource();
 //            
 //            Iterator iter = groupList.iterator();
 //            
 //            Object tmpGroup;
 //            
 //            RancidNodeAggregate rna = new RancidNodeAggregate();
 //            
 //            rna.setGroups(groupList);
 //            
 //            while (iter.hasNext()) {
 //                RancidNode rn = new RancidNode();
 //                tmpGroup = iter.next();
 //                try {
 //                    //System.out.println("Adding " + (String)tmpGroup + " " + deviceName);
 //                    rn = getRWSRancidNodeInventory(baseUri ,(String)tmpGroup, deviceName);
 //                    rna.addRancidAggregate((String)tmpGroup, rn);
 //                }
 //                catch (Exception e) {
 //                    //skip, Rancid Node not in group
 //                    //but at least in one group
 //                }
 //            }
 //            return rna;
 //        }
 //        catch (Exception e){
 //                e.printStackTrace();
 //        }
 //        return null;
 //    }
     
     //***************************************************************************
     //Rancid Group provisioning
     
     public static void createRWSGroup(String baseUri, String group) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         Form form = new Form();
         form.add("Resource", group );
                 
         Representation rep = form.getWebRepresentation();
 
         Reference rwsTest= new Reference(baseUri+"/rws/rancid/groups/" + group);
         Response response = client.put(rwsTest,rep);
         try {
             response.getEntity().write(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
     
     //***************************************************************************
     //***************************************************************************
     //Rancid Node Info provisioning
     
     public static void createRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         
     	if (!inited){
     		throw(new RancidApiException("Error: Api not initialized"));
     	}
         
         Form form = new Form();
         form.add("deviceType", rnode.getDeviceType());
         form.add("state", rnode.getState());
         form.add("comment", rnode.getComment());
                 
         Representation rep = form.getWebRepresentation();
 
         //rep.setMediaType(MediaType.APPLICATION_XHTML_XML);
         // Launch the request
         //System.out.println(BaseUri+"/rws/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName());
         Reference rwsTest= new Reference(baseUri+"/rws/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName());
         Response response = client.put(rwsTest,rep);
         try {
             response.getEntity().write(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 //        if (response.getStatus().isSuccess()) {
 //            return response.getEntity().getIdentifier();
 //        }
 //
 //        return new Reference("prova");
 
     }
     
     public static void updateRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         
     	if (!inited){
     		throw(new RancidApiException("Error: Api not initialized"));
     	}
         Form form = new Form();
         form.add("deviceType", rnode.getDeviceType());
         form.add("state", rnode.getState());
         form.add("comment", rnode.getComment());
                 
         Representation rep = form.getWebRepresentation();
 
         //rep.setMediaType(MediaType.APPLICATION_XHTML_XML);
         // Launch the request
         System.out.println(baseUri+"/rws/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName());
         Reference rwsTest= new Reference(baseUri+"/rws/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName());
         Response response = client.post(rwsTest,rep);
         try {
             response.getEntity().write(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public static void deleteRWSRancidNode(String baseUri, RancidNode rnode) throws RancidApiException{
         
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
         System.out.println(baseUri+"/rws/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName());
         Reference rwsTest= new Reference(baseUri+"/rws/rancid/groups/"+rnode.getGroup()+"/"+rnode.getDeviceName());
         //Response response = client.delete(rwsTest,rep);
         Response response = client.delete(rwsTest);
         try {
             response.getEntity().write(System.out);
         } catch (IOException e) {
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
     //Inventory Node info retrieve 
     
     public static InventoryNode getRWSInventoryNode(RancidNode rancidNode, String baseUri,  String version ) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         //System.out.println(baseUri + "/rws/rancid/groups/" + rancidNode.getGroup() + "/" + rancidNode.getDeviceName()+"/configs/"+version);
         Reference rwsTest= new Reference(baseUri + "/rws/rancid/groups/" + rancidNode.getGroup() + "/" + rancidNode.getDeviceName()+"/configs/"+version);
         Response response=client.get(rwsTest);
         DomRepresentation dmr = response.getEntityAsDom();
         
         InventoryNode in = new InventoryNode(rancidNode);
                
         //TODO get inventory too
         
         try {
             Document doc = dmr.getDocument();
             // 2008/11/13 13:54:35 UTC
             SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d H:m:s z");
             //System.out.println("DATA "+doc.getElementsByTagName("Date").item(0).getTextContent());
             Date date = format.parse(doc.getElementsByTagName("Date").item(0).getTextContent());
             in.setExpirationDate(date);
             
             in.setConfigurationUrl(doc.getElementsByTagName("UrlViewVC").item(0).getTextContent());
             
             in.setVersionId(version);
                       
         }
         catch( IOException e){
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch( ParseException e){
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return in;
 
     }
 
     //***************************************************************************
     //***************************************************************************
     //Inventory Node info retrieve 
     
     public static RancidNode getRWSRancidNodeInventory(String baseUri, String group, String deviceName) throws RancidApiException{
         
         if (!inited){
             throw(new RancidApiException("Error: Api not initialized"));
         }
         
         RWSResourceList versions = getRWSResourceConfigList(baseUri, group, deviceName);
         RancidNode rn = getRWSRancidNode(baseUri, group, deviceName);
         
         List<String> configlist = versions.getResource();
         Iterator iter1 = configlist.iterator();
         String tmpg1;
         
         while (iter1.hasNext()) {
             tmpg1 = (String)iter1.next();
             //System.out.println("Version " + tmpg1);
             InventoryNode in = getRWSInventoryNode(rn, baseUri, tmpg1);
             rn.addInventoryNode(tmpg1, in);
         }
         
         return rn;
 
     }
 
     
     //***************************************************************************
     //***************************************************************************
     //Authentication info retrieve
     
     public static RancidNodeAuthentication getRWSAuthNode(String baseUri, String devicename) throws RancidApiException{
         
     	if (!inited){
     		throw(new RancidApiException("Error: Api not initialized"));
     	}
         
         Reference rwsTest= new Reference(baseUri + "/rws/rancid/clogin/" + devicename);
         Response response=client.get(rwsTest);
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
                 System.out.println("auto enable field not found");
             }
             //rna.setAuthType(doc.getElementsByTagName("authType").item(0).getTextContent());
             //System.out.println("nel metodo "+ doc.getElementsByTagName("method").item(0).getTextContent());
 
 
         }
         catch( IOException e){
             // TODO Auto-generated catch block
             System.out.println("IOException " + e.getMessage());
             e.printStackTrace();
         }
         //TODO get data
         // copy data
         return rna;
 
     }
     
     
     
     //***************************************************************************
     //***************************************************************************
     //RancidNodeAuthetication provisioning
     
     public static void createOrUpdateRWSAuthNode(String baseUri, RancidNodeAuthentication rnodea) throws RancidApiException{
         
     	if (!inited){
     		throw(new RancidApiException("Error: Api not initialized"));
     	}
         
         System.out.println("createOrUpdateRWSAuthNode changing " + baseUri + "Node " + rnodea.getDeviceName());
         
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
 
         // Launch the request
         
         Reference rwsTest= new Reference(baseUri + "/rws/rancid/clogin/" +rnodea.getDeviceName());
         Response response = client.post(rwsTest,rep);
         try {
             System.out.println("createOrUpdateRWSAuthNode sent:");
             response.getEntity().write(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             System.out.println("createOrUpdateRWSAuthNode exception " + e.getMessage());
             e.printStackTrace();
         }
 
     }   
     
     public static void deleteRWSAuthNode(String baseUri, RancidNodeAuthentication rnodea)throws RancidApiException{
         
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
         System.out.println(baseUri + "/rws/rancid/clogin/" +rnodea.getDeviceName());
         Reference rwsTest= new Reference(baseUri + "/rws/rancid/clogin/" +rnodea.getDeviceName());
         Response response = client.delete(rwsTest);
         try {
             response.getEntity().write(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 //        if (response.getStatus().String _BaseUriiisSuccess()) {
 //            return response.getEntity().getIdentifier();
 //        }
 //
 //        return new Reference("prova"); 
     }
     //private  String BaseUri;
     
 
     
   
  
 }
 
 
 
     
