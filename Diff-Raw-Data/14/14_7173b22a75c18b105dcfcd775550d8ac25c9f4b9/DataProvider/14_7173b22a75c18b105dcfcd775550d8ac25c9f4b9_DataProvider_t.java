 /**
  * 
  * Copyright 2002 NCHELP
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
  ****************************************************************************/
 
 package org.nchelp.meteor.provider;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.nchelp.hpc.HPCMessage;
 import org.nchelp.hpc.SOAPTransport;
 import org.nchelp.hpc.util.exception.CompressionException;
 import org.nchelp.hpc.util.exception.EncodingException;
 import org.nchelp.hpc.util.exception.ParsingException;
 import org.nchelp.hpc.util.exception.TransportException;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataRequest;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.w3c.dom.Document;
 
 /**
 * This is the object that communicates with the actual Data Provider.
 * Instances of this object will be created on the successful
 * response from the Index Provider. 
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class DataProvider {
 	private final Logger log = Logger.create(this.getClass());
 	private URL address;
 	private String name;
 	private String id = "";
 	private int minimumAuthenticationLevel = -1;
 
 	private SecurityToken token;
 	private MeteorParameters params;
 	private String response;
 
 	/*
 	 * @see Object#Object()
 	 */
 	public DataProvider() {}
 
 	/**
 	 * Construct a DataProvider object from the passed in
 	 * xml.  Make sure that this can handle what 
 	 * this.toString() returns.
 	 * @param xml
 	 */
 	public DataProvider(String xml)
 		throws
 			org.nchelp.meteor.util.exception.ParsingException,
 			MalformedURLException {
 		Document doc = XMLParser.parseXML(xml);
 		this.init(doc);
 	}
 
 	public DataProvider(Document doc) throws MalformedURLException {
 		this.init(doc);
 	}
 
 	private void init(Document doc) throws MalformedURLException {
 		this.id = XMLParser.getNodeValue(doc, "EntityID");
 		if (this.id == null) {
 			this.id = "";
 		}
 
 		this.name = XMLParser.getNodeValue(doc, "EntityName");
 
 		String address = XMLParser.getNodeValue(doc, "EntityURL");
 		if (address != null && address.length() > 0) {
 			this.setURL(new URL(address));
 		}
 
 		log.debug(
 			"Creating a new Data Provider object from a Document"
 				+ " with the ID: "
 				+ this.id);
 
 	}
 	/**
 	 * This is the main workhorse of this object.  This is the 
 	 * method responsible for instantiating the HighPerformance
 	 * objects and making the call.
 	 * @param ssn Social Secrity Number to query
 	 * @param dob Date of birth for the SSN
 	 * @return MeteorDataResponse Response object that will contain
 	 *                             all of the information from the
 	 *                             Data Provider
 	 */
 	public String getData(
 		SecurityToken token,
 		MeteorParameters params)
 		throws DataException {
 		this.setToken(token);
 		this.setParams(params);
 
 		this.getData();
 
 		return this.response;
 
 	}
 
 	public void getData() throws DataException {
 		String transactionType = "METEORDATA";
 
 		MeteorDataRequest req = new MeteorDataRequest();
 		req.setSecurityToken(token);
 		req.setSSN(params.getSsn());
 
 		AccessProvider ap = new AccessProvider();
 		ap.setID(params.getInstitutionID());
 		ap.setUserHandle(params.getCurrentUser());
 		req.setAccessProvider(ap);
 
		if(log.isDebugEnabled()){
			log.debug("Request: " + req.toString());
		}
 
 		HPCMessage messSend = new HPCMessage();
 		messSend.setRecipientID(this.id);
 		messSend.setContent(req.toString().getBytes(), transactionType);
 		messSend.setTransactionType(transactionType);
 
		// @todo: should this do this here?  What happens if the IP returns a URL that isn't right
 		if (this.address == null) {
 			DistributedRegistry reg = DistributedRegistry.singleton();
 			this.address = reg.getDataProviderURL(this.id);
 			if (this.address == null) {
 				// something bad happened here!!
 				throw new DataException(
 					"Unable to determine URL for data provider '"
 						+ this.id
 						+ "'");
 			}
 		}
 
 		log.info("Data Request Send to " + this.address);
 		//log.debug("Data: " + req.toString());
 
 		HPCMessage messRecv = null;
 		try {
 			messRecv = SOAPTransport.send(messSend, this.address);
 		} catch (ParsingException e) {
 			throw new DataException(e);
 		} catch (TransportException e) {
 			throw new DataException(e);
 		}
 
 		log.info("Data Response Received from " + this.address);
 
 		String strResponse = null;
 		try {
			byte[] content = messRecv.getContent();
			if(content.length > 0){
				strResponse = new String(content);
			} else {
				strResponse = "";
			}
 		} catch (EncodingException e) {
 			throw new DataException(e);
 		} catch (CompressionException e) {
 			throw new DataException(e);
 		}
 		//log.debug(strResponse);
 
 		synchronized (this) {
 			this.response = strResponse;
 		}
 	}
 
 	/**
 	 * Gets the address.
 	 * @return Returns a URL
 	 */
 	public URL getURL() {
 		return address;
 	}
 
 	/**
 	 * Sets the address.
 	 * @param address The address to set
 	 */
 	public void setURL(URL address) {
 		this.address = address;
 	}
 
 	/*
 	 * Override the equals method of java.lang.Object. 
 	 * This will allow us to compare the contents of two
 	 * DataProvider objects rather than only compare the 
 	 * address of the two objects.
 	 * 
 	 * @see Object#equals(Object)
 	 */
 	public boolean equals(Object obj) {
 		//first, make sure they actually passed in a DataProvider object
 		if (obj.getClass().isAssignableFrom(this.getClass())) {
 			DataProvider dp = (DataProvider) obj;
 
 			// the ID is the primary key here
 			if (this.id.equals(dp.getId())) {
 				return true;
 			}
 		}
 		return super.equals(obj);
 	}
 
 	/**
 	 * Gets the name.
 	 * @return Returns a String
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name.
 	 * @param name The name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/*
 	 * @see Object#toString()
 	 */
 	public String toString() {
 		String strReturn = "<DataProvider>";
 
 		if (this.name != null) {
 			strReturn += "<EntityName>" + this.name + "</EntityName>";
 		}
 		strReturn += "<EntityID>" + this.id + "</EntityID>";
 		if (this.address != null) {
 			strReturn += "<EntityURL>"
 				+ this.address.toString()
 				+ "</EntityURL>";
 		}
 		strReturn += "</DataProvider>";
 		return strReturn;
 	}
 
 	/**
 	 * Gets the token.
 	 * @return Returns a SecurityToken
 	 */
 	public SecurityToken getToken() {
 		return token;
 	}
 
 	/**
 	 * Sets the token.
 	 * @param token The token to set
 	 */
 	public void setToken(SecurityToken token) {
 		this.token = token;
 	}
 
 	/**
 	 * Gets the params.
 	 * @return Returns a MeteorParameters
 	 */
 	public MeteorParameters getParams() {
 		return params;
 	}
 
 	/**
 	 * Sets the params.
 	 * @param params The params to set
 	 */
 	public void setParams(MeteorParameters params) {
 		this.params = params;
 	}
 
 	/**
 	 * Gets the response.
 	 * @return Returns a String containing the MeteorDataResponse
 	 */
 	public String getResponse() {
 		return response;
 	}
 
 	/**
 	 * Gets the id.
 	 * @return Returns a String
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * Sets the id.
 	 * @param id The id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * Gets the minimumAuthenticationLevel.
 	 * @return Returns a int
 	 */
 	public int getMinimumAuthenticationLevel() {
 		if (this.minimumAuthenticationLevel == -1) {
 			// Get it from the registry
 			try {
 				Directory dir = DirectoryFactory.getInstance().getDirectory();
 				this.minimumAuthenticationLevel =
 					dir.getAuthenticationLevel(
 						this.id,
 						"1",
 						Directory.TYPE_DATA_PROVIDER);
 			} catch (DirectoryException e) {
 				log.error(
 					"Error getting the Minimum Authentication Level for "
 						+ this.id,
 					e);
 			}
 
 		}
 		return this.minimumAuthenticationLevel;
 	}
 
 }
