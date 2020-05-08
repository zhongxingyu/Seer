 package edu.umich.lsa.cscs.gridsweeper;
 
 import org.ggf.drmaa.*;
 
 
 import java.io.*;
 import java.text.DateFormat;
 import java.util.*;
 
 import static edu.umich.lsa.cscs.gridsweeper.StringUtils.*;
 import static edu.umich.lsa.cscs.gridsweeper.DateUtils.*;
 import static edu.umich.lsa.cscs.gridsweeper.DLogger.*;
 
 /**
  * The GridSweeper command-line tool for job submission. Takes a .gsexp
  * XML experiment file and/or a bunch of command line options and submits
  * the resulting experiment to the grid via DRMAA.
  * Warning: begun on a houseboat in Paris. May still contain strange French bugs.
  * 
  * @author Ed Baskerville
  *
  */
 public class GridSweeper
 {
 	enum RunType
 	{
 		RUN,
 		DRY,
 		NORUN
 	}
 	
 	class CaseRun
 	{
 		ExperimentCase expCase;
 		String caseId;
 		int runNum;
 		int rngSeed;
 		
 		JobInfo jobInfo = null;
 		RunResults runResults = null;
 		
 		public CaseRun(ExperimentCase expCase, String caseId, int runNum, int rngSeed)
 		{
 			this.expCase = expCase;
 			this.caseId = caseId;
 			this.runNum = runNum;
 			this.rngSeed = rngSeed;
 		}
 		
 		public String getRunString()
 		{
 			String runStr;
 			if(caseId.equals("")) runStr = "run " + runNum;
 			else runStr = caseId + ", run " + runNum;
 			
 			return runStr;
 		}
 	}
 	
 	static String className;
 	
 	static
 	{
 		className = GridSweeper.class.toString();
 	}
 	
 	String root;
 	String pid;
 	
 	Experiment experiment;
 	RunType runType = RunType.RUN;
 	List<ExperimentCase> cases = null;
 	
 	boolean useFileTransfer = false;
 	
 	Calendar cal;
 	
 	String dateStr;
 	String timeStr;
 	String expDir;
 	
 	String email;
 	
 	String fileTransferSubpath;
 	
 	Session drmaaSession;
 	StringMap caseIdToJobIdMap;
 	Map<String, CaseRun> jobIdToRunMap;
 	
 	PrintStream msgOut;
 	
 	public GridSweeper() throws GridSweeperException
 	{
 		root = System.getenv("GRIDSWEEPER_ROOT");
 		if(root == null)
 			throw new GridSweeperException("GRIDSWEEPER_ROOT environment variable not set.");
 		
 		pid = getPid();
 		
 		cal = Calendar.getInstance();
 		
 		msgOut = System.err;
 	}
 	
 	private String getPid() throws GridSweeperException
 	{
 		String pid;
 		try
 		{
 			String getPidPath = appendPathComponent(root, "bin/gsgetpid");
 			Process pidProc = Runtime.getRuntime().exec(getPidPath);
 			
 			BufferedReader getPidReader =
 				new BufferedReader(new InputStreamReader(pidProc.getInputStream()));
 			pid = getPidReader.readLine();
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not get pid.");
 		}
 		
 		if(pid == null)
 		{
 			throw new GridSweeperException("Could not get pid.");
 		}
 		
 		return pid;
 	}
 	
 	/**
 	 * Generates experiment cases and sets up in preparation for grid submission.
 	 * Experiment results are collated in a master experiment directory,
 	 * specified in the user settings, in a subdirectory tagged with the experiment name
 	 * and date/time ({@code <name>/YYYY-MM-DD/hh-mm-ss}). If a shared filesystem is not
 	 * available, files are first staged to the experiment results directory on the
 	 * file transfer system.
 	 * Finally, a DRMAA session is established, and each case is submitted.
 	 * @throws GridSweeperException
 	 */
 	public void submitExperiment() throws GridSweeperException
 	{
 		if(runType == RunType.NORUN) return;
 		
 		email = experiment.getSettings().getSetting("EmailAddress");
 		if(email == null)
 		{
 			msgOut.println("Warning: no email address provided; "
 				+ " using local user account.");
 			email = System.getProperty("user.name");
 		}
 		
 		String expName = experiment.getName();
 		if(runType == RunType.DRY)
 		{
 			msgOut.println("Performing dry run for experiment \""
 					+ expName + "\"...");
 		}
 		else
 		{
 			msgOut.println("Running experiment \""
 					+ experiment.getName() + "\"...");	
 		}
 		
 		Settings settings = experiment.getSettings();
 
 		// Assemble cases
 		cases = experiment.generateCases();
 		
 		// Set up main experiment directory
 		setUpExperimentDirectory(settings);
 
 		// Set up directory & input files on file transfer system if asked for
 		if(runType == RunType.RUN && 
 				settings.getBooleanProperty("UseFileTransfer", false))
 		{
 			setUpFileTransfer(settings);
 		}
 		
 		// Create experiment XML in output directory
 		String xmlPath = appendPathComponent(expDir, "experiment.gsexp");
 		try
 		{
 			experiment.writeToFile(xmlPath, true);
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not write experiment XML to"
 					+ xmlPath, e);
 		}
 		
 		// Enumerate and submit cases
 		submitCases();
 		
 		// Finally, 
 		switch(runType)
 		{
 			case DRY:
 				msgOut.println("Dry run complete.");
 				break;
 			case RUN:
 				msgOut.println("Experiment submitted.");
 				break;
 		}
 		
 	}
 	
 	private void setUpExperimentDirectory(Settings settings)
 		throws GridSweeperException
 	{
 		try
 		{
 			String expsDir = expandTildeInPath(settings.getProperty("ResultsDirectory", "~/Results"));
 			
 			// First set up big directory for the whole experiment
 			// Located in <resultsDir>/<experimentName>/<experimentDate>/<experimentTime>
 			String expName = experiment.getName();
 			if(expName == null)
 			{
 				throw new GridSweeperException("Experiment name must be specified.");
 			}
 			
 			dateStr = getDateString(cal);
 			timeStr = getTimeString(cal);
 			String expSubDir = String.format("%s%s%s%s%s", expName, getFileSeparator(), dateStr, getFileSeparator(), timeStr);
 			
 			expDir = appendPathComponent(expsDir, expSubDir);
 			finer("Experiment subdirectory: " + expDir);
 
 			File expDirFile = new File(expDir);
 			expDirFile.mkdirs();
 			
 			msgOut.println("Created experiment directory \""
 					+ expDir + "\".");
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not create experiment directory "
 					+ expDir, e);
 		}
 	}
 	
 	private void setUpFileTransfer(Settings settings)
 		throws GridSweeperException
 	{
 		FileTransferSystem fts = null;
 		try
 		{
 			msgOut.println("Setting up file transfer system...");
 			
 			String className = settings.getProperty("FileTransferSystemClassName", "edu.umich.lsa.cscs.gridsweeper.FTPFileTransferSystem");
 			fts = FileTransferSystemFactory.getFactory().getFileTransferSystem(className, settings);
 			fts.connect();
 			
 			boolean alreadyExists;
 			do
 			{
 				fileTransferSubpath = UUID.randomUUID().toString();
 				alreadyExists = fts.fileExists(fileTransferSubpath);
 			}
 			while(alreadyExists);
 			
 			msgOut.println("Done setting up file transfer.");
 			
 			// If file transfer is on, make the directory
 			// and upload input files
 			StringMap inputFiles = experiment.getInputFiles();
 			
 			if(inputFiles.size() > 0)
 			{
 				msgOut.println("Uploading input files...");
 				
 				String inputDir = appendPathComponent(fileTransferSubpath, "input");
 				fts.makeDirectory(inputDir);
 				
 				for(String localPath : inputFiles.keySet())
 				{
 					String remotePath = appendPathComponent(inputDir, inputFiles.get(localPath));
 					msgOut.println("Uploading file \"" + localPath
 							+ "\" to \"" + remotePath + "\"");
 					
 					fts.uploadFile(localPath, remotePath);
 				}
 				
 				msgOut.println("Done uploading input files.");
 			}
 			
 			fts.disconnect();
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not set up file trasfer system", e);
 		}
 	}
 	
 	public void submitCases() throws GridSweeperException
 	{
 		if(runType == RunType.NORUN) return;
 		
 		try
 		{
 			// Establish DRMAA session, unless this is a dry run
 			if(runType == RunType.RUN)
 			{
 				msgOut.println("Establishing grid session");
 				drmaaSession = SessionFactory.getFactory().getSession();
 				drmaaSession.init(null);
 			}
 			
 			// Set up and run each case
 			caseIdToJobIdMap = new StringMap();
 			jobIdToRunMap = new HashMap<String, CaseRun>();
 			
 			if(cases.size() > 1)
 				msgOut.println("Submitting cases:");
 			for(ExperimentCase expCase : cases)
 			{
 				runCase(expCase);
 			}
 			if(cases.size() > 1)
 				msgOut.println("All cases submitted.");
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not run experiment", e);	
 		}
 	}
 	
 	/**
 	 * Submits a single experiment case. This means running one job for each
 	 * run of the case (one for each random seed).
 	 * @param expCase The experiment case to run.
 	 * @throws FileNotFoundException If the case directory cannot be found/created.
 	 * @throws DrmaaException If a DRMAA error occurs (in {@link #runCaseRun}).
 	 * @throws IOException If the case XML cannot be written out (in {@link #runCaseRun}).
 	 */
 	public void runCase(ExperimentCase expCase) throws FileNotFoundException, DrmaaException, IOException
 	{
 		String caseSubDir = experiment.getDirectoryNameForCase(expCase);
 		String caseDir = appendPathComponent(expDir, caseSubDir);
 		finer("Case subdirectory: " + caseDir);
 		
 		File caseDirFile = new File(caseDir);
 		caseDirFile.mkdirs();
 		
 		String caseName;
 		if(caseSubDir.equals(""))
 		{
 			caseName = experiment.getName()
 			+ " (" + dateStr + ", " + timeStr + ")";
 		}
 		else
 		{
 			caseName = experiment.getName() + " - "
 			+ caseSubDir + " (" + dateStr + ", " + timeStr + ")";
 		}
 
 		// Write XML
 		String xmlPath = appendPathComponent(caseDir, "case.gscase");
 		ExperimentCaseXMLWriter xmlWriter = new ExperimentCaseXMLWriter(
 				xmlPath, expCase, caseName);
 		xmlWriter.writeXML();
 		
 		if(!caseSubDir.equals(""))
 		{
 			msgOut.println(caseSubDir);
 		}
 		
 		// Run each individual run on the grid
 		List<Integer> rngSeeds = expCase.getRngSeeds();
 		for(int i = 0; i < rngSeeds.size(); i++)
 		{
 			CaseRun run = new CaseRun(expCase, caseSubDir, i, rngSeeds.get(i));
 			runCaseRun(run);
 		}
 	}
 	
 	/**
 	 * Submits a single run of an experiment case.
 	 * @param expCase The case to run.
 	 * @param caseDir The full path to where files are stored for this case.
 	 * @param caseSubDir The case directory relative to the experiment results directory.
 	 * @param i The run number for this run.
 	 * @param rngSeed The random seed for this run.
 	 * @throws DrmaaException If a DRMAA error occurs during job submission.
 	 * @throws IOException If the case XML cannot be written out.
 	 */
 	public void runCaseRun(CaseRun run) throws DrmaaException, IOException
 	{
 		ExperimentCase expCase = run.expCase;
 		String caseId = run.caseId;
 		int runNum = run.runNum;
 		int rngSeed = run.rngSeed;
 		
 		String caseDir;
 		if(caseId.equals(""))
 			caseDir = expDir;
 		else
 			caseDir = appendPathComponent(expDir, caseId);
 		
 		Settings settings = experiment.getSettings();
 		
 		String caseRunName;
 		if(caseId.equals(""))
 		{
 			caseRunName = experiment.getName() + " - run " + runNum
 			+ " (" + dateStr + ", " + timeStr + ")";
 		}
 		else
 		{
 			caseRunName = experiment.getName() + " - "
 			+ caseId + " - run " + runNum
 			+ " (" + dateStr + ", " + timeStr + ")";
 		}
 		
 		if(runType == RunType.RUN)
 		{
 			// Write setup file
 			String stdinPath = appendPathComponent(caseDir, ".gsweep_in." + runNum);
 			RunSetup setup = new RunSetup(settings,
 					experiment.getInputFiles(), caseId, expCase.getParameterMap(),
 					experiment.getNumRuns(), runNum, rngSeed, experiment.getOutputFiles());
 			ObjectOutputStream stdinStream = new ObjectOutputStream(new FileOutputStream(stdinPath));
 			stdinStream.writeObject(setup);
 			stdinStream.close();
 			
 			// Generate job template
 			JobTemplate jt = drmaaSession.createJobTemplate();
 			jt.setJobName(caseRunName);
 			jt.setRemoteCommand(appendPathComponent(root, "bin/gsrunner"));
 			if(!useFileTransfer) jt.setWorkingDirectory(caseDir);
 			jt.setInputPath(":" + stdinPath);
 			jt.setOutputPath(":" + appendPathComponent(caseDir, ".gsweep_out." + runNum));
 			jt.setErrorPath(":" + appendPathComponent(caseDir, ".gsweep_err." + runNum));
 			jt.setBlockEmail(true);
 			
 			try
 			{
 				jt.setTransferFiles(new FileTransferMode(true, true, true));
 			}
 			catch(DrmaaException e)
 			{
 				// If setTransferFiles isn't supported, we'll hope that the system defaults to
 				// transfering them. This works for SGE.
 			}
 			
 			Properties environment = new Properties();
 			environment.setProperty("GRIDSWEEPER_ROOT", root);
 			
 			String classpath = System.getenv("CLASSPATH");
 			if(classpath != null) environment.setProperty("CLASSPATH", classpath);
 			jt.setJobEnvironment(environment);
 			
 			String jobId = drmaaSession.runJob(jt);
 			
 			caseIdToJobIdMap.put(caseId + "." + runNum, jobId);
 			jobIdToRunMap.put(jobId, run);
 			fine("run in runmap: " + jobIdToRunMap.get(jobId));
 			
 			drmaaSession.deleteJobTemplate(jt);
 			
 			msgOut.println("  Submitted run " + runNum
 				+ " (DRMAA job ID " + jobId + ")");
 		}
 		else
 		{
 			msgOut.println("  Not submitting run " + runNum
 				+ " (dry run)");
 		}
 		fine("run: " + run);
 	}
 	
 	public void daemonize() throws GridSweeperException
 	{
 		if(runType != RunType.RUN) return;
 		
 		try
 		{
 			// Open PrintStream to status.log in experiment directory
 			String logPath = appendPathComponent(expDir, "status.log"); 
 			PrintStream logOut = new PrintStream(new FileOutputStream(logPath));
 			
 			msgOut.println("Detaching from console " +
 				"(monitoring process id: " + pid + ")...");
 			msgOut.println("Status output will be written to:");
 			msgOut.println("  " + logPath);
 			msgOut.println("and an email will be sent to " + email + 
 				" upon experiment completion.");
 			msgOut.println("You may now close this console or log out" +
 					" without disturbing the experiment.");
 			
 			msgOut = logOut;
 			System.out.close();
 			System.err.close();
 			
 			msgOut.println("Job monitoring process ID: " + pid);
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("An error occurred trying to "
 				+ "detach from the console.");
 		}
 	}
 	
 	/**
 	 * Cleans up: for now, just closes the DRMAA session.
 	 * @throws GridSweeperException If the DRMAA {@code exit()} call fails.
 	 */
 	public void finish() throws GridSweeperException
 	{
 		if(runType == RunType.NORUN) return;
 		
 		if(runType == RunType.RUN)
 		{
 			msgOut.println("Waiting for jobs to complete...");
 			
 			StringList drmaaErrorList = new StringList();
 			StringList gsErrorList = new StringList();
 			StringList execErrorList = new StringList();
 			
 			int runCount = jobIdToRunMap.size();
 			for(int i = 0; i < runCount; i++)
 			{
 				JobInfo info;
 				try
 				{
 					info = drmaaSession.wait(
 						Session.JOB_IDS_SESSION_ANY, Session.TIMEOUT_WAIT_FOREVER);
 				}
 				catch(DrmaaException e)
 				{
 					throw new GridSweeperException("Waiting for job completion failed.", e);
 				}
 				
 				String jobId = info.getJobId();
 				fine("got wait for job ID " + jobId);
 				fine("jobIdToRunMap: " + jobIdToRunMap.toString());
 				CaseRun run = jobIdToRunMap.get(jobId);
 				fine("run: " + run);
 				run.jobInfo = info;
 				
 				String caseId = run.caseId;
 				int runNum = run.runNum;
 				
 				String runStr = run.getRunString();
 				
 				msgOut.println("Completed run " + runStr
 					+ " (DRMAA job ID " + jobId + ")");
 				
 				// Check for DRMAA errors
 				if(info.hasCoreDump() || info.hasSignaled() || info.wasAborted()
 					|| info.getExitStatus() != 0)
 				{
 					drmaaErrorList.add(jobId);
 					msgOut.println("  (Warning: DRMAA reports that the run did not " +
 							"complete normally.)");
 				}
 				// Load RunResults from disk
 				else try
 				{
 					String caseDir = appendPathComponent(expDir, caseId);
 					String stdoutPath =
 						appendPathComponent(caseDir, ".gsweep_out." + runNum);
 					
 					fine("Loading RunResults from " + stdoutPath);
 					
 					FileInputStream fileStream = new FileInputStream(stdoutPath);
 					ObjectInputStream objStream = new ObjectInputStream(fileStream);
 					
 					RunResults runResults = (RunResults)objStream.readObject();
 					run.runResults = runResults;
 					
 					if(runResults == null || runResults.getException() != null)
 					{
 						gsErrorList.add(jobId);
 						msgOut.println("  (Warning: a GridSweeper exception occurred" +
 								" while performing this run.)"); 
 					}
 					else if(runResults.getStatus() != 0)
 					{
 						execErrorList.add(jobId);
 						msgOut.println("  (Warning: this run exited with an" +
 								"error code.)");
 					}
 				}
 				catch(Exception e)
 				{
 					msgOut.print("  (Warning: an exception occurred loading the" +
 						" run results for this run: ");
 					e.printStackTrace(msgOut);
 					msgOut.println("  .)");
 					gsErrorList.add(jobId);
 				}
 				
 				msgOut.format("%d of %d complete (%.1f%%).\n",
 						i + 1, runCount, (double)(i + 1)/runCount * 100);
 			}
 			
 			msgOut.println("All jobs completed.");
 			
 			sendEmail(drmaaErrorList, gsErrorList, execErrorList);
 			
 			try
 			{
 				// Finish it up
 				drmaaSession.exit();
 			}
 			catch(DrmaaException e)
 			{
 				throw new GridSweeperException("Received exception ending DRMAA session", e);
 			}
 		}
 		else
 		{
 			sendEmail(null, null, null);
 		}
 	}
 	
 	private void sendEmail(StringList drmaaErrorList, 
 		StringList gsErrorList, StringList execErrorList) 
 		throws GridSweeperException
 	{
 		String expName = experiment.getName();
 		
 		String subject = expName + " complete";
 		
 		// Construct and write out message
 		String messagePath = appendPathComponent(expDir, ".gsweep_email");
 		StringBuffer message = new StringBuffer();
 		
 		if(runType == RunType.RUN)
 			message.append("GridSweeper experiment run complete.\n\n");
 		else
 			message.append("GridSweeper experiment dry run complete.\n\n");
 		
 		message.append("   Experiment name: " + expName + "\n");
 		message.append(" Results directory: " + expDir + "\n");
 		
 		message.append("      Submitted at: ");
 		DateFormat format = DateFormat.getDateTimeInstance();
 		message.append(format.format(new Date(cal.getTimeInMillis())));
 		message.append("\n");
 		
 		message.append("      Elapsed time: ");
 		long elapsedMilli = (new Date()).getTime() - cal.getTimeInMillis();
 		elapsedMilli /= 1000;
 		long seconds = elapsedMilli % 60;
 		elapsedMilli /= 60;
 		long minutes = elapsedMilli % 60;
 		elapsedMilli /= 60;
 		long hours = elapsedMilli;
 		message.append("" + hours + "h" + minutes + "m" + seconds + "s");
 		message.append("\n\n");
 		
 		if(runType == RunType.RUN)
 		{
 			// Print error messages, if present
 			int errorCount = drmaaErrorList.size() + gsErrorList.size() + execErrorList.size();
 			if(errorCount == 0)
 			{
 				message.append("No errors occurred.\n");
 			}
 			else
 			{
 				int runCount = jobIdToRunMap.size();
				message.append(String.format("%d of %d runs had errors (%.1f%%)...\n\n",
 					errorCount, runCount, (double)errorCount/runCount * 100)); 
 				
 				// Start with DRMAA-detected errors
 				for(String jobId : drmaaErrorList)
 				{
 					CaseRun run = jobIdToRunMap.get(jobId);
 					JobInfo info = run.jobInfo;
 					
 					String runStr = run.getRunString();
 					
 					message.append("DRMAA returned an error for " + runStr + ":\n");
 					if(info.hasCoreDump())
 					{
 						message.append("  A core dump occurred.\n");
 					}
 					if(info.hasSignaled())
 					{
 						message.append("  The job ended with signal "
 							+ info.getTerminatingSignal() + ".\n");
 					}
 					if(info.wasAborted())
 					{
 						message.append("  The job was aborted.\n");
 					}
 					if(info.hasExited() && info.getExitStatus() != 0)
 					{
 						message.append("  The job exited with status " 
 							+ info.getExitStatus() + ".\n");
 					}
 					message.append("\n");
 				}
 				
 				// And then GridSweeper errors...
 				for(String jobId : gsErrorList)
 				{
 					CaseRun run = jobIdToRunMap.get(jobId);
 					RunResults results = run.runResults;
 					
 					String runStr = run.getRunString();
 	
 					message.append("An internal error occurred in GridSweeper for "
 						+ runStr + ": \n");
 					if(results == null)
 					{
 						message.append("  The run results object could not be loaded.\n");
 					}
 					else
 					{
 						Exception exception = results.getException();
 						if(exception != null)
 						{
 							message.append("  A Java exception occurred:\n  ");
 							
 							StringWriter sw = new StringWriter();
 							PrintWriter pw = new PrintWriter(sw);
 							exception.printStackTrace(pw);
 							
 							String stackStr = sw.getBuffer().toString();
 							message.append(stackStr);
 						}
 						else
 						{
 							message.append("  An unknown error occurred.\n");
 						}
 					}
 					message.append("\n");
 				}
 				
 				// And finally nonzero status from the executable itself...
 				for(String jobId : execErrorList)
 				{
 					CaseRun run = jobIdToRunMap.get(jobId);
 					RunResults results = run.runResults;
 					
 					String runStr = run.getRunString();
 					
 					message.append("The " + runStr + " exited with status " +
 						results.getStatus() + ".\n\n");
 				}
 			}
 		}
 		
 		try
 		{
 			FileWriter fw = new FileWriter(messagePath);
 			fw.write(message.toString());
 			fw.close();
 		}
 		catch(IOException e)
 		{
 			throw new GridSweeperException("Could not write email file.");
 		}
 		
 		String command = appendPathComponent(root, "bin/gsmail");
 		
 		String[] commandAndArgs = {command, subject, email, messagePath};
 		
 		try
 		{
 			Runtime.getRuntime().exec(commandAndArgs);
 		}
 		catch (IOException e)
 		{
 			throw new GridSweeperException("Could not send email.", e);
 		}
 		
 		msgOut.println("Sent notification email to " + email + ".");
 	}
 
 	public void setRunType(RunType runType)
 	{
 		this.runType = runType;
 	}
 	
 	public void setExperiment(Experiment experiment)
 	{
 		this.experiment = experiment;
 	}
 }
