 package net.sourceforge.importscrubber.ant;
 
 import java.io.File;
 import net.sourceforge.importscrubber.ImportScrubber;
 import net.sourceforge.importscrubber.IProgressMonitor;
 import net.sourceforge.importscrubber.ScrubTask;
 import net.sourceforge.importscrubber.StatementFormat;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 /**
  * Example:
  *
  * <PRE>
  * <importscrubber root="/home/tom/project/src" recurse="true" verbose="true"/>
  * </PRE>
  */
 public class ImportScrubberTask extends Task {
     private boolean verbose;
     private boolean recurse;
     private String rootString;
     private String classRoot;
     private String formatID;
     private boolean sortjavalibshigh;
     private String encoding;
 
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     public void setSortjavalibshigh(boolean sortjavalibshigh) {
         this.sortjavalibshigh = sortjavalibshigh;
     }
 
     public void setRecurse(boolean recurse) {
         this.recurse = recurse;
     }
 
     public void setRoot(String rootString) {
         this.rootString = rootString;
     }
 
     public void setClassRoot(String classRootString) {
         this.classRoot = classRootString;
     }
 
     public void setFormat(String format) {
         this.formatID = format;
     }
 
     public void setEncoding(String encoding) {
         this.encoding = encoding;
     }
 
     public void execute() throws BuildException {
         if ((rootString == null) || (rootString.length() == 0)) {
             throw new BuildException("You must set a root for the ImportScrubber task to work");
         }
 
         int formatIndex = StatementFormat.BREAK_NONE;
        if (formatID.equals("each")) {
             formatIndex = StatementFormat.BREAK_EACH_PACKAGE;
         }
         File root = new File(rootString);
         if (!root.exists()) {
             throw new BuildException("The root " + rootString + " does not exist");
         }
 
         try {
             ImportScrubber scrubber = new ImportScrubber(encoding);
             StatementFormat format = new StatementFormat(sortjavalibshigh, formatIndex, 5, true);
             scrubber.setFormat(format);
             scrubber.setFileRoot(rootString, classRoot, recurse);
             scrubber.buildTasks(scrubber.getFilesIterator());
             scrubber.runTasks(new IProgressMonitor() {
                 public void taskStarted(ScrubTask task) {}
                 public void taskComplete(ScrubTask task) {}
             });
         } catch (Exception ex) {
             ex.printStackTrace();
             throw new BuildException(ex);
         }
     }
 }
