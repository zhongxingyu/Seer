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
 
 import gov.nih.nci.caXchange.CaxchangeConstants;
 import gov.nih.nci.caXchange.outbound.GridInvocationException;
 import gov.nih.nci.caXchange.outbound.GridSU;
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.caxchange.security.CaXchangePrincipal;
 import gov.nih.nci.caxchange.servicemix.bean.util.XPathUtil;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.lang.reflect.Field;
 import java.security.Principal;
 
 import javax.annotation.Resource;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.Fault;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.security.auth.Subject;
 
 import org.apache.log4j.Category;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.servicemix.MessageExchangeListener;
 import org.apache.servicemix.jbi.util.MessageUtil;
 import org.cagrid.gaards.cds.client.CredentialDelegationServiceClient;
 import org.cagrid.gaards.cds.client.DelegatedCredentialUserClient;
 import org.cagrid.gaards.cds.delegated.stubs.types.DelegatedCredentialReference;
 import org.cagrid.gaards.cds.stubs.types.CDSInternalFault;
 import org.cagrid.gaards.cds.stubs.types.DelegationFault;
 import org.cagrid.gaards.cds.stubs.types.PermissionDeniedFault;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.GlobusCredentialException;
 import org.globus.wsrf.encoding.DeserializationException;
 
 public class InvokeDelegationServiceBean implements MessageExchangeListener {
 
 	@Resource
 	private DeliveryChannel channel;
 
 	public static final String USERNAME_XPATH = "/caxchange:caXchangeRequestMessage/metaData/credentials/userName";
 	public static final String GROUPNAME_XPATH = "/caxchange:caXchangeRequestMessage/metaData/credentials/groupName";
 	public static final String GRIDIDENTIFIER_XPATH = "/caxchange:caXchangeRequestMessage/metaData/credentials/gridIdentifier";
 
 	public static final String USERNAME = "userName";
 	public static final String GROUPNAME = "groupName";
 	public static final String GRIDIDENTIFIER = "gridIdentifier";
 	
 	private String certificateFilePath, keyFilePath;
 
 	public static Logger logger = LogManager
 	.getLogger(InvokeDelegationServiceBean.class);
 	
 	public void onMessageExchange(MessageExchange exchange)
 	throws MessagingException {
 		if (exchange.getStatus().equals(ExchangeStatus.DONE)) {
 			return;
 		}
 		if (exchange.getStatus().equals(ExchangeStatus.ERROR)) {
 			return;
 		}
 		
		
		

		
		// HACK: modify exchange object to log the complete content
		
		Field field;
		try {
			field = exchange.getClass().getField("maxMsgDisplaySize");
			field.setAccessible(true);
		    field.set(exchange, new Integer(10000));
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchFieldException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// HACK ENDS
		

		logger.debug("Received exchange: "+ exchange);
 		NormalizedMessage in = exchange.getMessage("in");
 		NormalizedMessage out = exchange.createMessage();
 		
 		
 		try {
 			invokeDelegationService(exchange);
 		} catch (Exception e) {
 			logger.error("Error calling delegation service.", e);
 			Fault fault = exchange.createFault();
 			MessageUtil.transfer(in, fault);
 			fault.setProperty(CaxchangeConstants.ERROR_CODE,
 			"DELEGATION_SERVICE_ERROR");
 			fault.setProperty(CaxchangeConstants.ERROR_MESSAGE,
 			"Error occurred calling  delegation service.");
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;
 		}
 		MessageUtil.transfer(in, out);
 		exchange.setMessage(out, "out");
 		channel.send(exchange);
 	}
 
 	/**
 	 * invokes the delegation service and sets the subject in the message.
 	 * 
 	 * @param exchange
 	 * @throws Exception
 	 */
 	public void invokeDelegationService(MessageExchange exchange)
 	throws Exception {
 		try {
 			NormalizedMessage in = exchange.getMessage("in");
 	
 			XPathUtil util = new XPathUtil();
 			util.setIn(in);
 			util.initialize();
 			
 			//TODO: this is a temporary arrangement for Lab Loader
 			if("CT_LAB_DATA".equals(util.getMessageType()))
 				return;
 
 			GlobusCredential hostCredential = null;
 			
 			String delegationEPR = util.getDelegatedCredentialReference();
 			
 			try{
 				hostCredential = new GlobusCredential(certificateFilePath, keyFilePath);
 			}catch (GlobusCredentialException e){
 				throw new GridInvocationException(
 						"Unable to create Host Credentials from the Certificate and Key File", e);
 			}
 			
 			DelegatedCredentialReference delegatedCredentialReference = null;
 			try{
 				
 				delegatedCredentialReference =  
 					(DelegatedCredentialReference)
 					                     Utils.deserializeObject(new StringReader(delegationEPR), DelegatedCredentialReference.class, CredentialDelegationServiceClient.class.getResourceAsStream("client-config.wsdd"));
 
 			}catch (DeserializationException e){
 				throw new GridInvocationException(
 						"Unable to deserialize the Delegation Reference", e);
 			}
 
 			DelegatedCredentialUserClient delegatedCredentialUserClient = null;
 
 			try{
 				delegatedCredentialUserClient = new DelegatedCredentialUserClient(
 						delegatedCredentialReference, hostCredential);
 			}catch (Exception e){
 				throw new GridInvocationException(
 						"Unable to Initialize the Delegation Lookup Client", e);
 			}
 
 			GlobusCredential userCredential;
 
 			try{
 				userCredential = delegatedCredentialUserClient
 				.getDelegatedCredential();
 			}catch (CDSInternalFault e){
 				throw new GridInvocationException(
 						"Error retrieve the Delegated Credentials", e);
 			}catch (DelegationFault e){
 				throw new GridInvocationException(
 						"Error retrieve the Delegated Credentials", e);
 			}catch (PermissionDeniedFault e){
 				throw new GridInvocationException(
 						"Error retrieve the Delegated Credentials", e);
 			}
 
 			Subject subject = new Subject();
 
 			CaXchangePrincipal principal = new CaXchangePrincipal();
 			principal.setName(userCredential.getIdentity());
 			subject.getPrincipals().add((Principal) principal);
 
 			subject.getPrivateCredentials().add(userCredential);
 			in.setSecuritySubject(subject);
 			
 			logger.debug("usercredential="+userCredential.toString());
 		
 		} catch (Exception e) {
 			logger.error("Error authenticating", e);
 			throw e;
 		}
 
 	}
 
 	public String getCertificateFilePath() {
 		return certificateFilePath;
 	}
 
 	public void setCertificateFilePath(String certificateFilePath) {
 		this.certificateFilePath = certificateFilePath;
 	}
 
 	public String getKeyFilePath() {
 		return keyFilePath;
 	}
 
 	public void setKeyFilePath(String keyFilePath) {
 		this.keyFilePath = keyFilePath;
 	}
 
 }
