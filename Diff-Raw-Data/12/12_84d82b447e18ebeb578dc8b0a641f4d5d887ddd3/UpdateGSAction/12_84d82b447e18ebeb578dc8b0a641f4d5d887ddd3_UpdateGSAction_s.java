 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 /*
  * Created on Jan 6, 2004
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.vfny.geoserver.action;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.GeoServerDTO;
 import org.vfny.geoserver.global.dto.WCSDTO;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * Update GeoServer with the current configuration.
  *
  * <p>
  * This is a real ConfigAction - you need to be logged in to use it.
  * </p>
  *
  * @author User To change the template for this generated type comment go to
  *         Window>Preferences>Java>Code Generation>Code and Comments
  */
 public class UpdateGSAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping, ActionForm form, UserContainer user,
         HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         ActionForward r1 = updateGeoserver(mapping, form, request, response);
         ActionForward r2 = updateValidation(mapping, form, request, response);
 
         getApplicationState().fireChange();
         return mapping.findForward("config");
     }
 
     public ActionForward updateGeoserver(ActionMapping mapping, ActionForm form,
         // UserContainer user,
     HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         GeoServer gs;
         ServletContext sc = request.getSession().getServletContext();
 
         try {
             WCSDTO wcsDTO = getWCSConfig().toDTO();
             WMSDTO wmsDTO = getWMSConfig().toDTO();
             WFSDTO wfsDTO = getWFSConfig().toDTO();
             GeoServerDTO geoserverDTO = getGlobalConfig().toDTO();
             DataDTO dataDTO = getDataConfig().toDTO();

             getWCS(request).load(wcsDTO);
             getWFS(request).load(wfsDTO);
             getWMS(request).load(wmsDTO);
            getWFS(request).getGeoServer().load(geoserverDTO);
            getWFS(request).getData().load(dataDTO);
             getWCS(request).getGeoServer().load(geoserverDTO);
             getWCS(request).getData().load(dataDTO);
 
             getApplicationState().notifyToGeoServer();
         } catch (ConfigurationException e) {
             e.printStackTrace();
             throw new ServletException(e);
         }
 
         // We need to stay on the same page!
         getApplicationState(request).notifyToGeoServer();
 
         return mapping.findForward("config");
     }
 
     public ActionForward updateValidation(ActionMapping mapping, ActionForm form,
         // UserContainer user,
     HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         GeoServer gs;
         ServletContext sc = request.getSession().getServletContext();
         gs = (GeoServer) sc.getAttribute(GeoServer.WEB_CONTAINER_KEY);
 
         try {
             Map plugins = new HashMap();
             Map testSuites = new HashMap();
 
             if (getValidationConfig().toDTO(plugins, testSuites)) {
                 //sorry, no time to really test this, but I got a null pointer
                 //exception with the demo build target. ch
                 if (getWFS(request).getValidation() != null) {
                     getWFS(request).getValidation().load(testSuites, plugins);
                 }
             } else {
                 throw new ConfigurationException(
                     "ValidationConfig experienced an error exporting Data Transpher Objects.");
             }
         } catch (ConfigurationException e) {
             e.printStackTrace();
             throw new ServletException(e);
         }
 
         // We need to stay on the same page!
         getApplicationState(request).notifyToGeoServer();
 
         return mapping.findForward("config.validation");
     }
 }
