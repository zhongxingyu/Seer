 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 */
 package org.pentaho.pac.client.datasources;
 
 import org.pentaho.gwt.widgets.client.buttons.ImageButton;
 import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
 import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.i18n.Messages;
 import org.pentaho.pac.client.utils.ExceptionParser;
 import org.pentaho.pac.common.NameValue;
 import org.pentaho.pac.common.datasources.PentahoDataSource;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class DataSourcesPanel extends DockPanel implements ClickListener, ChangeListener {
 
   public static final String EMPTY_STRING = ""; //$NON-NLS-1$ 
   public static final int GENERAL_PANEL_ID = 0;
 
   public static final int ADVANCE_PANEL_ID = 1;
 
 //  TabPanel generalAdvanceDbPanel = new TabPanel();
 
   HTML msgBoxHtml = new HTML();
   PromptDialogBox messageDialog = new PromptDialogBox("", Messages.getString("ok"), null, false, true, msgBoxHtml);  //$NON-NLS-1$//$NON-NLS-2$
   DataSourcesList dataSourcesList = new DataSourcesList();
   DataSourceGeneralPanel dataSourceGeneralPanel = new DataSourceGeneralPanel();
   DataSourceAdvancePanel dataSourceAdvancePanel = new DataSourceAdvancePanel();
   DeckPanel deckPanel = new DeckPanel();
   ToggleButton generalButton = new ToggleButton(Messages.getString("general"), Messages.getString("general")); //$NON-NLS-1$ //$NON-NLS-2$
   ToggleButton advanceButton = new ToggleButton(Messages.getString("advance"), Messages.getString("advance")); //$NON-NLS-1$ //$NON-NLS-2$
   Button updateDataSourceBtn = new Button(Messages.getString("update")); //$NON-NLS-1$
   Button testDataSourceBtn = new Button(Messages.getString("test")); //$NON-NLS-1$
   
   ImageButton addDataSourceBtn = new ImageButton("style/images/add.png", "style/images/add_disabled.png", Messages.getString("addDataSource"), 15, 15); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   ImageButton deleteDataSourceBtn = new ImageButton("style/images/remove.png", "style/images/remove_disabled.png", Messages.getString("deleteDataSources"), 15, 15); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
   NewDataSourceDialogBox newDataSourceDialogBox = new NewDataSourceDialogBox();
   
   PromptDialogBox confirmDataSourceDeleteDialog = new PromptDialogBox(Messages.getString("deleteDataSources"), Messages.getString("ok") , Messages.getString("cancel"), false, true, new HTML(Messages.getString("confirmDataSourceDeletionMsg")));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
 
   public DataSourcesPanel() {
     HorizontalPanel footerPanel = new HorizontalPanel();
     footerPanel.add(testDataSourceBtn);
     footerPanel.add(updateDataSourceBtn);
     DockPanel dataSourcesListPanel = buildDataSourcesListPanel();
     DockPanel dataSourceDetailsDockPanel = buildDataSourceDetailsDockPanel();
 
     dataSourcesListPanel.setStyleName("borderPane"); //$NON-NLS-1$
     dataSourceDetailsDockPanel.setStyleName("borderPane"); //$NON-NLS-1$
     
     add(dataSourcesListPanel, DockPanel.WEST);
     add(dataSourceDetailsDockPanel, DockPanel.CENTER);
     
     
     add(footerPanel, DockPanel.SOUTH);
     setCellHorizontalAlignment(footerPanel, HasHorizontalAlignment.ALIGN_RIGHT);
     setSpacing(10);
     setCellWidth(dataSourcesListPanel, "30%"); //$NON-NLS-1$
     setCellWidth(dataSourceDetailsDockPanel, "70%"); //$NON-NLS-1$
     setCellHeight(dataSourcesListPanel, "100%"); //$NON-NLS-1$
     setCellHeight(dataSourceDetailsDockPanel, "100%"); //$NON-NLS-1$
     dataSourcesListPanel.setWidth("100%"); //$NON-NLS-1$
     dataSourcesListPanel.setHeight("100%"); //$NON-NLS-1$
     dataSourceDetailsDockPanel.setWidth("100%"); //$NON-NLS-1$
     dataSourceDetailsDockPanel.setHeight("100%"); //$NON-NLS-1$
     updateDataSourceBtn.setEnabled(false);
     testDataSourceBtn.setEnabled(false);
     newDataSourceDialogBox.setCallback(new IDialogCallback() {
       public void cancelPressed() {
       }
       public void okPressed() {
         if (newDataSourceDialogBox.isDataSourceCreated()) {
           PentahoDataSource dataSource = newDataSourceDialogBox.getDataSource();
           if (dataSourcesList.addDataSource(dataSource)) {
             dataSourcesList.setSelectedDataSource(dataSource);
             dataSourceSelectionChanged();
           }
         }
       }      
     });
     updateDataSourceBtn.addClickListener(this);
     testDataSourceBtn.addClickListener(this);
     confirmDataSourceDeleteDialog.setCallback(new IDialogCallback() {
       public void cancelPressed() {
       }
 
       public void okPressed() {
         confirmDataSourceDeleteDialog.hide();
         deleteSelectedDataSources();
       }      
     });
   }
 
   public DockPanel buildDataSourceDetailsDockPanel() {
     DockPanel dockPanel = new DockPanel();
 
     HorizontalPanel horizontalPanel = new HorizontalPanel();
     horizontalPanel.add(generalButton);
     horizontalPanel.add(advanceButton);
     dockPanel.add(horizontalPanel, DockPanel.NORTH);
     dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
     dockPanel.setSpacing(10);
     generalButton.setTitle(Messages.getString("clickEditGeneral")); //$NON-NLS-1$
     advanceButton.setTitle(Messages.getString("clickEditAdvance")); //$NON-NLS-1$
 
     generalButton.setStylePrimaryName("generalToggleBtn"); //$NON-NLS-1$
     advanceButton.setStylePrimaryName("advanceToggleBtn"); //$NON-NLS-1$
     deckPanel.add(dataSourceGeneralPanel);
     deckPanel.add(dataSourceAdvancePanel);
     
     dataSourceGeneralPanel.setWidth("100%"); //$NON-NLS-1$
     dataSourceGeneralPanel.setHeight("100%"); //$NON-NLS-1$
     dataSourceAdvancePanel.setWidth("100%"); //$NON-NLS-1$
     dataSourceAdvancePanel.setHeight("100%"); //$NON-NLS-1$
 
     dockPanel.add(deckPanel, DockPanel.CENTER);
     dockPanel.setCellWidth(deckPanel, "100%"); //$NON-NLS-1$
     dockPanel.setCellHeight(deckPanel, "100%"); //$NON-NLS-1$
 
     deckPanel.setWidth("100%"); //$NON-NLS-1$
     deckPanel.setHeight("100%"); //$NON-NLS-1$
     deckPanel.setStyleName("newDataSourceDialogBox.detailsPanel"); //$NON-NLS-1$
     deckPanel.showWidget(GENERAL_PANEL_ID);
     generalButton.setDown(true);
     advanceButton.setDown(false);
     generalButton.addClickListener(this);
     advanceButton.addClickListener(this);
 
     dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
     dataSourceGeneralPanel.getJndiNameTextBox().setReadOnly(true);
     return dockPanel;
   }
 
   public DockPanel buildDataSourcesListPanel() {
     DockPanel headerDockPanel = new DockPanel();
     headerDockPanel.add(deleteDataSourceBtn, DockPanel.EAST);
     VerticalPanel spacer = new VerticalPanel();
     spacer.setWidth("2"); //$NON-NLS-1$
     headerDockPanel.add(spacer, DockPanel.EAST);
     headerDockPanel.add(addDataSourceBtn, DockPanel.EAST);
     Label label = new Label(Messages.getString("dataSources")); //$NON-NLS-1$
     headerDockPanel.add(label, DockPanel.WEST);
     headerDockPanel.setCellWidth(label, "100%"); //$NON-NLS-1$
     DockPanel dataSourceListPanel = new DockPanel();
     dataSourceListPanel.add(headerDockPanel, DockPanel.NORTH);
     dataSourceListPanel.add(dataSourcesList, DockPanel.CENTER);
     dataSourceListPanel.setCellHeight(dataSourcesList, "100%"); //$NON-NLS-1$
     dataSourceListPanel.setCellWidth(dataSourcesList, "100%"); //$NON-NLS-1$
     dataSourceListPanel.setHeight("100%"); //$NON-NLS-1$
     dataSourceListPanel.setWidth("100%"); //$NON-NLS-1$
     dataSourcesList.setHeight("100%"); //$NON-NLS-1$
     dataSourcesList.setWidth("100%"); //$NON-NLS-1$
     deleteDataSourceBtn.setEnabled(false);
     dataSourcesList.addChangeListener(this);
     addDataSourceBtn.addClickListener(this);
     deleteDataSourceBtn.addClickListener(this);
     return dataSourceListPanel;
   }
 
   public void onClick(Widget sender) {
     if (sender == updateDataSourceBtn) {
       updateDataSourceDetails(sender);
     } else if (sender == testDataSourceBtn) {
       testDataSourceConnection();
     } else if (sender == deleteDataSourceBtn) {
       if (dataSourcesList.getSelectedDataSources().length > 0) {
         confirmDataSourceDeleteDialog.center();
       }
     } else if (sender == addDataSourceBtn) {
       addNewDataSource();
     } else if (sender == generalButton) {
       if (!generalButton.isDown()) {
         generalButton.setDown(true);
       } else {
         advanceButton.setDown(false);
         deckPanel.showWidget(GENERAL_PANEL_ID);
       }
     } else if (sender == advanceButton) {
       if (!advanceButton.isDown()) {
         advanceButton.setDown(true);
       } else {
         generalButton.setDown(false);
         deckPanel.showWidget(ADVANCE_PANEL_ID);
       }
     }    
   }
 
   private void addNewDataSource() {
     newDataSourceDialogBox.setDataSource(null);
     newDataSourceDialogBox.center();
   }
 
   private void deleteSelectedDataSources() {
     final PentahoDataSource[] selectedDataSources = dataSourcesList.getSelectedDataSources();
     if (selectedDataSources.length > 0) {
       AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
         public void onSuccess(Boolean result) {
           messageDialog.setText(Messages.getString("deleteDataSources")); //$NON-NLS-1$
           msgBoxHtml.setHTML(Messages.getString("successfulDataSourceDelete"));  //$NON-NLS-1$
           messageDialog.center();
           refresh();
         }
 
         public void onFailure(Throwable caught) {
           messageDialog.setText(ExceptionParser.getErrorHeader(caught.getMessage()));
           msgBoxHtml.setHTML(ExceptionParser.getErrorMessage(caught.getMessage(), Messages.getString("errorDeletingDataSource")));           //$NON-NLS-1$
           messageDialog.center();
         }
       };
       PacServiceFactory.getPacService().deleteDataSources(selectedDataSources, callback);
     }
   }
 
   private void dataSourceSelectionChanged() {
     PentahoDataSource[] selectedDataSources = dataSourcesList.getSelectedDataSources();
     if (selectedDataSources.length == 1) {
       dataSourceGeneralPanel.setDataSource(selectedDataSources[0]);
       dataSourceAdvancePanel.setDataSource(selectedDataSources[0]);
     } else {
       dataSourceGeneralPanel.setDataSource(null);
       dataSourceAdvancePanel.setDataSource(null);
     }
     dataSourceGeneralPanel.setEnabled(selectedDataSources.length == 1);
     dataSourceAdvancePanel.setEnabled(selectedDataSources.length == 1);
     updateDataSourceBtn.setEnabled(selectedDataSources.length == 1);
     testDataSourceBtn.setEnabled(selectedDataSources.length == 1);
     deleteDataSourceBtn.setEnabled(selectedDataSources.length > 0);
 
   }
 
   private void updateDataSourceDetails(final Widget sender) {
     messageDialog.setText(Messages.getString("updateDataSource")); //$NON-NLS-1$
     if (dataSourceGeneralPanel.getJndiName().trim().length() == 0) {
       msgBoxHtml.setHTML(Messages.getString("invalidConnectionName")); //$NON-NLS-1$
       messageDialog.center();
     } else if (dataSourceGeneralPanel.getUrl().trim().length() == 0) {
       msgBoxHtml.setHTML(Messages.getString("missingDbUrl")); //$NON-NLS-1$
       messageDialog.center();
     } else if (dataSourceGeneralPanel.getDriverClass().trim().length() == 0) {
       msgBoxHtml.setHTML(Messages.getString("missingDbDriver")); //$NON-NLS-1$
       messageDialog.center();
     } else if (dataSourceGeneralPanel.getUserName().trim().length() == 0) {
       msgBoxHtml.setHTML(Messages.getString("missingDbUserName")); //$NON-NLS-1$
       messageDialog.center();
     } else {
       final PentahoDataSource dataSource = getDataSource();
       final int index = dataSourcesList.getSelectedIndex();
 
       ((Button) sender).setEnabled(false);
       AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
         public void onSuccess(Boolean result) {
           messageDialog.setText(Messages.getString("updateDataSource")); //$NON-NLS-1$
           msgBoxHtml.setHTML(Messages.getString("successfulDataSourceUpdate"));  //$NON-NLS-1$
           messageDialog.center();
           dataSourcesList.setDataSource(index, dataSource);
           ((Button) sender).setEnabled(true);
 
         }
 
         public void onFailure(Throwable caught) {
           messageDialog.setText(ExceptionParser.getErrorHeader(caught.getMessage()));
           msgBoxHtml.setHTML(ExceptionParser.getErrorMessage(caught.getMessage(), Messages.getString("errorUpdatingDataSource")));           //$NON-NLS-1$
           messageDialog.center();
           ((Button) sender).setEnabled(true);
         }
       };
       PacServiceFactory.getPacService().updateDataSource(dataSource, callback);
     }
   }
 
   private void testDataSourceConnection() {
     final PentahoDataSource dataSource = getDataSource();
     
     final AsyncCallback<Boolean> validationUrlCallback = new AsyncCallback<Boolean>() {
         public void onSuccess(Boolean result) {
           messageDialog.setText(Messages.getString("testConnection")); //$NON-NLS-1$
           msgBoxHtml.setHTML(Messages.getString("connectionTestSuccessful")); //$NON-NLS-1$
           messageDialog.center();
         }
 
         public void onFailure(Throwable caught) {
           messageDialog.setText(ExceptionParser.getErrorHeader(caught.getMessage()));
          msgBoxHtml.setHTML(Messages.getString("errorTestingValidationQuery",ExceptionParser.getErrorMessage(caught.getMessage(), Messages.getString("errorTestingValidationQueryDefault"))));           //$NON-NLS-1$          
           messageDialog.center();
         }
       };
       
     AsyncCallback<Boolean> connTestCallback = new AsyncCallback<Boolean>() {
       public void onSuccess(Boolean result) {
         if (dataSource.getQuery()!=null && !dataSource.getQuery().trim().equals(EMPTY_STRING)) { 
           PacServiceFactory.getPacService().testDataSourceValidationQuery(dataSource, validationUrlCallback);
         }
         else{
           messageDialog.setText(Messages.getString("testConnection")); //$NON-NLS-1$
           msgBoxHtml.setHTML(Messages.getString("connectionTestSuccessful")); //$NON-NLS-1$
             messageDialog.center();
         }
       }
 
       public void onFailure(Throwable caught) {
         messageDialog.setText(ExceptionParser.getErrorHeader(caught.getMessage()));
         msgBoxHtml.setHTML(ExceptionParser.getErrorMessage(caught.getMessage(), Messages.getString("errorTestingDataSourceConnection")));           //$NON-NLS-1$
         messageDialog.center();
       }
     };
     
     
       
       
     PacServiceFactory.getPacService().testDataSourceConnection(dataSource, connTestCallback);
 
   }
 
   public boolean validate() {
     return true;
   }
 
   public void onChange(Widget sender) {
     dataSourceSelectionChanged();
   }
 
   public void refresh() {
     PacServiceFactory.getJdbcDriverDiscoveryService().getAvailableJdbcDrivers(
         new AsyncCallback<NameValue[]>() {
           public void onFailure(Throwable caught) {
             dataSourcesList.refresh();
             dataSourceGeneralPanel.refresh(null);
             dataSourceAdvancePanel.refresh();
             newDataSourceDialogBox.refresh(null);
             dataSourceSelectionChanged();
           }
 
           public void onSuccess(NameValue[] result) {
             dataSourcesList.refresh();
             dataSourceGeneralPanel.refresh(result);
             newDataSourceDialogBox.refresh(result);
             dataSourceAdvancePanel.refresh();
             dataSourceSelectionChanged();
           }
         }); 
   }
 
   public boolean isInitialized() {
     return dataSourcesList.isInitialized();
   }
 
   public void clearDataSourcesCache() {
     dataSourcesList.clearDataSourcesCache();
   }
 
   private PentahoDataSource getNormalDataSource() {
     return dataSourceGeneralPanel.getDataSource();
   }
 
   public PentahoDataSource getDataSource() {
     PentahoDataSource normalDataSource = getNormalDataSource();
     PentahoDataSource advanceDataSource = getAdvanceDataSource();
     PentahoDataSource dataSource = consolidateNormalAndAdvance(normalDataSource, advanceDataSource);
     return dataSource;
   }
 
   private PentahoDataSource getAdvanceDataSource() {
     return dataSourceAdvancePanel.getDataSource();
   }
 
   private PentahoDataSource consolidateNormalAndAdvance(PentahoDataSource normalDataSource,
       PentahoDataSource advanceDataSource) {
     PentahoDataSource dataSource = new PentahoDataSource();
     dataSource.setDriverClass(normalDataSource.getDriverClass());
     dataSource.setPassword(normalDataSource.getPassword());
     dataSource.setName(normalDataSource.getName());
     dataSource.setUrl(normalDataSource.getUrl());
     dataSource.setUserName(normalDataSource.getUserName());
     dataSource.setIdleConn(advanceDataSource.getIdleConn());
     dataSource.setMaxActConn(advanceDataSource.getMaxActConn());
     dataSource.setQuery(advanceDataSource.getQuery());
     dataSource.setWait(advanceDataSource.getWait());
 
     return dataSource;
   }
 }
