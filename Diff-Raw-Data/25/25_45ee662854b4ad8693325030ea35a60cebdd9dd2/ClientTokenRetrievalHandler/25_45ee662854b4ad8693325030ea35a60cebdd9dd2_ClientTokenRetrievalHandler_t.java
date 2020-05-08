 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *    
  * http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.securitycommon.sif.impl.handlers;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
 import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
 import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
 import org.ebayopensource.turmeric.runtime.common.impl.utils.ReflectionUtils;
 import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceId;
 import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
 import org.ebayopensource.turmeric.securitycommon.intf.provider.TokenProvider;
 
 /**
  * Generic Handler to instrument token retrieval in SOA framework.
  * 
  * @author prjande
  */
 
 public class ClientTokenRetrievalHandler extends BaseHandler {
 
 	/**
 	 * Token retrieve style. Can be "dynamic" or "static". "static" means fixed
 	 * token. "dynamic" means different token is used based on the consumer Id.
 	 */
 	private volatile String m_tokenRetrievalStyle;
 	
 	/**
 	 * logger.
 	 */
 	private static Logger s_logger = LogManager
 			.getInstance(ClientTokenRetrievalHandler.class);
 	
 	/**
 	 * The implementation instance for the token provider.
 	 */
 	private volatile TokenProvider m_tokenProviderImpl;
 	
 	/**
 	 * The key used to store the token retrieval style value in the options.
 	 */
 	private static final String TOKEN_RETRIEVAL_STYLE = "token-retrieval-style";
 	
 	/**
 	 * The key used to store consumer Id in the options.
 	 */
 	private static final String CONSUMER_ID = "consumer-id";
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler#init(org.ebayopensource.turmeric.runtime.common.pipeline.Handler.InitContext)
 	 */
 	@Override
 	public void init(InitContext ctx) throws ServiceException {
 		super.init(ctx);
 
 		// only allow for client side
 		HandlerPreconditions.checkClientSide(ctx,
 				ClientTokenRetrievalHandler.class);
 
 		Map<String, String> options = ctx.getOptions();
 
 		// get the token retrieval style info
 		m_tokenRetrievalStyle = options.get(TOKEN_RETRIEVAL_STYLE);
 
 		if (m_tokenRetrievalStyle == null || m_tokenRetrievalStyle.isEmpty()) {
 			logCalMsg("Token retrieval style not specified.");
 			throw new ServiceException(
 					ErrorDataFactory
 							.createErrorData(
 									ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_INIT_FAILED,
 									ErrorConstants.ERRORDOMAIN.toString(),
 									new Object[] { "Token retrieval style not specified." }));
 		}
 
 		if (!("static".equalsIgnoreCase(m_tokenRetrievalStyle))
 				&& !("dynamic".equalsIgnoreCase(m_tokenRetrievalStyle))) {
 			logCalMsg("Invalid token retrieval style.");
 			throw new ServiceException(
 					ErrorDataFactory
 							.createErrorData(
 									ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_INIT_FAILED,
 									ErrorConstants.ERRORDOMAIN.toString(),
 									new Object[] { "Invalid token retrieval style." }));
 		}
 
 		// load the provider class
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		String tokenProviderClzName = options
 				.get("token-provider-impl-class-name");
 
 		if (m_tokenProviderImpl == null) {
 			TokenProvider tokenProviderImpl = null;
 			try {
 				tokenProviderImpl = ReflectionUtils.createInstance(
 						tokenProviderClzName, TokenProvider.class, cl);
 			} catch (Exception e) {
 				logCalMsg("Unable to load TokenProvider Impl class: " + e);
 				throw new ServiceException(
 						ErrorDataFactory
 								.createErrorData(
 										ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_INIT_FAILED,
 										ErrorConstants.ERRORDOMAIN.toString(),
 										new Object[] { "Unable to load TokenProvider Impl class" }));
 			}
 			synchronized (ClientTokenRetrievalHandler.class) {
 				if (m_tokenProviderImpl == null) {
 					m_tokenProviderImpl = tokenProviderImpl;
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler#invoke(org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext)
 	 */
 	@Override
 	public void invoke(MessageContext ctx) throws ServiceException {
 
 		String consumerId = ctx.getRequestMessage().getTransportHeader(
 				SOAHeaders.CONSUMER_ID);
 
 		if ("dynamic".equalsIgnoreCase(m_tokenRetrievalStyle)
 				&& consumerId == null) {
 			throw new ServiceException(
 					ErrorDataFactory
 							.createErrorData(
 									ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_INIT_FAILED,
 									ErrorConstants.ERRORDOMAIN.toString(),
 									new Object[] { "Consumer Id cannot be NULL." }));
 		}
 
 		// pass token retrieval style & consumer id
 		Map<String, String> options = new ConcurrentHashMap<String, String>();
 		options.put(TOKEN_RETRIEVAL_STYLE, m_tokenRetrievalStyle);
 
 		if (null != consumerId) {
 			options.put(CONSUMER_ID, consumerId);
 		}
 
 		if (m_tokenProviderImpl == null) {
 			logCalMsg("TokenProvider Class not loaded !");
 			throw new ServiceException(
 					ErrorDataFactory
 							.createErrorData(
 									ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_ERROR,
 									ErrorConstants.ERRORDOMAIN.toString(),
 									new Object[] { "TokenProvider Class not loaded." }));
 		}
 
 		String tokenType = m_tokenProviderImpl.getTokenType();
 		String token = m_tokenProviderImpl.getToken(options);
 
 		if (ctx.getRequestMessage() == null) {
 			logCalMsg("System Error !! Message context not initialized.");
 			throw new ServiceException(
 					ErrorDataFactory
 							.createErrorData(
 									ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_ERROR,
 									ErrorConstants.ERRORDOMAIN.toString(),
 									new Object[] { "Message context not initialized." }));
 		}
 		ctx.getRequestMessage().setTransportHeader(tokenType, token);
 	}
 
 	/**
 	 * Log cal msg.
 	 *
 	 * @param msg the msg
 	 */
 	private static void logCalMsg(String msg) {
 		if (s_logger.isLoggable(Level.INFO)) {
 			s_logger.log(Level.SEVERE, "PolicyEnforcementDebugInfo : " + msg);
 		}
 	}
 }
