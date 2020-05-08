 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.datastore;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.IValidator;
 import org.apache.wicket.validation.ValidationError;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CatalogFactory;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.web.GeoServerSecuredPage;
 import org.geoserver.web.data.datastore.panel.CheckBoxParamPanel;
 import org.geoserver.web.data.datastore.panel.LabelParamPanel;
 import org.geoserver.web.data.datastore.panel.PasswordParamPanel;
 import org.geoserver.web.data.datastore.panel.TextParamPanel;
 import org.geoserver.web.data.tree.DataPage;
 import org.geotools.data.DataStoreFactorySpi;
 import org.geotools.data.DataAccessFactory.Param;
 import org.geotools.util.NullProgressListener;
 import org.vfny.geoserver.util.DataStoreUtils;
 
 /**
  * Provides a form to configure a geotools DataStore
  * 
  * @author Gabriel Roldan
  */
 public class DataStoreConfiguration extends GeoServerSecuredPage {
 
     private static final String DATASTORE_ID_PROPERTY_NAME = "Wicket_Data_Source_Name";
 
     private static final String DATASTORE_DESCRIPTION_PROPERTY_NAME = "Wicket_Data_Source_Description";
 
     private static final String DATASTORE_ENABLED_PROPERTY_NAME = "Wicket_Data_Source_Enabled";
 
     /**
      * Holds datastore parameters. Properties will be settled by the form input
      * fields.
      */
     private final Map<String, Serializable> parametersMap;
 
     /**
      * Id of the workspace the datastore is or is going to be attached to
      */
     private final String workspaceId;
 
     /**
      * Id of the datastore, null if creating a new datastore
      */
     private final String dataStoreInfoId;
 
     /**
      * Creates a new datastore configuration page to edit the properties of the
      * given data store
      * 
      * @param dataStoreInfoId
      *            the datastore id to modify, as per
      *            {@link DataStoreInfo#getId()}
      */
     public DataStoreConfiguration(final String dataStoreInfoId) {
         final Catalog catalog = getCatalog();
         final DataStoreInfo dataStoreInfo = catalog.getDataStore(dataStoreInfoId);
 
         this.dataStoreInfoId = dataStoreInfoId;
 
         if (null == dataStoreInfo) {
             throw new IllegalArgumentException("DataStore " + dataStoreInfoId + " not found");
         }
 
        Map<String, Serializable> connectionParameters;
        connectionParameters = new HashMap<String, Serializable>(dataStoreInfo.getConnectionParameters());
        connectionParameters = DataStoreUtils.getParams(connectionParameters);
         final DataStoreFactorySpi dsFactory = DataStoreUtils.aquireFactory(connectionParameters);
         if (null == dsFactory) {
             throw new IllegalArgumentException(
                     "Can't get the DataStoreFactory for the given connection parameters");
         }
 
         parametersMap = new HashMap<String, Serializable>(connectionParameters);
         parametersMap.put(DATASTORE_ID_PROPERTY_NAME, dataStoreInfoId);
         parametersMap.put(DATASTORE_DESCRIPTION_PROPERTY_NAME, dataStoreInfo.getDescription());
         parametersMap.put(DATASTORE_ENABLED_PROPERTY_NAME, Boolean.valueOf(dataStoreInfo
                 .isEnabled()));
 
         this.workspaceId = dataStoreInfo.getWorkspace().getId();
         init(dsFactory);
     }
 
     /**
      * Creates a new datastore configuration page to create a new datastore of
      * the given type
      * 
      * @param the
      *            workspace to attach the new datastore to, like in
      *            {@link WorkspaceInfo#getId()}
      * 
      * @param dataStoreFactDisplayName
      *            the type of datastore to create, given by its factory display
      *            name
      */
     public DataStoreConfiguration(final String workspaceId, final String dataStoreFactDisplayName) {
         if (workspaceId == null) {
             throw new NullPointerException("workspaceId can't be null");
         }
         this.workspaceId = workspaceId;
         this.dataStoreInfoId = null;
 
         final DataStoreFactorySpi dsFact = DataStoreUtils.aquireFactory(dataStoreFactDisplayName);
         if (dsFact == null) {
             throw new IllegalArgumentException("Can't locate a datastore factory named '"
                     + dataStoreFactDisplayName + "'");
         }
 
         parametersMap = new HashMap<String, Serializable>();
         // pre-populate map with default values
 
         Param[] parametersInfo = dsFact.getParametersInfo();
         for (int i = 0; i < parametersInfo.length; i++) {
             Serializable value;
             final Param param = parametersInfo[i];
             if (param.sample == null || param.sample instanceof Serializable) {
                 value = (Serializable) param.sample;
             } else {
                 value = String.valueOf(param.sample);
             }
 
             // as for GEOS-2080, we need to pre-populate the namespace parameter
             // value with the namespace uri from the parent 'folder'
             if ("namespace".equals(param.key) && value == null) {
                 final Catalog catalog = getCatalog();
                 final NamespaceInfo nsInfo = catalog.getNamespace(workspaceId);
                 final String nsUri = nsInfo.getURI();
                 value = nsUri;
             }
 
             parametersMap.put(param.key, value);
         }
         parametersMap.put(DATASTORE_ID_PROPERTY_NAME, null);
         parametersMap.put(DATASTORE_DESCRIPTION_PROPERTY_NAME, null);
         parametersMap.put(DATASTORE_ENABLED_PROPERTY_NAME, Boolean.TRUE);
 
         init(dsFact);
     }
 
     /**
      * 
      * @param workspaceId
      *            the id for the workspace to attach the new datastore or the
      *            current datastore is attached to
      * 
      * @param dsFactory
      *            the datastore factory to use
      */
     private void init(final DataStoreFactorySpi dsFactory) {
         WorkspaceInfo workspace = getWorkspace();
         if (workspace == null) {
             throw new IllegalArgumentException("Can't locate workspace with id " + workspaceId);
         }
 
         final List<ParamInfo> paramsInfo = new ArrayList<ParamInfo>();
         {
             Param[] dsParams = dsFactory.getParametersInfo();
             for (Param p : dsParams) {
                 paramsInfo.add(new ParamInfo(p));
             }
         }
 
         add(new Label("storeType", dsFactory.getDisplayName()));
         add(new Label("storeTypeDescription", dsFactory.getDescription()));
         add(new Label("workspaceName", workspace.getName()));
 
         final Form paramsForm = new Form("dataStoreForm");
 
         add(paramsForm);
 
         Panel dataStoreIdPanel;
         if (dataStoreInfoId == null) {
             IValidator dsIdValidator = new IValidator() {
                 private static final long serialVersionUID = 1L;
 
                 public void validate(IValidatable validatable) {
                     String value = (String) validatable.getValue();
                     if (value == null || value.trim().length() == 0) {
                         ValidationError error = new ValidationError();
                         error.setMessage("The Data Source name is mandatory");
                         validatable.error(error);
                         return;
                     }
 
                     value = value.trim();
 
                     List<DataStoreInfo> dataStores = getCatalog().getDataStores();
                     for (DataStoreInfo dsi : dataStores) {
                         String name = dsi.getName();
                         if (name.equals(value)) {
                             ValidationError error = new ValidationError();
                             error
                                     .setMessage("The Data Source name '" + value
                                             + "' is already used");
                             validatable.error(error);
                             break;
                         }
                     }
                 }
             };
             dataStoreIdPanel = new TextParamPanel("dataStoreIdPanel", parametersMap,
                     DATASTORE_ID_PROPERTY_NAME, "Data Source Name", true, Collections
                             .singletonList(dsIdValidator));
         } else {
             dataStoreIdPanel = new LabelParamPanel("dataStoreIdPanel", parametersMap,
                     DATASTORE_ID_PROPERTY_NAME, "Data Source Name");
         }
 
         paramsForm.add(dataStoreIdPanel);
 
         paramsForm.add(new TextParamPanel("dataStoreDescriptionPanel", parametersMap,
                 DATASTORE_DESCRIPTION_PROPERTY_NAME, "Description", false, null));
 
         paramsForm.add(new CheckBoxParamPanel("dataStoreEnabledPanel", parametersMap,
                 DATASTORE_ENABLED_PROPERTY_NAME, "Enabled"));
 
         ListView paramsList = new ListView("parameters", paramsInfo) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void populateItem(ListItem item) {
                 ParamInfo parameter = (ParamInfo) item.getModelObject();
                 Component inputComponent = getInputComponent("parameterPanel", parametersMap,
                         parameter);
                 if (parameter.getTitle() != null) {
                     inputComponent.add(new SimpleAttributeModifier("title", parameter.getTitle()));
                 }
                 item.add(inputComponent);
             }
         };
         // needed for form components not to loose state
         paramsList.setReuseItems(true);
 
         paramsForm.add(paramsList);
 
         paramsForm.add(new BookmarkablePageLink("cancel", DataPage.class));
 
         paramsForm.add(new SubmitLink("save") {
             private static final long serialVersionUID = 1L;
             @Override
             public void onSubmit() {
                 onSaveDataStore(paramsForm);
             }
         });
 
         paramsForm.add(new FeedbackPanel("feedback"));
     }
 
     private WorkspaceInfo getWorkspace() {
         Catalog catalog = getCatalog();
         WorkspaceInfo workspace = catalog.getWorkspace(workspaceId);
         return workspace;
     }
 
     /**
      * Callback method called when the submit button have been hit and the
      * parameters validation has succeed.
      * 
      * @param paramsForm
      *            the form to report any error to
      */
     private void onSaveDataStore(final Form paramsForm) {
         final Catalog catalog = getCatalog();
         final Map<String, Serializable> dsParams = parametersMap;
 
         DataStoreInfo dataStoreInfo;
 
         // dataStoreId already validated, so its safe to use
         final String dataStoreUniqueName = (String) dsParams.get(DATASTORE_ID_PROPERTY_NAME);
         final String description = (String) dsParams.get(DATASTORE_DESCRIPTION_PROPERTY_NAME);
         final Boolean enabled = (Boolean) dsParams.get(DATASTORE_ENABLED_PROPERTY_NAME);
 
         if (null == dataStoreInfoId) {
             // it is a new datastore
 
             final WorkspaceInfo workspace = catalog.getWorkspace(workspaceId);
 
             CatalogFactory factory = catalog.getFactory();
             dataStoreInfo = factory.createDataStore();
             dataStoreInfo.setName(dataStoreUniqueName);
             dataStoreInfo.setWorkspace(workspace);
             dataStoreInfo.setDescription(description);
             dataStoreInfo.setEnabled(enabled.booleanValue());
 
             Map<String, Serializable> connectionParameters;
             connectionParameters = dataStoreInfo.getConnectionParameters();
             connectionParameters.clear();
             connectionParameters.putAll(dsParams);
 
             try {
                 dataStoreInfo.getDataStore(new NullProgressListener());
             } catch (IOException e) {
                 paramsForm.error("Error creating data store, check the parameters. Error message: "
                         + e.getMessage());
                 return;
             }
             try {
                 catalog.add(dataStoreInfo);
             } catch (Exception e) {
                 paramsForm.error("Error creating data store with the provided parameters: "
                         + e.getMessage());
                 return;
             }
         } else {
             // it is an existing datastore that's being modified
             dataStoreInfo = catalog.getDataStore(dataStoreInfoId);
             dataStoreInfo.setName(dataStoreUniqueName);
             dataStoreInfo.setDescription(description);
             dataStoreInfo.setEnabled(enabled.booleanValue());
 
             Map<String, Serializable> connectionParameters;
             connectionParameters = dataStoreInfo.getConnectionParameters();
             final Map<String, Serializable> oldParams = new HashMap<String, Serializable>(
                     connectionParameters);
 
             connectionParameters.clear();
             connectionParameters.putAll(dsParams);
 
             catalog.getResourcePool().clear(dataStoreInfo);
 
             // try and grab the datastore with the new configuration
             // parameters...
             try {
                 dataStoreInfo.getDataStore(new NullProgressListener());
             } catch (Exception e) {
                 catalog.getResourcePool().clear(dataStoreInfo);
                 connectionParameters.clear();
                 connectionParameters.putAll(oldParams);
                 paramsForm.error("Error updating data store parameters: " + e.getMessage());
                 return;
             }
             // it worked, save it
             catalog.save(dataStoreInfo);
         }
         setResponsePage(DataPage.class);
     }
 
     /**
      * Creates a form input component for the given datastore param based on its
      * type and metadata properties.
      * 
      * @param param
      * @return
      */
     private Panel getInputComponent(final String componentId, final Map<String, ?> paramsMap,
             final ParamInfo param) {
 
         final String paramName = param.getName();
         final String paramLabel = param.getName();
         final boolean required = param.isRequired();
         final Class<?> binding = param.getBinding();
 
         Panel parameterPanel;
         if (Boolean.class == binding) {
             parameterPanel = new CheckBoxParamPanel(componentId, paramsMap, paramName, paramLabel);
         } else if (String.class == binding && param.isPassword()) {
             parameterPanel = new PasswordParamPanel(componentId, paramsMap, paramName, paramLabel,
                     required);
         } else {
             parameterPanel = new TextParamPanel(componentId, paramsMap, paramName, paramLabel,
                     required, null);
         }
         return parameterPanel;
     }
 }
