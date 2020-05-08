 package edu.duke.cabig.catrip.gui.common;
 
 import java.awt.Image;
 import java.util.*;
 
 
 /**
  * ClassBean represents a Class as defined in UMLClass element of Domain Model Metadata.
  *
  * @author Sanjeev Agarwal
  */
 public class ClassBean{
     
     private String id="";
     private String version="";
     private ArrayList<AttributeBean> attributes = new ArrayList(50); 
     private HashMap attributesMap = new HashMap(50);
     private ArrayList<String> associatedClasses= new ArrayList(20); // from the domain model..
     private String packageName="";
     private String className="";
     private String description="";
     
     // set the icon based on the service name.
     private Image icon;// = "edu/duke/cabig/catrip/gui/dnd/resources/caCore.png";
     private String serviceName="";
     private String serviceUrl="";
     private String domainModelId="";
     private String CDEName="";
     
     
     
     // ----- DCQL helper attributes ---- //
     
     private boolean needImpl=false;
     
     private int numNotNullAttributes = 0;
     private ArrayList<AttributeBean> notNullAttributes = new ArrayList();
     
     private boolean hasAssociations = false;
     private ArrayList<ClassBean> associations = new ArrayList(20);
     private HashMap associationRoleNameMap = new HashMap(20);
     
     private ArrayList<ForeignAssociationBean> foreignAssociations = new ArrayList(10);
     private boolean hasForeignAssociations = false;
     // ----- DCQL helper attributes ---- //
     
     
     
     // ---- UML Generalization Collection attributes ----//
     private String superClassRefId="";
     private String superClassName="";
     private ArrayList<String> superClassAssociatedClassList = new ArrayList(20);
     private ArrayList<String> subClassIds = new ArrayList(100);
     // ---- UML Generalization Collection attributes ----//
     
     
     
     // ------------- AND / OR group attributes --------------- //
     private ArrayList<ClassBeanGroup> groups = new ArrayList(20);
     private HashMap groupIdMap = new HashMap();
     
     // ------------- AND / OR group attributes --------------- //
     
     
     
     
     
     /** Creates a new instance of ClassBean */
     public ClassBean() {
     }
     
     public String getId() {
         return id;
     }
     
     public void setId(String id) {
         this.id = id;
     }
     
     public String getVersion() {
         return version;
     }
     
     public void setVersion(String version) {
         this.version = version;
     }
     
     public ArrayList<AttributeBean> getAttributes() {
         return attributes;
     }
     
     public void setAttributes(ArrayList<AttributeBean> attributes) {
         this.attributes = attributes;
     }
     
     /** All associated classes as defined in the domain model */
     public ArrayList<String> getAssociatedClasses() {
         return associatedClasses;
     }
     
     /** All associated classes as defined in the domain model */
     public void setAssociatedClasses(ArrayList<String> associatedClasses) {
         this.associatedClasses = associatedClasses;
     }
     
     public String getPackageName() {
         return packageName;
     }
     
     public void setPackageName(String packageName) {
         this.packageName = packageName;
     }
     
     public String getClassName() {
         return className;
     }
     
     public void setClassName(String className) {
         this.className = className;
     }
     
     public Image getIcon() {
         return icon;
     }
     
     public void setIcon(Image icon) {
         this.icon = icon;
     }
     
     public String getServiceName() {
         return serviceName;
     }
     
     public void setServiceName(String serviceName) {
         this.serviceName = serviceName;
     }
     
     public String getCDEName() {
         return CDEName;
     }
     
     public void setCDEName(String CDEName) {
         this.CDEName = CDEName;
     }
     
     public String getDomainModelId() {
         return domainModelId;
     }
     
     public void setDomainModelId(String domainModelId) {
         this.domainModelId = domainModelId;
     }
     
     /** Add to the attribute list of the class. */
     public void addAttribute(AttributeBean ab){
         getAttributes().add(ab);
         addToAttributesMap(ab);
     }
     
     /** Add to the attribute list of the class. */
     public void addUniqueAttribute(AttributeBean ab){
         if (!getAttributesMap().containsKey(ab.getAttributeName())){
             addAttribute(ab);
         }
     }
     
     public HashMap getAttributesMap() {
         return attributesMap;
     }
 
     public void addToAttributesMap(AttributeBean ab){
         this.attributesMap.put(ab.getAttributeName(), ab);
     }
     
     
     public String getDescription() {
         return description;
     }
     
     public void setDescription(String description) {
         this.description = description;
     }
     
     /** add to the list of associated classes. Associations are defined in domain model. */
     public void addAssociatedClass(String refId){
         if (!associatedClasses.contains(refId)){
             associatedClasses.add(refId);
         }
     }
     
     /** this is to display the tree node name as class name only. */
     public String toString() {
         return getClassName();
     }
     
     /** used to clone the class object when it is dropped on the graph.
      * The cloned object contains all the property of the original class object except the values of the Attribute.
      */
     public ClassBean clone(){
         
         ClassBean cBean = new ClassBean();
         cBean.setAssociatedClasses(getAssociatedClasses()); // associated class will be same for all clones.
         
         // clone attributes also..
         // this is necessary as different graph nodes of the same class may have different attribute value
         ArrayList<AttributeBean> attributeClones = new ArrayList(50);
         ArrayList attributes = getAttributes();
         for (int k = 0; k < attributes.size(); k++) {
             AttributeBean aBean = ((AttributeBean)attributes.get(k)).clone();
             attributeClones.add(aBean);
         }
         cBean.setAttributes(attributeClones);
         
         cBean.setCDEName(getCDEName());
         cBean.setClassName(getClassName());
         cBean.setDescription(getDescription());
         cBean.setDomainModelId(getDomainModelId());
         cBean.setIcon(getIcon());
         cBean.setId(getId());
         cBean.setPackageName(getPackageName());
         cBean.setServiceName(getServiceName());
         cBean.setServiceUrl(getServiceUrl());
         cBean.setVersion(getVersion());
        cBean.removeAllUniqueAssociations();
        //cBean.setAssociationRoleNameMap(this.getAssociationRoleNameMap()); It was removed to solve bug: 4330. It needs to be enabled to fix bug: 4667
         cBean.needImpl(needImpl());
         
         return cBean;
     }
     
    
    public ClassBean clone2(){
        ClassBean cBean = clone();
        cBean.setAssociationRoleNameMap(this.getAssociationRoleNameMap());  // bug Id : 4667.
        return cBean;
    } 
    
    
     /** The service URL of the grid service. Required in DCQL generation from the class instance. */
     public String getServiceUrl() {
         return serviceUrl;
     }
     
     public void setServiceUrl(String serviceUrl) {
         this.serviceUrl = serviceUrl;
     }
     
     public String getFullyQualifiedName() {
 //        return getPackageName()+"."+getClassName();
         // TODO - change this in future.. for the demo only..
         if (needImpl()){
             return getPackageName()+".impl."+getClassName()+"Impl";
         } else {
             return getPackageName()+"."+getClassName();
         }
     }
     
     /** The map of the associated classes and corresponding roleNames.
      * Built from the Domain model element "exposedUMLAssociationCollection"
      */
     public HashMap getAssociationRoleNameMap() {
         return this.associationRoleNameMap;
     }
     
     public void setAssociationRoleNameMap(HashMap associationRoleNameMap) {
         this.associationRoleNameMap = associationRoleNameMap;
     }
     
     /** get the role name for a particular associated class. */
     public String getAssociationRoleName(String classId) {
         String roleName = (String)this.getAssociationRoleNameMap().get(classId);
         return roleName;
     }
     
     /** set the role name for a particular associated class and add to the map. */
     public void addAssociationRoleName(String classId, String associationRoleName) {
         if (!this.getAssociationRoleNameMap().containsKey(classId)){
             this.getAssociationRoleNameMap().put(classId, associationRoleName) ;
         }
     }
     
     
     
     
     
     
     
     
     
     
     // --------- helper methods for generating DCQL ------------------ //
     
     /** return true of the class object contains attributes without null values  */
     public boolean hasNotNullAttributes(){
         boolean result = false;
         ArrayList attributes = getAttributes();
         for (int i = 0; i < attributes.size(); i++) {
             AttributeBean aBean = (AttributeBean)attributes.get(i);
             if (!aBean.isNull()){
                 numNotNullAttributes++;
                 result = true;
             }
         }
         return result;
     }
     
     /** Get the list of attributes with values. */
     public ArrayList<AttributeBean> getNonNullAttributes(){
         notNullAttributes = new ArrayList(numNotNullAttributes);
         ArrayList attributes = getAttributes();
         for (int i = 0; i < attributes.size(); i++) {
             AttributeBean aBean = (AttributeBean)attributes.get(i);
             if (!aBean.isNull()){
                 notNullAttributes.add(aBean);
             }
             
         }
         return notNullAttributes;
     }
     
     // ----------------------------------
     
     /** Check for any association created by linking two nodes on the graph via class-link. */
     public boolean hasAssociations(){
         return hasAssociations;
     }
     public void setHasAssociations(boolean has){
         this.hasAssociations = has;
     }
     /** the list that contains the list of associations created on the graph. */
     public void addAssociation(ClassBean ass){
         associations.add(ass);
 //        numAssociations++;
     }
     
     public ArrayList getAssociations(){
         return associations;
     }
     
     // -----------------------------------
     /** Check for any foreign association created by linking two nodes on the graph via CDE link  */
     public boolean hasForeignAssociations(){
         return hasForeignAssociations;
     }
     public void setHasForeignAssociations(boolean has){
         this.hasForeignAssociations = has;
     }
     /** add to the list that contains the list of foreign associations created on the graph. */
     public void addForeignAssociation(ForeignAssociationBean fass){
         foreignAssociations.add(fass);
     }
     
     /** the list that contains the list of foreign associations created on the graph. */
     public ArrayList getForeignAssociations(){
         return foreignAssociations;
     }
     // --------- helper methods for generating DCQL ------------------ //
     
     
     
     /** method created for the "impl" problem in the caCORE SDK generated code. */
     public void needImpl(boolean need){
         this.needImpl = need;
     }
     /** method created for the "impl" problem in the caCORE SDK generated code. */
     public boolean needImpl(){
         return needImpl;
     }
     
     
     
     
     
     
     // --------------- methods for simple GUI.. ------------------ //
     
     /** Filter the attributes based on the  displaybleAttributes element of the xml..*/
     public void filterAttributes(String[] displaybleAttributes){
         ArrayList attributes = getAttributes();
         ArrayList displaybleAttributesList = new ArrayList(15);
         try{
             for (int j = 0; j < displaybleAttributes.length; j++) {
                 String attribute = displaybleAttributes[j];
                 for (int i = 0; i < attributes.size(); i++) {
                     AttributeBean aBean = (AttributeBean)attributes.get(i);
                     if (aBean.getAttributeName().equalsIgnoreCase(attribute)){
                         displaybleAttributesList.add(aBean);
                     }
                 }
                 
             }
             
         } catch (Exception e){
             e.printStackTrace();
         }
         this.setAttributes(displaybleAttributesList);
         
     }
     
     /** to add the associations only once.. used in Simple gui  */
     public void addUniqueForeignAssociation(ForeignAssociationBean fass){
         boolean alreadyAdded = foreignAssociations.contains(fass); // this thing checks the equals() method of this bean..
         if (!alreadyAdded){   // create a Map and than check in that....
             foreignAssociations.add(fass);
         }
     }
     
     /** to add the associations only once.. used in Simple gui  */
     public void addUniqueAssociation(ClassBean ass){
         // check if associationRoleNameMap already contains the class.. do not add duplicates...
         boolean alreadyAdded = (associationRoleNameMap.containsKey(ass.getId()) );
         if (!alreadyAdded){
             associations.add(ass);
         }
     }
     
     public void removeAllUniqueAssociations(){
         hasAssociations = false;
         associations = new ArrayList(20);
//        associationRoleNameMap = new HashMap(20);
         foreignAssociations = new ArrayList(10);
         hasForeignAssociations = false;
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     public void print(){
         ArrayList attributes = getAttributes();
         for (int i = 0; i < attributes.size(); i++) {
             AttributeBean aBean = (AttributeBean)attributes.get(i);
             System.out.println("Class CDE:"+getCDEName()+":Attribute name:"+aBean.getAttributeName() + ": Attribute CDE name:" + aBean.getCDEName()+":");
         }
     }
     
     public void printAttributes(){
         ArrayList attributes = getNonNullAttributes();
         for (int i = 0; i < attributes.size(); i++) {
             AttributeBean aBean = (AttributeBean)attributes.get(i);
             System.out.println("Class Name:"+getFullyQualifiedName()+": Attribute CDE name:" + aBean.getCDEName()+":Attribute value:"+aBean.getAttributeValue() );
         }
     }
     
     
     public void printAssociations(){
         
         if (hasAssociations()){
             ArrayList list = getAssociations();
             for (int i = 0; i < list.size(); i++) {
                 ClassBean cBean = (ClassBean)list.get(i);
                 System.out.println(getClassName() + " --"+getAssociationRoleName(cBean.getId())+"-> " +cBean.getClassName() );
                 cBean.printAssociations();
             }
         }
         
         if (hasForeignAssociations()){
             ArrayList list = getForeignAssociations();
             for (int i = 0; i < list.size(); i++) {
                 ForeignAssociationBean fBean = (ForeignAssociationBean)list.get(i);
                 System.out.println(getClassName() +"-:-"+ fBean.getLeftObj().getClassName()+":"+fBean.getLeftProperty()+" ---> " +fBean.getRighObj().getClassName()+":"+fBean.getRightProperty());
                 fBean.getRighObj().printAssociations();
             }
         }
         
     }
     
     
     
     
     
     
     /** Methods used in generalization section of the domain model xml. */
     
     public String getSuperClassRefId() {
         return superClassRefId;
     }
     
     public void setSuperClassRefId(String superClassRefId) {
         this.superClassRefId = superClassRefId;
     }
     
     public ArrayList<String> getSuperClassAssociatedClassList() {
         return superClassAssociatedClassList;
     }
     
     public void setSuperClassAssociatedClassList(ArrayList<String> superClassAssociatedClassList) {
         this.superClassAssociatedClassList = superClassAssociatedClassList;
     }
     
     public void addSuperClassAssociatedClassList(String superClassAssociatedClass) {
         
         boolean alreadyAdded = superClassAssociatedClassList.contains(superClassAssociatedClass);
         if (!alreadyAdded){
             this.superClassAssociatedClassList.add(superClassAssociatedClass);
         }
     }
     
     public String getSuperClassName() {
         return superClassName;
     }
     
     public void setSuperClassName(String superClassName) {
         this.superClassName = superClassName;
     }
     
     public ArrayList<String> getSubClassIds() {
         return subClassIds;
     }
     public void setSubClassIds(ArrayList<String> subClassIds) {
         this.subClassIds = subClassIds;
     }
     public void addSubClassId(String subClassId) {
         boolean alreadyAdded = getSubClassIds().contains(subClassId);
         if (!alreadyAdded){
             getSubClassIds().add( subClassId );
         }
         
     }
     
     
     
     
     
     //------------------- methods for AND / OR grouping ----------------------------
     
     public String getUniqueId(){
         // this returns the instance ref for the object which is unique for that jvm instance..
         return getClass().getName() + "@" + Integer.toHexString(hashCode());
     }
     
     public boolean equals(Object obj){
         boolean equal = false;
         if (obj instanceof ClassBean){
             ClassBean cBean = (ClassBean) obj;
             String thisRef = getUniqueId();
             String objRef = cBean.getUniqueId();
             equal = thisRef.equals(objRef); // this compares the two references.. and will be true only if that is same object..
         }
         return equal;
     }
     
 
     
     
     //------------------- methods for AND / OR grouping ----------------------------
     
     public ArrayList<ClassBeanGroup> getGroups() {
         return groups;
     }
     
     public void setGroups(ArrayList<ClassBeanGroup> groups) {
         this.groups = groups;
     }
     
     public boolean hasGroups() {
         return (groups.size()>0)?true:false;
     }
     
     public void addGroup(ClassBeanGroup group) {
         if (!hasGroup(group)){
             getGroups().add(group);
             groupIdMap.put(group.getGroupId(), group);
         }
     }
     
     public boolean hasGroup(ClassBeanGroup group) {
         return groupIdMap.containsKey(group.getGroupId());
     }
     public ClassBeanGroup getGroupById(String id) {
         return (ClassBeanGroup)groupIdMap.get(id);
     }
     
     
     
     // merge tow ClassBean instances..  child will be merged in parent.
     public static void mergeClassBeans(ClassBean parent, ClassBean child){
         // child has no not-null attributes and no associations..  don't do anything..
         boolean hasAttributes = child.hasNotNullAttributes();
         boolean hasAssociations = child.hasAssociations();
         boolean hasGroups = child.hasGroups();
         
         if (hasGroups){
             ArrayList groups = child.getGroups();
             int groupNums = groups.size();
             
             for (int i = 0; i < groupNums; i++) {
                 ClassBeanGroup group = (ClassBeanGroup)groups.get(i);
                 boolean parentHasSameGroup = parent.hasGroup(group);
                 if (parentHasSameGroup){
                     // set associations and attributes in that.. right now just set attributes..
                     // use group merge stuff here... create a method in group class.
                     ArrayList attList = group.getAttributeList();
                     for (int j = 0; j < attList.size(); j++) {
                         parent.getGroupById(group.getGroupId()).add((AttributeBean)attList.get(j));
                     }
                     
                 }else {
                     parent.addGroup(group);
                 }
                 
             }
             
         }
         
         
         
         if (!hasAttributes && !hasAssociations){
             // do nothing..
         }
         
         
         // child has  not-null attributes but no associations..
         // TODO - have to watch out for duplicate Attributes here...
         // you have have attributes with same or different values.. so you may have to group them as well...
         if (hasAttributes && !hasAssociations){
             // set the attributes in parent..
             ArrayList attributes = child.getAttributes(); // list of attributes..
             for (int i = 0; i < attributes.size(); i++) {
                 parent.addAttribute((AttributeBean)attributes.get(i));
             }
         }
         
         
         
         // child has no not-null attributes and only associations..
         else if (!hasAttributes && hasAssociations){
             // set the same associations in the parent also..
             ArrayList associations = child.getAssociations(); // list of associations..
             for (int i = 0; i < associations.size(); i++) {
                 ClassBean cBeanTmp = (ClassBean)associations.get(i);
                 parent.addUniqueAssociation(cBeanTmp);
                 parent.addAssociationRoleName(cBeanTmp.getId(), child.getAssociationRoleName(cBeanTmp.getId()));
             }
             
             // check for the foreign associations as well...
             ArrayList foreignAssociations = child.getForeignAssociations(); // list of associations..
             for (int i = 0; i < foreignAssociations.size(); i++) {
                 ForeignAssociationBean faBean = (ForeignAssociationBean)foreignAssociations.get(i);
                 parent.addUniqueForeignAssociation(faBean);
             }
             
         }
         
         
         
         
         // child has not-null attributes and associations..  set both in the parent also..
         // check for the foreign associations as well...
         else if (hasAttributes && hasAssociations){
             // set the attributes in parent..
             ArrayList attributes = child.getAttributes(); // list of attributes..
             for (int i = 0; i < attributes.size(); i++) {
                 parent.addAttribute((AttributeBean)attributes.get(i));
             }
             
             // set the same associations in the parent also..
             ArrayList associations = child.getAssociations(); // list of associations..
             for (int i = 0; i < associations.size(); i++) {
                 ClassBean cBeanTmp = (ClassBean)associations.get(i);
                 parent.addUniqueAssociation(cBeanTmp);
                 parent.addAssociationRoleName(cBeanTmp.getId(), child.getAssociationRoleName(cBeanTmp.getId()));
             }
             
             // check for the foreign associations as well...
             ArrayList foreignAssociations = child.getForeignAssociations(); // list of associations..
             for (int i = 0; i < foreignAssociations.size(); i++) {
                 ForeignAssociationBean faBean = (ForeignAssociationBean)foreignAssociations.get(i);
                 parent.addUniqueForeignAssociation(faBean);
             }
         }
         
         
     }
     
     
     
     //------------------- methods for AND / OR grouping ----------------------------
     
     public static HashMap cloneClassBeanMap(HashMap map){
         HashMap clonedMap = new HashMap(map.size());
         Object[] keys = (Object[])map.keySet().toArray();
         for (int i = 0; i < keys.length; i++) {
             ClassBean cBean = (ClassBean)map.get(keys[i]);
             clonedMap.put(keys[i], cBean.clone());
         }
         return clonedMap;
     }
 
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 }
