 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.io.InputStream;
import java.net.MalformedURLException;
 import java.util.Collection;
 import java.util.HashSet;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 public class ViolationParser {
 	private Collection<IViolation> violations;
 
 	public IViolation[] parse(InputStream violationReportStream) {
 		Document document = createDocument(violationReportStream);
 		if (null == document)
 			return new IViolation[] {};
 		return parse(document);
 	}
 
 	private Document createDocument(InputStream stream) {
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			db.setErrorHandler(new ErrorHandler() {
 				public void error(SAXParseException exception) throws SAXException {
 				}
 
 				public void fatalError(SAXParseException exception) throws SAXException {
 				}
 
 				public void warning(SAXParseException exception) throws SAXException {
 				}
 			});
 
 			return db.parse(stream);
 		} catch (Exception e) {
 		}
 		return null;
 	}
 
 	private IViolation[] parse(Document doc) {
 		violations = new HashSet<IViolation>();
 		NodeList nodes = doc.getElementsByTagName("file");
 		for (int i = 0; i < nodes.getLength(); ++i)
 			parse((Element) nodes.item(i));
 		return violations.toArray(new IViolation[violations.size()]);
 	}
 
 	private void parse(Element file) {
 		NodeList node = file.getElementsByTagName("violation");
 		for (int i = 0; i < node.getLength(); ++i)
 			parse((Element) node.item(i), file.getAttribute("name"));
 	}
 
 	private void parse(Element violationElement, String fileName) {
		try {
			IViolation newViolation = new Violation();
			newViolation.setClassName(violationElement.getAttribute("class"));
			newViolation.setPackageName(violationElement.getAttribute("package"));
			newViolation.setMethodName(violationElement.getAttribute("method"));
			newViolation.setFunctionName(violationElement.getAttribute("function"));
			newViolation.setBeginline(Integer.parseInt(violationElement.getAttribute("beginline")));
			newViolation.setEndline(Integer.parseInt(violationElement.getAttribute("endline")));
			newViolation.setRule(violationElement.getAttribute("rule"));
			newViolation.setRuleSet(violationElement.getAttribute("ruleset"));
			newViolation.setPriority(Integer.parseInt(violationElement.getAttribute("priority")));
			newViolation.setExternalInfoURL(violationElement.getAttribute("externalInfoUrl"));
			newViolation.setDescription(violationElement.getTextContent().trim());
			violations.add(newViolation);
		} catch (MalformedURLException e) {
			// ignore the item
		}
 	}
 }
