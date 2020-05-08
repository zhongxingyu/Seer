 /*
  * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
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
  * @created May 19, 2008
  * 
  */
 package org.pentaho.pac.client.scheduler.ctlr;
 
 import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor;
 import org.pentaho.gwt.widgets.client.ui.ICallback;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.TableListCtrl;
 import org.pentaho.pac.client.common.ui.dialog.BasicDialog;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.client.scheduler.model.Schedule;
 import org.pentaho.pac.client.scheduler.model.SchedulesModel;
 import org.pentaho.pac.client.scheduler.view.ActionSequencePickerDialog;
 import org.pentaho.pac.client.scheduler.view.ScheduleCreatorDialog;
 import org.pentaho.pac.client.scheduler.view.SchedulerPanel;
 import org.pentaho.pac.client.scheduler.view.SchedulerToolbar;
 import org.pentaho.pac.client.scheduler.view.SchedulesListCtrl;
 import org.pentaho.pac.client.scheduler.view.SolutionRepositoryActionSequenceListEditor;
 
 
 public class SchedulerController {
 
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   private SchedulerPanel schedulerPanel = null; // this is the view
   private ScheduleCreatorDialog scheduleCreatorDialog = null;
 
   // sub-controllers
   private SchedulesListController schedulesListController = null;
   private SolutionRepositoryActionSequenceListEditorController solRepActionSequenceEditorController = null;
   private SchedulerToolbarController schedulerToolbarController = null;
   
   private boolean isInitialized = false;
   
   public SchedulerController( SchedulerPanel schedulerPanel ) {
     assert (null != schedulerPanel ) : "schedulerPanel cannot be null."; //$NON-NLS-1$
     
     this.schedulerPanel = schedulerPanel;
   }
   
   // TODO sbarkdull, refactor creation and init of controllers
   public void init() {
     
     if ( !isInitialized ) {
       schedulerPanel.init();
       
       // init dialog
       this.scheduleCreatorDialog = new ScheduleCreatorDialog();
      this.scheduleCreatorDialog.setPanelSize("475px", "370px"); //$NON-NLS-1$ //$NON-NLS-2$
       this.scheduleCreatorDialog.setOnCancelHandler( new ICallback<BasicDialog>() {
         public void onHandle(BasicDialog dlg) {
           clearScheduleEditorValidationMsgs();
           scheduleCreatorDialog.hide();
         }
       });
       scheduleCreatorDialog.getScheduleEditor().setSubscriptionSchedule( true );
       
       // init list control
       SchedulesListCtrl listCtrl = schedulerPanel.getSchedulesListCtrl();
       schedulesListController = new SchedulesListController( listCtrl );
       listCtrl.setOnSelectHandler( new ICallback<TableListCtrl<Schedule>>() {
         public void onHandle(TableListCtrl<Schedule> pListCtrl) {
           schedulerToolbarController.enableTools();
         }
       });
       // TODO sbarkdull, schedulesListController.init( listCtrl, schedulesModel );
       
       listCtrl.setOnSelectAllHandler( new ICallback<TableListCtrl<Schedule>>() {
         public void onHandle(TableListCtrl<Schedule> pListCtrl) {
           schedulerToolbarController.enableTools();
         }
       });
       
       // init item picker
       SolutionRepositoryActionSequenceListEditor picker = scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor();
       ActionSequencePickerDialog actionSequencePickerDialog = new ActionSequencePickerDialog( MSGS.selectTitleBarLabel() );
       solRepActionSequenceEditorController = new SolutionRepositoryActionSequenceListEditorController( picker,
           actionSequencePickerDialog );
       
       // init toolbar
       SchedulerToolbar schedulerToolbar = schedulerPanel.getSchedulerToolbar();
       schedulerToolbarController = new SchedulerToolbarController( scheduleCreatorDialog, schedulerToolbar, listCtrl );
       schedulerToolbarController.init( schedulesListController, solRepActionSequenceEditorController );
       
       isInitialized = true;
     } // end isInitialized
   }
   
   /**
    * NOTE: code in this method must stay in sync with isScheduleEditorValid(), i.e. all error msgs
    * that may be set in isScheduleEditorValid(), must be cleared here.
    */
   private void clearScheduleEditorValidationMsgs() {
     
     scheduleCreatorDialog.clearTabError();
     
     ScheduleEditor schedEd = scheduleCreatorDialog.getScheduleEditor();
     SolutionRepositoryActionSequenceListEditor solRepPicker = scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor();
     
     SchedulesModel schedulesModel = schedulerToolbarController.getSchedulesModel();
     ScheduleEditorValidator schedEdValidator = new ScheduleEditorValidator( 
         schedEd, schedulesModel );
     schedEdValidator.clear();
     
     SolutionRepositoryActionSequenceListEditorValidator solRepValidator = new SolutionRepositoryActionSequenceListEditorValidator( solRepPicker,
         scheduleCreatorDialog.getScheduleEditor().isSubscriptionSchedule() );
     solRepValidator.clear();
     
   }
 }
