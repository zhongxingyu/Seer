 package edu.wustl.cab2b.server.cache;
 
 import java.util.Collection;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.wustl.cab2b.common.util.Utility;
 
 /**
  * @author Chandrakant Talele
  * @author Rahul Ner
  */
 public class CompareUtil {
     /**
      * @param cachedEntity
      * @param patternEntity
      * @return
      */
     public static boolean compare(EntityInterface cachedEntity, EntityInterface patternEntity) {
         boolean matchStatus = false;
         if (patternEntity.getName() != null && cachedEntity.getName() != null) {
             String patternName = "*" + patternEntity.getName() + "*";
             String className = cachedEntity.getName();
             String onlyClassName = className.substring(className.lastIndexOf(".") + 1, className.length());
             matchStatus = Utility.compareRegEx(patternName, onlyClassName);
         }
 
         if (patternEntity.getDescription() != null && cachedEntity.getDescription() != null) {
             String patternDescription = "*" + patternEntity.getDescription() + "*";
             matchStatus = matchStatus || Utility.compareRegEx(patternDescription, cachedEntity.getDescription());
         }
 
         matchStatus = matchStatus || compare(cachedEntity.getSemanticPropertyCollection(),
                                              patternEntity.getSemanticPropertyCollection());
         return matchStatus;
     }
 
     /**
      * @param cachedSemanticProperty
      * @param patternSemanticProperty
      * @return
      */
     public static boolean compare(SemanticPropertyInterface cachedSemanticProperty,
                                   SemanticPropertyInterface patternSemanticProperty) {
         boolean matchStatus = false;
         if (cachedSemanticProperty.getConceptCode() != null) {
             // TODO this null check is bcoz caTissue
             // has some permissible values without
             // concept codes. This will never be the case with models from cDSR
             if (patternSemanticProperty.getConceptCode() != null) {
                 String patternConceptCode = "*" + patternSemanticProperty.getConceptCode() + "*";
                 matchStatus = Utility.compareRegEx(patternConceptCode, cachedSemanticProperty.getConceptCode());
             }
         }
         return matchStatus;
     }
 
     /**
      * @param cachedAttribute
      * @param patternAttribute
      * @return
      */
     public static boolean compare(AttributeInterface cachedAttribute, AttributeInterface patternAttribute) {
         boolean matchStatus = false;
 
         if (patternAttribute.getName() != null && cachedAttribute.getName() != null) {
             String patternName = "*" + patternAttribute.getName() + "*";
             matchStatus = Utility.compareRegEx(patternName, cachedAttribute.getName());
         }
 
         if (patternAttribute.getDescription() != null && cachedAttribute.getDescription() != null) {
             String patternDescription = "*" + patternAttribute.getDescription() + "*";
             matchStatus = matchStatus
                     || Utility.compareRegEx(patternDescription, cachedAttribute.getDescription());
         }
         matchStatus = matchStatus || compare(cachedAttribute.getSemanticPropertyCollection(),
                            patternAttribute.getSemanticPropertyCollection());
 
         return matchStatus;
 
     }
 
     /**
      * @param cachedProperties
      * @param patternProperties
      * @return
      */
     private static boolean compare(Collection<SemanticPropertyInterface> cachedProperties,
                                    Collection<SemanticPropertyInterface> patternProperties) {
         boolean matchStatus = false;
 
         if (patternProperties != null && cachedProperties != null) {
 
             for (SemanticPropertyInterface patternSemanticProperty : patternProperties) {
                 for (SemanticPropertyInterface cachedSemanticProperty : cachedProperties) {
                    matchStatus = matchStatus || compare(patternSemanticProperty, cachedSemanticProperty);
                 }
             }
         }
         return matchStatus;
     }
     
     /**
      * @param cachedPermissibleValue
      * @param patternPermissibleValue
      * @return
      */
     public static boolean compare(PermissibleValueInterface cachedPermissibleValue,PermissibleValueInterface patternPermissibleValue) {
         boolean matchStatus = false;
         if(patternPermissibleValue.getValueAsObject()!= null && cachedPermissibleValue.getValueAsObject()!=null) {
             String patternPermissibleString = patternPermissibleValue.getValueAsObject().toString();
             String cachedPermissibleString = cachedPermissibleValue.getValueAsObject().toString();
             matchStatus = Utility.compareRegEx(patternPermissibleString, cachedPermissibleString);
         }
         matchStatus = matchStatus || compare(cachedPermissibleValue.getSemanticPropertyCollection(),
                                              patternPermissibleValue.getSemanticPropertyCollection());
         return matchStatus;
     }
     
 }
