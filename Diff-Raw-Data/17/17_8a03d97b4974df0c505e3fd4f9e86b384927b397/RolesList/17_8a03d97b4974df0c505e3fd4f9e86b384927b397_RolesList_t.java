 package org.pentaho.pac.client.roles;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.pentaho.pac.client.MessageDialog;
 import org.pentaho.pac.client.PacService;
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.users.ProxyPentahoUser;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.ListBox;
 
 public class RolesList extends ListBox {
   ArrayList roles = new ArrayList();
   MessageDialog messageDialog = new MessageDialog("Roles", "", new int[]{MessageDialog.OK_BTN});
   boolean isInitialized = false;
   
   public RolesList() {
     super(true);
   }
 
   public ProxyPentahoRole[] getRoles() {
     return (ProxyPentahoRole[])roles.toArray(new ProxyPentahoRole[0]);
   }
 
   public void setRoles(ProxyPentahoRole[] roles) {
     
     this.roles.clear();
     this.roles.addAll(Arrays.asList(roles));
     clear();
     for (int i = 0; i < roles.length; i++) {
       addItem(roles[i].getName());
     }
     isInitialized = true;
   }
   
   public void setRole(int index, ProxyPentahoRole role) {
     roles.set(index, role);
     setItemText(index, role.getName());
   }
   
   public ProxyPentahoRole[] getSelectedRoles() {
     ArrayList selectedRoles = new ArrayList();
     int itemCount = getItemCount();
     for (int i = 0; i < itemCount; i++) {
       if (isItemSelected(i)) {
         selectedRoles.add(roles.get(i));
       }
     }
     return (ProxyPentahoRole[])selectedRoles.toArray(new ProxyPentahoRole[0]);
   }
   
   public void setSelectedRole(ProxyPentahoRole role) {
     setSelectedRoles(new ProxyPentahoRole[]{role});
   }
   
   public void setSelectedRoles(ProxyPentahoRole[] roles) {
     ArrayList roleNames = new ArrayList();
     for (int i = 0; i < roles.length; i++) {
       roleNames.add(roles[i].getName());
     }
     int itemCount = getItemCount();
     for (int i = 0; i < itemCount; i++) {
       setItemSelected(i, roleNames.contains(getItemText(i)));
     }
     
   }
   
   public void removeSelectedRoles() {
     int numRolesDeleted = 0;
     int closingSelection = -1;
     int selectedIndex = -1;
     for (int i = getItemCount() - 1; i >= 0; i--) {
       if (isItemSelected(i)) {
         roles.remove(i);
         removeItem(i);
         numRolesDeleted++;
         selectedIndex = i;
       }
     }
     if (numRolesDeleted == 1) {
       if (selectedIndex >= getItemCount()) {
         selectedIndex = getItemCount() - 1;
       }
       if (selectedIndex >= 0) {
         setItemSelected(selectedIndex, true);
       }
     }
   }
   
   public void removeRoles(ProxyPentahoRole[] rolesToRemove) {
     int numRolesDeleted = 0;
     int selectedIndex = -1;
     for (int i = getItemCount() - 1; i >= 0; i--) {
       if (isItemSelected(i)) {
         selectedIndex = i;
       }
       for (int j = 0; j < rolesToRemove.length; j++) {
         if (roles.get(i).equals(rolesToRemove[j])) {
           roles.remove(i);
           removeItem(i);
           numRolesDeleted++;
         }
       }
     }
     if (numRolesDeleted == 1) {
       if (selectedIndex >= getItemCount()) {
         selectedIndex = getItemCount() - 1;
       }
       if (selectedIndex >= 0) {
         setItemSelected(selectedIndex, true);
       }
     }
   }
   
   public void refresh() {
     setRoles(new ProxyPentahoRole[0]);
     isInitialized = false;
     AsyncCallback callback = new AsyncCallback() {
       public void onSuccess(Object result) {
         setRoles((ProxyPentahoRole[])result);
         isInitialized = true;
       }
 
       public void onFailure(Throwable caught) {
        messageDialog.setText("Error Loading Roles");
         messageDialog.setMessage("Unable to refresh roles list: " + caught.getMessage());
         messageDialog.center();
       }
     };
     
     PacServiceFactory.getPacService().getRoles(callback);
   }
   
   public boolean addRole(ProxyPentahoRole role) {
     boolean result = false;
     if (!roles.contains(role)) {
       roles.add(role);
       addItem(role.getName());
       result = true;
     }
     return result;
   }
   
   public boolean isInitialized() {
     return isInitialized;
   }
   
   public void clearRolesCache() {
     setRoles(new ProxyPentahoRole[0]);
     isInitialized = false;
   }
 }
