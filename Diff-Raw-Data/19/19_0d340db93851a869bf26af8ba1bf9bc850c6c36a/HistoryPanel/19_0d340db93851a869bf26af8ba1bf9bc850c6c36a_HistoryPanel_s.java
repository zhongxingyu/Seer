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
 
 package org.mule.galaxy.web.client.artifact;
 
 import org.mule.galaxy.web.client.AbstractComposite;
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.util.ExternalHyperlink;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.ArtifactVersionInfo;
 import org.mule.galaxy.web.rpc.ExtendedArtifactInfo;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.TransitionResponse;
 
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
     private ExtendedArtifactInfo info;
     private final ErrorPanel errorPanel;
 
     public HistoryPanel(Galaxy galaxy,
                         ErrorPanel errorPanel,
                         ExtendedArtifactInfo info) {
         super();
         this.errorPanel = errorPanel;
         this.registryService = galaxy.getRegistryService();
         this.info = info;
         
         panel = new FlowPanel();
         initWidget(panel);
         
         setTitle("Artifact History");
         initializePanel();
     }
 
     protected void initializePanel() {
         for (Iterator iterator = info.getVersions().iterator(); iterator.hasNext();) {
             final ArtifactVersionInfo av = (ArtifactVersionInfo)iterator.next();
             
             FlowPanel avPanel = new FlowPanel();
             avPanel.setStyleName("artifact-version-panel");
 
             Label title = new Label("Version " + av.getVersionLabel());
             title.setStyleName("artifact-version-title");
             avPanel.add(title);
             
             FlowPanel bottom = new FlowPanel();
             avPanel.add(bottom);
             bottom.setStyleName("artifact-version-bottom-panel");
             
             bottom.add(new Label("By " + av.getAuthorName() 
                 + " (" + av.getAuthorUsername() + ") on " + av.getCreated()));
             
             InlineFlowPanel links = new InlineFlowPanel();
             bottom.add(links);
             
             Hyperlink viewLink = new Hyperlink("View", "view-version");
             viewLink.addClickListener(new ClickListener() {
 
                 public void onClick(Widget arg0) {
                     Window.open(av.getLink(), null, "scrollbars=yes");
                 }
                 
             });
             viewLink.addStyleName("hyperlink-NewWindow");
             links.add(viewLink);
 
             links.add(new Label(" | "));
 
             ExternalHyperlink permalink = new ExternalHyperlink("Permalink", av.getLink());
             permalink.setTitle("Direct artifact link for inclusion in email, etc.");
             links.add(permalink);
             
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
             
             panel.add(avPanel);
         }
     }
 
     protected void setDefault(String versionId) {
         registryService.setDefault(versionId, new AbstractCallback(errorPanel) {
 
             public void onSuccess(Object o) {
                 TransitionResponse tr = (TransitionResponse) o;
                 
                 if (tr.isSuccess()) {
                     History.newItem("artifact_" + info.getId() + "_2");
                 } else {
                     displayMessages(tr);
                 }
             }
 
         });
     }
 
     protected void setEnabled(String versionId, boolean enabled) {
         registryService.setEnabled(versionId, enabled, new AbstractCallback(errorPanel) {
 
             public void onSuccess(Object o) {
                 TransitionResponse tr = (TransitionResponse) o;
                 
                 if (tr == null || tr.isSuccess()) {
                     History.newItem("artifact_" + info.getId() + "_2");
                 } else {
                     displayMessages(tr);
                 }
             }
 
         });
     }
     
     protected void displayMessages(TransitionResponse tr) {
         errorPanel.setMessage("Policies were not met!");
     }
 
     
 
 }
