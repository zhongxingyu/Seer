 package org.openmrs.module.infopathconverter.web.controller;
 
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.xpath.XPathExpressionException;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lkurian
  * Date: Jun 28, 2010
  * Time: 3:01:08 PM
  * To change this template use File | Settings | File Templates.
  */
 public class InfopathForm {
     private String formName;
     private String content;
 
     public InfopathForm(String formName, String content) {
         this.formName = formName;
         this.content = content;
     }
 
     public Document toPage() throws Exception {
 
         ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes());
         Document rawDocument = XmlDocumentFactory.createXmlDocumentFromStream(stream);
         Node node = extractPageBody(rawDocument);
 
         Document page = XmlDocumentFactory.createEmptyXmlDocument();
         Element pageElement = page.createElement("page");
         pageElement.setAttribute("title", formName);
         page.appendChild(pageElement);
         pageElement.appendChild(page.importNode(node, true));
 
         extractBindings(page);
         return page;
     }
 
     public Node extractPageBody(Document document) throws XPathExpressionException {
 		NodeList matchNodes = XPathUtils.matchNodes(document, "//body");
 		if (matchNodes.getLength() > 0) {
 			return matchNodes.item(0);
 		} else {
 			return document.createElement("body");
 		}
 	}
 
 
     private void extractBindings(Document document) throws XPathExpressionException {
        NodeList nodes = XPathUtils.matchNodes(document, "//*[starts-with(@xd:binding,'patient/')]");
         new PatientRules().apply(document, nodes);
 
     }
 }
