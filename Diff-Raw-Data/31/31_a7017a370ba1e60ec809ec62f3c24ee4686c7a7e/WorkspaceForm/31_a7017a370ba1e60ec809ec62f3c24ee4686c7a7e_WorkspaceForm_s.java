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
 
 package org.mule.galaxy.web.client.workspace;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.mule.galaxy.web.client.AbstractErrorShowingComposite;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.registry.RegistryMenuPanel;
 import org.mule.galaxy.web.client.util.ConfirmDialog;
 import org.mule.galaxy.web.client.util.ConfirmDialogAdapter;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.LightBox;
 import org.mule.galaxy.web.client.util.WorkspaceOracle;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.ItemExistsException;
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WWorkspace;
 
 
 public class WorkspaceForm extends AbstractErrorShowingComposite {
 
     private TextBox workspaceTextBox;
     private Galaxy galaxy;
     private boolean edit;
     private ListBox lifecyclesLB;
     private FlowPanel panel;
     private String parentWorkspacePath;
     private String workspaceName;
     private String workspaceId;
     private String lifecycleId;
     private RegistryMenuPanel menuPanel;
     private SuggestBox workspacesSuggest;
 
     /**
      * Set up the form for adding a workspace.
      * @param galaxy
      */
     public WorkspaceForm(final Galaxy galaxy) {
         this.galaxy = galaxy;
         this.edit = false;
         
         panel = new FlowPanel();
         menuPanel = new RegistryMenuPanel(galaxy);
         menuPanel.setMain(panel);
         
         initWidget(menuPanel);
     }
     
     /**
      * Set up the form for editing a workspace.
      */
     public WorkspaceForm(Galaxy galaxy, 
                          WWorkspace workspace) {
         this.galaxy = galaxy;
         this.edit = true;
         this.workspaceId = workspace.getId();
         this.parentWorkspacePath = workspace.getParentPath();
         this.workspaceName = workspace.getName();
         this.lifecycleId = workspace.getDefaultLifecycleId();
         
         panel = new FlowPanel();
         
         initWidget(panel);
     }
 
     public void onShow(List<String> params) {
         panel.clear();
         if (menuPanel != null) {
             menuPanel.onShow();
         }
         if (params.size() > 0 && !edit) {
             parentWorkspacePath = params.get(0);
         }
         
         if (!edit) {
             panel.add(createPrimaryTitle("Add Workspace"));
         }
         
         final FlexTable table = createColumnTable();
         workspacesSuggest = new SuggestBox(new WorkspaceOracle(galaxy, this, parentWorkspacePath));
         workspacesSuggest.setText(parentWorkspacePath);
         
         table.setText(0, 0, "Parent Workspace:");
         table.setWidget(0, 1, workspacesSuggest);
         
         table.setText(1, 0, "Workspace Name:");
         
         workspaceTextBox = new TextBox();
         table.setWidget(1, 1, workspaceTextBox);
         
         if (edit) {
             workspaceTextBox.setText(workspaceName);
         }
 
         table.setText(2, 0, "Default Lifecycle:");
         galaxy.getRegistryService().getLifecycles(new AbstractCallback(this) {
             public void onSuccess(Object o) {
                 loadLifecycles(table, (Collection) o);
             }
         });
         panel.add(table);
         
         InlineFlowPanel buttonPanel = new InlineFlowPanel();
         String saveTitle = edit ? "Save": "Add";
         Button saveButton = new Button(saveTitle);
         saveButton.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 save(workspacesSuggest.getText(), 
                      workspaceTextBox.getText());
             }
         });
         buttonPanel.add(saveButton);
         
         if (edit) {
             Button deleteButton = new Button("Delete");
             deleteButton.addClickListener(new ClickListener() {
 
                 public void onClick(Widget arg0) {
                     showDeleteDialog(workspaceId);
                 }
                 
             });
             buttonPanel.add(deleteButton);
         }
 
         Button cancel = new Button("Cancel");
         cancel.addClickListener(new ClickListener() {
             public void onClick(final Widget widget) {
                 History.back();
             }
         });
         buttonPanel.add(cancel);
         
         table.setWidget(3, 1, buttonPanel);
 
         if (edit) {
             setTitle("Edit Workspace " + workspaceName);
         } else {
             setTitle("Add Workspace");
         }
         
         styleHeaderColumn(table);
     }
 
     protected void loadLifecycles(FlexTable table, Collection lifecycles) {
         lifecyclesLB = new ListBox();
         lifecyclesLB.addItem("[Inherit From Parent]", "inherit");
         
         for (Iterator itr = lifecycles.iterator(); itr.hasNext();) {
             WLifecycle l = (WLifecycle)itr.next();
             
             lifecyclesLB.addItem(l.getName(), l.getId());
             
             if (l.getId().equals(lifecycleId)) {
                 lifecyclesLB.setSelectedIndex(lifecyclesLB.getItemCount()-1);
             }
         }
         
         table.setWidget(2, 1, lifecyclesLB);
     }
 
     protected void showDeleteDialog(final String workspaceId) {
         final ConfirmDialog dialog = new ConfirmDialog(new ConfirmDialogAdapter()
         {
             public void onConfirm()
             {
                 delete(workspaceId);
             }
 
         }, "Are you sure you want to delete this workspace and all its artifacts?");
         new LightBox(dialog).show();
 
     }
 
     protected void save(final String parentWorkspace, final String text) {
         AbstractCallback callback = new AbstractCallback(this) {
 
             @Override
             public void onFailure(Throwable caught) {
                if (caught instanceof ItemExistsException) {
                    setMessage("Could not find parent workspace: " + parentWorkspace);
                 } else {
                     super.onFailure(caught);
                 }
             }
 
             public void onSuccess(Object arg0) {
                 History.newItem("browse");
             }
             
         };
         
         String lifecycleId = null;
         int idx = lifecyclesLB.getSelectedIndex();
         if (idx > 0) {
             lifecycleId = lifecyclesLB.getValue(idx);
         }
         
         if (edit) {
             galaxy.getRegistryService().updateWorkspace(workspaceId, 
                                                         parentWorkspace,
                                                         text, 
                                                         lifecycleId,
                                                         callback);
         } else {
             galaxy.getRegistryService().addWorkspace(parentWorkspace, text, lifecycleId, callback);
         }
     }
     
     protected void delete(String workspaceId2) {
         galaxy.getRegistryService().delete(workspaceId2, new AbstractCallback(this) {
 
             public void onSuccess(Object arg0) {
                 galaxy.setMessageAndGoto("browse", "Workspace was deleted.");
             }
             
         });
     }
 
 }
