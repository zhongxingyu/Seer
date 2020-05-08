 package org.iplantc.de.client.services.impl;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplate;
 import org.iplantc.core.uiapps.widgets.client.models.Argument;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentGroup;
 import org.iplantc.core.uiapps.widgets.client.models.JobExecution;
 import org.iplantc.core.uiapps.widgets.client.services.AppTemplateServices;
 import org.iplantc.core.uiapps.widgets.client.services.impl.AppTemplateCallbackConverter;
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.de.shared.SharedServiceFacade;
 import org.iplantc.de.shared.services.BaseServiceCallWrapper.Type;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 public class AppTemplateServicesImpl implements AppTemplateServices {
 
     @Override
     public void getAppTemplate(HasId appId, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() 
                 + "app/" + appId.getId(); //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(callback));
     }
 
     @Override
     public void getAppTemplateForEdit(HasId appId, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl()
                 + "edit-app/" + appId.getId(); //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(callback));
     }
 
     @Override
     public void saveAndPublishAppTemplate(AppTemplate at, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() 
                 + "update-app"; //$NON-NLS-1$
         Splittable split = appTemplateToSplittable(at);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PUT, 
                 address, split.getPayload());
         callSecuredService(callback, wrapper);
     }
     
     @Override
     public void getAppTemplatePreview(AppTemplate at, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() 
                 + "preview-template"; //$NON-NLS-1$
         Splittable split = appTemplateToSplittable(at);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.POST, 
                 address, split.getPayload());
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(callback));
     }
 
     @Override
     public void rerunAnalysis(HasId analysisId, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() 
                 + "app-rerun-info/" + analysisId.getId(); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
 
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(callback));
     }
 
     @Override
     public void cmdLinePreview(AppTemplate at, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl()
                + "arg-preview"; //$NON-NLS-1$
        Splittable split = appTemplateToSplittable(at);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.POST, 
                address, split.getPayload());
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void launchAnalysis(AppTemplate at, JobExecution je, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() 
                 + "workspaces/" + je.getWorkspaceId() + "/newexperiment"; //$NON-NLS-1$
         Splittable split = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(je));
         Splittable configSplit = StringQuoter.createSplittable();
         for (ArgumentGroup ag : at.getArgumentGroups()) {
             for (Argument arg : ag.getArguments()) {
                 if (arg.getValue() != null) {
                     if (arg.getValue().isKeyed()) {
                         for(String key : arg.getValue().getPropertyKeys()){
                             if(key.equals("id")){
                                 // JDS When we encounter anything has an "ID" key, we use the value of that key instead of the object.
                                 // For example, we do not need to pass in an entire "Folder" object, we just need the path.
                                 arg.getValue().get("id").assign(configSplit, arg.getId());
                             }
                         }
                     } else {
                         arg.getValue().assign(configSplit, arg.getId());
                     }
                 }
             }
         }
         configSplit.assign(split, "config");
         GWT.log("LaunchAnalysis Json:\n" + JsonUtil.prettyPrint(split));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PUT, address, split.getPayload());
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     private Splittable appTemplateToSplittable(AppTemplate at){
         Splittable ret = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(at));
         if(at.getDeployedComponent() != null){
             StringQuoter.create(at.getDeployedComponent().getId()).assign(ret, "component_id");
         }
         // JDS Convert Argument.getValue() which contain any selected/checked *Selection types to only
         // contain their value.
         /*AutoBean<AppTemplate> ab = AutoBeanUtils.getAutoBean(at);
         for (ArgumentGroup ag : at.getArgumentGroups()) {
             for (Argument arg : ag.getArguments()) {
                 if (AppWizardFieldFactory.isSelectionArgumentType(arg)) {
 
                 }
             }
         }*/
         return ret;
     }
 
     private void callSecuredService(AsyncCallback<String> callback, ServiceCallWrapper wrapper) {
         SharedServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
 }
