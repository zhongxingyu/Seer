 
 package edu.duke.cabig.catrip.gui.simplegui;
 
 import edu.duke.cabig.catrip.gui.common.AttributeBean;
 import edu.duke.cabig.catrip.gui.common.ClassBean;
 import edu.duke.cabig.catrip.gui.common.ClassBeanGroup;
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
 import java.util.HashSet;
 import java.util.List;
 import org.apache.commons.logging.Log;
 import edu.duke.cabig.catrip.gui.util.Logger;
 
 /**
  *
  * @author Sanjeev Agarwal
  */
 public class SimpleGuiRegistry {
     
     // <editor-fold defaultstate="collapsed" desc=" Member Declarations ">
     private static ObjectGraphProcessor processor = new ObjectGraphProcessor(GUIConfigurationLoader.getGUIConfiguration().getConfigRootLocation()+File.separator+"simplegui"+File.separator+"SimpleGuiObjectGraph.xml");
     private static List<GraphObject> currentXMLObjectList = new ArrayList(50);
     
     // sanjeev: holds unique classBean instances for all the classes that can be used in query.. This contains all the displayble classes..
     private static ArrayList<ClassBean> currentClassBeanList = new ArrayList(50);
     
     private static GraphObject targetGraphObject = null; // will hold the ref to target object ClassBean object..
     
     // array of the filterpanel.. currently added to the simple gui.. each one represent a unique filter..
     private static ArrayList<FilterRowPanel> filters = new ArrayList(50);
     private static ArrayList<FilterRowPanel> phonyFilters = new ArrayList(10);
     
     
     private static HashMap beanMap = new HashMap(20); // sanjeev: holds unique classBean instaces which are used in filters.. filled ones..
     private static HashMap currentClassBeanMap = new HashMap(100); // sanjeev: holds unique classBean instances for all the classes that are defined in the simple gui xml.. the objects get's populated laong the way in prepareForDcql method.
     
     private static HashMap serviceMap = new HashMap(20);
     
     private static boolean simpleGuiChanged = false; // sanjeev: this is to tell that something is changed so calculate the DCQL again.
     
     
     
     
     
     // AND / OR group members..
     private static ArrayList<FilterGroup> filterSubGroups =  new ArrayList(50);
     private static FilterGroup rootGroup;
     private static ArrayList<FilterRowPanel> nonGroupFilters = new ArrayList(50); // this represents the filterPanels that are not used in any of the AND/OR groups at a given time.
     private static int numGroupableEntities = 0; // total number of non-group filterPanels plus sub-level groups..
     private static boolean hasGroupsDefined = false;
     // AND / OR group members..
     
     
     
     // Returned Attributes....
     private static boolean returnedAttributeListAvailable = false;
     private static HashMap classNameReturnedAttributeMap = new HashMap(); // map of FullClassName vs List of attribute names..
     private static int numReturnedAttribute = 0;
     private static HashSet returnedAttributeForeignServices = new HashSet();
     // Returned Attributes....
     
     
     
     // query sharing
     private static ArrayList<GraphObject> allSimpleGuiXMLObjectList = new ArrayList(500);
     // query sharing
     
     // Define Logger..
     static Log log = Logger.getDefaultLogger();
 
     
     
     // </editor-fold>
     
     
     /** Creates a new instance of SimpleGuiRegistry */
     public SimpleGuiRegistry() {
     }
     
     
     public static ObjectGraphProcessor getProcessor(){
         return processor;
     }
     
     public static void loadMetadata(){// <editor-fold defaultstate="collapsed">
         GUIConfigurationBean guiConfiguration = GUIConfigurationLoader.getGUIConfiguration();
         List<Service> services = processor.getServices();
         
         Service service;
         for (int i=0;i<services.size();i++) {
             service =  services.get(i);
             // sanjeev: bind service to a searchable index by name.
             getServiceMap().put(service.getServiceName(), service);
             
             String domainModelFile = guiConfiguration.getDomainModelMetadataLocation()+File.separator+service.getMetadataXml().trim();
             
             ServiceMetaDataBean sBean = new ServiceMetaDataBean();
             sBean.setDomainModelEndPointRef(domainModelFile);
             sBean.setServiceName(service.getServiceName());
             sBean.setServiceUrl(service.getServiceURL());
             sBean.setIcon(SwingUtils.getTextAsRandomColorImage(service.getServiceName().trim()));
             sBean.needImpl(service.needImpls());
             
             
 //            //  - remove later... get this prop from properties file... same as complex gui...
 //            if (service.getServiceName().equalsIgnoreCase("caTissueCore")){
 //                sBean.needImpl(true);
 //            }
             
             DomainModelRetrievalStrategy retrievalStrategy = DomainModelRetrievalFactory.getRetrievalStrategy(sBean);
             DomainModel domainModel = retrievalStrategy.retrievDomainModel();
             // Now populate the Domain Model registry with this service metadata.
             DomainModelMetaDataRegistry.populateDomainModelMetaData(domainModel, sBean);
             
         }
         
         
     }// </editor-fold>
     
     
     
     
     public static List getCurrentClassBeanList() {
         return currentClassBeanList;
     }
     
     public static void setCurrentClassBeanList(ArrayList list) {
         currentClassBeanList = list;
     }
     
     public static void addClassBeanToList(ClassBean cBean) {
         currentClassBeanList.add(cBean);
     }
     
     
     
     public static void cleanRegistry() {// <editor-fold defaultstate="collapsed">
         setCurrentClassBeanList(new ArrayList(50));
         setCurrentXMLObjectList(new ArrayList(50));
         setFilterList(new ArrayList(50));
         beanMap = new HashMap(20);
         currentClassBeanMap = new HashMap(100);
         filters = new ArrayList(50);
         
         
         setTargetGraphObject(null);
         DCQLRegistry.clean();
         
         
         // TODO - sanju AND/OR   : this keeps the old groups filters.. even if you clear all filters...
         // clean things from AND/OR grouping
         setNonGroupFilters(new ArrayList(50));
         setFilterSubGroupList(new ArrayList(50));
         setNumGroupableEntities(0);
         setHasGroupsDefined(false);
         
         
         // clean things for returned attributes..
         setReturnedAttributeListAvailable(false);
         setClassNameReturnedAttributeMap(new HashMap());
         setNumReturnedAttribute(0);
         
         
     }// </editor-fold>
     
     public static List getCurrentXMLObjectList() {
         return currentXMLObjectList;
     }
     
     public static void setCurrentXMLObjectList(List<GraphObject> objs) {// <editor-fold defaultstate="collapsed">
         currentXMLObjectList = objs;
         
         GraphObject obj;
         GraphAssociation assoc;
         
         for (int i=0;i<objs.size();i++) {
             obj = objs.get(i);
             boolean display = obj.isDisplayable();
             if (display){
                 String[] displaybleAttributes = obj.getDisplaybleAttributes().split(",");
 //                ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByRefId(obj.getRefID()).clone();
                 ClassBean cBean =null;
                 try {
                     cBean = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(obj.getClassName()).clone();
                 } catch (Exception e) {
                     System.out.println("XXXX cound not find the class in registry: "+obj.getClassName());
                     e.printStackTrace();
                 }
                 
                 cBean.filterAttributes(displaybleAttributes);
                 
                 cBean.setAssociationRoleNameMap(new HashMap(20));
                 currentClassBeanList.add(cBean); // only visible / filterable classes...
                 obj.setClassBean(cBean);
                 addToCurrentClassBeanMap(obj.getClassName(), cBean); // fully classified class name vs ClassBean instance.
             }else {
 //                ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByRefId(obj.getRefID()).clone();
                 ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(obj.getClassName()).clone();
                 cBean.setAssociationRoleNameMap(new HashMap(20));
                 obj.setClassBean(cBean);
                 addToCurrentClassBeanMap(obj.getClassName(), cBean); // fully classified class name vs ClassBean instance.
             }
             
         }
         
     }// </editor-fold>
     
     public static GraphObject getTargetGraphObject() {
         return targetGraphObject;
     }
     
     public static void setTargetGraphObject(GraphObject aTargetGraphObject) {// <editor-fold defaultstate="collapsed">
         if (aTargetGraphObject != null){
             targetGraphObject = aTargetGraphObject;
             ClassNode tNode = new ClassNode(); // this is just to reuse the code.. ClassNode is a Graph object.. which is really not used here..
             tNode.setAssociatedClassObject(targetGraphObject.getClassBean());
             DCQLRegistry.setTargetNode(tNode);
         }
     }// </editor-fold>
     
     
     public static ArrayList<FilterRowPanel> getFilterList() {
         return filters;
     }
     
     public static void setFilterList(ArrayList<FilterRowPanel> filters_) {
         filters = filters_;
     }
     public static void addFilterToList(FilterRowPanel filter) {
         getFilterList().add(filter);
         addNonGroupFilter(filter);   // TODO - these two should not be together..
     }
     public static void addPhonyFilterToList(FilterRowPanel filter) {
         getFilterList().add(filter);
         //addNonGroupFilter(filter);   // do not add a phony filter to a group as that is only to support returned attribute.
         getPhonyFilterList().add(filter); // add to the phony filter list as well.
     }
     public static ArrayList<FilterRowPanel> getPhonyFilterList() {
         return phonyFilters;
     }
     public static void setPhonyFilterList(ArrayList<FilterRowPanel> pFilters) {
         phonyFilters = pFilters;
     }
     public static void cleanPhonyFilterList (){
         ArrayList<FilterRowPanel> pFilters = getPhonyFilterList();
         for (int i = 0; i < pFilters.size(); i++) {
             FilterRowPanel fPanel = pFilters.get(i);
             getFilterList().remove(fPanel);
         }
         setPhonyFilterList(new ArrayList<FilterRowPanel>(10));
         setSimpleGuiChanged(true);
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
         if (!alreadyThere){  // check if you have to overwrite the previous instance
             beanMap.put(fullClassName, cBean);
         }
         
     }
     
     
     
     
     public static void prepareForDcql(){// <editor-fold defaultstate="collapsed">
         log.info(" Preparing the Association tree in Simple GUI for generation of DCQL. "); 
         // set this flag again.. as it is done now..
         setSimpleGuiChanged(false);
         
         //clone the TargetObject bean in registry.. just to make sure that it is fresh..
         GraphObject target = getTargetGraphObject().clone();
         setTargetGraphObject(target);
         
         // clean the bean map, from the previous operations.
         setBeanMap(new HashMap());
         
         // sanjeev: fill the hash map with filled objects only...
         ArrayList<FilterRowPanel> list = getFilterList();
         for (int i = 0; i < list.size(); i++) {
             FilterRowPanel pnl = list.get(i);
 //            CDEComboboxBean cdeBean = pnl.getCDEComboboxBean();
             ClassBean cBean = pnl.getClassBean();
             
             addToBeanMap(cBean);  // TODO - // currently this is unique instances based on class name... these are filled filters... change this...
             // here I guess add a clonedMap.. and start from there.. and keep modifying these instances.. with groups and attributes..
 //            setBeanMap( ClassBean.cloneClassBeanMap(getCurrentClassBeanMap()) ); // this is done so that every time the gui is changed the map is new one altogether..
             
         }
         
 //        setBeanMap( ClassBean.cloneClassBeanMap(getCurrentClassBeanMap()) );
         
         // sanjeev: pick filters one by one and keep setting the associations in each item...
         
         for (int i = 0; i < list.size(); i++) {
             FilterRowPanel pnl = list.get(i);
 //            CDEComboboxBean cdeBean = pnl.getCDEComboboxBean();
             GraphObject filterObject = pnl.getGraphObject();
             GraphObject targetObject =  getTargetGraphObject();
             boolean filterIsOnTarget = filterObject.getClassBean().getFullyQualifiedName().equals(targetObject.getClassBean().getFullyQualifiedName()) ; // Before I was comparing the instances...
             if (!filterIsOnTarget && !pnl.isEmpty()){
                 addFilterPanelObjectToDCQL(pnl);//addFilterObjectToDCQL(filterObject); // send the panel so that it has the gui grouping information as well..
             }
             
             
            else {
                 // even if the filter is on target object.. check the grouping stuff.. there may be group of the attributes as well..
                 ClassBean targetBean = targetObject.getClassBean();
                 ClassBeanGroup group = null;
                 boolean hasGroup = pnl.hasParentGroup();
                 
                 if (hasGroup){
                     // TODO - check for group type.. if it is only attribute type then only add stuff.. otherwise things are taken care in the other place..
                     
                     group = new ClassBeanGroup(pnl.getParentGroup().getCondition());
                     group.setGroupId(pnl.getParentGroup().getUniqueId());
                     
                     if (!targetBean.hasGroup(group)){// check for the duplicate group.. if not there then add the group..
                         group.add(pnl.getAttributeBean());
                         targetBean.addGroup(group);
                     } else { // if found use the old one.. and add the attribute in that group..
                         targetBean.getGroupById(group.getGroupId()).add(pnl.getAttributeBean());
                     }
                 }
                 
                 // each filterPanel has different instances.. so you need to merge them.. for target object..
                 else {
                     targetBean.addUniqueAttribute(pnl.getAttributeBean()); // this is a bad way..  you should be merging the ClassBean..
                 }
                 
             }
             
             
         }
         
         
         
         
         
         
         // by default return all the attributes of Target Object..
 //        if (!hasGroupsDefined()){
         ClassBean targetBean = getTargetGraphObject().getClassBean();
         ArrayList atts = targetBean.getAttributes();
         for (int i = 0; i < atts.size(); i++) {
             String cName = targetBean.getFullyQualifiedName();
             String attName = ((AttributeBean)atts.get(i)).getAttributeName();
             addToClassNameReturnedAttributeMap(cName, attName);
         }
         
         
         
         
         
         // using this method to print the association tree starting from teh target object.
 //        getTargetGraphObject().getClassBean().printAssociations();
         
     } // </editor-fold>
     
     // setting the filter as association for the target object.
     private static void addFilterPanelObjectToDCQL(FilterRowPanel pnl){ // <editor-fold defaultstate="collapsed">
         GraphObject filterObject = pnl.getGraphObject();
         
         GraphObject targetObject =  getTargetGraphObject();
         ClassBeanGroup group = null;
         // check if the filter was in a group.. if yes than create a classBeangroup with same ID and condition..
         boolean hasGroup = pnl.hasParentGroup();
         
         
         if (filterObject.isLocal()){ // <editor-fold>
             // sanjeev: get the association path.. get their beans and then create classBean for each and then add association recursively..
             GraphAssociation assoc;
             List<GraphAssociation> assos = filterObject.getAssociationPathWRTTargetObject();
             
             ClassBean tmpBeanLeft = targetObject.getClassBean();
             for (int k=assos.size()-1;k>=0;k--) {
                 
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+"   " + assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = null;//(ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 
                 // first check for the filters with values..  the first filter is taken..
                 // TODO - this is the place where you have to get multiple values..
                 tmpBeanRight = (ClassBean)getBeanMap().get(assoc.getClassName()); // get this as blank bean for right.
                 if (k==0){ //  last one is in filterPanel.. so get it from filterPanel..
                     tmpBeanRight = pnl.getClassBean();
                 }
                 
                 
                 // check again in the all possible filters.. // get a blank associated Bean. // it should be in here..
                 if (tmpBeanRight == null){
 //                    tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName()); // currentClassBeanMap contains the classBeans defined in simple gui xml..
                     tmpBeanRight = ((ClassBean)getCurrentClassBeanMap().get(assoc.getClassName())).clone(); // clone it.. as they are temporary and are getting merged..
                 }
                 
                 // get the class name from the metadata.. the simple config file was not proper..
                 if (tmpBeanRight == null){
                     // System.out.println("Error : Please add details of class:"+assoc.getClassName()+": in the Association tree of Target Service:"+targetObject.getServiceName()+": in Simple Gui XML,");
                     tmpBeanRight = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(assoc.getClassName()).clone();
                     tmpBeanRight.setAssociationRoleNameMap(new HashMap(20));
                     addToCurrentClassBeanMap(assoc.getClassName(), tmpBeanRight); // fully classified name vs object.
                 }
                 
                 // cache this instance..
                 addToBeanMap(tmpBeanRight);
                 
                 
                 
                 // TODO - here I add the association... here I have to add the group also..
                 if (hasGroup){
                     group = new ClassBeanGroup(pnl.getParentGroup().getCondition());
                     group.setGroupId(pnl.getParentGroup().getUniqueId());
                     
                     if (!tmpBeanLeft.hasGroup(group)){// check for the duplicate group.. if not there then add the group..
                         group.add(tmpBeanRight);
                         group.addClassRole(tmpBeanRight.getId(), assoc.getRoleName());
                         tmpBeanLeft.addGroup(group);
                     } else { // if found use the old one.. and add the right classBean in that group..
                         tmpBeanLeft.getGroupById(group.getGroupId()).add(tmpBeanRight);
                     }
                 }
                 
                 else {  // add direct associations only if there is no group..
                     tmpBeanLeft.addUniqueAssociation(tmpBeanRight);
                     tmpBeanLeft.addAssociationRoleName(tmpBeanRight.getId(), assoc.getRoleName());
                     // combine above two in one method call.. so as to avoide the setting of roles for duplicates.. there may be two different roles for the same class resulting from a wrong simple gui config file..
                     tmpBeanLeft.setHasAssociations(true);
                 }
                 
                 // add it to our tmp tree..
 //                tmpBeanLeft = tmpBeanRight; // here you get the left beans from that list.. not this object..
                 tmpBeanLeft = ((ClassBean)getBeanMap().get(tmpBeanRight.getFullyQualifiedName()));  // left object always point to that same list..
                 
             }
             // at the end of iterating the whole association loop..  tmpBeanLeft is the right most bean which contains the filter value..
             
             addToBeanMap(tmpBeanLeft);
             
             
             // set the group with attribute here...
             if (hasGroup){
                 group = new ClassBeanGroup(pnl.getParentGroup().getCondition());
                 group.setGroupId(pnl.getParentGroup().getUniqueId());
                 
 //                System.out.println("xxx adding attributes to"+tmpBeanLeft);
                 
                 if (!tmpBeanLeft.hasGroup(group)){// check for the duplicate group.. if not there then add the group..
                     // remove that Attribute Bean from the list of class also.. this will ensure that the classBean merging will not add to attributes which are in group..
                     tmpBeanLeft.getAttributes().remove(pnl.getAttributeBean());
                     group.add(pnl.getAttributeBean());
                     tmpBeanLeft.addGroup(group);
                 } else { // if found use the old one.. and add the attribute in that group..
                     tmpBeanLeft.getAttributes().remove(pnl.getAttributeBean());
                     tmpBeanLeft.getGroupById(group.getGroupId()).add(pnl.getAttributeBean());
                 }
             } else {
                 tmpBeanLeft.getAttributes().remove(pnl.getAttributeBean());
                 tmpBeanLeft.addAttribute(pnl.getAttributeBean());
             }
             
             
             
 //            System.out.println("XXXXX "+tmpBeanLeft);
             
 //                targetObject.getClassBean().printAssociations();
             
 //                filterObject.getClassBean().printAttributes();
             // </editor-fold>
         }else { // <editor-fold>
             // sanjeev: traverse till the outermost object.. and set foreign association there..
             
             String leftProperty = targetObject.getForeignAssociationOutboundCDE();
             String rightProperty = filterObject.getForeignAssociationInboundCDE();
             
             GraphAssociation assoc;
             List<GraphAssociation> assos = targetObject.getForeignAssociationOutboundPath();
             
             ClassBean tmpBeanLeft = targetObject.getClassBean();
             for (int k=0;k<assos.size();k++) {
                 
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+"   " + assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 // sanjeev: check here if the class is available in the GraphObject tree of the Target object or not.
                 // sanjeev: otherwise locate that class from the metaData registry instead of the list.
                 ClassBean tmpBeanRight = null;//(ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 
                 // first check for the filters with values..  the first filter is taken..
                 tmpBeanRight = (ClassBean)getBeanMap().get(assoc.getClassName());
                 
                 // check again in the all possible filters..
                 if (tmpBeanRight == null){
                     tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 }
                 
                 // get the class name from the metadata.. the simple config file was not proper..
                 if (tmpBeanRight == null){
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
             // sanjeev: at the end the "tmpBeanLeft" is the outer most object of that service for the foreign association..
             // sanjeev: So use that object as leftObj..
             ClassBean leftClassBeanObject = tmpBeanLeft;
             ClassBean rightClassBeanObject;
             
             // sanjeev: now get the inbound path for filter object.. and use the first object as right join.
             assos = filterObject.getForeignAssociationInboundPath();
             
             // sanjeev: just add the foreign association to the outermost object of target service....
             assoc = assos.get(0);
             
             // first check for the filters with values..  the first filter is taken..
             rightClassBeanObject = (ClassBean)getBeanMap().get(assoc.getClassName());
             
             // check again in the all possible filters..
             if (rightClassBeanObject == null){
                 rightClassBeanObject = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
             }
             
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
             
             // set the returned attribute from FA in registry..
             // add the left class only when there is no returned attribute of this FA..
             boolean serviceInvolved = hasReturnedAttributesFromService(rightClassBeanObject.getServiceName());
             if (serviceInvolved){
                 SimpleGuiRegistry.addToClassNameReturnedAttributeMap(leftClassBeanObject.getFullyQualifiedName(), leftProperty);
             }
             SimpleGuiRegistry.addToClassNameReturnedAttributeMap(rightClassBeanObject.getFullyQualifiedName(), rightProperty);
             
             
             // sanjeev: now set the local associations for the inbound path for the filter object in foreign service..
             // sanjeev: now the index will start from 1.
             tmpBeanLeft = rightClassBeanObject;
             for (int k=1;k<assos.size();k++) {
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+" :" +k+":  "+ assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = null;//(ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 
                 // first check for the filters with values..  the first filter is taken..
                 tmpBeanRight = (ClassBean)getBeanMap().get(assoc.getClassName());
                 
                 // check again in the all possible filters..
                 if (tmpBeanRight == null){
                     tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 }
                 
                 
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
             
         } // </editor-fold>
     }// </editor-fold>
     
     
     
     
     
     // original method.. not used anymore now..
     private static void addFilterObjectToDCQL(GraphObject filterObject){ // <editor-fold defaultstate="collapsed">
 //       GraphObject filterObject = ((CDEComboboxBean)pnl.getCdeCombo().getSelectedItem()).getGraphObject();
         
         GraphObject targetObject =  getTargetGraphObject();
         
         if (filterObject.isLocal()){
             // sanjeev: get the association path.. get their beans and then create classBean for each and then add association recursively..
             GraphAssociation assoc;
             List<GraphAssociation> assos = filterObject.getAssociationPathWRTTargetObject();
             
             ClassBean tmpBeanLeft = targetObject.getClassBean();
             for (int k=assos.size()-1;k>=0;k--) {
                 
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+"   " + assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = null;//(ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 
                 // first check for the filters with values..  the first filter is taken..
                 // TODO - this is the place where you have to get multiple values..
                 tmpBeanRight = (ClassBean)getBeanMap().get(assoc.getClassName());
                 
                 // check again in the all possible filters.. // get a blank associated Bean.
                 if (tmpBeanRight == null){
                     tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName()); // currentClassBeanMap contains the classBeans defined in simple gui xml..
                 }
                 
                 // get the class name from the metadata.. the simple config file was not proper..
                 if (tmpBeanRight == null){
                     // System.out.println("Error : Please add details of class:"+assoc.getClassName()+": in the Association tree of Target Service:"+targetObject.getServiceName()+": in Simple Gui XML,");
                     tmpBeanRight = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(assoc.getClassName()).clone();
                     tmpBeanRight.setAssociationRoleNameMap(new HashMap(20));
                     addToCurrentClassBeanMap(assoc.getClassName(), tmpBeanRight);
                 }
                 
                 // TODO - here I add the association... here I have to add the group also..
                 
                 tmpBeanLeft.addUniqueAssociation(tmpBeanRight);
                 tmpBeanLeft.addAssociationRoleName(tmpBeanRight.getId(), assoc.getRoleName());
                 // combine above two in one method call.. so as to avoide the setting of roles for duplicates.. there may be two different roles for the same class resulting from a wrong simple gui config file..
                 
                 tmpBeanLeft.setHasAssociations(true);
                 tmpBeanLeft = tmpBeanRight;
             }
 //                targetObject.getClassBean().printAssociations();
             
 //                filterObject.getClassBean().printAttributes();
         }else {
             // sanjeev: traverse till the outermost object.. and set foreign association there..
             
             String leftProperty = targetObject.getForeignAssociationOutboundCDE();
             String rightProperty = filterObject.getForeignAssociationInboundCDE();
             
             GraphAssociation assoc;
             List<GraphAssociation> assos = targetObject.getForeignAssociationOutboundPath();
             
             ClassBean tmpBeanLeft = targetObject.getClassBean();
             for (int k=0;k<assos.size();k++) {
                 
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+"   " + assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 // sanjeev: check here if the class is available in the GraphObject tree of the Target object or not.
                 // sanjeev: otherwise locate that class from the metaData registry instead of the list.
                 ClassBean tmpBeanRight = null;//(ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 
                 // first check for the filters with values..  the first filter is taken..
                 tmpBeanRight = (ClassBean)getBeanMap().get(assoc.getClassName());
                 
                 // check again in the all possible filters..
                 if (tmpBeanRight == null){
                     tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 }
                 
                 // get the class name from the metadata.. the simple config file was not proper..
                 if (tmpBeanRight == null){
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
             // sanjeev: at the end the "tmpBeanLeft" is the outer most object of that service for the foreign association..
             // sanjeev: So use that object as leftObj..
             ClassBean leftClassBeanObject = tmpBeanLeft;
             ClassBean rightClassBeanObject;
             
             // sanjeev: now get the inbound path for filter object.. and use the first object as right join.
             assos = filterObject.getForeignAssociationInboundPath();
             
             // sanjeev: just add the foreign association to the outermost object of target service....
             assoc = assos.get(0);
             
             // first check for the filters with values..  the first filter is taken..
             rightClassBeanObject = (ClassBean)getBeanMap().get(assoc.getClassName());
             
             // check again in the all possible filters..
             if (rightClassBeanObject == null){
                 rightClassBeanObject = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
             }
             
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
             
             
             
             // sanjeev: now set the local associations for the inbound path for the filter object in foreign service..
             // sanjeev: now the index will start from 1.
             tmpBeanLeft = rightClassBeanObject;
             for (int k=1;k<assos.size();k++) {
                 assoc = assos.get(k);
 //                    System.out.println(filterObject.getClassName()+" :" +k+":  "+ assoc.getClassName() + "   ROLE : " + assoc.getRoleName());
                 ClassBean tmpBeanRight = null;//(ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 
                 // first check for the filters with values..  the first filter is taken..
                 tmpBeanRight = (ClassBean)getBeanMap().get(assoc.getClassName());
                 
                 // check again in the all possible filters..
                 if (tmpBeanRight == null){
                     tmpBeanRight = (ClassBean)getCurrentClassBeanMap().get(assoc.getClassName());
                 }
                 
                 
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
     }// </editor-fold>
     
     
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
     
     
     
     
     
     //---------------------------- AND / OR groups methods...----------------------------
     
     // <editor-fold defaultstate="collapsed" desc=" AND / OR Methods ">
     public static ArrayList<FilterGroup> getFilterSubGroupList() {
         return filterSubGroups;
     }
     public static void setFilterSubGroupList(ArrayList<FilterGroup> aFilterGroup) {
         filterSubGroups = aFilterGroup;
     }
     public static void addFilterSubGroup(FilterGroup aFilterGroup) {// <editor-fold defaultstate="collapsed">
         // when you add a new sub group... remove the filterPanels and inner groups from subgroup list..
         filterSubGroups.add(aFilterGroup);
         
         // now remove filters so that it may not be added to new groups..
         ArrayList filterList = aFilterGroup.getFilterList();
 //        System.out.println("Adding these filters in group..:"+filterList.size());
         for (int i = 0; i < filterList.size(); i++) {
             getNonGroupFilters().remove(filterList.get(i));
             numGroupableEntities--;
         }
         
         
         // now remove groups so that it may not be added to new groups..
         ArrayList groupList = aFilterGroup.getGroupList();
         for (int i = 0; i < groupList.size(); i++) {
             getFilterSubGroupList().remove(groupList.get(i));
             numGroupableEntities--;
         }
         
         // TODO -SB- uncomment the code below when you implement the nested gruping DCQL generation.
 //        numGroupableEntities++;
     }// </editor-fold>
     
     public static FilterGroup getRootGroup() {
         return rootGroup;
     }
     
     public static void setRootGroup(FilterGroup aRootGroup) {
         rootGroup = aRootGroup;
     }
     
     
     public static ArrayList<FilterRowPanel> getNonGroupFilters() {
         return nonGroupFilters;
     }
     
     public static void setNonGroupFilters(ArrayList<FilterRowPanel> aNonGroupFilters) {
         nonGroupFilters = aNonGroupFilters;
     }
     public static void addNonGroupFilter(FilterRowPanel aNonGroupFilter) {
         nonGroupFilters.add(aNonGroupFilter);
         numGroupableEntities++;
     }
     
     
     public static int getNumGroupableEntities() {
         return numGroupableEntities;
     }
     
     public static void setNumGroupableEntities(int _numGroupableEntities) {
         numGroupableEntities = _numGroupableEntities;
     }
     
     public static boolean hasGroupsDefined() {
         return hasGroupsDefined;
     }
     
     public static void setHasGroupsDefined(boolean aHasGroupsDefined) {
         hasGroupsDefined = aHasGroupsDefined;
     }
     // </editor-fold>
     
     //---------------------------- AND / OR groups methods...----------------------------
     
     
     
     
     
     
     
     
     
     
     
     
     
     //---------------------------- returned Attributes methods...----------------------------
     
     // <editor-fold defaultstate="collapsed" desc=" Returned Attributes Methods ">
     
     public static boolean isReturnedAttributeListAvailable() {
         return returnedAttributeListAvailable;
     }
     
     public static void setReturnedAttributeListAvailable(boolean available) {
         returnedAttributeListAvailable = available;
     }
     
     public static HashMap getClassNameReturnedAttributeMap() {
         return classNameReturnedAttributeMap;
     }
     
     public static void setClassNameReturnedAttributeMap(HashMap aClassNameReturnedAttributeMap) {
         classNameReturnedAttributeMap = aClassNameReturnedAttributeMap;
     }
     
     public static void addToClassNameReturnedAttributeMap(String classFullName, List attributes) {
         boolean alreadyThere = getClassNameReturnedAttributeMap().containsKey(classFullName);
         if (!alreadyThere){
             getClassNameReturnedAttributeMap().put(classFullName, attributes);
         }
     }
     
     public static void addToClassNameReturnedAttributeMap(String classFullName, String attribute) {// <editor-fold defaultstate="collapsed">
         boolean alreadyThere = getClassNameReturnedAttributeMap().containsKey(classFullName);
         if (alreadyThere){
             // add the attribute to the list.. check duplicates..
             List atts = (List)getClassNameReturnedAttributeMap().get(classFullName);
             if (!atts.contains(attribute)){
                 atts.add(attribute);
             }
         } else { // create a new List and add to the Map..
             List atts = new ArrayList();
             atts.add(attribute);
             getClassNameReturnedAttributeMap().put(classFullName, atts);
         }
        numReturnedAttribute++;
     }// </editor-fold>
     
     public static boolean hasReturnedAttributesForClass(String classFullName){
         return getClassNameReturnedAttributeMap().containsKey(classFullName);
     }
     
     public static List getReturnedAttributesForClass(String classFullName){// <editor-fold defaultstate="collapsed">
         List returnList;
         if (hasReturnedAttributesForClass(classFullName)){
             returnList  = (List)getClassNameReturnedAttributeMap().get(classFullName);
         } else{
             returnList = new ArrayList();
         }
         return returnList;
     }// </editor-fold>
     
     
     
     public static int getNumReturnedAttribute() {
         return numReturnedAttribute;
     }
     
     public static void setNumReturnedAttribute(int aNumReturnedAttribute) {
         numReturnedAttribute = aNumReturnedAttribute;
     }
     
     public static ArrayList<String> getClassesUsedInFilters() {// <editor-fold defaultstate="collapsed">
         // calculate every time.. as this is changing
         ArrayList<FilterRowPanel> filters = getFilterList();
         ArrayList<String> classList = new ArrayList<String>();
         
         for (int i = 0; i < filters.size(); i++) {
             classList.add(filters.get(i).getClassBean().getFullyQualifiedName());
         }
         return classList;
     }// </editor-fold>
     
     
     public static HashSet getReturnedAttributeForeignServices() {
         return returnedAttributeForeignServices;
     }
     
     public static void setReturnedAttributeForeignServices(HashSet aReturnedAttributeForeignServices) {
         returnedAttributeForeignServices = aReturnedAttributeForeignServices;
     }
     
     public static void addToReturnedAttributeForeignServices(String foreignServicesName) {
         returnedAttributeForeignServices.add(foreignServicesName);
     }
     
     public static boolean hasReturnedAttributesFromService( String serviceName){
         return returnedAttributeForeignServices.contains(serviceName);
     }
     
     // </editor-fold>
     
     //---------------------------- returned Attributes methods...----------------------------
     
     
     
     
     
     
     
     
     
     
     
     
     //---------------------------- query sharing methods...----------------------------
     public static ArrayList<GraphObject> getAllSimpleGuiXMLObjectList() { // <editor-fold defaultstate="collapsed">
         if (allSimpleGuiXMLObjectList.size() == 0){
             
             
             List<GraphObject> objss = processor.getTargetObjects();
             
             ArrayList tmpList = new ArrayList();
             ArrayList tmpListObj = new ArrayList();
             
             // get unique services and target objects from them..
             for (int i=0;i<objss.size();i++) {
                 String name = objss.get(i).getClassName();
 //                String serviceName = objss.get(i).getServiceName();
                 if (!tmpList.contains(name)){
                     tmpList.add(name);
                     tmpListObj.add(objss.get(i));
                 }
             }
             // add the target objects.
             for (int j = 0; j < tmpListObj.size(); j++) {
                 allSimpleGuiXMLObjectList.add((GraphObject)tmpListObj.get(j));
             }
             
             // get the list of associated GraphObjects for each service..
             for (int i = 0; i < tmpList.size(); i++) {
                 GraphObject tmpObj = (GraphObject)tmpListObj.get(i);
 //                boolean thisObjIsTarget = tmpObj.getClassName().equals(getTargetGraphObject().getClassName());
                 List<GraphObject> objs = processor.getAssociatedObjects(tmpObj.getClassName(),tmpObj.getServiceName());
                 for (int j = 0; j < objs.size(); j++) {
                     GraphObject innerObj = objs.get(j);
 //                    if (!thisObjIsTarget){
 //                        tmpObj.setLocalStatus(false);
 //                        innerObj.setLocalStatus(false);
 //                    }
                     if (innerObj.isDisplayable()){
                         allSimpleGuiXMLObjectList.add(innerObj);
                     }
                 }
             }
             
             // get the ClassBeans for each GraphObject and attach them to GraphObjects from the list..
             GraphObject obj;
             GraphAssociation assoc;
             for (int i = 0; i < allSimpleGuiXMLObjectList.size(); i++) {
                 obj = allSimpleGuiXMLObjectList.get(i);
                 boolean display = obj.isDisplayable();
                 if (display){
                     String[] displaybleAttributes = obj.getDisplaybleAttributes().split(",");
                     ClassBean cBean =null;
                     try {
                         cBean = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(obj.getClassName()).clone();
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     
                     cBean.filterAttributes(displaybleAttributes);
                     obj.setClassBean(cBean);
                     
                 }else { // show only visible classes here as well..
 //                ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByFullyQualifiedName(obj.getClassName()).clone();
 //                cBean.setAssociationRoleNameMap(new HashMap(20));
 //                obj.setClassBean(cBean);
                 }
 //                System.out.println("---- xml class :"+obj.getClassName()+"  local = "+obj.isLocal()+"  Selectable = "+obj.isSelectable()+"  displayble= "+obj.isDisplayable());
                 
             }
             
         } // by now the GraphObjects in this list is set..
         
         return allSimpleGuiXMLObjectList;
     }// </editor-fold>
     
     public static void setAllSimpleGuiXMLObjectList(ArrayList<GraphObject> aAllSimpleGuiXMLObjectList) {
         allSimpleGuiXMLObjectList = aAllSimpleGuiXMLObjectList;
     }
     
     //---------------------------- query sharing methods...----------------------------
     
     
     
     
     
     
     
     
     
     
     
 }
