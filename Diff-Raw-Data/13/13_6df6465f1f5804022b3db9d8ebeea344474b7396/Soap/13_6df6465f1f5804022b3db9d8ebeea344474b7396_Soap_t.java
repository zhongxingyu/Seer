 package utils;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.traversal.DocumentTraversal;
 import org.w3c.dom.traversal.NodeFilter;
 import org.w3c.dom.traversal.NodeIterator;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.eviware.soapui.SoapUI;
 import com.eviware.soapui.impl.WsdlInterfaceFactory;
 import com.eviware.soapui.impl.wsdl.WsdlInterface;
 import com.eviware.soapui.impl.wsdl.WsdlOperation;
 import com.eviware.soapui.impl.wsdl.WsdlProject;
 import com.eviware.soapui.impl.wsdl.WsdlRequest;
 import com.eviware.soapui.impl.wsdl.WsdlSubmit;
 import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
 import com.eviware.soapui.model.iface.Operation;
 import com.eviware.soapui.model.iface.Response;
 
 public class Soap {
 
 
 	/**
 	 * Sends a request to the webservice specified and returns the response as
 	 * XML string
 	 * 
 	 * @param serviceUrl
 	 * @param method
 	 * @param xmlRequest
 	 * @return
 	 * @throws ServiceException
 	 */
 	public static String sendRequest(String serviceUrl, String method, String xmlRequest) throws ServiceException {
 		String result = null;
 		try {
 
 			SoapUI.setStandalone(true);
 			WsdlProject project = new WsdlProject();
 			project.setTimeout(1500);
			project.setCacheDefinitions(false);
 			
 			if (!Soap.isAvailable(serviceUrl)) {
 				throw new ServiceException("Server not reachable"); 
 			}
 			
 			WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project, serviceUrl, true)[0];
 			WsdlOperation operation = (WsdlOperation) iface.getOperationByName(method);
 			WsdlRequest request = operation.addNewRequest("Request"); 
 			request.setRequestContent(xmlRequest);
 			
 			WsdlSubmit<?> submit = (WsdlSubmit<?>) request.submit(new WsdlSubmitContext(request), false);
 			
 			Response response = submit.getResponse();
 			result = response.getContentAsString();
 			SoapUI.shutdown(); 
 			
 			
 		} catch (Exception e) {
 			throw new ServiceException(e);
 		}
 
 		if (result == null) {
 			throw new ServiceException("Webservice did not response to call.");
 		}
 		
 		return result.toString();
 	}
 	
 	/**
 	 * Returns a Map containing methods names as key
 	 * and request templates as values.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	public static Map<String, String> getRequests(String serviceUrl) throws Exception {
 		SoapUI.setStandalone(true);
 
 		if (!Soap.isAvailable(serviceUrl)) {
 			throw new IOException("Server not reachable");
 		}
 
 		HashMap<String, String> result = new HashMap<>();
 		WsdlProject project = new WsdlProject();
 		project.setTimeout(15);
 		WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, serviceUrl);
 		WsdlInterface wsdl = wsdls[0];
 		wsdl.getEndpoints();
 		for (Operation operation : wsdl.getOperationList()) {
 			WsdlOperation op = (WsdlOperation) operation;
 			result.put(op.getName(), op.createRequest(true));
 		}
 	
 		SoapUI.shutdown();
 
 		return result;
 	}
 	
 	/**
 	 * Returns a Map containing the method name as key, with
 	 * the list of parameters as value.
 	 * 
 	 * @param serviceUrl
 	 * @return
 	 * @throws Exception
 	 */
 	public static Map<String, ArrayList<String>> getMethods(String serviceUrl) throws Exception {
 		Map <String, String> requests = getRequests(serviceUrl);
 		Map<String, ArrayList<String>> result = new HashMap<>();
 		for (String method : requests.keySet()) {
 			ArrayList<String> tmp = (ArrayList<String>) getParams(requests.get(method));
 			result.put(method, tmp);
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns a list of parameters for a particular method
 	 * 
 	 * @param methodRequest
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private static List<String> getParams(String methodRequest) throws ParserConfigurationException, SAXException, IOException {
 		ArrayList<String> result = new ArrayList<>(); 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder loader = factory.newDocumentBuilder();
 		Document document = loader.parse(new InputSource(new StringReader(methodRequest)));
 
 		DocumentTraversal traversal = (DocumentTraversal) document;
 
 		NodeIterator iterator = traversal.createNodeIterator(document.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
 
 		for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
 			Element element = ((Element) n);
 			if (element.getTextContent().equals("?")) {
 				result.add(element.getTagName());
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Checks whether the given URL is available
 	 * @param url
 	 * @return
 	 */
 	public static boolean isAvailable(String url) {
 		HttpURLConnection connection;
 		try {
 			connection = (HttpURLConnection) new URL(url).openConnection();
 			connection.setRequestMethod("HEAD");
 			int responseCode = connection.getResponseCode();
 			if (responseCode != 200) {
 				return true;
 			}
 		} catch (ProtocolException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	
 	/**
 	 * Generates a WSDL document for the given service and appends the
 	 * id to the resilient service for identification
 	 * 
 	 * @param wsdl
 	 * @param id
 	 * @return
 	 * @throws Exception
 	 */
 	public static String generateResilientWSDL(String wsdl, Long id) throws Exception {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		URL baseWSDL = new URL("http://localhost:8080/resilientWebservice/service?wsdl");
 		Document baseDoc = builder.parse(new InputSource(baseWSDL.openStream()));
 
 		ByteArrayInputStream sourceWSDL = new ByteArrayInputStream(wsdl.getBytes());
 		Document targetDoc = builder.parse(new InputSource(sourceWSDL));
 
 		NamedNodeMap attrs = targetDoc.getDocumentElement().getAttributes();
 
 		/* Find wsdl / soap prefix if any */
 		String wsdl_ns = "http://schemas.xmlsoap.org/wsdl";
 		String wsdl_prefix = "";
 
 		for (int i = 0; i < attrs.getLength(); i++) {
 			Node node = attrs.item(i);
 			if (node.getNodeValue().startsWith(wsdl_ns) && node.getNodeName().startsWith("xmlns:")) {
 				String[] tmp = node.getNodeName().split(":");
 				wsdl_prefix = tmp[1] + ":";
 			}
 		}
 		
 		// Make sure we have the tns prefix for the target namespace
 		if (attrs.getNamedItem("xmlns:tns") == null && attrs.getNamedItem("targetNamespace") != null) {
 			targetDoc.getDocumentElement().setAttribute("xmlns:tns", attrs.getNamedItem("targetNamespace").getNodeValue());
 		}
 
 		// Add our namespace prefix
 		targetDoc.getDocumentElement().setAttribute("xmlns:rws", "http://rws.tuwien.ac.at/DigitalPreservation");
 		
 		// Add new schema to types
 		Node types = targetDoc.getElementsByTagName(wsdl_prefix.isEmpty() ? "types" : wsdl_prefix + "types").item(0);
 		if (types == null) {
 			types = targetDoc.getElementsByTagName("types").item(0);
 		}
 		Node newSchema =  baseDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema").item(0);
 		types.appendChild(targetDoc.adoptNode(newSchema.cloneNode(true)));
 		
 		// Copy messages
 		Node firstMessage = targetDoc.getElementsByTagName(wsdl_prefix.isEmpty() ? "message" : wsdl_prefix + "message").item(0);
 		if (firstMessage == null) {
 			firstMessage = targetDoc.getElementsByTagName("message").item(0);
 		}
 		NodeList nl = baseDoc.getElementsByTagName("message");
 
 		for(int i = 0; i < nl.getLength(); i++) {
 			Node newNode = targetDoc.adoptNode(nl.item(i).cloneNode(true));
 			NodeList sub = newNode.getChildNodes();
 			// We need to replace the tns: prefix with our own
 			for (int j = 0; j < sub.getLength(); j++) {
 				if (sub.item(j).getNodeType() != Node.ELEMENT_NODE) {
 					continue;
 				}
 				attrs = sub.item(j).getAttributes();
 				for (int k = 0; k < attrs.getLength(); k++) {
 					if (attrs.item(k).getNodeValue().startsWith("tns:")) {
 						attrs.item(k).setNodeValue(attrs.item(k).getNodeValue().replaceAll("tns:", "rws:"));
 					}
 				}
 			}
 			targetDoc.getDocumentElement().insertBefore(newNode, firstMessage);
 		}
 		
 		// Copy portType
 		Node oldPortType = targetDoc.getElementsByTagName(wsdl_prefix.isEmpty() ? "portType" : wsdl_prefix + "portType").item(0);
 		if (oldPortType == null) {
 			oldPortType = targetDoc.getElementsByTagName("portType").item(0);
 		}
 		Node newPortType = baseDoc.getElementsByTagName("portType").item(0);
 		oldPortType.getParentNode().insertBefore(targetDoc.adoptNode(newPortType.cloneNode(true)), oldPortType);
 
 		// Copy binding
 		Node oldBinding = targetDoc.getElementsByTagName(wsdl_prefix.isEmpty() ? "binding" : wsdl_prefix + "binding").item(0);
 		if (oldBinding == null) {
 			oldBinding = targetDoc.getElementsByTagName("binding").item(0);
 		}
 		// We need the topmost binding element
 		while (!oldBinding.getParentNode().isSameNode(targetDoc.getDocumentElement())) {
 			oldBinding = oldBinding.getParentNode();
 		}
 		Node newBinding = baseDoc.getElementsByTagName("binding").item(0);
 		oldBinding.getParentNode().insertBefore(targetDoc.adoptNode(newBinding.cloneNode(true)), oldBinding);
 		
 		// Add new schema to types
 		Node service = targetDoc.getElementsByTagName(wsdl_prefix.isEmpty() ? "service" : wsdl_prefix + "service").item(0);
 		if (service == null) {
 			service = targetDoc.getElementsByTagName("service").item(0);
 		}
 		Node newPort =  baseDoc.getElementsByTagName("port").item(0);
 		nl = newPort.getChildNodes();
 		// Append ID to service URL
 		for (int i = 0; i < nl.getLength(); i++) {
 			if (nl.item(i).getNodeName().equals("soap:address")) {
 				Element address = (Element) nl.item(i);
 				address.setAttribute("location", address.getAttributes().getNamedItem("location").getNodeValue() + "?" + id);
 			}
 		}
 		service.appendChild(targetDoc.adoptNode(newPort.cloneNode(true)));
 		
 		TransformerFactory tFactory = TransformerFactory.newInstance();
 		Transformer transformer = tFactory.newTransformer();
 
 		ByteArrayOutputStream output = new ByteArrayOutputStream();
 		
 		DOMSource source = new DOMSource(targetDoc);
 		StreamResult result = new StreamResult(output);
 		transformer.transform(source, result);
 		
 		return output.toString();
 	}
 	
 	/**
 	 * Downloads the WSDL file and returns a string
 	 * 
 	 * @param service
 	 * @return
 	 * @throws IOException
 	 */
 	public static String downloadWSDL(String service) throws IOException {
 		URL url = new URL(service);
         URLConnection connection = url.openConnection();
         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
         StringBuilder response = new StringBuilder();
         String inputLine;
 
         while ((inputLine = in.readLine()) != null) { 
             response.append(inputLine);
         }
 
         in.close();
         
         return response.toString();
 	}
 	
 }
