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
 
 package org.guanxi.sp.engine.service.shibboleth;
 
 import org.apache.log4j.Logger;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.Utils;
 import org.guanxi.common.entity.EntityFarm;
 import org.guanxi.common.entity.EntityManager;
 import org.guanxi.common.metadata.Metadata;
 import org.guanxi.sp.engine.service.generic.ProfileService;
 import org.guanxi.xal.saml2.metadata.GuardRoleDescriptorExtensions;
 import org.guanxi.xal.saml_2_0.metadata.EndpointType;
 import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorType;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 /**
  * Shibboleth ProfileService implementation
  *
  * @author alistair
  */
 public class ShibbolethProfileService implements ProfileService {
   /** Our logger */
   private static final Logger logger = Logger.getLogger(ShibbolethProfileService.class.getName());
   /** The marker in our WAYF location map for the one to use as the default location */
   private static final String DEFAULT_WAYF_MARKER = "__DEFAULT__";
   /** The list of Guard to WAYF location mappings */
   private HashMap<String, String> wayfs = null;
   /** The view page to use for the WAYF */
   private String wayfViewJSP = null;
   /** The Shibboleth Attribute Consumer service for the Engine */
   private String attributeConsumerService = null;
 
   /** @see org.guanxi.sp.engine.service.generic.ProfileService#init() */
   public void init() {}
 
   /** @see org.guanxi.sp.engine.service.generic.ProfileService#doProfile(String, String, org.guanxi.xal.saml2.metadata.GuardRoleDescriptorExtensions, String, org.guanxi.common.entity.EntityFarm)  */
   public ModelAndView doProfile(String guardID, String guardSessionID, GuardRoleDescriptorExtensions guardNativeMetadata,
                                 String entityID, EntityFarm farm) throws GuanxiException {
     ModelAndView mAndV = new ModelAndView();
     mAndV.setViewName(wayfViewJSP);
 
     // If there's an entityID try to load its metadata
     String wayfForGuard = null;
     String defaultWAYFLocation = null;
     if (entityID != null) {
       EntityManager manager = farm.getEntityManagerForID(entityID);
       Metadata idpMetadata = manager.getMetadata(entityID);
       EntityDescriptorType saml2Metadata = (EntityDescriptorType)idpMetadata.getPrivateData();
       EndpointType[] ssoServices = saml2Metadata.getIDPSSODescriptorArray(0).getSingleSignOnServiceArray();
       for (EndpointType ssoService : ssoServices) {
         if (ssoService.getBinding().equals("urn:mace:shibboleth:1.0:profiles:AuthnRequest")) {
           wayfForGuard = ssoService.getLocation();
           logger.info("Guard '" + guardID + "' obtained WAYFless location : " + wayfForGuard);
         }
       }
     }
 
     // No entityID or no suitable profile endpoint so use a WAYF
     if (wayfForGuard == null) {
       // Find out which WAYF to use for this Guard
       for (String guardId : wayfs.keySet()) {
         if (guardId.equals(DEFAULT_WAYF_MARKER)) {
           defaultWAYFLocation = wayfs.get(guardId);
         }
         if (guardId.equals(guardID)) {
           wayfForGuard = wayfs.get(guardId);
         }
       }
       wayfForGuard = (wayfForGuard != null) ? wayfForGuard : defaultWAYFLocation;
       logger.info("Guard '" + guardID + "' obtained WAYF location : " + wayfForGuard);
     }
 
     try {
       // Guard either gets its own WAYF or the default one for all other Guards
       mAndV.getModel().put("wayfLocation", wayfForGuard);
       mAndV.getModel().put("shire", URLEncoder.encode(attributeConsumerService, "UTF-8"));
       mAndV.getModel().put("target", URLEncoder.encode(guardSessionID, "UTF-8"));
      mAndV.getModel().put("time", Long.toString(System.currentTimeMillis() / 1000));
       mAndV.getModel().put("providerId", guardID);
     }
     catch(UnsupportedEncodingException uee) {
       logger.error("something went wrong putting the WAYF location together", uee);
       throw new GuanxiException(uee);
     }
 
     return mAndV;
   }
 
   // Setters
   public void setWayfs(HashMap<String, String> wayfs) { this.wayfs = wayfs; }
   public void setWayfViewJSP(String wayfViewJSP) { this.wayfViewJSP = wayfViewJSP; }
   public void setAttributeConsumerService(String attributeConsumerService) { this.attributeConsumerService = attributeConsumerService; }
 }
