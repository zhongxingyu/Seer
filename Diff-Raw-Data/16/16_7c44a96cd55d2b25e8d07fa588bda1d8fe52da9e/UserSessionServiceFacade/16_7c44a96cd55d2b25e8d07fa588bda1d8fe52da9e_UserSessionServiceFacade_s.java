 package org.iplantc.de.client.services;
 
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * A service facade to save and retrieve user session
  * 
  * @author sriram
  * 
  */
 public class UserSessionServiceFacade {
 
     public void getUserSession(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "sessions"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void saveUserSession(JSONObject json, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "sessions"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 json.toString());
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void clearUserSession(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "sessions"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.DELETE, address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void getUserPreferences(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "preferences"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void saveUserPreferences(JSONObject json, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "preferences"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 json.toString());
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void searchCollaborators(String term, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "user-search/" + term; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
 
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void getCollaborators(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "collaborators";
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void addCollaborators(JSONObject users, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "collaborators"; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 users.toString());
 
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     public void removeCollaborators(JSONObject users, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "remove-collaborators"; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 users.toString());
 
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
 }
