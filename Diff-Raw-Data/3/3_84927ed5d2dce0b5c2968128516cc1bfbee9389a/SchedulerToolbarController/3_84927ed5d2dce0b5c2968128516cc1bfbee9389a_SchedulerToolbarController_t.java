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
 package org.pentaho.pac.client.scheduler.ctlr;
 
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.pentaho.gwt.widgets.client.controls.ProgressPopupPanel;
 import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor;
 import org.pentaho.gwt.widgets.client.ui.ICallback;
 import org.pentaho.gwt.widgets.client.utils.CronParseException;
 import org.pentaho.gwt.widgets.client.utils.TimeUtil;
 import org.pentaho.pac.client.ISchedulerServiceAsync;
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.IResponseCallback;
 import org.pentaho.pac.client.common.ui.dialog.ConfirmDialog;
 import org.pentaho.pac.client.common.ui.dialog.MessageDialog;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.client.scheduler.model.Schedule;
 import org.pentaho.pac.client.scheduler.model.SchedulesModel;
 import org.pentaho.pac.client.scheduler.view.DualModeScheduleEditor;
 import org.pentaho.pac.client.scheduler.view.ScheduleCreatorDialog;
 import org.pentaho.pac.client.scheduler.view.SchedulerToolbar;
 import org.pentaho.pac.client.scheduler.view.SchedulesListCtrl;
 import org.pentaho.pac.client.scheduler.view.SolutionRepositoryActionSequenceListEditor;
 import org.pentaho.pac.client.scheduler.view.ScheduleCreatorDialog.TabIndex;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ButtonBase;
 import com.google.gwt.user.client.ui.Widget;
 
 
 public class SchedulerToolbarController {
   
   private SchedulerToolbar schedulerToolbar;
   private SchedulesListCtrl schedulesListCtrl;
   private ScheduleCreatorDialog scheduleCreatorDialog = null;
   private SchedulesListController schedulesListController = null;
   private SolutionRepositoryActionSequenceListEditorController solRepActionSequenceEditorController = null;
   private SchedulesModel schedulesModel = null;
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   private static final int INVALID_SCROLL_POS = -1;
   private static final String DISABLED = "disabled"; //$NON-NLS-1$
   private boolean isInitialized = false;
   
   public SchedulerToolbarController( ScheduleCreatorDialog pScheduleCreatorDialog,
       SchedulerToolbar schedulerToolbar, SchedulesListCtrl schedulesListCtrl ) {
     this.scheduleCreatorDialog = pScheduleCreatorDialog;
     this.schedulerToolbar = schedulerToolbar;
     this.schedulesListCtrl = schedulesListCtrl;
   }
   
   public void init( SchedulesListController pSchedulesListController,
       SolutionRepositoryActionSequenceListEditorController solRepActionSequenceEditorController ) {
     
     if ( !isInitialized ) {
       this.schedulesListController = pSchedulesListController;
       this.solRepActionSequenceEditorController = solRepActionSequenceEditorController;
       
       final SchedulerToolbarController localThis = this;
   
       schedulerToolbar.setOnCreateListener( new ICallback<Widget>() { 
         public void onHandle(Widget w) {
           localThis.handleCreateSchedule();
         }
       });
       
       schedulerToolbar.setOnUpdateListener( new ICallback<Widget>() { 
         public void onHandle(Widget w) {
           localThis.handleUpdateSchedule();
         }
       });
   
       schedulerToolbar.setOnDeleteListener( new ICallback<Widget>() {
         public void onHandle(Widget w) {
           localThis.handleDeleteSchedules();
         }
       });
       
       schedulerToolbar.setOnResumeListener( new ICallback<Widget>() { 
         public void onHandle(Widget w) {
           localThis.handleResumeSchedules();
         }
       });
       
       schedulerToolbar.setOnSuspendListener( new ICallback<Widget>() { 
         public void onHandle(Widget w) {
           localThis.handlePauseSchedules();
         }
       });
       
       schedulerToolbar.setOnRunNowListener( new ICallback<Widget>() { 
         public void onHandle(Widget w) {
           localThis.handleRunNowSchedules();
         }
       });
       
       schedulerToolbar.setOnRefreshListener( new ICallback<Widget>() { 
         public void onHandle(Widget w) {
           loadJobsTable();
           // TODO sbarkdull reload sol rep model/view
         }
       });
       
       schedulerToolbar.setOnFilterListChangeListener( new ICallback<String>() { 
         public void onHandle(String s) {
           updateSchedulesTable();
         }
       });  
       
       loadJobsTable();  // TODO sbarkdull  belongs in SchedulesListController
       enableTools();
       
       isInitialized = true;
     }
   }
   
   private List<Schedule> getSortedSchedulesList( List<Schedule> scheduleList ) {
 
     assert null != scheduleList : "getSortedSchedulesList(): Schedule list cannot be null."; //$NON-NLS-1$
     Collections.sort( scheduleList, new Comparator<Schedule>() {
       public int compare(Schedule s1, Schedule s2) {
         return s1.getJobName().compareToIgnoreCase( s2.getJobName() );
       }
     });
     return scheduleList;
   }
   
   private void updateSchedulesTable() {
     List<Schedule> scheduleList = schedulesModel.getScheduleList();
     schedulesListController.updateSchedulesTable( 
         getSortedSchedulesList( getFilteredSchedulesList( scheduleList ) ) );
   }
 
   private List<Schedule> getFilteredSchedulesList( List<Schedule> scheduleList ) {
     List<Schedule> filteredList = null;
     String filterVal = schedulerToolbar.getFilterValue();
     if ( !SchedulerToolbar.ALL_GROUPS_FILTER.equals( filterVal ) ) {
       filteredList = new ArrayList<Schedule>();
       for ( int ii=0; ii<scheduleList.size(); ++ii ) {
         Schedule s = scheduleList.get( ii );
         if ( s.getJobGroup().equals( filterVal ) ) {
           filteredList.add( s );
         }
       }
     } else {
       filteredList = scheduleList;
     }
     return filteredList;
   }
   
   private void updateSchedule() {
 
     AsyncCallback<Object> updateScheduleResponseCallback = new AsyncCallback<Object>() {
       public void onSuccess( Object o ) {
         scheduleCreatorDialog.hide();
         loadJobsTable();
       }
 
       public void onFailure(Throwable caught) {
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     };
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     Schedule oldSchedule = scheduleList.get( 0 );
     DualModeScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
     
     ISchedulerServiceAsync schedSvc = scheduleEditor.isSubscriptionSchedule()
       ? PacServiceFactory.getSubscriptionService()
       : PacServiceFactory.getSchedulerService();
     
     String cronStr = scheduleEditor.getCronString();
     Date startDate = scheduleEditor.getStartDate();
     Date endDate = scheduleEditor.getEndDate();
     
     if ( null == cronStr ) {  // must be a repeating schedule
       String startTime = scheduleEditor.getStartTime(); // format of string should be: HH:MM:SS AM/PM, e.g. 7:12:28 PM
       startDate = TimeUtil.getDateTime( startTime, startDate );
       endDate = (null != endDate) ? TimeUtil.getDateTime( startTime, endDate ) : null;
     }
     
     ScheduleEditor.ScheduleType rt = scheduleEditor.getScheduleType(); 
     switch ( rt ) {
       case RUN_ONCE:
         schedSvc.updateRepeatSchedule(
             oldSchedule.getJobName(),
             oldSchedule.getJobGroup(),
             oldSchedule.getSchedId(),
             scheduleEditor.getName().trim(), 
             scheduleEditor.getGroupName().trim(), 
             scheduleEditor.getDescription().trim(), 
             startDate,
             endDate,
             "0" /*repeat count*/, //$NON-NLS-1$
             "0" /*repeat time*/,  //$NON-NLS-1$
             scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor().getActionsAsString().trim(),
             updateScheduleResponseCallback
           );
         break;
       case SECONDS: // fall through
       case MINUTES: // fall through
       case HOURS: // fall through
       case DAILY: // fall through
       case WEEKLY: // fall through
       case MONTHLY: // fall through
       case YEARLY:
         if ( null == cronStr ) {
           String repeatInterval = Integer.toString( TimeUtil.secsToMillisecs( 
                 scheduleEditor.getRepeatInSecs() ) );
           schedSvc.updateRepeatSchedule(
               oldSchedule.getJobName(),
               oldSchedule.getJobGroup(),
               oldSchedule.getSchedId(),
               scheduleEditor.getName().trim(), 
               scheduleEditor.getGroupName().trim(), 
               scheduleEditor.getDescription().trim(), 
               startDate,
               endDate,
               null /*repeat count*/,
               repeatInterval.trim(), 
               scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor().getActionsAsString().trim(),
               updateScheduleResponseCallback
             );
           break;
         } else {
           // fall through to case CRON
         }
       case CRON:
         schedSvc.updateCronSchedule(
             oldSchedule.getJobName(),
             oldSchedule.getJobGroup(),
             oldSchedule.getSchedId(),
             scheduleEditor.getName().trim(), 
             scheduleEditor.getGroupName().trim(), 
             scheduleEditor.getDescription().trim(), 
             startDate,
             endDate,
             cronStr.trim(), 
             scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor().getActionsAsString().trim(),
             updateScheduleResponseCallback
           );
         break;
       default:
         throw new RuntimeException( MSGS.invalidRunType( rt.toString() ) );
     }
   }
   
   /**
    * 
    */
   @SuppressWarnings("fallthrough")
   private void updateScheduleWithNewScheduleType() {
     
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     Schedule oldSchedule = scheduleList.get( 0 );
 
     AsyncCallback<Object> deleteScheduleCallback = new AsyncCallback<Object>() {
       public void onSuccess( Object o ) {
         createSchedule();
       }
 
       public void onFailure(Throwable caught) {
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() + " " + MSGS.updateFailedScheduleLost() ); //$NON-NLS-1$
         messageDialog.center();
       }
     };
     
     // TODO sbarkdull scheduleCreatorDialog -> scheduleEditorDialog
     DualModeScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
 
     ISchedulerServiceAsync schedSvc = null;
     if ( oldSchedule.isSubscriptionSchedule() != scheduleEditor.isSubscriptionSchedule() ) {
       // they are changing the schedule type, so delete it, and add a new one
       schedSvc = oldSchedule.isSubscriptionSchedule()  
         ? PacServiceFactory.getSubscriptionService()
         : PacServiceFactory.getSchedulerService();
       List<Schedule> deleteList = new ArrayList<Schedule>();
       deleteList.add( oldSchedule );
       schedSvc.deleteJobs(deleteList, deleteScheduleCallback );
     } else {
       // they are NOT changing the schedule type, so just update the existing schedule.
       updateSchedule();
     }
   }
 
   /**
    * Merge the two maps into one map. Add all key-values in the schedulerMap to
    * the mergedMap, unless the key is in both the schedulerMap and subscriptionMap.
    * If it is in both maps, add the one from the subscriptionMap.
    * NOTE: all elements in subscriptionMap should be in the schedulerMap, but not the
    * inverse.
    * 
    * @param schedulerMap
    * @param subscriptionMap
    * @return
    */
   private static List<Schedule> mergeSchedules( Map<String,Schedule> schedulerMap, 
       Map<String,Schedule> subscriptionMap ) {
     
     Schedule currentSched = null;
     List<Schedule> mergedList = new ArrayList<Schedule>();
     for ( Map.Entry<String,Schedule> me : schedulerMap.entrySet() ) {
       
       Schedule subscriptionSchedule = subscriptionMap.get( me.getKey() );
       if ( null != subscriptionSchedule ) {
         currentSched = subscriptionSchedule;
       } else {
         currentSched = me.getValue();
       }
       mergedList.add( currentSched );
     }
     return mergedList;
   }
   
   private void initFilterList() {
     String currentFilter = schedulerToolbar.getFilterValue();
     currentFilter = ( null == currentFilter ) ? SchedulerToolbar.ALL_GROUPS_FILTER : currentFilter;
     
     Set<String> groupNames = new HashSet<String>();
     List<Schedule> scheduleList = schedulesModel.getScheduleList();
     for ( int ii=0; ii<scheduleList.size(); ++ii ) {
       Schedule s = scheduleList.get( ii );
       String groupName = s.getJobGroup();
       if ( !groupNames.contains( groupName ) ) {
         groupNames.add( groupName );
       }
     }
     schedulerToolbar.clearFilters();
     
     schedulerToolbar.addFilterItem( SchedulerToolbar.ALL_GROUPS_FILTER );
     for ( String name : groupNames ) {
       schedulerToolbar.addFilterItem(name );
     }
     schedulerToolbar.setFilterValue( currentFilter );
     
   }
   
   private void loadJobsTable() {
 
     final ProgressPopupPanel loadingPanel = new ProgressPopupPanel();
     loadingPanel.setLabelText( MSGS.loading() );
     loadingPanel.center();
     
     final int currScrollPos = schedulesListCtrl.getScrollPosition();
     final Map<String,Schedule> schedulesMap = new HashMap<String,Schedule>();
     
     AsyncCallback<Map<String,Schedule>> schedulerServiceCallback = new AsyncCallback<Map<String,Schedule>>() {
       public void onSuccess( Map<String,Schedule> pSchedulesMap ) {
         schedulesMap.putAll( pSchedulesMap );
         enableTools();
         
         AsyncCallback<Map<String,Schedule>> subscriptionServiceCallback = new AsyncCallback<Map<String,Schedule>>() {
           public void onSuccess( Map<String,Schedule> subscriptionSchedulesMap ) {
             List<Schedule> schedulesList = mergeSchedules( schedulesMap, subscriptionSchedulesMap );
             schedulesModel = new SchedulesModel();
             schedulesModel.add( schedulesList );
             initFilterList();
             updateSchedulesTable();
             if ( INVALID_SCROLL_POS != currScrollPos ) { 
               schedulesListCtrl.setScrollPosition( currScrollPos );
             }
             enableTools();
             loadingPanel.hide();
           } // end inner onSuccess
 
           public void onFailure(Throwable caught) {
             loadingPanel.hide();
             schedulesListCtrl.setTempMessage( MSGS.noSchedules() );
             MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                 caught.getMessage() );
             messageDialog.center();
             enableTools();
           } // end inner onFailure
         }; // end subscriptionServiceCallback
         
         PacServiceFactory.getSubscriptionService().getJobNames( subscriptionServiceCallback );
       } // end outer onSuccess
 
       public void onFailure(Throwable caught) {
         loadingPanel.hide();
         schedulesListCtrl.setTempMessage( MSGS.noSchedules() );
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
         enableTools();
       } // end outer onFailure
     }; // end schedulerServiceCallback
       
     PacServiceFactory.getSchedulerService().getJobNames( schedulerServiceCallback );
   }
   
   public void enableTools() {
     int numSelectedItems = schedulesListCtrl.getNumSelections();
 
     enableWidget( schedulerToolbar.getCreateBtn(), true );
     enableWidget( schedulerToolbar.getUpdateBtn(), 1 == numSelectedItems );
     enableWidget( schedulerToolbar.getDeleteBtn(), numSelectedItems > 0 );
     enableWidget( schedulerToolbar.getSuspendBtn(),  numSelectedItems > 0 );
     enableWidget( schedulerToolbar.getResumeBtn(), numSelectedItems > 0 );
     enableWidget( schedulerToolbar.getRunNowBtn(), numSelectedItems > 0 );
     enableWidget( schedulerToolbar.getSuspendSchedulerBtn(), false );
     enableWidget( schedulerToolbar.getResumeSchedulerBtn(), false );
     enableWidget( schedulerToolbar.getRefreshBtn(), true );
   }
   
   private static void enableWidget( ButtonBase btn, boolean isEnabled ) {
     btn.setEnabled( isEnabled );
     if ( isEnabled ) {
       btn.removeStyleDependentName( DISABLED );
     } else {
       btn.addStyleDependentName( DISABLED );
     }
   }
   
   /**
    * NOTE: this method is extremely similar to updateSchedule, when modifying this method,
    * consider modifying updateSchedule in a similar way.
    */
   @SuppressWarnings("fallthrough")
   private void createSchedule() {
     // TODO, List<Schedule> is probably not what we will get back
     AsyncCallback<Object> responseCallback = new AsyncCallback<Object>() {
       public void onSuccess( Object o ) {
         scheduleCreatorDialog.hide();
         loadJobsTable();
       }
 
       public void onFailure(Throwable caught) {
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     }; // end responseCallback
     
     // TODO sbarkdull scheduleCreatorDialog -> scheduleEditorDialog
     DualModeScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
 
     String cronStr = scheduleEditor.getCronString();
     Date startDate = scheduleEditor.getStartDate();
     Date endDate = scheduleEditor.getEndDate();
     
     if ( null == cronStr ) {  // must be a repeating schedule
       String startTime = scheduleEditor.getStartTime(); // format of string should be: HH:MM:SS AM/PM, e.g. 7:12:28 PM
       startDate = TimeUtil.getDateTime( startTime, startDate );
       endDate = (null != endDate) ? TimeUtil.getDateTime( startTime, endDate ) : null;
     }
     
     ScheduleEditor.ScheduleType rt = scheduleEditor.getScheduleType();
 
     // TODO sbarkdull, if we want to support creation of scheduler schedules, we need to supply
  // a UI mechanism like a checkbox to allow user to identify scheduler vs subscription, 
  // and then test the value of the check box instead of the following "true".
     ISchedulerServiceAsync schedSvc = scheduleEditor.isSubscriptionSchedule()  
       ? PacServiceFactory.getSubscriptionService()
       : PacServiceFactory.getSchedulerService();
     
     switch ( rt ) {
       case RUN_ONCE:
         schedSvc.createRepeatSchedule(
             scheduleEditor.getName().trim(), 
             scheduleEditor.getGroupName().trim(), 
             scheduleEditor.getDescription().trim(), 
             startDate,
             endDate,
             "0" /*repeat count*/, //$NON-NLS-1$
             "0" /*repeat time*/,  //$NON-NLS-1$
             scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor().getActionsAsString().trim(),
             responseCallback
           );
         break;
       case SECONDS: // fall through
       case MINUTES: // fall through
       case HOURS: // fall through
       case DAILY: // fall through
       case WEEKLY: // fall through
       case MONTHLY: // fall through
       case YEARLY:
         if ( null == cronStr ) {
           String repeatInterval = Integer.toString( TimeUtil.secsToMillisecs( 
                 scheduleEditor.getRepeatInSecs() ) );
           schedSvc.createRepeatSchedule(
               scheduleEditor.getName().trim(), 
               scheduleEditor.getGroupName().trim(), 
               scheduleEditor.getDescription().trim(), 
               startDate,
               endDate,
               null /*repeat count*/,
               repeatInterval.trim(), 
               scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor().getActionsAsString().trim(),
               responseCallback
             );
           break;
         } else {
           // fall through to case CRON
         }
       case CRON:
         schedSvc.createCronSchedule(
             scheduleEditor.getName().trim(), 
             scheduleEditor.getGroupName().trim(), 
             scheduleEditor.getDescription().trim(), 
             startDate,
             endDate,
             cronStr.trim(), 
             scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor().getActionsAsString().trim(),
             responseCallback
           );
         break;
       default:
         throw new RuntimeException( MSGS.invalidRunType( rt.toString() ) );
     }
   }
   
   private void deleteSelectedSchedules() {
     final List<Schedule> selectedScheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> outerCallback = new AsyncCallback<Object>() {
       
       public void onSuccess(Object result) {
         AsyncCallback<Object> innerCallback = new AsyncCallback<Object>() {
           public void onSuccess(Object pResult) {
             loadJobsTable();
           }
           public void onFailure(Throwable caught) {
             // TODO sbarkdull
             MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                 caught.getMessage() );
             messageDialog.center();
           }
         }; // end inner callback
         final List<Schedule> subscriptionSchedList = getSubscriptionSchedules( selectedScheduleList );
         PacServiceFactory.getSubscriptionService().deleteJobs( subscriptionSchedList, innerCallback );
       } // end onSuccess
       
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     }; // end outer callback -----------
 
     List<Schedule> nonSubscriptionSchedList = getSchedules( selectedScheduleList );
     PacServiceFactory.getSchedulerService().deleteJobs( nonSubscriptionSchedList, outerCallback );
   }
   
   /**
    * initialize the <code>scheduleEditor</code>'s user interface with 
    * the contents of the <code>sched</code>.
    * 
    * @param scheduleEditor
    * @param sched
    * @throws CronParseException if sched has a non-empty CRON string, and the CRON string is not valid.
    */
   private void initScheduleCreatorDialog( Schedule sched ) throws CronParseException {
 
     scheduleCreatorDialog.reset( new Date() );
     initScheduleEditor( sched );
     initSolutionRepositoryActionSequenceEditor( sched );
   }
   
   private void initScheduleEditor( Schedule sched ) throws CronParseException {
     DualModeScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
 
     scheduleEditor.setSubscriptionSchedule( sched.isSubscriptionSchedule() );
     scheduleEditor.setName( sched.getJobName() );
     scheduleEditor.setGroupName( sched.getJobGroup() );
     scheduleEditor.setDescription( sched.getDescription() );
     
     String repeatIntervalInMillisecs = sched.getRepeatInterval();
     if ( sched.isCronSchedule() ) {
       scheduleEditor.setCronString( sched.getCronString() );  // throws CronParseException
     } else if ( sched.isRepeatSchedule() ) {
       int repeatIntervalInSecs = TimeUtil.millsecondsToSecs( Integer.parseInt( repeatIntervalInMillisecs ) );
       if ( 0 == repeatIntervalInSecs ) {
         // run once
         scheduleEditor.setScheduleType( ScheduleEditor.ScheduleType.RUN_ONCE );
       } else {
         // run multiple
         scheduleEditor.setRepeatInSecs( repeatIntervalInSecs );
       }
     } else {
       throw new RuntimeException( MSGS.illegalStateMissingCronAndRepeat() );
     }
 
     String timePart = null;
     String strDate = sched.getStartDate();
     if ( null != strDate ) {
       Date startDate = TimeUtil.getDate( strDate );
       if ( sched.isRepeatSchedule() ) {
         timePart = TimeUtil.getTimePart( startDate );
         scheduleEditor.setStartTime( timePart );
         startDate = TimeUtil.zeroTimePart( startDate );
       }
       scheduleEditor.setStartDate( startDate );
     }
 //    scheduleEditor.getRunOnceEditor().setStartTime(strTime)
 //    scheduleEditor.getRunOnceEditor().setStartDate(strTime)
     
     strDate = sched.getEndDate();
     if ( null != strDate ) {
       scheduleEditor.setEndBy();
       Date endDate = TimeUtil.getDate( strDate );
       if ( sched.isRepeatSchedule() ) {
         endDate = TimeUtil.zeroTimePart( endDate );
       }
       scheduleEditor.setEndDate(endDate);
     } else {
       scheduleEditor.setNoEndDate();
     }
     
   }
   
   private void initSolutionRepositoryActionSequenceEditor( Schedule sched ) {
     solRepActionSequenceEditorController.init( sched.getActionsList() );
     
   }
   
   private boolean isNewScheduleCreatorDialogValid() {
     ScheduleEditor schedEd = scheduleCreatorDialog.getScheduleEditor();
     ScheduleEditorValidator schedEdValidator = new NewScheduleEditorValidator( schedEd, schedulesModel );
     return isScheduleCreatorDialogValid( schedEdValidator );
   }
 
   private boolean isUpdateScheduleCreatorDialogValid() {
     ScheduleEditor schedEd = scheduleCreatorDialog.getScheduleEditor();
     ScheduleEditorValidator schedEdValidator = new ScheduleEditorValidator( schedEd, schedulesModel );
     return isScheduleCreatorDialogValid( schedEdValidator );
   }
   
   private void handleCreateSchedule() {
     final SchedulerToolbarController localThis = this;
     
     scheduleCreatorDialog.setTitle( MSGS.scheduleCreator() );
     scheduleCreatorDialog.reset( new Date() );
     
     scheduleCreatorDialog.setOnOkHandler( new ICallback<MessageDialog>() {
       public void onHandle(MessageDialog d) {
         localThis.createSchedule();
       }
     });
 
     this.scheduleCreatorDialog.setOnValidateHandler( new IResponseCallback<MessageDialog, Boolean>() {
       public Boolean onHandle( MessageDialog schedDlg ) {
         return isNewScheduleCreatorDialogValid();
       }
     });
 
     scheduleCreatorDialog.center();
     solRepActionSequenceEditorController.init( null );
     scheduleCreatorDialog.getScheduleEditor().setFocus();
   }
   
   private void handleUpdateSchedule() {
     final SchedulerToolbarController localThis = this;
 
     scheduleCreatorDialog.setTitle( MSGS.scheduleEditor() );
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     
     scheduleCreatorDialog.setOnOkHandler( new ICallback<MessageDialog>() {
       public void onHandle(MessageDialog d) {
         localThis.updateScheduleWithNewScheduleType();
       }
     });
     this.scheduleCreatorDialog.setOnValidateHandler( new IResponseCallback<MessageDialog, Boolean>() {
       public Boolean onHandle( MessageDialog schedDlg ) {
         return isUpdateScheduleCreatorDialogValid();
       }
     });
     // the update button should be enabled/disabled to guarantee that one and only one schedule is selected
     assert scheduleList.size() == 1 : "When clicking update, exactly one schedule should be selected."; //$NON-NLS-1$
     
     Schedule sched = scheduleList.get( 0 );
     try {
       initScheduleCreatorDialog( sched );
      scheduleCreatorDialog.center();
       scheduleCreatorDialog.getScheduleEditor().setFocus();
     } catch (CronParseException e) {
       final MessageDialog errorDialog = new MessageDialog( MSGS.error(),
           MSGS.invalidCronInInitOfRecurrenceDialog( sched.getCronString(), e.getMessage() ) );
       errorDialog.setOnOkHandler( new ICallback<MessageDialog>() {
         public void onHandle(MessageDialog messageDialog ) {
           errorDialog.hide();
           scheduleCreatorDialog.center();
         }
       });
       errorDialog.center();
     }
   }
 
   private void handleDeleteSchedules() {
     final SchedulerToolbarController localThis = this;
     
     final ConfirmDialog confirm = new ConfirmDialog( MSGS.confirmDelete(),
         MSGS.confirmDeleteQuestion( Integer.toString( getNumSubscribers() ) ) );
     confirm.setOnOkHandler( new ICallback<MessageDialog>() {
       public void onHandle( MessageDialog d ) {
         confirm.hide();
         localThis.deleteSelectedSchedules();
       }
     });
     confirm.center();
   }
   
   private int getNumSubscribers() {
     List<Schedule> schedList = schedulesListCtrl.getSelectedSchedules();
     int numSubscribers = 0;
     for ( Schedule sched: schedList ) {
       numSubscribers += Integer.parseInt( sched.getSubscriberCount() );
     }
     return numSubscribers;
   }
   
   private void handleResumeSchedules() {
     final List<Schedule> selectedScheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> outerCallback = new AsyncCallback<Object>() {
       
       public void onSuccess(Object result) {
         AsyncCallback<Object> innerCallback = new AsyncCallback<Object>() {
           public void onSuccess(Object pResult) {
             loadJobsTable();
           }
           public void onFailure(Throwable caught) {
             // TODO sbarkdull
             MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                 caught.getMessage() );
             messageDialog.center();
           }
         }; // end inner callback
         final List<Schedule> subscriptionSchedList = getSubscriptionSchedules( selectedScheduleList );
         PacServiceFactory.getSubscriptionService().resumeJobs( subscriptionSchedList, innerCallback );
       } // end onSuccess
       
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     }; // end outer callback -----------
 
     List<Schedule> nonSubscriptionSchedList = getSchedules( selectedScheduleList );
     PacServiceFactory.getSchedulerService().resumeJobs( nonSubscriptionSchedList, outerCallback );
   }
   
   private void handlePauseSchedules() {
     final List<Schedule> selectedScheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> outerCallback = new AsyncCallback<Object>() {
       
       public void onSuccess(Object result) {
         AsyncCallback<Object> innerCallback = new AsyncCallback<Object>() {
           public void onSuccess(Object pResult) {
             loadJobsTable();
           }
           public void onFailure(Throwable caught) {
             // TODO sbarkdull
             MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                 caught.getMessage() );
             messageDialog.center();
           }
         }; // end inner callback
         final List<Schedule> subscriptionSchedList = getSubscriptionSchedules( selectedScheduleList );
         PacServiceFactory.getSubscriptionService().pauseJobs( subscriptionSchedList, innerCallback );
       } // end onSuccess
       
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     }; // end outer callback -----------
 
     List<Schedule> nonSubscriptionSchedList = getSchedules( selectedScheduleList );
     PacServiceFactory.getSchedulerService().pauseJobs( nonSubscriptionSchedList, outerCallback );
   }
   
   private void handleRunNowSchedules() {
     final List<Schedule> selectedScheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> outerCallback = new AsyncCallback<Object>() {
       
       public void onSuccess(Object result) {
         AsyncCallback<Object> innerCallback = new AsyncCallback<Object>() {
           public void onSuccess(Object pResult) {
             loadJobsTable();
           }
           public void onFailure(Throwable caught) {
             // TODO sbarkdull
             MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                 caught.getMessage() );
             messageDialog.center();
           }
         }; // end inner callback
         final List<Schedule> subscriptionSchedList = getSubscriptionSchedules( selectedScheduleList );
         PacServiceFactory.getSubscriptionService().executeJobs( subscriptionSchedList, innerCallback );
       } // end onSuccess
       
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     }; // end outer callback -----------
 
     List<Schedule> nonSubscriptionSchedList = getSchedules( selectedScheduleList );
     PacServiceFactory.getSchedulerService().executeJobs( nonSubscriptionSchedList, outerCallback );
   }
 
   
   private static List<Schedule> getSchedules( List<Schedule> schedList ) {
     List<Schedule> list = new ArrayList<Schedule>();
     for ( Schedule sched : schedList ) {
       if ( !sched.isSubscriptionSchedule() ) {
         list.add( sched);
       }
     }
     return list;
   }
   
   private static List<Schedule> getSubscriptionSchedules( List<Schedule> schedList ) {
     List<Schedule> list = new ArrayList<Schedule>();
     for ( Schedule sched : schedList ) {
       if ( sched.isSubscriptionSchedule() ) {
         list.add( sched);
       }
     }
     return list;
     
   }
   
   /**
    * NOTE: This method should not be called directly except by isNewScheduleCreatorDialogValid and isUpdateScheduleCreatorDialogValid
    * NOTE: code in this method must stay in sync with isScheduleEditorValid(), i.e. all error msgs
    * that may be cleared in clearScheduleEditorValidationMsgs(), must be set-able here.
    */
   private boolean isScheduleCreatorDialogValid( ScheduleEditorValidator schedEdValidator ) {
 
     boolean isValid = true;
 
     boolean isSubscriptionSched = scheduleCreatorDialog.getScheduleEditor().isSubscriptionSchedule();
     SolutionRepositoryActionSequenceListEditor solRepPicker = scheduleCreatorDialog.getSolutionRepositoryActionSequenceEditor();
     SolutionRepositoryActionSequenceListEditorValidator solRepValidator = new SolutionRepositoryActionSequenceListEditorValidator( solRepPicker, isSubscriptionSched );
 
     scheduleCreatorDialog.clearTabError();
     schedEdValidator.clear();
     solRepValidator.clear();
     
     /*
      * If a tab's controls have errors, change the tab's appearance so that
      * the tab-label displays in an error-color (red). If the current tab 
      * does not have an error, find the first tab that does have an error,
      * and set it to the current tab.
      */
     TabIndex firstTabWithError = null;
     boolean doesSelectedTabHaveError = false;
     TabIndex selectedIdx = scheduleCreatorDialog.getSelectedTab();
     
     if ( !schedEdValidator.isValid() ) {
       isValid = false ;
       scheduleCreatorDialog.setTabError( TabIndex.SCHEDULE );
       if ( null == firstTabWithError ) {
         firstTabWithError = TabIndex.SCHEDULE;
       }
       if ( TabIndex.SCHEDULE == selectedIdx ) {
         doesSelectedTabHaveError = true;
       }
     }    
     if ( !solRepValidator.isValid() ) {
       isValid = false ;
       scheduleCreatorDialog.setTabError( TabIndex.SCHEDULE_ACTION );
       if ( null == firstTabWithError ) {
         firstTabWithError = TabIndex.SCHEDULE_ACTION;
       }
       if ( TabIndex.SCHEDULE_ACTION == selectedIdx ) {
         doesSelectedTabHaveError = true;
       }
     }
     if ( false == doesSelectedTabHaveError && firstTabWithError != null ) {
       scheduleCreatorDialog.setSelectedTab( firstTabWithError );
     }
     
     return isValid;
   }
 
   public SchedulesModel getSchedulesModel() {
     return schedulesModel;
   }
 }
