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
 
 package org.mule.galaxy.web.client.entry;
 
 import org.mule.galaxy.web.client.AbstractComposite;
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.admin.PolicyPanel;
 import org.mule.galaxy.web.client.util.ConfirmDialog;
 import org.mule.galaxy.web.client.util.ConfirmDialogAdapter;
 import org.mule.galaxy.web.client.util.ExternalHyperlink;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.LightBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.EntryVersionInfo;
 import org.mule.galaxy.web.rpc.ExtendedEntryInfo;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.WPolicyException;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Iterator;
 
 public class HistoryPanel extends AbstractComposite {
 
     private RegistryServiceAsync registryService;
     private FlowPanel panel;
     private ExtendedEntryInfo info;
     private final ErrorPanel errorPanel;
     private final Galaxy galaxy;
 
     public HistoryPanel(Galaxy galaxy,
                         ErrorPanel errorPanel,
                         ExtendedEntryInfo info) {
         super();
         this.galaxy = galaxy;
         this.errorPanel = errorPanel;
         this.registryService = galaxy.getRegistryService();
         this.info = info;
 
         panel = new FlowPanel();
         initWidget(panel);
 
         setTitle("Artifact History");
         initializePanel();
     }
 
     protected void initializePanel() {
         for (Iterator<EntryVersionInfo> iterator = info.getVersions().iterator(); iterator.hasNext();) {
             final EntryVersionInfo av = iterator.next();
 
             FlowPanel avPanel = new FlowPanel();
             avPanel.setStyleName("artifact-version-panel");
 
             Label title = new Label("Version " + av.getVersionLabel());
             title.setStyleName("artifact-version-title");
             avPanel.add(title);
 
             FlowPanel bottom = new FlowPanel();
             avPanel.add(bottom);
             bottom.setStyleName("artifact-version-bottom-panel");
 
             if (av.getAuthorName() != null) {
                 bottom.add(new Label("By " + av.getAuthorName()
                         + " (" + av.getAuthorUsername() + ") on " + av.getCreated()));
             }
             
             InlineFlowPanel links = new InlineFlowPanel();
             bottom.add(links);
 
             if (info.isArtifact()) {
                 Hyperlink viewLink = new Hyperlink("View", "view-version");
                 viewLink.addClickListener(new ClickListener() {
 
                     public void onClick(Widget arg0) {
                         Window.open(av.getLink(), null, "scrollbars=yes");
                     }
 
                 });
                 links.add(viewLink);
 
                 links.add(new Label(" | "));
 
                 ExternalHyperlink permalink = new ExternalHyperlink("Permalink", av.getLink());
                 permalink.setTitle("Direct artifact link for inclusion in email, etc.");
                 links.add(permalink);
             }
 
             if (!av.isDefault()) {
                 links.add(new Label(" | "));
 
                 Hyperlink rollbackLink = new Hyperlink("Set Default", "rollback-version");
                 rollbackLink.addClickListener(new ClickListener() {
 
                     public void onClick(Widget w) {
                         setDefault(av.getId());
                     }
 
                 });
                 links.add(rollbackLink);
             }
 
             links.add(new Label(" | "));
 
             if (!av.isEnabled()) {
                 Hyperlink enableLink = new Hyperlink("Reenable", "reenable-version");
                 enableLink.addClickListener(new ClickListener() {
 
                     public void onClick(Widget w) {
                         setEnabled(av.getId(), true);
                     }
 
                 });
                 links.add(enableLink);
             } else {
                 Hyperlink disableLink = new Hyperlink("Disable", "disable-version");
                 disableLink.addClickListener(new ClickListener() {
 
                     public void onClick(Widget w) {
                         setEnabled(av.getId(), false);
                     }
 
                 });
                 links.add(disableLink);
             }
 
             links.add(new Label(" | "));
             Hyperlink deleteLink = new Hyperlink("Delete", "delete-version");
             deleteLink.addClickListener(new ClickListener() {
 
                 public void onClick(Widget w) {
                     warnDelete(av.getId());
                 }
 
             });
             links.add(deleteLink);
 
             panel.add(avPanel);
         }
 
     }
 
     protected void warnDelete(final String id) {
         new LightBox(new ConfirmDialog(new ConfirmDialogAdapter() {
             public void onConfirm() {
                 delete(id);
             }
         }, "Are you sure you want to delete this artifact version?")).show();
     }
 
     protected void delete(String versionId) {
 
         registryService.deleteArtifactVersion(versionId, new AbstractCallback(errorPanel) {
 
             public void onSuccess(Object o) {
                 galaxy.setMessageAndGoto("browse", "Artifact version was deleted.");
 
                 // is it the last version?
                 if (((Boolean) o).booleanValue()) {
                     History.newItem("browse");
                 } else {
                     History.newItem("artifact/" + info.getId());
                 }
             }
 
         });
     }
 
     protected void setDefault(String versionId) {
         registryService.setDefault(versionId, getPolicyCallback());
     }
 
     protected void setEnabled(String versionId, boolean enabled) {
         registryService.setEnabled(versionId, enabled, getPolicyCallback());
     }
 
     private AbstractCallback getPolicyCallback() {
         return new AbstractCallback(errorPanel) {
 
             public void onFailure(Throwable caught) {
                 if (caught instanceof WPolicyException) {
                     PolicyPanel.handlePolicyFailure(galaxy, (WPolicyException) caught);
                 } else {
                     super.onFailure(caught);
                 }
             }
 
             public void onSuccess(Object o) {
                History.newItem("artifact/" + info.getId() + "_2");
             }
 
         };
     }
 }
