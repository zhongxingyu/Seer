 package edu.duke.cabig.catrip.gui.simplegui.objectgraph;
 
 import java.io.File;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.xpath.XPath;
 

 public class ObjectGraphProcessor {
     private Document doc = null;
     public ObjectGraphProcessor(String filename) {
         loadDocument(filename);
     }
     
     private void loadDocument(String filename){
         try {
             SAXBuilder builder = new SAXBuilder();
             doc = builder.build(new File(filename));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private GraphObject buildGraphObject(Element associationEle,String serviceName, boolean remoteService){
         GraphObject graphObject = new GraphObject();
         String className = associationEle.getAttributeValue("className");
         graphObject.setClassName(className);
         graphObject.setDisplaybleAttributes(associationEle.getAttributeValue("displaybleAttributes"));
         graphObject.setServiceName(serviceName);
         graphObject.setRefID(associationEle.getAttributeValue("refID"));
         graphObject.setDisplayable(Boolean.parseBoolean(associationEle.getAttributeValue("display")));
         
         List<GraphAssociation> associationPathWRTTargetObject = new ArrayList<GraphAssociation>();
         associationPathWRTTargetObject.add(buildGraphAssociation(associationEle));
         
         if (!remoteService) {
             Element parentAssoc = associationEle.getParentElement();
             while (parentAssoc.getName().equals("Association")) {
                 associationPathWRTTargetObject.add(buildGraphAssociation(parentAssoc));
                 parentAssoc = parentAssoc.getParentElement();
             }
         } else {
             Element childAssoc = associationEle.getChild("Association");
             while (childAssoc != null ){
                 associationPathWRTTargetObject.add(buildGraphAssociation(childAssoc));
                 childAssoc = childAssoc.getChild("Association");
             }
         }
         graphObject.setAssociationPathWRTTargetObject(associationPathWRTTargetObject);
         return graphObject;
     }
 
     private GraphAssociation buildGraphAssociation(Element associationEle){
         GraphAssociation assoc = new GraphAssociation();
         assoc.setClassName(associationEle.getAttributeValue("className"));
         assoc.setRoleName(associationEle.getAttributeValue("roleName"));
         //assoc.setRefID(associationEle.getAttributeValue("refID"));
         return assoc;
     }
     
     private List getNodeListForXpath(String xpathStr){
         XPath xpath;
         List nodeList=null;
         try {
             xpath = XPath.newInstance(xpathStr);
             nodeList = xpath.selectNodes(doc);
         } catch (JDOMException e) {
             e.printStackTrace();
         }
         return nodeList;
     }
     
     public List<Service> getServices(){
         Element rootElement = doc.getRootElement();
         List serviceEleList = rootElement.getChildren();
         
         Iterator servicesItr = serviceEleList.iterator();
         Element serviceEle = null;
         Service service = null;
         List<Service> services = new ArrayList<Service>();
         while (servicesItr.hasNext()) {
             serviceEle = (Element)servicesItr.next();
             service = new Service();
             service.setServiceName(serviceEle.getAttribute("displayName").getValue());
             service.setServiceURL(serviceEle.getAttribute("url").getValue());
             service.setMetadataXml(serviceEle.getAttribute("metadataXml").getValue());
             service.setNeedImpls(Boolean.parseBoolean(serviceEle.getAttribute("impls").getValue()));
             services.add(service);
         }
         return services;
     }
     public List<GraphObject> getTargetObjects() {
         return getTragetObjects(null);
     }
     public List<GraphObject> getTragetObjects(String serviceName) {
         List GraphObjectsNodes = null;
         List<GraphObject> GraphObjects = new ArrayList<GraphObject>();
         GraphObject targetObj = null;
         List<GraphAssociation> foreignAssociationOutboundPath = null;
         
         
         try {           
             String xpathStr;
             xpathStr = "/simpleGuiServices/service/targets/objectElement";
             if (serviceName != null) {
                 xpathStr = "/simpleGuiServices/service[@displayName='"+serviceName+"']/targets/objectElement";  
             }
             GraphObjectsNodes = getNodeListForXpath(xpathStr);
             for (int i=0;i<GraphObjectsNodes.size();i++){
                 foreignAssociationOutboundPath = new ArrayList<GraphAssociation>();
                 Element objectElement = (Element)GraphObjectsNodes.get(i);
                 String serviceName1 =  objectElement.getParentElement().getParentElement().getAttributeValue("displayName");
                 Element foreignAssociationOutboundPathEle = objectElement.getChild("foreignAssociationOutboundPath");
                 Element associationEle = null;
                 
                 associationEle = foreignAssociationOutboundPathEle.getChild("Association");
                 foreignAssociationOutboundPath.add(buildGraphAssociation(associationEle));
                 
                 while (associationEle.getChild("Association") != null ) { 
                     associationEle = associationEle.getChild("Association");
                     foreignAssociationOutboundPath.add(buildGraphAssociation(associationEle));
                 }              
                 
                 targetObj = new GraphObject();
                 targetObj.setClassName(objectElement.getAttributeValue("className"));
                 targetObj.setRefID(objectElement.getAttributeValue("refID"));
                 targetObj.setDisplaybleAttributes(objectElement.getAttributeValue("displaybleAttributes"));
                 targetObj.setDisplayable(Boolean.parseBoolean(objectElement.getAttributeValue("display")));
                 targetObj.setForeignAssociationOutboundCDE(foreignAssociationOutboundPathEle.getAttributeValue("cdeName"));
                 targetObj.setForeignAssociationOutboundPath(foreignAssociationOutboundPath);
                 targetObj.setServiceName(serviceName1);
                 
                 GraphObjects.add(targetObj);
                 
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return GraphObjects;
     }
     public List<GraphObject> getAssociatedObjects(String className,String serviceName){
         return getAssociatedObjects(className,serviceName,false);
     }
     public List<GraphObject> getAssociatedObjects(String className,String serviceName,boolean remoteService){
         List associationsNodes = null;
         List<GraphObject> associatedObjects = new ArrayList<GraphObject>();
         String xpathStr = null;
         try {
             xpathStr = "/simpleGuiServices/service[@displayName='"+serviceName+"']/targets/objectElement[@className='"+className+"']/associatedObjectTree/Association";
             if (remoteService){
                 xpathStr = "/simpleGuiServices/service[@displayName='"+serviceName+"']/foreignAssociationInboundTree/foreignAssociationInboundPath[@className='"+className+"']/Association";  
             }
             associationsNodes = getNodeListForXpath(xpathStr);
 
             for (int i=0;i<associationsNodes.size();i++){
                 Element associationEle = (Element)associationsNodes.get(i);                
                 associatedObjects.add(buildGraphObject(associationEle,serviceName,remoteService));
                 //System.out.println(associationEle.getAttributeValue("className"));
                 while (associationEle!=null && associationEle.getChildren("Association").size() > 0 ) {
                     List assx = associationEle.getChildren("Association");
//                    System.out.println("XXXXXXXXXXXXXXXXXXXXXX :"+assx.size());
                     Iterator itr = assx.iterator();
                     while (itr.hasNext()) {
                        Element e = (Element)itr.next();
//                        System.out.println(" #####  :"+e.getAttribute("className")+"\n" );
                        associatedObjects.add(buildGraphObject(e,serviceName,remoteService));
//                        associationEle = associationEle.getChild("Association"); 
                          // this line creates problem when there are more than 2 association element exist at same level. Moving it down works.
                     }
                    associationEle = associationEle.getChild("Association");
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return associatedObjects;
     }
 
     public List<GraphObject> getAvialbleTargetObjectsToAssociateInRemoteServices(String localServiceName) {
         List<GraphObject> avialableObjects = new ArrayList<GraphObject>();
         GraphObject targetObj = null;
         List<GraphAssociation> foreignAssociationInboundPath;
         try {
             String xpathStr = "/simpleGuiServices/service[@displayName!='"+localServiceName+"']/foreignAssociationInboundTree/foreignAssociationInboundPath";
             //System.out.println(xpathStr);
             List objectsFound = getNodeListForXpath(xpathStr);
             String serviceName;
             for (int i=0;i<objectsFound.size();i++){
                 Element objectElement = (Element)objectsFound.get(i); 
                 serviceName = objectElement.getParentElement().getParentElement().getAttributeValue("displayName");
                 targetObj = new GraphObject();
                 targetObj.setClassName(objectElement.getAttributeValue("className"));
                 targetObj.setDisplaybleAttributes(objectElement.getAttributeValue("displaybleAttributes"));
                 targetObj.setForeignAssociationInboundCDE(objectElement.getAttributeValue("cdeName"));
                 targetObj.setDisplayable(Boolean.parseBoolean(objectElement.getAttributeValue("display")));
                 targetObj.setServiceName(serviceName);
                 targetObj.setRefID(objectElement.getAttributeValue("refID"));
                 targetObj.setLocalStatus(false); //  mark this object as foreign object to the target service..
                 
                 foreignAssociationInboundPath = new ArrayList<GraphAssociation>();
                 Element associationEle = objectElement.getChild("Association");
                 foreignAssociationInboundPath.add(buildGraphAssociation(associationEle));
                 
                 while (associationEle.getChild("Association") != null ) { 
                     associationEle = associationEle.getChild("Association");
                     foreignAssociationInboundPath.add(buildGraphAssociation(associationEle));
                 } 
                 targetObj.setForeignAssociationInboundPath(foreignAssociationInboundPath);
                 
                 avialableObjects.add(targetObj);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return avialableObjects;
         
     }
 
 }
