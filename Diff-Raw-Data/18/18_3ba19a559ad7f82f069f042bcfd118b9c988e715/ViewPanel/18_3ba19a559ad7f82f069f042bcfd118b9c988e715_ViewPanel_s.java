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
 
 package org.mule.galaxy.web.client.registry;
 
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.util.ConfirmDialog;
 import org.mule.galaxy.web.client.util.ConfirmDialogAdapter;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.validation.StringNotEmptyValidator;
 import org.mule.galaxy.web.client.validation.ui.ValidatableTextBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WArtifactView;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.List;
 
 import org.gwtwidgets.client.ui.LightBox;
 
 /**
  * Shows/Edits an ArtifactView. 
  */
 public class ViewPanel extends AbstractBrowsePanel {
 
     /**
      * This is the id new views have before being persisted.
      */
     protected static final String NEW_VIEW_ID = "new";
     
     private String viewId;
     private SearchForm searchForm;
     private ValidatableTextBox nameTB;
     private WArtifactView view;
     private CheckBox sharedCB;
     protected Button delete;
     protected Button cancel;
     private FlowPanel editPanel;
     private boolean editMode;
     private Hyperlink editLink;
 
     public ViewPanel(Galaxy galaxy) {
         super(galaxy, false);
     }
     
     protected RegistryMenuPanel createRegistryMenuPanel() {
         return new RegistryMenuPanel(galaxy, true, true) {
 
             public void loadViews() {
                 loadViews(viewId, null);
             }
             
         };
     }
     
     protected String getHistoryToken() {
         return "view_" + viewId;
     }
 
     public void onShow(List params) {
         if (params.size() > 0) {
             viewId = (String) params.get(0);
         }
 
         editPanel = new InlineFlowPanel();
         currentTopPanel = editPanel;
         
         if (params.size() > 1) {
             editMode = "edit".equals((String) params.get(1));
         } else {
             editMode = false;
         }
         
         FlowPanel browseToolbar = new FlowPanel();
         browseToolbar.setStyleName("toolbar");
         
         final ViewPanel viewPanel = this;
         searchForm = new SearchForm(galaxy, "Save", false) {
 
             protected void initializeButtons(FlowPanel buttonPanel, String searchText) {
                 super.initializeButtons(buttonPanel, searchText);
 
                 buttonPanel.remove(buttonPanel.getWidgetCount()-1);
                 delete = new Button();
                 delete.setText("Delete");
                 delete.addClickListener(new ClickListener() {
                     public void onClick(Widget arg0) {
                         viewPanel.delete();
                     }
                 });
 
                 buttonPanel.add(delete);
 
                 cancel = new Button();
                 cancel.setText("Cancel");
                 cancel.addClickListener(new ClickListener() {
                     public void onClick(Widget arg0) {
                         if (NEW_VIEW_ID.equals(viewId)) {
                             History.back();
                         } else {
                             // Browse back to the view
                             History.newItem("view_" + viewId);
                         }
 
                     }
                 });
                 buttonPanel.add(cancel);
             }
 
             protected void initializeFields(FlexTable table) {
                 if (NEW_VIEW_ID.equals(viewId)) {
                     panel.add(createPrimaryTitle("New View"));
                 } else {
                     panel.add(createPrimaryTitle("Edit View"));
                 }
                 
                 nameTB = new ValidatableTextBox(new StringNotEmptyValidator());
                 nameTB.getTextBox().setVisibleLength(25);
                 table.setText(0, 0, "View Name: ");
                 table.setWidget(0, 1, nameTB);
 
                 sharedCB = new CheckBox("Shared");
                 table.getFlexCellFormatter().setColSpan(1, 0, 2);
                 table.setWidget(1, 0, sharedCB);
                 
                 super.initializeFields(table);
             }
             
         };
         
         searchForm.addSearchListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 if (!validate()) {
                     return;
                 }
                 save();
             }
         });
         
         if (NEW_VIEW_ID.equals(viewId)) {
            if (artifactListPanel != null) {
                artifactListPanel.clear();
            }
             view = new WArtifactView();
             showSearchForm();
         }
         
         super.onShow(params);
     }
 
     private void loadView() {
         galaxy.getRegistryService().getArtifactView(viewId, new AbstractCallback(this) {
             public void onSuccess(Object o) {
                 view = (WArtifactView) o;
                 nameTB.getTextBox().setText(view.getName());
                 sharedCB.setChecked(view.isShared());
                 
                 editPanel.clear();
                 editPanel.add(createPrimaryTitle(view.getName()));
                 editPanel.add(new Label(" "));
                 editLink = new Hyperlink("Edit", "view_" + viewId + "_edit");
                 editPanel.add(editLink);
                 searchForm.setPredicates(view.getPredicates());
                 searchForm.setWorkspace(view.getWorkspace());
                 searchForm.setWorkspaceSearchRecursive(view.isWorkspaceSearchRecursive());
                 
                 if (editMode) {
                     showSearchForm();
                 }
             }
         });
     }
     
     protected void delete()
     {
         new LightBox(new ConfirmDialog(new ConfirmDialogAdapter()
         {
             public void onConfirm()
             {
                galaxy.getRegistryService().deleteArtifactView(viewId, new AbstractCallback(menuPanel)
                 {
                     public void onSuccess(Object arg0)
                     {
                        galaxy.setMessageAndGoto("browse", "View was deleted.");
                     }
                 });
             }
         }, "Are you sure you want to delete this view?")).show();
     }
 
 
     public void refresh() {
         if (!editMode || NEW_VIEW_ID.equals(viewId)) {
             refreshArtifacts();
         } 
 
         loadView();
         
         menuPanel.loadViews(viewId, null);
     }
     
     protected void save() {
         view.setName(nameTB.getTextBox().getText());
         view.setShared(sharedCB.isChecked());
         view.setPredicates(searchForm.getPredicates());
         view.setWorkspace(searchForm.getWorkspacePath());
         view.setWorkspaceSearchRecursive(searchForm.isWorkspaceSearchRecursive());
         
         galaxy.getRegistryService().saveArtifactView(view, new AbstractCallback(menuPanel) {
             public void onSuccess(Object id) {
                 view.setId((String)id);
                 History.newItem("view_" + id);
             }
         });
         
         menuPanel.setTop(editPanel);
     }
 
     protected boolean validate() {
         boolean isOk = true;
 
         isOk &= nameTB.validate();
 
         return isOk;
     }
 
     private void showSearchForm() {
         currentTopPanel = new FlowPanel();
         currentTopPanel.add(searchForm);
         menuPanel.setTop(currentTopPanel);
     }
     
     protected void fetchArtifacts(int resultStart, int maxResults, AbstractCallback callback) {
         if (NEW_VIEW_ID.equals(viewId)) {
             return;
         }
         
         galaxy.getRegistryService().getArtifactsForView(viewId, resultStart, maxResults, callback);
     }
 }
