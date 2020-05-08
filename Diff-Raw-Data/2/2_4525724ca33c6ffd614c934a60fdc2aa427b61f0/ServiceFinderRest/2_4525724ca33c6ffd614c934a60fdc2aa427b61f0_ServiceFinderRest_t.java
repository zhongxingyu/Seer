 /**
  * EasySOA Registry
  * Copyright 2011 Open Wide
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact : easysoa-dev@googlegroups.com
  */
 
 package org.easysoa.registry.dbb.rest;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.dbb.BrowsingContext;
 import org.easysoa.registry.dbb.FoundService;
 import org.easysoa.registry.dbb.ServiceFinderService;
 import org.easysoa.registry.dbb.ServiceFinderStrategy;
 import org.easysoa.registry.types.Endpoint;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
 import org.nuxeo.runtime.api.Framework;
 
 import com.sun.jersey.api.core.HttpContext;
 
 /**
  * REST service to find WSDLs from given URL.
  * 
  * Use: .../nuxeo/site/easysoa/servicefinder/{url}
  * Params: {url} The URL of the page to consider (not encoded).
  * Other protocols than HTTP are not supported.
  * 
  * @author mkalam-alami
  * 
  */
 @Path("easysoa/servicefinder")
 @Produces("application/x-javascript")
 @WebObject(type = "servicefinder")
 public class ServiceFinderRest extends ModuleRoot {
 
 	private DocumentModelList endpointsCache = null;
 	
 	private List<String> environmentsNamesCache = null;
 	
     @GET
     public Object getDefault() {
         return "Invalid use (please append an address to explore to the URL)";
     }
 
     /**
      * Computes a list of all existing environments, by analysing the endpoints properties.
      * @return
      * @throws Exception
      */
     @GET
     @Path("/environments")
     public Object getEnvironmentList() throws Exception {
     	CoreSession session = SessionFactory.getSession(request);
     	DocumentService docService = Framework.getService(DocumentService.class);
     	DocumentModelList allEndpoints = docService.query(session, "SELECT * FROM " + Endpoint.DOCTYPE, true, false);
     	
     	// Use a cache for performance
     	boolean computeEnvList = true;
     	if (environmentsNamesCache != null) {
     		if (endpointsCache != null && endpointsCache.equals(allEndpoints)) {
     			computeEnvList = false;
     		}
     	}
     	
     	if (computeEnvList) {
     		List<String> environmentsNames = new ArrayList<String>();
     		for (DocumentModel endpoint : allEndpoints) {
     			String environmentName = (String) endpoint.getPropertyValue(Endpoint.XPATH_ENVIRONMENT);
     			if (environmentName != null && !environmentsNames.contains(environmentName)) {
     				environmentsNames.add(environmentName);
     			}
     		}
     		endpointsCache = allEndpoints;
     		environmentsNamesCache = environmentsNames;
     	}
     	
		return new JSONArray(environmentsNamesCache).toString();
     }
     
     @GET
     @Path("/find/{url:.*}")
     public Object findServices(@Context UriInfo uriInfo) throws Exception {
 
         URL url = null;
         String callback = null;
         try {
             // Retrieve URL
         	String restServiceURL = uriInfo.getBaseUri().toString()+"easysoa/servicefinder/find/";
         	url = new URL(uriInfo.getRequestUri().toString().substring(restServiceURL.length()));
         	
         	if (url.getQuery() != null && url.getQuery().contains("callback=")) {
         		List<NameValuePair> queryTokens = URLEncodedUtils.parse(url.toURI(), "UTF-8");
         		for (NameValuePair token : queryTokens) {
         			if (token.getName().equals("callback")) {
         				callback = token.getValue(); // TODO remove callback from original URL
         			}
         		}
         	}
         }
         catch (MalformedURLException e) {
             return "{ errors: '" + formatError(e) + "' }";
         }
         
         // Find WSDLs
         if (callback != null) {
         	return callback + '(' + findServices(new BrowsingContext(url)) + ')';
         }
         else {
         	return findServices(new BrowsingContext(url));
         }
     }
     
 
     @POST
     @Path("/")
     public Object findServices(@Context HttpContext httpContext, @Context HttpServletRequest request) throws Exception {
     	
     	// Retrieve params
     	@SuppressWarnings("unchecked")
 		Map<String, String> formValues = getFirstValues(request.getParameterMap());
     	
     	// Find WSDLs
     	BrowsingContext browsingContext = new BrowsingContext(new URL(formValues.get("url")), formValues.get("data"));
         return findServices(browsingContext);
     }
 
 	public String findServices(BrowsingContext context) throws Exception {
 
         JSONArray errors = new JSONArray();
         JSONObject result = new JSONObject();
         
 
         // Run finders
         List<FoundService> foundServices = new LinkedList<FoundService>();
         if (context.getURL() != null && context.getData() != null) {
         	ServiceFinderService finderComponent = Framework.getService(ServiceFinderService.class);
             List<ServiceFinderStrategy> strategies = finderComponent.getStrategies();
 
             for (ServiceFinderStrategy strategy : strategies) {
                 List<FoundService> strategyResult = null;
                 try {
                     strategyResult = strategy.findFromContext(context);
                 }
                 catch (Exception e) {
                     errors.put(formatError(e, "Failed to run service finder strategy "+strategy.getClass().getName()));
                 }
                 if (strategyResult != null) {
                     foundServices.addAll(strategyResult);
                 }
             }
         }
         
         // TODO: Filter duplicates
 
         // Format response
         JSONObject foundLinks = new JSONObject();
         for (FoundService foundService : foundServices) {
             String appName = foundService.getApplicationName();
             if (appName != null) {
                 result.put("applicationName", appName);
             }
             foundLinks.put(foundService.getName(), foundService.getURL());
         }
         if (foundLinks.keys().hasNext()) {
             result.put("foundLinks", foundLinks);
         }
         if (errors.length() > 0) {
             result.put("errors", errors);
         }
         
         return result.toString();
          
     }
 
     private static Map<String, String> getFirstValues(Map<String, String[]> multivaluedMap) {
 	    Map<String, String> map = new HashMap<String, String>();
 	    for (Entry<String, String[]> entry : multivaluedMap.entrySet()) {
 	    	if (entry.getValue().length > 0) {
 	    		map.put(entry.getKey(), entry.getValue()[0]);
 	    	}
 	    }
 	    return map;
 	}
 
 	private String formatError(Exception e, String message) {
         return e.getClass().getSimpleName()+": "+message+" (cause: "+e.getMessage()+")";
     }
     
     private String formatError(Exception e) {
         return e.getClass().getSimpleName()+": "+e.getMessage();
     }
     
 
 }
