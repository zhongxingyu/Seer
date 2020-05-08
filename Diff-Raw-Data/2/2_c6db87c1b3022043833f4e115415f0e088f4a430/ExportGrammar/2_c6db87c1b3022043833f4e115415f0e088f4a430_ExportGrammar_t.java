 package de.hszg.atocc.vcc.export.grammar.internal;
 
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 
 public class ExportGrammar extends AbstractXmlTransformationService {
 
     private XmlUtilService xmlUtils;
     private GrammarService grammarUtils;
 
     private Grammar grammar = new Grammar();
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             validateInput("COMPILER");
 
             final NodeList lhsElements = xmlUtils.extractNodes(getInput(), "//RULES/RULE");
 
             for (int i = 0; i < lhsElements.getLength(); ++i) {
                 final Element lhsElement = (Element) lhsElements.item(i);
                 final String lhs = lhsElement.getAttribute("name");
                 
                final NodeList rhsElements = xmlUtils.extractNodes(lhsElement, "RIGHTSIDE");
                 
                 for(int j = 0; j < rhsElements.getLength(); ++j) {
                     final Element rhsElement = (Element) rhsElements.item(j);
                     
                     String rhs = "";
                     
                     final NodeList childNodes = rhsElement.getChildNodes();
                     
                     for(int k = 0; k < childNodes.getLength(); ++k) {
                         final Node child = childNodes.item(k);
                         
                         if(child.getNodeType() == Node.ELEMENT_NODE) {
                             final Element partElement = (Element) child;
                             
                             rhs += partElement.getAttribute("name") + " ";
                         }
                     }
                     
                     grammar.appendRule(lhs, rhs.trim());
                 }
             }
 
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("ExportGrammar|INVALID_INPUT", e);
         } catch (XPathExpressionException e) {
             throw new XmlTransformationException("ExportGrammar|XPATH_ERROR", e);
         }
 
         try {
             setOutput(grammarUtils.grammarToXml(grammar));
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("ExportGrammar|INVALID_OUTPUT", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("ExportGrammar|SERIALIZATION_FAILED", e);
         }
     }
 
     private void tryToGetRequiredServices() {
         xmlUtils = getService(XmlUtilService.class);
         grammarUtils = getService(GrammarService.class);
     }
 }
