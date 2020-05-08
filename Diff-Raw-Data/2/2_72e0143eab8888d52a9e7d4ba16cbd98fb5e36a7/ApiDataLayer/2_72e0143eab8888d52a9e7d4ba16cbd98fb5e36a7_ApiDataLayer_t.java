 package com.versionone.common.sdk;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.versionone.Oid;
 import com.versionone.apiclient.AndFilterTerm;
 
 import com.versionone.apiclient.APIException;
 
 import com.versionone.apiclient.Asset;
 import com.versionone.apiclient.AssetState;
 import com.versionone.apiclient.Attribute;
 import com.versionone.apiclient.ConnectionException;
 import com.versionone.apiclient.FilterTerm;
 import com.versionone.apiclient.IAssetType;
 import com.versionone.apiclient.IAttributeDefinition;
 import com.versionone.apiclient.IFilterTerm;
 import com.versionone.apiclient.ILocalizer;
 import com.versionone.apiclient.IMetaModel;
 import com.versionone.apiclient.IServices;
 import com.versionone.apiclient.Localizer;
 import com.versionone.apiclient.MetaException;
 import com.versionone.apiclient.MetaModel;
 import com.versionone.apiclient.OidException;
 import com.versionone.apiclient.OrderBy;
 import com.versionone.apiclient.Query;
 import com.versionone.apiclient.QueryResult;
 import com.versionone.apiclient.Services;
 import com.versionone.apiclient.V1APIConnector;
 import com.versionone.apiclient.V1Configuration;
 import com.versionone.apiclient.IOperation;
 import com.versionone.apiclient.V1Exception;
 import com.versionone.common.Activator;
 import com.versionone.common.preferences.PreferenceConstants;
 import com.versionone.common.preferences.PreferencePage;
 
 public class ApiDataLayer {
 
     private static final String MetaUrlSuffix = "meta.v1/";
     private static final String LocalizerUrlSuffix = "loc.v1/";
     private static final String DataUrlSuffix = "rest-1.v1/";
     private static final String ConfigUrlSuffix = "config.v1/";
 
     private static final List<String> effortTrackingAttributesList = Arrays.asList(Workitem.DetailEstimateProperty,
             "ToDo", "Done", "Effort", "Actuals");
 
     private final Map<String, IAssetType> types = new HashMap<String, IAssetType>(5);
     private final Map<Asset, Double> efforts  = new HashMap<Asset, Double>();
 
     public static final String QuickCloseOperation = "QuickClose";
     public static final String CloseOperation = "Inactivate";
     public static final String SignupOperation = "QuickSignup";
     
     private IAssetType projectType;
     private IAssetType taskType;
     private IAssetType testType;
     private IAssetType defectType;
     private IAssetType storyType;
     private IAssetType workitemType;
     private IAssetType primaryWorkitemType;
     private IAssetType effortType;
 
     private static ApiDataLayer instance;
     private boolean isConnected;
 
     public Oid memberOid;
     private String path;
     private String userName;
     private String password;
     private boolean integrated;
 
     private QueryResult assetList;
     private final List<IAttributeDefinition> alreadyUsedDefinition = new ArrayList<IAttributeDefinition>();
     private static LinkedList<AttributeInfo> attributesToQuery = new LinkedList<AttributeInfo>();
     private Map<String, PropertyValues> listPropertyValues;
 
     private boolean trackEffort;
     public EffortTrackingLevel defectTrackingLevel;
     public EffortTrackingLevel storyTrackingLevel;
 
     private IMetaModel metaModel;
     private IServices services;
     private ILocalizer localizer;
 
     private String currentProjectId;
     public boolean showAllTasks = false;
     
     private HashSet<Asset> assetsToIgnore = new HashSet<Asset>();
 
     private ApiDataLayer() {
         String[] prefixes = new String[] { Workitem.TaskPrefix, Workitem.DefectPrefix, Workitem.StoryPrefix,
                 Workitem.TestPrefix };
         for (String prefix : prefixes) {
             attributesToQuery.addLast(new AttributeInfo("CheckQuickClose", prefix, false));
             attributesToQuery.addLast(new AttributeInfo("CheckQuickSignup", prefix, false));
         }
     }
 
     private ApiDataLayer(IServices services, IMetaModel metaModel, ILocalizer localizer) throws Exception {
         this.metaModel = metaModel;
         this.services = services;
         this.localizer = localizer;
         
         initTypes();
         isConnected = true;
         memberOid = this.services.getLoggedIn();
         listPropertyValues = getListPropertyValues();
     }
 
     public static ApiDataLayer getInstance() {
         if (instance == null) {
             instance = new ApiDataLayer();
         }
         return instance;
     }
 
     public boolean connect(String path, String userName, String password, boolean integrated) throws DataLayerException {
         isConnected = false;
         this.userName = userName;
         this.password = password;
         this.integrated = integrated;
         assetList = null;
         assetsToIgnore.clear();
         efforts.clear();
         types.clear();
         try {
             V1APIConnector metaConnector = new V1APIConnector(path + MetaUrlSuffix, userName, password);
             metaModel = new MetaModel(metaConnector);
 
             V1APIConnector localizerConnector = new V1APIConnector(path + LocalizerUrlSuffix, userName, password);
             localizer = new Localizer(localizerConnector);
 
             V1APIConnector dataConnector = new V1APIConnector(path + DataUrlSuffix, userName, password);
             services = new Services(metaModel, dataConnector);
 
             V1Configuration v1Config = new V1Configuration(new V1APIConnector(path + ConfigUrlSuffix));
 
             initTypes();
 
             trackEffort = v1Config.isEffortTracking();
             if (trackEffort) {
                 effortType = metaModel.getAssetType("Actual");
             }
 
             storyTrackingLevel = EffortTrackingLevel.translate(v1Config.getStoryTrackingLevel());
             defectTrackingLevel = EffortTrackingLevel.translate(v1Config.getDefectTrackingLevel());
 
             memberOid = services.getLoggedIn();
             listPropertyValues = getListPropertyValues();                       
             isConnected = true;
             
             //TODO review this place possible way when user change location and user has the same token
             String currentOid = PreferencePage.getPreferences().getString(PreferenceConstants.P_MEMBER_TOKEN);
             if (!currentOid.equals(memberOid.getToken() + ":" + path)) {
                 PreferencePage.getPreferences().setValue(PreferenceConstants.P_MEMBER_TOKEN, memberOid.getToken() + ":" + path);
             } 
             
             this.path = path; //
             return true;
         } catch (MetaException e) {
             throw warning("Cannot connect to V1 server.", e);
         } catch (Exception e) {
             throw warning("Cannot connect to V1 server.", e);
         }
     }
 
     private void initTypes() {
         projectType = getAssetType(Workitem.ProjectPrefix);
         taskType = getAssetType(Workitem.TaskPrefix);
         testType = getAssetType(Workitem.TestPrefix);
         defectType = getAssetType(Workitem.DefectPrefix);
         storyType = getAssetType(Workitem.StoryPrefix);
         workitemType = metaModel.getAssetType("Workitem");
         primaryWorkitemType = metaModel.getAssetType("PrimaryWorkitem");
     }
 
     /**
      * Reconnect with settings, used in last Connect() call.
      * 
      * @throws Exception
      */
     public void reconnect() throws DataLayerException {
         connect(path, userName, password, integrated);
     }
 
     public List<Workitem> getProjectTree() throws DataLayerException {
         try {
             Query scopeQuery = new Query(projectType, projectType.getAttributeDefinition("Parent"));
             FilterTerm stateTerm = new FilterTerm(projectType.getAttributeDefinition("AssetState"));
             stateTerm.NotEqual(AssetState.Closed);
             scopeQuery.setFilter(stateTerm);
             // clear all definitions used in previous queries
             alreadyUsedDefinition.clear();
             addSelection(scopeQuery, Workitem.ProjectPrefix);
             QueryResult result = services.retrieve(scopeQuery);
             List<Workitem> roots = new ArrayList<Workitem>(result.getAssets().length);
             for (Asset oneAsset : result.getAssets()) {
                 roots.add(new Workitem(oneAsset, null));
             }
             return roots;
         }/*
           * catch (WebException ex) { isConnected = false; throw
           * Warning("Can't get projects list.", ex); }
           */catch (Exception ex) {
             throw warning("Can't get projects list.", ex);
         }
     }
 
     private IAssetType getAssetType(String token) {
         IAssetType type = metaModel.getAssetType(token);
         types.put(token, type);
         return type;
     }
 
     public boolean isCurrentUserOwnerAsset(Asset childAsset) {
         return isCurrentUserOwnerAsset(childAsset, workitemType.getAttributeDefinition(Workitem.OwnersProperty));
     }
 
     public Workitem[] getWorkitemTree() throws Exception {
         checkConnection();
         if (currentProjectId == null) {
             // throw new DataLayerException("Current project is not selected");
             // // TODO implement
             // throw new Exception("Current project is not selected");
             currentProjectId = "Scope:0";
 
         }
 
         if (assetList == null) {
             try {
                 IAttributeDefinition parentDef = workitemType.getAttributeDefinition("Parent");
 
                 Query query = new Query(workitemType, parentDef);
                 // Query query = new Query(taskType, parentDef);
                 // clear all definitions which was used in previous queries
                 alreadyUsedDefinition.clear();
                 addSelection(query, Workitem.TaskPrefix);
                 addSelection(query, Workitem.StoryPrefix);
                 addSelection(query, Workitem.DefectPrefix);
                 addSelection(query, Workitem.TestPrefix);
 
                 query.setFilter(getScopeFilter(workitemType));
 
                 query.getOrderBy().majorSort(primaryWorkitemType.getDefaultOrderBy(), OrderBy.Order.Ascending);
                 query.getOrderBy().minorSort(workitemType.getDefaultOrderBy(), OrderBy.Order.Ascending);
 
                 assetList = services.retrieve(query);
             } catch (MetaException ex) {
                 throw warning("Unable to get workitems.", ex);
             }
             /*
              * catch (WebException ex) { isConnected = false; throw
              * Warning("Unable to get workitems.", ex); }
              */
             catch (Exception ex) {
                 throw warning("Unable to get workitems.", ex);
             }
         }
 
         IAttributeDefinition definition = workitemType.getAttributeDefinition(Workitem.OwnersProperty);
         List<Workitem> res = new ArrayList<Workitem>(assetList.getAssets().length);
 
         for (Asset asset : assetList.getAssets()) {
         	if(assetsToIgnore.contains(asset)) {
         		continue;
         	}
         	
             if (showAllTasks || isCurrentUserOwnerAsset(asset, definition)) {
                 res.add(new Workitem(asset, null));
             }
         }
         return res.toArray(new Workitem[res.size()]);
     }
     
     private boolean isCurrentUserOwnerAsset(Asset assets, IAttributeDefinition definition){
         Attribute attribute = assets.getAttribute(definition);
 
         Object[] owners = attribute.getValues();
         for (Object oid : owners) {
             if (memberOid.equals(oid)) {
                 return true;
             }
         }
         if (assets.hasChanged()) {
             for (Asset child : assets.getChildren()) {
                 if (isCurrentUserOwnerAsset(child, definition)) {
                     return true;
                 }
             }
         }
 
         return false;
 }
     
     public boolean checkConnection(String url, String user, String pass, boolean auth) {
         boolean result = true;
         
         V1APIConnector metaConnector = new V1APIConnector(url.toString() + MetaUrlSuffix);
         MetaModel model = new MetaModel(metaConnector);
 
         V1APIConnector dataConnector = null;
         if (auth) {
             dataConnector = new V1APIConnector(url.toString() + DataUrlSuffix);
         } else {
             dataConnector = new V1APIConnector(url.toString() + DataUrlSuffix, user, pass);
         }
 
         Services v1Service = new Services(model, dataConnector);
 
         try {
             v1Service.getLoggedIn();
         } catch (V1Exception e) {
             result = false;
         } catch (MetaException e) {
             result = false;
         }
         
         return result;
     }
     
 
     private void checkConnection() throws DataLayerException {
         if (!isConnected) {
             throw warning("Connection is not set.");
         }
     }
 
     private IFilterTerm getScopeFilter(IAssetType assetType) {
         List<FilterTerm> terms = new ArrayList<FilterTerm>(5);
         FilterTerm term = new FilterTerm(assetType.getAttributeDefinition("Scope.AssetState"));
         term.NotEqual(AssetState.Closed);
         terms.add(term);
         term = new FilterTerm(assetType.getAttributeDefinition("Scope.ParentMeAndUp"));
         term.Equal(currentProjectId);
         terms.add(term);
         term = new FilterTerm(assetType.getAttributeDefinition("Timebox.State.Code"));
         term.Equal("ACTV");
         terms.add(term);
         term = new FilterTerm(assetType.getAttributeDefinition("AssetState"));
         term.NotEqual(AssetState.Closed);
         terms.add(term);
         return new AndFilterTerm(terms.toArray(new FilterTerm[terms.size()]));
     }
 
     // need to make AlreadyUsedDefinition.Clear(); before first call of this
     // method
     private void addSelection(Query query, String typePrefix) throws DataLayerException {
         for (AttributeInfo attrInfo : attributesToQuery) {
             if (attrInfo.prefix == typePrefix) {
                 try {
                     IAttributeDefinition def = types.get(attrInfo.prefix).getAttributeDefinition(attrInfo.attr);
                     if (!alreadyUsedDefinition.contains(def)) {
                         query.getSelection().add(def);
                         alreadyUsedDefinition.add(def);
                     }
                 } catch (MetaException e) {
                     warning("Wrong attribute: " + attrInfo, e);
                 }
             }
         }
     }
 
     public void addProperty(String attr, String prefix, boolean isList) {
         attributesToQuery.addLast(new AttributeInfo(attr, prefix, isList));
     }
 
     private Map<String, PropertyValues> getListPropertyValues() throws Exception { // ConnectionException,
                                                                                    // APIException,
                                                                                    // OidException,
                                                                                    // MetaException
                                                                                    // {
         Map<String, PropertyValues> res = new HashMap<String, PropertyValues>(attributesToQuery.size());
         for (AttributeInfo attrInfo : attributesToQuery) {
             if (!attrInfo.isList) {
                 continue;
             }
 
             String propertyAlias = attrInfo.prefix + attrInfo.attr;
             if (!res.containsKey(propertyAlias)) {
                 String propertyName = resolvePropertyKey(propertyAlias);
 
                 PropertyValues values;
                 if (res.containsKey(propertyName)) {
                     values = res.get(propertyName);
                 } else {
                     values = queryPropertyValues(propertyName);
                     res.put(propertyName, values);
                 }
 
                 if (!res.containsKey(propertyAlias)) {
                     res.put(propertyAlias, values);
                 }
             }
         }
         return res;
     }
 
     private static String resolvePropertyKey(String propertyAlias) {
         if (propertyAlias.equals("DefectStatus")) {
             return "StoryStatus";
         } else if (propertyAlias.equals("DefectSource")) {
             return "StorySource";
         } else if (propertyAlias.equals("ScopeBuildProjects")) {
             return "BuildProject";
         } else if (propertyAlias.equals("TaskOwners") || propertyAlias.equals("StoryOwners")
                 || propertyAlias.equals("DefectOwners") || propertyAlias.equals("TestOwners")) {
             return "Member";
         }
 
         return propertyAlias;
     }
 
     private PropertyValues queryPropertyValues(String propertyName) throws ConnectionException, APIException,
             OidException, MetaException {
         PropertyValues res = new PropertyValues();
         IAssetType assetType = metaModel.getAssetType(propertyName);
         IAttributeDefinition nameDef = assetType.getAttributeDefinition(Workitem.NameProperty);
         IAttributeDefinition inactiveDef;
 
         Query query = new Query(assetType);
         query.getSelection().add(nameDef);
 
         inactiveDef = assetType.getAttributeDefinition("Inactive");
         if (inactiveDef != null) {
             FilterTerm filter = new FilterTerm(inactiveDef);
             filter.Equal("False");
             query.setFilter(filter);
         }
 
         query.getOrderBy().majorSort(assetType.getDefaultOrderBy(), OrderBy.Order.Ascending);
 
         res.addInternal(new ValueId());
         for (Asset asset : services.retrieve(query).getAssets()) {
             String name = (String) asset.getAttribute(nameDef).getValue();
             res.addInternal(new ValueId(asset.getOid(), name));
         }
         return res;
     }
 
     public PropertyValues getListPropertyValues(String type, String propertyName) {
         String propertyKey = resolvePropertyKey(type + propertyName);
         return listPropertyValues.get(propertyKey);
     }
 
     static DataLayerException warning(String string, Exception ex) {
         // TODO Auto-generated method stub
         return new DataLayerException(string, ex);
     }
 
     static DataLayerException warning(String string) {
         // TODO Auto-generated method stub
         return new DataLayerException(string);
     }
     
     public boolean isTrackEffortEnabled() {
         return trackEffort;
     }
 
     Double getEffort(Asset asset) {
         return efforts.get(asset);
     }
 
     void setEffort(Asset asset, Double value) {
         if (value == null || value == 0){
             efforts.remove(asset);
         } else {
             efforts.put(asset, value);
         }
     }
 
     public boolean isEffortTrackingRelated(String propertyName) {
         return effortTrackingAttributesList.contains(propertyName);
     }
 
     void commitAsset(Asset asset) throws V1Exception {
         services.save(asset);
         commitEffort(asset);
     }
 
     private void commitEffort(Asset asset) throws V1Exception {
         if (efforts.containsKey(asset)) {
             Asset effort = services.createNew(effortType, asset.getOid());
             effort.setAttributeValue(effortType.getAttributeDefinition("Value"), efforts.get(asset));
             effort.setAttributeValue(effortType.getAttributeDefinition("Date"), new Date());
             services.save(effort);
             efforts.remove(asset);
         }
     }
 
     void revertAsset(Asset asset) {
         asset.rejectChanges();
         efforts.remove(asset);
     }
 
     public void commitChanges() throws DataLayerException {
         checkConnection();
         try {
             commitAssetsRecursively(Arrays.asList(assetList.getAssets()));
         } catch (V1Exception e) {
             throw warning("Cannot commit changes.", e);
         }
     }
 
     private void commitAssetsRecursively(List<Asset> assets) throws V1Exception {
         for (Asset asset : assets){
             commitAsset(asset);
             commitAssetsRecursively(asset.getChildren());
         }
     }
 
     void executeOperation(Asset asset, IOperation operation) throws V1Exception {
         services.executeOperation(operation, asset.getOid());
         if(operation.getName() == CloseOperation || operation.getName() == QuickCloseOperation) {
         	assetsToIgnore.add(asset);
         }
     }
 
     void refreshAsset(Workitem workitem) throws DataLayerException {
     	try {
             IAttributeDefinition stateDef = workitem.asset.getAssetType().getAttributeDefinition("AssetState");
             Query query = new Query(workitem.asset.getOid().getMomentless(), false);
             addSelection(query, workitem.getTypePrefix());
             query.getSelection().add(stateDef);
             QueryResult newAssets = services.retrieve(query);
 
             List<Asset> parent = new ArrayList<Asset>();
             if (workitem.parent == null) {
                 // parent = AssetList.Assets;
             } else {
                 parent = workitem.parent.asset.getChildren();
             }
 
             //Removing old Asset from allAssets
             //allAssets.remove(workitem.asset);
 
             if (newAssets.getTotalAvaliable() != 1 ) {
                 // Just remove old Asset from AssetList
                 parent.remove(workitem.asset);
                 return;
             }
 
             Asset newAsset = newAssets.getAssets()[0];
            AssetState newAssetState = AssetState.valueOf((Integer)newAsset.getAttribute(stateDef).getValue());
             if (newAssetState == AssetState.Closed) {
                 // Just remove old Asset from AssetList
                 parent.remove(workitem.asset);
                 return;
             }
 
             //Adding new Asset to allAssets
             //allAssets.add(newAsset);
 
             //Adding new Asset to parent
             //parent[parent.indexOf(workitem.asset)] = newAsset;
             //newAsset.getChildren().addRange(workitem.asset.getChildren());
         }
         catch (MetaException ex) {
             throw warning("Unable to get workitems.", ex);
         } catch (Exception ex) {
             throw warning("Unable to get workitems.", ex);
         }
     }
 
     public void setCurrentProjectId(String value) {
         currentProjectId = value;
         assetList = null;
     }
 
     public String getCurrentProjectId() {
         return currentProjectId;
     }
 
     public void setCurrentProject(Workitem value) {
         currentProjectId = value.getId();
         assetList = null;
     }
 
     public Workitem getCurrentProject() throws Exception {
         if (currentProjectId == null || currentProjectId.equals("")) {
             currentProjectId = "Scope:0";
         }
         return getProjectById(currentProjectId);
     }
 
     private Workitem getProjectById(String id) throws Exception {
         if (!isConnected || id == null || id.equals("")) {
             return null;
         }
         
         Query query = new Query(Oid.fromToken(id, metaModel));
         // clear all definitions used in previous queries
         alreadyUsedDefinition.clear();
         addSelection(query, Workitem.ProjectPrefix);
         QueryResult result;
         try {
             result = services.retrieve(query);
         } catch (MetaException ex) {
             isConnected = false;
             throw warning("Unable to get projects", ex);
         } catch (Exception ex) {
             throw warning("Unable to get projects", ex);
         }
 
         if (result.getTotalAvaliable() == 1) {
             return new Workitem(result.getAssets()[0], null);
         }
         return null;
     }
     
     public String localizerResolve(String key) throws DataLayerException {
         try {
             return localizer.resolve(key);
         } catch (Exception ex) {
             throw warning("Failed to resolve key.", ex);
         }
     }
 
     public boolean tryLocalizerResolve(String key, String result) {
         result = null;
 
         if (localizer != null) {
             result = localizer.resolve(key);
             return true;
         }
 
         return false;
     }
 
     private static boolean isTestEnable = false;
     public static ApiDataLayer getInitializedInstance(IServices services, IMetaModel metaModel, ILocalizer localizer) throws Exception {
         if (!isTestEnable && instance != null) {
             instance = null;
             isTestEnable = true;
         }
         
         if (null == instance) {
             instance = new ApiDataLayer(services, metaModel, localizer);
         }
         
         return instance;
     }
 }
