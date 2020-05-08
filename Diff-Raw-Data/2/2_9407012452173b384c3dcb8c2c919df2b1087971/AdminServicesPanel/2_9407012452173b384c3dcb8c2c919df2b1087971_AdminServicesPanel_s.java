 /*
  * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * Created  
  * @author Steven Barkdull
  */
 
 package org.pentaho.pac.client.services;
 
 import org.pentaho.pac.client.PacServiceAsync;
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class AdminServicesPanel extends VerticalPanel implements ClickListener {
 
   Button refreshSolutionRepositoryBtn = new Button(PentahoAdminConsole.getLocalizedMessages().refreshSolutionRepository());
   Button cleanRepositoryBtn = new Button(PentahoAdminConsole.getLocalizedMessages().removeStaleContent());
   Button clearMondrianDataCacheBtn = new Button(PentahoAdminConsole.getLocalizedMessages().purgeMondrianDataCache());
   Button clearMondrianSchemaCacheBtn = new Button(PentahoAdminConsole.getLocalizedMessages().purgeMondrianSchemaCache());
   Button scheduleRepositoryCleaningBtn = new Button(PentahoAdminConsole.getLocalizedMessages().scheduleDailyRepositoryCleaning());
   Button resetRepositoryBtn = new Button(PentahoAdminConsole.getLocalizedMessages().restoreDefaultFilePermissions());
   Button refreshSystemSettingsBtn = new Button(PentahoAdminConsole.getLocalizedMessages().refreshSystemSettings());
   Button executeGlobalActionsBtn = new Button(PentahoAdminConsole.getLocalizedMessages().executeGlobalActions());
   Button refreshReportingMetadataBtn = new Button(PentahoAdminConsole.getLocalizedMessages().refreshReportingMetadata());
   
   public AdminServicesPanel() {
     Grid grid = new Grid(5, 2);
     grid.setWidth("100%"); //$NON-NLS-1$
     
     grid.setWidget(0, 0, refreshSolutionRepositoryBtn);
     grid.setWidget(0, 1, cleanRepositoryBtn);
     grid.setWidget(1, 0, clearMondrianDataCacheBtn);
     grid.setWidget(1, 1, clearMondrianSchemaCacheBtn);
     grid.setWidget(2, 0, scheduleRepositoryCleaningBtn);
     grid.setWidget(2, 1, resetRepositoryBtn);
     grid.setWidget(3, 0, refreshSystemSettingsBtn);
     grid.setWidget(3, 1, executeGlobalActionsBtn);
     grid.setWidget(4, 0, refreshReportingMetadataBtn);
     
     refreshSolutionRepositoryBtn.setWidth("100%"); //$NON-NLS-1$
     cleanRepositoryBtn.setWidth("100%"); //$NON-NLS-1$
     clearMondrianDataCacheBtn.setWidth("100%"); //$NON-NLS-1$
     clearMondrianSchemaCacheBtn.setWidth("100%"); //$NON-NLS-1$
     scheduleRepositoryCleaningBtn.setWidth("100%"); //$NON-NLS-1$
     resetRepositoryBtn.setWidth("100%"); //$NON-NLS-1$
     refreshSystemSettingsBtn.setWidth("100%"); //$NON-NLS-1$
     executeGlobalActionsBtn.setWidth("100%"); //$NON-NLS-1$
     refreshReportingMetadataBtn.setWidth("100%"); //$NON-NLS-1$
     
     refreshSolutionRepositoryBtn.addClickListener(this);
     cleanRepositoryBtn.addClickListener(this);
     clearMondrianDataCacheBtn.addClickListener(this);
     clearMondrianSchemaCacheBtn.addClickListener(this);
     scheduleRepositoryCleaningBtn.addClickListener(this);
     resetRepositoryBtn.addClickListener(this);
     refreshSystemSettingsBtn.addClickListener(this);
     executeGlobalActionsBtn.addClickListener(this);
     refreshReportingMetadataBtn.addClickListener(this);
     
     add(grid);
   }
 
   public void onClick(final Widget sender) {
     AsyncCallback callback = new AsyncCallback() {
       
       public void onSuccess(Object result) {
         final DialogBox dialogBox = new DialogBox();
         dialogBox.setText(PentahoAdminConsole.getLocalizedMessages().services());
         Button okButton = new Button(PentahoAdminConsole.getLocalizedMessages().ok());
         okButton.addClickListener(new ClickListener() {
           public void onClick(Widget sender) {
             dialogBox.hide();
           }  
         });
         
         ((Button)sender).setEnabled(true);
         HorizontalPanel footerPanel = new HorizontalPanel();
         footerPanel.add(okButton);
         
         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.add(new Label(result.toString()));
         verticalPanel.add(footerPanel);
         
         dialogBox.setWidget(verticalPanel);
         dialogBox.center();
       }
 
       public void onFailure(Throwable caught) {
         final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(PentahoAdminConsole.getLocalizedMessages().services());
         Button okButton = new Button(PentahoAdminConsole.getLocalizedMessages().ok());
         okButton.addClickListener(new ClickListener() {
           public void onClick(Widget sender) {
             dialogBox.hide();
           }
         });
         ((Button)sender).setEnabled(true);
         HorizontalPanel footerPanel = new HorizontalPanel();
         footerPanel.add(okButton);
         
         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.add(new Label(caught.getMessage()));
         verticalPanel.add(footerPanel);
         
         dialogBox.setWidget(verticalPanel);
         dialogBox.center();
       }
     }; // end AsyncCallback
 
     PacServiceAsync pacServiceAsync = PacServiceFactory.getPacService();
     
     ((Button)sender).setEnabled(false);
     if (sender == refreshSolutionRepositoryBtn) {
       pacServiceAsync.refreshSolutionRepository(callback);
     } else if (sender == cleanRepositoryBtn) {
       pacServiceAsync.cleanRepository(callback);
     } else if (sender == clearMondrianDataCacheBtn) {
       pacServiceAsync.clearMondrianDataCache(callback);
     } else if (sender == clearMondrianSchemaCacheBtn) {
       pacServiceAsync.clearMondrianSchemaCache(callback);
     } else if (sender == scheduleRepositoryCleaningBtn) {
       pacServiceAsync.scheduleRepositoryCleaning(callback);
     } else if (sender == resetRepositoryBtn) {
       pacServiceAsync.resetRepository(callback);
     } else if (sender == refreshSystemSettingsBtn) {
       pacServiceAsync.refreshSystemSettings(callback);
     } else if (sender == executeGlobalActionsBtn) {
       pacServiceAsync.executeGlobalActions(callback);
     } else if (sender == refreshReportingMetadataBtn) {
       pacServiceAsync.refreshReportingMetadata(callback);
     }
   }
   
 }
