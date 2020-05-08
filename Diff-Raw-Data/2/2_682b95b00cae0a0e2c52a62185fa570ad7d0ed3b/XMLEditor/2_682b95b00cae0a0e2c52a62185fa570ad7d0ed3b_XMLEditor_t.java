 package org.investovator.controller.config;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 
 import org.w3c.dom.*;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * Class fir manipulating XML
  */
 public class XMLEditor {
 
     /**
      * Add Elements to document replacing attributes which has variable value given in replacements keys and replaces with its value.
      * @param original
      * @param replacements
      * @param parent
      */
     public static void addElementReplacingAttributes(Element original, HashMap<String, String> replacements, Document outputDoc ,Element parent){
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         Element importedElement =  (Element) outputDoc.importNode(original,true);
         parent.appendChild(importedElement);
 
 
         Iterator test =  replacements.keySet().iterator();
         while (test.hasNext()) {
             String next = test.next().toString();
 
             String agentExpr = String.format("//@*[.=\"%s\"]",next);
 
             try {
                 Attr attribute = (Attr) xpath.evaluate(agentExpr,importedElement, XPathConstants.NODE);
 
                 attribute.setValue(replacements.get(next));
 
 
 
             } catch (XPathExpressionException e) {
                 e.printStackTrace();
             }
         }
     }
 
 
     public static void replacePlaceHolder(Node source, String placeholder, String replacement){
 
         XPath xpath = XPathFactory.newInstance().newXPath();
 
         String xPathExpressionAttr = "//@*";
 
         NodeList nodesAttr = null;
         try {
             nodesAttr = (NodeList) xpath.evaluate(xPathExpressionAttr, source, XPathConstants.NODESET);
         } catch (XPathExpressionException e) {
             e.printStackTrace();
         }
 
         String resultAtt ;
 
         for(int i=0; i<nodesAttr.getLength(); i++) {
             resultAtt = nodesAttr.item(i).getTextContent();
 
             if(resultAtt.contains(placeholder)){
                 resultAtt = resultAtt.replace("$stockID",replacement);
                 nodesAttr.item(i).setTextContent(resultAtt);
             }
         }
 
     }
 
 
     public static void replacePlaceholderElement(String placeholderName, Document doc, Element[] replacements){
 
         XPath xpath = XPathFactory.newInstance().newXPath();
                                    String xPathExpressionAttr = "//" + placeholderName;
 
         try {
             Node reportListElement = (Node) xpath.evaluate(xPathExpressionAttr, doc, XPathConstants.NODE);
 
             Element parent = (Element) reportListElement.getParentNode();
 
             for (Element element : replacements) {
                 parent.appendChild(element);
             }
 
             parent.removeChild(reportListElement);
 
         } catch (XPathExpressionException e) {
             e.printStackTrace();
        } catch (NullPointerException e){

         }
 
     }
 
 
     public static void replacePlaceholderElement(String placeholderName, Element doc, Element[] replacements){
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         String xPathExpressionAttr = "//" + placeholderName;
 
         try {
             Node reportListElement = (Node) xpath.evaluate(xPathExpressionAttr, doc, XPathConstants.NODE);
 
             Element parent = (Element) reportListElement.getParentNode();
 
             for (Element element : replacements) {
                 parent.appendChild(element);
             }
 
             parent.removeChild(reportListElement);
 
         } catch (XPathExpressionException e) {
             e.printStackTrace();
         }
 
     }
 
 
     public static void replacePlaceholderElement(String placeholderName, Element doc, String replacement){
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         String xPathExpressionAttr = "//" + placeholderName;
 
         try {
             Node reportListElement = (Node) xpath.evaluate(xPathExpressionAttr, doc, XPathConstants.NODE);
 
             reportListElement.getParentNode().setTextContent(replacement);
 
         } catch (XPathExpressionException e) {
             e.printStackTrace();
         }
 
     }
 
     public static Element createImportElement(Document sourceDoc, String fileName){
 
         Element importElement = sourceDoc.createElement("import");
         NamedNodeMap attribs = importElement.getAttributes();
         Attr bean = sourceDoc.createAttribute("resource");
         bean.setValue(fileName);
         attribs.setNamedItem(bean);
         return importElement;
     }
 
 
     public static Element createControllerElement(Document sourceDoc, String fileName){
 
         Element importElement = sourceDoc.createElement("ref");
         NamedNodeMap attribs = importElement.getAttributes();
         Attr bean = sourceDoc.createAttribute("bean");
         bean.setValue(fileName);
         attribs.setNamedItem(bean);
         return importElement;
     }
 
 
     public static Element createAgentElement(Document sourceDoc, String fileName){
 
         Element importElement = sourceDoc.createElement("ref");
         NamedNodeMap attribs = importElement.getAttributes();
         Attr bean = sourceDoc.createAttribute("bean");
         bean.setValue(fileName);
         attribs.setNamedItem(bean);
         return importElement;
     }
 
 
     public static Element createPropertyFileElement(Document sourceDoc, String propFileName){
 
         Element importElement = sourceDoc.createElement("value");
         importElement.setTextContent(propFileName);
         return importElement;
     }
 
 }
