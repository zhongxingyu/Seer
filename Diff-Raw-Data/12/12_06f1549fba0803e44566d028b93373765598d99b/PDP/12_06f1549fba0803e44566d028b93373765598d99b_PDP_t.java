 /**
  * 
  */
 package org.ccci.gcx.authorization;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Daniel Frett
  *
  */
 public class PDP {
 
 	private HttpClient client;
 	private DocumentBuilder XMLParser;
 	private XPath xpathEngine;
 	private String gcxServerRoot;
 
 	/**
 	 * 
 	 */
 	public PDP() {
 		this.client = new HttpClient();
 		this.gcxServerRoot = "https://www.mygcx.org";
 		try {
 			//create the XML parser
 			DocumentBuilderFactory XMLParserFactory = DocumentBuilderFactory.newInstance();
 			XMLParserFactory.setCoalescing(true);
 			this.XMLParser = XMLParserFactory.newDocumentBuilder();
 
 			//create the XPath engine for use with the XML parser
 			XPathFactory xpathFactory = XPathFactory.newInstance();
 			this.xpathEngine = xpathFactory.newXPath();
 		}
 		catch(Exception e) {
 		}
 	}
 
 	/**
 	 * @param entity authorization entity to check authorization for
 	 * @param target authorization target to check authorization for
 	 * @return boolean indicating whether entity has access to target, defaults to false if an error is encountered
 	 */
 	public boolean check(String entity, String target) {
 
 		//generate POST request
 		String requestXML = "<?xml version='1.0' encoding='UTF-8'?><auth_question><entity name='" + entity + "'><target name='" + target + "'/></entity></auth_question>";
 		PostMethod request = new PostMethod(this.gcxServerRoot + "/system/authz");
 		request.addParameter("auth_question", requestXML);
 
 		//try executing the request and parsing the response
 		try {
 			//execute the request
 			this.client.executeMethod(request);
 
 			//parse the XML response to a Document object
 			Document responseDOM = this.XMLParser.parse(request.getResponseBodyAsStream());
 
 			//locate all entity nodes
 			NodeList entities = (NodeList) this.xpathEngine.evaluate("/auth_answer/entity", responseDOM.getDocumentElement(), XPathConstants.NODESET);
 
 			//iterate over all found entity nodes
 			for(int x = 0; x < entities.getLength(); x++) {
 
 				//find the correct entity node
 				if(this.xpathEngine.evaluate("@name", entities.item(x)).equalsIgnoreCase(entity)) {
 					//locate all target nodes
 					NodeList targets = (NodeList) this.xpathEngine.evaluate("target", entities.item(x), XPathConstants.NODESET);
 					//iterate over all target nodes
 					for(int y = 0; y < targets.getLength(); y++) {
 						//is this the requested target?
 						if(this.xpathEngine.evaluate("@name", targets.item(y)).equalsIgnoreCase(target)) {
 							//test if the entity is authorized for this target
 							return (Boolean) this.xpathEngine.evaluate(". = 'yes'", targets.item(y), XPathConstants.BOOLEAN); 
 						}
 					}
 				}
 			}
 		}
 		//we had an error while trying to execute the request, assume not authorized
		catch(Exception e) {
			//output error encountered to the standard error stream before returning not authorized
			e.printStackTrace(System.err);
 			return false;
 		}
 		// default to false
 		return false;
 	}
 
 	public boolean confluenceCheck(String entity, String spaceKey, String permissionType) {
 
 		//generate POST request
 		PostMethod request = new PostMethod(this.gcxServerRoot + "/system/authz-confluence");
 		request.addParameter("entity", entity);
 		request.addParameter("spaceKey", spaceKey);
 		request.addParameter("permissionType", permissionType);
 
 		//try executing the request and parsing the response
 		try {
 			//execute the request
 			this.client.executeMethod(request);
 			
 			//test response to see if it was yes or no
 			return request.getResponseBodyAsString().equalsIgnoreCase("yes");
 		}
		//an error was encountered, so assume not authorized
 		catch(Exception e) {
			//output error encountered to the standard error stream before returning not authorized
			e.printStackTrace(System.err);
			return false;
 		}
 	}
 
 	public void setGcxServerRoot(String gcxServerRoot) {
 		this.gcxServerRoot = gcxServerRoot;
 	}
 }
