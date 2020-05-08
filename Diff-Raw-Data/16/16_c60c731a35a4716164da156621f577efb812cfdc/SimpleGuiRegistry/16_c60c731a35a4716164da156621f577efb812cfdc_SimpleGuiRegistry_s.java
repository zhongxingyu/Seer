 
 package edu.duke.cabig.catrip.gui.simplegui;
 
 import edu.duke.cabig.catrip.gui.common.ClassBean;
 import edu.duke.cabig.catrip.gui.common.ForeignAssociationBean;
 import edu.duke.cabig.catrip.gui.common.ServiceMetaDataBean;
 import edu.duke.cabig.catrip.gui.config.GUIConfigurationBean;
 import edu.duke.cabig.catrip.gui.config.GUIConfigurationLoader;
 import edu.duke.cabig.catrip.gui.discovery.DomainModelMetaDataRegistry;
 import edu.duke.cabig.catrip.gui.discovery.DomainModelRetrievalFactory;
 import edu.duke.cabig.catrip.gui.discovery.DomainModelRetrievalStrategy;
 import edu.duke.cabig.catrip.gui.dnd.ClassNode;
 import edu.duke.cabig.catrip.gui.panels.FilterRowPanel;
 import edu.duke.cabig.catrip.gui.query.DCQLRegistry;
 import edu.duke.cabig.catrip.gui.simplegui.objectgraph.GraphAssociation;
 import edu.duke.cabig.catrip.gui.simplegui.objectgraph.GraphObject;
 import edu.duke.cabig.catrip.gui.simplegui.objectgraph.ObjectGraphProcessor;
 import edu.duke.cabig.catrip.gui.simplegui.objectgraph.Service;
 import edu.duke.cabig.catrip.gui.util.SwingUtils;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  *
  * @author Sanjeev Agarwal
  */
 public class SimpleGuiRegistry {
     // array of the filterpanel..
     private static ObjectGraphProcessor processor = new ObjectGraphProcessor(GUIConfigurationLoader.getGUIConfiguration().getConfigRootLocation()+File.separator+"simplegui"+File.separator+"SimpleGuiObjectGraph.xml");
     private static List<GraphObject> currentXMLObjectList = new ArrayList(50);
     private static ArrayList<ClassBean> currentClassBeanList = new ArrayList(50);
     private static GraphObject targetGraphObject = null; // will hold the ref to target object ClassBean object..
     
     private static ArrayList<FilterRowPanel> filters = new ArrayList(10);
     
     private static HashMap beanMap = new HashMap(20); // holds unique classBean instaces which are used in filters..
     private static HashMap currentClassBeanMap = new HashMap(100); // holds unique classBean instances for all the classes used in query..
     
     private static HashMap serviceMap = new HashMap(10);
             
     private static boolean simpleGuiChanged = false;
     
     /** Creates a new instance of SimpleGuiRegistry */
     public SimpleGuiRegistry() {
     }
     
     
     public static ObjectGraphProcessor getProcessor(){
         return processor;
     }
     
     public static void loadMetadata(){
         GUIConfigurationBean guiConfiguration = GUIConfigurationLoader.getGUIConfiguration();
         List<Service> services = processor.getServices();
         
         Service service;
         for (int i=0;i<services.size();i++) {
             service =  services.get(i);
             // bind service to a searchable index by name.
             getServiceMap().put(service.getServiceName(), service);
             
             String domainModelFile = guiConfiguration.getDomainModelMetadataLocation()+File.separator+service.getMetadataXml().trim();
             
             ServiceMetaDataBean sBean = new ServiceMetaDataBean();
             sBean.setDomainModelEndPointRef(domainModelFile);
             sBean.setServiceName(service.getServiceName());
             sBean.setServiceUrl(service.getServiceURL());
             sBean.setIcon(SwingUtils.getTextAsRandomColorImage(service.getServiceName().trim()));
             sBean.needImpl(service.needImpls());
             
 //            // TODO - remove later... get this prop from properties file... same as complex gui...
 //            if (service.getServiceName().equalsIgnoreCase("caTissueCore")){
 //                sBean.needImpl(true);
 //            }
             
             DomainModelRetrievalStrategy retrievalStrategy = DomainModelRetrievalFactory.getRetrievalStrategy(sBean);
             DomainModel domainModel = retrievalStrategy.retrievDomainModel();
             DomainModelMetaDataRegistry.populateDomainModelMetaData(domainModel, sBean);
             
         }
         
         
     }
     
     
     
     
     public static List getCurrentClassBeanList() {
         return currentClassBeanList;
     }
     
     public static void setCurrentClassBeanList(ArrayList list) {
         currentClassBeanList = list;
     }
     
     public static void addClassBeanToList(ClassBean cBean) {
         currentClassBeanList.add(cBean);
     }
     
     
     
     public static void cleanRegistry() {
         setCurrentClassBeanList(new ArrayList(50));
         setCurrentXMLObjectList(new ArrayList(50));
         setFilterList(new ArrayList(10));
         beanMap = new HashMap(20);
         currentClassBeanMap = new HashMap(100);
         
         setTargetGraphObject(null);
         DCQLRegistry.clean();
     }
     
     public static List getCurrentXMLObjectList() {
         return currentXMLObjectList;
     }
     
     public static void setCurrentXMLObjectList(List<GraphObject> objs) {
         currentXMLObjectList = objs;
         
         GraphObject obj;
         GraphAssociation assoc;
         
         for (int i=0;i<objs.size();i++) {
             obj = objs.get(i);
             boolean display = obj.isDisplayable();
             if (display){
                 String[] displaybleAttributes = obj.getDisplaybleAttributes().split(",");
 //                ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByRefId(obj.getRefID()).clone();
                 ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(obj.getClassName()).clone();
                 cBean.filterAttributes(displaybleAttributes);
                 
                 cBean.setAssociationRoleNameMap(new HashMap(20));
                 currentClassBeanList.add(cBean);
                 obj.setClassBean(cBean);
                 addToCurrentClassBeanMap(obj.getClassName(), cBean);
             }else {
 //                ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByRefId(obj.getRefID()).clone();
                 ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(obj.getClassName()).clone();
                 cBean.setAssociationRoleNameMap(new HashMap(20));
                 obj.setClassBean(cBean);
                 addToCurrentClassBeanMap(obj.getClassName(), cBean);
             }
             
         }
         
     }
     
     public static GraphObject getTargetGraphObject() {
         return targetGraphObject;
     }
     
     public static void setTargetGraphObject(GraphObject aTargetGraphObject) {
         if (aTargetGraphObject != null){
             targetGraphObject = aTargetGraphObject;
             ClassNode tNode = new ClassNode();
             tNode.setAssociatedClassObject(targetGraphObject.getClassBean());
             DCQLRegistry.setTargetNode(tNode);
         }
     }
     
     
     public static ArrayList<FilterRowPanel> getFilterList() {
         return filters;
     }
     
     public static void setFilterList(ArrayList<FilterRowPanel> filters) {
         filters = filters;
     }
     
     public static void addFilterToList(FilterRowPanel filter) {
         filters.add(filter);
     }
     
     public static HashMap getBeanMap() {
         return beanMap;
     }
     
     public static void setBeanMap(HashMap aBeanMap) {
         beanMap = aBeanMap;
     }
     
     public static void addToBeanMap(ClassBean cBean) {
         String fullClassName = cBean.getFullyQualifiedName();
         boolean alreadyThere = beanMap.containsKey(fullClassName);
         if (!alreadyThere){
             beanMap.put(fullClassName, cBean);
         }
         
     }
     
     
     
     
     public static void prepareForDcql(){
         // fill the hash map with filled objects only...
         ArrayList<FilterRowPanel> list = getFilterList();
         for (int i = 0; i < list.size(); i++) {
             FilterRowPanel pnl = list.get(i);
             CDEComboboxBean cdeBean = (CDEComboboxBean)pnl.getCdeCombo().getSelectedItem();
             ClassBean cBean = cdeBean.getClassBean();
             addToBeanMap(cBean);
         }
         // pick filters one by one and keep setting the associations in each item...
         
         for (int i = 0; i < list.size(); i++) {
             FilterRowPanel pnl = list.get(i);
             CDEComboboxBean cdeBean = (CDEComboboxBean)pnl.getCdeCombo().getSelectedItem();
             GraphObject filterObject = cdeBean.getGraphObject();
             GraphObject targetObject =  getTargetGraphObject();
             boolean filterIsOnTarget = filterObject.getClassBean().equals(targetObject.getClassBean()) ;
             if (!filterIsOnTarget){
                 addFilterObjectToDCQL(filterObject);
             }
             
         }
         
 //        getTargetGraphObject().getClassBean().printAssociations();
         
     }
     
     
     private static void addFilterObjectToDCQL(GraphObject filterObject){
 
         GraphObject targetObject =  getTargetGraphObject();
         
         if (filterObject.isLocal()){
             // get the association path.. get their beans and then create classBean for each and then add association recursively..
             GraphAssociation assoc;
             List<GraphAssociation> assos = filterObject.getAssociationPathWRTTargetObject();
             
             ClassBean tmpBeanLeft = targetObject.getClassBean();
             for (int k=assos.size()-1;k>=0;k--) {
                 
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+"   " + assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 if (tmpBeanRight == null){
                    // few classes are not defined like ParticipantMedicalIdentifierImpl.. so those are not here in map.. c
                    // co create an instance  of class bean fpr those... after chwecking for null values..
                     // System.out.println("Error : Please add details of class:"+assoc.getClassName()+": in the Association tree of Target Service:"+targetObject.getServiceName()+": in Simple Gui XML,");
                     tmpBeanRight = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(assoc.getClassName()).clone();
                     tmpBeanRight.setAssociationRoleNameMap(new HashMap(20));
                     addToCurrentClassBeanMap(assoc.getClassName(), tmpBeanRight);
                 }
                 
                 tmpBeanLeft.addUniqueAssociation(tmpBeanRight);
                 tmpBeanLeft.addAssociationRoleName(tmpBeanRight.getId(), assoc.getRoleName());
                 tmpBeanLeft.setHasAssociations(true);
                 tmpBeanLeft = tmpBeanRight;
             }
 //                targetObject.getClassBean().printAssociations();
             
 //                filterObject.getClassBean().printAttributes();
         }else {
             // traverse till the outermost object.. and set foreign association there..
             
             String leftProperty = targetObject.getForeignAssociationOutboundCDE();
             String rightProperty = filterObject.getForeignAssociationInboundCDE();
             
             GraphAssociation assoc;
             List<GraphAssociation> assos = targetObject.getForeignAssociationOutboundPath();
             
             ClassBean tmpBeanLeft = targetObject.getClassBean();
             for (int k=0;k<assos.size();k++) {
                 
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+"   " + assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 tmpBeanLeft.addUniqueAssociation(tmpBeanRight);
                 tmpBeanLeft.addAssociationRoleName(tmpBeanRight.getId(), assoc.getRoleName());
                 tmpBeanLeft.setHasAssociations(true);
                 tmpBeanLeft = tmpBeanRight;
             }
             //at the end the "tmpBeanLeft" is the outer most object of that service for the foreign association..
             // So use that object as leftObj..
             ClassBean leftClassBeanObject = tmpBeanLeft;
             ClassBean rightClassBeanObject;
             
             // now get the inbound path for filter object.. and use the first object as right join.
             assos = filterObject.getForeignAssociationInboundPath();
             
             // just add the foreign association to the outermost object of target service....
             assoc = assos.get(0);
             rightClassBeanObject = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
             if (rightClassBeanObject == null){
 //                System.out.println("Error : Please add details of class:"+assoc.getClassName()+": in the Association tree of Target Service:"+filterObject.getServiceName()+": in Simple Gui XML,");
                 rightClassBeanObject = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(assoc.getClassName()).clone();
                 rightClassBeanObject.setAssociationRoleNameMap(new HashMap(20));
                 addToCurrentClassBeanMap(assoc.getClassName(), rightClassBeanObject);
             }
             
             ForeignAssociationBean foreignAssociationBean = new ForeignAssociationBean();
             foreignAssociationBean.setLeftObj(leftClassBeanObject);
             foreignAssociationBean.setLeftProperty(leftProperty);
             foreignAssociationBean.setRighObj(rightClassBeanObject);
             foreignAssociationBean.setRightProperty(rightProperty);
             
             leftClassBeanObject.addUniqueForeignAssociation(foreignAssociationBean);
             leftClassBeanObject.setHasForeignAssociations(true);
             
             // now set the local associations for the inbound path for the filter object in foreign service..
             // now the index will start from 1.
             tmpBeanLeft = rightClassBeanObject;
             for (int k=1;k<assos.size();k++) {
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+" :" +k+":  "+ assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 if (tmpBeanRight == null){
 //                    System.out.println("Error : Please add details of class:"+assoc.getClassName()+": in the Association tree of Target Service:"+filterObject.getServiceName()+": in Simple Gui XML,");
                     tmpBeanRight = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(assoc.getClassName()).clone();
                     tmpBeanRight.setAssociationRoleNameMap(new HashMap(20));
                     addToCurrentClassBeanMap(assoc.getClassName(), tmpBeanRight);
                 }
                 tmpBeanLeft.addUniqueAssociation(tmpBeanRight);
                 tmpBeanLeft.addAssociationRoleName(tmpBeanRight.getId(), assoc.getRoleName());
                 tmpBeanLeft.setHasAssociations(true);
                 tmpBeanLeft = tmpBeanRight;
             }
             
         }
     }
     
     
     public static HashMap getCurrentClassBeanMap() {
         return currentClassBeanMap;
     }
     
     public static void setCurrentClassBeanMap(HashMap aCurrentClassBeanMap) {
         currentClassBeanMap = aCurrentClassBeanMap;
     }
     
     public static void addToCurrentClassBeanMap(String key, ClassBean cBean) {
         currentClassBeanMap.put(key, cBean);
     }
     
     
     
     
     
     public static boolean isSimpleGuiChanged() {
         return simpleGuiChanged;
     }
     
     public static void setSimpleGuiChanged(boolean status) {
         simpleGuiChanged = status;
     }
 
     public static HashMap getServiceMap() {
         return serviceMap;
     }
     
     public static Service getServiceFromMap(String serviceName) {
         return (Service)serviceMap.get(serviceName);
     }
     
     
     
 }
