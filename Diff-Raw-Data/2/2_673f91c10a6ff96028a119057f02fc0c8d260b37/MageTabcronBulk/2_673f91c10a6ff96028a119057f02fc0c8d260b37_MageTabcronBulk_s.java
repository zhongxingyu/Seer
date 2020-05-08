 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class MageTabcronBulk {
 	private Logger log = LoggerFactory.getLogger(getClass());
 	// singlton instance
 	private static MageTabcronBulk instance = null;
 
 	private MageTabcronBulk() {
 		// private constructor to prevent accidental multiple initialisations
 	}
 
 	public static MageTabcronBulk getInstance() {
 		if (instance == null) {
 			instance = new MageTabcronBulk();
 		}
 		return instance;
 	}
 
 	private boolean doCommand(String command){
         log.debug(command);
         
         ArrayList<String> bashcommand = new ArrayList<String>();
         bashcommand.add("/bin/bash");
         bashcommand.add("-c");
        bashcommand.add(command);
         
         ProcessBuilder pb = new ProcessBuilder();
         pb.command(command);
 
         Process p;
         try {
 			p = pb.start();
             synchronized (p) {
                 p.waitFor();
             }
 		} catch (IOException e) {
 			System.err.println("Error running "+command);
 			e.printStackTrace();
 			return false;
 		} catch (InterruptedException e) {
 			System.err.println("Error running "+command);
 			e.printStackTrace();
 			return false;
 		}
         return true;
 		
 	}
 	
 	public void run(File dir, File scriptdir) {
 		dir = dir.getAbsoluteFile();
 		scriptdir = scriptdir.getAbsoluteFile();
 		
 		for (File subdir : dir.listFiles()){
 			if (subdir.isDirectory()){
 				String idffilename = (subdir.getName().replace("GAE-", "E-"))+".idf.txt";
 				File idffile = new File(subdir, idffilename);
 				File sampletabpre = new File(subdir, "sampletab.pre.txt");
 				File sampletab = new File(subdir, "sampletab.txt");
 				File sampletabtoload = new File(subdir, "sampletab.toload.txt");
 				File age = new File(subdir, "age");
 
 				if (!idffile.exists()){
 					continue;
 				}
 				
 				if (!sampletabpre.exists()){
 					//convert idf/sdrf to sampletab.pre.txt 
 					File script = new File(scriptdir, "MageTabToSampleTab.sh");
 			        String bashcom = script+" "+idffile+" "+sampletabpre;
 			        if (! doCommand(bashcom))
 		        		return;
 				}
 				
 				//accession sampletab.pre.txt to sampletab.txt
 				if (!sampletab.exists()){
 					//TODO hardcoding bad
 					File script = new File(scriptdir, "SampleTabAccessioner.sh");
 			        String bashcom = script
 							+ " --input " + sampletabpre
 							+ " --output " + sampletab
 							+ " --hostname mysql-ae-autosubs-test.ebi.ac.uk" 
 							+ " --port 4340" 
 							+ " --database autosubs_test" 
 							+ " --username admin" 
 							+ " --password edsK6BV6" ;
 			        if (! doCommand(bashcom))
 		        		return;
 				}
 				
 				//preprocess to load
 				if (!sampletabtoload.exists()){
 					File script = new File(scriptdir, "SampleTabToLoad.sh");
 			        String bashcom = script
 							+ " --input " + sampletab
 							+ " --output " + sampletabtoload
 							+ " --hostname mysql-ae-autosubs-test.ebi.ac.uk" 
 							+ " --port 4340" 
 							+ " --database autosubs_test" 
 							+ " --username admin" 
 							+ " --password edsK6BV6" ;
 			        if (! doCommand(bashcom))
 		        		return;
 				}
 				
 				//convert to age
 				if (!age.exists()){
 					File script = new File(scriptdir, "SampleTab-to-AGETAB.sh");
 			        String bashcom = script
 			        		+ " -o "+age
 			        		+ " "+sampletabtoload;
 			        if (! doCommand(bashcom))
 		        		return;
 				}
 				
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		if (args.length < 1) {
 			System.err.println("Must provide the following paramters:");
 			System.err.println("  ArrayExpress local directory");
 			System.err.println("  script directory");
 			System.exit(1);
 			return;
 		}
 		
 		File outdir = new File(args[0]);
 
 		if (outdir.exists() && !outdir.isDirectory()) {
 			System.err.println("Target is not a directory");
 			System.exit(1);
 			return;
 		}
 
 		if (!outdir.exists())
 			outdir.mkdirs();
 		
 
 		File scriptdir = new File(args[1]);
 		
 		getInstance().run(outdir, scriptdir);
 	}
 }
