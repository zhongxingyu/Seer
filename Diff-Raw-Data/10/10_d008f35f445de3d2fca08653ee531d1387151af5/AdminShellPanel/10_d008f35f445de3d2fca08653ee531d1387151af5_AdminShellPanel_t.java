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
 
 import org.mule.galaxy.web.client.util.ConfirmDialog;
 import org.mule.galaxy.web.client.util.ConfirmDialogAdapter;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.LightBox;
 import org.mule.galaxy.web.client.validation.StringNotEmptyValidator;
 import org.mule.galaxy.web.client.validation.ui.ValidatableTextBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WScript;
 
 import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasAlignment;
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.TreeListener;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class AdminShellPanel extends AbstractAdministrationComposite
         implements ClickListener, KeyboardListener {
 
 
     private Tree scriptTree;
     private FlexTable table;
     private Button evaluateBtn;
     private CheckBox saveAsCB;
     private ValidatableTextBox saveAsTB;
     private Button saveBtn;
     private Button deleteBtn;
     private Button clearBtn;
     private Button cancelBtn;
     private TextArea scriptArea;
     private Label scriptResultsLabel;
 
     public AdminShellPanel(AdministrationPanel a) {
         super(a);
         initLocalWidgets();
     }
 
 
     private void initLocalWidgets() {
         // create objects, set initial state including listeners
         // display existing scripts in a tree
         scriptTree = new Tree();
 
         // main page layout
         table = new FlexTable();
 
         saveBtn = new Button("Save");
         saveBtn.addClickListener(this);
 
         cancelBtn = new Button("Cancel");
         cancelBtn.addClickListener(this);
 
         deleteBtn = new Button("Delete");
         deleteBtn.addClickListener(this);
 
         clearBtn = new Button("Clear");
         clearBtn.addClickListener(this);
 
         evaluateBtn = new Button("Evaluate");
         evaluateBtn.addClickListener(this);
 
         saveAsCB = new CheckBox("Save As... ");
         saveAsCB.addClickListener(this);
 
         saveAsTB = new ValidatableTextBox(new StringNotEmptyValidator());
         saveAsTB.getTextBox().setEnabled(false);
 
         // where the scripts are pasted into
         scriptArea = new TextArea();
         scriptArea.setCharacterWidth(80);
         scriptArea.setVisibleLines(30);
 
         scriptResultsLabel = new Label();
     }
 
     public void onShow() {
         super.onShow();
 
         scriptArea.setText(null);
 
         // text area to paste script into
         table.setWidget(0, 0, createPrimaryTitle("Galaxy Admin Shell"));
         table.setWidget(1, 0, new Label("Type or paste a Groovy script to be executed on the server. A return value will be displayed below the area. " +
                 "Tips: \n   Spring's context is available as 'applicationContext' variable" +
                 "\n   only String return values are supported (or null)"));
         table.getFlexCellFormatter().setColSpan(1, 0, 2);
         table.getCellFormatter().setWidth(1, 0, "500px");
         table.setWidget(2, 0, scriptArea);
 
         this.createScriptTree();
         VerticalPanel vp = new VerticalPanel();
         vp.add(createTitleText("Saved Scripts"));
         vp.add(scriptTree);
 
         table.setWidget(2, 1, vp);
         table.getCellFormatter().setVerticalAlignment(2, 1, HasAlignment.ALIGN_TOP);
         table.getCellFormatter().setHorizontalAlignment(2, 1, HasAlignment.ALIGN_LEFT);
 
         // script results
         FlowPanel scriptOutputPanel = new FlowPanel();
         scriptResultsLabel.setWordWrap(false);
         scriptOutputPanel.add(scriptResultsLabel);
 
         // results of script execution
         table.setWidget(4, 0, scriptOutputPanel);
 
         // add user control buttons
         InlineFlowPanel buttons = new InlineFlowPanel();
         buttons.add(evaluateBtn);
         buttons.add(saveAsCB);
         buttons.add(saveAsTB);
         buttons.add(saveBtn);
         buttons.add(deleteBtn);
         buttons.add(clearBtn);
         buttons.add(cancelBtn);
 
         table.setWidget(3, 0, buttons);
 
         panel.add(table);
         saveBtn.setEnabled(true);
     }
 
 
     protected void createScriptTree() {
         scriptTree = new Tree();
         scriptTree.addTreeListener(new TreeListener() {
             public void onTreeItemSelected(TreeItem ti) {
                 WScript ws = (WScript) ti.getUserObject();
                 scriptArea.setText(ws.getScript());
             }
 
             public void onTreeItemStateChanged(TreeItem ti) {
             }
         });
 
         adminPanel.getGalaxy().getAdminService().getScripts(new AbstractCallback(adminPanel) {
             @SuppressWarnings("unchecked")
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
             }
 
             @SuppressWarnings("unchecked")
             public void onSuccess(Object o) {
                 addTreeItems((List<WScript>) o);
             }
         });
     }
 
     private void addTreeItems(List<WScript> scripts) {
         for (Iterator<WScript> itr = scripts.iterator(); itr.hasNext();) {
             WScript s = itr.next();
             TreeItem treeItem = scriptTree.addItem(s.getName());
             treeItem.setUserObject(s);
         }
     }
 
 
     protected void refresh() {
         onShow();
         saveAsCB.setChecked(false);
         saveAsTB.setText(null);
     }
 
 
     // only use one listener for all events. It's less overhead and easier to read
     public void onClick(Widget sender) {
         if (sender == saveBtn) {
             save();
         }
 
         if (sender == deleteBtn) {
             final ConfirmDialog dialog = new ConfirmDialog(new ConfirmDialogAdapter() {
                 public void onConfirm() {
                     delete();
                 }
 
             }, "Are you sure you want to delete this script?");
             new LightBox(dialog).show();
         }
 
         if (sender == saveAsCB) {
             saveAsTB.getTextBox().setEnabled(true);
             if (saveAsCB.isChecked()) {
                 saveAsTB.getTextBox().setFocus(true);
             }
         }
 
         if (sender == clearBtn) {
             scriptArea.setText(null);
            scriptTree.setSelectedItem(null);
         }
 
         if (sender == cancelBtn) {
             History.back();
         }
 
         if (sender == evaluateBtn) {
             evaluateBtn.setEnabled(false);
             adminPanel.getGalaxy().getAdminService().executeScript(scriptArea.getText(), new AbstractCallback(adminPanel) {
                 public void onFailure(Throwable caught) {
                     evaluateBtn.setEnabled(true);
                     scriptResultsLabel.setText("");
                     super.onFailure(caught);
                 }
 
                 public void onSuccess(Object o) {
                     adminPanel.clearErrorMessage();
                     evaluateBtn.setEnabled(true);
                     scriptResultsLabel.setText(o == null ? "No value returned" : o.toString());
                 }
             });
 
         }
 
     }
 
 
     private void save() {
        if (scriptTree.getSelectedItem() == null) {
            // TODO use validatable textarea instead
            Window.alert("Please select a script to save or give it a new name.");
            return;
        }

         saveBtn.setEnabled(false);
         WScript ws = new WScript();
 
         // try and get it from the tree first
         final TreeItem ti = scriptTree.getSelectedItem();
         if (ti != null) {
             ws = (WScript) ti.getUserObject();
         }
 
         if (saveAsCB.isChecked()) {
             ws.setName(saveAsTB.getText());
             // save as should null out the Id so it creates a new copy
             ws.setId(null);
         }
         ws.setScript(scriptArea.getText());
         // a local ref to satisfy anonymous inner class requirements
         final WScript localCopyWs = ws;
         adminPanel.getGalaxy().getAdminService().save(ws, new AbstractCallback(adminPanel) {
             public void onFailure(Throwable caught) {
                 saveBtn.setEnabled(true);
                 super.onFailure(caught);
             }
 
             public void onSuccess(Object o) {
                 saveBtn.setEnabled(true);
                 adminPanel.setMessage("Script '" + localCopyWs.getName() + "' has been saved");
                 refresh();
                 // if it was not a New script, redisplay it in the window.
                 if (ti != null) {
                     scriptTree.setSelectedItem(ti, true);
                 }
             }
         });
 
 
     }
 
 
     private void delete() {
         TreeItem ti = scriptTree.getSelectedItem();
         WScript wsx = (WScript) ti.getUserObject();
 
         deleteBtn.setEnabled(false);
         adminPanel.getGalaxy().getAdminService().deleteScript(wsx.getId(), new AbstractCallback(adminPanel) {
             public void onFailure(Throwable caught) {
                 deleteBtn.setEnabled(true);
                 super.onFailure(caught);
             }
 
             public void onSuccess(Object o) {
                 deleteBtn.setEnabled(true);
                 scriptArea.setText(null);
                 adminPanel.setMessage("Script Deleted");
                 refresh();
             }
         });
     }
 
     public void onKeyPress(Widget widget, char keyCode, int modifiers) {
         if ((keyCode == KEY_ENTER) && (modifiers == 0)) {
             save();
         }
     }
 
 
     public void onKeyDown(Widget widget, char c, int i) {
     }
 
     public void onKeyUp(Widget widget, char c, int i) {
     }
 }
