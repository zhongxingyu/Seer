 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.dom4j.DocumentException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.FTPUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.PRIDEutils;
 
 public class PRIDEcron {
     private Logger log = LoggerFactory.getLogger(getClass());
     // singlton instance
     private static PRIDEcron instance = null;
 
     private FTPClient ftp = null;
 
     private PRIDEcron() {
         // private constructor to prevent accidental multiple initialisations
     }
 
     public static PRIDEcron getInstance() {
         if (instance == null) {
             instance = new PRIDEcron();
         }
         return instance;
     }
 
 
     private void close() {
         try {
             ftp.logout();
         } catch (IOException e) {
             if (ftp.isConnected()) {
                 try {
                     ftp.disconnect();
                 } catch (IOException ioe) {
                     // do nothing
                 }
             }
         }
 
     }
 
     private File getTrimFile(File outdir, String accesssion) {
         File subdir = new File(outdir, "GPR-" + accesssion);
         File trim = new File(subdir, "trimmed.xml");
         return trim.getAbsoluteFile();
     }
     
     private class XMLProjectRunnable implements Runnable {
 
         private File subdir = null;
         private Map<String, Set<String>> subs = null;
         
         public XMLProjectRunnable(File subdir, Map<String, Set<String>> subs){
             this.subdir = subdir;
             this.subs = subs;
         }
         
         public void run() {
             if (subdir.isDirectory() && subdir.getName().matches("GPR-[0-9]+")) {
                 // get the xml file in this subdirectory
                 File xmlfile = new File(subdir, "trimmed.xml");
                 if (xmlfile.exists()) {
                     Set<String> projects;
                     try {
                         projects = PRIDEutils.getProjects(xmlfile);
                     } catch (DocumentException e){
                         return;
                     }
                     for (String project : projects) {
                         // add it if it does not exist
                         synchronized (this.subs) {
                             if (!subs.containsKey(project)) {
                                 subs.put(project, Collections.synchronizedSet(new HashSet<String>()));
                             }
                         }
                         // now put it in the mapping
                         subs.get(project).add(subdir.getName());
                     }
                 }
             }
             return;
         }
         
     }
 
     public void run(File outdir) {
         FTPFile[] files;
         try {
             ftp = FTPUtils.connect("ftp.ebi.ac.uk");
             log.info("Getting file listing...");
             files = ftp.listFiles("/pub/databases/pride/");
             log.info("Got file listing");
         } catch (IOException e) {
             System.err.println("Unable to connect to FTP");
             e.printStackTrace();
             System.exit(1);
             return;
         }
         //Pattern regex = Pattern.compile("PRIDE_Exp_Complete_Ac_([0-9]+)\\.xml\\.gz");
         Pattern regex = Pattern.compile("PRIDE_Exp_IdentOnly_Ac_([0-9]+)\\.xml\\.gz");
 
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads*2);
 
         for (FTPFile file : files) {
             String filename = file.getName();
             //do a regular expression to match and pull out accession
             Matcher matcher = regex.matcher(filename);
             if (matcher.matches()) {
                 String accession = matcher.group(1);
                 File outfile = getTrimFile(outdir, accession);
                 // do not overwrite existing files unless newer
                 Calendar ftptime = file.getTimestamp();
                 Calendar outfiletime = new GregorianCalendar();
                 outfiletime.setTimeInMillis(outfile.lastModified());
                 if (!outfile.exists() || ftptime.after(outfiletime)) {
                     pool.execute(new PRIDEXMLFTPDownload(accession, outfile, false));
                 }
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
         
         log.info("Completed downloading, starting parsing...");
 
         // now that all the files have been updated, parse them to extract the relevant data
         Map<String, Set<String>> subs = new HashMap<String, Set<String>>();
         subs = Collections.synchronizedMap(subs);
         
         //create a new thread pool since the last one was shut down
         pool = Executors.newFixedThreadPool(nothreads);        
         
         for (File subdir : outdir.listFiles()) {
             pool.execute(new XMLProjectRunnable(subdir, subs));
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
         
         //5 min pause to watch where file descriptors are used
         log.info("Parsing completed, starting output to disk...");
         try {
			Thread.sleep((long) 300.0);
 		} catch (InterruptedException e) {
 			//do nothing, carry on as normal
 		}
         
         // at this point, subs is a mapping from the project name to a set of BioSample accessions
         // output them to a file
         File projout = new File(outdir, "projects.tab.txt");
         BufferedWriter projoutwrite = null; 
         try {
             projoutwrite = new BufferedWriter(new FileWriter(projout));
             synchronized (subs) {
                 // sort them to put them in a sensible order
                 List<String> projects = new ArrayList<String>(subs.keySet());
                 Collections.sort(projects);
                 for (String project : projects) {
 
                     projoutwrite.write(project);
                     projoutwrite.write("\t");
                     List<String> accessions = new ArrayList<String>(subs.get(project));
                     Collections.sort(accessions);
                     for (String accession : accessions) {
 
                         projoutwrite.write(accession);
                         projoutwrite.write("\t");
                     }
 
                     projoutwrite.write("\n");
                 }
             }
         } catch (IOException e) {
             log.error("Unable to write to " + projout);
             e.printStackTrace();
             System.exit(1);
             return;
         } finally {
             if (projoutwrite != null){
                 try {
                     projoutwrite.close();
                 } catch (IOException e) {
                     //failed within a fail so give up
                     log.error("Unable to close file writer " + projout);
                     
                 }
             }
         }
         
         //TODO hide files that have disappeared from the FTP site.
     }
 
     public static void main(String[] args) {
         if (args.length < 1) {
             System.err.println("Must provide the following paramters:");
             System.err.println("  PRIDE local directory");
             System.exit(1);
             return;
         }
         
         String path = args[0];
         File outdir = new File(path);
 
         if (outdir.exists() && !outdir.isDirectory()) {
             System.err.println("Target is not a directory");
             System.exit(1);
             return;
         }
 
         if (!outdir.exists())
             outdir.mkdirs();
 
         getInstance().run(outdir);
         // tidy up ftp connection
         getInstance().close();
     }
 }
