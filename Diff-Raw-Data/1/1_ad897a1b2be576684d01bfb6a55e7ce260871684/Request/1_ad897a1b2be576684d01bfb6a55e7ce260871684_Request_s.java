breaking the build
 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.vfny.geoserver.servlets.AbstractService;
 import org.vfny.geoserver.util.Requests;
 
 /**
  * Defines a general Request type and provides accessor methods for universal
  * request information.
  * <p>
  * Also provides access to the HttpRequest that spawned this GeoServer Request.
  * This HttpRequest is most often used to lookup information stored in the
  * Web Container (such as the GeoServer Global information).
  * </p>
  * 
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @author Gabriel Roldan
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last modification)
  * @version $Id: Request.java,v 1.16 2004/09/08 17:36:35 cholmesny Exp $
  */
 abstract public class Request {
 	/**
 	 * HttpServletRequest responsible for generating this GeoServer Request.
 	 */
 	protected HttpServletRequest httpServletRequest;
 	
     /**
      * The service type of the request.  In other words, is it a WMS
      * or a WFS.  This is a standard element of a request.  It now has
      * a practical purpose in GeoServer, as a GetCapabilities request
      * can be WMS or WFS, this element tells which it is.
      */
     protected String service;
 
     /** Request type */
     protected String request = "";
 
     /** Request version */
     protected String version = "";
 
     /** service reference */
     protected AbstractService serviceRef;
     
    /**
      * ServiceType,RequestType,ServiceRef constructor.
      * 
      * @param serviceType Name of hte service (example, WFS)
      * @param requestType Name of the request (example, GetCapabilties)
      * @param serviceRef The servlet for the request.
      */
     protected Request(String serviceType, String requestType, AbstractService serviceRef) {
     		this.service = serviceType;
     		this.request = requestType;
     		this.serviceRef = serviceRef;
     }
     
     
     /**
      * Gets requested service.
      *
      * @return The requested service.
      */
     public String getService() {
         return this.service;
     }
 
     /**
      * Gets requested service.
      *
      * @param service The requested service.
      */
     public void setService(String service) {
         this.service = service;
     }
 
     /**
      * Gets requested request type.
      * <p>
      * TODO: Could this bre renamed getType() for clarity?
      * </p>
      * <p>
      * Um, no.  getType() is less clear.  getRequest makes sense because
      * this is directly modeled off of the XML and KVP Requests that a
      * wfs or wms makes, and they all contain an element called Request.
      * 
      * @return The name of the request.
      */
     public String getRequest() {
         return this.request;
     }
 
     /**
      * Sets requested request type.
      *
      * @param reqeust The type of request.
      */
     public void setRequest(String requestType) {
         this.request = requestType;
     }
 
     /**
      * Return version type.
      *
      * @return The request type version.
      */
     public String getVersion() {
         return this.version;
     }
 
     /**
      * Sets version type.
      *
      * @param version The request type version.
      */
     public void setVersion(String version) {
         this.version = version;
     }
 
     /**
      * Sets the reference to the service.
      */
     public void setServiceRef(AbstractService serviceRef) {
     		this.serviceRef = serviceRef;
     }
     
     /**
      * @return the reference the service.
      */
     public AbstractService getServiceRef() {
     		return serviceRef;
     }
     
     public boolean equals(Object o) {
         if (!(o instanceof Request)) {
             return false;
         }
 
         Request req = (Request) o;
         boolean equals = true;
         equals = ((request == null) ? (req.getRequest() == null)
                                     : request.equals(req.getRequest()))
             && equals;
         equals = ((version == null) ? (req.getVersion() == null)
                                     : version.equals(req.getVersion()))
             && equals;
         equals = ((service == null) ? (req.getService() == null)
                                     : service.equals(req.getService()))
             && equals;
 
         return equals;
     }
     /**
      * Generate a hashCode based on this Request Object.
      */
     public int hashCode() {
         int result = 17;
         result = (23 * result) + ((request == null) ? 0 : request.hashCode());
         result = (23 * result) + ((request == null) ? 0 : version.hashCode());
         result = (23 * result) + ((request == null) ? 0 : service.hashCode());
 
         return result;
     }
 
     /**
      * Retrive the ServletRequest that generated this GeoServer request.
      * <p>
      * The ServletRequest is often used to:
      * </p>
 	 * <ul>
 	 * <li>Access the Sesssion and WebContainer by execute opperations
 	 *     </li>
 	 * <li>Of special importance is the use of the ServletRequest to locate the GeoServer Application
 	 *     </li> 
 	 * </p>
 	 * <p>
 	 * This method is called by AbstractServlet during the processing of a Request.
 	 * </p>
 	 * @return The HttpServletRequest responsible for generating this SerivceRequest
 	 */
 	public HttpServletRequest getHttpServletRequest() throws ClassCastException {
 		return httpServletRequest;
 	}
 	
 	//JD: delete this
 //	public WMS getWMS(){
 //		WMS vp = Requests.getWMS( getHttpServletRequest() );
 //		return vp;
 //	}
 //	
 //	public WFS getWFS(){
 //		WFS vp = Requests.getWFS( getHttpServletRequest() );
 //		return vp;
 //	}
 
 	
 	public String getRootDir(){
 		throw new IllegalArgumentException("getRootDir -- functionality removed - please verify that its okay with geoserver_data_dir");
 		//return httpServletRequest.getSession().getServletContext().getRealPath("/");
 	}
 	
 
    /**
     * Gets the base url that made this request.  This is used to return the
     * referenced schemas and whatnot relative to the request.  
     * @return The url that the client used to make the request.
     */
 	public String getBaseUrl(){
 		return Requests.getBaseUrl( getHttpServletRequest() );
 	}
 	
 	/**
 	 * Gets the url that schemas should be referenced from.  For now this will
 	 * always be local, if we bring back schemaBaseUrl as a param then that will
 	 * be possible too.  So it is just baseUrl plus data/capabilities, which
 	 * is where we store the schemas now.  
 	 * 
 	 * @return the base url of the schemas.  Will be getBaseUrl() + data/capabilities.
 	 */
 	public String getSchemaBaseUrl(){
 		return Requests.getSchemaBaseUrl( getHttpServletRequest() );
 	}
 	
 	/**
 	 * Whether this request was sent through one of the dispatchers, or if
 	 * it went directly through the servlet.  This is used by the capabilities
 	 * response, since we give back a dispatched capabilities document to clients
 	 * who request it with a dispatcher.
 	 * @return true if the request came through a dispatcher.
 	 */
 	public boolean isDispatchedRequest(){
 		HttpServletRequest hsr = getHttpServletRequest();
 		// will happen if the dispatcher was called, as opposed to using the /wfs url.
 		String uri = hsr.getRequestURI();
 		if(uri!=null) {
 		uri = uri.toLowerCase();
 		}
 		// will happen if the dispatcher was called, as opposed to using the /wfs url.
 		if(uri.endsWith("/wfs") || uri.endsWith("/wms"))
 			return true;
 		return false;
 	}
 	/**
 	 * Tests if user is Logged into GeoServer.
 	 * 
 	 * @return <code>true</code> if user is logged in
 	 */
 	public boolean isLoggedIn(){
 		return Requests.isLoggedIn( getHttpServletRequest() );
 	}
 	
 	/**
 	 * Sets the servletRequest that generated this GeoServer request.
 	 * <p>
 	 * The ServletRequest is often used to:
 	 * </p>
 	 * <ul>
 	 * <li>Access the Sesssion and WebContainer by execute opperations
 	 *     </li>
 	 * <li>Of special importance is the use of the ServletRequest to locate the GeoServer Application
 	 *     </li> 
 	 * </p>
 	 * @param servletRequest The servletRequest to set.
 	 */
 	public void setHttpServletRequest(HttpServletRequest servletRequest) {
 		httpServletRequest = servletRequest;
 	}
 
 }
