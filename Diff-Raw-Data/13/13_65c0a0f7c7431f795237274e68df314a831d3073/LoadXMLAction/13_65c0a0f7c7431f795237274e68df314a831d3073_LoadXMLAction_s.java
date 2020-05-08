 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.action;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.GeoServerDTO;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 import org.vfny.geoserver.global.xml.XMLConfigReader;
 
 
 /**
  * Load GeoServer configuration.
  * <p>
  * The existing getServer instances is updated with a call to load(..) based
  * on the existing XML configuration files.
  * </p>
  * <p>
  * It seems this class also creates the GeoServer instance in a lazy fashion!
  * That would mean that if this class cannot load, the application cannot load?
  * This could not possibly be the case, because the load action should only
  * appear when logged in.
  * </p>
  * <p>
  * Load need to remain on the current page, right now it takes us on a wild
  * ride back to the welcome screen.
  * </p>
  * <p>
  * Q: Does this need to load the Validation Processor as well?
  * </p>
  */
 public class LoadXMLAction extends ConfigAction {
 	
     public ActionForward execute(ActionMapping mapping,
     		                     ActionForm form,
 								 //UserContainer user,
 								 HttpServletRequest request,
 								 HttpServletResponse response)
         throws IOException, ServletException {
     	ServletContext sc = request.getSession().getServletContext();
     	
     	GeoServer geoServer = getGeoServer( request );
         if( geoServer == null ){
         	// lazy creation on load?
         	geoServer = new GeoServer();
         	sc.setAttribute(GeoServer.WEB_CONTAINER_KEY, geoServer);        	
         }        
                         
         // geoServer = new GeoServer();
         // sc.setAttribute(GeoServer.WEB_CONTAINER_KEY, geoServer);
         //
         // Um pardon? This appears to create a new GeoServer from scratch -
         // we *really* need to conserve dataStores during the load process.
         // That is - if the dataStore definition does not change - don't replace it.
         // Asking dataStore authors to handle this sort of thing internally is
         // too much
         WMSDTO wmsDTO = null;
         WFSDTO wfsDTO = null;
         GeoServerDTO geoserverDTO = null;
         DataDTO dataDTO = null;
         File rootDir = new File( sc.getRealPath("/") );
         
         XMLConfigReader configReader;
 		try {
 			configReader = new XMLConfigReader( rootDir );
 		} catch (ConfigurationException configException) {
 			configException.printStackTrace();
 			return mapping.findForward("welcome");
 			//throw new ServletException( configException );
 		}
 		if (configReader.isInitialized()) {	        		
         	// These are on separate lines so we can tell with the
         	// stack trace/debugger where things go wrong
             wmsDTO = configReader.getWms();
             wfsDTO = configReader.getWfs();
             geoserverDTO = configReader.getGeoServer();
             dataDTO = configReader.getData();
     	}
     	else {
     		System.err.println("Config Reader not initialized for LoadXMLAction.execute().");
     		return mapping.findForward("welcome");    		
     		// throw new ServletException( new ConfigurationException( "An error occured loading the initial configuration" ));
     	}
         // Update GeoServer
         
         try {
         	geoServer.load( wmsDTO, wfsDTO, geoserverDTO, dataDTO, rootDir );
 		} catch (ConfigurationException configException) {
 			configException.printStackTrace();
 			return mapping.findForward("welcome");			
 //			throw new ServletException( configException );			
 		}
 		getApplicationState( request ).notifyLoadXML();		
         // We need to stash the current page?
         // or can we use null or something?
         //
 		System.out.println("request:"+request.getServletPath());
 		System.out.println("forward:"+mapping.getForward());
         return mapping.findForward("welcome");
     }
 }
