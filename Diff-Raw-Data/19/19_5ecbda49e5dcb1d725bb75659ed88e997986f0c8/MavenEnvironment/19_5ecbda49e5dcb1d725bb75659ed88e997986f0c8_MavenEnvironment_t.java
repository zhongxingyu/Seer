 package net.sf.alchim.spoon.contrib.maven;
 
 import java.io.File;
 import java.io.FileWriter;
 
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 
 import spoon.processing.ProblemFixer;
 import spoon.processing.Processor;
 import spoon.processing.Severity;
 import spoon.reflect.declaration.CtElement;
 import spoon.support.StandardEnvironment;
 
 @SuppressWarnings("serial")
 class MavenEnvironment extends StandardEnvironment {
     private final Log logger_;
     private int warningCount_;
     private int errorCount_;
     private FileWriter reportDataWriter_;
    private File baseDir_;
 
    public MavenEnvironment(File baseDir, Log log, File reportDataFile) throws Exception {
         logger_ = log;
        baseDir_ = baseDir;
         if (reportDataFile != null) {
             FileUtils.forceDelete(reportDataFile);
             reportDataFile.getParentFile().mkdirs();
             reportDataWriter_ = new FileWriter(reportDataFile);
         }
     }
 
     @Override()
     public void debugMessage(String msg) {
         logger_.debug(msg);
     }
 
     @Override
     public void setDebug(boolean v) {
         if (logger_.isDebugEnabled() != v) {
             logger_.warn("can't change logger level to debug = " + v);
         }
     }
 
     @Override
     public boolean isDebug() {
         return logger_.isDebugEnabled();
     }
 
     @Override
     public void setVerbose(boolean v) {
         if (logger_.isInfoEnabled() != v) {
             logger_.warn("can't change logger level to info = " + v);
         }
     }
 
     /**
      * Returns true if Spoon is in verbose mode.
      */
     @Override
     public boolean isVerbose() {
         return logger_.isInfoEnabled();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void report(Processor processor, Severity severity, CtElement element, String message) {
         reportToLog(processor, severity, element, message);
         reportToWriter(processor, severity, element, message);
     }
 
     @SuppressWarnings("unchecked")
     protected void reportToLog(Processor processor, Severity severity, CtElement element, String message) {
         String where = "";
         if (element != null) {
             where = String.format("%s@[%d,%d]:",
                     element.getPosition().getFile().getAbsolutePath(),
                     element.getPosition().getLine(),
                     element.getPosition().getColumn()
                     );
         }
         String msg = String.format("%s >> %s %s",
                 (processor == null) ? "" : processor.getClass().getSimpleName(),
                 where,
                 message
                 );
         if (Severity.ERROR.equals(severity)) {
             errorCount_++;
             logger_.error(msg);
         } else if (Severity.WARNING.equals(severity)) {
             warningCount_++;
             logger_.warn(msg);
         } else {
             logger_.info(msg);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void reportToWriter(Processor processor, Severity severity, CtElement element, String message) {
         if (reportDataWriter_ == null) {
             return;
         }
         try {
             String dataLine = String.format("%s;%s;%d;%s;%s\n",
                     (processor == null) ? "" : processor.getClass().getSimpleName(),
                      (element != null)?element.getPosition().getCompilationUnit().getMainType().getQualifiedName() : "null",
                      (element != null)?element.getPosition().getLine(): -1,
                      severity.name(),
                      message
                     );
             reportDataWriter_.write(dataLine);
         } catch (RuntimeException exc) {
             throw exc;
         } catch (Exception exc) {
             throw new RuntimeException("wrap: " + exc.getMessage(), exc);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void report(Processor processor, Severity severity, String message) {
         report(processor, severity, null, message);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void report(Processor processor, Severity severity, CtElement element, String message, ProblemFixer... fix) {
         // Fix not (yet) used in command-line mode
         report(processor, severity, element, message);
     }
 
     /**
      * This method should be called to report the end of the processing.
      */
     @Override
     public void reportEnd() {
         IOUtil.close(reportDataWriter_);
         if (isVerbose()) {
             logger_.info(String.format("end of processing: %d warning(s), %d error(s)", warningCount_, errorCount_));
         }
     }
 
     @Override
     public void reportProgressMessage(String message) {
         logger_.info(message);
     }
 
     public boolean hasWarning() throws Exception {
         return warningCount_ > 0;
     }
 
     public boolean hasError() throws Exception {
         return errorCount_ > 0;
     }
    
    public String getSourcePath()
    {
        return baseDir_.getPath();
    }
 }
