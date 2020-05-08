 /**
  * 
  */
 package org.cotrix.web.permissionmanager.client.codelists;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.cotrix.web.permissionmanager.client.PermissionBus;
 import org.cotrix.web.permissionmanager.client.PermissionServiceAsync;
 import org.cotrix.web.permissionmanager.client.codelists.AddUserDialog.AddUserEvent;
 import org.cotrix.web.permissionmanager.client.codelists.AddUserDialog.AddUserHandler;
 import org.cotrix.web.permissionmanager.client.codelists.tree.CodelistSelectedEvent;
 import org.cotrix.web.permissionmanager.client.codelists.tree.CodelistsTreePanel;
 import org.cotrix.web.permissionmanager.client.matrix.RolesRowUpdatedEvent;
 import org.cotrix.web.permissionmanager.client.matrix.UsersRolesMatrix;
 import org.cotrix.web.permissionmanager.shared.CodelistGroup.CodelistVersion;
 import org.cotrix.web.permissionmanager.shared.RoleAction;
 import org.cotrix.web.permissionmanager.shared.RoleState;
 import org.cotrix.web.permissionmanager.shared.RolesRow;
 import org.cotrix.web.permissionmanager.shared.RolesType;
 import org.cotrix.web.share.client.error.ManagedFailureCallback;
 import org.cotrix.web.share.client.event.CotrixBus;
 import org.cotrix.web.share.client.event.UserLoggedEvent;
 import org.cotrix.web.share.client.util.DataUpdatedEvent;
 import org.cotrix.web.share.client.util.DataUpdatedEvent.DataUpdatedHandler;
 import org.cotrix.web.share.client.util.StatusUpdates;
 import org.cotrix.web.share.client.widgets.ItemToolbar;
 import org.cotrix.web.share.client.widgets.ItemToolbar.ButtonClickedEvent;
 import org.cotrix.web.share.shared.UIUser;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.DeckLayoutPanel;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class CodelistsPermissionsPanel extends ResizeComposite {
 
 	interface CodelistsPermissionsPanelUiBinder extends	UiBinder<Widget, CodelistsPermissionsPanel> {}
 	interface CodelistsPermissionsPanelEventBinder extends EventBinder<CodelistsPermissionsPanel> {}
 
 	@Inject
 	protected PermissionServiceAsync service;
 
 	@UiField DeckLayoutPanel centralPanel;
 	@UiField DockLayoutPanel blankPanel;
 	@UiField DockLayoutPanel rolesPanel;
 	@Inject @UiField(provided=true) UsersRolesMatrix usersRolesMatrix;
 	@Inject @UiField(provided=true) CodelistsTreePanel codelistsTreePanel;
 	@UiField ItemToolbar toolBar;
 
 	@Inject
 	protected AddUserDialog addUserDialog;
 
 	protected String currentCodelistId = null;
 	
 	protected String currentUserId;
 	protected Set<String> codelistsUsersIds = new HashSet<String>();
 
 	@Inject
 	protected CodelistRolesRowDataProvider dataProvider;
 
 	protected List<String> roles;
 
 	@Inject
 	protected void init(CodelistsPermissionsPanelUiBinder uiBinder) {
 		initWidget(uiBinder.createAndBindUi(this));
 		centralPanel.showWidget(blankPanel);
 		addUserDialog.addAddUserHandler(new AddUserHandler() {
 			
 			@Override
 			public void onAddUser(AddUserEvent event) {
 				userAdded(event.getUser().toUiUser());
 			}
 		});
 	}
 	
 	@UiHandler("toolBar")
 	protected void onToolBarButtonClicked(ButtonClickedEvent event) {
 		switch (event.getButton()) {
 			case PLUS: addUserDialog.center(); break;
 			case MINUS: {
 				RolesRow row = usersRolesMatrix.getSelectedRow();
 				if (row!=null) removeRow(row);
 			} break;
 		}
 	}
 	
 	protected void removeRow(final RolesRow row) {
 		Log.trace("removeRow row: "+row);
 		StatusUpdates.statusSaving();
 		row.setLoading(true);
 		dataProvider.refresh();
 		service.codelistRolesRowRemoved(currentCodelistId, row, new ManagedFailureCallback<Void>() {
 			
 			@Override
 			public void onSuccess(Void result) {
 				/*Log.trace("removed row "+row);
 				boolean removed = dataProvider.getCache().remove(row);
 				Log.trace("removed: "+removed);
 				Log.trace("current cache: "+dataProvider.getCache());*/
 				usersRolesMatrix.refresh();
 				StatusUpdates.statusSaved();
 			}
 		});
 	}
 	
 	protected void userAdded(UIUser user) {
 		RolesRow row = new RolesRow(user, new HashMap<String, RoleState>());
 		saveRow(row, null, true);
 		dataProvider.getCache().add(row);
 		dataProvider.refresh();
 	}
 
 	@Inject
 	protected void bind(@CotrixBus EventBus cotrixBus, @PermissionBus EventBus bus, CodelistsPermissionsPanelEventBinder binder) {
 		binder.bindEventHandlers(this, bus);
 		cotrixBus.addHandler(UserLoggedEvent.TYPE, new UserLoggedEvent.UserLoggedHandler() {
 
 			@Override
 			public void onUserLogged(UserLoggedEvent event) {
 				currentCodelistId = null;
 				currentUserId = event.getUser().getId();
 				centralPanel.showWidget(blankPanel);
 				codelistsTreePanel.refresh();
 			}
 		});
 	}
 
 	@Inject
 	protected void setupMatrix() {
 		service.getRoles(RolesType.CODELISTS, new ManagedFailureCallback<List<String>>() {
 
 			@Override
 			public void onSuccess(List<String> result) {
 				usersRolesMatrix.setupMatrix(result, dataProvider);
 			}
 		});
 		dataProvider.addDataUpdatedHandler(new DataUpdatedHandler() {
 			
 			@Override
 			public void onDataUpdated(DataUpdatedEvent event) {
 				codelistsUsersIds.clear();
 				if (currentUserId!=null) codelistsUsersIds.add(currentUserId);
 				for (RolesRow row:dataProvider.getCache()) {
 					codelistsUsersIds.add(row.getUser().getId());
 				}
 				addUserDialog.setIds(codelistsUsersIds);
 			}
 		});
 	}
 
 	@EventHandler
 	protected void onRolesRowUpdate(RolesRowUpdatedEvent event) {
 		if (event.getSource()!=usersRolesMatrix) return;
 		saveRow(event.getRow(), event.getRole(), event.getValue());
 	}
 
 	@EventHandler
 	protected void onCodelistSelected(CodelistSelectedEvent event) {
 		Log.trace("onCodelistSelected "+event.getCodelist());
 		showMatrix(event.getCodelist());
 	}
 
 	protected void showMatrix(CodelistVersion codelist) {
 		Log.trace("showMatrix "+codelist);
 		centralPanel.showWidget(rolesPanel);
 
 		currentCodelistId = codelist.getId();
 		dataProvider.setCodelistId(currentCodelistId);
 		usersRolesMatrix.refresh();
 	}
 
 	protected void saveRow(final RolesRow row, String role, boolean value) {
 
 		RoleAction action = value?RoleAction.DELEGATE:RoleAction.REVOKE;
 		StatusUpdates.statusSaving();
 		service.codelistRoleUpdated(row.getUser().getId(), currentCodelistId, role, action, new ManagedFailureCallback<RolesRow>() {
 
 			@Override
 			public void onSuccess(RolesRow updatedRow) {
 				row.setRoles(updatedRow.getRoles());
 				dataProvider.refresh();
 				StatusUpdates.statusSaved();
 			}
 		});
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
		//Fix GWT issue
 		if (visible) onResize();
 	}
 
 }
