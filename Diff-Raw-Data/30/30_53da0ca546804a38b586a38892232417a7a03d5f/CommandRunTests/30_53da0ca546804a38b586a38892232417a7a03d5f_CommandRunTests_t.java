 /*******************************************************************************
  * Copyright (c) 2009 Intuit, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.opensource.org/licenses/eclipse-1.0.php
  * 
  * Contributors:
  *     Intuit, Inc - initial API and implementation
  *******************************************************************************/
 package com.intuit.tools.imat.commands;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedHashMap;
 
 import org.apache.log4j.Logger;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.intuit.tools.imat.IApplicationResourceService;
 import com.intuit.tools.imat.ICommand;
 import com.intuit.tools.imat.IScriptLauncher;
 import com.intuit.tools.imat.MisconfigurationException;
 import com.intuit.tools.imat.cli.ExitStatus;
 import com.intuit.tools.imat.cli.converters.FileConverter;
 import com.intuit.tools.imat.monitor.iOSEndOfLogMsg;
 import com.intuit.tools.imat.reporting.IReportingService;
 import com.intuit.tools.imat.scripts.InstrumentsLauncher;
 import com.intuit.tools.imat.system.SystemReflectionService;
 import com.intuit.tools.imat.validators.CanonicalFileValidator;
 import com.intuit.tools.imat.validators.InstrumentsVersionValidator;
 import com.intuit.tools.imat.validators.JavaScriptFileValidator;
 import com.intuit.tools.imat.validators.TemplateFileValidator;
 
 /**
  * @author rpfeffer
  * @dateCreated Mar 25, 2011
  * 
  *              //TODO RP: We should explain the use of this class once it has
  *              been implemented.
  * 
  */
 @Parameters(commandDescription = "Use this command to run your tests.")
 public class CommandRunTests extends ScriptedCommand implements ICommand {
 
 	private static final String AUTOMATION_RESULTS_PATH = "logs" + File.separator + "runs";
 	private static final String AUTOMATION_RESULTS_FILE = AUTOMATION_RESULTS_PATH + File.separator + "Run 1" + File.separator + "Automation Results.plist";
 	private static final String HORIZONTAL_RULE = "-------------------------------------------------------";
 
 	/**
 	 * The name of the command
 	 */
 	public static final String NAME = "run-tests";
 
 	/**
 	 * The suite we will run in our tests
 	 */
 	@Parameter(names = { InstrumentsLauncher.SUITE_FILE, "-s" }, description = "The filename of the suite file to run. Note, this path should be relative to the current directory. This option is required.", validateWith = JavaScriptFileValidator.class)
 	public String suite = null;
 
 	/**
 	 * The template file we will use in our tests.
 	 */
 	@Parameter(names = { InstrumentsLauncher.TEMPLATE_FILE, "-t" }, description = "The file path of the template to run. The file path must end in .tracetemplate. Overriding the default value of this option is required when running tests on the simulator.", converter = FileConverter.class, validateWith = TemplateFileValidator.class)
 	public File template = null;
 	
 	/**
 	 * The device ID depicting the device to run tests on.
 	 */
 	@Parameter(names = { InstrumentsLauncher.DEVICE_ID, "-d"}, description = "The Device ID (UDID) of the device where the tests will run. Use of this option will cause your tests to run against a device.")
 	public String deviceID = null;
 	
 	/**
 	 * The location where the XML reports will be placed.
 	 */
 	public static final String DEFAULT_REPORT_PATH_SUFFIX = "reports";
 	public static final String REPORT_PATH = "-reportPath";
 	@Parameter(names = { REPORT_PATH, "-r" }, description = "The path to the directory where the xml reports will be written to should the tests run to completion.", validateWith = CanonicalFileValidator.class, converter = FileConverter.class)
 	public File reportsDir = new File(DEFAULT_REPORT_PATH_SUFFIX);
 
 	/**
 	 * The application we are testing
 	 */
 	@Parameter(names = { InstrumentsLauncher.APPLICATION_UNDER_TEST, "-a" }, description = "The the application under test. When running against the simulator this should be a path to the App. However, when running against a device supplying the App name like \"sampleApp.app\" should suffice.")
 	public String app = null;
 
 	private final IScriptLauncher instrumentsLauncher;
 	private final IApplicationResourceService applicationResourceService;
 	private final IScriptLauncher antScriptLauncher;
 	private final IReportingService reportingService;
	private final SystemReflectionService reflectionService;
 	private boolean isDevice = false;
 
 	/**
 	 * @param printwriter
 	 * @param logger
 	 * @param scriptLauncher
 	 */
 	CommandRunTests(PrintWriter printwriter, Logger logger,
 			IScriptLauncher scriptLauncher,
 			IScriptLauncher instrumentsLauncher,
 			IScriptLauncher antScriptLauncher,
 			IApplicationResourceService applicationResourceService,
 			IReportingService reportingService,
 			SystemReflectionService reflectionService) {
 		super(printwriter, logger, scriptLauncher);
 		this.applicationResourceService = applicationResourceService;
 		this.reportingService = reportingService;
 		this.instrumentsLauncher = instrumentsLauncher;
		this.reflectionService = reflectionService;
 		this.antScriptLauncher = antScriptLauncher;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.intuit.tools.imat.ICommand#run()
 	 */
 	public ExitStatus run() throws MisconfigurationException {
 		//TODO: Add a parameter validation mechanism.
		if (validate()) {
 			exitStatus = archiveTestResults();
 			if (exitStatus == ExitStatus.SUCCESS) {
 				exitStatus = startTests();
 				captureTrace();
 			}
 			if (exitStatus == ExitStatus.SUCCESS) {
 				exitStatus = checkForTestExecution();
 			}
 			if (exitStatus == ExitStatus.SUCCESS) {
 				generateReport();
 			}
 		} else {
 			logger.error("Invalid Parameters: Run \"imat run-tests -help\" for more information.");
 			exitStatus = ExitStatus.VALIDATION_ERROR;
 		}
 		return exitStatus;
 	}
 
 	/**
 	 * @throws MisconfigurationException
 	 */
 	private ExitStatus startTests() throws MisconfigurationException {
 		logger.info(HORIZONTAL_RULE + "\n" + 
 					"Starting Tests...\n" +
 					HORIZONTAL_RULE);
 		String suiteFilePath = "";
 		ExitStatus exitCode = ExitStatus.SUCCESS;
 		try {
 			LinkedHashMap<String, String> arguments = new LinkedHashMap<String, String>();
 			suiteFilePath = (new File(suite)).getCanonicalPath();
 			String runLogDirPath = applicationResourceService.getAppResourceFile(
 					AUTOMATION_RESULTS_PATH, true).getCanonicalPath();
 			if (isDevice)
 			{
 				arguments.put(InstrumentsLauncher.DEVICE_ID, deviceID);
 			} 
 			if (template != null) {
 				arguments.put(InstrumentsLauncher.TEMPLATE_FILE, template.getCanonicalPath());
 			}
 			arguments.put(InstrumentsLauncher.APPLICATION_UNDER_TEST, app);
 			arguments.put(InstrumentsLauncher.SUITE_FILE, suiteFilePath);
 			arguments.put(InstrumentsLauncher.RESULTS_PATH, runLogDirPath);
 			instrumentsLauncher.setProperties(arguments);
 			logger.info("Waiting for tests to complete...");
 			exitCode = instrumentsLauncher.runScript();
 		} catch (FileNotFoundException e) {
 			MisconfigurationException ex = new MisconfigurationException(
 					"Could not find suite file: " + suiteFilePath);
 			ex.initCause(e);
 			throw ex;
 		} catch (IOException e) {
 			MisconfigurationException ex = new MisconfigurationException(
 					"Error while resolving path.");
 			ex.initCause(e);
 			throw ex;
 		}
 		return exitCode;
 	}
 
 	private void captureTrace() {
 		LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
 		String suffix = File.separator + "instrumentscli0.trace";
 		File fromPath = new File("." + suffix);
 		properties.put("src", fromPath.getAbsolutePath());
 		File toPath = new File(this.reportsDir + suffix);
 		properties.put("target", toPath.getAbsolutePath());
 		antScriptLauncher.setScript("moveResource.xml");
 		antScriptLauncher.setProperties(properties);
 		antScriptLauncher.runScript();
 	}
 
 	/**
 	 * @throws MisconfigurationException
 	 * 
 	 */
 	private ExitStatus checkForTestExecution() {
 		ExitStatus status = ExitStatus.SUCCESS;
 		try {
 			File testResultLog = applicationResourceService.getAppResourceFile(
 					AUTOMATION_RESULTS_FILE, true);
 			BufferedReader bfr = new BufferedReader(new FileReader(testResultLog));
 			String line;
 			while ((line = bfr.readLine()) != null) {
 				if (line.contains(iOSEndOfLogMsg.PARSE_ERROR.getMessage())) {
 					status = ExitStatus.TESTING_ERROR;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			MisconfigurationException ex = new MisconfigurationException(
 					"Could not find Automation Results file: "
 							+ AUTOMATION_RESULTS_FILE + " Stopping "
 							+ "test suite.");
 			ex.initCause(e);
 			logger.error(ex);
 			status = ExitStatus.SETUP_ERROR;
 		} catch (IOException e) {
 			logger.error(e);
 			status = ExitStatus.INTERNAL_ERROR;
 		}
 		return status;
 	}
 
 	private void generateReport() {
 		try {
 			logger.info(HORIZONTAL_RULE + "\n" + 
 						"Generating reports...\n" +
 						HORIZONTAL_RULE);
 			File testResultLog = applicationResourceService.getAppResourceFile(
 					AUTOMATION_RESULTS_FILE, true);
 			reportingService.setJunitXMLResultPath(this.reportsDir);
 			logger.debug("converting test result log: " + testResultLog
 					+ " to jUnit report for path: " + this.reportsDir);
 			reportingService
 					.convertTestOutputFileToJunitXMLResultFormat(testResultLog);
 		} catch (FileNotFoundException e) {
 			logger.error(e);
 		}
 	}
 
 	private ExitStatus archiveTestResults() {
 		logger.info(HORIZONTAL_RULE + "\n" + 
 					"Cleaning up...\n" +
 					HORIZONTAL_RULE);
 		String runLogDirPath = "";
 		ExitStatus exitStatus = ExitStatus.SUCCESS;
 		try {
 			LinkedHashMap<String, String> arguments = new LinkedHashMap<String, String>();
 			runLogDirPath = applicationResourceService.getAppResourceFile(
 					AUTOMATION_RESULTS_PATH, true).getCanonicalPath();
 			arguments.put("RUN_LOG_DIR", runLogDirPath);
 			IScriptLauncher scriptLauncher = this.getScriptLauncher();
 			scriptLauncher.setProperties(arguments);
 			scriptLauncher.setScript("CleanUpXCodeInstrument.scpt");
 			exitStatus = scriptLauncher.runScript();
 		} catch (FileNotFoundException e) {
 			FileNotFoundException ex = new FileNotFoundException(
 					"Could not find run log directory: " + runLogDirPath);
 			ex.initCause(e);
 			logger.error(ex);
 			exitStatus = ExitStatus.INTERNAL_ERROR;
 		} catch (IOException e) {
 			IOException ex = new IOException(
 					"Error while resolving path to run log directory.");
 			ex.initCause(e);
 			logger.error(ex);
 			exitStatus = ExitStatus.INTERNAL_ERROR;
 		}
 		return exitStatus;
 	}
 	
 	private boolean validateSimulatorArgs()
 	{
 		boolean isValid = false;
 		if (suite != null && app != null && template != null && deviceID == null) {
 			isValid = true;
 		}
 		return isValid;
 	}
 	
 	private boolean validateDeviceArgs() {
 		boolean isValid = false;
 		if (suite != null && app != null && deviceID != null) {
 			isValid = true;
 			isDevice = true;
 		}
 		return isValid;
 	}
	
	private boolean validate() {
		InstrumentsVersionValidator versionValidator = new InstrumentsVersionValidator();
		versionValidator.validate("", reflectionService.getSystemInstrumentsVersion());
		return (validateSimulatorArgs() || validateDeviceArgs());
	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.intuit.tools.imat.ICommand#getName()
 	 */
 	public String getName() {
 		return CommandRunTests.NAME;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.intuit.tools.imat.ICommand#isRunnable()
 	 */
 	public boolean isRunnable() {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.intuit.tools.imat.commands.Command#expectsProject()
 	 */
 	@Override
 	public boolean expectsProject() {
 		return true;
 	}
 }
