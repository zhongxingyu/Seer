 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.admin;
 
 import org.mule.galaxy.web.client.AbstractFlowComposite;
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WGroup;
 import org.mule.galaxy.web.rpc.WPermission;
 import org.mule.galaxy.web.rpc.WPermissionGrant;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractGroupPanel extends AbstractFlowComposite {
 
     protected final ErrorPanel errorPanel;
     protected FlexTable table;
     private Collection permissions;
     private Button applyButton;
     private List rows;
     private Map groups2Permissions;
     private Button resetButton;
     protected final Galaxy galaxy;
     protected FlowPanel mainPanel;
     
     public AbstractGroupPanel(Galaxy galaxy, ErrorPanel errorPanel) {
         super();
         this.galaxy = galaxy;
         this.errorPanel = errorPanel;
         mainPanel = panel;
     }
 
     public void onShow() {
         // table.setStyleName("permission-grant-table");
         mainPanel.clear();
         mainPanel.add(new Label("Loading..."));
         
         AbstractCallback callback = new AbstractCallback(errorPanel) {
             public void onSuccess(Object permissions) {
                 receivePermissions((Collection) permissions);
             }
         };
         getPermissions(callback);
     }
 
     protected void receivePermissions(Collection permissions) {
         this.permissions = permissions;
         AbstractCallback callback = new AbstractCallback(errorPanel) {
             public void onSuccess(Object groups) {
                 receiveGroups((Map) groups);
             }
         };
         
         getGroupPermissionGrants(callback);
     }
 
     protected abstract void getPermissions(AbstractCallback callback);
 
     protected abstract void getGroupPermissionGrants(AbstractCallback callback);
     
     protected void receiveGroups(Map groups2Permissions) {
         this.groups2Permissions = groups2Permissions;
         mainPanel.clear();
         
         table = createTitledRowTable(mainPanel, "Manage Group Permissions");
         int col = 1;
         for (Iterator itr = permissions.iterator(); itr.hasNext();) {
             WPermission p = (WPermission)itr.next();
             table.setText(0, col, p.getDescription());
             
             col++;
         }   
         
         rows = new ArrayList();
         for (Iterator itr = groups2Permissions.keySet().iterator(); itr.hasNext();) {
             rows.add(((WGroup)itr.next()).getName());
         }
         Collections.sort(rows);
         
         for (Iterator itr = groups2Permissions.entrySet().iterator(); itr.hasNext();) {
             Map.Entry e = (Map.Entry)itr.next();
             
             final WGroup group = (WGroup) e.getKey();
 
             final Hyperlink hl = new Hyperlink(group.getName(), "groups/" + group.getId());
 
             int row = rows.indexOf(group.getName()) + 1;
             table.setWidget(row, 0, hl);
             
             Collection grants = (Collection) e.getValue();
             for (Iterator gItr = grants.iterator(); gItr.hasNext();) {
                 WPermissionGrant pg = (WPermissionGrant)gItr.next();
 
                 table.setWidget(row, getPermissionColumn(pg.getPermission()), createGrantWidget(pg));
             }
             row++;
         }
         
         table.getFlexCellFormatter().setColSpan(rows.size() + 1, 0, col);
         
         applyButton = new Button("Apply");
         applyButton.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 beginApply();
             }
         });
         
         resetButton = new Button("Reset");
         resetButton.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 reset();
             }
         });
         
         table.setWidget(rows.size() + 1, 0, asHorizontal(applyButton, resetButton));
         
     }
 
     /**
      * Go back to the saved state on the server.
      */
     protected void reset() {
         onShow();
     }
 
     /**
      * Update the permission map and then save it.
      */
     protected void beginApply() {
         setEnabled(false);
         for (Iterator itr = groups2Permissions.entrySet().iterator(); itr.hasNext();) {
             Map.Entry e = (Map.Entry)itr.next();
             
             WGroup g = (WGroup) e.getKey();
             Collection permGrants = (Collection) e.getValue();
             
             int row = rows.indexOf(g.getName());
             
             for (Iterator pgItr = permGrants.iterator(); pgItr.hasNext();) {
                 WPermissionGrant pg = (WPermissionGrant)pgItr.next();
                 
                 int col = getPermissionColumn(pg.getPermission());
                 
                 setGrant(row, col, pg);
             }
         }
         
         AbstractCallback callback = new AbstractCallback(errorPanel) {
             
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
                 setEnabled(true);
             }
 
             public void onSuccess(Object arg0) {
                 setEnabled(true);
             }
         };
         applyPermissions(groups2Permissions, callback);
     }
 
     protected abstract void applyPermissions(Map groups2Permissions, AbstractCallback callback);
 
     protected abstract void setGrant(int row, int col, WPermissionGrant pg);
 
     protected void setEnabled(boolean e) {
         applyButton.setEnabled(e);
         resetButton.setEnabled(e);
     }
 
     private int getPermissionColumn(String permission) {
         int i = 1;
         for (Iterator itr = permissions.iterator(); itr.hasNext();) {
             WPermission p = (WPermission)itr.next();
             
             if (p.getName().equals(permission)) {
                 return i;
             }
             i++;
         }
         return -1;
     }
 
     protected abstract Widget createGrantWidget(WPermissionGrant pg);
 
 }
