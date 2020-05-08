 package org.pentaho.pac.client.roles;
 
 import org.pentaho.pac.client.MessageDialog;
 import org.pentaho.pac.client.PacService;
 import org.pentaho.pac.client.PacServiceAsync;
 import org.pentaho.pac.client.PacServiceFactory;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupListener;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class RolesPanel extends DockPanel implements ClickListener, ChangeListener, PopupListener {
 
   RolesList rolesList = new RolesList();
   ListBox assignedUsersList = new ListBox(true);
   ProxyPentahoRole[] roles = null;
   RoleDetailsPanel roleDetailsPanel = new RoleDetailsPanel();
   Button updateRoleBtn = new Button("Update");
   Button addRoleBtn = new Button("+");
   Button deleteRoleBtn = new Button("-");
   NewRoleDialogBox newRoleDialogBox = new NewRoleDialogBox();
   MessageDialog confirmRoleDeleteDialog = new MessageDialog("Delete Roles", "Are your sure you want to delete the selected roles.", new int[] {MessageDialog.OK_BTN, MessageDialog.CANCEL_BTN});
   
 	public RolesPanel() {
 	  add(new Label("Roles go here."), DockPanel.NORTH);
 //	  DockPanel roleListPanel = buildRolesListPanel();
 //	  
 //	  DockPanel roleDetailsDockPanel = buildRoleDetailsPanel();
 //    add(roleListPanel, DockPanel.WEST);
 //    add(roleDetailsDockPanel, DockPanel.CENTER);
 //    
 //    setSpacing(10);
 //    
 //    setCellWidth(roleListPanel, "30%");
 //    setCellWidth(roleDetailsDockPanel, "70%");
 //    setCellHeight(roleListPanel, "100%");
 //    setCellHeight(roleDetailsDockPanel, "100%");
 //    roleListPanel.setWidth("100%");
 //    roleListPanel.setHeight("100%");
 //    roleDetailsDockPanel.setWidth("100%");
 //    roleDetailsDockPanel.setHeight("100%");
 //    
 //    roleDetailsPanel.setEnabled(false);
 //    updateRoleBtn.setEnabled(false);
 //    
 //    newRoleDialogBox.addPopupListener(this);
  	}
 
 	public DockPanel buildRoleDetailsPanel() {
 	  
     DockPanel assignedRolesPanel = buildAssignedUsersPanel();
     
 	  DockPanel dockPanel = new DockPanel();
 	  dockPanel.add(roleDetailsPanel, DockPanel.NORTH);
     dockPanel.add(updateRoleBtn, DockPanel.NORTH);
 	  dockPanel.add(assignedRolesPanel, DockPanel.CENTER);
 	  
 	  dockPanel.setCellHorizontalAlignment(updateRoleBtn, HasHorizontalAlignment.ALIGN_RIGHT);
     
 	  dockPanel.setCellWidth(roleDetailsPanel, "100%");
 	  dockPanel.setCellHeight(assignedRolesPanel, "100%");
 	  dockPanel.setCellWidth(assignedRolesPanel, "100%");
 	  
 	  roleDetailsPanel.setWidth("100%");
     assignedRolesPanel.setWidth("100%");
     assignedRolesPanel.setHeight("100%");
 	  
     updateRoleBtn.addClickListener(this);
 	  return dockPanel;
 	}
 	
 	public DockPanel buildRolesListPanel() {
 	  DockPanel headerDockPanel = new DockPanel();
     headerDockPanel.add(deleteRoleBtn, DockPanel.EAST);
 	  headerDockPanel.add(addRoleBtn, DockPanel.EAST);
     Label label = new Label("Roles");
 	  headerDockPanel.add(label, DockPanel.WEST);
 	  headerDockPanel.setCellWidth(label, "100%");
     DockPanel roleListPanel = new DockPanel();
     roleListPanel.add(headerDockPanel, DockPanel.NORTH);
     roleListPanel.add(rolesList, DockPanel.CENTER);
     roleListPanel.setCellHeight(rolesList, "100%");
     roleListPanel.setCellWidth(rolesList, "100%");
     roleListPanel.setHeight("100%");
     roleListPanel.setWidth("100%");
     rolesList.setHeight("100%");
     rolesList.setWidth("100%");
     addRoleBtn.setWidth("20px");
     deleteRoleBtn.setWidth("20px");
     addRoleBtn.setHeight("20px");
     deleteRoleBtn.setHeight("20px");
     deleteRoleBtn.setEnabled(false);
     rolesList.addChangeListener(this);
     addRoleBtn.addClickListener(this);
     deleteRoleBtn.addClickListener(this);
     return roleListPanel;
 	}
 	
 	public DockPanel buildAssignedUsersPanel() {
 	  DockPanel assignedRolesPanel = new DockPanel();
 	  assignedRolesPanel.add(new Label("Assigned Users"), DockPanel.NORTH);
 	  assignedRolesPanel.add(assignedUsersList, DockPanel.CENTER);
 	  assignedRolesPanel.setCellHeight(assignedUsersList, "100%");
 	  assignedRolesPanel.setCellWidth(assignedUsersList, "100%");
 	  assignedUsersList.setHeight("100%");
 	  assignedUsersList.setWidth("100%");
 	  return assignedRolesPanel;
 	}
 	
 	
 	public void onClick(Widget sender) {
 	  if (sender == updateRoleBtn) {
 	    updateRoleDetails();
 	  } else if (sender == deleteRoleBtn) {
 	    deleteSelectedRoles();
 	  } else if (sender == addRoleBtn) {
 	    addNewRole();
 	  }
 	}
 
 
 	private void addNewRole() {
 	  newRoleDialogBox.setUser(null);
     newRoleDialogBox.center();
    }
 	
 	private void deleteSelectedRoles() {
 	  final ProxyPentahoRole[] selectedRoles = rolesList.getSelectedRoles();
 	  if (selectedRoles.length > 0) {
 	    final int index = rolesList.getSelectedIndex();
 	    AsyncCallback callback = new AsyncCallback() {
 	      public void onSuccess(Object result) {
 	        rolesList.removeRoles(selectedRoles);
 	        roleSelectionChanged();
 	      }
 
 	      public void onFailure(Throwable caught) {
	        int x = 1;
 	      }
 	    };
 	    PacServiceFactory.getPacService().deleteRoles(selectedRoles, callback);
 	  }
 	}
 	
 	private void roleSelectionChanged() {
     ProxyPentahoRole[] selectedRoles = rolesList.getSelectedRoles();
     if (selectedRoles.length == 1) {
       roleDetailsPanel.setRole(selectedRoles[0]);
    } else {
       roleDetailsPanel.setRole(null);
     }
     roleDetailsPanel.setEnabled(selectedRoles.length == 1);
     updateRoleBtn.setEnabled(selectedRoles.length == 1);
     deleteRoleBtn.setEnabled(selectedRoles.length > 0);
 	}
 	
 	private void updateRoleDetails() {
     final ProxyPentahoRole role = roleDetailsPanel.getRole();
     final int index = rolesList.getSelectedIndex();
     AsyncCallback callback = new AsyncCallback() {
       public void onSuccess(Object result) {
         rolesList.setRole(index, role);
       }
 
       public void onFailure(Throwable caught) {
        int x = 1;
       }
     };
     PacServiceFactory.getPacService().updateRole(role, callback);
 	}
 	
 	public boolean validate() {return true;}
 
 	public void onChange(Widget sender) {
 	  roleSelectionChanged();
 	}
 	
 	public void refresh() {
 //	  rolesList.refresh();
 //	  roleSelectionChanged();
 	}
 	
   public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
     if (newRoleDialogBox.isUserCreated()) {
       ProxyPentahoRole newRole = newRoleDialogBox.getRole();
       if (rolesList.addRole(newRole)) {
         rolesList.setSelectedRole(newRole);
         roleSelectionChanged();
       }
     }
   }
   
   public boolean isInitialized() {
     return rolesList.isInitialized();
   }
 
   public void clearRolesCache() {
     rolesList.clearRolesCache();
   }
 }
