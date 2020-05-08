 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.ftp.FTPReply;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.FTPUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.PRIDEutils;
 import uk.ac.ebi.fgpt.sampletab.utils.XMLUtils;
 
 public class MageTabcron {
 	private Logger log = LoggerFactory.getLogger(getClass());
 	// singlton instance
 	private static MageTabcron instance = null;
 
 	private FTPClient ftp = null;
 
 	private MageTabcron() {
 		// private constructor to prevent accidental multiple initialisations
 	}
 
 	public static MageTabcron getInstance() {
 		if (instance == null) {
 			instance = new MageTabcron();
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
 
 	public void run(File outdir) {
 		FTPFile[] subdirs = null;
 		try {
 			ftp = FTPUtils.connect("ftp.ebi.ac.uk");
 		} catch (IOException e) {
 			System.err.println("Unable to connect to FTP");
 			e.printStackTrace();
 			System.exit(1);
 			return;
 		}
 
 		String root = "/pub/databases/arrayexpress/data/experiment/";
 		try {
 			subdirs = ftp.listDirectories(root);
 		} catch (IOException e) {
 			System.err.println("Unable to connect to FTP");
 			e.printStackTrace();
 		}
 
 		
 		
 		if (subdirs != null) {
 			//convert the subdir FTPFile objects to string names
 			//otherwise the time it takes to process GEOD causes problems.
 			Collection<String> subdirstrs = new ArrayList<String>();
 			for (FTPFile subdir : subdirs) {
 				subdirstrs.add(subdir.getName());
 			}
 			for (String subdirstr : subdirstrs) {
				String subdirpath = root + subdirstr + "/";
 				log.info("working on " + subdirpath);
 
 				FTPFile[] subsubdirs = null;
 				try {
 					subsubdirs = ftp.listDirectories(subdirpath);
 				} catch (IOException e) {
 					System.err.println("Unable to list subdirs " + subdirpath);
 					e.printStackTrace();
 				}
 				if (subsubdirs != null) {
 					for (FTPFile subsubdir : subsubdirs) {
 						String subsubdirpath = subdirpath + subsubdir.getName()
 								+ "/";
 						String idfpath = subsubdirpath + subsubdir.getName()
 								+ ".idf.txt";
 						String sdrfpath = subsubdirpath + subsubdir.getName()
 								+ ".sdrf.txt";
 
 						log.info("working on " + subsubdirpath);
 
 						File outsubdir = new File(outdir, "GA"
 								+ subsubdir.getName());
 						if (!outsubdir.exists())
 							outsubdir.mkdirs();
 						File outidf = new File(outsubdir, subsubdir.getName()
 								+ ".idf.txt");
 						File outsdrf = new File(outsubdir, subsubdir.getName()
 								+ ".sdrf.txt");
 
 						//rather than using the java FTP libraries - which seem to
 						//break quite often - use curl. Sacrifices multiplatformness
 						//for reliability.
 						
 
 				        ProcessBuilder pb = new ProcessBuilder();
 				        Process pidf;
 				        Process psdrf;
 				        String bashcom;
 				        ArrayList<String> command;
 				        
 				        
 				        bashcom = "curl -z "+outidf+" -o "+outidf+" ftp://ftp.ebi.ac.uk"+idfpath;
 				        log.debug(bashcom);
 
 			            command = new ArrayList<String>();
 			            command.add("/bin/bash");
 			            command.add("-c");
 			            command.add(bashcom);
 			            pb.command(command);
 				        
 				        bashcom = "curl -z "+outsdrf+" -o "+outsdrf+" ftp://ftp.ebi.ac.uk"+sdrfpath;
 				        log.debug(bashcom);
 
 			            command = new ArrayList<String>();
 			            command.add("/bin/bash");
 			            command.add("-c");
 			            command.add(bashcom);
 			            pb.command(command);
 			            
 			            try {
 							pidf = pb.start();
 				            synchronized (pidf) {
 				                pidf.waitFor();
 				            }
 			            	psdrf = pb.start();
 				            synchronized (psdrf) {
 				            	psdrf.waitFor();
 				            }
 						} catch (IOException e) {
 							System.err.println("Error running curl");
 							e.printStackTrace();
 							System.exit(1);
 							return;
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 							System.exit(1);
 							return;
 						}
 					}
 				}
 				//restart the connection after each subdir
 				//otherwise we hit some sort of limit?
 				try {
 					ftp = FTPUtils.connect("ftp.ebi.ac.uk");
 				} catch (IOException e) {
 					System.err.println("Unable to connect to FTP");
 					e.printStackTrace();
 					System.exit(1);
 					return;
 				}
 			}
 		}
 
 		// TODO hide files that have disappeared from the FTP site.
 	}
 
 	public static void main(String[] args) {
 		if (args.length < 1) {
 			System.err.println("Must provide the following paramters:");
 			System.err.println("  ArrayExpress local directory");
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
