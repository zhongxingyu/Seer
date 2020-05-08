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
 
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.SecurityService;
 import org.mule.galaxy.web.rpc.SecurityServiceAsync;
 import org.mule.galaxy.web.rpc.WPermissionGrant;
 
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Map;
 
 public class GroupListPanel extends AbstractGroupPanel {
 
     private final AdministrationPanel adminPanel;
 
     public GroupListPanel(AdministrationPanel a) {
         super(a.getGalaxy(), a);
         this.adminPanel = a;
        a.getGalaxy().createPageInfo("groups/*", new GroupForm(adminPanel), a.getGalaxy().getAdminTab());
     }
     
     protected void setGrant(int row, int col, WPermissionGrant pg) {
         CheckBox cb = (CheckBox) table.getWidget(row + 1, col);
         
         if (cb.isChecked()) {
             pg.setGrant(WPermissionGrant.GRANTED);
         } else {
             pg.setGrant(WPermissionGrant.REVOKED);
         }
     }
     
     protected Widget createGrantWidget(WPermissionGrant pg) {
         CheckBox cb = new CheckBox();
         if (pg.getGrant() == WPermissionGrant.GRANTED) {
             cb.setChecked(true);
         }
         return cb;
     }
     
     protected void getPermissions(AbstractCallback callback) {
         getSecurityService().getPermissions(SecurityService.GLOBAL_PERMISSIONS, callback);
     }
 
     private SecurityServiceAsync getSecurityService() {
         return adminPanel.getSecurityService();
     }
 
     protected void getGroupPermissionGrants(AbstractCallback callback) {
         getSecurityService().getGroupPermissions(callback);
     }
 
     protected void applyPermissions(Map groups2Permissions, AbstractCallback callback) {
         getSecurityService().applyPermissions(groups2Permissions, callback);
     }
 
 }
