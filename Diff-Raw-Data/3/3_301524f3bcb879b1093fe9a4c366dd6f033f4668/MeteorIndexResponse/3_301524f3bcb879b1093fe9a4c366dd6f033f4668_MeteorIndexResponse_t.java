 package org.nchelp.meteor.message;
 
 import java.net.MalformedURLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.nchelp.hpc.util.exception.ParsingException;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.util.XMLParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
 * Object representation of the XML that is returned from an Index
 * Provider.  We really can't define the getters and setters until 
 * the spec is finalized.  
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class MeteorIndexResponse extends MeteorResponse{
 
 	private final Logger log = Logger.create(this.getClass());
 	List dataProviders = new Vector();
	private String errorCode = "";
	private String errorMessage = "";	
	
 	/*
 	 * @see Object#Object()
 	 */
 	public MeteorIndexResponse(){
 	}
 	
 	/**
 	 * Constructor used to create an instance of this class from a String
 	 * @param response String representation of this object
 	 */
 	public MeteorIndexResponse(String response) throws ParsingException{
 		Document doc = XMLParser.parseXML(response);
 		
 		log.debug("Creating a MeteorIndexResponse object from: " + response);
 		NodeList nl = doc.getElementsByTagName("DataProvider");
 		for(int i = 0; i < nl.getLength(); i++){
 			Node n = nl.item(i);
 			Document dpDoc = null;
 			try{
 				dpDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 			} catch(Exception e){
 				throw new ParsingException(e);
 			}	
 			Node newNode = dpDoc.importNode(n, true);
 			
 			dpDoc.appendChild(newNode);
 			try{
 				DataProvider dp = new DataProvider(dpDoc);
 				log.debug("Data Provider URL: " + dp.getURL());
 
 				this.addDataProvider(dp);
 			} catch(MalformedURLException e){
 				throw new ParsingException(e);
 			}
 		}
 	}
 
 	/**
 	 * Get a List of all of the DataProvider objects that were returned
 	 * from the call to the Index Provider.
 	 * @return List List of DataProvider objects
 	 */
 	public List getDataProviderList() {
 		// Should we really be passing this back?  
 		// Instead maybe we should make a copy and pass 
 		// that back.  I really don't know.
 		return dataProviders;
 	}
 	
 	public void addDataProvider(DataProvider dp){
 		dataProviders.add(dp);
 	}
 	
 	public void addDataProviderList(List dpl){
 		dataProviders.addAll(dpl);
 	}
 	
 	public String toString(){
 		Iterator i = dataProviders.iterator();
 		
 		String returnString = "<MeteorIndexResponse>";
 		
 		while(i.hasNext()){
 			DataProvider dp = (DataProvider) i.next();
 			returnString += dp.toString();
 		}
 		returnString += "</MeteorIndexResponse>";
 		
 		return returnString;
 	}
 	
 }
