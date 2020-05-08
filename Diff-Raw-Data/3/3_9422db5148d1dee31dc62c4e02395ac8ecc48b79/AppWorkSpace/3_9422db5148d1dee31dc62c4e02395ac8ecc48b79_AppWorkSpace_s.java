 package com.scholastic.sbam.client.uiobjects.uiapp;
 
 import java.util.List;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.UserPortletCacheListService;
 import com.scholastic.sbam.client.services.UserPortletCacheListServiceAsync;
 import com.scholastic.sbam.client.uiobjects.foundation.AppSleeper;
 import com.scholastic.sbam.client.uiobjects.uitop.HelpTextDialog;
 import com.scholastic.sbam.shared.objects.Authentication;
 import com.scholastic.sbam.shared.objects.UserPortletCacheInstance;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class AppWorkSpace extends LayoutContainer implements AppSleeper {
 	public static int DEFAULT_PORTAL_COL_COUNT	=	2;
 	public static int DEFAULT_PORTAL_COL_WIDTH	=	750;
 	
 	public static class AppTreeSelectionModel extends TreePanelSelectionModel<ModelData> {
 		AppPortletProvider provider;
 		
 		AppTreeSelectionModel(AppPortletProvider provider) {
 			super();
 			this.provider = provider;
 		}
 		
 		@Override
 		public void onSelectChange(ModelData model, boolean select) {
 			super.onSelectChange(model, select);
 			if (select) {
 				provider.insertPortlet(model, 0, 0);
 			}
 			this.deselectAll();
 		}
 	}
 	
 	//	These must be instantiated now, not on render
 	private LayoutContainer			thePortalArea	=	new LayoutContainer();
 	private AppPortletPresenter		thePortal;	//		=	new AppPortalWithCache(DEFAULT_PORTAL_COL_COUNT);
 	private AppPortletProvider		portletProvider;//	=	new AppPortletProvider(thePortal);
 	private TreePanel<ModelData>	appNavTree;
 	
 	private boolean					cachedPortletsLoaded	= false;
 
 	@Override  
 	protected void onRender(Element parent, int index) {  
 
 		super.onRender(parent, index);
 		setLayout(new BorderLayout());
 		
 		thePortalArea.setId("portalContainer");
 		
 	//	thePortal = new AppPortalWithCache(2);
 //		thePortal.setId("thePortal");
 //		thePortal.setWidth(DEFAULT_PORTAL_COL_COUNT * DEFAULT_PORTAL_COL_WIDTH);
 //		thePortal.setAutoWidth(false);
 //		thePortal.setColumnWidth(0, DEFAULT_PORTAL_COL_WIDTH);
 //		thePortal.setColumnWidth(1, DEFAULT_PORTAL_COL_WIDTH);
 		thePortal = getPreferredPortal(parent);
 		//	getPortalWithCache();	
 		//	getTabPortal();
 		portletProvider = new AppPortletProvider(thePortal);
 		if (thePortal instanceof AppTabPortal) {
 			thePortalArea.setWidth(0);
 			thePortalArea.setScrollMode(Scroll.NONE);
 		} else {
 			thePortalArea.setWidth(DEFAULT_PORTAL_COL_COUNT * DEFAULT_PORTAL_COL_WIDTH);
 			thePortalArea.setScrollMode(Scroll.AUTO);
 			thePortalArea.setScrollMode(Scroll.AUTO);
 		}
 		
 		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
 		centerData.setSplit(true);
 		
 		ContentPanel contentPanel = new ContentPanel() {
 			@Override
 			public void initTools() {
 				addNavHelp(this);
 				super.initTools();
 			}
 		};
 		contentPanel.setHeading("Navigation");
 		contentPanel.setCollapsible(false);
 		contentPanel.setBorders(true);
 		contentPanel.setScrollMode(Scroll.AUTOY);
 		
 		appNavTree = AppNavTree.getTreePanel();
 	//	portletProvider = new AppPortletProvider(thePortal);
 		appNavTree.setSelectionModel(new AppTreeSelectionModel(portletProvider));
 		contentPanel.add(appNavTree);
 		
 		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 180f, 180, 200);
 		westData.setCollapsible(true);
 		westData.setFloatable(true);
 		westData.setSplit(true);
 		westData.setMargins(new Margins(5));
 
 //		thePortalArea.add(thePortal);
 		
 		add(contentPanel, 	westData);
 		add(thePortalArea, 	centerData);
 	}
 	
 	protected void addNavHelp(ContentPanel contentPanel) {	
 		ToolButton helpBtn = new ToolButton("x-tool-help");
 //		if (GXT.isAriaEnabled()) {
 //			helpBtn.setTitle(GXT.MESSAGES.pagingToolBar_beforePageText());
 //		}
 		helpBtn.addListener(Events.Select, new Listener<ComponentEvent>() {
 			public void handleEvent(ComponentEvent ce) {
 				HelpTextDialog htd = new HelpTextDialog("NavTree");
 				htd.show();
 			}
 		});
 		contentPanel.getHeader().addTool(helpBtn);
 	}
 	
 	public AppPortletPresenter getPreferredPortal(Element parent) {
 		if (parent != null) {
 			//	If there's enough room for at least two portal columns plus the nav tree, go that route
 			if (parent.getOffsetWidth() > (2 * DEFAULT_PORTAL_COL_WIDTH) + 200)
 				return getPortalWithCache();
 			else
 				return getTabPortal();
 		} else {
 			return getPortalWithCache();
 		}
 	}
 	
 	public AppPortletPresenter getPortalWithCache() {
 
 			AppPortalWithCache thePortal = new AppPortalWithCache(2);
 			thePortal.setId("thePortal");
 			thePortal.setWidth(DEFAULT_PORTAL_COL_COUNT * DEFAULT_PORTAL_COL_WIDTH);
 			thePortal.setAutoWidth(false);
 			thePortal.setColumnWidth(0, DEFAULT_PORTAL_COL_WIDTH);
 			thePortal.setColumnWidth(1, DEFAULT_PORTAL_COL_WIDTH);
 			
 			thePortalArea.add(thePortal);
 			
 			return thePortal;
 	}
 	
 	public AppPortletPresenter getTabPortal() {
 		AppTabPortal thePortal = new AppTabPortal();
 		thePortal.setId("theTabPortal");
 		
 		thePortalArea.setLayout(new FitLayout());
 		thePortalArea.add(thePortal);
 		
 		return thePortal;
 	}
 	
 	public void setLoggedIn(Authentication auth) {
 		//	Determine if portlets need to be loaded now that the user has logged in (if they have none to load, mark them as loaded).
 		cachedPortletsLoaded = auth.getCachedPortlets() <= 0;
 	}
 	
 	public void setLoggedOut() {
 		//	On logout, remove all portlets
 		removeAllPortlets();
 		//	And mark them as gone
 		cachedPortletsLoaded = false;
 	}
 	
 	/**
 	 * Restore all portlets for this user from the user portlet cache.
 	 */
 	public void restorePortlets() {
 		/*
 		 * NOTE: Portlets must be reloaded this way, because simply loading them on login, before the portal is actually shown, causes
 		 * 		 discrepancies in the layouts (i.e. portlets get drawn incorrectly when a user logs out then logs back in without
 		 * 		 first reloading the entire app in the browser).
 		 */
 		
 		if (cachedPortletsLoaded)
 			return;
 		
 		cachedPortletsLoaded = true;
 		
 		if (!AppConstants.USER_PORTLET_CACHE_ACTIVE)
 			return;
 		
 		final UserPortletCacheListServiceAsync userPortletCacheUpdateService = GWT.create(UserPortletCacheListService.class);
 
 		mask("Loading cached portlets...");
 		// Initiate the call to load the portlets
 		userPortletCacheUpdateService.getUserPortlets(null, null,
 				new AsyncCallback<List<UserPortletCacheInstance>>() {
 					public void onFailure(Throwable caught) {
 						// In production, this might all be removed, and treated as something users don't care about
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "User cache update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(List<UserPortletCacheInstance> list) {
 						//	First, set the column widths
 						thePortal.restorePresentationState(list);
 						
 						//	Second, create and add the portlets
 						for (UserPortletCacheInstance instance : list) {
 							AppPortlet portlet = portletProvider.getPortlet(instance.getPortletType());
 							if (instance.getRestoreHeight() > 0)
 								portlet.setForceHeight(instance.getRestoreHeight());
 //							portlet.setPortletId(instance.getPortletId());
 							portlet.setFromKeyData(instance.getKeyData());
 							portlet.setLastCacheInstance(instance);
 							if (thePortal instanceof AppPortalWithCache && instance.isMinimized())
 								portlet.collapse();
 							thePortal.reinsert(portlet, instance.getRestoreRow(), instance.getRestoreColumn(), instance.getPortletId());
 						}
 						unmask();
 					}
 			});
 	}
 
 	/**
 	 * Remove all portlets (without recording them as closed in the user portlet cache)
 	 */
 	public void removeAllPortlets() {
		thePortal.removeAllPortlets();
 	}
 
 	@Override
 	public void awaken() {
 		//	When the user chooses this tab, if it hasn't happened already, load the portlets
 		if (!cachedPortletsLoaded)
 			restorePortlets();
 		
 //		for (LayoutContainer portlet : thePortal.getItems())
 //			if (portlet instanceof AppSleeper)
 //				((AppSleeper) portlet).awaken();
 	}
 
 	@Override
 	public void sleep() {
 //		for (LayoutContainer portlet : thePortal.getItems())
 //			if (portlet instanceof AppSleeper)
 //				((AppSleeper) portlet).sleep();
 	}
 
 }
 
