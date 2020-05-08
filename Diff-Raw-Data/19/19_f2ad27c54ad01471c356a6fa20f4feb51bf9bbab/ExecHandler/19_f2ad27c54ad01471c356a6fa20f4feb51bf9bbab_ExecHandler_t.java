 package edu.ucdenver.ccp.PhenoGen.driver;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import edu.ucdenver.ccp.util.FileHandler;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 /**
  * Class for running an executable.
  *  @author  Cheryl Hornbaker
  */
 
 public class ExecHandler {
 
 	private Logger log = null;
 	private String execFunctionPath = null;
 	private String[] functionArgs = null;
 	private String[] envVariables = null;
 	private String filePrefix = null;
 	private String execErrors = null;
         private int exitValue=-1;
         private String outputFileName="";
         private String errorFileName="";
         
 	public ExecHandler(String execFunctionPath, 
 				String[] functionArgs, 
 				String[] envVariables, 
 				String filePrefix) {
 
                 log = Logger.getRootLogger();
 
 		this.execFunctionPath = execFunctionPath;
 		this.functionArgs = functionArgs;
 		this.envVariables = envVariables;
 		this.filePrefix = filePrefix; 
         }
 
 	public void setErrors(String inString) {
 		this.execErrors = inString;
 	} 
 
 	public String getErrors() {
             FileHandler myFileHandler = new FileHandler();
             String[] errorFileContents=new String[0];
            execErrors="";
             try{
                 errorFileContents = myFileHandler.getFileContents(new File(errorFileName));
                execErrors="";
                for (int i=0; i<errorFileContents.length; i++) {
                        execErrors = execErrors + "\n" + errorFileContents[i];
                }
             }catch(IOException e){
                 log.error("Error reading exec_error file contents.",e);
             }
            if(!execErrors.equals("")){
                execErrors = "Error File Contents:\n\n"+execErrors;
                 String[] outputFileContents=new String[0];
                 try{
                      outputFileContents = myFileHandler.getFileContents(new File(outputFileName));
                 }catch(IOException e){
                     log.error("Error reading exec output files",e);
                 }
                execErrors = execErrors + 
                        "\n\nThe following information may be useful in determining "+
                             "where the problem occurred:\n\n OUTPUT FILE CONTENTS:\n\n";
                 for (int i=0; i<outputFileContents.length; i++) {
                         execErrors = execErrors + "\n" + outputFileContents[i];
                 }
             }
             return execErrors;
 	} 
         
         public int getExitValue(){
             return exitValue;
         }
         
 	public void runExec() throws ExecException {
 
 	        log.debug("Starting run method of ExecHandler. filePrefix = " + filePrefix + ", execFunctionPath = "+execFunctionPath);
 
 		try {
 
 	                Process p = Runtime.getRuntime().exec(
 				functionArgs, 
 				envVariables,
 				new File(execFunctionPath));
 
 			errorFileName = filePrefix +"_execErrors.txt";
 			outputFileName = filePrefix +"_execOut.out";
 
                         FileOutputStream outputStream = new FileOutputStream(outputFileName);
                         FileOutputStream errorStream = new FileOutputStream(errorFileName);
 
                         // any error message?
                         StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", errorStream);
 
                         // any output?
                         StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", outputStream);
 
                         // kick them off
                         errorGobbler.start();
                         outputGobbler.start();
 
 			int wait = p.waitFor();
                         exitValue=wait;
 			log.debug("in ExecHandler.  process completed. exit value is "+wait);
 			
                         String[] errorFileContents = null;
                 	String[] outputFileContents = null;
                 	
 			
 			
                         outputStream.flush();
                         errorStream.flush();
                       
                         outputStream.close();
                         errorStream.close();
 
                         if (p.exitValue() != 0) {
                                 
                         	/*errorFileContents = myFileHandler.getFileContents(new File(errorFileName));
                         	outputFileContents = myFileHandler.getFileContents(new File(outputFileName));
                         	for (int i=0; i<errorFileContents.length; i++) {
                                 	execErrors = execErrors + "\n" + errorFileContents[i];
                         	}
                         	execErrors = execErrors + "\n\n" + 
 					"The following information may be useful in determining "+
                                          "where the problem occurred:\n\n OUTPUT FILE CONTENTS:\n\n";
                         	for (int i=0; i<outputFileContents.length; i++) {
                                 	execErrors = execErrors + "\n" + outputFileContents[i];
                         	}*/
                                 //setErrors(execErrors);
 
                                 /*if (execErrors.length() > 0) {
                                     log.debug("got error running exec process");
                                     throw new ExecException("getErrors()");
                                 }*/
                             throw new ExecException("problem running Exec process.  Exit value was not 0.");
                 	} else {
                         	log.info("exitValue was 0 -- no errors occurred");
                 	}
                         
                         //
 			// There's some problem
 			//
 			/*if (wait != 0) {
 				
 			}*/
 
                 	
                 	
 
 		} catch(InterruptedException e) {
 			log.error("in Interrupted exception of ExecHandler while executing exec", e);
 			throw new ExecException(getErrors());
 		} catch(IOException e) {
 			log.error("in IO exception of ExecHandler while executing exec", e);
 			throw new ExecException(getErrors());
 		} catch(Exception e) {
 			log.error("in exception of ExecHandler while executing exec", e);
 			throw new ExecException(getErrors());
 		} finally {
 			log.debug("executing finally clause in ExecHandler");
 		}
 	}
 }
  
