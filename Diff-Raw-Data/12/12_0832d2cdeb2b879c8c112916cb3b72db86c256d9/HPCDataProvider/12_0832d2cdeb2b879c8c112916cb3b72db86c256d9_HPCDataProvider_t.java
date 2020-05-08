 package org.nchelp.eeat;
 
 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 				John Gill, 		EEAT
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
 
 import org.nchelp.hpc.HPCMessage;
 import org.nchelp.hpc.SOAPTransport;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.provider.MeteorContext;
 import org.nchelp.meteor.provider.data.DataServerAbstraction;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 
 public class HPCDataProvider implements DataServerAbstraction {
 
 	private final Logger log = Logger.create(this.getClass());
 	
 	/**
 	 * Constructor for HPCDataProvider.
 	 */
 	public HPCDataProvider() {
 		super();
 	}
 
 	/**
 	 * @see DataServerAbstraction#getData(MeteorContext, String)
 	 */
 	public MeteorDataResponse getData(MeteorContext context, String ssn) {
 		
 		HPCMessage mess = new HPCMessage();
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 		MeteorDataResponse resp;
 		
		mess.setRecipientID(res.getProperty("DataProvider.HPC.AccessPoint","HPC.DATAPROVIDER.ACCESSPOINT"));
		mess.setTransactionType(res.getProperty("DataProvider.HPC.DataSource","EEAT-HPCP-DATASOURCE"));
		mess.setMessageID(res.getProperty("DataProvider.HPC.MessageID","Hi John"));
 		
 		if (context.getSecurityToken().getRole().equals(SecurityToken.roleSTUDENT)) {					
			mess.setContent("<BorrowerSSN>"+ ssn + "</BorrowerSSN>", res.getProperty("DataProvider.HPC.ContentID","METEOR001"));
 		} else {					
			mess.setContent("<StudentSSN>"+ ssn + "</StudentSSN>", res.getProperty("DataProvider.HPC.ContentID","METEOR001"));
 		}
 		
 		SOAPTransport trans = new SOAPTransport();
 		String responseContent = null;
 		
 		try {
 			HPCMessage responseMessage = trans.send(mess);
 			
 			responseContent = new String(responseMessage.getContent());
 		} catch(Exception e) {
 			log.error("Error communicating with HPC server: " + mess.getRecipientID() + "\n" +
 			          "Exception message: " + e.getMessage());
 			resp = new MeteorDataResponse();
 			resp.setError("E", "Unable to retrieve data, please try again later");
 			resp.createMinimalResponse();
 			
 			return resp;
 		}
 		
 		try {
 			resp = new MeteorDataResponse(responseContent);
 		} catch (org.nchelp.meteor.util.exception.ParsingException e) {
 			log.error("Unable to parse response as a valid Meteor Data Response.  \n" +
 			          "Exception message: " + e.getMessage() + "\n" +
 			          "Actual Response is: " + responseContent);
 			resp = new MeteorDataResponse();
 			resp.setError("E", "Unable to retrieve data, please try again later");
 			resp.createMinimalResponse();
 			
 			return resp;
 		}
 		
 		return resp;
 	}
 
 }
