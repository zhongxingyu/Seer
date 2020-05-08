 /**
  * 
  */
 package org.cotrix.web.permissionmanager.client.menu;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.cotrix.web.permissionmanager.client.AdminArea;
 import org.cotrix.web.permissionmanager.client.resources.PermissionsResources;
 import org.cotrix.web.permissionmanager.shared.PermissionUIFeatures;
 import org.cotrix.web.share.client.feature.FeatureBinder;
 import org.cotrix.web.share.client.feature.HasFeature;
 import org.cotrix.web.share.client.resources.CommonResources;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.TreeViewModel;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class MenuTreeViewModel implements TreeViewModel {
 
 	protected static final MenuItem PROFILE_MENU = new MenuArea("My Profile", AdminArea.PROFILE, PermissionsResources.INSTANCE.profile());
 	protected static final MenuItem PREFERENCES_MENU = new MenuArea("My Preferences", AdminArea.PREFERENCES, CommonResources.INSTANCE.userPreferences());
 
 	protected static final MenuItem USERS_MENU = new MenuArea("My Users", AdminArea.USERS_PERMISSIONS, PermissionsResources.INSTANCE.users());
 	protected static final MenuItem CODELISTS_MENU = new MenuArea("My Codelists", AdminArea.CODELISTS_PERMISSIONS, PermissionsResources.INSTANCE.codelists());
 
 	protected ListDataProvider<MenuItem> MENU_PROVIDER = new ListDataProvider<MenuItem>(new ArrayList<MenuItem>(Arrays.asList(PROFILE_MENU, PREFERENCES_MENU, CODELISTS_MENU)));
 
 	protected static final AbstractCell<MenuItem> MENU_CELL = new AbstractCell<MenuItem>() {
 
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context, MenuItem value, SafeHtmlBuilder sb) {
 			if (value != null) {
				sb.appendHtmlConstant(value.getImageHtml()).appendEscaped(" ");
 				sb.appendEscaped(value.getLabel());
 			}
 		}
 	};
 
 	protected DefaultNodeInfo<MenuItem> rootNode;
 	protected DefaultNodeInfo<MenuItem> permissionsNode;
 
 	public MenuTreeViewModel(SelectionModel<MenuItem> selectionModel) {
 		rootNode = new DefaultNodeInfo<MenuItem>(MENU_PROVIDER, MENU_CELL, selectionModel, null);
 		bindFeatures();
 	}
 
 	protected void bindFeatures() {
 		FeatureBinder.bind(new HasFeature() {
 
 			@Override
 			public void unsetFeature() {
 				MENU_PROVIDER.getList().remove(USERS_MENU);
 				MENU_PROVIDER.refresh();
 			}
 
 			@Override
 			public void setFeature() {
 				MENU_PROVIDER.getList().add(USERS_MENU);
 				MENU_PROVIDER.refresh();
 			}
 		}, PermissionUIFeatures.EDIT_USERS_ROLES);
 	}
 
 	@Override
 	public <T> NodeInfo<?> getNodeInfo(T value) {
 		if (value == null) return rootNode;
 		//if (value == PERMISSIONS_MENU) return permissionsNode;
 		return null;
 	}
 
 	@Override
 	public boolean isLeaf(Object value) {
 		return value instanceof MenuArea;
 	}
 
 }
