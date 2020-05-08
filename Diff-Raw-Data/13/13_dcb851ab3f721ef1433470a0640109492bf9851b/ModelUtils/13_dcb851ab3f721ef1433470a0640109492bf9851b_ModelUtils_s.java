 package org.eclipse.emf.texo.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.texo.model.ModelConstants;
 import org.eclipse.emf.texo.model.ModelFeatureMapEntry;
 import org.eclipse.emf.texo.model.ModelPackage;
 import org.eclipse.emf.texo.model.ModelResolver;
 
 /**
  * Utility methods which are used at runtime.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class ModelUtils {
   public static final String QUALIFIERSEPARATOR = "|"; //$NON-NLS-1$
 
  private static final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'"); //$NON-NLS-1$
 
   /**
    * Return true if the {@link EStructuralFeature} is an {@link EAttribute} is a optional xsd attribute.
    */
   public static boolean isOptionalXSDAttribute(EStructuralFeature eFeature) {
     return eFeature instanceof EAttribute && !eFeature.isMany()
         && ExtendedMetaData.INSTANCE.getFeatureKind(eFeature) == ExtendedMetaData.ATTRIBUTE_FEATURE;
   }
 
   /**
    * Returns true if the {@link EStructuralFeature} models an xsd:any.
    * 
    * @see AnyFeatureMapEntry
    */
   public static boolean isAnyType(EStructuralFeature eFeature) {
     final EAnnotation eAnnotation = eFeature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
     if (eAnnotation == null) {
       return false;
     }
     final String kind = eAnnotation.getDetails().get("kind"); //$NON-NLS-1$
     return kind != null && (kind.equals("elementWildcard") || kind.equals("attributeWildcard")); //$NON-NLS-1$//$NON-NLS-2$
   }
 
   /**
    * If the value is a featuregroup then walk through the structure to find the deepest one and return that value.
    */
   public static Object findValue(ModelFeatureMapEntry<?> modelFeatureMap) {
     if (FeatureMapUtil.isFeatureMap(modelFeatureMap.getEStructuralFeature())) {
       final ModelFeatureMapEntry<?> modelFeatureMapEntry = ModelResolver.getInstance().getModelFeatureMapEntry(
           modelFeatureMap.getEStructuralFeature(), modelFeatureMap.getValue());
 
       return findValue(modelFeatureMapEntry);
     }
     return modelFeatureMap.getValue();
   }
 
   /**
    * if the value is a featuregroup then walk through the structure to find the deepest one and return that value.
    * 
    * In EMF nested featuremaps are stored in a flat list, in Texo the hierarchical structure is maintained this method
    * helps in the conversion.
    */
   public static EStructuralFeature findFeature(ModelFeatureMapEntry<?> modelFeatureMap) {
     if (FeatureMapUtil.isFeatureMap(modelFeatureMap.getEStructuralFeature())) {
       final ModelFeatureMapEntry<?> modelFeatureMapEntry = ModelResolver.getInstance().getModelFeatureMapEntry(
           modelFeatureMap.getEStructuralFeature(), modelFeatureMap.getValue());
 
       return findFeature(modelFeatureMapEntry);
     }
     return modelFeatureMap.getEStructuralFeature();
   }
 
   /**
    * Returns a qualified string representation of the {@link EStructuralFeature} using
    * {@link #getQualifiedNameFromEClass(EClass)}.
    */
   public static String getQualifiedNameFromEStructuralFeature(EStructuralFeature eFeature) {
     return getQualifiedNameFromEClass(eFeature.getEContainingClass()) + QUALIFIERSEPARATOR + eFeature.getName();
   }
 
   /**
    * Returns the {@link EStructuralFeature} of a certain {@link EClass} also encoded in the name.
    * 
    * @param name
    * @see #getEClassFromQualifiedName(String)
    */
   public static EStructuralFeature getEStructuralFeatureFromQualifiedName(String name) {
     final int index = name.lastIndexOf(QUALIFIERSEPARATOR);
     final String eClassString = name.substring(0, index);
     final EClass eClass = getEClassFromQualifiedName(eClassString);
     final String featureName = name.substring(1 + index);
     return eClass.getEStructuralFeature(featureName);
   }
 
   /**
    * Returns a qualified string representation of the class using the ns prefix of the epackage.
    */
   public static String getQualifiedNameFromEClass(EClass eClass) {
     return eClass.getEPackage().getNsPrefix() + QUALIFIERSEPARATOR + eClass.getName();
   }
 
   /**
    * Searches all the eclassifiers of the {@link ModelPackage} instances that have been registered with the
    * {@link ModelResolver}.
    * 
    * Note, can handle qualified as well as unqualified names. The qualified names will have the epackage ns prefix
    * prepended separated by the {@link #QUALIFIERSEPARATOR}, for example: library|Book
    * 
    * But also unqualified names are handled, although then if different epackages have eclasses with the same name then
    * the wrong eclass can be returned.
    * 
    * @param name
    *          the (qualified) name of the EClass
    * @return an EClass
    * @throws IllegalArgumentException
    *           if not EClass was found
    */
   public static EClass getEClassFromQualifiedName(String name) {
     String nameSpacePrefix = null;
     String eClassName = name;
     if (eClassName.contains(QUALIFIERSEPARATOR)) {
       final int index = eClassName.indexOf(QUALIFIERSEPARATOR);
       nameSpacePrefix = eClassName.substring(0, index);
       eClassName = eClassName.substring(index + 1);
     }
     for (ModelPackage modelPackage : ModelResolver.getInstance().getModelPackages()) {
       final EPackage ePackage = modelPackage.getEPackage();
       if (nameSpacePrefix != null && !ePackage.getNsPrefix().equals(nameSpacePrefix)) {
         continue;
       }
       for (EClassifier eClassifier : ePackage.getEClassifiers()) {
         if (eClassifier.getName().equals(eClassName)) {
           return (EClass) eClassifier;
         }
       }
     }
     throw new IllegalArgumentException("No EClass found using name " + name); //$NON-NLS-1$
   }
 
   /** Returns the lower case version of the string converted with English Locale **/
   public static String toLowerCase(String value) {
     return value == null ? null : value.toLowerCase(Locale.ENGLISH);
   }
 
   /** Returns the upper case version of the string converted with English Locale **/
   public static String toUpperCase(String value) {
     return value == null ? null : value.toUpperCase(Locale.ENGLISH);
   }
 
   /** Returns the first char only lower case version of the string converted with English Locale **/
   public static String lowerCaseFirst(String value) {
     if (value == null) {
       return null;
     }
 
     if (value.length() == 1) {
       return toLowerCase(value);
     }
 
     return value.substring(0, 1).toLowerCase(Locale.ENGLISH) + value.substring(1);
   }
 
   /** Returns the first char only upper case version of the string converted with English Locale **/
   public static String upCaseFirst(String value) {
     if (value == null) {
       return null;
     }
 
     if (value.length() == 1) {
       return toUpperCase(value);
     }
 
     return value.substring(0, 1).toUpperCase(Locale.ENGLISH) + value.substring(1);
   }
 
   /**
    * Converts a XML String date(time) to a Date object. Note: method is synchronized as it uses a shared
    * {@link SimpleDateFormat} instance.
    * 
    * @param xmlString
    *          the xml String containing the date time in XML Schema format.
    * @return a java Date object representing the date time coded in the xmlString
    */
   public static synchronized Date createFromXML(final String xmlString) {
     try {
       return xmlDateFormat.parse(xmlString);
     } catch (final ParseException e) {
       throw new IllegalArgumentException(e);
     }
   }
 
   /**
    * Converts a Date to a valid XML String. Note: method is synchronized as it uses a shared {@link SimpleDateFormat}
    * instance.
    * 
    * @param dt
    *          the Date to format
    * @return the String format of a {@link Date}, the standard xml format is used: yyyy-MM-dd'T'HH:mm:ss.S'Z'
    */
   public static synchronized String convertToXML(final Date dt) {
     return xmlDateFormat.format(dt);
   }
 
   /**
    * Read the ecore file belonging to the {@link ModelPackage} ({@see ModelPackage#getEcoreFileName()}. The default
    * impl. will read the ecore file from the classpath of the passed ModelPackage . All read EPackages are registered in
    * the central {@link EPackage.Registry} from where they can be retrieved.
    * 
    * @param modelPackage
    *          the modelPackage for which the EPackage is read
    */
   public static void readEPackagesFromFile(final ModelPackage modelPackage) {
 
     // if already present in the package registry then don't read anymore from file.
     if (ModelResolver.getInstance().getEPackageRegistry().getEPackage(modelPackage.getNsURI()) != null) {
       return;
     }
 
     final String ecoreFileName = modelPackage.getEcoreFileName();
     Check.isNotEmpty(ecoreFileName, "Ecore file of ModelPackage may not be empty."); //$NON-NLS-1$
     if (ecoreFileName != null && ecoreFileName.length() > 0) {
       try {
 
         // note the resource uri is the same as by which it is saved in the
         // GenEPackage.getECoreFileContent
         final Resource res = new EcoreResourceFactoryImpl().createResource(URI.createURI(modelPackage.getNsURI()));
         final InputStream is = modelPackage.getClass().getResourceAsStream(ecoreFileName);
         res.load(is, Collections.EMPTY_MAP);
         is.close();
 
         // NOTE beware: for reference the below does not work if local references need to
         // resolved, probably because the resource is created with the wrong uri
         // final InputStream is = ecoreModelPackage.getClass().getResourceAsStream(ecoreFileName);
         // final ResourceSet rs = new ResourceSetImpl();
         // rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new
         // EcoreResourceFactoryImpl());
         // final Resource res = rs.createResource(URI.createURI(ecoreFileName));
         // res.load(is, Collections.EMPTY_MAP);
         // is.close();
 
         final Iterator<?> it = res.getAllContents();
         while (it.hasNext()) {
           final Object obj = it.next();
           if (obj instanceof EPackage) {
             final EPackage epack = (EPackage) obj;
             registerEPackage(epack, ModelResolver.getInstance().getEPackageRegistry());
           }
         }
       } catch (final IOException e) {
         throw new IllegalStateException("Exception while loading models from ecorefile " //$NON-NLS-1$
             + ecoreFileName);
       }
     }
   }
 
   /**
    * Recursively registers an EPackage and its subpackages. If an ePackage was already registered then that one is
    * returned.
    * 
    * @param the
    *          ePackage to register
    * @return the ePackage found in the registry or the passed ePackage
    */
   public static EPackage registerEPackage(final EPackage ePackage, EPackage.Registry registry) {
     if (registry.containsKey(ePackage.getNsURI())) {
       // log.warn("EPackage with this \"" + ePackage.getNsURI()
       // + "\" already registered. Returning the registered one if it is an epackage");
       final Object packageObject = registry.get(ePackage.getNsURI());
       if (packageObject instanceof EPackage) {
         return (EPackage) packageObject;
       }
     }
 
     registry.put(ePackage.getNsURI(), ePackage);
     for (final EPackage eSubPackage : new ArrayList<EPackage>(ePackage.getESubpackages())) {
       final EPackage registeredSubPackage = registerEPackage(eSubPackage, registry);
       if (registeredSubPackage != eSubPackage) {
         final int currentIndex = ePackage.getESubpackages().indexOf(eSubPackage);
         assert eSubPackage == ePackage.getESubpackages().set(currentIndex, registeredSubPackage);
       }
     }
     return ePackage;
   }
 
   /**
    * See the {@link #isObjectTypeWithEnumBaseType(EDataType)} for information.
    * 
    * @param eDataType
    * @return
    */
   public static EEnum getEnumBaseDataTypeIfObject(EDataType eDataType) {
     if (eDataType instanceof EEnum) {
       return null;
     }
     final ExtendedMetaData extendedMetaData = ExtendedMetaData.INSTANCE;
     for (EDataType baseDataType = eDataType; baseDataType != null; baseDataType = extendedMetaData
         .getBaseType(baseDataType)) {
       if (baseDataType instanceof EEnum) {
         return (EEnum) baseDataType;
       }
     }
     return null;
   }
 
   /**
    * @param nsuri
    *          the namespace uri to search for the epackage
    * @return the EPackage on the basis of the nsuri
    */
   public static EPackage getEPackage(final String nsuri) {
     final EPackage epackage = ModelResolver.getInstance().getEPackageRegistry().getEPackage(nsuri);
     if (epackage == null) {
       throw new IllegalArgumentException("No EPackage registered using the nsuri: " + nsuri); //$NON-NLS-1$
     }
     return epackage;
   }
 
   /** @return true if the eClass represents a Map.Entry */
   // Note method is copied in DataGeneratorUtils
   public static boolean isEMap(final EClass eClass) {
     return eClass != null && eClass.getInstanceClass() != null
         && Map.Entry.class.isAssignableFrom(eClass.getInstanceClass()) && eClass.getEStructuralFeature("key") != null //$NON-NLS-1$ 
         && eClass.getEStructuralFeature("value") != null; //$NON-NLS-1$
   }
 
   /** @return true if the eFeature refers to an EMap EClass */
   public static boolean isEMap(final EStructuralFeature eFeature) {
     if (!(eFeature.getEType() instanceof EClass)) {
       return false;
     }
     return isEMap((EClass) eFeature.getEType());
   }
 
   /** Returns true if the passed feature is a wildcard mixed feature */
   public static boolean isMixed(EStructuralFeature feature) {
     EAnnotation eAnnotation = feature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
     if (eAnnotation == null) {
       return false;
     }
     final String kind = eAnnotation.getDetails().get("kind"); //$NON-NLS-1$
     final String name = eAnnotation.getDetails().get("name"); //$NON-NLS-1$
     return kind != null && kind.compareTo("elementWildcard") == 0 && name != null //$NON-NLS-1$
         && name.compareTo(":mixed") == 0; //$NON-NLS-1$
   }
 
   /**
    * Returns true if the {@link EModelElement} has an {@link EAnnotation} with source
    * {@link ModelConstants#EANNOTATION_SOURCE} and the passed in annotionKey.
    * 
    * @param eModelElement
    *          the model element for which EAnnotations are checked
    * @param annotationKey
    *          the key to search for in the EAnnotations
    * @return true if the annotation is present
    * @see #getEAnnotation(EModelElement, String)
    */
   public static boolean hasEAnnotation(EModelElement eModelElement, String annotationKey) {
     return null != getEAnnotation(eModelElement, annotationKey);
   }
 
   /**
    * Returns the value of the EAnnotation of the {@link EModelElement} with source
    * {@link ModelConstants#EANNOTATION_SOURCE} and as the key the annotionKey.
    * 
    * @param eModelElement
    *          the model element for which the EAnnotations are checked
    * @param annotationKey
    *          the key to search for in the EAnnotations
    * @return the value for the annotation key, or null if not present
    */
   public static String getEAnnotation(EModelElement eModelElement, String annotationKey) {
     final EAnnotation eAnnotation = eModelElement.getEAnnotation(ModelConstants.EANNOTATION_SOURCE);
     if (eAnnotation == null) {
       return null;
     }
     return eAnnotation.getDetails().get(annotationKey);
   }
 
 }
