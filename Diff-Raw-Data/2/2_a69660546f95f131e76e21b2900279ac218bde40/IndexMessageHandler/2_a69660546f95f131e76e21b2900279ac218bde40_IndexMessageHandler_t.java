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
  ********************************************************************************/
 
 
 package org.nchelp.meteor.provider.index;
 
 import java.util.List;
 
 import org.apache.log4j.Priority;
 import org.nchelp.hpc.HPCMessage;
 import org.nchelp.hpc.server.HPCMessageHandler;
 import org.nchelp.hpc.util.exception.CompressionException;
 import org.nchelp.hpc.util.exception.EncodingException;
 import org.nchelp.hpc.util.exception.ParsingException;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorIndexRequest;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.MeteorContext;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 /**
 * This is the receiver class for Meteor Index requests.  
 *  
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class IndexMessageHandler implements HPCMessageHandler {
 
 	private final Logger log = Logger.create(this.getClass());
 
 	/**
 	 * Constructor for IndexMessageHandler.
 	 */
 	public IndexMessageHandler() {
 		super();
 	}
 
 	/*
 	 * @see HPCMessageHandler#handle(HPCMessage)
 	 */
 	public HPCMessage handle(HPCMessage message)
 		throws ParsingException, CompressionException, EncodingException {
 		byte[] request = message.getContent();
 		if(request == null){
 			throw new ParsingException(new Exception());
 		}
 		
 		if (log.isEnabledFor(Priority.DEBUG));
 		{
 			log.debug("Received message: " + new String(request));
 		}
 		
 
 		MeteorIndexRequest miReq = null;
 		try {
 			miReq = new MeteorIndexRequest(new String(request));
 		} catch(org.nchelp.meteor.util.exception.ParsingException e) {
 			throw new org.nchelp.hpc.util.exception.ParsingException(e);
 		}
 		MeteorIndexResponse miResp = new MeteorIndexResponse();
 		
 		DistributedRegistry registry = DistributedRegistry.singleton();
 
 		//error checking here
 		if(! registry.authenticateProvider(miReq.getSecurityToken())){
 			// fail here.  Create a MeteorIndexResponse and say what happened
 			miResp.setError("Not Authorized", "You ain't supposed to be here");
 
 			HPCMessage errMessage = new HPCMessage();			
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(miResp.toString(), "METEORINDEXRESP");
 			
 			return errMessage;
 		}
 		
 		
 		// Figure out which implementation of IndexServerAbstraction 
 		// to instantiate and call the getData() method
 		// Handle the returned data and put it in a MeteorIndexResponse
 		// object and return
 
 		Resource res = ResourceFactory.createResource("indexprovider.properties");
 		String indexClass = res.getProperty("default.index.server");
 		
 		// Now that the class name has been looked up in the properties
 		// file, go ahead ans instantiate one of those and store it
 		// as a generic IndexServerAbstraction object.
 		
 		IndexServerAbstraction isa = null;
 		try {
 
 			isa = (IndexServerAbstraction) Class.forName(indexClass).newInstance();
 
 		} catch (Exception e) {
 			throw new ParsingException(e);
 		}
 		
 		
 		/*
 		 * Set up the context object so the Index Provider
 		 * implementation can get all of the desired information
 		 * about who is makin the request
 		 */
 		MeteorContext context = new MeteorContext();
 		context.setSecurityToken(miReq.getSecurityToken());
 		
 		try{
 			miResp = isa.getDataProviders(context, miReq.getSSN());
 		} catch(Exception e){
 			// don't do anything but a log message here
			log.error(indexClass + " threw the exception " + e.getClass().getName() + ": " + e, e);
 		}
 
 		if(miResp == null){
 			miResp = new MeteorIndexResponse();
 		}
 		
 		HPCMessage response = new HPCMessage();			
 		response.setRecipientID(message.getRecipientID());
 		response.setContent(miResp.toString(), "METEORINDEXRESP");
 			
 		return response;
 	}
 
 	/*
 	 * @see HPCMessageHandler#getMode()
 	 */
 	public String getMode() {
 		return SYNC;
 	}
 
 }
 
