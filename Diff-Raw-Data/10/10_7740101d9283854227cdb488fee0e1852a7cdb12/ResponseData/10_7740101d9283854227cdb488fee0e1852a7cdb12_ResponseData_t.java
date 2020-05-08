 /**
  * 
  * Copyright 2002 - 2005 NCHELP
  * 
  * Author:	Priority Technologies, Inc.
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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  ********************************************************************************/
 package org.nchelp.meteor.provider.access;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xpath.XPathAPI;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.nchelp.meteor.aggregation.AggregateConstants;
 import org.nchelp.meteor.aggregation.BestSourceDeduplication;
 import org.nchelp.meteor.aggregation.DuplicateLoanDataVO;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLDataTypes;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.ParsingException;
 
 
 /**
  * Class ResponseData.java
  * 
  * @since Feb 10, 2003
  */
 
 public class ResponseData implements Serializable
 {
 	private transient Log log = LogFactory.getLog(this.getClass());
 	private BestSourceDeduplication aggregator;
 	private List responses = new ArrayList();
 	private HashMap awardDocs = new HashMap();
 
 	// Keep a list of awards that should be displayed
 	private Object[] best = null;
 	private Collection showAwards = new TreeSet();
 	private int awardID = 0;
 	private int orgID = 0;
 	private String ssn;
 
 	/**
 	 * Constructor for ResponseData.
 	 */
 	public ResponseData (String ssn)
 	{
 		this.ssn = ssn;
 		this.aggregator = new BestSourceDeduplication();
 	}
 
 	public void addResponse (String response) throws ParsingException
 	{
		log.debug("Unescaping the response message");
		String msg = XMLDataTypes.unescapeXML(response);
		Document resp = XMLParser.parseXML(msg);
 		this.addResponse(resp);
 	}
 
 	public void addResponse (Document response) throws ParsingException
 	{
 		this.responses.add(response);
 		this.setUniqueOrgIds(response);
 		this.setUniqueAwardIds(response);
 	}
 
 	/**
 	 * @param awds
 	 */
 	private void setUniqueAwardIds (Document response) throws ParsingException
 	{
 		NodeList awds;
 		NodeList pvds;
 		Node pvd;
 		try
 		{
 			awds = XPathAPI.selectNodeList(response, "//Award");
 		}
 		catch (TransformerException e)
 		{
 			log.debug("Transforming Error", e);
 			throw new ParsingException("Unable to locate any elements matching the expression '//Award': " + e.getMessage());
 		}
 
 		// Get current date in format CCYY-MM-DD
 		Calendar cal = Calendar.getInstance();
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String testString = "2004-01-02";
 		Date testDate = null;
 		try
 		{
 			testDate = format.parse(testString);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		Date currentDate = cal.getTime();
 
 		Node awardNode = null;
 		for (int i = 0; i < awds.getLength(); i++)
 		{
 			awardNode = awds.item(i);
 			log.debug("Setting the APSUniqueAwardID of " + awardID);
 
 			NodeList idNodes = null;
 			Node awardTypeDescNode = null;
 			Node pastDueNode = null;
 			Node pmtDueDateNode = null;
 			Node awardTypeNode = null;
 			try
 			{
 				idNodes = XPathAPI.selectNodeList(awardNode, "APSUniqueAwardID");
 				awardTypeDescNode = XPathAPI.selectSingleNode(awardNode, "AwardTypeDescription");
 				pastDueNode = XPathAPI.selectSingleNode(awardNode, "PastDue");
 				pmtDueDateNode = XPathAPI.selectSingleNode(awardNode, "Repayment/NextDueDt");
 				awardTypeNode = XPathAPI.selectSingleNode(awardNode, "AwardType");
 			}
 			catch (TransformerException e1)
 			{
 				log.info("TransformerException: " + e1.getMessage());
 			}
 
 			if (idNodes.getLength() > 0)
 			{
 				Node id = idNodes.item(0);
 				id.getFirstChild().setNodeValue(Integer.toString(awardID));
 			}
 			else
 			{
 				// Create the node and add it
 				Node tmpIdNode = response.createElement("APSUniqueAwardID");
 				Node idNode = response.createTextNode(Integer.toString(awardID));
 				awardNode.appendChild(tmpIdNode);
 				tmpIdNode.appendChild(idNode);
 			}
 
 			String awardTypeDesc = determineAwardTypeDescription(awardTypeNode.getFirstChild().getNodeValue());
 			if (awardTypeDescNode != null)
 			{
 				awardTypeDescNode.getFirstChild().setNodeValue(awardTypeDesc);
 			}
 			else
 			{
 				// Create the node and add it
 				Node tmpIdNode = response.createElement("AwardTypeDescription");
 				Node idNode = response.createTextNode(awardTypeDesc);
 				awardNode.appendChild(tmpIdNode);
 				tmpIdNode.appendChild(idNode);
 			}
 
 			if (pmtDueDateNode != null)
 			{
 				String pmtDueString = pmtDueDateNode.getFirstChild().getNodeValue();
 				if (pmtDueString != null && !pmtDueString.equals(XMLDataTypes.BLANK_XML_DATE))
 				{
 					Date pmtDueDate = null;
 					try
 					{
 						pmtDueDate = format.parse(pmtDueString);
 						if (pmtDueDate.before(currentDate))
 						{
 							if (pastDueNode != null)
 							{
 								pastDueNode.getFirstChild().setNodeValue("Y");
 							}
 							else
 							{
 								// Create the node and add it
 								Node tmpIdNode = response.createElement("PastDue");
 								Node idNode = response.createTextNode("Y");
 								awardNode.appendChild(tmpIdNode);
 								tmpIdNode.appendChild(idNode);
 							}
 						}
 					}
 					catch (Exception e)
 					{
 						// Due nothing if the date has errors
 					}
 				}
 			}
 			int awardDocPosition = responses.size() - 1;
 			awardDocs.put(new Integer(awardID), new Integer(awardDocPosition));
 			awardID++;
 
 			// Put it in a separate document for the aggregator to use
 			Document award;
 			try
 			{
 				award = XMLParser.createDocument(awardNode);
 			}
 			catch (ParsingException e)
 			{
 				log.info("Parsing Error", e);
 				continue;
 			}
 
 			aggregator.add(award);
 		}
 	}
 
 	/**
 	 * @param award
 	 */
 	private Node setUniqueOrgIds (Document award) throws ParsingException
 	{
 		NodeList orgs;
 		String xpathStr = "//self::node()[EntityName]";
 		try
 		{
 			orgs = XPathAPI.selectNodeList(award, xpathStr);
 		}
 		catch (TransformerException e)
 		{
 			log.info("Transforming Error", e);
 			throw new ParsingException("Unable to locate any elements matching the expression '" + xpathStr + "': " + e.getMessage());
 		}
 
 		int length = orgs.getLength();
 		for (int i = 0; i < length; i++)
 		{
 			Node orgNode = orgs.item(i);
 			log.debug("Setting the APSUniqueOrgID of " + orgID);
 
 			NodeList idNodes = null;
 			try
 			{
 				idNodes = XPathAPI.selectNodeList(orgNode, "APSUniqueOrgID");
 			}
 			catch (TransformerException e1)
 			{
 				log.info("TransformerException: " + e1.getMessage());
 			}
 
 			if (idNodes.getLength() > 0)
 			{
 				log.debug("APSUniqueOrgID exists");
 				Node id = idNodes.item(0);
 				id.getFirstChild().setNodeValue(Integer.toString(orgID));
 			}
 			else
 			{
 				// Create the node and add it
 				log.debug("Creating a new APSUniqueOrgID of " + orgID);
 				Node tmpIdNode = award.createElement("APSUniqueOrgID");
 				Node idNode = award.createTextNode(Integer.toString(orgID));
 				orgNode.appendChild(tmpIdNode);
 				tmpIdNode.appendChild(idNode);
 			}
 
 			orgID++;
 		}
 
 		return award;
 	}
 
 	public void showBest ()
 	{
 		if (best == null)
 		{
 			aggregator.aggregateLoans();
 			best = aggregator.getBest();
 		}
 
 		showAwards = new TreeSet();
 		DuplicateLoanDataVO dataVO = null;
 
 		for (int i = 0; i < best.length; i++)
 		{
 			dataVO = (DuplicateLoanDataVO)best[i];
 
 			String idStr = XMLParser.getNodeValue(dataVO.getDocument(), "APSUniqueAwardID");
 			
 			this.setBestSource(idStr, true);
 
 			showAwards.add(idStr);
 		}
 	}
 
 	public void showAward (String awardID)
 	{
 		if (awardID != null)
 			this.showAwards.add(awardID);
 	}
 
 	public void hideAward (String awardID)
 	{
 		if (awardID != null)
 			this.showAwards.remove(awardID);
 	}
 
 	public void showDuplicates (String awardID)
 	{
 		this.showAwards = new TreeSet();
 		if (awardID != null)
 		{
 			this.showAwards.add(awardID);
 
 			// there better only be one award
 			Iterator iter = showAwards.iterator();
 			int id = Integer.parseInt((String)iter.next());
 
 			Object[] awards = null;
 
 			awards = aggregator.getDuplicates(id);
 
 			showAwards = new TreeSet();
 
 			for (int i = 0; i < awards.length; i++)
 			{
 				DuplicateLoanDataVO dataVO = (DuplicateLoanDataVO)awards[i];
 			
 				String idStr = XMLParser.getNodeValue(dataVO.getDocument(), "APSUniqueAwardID");
 
 				this.setBestSource(idStr, (i == 0 ? true : false));
 				
 				showAwards.add(idStr);
 			}
 		}
 	}
 
 	public void showAll ()
 	{
 		this.showAwards = new TreeSet();
 		Object[] awards = null;
 
 		awards = aggregator.get();
 
 		showAwards = new TreeSet();
 
 		for (int i = 0; i < awards.length; i++)
 		{
 			DuplicateLoanDataVO dataVO = (DuplicateLoanDataVO)awards[i];
 			String idStr = XMLParser.getNodeValue(dataVO.getDocument(), "APSUniqueAwardID");
 			showAwards.add(idStr);
 		}
 	}
 
 	public void removeAll ()
 	{
 		showAwards = new TreeSet();
 	}
 
 	/**
 	 * Take all of the Data Responses that have been returned and display the ones
 	 * that are determined by <code>AggregatedLoanData</code> to be the best
 	 * awards.
 	 * 
 	 * Here's the general sequence of this method:
 	 * 
 	 * <ol>
 	 * <li>Make sure that <code>aggregateLoans</code> has been called</li>
 	 * <li>Make a copy of the List of all responses</li>
 	 * <li>Using the awardDocs, remove the awards from the main document that
 	 * aren't part of the best source</li>
 	 * <li>Add all of the elements of the <code>responses</code> List to a
 	 * document to return</li>
 	 * </ol>
 	 * 
 	 * @return String representing the data to display. If <code>null</code> is
 	 * returned, then soemthing really bad happened. If an empty string is
 	 * returned then there is no data to display.
 	 */
 	public String toString ()
 	{
 		Document doc = this.toXML();
 		if (doc == null)
 		{
 			return "";
 		}
 		String xml = XMLParser.xmlToString(doc);
 		doc = null;
 		return xml;
 	}
 
 	public Document toXML ()
 	{
 		if (this.responses.size() == 0)
 		{
 			return null;
 		}
 
 		//aggregator.aggregateLoans();
 
 		String tmpString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 				+ "<PESCXML:MeteorRsMsg xmlns:PESCXML=\"http://schemas.pescxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.pescxml.org Meteor_Schema_1-0-0.xsd\" PESCXMLVersion=\"1.0.0\">\n"
 				+ "</PESCXML:MeteorRsMsg>";
 
 		Document xmlDocument = null;
 		try
 		{
 			xmlDocument = XMLParser.parseXML(tmpString);
 		}
 		catch (ParsingException e)
 		{
 			// we just made the XML string
 			// how could it not parse???
 		}
 
 		for (int i = 0; i < responses.size(); i++)
 		{
 			Document awardDoc = (Document)responses.get(i);
 
 			//log.info("TJB: adding doc to response\n" + XMLParser.xmlToString(awardDoc));
 			NodeList nodes = null;
 			try
 			{
 				nodes = XPathAPI.selectNodeList(awardDoc, "//MeteorDataProviderInfo");
 			}
 			catch (TransformerException e)
 			{
 				log.error("Transforming Error", e);
 				// Not a critical enough issue to stop processing
 				continue;
 			}
 
 			for (int k = 0; k < nodes.getLength(); k++)
 			{
 				Node node = nodes.item(k);
 				node = xmlDocument.importNode(node, true);
 
 				xmlDocument.getDocumentElement().appendChild(node);
 			}
 		}
 
 		List removeIDs = this.getRemoveList();
 		try
 		{
 			//xmlDocument = this.removeAwards(xmlDocument, removeIDs);
 			this.removeAwards(xmlDocument, removeIDs);
 		}
 		catch (ParsingException e)
 		{
 			log.error("ParsingException removing an award fron the list: " + e.getMessage());
 		}
 
 		return xmlDocument;
 	}
 
 	/**
 	 * @return
 	 */
 	private List getRemoveList ()
 	{
 		// this.showAwards is a Set of Strings
 		// keys is a Set of Integers
 		Set keys = this.awardDocs.keySet();
 
 		List returnList = new ArrayList();
 
 		Iterator iter = keys.iterator();
 		while (iter.hasNext())
 		{
 			Integer i = (Integer)iter.next();
 			String str = i.toString();
 
 			// If the award ID is *not* in the list of awards to show then
 			// it *is* in the list of awards to remove
 			if (!this.showAwards.contains(str))
 			{
 				returnList.add(i);
 			}
 		}
 
 		return returnList;
 	}
 
 	private void removeAwards (Document doc, List ids) throws ParsingException
 	{
 		StringBuffer expr = new StringBuffer();
 
 		Iterator iter = ids.iterator();
 		boolean first = true;
 
 		while (iter.hasNext())
 		{
 			if (first)
 			{
 				first = false;
 			}
 			else
 			{
 				expr.append(" | ");
 			}
 			Integer i = (Integer)iter.next();
 			expr.append("//Award [APSUniqueAwardID=");
 			expr.append(i.toString());
 			expr.append("]");
 
 		}
		if (expr == null || expr.length() == 0)
 		{
 			//return doc;
 			return;
 		}
 
		//log.debug(expr.toString());
 
 		NodeList awds;
 		try
 		{
 			awds = XPathAPI.selectNodeList(doc, expr.toString());
 		}
 		catch (TransformerException e)
 		{
 			log.debug("Transforming Error", e);
 			throw new ParsingException("Unable to locate any elements matching the expression '" + expr.toString() + "': " + e.getMessage());
 		}
 
 		for (int k = 0; k < awds.getLength(); k++)
 		{
 			Node awardNode = awds.item(k);
 
 			//log.debug("Removing a document matching " + expression);
 			awardNode.getParentNode().removeChild(awardNode);
 		}
 
 		//return doc;
 		return;
 	}
 
 	private Document removeAward (Document doc, int awardID) throws ParsingException
 	{
 		NodeList awds;
 		String expression = "//Award [APSUniqueAwardID=" + awardID + "]";
 		try
 		{
 			awds = XPathAPI.selectNodeList(doc, expression);
 		}
 		catch (TransformerException e)
 		{
 			log.debug("Transforming Error", e);
 			throw new ParsingException("Unable to locate any elements matching the expression '" + expression + "': " + e.getMessage());
 		}
 
 		for (int k = 0; k < awds.getLength(); k++)
 		{
 			Node awardNode = awds.item(k);
 
 			//log.debug("Removing a document matching " + expression);
 			awardNode.getParentNode().removeChild(awardNode);
 		}
 
 		return doc;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getSsn ()
 	{
 		return ssn;
 	}
 
 	private String determineAwardTypeDescription (String awardType)
 	{
 		String newAwardType = null;
 
 		if (awardType.equalsIgnoreCase(AggregateConstants.CONSOL))
 		{
 			newAwardType = AggregateConstants.CONSOL_LONG_NAME;
 		}
 		else
 		{
 			if (awardType.equalsIgnoreCase(AggregateConstants.CONSOLIDATION))
 			{
 				newAwardType = AggregateConstants.CONSOLIDATION_LONG_NAME;
 			}
 			else
 			{
 				if (awardType.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL))
 				{
 					newAwardType = AggregateConstants.CONSOLIDATION_HEAL_LONG_NAME;
 				}
 				else
 				{
 					if (awardType.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER))
 					{
 						newAwardType = AggregateConstants.CONSOLIDATION_OTHER_LONG_NAME;
 					}
 					else
 					{
 						if (awardType.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED))
 						{
 							newAwardType = AggregateConstants.CONSOLIDATION_SUBSIDIZED_LONG_NAME;
 						}
 						else
 						{
 							if (awardType.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED))
 							{
 								newAwardType = AggregateConstants.CONSOLIDATION_UNSUBSIDIZED_LONG_NAME;
 							}
 							else
 							{
 								if (awardType.equalsIgnoreCase(AggregateConstants.SLS))
 								{
 									newAwardType = AggregateConstants.SLS_LONG_NAME;
 								}
 								else
 								{
 									if (awardType.equalsIgnoreCase(AggregateConstants.ALTERNATIVE))
 									{
 										newAwardType = AggregateConstants.ALTERNATIVE_LONG_NAME;
 									}
 									else
 									{
 										if (awardType.equalsIgnoreCase(AggregateConstants.SUBSIDIZED))
 										{
 											newAwardType = AggregateConstants.SUBSIDIZED_LONG_NAME;
 										}
 										else
 										{
 											if (awardType.equalsIgnoreCase(AggregateConstants.UNSUBSIDIZED))
 											{
 												newAwardType = AggregateConstants.UNSUBSIDIZED_LONG_NAME;
 											}
 											else
 											{
 												if (awardType.equalsIgnoreCase(AggregateConstants.ALTERNATIVE))
 												{
 													newAwardType = AggregateConstants.ALTERNATIVE_LONG_NAME;
 												}
 												else
 												{
 													if (awardType.equalsIgnoreCase(AggregateConstants.PLUS))
 													{
 														newAwardType = AggregateConstants.PLUS_LONG_NAME;
 													}
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		if (newAwardType == null)
 		{
 			newAwardType = awardType;
 		}
 		return newAwardType;
 	}
 
 	private void setBestSource(String awardID, boolean value) {
 
 		if(log == null){
 			log = LogFactory.getLog(this.getClass());
 		}
 		
 		for (int i = 0; i < responses.size(); i++)
 		{
 			Document awardDoc = (Document)responses.get(i);
 
 			Node award;
 	
 			try
 			{
 				// There can be only one
 				award = XPathAPI.selectSingleNode(awardDoc, "//Award [APSUniqueAwardID=" + awardID + "]");
 			}
 			catch (TransformerException e)
 			{
 				log.debug("Transforming Error", e);
 				continue;
 			}
 			
 			if(award == null){
 				// then not found
 				continue;
 			}
 	
 			//log.info("Searching for the BestSource node");
 			Node bestNode = null;
 			try
 			{
 				bestNode = XPathAPI.selectSingleNode(award, "BestSource");
 			}
 			catch (TransformerException e1)
 			{
 				log.info("TransformerException: " + e1.getMessage());
 			}
 	
 			//log.info("BestSource node is " + bestNode);
 			
 			if (bestNode!= null)
 			{
 				//log.info("Setting the value of the BestSource node to the existing node");
 				bestNode.getFirstChild().setNodeValue(XMLDataTypes.booleanToXml(value));
 			}
 			else
 			{
 				//log.info("Creating the new BestSource node and setting the value");
 				// Create the node and add it
 				Node tmpIdNode = awardDoc.createElement("BestSource");
 				Node idNode = awardDoc.createTextNode(XMLDataTypes.booleanToXml(value));
 				award.appendChild(tmpIdNode);
 				tmpIdNode.appendChild(idNode);
 			}
 			
 			if(log.isInfoEnabled() && awardDoc != null){
 				log.info("Award after setting BestSource: \n" + XMLParser.xmlToString(awardDoc));
 			}
 		}
 	}
 
 	
 }
