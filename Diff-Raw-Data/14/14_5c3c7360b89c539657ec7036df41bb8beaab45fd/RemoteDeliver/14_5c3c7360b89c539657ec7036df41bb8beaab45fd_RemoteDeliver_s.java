 package net.praqma.hudson.scm;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.VirtualChannel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.UCMException.UCMType;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
 import net.praqma.clearcase.ucm.view.UCMView;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.Util;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 
 /**
  *
  * @author wolfgang
  *
  */
 class RemoteDeliver implements FileCallable<Integer> {
 
     private static final long serialVersionUID = 1L;
     private String jobName;
     private String baseline;
     private String stream;
     private BuildListener listener;
     private String id = "";
     private SnapshotView snapview;
 
     /*
      * private boolean apply4level; private String alternateTarget; private
      * String baselineName;
      */
     private String component;
     private String loadModule;
     // UCMDeliver ucmDeliver = null;
     private Logger logger = null;
     private PrintStream hudsonOut = null;
 
     public RemoteDeliver(String stream, BuildListener listener,
             /* Common values */
             String component, String loadModule, String baseline, String jobName) {
         this.jobName = jobName;
 
         this.baseline = baseline;
         this.stream = stream;
 
         this.listener = listener;
 
         this.component = component;
         this.loadModule = loadModule;
 
         // this.ucmDeliver = ucmDeliver;
     }
 
     public Integer invoke(File workspace, VirtualChannel channel) throws IOException {
         
         hudsonOut = listener.getLogger();
 
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
             throw new IOException("Could not create Baseline object: " + e.getMessage());
         }
 
         /* Create the development stream object */
         /* Append vob to dev stream */
 
         Stream stream = null;
         try {
             stream = UCMEntity.getStream(this.stream);
         } catch (UCMException e) {
             if (e.stdout != null) {
                 hudsonOut.println(e.stdout);
             }
             throw new IOException("Could not create Stream object: " + e.getMessage());
         }
 
         /* Get the target Stream */
         Stream target = null;
         try {
             target = stream.getDefaultTarget();
         } catch (UCMException e) {
             if (e.stdout != null) {
                 hudsonOut.println(e.stdout);
             }
             throw new IOException("The Stream did not have a default target: " + e.getMessage());
         }
 
         /* Make deliver view */
         try {
             snapview = makeDeliverView(target, workspace);
         } catch (ScmException e) {
             throw new IOException("Could not create deliver view: " + e.getMessage());
         }
         
         /* Make the deliver */
         try {
             hudsonOut.println( "[" + Config.nameShort + "] Starting deliver" );
             baseline.deliver(baseline.getStream(), stream.getDefaultTarget(), snapview.GetViewRoot(), snapview.GetViewtag(), true, false, true);
         } catch (UCMException e) {
         	/* Figure out what happened */
         	if( e.type.equals( UCMType.DELIVER_REQUIRES_REBASE ) ) {
         		hudsonOut.println(e.getMessage());
         		return 1;
         	}
         	
             throw new IOException(e.getMessage());
         }
 
         /* End of deliver */
         return 0;
     }
 
     private SnapshotView makeDeliverView(Stream stream, File workspace) throws ScmException {
         /* Replace evil characters with less evil characters */
         String newJobName = jobName.replaceAll("\\s", "_");
 
         String viewtag = newJobName + "_" + System.getenv("COMPUTERNAME") + "_" + stream.getShortname();
         
         File viewroot = new File(workspace, viewtag);
 
         return Util.makeView( stream, workspace, listener, loadModule, viewroot, viewtag );
     }
 
     public SnapshotView getSnapShotView() {
         return this.snapview;
     }
 }
