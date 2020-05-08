 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 23rd October 2009
  *
  * Changelog:
  * - 23/10/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig.control;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Abstract class for batch control. Batch control in Sahara is run by 
  * invoking a system executable which does the batch control and
  * (presumably) generates a results file. The following methods
  * should be overridden for rig specific batch control.
  * <ul>
  *     <li><code>init()</code> - Should setup the command, arguments,
  *     working directory and execution environment.</li>
  *     <li><code>checkFile()</code> - Should provide a sanity check
  *     on the provided batch instruction file.</li>
  *     <li><code>sync()</code> - This is run after the completion of batch
  *     control and should do synchronisation of the generated results 
  *     files to a persistent store.</li>
  * </ul>
  * In each of the above methods, the fields <code>errorCode</code> and
  * <code>errorReason</code> should be set if they fail to provide
  * meaningful information to the requestor. Some important error
  * codes are:
  * <ul>
  *     <li><strong>11</strong> - if the batch file sanity check fails.</li>
  *     <li><strong>16</strong> - catch all for other errors. The specific
  *     meaning of this is <em>unknown error</em> but is used for lab
  *     setup and configuration errors.</li>
  * </ul>
  * 
  * <strong>NOTE:</strong>The premise of uploading code and running it on 
  * a laboratory server is inherently <strong><em>insecure.</em></strong>
  * Care must be taken with what is run, what may be generated, 
  * how it is cleaned up and with what permission the batch process is run
  * with.
  * 
  * <div align="center"><strong style="color:red;font-size:3em">YOU HAVE BEEN 
  * WARNED!</strong></div>
  */
 public abstract class AbstractBatchRunner implements Runnable
 {
     /** Batch process. */
     private Process batchProc;
     
     /** Standard out of the batch process. */
     private BufferedReader batchStdOut;
     
     /** Standard err of the batch process. */
     private BufferedReader batchStdErr;
     
     /** Buffer containing all captured batch process standard out. */
     private final StringBuffer stdOutBuffer;
     
     /** Buffer containing all captured batch process standard err. */
     private final StringBuffer stdErrBuffer;
     
     /** Command line argument to invoke. */
     protected String command;
     
     /** Command line argument to invoke. */ 
     protected List<String> commandArgs;
     
     /** File name of instruction file. */
     protected String fileName;
     
     /** User name who invoked batch control. */
     protected String username;
     
     /** Batch process working directory base (base has a new folder added to
      *  be set as the batch processes CWD. */
     protected String workingDirBase;
     
     /** Batch process working directory. */
     protected String workingDir;
     
     /** Environment variables for the batch process. */
     protected Map<String, String> envMap;
     
     /** Flag to specify if batch execution has started. */
     protected boolean started;
     
     /** Flag to specify if batch execution is running. */
     protected boolean running;
     
     /** Flag to specify if batch execution failed. */
     protected boolean failed;
     
     /** Flag to specify if the batch execution was killed. */
     protected boolean killed;
     
     /** Batch process exit code. */
     protected int exitCode;
     
     /* File test error information. */
     /** Invocation error code (e.g. if file test failed). */
     protected int errorCode;
     
     /** Invocation error reason. */
     protected String errorReason;
     
     /** Results files. */
     protected List<String> resultsFiles;
     
     /** Logger. */
     protected ILogger logger;
     
     /**
      * Constructor.
      * 
      * @param file uploaded instruction file
      * @throws IllegalArgumentException if the <code>file</code> or \
      *         </code>user>/code> parameters are <code>null</code>
      */
     public AbstractBatchRunner(final String file, final String user)
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.logger.debug("Creating a Batch Runner.");
         
         if (file == null)
         {
             this.logger.error("An null parameter was passed as the uploaded instruction file path.");
             throw new IllegalArgumentException("file parameter must not be null.");
         }
         
         if (user == null)
         {
             this.logger.error("An null parameter was passed as the batch initating username.");
             throw new IllegalArgumentException("user parameter must not be null.");
         }
         
         
         this.fileName = file;
         this.username = user;
         this.commandArgs = new ArrayList<String>();
         this.envMap = new HashMap<String, String>();
         
         this.stdOutBuffer = new StringBuffer();
         this.stdErrBuffer = new StringBuffer();
     }
     
     /**
      * Processes the batch control invocation performing the following steps, 
      * calling the listed methods:
      * <ol>
      *      <li><strong><code>init()</code></strong> - Command, argument, 
      *      working directory and environment setup.</li>
      *      <li><strong><code>checkFile()</code></strong> - Instruction file
      *      test.</li>
      *      <li><strong><code>invoke()</code></strong> - Process creation.
      *      </li>
      *      <li><strong><code>sync()</code></strong> - Generated file 
      *      synchronisation.<li>
      *      <li><strong><code>clean()</code></strong> - Invocation 
      *      cleanup.</li>
      * </ol>
      * If a preceding method fails (returns <code>false</code>), successive 
      * methods in the stack are not called, except <code>cleanup()</code>
      * which is always called.
      */
     @Override
     public void run()
     {
         try
         {
             if (!this.init()) // Batch initialisation
             {
                 this.logger.warn("Batch process initialisation has failed.");
                 this.failed = true;
                 return;
             }
             else if (!this.checkFile()) // Check batch file
             {
                 this.logger.warn("Uploaded batch file has failed the instruction file sanity test.");
                 this.failed = true;
             }          
             else if (!this.invoke()) // Invoke command and create process
             {
                 this.logger.warn("Batch control invocation has failed.");
                 this.failed = true;
                 this.running = false;
                 return;
             }
             else
             {
                 this.exitCode = this.batchProc.waitFor(); // Blocks up process completion.
                 this.logger.info("The batch control process terminated with error code " + this.exitCode + " at " +
                         this.getTimeStamp('/', ' ', ':') + ".");
             }
             
             this.running = false;
             this.failed = false;
             
             if (!this.failed)
             {            
                 /* Find out the list of results files. */            
                 this.detectResultsFiles();
                 
                 /* Synchronise. */
                 if (!this.sync())
                 {
                     this.logger.warn("File syncronisation has failed.");
                 }
             }
         }
         catch (Exception ex)
         {
             this.logger.warn("Batch control failed with exception of type " + ex.getClass().getName() + " and with " +
             		"message " + ex.getMessage());
         }
         finally
         {
             /* Cleanup. */
             this.cleanup();
         }
     }
 
  
     
     /**
      * Sets the required batch control process variables. The variables that
      * should be set in this method are:
      * 
      * <ul>
      *     <li><strong><code>command</code></strong> - The batch command name
      *     executable name.</li>
      *     <li><strong><code>commandArgs</code></strong> - The command line 
      *     arguments to provide for the command. The arguments are provided
      *     in the same order as they are entered into the
      *     <code>commandArgs</code> list. <strong>NOTE:</strong> You should
      *     add the <code>fileName</code> field as a parameter for the command
      *     since this contains an absolute reference to the uploaded 
      *     instruction file.</li>
      *     <li><strong><code>workingDirBase</code></strong> - The working
      *     directory base for the batch command process. If not set, the default
      *     base is the system temporary directory. The base is appended with
      *     a new folder to contain the results files from the batch executable
      *     (<em>assuming</em> it puts the files in its current working 
      *     directory.)</li>
      *     <li><strong><code>envMap</code></strong> - Environment variable map
      *     for the batch process environment. Additions to this will be appended
      *     to the default environment variables.</li>
      * </ul>
      * 
      * @return true if successful, false otherwise
      */
     protected abstract boolean init();
     
     /**
      * Provides a sanity check on the provided batch instruction file.
      * For example, it should check the file extension, file size and/or
      * file magic number.
      * 
      * It may also check the file extension and apply a transform. For
      * example, if the extension is <code>.gz</code>, it may used 
      * <code>GZIPInputStream</code> to uncompress the file. 
      * 
      * @return true if the file passed the sanity check, false otherwise
      */
     protected abstract boolean checkFile();
     
     /**
      * Invokes the batch control executable providing the instruction files
      * as a command line parameter. Returns are invocation and does not
      * wait for completion.
      * 
      * @return true if the executable was successfully invoked
      * @throws IOException exception caused by starting the batch command
      */
     protected boolean invoke() throws IOException
     {
         /* --------------------------------------------------------------------
          * ---- 1. Set up batch process builder. ------------------------------
          * ------------------------------------------------------------------*/
         if (this.command == null)
         {
             this.errorCode = 16;
             this.errorReason = "No command has been set to run batch control (setup error).";
             this.logger.warn("No command to invoke has been set for batch control, batch invocation has failed.");
             return false;
         }
         
         final List<String> invocation = new ArrayList<String>();
         invocation.add(this.command);
         this.logger.info("Batch command that will be invoked is " + this.command);
         
         if (this.commandArgs == null)
         {
             this.logger.warn("Not providing any arguments for the batch control process. This is highly abnormal, " +
             		"so please email mdiponio@eng.uts.ed.au with your use case for not having arguments as this " +
             		"sort of breaks the premise of batch control!");
         }
         else
         {
             invocation.addAll(this.commandArgs);
             this.logger.info("Batch command arguments are " + this.commandArgs.toString());
         }
         
         final ProcessBuilder builder = new ProcessBuilder(invocation);
         
         /* --------------------------------------------------------------------
          * ---- 2. Set up working directory. ----------------------------------
          * --------------------------------------------------------------------*/
         final IConfig config = ConfigFactory.getInstance();
         if ((this.workingDirBase = config.getProperty("Batch_Working_Dir")) == null)
         {
             /* Default is a directory in the system directory. */
             this.workingDirBase = System.getProperty("java.io.tmpdir");
             if (this.workingDirBase == null)
             {
                 this.logger.warn("The temporary directory system property (java.io.tmpdir) did not find the system" +
                         "temporary directory. Going to use the current Rig Client working directory as base.");
                 this.workingDirBase = System.getProperty("user.dir");
                 this.logger.warn("Using Rig Client working directory " + this.workingDirBase + " as batch control " +
                         "working directory base.");
             }
         }
         else
         {
             this.logger.info("User set batch working directory base is " + this.workingDirBase);
         }
         this.workingDirBase = new File(this.workingDirBase).getCanonicalPath();
         
         if (Boolean.parseBoolean(config.getProperty("Batch_Create_Nested_Dir", "true")))
         {
             /* Add a new folder with a timestamp as the name. */
             final String dirName = this.getTimeStamp('-', '-', '-');
             this.workingDir = this.workingDirBase + File.separatorChar + dirName;
             this.logger.debug("Creating a nested working directory with name " + dirName + ".");
             
             final File wDir = new File(this.workingDir);
             if (!wDir.mkdir())
             {
                 final File wdBase = new File(this.workingDirBase);
                 final StringBuffer buf = new StringBuffer(25);
                 buf.append("Unable to create batch process working directory (" + this.workingDir + ").");
                 if (!wdBase.exists()) buf.append(" Base " + this.workingDirBase + " does not exist. ");
                 if (!wdBase.isDirectory()) buf.append(" Base " + this.workingDirBase + " is not a directory. ");
                 if (!wdBase.canWrite()) buf.append("Base " + this.workingDirBase +  " is not writeable. ");
                 this.logger.warn(buf.toString());
                 
                 this.errorCode = 16;
                 this.errorReason = buf.toString();
                 return false;
             }
         }
         else
         {
             this.logger.debug("Not creating a nested working directory.");
             this.workingDir = this.workingDirBase;
         }
         builder.directory(new File(this.workingDir));
         this.logger.info("Batch process working directory is " + this.workingDir);
         
         /* --------------------------------------------------------------------
          * ---- 3. Set up environment variables. -------------------------------
          * ------------------------------------------------------------------*/
         final Map<String, String> env = builder.environment();
         if (Boolean.parseBoolean(config.getProperty("Batch_Flush_Env", "false")))
         {
             env.clear();
         }
         
         for (Entry<String, String> e : this.envMap.entrySet())
         {
             this.logger.info("Adding batch control env variable " + e.getKey() + " with value " + e.getValue());
             env.put(e.getKey(), e.getValue());
         }
         this.logger.info("Batch control environment variables: " + env.toString());
         
         /* --------------------------------------------------------------------
          * ---- 4. Test command. ----------------------------------------------
          * ------------------------------------------------------------------*/
         /* If the configured command is not absolute, test each path 
          * directory to try and find command. */
         File commFile = new File(this.command);
         if (!commFile.isAbsolute())
         {
             this.logger.info("Batch control command is not absolute. This isn't recommended but will test path for" +
             		" command file.");
             
             String path = env.get("PATH");
             if (path == null)
             {
                 for (Entry<String, String> envVar : env.entrySet())
                 {
                     if (envVar.getKey().toUpperCase().equals("PATH"))
                     {
                         path = envVar.getValue();
                         break;
                     }
                 }
             }
             if (path == null)
             {
                 this.logger.error("Path enviroment variable (path) for batch control not found, batch failing.");
                 this.errorCode = 16;
                 this.errorReason = "Path environment variable (path) not found.";
                 return false;
             }
             /* Iterate through path elements and test if the command is one of the path directories. */
             boolean found = false;
             for (String p : path.split(File.pathSeparator))
             {
                 this.logger.debug("Looking for batch command " + this.command + " in path directory " + p + ".");
                 commFile = new File(p + File.separator + this.command);
                 if (commFile.isFile())
                 {
                     this.logger.debug("Found batch command " + this.command + " in path directory " + p + ".");
                     found = true;
                     break;
                 }
             }
             
             if (!found)
             {
                 this.logger.warn("Batch command " + this.command + " not found, batch control failing.");
                 this.errorCode = 16;
                 this.errorReason = "Batch command not found as an executable.";
                 return false;
             }
             
             /* Update the process builder command with the absolute reference to the batch command. */
             final List<String> comm = builder.command();
             comm.set(0, commFile.getAbsolutePath());
         }
         
         if (!commFile.canExecute())
         {
             this.logger.warn("Do not have permission to execute batch command " + commFile.getAbsolutePath() + 
                     ". Batch control failing.");
             this.errorCode = 16;
             this.errorReason = "Batch command is not executable either not executable file or invalid permission.";
             return false;
         }
         
         /* --------------------------------------------------------------------
          * ---- 5. Invoke command. --------------------------------------------
          * ------------------------------------------------------------------*/
         this.batchProc = builder.start();
         this.batchStdOut = new BufferedReader(new InputStreamReader(this.batchProc.getInputStream()));
         this.batchStdErr = new BufferedReader(new InputStreamReader(this.batchProc.getErrorStream()));
         this.logger.info("Invoked batch command at " + this.getTimeStamp('/', ' ', ':'));
         
         this.started = true;
         this.running = true;
         return true;
     }
     
     /**
      * Detects results files by reading the list of files in the batch process
      * working directory. This may be overridden to remove superfluous
      * results files.
      */
     protected void detectResultsFiles()
     {
         final String[] files = new File(this.workingDir).list();
         if (files == null)
         {
             this.logger.info("No results files have been detected.");
             return;
         }
 
         this.resultsFiles = Arrays.asList(files);
         this.logger.info("Detected batch results files " + this.resultsFiles);
     }
     
     /**
      * This method is run after completion of the batch control executable 
      * and should do cleanup, if needed and file synchronisation to some 
      * persistent user accessible store, again if needed.
      * 
      * This is provided as a utility with a guarantee that it is run
      * <strong>only</strong> after the successful completion of the 
      * batch control executable.
      * 
      * @return true if successfully completed
      */
     protected abstract boolean sync();
     
     /**
      * Terminate the batch process.
      */
     public void terminate()
     {
         /* Do a read as the input streams may not be readable after 
          * termination. */
         this.getBatchStandardOut();
         this.getBatchStandardError();
         
         this.logger.info("Terminating batch process.");
         this.killed = true;
         this.batchProc.destroy();
     }
     
     /**
      * Cleans up a batch control invocation.
      */
     protected void cleanup()
     {
         this.logger.debug("Cleaning a batch control invocation.");
         try
         {
             if (this.batchStdErr != null) 
             {
                 this.getBatchStandardError();
                 this.batchStdErr.close();
                 this.batchStdErr = null;
             }
             if (this.batchStdOut != null)
             {
                 this.getAllStandardOut();
                 this.batchStdOut.close();
                 this.batchStdOut = null;
             }
             
             final IConfig conf = ConfigFactory.getInstance();
             if (Boolean.parseBoolean(conf.getProperty("Batch_Clean_Up", "false")) 
                     && !this.workingDir.equals(this.workingDirBase))
             {
                 this.recusiveDelete(new File(this.workingDir));
             }
             
             /* Delete instruction file. */
             final File file = new File(this.fileName);
             if (Boolean.parseBoolean(conf.getProperty("Batch_Instruct_File_Delete", "true")) && file.isFile()
                     && !file.delete())
             {
                 this.logger.warn("Failed to delete file " + file.getAbsolutePath());
             }
         }
         catch (IOException ex)
         {
             this.logger.warn("Failed to clean a batch control invocation because of error " + ex.getMessage());
         }
         
     }
     
     /**
      * Reads and returns the batch process standard output stream. 
      * <code>null</code> is returned if there is nothing to read.
      * This returns the portion of the standard output stream which is
      * between the end of the last read of the standard output stream
      * and the end of the stream.
      * <br /><br />
      * <strong>NOTE:</strong> The standard output stream is buffered
      * and may be accessed using <code>getAllStandardOut</code>.
      * 
      * @return batch process standard out 
      */
     public String getBatchStandardOut()
     {
         final StringBuffer buf = new StringBuffer();
         try
         {
             while (this.batchStdOut != null && this.batchStdOut.ready())
             {
                 buf.append(this.batchStdOut.readLine());
                 buf.append(System.getProperty("line.separator"));
             }
         }
         catch (IOException e)
         {
             this.logger.warn("Reading batch process standard out failed with error " + e.getMessage() + ".");
             return null;
         }
         
         this.logger.debug("--- Batch process standard out read at " + this.getTimeStamp('/', ' ', ':') + " ------");
         this.logger.debug(buf.toString());
         this.logger.debug("--- End batch process standard out read ------");
 
         this.stdOutBuffer.append(buf);
         return buf.toString();
     }
     
     /**
      * Returns the captured standard out of the batch process.
      * 
      * @return standard out 
      */
     public String getAllStandardOut()
     {
         this.getBatchStandardOut();
         return this.stdOutBuffer.toString();
     }
     
     /**
      * Reads and returns the batch process standard error stream. 
      * <code>null</code> is returned if there is nothing to read.
      * This returns the portion of the standard error stream which is
      * between the end of the last read of the standard error stream
      * and the end of the stream.
      * <br /><br />
      * <strong>NOTE:</strong> The standard error stream is buffered
      * and may be accessed using <code>getAllStandardErr</code>.
      * 
      * @return batch process standard error 
      */
     public String getBatchStandardError()
     {
         final StringBuffer buf = new StringBuffer();
         try
         {
             while (this.batchStdErr != null && this.batchStdErr.ready())
             {
                 buf.append(this.batchStdErr.readLine());
                 buf.append(System.getProperty("line.separator"));
             }
         }
         catch (IOException e)
         {
             this.logger.warn("Reading batch process standard error stream failed with error " + e.getMessage() + ".");
             return null;
         }
         
         this.logger.debug("--- Batch process standard err read at " + this.getTimeStamp('/', ' ', ':') + " ------");
         this.logger.debug(buf.toString());
         this.logger.debug("--- End batch process standard err read ------");
         
         this.stdErrBuffer.append(buf);
         return buf.toString();
     }
     
     /**
      * Returns the captured standard err of the batch process.
      */
     public String getAllStandardErr()
     {
         this.getBatchStandardError();
         return this.stdErrBuffer.toString();
     }
     
     /**
      * Returns <code>true</code> if a batch process has been started. Started
      * is defined as when the command is called to fork a new process. This
      * returns <code>true</code> irrespective if the batch process is
      * started and running or if the batch process is started and complete.
      * 
      * @return true if batch started, false otherwise
      */
     public boolean isStarted()
     {
         return this.started;
     }
     
     /**
      * Returns <code>true</code> if a batch process has been started and is
      * currently running. 
      * 
      * @return true if batch is running, false otherwise
      */
     public boolean isRunning()
     {
         return this.running;
     }
     
     /**
      * Returns <code>true</code> if the batch invocation attempt has failed.
      * 
      * @return true if batch failed
      */
     public boolean isFailed()
     {
        return this.failed; 
     }
     
     /**
      * Returns <code>true</code> if the batch invocation attempt has been killed.
      * 
      * @return true if batch killed
      */
     public boolean isKilled()
     {
         return this.killed;
     }
     
     /**
      * Returns the detected results files for a batch control invocation.
      * Detected results files are those that are generated in the
      * batch process working directory.
      * 
      * @return list of generated results files
      */
     public List<String> getResultsFiles()
     {
         if (this.resultsFiles == null) return null;
         
         return new ArrayList<String>(Arrays.asList(this.resultsFiles.toArray(new String[0])));
     }
     
     /**
      * Gets the exit code of the batch process. If the batch process has not
      * completed, the returned exit code is 0.
      * 
      * @return exit code
      */
     public int getExitCode()
     {
         return this.exitCode;
     }
     
     /**
      * @return the errorCode
      */
     public int getErrorCode()
     {
         return this.errorCode;
     }
 
     /**
      * @return the errorReason
      */
     public String getErrorReason()
     {
         return this.errorReason;
     }
 
     /**
      * Gets the user who invoked batch control.
      * 
      * @return batch user
      */
     public String getBatchUser()
     {
         return this.username;
     }
     
     /**
      * Gets the uploaded instruction file path.
      * 
      * @return instruction file
      */
     public String getInstructionFilePath()
     {
         return this.fileName;
     }
     
     /**
      * Gets a formatted time stamp with day, month, year, hour, minute
      * and second components separated with the <code>glue</code> parameter.
      * 
      * @param dateGlue character to append date components with
      * @param join character to join date and time with
      * @param timeGlue character to append time with
      * @return String formatted time stamp
      */
     protected String getTimeStamp(final char dateGlue, final char join, final char timeGlue)
     {
         final Calendar cal = Calendar.getInstance();
         final StringBuffer buf = new StringBuffer();
         buf.append(cal.get(Calendar.DATE));
         buf.append(dateGlue);
         buf.append(cal.get(Calendar.MONTH) + 1);
         buf.append(dateGlue);
         buf.append(cal.get(Calendar.YEAR));
         buf.append(join);
         buf.append(cal.get(Calendar.HOUR_OF_DAY));
         buf.append(timeGlue);
         buf.append(cal.get(Calendar.MINUTE));
         buf.append(timeGlue);
         buf.append(cal.get(Calendar.SECOND));
         return buf.toString();
     }
     
     /**
      * Recursively delete a directory. The directory and all its child files 
      * and elements will be deleted.
      *  
      * @param file directory or file to delete
      * @throws IOException 
      */
     private void recusiveDelete(final File file) throws IOException
     {   
         this.logger.debug("Attempting to delete " + file.getName());
         
         if (!file.getCanonicalPath().startsWith(this.workingDirBase))
         {
             this.logger.warn("Not deleting directory " + file.getCanonicalPath() + " since it does not appear to be " +
             		"located in " + this.workingDirBase);
             return;
         }

         if (file.isDirectory())
         {
             /* Delete all the nested directories and files. */
             for (File f : file.listFiles())
             {
                 if (f.isDirectory())
                 {
                     this.recusiveDelete(f);
                 }
                 else
                 {
                     if (!f.delete()) this.logger.warn("Unable to delete " + f.getCanonicalPath());
                 }
             }
         }
         
         if (!file.delete()) this.logger.warn("Unable to delete " + file.getCanonicalPath());
         
     }
 }
