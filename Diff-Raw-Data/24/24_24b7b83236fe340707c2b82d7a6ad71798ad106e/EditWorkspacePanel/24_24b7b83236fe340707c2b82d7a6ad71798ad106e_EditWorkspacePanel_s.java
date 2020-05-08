 package org.mule.galaxy.web.client.workspace;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.mule.galaxy.web.client.AbstractTitledComposite;
 import org.mule.galaxy.web.client.RegistryPanel;
 import org.mule.galaxy.web.client.WorkspacePanel;
 import org.mule.galaxy.web.client.util.ColumnView;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.WorkspacesListBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WWorkspace;
 
 public class EditWorkspacePanel extends AbstractTitledComposite {
 
     private TextBox workspaceTextBox;
     private RegistryPanel registryPanel;
     private boolean edit;
     private ListBox lifecyclesLB;
     private final WWorkspace workspace;
     private FlowPanel panel;
     private Collection workspaces;
     private String parentWorkspaceId;
 
     public EditWorkspacePanel(final RegistryPanel registryPanel,
                               final Collection workspaces,
                               final String parentWorkspaceId) {
         this(registryPanel, workspaces, new WWorkspace(), false);
         
         this.parentWorkspaceId = parentWorkspaceId;
     }
 
     public EditWorkspacePanel(final RegistryPanel registryPanel,
                               final Collection workspaces,
                               final String parentWorkspaceId,
                               final WWorkspace workspace) {
         this(registryPanel, workspaces, workspace, true);
         
         this.parentWorkspaceId = parentWorkspaceId;
     }
     
     protected EditWorkspacePanel(final RegistryPanel registryPanel,
                                  final Collection workspaces,
                                  final WWorkspace workspace,
                                  boolean edit) {
         super();
         this.workspace = workspace;
         this.edit = edit;
         this.registryPanel = registryPanel;
         this.workspaces = workspaces;
         
         panel = new FlowPanel();
         
         initWidget(panel);
     }
     
     public void onShow() {
         panel.clear();
         
         final FlexTable table = createColumnTable();
         
         final WorkspacesListBox workspacesLB = 
             new WorkspacesListBox(workspaces, workspace.getId(), parentWorkspaceId, true);
         
         table.setText(0, 0, "Parent Workspace:");
         table.setWidget(0, 1, workspacesLB);
         
         table.setText(1, 0, "Workspace Name:");
         
         workspaceTextBox = new TextBox();
         table.setWidget(1, 1, workspaceTextBox);
         
         if (edit) {
             workspaceTextBox.setText(workspace.getName());
         }
 
         table.setText(2, 0, "Default Lifecycle:");
         registryPanel.getRegistryService().getLifecycles(new AbstractCallback(registryPanel) {
             public void onSuccess(Object o) {
                 loadLifecycles(table, (Collection) o);
             }
         });
         panel.add(table);
         
         InlineFlowPanel buttonPanel = new InlineFlowPanel();
         Button saveButton = new Button("Save");
         saveButton.addClickListener(new ClickListener() {

             public void onClick(Widget arg0) {
                 save(workspacesLB.getSelectedValue(), 
                      workspaceTextBox.getText());
                
             }
            
         });
         buttonPanel.add(saveButton);
         
         if (edit) {
             Button deleteButton = new Button("Delete");
             deleteButton.addClickListener(new ClickListener() {
 
                 public void onClick(Widget arg0) {
                     showDeleteDialog(workspace.getId());
                 }
                 
             });
             buttonPanel.add(deleteButton);
         }
         table.setWidget(3, 1, buttonPanel);
 
         if (edit) {
             setTitle("Edit Workspace " + workspace.getName());
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
             
             if (l.getId().equals(workspace.getDefaultLifecycleId())) {
                 lifecyclesLB.setSelectedIndex(lifecyclesLB.getItemCount()-1);
             }
         }
         
         table.setWidget(2, 1, lifecyclesLB);
     }
 
     protected void showDeleteDialog(String workspaceId) {
         final DeleteDialog popup = new DeleteDialog(this, workspaceId);
         popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
             public void setPosition(int offsetWidth, int offsetHeight) {
                 int left = (Window.getClientWidth() - offsetWidth) / 3;
                 int top = (Window.getClientHeight() - offsetHeight) / 3;
                 popup.setPopupPosition(left, top);
             }
         });
     }
 
     protected void save(String parentWorkspaceId, final String text) {
         AbstractCallback callback = new AbstractCallback(registryPanel) {
 
             public void onSuccess(Object arg0) {
                 registryPanel.setMain(new WorkspacePanel(registryPanel));
                 registryPanel.onShow();
             }
             
         };
         
         String lifecycleId = null;
         int idx = lifecyclesLB.getSelectedIndex();
         if (idx > 0) {
             lifecycleId = lifecyclesLB.getValue(idx);
         }
         
         if (edit) {
             registryPanel.getRegistryService().updateWorkspace(workspace.getId(), 
                                                                parentWorkspaceId,
                                                                text, 
                                                                lifecycleId,
                                                                callback);
         } else {
             registryPanel.getRegistryService().addWorkspace(parentWorkspaceId, text, lifecycleId, callback);
         }
     }
     protected void delete(String workspaceId2) {
         registryPanel.getRegistryService().deleteWorkspace(workspaceId2, new AbstractCallback(registryPanel) {
 
             public void onSuccess(Object arg0) {
                 registryPanel.refreshWorkspaces();
                 registryPanel.setMain(new WorkspacePanel(registryPanel));
                 registryPanel.setMessage("Workspace was deleted.");
             }
             
         });
     }
 
     private static class DeleteDialog extends DialogBox {
 
         public DeleteDialog(final EditWorkspacePanel panel, final String workspaceId) {
           // Set the dialog box's caption.
           setText("Are you sure you want to delete this workspace and all it's artifacts?");
 
           InlineFlowPanel buttonPanel = new InlineFlowPanel();
 
           Button no = new Button("No");
           no.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 DeleteDialog.this.hide();
             }
           });
           
           Button yes = new Button("Yes");
           yes.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 DeleteDialog.this.hide();
                 panel.delete(workspaceId);
             }
           });
           buttonPanel.add(no);
           buttonPanel.add(yes);
           
           setWidget(buttonPanel);
         }
       }
 }
