 package pl.edu.agh.yamlconverter;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class WSDLBuilder {
 	private Document doc;
 	
 	public WSDLBuilder createDocument() throws ParserConfigurationException {
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		doc = docBuilder.newDocument();
 		return this;
 	}
 	
 	public WSDLBuilder createRootElement(LinkedHashMap<String, Object> attr) {
 		Element rootElement = doc.createElement("definitions");
 		setAttributes(rootElement, attr);
 		rootElement.setAttribute("xmlns:ns1", "http://schemas.xmlsoap.org/soap/http");
 		rootElement.setAttribute("xmlns:soap", "http://schemas.xmlsoap.org/wsdl/soap/");
 		rootElement.setAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/");
 		rootElement.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
 		doc.appendChild(rootElement);
 		return this;
 	}
 	
 	public WSDLBuilder createTypesElement(LinkedHashMap<String, Object> attr) {
 		Element types = doc.createElement("types");
 		if(attr != null)
 			setAttributes(types, attr);
 		doc.getElementsByTagName("definitions").item(0).appendChild(types);
 		return this;
 	}
 	
 	public WSDLBuilder createSchemaElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("schema");
 		if(attr != null)
 			setAttributes(element, attr);
 		doc.getElementsByTagName("types").item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createComplexTypeElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("complexType");
 		if(attr != null)
 			setAttributes(element, attr);
 		doc.getElementsByTagName("schema").item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createSequenceElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("sequence");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("complexType");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createElementElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("element");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("sequence");
		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createMessageElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("message");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("definitions");
 		nodeList.item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createPartElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("part");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("message");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createPortTypeElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("portType");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("definitions");
 		nodeList.item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createOperationElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("operation");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("portType");
 		nodeList.item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInputElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("input");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("operation");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createOutputElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("output");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("operation");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createBindingElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("wsdl:binding");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("definitions");
 		nodeList.item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInsideBindingElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("soap:binding");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:binding");
 		nodeList.item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createBindingOperationElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("wsdl:operation");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:binding");
 		nodeList.item(0).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInsideBindingOperationElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("soap:operation");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:operation");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInsideInputElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("wsdl:input");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:operation");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInsideInputBodyElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("soap:body");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:input");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInsideOutputElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("wsdl:output");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:operation");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createInsideOutputBodyElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("soap:body");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:output");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createServiceElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("wsdl:service");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("definitions");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createPortElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("wsdl:port");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:service");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public WSDLBuilder createAddressElement(LinkedHashMap<String, Object> attr) {
 		Element element = doc.createElement("soap:address");
 		if(attr != null)
 			setAttributes(element, attr);
 		NodeList nodeList = doc.getElementsByTagName("wsdl:port");
 		nodeList.item(nodeList.getLength()-1).appendChild(element);
 		return this;
 	}
 	
 	public void setAttributes(Element el, LinkedHashMap<String, Object> attr) {
 		Set<String> s = attr.keySet();
 		for(String at : s) {
 			try {
 				el.setAttribute(at, attr.get(at).toString());
 			} catch (NullPointerException e) {
 				el.setAttribute(at, "null");
 			}
 		}
 	}
 	
 	public Document getDocument() {
 		return doc;
 	}
 	
 }
