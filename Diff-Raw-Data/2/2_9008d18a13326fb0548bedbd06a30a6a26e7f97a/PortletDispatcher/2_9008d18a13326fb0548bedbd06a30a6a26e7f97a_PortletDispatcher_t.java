 /**
  * Copyright 2009-2011 The Australian National University
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package au.edu.anu.portal.portlets.basiclti;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.GenericPortlet;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletException;
 import javax.portlet.PortletMode;
 import javax.portlet.PortletModeException;
 import javax.portlet.PortletPreferences;
 import javax.portlet.PortletRequest;
 import javax.portlet.PortletRequestDispatcher;
 import javax.portlet.ReadOnlyException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ValidatorException;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import au.edu.anu.portal.portlets.basiclti.helper.SakaiWebServiceHelper;
 import au.edu.anu.portal.portlets.basiclti.logic.SakaiWebServiceLogic;
 import au.edu.anu.portal.portlets.basiclti.models.Site;
 import au.edu.anu.portal.portlets.basiclti.support.CollectionsSupport;
 import au.edu.anu.portal.portlets.basiclti.support.HttpSupport;
 import au.edu.anu.portal.portlets.basiclti.support.OAuthSupport;
 import au.edu.anu.portal.portlets.basiclti.utils.Constants;
 import au.edu.anu.portal.portlets.basiclti.utils.Messages;
 
 
 
 public class PortletDispatcher extends GenericPortlet{
 
 	private final Log log = LogFactory.getLog(getClass().getName());
 	
 	// pages
 	private String viewUrl;
 	private String editUrl;
 	private String proxyUrl;
 	private String errorUrl;
 	private String configUrl;
 	
 	// params
 	private String key;
 	private String secret;
 	private String endpoint;
 
 	private List<String> allowedTools;
 	
 	private String adminUsername;
 	private String adminPassword;
 	private String loginUrl;
 	private String scriptUrl;
 	
 	//attribute mappings
 	private String attributeMappingForUsername;
 	
 	// local
 	private boolean replayForm;
 	private boolean isValid;
 	
 	//caches
 	private Cache cache;
	private static final String CACHE_NAME = "au.edu.anu.portal.portlets.cache.SakaiConnectorPortletCache";
 	
 	public void init(PortletConfig config) throws PortletException {	   
 	   super.init(config);
 	   log.info("init()");
 	   
 	   //get pages
 	   viewUrl = config.getInitParameter("viewUrl");
 	   editUrl = config.getInitParameter("editUrl");
 	   proxyUrl = config.getInitParameter("proxyUrl");
 	   errorUrl = config.getInitParameter("errorUrl");
 	   configUrl = config.getInitParameter("configUrl");
 
 	   //get params
 	   key = config.getInitParameter("key");
 	   secret = config.getInitParameter("secret");
 	   endpoint = config.getInitParameter("endpoint");
 	   adminUsername = config.getInitParameter("sakai.admin.username");
 	   adminPassword = config.getInitParameter("sakai.admin.password");
 	   loginUrl = config.getInitParameter("sakai.ws.login.url");
 	   scriptUrl = config.getInitParameter("sakai.ws.script.url");
 	   allowedTools = Arrays.asList(StringUtils.split(config.getInitParameter("allowedtools"), ':'));
 	   attributeMappingForUsername = config.getInitParameter("portal.attribute.mapping.username");
 	   
 	   //setup cache, use factory method to ensure singleton
 	   CacheManager manager = CacheManager.create();
 	   cache = manager.getCache(CACHE_NAME);
 	   
 	}
 	
 	/**
 	 * Delegate to appropriate PortletMode.
 	 */
 	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("doDispatch()");
 
 		if (StringUtils.equalsIgnoreCase(request.getPortletMode().toString(), "CONFIG")) {
 			doConfig(request, response);
 		}
 		else {
 			super.doDispatch(request, response);
 		}
 	}
 	
 	/**
 	 * Process any portlet actions
 	 */
 	public void processAction(ActionRequest request, ActionResponse response) {
 		
 		if(request.getPortletMode().equals(PortletMode.EDIT)) {
 			replayForm = false;
 			isValid = false;
 			
 			//get prefs and submitted values
 			PortletPreferences prefs = request.getPreferences();
 			String portletHeight = request.getParameter("portletHeight");
 			String portletTitle = request.getParameter("portletTitle");
 			String remoteSiteId = request.getParameter("remoteSiteId");
 			String remoteToolId = request.getParameter("remoteToolId");
 			
 			//catch a blank remoteSiteId and replay form
 			if(StringUtils.isBlank(remoteSiteId)) {
 				replayForm = true;
 				response.setRenderParameter("portletTitle", portletTitle);
 				response.setRenderParameter("portletHeight", portletHeight);
 				response.setRenderParameter("remoteSiteId", remoteSiteId);
 				//response.setRenderParameter("errorMessage", Messages.getString("error.form.nosite"));
 				return;
 			}
 			
 			//catch a blank remoteSiteid and replay form
 			if(StringUtils.isBlank(remoteToolId)) {
 				replayForm = true;
 				response.setRenderParameter("portletTitle", portletTitle);
 				response.setRenderParameter("portletHeight", portletHeight);
 				response.setRenderParameter("remoteSiteId", remoteSiteId);
 				//response.setRenderParameter("errorMessage", Messages.getString("error.form.notool"));
 				return;
 			}
 			
 			
 			
 			//portlet title could be blank, set to default
 			if(StringUtils.isBlank(portletTitle)){
 				portletTitle=Constants.PORTLET_TITLE_DEFAULT;
 			}
 			
 			//form ok so validate
 			try {
 				prefs.setValue("portletHeight", portletHeight);
 				prefs.setValue("portletTitle", portletTitle);
 				prefs.setValue("remoteSiteId", remoteSiteId);
 				prefs.setValue("remoteToolId", remoteToolId);
 			} catch (ReadOnlyException e) {
 				response.setRenderParameter("errorMessage", Messages.getString("error.form.readonly.error"));
 				log.error(e);
 			}
 			
 			//save them
 			try {
 				prefs.store();
 				isValid=true;
 			} catch (ValidatorException e) {
 				response.setRenderParameter("errorMessage", e.getMessage());
 				log.error(e);
 			} catch (IOException e) {
 				response.setRenderParameter("errorMessage", Messages.getString("error.form.save.error"));
 				log.error(e);
 			}
 			
 			//if ok, invalidate cache and return to view
 			if(isValid) {
 				
 				try {
 					response.setPortletMode(PortletMode.VIEW);
 				} catch (PortletModeException e) {
 					e.printStackTrace();
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * Custom mode handler for CONFIG view
 	 */
 	protected void doConfig(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("doConfig()");
 
 		//todo get the settings for each form field, put in scope and dispatch
 		
 		//request.setAttribute("configuredPortletHeight", getConfiguredPortletHeight(request));
 		//request.setAttribute("configuredPortletTitle", getConfiguredPortletTitle(request));
 		//request.setAttribute("configuredProviderType", getConfiguredProviderType(request));
 		//request.setAttribute("configuredLaunchData", getConfiguredLaunchData(request));
 		
 		dispatch(request, response, configUrl);
 	}
 
 	
 	/**
 	 * Render the main view
 	 */
 	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("Basic LTI doView()");
 		
 		//get data
 		Map<String,String> launchData = getLaunchData(request, response);
 		
 		//catch - errors already handled
 		if(launchData == null) {
 			return;
 		}
 		
 		
 		//setup the params, serialise to a URL
 		StringBuilder proxy = new StringBuilder();
 		proxy.append(request.getContextPath());
 		proxy.append(proxyUrl);
 		proxy.append("?");
 		proxy.append(HttpSupport.serialiseMapToQueryString(launchData));
 		
 		request.setAttribute("proxyContextUrl", proxy.toString());
 		request.setAttribute("preferredHeight", getPreferredPortletHeight(request));
 		
 		dispatch(request, response, viewUrl);
 	}	
 		
 	/**
 	 * Render the edit page, invalidates any cached data
 	 */
 	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("Basic LTI doEdit()");
 		
 		//setup the web service bean
 		SakaiWebServiceLogic logic = new SakaiWebServiceLogic();
 		logic.setAdminUsername(adminUsername);
 		logic.setAdminPassword(adminPassword);
 		logic.setLoginUrl(loginUrl);
 		logic.setScriptUrl(scriptUrl);
 		request.setAttribute("logic", logic);
 		
 		//setup remote userId
 		String remoteUserId = getRemoteUserId(request, logic);
 		if(StringUtils.isBlank(remoteUserId)) {
 			log.error("No user info was returned from remote server.");
 			doError("error.no.remote.data", "error.heading.general", request, response);
 			return;
 		}
 		
 		request.setAttribute("eid", getAuthenticatedUsername(request));
 		request.setAttribute("remoteUserId", remoteUserId);
 
 		
 		// get list of sites
 		List<Site> sites = getRemoteSitesForUser(request, logic);
 		if(sites.isEmpty()){
 			log.error("No sites were returned from remote server.");
 			doError("error.no.remote.data", "error.heading.general", request, response);
 			return;
 		}
 		request.setAttribute("remoteSites", sites);
 		
 		//set list of allowed tool registrations
 		request.setAttribute("allowedToolIds", allowedTools);
 	
 		//do we need to replay the form? This could be due to an error, or we need to show the lists again.
 		//if so, use the original request params
 		//otherwise, use the preferences
 		if(replayForm) {
 			request.setAttribute("preferredPortletHeight", request.getParameter("portletHeight"));
 			request.setAttribute("preferredPortletTitle", request.getParameter("portletTitle"));
 			request.setAttribute("preferredRemoteSiteId", request.getParameter("remoteSiteId"));
 			request.setAttribute("preferredRemoteToolId", request.getParameter("remoteToolId"));
 			request.setAttribute("errorMessage", request.getParameter("errorMessage"));
 		} else {
 			request.setAttribute("preferredPortletHeight", getPreferredPortletHeight(request));
 			request.setAttribute("preferredPortletTitle", getPreferredPortletTitle(request));
 			request.setAttribute("preferredRemoteSiteId", getPreferredRemoteSiteId(request));
 			request.setAttribute("preferredRemoteToolId", getPreferredRemoteToolId(request));
 			
 			//invalidate the cache for this item as it may change (only need to do this once per edit page view)
 			evictFromCache(getPortletNamespace(response));
 		}
 		
 		dispatch(request, response, editUrl);
 	}
 	
 	/**
 	 * Get the current user's details, exposed via portlet.xml
 	 * @param request
 	 * @return Map<String,String> of info
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<String,String> getUserInfo(RenderRequest request) {
 		return (Map<String,String>)request.getAttribute(PortletRequest.USER_INFO);
 	}
 	
 	
 	/**
 	 * Gets the unique namespace for this portlet
 	 * @param response
 	 * @return
 	 */
 	private String getPortletNamespace(RenderResponse response) {
 		return response.getNamespace();
 	}
 	
 	/**
 	 * Setup the Map of params for the request
 	 * @param request
 	 * @param response
 	 * @return Map of params or null if any required data is missing
 	 */
 	private Map<String,String> getLaunchData(RenderRequest request, RenderResponse response) {
 		
 		//launch map
 		Map<String,String> params;
 		
 		//check cache, otherwise form up all of the data
 		String cacheKey = getPortletNamespace(response);
 		params = retrieveFromCache(cacheKey);
 		if(params == null) {
 		
 			//init for new data
 			params = new HashMap<String,String>();
 			
 			//get site prefs
 			String preferredRemoteSiteId = getPreferredRemoteSiteId(request);
 			if(StringUtils.isBlank(preferredRemoteSiteId)) {
 				doError("error.no.config", "error.heading.config", request, response);
 				return null;
 			}
 		
 			//get tool prefs
 			String preferredRemoteToolId = getPreferredRemoteToolId(request);
 			if(StringUtils.isBlank(preferredRemoteToolId)) {
 				doError("error.no.config", "error.heading.config", request, response);
 				return null;
 			}
 		
 			//setup the web service bean
 			SakaiWebServiceLogic logic = new SakaiWebServiceLogic();
 			logic.setAdminUsername(adminUsername);
 			logic.setAdminPassword(adminPassword);
 			logic.setLoginUrl(loginUrl);
 			logic.setScriptUrl(scriptUrl);
 		
 			//get remote userId
 			String remoteUserId = getRemoteUserId(request, logic);
 			if(StringUtils.isBlank(remoteUserId)) {
 				doError("error.no.remote.data", "error.heading.general", request, response);
 				return null;
 			}
 			
 			//setup full endpoint
 			params.put("endpoint_url", endpoint + preferredRemoteToolId);
 			
 		
 			//required fields
 			params.put("user_id", getAuthenticatedUsername(request));
 			params.put("lis_person_name_given", null);
 			params.put("lis_person_name_family", null);
 			params.put("lis_person_name_full", null);
 			params.put("lis_person_contact_email_primary", null);
 			params.put("resource_link_id", getPortletNamespace(response));
 			params.put("context_id", preferredRemoteSiteId);
 			params.put("tool_consumer_instance_guid", key);
 			params.put("lti_version","LTI-1p0");
 			params.put("lti_message_type","basic-lti-launch-request");
 			params.put("oauth_callback","about:blank");
 			params.put("basiclti_submit", "Launch Endpoint with BasicLTI Data");
 			params.put("user_id", remoteUserId);
 		
 			//additional fields
 			params.put("remote_tool_id", preferredRemoteToolId);
 			
 			//cache the data, must be done before signing
 			updateCache(cacheKey, params);
 		}
 		
 		if(log.isDebugEnabled()) {
 			log.debug("Parameter map before OAuth signing");
 			CollectionsSupport.printMap(params);
 		}
 		
 		//sign the properties map
 		params = OAuthSupport.signProperties(params.get("endpoint_url"), params, "POST", key, secret);
 
 		if(log.isDebugEnabled()) {
 			log.warn("Parameter map after OAuth signing");
 			CollectionsSupport.printMap(params);
 		}
 		
 		return params;
 	}
 	
 	
 	/**
 	 * Get the preferred portlet height if set, or default from Constants
 	 * @param request
 	 * @return
 	 */
 	private int getPreferredPortletHeight(RenderRequest request) {
 	      PortletPreferences pref = request.getPreferences();
 	      return Integer.parseInt(pref.getValue("portletHeight", String.valueOf(Constants.PORTLET_HEIGHT_DEFAULT)));
 	}
 	
 	/**
 	 * Get the preferred portlet title if set, or default from Constants
 	 * @param request
 	 * @return
 	 */
 	private String getPreferredPortletTitle(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("portletTitle", Constants.PORTLET_TITLE_DEFAULT);
 	}
 	
 	/**
 	 * Get the preferred remote site id, or null if they have not made a choice yet
 	 * @param request
 	 * @return
 	 */
 	private String getPreferredRemoteSiteId(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("remoteSiteId", null);
 	}
 	
 	/**
 	 * Get the preferred remote tool id, or null if they have not made a choice yet
 	 * @param request
 	 * @return
 	 */
 	private String getPreferredRemoteToolId(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("remoteToolId", null);
 	}
 	
 	/**
 	 * Get the current username
 	 * @param request
 	 * @return
 	 */
 	private String getAuthenticatedUsername(RenderRequest request) {
 		Map<String,String> userInfo = getUserInfo(request);
 		//return userInfo.get("username");
 		return userInfo.get(attributeMappingForUsername);
 	}
 	
 	/**
 	 * Get the remote userId for this user, either from session or from remote service
 	 * @param request
 	 * @param logic
 	 * @return
 	 */
 	private String getRemoteUserId(RenderRequest request, SakaiWebServiceLogic logic){
 		
 		String remoteUserId = (String) request.getPortletSession().getAttribute("remoteUserId");
 		if(StringUtils.isBlank(remoteUserId)) {
 			remoteUserId = SakaiWebServiceHelper.getRemoteUserIdForUser(logic, getAuthenticatedUsername(request));
 			request.getPortletSession().setAttribute("remoteUserId", remoteUserId);
 		}
 		
 		return remoteUserId;
 	}
 	
 	/**
 	 * Get the list of remote sites for this user
 	 * @param logic
 	 * @return
 	 */
 	private List<Site> getRemoteSitesForUser(RenderRequest request, SakaiWebServiceLogic logic){
 		return SakaiWebServiceHelper.getAllSitesForUser(logic, getAuthenticatedUsername(request));
 	}
 	
 	
 	
 	
 	/**
 	 * Override GenericPortlet.getTitle() to use the preferred title for the portlet instead
 	 */
 	@Override
 	protected String getTitle(RenderRequest request) {
 		return getPreferredPortletTitle(request);
 	}
 	
 	/**
 	 * Helper to handle error messages
 	 * @param messageKey	Message bundle key
 	 * @param headingKey	optional error heading message bundle key, if not specified, the general one is used
 	 * @param request
 	 * @param response
 	 */
 	private void doError(String messageKey, String headingKey, RenderRequest request, RenderResponse response){
 		
 		//message
 		request.setAttribute("errorMessage", Messages.getString(messageKey));
 		
 		//optional heading
 		if(StringUtils.isNotBlank(headingKey)){
 			request.setAttribute("errorHeading", Messages.getString(headingKey));
 		} else {
 			request.setAttribute("errorHeading", Messages.getString("error.heading.general"));
 		}
 		
 		//dispatch
 		try {
 			dispatch(request, response, errorUrl);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Dispatch to a JSP or servlet
 	 * @param request
 	 * @param response
 	 * @param path
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	protected void dispatch(RenderRequest request, RenderResponse response, String path)throws PortletException, IOException {
 		response.setContentType("text/html"); 
 		PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(path);
 		dispatcher.include(request, response);
 	}
 	
 	/**
 	 * Helper to evict an item from a cache. If we visit the edit mode, we must evict the current data. It will be re-cached later.
 	 * @param cacheKey	the id for the data in the cache
 	 */
 	private void evictFromCache(String cacheKey) {
 		cache.remove(cacheKey);
 		log.info("Evicted data in cache for key: " + cacheKey);
 	}
 
 	
 	
 	
 	
 	/**
 	 * Helper to retrieve data from the cache
 	 * @param key
 	 * @return
 	 */
 	private Map<String,String> retrieveFromCache(String key) {
 		Element element = cache.get(key);
 		if(element != null) {
 			Map<String,String> data = (Map<String,String>) element.getObjectValue();
 			log.info("Fetching data from cache for key: " + key);
 			return data;
 		} 
 		return null;
 	}
 	
 	
 	/**
 	 * Helper to update the cache
 	 * @param cacheKey	the id for the data in the cache	
 	 * @param data		the data to be assocaited with that key in the cache
 	 */
 	private void updateCache(String cacheKey, Map<String,String> data){
 		cache.put(new Element(cacheKey, data));
 		log.info("Added data to cache for key: " + cacheKey);
 	}
 	
 	
 	public void destroy() {
 		log.info("destroy()");
 	}
 	
 	
 	
 }
