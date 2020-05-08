 package edu.umich.lsa.cscs.gridsweeper;
 
 import org.ggf.drmaa.*;
 
 import edu.umich.lsa.cscs.gridsweeper.parameters.*;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.*;
 import java.util.logging.*;
 import java.util.regex.*;
 
 import static edu.umich.lsa.cscs.gridsweeper.StringUtils.*;
 import static edu.umich.lsa.cscs.gridsweeper.DateUtils.*;
 import static edu.umich.lsa.cscs.gridsweeper.DLogger.*;
 
 /**
  * The GridSweeper command-line tool for job submission. Takes a .gsweep
  * XML experiment file and submits it to the grid for execution via DRMAA.
  * Warning: begun on a houseboat in Paris. May still contain strange French bugs.
  * 
  * TODO: Refactor all functionality into a class that can be reused for
  * both the command-line tool and graphical application.
  * 
  * @author Ed Baskerville
  *
  */
 public class GridSweeper
 {
 	/**
 	 * A state enum for the argument-parsing state machine.
 	 * @author Ed Baskerville
 	 *
 	 */
 	enum ArgState
 	{
 		START,
 		ADAPTER,
 		COMMAND,
 		INPUT,
 		OUTPUT,
 		RUNTYPE
 	}
 	
 	enum RunType
 	{
 		RUN,
 		DRY,
 		NORUN
 	}
 	
 	static String experimentPath = null;
 	static String outputPath = null;
 	
 	static String root = null;
 	
 	static Experiment experiment = null;
 	static RunType runType = RunType.RUN;
 	static List<ExperimentCase> cases = null;
 	
 	static Settings settings;
 	static Settings commandLineSettings;
 	static Settings commandLineAdapterSettings;
 	static Settings commandLineFileTransferSettings;
 	static List<Sweep> commandLineSweeps;
 	
 	static boolean useFileTransfer = false;
 	
 	static String className;
 	static Calendar cal;
 	
 	static String dateStr;
 	static String timeStr;
 	static String expDir;
 	
 	static String fileTransferSubpath = null;
 	
 	static Session drmaaSession;
 	
 	static Matcher singleValueSweepMatcher;
 	
 	static
 	{
 		settings = Settings.sharedSettings();
 		commandLineSettings = new Settings();
 		commandLineAdapterSettings = new Settings();
 		commandLineFileTransferSettings = new Settings();
 		className = GridSweeper.class.toString();
 		cal = new GregorianCalendar();
 		
		commandLineSweeps = new ArrayList<Sweep>();
		
 		singleValueSweepMatcher = Pattern.compile("(\\S+)\\s*=\\s*(\\S+)").matcher("");
 	}
 	
 	/**
 	 * Does everything: loads the experiment, runs the experiment, and (soon)
 	 * monitors the experiment.
 	 * @param args Command-line arguments.
 	 * @throws GridSweeperException If the GRIDSWEEPER_ROOT environment variable
 	 * is not set, or if parsing, loading, setup, running, or monitoring jobs
 	 * generate any other uncaught exceptions.
 	 */
 	public static void main(String[] args) throws GridSweeperException
 	{
 		// Set up logging to /tmp/gridsweeper.log
 		DLogger.addFileHandler(Level.ALL, "%t/gridsweeper.log");
 		
 		entering(className, "main");
 		
 		root = System.getenv("GRIDSWEEPER_ROOT");
 		if(root == null)
 		{
 			throw new GridSweeperException("GRIDSWEEPER_ROOT environment variable not set.");
 		}
 		
 		// Parse args
 		parseArgs(args);
 		
 		// Load experiment file
 		loadExperiment();
 		
 		// Combine settings from command-line arguments and experiment
 		settings.putAll(experiment.getSettings());
 		settings.putAll(commandLineSettings);
 		for(Sweep sweep : commandLineSweeps)
 		{
 			experiment.getRootSweep().add(sweep);
 		}
 		
 		// Generate experiment cases, etc.
 		setUpExperiment();
 		
 		// Run jobs
 		if(runType != RunType.NORUN)
 		{
 			run();
 	
 			// Wait for job completion
 			finish();
 		}
 		
 		exiting(className, "main");
 	}
 	
 	/**
 	 * <p>Parses command-line arguments. Currently only handles -a (adapter class),
 	 * -e (experiment file path), and -d (whether to perform a dry run).</p>
 	 * 
 	 * <table valign="top">
 	 * 
 	 * <tr>
 	 * <td><b>Switch</b></td>
 	 * <td><b>Description</b></td>
 	 * </tr>
 	 * 
 	 * <tr>
 	 * <td>-a, --adapter</td>
 	 * <td>
 	 * Adapter class name, e.g., {@code edu.umich.lsa.cscs.gridsweeper.DroneAdapter}.
 	 * Only the first {@code -a} argument will be used. When GridSweeper is run
 	 * using the {@code gdrone} tool, {@code -a} is already provided, and any
 	 * provided by the user will be ignored.
 	 * </td>
 	 * </tr>
 	 *
 	 * <tr>
 	 * <td>-c, --command</td>
 	 * <td>
 	 * Command (model executable path) to run. In effect, this sets the "command"
 	 * setting in the adapter domain, e.g.,
 	 * {@code edu.umich.lsa.cscs.gridsweeper.Drone.command}, so if the adapter class
 	 * does not support the "command" setting, this argument has no effect.
 	 * </td>
 	 * </tr>
 	 * 
 	 * <tr>
 	 * <td>-i, --input</td>
 	 * <td>
 	 * Path to experiment XML input file. This is usually required,
 	 * but can theoretically be left out if all the necessary experiment
 	 * information is provided with other command-line switches—for the
 	 * Drone adapter, this would mean that {@code -c} and {@code -n}
 	 * would be required, among others. Note that command-line settings
 	 * override any settings made in the experiment XML file, which
 	 * in turn override any settings in the user’s {@code ~/.gridsweeper}
 	 * configuration file.
 	 * </td>
 	 * </tr>
 	 * 
 	 * <tr>
 	 * <td>-o, --output</td>
 	 * <td>
 	 * <p>Path at which experiment XML should be written. The outputted file
 	 * will contain all the settings needed to re-run the experiment using
 	 * <br/><br/>
 	 * {@code gsweep -e <experimentXMLPath>}
 	 * <br/><br/>
 	 * and nothing else. This will <em>not</em> be an exact replica of this
 	 * run, because the seed for GridSweeper’s random number generator will
 	 * be generated at runtime. A file that will be able to produce an
 	 * <em>exact</em> reproduction of this experiment will be generated
 	 * in the experiment results directory.</p>
 	 * <p>
 	 * All settings will be written out to this file, including those
 	 * set in the user’s {@code ~/.gridsweeper} configuration file,
 	 * in the input experiment file, and those set at the command line,
 	 * so that the above command should re-run the experiment as before
 	 * unless some setting in the {@code ~/.gridsweeper} changes the behavior. 
 	 * </p>
 	 * </td>
 	 * </tr>
 	 * 
 	 * <tr>
 	 * <td>-r, --runtype</td>
 	 * <td>
 	 * <p>Run style, either {@code run}, {@code dry}, or {@code norun}.
 	 * Defaults to {@code run}. If {@code dry} is specified, a “dry run”
 	 * is performed, simulating the parameter sweep without actually 
 	 * submitting jobs to the grid to be run. Output directories are created
 	 * and are populated with case files needed to reproduce each case,
 	 * so you can test that all the parameter settings are correct before
 	 * running the experiment for real. If {@code norun} is specified,
 	 * the only effect of this command will be to generate a new experiment
 	 * XML file as provided with the {@code -o} option.
 	 * </td>
 	 * </tr>
 	 * 
 	 * <tr>
 	 * <td><em>param</em>=<em>value</em></td>
 	 * <td>
 	 * Sets a fixed parameter <em>param</em> to value <em>value</em>,
 	 * valid for all runs of the model.
 	 * </td>
 	 * </tr>
 	 * 
 	 * <tr>
 	 * <td><em>param</em>=<em>start</em>:<em>incr</em>:<em>end</em></td>
 	 * <td>
 	 * Sweeps parameter <em>param</em>, starting at value <em>start</em>
 	 * and incrementing by <em>incr</em> until the value is greater than
 	 * <em>end</em>. The value <em>end</em> is only used, then, if it is
 	 * exactly a multiple of <em>incr</em> greater than <em>start</em>.
 	 * (Rounding error is not a problem, because an infinite-precision
 	 * decimal number representation is used.)   
 	 * </td>
 	 * </tr>
 	 * 
 	 * </table>
 	 * @param args Command-line arguments.
 	 */
 	private static void parseArgs(String[] args) throws GridSweeperException
 	{
 		entering(className, "parseArgs");
 		
 		ArgState state = ArgState.START;
 		
 		for(String arg : args)
 		{
 			switch(state)
 			{
 				case START:
 					if(arg.equals("-a") || arg.equals("--adapter"))
 						state = ArgState.ADAPTER;
 					else if(arg.equals("-c") || arg.equals("--command"))
 						state = ArgState.COMMAND;
 					else if(arg.equals("-i") || arg.equals("--input"))
 						state = ArgState.INPUT;
 					else if(arg.equals("-o") || arg.equals("--output"))
 						state = ArgState.OUTPUT;
 					else if(arg.equals("-r") || arg.equals("--runtype"))
 						state = ArgState.RUNTYPE;
 					else if(arg.equals("--debug"))
 					{
 						DLogger.addConsoleHandler(Level.ALL);
 					}
 					else
 					{
 						singleValueSweepMatcher.reset(arg);
 						
 						if(singleValueSweepMatcher.matches())
 						{
 							String name = singleValueSweepMatcher.group(1);
 							String value = singleValueSweepMatcher.group(2);
 							fine("Matched parameter " + name + "=" + value);
 							
 							commandLineSweeps.add(new SingleValueSweep(name, value));
 						}
 					}
 					break;
 				case ADAPTER:
 					if(!commandLineSettings.contains("AdapterClass"))
 						commandLineSettings.put("AdapterClass", arg);
 					state = ArgState.START;
 					break;
 				case COMMAND:
 					if(!commandLineSettings.contains("command"))
 						commandLineSettings.put("command", arg);
 					state = ArgState.START;
 					break;
 				case INPUT:
 					experimentPath = arg;
 					state = ArgState.START;
 					break;
 				case OUTPUT:
 					outputPath = arg;
 					state = ArgState.START;
 					break;
 				case RUNTYPE:
 					if(arg.equalsIgnoreCase("run"))
 					{
 						runType = RunType.RUN;
 					}
 					else if(arg.equals("dry"))
 					{
 						runType = RunType.DRY;
 					}
 					else if(arg.equals("norun"))
 					{
 						runType = RunType.NORUN;
 					}
 					else
 					{
 						throw new GridSweeperException("Invalid run type " + arg + "specified.");
 					}
 					break;
 			}
 		}
 		
 		exiting(className, "parseArgs");
 	}
 	
 	/**
 	 * Loads the experiment from the provided XML file.
 	 * @throws GridSweeperException If the experiment path is not provided,
 	 * or if the file cannot be loaded or parsed.
 	 */
 	private static void loadExperiment() throws GridSweeperException
 	{
 		entering(className, "loadExperiment");
 		
 		if(experimentPath == null)
 		{
 			throw new GridSweeperException("No experiment file provided.");
 		}
 		
 		try
 		{
 			experiment = new Experiment(new java.net.URL("file", "", experimentPath));
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not load experiment file.", e);
 		}
 		
 		exiting(className, "loadExperiment");
 	}
 	
 	/**
 	 * Generates experiment cases in preparation for running the experiment.
 	 * @throws GridSweeperException If case generation fails.
 	 */
 	private static void setUpExperiment() throws GridSweeperException
 	{
 		// TODO: add parameter settings loaded from command line to experiment.
 
 		// TODO: output XML experiment file, if provided by -o
 		
 		if(runType != RunType.NORUN) try
 		{
 			// Assemble cases
 			cases = experiment.generateCases(new Random());
 		}
 		catch (ExperimentException e)
 		{
 			throw new GridSweeperException("Could not generate experiment cases", e);
 		}
 	}
 	
 	/**
 	 * Runs the experiment. Experiment results are collated in a master experiment directory,
 	 * specified in the user settings, in a subdirectory tagged with the experiment name
 	 * and date/time ({@code <name>/YYYY-MM-DD/hh-mm-ss}). If a shared filesystem is not
 	 * available, files are first staged to the experiment results directory on the
 	 * file transfer system. Then a DRMAA session is established, and each case is submitted.
 	 * 
 	 * @throws GridSweeperException
 	 */
 	private static void run() throws GridSweeperException
 	{
 		entering(className, "prepare");
 		
 		useFileTransfer = settings.getBooleanProperty("UseFileTransfer");
 
 		FileTransferSystem fts = null;
 		try
 		{
 			finer("settings: " + settings);
 			
 			// Set up file transfer system if asked for
 			if(runType==RunType.RUN && useFileTransfer)
 			{
 				String className = settings.getSetting("FileTransferSystemClassName");
 				Settings ftsSettings = settings.getSettingsForClass(className);
 				fts = FileTransferSystemFactory.getFactory().getFileTransferSystem(className, ftsSettings);
 				fts.connect();
 				
 				boolean alreadyExists;
 				do
 				{
 					fileTransferSubpath = UUID.randomUUID().toString();
 					alreadyExists = fts.fileExists(fileTransferSubpath);
 				}
 				while(alreadyExists);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not set up file trasfer system", e);
 		}
 			
 		try
 		{
 			String expsDir = expandTildeInPath(settings.getProperty("ExperimentsDirectory"));
 			
 			// First set up big directory for the whole experiment
 			// Located in <experimentDir>/<experimentName>/<experimentDate>/<experimentTime>
 			String expName = experiment.getName();
 			dateStr = getDateString(cal);
 			timeStr = getTimeString(cal);
 			String expSubDir = String.format("%s%s%s%s%s", expName, getFileSeparator(), dateStr, getFileSeparator(), timeStr);
 			
 			expDir = appendPathComponent(expsDir, expSubDir);
 			finer("Experiment subdirectory: " + expDir);
 			
 			File expDirFile = new File(expDir);
 			expDirFile.mkdirs();
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not set up local dirs", e);
 		}
 		
 		try
 		{
 			// If file transfer is on, make the directory
 			// and upload input files
 			if(runType==RunType.RUN && useFileTransfer)
 			{
 				String inputDir = appendPathComponent(fileTransferSubpath, "input");
 				fts.makeDirectory(inputDir);
 				
 				StringMap inputFiles = experiment.getInputFiles();
 				for(String localPath : inputFiles.keySet())
 				{
 					String remotePath = appendPathComponent(inputDir, inputFiles.get(localPath));
 					
 					fts.uploadFile(localPath, remotePath);
 				}
 				
 				fts.disconnect();
 			}
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not create remote dirs", e);
 		}
 			
 		try
 		{
 			if(runType==RunType.RUN)
 			{
 				// Establish DRMAA session
 				drmaaSession = SessionFactory.getFactory().getSession();
 				drmaaSession.init(null);
 			}
 			
 			// Set up and run each case
 			for(ExperimentCase expCase : cases)
 			{
 				runCase(expCase);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not run experiments", e);
 		}
 		
 		exiting(className, "prepare");
 	}
 	
 	/**
 	 * Submits a single experiment case. This means running one job for each
 	 * run of the case (one for each random seed).
 	 * @param expCase The experiment case to run.
 	 * @throws FileNotFoundException If the case directory cannot be found/created.
 	 * @throws DrmaaException If a DRMAA error occurs (in {@link #runCaseRun}).
 	 * @throws IOException If the case XML cannot be written out (in {@link #runCaseRun}).
 	 */
 	private static void runCase(ExperimentCase expCase) throws FileNotFoundException, DrmaaException, IOException
 	{		
 		String caseSubDir = experiment.getDirectoryNameForCase(expCase);
 		String caseDir = appendPathComponent(expDir, caseSubDir);
 		finer("Case subdirectory: " + caseDir);
 		
 		File caseDirFile = new File(caseDir);
 		caseDirFile.mkdirs();
 		
 		// For each run, output XML and run the damn thing
 		List<Long> rngSeeds = expCase.getRngSeeds();
 		for(int i = 0; i < rngSeeds.size(); i++)
 		{
 			runCaseRun(expCase, caseDir, caseSubDir, i, rngSeeds.get(i));
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
 	private static void runCaseRun(ExperimentCase expCase, String caseDir, String caseSubDir, int i, Long rngSeed) throws DrmaaException, IOException
 	{
 		String caseRunName = experiment.getName() + " - "
 			+ caseSubDir + " - run " + i
 			+ " (" + dateStr + ", " + timeStr + ")";
 
 		// Write XML
 		String xmlPath = appendPathComponent(caseDir, "case." + i + ".gsweep");
 		ExperimentCaseXMLWriter xmlWriter = new ExperimentCaseXMLWriter(
 				xmlPath, experiment, expCase, caseRunName, rngSeed);
 		xmlWriter.writeXML();
 		
 		// Write setup file
 		String stdinPath = appendPathComponent(caseDir, ".gsweep_in." + i);
 		RunSetup setup = new RunSetup(settings,
 				experiment.getInputFiles(), caseSubDir, expCase.getParameterMap(),
 				i, rngSeed, experiment.getOutputFiles());
 		ObjectOutputStream stdinStream = new ObjectOutputStream(new FileOutputStream(stdinPath));
 		stdinStream.writeObject(setup);
 		
 		// Generate job template
 		JobTemplate jt = drmaaSession.createJobTemplate();
 		jt.setJobName(caseRunName);
 		jt.setRemoteCommand(appendPathComponent(root, "bin/grunner"));
 		if(!useFileTransfer) jt.setWorkingDirectory(caseDir);
 		jt.setInputPath(":" + stdinPath);
 		jt.setOutputPath(":" + appendPathComponent(caseDir, ".gsweep_out." + i));
 		jt.setErrorPath(":" + appendPathComponent(caseDir, ".gsweep_err." + i));
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
 		drmaaSession.deleteJobTemplate(jt);
 		
 		// TODO: Record job id so it can be reported back as tied to this case run.
 	}
 	
 	/**
 	 * Cleans up: for now, just closes the DRMAA session.
 	 * @throws GridSweeperException If the DRMAA {@code exit()} call fails.
 	 */
 	private static void finish() throws GridSweeperException
 	{
 		// TODO: wait for all jobs to complete, giving notification
 		// as each one arrives
 		// TODO: provide mechanism to detach this session to the background,
 		// in some way that works even if the user logs out
 		// This is a bit like a daemon, so cf:
 		// http://pezra.barelyenough.org/blog/2005/03/java-daemon/
 		// http://wrapper.tanukisoftware.org/doc/english/prop-daemonize.html
 		// TODO: upon full completion, send an email to the user
 		
 		try
 		{
 			drmaaSession.exit();
 		}
 		catch(DrmaaException e)
 		{
 			throw new GridSweeperException("Received exception ending DRMAA session", e);
 		}
 	}
 }
