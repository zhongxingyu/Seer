 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package gov.nih.nci.caxchange.servicemix.bean.routing;
 
 import gov.nih.nci.caXchange.CaxchangeErrors;
 import gov.nih.nci.caXchange.outbound.GridInvocationException;
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.caxchange.security.CaXchangePrincipal;
 import gov.nih.nci.caxchange.servicemix.bean.CaXchangeMessagingBean;
 import gov.nih.nci.caxchange.servicemix.bean.cache.UserProxyCache;
 
 import java.io.StringReader;
 import java.security.Principal;
 
 import javax.jbi.messaging.Fault;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.security.auth.Subject;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.util.MessageUtil;
 import org.cagrid.gaards.cds.client.DelegatedCredentialUserClient;
 import org.cagrid.gaards.cds.delegated.stubs.types.DelegatedCredentialReference;
 import org.cagrid.gaards.cds.stubs.types.CDSInternalFault;
 import org.cagrid.gaards.cds.stubs.types.DelegationFault;
 import org.cagrid.gaards.cds.stubs.types.PermissionDeniedFault;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.GlobusCredentialException;
 import org.globus.wsrf.encoding.DeserializationException;
 /**
  * It includes two components: a pipeline, which invokes a bean component that calls 
  * delegation service for authorization and authentication
  *  
  * @author Ekagra Software Technologies
  *
  */
 public class InvokeDelegationServiceBean extends CaXchangeMessagingBean {
 	
 	private String certificateFilePath, keyFilePath;
 	private UserProxyCache userProxyCache;
 
 	private static GlobusCredential hostCredential = null;
 	
 	public static Logger logger = LogManager.getLogger(InvokeDelegationServiceBean.class);
 	
 	/**
      * Default constructor
      */
 	public InvokeDelegationServiceBean() {
 
 	}
 	/**
 	 * This method creates the Host credentials from the certificate and the key file provides in 
 	 * caxchange.property file
 	 * @param 
 	 * @return
 	 * @throws GridInvocationException
 	 */	
 	public void init() throws GridInvocationException{
 		try{
 			hostCredential = new GlobusCredential(certificateFilePath, keyFilePath);
 		}catch (GlobusCredentialException e){
 			throw new GridInvocationException(
 					"Unable to create Host Credentials from the Certificate and Key File", e);
 		}
 	}
 	
 	/**
 	 * When the POJO Implements the MessageExchangeListener interface of servicemix-bean component
 	 * all the exchange will be dispatched to the onMessageExchange() method
 	 * @param exchange
 	 * @return
 	 * @throws MessagingException
 	 */	
 	public void processMessageExchange(MessageExchange exchange)
 	throws MessagingException {
 		long timeBefore = new java.util.Date().getTime();
 		NormalizedMessage in = exchange.getMessage("in");
		in.setContent(caXchangeDataUtil.getDOMSource());
 		NormalizedMessage out = exchange.createMessage();
 		try {
 			invokeDelegationService(exchange);
 		}
 		catch (CDSInternalFault e){
 			Fault fault = getFault(CaxchangeErrors.CDS_INTERNAL_FAULT,"Internal error invoking delegation service."+e.getMessage(), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;			
 		}catch (DelegationFault e){
 			Fault fault = getFault(CaxchangeErrors.DELEGATION_FAULT,"Delegation fault invoking delegation service."+e.getMessage(), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;	
 		}catch (PermissionDeniedFault e){
 			Fault fault = getFault(CaxchangeErrors.PERMISSION_DENIED_FAULT,"Permission denied fault invoking delegation service."+e.getMessage(), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;	
 		}catch (InvalidDelegatedCredentialsException idc){
 			Fault fault = getFault(CaxchangeErrors.PERMISSION_DENIED_FAULT,"Invalid delegated credentials grid Id."+idc.getMessage(), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;				
 		}
 		catch (Throwable e) {
 			Fault fault = getFault(CaxchangeErrors.UNKNOWN,"Error invoking delegation service."+e.getMessage(), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;
 		}
 		MessageUtil.transfer(in, out);
         out.setContent(caXchangeDataUtil.getDOMSource());
 		exchange.setMessage(out, "out");
 		channel.send(exchange);
 		long timeAfter = new java.util.Date().getTime();
 		logger.debug("Timer for set subject service:"+(timeAfter-timeBefore));
 	}
 
 	/**
 	 * invokes the delegation service and sets the subject in the message.
 	 * @param exchange
 	 * @return
 	 * @throws Exception
 	 */
 	public void invokeDelegationService(MessageExchange exchange)
 	throws Exception {
 		try {
 			GlobusCredential userCredential=null;
             userCredential = getCachedGlobusCredential(exchange);
             if (userCredential==null) {
             	logger.debug("User credential not found in cache.");
             	userCredential = getGlobusCredentialFromDelgationService(exchange);
             }
             if (!(userCredential.getIdentity().equals(caXchangeDataUtil.getGridIdentifier()))) {
                throw new InvalidDelegatedCredentialsException("Identity of the delegated grid user:"+userCredential.getIdentity()+ " does not match the identity of the grid user:"+caXchangeDataUtil.getGridIdentifier());          	
             }
  			Subject subject = new Subject();
 
 			CaXchangePrincipal principal = new CaXchangePrincipal();
 			principal.setName(userCredential.getIdentity());
 			subject.getPrincipals().add((Principal) principal);
 
 			subject.getPrivateCredentials().add(userCredential);
 			NormalizedMessage in = exchange.getMessage("in");
 			in.setSecuritySubject(subject);
 			logger.debug("usercredential="+userCredential.toString());
 		
 		} catch (Exception e) {
 			logger.error("Error authenticating", e);
 			throw e;
 		}
 
 	}
 	
 	public GlobusCredential getCachedGlobusCredential(MessageExchange exchange)
 	throws Exception {
 		String delegationEPR = null;
 	    try {
 			delegationEPR = caXchangeDataUtil.getDelegatedCredentialReference();
 			GlobusCredential userCredentials = null;
 			if (userProxyCache !=null) {
 				userCredentials = userProxyCache.get(delegationEPR);
 			}
 			try {
 				if (userCredentials !=null) {
 					userCredentials.verify();
 				}
 			}catch(GlobusCredentialException gce) {
 				userProxyCache.remove(delegationEPR);
 				userCredentials = null;
 			}
 
 			return userCredentials;
 	    }catch(Exception e) {
 	    	logger.error("Error retreiving user credentials from cache for:"+delegationEPR,e);
 	    	throw new Exception("Error retreiving user credentials from cache for:"+delegationEPR,e);
 	    }
 	}
 	
 	public GlobusCredential getGlobusCredentialFromDelgationService(MessageExchange exchange)
 	throws Exception {
 	  try {
 		String delegationEPR = caXchangeDataUtil.getDelegatedCredentialReference();
 		DelegatedCredentialReference delegatedCredentialReference = null;
 		try{
 			logger.debug("**** Delegated EPR:"+delegationEPR);
 			delegatedCredentialReference =  
 				(DelegatedCredentialReference)
 				                     Utils.deserializeObject(new StringReader(delegationEPR), DelegatedCredentialReference.class, DelegatedCredentialUserClient.class.getResourceAsStream("client-config.wsdd"));
 		}catch (DeserializationException e){
 			throw new GridInvocationException(
 					"Unable to deserialize the Delegation Reference", e);
 		}
 		DelegatedCredentialUserClient delegatedCredentialUserClient = null;
 		try{
 			logger.debug("**** delegatedCredentialReference.getEndpointReference"+delegatedCredentialReference.getEndpointReference());
 			delegatedCredentialUserClient = new DelegatedCredentialUserClient(
 					delegatedCredentialReference, hostCredential);
 		}catch (Exception e){
 			throw new GridInvocationException(
 					"Unable to Initialize the Delegation Lookup Client", e);
 		}
 		GlobusCredential userCredential;
 		try{
 			long startTime = new java.util.Date().getTime();
 			userCredential = delegatedCredentialUserClient
 			.getDelegatedCredential();
 			long endTime = new java.util.Date().getTime();
 			logger.debug("Time for delegation service:"+ (endTime-startTime));
 		}catch (CDSInternalFault e){
 			logger.error("Internal error getting delegated credentials.",e); 
 			throw e;
 		}catch (DelegationFault e){
 			logger.error("Delegation fault occurred getting delegated credentials.", e);
 			throw e;
 		}catch (PermissionDeniedFault e){
 			logger.error("Permission denied fault getting delegated credentials.", e);
 			throw e;
 		}
 		if (userProxyCache !=null) {
 			userProxyCache.put(delegationEPR, userCredential);
 		}		
 		return userCredential;
 	  }catch(Exception e) {
 		  logger.error("Error getting user credentials from the delegation service.",e);
 		  throw new Exception("Error getting user credentials from the delegation service.",e);
 	  }
 	}
 	
    /*No references*/
 	public String getCertificateFilePath() {
 		return certificateFilePath;
 	}
 	/*No references*/
 	public void setCertificateFilePath(String certificateFilePath) {
 		this.certificateFilePath = certificateFilePath;
 	}
 	/*No references*/
 	public String getKeyFilePath() {
 		return keyFilePath;
 	}
 	/*No references*/
 	public void setKeyFilePath(String keyFilePath) {
 		this.keyFilePath = keyFilePath;
 	}
 	public UserProxyCache getUserProxyCache() {
 		return userProxyCache;
 	}
 	public void setUserProxyCache(UserProxyCache userProxyCache) {
 		this.userProxyCache = userProxyCache;
 	}
 	
 	public class InvalidDelegatedCredentialsException extends Exception {
 
 		public InvalidDelegatedCredentialsException() {
 			super();
 			// TODO Auto-generated constructor stub
 		}
 
 		public InvalidDelegatedCredentialsException(String message,
 				Throwable cause) {
 			super(message, cause);
 			// TODO Auto-generated constructor stub
 		}
 
 		public InvalidDelegatedCredentialsException(String message) {
 			super(message);
 			// TODO Auto-generated constructor stub
 		}
 
 		public InvalidDelegatedCredentialsException(Throwable cause) {
 			super(cause);
 			// TODO Auto-generated constructor stub
 		}
 		
 	}
 
 }
