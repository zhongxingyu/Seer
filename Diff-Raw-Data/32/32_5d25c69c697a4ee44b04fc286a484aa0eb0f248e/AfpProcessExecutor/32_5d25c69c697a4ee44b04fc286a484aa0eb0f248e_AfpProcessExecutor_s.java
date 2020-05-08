 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.afp.executors;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 
 import org.amanzi.awe.afp.Activator;
 import org.amanzi.awe.afp.AfpEngine;
 import org.amanzi.awe.afp.exporters.AfpExporter;
 import org.amanzi.awe.afp.loaders.AfpOutputFileLoader;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 
 /**
  * Afp Process Executor
  * Executes the Afp Process, and shows the output, errors and progress on Awe Console
  * User can terminate the process at any time from progress bar.
  * 
  * @author Rahul
  *
  */
 public class AfpProcessExecutor extends Job {
 	
 	/** Process to execute the command*/
 	private Process process;
 	private AfpProcessProgress progress;
 	
 	/** Flag whether process is completed*/
 	private boolean jobFinished = false;
 	long progressTime =0;
 	
 	private Node afpRoot;
 	protected Transaction transaction;
 	private HashMap<String, String> parameters;
 	private Node afpDataset;
 	private IProgressMonitor progressMonitor;
 	private AfpModel model;
 	
 	public AfpProcessExecutor(String name, Node afpRoot,Node afpDataset, HashMap<String, String> parameters, AfpModel model) {
 		super(name);
 		this.afpRoot = afpRoot;
 		this.parameters = parameters;
 		this.afpDataset = afpDataset;
 		this.model = model;
 	}
 
 	public void setProgress(AfpProcessProgress progress) {
 		this.progress = progress;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public IStatus run(IProgressMonitor monitor){
 		progressMonitor = monitor;
 		monitor.beginTask("Execute Afp", 100);
         AfpExporter afpE = new AfpExporter(afpRoot, afpDataset, model);
 
 		createFiles(monitor, afpE);
 		Runtime run = Runtime.getRuntime();
 		AweConsolePlugin.info("AFP Engine .... starting");
 		try {
 			AfpEngine engine = AfpEngine.getAfpEngine();
 			
 			String path = engine.getAfpEngineExecutablePath();
 			//String command = path + " \"" + afpE.controlFileName + "\"";
 			//AweConsolePlugin.info("Executing Cmd: " + command);
 			//process = run.exec(command);
 			process = run.exec(new String[]{path,afpE.fileNames[AfpExporter.CONTROL]});
 			monitor.worked(0);
 			AweConsolePlugin.info("AFP Engine .... started");
 					
 			/**
 			 * Thread to read the stderr and display it on Awe Console
 			 */
 			Thread errorThread = new Thread("AFP stderr"){
 				@Override
 				public void run(){
 					BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 	    			String output = null;
 	    			try{
 	    				while ((output = error.readLine()) != null){
 	    					//TODO have to make it red
 	    					AweConsolePlugin.error(output);
 	    				}
 	    				error.close();
 	    				AweConsolePlugin.info("AFP stderr closed");
 	    			}catch(IOException ioe){
 	    				AweConsolePlugin.debug(ioe.getLocalizedMessage());
 	    			}
 				}
 			};
 			
 			/**
 			 * Thread to read the stdout and display it on Awe Console
 			 */
 			Thread outputThread = new Thread("AFP stdout"){
 				@Override
 				public void run(){
 					BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
 					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
 	    			String output = null;
 	    			try{
 	    				while ((output = input.readLine()) != null){
 	    					// check the progress variable
 	    					AweConsolePlugin.info(output);
 	    					checkForProgress(progressMonitor, output);
 	    				}
 	    				input.close();
 	    				writer.close();
 	    			    AweConsolePlugin.info("AFP stdout closed");
 	    			}catch(IOException ioe){
 	    				AweConsolePlugin.debug(ioe.getLocalizedMessage());
 	    			}
 	    			jobFinished = true;
 				}
 			};
 			
 			errorThread.start();
 			outputThread.start();
 			
 			/**
 			 * poll the monitor to check if the process is over
 			 * or if the user have terminated it.
 			 */
 			while (true) {
                 if (!errorThread.isAlive()) {
                     AweConsolePlugin.info("AFP Threads terminated, closing process...");
                     process.destroy();
                     break;
                 }
 				if (monitor.isCanceled()){
                     AweConsolePlugin.info("User cancelled worker, stopping AFP...");
 					process.destroy();
 					break;
 				}
 				
 				if(jobFinished){
                     AweConsolePlugin.info("AFP Finished, closing process...");
 					process.destroy();
 					// load the output file
 					break;
 				}
 				//Thread.sleep(100);
 				try {
 	                errorThread.join(1000);
 	                outputThread.join(100);
 				} catch (Exception e) {
                     AweConsolePlugin.info("Interrupted waiting for threads: " + e);
                 }
 			}
 			AweConsolePlugin.info("AFP Engine .... finished");
 			monitor.worked(100);
 			AfpOutputFileLoader afpOutputFileLoader = new AfpOutputFileLoader(afpRoot, afpE.outputFileName, afpDataset);
 			afpOutputFileLoader.run(monitor);
 			if(progress != null) {
 				progress.onProgressUpdate(1, 0, 0,0, 0, 0,0, 0, 0, 0);
 			}
 		}catch (Exception e){
 			e.printStackTrace();
 			AweConsolePlugin.exception(e);
             return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
 		}
 		finally{
 			monitor.done();
 		}
 		
 		return Status.OK_STATUS;
 	}
 	
 	/**
 	 * Writes data in files to be used as input by the C++ engine
 	 * @param monitor
 	 */
 	
 	private void createFiles(IProgressMonitor monitor, AfpExporter afpE){
 		
 		// delete output file if exist
 		File outputFile = new File(afpE.outputFileName);
 		
 		if(outputFile.exists()) {
 			try {
 				outputFile.delete();
 			} catch(Exception e) {
 				AweConsolePlugin.error("AFP Unable to delete output file");
 			}
 		}
 		
 		/** Create the carrier file */
		afpE.writeFilesNew();
 //		afpE.createCarrierFile(); 
 			
 		/** Create the neighbours file */
 //		afpE.createNeighboursFile();
 		
 		/** Create the interference file */
 //		afpE.createInterferenceFile(monitor);
 		
 		/** Create the cliques file */
 		afpE.createCliquesFile();
 		
 		/** Create the forbidden file */
 		afpE.createForbiddenFile();
 		
 		/** Create the exception file */
 		afpE.createExceptionFile();
 		
 		/** Create the control file */
 		afpE.createControlFile(parameters);
 		
 //		afpE.createParamFile();
 	}
 
 	public void onProgressUpdate(int result, long time, long remaingtotal,
 			long sectorSeperations, long siteSeperation, long freqConstraints,
 			long interference, long neighbor, long tringulation, long shadowing) {
 		
 		if(progressTime ==0) {
 			progressTime = time;
 		} else {
 			if(time == progressTime)
 				time +=10;
 		}
 		if(this.progress != null) {
 			this.progress.onProgressUpdate(result, time, remaingtotal,
 					sectorSeperations, siteSeperation, freqConstraints,
 					interference, neighbor, tringulation, shadowing);
 		}
 		
 	}
 	
 	void checkForProgress(IProgressMonitor monitor, String output) {
 		
 		if(output.startsWith("PROGRESS CoIT1Done/CoIT1")) {
 		// progress line
 		String[] tokens = output.split(",");
 		if (tokens.length >= 3) {
 			try {
 				long time =  Long.parseLong(tokens[1]);
 				long completed = Long.parseLong(tokens[2]);
 				long total = Long.parseLong(tokens[3]);
 				AweConsolePlugin
 						.info(" total " + total);
 				if (completed > total) {
 					completed = total;
 				}
 				AweConsolePlugin.info(" completed "
 						+ completed);
 				
 				if(monitor != null) {
 					long per = (completed *100) /total;
 					monitor.worked((int)per);
 				}
 				onProgressUpdate(0, time *1000, total - completed,
 						0, 0, 0, 0, 0, 0, 0);
 			} catch (Exception e) {
 
 			}
 			
 		}
 	}
 	}
 	
 }
