 package org.abratuhi.snippettool.util;
 
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.xmldb.api.DatabaseManager;
 import org.xmldb.api.base.Collection;
 import org.xmldb.api.base.Database;
 import org.xmldb.api.base.XMLDBException;
 import org.xmldb.api.modules.XUpdateQueryService;
 
 /**
  * Collection of functions for dealing with XML data of inscript's .xml description.
  * Primarily created for transformation functions, used to extract needed data from inscripts .xml description.
  * 
  * @author Alexei Bratuhin
  *
  */
 public class XMLUtil {
 	/**
 	 * Apply XSL Transformation to XML
 	 * @param xml	content of XML
 	 * @param xslt	content of XSLT
 	 * @return		content of resultant transformation
 	 */
 	public static String transformXML(String xml, String xslt){
 		StringWriter sw = new StringWriter();
 		TransformerFactory  tFactory = TransformerFactory.newInstance();
 		Source xslSource = new StreamSource(new StringReader(xslt));
 		Transformer transformer;
 		try {
 			transformer = tFactory.newTransformer(xslSource);
 			if(transformer!=null) transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(sw));
 			return sw.toString();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 			System.out.println("xml = "+xml);
 			System.out.println("xslt = "+xslt);
 		} catch (TransformerException e) {
 			e.printStackTrace();
 			System.out.println("xml = "+xml);
 			System.out.println("xslt = "+xslt);
 		}
 		return null;
 	}
 
 	/**
 	 * Perform an XUpdate on eXist database
 	 * @param root	reference to HiWi_GUI
 	 * @param xupdate	xupdate text
 	 * @param user		database's username
 	 * @param pass		database's password
 	 * @param out		dtabase's collection to update
 	 */
 	public static void updateXML(String xupdate, String user, String pass, String out){
 		try {
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database);
 			Collection col = DatabaseManager.getCollection(out, user, pass);
 			XUpdateQueryService service = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
 			service.update(xupdate);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Clear all <appearance>s to given inscript id, meaning - clear marking of inscript with given id
 	 * Notice: regexp must end with an '_'(underscore), since matching is done by startsWith() mean.
 	 * @param root		reference to HiWi_GUI
 	 * @param user		database's username
 	 * @param pass		database's password
 	 * @param out		database's collection, containing inscript's marking
 	 * @param xmlid	inscript's id
 	 */
 	@SuppressWarnings("unchecked")
 	public static void clearAppearances(String user, String pass, String out, String xmlid){
 		// avoid accidentaly deleting all appearances
 		if(xmlid.length() < 2) return;
 		
 		if(!xmlid.endsWith("_")) xmlid += "_";
 		
 		// remove appearances
 		try {
 			String driver = "org.exist.xmldb.DatabaseImpl";
 			String xupdate = 	"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'> " +
 			"<xu:remove select=\"//appearance[substring(@id,0,"+(xmlid.length()+1)+")='"+xmlid+"']\" /> " +
 			"</xu:modifications>";
 			
 			Class cl = Class.forName(driver);  
 			Database database = (Database) cl.newInstance();   
 			DatabaseManager.registerDatabase(database);   
 
 			Collection col = DatabaseManager.getCollection(out, user, pass);
 			String[] xml_out = col.listResources();
 			XUpdateQueryService service = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
 			long modifiedTotal = 0;
 			for(int i=0; i<xml_out.length; i++){
 				long modified = service.updateResource(xml_out[i], xupdate);
 				modifiedTotal += modified;
 			}
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} 
 
 		// TODO: remove snippets
 	}
 
 	/**
 	 * Standardize xml after applying the xsl transformation. This is done to make the implementation of HiWi_Object_Inscript.readTextFromXML() easier.
 	 * Notice: It's better to have an agreement of what the inscript's xml structure may be like, to avoid complicated transformations
 	 * @param xml
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static String standardizeXML(String xml){
 		try {
 			StringWriter sw = new StringWriter();
 			SAXBuilder builder = new SAXBuilder();
 			XMLOutputter outputter = new XMLOutputter();
 			outputter.setFormat(Format.getPrettyFormat());
 			Document docIn = builder.build(new StringReader(xml));
 			Document docOut= new Document(new Element("text"));
 
 			Element rootIn = docIn.getRootElement();
 			Element rootOut= docOut.getRootElement();
 
 			rootOut.removeContent();
 
 			ArrayList<Element> elementsIn = new ArrayList<Element>(rootIn.getChildren());
 			
 			/*
 			 * Possible children:
 			 * 
 			 * <br/>
 			 * 
 			 * <span/>
 			 * 
 			 * <norm/>
 			 * 
 			 * <choice>
 			 * 	<variant>
 			 * 		<span/>
 			 * 		<span/>
 			 * 	</variant>
 			 * 	<variant/>
 			 * </choice>
 			 * 
 			 * <supplied rend="ignore">
 			 * 	<choice>
 			 * 		<variant>
 			 * 			<span/>
 			 * 			<span/>
 			 * 		</variant>
 			 * 		<variant/>
 			 * 	</choice>
 			 * </supplied>
 			 * 
 			 * */
 
 			for(int i=0; i<elementsIn.size(); i++){
 
 				Element element = elementsIn.get(i);
 				
 
 				// newline
 				if(element.getName().equals("br")){
 					element.getParentElement().removeContent(element);
 
 					rootOut.addContent(element);
 				}
 
 				// supplied
 				if(element.getName().equals("supplied")){					
 					element.getParentElement().removeContent(element);
 					
					boolean ignoreSupplied = true; 
 					List<Element> suppliedChildren = element.getChildren();
 					
 					int iterations = suppliedChildren.size();
 					for(int j=0; j<iterations; j++){						
 						Element suppliedElement = suppliedChildren.remove(0);
 						
 						if(suppliedElement.getName().equals("span")){
 							if(ignoreSupplied) suppliedElement.setAttribute("class", "supplied");
 							else suppliedElement.setAttribute("class", "quasi-supplied");
 
 							Element choice = new Element("choice");
 							Element variant = new Element("variant");
 
 							choice.addContent(variant);
 							variant.setAttribute("cert", "1.0");
 							variant.addContent(suppliedElement);
 
 							rootOut.addContent(choice);
 						}
 						if(suppliedElement.getName().equals("norm")){
 							Element choice = new Element("choice");
 							Element variant = new Element("variant");
 
 							Element span = (Element) suppliedElement.getChildren().get(0);
 							suppliedElement.removeContent(span);
 
 							choice.addContent(variant);
 							variant.setAttribute("cert", "1.0");
 							span.setAttribute("class", "supplied");
 							span.setAttribute("original", suppliedElement.getAttributeValue("orig"));
 							variant.addContent(span);
 
 							rootOut.addContent(choice);
 						}
 						if(suppliedElement.getName().equals("choice")){
 							for(int k=0; k<suppliedElement.getChildren().size(); k++){
 								Element cvariant = (Element) suppliedElement.getChildren().get(k);
 								for(int l=0; l<cvariant.getChildren().size(); l++){
 									Element cspan = (Element) cvariant.getChildren().get(l);
 									cspan.setAttribute("class", "supplied");
 								}
 							}
 
 							rootOut.addContent(suppliedElement);
 						}
 					}
 				}
 				// other
 				else{
 					if(element.getName().equals("span")){
 						element.getParentElement().removeContent(element);
 						Element choice = new Element("choice");
 						Element variant = new Element("variant");
 
 						choice.addContent(variant);
 						variant.setAttribute("cert", "1.0");
 						variant.addContent(element);
 
 						rootOut.addContent(choice);
 					}
 					if(element.getName().equals("norm")){
 						element.getParentElement().removeContent(element);
 						Element choice = new Element("choice");
 						Element variant = new Element("variant");
 
 						Element span = (Element) element.getChildren().get(0);
 						element.removeContent(span);
 
 						choice.addContent(variant);
 						variant.setAttribute("cert", "1.0");
 						span.setAttribute("class", "normalized");
 						span.setAttribute("original", element.getAttributeValue("orig"));
 						variant.addContent(span);
 
 						rootOut.addContent(choice);
 					}
 					if(element.getName().equals("choice")){
 						element.getParentElement().removeContent(element);
 						rootOut.addContent(element);
 					}
 				}
 
 			}
 
 			outputter.output(docOut, sw);
 
 			return sw.toString();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
