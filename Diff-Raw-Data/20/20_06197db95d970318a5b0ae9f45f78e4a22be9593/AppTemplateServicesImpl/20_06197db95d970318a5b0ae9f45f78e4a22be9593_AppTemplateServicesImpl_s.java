 package org.iplantc.de.client.services.impl;
 
 import org.iplantc.de.client.models.DEProperties;
 import org.iplantc.de.client.models.HasId;
 import org.iplantc.de.client.models.apps.integration.AppTemplate;
 import org.iplantc.de.client.models.apps.integration.AppTemplateAutoBeanFactory;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.ArgumentGroup;
 import org.iplantc.de.client.models.apps.integration.ArgumentType;
 import org.iplantc.de.client.models.apps.integration.DataSource;
 import org.iplantc.de.client.models.apps.integration.DataSourceList;
 import org.iplantc.de.client.models.apps.integration.FileInfoType;
 import org.iplantc.de.client.models.apps.integration.FileInfoTypeList;
 import org.iplantc.de.client.models.apps.integration.JobExecution;
 import org.iplantc.de.client.models.apps.integration.ReferenceGenome;
 import org.iplantc.de.client.models.apps.integration.ReferenceGenomeList;
 import org.iplantc.de.client.models.apps.integration.SelectionItem;
 import org.iplantc.de.client.models.apps.integration.SelectionItemGroup;
 import org.iplantc.de.client.services.AppMetadataServiceFacade;
 import org.iplantc.de.client.services.AppTemplateServices;
 import org.iplantc.de.client.services.DEServiceFacade;
 import org.iplantc.de.client.services.DeployedComponentServices;
 import org.iplantc.de.client.services.converters.AppTemplateCallbackConverter;
 import org.iplantc.de.client.util.AppTemplateUtils;
 import org.iplantc.de.client.util.JsonUtil;
 import org.iplantc.de.shared.SharedServiceFacade;
 import org.iplantc.de.shared.services.BaseServiceCallWrapper.Type;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 import java.util.List;
 import java.util.Queue;
 
 public class AppTemplateServicesImpl implements AppTemplateServices, AppMetadataServiceFacade {
     private static final Queue<AsyncCallback<List<DataSource>>> dataSourceQueue = Lists.newLinkedList();
     private static final AppTemplateAutoBeanFactory factory = GWT.create(AppTemplateAutoBeanFactory.class);
     private static final Queue<AsyncCallback<List<FileInfoType>>> fileInfoTypeQueue = Lists.newLinkedList();
     private static final Queue<AsyncCallback<List<ReferenceGenome>>> refGenQueue = Lists.newLinkedList();
     private final List<DataSource> dataSourceList = Lists.newArrayList();
     // private final DataSourceProperties dataSourceProperties = GWT.create(DataSourceProperties.class);
     private final DeployedComponentServices dcServices = GWT.create(DeployedComponentServices.class);
     private final List<FileInfoType> fileInfoTypeList = Lists.newArrayList();
 
     // private final FileInfoTypeProperties fileInfoTypeProperties =
     // GWT.create(FileInfoTypeProperties.class);
     // private final ReferenceGenomeProperties referenceGenomeProperties =
     // GWT.create(ReferenceGenomeProperties.class);
     private final List<ReferenceGenome> refGenList = Lists.newArrayList();
 
     @Override
     public void cmdLinePreview(AppTemplate at, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "arg-preview"; //$NON-NLS-1$
         AppTemplate cleaned = doCmdLinePreviewCleanup(at);
         Splittable split = appTemplateToSplittable(cleaned);
         String payload = split.getPayload();
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.POST, address, payload);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void getAppTemplate(HasId appId, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "app/" + appId.getId(); //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(factory, dcServices, callback));
     }
 
     @Override
     public AppTemplateAutoBeanFactory getAppTemplateFactory() {
         return factory;
     }
 
     @Override
     public void getAppTemplateForEdit(HasId appId, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "edit-app/" + appId.getId(); //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(factory, dcServices, callback));
     }
 
     @Override
     public void getAppTemplatePreview(AppTemplate at, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "preview-template"; //$NON-NLS-1$
         Splittable split = appTemplateToSplittable(at);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.POST, address, split.getPayload());
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(factory, dcServices, callback));
     }
 
     @Override
     public void getDataSources(AsyncCallback<List<DataSource>> callback) {
         if (!dataSourceList.isEmpty()) {
             callback.onSuccess(dataSourceList);
             return;
         } else {
             enqueueDataSourceCallback(callback);
         }
     }
 
     @Override
     public void getFileInfoTypes(AsyncCallback<List<FileInfoType>> callback) {
         if (!fileInfoTypeList.isEmpty()) {
             callback.onSuccess(fileInfoTypeList);
             return;
         } else {
             enqueueFileInfoTypeCallback(callback);
         }
     }
 
     @Override
     public void getReferenceGenomes(AsyncCallback<List<ReferenceGenome>> callback) {
         if (!refGenList.isEmpty()) {
             callback.onSuccess(refGenList);
             return;
         } else {
             enqueueRefGenomeCallback(callback);
         }
     }
 
     @Override
     public void launchAnalysis(AppTemplate at, JobExecution je, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "workspaces/" + je.getWorkspaceId() + "/newexperiment"; //$NON-NLS-1$ //$NON-NLS-2$
         Splittable assembledPayload = doAssembleLaunchAnalysisPayload(at, je);
         GWT.log("LaunchAnalysis Json:\n" + JsonUtil.prettyPrint(assembledPayload));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PUT, address, assembledPayload.getPayload());
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void rerunAnalysis(HasId analysisId, AsyncCallback<AppTemplate> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "app-rerun-info/" + analysisId.getId(); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
 
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppTemplateCallbackConverter(factory, dcServices, callback));
     }
 
     @Override
     public void saveAndPublishAppTemplate(AppTemplate at, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "update-app"; //$NON-NLS-1$
         Splittable split = appTemplateToSplittable(at);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PUT, address, split.getPayload());
         callSecuredService(callback, wrapper);
     }
 
     @Override
     public void updateAppLabels(AppTemplate at, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "update-app-labels"; //$NON-NLS-1$
         Splittable split = appTemplateToSplittable(at);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.POST, address, split.getPayload());
         callSecuredService(callback, wrapper);
 
     }
 
     private Splittable appTemplateToSplittable(AppTemplate at) {
         Splittable ret = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(at));
         if (at.getDeployedComponent() != null) {
             StringQuoter.create(at.getDeployedComponent().getId()).assign(ret, "component_id");//$NON-NLS-1$
         }
         // JDS Convert Argument.getValue() which contain any selected/checked *Selection types to only
         // contain their value.
         for (ArgumentGroup ag : at.getArgumentGroups()) {
             for (Argument arg : ag.getArguments()) {
                 if (arg.getType().equals(ArgumentType.TreeSelection)) {
                     if ((arg.getSelectionItems() != null) && (arg.getSelectionItems().size() == 1)) {
                         SelectionItemGroup sig = AppTemplateUtils.selectionItemToSelectionItemGroup(arg.getSelectionItems().get(0));
                         Splittable split = AppTemplateUtils.getSelectedTreeItemsAsSplittable(sig);
                         arg.setValue(split);
                     }
                 }
                     }
                 }
         return ret;
     }
 
     private void callSecuredService(AsyncCallback<String> callback, ServiceCallWrapper wrapper) {
         SharedServiceFacade.getInstance().getServiceData(wrapper, callback);
             }
 
     private Splittable doAssembleLaunchAnalysisPayload(AppTemplate at, JobExecution je) {
         Splittable assembledPayload = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(je));
         Splittable configSplit = StringQuoter.createSplittable();
         for (ArgumentGroup ag : at.getArgumentGroups()) {
             for (Argument arg : ag.getArguments()) {
                 Splittable value = arg.getValue();
                 if ((value == null) && !arg.getType().equals(ArgumentType.TreeSelection)) {
                     continue;
                 }
                 if (AppTemplateUtils.isSimpleSelectionArgumentType(arg.getType())) {
                     value.assign(configSplit, arg.getId());
                 } else if (AppTemplateUtils.isDiskResourceArgumentType(arg.getType()) && !arg.getType().equals(ArgumentType.MultiFileSelector)) {
                     value.get("path").assign(configSplit, arg.getId());
                 } else if (arg.getType().equals(ArgumentType.MultiFileSelector) && value.isIndexed()) {
                     value.assign(configSplit, arg.getId());
                 } else if (arg.getType().equals(ArgumentType.TreeSelection) && (arg.getSelectionItems() != null) && (arg.getSelectionItems().size() == 1)) {
                     SelectionItemGroup sig = AppTemplateUtils.selectionItemToSelectionItemGroup(arg.getSelectionItems().get(0));
                     Splittable sigSplit = AppTemplateUtils.getSelectedTreeItemsAsSplittable(sig);
                     sigSplit.assign(configSplit, arg.getId());
                 } else {
                     value.assign(configSplit, arg.getId());
                 }
 
             }
                 }
         configSplit.assign(assembledPayload, "config");
         return assembledPayload;
             }
 
     private AppTemplate doCmdLinePreviewCleanup(AppTemplate templateToClean) {
         AppTemplate copy = AppTemplateUtils.copyAppTemplate(templateToClean);
         // JDS Transform any Argument's value which contains a full SelectionItem obj to the
         // SelectionItem's value
         for (ArgumentGroup ag : copy.getArgumentGroups()) {
             for (Argument arg : ag.getArguments()) {
                 if (AppTemplateUtils.isSimpleSelectionArgumentType(arg.getType())) {
 
                     if ((arg.getValue() != null) && arg.getValue().isKeyed() && !arg.getValue().isUndefined("value")) {
                         arg.setValue(arg.getValue().get("value"));
                     } else {
                         arg.setValue(null);
                     }
                 } else if (arg.getType().equals(ArgumentType.TreeSelection)) {
                     if ((arg.getSelectionItems() != null) && (arg.getSelectionItems().size() == 1)) {
                         SelectionItemGroup sig = AppTemplateUtils.selectionItemToSelectionItemGroup(arg.getSelectionItems().get(0));
                         List<SelectionItem> siList = AppTemplateUtils.getSelectedTreeItems(sig);
                         String retVal = "";
                         for (SelectionItem si : siList) {
                             if (si.getValue() != null) {
                                 retVal += si.getValue() + " ";
                             }
                         }
                         arg.setValue(StringQuoter.create(retVal.trim()));
                     }
                 } else if (arg.getType().equals(ArgumentType.EnvironmentVariable)) {
                     // Exclude environment variables from the command line
                     arg.setValue(null);
                     arg.setName("");
                 } else if (AppTemplateUtils.isDiskResourceOutputType(arg.getType())) {
                     if (arg.getDataObject().isImplicit()) {
                         arg.setValue(null);
                         arg.setName("");
                             }
                         }
                     }
                 }
 
         return copy;
             }
 
    private void enqueueDataSourceCallback(AsyncCallback<List<DataSource>> callback) {
         if (dataSourceQueue.isEmpty()) {
             String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "get-workflow-elements/data-sources"; //$NON-NLS-1$
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
             DEServiceFacade.getInstance().getServiceData(wrapper, new AsyncCallback<String>() {
                 @Override
                 public void onFailure(Throwable caught) {
                    // FIXME This error needs to be thrown from somewhere else
                    // REMOVE_THIS_ErrorHandler.post(caught);
                 }
 
                 @Override
                 public void onSuccess(String result) {
                     DataSourceList dsList = AutoBeanCodex.decode(factory, DataSourceList.class, result).as();
                     dataSourceList.clear();
                     dataSourceList.addAll(dsList.getDataSources());
 
                     while (!dataSourceQueue.isEmpty()) {
                         dataSourceQueue.remove().onSuccess(dataSourceList);
                     }
                 }
             });
 
                 }
         dataSourceQueue.add(callback);
     }
 
    private void enqueueFileInfoTypeCallback(AsyncCallback<List<FileInfoType>> callback) {
         if (fileInfoTypeQueue.isEmpty()) {
             String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "get-workflow-elements/info-types";//$NON-NLS-1$
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 
             DEServiceFacade.getInstance().getServiceData(wrapper, new AsyncCallback<String>() {
 
                 @Override
                 public void onFailure(Throwable caught) {
                    // FIXME This error needs to be thrown from somewhere else
                    // REMOVE_THIS_ErrorHandler.post(caught);
                 }
 
                 @Override
                 public void onSuccess(String result) {
                     FileInfoTypeList fitListWrapper = AutoBeanCodex.decode(factory, FileInfoTypeList.class, result).as();
 
                     fileInfoTypeList.clear();
                     fileInfoTypeList.addAll(fitListWrapper.getFileInfoTypes());
 
                     while (!fileInfoTypeQueue.isEmpty()) {
                         fileInfoTypeQueue.remove().onSuccess(fileInfoTypeList);
                     }
                 }
             });
                 }
         fileInfoTypeQueue.add(callback);
 
     }
 
    private void enqueueRefGenomeCallback(AsyncCallback<List<ReferenceGenome>> callback) {
         if (refGenQueue.isEmpty()) {
             String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "reference-genomes"; //$NON-NLS-1$
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
             DEServiceFacade.getInstance().getServiceData(wrapper, new AsyncCallback<String>() {
                 @Override
                 public void onFailure(Throwable caught) {
                    // FIXME This error needs to be thrown from somewhere else
                    // REMOVE_THIS_ErrorHandler.post(caught);
                 }
 
                 @Override
                 public void onSuccess(String result) {
                     ReferenceGenomeList rgList = AutoBeanCodex.decode(factory, ReferenceGenomeList.class, result).as();
                     refGenList.clear();
                     refGenList.addAll(rgList.getReferenceGenomes());
 
                     while (!refGenQueue.isEmpty()) {
                         refGenQueue.remove().onSuccess(refGenList);
                     }
                 }
             });
 
         }
         refGenQueue.add(callback);
     }
 
 }
