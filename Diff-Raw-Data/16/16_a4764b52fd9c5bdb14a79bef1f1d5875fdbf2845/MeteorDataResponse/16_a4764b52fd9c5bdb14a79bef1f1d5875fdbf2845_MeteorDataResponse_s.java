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
  ****************************************************************************/
 
 package org.nchelp.meteor.message;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
 
 import org.w3c.dom.Document;
 
 import org.nchelp.meteor.message.response.*;
 import org.nchelp.meteor.util.exception.ParsingException;
 
 
 /**
  * Wrapper object for the XML that will be returned from a call to a Data
  * Provider. This handles successfull calls as well as all errors that could
  * occur. This class must have intricate knowledge of the XML Schema. The public
  * interface should not change if the data elements of the schema changes
  * 
  * @since Meteor1.0
  *
  */
 public class MeteorDataResponse extends MeteorResponse
 {
 
	private final Log log = LogFactory.getLog(this.getClass());
 
 	private MeteorRsMsg message = new MeteorRsMsg();
 
 	/*
 	 * @see Object#Object()
 	 */
 	public MeteorDataResponse ()
 	{
 	}
 
 	/**
 	 * Method MeteorDataResponse.
 	 * 
 	 * @param xml
 	 */
 	public MeteorDataResponse (String xml) throws ParsingException
 	{
 
 		StringReader reader = new StringReader(xml);
 		try
 		{
 			Unmarshaller unm = new Unmarshaller(MeteorRsMsg.class);
 
 			if (log.isDebugEnabled())
 				unm.setDebug(true);
 			else
 				unm.setDebug(false);
 
 			message = (MeteorRsMsg)unm.unmarshal(reader);
 		}
 		catch (MarshalException e)
 		{
 			e.printStackTrace();
 			throw new ParsingException(e);
 		}
 		catch (ValidationException e)
 		{
 			throw new ParsingException(e);
 		}
 	}
 
 	/**
 	 * Get the underlying MeteorRsMsg object. This will allow you to walk through
 	 * the object tree.
 	 * 
 	 * @return MeteorRsMsg
 	 */
 	public MeteorRsMsg getRsMsg ()
 	{
 		return message;
 	}
 
 	/*
 	 * @see Object#toString()
 	 */
 	public String toString ()
 	{
 		// if an error was set somehow then add it to the XML.
 		if (this.getErrorMessage() != null)
 		{
 			MeteorDataProviderMsg mdpm = new MeteorDataProviderMsg();
 			mdpm.setRsMsg(this.getErrorMessage());
 			mdpm.setRsMsgLevel("E");
 			if (message.getMeteorDataProviderInfoCount() < 1)
 			{
 				this.createMinimalResponse();
 			}
 			message.getMeteorDataProviderInfo(0).addMeteorDataProviderMsg(mdpm);
 			// since we added it, we can now clear it out
 			// to make sure it doesn't get added again
 			this.setError(null, null);
 		}
 
 		StringWriter writer = new StringWriter();
 		try
 		{
 			Marshaller m = new Marshaller(writer);
 			//m.setValidation(false);
 			m.marshal(message);
 		}
 		catch (MarshalException e)
 		{
			log.warn("Error Marshalling MeteorRsMsg Object:" + e.getMessage());
 		}
 		catch (ValidationException e)
 		{
			log.warn("Error Validating MeteorRsMsg Object: " + e.getMessage());
 
 			// Big hack here for Testing ONLY!!
 			StringWriter temp = new StringWriter();
 			try
 			{
 				Marshaller m = new Marshaller(temp);
 				m.setValidation(false);
 				m.marshal(message);
 			}
 			catch (Exception subException)
 			{
				log.warn("Tried to turn off validation and marshal the object again, but that failed too: "
 								+ subException.getMessage());
 			}
 
			log.warn("XML is currently: " + temp.toString());
 		}
 		catch (IOException e)
 		{
 			log.warn("IO Exception while marshalling XML: " + e.getMessage());
 		}
 
 		return writer.toString();
 	}
 
 	/**
 	 * This method ensures that the MeteorRsMsg that this encapsulates is well
 	 * formed. This will walk through the hierarchy and force creation of the
 	 * elements that must be there to match the schema
 	 */
 	public void createMinimalResponse ()
 	{
 
 		MeteorRsMsg msg = this.getRsMsg();
 
 		MeteorDataProviderInfo mdpi = null;
 		if (msg.getMeteorDataProviderInfoCount() < 1)
 		{
 			mdpi = new MeteorDataProviderInfo();
 			msg.addMeteorDataProviderInfo(mdpi);
 		}
 		else
 		{
 			mdpi = msg.getMeteorDataProviderInfo()[0];
 		}
 
 		MeteorDataProviderDetailInfo mdpdi = mdpi.getMeteorDataProviderDetailInfo();
 		if (mdpdi == null)
 		{
 			mdpdi = new MeteorDataProviderDetailInfo();
 			mdpi.setMeteorDataProviderDetailInfo(mdpdi);
 		}
 
 		if (mdpdi.getDataProviderType() == null)
 		{
 			mdpdi.setDataProviderType("G");
 		}
 
 		DataProviderData dpd = mdpdi.getDataProviderData();
 		if (dpd == null)
 		{
 			dpd = new DataProviderData();
 			mdpdi.setDataProviderData(dpd);
 		}
 
 		Contacts contacts = dpd.getContacts();
 		if (contacts == null)
 		{
 			contacts = new Contacts();
 			dpd.setContacts(contacts);
 		}
 
 		DataProviderAggregateTotal dpat = mdpdi.getDataProviderAggregateTotal();
 		if (dpat == null)
 		{
 			dpat = new DataProviderAggregateTotal();
 			mdpdi.setDataProviderAggregateTotal(dpat);
 		}
 
 		return;
 	}
 
 	/**
 	 * This method will return a List object that contains all of the awards as
 	 * 'org.w3c.dom.Document'
 	 * 
 	 * @return List
 	 */
 	public List getAwardDocuments (int startingAPSUniqueAwardID)
 	{
 		List returnList = new ArrayList();
 
 		int count = message.getMeteorDataProviderInfoCount();
 		for (int i = 0; i < count; i++)
 		{
 			MeteorDataProviderInfo mdpi = message.getMeteorDataProviderInfo(i);
 			MeteorDataProviderAwardDetails mdpad = mdpi.getMeteorDataProviderAwardDetails();
 			if (mdpad == null)
 				continue;
 
 			Award[] awards = mdpad.getAward();
 
 			for (int j = 0; j < awards.length; j++)
 			{
 				awards[j].setAPSUniqueAwardID(BigDecimal.valueOf(startingAPSUniqueAwardID));
 				startingAPSUniqueAwardID++;
 
 				Document awdDoc = null;
 
 				awdDoc = this.marshallObject(awards[j]);
 
 				returnList.add(awdDoc);
 			}
 		}
 
 		return returnList;
 	}
 
 	private Document marshallObject (Object obj)
 	{
 
 		Document doc = null;
 
 		try
 		{
 			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		}
 		catch (ParserConfigurationException e)
 		{
 			log.warn("Error Creating new Document", e);
 		}
 
 		// Now Marshall that Award object into a Document
 		try
 		{
 			Marshaller m = new Marshaller(doc);
 			//m.setValidation(false);
 			m.marshal(obj);
 		}
 		catch (MarshalException e)
 		{
 			log.warn("Error Marshalling Award Object", e);
 		}
 		catch (ValidationException e)
 		{
 			log.warn("Error Validating Award Object", e);
 		}
 
 		return doc;
 
 	}
 }
