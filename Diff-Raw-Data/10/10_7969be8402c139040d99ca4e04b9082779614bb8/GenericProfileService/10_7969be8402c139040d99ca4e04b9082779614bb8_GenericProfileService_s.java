 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.sp.engine.service.generic;
 
 import org.apache.log4j.Logger;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.common.definitions.SAML;
 import org.guanxi.common.entity.EntityFarm;
 import org.guanxi.common.entity.EntityManager;
 import org.guanxi.common.metadata.Metadata;
 import org.guanxi.sp.Util;
 import org.guanxi.xal.saml2.metadata.GuardRoleDescriptorExtensions;
 import org.guanxi.xal.saml_2_0.metadata.EndpointType;
 import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorType;
 import org.springframework.context.MessageSource;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.HashMap;
 
 /**
  * Generic profile service for selecting a profile based on metadata
  *
  * @author alistair
  */
 public class GenericProfileService extends MultiActionController implements ServletContextAware {
   /** Our logger */
   private static final Logger logger = Logger.getLogger(GenericProfileService.class.getName());
   /** The marker in our entityID map for the one to use as the default entityID */
   private static final String DEFAULT_ENTITYID_MARKER = "__DEFAULT__";
   /** The localised messages to use */
   private MessageSource messages = null;
   /** The JSP to use to display any errors */
   private String errorView = null;
   /** The request attribute that holds the error message for the error view */
   private String errorViewDisplayVar = null;
   /** The Shibboleth profile service to use */
   private ProfileService shibbolethProfileService = null;
   /** The SAML2 profile service to use */
   private ProfileService saml2ProfileService = null;
   /** The SAML2 Discovery profile service to use */
   private ProfileService saml2DiscoveryProfileService = null;
   /** The list of Guard to entityID mappings */
   private HashMap<String, String> entityIDs = null;
   /** Whether to use the SAML2 Discovery Service */
   private boolean useDiscoveryService;
 
   public void init() {}
 
   public ModelAndView gps(HttpServletRequest request, HttpServletResponse response) {
     String guardID = request.getParameter(Guanxi.WAYF_PARAM_GUARD_ID);
     String guardSessionID = request.getParameter(Guanxi.WAYF_PARAM_SESSION_ID);
 
     // Optional entityID
     String idpEntityID = request.getParameter("entityID");
 
     // If the Guard hasn't specified an entityID, see if it has one registered for it
     if (idpEntityID == null) {
       if (entityIDs != null) {
         String entityIDForGuard = null;
         String defaultEntityID = null;
 
         // Find out which entityID to use for this Guard
         for (String registeredGuardID : entityIDs.keySet()) {
           if (registeredGuardID.equals(DEFAULT_ENTITYID_MARKER)) {
             defaultEntityID = entityIDs.get(registeredGuardID);
           }
           if (guardID.equals(registeredGuardID)) {
             entityIDForGuard = entityIDs.get(registeredGuardID);
           }
         }
 
         idpEntityID = (entityIDForGuard != null) ? entityIDForGuard : defaultEntityID;
         logger.info("Guard '" + guardID + "' obtained entityID : " + idpEntityID);
       }
     }
     else {
       logger.info("Guard '" + guardID + "' specified entityID : " + idpEntityID);
     }
 
     // If no IdP entityID, see if it's coming from the discovery service
     if ((idpEntityID == null) && (useDiscoveryService)) {
       idpEntityID = request.getParameter("edsEntityID");
     }
 
     // Get the Guard's metadata, previously loaded by the Bootstrapper
     EntityDescriptorType guardEntityDescriptor = (EntityDescriptorType)getServletContext().getAttribute(guardID);
     if (guardEntityDescriptor == null) {
       logger.error("Guard '" + guardID + "' not found in metadata repository");
       ModelAndView mAndV = new ModelAndView();
       mAndV.setViewName(errorView);
       mAndV.getModel().put(errorViewDisplayVar, messages.getMessage("engine.error.no.guard.metadata",
                                                                     null, request.getLocale()));
       return mAndV;
     }
     
     // Load the GuanxiGuardService node from the metadata
     GuardRoleDescriptorExtensions guardNativeMetadata = Util.getGuardNativeMetadata(guardEntityDescriptor);
 
     /* Convert the Guard's session ID to an Engine session ID and store the Guard's GuanxiGuardService
     * node under it.
      */
     getServletContext().setAttribute(guardSessionID.replaceAll("GUARD", "ENGINE"), guardEntityDescriptor);
 
     EntityFarm farm = (EntityFarm)getServletContext().getAttribute(Guanxi.CONTEXT_ATTR_ENGINE_ENTITY_FARM);
     try {
       return getProfileService(request, farm, idpEntityID).doProfile(request, guardID, guardSessionID, guardNativeMetadata, idpEntityID, farm);
     }
     catch(GuanxiException ge) {
      logger.error("Shibboleth error: ", ge);
       ModelAndView mAndV = new ModelAndView();
       mAndV.setViewName(errorView);
      mAndV.getModel().put(errorViewDisplayVar, messages.getMessage("engine.error.no.guard.metadata",
                                                                    null, request.getLocale()));
       return mAndV;
     }
   }
 
   /**
    * Selects a profile to use
    *
    * @param request the request
    * @param farm entity farm
    * @param idpEntityID entityID of the IdP or null if there isn't one
    * @return ProfileService instance which defaults to Shibboleth
    * @throws GuanxiException if an error occurs
    */
   private ProfileService getProfileService(HttpServletRequest request, EntityFarm farm, String idpEntityID) throws GuanxiException {
     if (idpEntityID == null) {
       // Check to see if the Discovery Service has anything for us
       if (useDiscoveryService) {
         if (request.getParameter("edsEntityID") != null) {
           idpEntityID = request.getParameter("edsEntityID");
         }
         else if (request.getParameter("edsEntityID") == null) {
           // Use the Embedded Discovery Service to get an IdP entityID
           return saml2DiscoveryProfileService;
         }
       }
       else {
         // No entityID so assume Shibboleth
         return shibbolethProfileService;
       }
     }
 
     // By now we have an IdP entityID
 
     // Load the metadata for the IdP
     EntityManager manager = farm.getEntityManagerForID(idpEntityID);
     if (manager == null) {
       throw new GuanxiException("Could not find manager for IdP '" + idpEntityID);
     }
     Metadata entityMetadata = manager.getMetadata(idpEntityID);
     if (entityMetadata == null) {
       throw new GuanxiException("Could not find metadata for IdP " + idpEntityID);
     }
     EntityDescriptorType saml2Metadata = (EntityDescriptorType)entityMetadata.getPrivateData();
 
     // Look for SAML2 endpoints
     EndpointType[] ssos = saml2Metadata.getIDPSSODescriptorArray(0).getSingleSignOnServiceArray();
     for (EndpointType sso : ssos) {
       String binding = sso.getBinding();
       if ((binding.equals(SAML.SAML2_BINDING_HTTP_POST)) ||
           (binding.equals(SAML.SAML2_BINDING_HTTP_REDIRECT))) {
         return saml2ProfileService;
       }
     }
 
     // If we get here, SAML2 isn't supported so use Shibboleth
     return shibbolethProfileService;
   }
 
   // Setters
   public void setMessages(MessageSource messages) { this.messages = messages; }
   public void setErrorView(String errorView) { this.errorView = errorView; }
   public void setErrorViewDisplayVar(String errorViewDisplayVar) { this.errorViewDisplayVar = errorViewDisplayVar; }
   public void setShibbolethProfileService(ProfileService shibbolethProfileService) { this.shibbolethProfileService = shibbolethProfileService; }
   public void setSaml2ProfileService(ProfileService saml2ProfileService) { this.saml2ProfileService = saml2ProfileService; }
   public void setSaml2DiscoveryProfileService(ProfileService saml2DiscoveryProfileService) { this.saml2DiscoveryProfileService = saml2DiscoveryProfileService; }
   public void setEntityIDs(HashMap<String, String> entityIDs) { this.entityIDs = entityIDs; }
   public void setUseDiscoveryService(boolean useDiscoveryService) { this.useDiscoveryService = useDiscoveryService; }
 }
