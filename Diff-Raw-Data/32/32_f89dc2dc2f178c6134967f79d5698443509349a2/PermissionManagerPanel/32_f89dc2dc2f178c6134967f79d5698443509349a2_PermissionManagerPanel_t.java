 /**
  * 
  */
 package org.cotrix.web.permissionmanager.client;
 
 import org.cotrix.web.permissionmanager.client.application.ApplicationPermissionPanel;
 import org.cotrix.web.permissionmanager.client.codelists.CodelistsPermissionsPanel;
 import org.cotrix.web.permissionmanager.client.menu.MenuPanel;
 import org.cotrix.web.permissionmanager.client.menu.MenuSelectedEvent;
 import org.cotrix.web.permissionmanager.client.preferences.PreferencesPanel;
 import org.cotrix.web.permissionmanager.client.profile.ProfilePanel;
 import org.cotrix.web.share.client.event.CotrixBus;
 import org.cotrix.web.share.client.event.UserLoggedEvent;
 
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.DeckLayoutPanel;
 import com.google.gwt.user.client.ui.RequiresResize;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class PermissionManagerPanel extends ResizeComposite {
 
 	interface PermissionManagerPanelUiBinder extends UiBinder<Widget, PermissionManagerPanel> {}
 	interface PermissionManagerPanelEventBinder extends EventBinder<PermissionManagerPanel> {}
 
 	@Inject @UiField(provided=true) MenuPanel menuPanel;
 
 	@UiField DeckLayoutPanel contentPanel;
 	@Inject @UiField(provided=true) ApplicationPermissionPanel applicationPermissionPanel;
 	@Inject @UiField(provided=true) CodelistsPermissionsPanel codelistsPermissionspanel;
 	@Inject @UiField(provided=true) PreferencesPanel preferencesPanel;
 	@Inject @UiField(provided=true) ProfilePanel profilePanel;
 
 	@Inject
 	protected void init(PermissionManagerPanelUiBinder uiBinder) {
 		initWidget(uiBinder.createAndBindUi(this));
 		switchContent(AdminArea.PROFILE);
 	}
 
 	@Inject
 	protected void bind(@PermissionBus EventBus bus, PermissionManagerPanelEventBinder eventBinder) {
 		eventBinder.bindEventHandlers(this, bus);
 	}
 	
 	@Inject
 	protected void init(@CotrixBus EventBus bus) {
 		bus.addHandler(UserLoggedEvent.TYPE, new UserLoggedEvent.UserLoggedHandler() {
 			
 			@Override
 			public void onUserLogged(UserLoggedEvent event) {
 				switchContent(AdminArea.PROFILE);
				menuPanel.resetToProfile();
 			}
 		});
 	}
 
 	@EventHandler
 	protected void onMenuSelected(MenuSelectedEvent event) {
 		switchContent(event.getAdminArea());
 	}
 
 	protected void switchContent(AdminArea area) {
 		Widget areaWidget = getContentWidget(area);
 		contentPanel.showWidget(areaWidget);
 		
 		//GOOGLE issue workaround
 		if (areaWidget  instanceof RequiresResize) {
 			((RequiresResize)areaWidget).onResize();
 		}
 	}
 
 	protected Widget getContentWidget(AdminArea area) {
 		switch (area) {
 			case USERS_PERMISSIONS: return applicationPermissionPanel;
 			case CODELISTS_PERMISSIONS: return codelistsPermissionspanel;
 			case PREFERENCES: return preferencesPanel;
 			case PROFILE: return profilePanel;
 			default: throw new IllegalArgumentException("Unknown area "+area);
 		}
 	}
 }
