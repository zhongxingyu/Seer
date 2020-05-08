 package hudson.plugins.jprt;
 
 import JPRT.shared.GlobalProperties;
 import hudson.model.Hudson;
 import hudson.model.Items;
 import hudson.model.Job;
 import hudson.model.TopLevelItem;
 import hudson.model.TopLevelItemDescriptor;
 import hudson.model.ViewJob;
import hudson.model.Descriptor.FormException;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * {@link Job} that monitors a remote JPRT system.
  *
  * @author Kohsuke Kawaguchi
  */
 public class JPRTJob extends ViewJob<JPRTJob,JPRTRun> implements TopLevelItem {
 
     /**
      * Path to the JPRT archive root directory.
      */
     private volatile File archiveRoot;
 
     /**
      * URL to the JRPT archive root.
      */
     private volatile String archiveUrl;
 
     public JPRTJob(String name) {
         super(Hudson.getInstance(), name);
     }
 
     @Override
     public Hudson getParent() {
         return (Hudson)super.getParent();
     }
 
     public File getArchiveRoot() {
         return archiveRoot;
     }
 
     public String getArchiveUrl() {
         return archiveUrl;
     }
 
     @Override
     public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
         // JPRT ID is used as the primary means to identify a job in the URL.
         for (JPRTRun r : _getRuns().values()) {
             if(r.getId().equals(token))
                 return r;
         }
         return super.getDynamic(token, req, rsp);
     }
 
     /**
      * Set up properties to talk to JPRT
      */
     /*package*/ void prepareToTalkToJPRT() {
         GlobalProperties.setProperty("JPRT.archive.root.directory",archiveRoot.toString());
         GlobalProperties.setProperty("JPRT.archive.url",archiveUrl);
     }
 
     protected void reload() {
         prepareToTalkToJPRT();
         // TODO: what about the queue and on-going builds?
 
         TreeMap<Integer,JPRTRun> runs = new TreeMap<Integer,JPRTRun>(REVERSE_INT_COMPARATOR);
 
         File[] dirs = archiveRoot.listFiles(new FileFilter() {
             public boolean accept(File f) {
                 return f.isDirectory() && f.getName().length()>18;
             }
         });
         if(dirs!=null) {
             Arrays.sort(dirs,new Comparator<File>() {
                 public int compare(File lhs, File rhs) {
                     return lhs.getName().compareTo(rhs.getName());
                 }
             });
 
             JPRTRun last = null;
             for (File dir : dirs) {
                 try {
                     last = new JPRTRun(this,last,dir);
                     runs.put( last.getNumber(), last );
                 } catch (ParseException e) {
                     logger.log(Level.WARNING,"Unable to load "+dir,e);
                 }
             }
         }
 
         this.runs.reset(runs);
     }
 
     /**
      * Accepts submission from the configuration page.
      */
     @Override
    public void submit( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        super.submit(req,rsp);
 
         archiveRoot = new File(req.getParameter("jprt.archiveRoot"));
        if(!archiveRoot.isDirectory())
            throw new FormException(archiveRoot+" is not a directory",null);
         archiveUrl = req.getParameter("jprt.archiveUrl");
     }
 
 
     public TopLevelItemDescriptor getDescriptor() {
         return DESCRIPTOR;
     }
 
     static final TopLevelItemDescriptor DESCRIPTOR = new TopLevelItemDescriptor(JPRTJob.class) {
         public String getDisplayName() {
             return "Monitoring a JPRT system";
         }
 
         public JPRTJob newInstance(String name) {
             return new JPRTJob(name);
         }
     };
 
     static {
         Items.XSTREAM.alias("jprt",JPRTJob.class);
     }
 
     private static final Logger logger = Logger.getLogger(JPRTJob.class.getName());
 
     private static final Comparator<Integer> REVERSE_INT_COMPARATOR = new Comparator<Integer>() {
         public int compare(Integer lhs, Integer rhs) {
             return rhs-lhs;
         }
     };
 
 }
