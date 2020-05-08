 
 package edu.duke.cabig.catrip.gui.discovery;
 
 import edu.duke.cabig.catrip.gui.common.AttributeBean;
 import edu.duke.cabig.catrip.gui.common.ClassBean;
 import edu.duke.cabig.catrip.gui.common.ServiceMetaDataBean;
 import gov.nih.nci.cagrid.metadata.common.SemanticMetadata;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.UMLClass;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLGeneralization;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * This class acts as Registry for the Domain Model Metadata.
  *
  * @author Sanjeev Agarwal
  */
 public class DomainModelMetaDataRegistry {
     
     /** This Map contains the ClassBean instances vs. class RefIds as defined in the Domain Model extract */
     private static HashMap classMap = new HashMap(200);
     
     /** This Map contains the ClassBean instances vs. fully classified class names as defined in the Domain Model extract */
     private static HashMap classNameClassMap= new HashMap(200);
     
     /** Map of the class List vs service/model name. */
     private static HashMap domainModelMap = new HashMap(20);
     
     /** Map of umlGeneralizationCollection refIds.. key = subClass refID, value = superClass refID */
     private static HashMap classGeneralizationMap = new HashMap(200);
     
     
     /** Creates a new instance of DomainModelMetaDataRegistry */
     public DomainModelMetaDataRegistry() {
     }
     
     public static ClassBean lookupClassByRefId(String refId) {
         return (ClassBean) classMap.get(refId);
     }
     
     public static ClassBean lookupClassByFullyQualifiedName(String className) {
         return (ClassBean) classNameClassMap.get(className);
     }
     
     public static void bindClassByRefId(ClassBean classBean){
         classMap.put(classBean.getId(), classBean);
         classNameClassMap.put(classBean.getFullyQualifiedName(), classBean);
     }
     
     public static ArrayList<ClassBean> lookupClassListByDomainModelName(String id) {
         return (ArrayList) domainModelMap.get(id);
     }
     
     public static void bindClassListByDomainModelName(String domainModelName, ArrayList<ClassBean> classList){
         domainModelMap.put(domainModelName, classList);
     }
     
     public static ArrayList<ClassBean> lookupClassListByServiceName(String id) {
         return (ArrayList) domainModelMap.get(id);
     }
     
     /** Bind class List with the serviceName. The list is later retrieved to draw the tree.*/
     public static void bindClassListByServiceName(String domainModelName, ArrayList<ClassBean> classList){
         domainModelMap.put(domainModelName, classList);
     }
     
     /** Populate the data from Domain Model extract into ClassBean instances and bind them into registry.  */
     
     public static void populateDomainModelMetaData(DomainModel domainModel, ServiceMetaDataBean sBean){
 //        System.out.println("XXXXX---   Service name :"+sBean.getServiceName());
         ArrayList<ClassBean> serviceClassList = new ArrayList(100); // The collection of classes that belongs to only this DomainModel.
         
         UMLClass[] umlClasses = domainModel.getExposedUMLClassCollection().getUMLClass();
         for (int i = 0; i < umlClasses.length; i++) {
             ClassBean classBean = new ClassBean();
             classBean.setClassName(umlClasses[i].getClassName() );
             classBean.setPackageName(umlClasses[i].getPackageName() );
             classBean.setDescription(umlClasses[i].getDescription());
             classBean.setId(umlClasses[i].getId());
             classBean.setVersion(umlClasses[i].getProjectVersion());
             
             classBean.setServiceName(sBean.getServiceName());
             classBean.setServiceUrl(sBean.getServiceUrl());
             classBean.setIcon(sBean.getIcon());
             classBean.needImpl(sBean.needImpl());
             
             SemanticMetadata[] semanticMetadata = umlClasses[i].getSemanticMetadata(); //umlClasses[i].getSemanticMetadataCollection().getSemanticMetadata();
             String classCDEName = "";
             if ((semanticMetadata != null)  &&  (semanticMetadata.length >= 1)){
                 for (int k = 0; k < semanticMetadata.length; k++) {
                     classCDEName = classCDEName + " "+ semanticMetadata[k].getConceptName() ;
                 }
             }
             classBean.setCDEName(classCDEName);
             
             
             // now set the attributes..
             ArrayList<AttributeBean> attributeList = new ArrayList(100);
             ArrayList<String> attributeListUnique = new ArrayList(100); // just to check for the duplictate entries..
             
             UMLAttribute[] umlAttributes = umlClasses[i].getUmlAttributeCollection().getUMLAttribute();
             if ((umlAttributes != null)  &&  (umlAttributes.length > 1)){
                 for (int j = 0; j < umlAttributes.length; j++) {
                     AttributeBean attributeBean = new AttributeBean();
                     attributeBean.setAttributeName(umlAttributes[j].getName());
                     /**   get the CDE name or Display Name by concatenation of the concept names in reverse order. */
                     String cdeName = "";
                     SemanticMetadata[]  attributeMetadata =  umlAttributes[j].getSemanticMetadata(); //umlAttributes[j].getSemanticMetadataCollection().getSemanticMetadata();
                     
                     for (int k = 0; k < attributeMetadata.length; k++) {
                         cdeName = cdeName + " "+ attributeMetadata[k].getConceptName() ;
                     }
                     attributeBean.setDisplayName(cdeName);
                     attributeBean.setCDEName(cdeName);
 //                    System.out.println( classBean.getClassName()+ ":" + cdeName );
                     // check for duplicate entries..
                     if (!attributeListUnique.contains(attributeBean.getAttributeName())){
                         attributeList.add(attributeBean);
                         attributeListUnique.add(attributeBean.getAttributeName());
                     }
                 }
             }
             classBean.setAttributes(attributeList);
             
             // now bind the class Obj with the Registry.
             bindClassByRefId(classBean);
             serviceClassList.add(classBean);
         }
         // bind the class list to the service name.
         bindClassListByServiceName(sBean.getServiceName(), serviceClassList); //
         
         // now set the umlAssociations with the classes in registry :
         UMLAssociation[] umlAssociations = domainModel.getExposedUMLAssociationCollection().getUMLAssociation();
         
         for (int i = 0; i < umlAssociations.length; i++) {
             boolean biDirectional = umlAssociations[i].isBidirectional();
             String sourceRef = umlAssociations[i].getSourceUMLAssociationEdge().getUMLAssociationEdge().getUMLClassReference().getRefid();
             String targetRef = umlAssociations[i].getTargetUMLAssociationEdge().getUMLAssociationEdge().getUMLClassReference().getRefid();
             String targetRole = umlAssociations[i].getTargetUMLAssociationEdge().getUMLAssociationEdge().getRoleName();
             
             ClassBean sourceClassBean = null;
             ClassBean targetClassBean = null;
             try {
                 sourceClassBean = lookupClassByRefId(sourceRef);
             } catch (Exception ee) {
                 ee.printStackTrace();
             }
             try {
                 targetClassBean = lookupClassByRefId(targetRef);
             } catch (Exception ee) {
                 ee.printStackTrace();
             }
             if (sourceClassBean != null){
                 sourceClassBean.addAssociatedClass(targetRef);
                 sourceClassBean.addAssociationRoleName(targetRef, targetRole);
             } // put the cross references for the source into target class...
             if ( (targetClassBean != null ) && biDirectional ) {
                 targetClassBean.addAssociatedClass(sourceRef);
                 String sourceRole = umlAssociations[i].getSourceUMLAssociationEdge().getUMLAssociationEdge().getRoleName();
                 targetClassBean.addAssociationRoleName(sourceRef, sourceRole);
             }
         }
         
         
         
         // load association data from the umlGeneralizationCollection also..
         UMLGeneralization[] umlUMLGeneralization = domainModel.getUmlGeneralizationCollection().getUMLGeneralization();
         for (int i = 0; i < umlUMLGeneralization.length; i++) {
             //  get  superRefId subRefId
             String superRefId = umlUMLGeneralization[i].getSuperClassReference().getRefid();
             String subRefId = umlUMLGeneralization[i].getSubClassReference().getRefid();
             
             // add to the umlUMLGeneralization map.. used for recursive action..
             getClassGeneralizationMap().put(subRefId, superRefId);
 //            setSuperClassAssociations(subRefId, superRefId, domainModel);
         }
         
         // now as the ClassGeneralizationMap is ready.. set the associations from super class to sub class recursively..
         HashMap subSuperclassMap = getClassGeneralizationMap();
         Object[] subrefs = subSuperclassMap.keySet().toArray();
         for (int i = 0; i < subrefs.length; i++) {
             String subRefId = (String)subrefs[i];
             String superRefId = (String)subSuperclassMap.get(subRefId); 
             setSuperClassAssociations(subRefId, superRefId, domainModel);   
         }
         
         
         System.out.println("Successful loading of domainModel for Project: "+domainModel.getProjectLongName());
     }
     
     
     
     
     public static HashMap getClassGeneralizationMap() {
         return classGeneralizationMap;
     }
     
     public static void setClassGeneralizationMap(HashMap aClassGeneralizationMap) {
         classGeneralizationMap = aClassGeneralizationMap;
     }
     
     
     public static void setSuperClassAssociations(String subRefId, String superRefId, DomainModel domainModel) {
 
         if (getClassGeneralizationMap().get(superRefId) != null){
             // it's super Class also there..
             String superSuperClassrefId = (String) getClassGeneralizationMap().get(superRefId);
             setSuperClassAssociations(superRefId, superSuperClassrefId, domainModel);
         }
         
         
         // locate the subclass and super class..
         ClassBean subClass = null;
         ClassBean superClass = null;
         
         try {
             subClass = lookupClassByRefId(subRefId);
             superClass = lookupClassByRefId(superRefId);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
 //            String superName = superClass.getClassName();
 //            String subName = subClass.getClassName();
 //            System.out.println("XXXXX   Class:  "+subName+"  --extends->   "+superName);
         
         // set the super Class RefId to the Sub Class..
        subClass.setSuperClassRefId(superRefId);
         subClass.setSuperClassName(superClass.getClassName());
         
         // attach all the association of super class to sub class.. one by one..
         ArrayList<String>  assClassList = superClass.getAssociatedClasses();
         for (int k = 0; k < assClassList.size(); k++) {
             String refID = assClassList.get(k);
             String roleName = superClass.getAssociationRoleName(refID);
             subClass.addAssociatedClass(refID);
             subClass.addAssociationRoleName(refID, roleName);
 //                System.out.println("XXXXX   Class:  "+subName+"  --extends->   "+superName +"  : ClassID:"+refID+ ": RoleName :"+roleName);
             // add it to a seperate list also..
             subClass.addSuperClassAssociatedClassList(refID);
         }
     }
     
     
     
     
     
     
     
 }
