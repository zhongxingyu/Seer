 /**
  * 
  * Copyright 2003 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 package org.nchelp.meteor.provider.access;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.xpath.XPathAPI;
 import org.nchelp.meteor.aggregation.AggregatedLoanData;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  *   Class ResponseData.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Feb 10, 2003
  */
 
 public class ResponseData {
 
 	private final Logger log = Logger.create(this.getClass().getName());
 
 	private AggregatedLoanData aggregator;
 
 	private List responses          = new ArrayList();
 	private HashMap awardDocs       = new HashMap();
 	
 	// Keep a list of awards that should be displayed
 	private Collection showAwards   = new TreeSet();
 	private boolean showBest       = true;
 	private boolean showDuplicates = false;
 	private boolean showAll        = false;
 
 	private int awardID             = 0;
 
 	/**
 	 * Constructor for ResponseData.
 	 */
 	public ResponseData() {
 		/* Set up the Aggregation object here 
 		 * Figure out which files to use for best source 
 		 * and for duplicate logic
 		 */
 		Resource res =
 			ResourceFactory.createResource("accessprovider.properties");
 		String bestsource = res.getProperty("meteor.aggregation.bestsource");
 		String duplicate = res.getProperty("meteor.aggregation.duplicateaward");
 
 		try {
 			AggregatedLoanData.setXMLEngines(duplicate, bestsource);
 		} catch (ParsingException e) {
 			log.error("Error initializing XMLEngines", e);
 		}
 
 		aggregator = new AggregatedLoanData();
 	}
 
 	public void addResponse(String response) throws ParsingException {
 		Document resp = XMLParser.parseXML(response);
 
 		this.addResponse(resp);
 	}
 
 	public void addResponse(Document response) throws ParsingException {
 
 		int awardDocPosition = responses.size();
 
 		responses.add(response);
 
 		NodeList awds;
 		try {
 			awds = XPathAPI.selectNodeList(response, "//Award");
 		} catch (TransformerException e) {
 			log.debug("Transforming Error", e);
 			throw new ParsingException(
 				"Unable to locate any elements matching the expression '//Award': "
 					+ e.getMessage());
 		}
 
 		for (int i = 0; i < awds.getLength(); i++) {
 			
 			Node awardNode = awds.item(i);
 			log.debug("Setting the APSUniqueAwardID of " + awardID);
 
 			NodeList idNodes = null;
 			try {
 				idNodes = XPathAPI.selectNodeList(awardNode, "APSUniqueAwardID");
 			} catch (TransformerException e1) {
 				log.debug("TransformerException: " + e1.getMessage());
 			}
 
 			if(idNodes.getLength() > 0){
 				Node id = idNodes.item(0);
 				id.getFirstChild().setNodeValue(Integer.toString(awardID));
 			} else {
 				// Create the node and add it
 				Node tmpIdNode = response.createElement("APSUniqueAwardID");
 				Node idNode = response.createTextNode(Integer.toString(awardID));
 				awardNode.appendChild(tmpIdNode);
 				tmpIdNode.appendChild(idNode);
 			}
 			
 			awardDocs.put(new Integer(awardID), new Integer(awardDocPosition));
 
 			awardID++;
 
 			// Put it in a separate document for the aggregator to use
 			Document award;
 			try {
 				award = XMLParser.createDocument(awardNode);
 			} catch (ParsingException e) {
 				log.debug("Parsing Error", e);
 				continue;
 			}
 
 			aggregator.add(award);
 		}
 	}
 
 	public void showBest() {
 		this.showBest = true;
 		this.showDuplicates = false;	
 		this.showAll = false;
 		
 		showAwards   = new TreeSet();
 	}
 
 	public void showAward(String awardID) {
 		this.showBest = false;
 		this.showAwards.add(awardID);
 		this.showDuplicates = false;	
 		this.showAll = false;
 	}
 	
 	public void hideAward(String awardID) {
 		this.showBest = false;
 		this.showAwards.remove(awardID);
 		this.showDuplicates = false;	
 		this.showAll = false;
 	}
 	
 	public void showDuplicates(String awardID) {
 		this.showBest = false;
 		this.showAwards = new TreeSet();
 		this.showAwards.add(awardID);
 		
 		this.showDuplicates = true;	
 		this.showAll = false;
 	}
 
 	public void showAll() {
 		this.showBest = false;
 		this.showDuplicates = false;	
 		this.showAll = true;
 		
 		showAwards   = new TreeSet();
 	}
 				
 	/**
 	 * Take all of the Data Responses that have been returned and display the
 	 * ones that are determined by <code>AggregatedLoanData</code> to be the
 	 * best awards.
 	 * 
 	 * Here's the general sequence of this method:
 	 * 
 	 * <ol>
 	 * <li>Make sure that <code>aggregateLoans</code> has been called</li>
 	 * <li>Make a copy of the List of all responses</li>
 	 * <li>Using the awardDocs, remove the awards from the main document that
 	 * aren't part of the best source</li>
 	 * <li>Add all of the elements of the <code>responses</code>
 	 *     List to a document to return</li>
 	 * </ol>
 	 * 
	 * @returns String representing the data to display.  If <code>null</code>
 	 * is returned, then soemthing really bad happened.  If an empty string is
 	 * returned then there is no data to display.
 	 */
 	public String toString() {
 		
 		if(responses.size() == 0){
 			return "";
 		}
 
 
 		aggregator.aggregateLoans();
 
 		// Somehow figure out which possible awards are
 		// duplicates
 		Object[] awards = null;
 		
 		if(this.showBest) {
 			awards = aggregator.getBest();
 		} else if(this.showDuplicates) {
 			// there better only be one award
 			Iterator i = showAwards.iterator();
 			int id = Integer.parseInt((String)i.next());
 			
 			awards = aggregator.getDuplicates(id);
 			
 		} else if(this.showAll) {
 			awards = aggregator.get();
 		} else {
 			// Loop through the showAwards and add them to the array
 			awards = new Object[showAwards.size()];
 			Iterator i = showAwards.iterator();
 			int counter = 0;
 			while(i.hasNext()) {
 				int id = Integer.parseInt((String)i.next());
 				
 				Document awd = aggregator.get(id);
 				if(awd == null){
 					continue;
 				}
 				awards[counter] = awd;
 				counter++;
 			}	
 		
 			
 		}
 
 		// Put the best sources in a hashmap
 		HashMap bestMap = new HashMap();
 
 		for (int i = 0; i < awards.length; i++) {
 			//log.debug("Document: " + XMLParser.XMLToString((Document) awards[i]));
 			String idStr = XMLParser.getNodeValue((Document) awards[i],"APSUniqueAwardID");
 			Integer id = null;
 			
 			try{
 				id = new Integer(idStr);
 			} catch(NumberFormatException e){
 				log.info("Error get the APSUniqueAwardID from the document.");
 				continue;
 			}
 
 			bestMap.put(id, awards[i]);
 		}
 
 		// Make a copy of the response documents
 		// Dont just copy the array, duplicate each
 		// object within the array becuase we are
 		// about to seriously mess with the content 
 		// of each element.
 		List tmpList = new ArrayList();
 		
 		Iterator iter = responses.iterator();
 		while(iter.hasNext()){
 			Document doc = (Document)iter.next();
 			
 			// This most likely isn't the fastest way to do this
 			String xml = XMLParser.XMLToString(doc);
 			Document newDoc;
 			try {
 				newDoc = XMLParser.parseXML(xml);
 			} catch (ParsingException e1) {
 				continue;
 			}
 			
 			tmpList.add(newDoc);
 		}
 		
 		
 
 		/*
 		 * Loop through all of the responses that were
 		 * set and remove all of the awards that were
 		 * *not* set to be displayed.  
 		 * 
 		 * This is necessary because we want to keep
 		 * each individual award document with the
 		 * corresponding MeteorDataResponse that it
 		 * was originaly set with 
 		 */
 		iter = awardDocs.keySet().iterator();
 		
 		while(iter.hasNext()){
 			Integer key = (Integer)iter.next();
 		
 			if(bestMap.get(key) == null){
 				Integer docID = (Integer)awardDocs.get(key);
 				try {
 					this.removeAward((Document)tmpList.get(docID.intValue()), key.intValue());
 				} catch (ParsingException e) {
 					log.error(e.getMessage());
 				}
 			}
 		}
 		
 
 
 
 		String tmpString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 				 "<PESCXML:MeteorRsMsg xmlns:PESCXML=\"http://schemas.pescxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.pescxml.org Meteor_Schema_1-0-0.xsd\" PESCXMLVersion=\"1.0.0\">\n" +
 				 "</PESCXML:MeteorRsMsg>";
 	             
 		Document xmlDocument = null;
 		try {
 			xmlDocument = XMLParser.parseXML(tmpString);
 		} catch (ParsingException e) {
 			// we just made the XML string
 			// how could it not parse???
 		}
 
 		for(int i = 0; i < tmpList.size(); i++){
 			Document awardDoc = (Document)tmpList.get(i);
 	
 			
 			NodeList nodes  = null;
 			try {
 				nodes = XPathAPI.selectNodeList(awardDoc, "//MeteorDataProviderInfo");
 			} catch (TransformerException e) {
 				log.error("Transforming Error", e);
 				// Not a critical enough issue to stop processing
 				continue;
 			}
 
 			for (int k = 0; k < nodes.getLength(); k++) {
 				Node node = nodes.item(k);
 				node = xmlDocument.importNode(node, true);
 		
 				xmlDocument.getDocumentElement().appendChild(node);
 			}
 		}
 
 		return XMLParser.XMLToString(xmlDocument);
 	}
 
 
 	private Document removeAward(Document doc, int awardID) throws ParsingException {
 		
 		
 		NodeList awds;
 		String expression = "//Award [APSUniqueAwardID=" + awardID + "]";
 		try {
 			awds = XPathAPI.selectNodeList(doc, expression);
 		} catch (TransformerException e) {
 			log.debug("Transforming Error", e);
 			throw new ParsingException(
 				"Unable to locate any elements matching the expression '" + expression + "': "
 					+ e.getMessage());
 		}
 
 		for (int k = 0; k < awds.getLength(); k++) {
 			Node awardNode = awds.item(k);
 			
 			log.debug("Removing a document matching " + expression);
 			
 			Node parentNode = awardNode.getParentNode();
 			parentNode.removeChild(awardNode);
 		}		
 		
 		return doc;
 	}
 
 	private void nothing() {
 		/*
 		 * All of this code came from the refactoring of this
 		 * from IndexQueryService into a separate object.
 		 * Surely it did something there, right?
 		 */
 
 		//		if(xmlDocument == null){
 		//			xmlDocument = resp;
 		//		} else {
 		//			// Find all of the MeteorDataProviderInfo tags and add
 		//			// them to the xmlDocument
 		//			NodeList mdpiNodeList = null;
 		//			try {
 		//				mdpiNodeList =
 		//					XPathAPI.selectNodeList(resp, "//MeteorDataProviderInfo");
 		//			} catch (TransformerException e) {
 		//				log.error("Error selecting the list of MeteorDataProviderInfo nodes: " + e);
 		//			}
 		//				
 		//			if(mdpiNodeList == null){
 		//				// then the exception was just thrown
 		//				continue;	
 		//			}
 		//				
 		//				
 		//			for(int i=0; i< mdpiNodeList.getLength(); i++){
 		//				Node mdpi = mdpiNodeList.item(i);
 		//					
 		//				if(mdpi == null){  continue; }
 		//					
 		//				// This makes it allowable to be imported into the document
 		//				Node tmpNode = xmlDocument.importNode(mdpi, true);
 		//				//Now import it after the root node
 		//				xmlDocument.getDocumentElement().appendChild(tmpNode);	
 		//			}
 		//		}
 	}
 
 }
