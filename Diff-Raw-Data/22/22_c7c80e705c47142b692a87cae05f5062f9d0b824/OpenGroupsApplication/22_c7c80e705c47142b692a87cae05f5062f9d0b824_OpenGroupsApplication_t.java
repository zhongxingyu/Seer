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
 package ro.zg.open_groups;
 
 import java.net.URL;
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import ro.zg.commons.exceptions.ContextAwareException;
 import ro.zg.commons.exceptions.ExceptionContext;
 import ro.zg.netcell.vaadin.action.ActionContext;
 import ro.zg.netcell.vaadin.action.ActionsManager;
 import ro.zg.open_groups.gui.OpenGroupsMainWindow;
 import ro.zg.open_groups.gui.components.logic.CausalHierarchyItemSelectedListener;
 import ro.zg.open_groups.gui.components.logic.CausalHierarchyStartDepthChangedListener;
 import ro.zg.open_groups.gui.components.logic.CausalHierarchyTreeExpandListener;
 import ro.zg.open_groups.managers.ApplicationConfigManager;
 import ro.zg.open_groups.model.OpenGroupsModel;
 import ro.zg.open_groups.resources.OpenGroupsResources;
 import ro.zg.open_groups.user.UsersManager;
 import ro.zg.opengroups.constants.ApplicationConfigParam;
 import ro.zg.opengroups.constants.OpenGroupsExceptions;
 import ro.zg.opengroups.util.OpenGroupsUtil;
 import ro.zg.opengroups.vo.Entity;
 import ro.zg.opengroups.vo.EntityState;
 import ro.zg.opengroups.vo.OpenGroupsApplicationState;
 import ro.zg.opengroups.vo.TabContainer;
 import ro.zg.opengroups.vo.User;
 import ro.zg.openid.util.OpenIdConstants;
 import ro.zg.util.logging.Logger;
 import ro.zg.util.logging.MasterLogManager;
 import ro.zg.vaadin.util.WindowsManger;
 
 import com.vaadin.Application;
 import com.vaadin.service.ApplicationContext.TransactionListener;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.terminal.Terminal;
 import com.vaadin.terminal.gwt.server.WebApplicationContext;
 import com.vaadin.terminal.gwt.server.WebBrowser;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.CloseEvent;
 import com.vaadin.ui.Window.Notification;
 
 public class OpenGroupsApplication extends Application {
     /**
      * 
      */
     private static final long serialVersionUID = 2543794851816120227L;
 
     private WebApplicationContext appContext;
     private ActionsManager actionsManager = ActionsManager.getInstance();
     private Map<Long, TabContainer> tabsForEntities = new HashMap<Long, TabContainer>();
     private User currentUser;
     private Map<Long, Entity> openEntities = new HashMap<Long, Entity>();
     private Deque<Exception> errorsStack = new ArrayDeque<Exception>();
     private static Logger logger = MasterLogManager.getLogger("APP-LOGGER");
 
     private boolean botClient;
     private String lastFragment;
     private long lastFragmentUpdate;
 
     private static final String VAADIN_WINDOW_SEPARATOR = "_";
     private static final String MY_WINDOW_SEPARATOR = ":";
     private WindowsManger windowsManager;
     private OpenGroupsApplicationState appState;
     private OpenGroupsUriHandler uriHandler;
     private CausalHierarchyStartDepthChangedListener hierarchyStartDepthListener;
     private CausalHierarchyTreeExpandListener hierarchyTreeExpandListener;
     private CausalHierarchyItemSelectedListener hierarchyItemSelectedListener;
 
     @Override
     public void init() {
 
 	this.setLogoutURL(getAppRootContext());
 	windowsManager = new WindowsManger();
 	try {
 	    appState = new OpenGroupsApplicationState();
 	} catch (ContextAwareException e) {
 	    handleErrors();
 	}
 	setTheme("open-groupstheme");
 	appContext = (WebApplicationContext) getContext();
 	uriHandler = new OpenGroupsUriHandler(this);
 	hierarchyStartDepthListener = new CausalHierarchyStartDepthChangedListener(
 		this);
 	hierarchyTreeExpandListener = new CausalHierarchyTreeExpandListener();
 	hierarchyItemSelectedListener = new CausalHierarchyItemSelectedListener(
 		this);
 
 	printUserAgentInfo();
 	initSession();
 
 	getContext().addTransactionListener(new TransactionListener() {
 
 	    @Override
 	    public void transactionStart(Application application,
 		    Object transactionData) {
 		HttpServletRequest req = (HttpServletRequest) transactionData;
 		String fragment = req.getParameter("fr");
 		if (fragment != null) {
 		    fragment = fragment.trim();
 		    OpenGroupsApplication app = (OpenGroupsApplication) application;
 		    OpenGroupsMainWindow activeWindow = app.getActiveWindow();
 		    if (activeWindow == null) {
 			return;
 		    }
 		    String oldFragment = activeWindow.getFragment();
 
 		    logger.debug("Transaction start: new->'" + fragment
 			    + "' + old->'" + oldFragment + "'");
 		    if (oldFragment == null) {
 			logger.debug("Process fragment '" + fragment + "'");
 			uriHandler.handleFragment(fragment);
 			activeWindow.getUriUtility().setFragment(fragment,
 				false);
 		    }
 		    /* this may be a refresh */
 		    if (fragment.equals(oldFragment)) {
 			logger.debug("Refresh");
 			uriHandler.handleFragment(fragment);
 		    }
 		}
 	    }
 
 	    @Override
 	    public void transactionEnd(Application application,
 		    Object transactionData) {
 		// TODO Auto-generated method stub
 
 	    }
 	});
 
 	Window mainWindow = getWindow("showEntity");
 
 	// Window w = new Window();
 	// w.addURIHandler(uriHandler);
 	// setMainWindow(w);
     }
 
     private boolean checkOptimalBrowser() {
 	WebBrowser wb = appContext.getBrowser();
 	if (wb.isFirefox()) {
 	    return false;
 	}
 	return true;
     }
 
     public String getAppRootContext() {
 	String contextPath = getContext().getBaseDirectory().getName();
 	if (contextPath.length() > 1) {
 	    return "/" + contextPath + "/";
 	}
 	return contextPath;
     }
 
     private void printUserAgentInfo() {
 	WebBrowser wb = appContext.getBrowser();
 	StringBuffer sb = new StringBuffer();
 	sb.append("Session initialized from ip: ").append(wb.getAddress());
 	sb.append(", user agent: ").append(wb.getBrowserApplication());
 	sb.append(", version: ").append(wb.getBrowserMajorVersion());
 	logger.info(sb.toString());
     }
 
     @Override
     public void close() {
 	super.close();
 	logger.info("Session ended from ip "
 		+ appContext.getBrowser().getAddress());
     }
 
     private void initSession() {
 	getAppContext().getHttpSession().setMaxInactiveInterval(-1);
     }
 
     public void refreshEntity(Entity entity, ActionContext ac) {
 	actionsManager.executeAction(ActionsManager.OPEN_AND_REFRESH_ENTITY,
 		entity, this, entity.getEntityContainer(), false, ac);
     }
 
     public void fullyRefreshEntity(Entity entity, ActionContext ac) {
 	EntityState state = entity.getState();
 	if (state.getDesiredActionTabsQueue().size() == 0) {
 	    state.setDesiredActionTabsQueue(new ArrayDeque<String>(state
 		    .getCurrentActionTabsQueue()));
 	}
 	state.setOpened(true);
 	OpenGroupsMainWindow window = ac.getWindow();
 	logger.debug("Refresh entity " + entity.getId() + " in window "
 		+ window);
 	entity.setEntityContainer(window.getEntityContent());
 	actionsManager.executeAction(ActionsManager.OPEN_ENTITY_WITH_ACTIONS,
 		entity, this, window.getEntityContent(), false, ac);
 	if (hasErrors()) {
 	    handleErrors();
 	    return;
 	}
     }
 
     public void showNotification(String messageKey) {
 	String message = OpenGroupsResources.getMessage(messageKey);
 	Notification notification = new Notification(message);
 
 	Window activeWindow = getActiveWindow();
 	if (activeWindow == null) {
 	    activeWindow = getMainWindow();
 	}
 	activeWindow.showNotification(notification);
     }
 
     public void showNotification(String messageKey, int type) {
 	String message = OpenGroupsResources.getMessage(messageKey);
 	Notification notification = new Notification(message, type);
 	Window activeWindow = getActiveWindow();
 	if (activeWindow == null) {
 	    activeWindow = getMainWindow();
 	}
 	activeWindow.showNotification(notification);
     }
 
     public String getMessage(String msgKey) {
 	return OpenGroupsResources.getMessage(msgKey);
     }
 
     public String getMessage(String msgKey, Object... args) {
 	return OpenGroupsResources.getMessage(msgKey, args);
     }
 
     public void openEntityById(Long id, String actionPath, int pageNumber) {
 	Entity e = new Entity(id);
 	EntityState state = e.getState();
 	state.setDesiredActionsPath(actionPath);
 	state.setCurrentPageForAction(state.getDesiredActionsPath(), pageNumber);
 
 	openInActiveWindow(e);
 
 	// HttpSession session = getAppContext().getHttpSession();
 	// String userOpenId = (String)
 	// session.getAttribute(OpenIdConstants.USER_OPENID);
 	// if (userOpenId != null) {
 	// session.removeAttribute(OpenIdConstants.USER_OPENID);
 	// User user = OpenGroupsModel.getInstance().loginWithOpenId(userOpenId,
 	// getAppContext().getBrowser().getAddress());
 	// login(user, e);
 	// logger.info("Logged in with openid: " + userOpenId);
 	// } else {
 	// openInActiveWindow(e);
 	// }
     }
 
     private void displayMandatoryLogin() {
 	logger.debug("Manadatory login");
 	closeAllSubwindows();
 	actionsManager.getGlobalActionByName(ActionsManager.LOGIN_ACTION)
 		.executeHandler(this, null);
     }
 
     private void closeAllSubwindows() {
 	Window mainWindow = getMainWindow();
 	for (Window w : mainWindow.getChildWindows()) {
 	    mainWindow.removeWindow(w);
 	}
     }
 
     private User getUserFromOpenid() {
 	HttpSession session = getAppContext().getHttpSession();
 	String userOpenId = (String) session
 		.getAttribute(OpenIdConstants.USER_OPENID);
 	session.removeAttribute(OpenIdConstants.USER_OPENID);
 	if (userOpenId != null && checkAllowedDomain(userOpenId)) {
 	    User user = OpenGroupsModel.getInstance().loginWithOpenId(
 		    userOpenId, getAppContext().getBrowser().getAddress());
 	    return user;
 	}
 	return null;
     }
 
     private boolean checkAllowedDomain(String openid) {
 	String domains = (String) ApplicationConfigManager.getInstance()
 		.getApplicationConfigParam(
 			ApplicationConfigParam.ALLOWED_OPENID_DOMAINS);
 	if (domains == null) {
 	    return true;
 	}
 	String[] domainsArray = domains.split(",");
 	for (String domain : domainsArray) {
 	    if (openid.endsWith(domain.trim())) {
 		return true;
 	    }
 	}
 
 	/* the domain does not comply */
 
 	ExceptionContext ec = new ExceptionContext();
 	ec.put("args", new Object[] { domains });
 	if (domainsArray.length > 1) {
 	    pushError(new ContextAwareException("openid.allowed.domains", ec));
 	} else {
 	    pushError(new ContextAwareException("openid.allowed.domain", ec));
 	}
 	handleErrors();
 	return false;
     }
 
     private boolean isAllowed(Entity entity) {
 	User user = getCurrentUser();
 	if (user == null && getAppConfigManager().isInstancePrivate()) {
 	    displayMandatoryLogin();
 	    return false;
 	}
 
 	/*
 	 * don't allow the user to display entities from the inner logic of the
 	 * application
 	 */
 	long entityId = entity.getId();
 	if (entityId < getRootEntity().getId()) {
 	    pushError(OpenGroupsExceptions.getNoSuchEntityException(entityId));
 	    handleErrors();
 	    return false;
 	}
 
 	return true;
     }
 
     public Entity getOpenEntityForId(long id) {
 	return openEntities.get(id);
     }
 
     private String shortenCaption(String c, int length) {
 	if (c == null) {
 	    return null;
 	}
 	if (c.length() > length) {
 	    return c.substring(0, length - 3) + "...";
 	}
 	return c;
     }
 
 
     public void pushError(Exception e) {
 	errorsStack.push(e);
     }
 
     public boolean hasErrors() {
 	return !errorsStack.isEmpty();
     }
 
     public void handleErrors() {
 	String errorMessage = "";
 	while (errorsStack.size() > 0) {
 	    Exception e = errorsStack.pop();
 	    if (e instanceof ContextAwareException) {
 		ContextAwareException cae = (ContextAwareException) e;
 		ExceptionContext ec = cae.getExceptionContext();
 		if (ec != null) {
 		    Object[] args = (Object[]) ec.getValue("args");
 		    if (args != null) {
 			errorMessage += "<br>- "
 				+ getMessage(cae.getType(), args);
 			continue;
 		    }
 		}
 		errorMessage += "<br>- "
 			+ getMessage(((ContextAwareException) e).getType());
 	    } else {
 		errorMessage += "<br>- " + getMessage("system.error");
 	    }
 	}
 	Notification notification = new Notification(
 		getMessage("error.notification.caption"), errorMessage,
 		Notification.TYPE_ERROR_MESSAGE);
 	getActiveWindow().showNotification(notification);
 
 	/* to be sure */
 	errorsStack.clear();
     }
 
     /**
      * @return the appContext
      */
     public WebApplicationContext getAppContext() {
 	return appContext;
     }
 
     // /**
     // * @return the mainWindow
     // */
     // public OpenGroupsMainWindow getMainWindow() {
     // return (OpenGroupsMainWindow) super.getMainWindow();
     // }
 
     /**
      * @return the currentUser
      */
     public User getCurrentUser() {
 	if (currentUser != null) {
 	    return currentUser;
 	}
 	User user = getUserFromOpenid();
 	if (user != null) {
 	    login(user);
 	}
 	return currentUser;
     }
 
     public Long getCurrentUserId() {
 	if (currentUser != null) {
 	    return currentUser.getUserId();
 	}
 	return null;
     }
 
     /**
      * @param currentUser
      *            the currentUser to set
      */
     public void login(User currentUser/* , Entity entity */) {
 	if (currentUser != null) {
 	    this.currentUser = currentUser;
 	    UsersManager.getInstance().addUser(currentUser);
 	    logger.info("User logged in: " + currentUser.getEmail());
 
 	}
     }
 
     public void loginAndShowEntity(User currentUser, Entity entity) {
 	login(currentUser);
 	/* refresh main application window */
 	openInActiveWindow(entity);
     }
 
     public void logout() {
 	UsersManager.getInstance().removeUser(currentUser.getUserId());
 	this.currentUser = null;
 	getAppContext().getHttpSession().invalidate();
 	// this.close();
     }
 
     public void openIdLogin(String providerUrl) {
 	HttpSession session = getAppContext().getHttpSession();
 	session.setAttribute(OpenIdConstants.PROVIDER_URL, providerUrl);
 	OpenGroupsMainWindow currentWindow = appState.getActiveWindow();
 	URL url = currentWindow.getURL();
 	String path = url.getPath();
 	String urlString = url.getProtocol() + "://" + url.getHost() + ":"
 		+ url.getPort();
 	if (path.length() > 1) {
 	    int contextIndex = path.substring(1).indexOf("/");
 	    String contextPath = null;
 	    if (contextIndex > 0) {
 		contextPath = path.substring(0, contextIndex + 1);
 		urlString += contextPath + "/";
 	    }
 	}
 
 	String returnUrl = urlString
 		+ "#"
 		+ OpenGroupsUtil.getFragmentForEntity(appState
			.getDesiredEntity());
 	session.setAttribute(OpenIdConstants.RETURN_URL, returnUrl);
 	appState.getActiveWindow().open(
 		new ExternalResource(urlString + "openid/login"));
     }
 
     /**
      * Opens an entity in a new window
      * 
      * @param entity
      */
     public void openInActiveWindow(Entity entity) {
 	System.out.println("*****opening entity: " + entity);
 	long entityId = -1;
 	long rootEntityId = -1;
 	if (entity == null) {
 	    entity = getRootEntity();
 	} else if ((entityId = entity.getId()) != (rootEntityId = getRootEntity()
 		.getId())) {
 
 	    /*
 	     * this is stupid, should be handled via the complex_entity_type
 	     * table
 	     */
 	    entity.getState().setEntityTypeVisible(true);
 	}
	appState.setDesiredEntity(entity);
 	OpenGroupsMainWindow activeWindow = getActiveWindow();
 	if (!isAllowed(entity)) {
 	    activeWindow.setContentVisible(false);
 	    return;
 	}
 	activeWindow.setContentVisible(true);
 	appState.setActiveEntity(entity);
 	entity.getState().setOpened(true);
 
 	logger.debug("Open entity " + entity.getId() + " in window "
 		+ activeWindow);
 	actionsManager.executeAction(ActionsManager.OPEN_ENTITY_IN_WINDOW,
 		entity, this, activeWindow, false);
 	if (hasErrors()) {
 	    handleErrors();
 	    return;
 	}
     }
 
     protected OpenGroupsMainWindow createWindow(String name) {
 	Integer nextId = windowsManager.getNextWindowId(name);
 	name = name + MY_WINDOW_SEPARATOR + nextId;
 	System.out.println("->createWindow(" + name + ")");
 	OpenGroupsMainWindow w = new OpenGroupsMainWindow(this,
 		getAppConfigManager().getInstanceName());
 	System.out.println("createWindow(" + name + ") -> " + w);
 	return w;
     }
 
     @Override
     public Window getWindow(String name) {
 	Window w = super.getWindow(name);
 
 	if ((w == null) && !name.contains("UIDL")) {
 	    w = createWindow(name);
 	    initWindow((OpenGroupsMainWindow) w);
 	    setMainWindow(w);
 
 	}
 	if (w != null) {
 	    appState.setActiveWindow((OpenGroupsMainWindow) w);
 	    logger.debug("Active window set to: " + w);
 	}
 	return w;
     }
 
     private void initWindow(OpenGroupsMainWindow w) {
 	// OpenGroupsUriHandler uriHandler = new OpenGroupsUriHandler(this);
 	// w.getUriUtility().addListener(uriHandler);
 	// w.addURIHandler(uriHandler);
 	addCloseListener(w);
 	// CausalHierarchyContainer chc = w.getHierarchyContainer();
 	// chc.setStartDepthChangedListener(hierarchyStartDepthListener);
 	// chc.setTreeExpandListener(hierarchyTreeExpandListener);
 	// chc.setSelectionChangedListener(hierarchyItemSelectedListener);
 	w.init();
     }
 
     public OpenGroupsMainWindow getActiveWindow() {
 	return appState.getActiveWindow();
     }
 
     public Entity getActiveEntity() {
 	return appState.getActiveEntity();
     }
 
     private void addCloseListener(final Window w) {
 	w.addListener(new Window.CloseListener() {
 
 	    @Override
 	    public void windowClose(CloseEvent e) {
 		if (w == getMainWindow()) {
 		    return;
 		}
 		removeWindow(w);
 		// String[] args = w.getName().split(MY_WINDOW_SEPARATOR);
 		// windowsManager.removeWindowId(args[0], new Integer(args[1]));
 		System.out.println("removed " + w.getName());
 	    }
 	});
     }
 
     /**
      * @return the rootEntity
      */
     public Entity getRootEntity() {
 	return appState.getRootEntity();
     }
 
     public boolean isRootSelected() {
 	return appState.isRootSelected();
     }
 
     public String getCurrentUri() {
 	return appState.getCurrentUri();
     }
 
     public void setCurrentUri(String currentUri) {
 	appState.setCurrentUri(currentUri);
     }
 
     @Override
     public void terminalError(Terminal.ErrorEvent event) {
 	String message = event.getThrowable().getMessage();
 	if ("FILE_NOT_FOUND".equals(message)) {
 	    Notification notification = new Notification(
 		    getMessage("error.notification.caption"), "<br>- "
 			    + getMessage("system.error"),
 		    Notification.TYPE_ERROR_MESSAGE);
 	    getActiveWindow().showNotification(notification);
 	}
 	// Call the default implementation.
 	super.terminalError(event);
 	// Some custom behaviour.
 	if (getActiveWindow() != null) {
 	    Notification notification = new Notification(
 		    getMessage("error.notification.caption"), "<br>- "
 			    + getMessage("system.error"),
 		    Notification.TYPE_ERROR_MESSAGE);
 	    getActiveWindow().showNotification(notification);
 	}
     }
 
     /**
      * @return the botClient
      */
     public boolean isBotClient() {
 	return botClient;
     }
 
     /**
      * @param botClient
      *            the botClient to set
      */
     public void setBotClient(boolean botClient) {
 	this.botClient = botClient;
     }
 
     /**
      * @return the lastFragment
      */
     public String getLastFragment() {
 	return lastFragment;
     }
 
     /**
      * @param lastFragment
      *            the lastFragment to set
      */
     public void setLastFragment(String lastFragment) {
 	this.lastFragment = lastFragment;
     }
 
     /**
      * @return the lastFragmentUpdate
      */
     public long getLastFragmentUpdate() {
 	return lastFragmentUpdate;
     }
 
     /**
      * @param lastFragmentUpdate
      *            the lastFragmentUpdate to set
      */
     public void setLastFragmentUpdate(long lastFragmentUpdate) {
 	this.lastFragmentUpdate = lastFragmentUpdate;
     }
 
     /**
      * @return the hierarchyStartDepthListener
      */
     public CausalHierarchyStartDepthChangedListener getHierarchyStartDepthListener() {
 	return hierarchyStartDepthListener;
     }
 
     /**
      * @return the hierarchyTreeExpandListener
      */
     public CausalHierarchyTreeExpandListener getHierarchyTreeExpandListener() {
 	return hierarchyTreeExpandListener;
     }
 
     /**
      * @return the hierarchyItemSelectedListener
      */
     public CausalHierarchyItemSelectedListener getHierarchyItemSelectedListener() {
 	return hierarchyItemSelectedListener;
     }
 
     /**
      * @return the uriHandler
      */
     public OpenGroupsUriHandler getUriHandler() {
 	return uriHandler;
     }
 
     public ApplicationConfigManager getAppConfigManager() {
 	return ApplicationConfigManager.getInstance();
     }
 }
