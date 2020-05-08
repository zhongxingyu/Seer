 package mx.itesm.model2roo;
 
 import java.beans.Introspector;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.jdom.xpath.XPath;
 
 /**
  * Convert UML Profile data to Ecore EAnnotation data.
  * 
  * @author jccastrejon
  * 
  */
 public class UmlProfile2Ecore {
 
     /**
      * Mapping between Uml and Ecore types.
      */
     private static Map<String, String> uml2EcoreTypes;
 
     /**
     * List of Ecore types for which the UML2 transformation decapitalizes names.
      */
     private static List<String> decapitalizedEcoreTypes;
 
     static {
         uml2EcoreTypes = new HashMap<String, String>();
         uml2EcoreTypes.put("Model", "ecore:EPackage");
         uml2EcoreTypes.put("Class", "eClassifiers");
         uml2EcoreTypes.put("Property", "eStructuralFeatures");
         uml2EcoreTypes.put("Enumeration", "eClassifiers");
         uml2EcoreTypes.put("EnumerationLiteral", "eLiterals");
 
         decapitalizedEcoreTypes = new ArrayList<String>();
        decapitalizedEcoreTypes.add("EPackage");
     }
 
     /**
      * Transform the UML Profiles applied in the specified UML file, into
      * corresponding EAnnotations in the specified Ecore file.
      * 
      * @param ecoreFile
      * @param umlFile
      * @throws JDOMException
      * @throws IOException
      */
     public static void transformUmlProfiles(final File ecoreFile, final File umlFile) throws JDOMException, IOException {
         XMLOutputter out;
         Document umlDocument;
         SAXBuilder saxBuilder;
         Document ecoreDocument;
 
         out = new XMLOutputter();
         saxBuilder = new SAXBuilder();
         umlDocument = saxBuilder.build(umlFile);
         ecoreDocument = saxBuilder.build(ecoreFile);
 
         for (String umlType : UmlProfile2Ecore.uml2EcoreTypes.keySet()) {
             UmlProfile2Ecore.associateStereotypes(ecoreDocument, umlDocument, umlType,
                             UmlProfile2Ecore.uml2EcoreTypes.get(umlType));
         }
 
         out.output(ecoreDocument, new FileWriter(ecoreFile));
     }
 
     /**
      * 
      * @param ecoreDocument
      * @param umlDocument
      * @param umlType
      * @param ecoreType
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     private static void associateStereotypes(final Document ecoreDocument, final Document umlDocument,
                     final String umlType, final String ecoreType) throws JDOMException {
         boolean associate;
         String parentName;
         XPath umlTypePath;
         String elementName;
         Element umlElement;
         XPath ecoreTypePath;
         Element parentElement;
         XPath umlStereotypesPath;
         List<Element> stereotypes;
         List<Element> ecoreElements;
 
         umlStereotypesPath = XPath.newInstance("//*[@base_" + umlType + "]");
         stereotypes = umlStereotypesPath.selectNodes(umlDocument);
         if (stereotypes != null) {
             for (Element stereotype : stereotypes) {
                 umlTypePath = XPath.newInstance("//*[@xmi:id='" + stereotype.getAttributeValue("base_" + umlType)
                                 + "']");
                 umlElement = (Element) umlTypePath.selectSingleNode(umlDocument);
                 parentName = umlElement.getParentElement().getAttributeValue("name");
 
                 // Possible Ecore elements associated to the Uml element
                 elementName = umlElement.getAttributeValue("name");
                if (UmlProfile2Ecore.decapitalizedEcoreTypes.contains(elementName)) {
                     elementName = Introspector.decapitalize(elementName);
                 }
                 ecoreTypePath = XPath.newInstance("//" + ecoreType + "[@name='" + elementName + "']");
 
                 // The right Ecore element should not only have the appropriate
                 // name, but also the appropriate parent
                 associate = false;
                 ecoreElements = ecoreTypePath.selectNodes(ecoreDocument);
                 for (Element ecoreElement : ecoreElements) {
                     associate = true;
                     parentElement = ecoreElement.getParentElement();
                     if ((parentElement != null) && (parentElement.getAttributeValue("name").equals(parentName))) {
                         associate = true;
                     }
 
                     // Ecore element found
                     if (associate) {
                         UmlProfile2Ecore.associateStereotype(ecoreElement, umlElement, stereotype);
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * 
      * @param ecoreElement
      * @param umlElement
      * @param umlStereotype
      */
     @SuppressWarnings("unchecked")
     private static void associateStereotype(final Element ecoreElement, final Element umlElement,
                     final Element umlStereotype) {
         Element detail;
         Element annotation;
 
         annotation = new Element("eAnnotations");
         annotation.setAttribute("source", umlStereotype.getNamespacePrefix() + ":" + umlStereotype.getName());
 
         for (Attribute attribute : (List<Attribute>) umlStereotype.getAttributes()) {
             if ((attribute.getNamespaceURI().equals("")) && (!attribute.getName().startsWith("base_"))) {
                 detail = new Element("details");
                 annotation.addContent(detail);
                 detail.setAttribute("key", attribute.getName());
                 detail.setAttribute("value", attribute.getValue());
             }
         }
 
         ecoreElement.removeContent(RooAnnotationFilter.getFilter(""));
         ecoreElement.addContent(annotation);
     }
 }
