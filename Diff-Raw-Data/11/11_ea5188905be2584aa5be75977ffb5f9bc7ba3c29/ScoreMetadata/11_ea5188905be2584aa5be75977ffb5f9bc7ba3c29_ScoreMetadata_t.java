 package au.gov.nla.forte.model;
 
 import java.util.HashMap;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.util.Log;
 import au.gov.nla.forte.util.SimpleNamespaceContext;
 
 public class ScoreMetadata {
 
 	private String title;
 	private String creator;
 	private String publisher;
 	private String description;
 	private String date;
 	private String identifier;
 	
 	public ScoreMetadata() {
 		this.title = "";
 		this.identifier = "";
 		this.publisher = "";
 		this.description = "";
 		this.date = "";
 		this.creator = "";
 	}
 	
 	/**
 	 * Based on an OAI document from http://www.nla.gov.au/apps/oaicat/servlet/OAIHandler?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:nla.gov.au:nla.mus-an5892255
 	 * populate the object.
 	 */
 	public void populateFromDocument(Document doc) {
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		HashMap<String, String> prefMap = new HashMap<String, String>() {{
 		    put("oai", "http://www.openarchives.org/OAI/2.0/");
 		    put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
 		    put("dc", "http://purl.org/dc/elements/1.1/");
 		}};
 		SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
 		xpath.setNamespaceContext(namespaces);
 		
 		title = getNodeText(doc, xpath, "//GetRecord/record/metadata/dc/title");
 		publisher = getNodeText(doc, xpath, "//GetRecord/record/metadata/dc/publisher");
 		creator = getNodeText(doc, xpath, "//GetRecord/record/metadata/dc/creator");
 		description = getNodeText(doc, xpath, "//GetRecord/record/metadata/dc/description");
 		date = getNodeText(doc, xpath, "//GetRecord/record/metadata/dc/date");
 		identifier = getIdentifierNodeText(doc, xpath, "//GetRecord/record/metadata/dc/identifier");
 		
 	}
 	
 	private String getIdentifierNodeText(Document doc, XPath xpath, String xPathExpr) {
 		try {
 			XPathExpression expr = xpath.compile(xPathExpr);
 			Object result = expr.evaluate(doc, XPathConstants.NODESET);
 			NodeList nodes = (NodeList) result;
 			for (int i = 0; i < nodes.getLength(); i++) {
 				String nodeText = nodes.item(i).getTextContent();
 				if (nodeText.startsWith("http://")) return nodeText;
 			}
 		} catch (XPathExpressionException e) {
 			Log.e("ScoreMetadata", "Error while parsing OAI xml document with xpath : " + xPathExpr);
 			Log.e("ScoreMetadata", ""+e);
		} catch (NullPointerException e) {
			Log.e("ScoreMetadata", "Nullpointer : " + xPathExpr);
			Log.e("ScoreMetadata", ""+e);			
 		}
 		return "";
 	}
 	
 	private String getNodeText(Document doc, XPath xpath, String xPathExpr) {		
 		try {
 			XPathExpression expr = xpath.compile(xPathExpr);
 			Object result = expr.evaluate(doc, XPathConstants.NODE);
 			Node node = (Node)result;
 			return node.getTextContent();
 		} catch (XPathExpressionException e) {
 			Log.e("ScoreMetadata", "Error while parsing OAI xml document with xpath : " + xPathExpr);
 			Log.e("ScoreMetadata", ""+e);
		} catch (NullPointerException e) {
			Log.e("ScoreMetadata", "Nullpointer : " + xPathExpr);
			Log.e("ScoreMetadata", ""+e);			
 		}
		return "";
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getCreator() {
 		return creator;
 	}
 
 	public void setCreator(String creator) {
 		this.creator = creator;
 	}
 
 	public String getPublisher() {
 		return publisher;
 	}
 
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 	
 }
