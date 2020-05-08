 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: ORMJavaAnnotationGenerator.java,v 1.4 2011/10/26 05:29:39 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.orm.annotator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Enumerator;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.texo.orm.annotations.model.orm.EnumType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrmPackage;
 
 /**
  * Generates java annotations from an EObject and its efeatures.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class ORMJavaAnnotationGenerator {
 
   public static final String PACKAGE_PREFIX_SOURCE = "texo.package.prefix"; //$NON-NLS-1$
 
   private static ORMJavaAnnotationGenerator instance = new ORMJavaAnnotationGenerator();
 
   public static ORMJavaAnnotationGenerator getInstance() {
     return instance;
   }
 
   public static void setInstance(ORMJavaAnnotationGenerator instance) {
     ORMJavaAnnotationGenerator.instance = instance;
   }
 
   private List<EClassifier> eclipseLinkEClassifiers = new ArrayList<EClassifier>();
   private List<String> classFeatureNames = new ArrayList<String>();
   private Map<Class<?>, String> enumTypeNames = new HashMap<Class<?>, String>();
   private Map<String, String> renames = new HashMap<String, String>();
   private Map<EStructuralFeature, String> eFeatureAnnotationMapping = new HashMap<EStructuralFeature, String>();
 
   public ORMJavaAnnotationGenerator() {
     // the list of eclasses coming from eclipselink
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getAccessMethods());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getBasicCollection());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getBasicMap());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCache());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCacheCoordinationType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCacheInterceptor());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCacheType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getChangeTracking());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getChangeTrackingType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCloneCopyPolicy());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getConversionValue());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getConverter());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCopyPolicy());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getCustomizer());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getDirectionType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getDiscriminatorClass());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getEclipselinkCollectionTable());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getExistenceType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getIdValidation());
    eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getIndex());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getInstantiationCopyPolicy());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getJoinFetchType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getNamedStoredProcedureQuery());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getPrimaryKey());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getProperty());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getObjectTypeConverter());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getOptimisticLocking());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getOptimisticLockingType());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getQueryRedirectors());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getReadTransformer());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getStoredProcedureParameter());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getStructConverter());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getTimeOfDay());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getTransformation());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getTypeConverter());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getVariableOneToOne());
     eclipseLinkEClassifiers.add(OrmPackage.eINSTANCE.getWriteTransformer());
 
     // the feature names which represent java classes
     classFeatureNames.add("entityClass"); //$NON-NLS-1$
     classFeatureNames.add("targetEntity"); //$NON-NLS-1$
     classFeatureNames.add("targetClass"); //$NON-NLS-1$
     classFeatureNames.add("class"); //$NON-NLS-1$
     classFeatureNames.add("dataType"); //$NON-NLS-1$
     classFeatureNames.add("objectType"); //$NON-NLS-1$
 
     enumTypeNames.put(EnumType.class, "EnumType");
 
     eFeatureAnnotationMapping.put(OrmPackage.eINSTANCE.getElementCollection_Convert(),
         "org.eclipse.persistence.annotations.Convert");
     eFeatureAnnotationMapping.put(OrmPackage.eINSTANCE.getBasic_Convert(),
         "org.eclipse.persistence.annotations.Convert");
 
     renames.put("entityResults", "entities");
     renames.put("columnResults", "columns");
   }
 
   /**
    * Generate java annotation for a certain ormAnnotation.
    * 
    * @param annotation
    * @param includeFeatures
    *          if set then only these eFeatures are exported as javaannotation (in addition to the ones controlled by the
    *          next parameter)
    * @param referencesEnumsAsSeparateAnnotation
    *          export the enums and ereferences not listed in includeFeatures as separate main annotations.
    * @return the java annotation which can be added to the javacode
    */
   public String generateJavaAnnotation(EObject annotation, List<EStructuralFeature> includeFeatures,
       boolean referencesEnumsAsSeparateAnnotation, String identifier) {
 
     final StringBuilder separateAnnotation = new StringBuilder();
 
     List<EStructuralFeature> localIncludedFeatures = includeFeatures;
     if (localIncludedFeatures == null) {
       localIncludedFeatures = Collections.emptyList();
     }
 
     final StringBuilder sb = new StringBuilder();
     sb.append("@" + getJavaPackage(annotation.eClass()) + "." + annotation.eClass().getName()); //$NON-NLS-1$//$NON-NLS-2$
     sb.append("("); //$NON-NLS-1$
     boolean addComma = false;
     for (EStructuralFeature eFeature : annotation.eClass().getEStructuralFeatures()) {
       final Object value = annotation.eGet(eFeature);
       if (!annotation.eIsSet(eFeature)) {
         continue;
       }
       // ignore this one
       if (eFeature.getName().equals("description")) {
         continue;
       }
       if (value == null || value instanceof Collection<?> && ((Collection<?>) value).isEmpty()) {
         continue;
       }
 
       // always ignore this one, except for the converter
       if (eFeature.getEContainingClass() != OrmPackage.eINSTANCE.getConverter() && eFeature.getName().equals("class")) { //$NON-NLS-1$
         continue;
       }
 
       // for now assume that the value is a primitive
       if (eFeatureAnnotationMapping.containsKey(eFeature)) {
         if (value instanceof String) {
           separateAnnotation.append("@" + eFeatureAnnotationMapping.get(eFeature) + "(\"" + value + "\")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         } else {
           separateAnnotation.append("@" + eFeatureAnnotationMapping.get(eFeature) + "(" + value + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         }
         continue;
       }
 
       if ((localIncludedFeatures.size() == 0 || !localIncludedFeatures.contains(eFeature))
           && eFeature instanceof EAttribute && value instanceof Enumerator && referencesEnumsAsSeparateAnnotation) {
         if (separateAnnotation.length() > 0) {
           separateAnnotation.append("\n"); //$NON-NLS-1$
         }
         final Enumerator enumerator = (Enumerator) value;
         separateAnnotation.append(generateJavaAnnotation(eFeature, enumerator));
         continue;
       }
 
       if ((localIncludedFeatures.size() == 0 || !localIncludedFeatures.contains(eFeature))
           && eFeature instanceof EReference && referencesEnumsAsSeparateAnnotation) {
         if (separateAnnotation.length() > 0) {
           separateAnnotation.append("\n"); //$NON-NLS-1$
         }
         if (((EReference) eFeature).isMany()) {
           final Collection<?> values = (Collection<?>) value;
           if (values.isEmpty()) {
             continue;
           }
           separateAnnotation
               .append("@" + getJavaPackage(eFeature.getEType()) + "." + pluralize(upperCaseFirst(eFeature.getName())) + "({"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
           boolean addArrayComma = false;
           for (Object val : values) {
             if (val instanceof BaseOrmAnnotation) {
               if (addArrayComma) {
                 separateAnnotation.append(", "); //$NON-NLS-1$
               }
               addArrayComma = true;
               separateAnnotation.append(((BaseOrmAnnotation) val).getJavaAnnotation(identifier));
             }
           }
           separateAnnotation.append("})"); //$NON-NLS-1$
         } else {
           separateAnnotation.append(((BaseOrmAnnotation) value).getJavaAnnotation(identifier));
         }
         continue;
       }
       if (localIncludedFeatures.size() > 0 && !localIncludedFeatures.contains(eFeature)) {
         continue;
       }
       if (addComma) {
         sb.append(", "); //$NON-NLS-1$
       }
       addComma = true;
       if (value instanceof Collection<?>) {
         if (eFeature.getName().endsWith("s")) { //$NON-NLS-1$
           sb.append(eFeature.getName() + "="); //$NON-NLS-1$
         } else {
           sb.append(eFeature.getName() + "s="); //$NON-NLS-1$
         }
 
         final Collection<?> values = (Collection<?>) value;
         sb.append("{"); //$NON-NLS-1$
         boolean doAddComma = false;
         for (Object val : values) {
           if (doAddComma) {
             sb.append(", "); //$NON-NLS-1$
           }
           sb.append(generateJavaAnnotation(eFeature, val, identifier));
           doAddComma = true;
         }
         sb.append("}"); //$NON-NLS-1$
       } else {
         // special case to rename, make more generic
         if (eFeature == OrmPackage.eINSTANCE.getConverter_Class()) {
           sb.append("converterClass=");
         } else {
           sb.append(eFeature.getName() + "="); //$NON-NLS-1$
         }
         sb.append(generateJavaAnnotation(eFeature, value, identifier));
       }
     }
     sb.append(")"); //$NON-NLS-1$
     if (separateAnnotation.length() > 0) {
       sb.append("\n" + separateAnnotation); //$NON-NLS-1$
     }
 
     String resultStr = sb.toString();
     for (String key : renames.keySet()) {
       if (resultStr.contains(key)) {
         resultStr = resultStr.replace(key, renames.get(key));
       }
     }
 
     return resultStr;
   }
 
   private String pluralize(String value) {
     if (value == null) {
       return null;
     }
     if (value.endsWith("y")) {
       return value.substring(0, value.length() - 1) + "ies";
     }
    if (value.endsWith("x")) {
      return value + "es";
    }
     return value + "s";
   }
 
   private String upperCaseFirst(String value) {
     return value.substring(0, 1).toUpperCase() + value.substring(1);
   }
 
   public String generateJavaAnnotation(EStructuralFeature eFeature, Object value, String identifier) {
     if (value instanceof String) {
       // special case
       if (classFeatureNames.contains(eFeature.getName())) {
         return value + ".class"; //$NON-NLS-1$
       }
       return "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
     }
     if (eFeature instanceof EReference) {
       if (value instanceof BaseOrmAnnotation) {
         return ((BaseOrmAnnotation) value).getJavaAnnotation(identifier);
       }
       return ""; //$NON-NLS-1$
     }
     if (eFeature.getEType() instanceof EEnum) {
       final Enumerator enumerator = (Enumerator) value;
       return getJavaPackage(eFeature.getEType()) + "." + eFeature.getEType().getName() + "." + enumerator.getLiteral(); //$NON-NLS-1$ //$NON-NLS-2$
     }
     return value + ""; //$NON-NLS-1$
   }
 
   private String generateJavaAnnotation(EStructuralFeature eFeature, Enumerator enumerator) {
     String valueType = enumerator.getClass().getSimpleName();
     if (enumTypeNames.containsKey(enumerator.getClass())) {
       valueType = enumTypeNames.get(enumerator.getClass());
     } else if (!valueType.endsWith("Type")) { //$NON-NLS-1$
       valueType += "Type"; //$NON-NLS-1$
     }
 
     return "@" + getJavaPackage(eFeature.getEType()) + "." + upperCaseFirst(eFeature.getName()) + "(" + getJavaPackage(eFeature.getEType()) + "." + valueType //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
         + "." + enumerator.getLiteral() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   private String getJavaPackage(EClassifier eClassifier) {
 
     for (EClassifier chkEClassifier : eclipseLinkEClassifiers) {
       if (chkEClassifier == eClassifier) {
         return "org.eclipse.persistence.annotations"; //$NON-NLS-1$
       }
     }
     return "javax.persistence"; //$NON-NLS-1$
   }
 }
