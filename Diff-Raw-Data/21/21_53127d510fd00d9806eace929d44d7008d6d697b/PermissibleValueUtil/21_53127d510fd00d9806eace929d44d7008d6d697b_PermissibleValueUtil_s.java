 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.common;
 
 import gov.nih.nci.caintegrator.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator.domain.annotation.PermissibleValue;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * This is a static utility class used by the UI to update the permissibleValue collection.
  */
 public final class PermissibleValueUtil {
 
     private PermissibleValueUtil() {
         //Intentionally left blank.
     }
 
     /**
      * @param abstractPermissibleValues abstractPermissibleValues
      * @return set of permissible values
      */
     public static Set<String> getDisplayPermissibleValue(Collection<PermissibleValue> abstractPermissibleValues) {
         Set<String> results = new HashSet<String>();
         for (PermissibleValue abstractPermissibleValue : abstractPermissibleValues) {
             String displayString = abstractPermissibleValue.toString();
             //TODO Need to decide how to display null value, for now we just skip it
             if (displayString != null) {
                 results.add(displayString);
             }
         }
         return results;
     }
 
 
     /**
      * Update the permissibleValue collection.
      *
      * @param type the type of the abstractPermissibleValue
      * @param abstractPermissibleValues the PermissibleValue collection
      * @param newList the new list of DisplayValues
      */
     public static void update(String type, Collection<PermissibleValue> abstractPermissibleValues,
             List<String> newList) {
 
         checkObsolete(abstractPermissibleValues, newList);
         addNewValue(type, abstractPermissibleValues, newList);
     }
 
     private static void checkObsolete(Collection<PermissibleValue> abstractPermissibleValues, List<String> newList) {
         List<PermissibleValue> removeList = new ArrayList<PermissibleValue>();
         for (PermissibleValue abstractPermissibleValue : abstractPermissibleValues) {
             if (newList == null || !newList.contains(abstractPermissibleValue.toString())) {
                 removeList.add(abstractPermissibleValue);
             }
         }
         abstractPermissibleValues.removeAll(removeList);
     }
 
     /**
      * Add new values to the permissibleValue collection.
      *
      * @param type the type of the abstractPermissibleValue
      * @param abstractPermissibleValues the PermissibleValue collection
      * @param addList the list of DisplayString to add
      */
     public static void addNewValue(String type, Collection<PermissibleValue> abstractPermissibleValues,
             List<String> addList) {
         if (addList == null) {
             return;
         }
         for (String displayString : addList) {
             try {
                 addNewValue(type, abstractPermissibleValues, displayString);
             } catch (NumberFormatException e) {
                 abstractPermissibleValues.clear();
                 return;
             } catch (ParseException e) {
                 abstractPermissibleValues.clear();
                 return;
             }
         }
     }
 
     /**
      * Remove a list of permissibleValues from the collection.
      *
      * @param abstractPermissibleValues the PermissibleValue collection
      * @param removePermissibleDisplayValues the list PermissibleDisplayValue to be removed
      */
     public static void removeValue(Collection<PermissibleValue> abstractPermissibleValues,
             List<String> removePermissibleDisplayValues) {
         if (removePermissibleDisplayValues == null) {
             return;
         }
         for (String displayString : removePermissibleDisplayValues) {
             removeValue(abstractPermissibleValues, displayString);
         }
     }
 
     private static void removeValue(Collection<PermissibleValue> abstractPermissibleValues,
             String removePermissibleDisplayValue) {
         PermissibleValue abstractPermissibleValue = getObject(abstractPermissibleValues,
                 removePermissibleDisplayValue);
         if (abstractPermissibleValue == null) {
             return;
         }
         abstractPermissibleValues.remove(abstractPermissibleValue);
     }
 
     /**
      * Return the object with the same display string.
      */
     private static PermissibleValue getObject(Collection<PermissibleValue> abstractPermissibleValues,
             String displayString) {
         for (PermissibleValue abstractPermissibleValue : abstractPermissibleValues) {
             if (displayString.equals(abstractPermissibleValue.toString())) {
                 return abstractPermissibleValue;
             }
         }
         return null;
     }
 
     private static void addNewValue(String type, Collection<PermissibleValue> abstractPermissibleValues,
             String displayString) throws ParseException {
         if (containsDisplayString(abstractPermissibleValues, displayString) || StringUtils.isBlank(displayString)) {
             return;
         }
         if (type.equals(AnnotationTypeEnum.STRING.getValue())) {
             addStringValue(abstractPermissibleValues, displayString);
         }
         if (type.equals(AnnotationTypeEnum.NUMERIC.getValue())) {
             addNumericValue(abstractPermissibleValues, displayString);
         }
         if (type.equals(AnnotationTypeEnum.DATE.getValue())) {
             addDateValue(abstractPermissibleValues, displayString);
         }
     }
 
     private static void addStringValue(Collection<PermissibleValue> abstractPermissibleValues,
             String displayString) {
         PermissibleValue newPermissibleValue = new PermissibleValue();
         newPermissibleValue.setValue(displayString);
         abstractPermissibleValues.add(newPermissibleValue);
     }
 
     private static void addNumericValue(Collection<PermissibleValue> abstractPermissibleValues,
             String displayString) {
         PermissibleValue newPermissibleValue = new PermissibleValue();
         newPermissibleValue.setValue(NumericUtil.formatDisplay(displayString));
         abstractPermissibleValues.add(newPermissibleValue);
     }
 
     private static void addDateValue(Collection<PermissibleValue> abstractPermissibleValues,
             String displayString) throws ParseException {
         PermissibleValue newPermissibleValue = new PermissibleValue();
         newPermissibleValue.setValue(DateUtil.toString(DateUtil.createDate(displayString)));
         abstractPermissibleValues.add(newPermissibleValue);
     }
 
     private static boolean containsDisplayString(Collection<PermissibleValue> abstractPermissibleValues,
             String displayString) {
 
         for (PermissibleValue abstractPermissibleValue : abstractPermissibleValues) {
             if (displayString.equals(abstractPermissibleValue.toString())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Given a set of uniqueValues and an AnnotationDefinition it will return all values that are not permissible.
      * @param uniqueValues to check against.
      * @param annotationDefinition to validate permissible values.
      * @return all values that are non permissible (or empty set).
      */
     public static Set<String> retrieveValuesNotPermissible(Set<Object> uniqueValues,
             AnnotationDefinition annotationDefinition) {
         return retrieveValuesNotInPermissibleValues(retrieveUniquePermissibleValues(annotationDefinition),
                 uniqueValues);
     }
 
     private static <T> Set<String> retrieveValuesNotInPermissibleValues(Set<T> permissibleValues,
             Collection<T> uniqueValues) {
         Set<String> valuesNotInPemissibleList = new HashSet<String>();
         if (CollectionUtils.isNotEmpty(permissibleValues)) {
             for (T uniqueValue : uniqueValues) {
                 if (!permissibleValues.contains(uniqueValue)
                      && StringUtils.isNotBlank(ObjectUtils.toString(uniqueValue))) {
                    valuesNotInPemissibleList.add(uniqueValue.toString());
                 }
             }
         }
         return valuesNotInPemissibleList;
    }
 
     @SuppressWarnings("unchecked")
     private static <T> Set <T> retrieveUniquePermissibleValues(AnnotationDefinition annotationDefinition) {
         Set<T> permissibleValues = new HashSet<T>();
         for (PermissibleValue permissibleValue : annotationDefinition.getPermissibleValueCollection()) {
             permissibleValues.add((T) retrievePermissibleValueAsPrimitiveType(permissibleValue,
                     annotationDefinition.getDataType()));
         }
         return permissibleValues;
     }
 
     @SuppressWarnings("PMD.MissingBreakInSwitch") // No need for break, it returns instead.
     private static Object retrievePermissibleValueAsPrimitiveType(PermissibleValue permissibleValue,
                         AnnotationTypeEnum dataType) {
         if (dataType == null) {
             throw new IllegalArgumentException("Data Type for the Annotation Definition is unknown.");
         }
         switch (dataType) {
         case STRING:
             return permissibleValue.getValue();
         case NUMERIC:
             return (Double.valueOf(permissibleValue.getValue()));
         case DATE:
             try {
                 return (DateUtil.createDate(permissibleValue.getValue()));
             } catch (ParseException e) {
                 throw new IllegalStateException("Invalid date format as a Permissible Value, unable to parse.", e);
             }
         default:
             throw new IllegalArgumentException("Unknown permissibleValue Type");
         }
     }
 
 }
