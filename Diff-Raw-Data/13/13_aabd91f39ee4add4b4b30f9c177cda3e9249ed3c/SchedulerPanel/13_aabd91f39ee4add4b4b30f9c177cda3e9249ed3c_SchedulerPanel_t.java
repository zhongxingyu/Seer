 package org.pentaho.pac.client.scheduler;
 
 import java.util.List;
 
 import org.pentaho.pac.client.MessageDialog;
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SchedulerPanel extends VerticalPanel implements ClickListener {
 
   private ScrollPanel jobsTableScrollPanel = null;
   private FlexTable jobsTable = null;
   private FlexTable allActionsTable = null;
   private Label userInstructionLabel = null;
   private boolean isInitialized = false;
   private String resumeSuspendState = "&nbsp;"; //$NON-NLS-1$
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   
   private static final String USER_INSTRUCTION = MSGS.schedulerUserInstruction();
 
   private static final String[] COLUMN_HEADER_TITLE = {
     MSGS.jobGroupName(),
     MSGS.triggerGroupName(),
     MSGS.description(),
     MSGS.fireTimeLastNext(),
     MSGS.state(),
     MSGS.action()
   };
   
   public SchedulerPanel()
   {
     this.setStylePrimaryName( "schedulerPanel" ); //$NON-NLS-1$
   }
   
   public void onClick( Widget sender ) {
   }
 
 
   public void refresh() {
     if ( !isInitialized ) {
       
       if ( !hasUIBeenCreated() ) {
         loading = new Label( MSGS.loading() );
         add( loading );
       }
       
       PacServiceFactory.getPacService().getJobNames(
           new AsyncCallback() {
             public void onSuccess( Object oJobList ) {
               List/*<Job>*/ jobList = (List/*<Job>*/)oJobList;
               createUI( jobList );
               allActionsTable.setHTML( 2, 1, resumeSuspendState );
               isInitialized = true;
              updateSchedulerPausedStatus();
             }
       
             public void onFailure(Throwable caught) {
               MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                   caught.getMessage(), new int[]{MessageDialog.OK_BTN});
               messageDialog.center();
               remove( loading );
               isInitialized = false;
             }
           }
         );
     } // end if (!isInitialized)
   }
 
   public void forceRefresh() {
     isInitialized = false;
     refresh();
   }
 
   public boolean isInitialized() {
     return isInitialized;
   }
   
   private boolean hasUIBeenCreated()
   {
     return null != userInstructionLabel;
   }
   
   Label loading = null;
   
   private void createUI( List/*<Job>*/ jobList )
   {
     if ( hasUIBeenCreated() ) {
       remove( userInstructionLabel );
       remove( allActionsTable );
       remove( jobsTableScrollPanel );
     } else {
       remove( loading );
     }
     userInstructionLabel = new Label( USER_INSTRUCTION );
     userInstructionLabel.setStylePrimaryName( "schedulerInstructionLabel" ); //$NON-NLS-1$
     add( userInstructionLabel );
     
     allActionsTable = createAllActionsTable();
     add( allActionsTable );
 
     jobsTable = createJobsTable( jobList );
     jobsTableScrollPanel = new ScrollPanel( jobsTable );
     // TODO sbarkdull, move the 450px to style sheet
     jobsTableScrollPanel.setHeight( "450px" ); //$NON-NLS-1$
     add( jobsTableScrollPanel );
   }
   
   private void addJobListToTable( List/*<Job>*/ jobList, FlexTable table )
   {
     for ( int jobIdx=0; jobIdx< jobList.size(); ++jobIdx ) {
       Job job = (Job)jobList.get( jobIdx );
 
       int rowNum = jobIdx+1;
       table.setHTML( rowNum, 0, job.jobGroup + "<br/>" + job.jobName ); //$NON-NLS-1$
       table.setHTML( rowNum, 1, job.triggerGroup + "<br/>" + job.triggerName ); //$NON-NLS-1$
       table.setHTML( rowNum, 2, defaultIfEmpty( job.description ) );
       table.setHTML( rowNum, 3, job.prevFireTime + "<br/>" + job.getNextFireTime() ); //$NON-NLS-1$
       table.setHTML( rowNum, 4, defaultIfEmpty( job.triggerState ) );
       
       VerticalPanel actionPanel = createActionPanel( job, rowNum, job.triggerState );
       table.setWidget( rowNum, 5, actionPanel );
     }
   }
   
 //TODO move to a client side utils class
   /**
    * mimics StringUtils.defaultIfEmpty()
    * @param str
    * @return
    */
   private static String defaultIfEmpty( String str )
   {
     return ( null == str || "".equals( str ) ) ? "&nbsp;" : str; //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   private FlexTable createAllActionsTable()
   {
     FlexTable t = new FlexTable();
     t.setStylePrimaryName( "allActionTable"); //$NON-NLS-1$
     t.setCellPadding( 0 );
     t.setCellSpacing( 0 );
 
     int rowNum = 0;
     Label l = new Label( MSGS.actions() );
     t.setWidget( rowNum, 0, l );
     t.setHTML( rowNum, 1, MSGS.status() );
     t.getRowFormatter().setStylePrimaryName( 0, "allActionTableHeader" ); //$NON-NLS-1$
     
     rowNum++; // 1
     Hyperlink statusHyper = createSchedulerStatusHyperlink();
     t.setWidget( rowNum, 0, statusHyper );
    //see setSchedulerStatusMsg
     t.setHTML( rowNum, 1, "&nbsp;" ); //$NON-NLS-1$
 
     rowNum++; // 2
     Hyperlink resumeHyper = createResumeAllHyperlink();
     Hyperlink suspendHyper = createSuspendAllHyperlink();
     VerticalPanel p = new VerticalPanel();
     p.setStylePrimaryName( "resumeSuspendPanel" ); //$NON-NLS-1$
     p.setSpacing(0);
     
     p.add( resumeHyper );
     p.add( suspendHyper );
     t.setWidget( rowNum, 0, p );
     t.setHTML( rowNum, 1, resumeSuspendState );
     
     return t;
   }
   
   private VerticalPanel createActionPanel( Job job, int rowNum, String triggerState ) {
     VerticalPanel p = new VerticalPanel();
     p.setStylePrimaryName( "actionCellPanel" ); //$NON-NLS-1$
     p.setSpacing(0);
     
     Hyperlink a = MSGS.stateSuspended().equals( triggerState )
       ? createResumeHyperlink( job.getJobName(), job.getJobGroup() )
       : createSuspendHyperlink( job.getJobName(), job.getJobGroup() );
     p.add( a );
     a = createDeleteHyperlink( job.getJobName(), job.getJobGroup(), rowNum );
     p.add( a );
     a = createRunNowHyperlink( job.getJobName(), job.getJobGroup() );
     p.add( a );
     return p;
   }
    
   private Hyperlink createSuspendHyperlink( final String jobName, final String jobGroup ) {
     Hyperlink a = new Hyperlink();
    a.setText( MSGS.suspend() );
     // TODO sbarkdull, yuk
     a.addClickListener( new ClickListener() {
       public void onClick( final Widget sender ) {
         ((Hyperlink)sender).setText( MSGS.working() );
         PacServiceFactory.getPacService().pauseJob(
             jobName,
             jobGroup,
             new AsyncCallback() {
               public void onSuccess( Object o ) {
                 forceRefresh();
               }
         
               public void onFailure(Throwable caught) {
                 MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                     caught.getMessage(), new int[]{MessageDialog.OK_BTN});
                 messageDialog.center();
               }
             }
           );
       }
     } );
     
     return a;
   }
    
   private Hyperlink createResumeHyperlink( final String jobName, final String jobGroup ) {
     Hyperlink a = new Hyperlink();
     a.setText( MSGS.resume() );
     // TODO sbarkdull, yuk
     a.addClickListener( new ClickListener() {
       public void onClick( Widget sender ) {
         ((Hyperlink)sender).setText( MSGS.working() );
         PacServiceFactory.getPacService().resumeJob(
             jobName,
             jobGroup,
             new AsyncCallback() {
               public void onSuccess( Object o ) {
                 forceRefresh();
               }
         
               public void onFailure(Throwable caught) {
                 // TODO sbarkdull
                 MessageDialog messageDialog = new MessageDialog(MSGS.error(), 
                     caught.getMessage(), new int[]{MessageDialog.OK_BTN});
                 messageDialog.center();
               }
             }
           );
       }
     } );
     
     return a;
   }
   private Hyperlink createDeleteHyperlink( final String jobName, final String jobGroup, int rowNum ) {
     Hyperlink a = new Hyperlink();
     a.setText( MSGS.delete() );
     // TODO sbarkdull, yuk
     final int localRowNum = rowNum;
     a.addClickListener( new ClickListener() {
       public void onClick( Widget sender ) {
         ((Hyperlink)sender).setText( MSGS.working() );
         PacServiceFactory.getPacService().deleteJob( 
             jobName,
             jobGroup,
             new AsyncCallback() {
               public void onSuccess( Object o ) {
                 forceRefresh();
                 jobsTable.removeRow( localRowNum );
               }
         
               public void onFailure(Throwable caught) {
                 MessageDialog messageDialog = new MessageDialog(MSGS.error(), 
                     caught.getMessage(), new int[]{MessageDialog.OK_BTN});
                 messageDialog.center();
               }
             }
           );
       }
     } );
     
     return a;
   }
   private Hyperlink createRunNowHyperlink( final String jobName, final String jobGroup ) {
     Hyperlink a = new Hyperlink();
     a.setText( MSGS.runNow() );
     // TODO sbarkdull, yuk
     a.addClickListener( new ClickListener() {
       public void onClick( Widget sender ) {
         ((Hyperlink)sender).setText( MSGS.working() );
         PacServiceFactory.getPacService().executeJobNow( 
             jobName,
             jobGroup,
             new AsyncCallback() {
               public void onSuccess( Object o ) {
                 forceRefresh();
               }
         
               public void onFailure(Throwable caught) {
                 MessageDialog messageDialog = new MessageDialog(MSGS.error(), 
                     caught.getMessage(), new int[]{MessageDialog.OK_BTN});
                 messageDialog.center();
               }
             }
           );
       }
     } );
     
     return a;
   }
 
   
   private Hyperlink createSuspendAllHyperlink() {
     Hyperlink a = new Hyperlink();
     a.setText( MSGS.suspendAllJobs() );
     // TODO sbarkdull, yuk
     a.addClickListener( new ClickListener() {
       public void onClick( Widget sender ) {
         allActionsTable.setHTML( 2, 1, MSGS.working() );
         PacServiceFactory.getPacService().pauseAll(
             new AsyncCallback() {
               public void onSuccess( Object o ) {
                 resumeSuspendState = MSGS.allJobsSuspended();
                 forceRefresh();
               }
         
               public void onFailure(Throwable caught) {
                 MessageDialog messageDialog = new MessageDialog(MSGS.error(), 
                     caught.getMessage(), new int[]{MessageDialog.OK_BTN});
                 messageDialog.center();
               }
             }
           );
       }
     } );
     
     return a;
   }
   private Hyperlink createResumeAllHyperlink() {
     Hyperlink a = new Hyperlink();
     a.setText( MSGS.resumeAllJobs() );
     // TODO sbarkdull, yuk
     a.addClickListener( new ClickListener() {
       public void onClick( Widget sender ) {
         allActionsTable.setHTML( 2, 1, MSGS.working() );
         PacServiceFactory.getPacService().resumeAll(
             new AsyncCallback() {
               public void onSuccess( Object o ) {
                 resumeSuspendState = MSGS.allJobsResumed();
                 forceRefresh();
               }
         
               public void onFailure(Throwable caught) {
                 MessageDialog messageDialog = new MessageDialog(MSGS.error(), 
                     caught.getMessage(), new int[]{MessageDialog.OK_BTN});
                 messageDialog.center();
               }
             }
           );
       }
     } );
     
     return a;
   }
   
   private Hyperlink createSchedulerStatusHyperlink() {
     Hyperlink a = new Hyperlink();
     a.setText( MSGS.schedulerStatus() );
     // TODO sbarkdull, yuk
     a.addClickListener( new ClickListener() {
       public void onClick( Widget sender ) {
         setSchedulerStatusMsg( MSGS.working() );
         updateSchedulerPausedStatus();
       }
     } );
     
     return a;
   }
   
   private void updateSchedulerPausedStatus()
   {
     PacServiceFactory.getPacService().isSchedulerPaused(
         new AsyncCallback() {
           public void onSuccess( Object oIsRunning ) {
             boolean isRunning = ((Boolean)oIsRunning).booleanValue();
            setSchedulerStatusMsg( isRunning ? MSGS.running() : MSGS.notRunning() );
           }
     
           public void onFailure(Throwable caught) {
             MessageDialog messageDialog = new MessageDialog(MSGS.error(), 
                 caught.getMessage(), new int[]{MessageDialog.OK_BTN});
             messageDialog.center();
           }
         }
       );
   }
   
   private void setSchedulerStatusMsg( String statusMsg )
   {
     allActionsTable.setHTML( 1, 1, statusMsg );
   }
  
   private FlexTable createJobsTable( List/*<Job>*/ jobList ) {
     
     FlexTable table = new FlexTable();
     table.setStylePrimaryName( "jobsTable" ); //$NON-NLS-1$
     table.setCellPadding( 0 );
     table.setCellSpacing( 0 );
     addJobsTableHeader( table );
     
     addJobListToTable( jobList, table );
     
     return table;
   }
   
   private void addJobsTableHeader( FlexTable t )
   {
     for ( int ii=0; ii<COLUMN_HEADER_TITLE.length; ++ii ) {
       String title = COLUMN_HEADER_TITLE[ii];
       //t.addCell( 0 );
       t.setText(0, ii, title );
     }
     t.getRowFormatter().setStylePrimaryName( 0, "jobsTableHeader" ); //$NON-NLS-1$
   }
 }
