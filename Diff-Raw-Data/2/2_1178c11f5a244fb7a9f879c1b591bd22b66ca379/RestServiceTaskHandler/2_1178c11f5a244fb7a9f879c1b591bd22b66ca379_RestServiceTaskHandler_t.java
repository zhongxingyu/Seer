 package org.wiredwidgets.cow.server.manager;
 
 import java.io.ByteArrayInputStream;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.process.WorkItem;
 import org.drools.runtime.process.WorkItemHandler;
 import org.drools.runtime.process.WorkItemManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpMethod;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 public class RestServiceTaskHandler implements WorkItemHandler {
 
 	private static Logger log = Logger.getLogger(RestServiceTaskHandler.class);
 
 	@Autowired
 	RestTemplate restTemplate; 
 	 
 	
 	@Override
 	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {	
 		// TODO Auto-generated method stub
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
 		log.info("Work item: " + item.getName());
 
 		
 		for (Entry<String, Object> entry : item.getParameters().entrySet()) {
 			log.info(entry.getKey() + ":" + entry.getValue());
 		}
 
 		String contentPattern = (String) item.getParameter("content");
 		String var = (String) item.getParameter("var");
 		String urlPattern = (String) item.getParameter("url");
 		String method = (String) item.getParameter("method"); 
 		String xpath = (String) item.getParameter("resultSelectorXPath");
 		
 		Object variableObj = item.getParameter("Variables");
 		Map<String, Object> variables;
 		if (variableObj instanceof Map) {
 			variables = (Map<String, Object>) variableObj;
 		}
 		else {
 			variables = new HashMap<String, Object>();
 		}
 
 		String result = "";
 		String url = replaceVariables(urlPattern, variables);
 		
 		if (method.equalsIgnoreCase(HttpMethod.GET.name())) {                  
 			result = restTemplate.getForObject(url, String.class);
 			log.info("GET result: " + result);
 		} 
 		else if (method.equalsIgnoreCase(HttpMethod.POST.name())) {            
 			try {
 				// this method expects XML content in the response.  if none if found an exception is thrown
 				String content = replaceVariables(contentPattern, variables);
 				result = restTemplate.postForObject(url, content, String.class);
 				log.info("POST result: " + result);
 			}
 			catch (RestClientException e) {
 				log.error(e);
 			}
 		}  
 		
 		try {
 			result = evalXpath(xpath, result);
 		} catch (Exception e) {
 			log.info("xpath evaluation failed, returning entire response");
 		}
 		
 		Map<String, Object> outputMap = new HashMap<String, Object>();
 		outputMap.put("Variables", variables);
 		// update the result variable, if specified
 		
 		if (var != null && !var.trim().equals("")) {  	
 			variables.put(var, result);
 		}   
 	
 		manager.completeWorkItem(item.getId(), outputMap);		
 	}
 
 
 	
	private static Pattern regexPattern = Pattern.compile("\\$\\{(.*?)\\}");
 	
 	private static String replaceVariables(String format, Map<String, Object> vars) {	
 		Matcher matcher = regexPattern.matcher(format);
 		
 		StringBuffer result = new StringBuffer();
 		while(matcher.find()) {
 			String varName = matcher.group(1);
 			String varVal = "";
 			if (vars.containsKey(varName)) {
 				varVal = vars.get(varName).toString();
 			}
 			matcher.appendReplacement(result, varVal);
 		}
 		matcher.appendTail(result);
 		return result.toString();
 	}
 	
 	
 	private static String evalXpath(String xpathExpr, String content) throws Exception {
 		
 		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
 		
 		DocumentBuilder builder = builderFactory.newDocumentBuilder();
 		Document document = builder.parse(new ByteArrayInputStream(content.getBytes()));
 		renameNamespaceRecursive(document, "");
 		
 		XPath xPath =  XPathFactory.newInstance().newXPath();
 		
 		Node node = (Node) xPath.compile(xpathExpr).evaluate(document, XPathConstants.NODE);	
 		return nodeToString(node);
 	}
 	
 	
 	private static String nodeToString(Node node) {
 		StringWriter sw = new StringWriter();
 		try {
 			Transformer t = TransformerFactory.newInstance().newTransformer();
 			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			t.setOutputProperty(OutputKeys.INDENT, "yes");
 			t.transform(new DOMSource(node), new StreamResult(sw));
 		} catch (TransformerException te) {
 			System.out.println("nodeToString Transformer Exception");
 		}
 		return sw.toString();
 	}
 	
 	
 	private static void renameNamespaceRecursive(Node node, String namespace) {
 	    Document document = node.getOwnerDocument();
 	    if (node.getNodeType() == Node.ELEMENT_NODE) {
 	        document.renameNode(node, namespace, node.getNodeName());
 	    }
 	    NodeList list = node.getChildNodes();
 	    for (int i = 0; i < list.getLength(); ++i) {
 	        renameNamespaceRecursive(list.item(i), namespace);
 	    }
 	}
 
 
 
 }
