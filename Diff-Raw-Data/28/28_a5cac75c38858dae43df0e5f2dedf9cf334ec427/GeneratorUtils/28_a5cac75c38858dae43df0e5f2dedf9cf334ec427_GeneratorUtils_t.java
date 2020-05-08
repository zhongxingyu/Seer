 package org.eclipse.emf.texo.generator;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
 import org.eclipse.emf.texo.model.ModelFeatureMapEntry;
 import org.eclipse.emf.texo.utils.Check;
 import org.eclipse.emf.texo.utils.ModelUtils;
import org.eclipse.xsd.XSDDiagnostic;
import org.eclipse.xsd.XSDDiagnosticSeverity;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.ecore.XSDEcoreBuilder;
 import org.eclipse.xsd.util.XSDResourceFactoryImpl;
 import org.eclipse.xsd.util.XSDResourceImpl;
 
 /**
  * Model/Ecore related convenience methods to for example read ecore and xsd's.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class GeneratorUtils {
 
   /**
    * @param value
    * @return true if the value is null or has length 0 after trimming, false otherwise.
    */
   public static boolean isEmptyOrNull(String value) {
     return value == null || value.trim().length() == 0;
   }
 
   /**
    * @param value
    * @return true if the value is not null and not empty, false otherwise.
    */
   public static boolean isNotEmptyAndNotNull(String value) {
     return value != null && value.trim().length() > 0;
   }
 
   /**
    * @return true if the passed EStructuralFeature is part of a group
    */
   public static boolean isPartOfGroup(EStructuralFeature eFeature) {
     return null != ExtendedMetaData.INSTANCE.getGroup(eFeature);
   }
 
   /**
    * Create an unmodifiable list for a feature map for a specific EStructuralFeature.
    * 
    * @param featureMap
    *          the feature map to extract the values for a specific EStructuralFeature from
    * @param eStructuralFeature
    *          the EStructuralFeature to get all the values for
    * @see FeatureMap
    * @see Collections#unmodifiableList(List)
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <T, E extends ModelFeatureMapEntry<?>> List<T> createUnModifiableList(final Collection<E> featureMap,
       final EStructuralFeature eStructuralFeature) {
     final List<Object> result = new ArrayList<Object>();
     for (final ModelFeatureMapEntry<?> entry : featureMap) {
       // direct == should work, instances should be the same!
       if (entry.getEStructuralFeature() == eStructuralFeature) {
         result.add(entry.getValue());
       }
     }
     return (List<T>) Collections.unmodifiableList(result);
   }
 
   /**
    * @param feature
    *          the EStructuralFeature to check if it is an element
    * @return true if the passed feature is an element
    */
   public static boolean isElement(EStructuralFeature feature) {
     return isKind(feature, "element"); //$NON-NLS-1$
   }
 
   /**
    * @param feature
    *          the EStructuralFeature to check if it is a group
    * @return true if the passed feature is a group
    */
   public static boolean isGroup(EStructuralFeature feature) {
     return isKind(feature, "group"); //$NON-NLS-1$
   }
 
   /**
    * @param feature
    *          the EStructuralFeature to check if it is an attribute
    * @return true if the passed feature is an attribute
    */
   public static boolean isAttribute(EStructuralFeature feature) {
     return isKind(feature, "attribute"); //$NON-NLS-1$
   }
 
   /**
    * @param eClass
    *          the EStructuralFeatures of this EClass are searched for a mixed EStructuralFeature
    * @return the EStructuralFeature of the EClass which is mixed (see {@link #isMixed(EStructuralFeature)}).
    * @see EClass#getEAllStructuralFeatures()
    */
   public static EStructuralFeature getMixedEStructuralFeature(EClass eClass) {
     for (EStructuralFeature eStructuralFeature : eClass.getEAllStructuralFeatures()) {
       if (ModelUtils.isMixed(eStructuralFeature)) {
         return eStructuralFeature;
       }
     }
     return null;
   }
 
   private static boolean isKind(EStructuralFeature feature, String kindValue) {
     EAnnotation eAnnotation = feature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
     if (eAnnotation == null) {
       return false;
     }
     final String kind = eAnnotation.getDetails().get("kind"); //$NON-NLS-1$
     return kind != null && kind.compareTo(kindValue) == 0;
   }
 
   /**
    * Creates a new instance of the {@link EPackage.Registry}. This registry contains a copy of the content of the global
    * package registry.
    * 
    * @return a new instance of the EPackage registry, is initially filled with the content of the global EPackage
    *         Registry.
    */
   public static EPackage.Registry createEPackageRegistry() {
     final EPackage.Registry registry = new EPackageRegistryImpl();
     registry.putAll(EPackage.Registry.INSTANCE);
     return registry;
   }
 
   /**
    * Reads EPackages from an ecore or xsd files.
    * 
    * @param uri
    *          the URI pointing to ecore or xsd file
    * @param registry
    *          the epackage registry to use
    * @return the EPackages
    */
   public static List<EPackage> readEPackage(final java.net.URI uri, EPackage.Registry registry) {
     final List<java.net.URI> list = new ArrayList<java.net.URI>();
     list.add(uri);
     return readEPackages(list, registry);
   }
 
   /**
    * Reads EPackages from a set of ecore or xsd files.
    * 
    * @param uris
    *          the array of URI's pointing to ecore or xsd files
    * @param registry
    *          the registry to use when reading epackages
    * @return the EPackages
    */
   public static List<EPackage> readEPackages(final List<java.net.URI> uris, EPackage.Registry registry) {
     final List<URI> emfUris = new ArrayList<URI>();
     for (final java.net.URI uri : uris) {
       final String uriStr = uri.toString();
       emfUris.add(URI.createURI(uriStr));
     }
     return readEPackagesUsingEMFURI(emfUris, registry);
   }
 
   /**
    * Reads EPackages from a set of ecore or xsd files.
    * 
    * @param uris
    *          the array of EMF URI's pointing to ecore or xsd files
    * @param registry
    *          the registry to use when reading epackages
    * @return the EPackages
    */
   public static List<EPackage> readEPackagesUsingEMFURI(final List<URI> uris, EPackage.Registry registry) {
     final List<EPackage> ePackages = new ArrayList<EPackage>();
     final ResourceSet rs = new ResourceSetImpl();
     rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", //$NON-NLS-1$
         new EcoreResourceFactoryImpl());
     rs.setPackageRegistry(registry);
     // note passing resourcesets package registry to the xsdecore builder
     // this ensures that epackages which refer to eachother are handled
     // correctly
     final XSDEcoreBuilder ecoreBuilder = new XSDEcoreBuilder(new BasicExtendedMetaData(rs.getPackageRegistry()));
    ecoreBuilder.setValidate(true);
     for (final URI emfURI : uris) {
       if (emfURI.toString().endsWith("xsd")) { //$NON-NLS-1$
         for (final EObject eObject : ecoreBuilder.generate(emfURI)) {

          checkDiagnostics(ecoreBuilder);

           final EPackage ePackage = ModelUtils.registerEPackage((EPackage) eObject, registry);
           if (!ePackages.contains(ePackage)) {
             ePackages.add(ePackage);
           }
         }
       } else {
         final Resource res = rs.createResource(emfURI);
         try {
           res.load(Collections.EMPTY_MAP);
           final Iterator<EObject> it = res.getAllContents();
           while (it.hasNext()) {
             final Object obj = it.next();
             if (obj instanceof EPackage) {
               // TODO: should subepackages also be placed directly
               // in the result
               final EPackage ePackage = ModelUtils.registerEPackage((EPackage) obj, registry);
               if (!ePackages.contains(ePackage)) {
                 ePackages.add(ePackage);
               }
             }
           }
         } catch (final Exception e) {
           throw new IllegalArgumentException("Exception while loading resource from " //$NON-NLS-1$
               + emfURI.toString(), e);
         }
       }
     }
     return ePackages;
   }
 
  private static void checkDiagnostics(XSDEcoreBuilder ecoreBuilder) {
    final StringBuilder sb = new StringBuilder();
    final List<XSDDiagnostic> diagnostics = ecoreBuilder.getDiagnostics();
    for (XSDDiagnostic xsdDiagnostic : diagnostics) {
      if (xsdDiagnostic.getSeverity() == XSDDiagnosticSeverity.ERROR_LITERAL
          || xsdDiagnostic.getSeverity() == XSDDiagnosticSeverity.FATAL_LITERAL) {
        if (sb.length() > 0) {
          sb.append("\n"); //$NON-NLS-1$
        }
        sb.append(xsdDiagnostic.getLocation() + " (" + xsdDiagnostic.getLine() + ":" + xsdDiagnostic.getColumn() //$NON-NLS-1$//$NON-NLS-2$
            + "):" + xsdDiagnostic.getMessage());//$NON-NLS-1$
      }
    }

    if (sb.length() > 0) {
      throw new IllegalStateException(sb.toString());
    }
  }

   /**
    * Read the ecore file belonging from the ecoreFileName location and register it with the nsURI passed in the global
    * package registry.
    * 
    * @param modelPackage
    *          the modelPackage for which the EPackage is read
    * @param registry
    *          the registry to use when reading epackages
    */
   public static void readEPackagesFromFile(final String nsUri, final String ecoreFileName, EPackage.Registry registry) {
     Check.isNotEmpty(ecoreFileName, "Ecore file of ModelPackage may not be empty."); //$NON-NLS-1$
     if (ecoreFileName != null && ecoreFileName.length() > 0) {
       try {
         // note the resource uri is the same as by which it is saved in
         // the GenEPackage.getECoreFileContent
         final Resource res = new EcoreResourceFactoryImpl().createResource(URI.createURI(nsUri));
         final InputStream is = GeneratorUtils.class.getResourceAsStream(ecoreFileName);
         res.load(is, Collections.EMPTY_MAP);
         is.close();
 
         // NOTE beware: for reference the below does not work if local
         // references need to
         // resolved, probably because the resource is created with the
         // wrong uri
         // final InputStream is =
         // ecoreModelPackage.getClass().getResourceAsStream(ecoreFileName);
         // final ResourceSet rs = new ResourceSetImpl();
         // rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
         // new
         // EcoreResourceFactoryImpl());
         // final Resource res =
         // rs.createResource(URI.createURI(ecoreFileName));
         // res.load(is, Collections.EMPTY_MAP);
         // is.close();
 
         final Iterator<?> it = res.getAllContents();
         while (it.hasNext()) {
           final Object obj = it.next();
           if (obj instanceof EPackage) {
             final EPackage epack = (EPackage) obj;
             ModelUtils.registerEPackage(epack, registry);
           }
         }
       } catch (final IOException e) {
         throw new IllegalStateException("Exception while loading models from ecorefile " //$NON-NLS-1$
             + ecoreFileName);
       }
     }
   }
 
   /**
    * @return the flattened list of all sub epackages.
    */
   public static List<EPackage> getAllSubEPackages(final List<EPackage> ePackages) {
     final List<EPackage> result = new ArrayList<EPackage>();
     for (final EPackage ePackage : ePackages) {
       result.add(ePackage);
       result.addAll(getAllSubEPackages(ePackage));
     }
     return result;
   }
 
   /** @return all sub epackages from a parent package */
   public static List<EPackage> getAllSubEPackages(final EPackage ePackage) {
     final List<EPackage> subEPackages = new ArrayList<EPackage>();
     for (final EPackage subEPackage : ePackage.getESubpackages()) {
       subEPackages.add(subEPackage);
       subEPackages.addAll(getAllSubEPackages(subEPackage));
     }
     return subEPackages;
   }
 
   /** @return the value of an annotation with a certain key */
   public static String getEAnnotationValue(final EModelElement eModelElement, final String source, final String key) {
     final EAnnotation eAnnotation = eModelElement.getEAnnotation(source);
     if (eAnnotation == null) {
       return null;
     }
     return eAnnotation.getDetails().get(key);
   }
 
   /**
    * Creates a readable String from an {@link EStructuralFeature}.
    * 
    * @param eFeature
    *          the EStructuralFeature for which a readable String is created.
    * @return a readable String
    */
   public static String toString(final EStructuralFeature eFeature) {
     Check.isNotNullArgument(eFeature, "eFeature");//$NON-NLS-1$
     return eFeature.getClass().getSimpleName() + ": " + eFeature.getEContainingClass().getName() //$NON-NLS-1$
         + "." + eFeature.getName(); //$NON-NLS-1$
   }
 
   /**
    * Return the EPackage instance using a classname and a class loader. TODO: the classloader needs to be made
    * configurable
    * 
    * @param className
    *          the classname of the EPackage class instance
    * @return the EPackage instance
    */
   public static EPackage getEPackageInstance(final String className) {
     try {
       final Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(className);
       final EPackage ePackage = (EPackage) clz.getField("eINSTANCE").get(null);//$NON-NLS-1$
       return ePackage;
     } catch (final Exception e) {
       throw new IllegalArgumentException("Exception when getting EPackage instance " + className, e); //$NON-NLS-1$
     }
   }
 
   /**
    * Loads the xml/xmi referenced by the uri. Uses the standard {@link EPackage.Registry#INSTANCE}.
    * 
    * Different {@link XMLResource} options are set to read xml which is based by a XSD.
    * 
    * @param uri
    *          the uri pointing to a xml or xmi file
    * @return the contents in the files
    */
   public static List<EObject> loadResource(final String uri, EPackage.Registry registry) {
     final ResourceSet resourceSet = createDataResourceSet(registry);
     final Resource resource = resourceSet.createResource(URI.createURI(uri));
     final Map<Object, Object> options = new HashMap<Object, Object>();
     try {
       options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
       options.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
       options.put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);
       options.put(XMLResource.OPTION_LAX_WILDCARD_PROCESSING, Boolean.TRUE);
       options.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
       resource.load(options);
     } catch (final IOException e) {
       throw new IllegalStateException("Exception when loading uri " + uri, e); //$NON-NLS-1$
     }
     return resource.getContents();
   }
 
   /**
    * Creates a resource set which uses the passed in {@link EPackage.Registry} .
    * <p/>
    * This method registers the {@link XMLResourceFactoryImpl} and the {@link XMIResourceFactoryImpl} for resp. xml and
    * xmi extensions.
    * 
    * @return a new ResourceSet
    */
   public static ResourceSet createDataResourceSet(final EPackage.Registry registry) {
     final ResourceSet resourceSet = new ResourceSetImpl();
     resourceSet.setPackageRegistry(registry);
     resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", //$NON-NLS-1$
         new XMLResourceFactoryImpl());
     resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", //$NON-NLS-1$
         new XMIResourceFactoryImpl());
     return resourceSet;
   }
 
   /**
    * Read XML Schemas from a xsd file defined by the uri.
    * 
    * @param uri
    *          the uri should point to a xsd file
    * @return the list of XSD Schema objects (is often only one)
    * @see URI#createURI(String)
    * @see #getXSDResourceSet()
    */
   public static List<XSDSchema> getXSDSchemas(final String uri, EPackage.Registry registry) {
     final List<XSDSchema> xsdSchemas = new ArrayList<XSDSchema>();
     final ResourceSet resourceSet = getXSDResourceSet(registry);
     final Resource resource = resourceSet.getResource(URI.createURI(uri), true);
     for (final EObject eObject : resource.getContents()) {
       // TODO: can it ever be anything else?
       if (eObject instanceof XSDSchema) {
         xsdSchemas.add((XSDSchema) eObject);
       }
     }
     return xsdSchemas;
   }
 
   /**
    * Create a {@link ResourceSet} which can be used to read {@link XSDSchema} objects from a xsd file. This resource set
    * has extension mappings for: wsdl, xsd and ecore.
    * 
    * @return a resource set which can be used to read xsd's
    * @see XSDResourceFactoryImpl
    * @see EcoreResourceFactoryImpl
    */
   public static ResourceSet getXSDResourceSet(final EPackage.Registry registry) {
     final ResourceSet result = new ResourceSetImpl();
     result.setPackageRegistry(registry);
     result.getLoadOptions().put(XSDResourceImpl.XSD_TRACK_LOCATION, Boolean.TRUE);
     final Map<String, Object> extensionToFactoryMap = result.getResourceFactoryRegistry().getExtensionToFactoryMap();
     extensionToFactoryMap.put("wsdl", new XSDResourceFactoryImpl()); //$NON-NLS-1$
     extensionToFactoryMap.put("xsd", new XSDResourceFactoryImpl()); //$NON-NLS-1$
     extensionToFactoryMap.put("ecore", new EcoreResourceFactoryImpl()); //$NON-NLS-1$
     return result;
   }
 
   /**
    * Convert an EPackage and all its sub epackages to a byte array.
    * 
    * @param ePackage
    *          the EPackage to convert
    * @param packageRegistry
    *          the EPackage.Registry to use. Should normally be the one provided by the
    *          {@link ModelManager#getPackageRegistry()}.
    * @return the EPackage as byte array
    */
   public static byte[] getEPackageAsByteArray(final EPackage ePackage, final EPackage.Registry packageRegistry) {
     try {
       Resource resource = ePackage.eResource();
       if (resource == null) {
         resource = createEcoreResource(ePackage.getNsURI(), packageRegistry);
         resource.getContents().add(ePackage);
       }
 
       final Map<String, Object> options = new HashMap<String, Object>();
       options.put(Resource.OPTION_ZIP, true);
 
       final ByteArrayOutputStream os = new ByteArrayOutputStream();
       resource.save(os, options);
       return os.toByteArray();
     } catch (final Exception e) {
       throw new IllegalStateException(e);
     }
   }
 
   /**
    * Creates an EPackage (and all its sub epackages) from a byte array. The method first checks if the EPackage is
    * already present in the passed registry. If so then the instance from the registry is returned and no conversion
    * from the byte[] array is done.
    * 
    * Note: this method *does not* register the read EPackages in the passed EPackage.Registry!
    * 
    * @param uri
    *          the uri of the EPackage ({@link EPackage#getNsURI()}).
    * @param bytes
    *          the EPackage as byte array
    * @param packageRegistry
    *          the EPackage.Registry to use. Should normally be the one provided by the
    *          {@link ModelManager#getPackageRegistry()}.
    * @return a new EPackage or the one already registered in the packageRegistry.
    */
   public static EPackage createEPackage(final String nsUri, final byte[] bytes, final EPackage.Registry packageRegistry) {
     if (packageRegistry.getEPackage(nsUri) != null) {
       return packageRegistry.getEPackage(nsUri);
     }
 
     try {
       final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
       final Resource resource = createEcoreResource(nsUri, packageRegistry);
 
       final Map<String, Object> options = new HashMap<String, Object>();
       options.put(Resource.OPTION_ZIP, true);
 
       resource.load(bais, options);
 
       final EList<EObject> contents = resource.getContents();
       final EPackage ePackage = (EPackage) contents.get(0);
 
       return ePackage;
     } catch (final Exception e) {
       throw new IllegalStateException(e);
     }
   }
 
   // private void registerSubPackages(final List<EPackage> ePackages,
   // final EPackage.Registry packageRegistry) {
   // for (EPackage ePackage : ePackages) {
   // if (packageRegistry.containsKey(ePackage.getNsURI())) {
   // throw new IllegalStateException("EPackage "
   // + ePackage.getNsURI() + " is already registered");
   // }
   // packageRegistry.put(ePackage.getNsURI(), ePackage);
   // registerSubPackages(ePackage.getESubpackages(), packageRegistry);
   // }
   // }
 
   private static Resource createEcoreResource(final String uri, final EPackage.Registry packageRegistry) {
     final ResourceSet resourceSet = new ResourceSetImpl();
     resourceSet.setPackageRegistry(packageRegistry);
 
     final Resource.Factory resourceFactory = new EcoreResourceFactoryImpl();
     resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", resourceFactory); //$NON-NLS-1$ 
     resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("*", resourceFactory); //$NON-NLS-1$
 
     return resourceSet.createResource(URI.createURI(uri));
   }
 
 }
