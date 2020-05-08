 package hudson.plugins.sctmexecutor.publisher;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 import hudson.plugins.sctmexecutor.exceptions.SCTMArchiverException;
 import hudson.plugins.sctmexecutor.publisher.handler.OutputXMLParserHandler;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Recorder;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.commons.io.filefilter.RegexFileFilter;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class SCTMResultArchiver extends Recorder implements Serializable {
   private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor");
 
   @DataBoundConstructor
   public SCTMResultArchiver() {
   }
 
   @Override
   public BuildStepMonitor getRequiredMonitorService() {
     return BuildStepMonitor.NONE;
   }
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
       throws InterruptedException, IOException {
     final SCTMTestSuiteResult rootSuite = new SCTMTestSuiteResult("root");
 
     FilePath workspace = build.getWorkspace();
     FilePath resultRootPath = workspace.child(String.format("SCTMResults/%d", build.getNumber()));
     List<FilePath> resultFiles;
     try {
       resultFiles = findAllResultFiles(resultRootPath);
     } catch (SCTMArchiverException e) {
       listener.fatalError(MessageFormat.format("FATAL ERROR: Cannot find any result files, because: {0}",
           e.getMessage()));
       LOGGER.log(Level.SEVERE, "output.xml files not found", e);
       build.setResult(Result.FAILURE);
       return false;
     }
 
     ExecutorService executor = Executors.newFixedThreadPool(4);
     for (final FilePath resultFilePath : resultFiles) {
       executor.execute(new Runnable() {
         @Override
         public void run() {
           try {
             SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
             OutputXMLParserHandler handler = new OutputXMLParserHandler(rootSuite, resultFilePath.getParent()
                 .getParent().getName());
             parser.parse(resultFilePath.read(), handler);
           } catch (Exception e) {
             listener.fatalError(MessageFormat.format("FATAL ERROR: Result cannot be parsed, because: {0}",
                 e.getMessage()));
             LOGGER.log(Level.SEVERE, "SCTMResult cannot be parsed", e);
           }
 
         }
       });
     }
 
     executor.shutdown();
     executor.awaitTermination(10, TimeUnit.MINUTES);
    rootSuite.calculateConfigurationResults();
 
     build.getActions().add(new SCTMResultAction(build, rootSuite, listener));
     build.setResult(rootSuite.getFailCount() > 0 ? Result.UNSTABLE : Result.SUCCESS);
     return true;
   }
 
   private List<FilePath> findAllResultFiles(FilePath rootPath) throws SCTMArchiverException, InterruptedException {
     List<FilePath> resultFilePath = new ArrayList<FilePath>();
     try {
       if (rootPath.isDirectory()) { // just to be sure we are in a directory
         List<FilePath> list = rootPath.list(new RegexFileFilter("output.xml"));
         resultFilePath.addAll(list);
         list = rootPath.listDirectories();
         for (FilePath dirPath : list) {
           List<FilePath> childrens = findAllResultFiles(dirPath);
           resultFilePath.addAll(childrens);
         }
       }
     } catch (IOException e) {
       throw new SCTMArchiverException(e);
     }
 
     return resultFilePath;
   }
 
   @Override
   public SCTMResultArchiverDescriptor getDescriptor() {
     return (SCTMResultArchiverDescriptor) Hudson.getInstance().getDescriptor(this.getClass());
   }
 
 }
