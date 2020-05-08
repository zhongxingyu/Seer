 /*******************************************************************************
  * Copyright 2011 Adrian Cristian Ionescu
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package ro.zg.netcell.vaadin.action;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import ro.zg.netcell.client.ThinClientNetcellDao;
 import ro.zg.netcell.control.CommandResponse;
 import ro.zg.netcell.vaadin.action.application.LoadMainWindowHandler;
 import ro.zg.netcell.vaadin.action.application.OpenAndRefreshEntityHandler;
 import ro.zg.netcell.vaadin.action.application.OpenEntityInTabHandler;
 import ro.zg.netcell.vaadin.action.application.OpenEntityInWindowHandler;
 import ro.zg.netcell.vaadin.action.application.OpenEntityWithActionsHandler;
 import ro.zg.netcell.vaadin.action.application.OpenSelectedEntityHandler;
 import ro.zg.netcell.vaadin.action.application.OpenSelectedEntityWithHeaderActions;
 import ro.zg.netcell.vaadin.action.application.RefreshCausalHierarchyHandler;
 import ro.zg.netcell.vaadin.action.application.RefreshSelectedEntityHandler;
 import ro.zg.netcell.vaadin.action.application.ShowCausalHierarchyHandler;
 import ro.zg.netcell.vo.definitions.EntityDefinitionSummary;
 import ro.zg.open_groups.OpenGroupsApplication;
 import ro.zg.opengroups.vo.Entity;
 import ro.zg.opengroups.vo.UserAction;
 import ro.zg.opengroups.vo.UserActionList;
 import ro.zg.util.data.GenericNameValueContext;
 import ro.zg.util.data.GenericNameValueList;
 import ro.zg.util.logging.Logger;
 import ro.zg.util.logging.MasterLogManager;
 
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.ProgressIndicator;
 
 public class ActionsManager implements Serializable, ActionErrorHandler {
     /**
      * 
      */
     private static final long serialVersionUID = 4152642862742183342L;
 
     /* constants */
     public static String GET_POSSIBLE_ACTIONS = "ro.problems.flows.get-actions-by-entity-and-user";
     private static String GET_ALL_AVAILABLE_ACTIONS = "ro.problems.flows.get-all-available-actions";
     public static String GET_STATUSES = "ro.problems.flows.get-statuses";
     public static String GET_TAGS = "ro.problems.flows.get-tags";
 
     public static final String LOAD_MAIN_WINDOW = "loadMainWindow";
     public static final String SHOW_CAUSAL_HIERARCHY="SHOW_CAUSAL_HIERARCHY";
     public static final String REFRESH_CAUSAL_HIERARCHY="REFRESH_CAUSAL_HIERARCHY";
     public static final String OPEN_ENTITY_WITH_ACTIONS = "openEntityWithActions";
     public static final String OPEN_AND_REFRESH_ENTITY = "openAndRefreshEntity";
     public static final String OPEN_SELECTED_ENTITY = "openSelectedEntity";
     public static final String OPEN_ENTITY_IN_TAB = "openEntityInTab";
     public static final String OPEN_ENTITY_IN_WINDOW = "openEntityInWindow";
     public static final String GET_APPLICATION_CONFIG_PARAMS = "getApplicationConfigParams";
     public static final String GET_COMPLEX_ENTITY_TYPES = "getComplexEntityTypes";
     public static final String REFRESH_SELECTED_ENTITY = "refreshSelectedEntity";
     public static final String OPEN_SELECTED_ENTITY_WITH_HEADER_ACTIONS = "openEntityWithHeaderActions";
 
     public static final String ENTITY_WITH_UPSTREAM_HIERARCHY = "entity.upstream.hierarchy";
     public static final String LOGIN_ACTION="user.login";
 
     private Map<String, OpenGroupsActionHandler> handlers = new HashMap<String, OpenGroupsActionHandler>();
 
     private ThinClientNetcellDao netcellDao = new ThinClientNetcellDao("localhost", 2000);
     private Map<String, EntityDefinitionSummary> flowDefSummaries;
     private Map<String, UserActionList> actionsMap = new HashMap<String, UserActionList>();
     private Map<String, UserAction> allActions = new HashMap<String, UserAction>();
     private Map<String, UserAction> globalActionsByLocation = new HashMap<String, UserAction>();
     private Map<String, UserAction> globalActionsByName = new HashMap<String, UserAction>();
     
     private static ActionsManager _instance = new ActionsManager();
     private ThreadPoolExecutor handlersExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
     private boolean initialized;
     private Logger logger = MasterLogManager.getLogger("ACTIONS-LOGGER");
 
     public static ActionsManager getInstance() {
 	return _instance;
     }
 
     private ActionsManager() {
 	init();
     }
 
     private void init() {
 	if (!initialized) {
 	    initHandlers();
 	    initialized = getAllAvailableActions();
 	    if (initialized) {
 		System.out.println("ActionsManager initialized.");
 	    } else {
 		System.out.println("Failed to initialize ActionsManager");
 	    }
 	}
     }
 
     private void initHandlers() {
 	handlers.put(LOAD_MAIN_WINDOW, new LoadMainWindowHandler());
 	handlers.put(SHOW_CAUSAL_HIERARCHY, new ShowCausalHierarchyHandler());
 	handlers.put(REFRESH_CAUSAL_HIERARCHY, new RefreshCausalHierarchyHandler());
 	handlers.put(OPEN_ENTITY_WITH_ACTIONS, new OpenEntityWithActionsHandler());
 	handlers.put(OPEN_AND_REFRESH_ENTITY, new OpenAndRefreshEntityHandler());
 	handlers.put(OPEN_SELECTED_ENTITY, new OpenSelectedEntityHandler());
 	handlers.put(OPEN_ENTITY_IN_WINDOW, new OpenEntityInWindowHandler());
 	handlers.put(OPEN_ENTITY_IN_TAB, new OpenEntityInTabHandler());
 	handlers.put(REFRESH_SELECTED_ENTITY, new RefreshSelectedEntityHandler());
 	handlers.put(OPEN_SELECTED_ENTITY_WITH_HEADER_ACTIONS, new OpenSelectedEntityWithHeaderActions());
     }
 
 
     private boolean getAllAvailableActions() {
 	CommandResponse response = netcellDao.execute(GET_ALL_AVAILABLE_ACTIONS, new HashMap<String, Object>());
 	if (response != null && response.isSuccessful()) {
 	    GenericNameValueList list = (GenericNameValueList) response.getValue("result");
 
 	    for (int i = 0; i < list.size(); i++) {
 		GenericNameValueContext row = (GenericNameValueContext) list.getValueForIndex(i);
 		UserAction ua = new UserAction(row);
 		// System.out.println(ua.getSourceEntityComplexType() + " "
 		// + ua.getActionName() + " : " + ua.getActionPath());
 		getUserActionList(ua.getSourceEntityActionLocation()).addAction(ua.getActionPath(), ua);
 		allActions.put(ua.getFullActionPath(), ua);
 		
 		if(ua.getSourceEntityComplexType().equals("*")) {
 		    globalActionsByLocation.put(ua.getActionLocation(), ua);
 		    globalActionsByName.put(ua.getActionName(), ua);
 		}
 	    }
 	    injectGlobalActions();
 	    
 	    return true;
 	}
 	return false;
     }
 
     private UserActionList getUserActionList(String key) {
 	UserActionList ual = actionsMap.get(key);
 	if (ual == null) {
 	    ual = new UserActionList();
 	    actionsMap.put(key, ual);
 	}
 	return ual;
     }
 
     public CommandResponse executeAction(ActionContext actionContext, Map<String, Object> params) {
 	UserAction ua = actionContext.getUserAction();
 //	Entity entity = actionContext.getEntity();
 	OpenGroupsApplication app = actionContext.getApp();
 
 	/* check if the action is allowed */
 //	List<String> currentUserTypes = UsersManager.getInstance().getCurrentUserTypes(entity, app);
 //	boolean actionAllowed = currentUserTypes.contains(ua.getUserType());
 	boolean actionAllowed = ua.allowExecute(actionContext);
 	if (actionAllowed) {
 	    return execute(ua.getAction(), params);
 	}
 	app.showNotification(ua.getUserType() + ".required.to." + ua.getActionName());
 	return null;
     }
 
     public void executeAction(String actionName, UserAction ua, Entity entity, OpenGroupsApplication app,
 	    ComponentContainer container, boolean runInSeparateThread) {
 	ActionContext ac = new ActionContext(ua, app, entity, container, runInSeparateThread);
 	executeAction(actionName, ac);
     }
 
     public void executeAction(String actionName, Entity entity, OpenGroupsApplication app,
 	    ComponentContainer container, boolean runInSeparateThread) {
 	ActionContext ac = new ActionContext(null, app, entity, container, runInSeparateThread);
 	executeAction(actionName, ac);
     }
 
     public void executeHandler(OpenGroupsActionHandler handler, UserAction ua, Entity entity,
 	    OpenGroupsApplication app, ComponentContainer container, boolean runInSeparateThread) {
 	ActionContext ac = new ActionContext(ua, app, entity, container, runInSeparateThread);
 	executeHandler(handler, ac);
     }
     
     public void executeHandler(OpenGroupsActionHandler handler, UserAction ua, Entity entity,
 	    OpenGroupsApplication app, ComponentContainer container, boolean runInSeparateThread,ActionContext rootContext) {
 	ActionContext ac = new ActionContext(ua, app, entity, container, runInSeparateThread);
 	ac.setMainEntity(rootContext.getMainEntity());
 	ac.setWindow(rootContext.getWindow());
 	if(entity == null) {
 	    ac.setEntity(rootContext.getMainEntity());
 	}
 	executeHandler(handler, ac);
     }
 
     public void executeAction(String actionName, Entity entity, OpenGroupsApplication app,
 	    ComponentContainer container, boolean runInSeparateThread, ActionContext rootContext) {
 	ActionContext ac = new ActionContext(null, app, entity, container, runInSeparateThread);
 	ac.setMainEntity(rootContext.getMainEntity());
 	ac.setWindow(rootContext.getWindow());
 	if(entity == null) {
 	    ac.setEntity(rootContext.getMainEntity());
 	}
 	executeAction(actionName, ac);
     }
 
     public void executeAction(String actionName, ActionContext ac) {
 	executeHandler(handlers.get(actionName), ac);
     }
 
     public void executeHandler(OpenGroupsActionHandler handler, ActionContext ac) {
 	if (ac.isRunInThread()) {
 	    // showProgressIndicator(ac.getTargetContainer());
 	    RunnableActionHandler runnableHandler = new RunnableActionHandler(handler, ac, this);
 	    handlersExecutor.execute(runnableHandler);
 	    System.out.println("Active threads: " + handlersExecutor.getActiveCount());
 	} else {
 	    try {
 		handler.handle(ac);
 	    } catch (Exception e) {
 		handleActionError(e, ac.getApp());
 	    }
 	}
     }
 
     private void showProgressIndicator(ComponentContainer container) {
 	if (container == null) {
 	    return;
 	}
 	container.removeAllComponents();
 	GridLayout lc = new GridLayout(1, 1);
 	lc.setSizeFull();
 	ProgressIndicator pi = new ProgressIndicator();
 	pi.setIndeterminate(true);
 	lc.addComponent(pi, 0, 0);
 	lc.setComponentAlignment(pi, Alignment.TOP_CENTER);
 	container.addComponent(lc);
     }
 
     public CommandResponse execute(String actionname, Map<String, Object> params) {
 	logger.info("Executing: " + actionname + " " + params);
 	return netcellDao.execute(actionname, params);
     }
 
     /*
      * protected UserActionList getAvailableActions(User user, OpenGroupsApplication app) { Map<String, Object> params =
      * new HashMap<String, Object>(); if (user != null) { params.put("userId", user.getUserId()); }
      * params.put("entityId", app.getSelectedEntity().getId()); CommandResponse response =
      * netcellDao.execute(GET_POSSIBLE_ACTIONS, params); GenericNameValueList list = (GenericNameValueList)
      * response.getValue("result"); UserActionList userActionsList = new UserActionList(list); return userActionsList; }
      * 
      * public UserActionList getAvailableActions(OpenGroupsApplication app) { User user = (User)
      * app.getAppContext().getHttpSession().getAttribute("user"); return getAvailableActions(user, app); }
      */
     
     
     private void injectGlobalActions() {
 	for(Map.Entry<String, UserAction> e : globalActionsByLocation.entrySet()) {
 	    String location = e.getKey();
 	    UserAction ua = e.getValue();
 	    for(Map.Entry<String, UserActionList> uale : actionsMap.entrySet()) {
 		if(uale.getKey().contains(location)) {
 		    uale.getValue().addAction(ua.getActionPath(), ua);
 		}
 	    }
 	}
     }
     
     public UserActionList getAvailableActions(Entity selectedEntity, String actionLocation) {
 	init();
 	String searchValue = selectedEntity.getComplexType() + ":" + actionLocation;
 	return actionsMap.get(searchValue);
 
     }
 
     public UserActionList getGlobalActions(String actionLocation) {
 	init();
 	String searchValue = "*:" + actionLocation;
 	return actionsMap.get(searchValue);
     }
 
     public EntityDefinitionSummary getFlowDefinitionSummary(String flowId) {
 	return flowDefSummaries.get(flowId);
     }
 
     @Override
     public void handleActionError(Exception e, OpenGroupsApplication app) {
 	app.pushError(e);
	e.printStackTrace();
     }
 
     public UserAction getActionByPath(String path) {
 	return allActions.get(path);
     }
     
     public UserAction getGlobalActionByName(String actionName){
 	return globalActionsByName.get(actionName);
     }
 }
