 package com.amee.platform.resource.profileitem.v_3_6;
 
 import java.util.TimeZone;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.RequestWrapper;
 import com.amee.base.resource.ResourceBeanFinder;
 import com.amee.base.transaction.AMEETransaction;
 import com.amee.calculation.service.CalculationService;
 import com.amee.domain.ProfileItemService;
 import com.amee.domain.item.profile.ProfileItem;
 import com.amee.domain.profile.Profile;
 import com.amee.platform.resource.ResourceService;
 import com.amee.platform.resource.profileitem.ProfileItemResource;
 import com.amee.service.auth.ResourceAuthorizationService;
 
 @Service
 @Scope("prototype")
 @Since("3.6.0")
 public class ProfileItemBuilder_3_6_0 implements ProfileItemResource.Builder {
 
     @Autowired
     private ProfileItemService profileItemService;
 
     @Autowired
     private ResourceService resourceService;
 
     @Autowired
     private ResourceAuthorizationService resourceAuthorizationService;
 
     @Autowired
     private ResourceBeanFinder resourceBeanFinder;
 
     @Autowired
     private CalculationService calculationService;
 
     private ProfileItemResource.Renderer renderer;
 
     @Override
     @AMEETransaction
     @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
     public Object handle(RequestWrapper requestWrapper) {
 
         // Get resource entities for this request
         Profile profile = resourceService.getProfile(requestWrapper);
         ProfileItem profileItem = resourceService.getProfileItem(requestWrapper, profile);
 
         // Authorised for profile item?
         resourceAuthorizationService.ensureAuthorizedForBuild(
                 requestWrapper.getAttributes().get("activeUserUid"), profileItem);
 
         // Handle the profile item
         handle(requestWrapper, profileItem);
         ProfileItemResource.Renderer renderer = getRenderer(requestWrapper);
         renderer.ok();
         return renderer.getObject();
     }
 
     @Override
     public void handle(RequestWrapper requestWrapper, ProfileItem profileItem) {
 
         // Get renderer
         ProfileItemResource.Renderer renderer = getRenderer(requestWrapper);
         renderer.start();
 
         // Get rendering options from matrix parameters
         boolean full = requestWrapper.getMatrixParameters().containsKey("full");
         boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
         boolean amounts = requestWrapper.getMatrixParameters().containsKey("amounts");
         boolean name = requestWrapper.getMatrixParameters().containsKey("name");
         boolean dates = requestWrapper.getMatrixParameters().containsKey("dates");
         boolean category = requestWrapper.getMatrixParameters().containsKey("category");
         boolean note = requestWrapper.getMatrixParameters().containsKey("note");
 
         // New Profile Item and basic
         renderer.newProfileItem(profileItem);
         renderer.addBasic();
 
         // Optionals
         if (audit || full) {
             renderer.addAudit();
         }
         if (name || full) {
             renderer.addName();
         }
         if (dates || full) {
             TimeZone currentUserTimeZone = resourceService.getCurrentUser(requestWrapper).getTimeZone();
             renderer.addDates(currentUserTimeZone);
         }
         if (category || full) {
             renderer.addCategory();
         }
         if (amounts || full) {
             renderer.addReturnValues(profileItem.getAmounts());
         }
         if (note || full) {
             renderer.addNote();
         }
 
     @Override
     public ProfileItemResource.Renderer getRenderer(RequestWrapper requestWrapper) {
         if (renderer == null) {
             renderer =
                     (ProfileItemResource.Renderer) resourceBeanFinder.getRenderer(ProfileItemResource.Renderer.class, requestWrapper);
         }
         return renderer;
     }
 }
