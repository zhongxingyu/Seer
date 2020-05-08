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
 
 package org.nchelp.meteor.provider.access;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.DataProviderThread;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.Messages;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.*;
 import org.w3c.dom.Document;
 
 
 
 /**
  *   Class IndexQueryService.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Meteor 1.0
  */
 public class IndexQueryService {
 	private static Cache dProviderCache = new Cache();
 
 	private static Cache meteorDataCache = new Cache();
 
 	private final Log log = LogFactory.getLog(this.getClass());
 	
 	private HttpSession session = null;
 	private MeteorParameters parameters = null;
 	private String response = null;
 	
 	private AuthenticationLevelException authLevelException = null;
 	
 	public static final String SESSION_TOTAL = "TotalAwards";
 	public static final String SESSION_REMAINING = "RemainingAwards";
 	public static final String SESSION_STATUS = "StatusMessage";
 	
 
 	private ResponseData responseData = null;
 
 	/**
 	 * Public Constructor
 	 * This will initialize the aggregation objects
 	 * 
 	 * 
 	 */
 	public IndexQueryService(){
 				
 	}
 	
 	private void callDataProviders(List  providers){
 		int defaultTimeoutValue = 20000;
 		
 		ThreadGroup tg = new ThreadGroup("Meteor Data Providers ThreadGroup");
 		
 		Resource res = ResourceFactory.createResource("accessprovider.properties");
 		
 		// Get the timeout value with the default of 20 seonds
 		String strTimeout = res.getProperty("dataprovider.timeout", "20000");
 		int timeout = defaultTimeoutValue;
 		
 		try{
 			timeout = Integer.parseInt(strTimeout);
 		} catch(NumberFormatException e){
 			// If this throws an exception because the number
 			// wasn't correct, then rest it to the default
 			log.error("dataprovider.timeout was not a numeric value.  It is: " + strTimeout);
 			timeout = defaultTimeoutValue;
 		}
 		
 		Iterator i = providers.iterator();
 		
 		if(session != null){
 			session.setAttribute(SESSION_TOTAL, String.valueOf(providers.size()));
 		}
 		
 		while(i.hasNext()){
 			DataProvider dp = (DataProvider) i.next();
 			DataProviderThread dpt = new DataProviderThread(tg, dp);
 			dpt.start();
 		}
 		
 		int elapsedTime = 0;
 		while(tg.activeCount() > 0 && elapsedTime < timeout){
 			if(session != null){
 				session.setAttribute(SESSION_REMAINING, String.valueOf(tg.activeCount()));
 			}
 			try {
 				Thread.sleep (100);
 			}
 			catch (InterruptedException e) {
 				log.error("Sleep was Interrupted!!");
 				break;
 			}
 			elapsedTime += 100;
 			
 			if(elapsedTime >= timeout){
 				log.info("Threadgroup terminating with an active count of: " + tg.activeCount());
 				
 				Thread[] t = new Thread[tg.activeCount()];
 				tg.enumerate(t);
 				for(int j = 0; j < t.length; j++){
 					Thread thread = t[j];
 					if(thread == null) continue;
 
 					if(log.isErrorEnabled()){
 						log.error("Thread '" + thread.toString() + "' is being forcefully interrupted");
 					}
 					
 					thread.interrupt();
 				}
 				if(session != null){
 					session.removeAttribute(SESSION_REMAINING);
 					session.removeAttribute(SESSION_TOTAL);
 				}
 				
 			}
 		}
 		
 	}
 	
 	private String createErrorXML(String severity, String message){
 		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 		             "<PESCXML:MeteorRsMsg xmlns:PESCXML=\"http://schemas.pescxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.pescxml.org Meteor_Schema_1-0-0.xsd\" PESCXMLVersion=\"1.0.0\">\n" +
 		             "   <MeteorDataProviderInfo>\n" +
 		             "      <MeteorDataProviderDetailInfo>\n" +
 		             "         <DataProviderType>G</DataProviderType>\n" +
 		             "         <DataProviderData>\n" +
 		             "            <Contacts/>\n" +
 		             "         </DataProviderData>\n" +
 		             "         <DataProviderAggregateTotal/>\n" + 
 		             "      </MeteorDataProviderDetailInfo>\n" +
 		             "      <MeteorDataProviderMsg>\n" +
 		             "         <RsMsg>" + message + "</RsMsg>\n" +
 		             "         <RsMsgLevel>" + severity + "</RsMsgLevel>\n" + 
 		             "      </MeteorDataProviderMsg>\n" +
 		             "   </MeteorDataProviderInfo>\n" +
 		             "</PESCXML:MeteorRsMsg>";
 
 		if(session != null){
 			session.removeAttribute(SESSION_REMAINING);
 			session.removeAttribute(SESSION_TOTAL);
 			session.removeAttribute(SESSION_STATUS);
 		}
 
 		return xml;
 	}	
 	
 	
 	/**
 	 * There are a few configuration parameters
 	 * available in the authentication.properties
 	 * that determine the functionality here.
 	 * 
 	 * First: meteor.authentication.requirepassedassertion
 	 * If this is "Yes", then the access provider
 	 * will never create its own assertion
 	 * The default is "No"
 	 * 
 	 * Second: meteor.authentication.acceptassertions
 	 * If this is "No" then this access provider
 	 * cannot be the recipient of a "bump"
 	 * The default is "Yes" 
 	 *
 	 * @param params MeteorParameters containing everything passed
 	 * into the Meteor Access Provider software
 	 * @return SecurityToken
 	 */
 	private SecurityToken getSecurityToken(MeteorParameters params) throws AssertionException{
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		
 		Resource res =
 			ResourceFactory.createResource("accessprovider.properties");
 			
 		String strRequireAssertion = res.getProperty(
 				"meteor.authentication.requirepassedassertion",
 				"No");
 		boolean requirePassedAssertion =
 			("Yes".equalsIgnoreCase(strRequireAssertion) ? true : false);
 
 		String strAcceptAssertion =	res.getProperty(
 		        "meteor.authentication.acceptassertions", "Yes");
 		boolean acceptPassedAssertion =
 			("Yes".equalsIgnoreCase(strAcceptAssertion) ? true : false);
 		
 		
 		SecurityToken token = null;
 		
 		// If a SecurityToken was passed in on the parameters
 		// then the user went through an authentication bump
 		
 		token = params.getSecurityToken();
 		
 		// If there was no assertion passed and this is allowable,
 		// then create one.
 		if( token == null && ! requirePassedAssertion){
 			try{
 				token = registry.getAuthentication(params.getRole());
 				//token.setRole(params.getRole());
 				token.setUserid(params.getCurrentUser());
 
 				String role = params.getRole();
 				if(SecurityToken.roleBORROWER.equals(role)){
 					token.setAttribute("SSN", params.getSsn());
 				}
 				
 				if(SecurityToken.roleLENDER.equals(role)) {
 					token.setAttribute(SecurityToken.roleLENDER, params.getLenderID());
 				}
 				
 			} catch(ParameterException e){
 				log.error("Invalid Role Defined", e);	
 			} catch(SignatureException e){
 				// I know that the token isn't signed here
 				// So, the only way a SignatureException 
 				// can happen here is if there was some 
 				// problem contacting the registry
 				log.error("Signature Exception getting a Security Token from the registry: " + e);
 				
 				
 				AssertionException ae = new AssertionException(Messages.getMessage("registry.noconnection"));
 				ae.setOriginalException(e);
 				throw ae;
 			}
 		// Token is passed as a parameter	
 		} else if( token != null && acceptPassedAssertion) {  
 			log.debug("Using the security token passed as a parameter");
 			if(! registry.authenticateProvider(token)) {
 				// There's really no point in going on here
 				AssertionException ae =
 					new AssertionException(Messages.getMessage("security.invalidtoken"));
 				throw ae;
 			}
 			
 		// Else we have a conflict with the allowable parameters
 		} else if( token != null && ! acceptPassedAssertion) {
 			throw new AssertionException(Messages.getMessage("security.tokennotallowed"));
 			
 		} else if( token == null && requirePassedAssertion) {
 			throw new AssertionException(Messages.getMessage("security.tokennotpassed"));
 		}
 		
 		return token;
 	}
 	
 	
 	private void query(MeteorParameters params) throws AssertionException, 
 	                                                     DataException, 
 	                                                     AuthenticationLevelException {
 
 		// check dataProviderCache
 		// if no providers in cache should 
 		// look for index providers in index provider cache
 		// if not found should look it up and put it in cache 
 
 		List dProviders = null;
 		
 		Document xmlDocument = null;
 
 
 		DistributedRegistry registry = DistributedRegistry.singleton();
 
 		SecurityToken token;
 
 		// This could throw an AssertionException
 		token = this.getSecurityToken(params);
 		params.setSecurityToken(token);
 		
 		dProviders = (List)dProviderCache.cached(params.getSsn());
 
 		if (dProviders == null ){
 			dProviders = new ArrayList();
 		}
 		
 		if (dProviders.isEmpty()) {
 			if(session != null){
 				session.setAttribute(SESSION_STATUS, "Contacting Index Provider");
 			}
 			IndexProviderService ips = new IndexProviderService();
 			try {
 				dProviders = ips.getDataProviders(registry, responseData, params);
 			} catch (IndexException e) {
 				String respXML = this.createErrorXML("W", e.getMessage());
 				respXML = XMLParser.removeDefaultNamespace(respXML);
 				
 				try {
 					responseData.addResponse(respXML);
 				} catch (ParsingException e1) {
 					// this better not happen!!
 				}
 						
 			}
 		}
 		
 		// If there are no data providers in the list at this
 		// point, then go ahead and quit.
 		if(dProviders.size() == 0){
 			String mess = Messages.getMessage("index.nodataproviders");
 			throw new DataException(mess);
 		}
 
 		// Check the current Authentication level against the
 		// minimum level required to contact every Data Provider
 		
 		// Also set the parameters for each Data Provider while
 		// I'm already looping through them
 		Iterator iterator = dProviders.iterator();
 		DataProvider dProvider = null;
 		
 		int currentLevel = token.getCurrentAuthLevel();
 		int minimumLevel = -1;
 		while(iterator.hasNext()){
 			dProvider = (DataProvider) iterator.next();
 			
 			// If the current level is less than the minimum level 
 			// then throw the exception
 			int level = dProvider.getMinimumAuthenticationLevel(token.getRole());
 			if(level > minimumLevel){ minimumLevel = level;}
 			
 			
 			/* If this particular Data Provider requires a higher
 			 * level than the user has and the user chose to
 			 * override the Minimum Authentication Level stuff then
 			 * don't call this Data Provider.
 			 * 
 			 * This will only happen when the user is requested to bump
 			 * their authentication level and they choose not to 
 			 * bump but go ahead with the data they are already 
 			 * authorized to see.  
 			 */
 			if(level > currentLevel && params.isOverrideMinimumAuthenticationLevel()){
 				iterator.remove();
 				continue;	
 			}
 			
 			dProvider.setToken(token);
 			dProvider.setParams(params);
 		}
 
 		if(currentLevel < minimumLevel && (! params.isOverrideMinimumAuthenticationLevel())){
 			String message = Messages.getMessage("security.leveltoolow");
 			AuthenticationLevelException ale = new AuthenticationLevelException(message);
 			ale.setMinimumAuthenticationLevel(minimumLevel);
 			log.warn("User does not have a sufficient authorization level");
 			throw ale;
 		}
 		
 
 		if(session != null){
 			session.setAttribute(SESSION_STATUS, "Contacting Data Providers");
 		}
 		// Make all of the calls.  This is multithreaded.
 		this.callDataProviders(dProviders);
 		
 		iterator = dProviders.iterator();
 		
 		/*
 		 * These two booleans are used to determine if a message 
 		 * should be displayed to the user
 		 */
 		boolean gotValidResponse = false;
 		boolean gotInvalidResponse = false;
 		
 		while(iterator.hasNext()){
 			dProvider = (DataProvider) iterator.next();
 			String respTemp = null;
 			
 			respTemp = dProvider.getResponse();
 			
 			if(respTemp == null){ 
 				gotInvalidResponse = true;
 				continue; 
 			}
 
 			respTemp = XMLParser.removeDefaultNamespace(respTemp);
 			
 			try {
 				responseData.addResponse(respTemp);
 				gotValidResponse = true;
 			} catch (ParsingException e) {
 				// what can I really do here
				log.error(dProvider.getName() + " [url: " + dProvider.getURL().toString() + "] returned completely bogus data that didn't parse at all!");
 			}
 		}
 		
 		String message;	
 		if(! gotValidResponse){
 			// None of them were valid responses
 			message = Messages.getMessage("data.noresponse.all");
 
 		} else if( gotInvalidResponse) {
 			message = Messages.getMessage("data.noresponse.one");
 		}
 		else {
 			// everything worked
 			message = null;
 		}
 		
 		if(message != null){
 			String xml = createErrorXML("E", message);
 			try {
 				responseData.addResponse(xml);
 			} catch (ParsingException e) {
 				// This better not ever happen with the xml
 				// that just got created!
 			}
 		}
 		
 	}
 
 	/**
 	 * Main method for Meteor queries.  This will return an XML document
 	 * that represents the information
 	 * @param params MeterParameters that describe which data should be
 	 * displayed
 	 * @return String
 	 */
 	public String query(MeteorParameters params, HttpSession session) throws AuthenticationLevelException{
 		this.setMeteorParameters(params);
 		this.setHttpSession(session);
 		
 		this.query();
 		
 		return this.getResponse();
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getResponse() throws AuthenticationLevelException {
 		if(this.authLevelException != null){
 			throw authLevelException;
 		}
 		
 		return this.response;
 	}
 
 	/**
 	 * @param session
 	 */
 	public void setHttpSession(HttpSession session) {
 		this.session = session;		
 	}
 
 	/**
 	 * @param params
 	 */
 	public void setMeteorParameters(MeteorParameters params) {
 		this.parameters = params;
 	}
 
 	public void query() {
 			
 		String   xmlString = null;
 		
 		
 		int awardID = 1;
 		
 		// Check to see if this is a subsequent request for data that we've 
 		// already looked up.  If the ForceRefresh is true, then no matter
 		// what we won't use cached data.
 		if (parameters.getForceRefresh()) {
 			log.debug("Data is cached but ForceRefresh is true so clearing cache and requerying");
 			session.removeAttribute(parameters.getSsn());
 		}
 
 		if(session != null){			
 			try {
 				responseData = (ResponseData) session.getAttribute(parameters.getSsn());
 				if(responseData != null) {
 					log.debug("Data is cached.");
 				}
 			} catch (IllegalStateException e) {
 				log.debug("IllegalStateException getting XML from the session. Continuing on without session information");
 			}
 		}
 		
 		if(responseData == null) {
 			try {
 				responseData = new ResponseData();
 				
 				// Go get the data here
 				this.query(parameters);
 			} catch (AssertionException e) {
 				this.response = createErrorXML("E", e.getMessage());
 				return;
 			} catch (DataException e) {
 				this.response = createErrorXML("E", e.getMessage());
 				return;
 			} catch (AuthenticationLevelException e) {
 				authLevelException = e;
 				return;
 			}
 
 		}
 		
 		// It better have goten filled out.  If not then don't save null in the session
 		if(responseData != null && session != null) {
 			// Save it off for subsequent queries		
 			session.setAttribute(parameters.getSsn(), responseData);	
 		}
 
 		if(responseData == null) {
 			log.error("We got to the end of processing in the query method and the responseData object is still null");
 			this.response = this.createErrorXML("E", "Error Retrieving Data");
 			return;
 		}
 
 		// Now set which awards should show
 		String action = parameters.getAction();
 		if(action == null){
 			// If the action is null, then just keep
 			// the awards that were displayed the last page
 			// when the responseData object is populated with 
 			// data, it is set to display the best source.
 		} else if(action.equalsIgnoreCase("best")) {
 			responseData.showBest();
 		} else if(action.equalsIgnoreCase("all")){
 			responseData.showAll();
 		} else if(action.equalsIgnoreCase("duplicates")){
 			List awds = parameters.getAwards();
 			responseData.showDuplicates((String)awds.get(0));
 		} else if(action.equalsIgnoreCase("add")){
 			List awds = parameters.getAwards();
 
 			Iterator i = awds.iterator();
 			while(i.hasNext()){
 				responseData.showAward((String)i.next());			
 			}
 		} else if(action.equalsIgnoreCase("remove")){
 			List awds = parameters.getAwards();
 	
 			Iterator i = awds.iterator();
 			while(i.hasNext()){
 				responseData.hideAward((String)i.next());			
 			}
 		}
 
 		xmlString = responseData.toString();
 	
 		// if it is still null we're in trouble
 		if(xmlString.equals("")) {
 			// everything worked fine, there just isn't any data!
 			String message = Messages.getMessage("data.nodata");
 			xmlString = this.createErrorXML("I", message);
 		}
 		
 		
 		//log.debug("Final XML Returned to browser: " + xmlString);
 		this.response = xmlString;		
 	}
 }
