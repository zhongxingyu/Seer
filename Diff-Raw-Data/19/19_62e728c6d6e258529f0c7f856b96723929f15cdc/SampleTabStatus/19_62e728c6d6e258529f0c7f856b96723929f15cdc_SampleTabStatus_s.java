 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.io.Files;
 
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.ProcessUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 
 public class SampleTabStatus {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     @Argument(required=true, index=0, metaVar="FTP", usage = "ftp directory")
     private String ftpDirFilename;
 
     @Argument(required=true, index=1, metaVar="SCRIPT", usage = "script directory")
     private String scriptDirFilename;
 
     @Argument(required=true, index=2, metaVar="INPUT", usage = "input filename or globs")
     private List<String> inputFilenames;
     
     @Option(name = "--threaded", usage = "use multiple threads?")
     private boolean threaded = false;
 
     @Option(name = "--agename", usage = "Age server hostname")
     private String agename = null;
 
     @Option(name = "--ageusername", usage = "Age server username")
     private String ageusername = null;
 
     @Option(name = "--agepassword", usage = "Age server password")
     private String agepassword = null;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public SampleTabStatus(){
         Properties ageProperties = new Properties();
         try {
             ageProperties.load(SampleTabStatus.class.getResourceAsStream("/age.properties"));
         } catch (IOException e) {
             log.error("Unable to read resource age.properties");
             e.printStackTrace();
         }
         this.agename = ageProperties.getProperty("hostname");
         this.ageusername = ageProperties.getProperty("username");
         this.agepassword = ageProperties.getProperty("password");
     }
     
     private void toDatabase(List<File> inputFiles){
         List<File> ageFiles = new ArrayList<File>();
         for(File inputFile : inputFiles) {
             log.info("Adding to database "+inputFile);
             /*
             File ageDir = new File(inputFile, "age");
             File ageFile = new File(ageDir, inputFile.getName()+".age.txt");
             
             File loadDirFile = new File(inputFile, "load");
             File loadSuccessFile = new File(loadDirFile, inputFile.getName()+".SUCCESS");
             
             if (ageFile.exists()){
                 //if it has never been loaded, or it is loaded but out of date
                 if (!loadSuccessFile.exists() || ageFile.lastModified() > loadSuccessFile.lastModified()){
                     ageFiles.add(ageDir);
                 }
             }*/
         }
         /*
         File scriptDir = new File(scriptDirFilename);
         File scriptFile = new File(scriptDir, "AgeTab-Loader.sh");
         
         File tempDir = Files.createTempDir();
         
         String mainCommand = scriptFile.getAbsolutePath() 
             + " -s -m -i -e -mmode"
             + " -o "+tempDir.getAbsolutePath()
             + " -u "+ageusername
             + " -p "+agepassword
             + " -h \""+agename+"\""; 
         
         //because of shell maximum command length
         //(which you can find by running 'getconf ARG_MAX')
         //only process some files at a time
         //approximate max length is at least 100,000 characters
         
         String commandRoot = mainCommand;
         for(File ageDir : ageFiles){
             if (mainCommand.length() + ageDir.getAbsolutePath().length()+1 < 100000){
                 mainCommand = mainCommand+" "+ageDir.getAbsolutePath();
             } else {
                 //command is too long. run and reset.
                 ProcessUtils.doCommand(mainCommand, null);
                 mainCommand = commandRoot+" "+ageDir.getAbsolutePath();
             }
         }
         
         //now all of them should be loaded, but we have to put the load reports 
         //back in the right directories.
         for(File inputFile : inputFiles){
             
             File successFile = new File(tempDir, inputFile.getName()+".SUCCESS");
             File errorFile = new File(tempDir, inputFile.getName()+".ERROR");
             
             File loadDirFile = new File(inputFile, "load");
             File loadSuccessFile = new File(loadDirFile, inputFile.getName()+".SUCCESS");
             File loadErrorFile = new File(loadDirFile, inputFile.getName()+".ERROR");
             
             if (successFile.exists()){
                 try {
                     FileUtils.move(successFile, loadSuccessFile);
                 } catch (IOException e) {
                     log.error("Unable to move "+successFile);
                     e.printStackTrace();
                 }
             }
 
             if (errorFile.exists()){
                 try {
                     FileUtils.move(errorFile, loadErrorFile);
                 } catch (IOException e) {
                     log.error("Unable to move "+errorFile);
                     e.printStackTrace();
                 }
             }
         }
         
         //and finally, clean up
         //Note due to issues described in http://code.google.com/p/guava-libraries/issues/detail?id=365
         //this is not totally secure against concurrent modification.
         for (File tempFile : tempDir.listFiles()){
             if (!tempFile.delete()){
                 log.error("Unable to delete "+tempFile);
             }
         }
         if (!tempDir.delete()){
             log.error("Unable to delete "+tempDir);
         }
         */
     }
     
     private void toFTP(List<File> inputFiles){
         File ftpDir = new File(ftpDirFilename);
         for(File inputFile : inputFiles){
             log.info("Adding to ftp "+inputFile);
             /*
             String accession = inputFile.getName();
             File ftpSubDir = new File(ftpDir, SampleTabUtils.getPathPrefix(accession)); 
             File ftpSubSubDir = new File(ftpSubDir, accession);
             File ftpFile = new File(ftpSubSubDir, "sampletab.txt");
             
             File sampletabFile = new File(inputFile, "sampletab.txt");
             
             if (!ftpFile.exists() && sampletabFile.exists()){
                 try {
                     FileUtils.copy(sampletabFile, ftpFile);
                     ftpFile.setLastModified(sampletabFile.lastModified());
                 } catch (IOException e) {
                     log.error("Unable to copy to FTP "+ftpFile);
                     e.printStackTrace();
                 }
             }
             
             //also need to try to remove private tag from database
             //not sure what will happen if it doesn't have the tag
             File scriptDir = new File(scriptDirFilename);
             File scriptFile = new File(scriptDir, "TagControl.sh");
 
             String command = scriptFile.getAbsolutePath() 
                 + " -u "+ageusername
                 + " -p "+agepassword
                 + " -h \""+agename+"\"" 
                 + " -r Security:Private"
                 + " -i "+inputFile.getName();
 
             ProcessUtils.doCommand(command, null);
             */
         }
     }
     
     public static void main(String[] args) {
         new SampleTabStatus().doMain(args);
     }
 
     public void doMain(String[] args) {
         CmdLineParser parser = new CmdLineParser(this);
         try {
             // parse the arguments.
             parser.parseArgument(args);
             // TODO check for extra arguments?
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             help = true;
         }
 
         if (help) {
             // print the list of available options
             parser.printSingleLineUsage(System.err);
             System.err.println();
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
         
 
         log.info("Looking for input files");
         List<File> inputFiles = new ArrayList<File>();
         for (String inputFilename : inputFilenames){
             inputFiles.addAll(FileUtils.getMatchesGlob(inputFilename));
         }
         log.info("Found " + inputFiles.size() + " input files");
         //TODO no duplicates
         Collections.sort(inputFiles);
 
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
         
         for (File inputFile : inputFiles) {
         	if (!inputFile.isDirectory()){
         		inputFile = inputFile.getAbsoluteFile().getParentFile();
         	}
     		if (inputFile == null){
     			continue;
     		}
    		inputFile = new File(inputFile, "sampletab.txt");
             Runnable t = new SampleTabStatusRunnable(inputFile, ftpDirFilename);
             if (threaded){
                 pool.execute(t);
             } else {
                 t.run();
             }
     		
         }
         
         // run the pool and then close it afterwards
         // must synchronize on the pool object
         synchronized (pool) {
             pool.shutdown();
             try {
                 // allow 24h to execute. Rather too much, but meh
                 pool.awaitTermination(1, TimeUnit.DAYS);
             } catch (InterruptedException e) {
                 log.error("Interuppted awaiting thread pool termination");
                 e.printStackTrace();
             }
         }
         
         //remove from FTP
         for(File inputFile : SampleTabStatusRunnable.toRemoveFromFTP){
             log.info("Removing from ftp "+inputFile);
             /*
     		File ftpDir = new File(ftpDirFilename);
         	File ftpSubDir = new File(ftpDir, SampleTabUtils.getPathPrefix(inputFile.getName())); 
     		File ftpSubSubDir = new File(ftpSubDir, inputFile.getName());
     		File ftpFile = new File(ftpSubSubDir, "sampletab.txt");
     		
     		if (ftpFile.exists()){
 	    		if (!ftpFile.delete()){
 	    			log.error("Unable to delete from FTP "+ftpFile);
 	    		}
     		}
     		*/
         }
         
         //remove from database
         //or rather "hide from the public"
         for(File inputFile : SampleTabStatusRunnable.toRemoveFromDatabase){
             log.info("Removing from database "+inputFile);
 /*
             File scriptDir = new File(scriptDirFilename);
             File scriptFile = new File(scriptDir, "TagControl.sh");
 
             String command = scriptFile.getAbsolutePath() 
                 + " -u "+ageusername
                 + " -p "+agepassword
                 + " -h \""+agename+"\"" 
                 + " -a Security:Private"
                 + " -i "+inputFile.getName();
 
             ProcessUtils.doCommand(command, null);
             */
         }
         
         //add to database
         toDatabase(SampleTabStatusRunnable.toAddToDatabase);
         
         
         //copy to FTP
         toFTP(SampleTabStatusRunnable.toCopyToFTP);
         //because tags may have changed, need to trigger re-indexing
         //this could be done by going into and out of maintenence mode
         //once that tool is finished
         
     }
 }
