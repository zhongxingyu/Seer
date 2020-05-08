 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *    
  *******************************************************************************/
 package org.ebayopensource.turmeric.services.authenticationservice.authenticator;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ebayopensource.turmeric.authentication.provider.Authenticator;
 import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
 import org.ebayopensource.turmeric.utils.DomParseUtils;
 import org.ebayopensource.turmeric.utils.ReflectionUtils;
 import org.ebayopensource.turmeric.utils.config.BaseConfigManager;
 import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * The Class AuthenticatorStore.
  */
 public class AuthenticatorStore extends BaseConfigManager {
 
      private static  Logger s_logger = LogManager.getInstance(AuthenticatorStore.class);
 	
 	 private static final String CONFIG_FILENAME = "AuthenticationServiceConfig.xml";
 	 private static final String SCHEMA_FILENAME = "AuthenticationServiceConfig.xsd";
 	 private static final String ROOT_ELEMENT = "authentication-service-config";
 	 private static AuthenticatorStore s_instance = null;
 	 private Map<String, AuthenticatorConfig> m_authenticatorConfigMap = new HashMap<String, AuthenticatorConfig>();
 	 private Map<String, Authenticator> m_authenticatorMap = new HashMap<String, Authenticator>();
 	 private boolean m_initFlag;
 	 
 	 // disable creating instances
 	 private AuthenticatorStore() {
 		 super();
 	}
 	 
 	 /**
  	 * Gets the single instance of AuthenticatorStore.
  	 *
  	 * @return single instance of AuthenticatorStore
  	 */
  	public static AuthenticatorStore getInstance() {
 	    if (s_instance == null) {
	    	AuthenticatorStore s = new AuthenticatorStore();
	    	s.initialize();
	    	s_instance = s;
 	    }
 	    	return s_instance;
 	 } 
 
 	 private void initialize() {
 		 // load the config file, and load the authenticators
 		 try {
 			 // call super's init() to load config
 			init();
 		
 			// create instances for the Authenticators and initialize them
 			loadAuthenticators();
 			
 		 } catch (ConfigurationException e) {
 				s_logger.log(Level.SEVERE, 
 						"Failed to load AuthenticationServiceConfig.xml", e);
 				m_initFlag = true;
 		 }
 	 }
 
 	private void loadAuthenticators() {
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		for (AuthenticatorConfig authnConfig : m_authenticatorConfigMap.values()) {
 			String clzName = authnConfig.getClassname();
             String authnMethod = authnConfig.getSupportedAuthenticationMethod();
             if (authnMethod != null) {
                 authnMethod = authnMethod.toLowerCase();
                 // create an instance of authenticator, pass in authnMethod to constructor
                 Authenticator authenticator = null;
                 try {
                     authenticator = ReflectionUtils.createInstance(
                             clzName, Authenticator.class, cl, 
                             new Class[]{String.class}, 
                             new Object[]{authnMethod});
                     authenticator.initialize();
                 } catch (Exception e) {
                     s_logger.log(Level.SEVERE, "exception during authenticator init: " + clzName, e);
                 }
                 if(authenticator != null) {
                     // initialize each authenticator
                     m_authenticatorMap.put(authnMethod, authenticator);
                 }
             }
 		}
 	}
 	
 	/**
 	 * Gets the authenticator.
 	 *
 	 * @param authnMethod the authn method
 	 * @return the authenticator
 	 * @throws ServiceException the service exception
 	 */
 	public Authenticator getAuthenticator(String authnMethod) throws ServiceException {
 		if (m_initFlag) {
 			throw new ServiceException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_SECURITY_AUTHN_CONFIG_INIT_ERROR, 
 					ErrorConstants.ERRORDOMAIN));
 		}
 		
 		return m_authenticatorMap.get(authnMethod.toLowerCase());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.utils.config.BaseConfigManager#getConfigFileName()
 	 */
 	@Override
 	public String getConfigFileName() {
 		return CONFIG_FILENAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.utils.config.BaseConfigManager#getRootElementName()
 	 */
 	@Override
 	public String getRootElementName() {		
 		return ROOT_ELEMENT;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.utils.config.BaseConfigManager#getSchemaFileName()
 	 */
 	@Override
 	public String getSchemaFileName() {
 		return SCHEMA_FILENAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.utils.config.BaseConfigManager#map(org.w3c.dom.Element)
 	 */
 	@Override
 	public void map(Element rootData) throws ConfigurationException {
 		
 		if (rootData == null) return;
 		
 		String filename = getConfigFileName();
 		try {
             Element supportedAuthenticatorContainer = DomParseUtils.getSingleElement(filename, rootData, "supported-authenticator");
             if (supportedAuthenticatorContainer != null) {
                 NodeList authenticators = DomParseUtils.getImmediateChildrenByTagName(supportedAuthenticatorContainer, "authenticator");
                 for (int i = 0; i < authenticators.getLength(); i++) {
                     Element authenticator = (Element) authenticators.item(i);
                     // add new fields to be mapped here..
                     String name = DomParseUtils.getRequiredAttribute(filename, authenticator, "name");
                     String classname = DomParseUtils.getElementText(filename, authenticator, "class-name");
                     String authnMethod = DomParseUtils.getElementText(filename, authenticator, "authentication-method");
                     AuthenticatorConfig authenticatorConfigOut = new AuthenticatorConfig();
                     authenticatorConfigOut.setName(name);
                     authenticatorConfigOut.setClassname(classname);
                     authenticatorConfigOut.setSupportedAuthenticationMethod(authnMethod);
                     //Map<String, String> optionsMap = authenticatorConfig.getOptions();
                     //OptionList options = DomParseUtils.getOptionList(filename, authenticator, "authn-options");
                     //DomParseUtils.storeNVListToHashMap(filename, options, optionsMap);
         
                     m_authenticatorConfigMap.put(name, authenticatorConfigOut);
                 }
             }
         } catch(Exception e) {
             throw new ConfigurationException("Error in mapping authn service config: " + e.getMessage(), e);
         }
 	}
 	
 
 }
