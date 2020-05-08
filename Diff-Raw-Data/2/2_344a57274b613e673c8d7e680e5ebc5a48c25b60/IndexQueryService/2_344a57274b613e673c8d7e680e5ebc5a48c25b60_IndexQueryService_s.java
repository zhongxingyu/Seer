 /**
  * 
  * Copyright 2002 - 2007 NCHELP
  * 
  * Author:	Priority Technologies, Inc.
  *          The Bornholtz Group, Inc.
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
 
 package org.nchelp.meteor.provider.access;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.DataProviderList;
 import org.nchelp.meteor.provider.DataProviderThread;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Messages;
 import org.nchelp.meteor.util.MeteorConstants;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.Ssn;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.AssertionException;
 import org.nchelp.meteor.util.exception.AuthenticationLevelException;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.nchelp.meteor.util.exception.SignatureException;
 
 
 /**
  * Class IndexQueryService.java
  * 
  * @since Meteor 1.0
  */
 public class IndexQueryService
 {
 	//private static Cache dProviderCache = new Cache();
 	private static final Log log = LogFactory.getLog(IndexQueryService.class);
 	private HttpSession session = null;
 	private MeteorParameters parameters = null;
 	private AuthenticationLevelException authLevelException = null;
 	private ResponseData responseData = null;
 	private LoanLocator  loanLocator = new LoanLocator();
 	
 	/**
 	 * Public Constructor This will initialize the aggregation objects
 	 * 
 	 *  
 	 */
 	public IndexQueryService ()
 	{
 	}
 
 	private void callDataProviders (List providers)
 	{
 		int defaultTimeoutValue = 20000;
 
 		ThreadGroup tg = new ThreadGroup("Meteor Data Providers ThreadGroup");
 
 		Resource res = ResourceFactory.createResource("accessprovider.properties");
 
 		// Get the timeout value with the default of 20 seonds
 		String strTimeout = res.getProperty("dataprovider.timeout", "20000");
 		int timeout = defaultTimeoutValue;
 
 		try
 		{
 			timeout = Integer.parseInt(strTimeout);
 		}
 		catch (NumberFormatException ex)
 		{
 			// If this throws an exception because the number
 			// wasn't correct, then rest it to the default
 			log.error("dataprovider.timeout was not a numeric value.  It is: " + strTimeout);
 			timeout = defaultTimeoutValue;
 		}
 
 		Iterator i = providers.iterator();
 
 		if (session != null)
 		{
 			session.setAttribute(MeteorConstants.SESSION_TOTAL, String.valueOf(providers.size()));
 			session.setAttribute(MeteorConstants.SESSION_REMAINING, String.valueOf(providers.size()));
 			this.logCurrentStatus("Starting to call Data Providers");
 		}
 
 		while (i.hasNext())
 		{
 			DataProvider dp = (DataProvider)i.next();
 			DataProviderThread dpt = new DataProviderThread(tg, dp);
 			this.logCurrentStatus("Starting Data Provider " + dp.getId());
 			dpt.start();
 		}
 
 		int elapsedTime = 0;
 		while (tg.activeCount() > 0 && elapsedTime < timeout)
 		{
 			if (session != null)
 			{
 			    int numThreads = tg.activeCount();
 			    if(numThreads > providers.size()){
 			    	numThreads = providers.size();
 			    }
 				session.setAttribute(MeteorConstants.SESSION_REMAINING, String.valueOf(numThreads));
 				this.logCurrentStatus("Current Status");
 			}
 			try
 			{
 				Thread.sleep(100);
 			}
 			catch (InterruptedException ex)
 			{
 				log.error("Sleep was Interrupted!!");
 				break;
 			}
 			elapsedTime += 100;
 
 			if (elapsedTime >= timeout)
 			{
 				log.info("Threadgroup terminating with an active count of: " + tg.activeCount());
 
 				Thread[] t = new Thread[tg.activeCount()];
 				tg.enumerate(t);
 				for (int j = 0; j < t.length; j++)
 				{
 					Thread thread = t[j];
 					if (thread == null)
 						continue;
 
 					if (log.isErrorEnabled())
 					{
 						log.error("Thread '" + thread.toString() + "' is being forcefully interrupted");
 					}
 
 					thread.interrupt();
 				}
 			}
 		}
 		if (session != null)
 		{
 			session.removeAttribute(MeteorConstants.SESSION_REMAINING);
 			session.removeAttribute(MeteorConstants.SESSION_TOTAL);
 			this.logCurrentStatus("After all are done or timeout");
 		}
 		tg.interrupt();
 		if (!tg.isDestroyed())
 		{
 			try
 			{
 				if(tg != null){
 					tg.destroy();
 				}
 			}
 			catch (IllegalThreadStateException ex)
 			{
 				log.warn("ThreadGroup.destroy() Exception: " + ex.getMessage());
 			}
 		}
 		this.logCurrentStatus("End of callDataProviders");
 	}
 
 	private String createErrorXML (String severity, String message)
 	{
 		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 				+ "<PESCXML:MeteorRsMsg xmlns:PESCXML=\"http://schemas.pescxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.pescxml.org Meteor_Schema_1-0-0.xsd\" PESCXMLVersion=\"1.0.0\">\n"
 				+ "   <MeteorDataProviderInfo>\n"
 				+ "      <MeteorDataProviderDetailInfo>\n"
 				+ "         <DataProviderType>G</DataProviderType>\n"
 				+ "         <DataProviderData>\n" + "            <Contacts/>\n"
 				+ "         </DataProviderData>\n"
 				+ "         <DataProviderAggregateTotal/>\n"
 				+ "      </MeteorDataProviderDetailInfo>\n"
 				+ "      <MeteorDataProviderMsg>\n" + "         <RsMsg>" + message
 				+ "</RsMsg>\n" + "         <RsMsgLevel>" + severity + "</RsMsgLevel>\n"
 				+ "      </MeteorDataProviderMsg>\n" + "   </MeteorDataProviderInfo>\n"
 				+ "</PESCXML:MeteorRsMsg>";
 
 		if (session != null)
 		{
 			session.removeAttribute(MeteorConstants.SESSION_REMAINING);
 			session.removeAttribute(MeteorConstants.SESSION_TOTAL);
 			session.removeAttribute(MeteorConstants.SESSION_STATUS);
 		}
 
 		return xml;
 	}
 
 	/**
 	 * There are a few configuration parameters available in the
	 * authentication.properties that determine the functionality here.
 	 * 
 	 * First: meteor.authentication.requirepassedassertion If this is "Yes", then
 	 * the access provider will never create its own assertion The default is "No"
 	 * 
 	 * Second: meteor.authentication.acceptassertions If this is "No" then this
 	 * access provider cannot be the recipient of a "bump" The default is "Yes"
 	 * 
 	 * @param params
 	 *          MeteorParameters containing everything passed into the Meteor
 	 *          Access Provider software
 	 * @return SecurityToken
 	 */
 	private SecurityToken getSecurityToken (MeteorParameters params) throws AssertionException
 	{
 		DistributedRegistry registry = DistributedRegistry.singleton();
 
 		Resource res = ResourceFactory.createResource("accessprovider.properties");
 
 		String strRequireAssertion = res.getProperty("meteor.authentication.requirepassedassertion", "No");
 		boolean requirePassedAssertion = ("Yes".equalsIgnoreCase(strRequireAssertion) ? true : false);
 
 		String strAcceptAssertion = res.getProperty("meteor.authentication.acceptassertions", "Yes");
 		boolean acceptPassedAssertion = ("Yes".equalsIgnoreCase(strAcceptAssertion) ? true : false);
 
 		SecurityToken token = null;
 
 		// If a SecurityToken was passed in on the parameters
 		// then the user went through an authentication bump
 
 		token = params.getSecurityToken();
 
 		// If there was no assertion passed and this is allowable,
 		// then create one.
 		if (token == null && !requirePassedAssertion)
 		{
 			try
 			{
 				token = registry.getAuthentication(params.getRole());
 				//token.setRole(params.getRole());
 				token.setUserid(params.getCurrentUser());
 
 				String role = params.getRole();
 				if (SecurityToken.roleFAA.equals(role))
 				{
 					token.setAttribute("OrganizationType", params.getOrganizationType());
 					token.setAttribute("OrganizationIDType", params.getOrganizationIDType());
 					token.setAttribute("OrganizationID", params.getOrganizationID());
 				}
 
 				if (SecurityToken.roleBORROWER.equals(role))
 				{
 					token.setAttribute("SSN", params.getSsn());
 				}
 
 				if (SecurityToken.roleLENDER.equals(role))
 				{
 					token.setAttribute(SecurityToken.roleLENDER, params.getLenderID());
 				}
 
 			}
 			catch (ParameterException ex)
 			{
 				log.error("Invalid Role Defined", ex);
 			}
 			catch (SignatureException ex)
 			{
 				// I know that the token isn't signed here
 				// So, the only way a SignatureException
 				// can happen here is if there was some
 				// problem contacting the registry
 				log.error("Signature Exception getting a Security Token from the registry: " + ex);
 
 				AssertionException ae = new AssertionException(Messages.getMessage("registry.noconnection"));
 				ae.setOriginalException(ex);
 				throw ae;
 			}
 			// Token is passed as a parameter
 		}
 		else if (token != null && acceptPassedAssertion)
 		{
 			log.debug("Authenticating the security token passed as a parameter");
 			if (!registry.authenticateProvider(token))
 			{
 				// There's really no point in going on here
 				AssertionException ae = new AssertionException(Messages.getMessage("security.invalidtoken"));
 				throw ae;
 			}
 		}
 		// else we have a conflict with the allowable parameters
 		else if (token != null && !acceptPassedAssertion)
 		{
 			throw new AssertionException(Messages.getMessage("security.tokennotallowed"));
 		}
 		else if (token == null && requirePassedAssertion)
 		{
 			throw new AssertionException(Messages.getMessage("security.tokennotpassed"));
 		}
 
 		return token;
 	}
 
 	private void query (MeteorParameters params) throws AssertionException, DataException, AuthenticationLevelException
 	{
 		// If we're already in the process of working on a request then don't start all over
 		if(this.isProcessing()){
 			log.debug("Processing already in progress, not querying again");
 			return;
 		}
 		
 		log.debug("Processing beginning for SSN: " + params.getSsn());
 		
 		this.setProcessing(true);
 
 		if(params.isOverrideMinimumAuthenticationLevel() && this.authLevelException != null){
 			this.authLevelException = null;
 		}
 		
 		// check dataProviderCache
 		// if no providers in cache should
 		// look for index providers in index provider cache
 		// if not found should look it up and put it in cache
 
 		DataProviderList dProviders = null;
 
 		DistributedRegistry registry = DistributedRegistry.singleton();
 
 		SecurityToken token = null;
 
 		// This could throw an AssertionException
 		token = this.getSecurityToken(params);
 		params.setSecurityToken(token);
 
 		if (log.isInfoEnabled())
 		{
 			String userid = token.getUserid();
 			String role = params.getRole();
 			String ssn = params.getSsn();
 			log.info("Initiating request with the user handle: " + userid + " and role: " + role + " for the SSN: " + ssn);
 		}
 
 		if (dProviders == null || dProviders.isEmpty())
 		{
 			if (session != null)
 			{
 				session.setAttribute(MeteorConstants.SESSION_STATUS, "Contacting Index Provider");
 				session.setAttribute(MeteorConstants.SESSION_TOTAL, "1");
 				session.setAttribute(MeteorConstants.SESSION_REMAINING, "1");
 				this.logCurrentStatus("Getting started");
 			}
 			IndexProviderService ips = new IndexProviderService();
 
 			dProviders = ips.getDataProviders(registry, responseData, params, loanLocator);
 
 			this.logCurrentStatus("After ips.getDataProviders");
 			
 			List messages = dProviders.getMessages();
 			
 			Iterator msgIter = messages.iterator();
 			while (msgIter.hasNext())
 			{
 				String message = (String)msgIter.next();
 				String xml = createErrorXML("E", message);
 				try
 				{
 					log.debug("Adding error message: " + xml);
 					responseData.addResponse(xml);
 				}
 				catch (ParsingException ex)
 				{
 					// This better not ever happen with the xml
 					// that just got created!
 				}
 
 			}
 		}
 
 		// If there are no data providers in the list at this
 		// point, then go ahead and quit.
 		if (dProviders.size() == 0)
 		{
 			Map map = new HashMap();
 			if(parameters.getRole().equals(SecurityToken.roleBORROWER)){
 				// Don't display the SSN for the Borrower role
 				map.put("ssn", "");
 			} else {
 				map.put("ssn", parameters.getSsn());
 			}
 			String messageKey;
 			if(parameters.getRole().equals(SecurityToken.roleBORROWER)){
 				messageKey = "index.nodataproviders.borrower";
 			} else {
 				messageKey = "index.nodataproviders.faa";
 			}
 			String mess = Messages.getMessage(messageKey, map);
 			String xml = createErrorXML("E", mess);
 			try
 			{
 				responseData.addResponse(xml);
 			}
 			catch (ParsingException ex)
 			{
 				// This better not ever happen with the xml
 				// that just got created!
 			}
 
 			log.debug("Processing complete for SSN: " + params.getSsn() + " No data providers in the list");
 			this.setProcessing(false);
 
 			this.logCurrentStatus("No data providers left.  Done processing");
 			return;
 		}
 
 		// Check the current Authentication level against the
 		// minimum level required to contact every Data Provider
 
 		// Also set the parameters for each Data Provider while
 		// I'm already looping through them
 		Iterator iterator = dProviders.iterator();
 		DataProvider dProvider = null;
 
 		int currentLevel = token.getCurrentAuthLevel();
 		int minimumLevel = -1;
 		while (iterator.hasNext())
 		{
 			dProvider = (DataProvider)iterator.next();
 			
 			this.logCurrentStatus("checking dp: " + dProvider.getId());
 
 			// If the current level is less than the minimum level
 			// then throw the exception
 			int level = dProvider.getMinimumAuthenticationLevel(token.getRole());
 			if (level > minimumLevel)
 			{
 				minimumLevel = level;
 			}
 
 			/*
 			 * If this particular Data Provider requires a higher level than the user
 			 * has and the user chose to override the Minimum Authentication Level
 			 * stuff then don't call this Data Provider.
 			 * 
 			 * This will only happen when the user is requested to bump their
 			 * authentication level and they choose not to bump but go ahead with the
 			 * data they are already authorized to see.
 			 */
 			if (level > currentLevel && params.isOverrideMinimumAuthenticationLevel())
 			{
 				iterator.remove();
 				continue;
 			}
 
 			dProvider.setToken(token);
 			dProvider.setParams(params);
 		}
 
 		if (currentLevel < minimumLevel && (!params.isOverrideMinimumAuthenticationLevel()))
 		{
 			String message = Messages.getMessage("security.leveltoolow");
 			AuthenticationLevelException ale = new AuthenticationLevelException(message);
 			ale.setMinimumAuthenticationLevel(minimumLevel);
 			log.warn("User does not have a sufficient authorization level and Override Indicator is " + params.isOverrideMinimumAuthenticationLevel());
 			log.debug("Processing complete for ssn: " + params.getSsn() + " Insufficient authorization level");
 			this.setProcessing(false);
 
 			throw ale;
 		}
 
 		if (session != null)
 		{
 			session.setAttribute(MeteorConstants.SESSION_STATUS, "Contacting Data Providers");
 			this.logCurrentStatus("");
 		}
 		// Make all of the calls. This is multithreaded.
 		this.callDataProviders(dProviders);
 		this.logCurrentStatus("after callling this.callDataProviders");
 		
 		iterator = dProviders.iterator();
 
 		/*
 		 * These two booleans are used to determine if a message should be displayed
 		 * to the user
 		 */
 		boolean gotValidResponse = false;
 		boolean gotInvalidResponse = false;
 		if(responseData != null){
 			log.debug("responseData is not null.  Removing all prior entries");
 			responseData.removeAll();
 		} else {
 			log.debug("responseData is null.  Creating a new object");
 			responseData = new ResponseData(parameters.getSsn());
 		}
 		while (iterator.hasNext())
 		{
 			dProvider = (DataProvider)iterator.next();
 			this.logCurrentStatus("Getting the response from " + dProvider.getId());
 			
 			String respTemp = null;
 
 			respTemp = dProvider.getResponse();
 			this.logCurrentStatus("Got the response from " + dProvider.getId());
 
 			if (respTemp == null)
 			{
 				this.logCurrentStatus("Adding the response from " + dProvider.getId() + " to the loan locator block");
 
 				loanLocator.addDataProvider(dProvider);
 				gotInvalidResponse = true;
 				continue;
 			}
 
 			this.logCurrentStatus("Removing default namespace for " + dProvider.getId());
 			respTemp = XMLParser.removeDefaultNamespace(respTemp);
 			this.logCurrentStatus("Removed default namespace from " + dProvider.getId());
 
 
 			try
 			{
 				this.logCurrentStatus("Adding the response from dp " + dProvider.getId());
 				responseData.addResponse(respTemp);
 				this.logCurrentStatus("After adding the response from dp " + dProvider.getId());
 				gotValidResponse = true;
 			}
 			catch (ParsingException ex)
 			{
 				this.logCurrentStatus("Received a ParsingException from " + dProvider.getId());
 				loanLocator.addDataProvider(dProvider);
 				// what can I really do here
 				log.error(dProvider.getName() + " returned completely bogus data that didn't parse at all!");
 			}
 			catch (Throwable t){
 				this.logCurrentStatus("Caught a throwable exception from " + dProvider.getId() + ": " + t.getLocalizedMessage());
 				loanLocator.addDataProvider(dProvider);
 				log.error(dProvider.getName() + " threw an " + t.getClass().getName() + " exception: " + t.getLocalizedMessage());
 			}
 		}
 
 		//If the user is in the APCSR role then the LoanLocator block does NOT go into the response
 		if(!SecurityToken.roleAPCSR.equals(parameters.getRole())){
 			try {
 				this.logCurrentStatus("Adding the loanLocator block to the responseData");
 	
 				responseData.addResponse(loanLocator.toString());
 				this.logCurrentStatus("Finished adding the loanLocator block to the responseData");
 			} catch (ParsingException e) {
 				log.error("Error adding loan locator information to the response: " + e.getLocalizedMessage());
 			}
 		}
 		
 		String message;
 		if (!gotValidResponse)
 		{
 			// None of them were valid responses
 			message = Messages.getMessage("data.noresponse.all");
 		}
 		else if (gotInvalidResponse)
 		{
 			message = Messages.getMessage("data.noresponse.one");
 		}
 		else
 		{
 			// everything worked
 			message = null;
 		}
 
 		if (message != null)
 		{
 			String xml = createErrorXML("E", message);
 			try
 			{
 				responseData.addResponse(xml);
 			}
 			catch (ParsingException ex)
 			{
 				// This better not ever happen with the xml
 				// that just got created!
 			}
 		}
 		
 		this.setProcessing(false);
 		log.debug("Processing complete for ssn: " + params.getSsn() + " Normal completion of query");
 	}
 
 	/**
 	 * Main method for Meteor queries. This will return an XML document that
 	 * represents the information
 	 * 
 	 * @param params
 	 *          MeterParameters that describe which data should be displayed
 	 * @return String
 	 */
 	public String query (MeteorParameters params, HttpSession session) throws AuthenticationLevelException
 	{
 		this.setMeteorParameters(params);
 		this.setHttpSession(session);
 
 		this.query();
 
 		return this.getResponse();
 	}
 
 	/**
 	 * @return
 	 */
 	public String getResponse () throws AuthenticationLevelException
 	{
 		log.debug("Attempting to retrieve response from IndexQueryService");
 		if (this.authLevelException != null && ! this.parameters.isOverrideMinimumAuthenticationLevel())
 		{
 			log.debug("IndexQueryService.authLevelException is not null. getResponse() re-throwing the exception");
 			AuthenticationLevelException tmpALE = this.authLevelException;
 			this.authLevelException = null;
 			throw tmpALE;
 		}
 
 		this.checkResponseData();
 		
 		if(this.isProcessing()){
 			log.debug("Request still in progress, returning null");
 			return null;
 		} else {
 			log.debug("Request not in progress.  Retrieving data from responseData");
 		}
 		
 		if(this.responseData == null){
 			log.debug("this.responseData is null");
 			return null;
 		}
 		
 		// Now set which awards should show
 		String action = parameters.getAction();
 		if (action == null)
 		{
 			// If the action is null, then just keep
 			// the awards that were displayed the last page
 			// when the responseData object is populated with
 			// data, it is set to display the best source.
 			action = "best";
 			responseData.showBest();
 		}
 		else if (action.equalsIgnoreCase("best"))
 		{
 			responseData.showBest();
 		}
 		else if (action.equalsIgnoreCase("all"))
 		{
 			responseData.showAll();
 		}
 		else if (action.equalsIgnoreCase("duplicates"))
 		{
 			List awds = parameters.getAwards();
 			responseData.showDuplicates((String)awds.get(0));
 		}
 		else if (action.equalsIgnoreCase("add"))
 		{
 			List awds = parameters.getAwards();
 
 			Iterator i = awds.iterator();
 			while (i.hasNext())
 			{
 				responseData.showAward((String)i.next());
 			}
 		}
 		else if (action.equalsIgnoreCase("remove"))
 		{
 			List awds = parameters.getAwards();
 
 			Iterator i = awds.iterator();
 			while (i.hasNext())
 			{
 				responseData.hideAward((String)i.next());
 			}
 		}
 
 		String xmlString = responseData.toString();
 
 		// if it is still null we're in trouble
 		if (xmlString.equals(""))
 		{
 			Map map = new HashMap();
 			String ssn = Ssn.adddashes(parameters.getSsn());
 			map.put("ssn", ssn);
 			
 			// everything worked fine, there just isn't any data!
 			String message = Messages.getMessage("data.nodata", "No data found for SSN: " + ssn, map);
 			xmlString = this.createErrorXML("I", message);
 		}
 
 		log.debug("Returning a string from IndexQueryService.query() of length: " + (xmlString == null ? 0 : xmlString.length()));
 		//log.debug("Final XML Returned from IndexQueryService.query(): " + xmlString);
 
 		
 		return xmlString;
 	}
 
 	/**
 	 * @param session
 	 */
 	public void setHttpSession (HttpSession session)
 	{
 		this.session = session;
 	}
 
 	/**
 	 * @param params
 	 */
 	public void setMeteorParameters (MeteorParameters params)
 	{
 		this.parameters = params;
 	}
 	
 	public MeteorParameters getMeteorParameters (){
 		return this.parameters;
 	}
 
 	public void query ()
 	{
 		// Check to see if this is a subsequent request for data that we've
 		// already looked up. If the ForceRefresh is true, then no matter
 		// what we won't use cached data.
 		if (parameters.getForceRefresh() && session != null)
 		{
 			log.debug("Data may be cached but ForceRefresh is true so clearing cache and requerying");
 			this.responseData = null;
 			
 			// If the form is re-submitted (like may happen with the status check)
 			// we don't want to start all over.  Let the prior run finish
 			parameters.setForceRefresh(false);
 		}
 
 		this.checkResponseData();
 
 		if (responseData == null)
 		{
 			responseData = new ResponseData(parameters.getSsn());
 
 			try
 			{
 				// Go get the data here
 				this.query(parameters);
 			}
 			catch (AssertionException ex)
 			{
 				log.info("IndexQueryService.query() threw an AssertionException with the message: " + ex.getLocalizedMessage());
 
 				String error = createErrorXML("E", ex.getMessage());
 				log.debug("Adding the error to the response: " + error);
 				try {
 					responseData.addResponse(error);
 				} catch (ParsingException e) {
 					log.error("Error parsing the error XML that we just created.", e);
 				}
 			}
 			catch (DataException ex)
 			{
 				log.info("IndexQueryService.query() threw a DataException with the message: " + ex.getLocalizedMessage());
 				try {
 					responseData.addResponse(createErrorXML("E", ex.getMessage()));
 				} catch (ParsingException e) {
 					log.error("Error parsing the error XML that we just created.", e);
 				}
 			}
 			catch (AuthenticationLevelException ex)
 			{
 				authLevelException = ex;
 				log.debug("Caught an AuthenticationLevelException");
 			}
 			catch (Throwable t){
 				log.error("Unknown error occured in the query method: " + t.getLocalizedMessage());
 			}
 			finally {
 				this.setProcessing(false);
 			}
 
 		}
 
 		if (responseData == null)
 		{
 			log.error("We got to the end of processing in the query method and the responseData object is still null");
 			try {
 				responseData.addResponse(createErrorXML("E", "Error Retrieving Data"));
 			} catch (ParsingException e) {
 				log.error("Error parsing the error XML that we just created.", e);
 			}
 
 			return;
 		}
 
 	}
 
 	private void checkResponseData() {
 		if(this.responseData != null){
 			this.logCurrentStatus("responseData is not null in 'checkResponseData'");
 			return;
 		}
 		
 		if (session != null)
 		{
 			try
 			{
 				log.debug("Checking the ResponseData in the session");
 				responseData = (ResponseData)session.getAttribute(MeteorConstants.SESSION_RESPONSE);
 				log.debug("ResponseData is " + (responseData == null ? "null" : "not null for ssn: " + responseData.getSsn()));
 				if (responseData != null)
 				{
 					log.debug("Data is cached.");
 
 					// If the SSN's don't match then remove the old on.
 					if (!responseData.getSsn().equals(parameters.getSsn()))
 					{
 						log.debug("responseData SSN does not match parameter SSN.  Removing response data from cache");
 						session.removeAttribute(MeteorConstants.SESSION_RESPONSE);
 						responseData = null;
 					}
 				}
 			}
 			catch (IllegalStateException ex)
 			{
 				log.debug("IllegalStateException getting XML from the session. Continuing on without session information");
 			}
 		} else {
 			log.error("Session is null in IndexQueryService.  There is a good chance that nothing within Meteor is working correctly! Please enable session management in your container");
 		}
 	}
 
 	private boolean isProcessing(){
 		String processing = (String)session.getAttribute(MeteorConstants.SESSION_PROCESSING);
 		log.debug("Meteor request in process: '" + processing + "'");
 		return new Boolean(processing).booleanValue();
 	}
 	
 	private void setProcessing(boolean processing){
 		if(this.session != null)
 			session.setAttribute(MeteorConstants.SESSION_PROCESSING, Boolean.toString(processing));
 	}
 	
 	private void logCurrentStatus(String info){
 		if(log.isDebugEnabled()){
 			String status = (String)session.getAttribute(MeteorConstants.SESSION_STATUS);
 			String total = (String)session.getAttribute(MeteorConstants.SESSION_TOTAL);
 			String remaining = (String)session.getAttribute(MeteorConstants.SESSION_REMAINING);
 			String processing = (String)session.getAttribute(MeteorConstants.SESSION_PROCESSING);
 
 			log.debug(info + " Processing: " + processing + " Status: " + status + " " + remaining + " of " +
 					total + " remaining");
 		}
 	}
 	
 }
