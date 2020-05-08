 /**
  * Created On: Mar 5, 2007 11:29:45 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.platform.action.platform;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 
 import com.thinkparity.ophelia.browser.platform.Platform;
 import com.thinkparity.ophelia.browser.platform.action.AbstractAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingMonitor;
 import com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingWorker;
 import com.thinkparity.ophelia.browser.util.localization.ActionLocalization;
 import com.thinkparity.ophelia.model.util.ProcessAdapter;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.util.Step;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 import com.thinkparity.ophelia.model.workspace.monitor.InitializeStep;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class Login extends AbstractAction {
 
     /** The browser platform. */
     private final Platform platform;
 
     /**
      * Create Login.
      * 
      * @param platform
      *            The browser platform.
      */
     public Login(final Platform platform) {
         super(ActionId.PLATFORM_LOGIN);
         this.platform = platform;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
      */
     public void invoke(Data data) {
         final ThinkParitySwingMonitor monitor = (ThinkParitySwingMonitor) data.get(DataKey.MONITOR);
         final String password = (String) data.get(DataKey.PASSWORD);
         final String username = (String) data.get(DataKey.USERNAME);
         invoke(username, password, monitor);
     }
 
     public void invoke(final String username, final String password, final ThinkParitySwingMonitor monitor) {
         final Credentials credentials = new Credentials();
         credentials.setPassword(password);
         credentials.setUsername(username);
         final ThinkParitySwingWorker worker = new LoginWorker(this, platform, localization, credentials);
         worker.setMonitor(monitor);
         worker.start();
     }
 
     /** Data keys. */
     public enum DataKey {MONITOR, PASSWORD, USERNAME}
 
     /** A login action worker object. */
     private static class LoginWorker extends ThinkParitySwingWorker<Login> {
 
         /** The <code>Credentials</code>.. */
         private final Credentials credentials;
 
         /** Action localization. */
         private final ActionLocalization localization;
 
         /** The login <code>ProcessMonitor</code>. */
         private final ProcessMonitor loginMonitor;
 
         /** The thinkParity <code>Platform</code>. */
         private final Platform platform;
 
         /** The thinkParity <code>Workspace</code>. */
         private final Workspace workspace;
 
         private LoginWorker(final Login login,
                 final Platform platform,
                 final ActionLocalization localization,
                 final Credentials credentials) {
             super(login);
             this.credentials = credentials;
             this.platform = platform;
             this.localization = localization;
             this.loginMonitor = newLoginMonitor();
             this.workspace = platform.getModelFactory().getWorkspace(getClass());
         }
 
         /**
          * @see com.thinkparity.codebase.swing.SwingWorker#construct()
          */
         @Override
         public Object construct() {
             try {
                 platform.initializeWorkspace(loginMonitor, workspace, credentials);
             } catch (final InvalidCredentialsException icx) {
                monitor.reset();
             }
             return null;
         }
 
         private String getNote(final Step step) {
             final String note;
             if (step instanceof InitializeStep) {
                 switch ((InitializeStep)step) {
                 case CONTACT_DOWNLOAD:
                     note = localization.getString("StepContactDownload");
                     break;
                 case CONTAINER_RESTORE_BACKUP:
                     note = localization.getString("StepContainerRestoreBackup");
                     break;
                 case PROFILE_CREATE:
                     note = localization.getString("StepProfileCreate");
                     break;
                 case SESSION_LOGIN:
                     note = localization.getString("StepSessionLogin");
                     break;
                 case SESSION_PROCESS_QUEUE:
                     note = localization.getString("StepProcessQueue");
                     break;
                 case PERSISTENCE_INITIALIZE:
                     note = localization.getString("StepPersistenceInitialize");
                     break;
                 default:
                     throw Assert.createUnreachable("UNKNOWN STEP STATE");
                 }
             } else {
                 note = null;
             }
             
             return note;
         }
 
         /**
          * Create a new process monitor.
          * 
          * @return A <code>ProcessMonitor</code>.
          */
         private ProcessMonitor newLoginMonitor() {
             return new ProcessAdapter() {
                 private Integer stage = 0;
                 private Integer stepIndex;
                 private Integer steps;
                 @Override
                 public void determineSteps(final Integer steps) {
                     if (0 < steps) {
                         this.stage++;
                         this.stepIndex = 0;
                         this.steps = steps;
                         monitor.setSteps(steps);
                         monitor.setStep(stepIndex);
                     }
                 }
                 @Override
                 public void beginProcess() {
                     monitor.monitor();
                 }
                 @Override
                 public void endProcess() {
                     monitor.complete();
                 }
                 @Override
                 public void beginStep(final Step step,
                         final Object data) {
                     final String note = getNote(step);
                     if (null != steps && steps.intValue() > 0) {
                         if (null != note) {
                             monitor.setStep(stepIndex, note);
                         } else {
                             monitor.setStep(stepIndex);
                         }
                     }
                 }
                 @Override
                 public void endStep(final Step step) {
                     if (null != steps && steps.intValue() > 0) {
                         stepIndex++;
                         monitor.setStep(stepIndex);
                     }
                 }
             };
         }
     }
 }
