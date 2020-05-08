 package mx.itesm.model2roo;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.jdom.xpath.XPath;
 
 /**
  * 
  * @author jccastrejon
  * 
  */
 public class Ecore2RooAnnotatedEcore {
 
     /**
      * Mapping between Uml and Ecore types.
      */
     private static Map<String, String[]> uml2EcoreTypes;
 
     /**
      * Mapping between Ecore and Roo types.
      */
     private static Map<String, String> ecore2RooTypes;
 
     /**
      * Ecore types that don't have an associated type name.
      */
     private static List<String> unnamedEcoreTypes;
 
     /**
      * Top Ecore types that can be annotated.
      */
     private static List<String> topEcoreTypes;
 
     /**
      * XMI namespace.
      */
     private static Namespace XMI_NAMESPACE = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema-instance");
 
     static {
         uml2EcoreTypes = new HashMap<String, String[]>();
         uml2EcoreTypes.put("Model", new String[] { "ecore:EPackage" });
         uml2EcoreTypes.put("Class", new String[] { "EClass" });
         uml2EcoreTypes.put("Property", new String[] { "EAttribute", "EReference" });
         uml2EcoreTypes.put("Enumeration", new String[] { "EEnum" });
         uml2EcoreTypes.put("EnumerationLiteral", new String[] { "eLiterals" });
 
         ecore2RooTypes = new HashMap<String, String>();
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString", "String");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EBoolean", "Boolean");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EBooleanObject", "Boolean");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EByte", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EByteObject", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EFloat", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EFloatObject", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EShort", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EShortObject", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EDate", "Date");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//ELong", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//ELongObject", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EIntegerObject", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EDouble", "Number");
         ecore2RooTypes.put("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EDoubleObject", "Number");
 
         unnamedEcoreTypes = new ArrayList<String>();
         unnamedEcoreTypes.add("ecore:EPackage");
         unnamedEcoreTypes.add("eLiterals");
 
         topEcoreTypes = new ArrayList<String>();
         topEcoreTypes.add("ecore:EClass");
         topEcoreTypes.add("ecore:EEnum");
     }
 
     /**
      * 
      * @param ecoreFile
      * @param rooEcoreFiles
      * @throws JDOMException
      * @throws IOException
      */
     public static void annotateEcore(final File ecoreFile, final File... rooEcoreFiles) throws JDOMException,
                     IOException {
         InputStream[] inputStreams;
 
         inputStreams = null;
         if (rooEcoreFiles != null) {
             inputStreams = new InputStream[rooEcoreFiles.length];
             for (int i = 0; i < inputStreams.length; i++) {
                 inputStreams[i] = new BufferedInputStream(new FileInputStream(rooEcoreFiles[i]));
             }
         }
 
         Ecore2RooAnnotatedEcore.annotateEcore(ecoreFile, inputStreams);
     }
 
     /**
      * 
      * @param ecoreFile
      * @param rooEcoreFiles
      * @throws IOException
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     public static void annotateEcore(final File ecoreFile, final InputStream... rooEcoreFiles) throws JDOMException,
                     IOException {
         Element parent;
         XMLOutputter out;
         XPath elementsPath;
         String profileName;
         String elementType;
         Document rooDocument;
         SAXBuilder saxBuilder;
         Element currentDetail;
         Element parentElement;
         Document ecoreDocument;
         Element ecoreAnnotation;
         String currentDetailName;
         List<Element> rooElements;
         List<Element> elementDetails;
 
         if (rooEcoreFiles != null) {
             saxBuilder = new SAXBuilder();
             ecoreDocument = saxBuilder.build(ecoreFile);
 
             // Process each Roo ecore profile
             for (InputStream rooEcoreFile : rooEcoreFiles) {
                 rooDocument = saxBuilder.build(rooEcoreFile);
                 profileName = rooDocument.getRootElement().getAttributeValue("name");
 
                // Identify Roo elements that should be converted into
                // EAnnotations
                 elementsPath = XPath.newInstance("//eClassifiers");
                 rooElements = elementsPath.selectNodes(rooDocument);
                 if (rooElements != null) {
                     for (Element rooElement : rooElements) {
                         elementType = null;
 
                         // EAnnotation general data
                         ecoreAnnotation = new Element("eAnnotations");
                         ecoreAnnotation.setAttribute("source", profileName + ":" + rooElement.getAttributeValue("name"));
 
                        // A detail element should be generated for each
                        // associated structural feature
                         elementDetails = (List<Element>) rooElement.getChildren("eStructuralFeatures");
                         elementDetails = new ArrayList<Element>(elementDetails);
                         elementDetails.addAll(Ecore2RooAnnotatedEcore.getParentDetails(rooElement, rooDocument));
                         parent = getParentAnnotationElement(rooElement, rooDocument);
 
                         for (Element structuralFeature : elementDetails) {
                             currentDetailName = structuralFeature.getAttributeValue("name");
 
                             if (!currentDetailName.startsWith("base")) {
                                 currentDetail = new Element("details");
                                 ecoreAnnotation.addContent(currentDetail);
                                 currentDetail.setAttribute("key", currentDetailName);
                                 currentDetail.setAttribute("value", "");
                             } else {
                                 elementType = Ecore2RooAnnotatedEcore.getElementType(structuralFeature);
                             }
                         }
 
                        // If no associated Ecore element type could be found,
                        // may we'll find one in the element hierarchy
                         if (elementType == null) {
                             parentElement = Ecore2RooAnnotatedEcore.getParentAnnotationElement(rooElement, rooDocument);
                             parentSearch: while (parentElement != null) {
                                 for (Element structuralFeature : (List<Element>) parentElement
                                                 .getChildren("eStructuralFeatures")) {
                                     currentDetailName = structuralFeature.getAttributeValue("name");
                                     if (currentDetailName.startsWith("base")) {
                                         elementType = Ecore2RooAnnotatedEcore.getElementType(structuralFeature);
                                         break parentSearch;
                                     }
                                 }
                                parentElement = Ecore2RooAnnotatedEcore.getParentAnnotationElement(parentElement,
                                                rooDocument);
                             }
                         }
 
                         // Annotate the corresponding Ecore elements
                         Ecore2RooAnnotatedEcore.annotateElements(elementType, ecoreAnnotation, ecoreDocument, parent,
                                         profileName);
                     }
 
                     out = new XMLOutputter();
                     out.output(ecoreDocument, new FileWriter(ecoreFile));
                 }
             }
         }
     }
 
     /**
      * Annotate the ecoreDocument's elements of the corresponding elementType
      * with the specified ecoreAnnotation.
      * 
      * @param elementType
      * @param ecoreAnnotation
      * @param ecoreDocument
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     private static void annotateElements(final String elementType, final Element ecoreAnnotation,
                     final Document ecoreDocument, final Element parent, final String profileName) throws JDOMException {
         boolean annotate;
         String ecoreType;
         String ecoreEType;
         XPath elementsPath;
         Set<String> supportedEcoreTypes;
         List<Element> elementsToAnnotate;
         if (elementType != null) {
             // Look for possible Ecore elements that could be annotated
             for (String currentEcoreType : Ecore2RooAnnotatedEcore.uml2EcoreTypes.get(elementType)) {
                 // The Ecore type is contained as an attribute
                 if (Character.isUpperCase(currentEcoreType.charAt(0))) {
                     elementsPath = XPath.newInstance("//*[@xsi:type='ecore:" + currentEcoreType + "']");
                     elementsToAnnotate = elementsPath.selectNodes(ecoreDocument);
                 }
 
                 // The Ecore type is the element's name
                 else {
                     elementsPath = XPath.newInstance("//" + currentEcoreType);
                     elementsToAnnotate = elementsPath.selectNodes(ecoreDocument);
                 }
 
                 // Associate the annotation to the corresponding elements
                 if (elementsToAnnotate != null) {
                     supportedEcoreTypes = Ecore2RooAnnotatedEcore.ecore2RooTypes.keySet();
                     for (Element elementToAnnotate : elementsToAnnotate) {
                         annotate = false;
                         ecoreType = elementToAnnotate.getAttributeValue("type", Ecore2RooAnnotatedEcore.XMI_NAMESPACE);
                         ecoreEType = elementToAnnotate.getAttributeValue("eType");
 
                         // References and Sets
                         if (ecoreType != null) {
                             // Top Ecore elements
                             if (Ecore2RooAnnotatedEcore.topEcoreTypes.contains(ecoreType)) {
                                 annotate = true;
                             } else if (ecoreType.endsWith("Reference")) {
                                 if (Boolean.parseBoolean(elementToAnnotate.getAttributeValue("containment"))) {
                                     if (ecoreAnnotation.getAttributeValue("source").endsWith("Set")) {
                                         annotate = true;
                                     }
                                 } else if (ecoreAnnotation.getAttributeValue("source").endsWith("Reference")) {
                                     annotate = true;
                                 }
                             }
                         }
 
                        if ((!annotate) && (ecoreEType != null)) {
                             // Enum attributes
                             if (ecoreEType.startsWith("#")
                                             && (ecoreAnnotation.getAttributeValue("source").endsWith("Enum"))) {
                                 annotate = true;
                             }
 
                             // Simple attributes
                             else {
                                 // Specific supported types
                                 if ((supportedEcoreTypes.contains(ecoreEType))
                                                 && ((ecoreAnnotation.getAttributeValue("source")
                                                                 .endsWith(Ecore2RooAnnotatedEcore.ecore2RooTypes
                                                                                 .get(ecoreEType))))) {
                                     annotate = true;
                                 }
                             }
                         }
                         // Enum constants
                         else if (Ecore2RooAnnotatedEcore.unnamedEcoreTypes.contains(currentEcoreType)) {
                             annotate = true;
                         }
 
                         // Only annotate if the element was a valid one
                         if (annotate) {
                             elementToAnnotate.removeContent(RooAnnotationFilter.getFilter(profileName));
                             elementToAnnotate.addContent((Element) ecoreAnnotation.clone());
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Get the super type of the corresponding annotation element. TODO: More
      * than one parent?
      * 
      * @param annotation
      * @param rooDocument
      * @return
      * @throws JDOMException
      */
     private static Element getParentAnnotationElement(final Element annotation, final Document rooDocument)
                     throws JDOMException {
         String parent;
         XPath parentPath;
         Element returnValue;
 
         returnValue = null;
         parent = annotation.getAttributeValue("eSuperTypes");
         if (parent != null) {
             parent = parent.replace("#//", "");
             parentPath = XPath.newInstance("//*[@name='" + parent + "']");
             returnValue = (Element) parentPath.selectSingleNode(rooDocument);
         }
 
         return returnValue;
     }
 
     /**
      * Get the associated Ecore element type.
      * 
      * @param structuralFeature
      * @return
      */
     private static String getElementType(final Element structuralFeature) {
         String returnValue;
 
         returnValue = structuralFeature.getAttributeValue("eType");
         returnValue = returnValue.replace("ecore:EClass uml.ecore#//", "");
 
         return returnValue;
     }
 
     /**
      * 
      * @param element
      * @param rooDocument
      * @return
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     private static List<Element> getParentDetails(final Element element, final Document rooDocument)
                     throws JDOMException {
         Element parent;
         List<Element> returnValue;
 
         parent = Ecore2RooAnnotatedEcore.getParentAnnotationElement(element, rooDocument);
         if (parent != null) {
             returnValue = parent.getChildren("eStructuralFeatures");
         } else {
             returnValue = new ArrayList<Element>(0);
         }
 
         return returnValue;
     }
 }
