 package com.scholastic.sbam.client.uiobjects;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.fx.Resizable;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.custom.Portlet;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.UpdateUserCacheService;
 import com.scholastic.sbam.client.services.UpdateUserCacheServiceAsync;
 import com.scholastic.sbam.client.services.UpdateUserPortletCacheService;
 import com.scholastic.sbam.client.services.UpdateUserPortletCacheServiceAsync;
 import com.scholastic.sbam.client.uiobjects.HelpTextDialog;
 import com.scholastic.sbam.shared.objects.UserCacheTarget;
 import com.scholastic.sbam.shared.objects.UserPortletCacheInstance;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public abstract class AppPortlet extends Portlet {
 	
 	protected String	helpTextId;
	protected boolean	resizeable = true;	//	This doesn't quite work... Enclosing portal has to be adjusted somehow, to reposition the other portlets after resizing
 	protected Resizable	resizer;
 	
 	protected int		portalColumn = -1;
 	protected int		portalRow	 = -1;
 	
 	protected int		portletId;
 	
 	protected final UpdateUserCacheServiceAsync userCacheUpdateService = GWT.create(UpdateUserCacheService.class);
 	protected final UpdateUserPortletCacheServiceAsync userPortletCacheUpdateService = GWT.create(UpdateUserPortletCacheService.class);
 	
 	public AppPortlet(String helpTextId) {
 		super();
 		this.helpTextId = helpTextId;
 	}
 	
 	@Override
 	protected void onRender(Element el, int index) {
 		super.onRender(el, index);
 		if (resizeable) {
 			resizer = new Resizable(this);
			//	This can't be set to true, because of a bug in the resizer, where it sets the page position wrong and moves portlets to overlap over those above them
			resizer.setDynamic(false);
 			resizer.setMaxWidth(this.getWidth());
 			resizer.setMinWidth(this.getWidth());
 		}
 	}
 
 	@Override
 	protected void initTools() {
 		addHelp();
 		super.initTools();
 		addClosable();
 	}
 	
//	@Override
//	protected void onResize(int width, int height) {
//		super.onResize(width, height);
//		if (resizer != null) {
//			resizer.setMaxWidth(this.getWidth());	//	This turns off horizontal resizing
////			resizer.setMaxHeight(this.getHeight());	//	This turns off vertical resizing
//		}
//	}
 	
 	protected void addHelp() {
 		if (helpTextId == null)
 			return;
 		
 		ToolButton helpBtn = new ToolButton("x-tool-help");
 //		if (GXT.isAriaEnabled()) {
 //			helpBtn.setTitle(GXT.MESSAGES.pagingToolBar_beforePageText());
 //		}
 		helpBtn.addListener(Events.Select, new Listener<ComponentEvent>() {
 			public void handleEvent(ComponentEvent ce) {
 				HelpTextDialog htd = new HelpTextDialog(helpTextId);
 				htd.show();
 			}
 		});
 		head.addTool(helpBtn);
 	}
 	
 	protected void addClosable() {
 		
 		ToolButton closeBtn = new ToolButton("x-tool-close");
 		if (GXT.isAriaEnabled()) {
 			closeBtn.setTitle(GXT.MESSAGES.messageBox_close());
 		}
 		closeBtn.addListener(Events.Select, new Listener<ComponentEvent>() {
 			public void handleEvent(ComponentEvent ce) {
 				removeFromParent();
 				updateUserPortlet();
 			}
 		});
 		head.addTool(closeBtn);
 	}
 
 	public String getHelpTextId() {
 		return helpTextId;
 	}
 
 	public void setHelpTextId(String helpTextId) {
 		this.helpTextId = helpTextId;
 	}
 
 	public boolean isResizeable() {
 		return resizeable;
 	}
 
 	public void setResizeable(boolean resizeable) {
 		this.resizeable = resizeable;
 		if (resizeable)
 			System.out.println("WARNING: Attempt made to make a portlet resizeable, when there are bugs in it.");
 	}
 	
 	@Override
 	public void onCollapse() {
 		super.onCollapse();
 		updateUserPortlet();
 	}
 	
 	@Override
 	public void onExpand() {
 		super.onExpand();
 		updateUserPortlet();
 	}
 	
 	public void registerUserCache(UserCacheTarget target, String hint) {
 		
 		if (!AppConstants.USER_ACCESS_CACHE_ACTIVE)
 			return;
 		
 		if (target != null) {
 			userCacheUpdateService.updateUserCache(target, hint,
 					new AsyncCallback<String>() {
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
 
 						public void onSuccess(String result) {
 							//	Do nothing
 						}
 				});
 		}
 	}
 	
 	public void registerUserPortlet(int portletId, int row, int col) {
 		this.portletId = portletId;
 		updateUserPortlet(row, col, getParent() == null, this.isCollapsed());
 	}
 	
 	public void updateUserPortlet() {
 		updateUserPortlet(portalRow, portalColumn, getParent() == null, this.isCollapsed());
 	}
 	
 	public void updateUserPortlet(int row, int col) {
 		updateUserPortlet(row, col, getParent() == null, this.isCollapsed());
 	}
 	
 	public void updateUserPortlet(int row, int col, boolean closed, boolean minimized) {
 		portalRow = row;
 		portalColumn = col;
 		
 		if (!AppConstants.USER_PORTLET_CACHE_ACTIVE)
 			return;
 		
 		UserPortletCacheInstance cacheInstance = new UserPortletCacheInstance();
 		
 		cacheInstance.setPortletId(portletId);
 		cacheInstance.setPortletType(getClass().getName());
 		cacheInstance.setKeyData(getKeyData());
 		if (closed) {
 			cacheInstance.setRestoreColumn(-1);
 			cacheInstance.setRestoreRow(-1);
 		} else {
 			cacheInstance.setRestoreColumn(col);
 			cacheInstance.setRestoreRow(row);
 		}
 		cacheInstance.setMinimized(minimized);
 		
 		userPortletCacheUpdateService.updateUserPortletCache(cacheInstance,
 				new AsyncCallback<String>() {
 					public void onFailure(Throwable caught) {
 						// In production, this might all be removed, and treated as something users don't care about
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "User portlet cache update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(String result) {
 						//	Do nothing
 					}
 			});
 	}
 	
 	public int getPortalColumn() {
 		return portalColumn;
 	}
 
 	public void setPortalColumn(int portalColumn) {
 		this.portalColumn = portalColumn;
 	}
 
 	public int getPortalRow() {
 		return portalRow;
 	}
 
 	public void setPortalRow(int portalRow) {
 		this.portalRow = portalRow;
 	}
 
 	public int getPortletId() {
 		return portletId;
 	}
 
 	public void setPortletId(int portletId) {
 		this.portletId = portletId;
 	}
 
 	public abstract void setFromKeyData(String keyData);
 
 	public abstract String getKeyData();
 }
