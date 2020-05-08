 package net.praqma.hudson.scm;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 import java.util.Set;
 
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.naming.Context;
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.UCMException.UCMType;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.persistence.UCMContext;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.Util;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.scm.EstablishResult.ResultType;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.appenders.StreamAppender;
 
 /**
  *
  * @author wolfgang
  *
  */
 class RemoteDeliver implements FileCallable<EstablishResult> {
 
     private static final long serialVersionUID = 1L;
     private String jobName;
     private String baseline;
     private String destinationstream;
     private BuildListener listener;
     private String id = "";
     private SnapshotView snapview;
 
     /*
      * private boolean apply4level; private String alternateTarget; private
      * String baselineName;
      */
     private String component;
     private String loadModule;
     private PrintStream hudsonOut = null;
     private Pipe pipe;
     private Set<String> subscriptions;
     private String viewtag = "";
 
     public RemoteDeliver(String destinationstream, BuildListener listener, Pipe pipe, Set<String> subscriptions,
             /* Common values */
             String component, String loadModule, String baseline, String jobName) {
         this.jobName = jobName;
 
         this.baseline = baseline;
         this.destinationstream = destinationstream;
 
         this.listener = listener;
 
         this.component = component;
         this.loadModule = loadModule;
 
         this.pipe = pipe;
         this.subscriptions = subscriptions;
     }
 
     public EstablishResult invoke(File workspace, VirtualChannel channel) throws IOException {
 
         hudsonOut = listener.getLogger();
 
         StreamAppender app = null;
         if (pipe != null) {
             PrintStream toMaster = new PrintStream(pipe.getOut());
             app = new StreamAppender(toMaster);
             Logger.addAppender(app);
             app.setSubscriptions(subscriptions);
         }
 
         //TODO this should not be necessary cause its done in the Config.java file ????.
         UCM.setContext(UCM.ContextType.CLEARTOOL);
 
         /* Create the baseline object */
         Baseline baseline = null;
         try {
             baseline = UCMEntity.getBaseline(this.baseline);
         } catch (UCMException e) {
             if (e.stdout != null) {
                 hudsonOut.println(e.stdout);
             }
             Logger.removeAppender(app);
             throw new IOException("Could not create Baseline object: " + e.getMessage());
         }
 
         /* Create the development stream object */
         /* Append vob to dev stream */
 
         Stream destinationStream = null;
         try {
             destinationStream = UCMEntity.getStream(this.destinationstream);
         } catch (UCMException e) {
             if (e.stdout != null) {
                 hudsonOut.println(e.stdout);
             }
             Logger.removeAppender(app);
             throw new IOException("Could not create destination Stream object: " + e.getMessage());
         }
 
         /* Make deliver view */
         try {
             snapview = makeDeliverView(destinationStream, workspace);
         } catch (ScmException e) {
             Logger.removeAppender(app);
             throw new IOException("Could not create deliver view: " + e.getMessage());
         }
 
 
         String diff = "";
         ClearCaseChangeset changeset = new ClearCaseChangeset();
 
         try {
             List<Activity> bldiff = Version.getBaselineDiff(destinationStream, baseline, true, snapview.getViewRoot());
             hudsonOut.print("[" + Config.nameShort + "] Found " + bldiff.size() + " activit" + (bldiff.size() == 1 ? "y" : "ies") + ". ");
 
             int c = 0;
             for (Activity a : bldiff) {
                 c += a.changeset.versions.size();
                 for (Version version : a.changeset.versions) {
                     changeset.addChange(version.getFullyQualifiedName(), version.getUser());
                 }
             }
             hudsonOut.println(c + " version" + (c == 1 ? "" : "s") + " involved");
             diff = Util.createChangelog(bldiff, baseline);
         } catch (UCMException e1) {
             hudsonOut.println("[" + Config.nameShort + "] Unable to create change log: " + e1.getMessage());
         }
 
         EstablishResult er = new EstablishResult(viewtag);
         er.setView(snapview);
         er.setMessage(diff);
         er.setChangeset(changeset);
 
         /* Make the deliver */
         try {
             hudsonOut.println("[" + Config.nameShort + "] Starting deliver");
             baseline.deliver(baseline.getStream(), destinationStream, snapview.getViewRoot(), snapview.getViewtag(), true, false, true);
         } catch (UCMException e) {
             /* Figure out what happened */
             if (e.type.equals(UCMType.DELIVER_REQUIRES_REBASE)) {
                 hudsonOut.println(e.getMessage());
                 er.setResultType(ResultType.DELIVER_REQUIRES_REBASE);
                 Logger.removeAppender(app);
                 return er;
             }
 
             if (e.type.equals(UCMType.MERGE_ERROR)) {
                 hudsonOut.println(e.getMessage());
                 er.setResultType(ResultType.MERGE_ERROR);
                 Logger.removeAppender(app);
                 return er;
             }
 
             if (e.type.equals(UCMType.INTERPROJECT_DELIVER_DENIED)) {
                 hudsonOut.println(e.getMessage());
                 er.setResultType(ResultType.INTERPROJECT_DELIVER_DENIED);
                 Logger.removeAppender(app);
                 return er;
             }
 
             if (e.type.equals(UCMType.DELIVER_IN_PROGRESS)) {
 
                 /**
                  * rollback deliver..
                  * *******A DELIVER OPERATION IS ALREADY IN PROGRESS. Details about the delivery:
                  *
                  * <b Deliver operation in progress on stream "stream:pds316_deliver_test@\PDS_PVOB"/>
                  *      Started by "PDS316" on "2011-09-28T11:23:53+02:00"
                  *      Using integration activity "deliver.pds316_deliver_test.20110928.112353".
                  *    <bUsing view "pds316_deliver_test_int" />.
                  *      Baselines will be delivered to the default target stream "stream:deliver_test_int@\PDS_PVOB"
                  *    in project "project:deliver_test@\PDS_PVOB".
                  *
                  * Baselines to be delivered:
 
                  * *******Please try again later.
                  */
                 String msg = e.getMessage();
                 String stream = "";
                 String oldViewtag = null;
 
                 hudsonOut.println("");
                 hudsonOut.println("[" + Config.nameShort + "] Deliver already in progess, but we are forcing this anyway.");
                 hudsonOut.println("");
 
                 Pattern STREAM_PATTERN = Pattern.compile("Deliver operation .* on stream \\\"(.*)\\\"", Pattern.MULTILINE);
                 Pattern TAG_PATTERN = Pattern.compile("Using view \\\"(.*)\\\".", Pattern.MULTILINE);
 
                 Matcher mSTREAM = STREAM_PATTERN.matcher(msg);
                 while (mSTREAM.find()) {
                     stream = mSTREAM.group(1);
                     stream = "stream:" + stream + "@" + baseline.getPvobString();
                 }
 
                 Matcher mTAG = TAG_PATTERN.matcher(msg);
                 while (mTAG.find()) {
                     oldViewtag = mTAG.group(1);
                 }
 
                 File newView = null;
                 if (oldViewtag == null) {
                     newView = snapview.getViewRoot();
                 } else {
                     newView = new File(workspace + "\\rm_delv_view");
                 }
 
                 try {
                     UCMContext.context.remoteDeliverCancel(oldViewtag, stream, newView);
                 } catch (UCMException ex) {
                     hudsonOut.println(ex.getMessage());
                     throw new IOException(ex.getMessage(), ex.getCause());
                 }
 
                 //INVOKE recursive method call(..)
                 this.invoke(workspace, channel);

                /**
                 * Here we must return before the end of the methos
                 * cause in anyother case than this exception we whis to return
                 * the exception further op in the system.
                 *
                 * But here we wish to try agian until we succeed.
                 */
                er.setResultType(ResultType.SUCCESS);
                Logger.removeAppender(app);
                return er;
             }
 
             Logger.removeAppender(app);
             throw new IOException(e.getMessage());
         }
 
         /* End of deliver */
         er.setResultType(ResultType.SUCCESS);
         Logger.removeAppender(app);
         return er;
     }
 
     private SnapshotView makeDeliverView(Stream stream, File workspace) throws ScmException {
         /* Replace evil characters with less evil characters */
         String newJobName = jobName.replaceAll("\\s", "_");
 
         viewtag = "CCUCM_" + newJobName + "_" + System.getenv("COMPUTERNAME") + "_" + stream.getShortname();
 
         File viewroot = new File(workspace, "view");
 
         return Util.makeView(stream, workspace, listener, loadModule, viewroot, viewtag);
     }
 
     public SnapshotView getSnapShotView() {
         return this.snapview;
     }
 }
