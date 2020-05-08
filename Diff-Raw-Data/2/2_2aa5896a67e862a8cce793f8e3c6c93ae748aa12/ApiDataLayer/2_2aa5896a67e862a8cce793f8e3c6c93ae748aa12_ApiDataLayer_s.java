 package com.versionone.common.sdk;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 
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
 import com.versionone.apiclient.IV1Configuration.TrackingLevel;
 
 public class ApiDataLayer {
 
     private static final String MetaUrlSuffix = "meta.v1/";
     private static final String LocalizerUrlSuffix = "loc.v1/";
     private static final String DataUrlSuffix = "rest-1.v1/";
     private static final String ConfigUrlSuffix = "config.v1/";
 
     private final Map<String, IAssetType> types = new HashMap<String, IAssetType>(5);
     private final Map<Asset, Double> efforts = new HashMap<Asset, Double>();
     private final Set<Asset> assetsToIgnore = new HashSet<Asset>();
     private final Set<IAttributeDefinition> alreadyUsedDefinition = new HashSet<IAttributeDefinition>();
 
     private IAssetType projectType;
     private IAssetType workitemType;
     private IAssetType primaryWorkitemType;
     private IAssetType effortType;
 
     protected static ApiDataLayer instance;
     private boolean isConnected;
     private boolean testConnection;
 
     public Oid memberOid;
     private String path;
     private String userName;
     private String password;
     private boolean integrated;
 
     private QueryResult assetList;
     private static Set<AttributeInfo> attributesToQuery = new HashSet<AttributeInfo>();
     private Map<String, PropertyValues> listPropertyValues;
 
     private boolean trackEffort;
     public final EffortTrackingLevel trackingLevel = new EffortTrackingLevel(Workitem.TASK_PREFIX, Workitem.TEST_PREFIX);
 
     private IMetaModel metaModel;
     private IServices services;
     private ILocalizer localizer;
     
    private HashMap<String,LinkedList<String>> requiredFields;
 
     private String currentProjectId;
     private boolean showAllTasks = true;
 
     protected ApiDataLayer() {
     }
 
     /**
      * Special method ONLY for testing.
      */
     public void connectFotTesting(Object services, Object metaModel, Object localizer, Object storyTrackingLevel, Object defectTrackingLevel)
             throws Exception {
         this.metaModel = (IMetaModel) metaModel;
         this.services = (IServices) services;
         this.localizer = (ILocalizer) localizer;
 
         if (storyTrackingLevel != null && defectTrackingLevel != null) {
             trackEffort = true;
             effortType = this.metaModel.getAssetType("Actual");
             trackingLevel.clear();
             trackingLevel.addPrimaryTypeLevel(Workitem.STORY_PREFIX, (TrackingLevel) storyTrackingLevel);
             trackingLevel.addPrimaryTypeLevel(Workitem.DEFECT_PREFIX, (TrackingLevel) defectTrackingLevel);
         } else {
             trackEffort = false;
         }
 
         initTypes();
         testConnection = isConnected = true;
         memberOid = this.services.getLoggedIn();
         listPropertyValues = getListPropertyValues();
     }
 
     public static ApiDataLayer getInstance() {
         if (instance == null) {
             instance = new ApiDataLayer();
         }
         return instance;
     }
 
     public void connect(String path, String userName, String password, boolean integrated) throws DataLayerException {
         if (testConnection) {
             return;
         }
         isConnected = false;
         boolean isUserChanged = true;
         if ((this.userName != null || integrated) && this.path != null) {
             isUserChanged = (this.userName != null && !this.userName.equals(userName)) || integrated != this.integrated
                     || !this.path.equals(path);
         }
 
         this.path = path;
         this.userName = userName;
         this.password = password;
         this.integrated = integrated;
         assetList = null;
         boolean isUpdateData = isUserChanged || metaModel == null || localizer == null || services == null;
 
         try {
             if (isUpdateData) {
                 cleanConnectionData();
 
                 V1APIConnector metaConnector = new V1APIConnector(path + MetaUrlSuffix, userName, password);
                 metaModel = new MetaModel(metaConnector);
 
                 V1APIConnector localizerConnector = new V1APIConnector(path + LocalizerUrlSuffix, userName, password);
                 localizer = new Localizer(localizerConnector);
 
                 V1APIConnector dataConnector = new V1APIConnector(path + DataUrlSuffix, userName, password);
                 services = new Services(metaModel, dataConnector);
                 
             }
             if (types.isEmpty()) {
                 initTypes();
             }
             processConfig(path);
 
             memberOid = services.getLoggedIn();
             listPropertyValues = getListPropertyValues();
             isConnected = true;
             updateCurrentProjectId();
             
             requiredFields.put(Workitem.TASK_PREFIX, getRequiredFields(Workitem.TASK_PREFIX));
             requiredFields.put(Workitem.DEFECT_PREFIX, getRequiredFields(Workitem.DEFECT_PREFIX));
             requiredFields.put(Workitem.STORY_PREFIX, getRequiredFields(Workitem.STORY_PREFIX));
             requiredFields.put(Workitem.TEST_PREFIX, getRequiredFields(Workitem.TEST_PREFIX));
             
             return;
         } catch (MetaException e) {
             throw warning("Cannot connect to V1 server.", e);
         } catch (Exception e) {
             throw warning("Cannot connect to V1 server.", e);
         }
     }
 
     private void processConfig(String path) throws ConnectionException, APIException {
         V1Configuration v1Config = new V1Configuration(new V1APIConnector(path + ConfigUrlSuffix));
 
         trackEffort = v1Config.isEffortTracking();
         if (trackEffort) {
             effortType = metaModel.getAssetType("Actual");
         }
 
         trackingLevel.clear();
         trackingLevel.addPrimaryTypeLevel(Workitem.STORY_PREFIX, v1Config.getStoryTrackingLevel());
         trackingLevel.addPrimaryTypeLevel(Workitem.DEFECT_PREFIX, v1Config.getDefectTrackingLevel());
     }
 
     private void cleanConnectionData() {
         assetsToIgnore.clear();
         efforts.clear();
         types.clear();
         projectType = null;
         workitemType = null;
         primaryWorkitemType = null;
     }
 
     private void initTypes() {
         projectType = getAssetType(Workitem.PROJECT_PREFIX);
         getAssetType(Workitem.TASK_PREFIX);
         getAssetType(Workitem.TEST_PREFIX);
         getAssetType(Workitem.DEFECT_PREFIX);
         getAssetType(Workitem.STORY_PREFIX);
         workitemType = metaModel.getAssetType("Workitem");
         primaryWorkitemType = metaModel.getAssetType("PrimaryWorkitem");
     }
 
     /**
      * Reconnect with settings, used in last Connect() call.
      * 
      * @throws DataLayerException
      */
     public void reconnect() throws DataLayerException {
         connect(path, userName, password, integrated);
     }
 
     public List<Workitem> getProjectTree() throws DataLayerException {
         checkConnection();
         try {
             Query scopeQuery = new Query(projectType, projectType.getAttributeDefinition("Parent"));
             FilterTerm stateTerm = new FilterTerm(projectType.getAttributeDefinition("AssetState"));
             stateTerm.NotEqual(AssetState.Closed);
             scopeQuery.setFilter(stateTerm);
             // clear all definitions used in previous queries
             alreadyUsedDefinition.clear();
             addSelection(scopeQuery, Workitem.PROJECT_PREFIX);
             QueryResult result = services.retrieve(scopeQuery);
             List<Workitem> roots = new ArrayList<Workitem>(result.getAssets().length);
             for (Asset oneAsset : result.getAssets()) {
                 roots.add(new Workitem(oneAsset, null));
             }
             return roots;
         } catch (Exception ex) {
             throw warning("Can't get projects list.", ex);
         }
     }
 
     private IAssetType getAssetType(String token) {
         IAssetType type = metaModel.getAssetType(token);
         types.put(token, type);
         return type;
     }
 
     public List<Workitem> getWorkitemTree() throws Exception {
         checkConnection();
         if (currentProjectId == null) {
             currentProjectId = getDefaultProjectId();
         }
 
         if (assetList == null) {
             try {
                 IAttributeDefinition parentDef = workitemType.getAttributeDefinition("Parent");
 
                 Query query = new Query(workitemType, parentDef);
 
                 // clear all definitions used in previous queries
                 alreadyUsedDefinition.clear();
                 addSelection(query, Workitem.TASK_PREFIX);
                 addSelection(query, Workitem.STORY_PREFIX);
                 addSelection(query, Workitem.DEFECT_PREFIX);
                 addSelection(query, Workitem.TEST_PREFIX);
 
                 query.setFilter(getScopeFilter(workitemType));
 
                 query.getOrderBy().majorSort(primaryWorkitemType.getDefaultOrderBy(), OrderBy.Order.Ascending);
                 query.getOrderBy().minorSort(workitemType.getDefaultOrderBy(), OrderBy.Order.Ascending);
 
                 assetList = services.retrieve(query);
             } catch (MetaException ex) {
                 throw warning("Unable to get workitems.", ex);
             } catch (Exception ex) {
                 throw warning("Unable to get workitems.", ex);
             }
         }
 
         List<Workitem> res = new ArrayList<Workitem>(assetList.getAssets().length);
 
         for (Asset asset : assetList.getAssets()) {
             if (isAssetSuspended(asset)) {
                 continue;
             }
 
             if (isShowed(asset)) {
                 res.add(new Workitem(asset, null));
             }
         }
         return res;
     }
 
     /**
      * Sets visibility for workitems
      * @param showAllTasks true  - all workitems can be shown
      *                     false - only changed, new and workitem with current owner can be shown 
      */
     public void setShowAllTasks(boolean showAllTasks) {
         this.showAllTasks = showAllTasks;         
     }
 
     /**
      * Determines whether this Asset can be showed or no. 
      * 
      * @param asset to determine visibility status.
      * @return true if Asset can be showed at the moment; otherwise - false.
      */
     public boolean isShowed(Asset asset) {
         if (showAllTasks || asset.hasChanged()) {
             return true;
         }
         
         
         final Attribute attribute = asset.getAttribute(workitemType.getAttributeDefinition(Workitem.OWNERS_PROPERTY));
         final Object[] owners = attribute.getValues();
         for (Object oid : owners) {
             if (memberOid.equals(oid)) {
                 return true;
             }
         }
 
         for (Asset child : asset.getChildren()) {
             if (isShowed(child)) {
                 return true;
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
             reconnect();
             if (!isConnected) {
                 throw warning("Connection is not set.");
             }
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
             if (attrInfo.prefix.equals(typePrefix)) {
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
 
     private void addSelection(Query query, String typePrefix, boolean clearDefinitions) throws DataLayerException {
         if (clearDefinitions) {
             alreadyUsedDefinition.clear();
         }
         addSelection(query, typePrefix);
     }
 
     public void addProperty(String attr, String prefix, boolean isList) {
         attributesToQuery.add(new AttributeInfo(attr, prefix, isList));
     }
 
     private Map<String, PropertyValues> getListPropertyValues() throws Exception {
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
         IAttributeDefinition nameDef = assetType.getAttributeDefinition(Workitem.NAME_PROPERTY);
         IAttributeDefinition inactiveDef = null;
 
         Query query = new Query(assetType);
         query.getSelection().add(nameDef);
 
         try {// Some properties may not have INACTIVE attribute
             inactiveDef = assetType.getAttributeDefinition("Inactive");
         } catch (Exception ex) {
             // do nothing
         }
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
         System.out.println(string);
         ex.printStackTrace();
         return new DataLayerException(string, ex);
     }
 
     static DataLayerException warning(String string) {
         System.out.println(string);
         return new DataLayerException(string);
     }
 
     public boolean isTrackEffortEnabled() {
         return trackEffort;
     }
 
     Double getEffort(Asset asset) {
         return efforts.get(asset);
     }
 
     void setEffort(Asset asset, Double value) {
         if (value == null || value == 0) {
             efforts.remove(asset);
         } else {
             efforts.put(asset, value);
         }
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
         for (Asset asset : assets) {
             // do not commit assets that were closed and their children
             if (assetsToIgnore.contains(asset)) {
                 continue;
             }
 
             commitAsset(asset);
             commitAssetsRecursively(asset.getChildren());
         }
     }
 
     private boolean isAllRequiredFieldsFilled(Asset asset) throws DataLayerException {
         final String type = asset.getAssetType().getToken();
         final IAssetType attributeDefinitionAssetType = metaModel.getAssetType("AttributeDefinition");
         try {
             for (String field : requiredFields.get(type)) {
                 final IAttributeDefinition def = attributeDefinitionAssetType.getAttributeDefinition(field);
 
                 if (asset.getAttribute(def).getValue() == null) {
                     return false;
                 }
             }
         } catch (APIException e) {
             throw warning("Can get attribute definition", e);
         } catch (MetaException e) {
             throw warning("Can get attribute definition", e);
         }
         return true;
     }
 
     void executeOperation(Asset asset, IOperation operation) throws V1Exception {
         services.executeOperation(operation, asset.getOid());
     }
 
     boolean isAssetClosed(Asset asset) {
         try {
             IAttributeDefinition stateDef = asset.getAssetType().getAttributeDefinition("AssetState");
             AssetState state = AssetState.valueOf((Integer) asset.getAttribute(stateDef).getValue());
             return state == AssetState.Closed || assetsToIgnore.contains(asset.getOid().getMomentless());
         } catch (MetaException e) {
         } catch (APIException e) {
         }
         return false;
     }
 
     boolean isAssetSuspended(Asset asset) {
         return assetsToIgnore.contains(asset);
     }
 
     void addIgnoreRecursively(Workitem item) {
         assetsToIgnore.add(item.asset);
         for (Workitem child : item.children) {
             addIgnoreRecursively(child);
         }
     }
 
     // TODO refactor
     void refreshAsset(Workitem workitem) throws DataLayerException {
         try {
             IAttributeDefinition stateDef = workitem.asset.getAssetType().getAttributeDefinition("AssetState");
             Query query = new Query(workitem.asset.getOid().getMomentless(), false);
             addSelection(query, workitem.getTypePrefix(), true);
             query.getSelection().add(stateDef);
             QueryResult newAssets = services.retrieve(query);
 
             Asset[] parentArray = assetList.getAssets();
             List<Asset> parentList = null;
             if (workitem.parent != null) {
                 parentList = workitem.parent.asset.getChildren();
             }
 
             Assert.isTrue(newAssets.getTotalAvaliable() == 1, "Query should return exactly one asset.");
 
             Asset newAsset = newAssets.getAssets()[0];
             if (isAssetClosed(newAsset)) {
                 assetsToIgnore.add(workitem.asset);
                 return;
             }
 
             // Adding new Asset to parent
             for (int i = 0; i < parentArray.length; i++) {
                 if (parentArray[i].equals(workitem.asset)) {
                     parentArray[i] = newAsset;
                     break;
                 }
             }
 
             if (parentList != null) {
                 int index = parentList.indexOf(workitem.asset);
                 if (index != -1) {
                     parentList.set(index, newAsset);
                 }
             }
             newAsset.getChildren().addAll(workitem.asset.getChildren());
         } catch (MetaException ex) {
             throw warning("Unable to get workitems.", ex);
         } catch (Exception ex) {
             throw warning("Unable to get workitems.", ex);
         }
     }
 
     public void setCurrentProjectId(String value) {
         if (isProjectExist(value)) {
             currentProjectId = value;
         } else {
             currentProjectId = getDefaultProjectId();
         }
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
             currentProjectId = getDefaultProjectId();
         }
         return getProjectById(currentProjectId);
     }
 
     public String getCurrentMemberToken() {
         return memberOid != null ? memberOid.getToken() : null;
     }
 
     private String getDefaultProjectId() {
         String id = "";
 
         Query query = new Query(projectType);
 
         QueryResult result = null;
         try {
             result = services.retrieve(query);
         } catch (Exception ex) {
         }
 
         if (result != null && result.getTotalAvaliable() > 0) {
             id = result.getAssets()[0].getOid().getMomentless().getToken();
         }
 
         return id;
     }
 
     /***
      * Update current project Id to the root project Id from current server
      */
     public String updateCurrentProjectId() {
         currentProjectId = getDefaultProjectId();
         return currentProjectId;
     }
 
     private boolean isProjectExist(String id) {
         boolean isExist = true;
 
         try {
             Query query = new Query(Oid.fromToken(id, metaModel));
             alreadyUsedDefinition.clear();
             addSelection(query, Workitem.PROJECT_PREFIX);
             QueryResult result = null;
             result = services.retrieve(query);
             if (result.getTotalAvaliable() == 0) {
                 isExist = false;
             }
         } catch (Exception ex) {
             isExist = false;
         }
 
         return isExist;
     }
 
     private Workitem getProjectById(String id) throws Exception {
         if (!isConnected || id == null || id.equals("")) {
             return null;
         }
 
         Query query = new Query(Oid.fromToken(id, metaModel));
         // clear all definitions used in previous queries
         alreadyUsedDefinition.clear();
         addSelection(query, Workitem.PROJECT_PREFIX);
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
 
     public String localizerResolve(String key) {
         return localizer.resolve(key);
     }
 
     public Workitem createWorkitem(String prefix, Workitem parent) throws DataLayerException {
         try {
             final Asset asset = new Asset(types.get(prefix));
             for (AttributeInfo attrInfo : attributesToQuery) {
                 if (attrInfo.prefix == prefix) {
                     setAssetAttribute(asset, attrInfo.attr, null);
                 }
             }
 
             if (prefix.equals(Workitem.TEST_PREFIX)) { 
                 // TODO if item.isSecondaryWorkitem()
                 setAssetAttribute(asset, "Parent", parent.asset.getOid());
                 parent.asset.getChildren().add(asset);
             } else if (prefix.equals(Workitem.STORY_PREFIX)) {
                 // TODO if item.isPrimaryWorkitem()
                 setAssetAttribute(asset, "Scope", currentProjectId);
                 //TODO add to allAssets
             }
             return new VirtualWorkitem(asset, parent);
         } catch (MetaException e) {
             throw new DataLayerException("Cannot create workitem: " + prefix, e);
         } catch (APIException e) {
             throw new DataLayerException("Cannot create workitem: " + prefix, e);
         }
     }
 
     private static void setAssetAttribute(final Asset asset, final String attrName, final Object value)
             throws MetaException, APIException {
         final IAssetType type = asset.getAssetType();
         IAttributeDefinition def = type.getAttributeDefinition(attrName);
         if (value != null) {
             asset.setAttributeValue(def, value);
         } else {
             asset.ensureAttribute(def);
         }
     }
     
     private LinkedList<String> getRequiredFields(String assetType) throws DataLayerException {
         final LinkedList<String> fileds = new LinkedList<String>();
         final IAssetType attributeDefinitionAssetType = metaModel.getAssetType("AttributeDefinition");
         
 
         final IAttributeDefinition nameAttributeDef = attributeDefinitionAssetType.getAttributeDefinition("Name");
         final IAttributeDefinition isRequiredAttributeDef = attributeDefinitionAssetType.getAttributeDefinition("IsRequired");
         final IAttributeDefinition isReadOnlyAttributeDef = attributeDefinitionAssetType.getAttributeDefinition("IsReadOnly");
         
         
         final IAttributeDefinition assetNameAttributeDef = attributeDefinitionAssetType
                 .getAttributeDefinition("Asset.AssetTypesMeAndDown.Name");
 
         Query query = new Query(attributeDefinitionAssetType);
         query.getSelection().add(nameAttributeDef);
         query.getSelection().add(isRequiredAttributeDef);
         query.getSelection().add(isReadOnlyAttributeDef);
 
         FilterTerm assetTypeTerm = new FilterTerm(assetNameAttributeDef);
         assetTypeTerm.Equal(assetType);
         FilterTerm assetIsReadOnlyTerm = new FilterTerm(isReadOnlyAttributeDef);
         assetIsReadOnlyTerm.Equal(false);
 
         query.setFilter(new AndFilterTerm(new IFilterTerm[] { assetTypeTerm, assetIsReadOnlyTerm}));
 
         QueryResult result = null;
         try {
             result = services.retrieve(query);
         } catch (ConnectionException e) {
             throw warning("Cannot get meta data for " + assetType, e);
         } catch (APIException e) {
             throw warning("Cannot get meta data for " + assetType, e);
         } catch (OidException e) {
             throw warning("Cannot get meta data for " + assetType, e);
         } catch (Exception e) {
             throw warning("Cannot get meta data for " + assetType, e);
         }
         System.out.println("\n\n\nCustom Text Attributes available to " + assetType + " Are:");
         for (Asset asset : result.getAssets()) {
             try {
                 if (Boolean.parseBoolean(asset.getAttribute(isRequiredAttributeDef).getValue().toString())) {
                     fileds.add(asset.getAttribute(nameAttributeDef).getValue().toString());
                     System.out.println(asset.getAttribute(nameAttributeDef).getValue().toString() + " - " + asset.getAttribute(isReadOnlyAttributeDef).getValue().toString());
                 }                
             } catch (APIException e) {
                 throw warning("Cannot get meta data for " + assetType, e);
             } catch (MetaException e) {
                 throw warning("Cannot get meta data for " + assetType, e);
             } catch (Exception e) {
                 throw warning("Cannot get meta data for " + assetType, e);
             }
         }
         
         return fileds;
     }
     
 }
