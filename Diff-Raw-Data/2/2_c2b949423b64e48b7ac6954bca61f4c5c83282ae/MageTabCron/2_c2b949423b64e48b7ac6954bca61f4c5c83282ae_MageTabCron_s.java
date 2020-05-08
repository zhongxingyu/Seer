 package uk.ac.ebi.fgpt.sampletab.arrayexpress;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.fgpt.sampletab.utils.ConanUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.FTPUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.ProcessUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 
 public class MageTabCron {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     @Argument(required=true, index=0, metaVar="OUTPUT", usage = "output directory")
     private String outputDirName;
     
    @Option(name = "--threaded", usage = "use multiple threads?")
     private boolean threaded = false;
 
     @Option(name = "--no-conan", usage = "do not trigger conan loads?")
     private boolean noconan = false;
     
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private FTPClient ftp = null;
 
 	private MageTabCron() {
 	}
 
 	private void close() {
 		try {
 			ftp.logout();
             ftp = null;
 		} catch (IOException e) {
 			if (ftp.isConnected()) {
 				try {
 					ftp.disconnect();
 				} catch (IOException ioe) {
 					// do nothing
 				}
 			}
 			ftp = null;
 		}
 
 	}
 	
 	private class CurlDownload implements Runnable {
 		private final String url;
 		private final String filename;
 		
         public CurlDownload(String url, String filename){
             this.url = url;
             this.filename = filename;
         }
         
         public void run(){
             log.info("Curl downloading "+this.filename);
 	        
 	        String bashcom = "curl -z "+filename+" -o "+filename+" "+url;
 	        
 	        ProcessUtils.doCommand(bashcom, null);        	
         }
 	}
 	
 	private boolean connectFTP(){
 	    if (ftp != null && !ftp.isConnected()){
 	        close();
 	        ftp = null;
 	    }
 	    if (ftp == null){
             try {
                 ftp = FTPUtils.connect("ftp.ebi.ac.uk");
             } catch (IOException e) {
                 log.error("Unable to connect to FTP", e);
                 return false;
             }
 	    }
 	    if (ftp.isConnected()) {
 	        return true;
 	    } else {
 	        return false;
 	    }
 	}
 
 	public void run(File outdir) {
 		FTPFile[] subdirs = null;
 
 		if (!connectFTP()){
 		    System.exit(1);
 		    return;
 		}
 		
 		String root = "/pub/databases/arrayexpress/data/experiment/";
 		try {
 			subdirs = ftp.listDirectories(root);
 		} catch (IOException e) {
 		    log.error("Unable to connect to FTP", e);
             System.exit(1);
             return;
 		}
 
 
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
         
         
         Set<String> conanProcess = new HashSet<String>();
         
         
 		if (subdirs != null) {
 			//convert the subdir FTPFile objects to string names
 			//otherwise the time it takes to process GEOD causes problems.
 			Collection<String> subdirstrs = new ArrayList<String>();
 			for (FTPFile subdir : subdirs) {
 				subdirstrs.add(subdir.getName());
 			}
 			for (String subdirstr : subdirstrs) {
 				String subdirpath = root + subdirstr + "/";
 				log.debug("working on " + subdirpath);
 
 		        if (!connectFTP()){
 		            System.exit(1);
 		            return;
 		        }
 		        
 				FTPFile[] subsubdirs = null;
 				try {
 					subsubdirs = ftp.listDirectories(subdirpath);
 				} catch (IOException e) {
 				    log.error("Unable to list subdirs " + subdirpath, e);
 					continue;
 				}
 				if (subsubdirs != null) {
 					for (FTPFile subsubdir : subsubdirs) {
 					    
 		                if (!connectFTP()){
 		                    System.exit(1);
 		                    return;
 		                }
 
 					    String submissionIdentifier = "GA"+subsubdir.getName();
 					    
                         String subsubdirpath = subdirpath + subsubdir.getName()
                                 + "/";
                         String idfpath = subsubdirpath + subsubdir.getName()
                                 + ".idf.txt";
                         //TODO fix this for cases where there are multiple or unusually names sdrf files
                         //e.g. E-GEOD-11395
                         String sdrfpath = subsubdirpath + subsubdir.getName()
                                 + ".sdrf.txt";
                         
 					    FTPFile idfFTPFile = null;
                         FTPFile sdrfFTPFile = null;
                         FTPFile[] testFTPFiles = null;
                         try {
                             testFTPFiles = ftp.listFiles(subsubdirpath);
                         } catch (IOException e) {
                             log.error("Unable to list files " + subsubdirpath, e);
                             continue;
                         }
                         for (FTPFile testFTPFile : testFTPFiles){
                             if (testFTPFile.getName().equals(subsubdir.getName()+ ".idf.txt")){
                                 idfFTPFile = testFTPFile;
                             }
                             if (testFTPFile.getName().equals(subsubdir.getName()+ ".sdrf.txt")){
                                 sdrfFTPFile = testFTPFile;
                             }
                         }
                         
                         if (idfFTPFile == null){
                             log.error("Unable to find file " + idfpath);
                             continue;
                         }
                         if (sdrfFTPFile == null){
                             log.error("Unable to find file " + sdrfpath);
                             continue;
                         }
                         
                         
 						File outsubdir = new File(outdir, submissionIdentifier);
 						if (!outsubdir.exists())
 							outsubdir.mkdirs();
 						File outidf = new File(outsubdir, subsubdir.getName()
 								+ ".idf.txt");
 						File outsdrf = new File(outsubdir, subsubdir.getName()
 								+ ".sdrf.txt");
 
                         Calendar ftpidftime = idfFTPFile.getTimestamp();
                         Calendar outidftime = new GregorianCalendar();
                         outidftime.setTimeInMillis(outidf.lastModified());
                         Calendar ftpsdrftime = sdrfFTPFile.getTimestamp();
                         Calendar outsdrftime = new GregorianCalendar();
                         outsdrftime.setTimeInMillis(outsdrf.lastModified());
 
                         //rather than using the java FTP libraries - which seem to
                         //break quite often - use curl. Sacrifices multiplatformness
                         //for reliability.
 						if (!outidf.exists() || ftpidftime.after(outidftime)){
 	                        Runnable t = new CurlDownload("ftp://ftp.ebi.ac.uk"+idfpath, outidf.getAbsolutePath());
 	                        if (threaded){
 	                            pool.execute(t);
 	                        } else {
 	                            t.run();
 	                        }
 	                        conanProcess.add(submissionIdentifier);
 						}
                         if (!outsdrf.exists() || ftpsdrftime.after(outsdrftime)){
                             //TODO fix where SDRF does not match this file pattern
                             Runnable t = new CurlDownload("ftp://ftp.ebi.ac.uk"+sdrfpath, outsdrf.getAbsolutePath());
                             if (threaded){
                                 pool.execute(t);
                             } else {
                                 t.run();
                             }
                             if (!noconan)
                                 conanProcess.add(submissionIdentifier);
                         }
 					}
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
                 log.error("Interuppted awaiting thread pool termination", e);
             }
         }
 
         
         //tell conan to process those files that have changed
         
         for (String submissionIdentifier : conanProcess){
             if (!noconan) {
                 try {
                     ConanUtils.submit(submissionIdentifier, "BioSamples (AE)");
                 } catch (IOException e) {
                     log.warn("Problem submitting to Conan "+submissionIdentifier, e);
                 }
             }
         }
         
                
         
         //for each ae sampletab
         // check if it is on the ae ftp
         // if not, then put publication date in the far future
         log.info("Starting deleted processing");
         File[] stsubdirs = outdir.listFiles();
         Arrays.sort(stsubdirs);
         for(File subdir : stsubdirs){
             
             if (!connectFTP()){
                 System.exit(1);
                 return;
             }
             
             String submission = subdir.getName();
             
             //extract the middle part of the accession - the array-express pipeline
             String prefix = submission.substring(4);
             prefix = prefix.substring(0, prefix.indexOf("-"));
             
             //get the array express accession - start with AE-
             String aename = submission.substring(2);
             
             boolean onAEFTP = false;
             
             try {
                 if (ftp.listFiles(root + prefix+"/"+aename+"/"+aename+".idf.txt").length > 0){
                     onAEFTP = true;
                     log.debug("On FTP "+aename);
                 } else {
                     log.debug("Not on FTP "+aename);
                 }
             } catch (IOException e) {
                 log.error("Problem accessing FTP.", e);
                 close();
                 continue;
             }
             
             if (!onAEFTP){
                 //TODO multi-thread this
                 File sampletabFile = new File(subdir, "sampletab.pre.txt");
                 
                 if (sampletabFile.exists()){
                     log.warn("Hiding deleted experiment "+submission);
                     boolean doConan = false;
                     try {
                         SampleTabUtils.releaseInACentury(sampletabFile);
                         doConan = true;
                     } catch (IOException e) {
                         log.error("Problem with "+sampletabFile, e);
                     } catch (ParseException e) {
                         log.error("Problem with "+sampletabFile, e);
                     }
                     //trigger conan to complete processing
                     if (!noconan && doConan) {
                         try {
                             ConanUtils.submit(submission, "BioSamples (AE)", 1);
                         } catch (IOException e) {
                             log.warn("Problem submitting to Conan "+submission, e);
                         }
                     }
                 }
             }
         }
 	}
 
 	public static void main(String[] args) {
         new MageTabCron().doMain(args);
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
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
         
 		File outdir = new File(this.outputDirName);
 
 		if (outdir.exists() && !outdir.isDirectory()) {
 			System.err.println("Target is not a directory");
 			System.exit(1);
 			return;
 		}
 
 		if (!outdir.exists())
 			outdir.mkdirs();
 
 		this.run(outdir);
 		// tidy up ftp connection
 		this.close();
 	}
 }
