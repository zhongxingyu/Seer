 /**
  * 
  * Copyright 2002-2007 NCHELP
  * 
  * Author:		Tim Bornholtz, The Bornholtz Group, Inc.
  *              Priority Technologies, Inc.
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
 
 
 package org.nchelp.meteor.message;
 
 import java.security.PrivateKey;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.provider.AccessProvider;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.security.Signature;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 
 /**
  * Super class for the Meteor Index Request and the Meteor Data Request.
  * 
  * @author timb
  *
  */
 public class MeteorRequest extends MeteorMessage {
 	private final transient Log log = LogFactory.getLog(this.getClass());
 
 	protected SecurityToken security;
 	protected AccessProvider accessProvider;
 	protected String ssn;
 	
 	// As of Meteor 3.3 the request is signed. So we need to store
 	// the whole document so we can validate it.
 	protected Document doc;
 
 
 	protected transient PrivateKey privateKey;
 
 	/**
 	 * Gets the accessProvider.
 	 * @return Returns a AccessProvider
 	 */
 	public AccessProvider getAccessProvider() {
 		return accessProvider;
 	}
 
 	/**
 	 * Sets the accessProvider.
 	 * @param accessProvider The accessProvider to set
 	 */
 	public void setAccessProvider(AccessProvider accessProvider) {
 		this.accessProvider = accessProvider;
 	}
 
 	/**
 	 * Method getSSN.
 	 * @return String
 	 */
 	public String getSSN() {
 		return this.ssn;
 	}
 
 	/**
 	 * Set the SSN for the request
 	 * @param ssn String containing the SSN
 	 */
 	public void setSSN(String ssn) {
 		this.ssn = ssn;
 	}
 
 
 	/**
 	 * Gets the SecurityToken associated with the Access Provider
 	 * making the request.
 	 * @return Returns the SecurityToken that identifies the sender of the request
 	 */
 	public SecurityToken getSecurityToken() {
 		return security;
 	}
 
 	/**
 	 * Sets the SecurityToken for the Access Provider that is making the request
 	 * @param security The SecurityToken of this Access Provider
 	 * @throws DataException 
 	 */
 	public void setSecurityToken(SecurityToken security) throws DataException {
 		DistributedRegistry reg = DistributedRegistry.singleton();
 		boolean result = reg.authenticateProvider(security);
 		if(! result){
 			throw new DataException("Assertion is not valid");
 		}
 		this.security = security;
 	}
 
 	/**
 	 * Mark the message for signing.  This may not actually sign the request when this method is called,
 	 * but the message is guaranteed to be signed by the time it is serialized.
 	 * @throws SignatureException
 	 */
 	public void sign() throws SignatureException {
 		DistributedRegistry reg = DistributedRegistry.singleton();
 		this.privateKey = reg.getPrivateKey();
 		
 	}
 	
 	/**
 	 * @return boolean True if the message signature is valid.  If the message is not signed
 	 * then this will always return false.
 	 */
 	public boolean validateMessage(){
 		return this.validateMessage(true);
 	}
 	
 	/**
 	 * @param requireSignature  boolean if the message must be signed.  If this is is true and the message is not signed this method will return false 
 	 * @return boolean if the message signature is valid
 	 */
 	public boolean validateMessage(boolean requireSignature){
 		if(this.doc == null){
			log.error("Original Index Request is null. Unable to validate the signature");
 			return false;
 		}
 		
 		if(this.accessProvider == null){
 			log.error("AccessProvider not found in original request.  Unable to validate the signature");
 		}
 		
 		try {
 			boolean isSigned = Signature.isSigned(doc);
 			
 			//if it isn't signed but signatures are required then this will return false
 			if(requireSignature && ! isSigned){
 				return false;
 			}
 			
 			// if it isn't signed and signatures aren't required then return true
 			if(! requireSignature && ! isSigned){
 				return true;
 			}
 			
 		} catch (SignatureException e) {
 			log.error("Error checking if document is signed", e);
 			return false;
 		}
 		
 		DistributedRegistry reg = DistributedRegistry.singleton();
 		return reg.validateSignature(doc, this.accessProvider.getID());
 		
 	}
 
 
 
 }
 
