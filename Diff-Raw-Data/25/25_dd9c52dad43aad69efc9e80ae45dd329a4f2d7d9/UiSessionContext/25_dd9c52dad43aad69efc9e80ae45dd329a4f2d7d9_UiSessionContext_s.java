 package org.openmrs.module.appui;
 
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

 import org.openmrs.Location;
 import org.openmrs.Provider;
 import org.openmrs.User;
 import org.openmrs.api.APIAuthenticationException;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.ProviderService;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.context.UserContext;
 import org.openmrs.module.appframework.context.SessionContext;
 
 /**
  *
  */
 public class UiSessionContext extends SessionContext {
 
     public final static String LOCATION_SESSION_ATTRIBUTE = "emrContext.sessionLocationId";
 
    LocationService locationService;
 
    ProviderService providerService;
 
    UserContext userContext;
 
    Provider currentProvider;
 
    Location sessionLocation;
 
     public UiSessionContext(LocationService locationService, ProviderService providerService, HttpServletRequest request) {
         this.locationService = locationService;
         this.providerService = providerService;
         Integer locationId = (Integer) request.getSession().getAttribute(LOCATION_SESSION_ATTRIBUTE);
         if (locationId != null) {
             this.setSessionLocationId(locationId);
             sessionLocation = locationService.getLocation(locationId);
         }
         userContext = Context.getUserContext();
         if (userContext != null && userContext.getAuthenticatedUser() != null) {
             User currentUser = userContext.getAuthenticatedUser();
             Collection<Provider> providers = providerService.getProvidersByPerson(currentUser.getPerson(), false);
             if (providers.size() > 1) {
                 throw new IllegalStateException("Can't handle users with multiple provider accounts");
             } else if (providers.size() == 1) {
                 currentProvider = providers.iterator().next();
             }
         }
     }
 
     public Location getSessionLocation() {
         return sessionLocation;
     }
 
     public void setSessionLocation(Location sessionLocation) {
         this.sessionLocation = sessionLocation;
     }
 
     @Override
     public Integer getSessionLocationId() {
         return sessionLocation == null ? null : sessionLocation.getLocationId();
     }
 
     public User getCurrentUser() {
         return userContext.getAuthenticatedUser();
     }
 
     @Override
     public Integer getCurrentUserId() {
         User currentUser = getCurrentUser();
         return currentUser == null ? null : currentUser.getUserId();
     }
 
     public Provider getCurrentProvider() {
         return currentProvider;
     }
 
     @Override
     public Integer getCurrentProviderId() {
         return currentProvider == null ? null : currentProvider.getProviderId();
     }
 
     public boolean isAuthenticated() {
         return userContext.isAuthenticated();
     }
 
     /**
      * @throws {@link org.openmrs.api.APIAuthenticationException} if no user is authenticated
      */
     public void requireAuthentication() throws APIAuthenticationException {
         if (!isAuthenticated()) {
             throw new APIAuthenticationException();
         }
     }
 
     public Locale getLocale() {
         return userContext.getLocale();
     }
 }
