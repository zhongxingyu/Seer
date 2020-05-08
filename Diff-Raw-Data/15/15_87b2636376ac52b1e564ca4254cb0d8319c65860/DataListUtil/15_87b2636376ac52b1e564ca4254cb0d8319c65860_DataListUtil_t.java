 package edu.wustl.cab2b.server.datalist;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.util.DynamicExtensionUtility;
 
 public class DataListUtil {
     private static EntityGroupInterface dataListEntityGroup = null;
 
     static final String VIRTUAL_ATTRIBUTE_IND = "virtual";
 
     static final String URL_ATTRIBUTE_NAME = "virtual_url_attr";
 
     static final String ID_ATTRIBUTE_NAME = "virtual_id_attr";
 
     public static List<Map<AbstractAttributeInterface, Object>> getAssociatedRecordsList(
                                                                                          Map<AbstractAttributeInterface, Object> map,
                                                                                          AssociationInterface association) {
         Object existingRecords = map.get(association);
         if (existingRecords == null) {
             existingRecords = new ArrayList<Map<AbstractAttributeInterface, Object>>();
             map.put(association, existingRecords);
         }
         List<Map<AbstractAttributeInterface, Object>> existingRecordsList = (List<Map<AbstractAttributeInterface, Object>>) existingRecords;
         return existingRecordsList;
     }
 
     public static EntityGroupInterface getDatalistEntityGroup() {
         if (dataListEntityGroup == null) {
             dataListEntityGroup = DynamicExtensionUtility.getEntityGroupByName(edu.wustl.cab2b.common.util.Constants.DATALIST_ENTITY_GROUP_NAME);
         }
         return dataListEntityGroup;
     }
 
     public static AssociationInterface createAssociation(EntityInterface srcEntity, EntityInterface targetEntity) {
         return DynamicExtensionUtility.createNewOneToManyAsso(srcEntity, targetEntity);
     }
 
     public static AttributeInterface getVirtualIdAttribute(EntityInterface entity) {
         return getAttributeByName(entity, ID_ATTRIBUTE_NAME);
     }
 
     public static AttributeInterface getVirtualUrlAttribute(EntityInterface entity) {
         return getAttributeByName(entity, URL_ATTRIBUTE_NAME);
     }
 
     public static boolean isVirtualAttribute(AttributeInterface attribute) {
         return Utility.getTaggedValue(attribute.getTaggedValueCollection(), VIRTUAL_ATTRIBUTE_IND) != null;
     }
 
     public static AttributeInterface getAttributeByName(EntityInterface entity, String attributeName) {
         return getAttributeByName(entity.getAttributeCollection(), attributeName);
     }
 
     public static AttributeInterface getAttributeByName(Collection<AttributeInterface> attributes,
                                                         String attributeName) {
         for (AttributeInterface attribute : attributes) {
             if (attribute.getName().equals(attributeName)) {
                 return attribute;
             }
         }
         return null;
     }
 
     // virtual attribute is one present in entity, but not in the R's map.
     public static void markVirtual(AttributeInterface attribute) {
         DynamicExtensionUtility.addTaggedValue(attribute, DataListUtil.VIRTUAL_ATTRIBUTE_IND, "");
     }
 
     /**
      * Creates and returns a new attribute given old attribute.
      * 
      * @param oldAttribute
      * @return new attriute with same old attribute name.
      */
     public static AttributeInterface createNewAttribute(AttributeInterface oldAttribute) {
         return DynamicExtensionUtility.getAttributeCopy(oldAttribute);
     }
 
    public static void copyNonVirtualAttributes(EntityInterface newEntity, EntityInterface oldEntity) {
         Collection<AttributeInterface> oldAttribs = oldEntity.getAttributeCollection();
         for (AttributeInterface oldAttrib : oldAttribs) {
            if (isVirtualAttribute(oldAttrib)) {
                continue;
            }
             AttributeInterface newAttrib = DataListUtil.createNewAttribute(oldAttrib);
             newEntity.addAttribute(newAttrib);
         }
     }
 }
