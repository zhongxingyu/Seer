 package edu.wustl.common.querysuite.metadata;
 
 import static edu.wustl.common.querysuite.metadata.Constants.CONNECTOR;
 import static edu.wustl.common.querysuite.metadata.Constants.TYPE_CATEGORY;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 
 public class Utility {
     // TODO COPIED from edu.wustl.cab2b.common.util.Utility
     /**
      * Generates unique string identifier for given association. It is generated
      * by concatenating
      * 
      * sourceEntityName +{@link Constants#CONNECTOR} + sourceRoleName +{@link Constants#CONNECTOR} +
      * targetRoleName +{@link Constants#CONNECTOR} + TargetEntityName
      * 
      * @param association Association
      * @return Unique string to represent given association
      */
     public static String generateUniqueId(AssociationInterface association) {
         return concatStrings(association.getEntity().getName(), association.getSourceRole().getName(), association
                 .getTargetRole().getName(), association.getTargetEntity().getName());
     }
 
     /**
      * @param s1 String
      * @param s2 String
      * @param s3 String
      * @param s4 String
      * @return Concatenated string made after connecting s1, s2, s3, s4 by
      *         {@link Constants#CONNECTOR}
      */
     public static String concatStrings(String s1, String s2, String s3, String s4) {
         StringBuffer buff = new StringBuffer();
         buff.append(s1);
         buff.append(CONNECTOR);
         buff.append(s2);
         buff.append(CONNECTOR);
         buff.append(s3);
         buff.append(CONNECTOR);
         buff.append(s4);
         return buff.toString();
 
     }
 
     /**
      * @param attribute Check will be done for this Attribute.
      * @return TRUE if there are any permissible values associated with this
      *         attribute, otherwise returns false.
      */
     public static boolean isEnumerated(AttributeInterface attribute) {
         if (attribute.getAttributeTypeInformation().getDataElement() instanceof UserDefinedDEInterface) {
             UserDefinedDEInterface de = (UserDefinedDEInterface) attribute.getAttributeTypeInformation()
                     .getDataElement();
             return de.getPermissibleValueCollection().size() != 0;
         }
         return false;
     }
 
     /**
      * @param attribute Attribute to process.
      * @return Returns all the permissible values associated with this
      *         attribute.
      */
     public static Collection<PermissibleValueInterface> getPermissibleValues(AttributeInterface attribute) {
         if (isEnumerated(attribute)) {
             UserDefinedDEInterface de = (UserDefinedDEInterface) attribute.getAttributeTypeInformation()
                     .getDataElement();
             return de.getPermissibleValueCollection();
         }
         return new ArrayList<PermissibleValueInterface>(0);
     }
 
     /**
      * Checks whether passed attribute/association is inherited.
      * 
      * @param abstractAttribute Attribute/Association to check.
      * @return TRUE if it is inherited else returns FALSE
      */
     public static boolean isInherited(AbstractAttributeInterface abstractAttribute) {
         String TYPE_DERIVED = "derived";
         for (TaggedValueInterface tag : abstractAttribute.getTaggedValueCollection()) {
             if (tag.getKey().equals(TYPE_DERIVED)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @param taggedValues collection of TaggedValueInterface
      * @param key string
      * @return The tagged value for given key in given tagged value collection.
      */
     public static TaggedValueInterface getTaggedValue(Collection<TaggedValueInterface> taggedValues, String key) {
         for (TaggedValueInterface taggedValue : taggedValues) {
             if (taggedValue.getKey().equals(key)) {
                 return taggedValue;
             }
         }
         return null;
     }
 
     /**
      * Checks whether passed Entity is a category or not.
      * 
      * @param entity Entity to check
      * @return Returns TRUE if given entity is Category, else returns false.
      */
     public static boolean isCategory(EntityInterface entity) {
         TaggedValueInterface tag = getTaggedValue(entity.getTaggedValueCollection(), TYPE_CATEGORY);
         return tag != null;
     }
 
     // END COPY
 
     // TODO COPIED From edu.wustl.common.Utility
     public static String parseClassName(String fullyQualifiedName) {
        return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
     }
 
     // END COPY
 }
