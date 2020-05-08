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
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedHashMap;
 
 import org.apache.log4j.Logger;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.intuit.tools.imat.IApplicationResourceService;
 import com.intuit.tools.imat.ICommand;
 import com.intuit.tools.imat.IFileListener;
 import com.intuit.tools.imat.IFileMonitoringService;
 import com.intuit.tools.imat.IProjectResourceService;
 import com.intuit.tools.imat.IScriptLauncher;
 import com.intuit.tools.imat.ITestMonitor;
 import com.intuit.tools.imat.MisconfigurationException;
 import com.intuit.tools.imat.cli.converters.FileConverter;
 import com.intuit.tools.imat.reporting.IReportingService;
 import com.intuit.tools.imat.validators.JavaScriptFileValidator;
 
 /**
  * @author rpfeffer
  * @dateCreated Mar 25, 2011
  *
  * //TODO RP: We should explain the use of this class once it has been implemented.
  *
  */
 @Parameters(commandDescription = "Use this command to run your tests.")
 public class CommandRunTests extends ScriptedCommand implements ICommand, IFileListener{
 
 	private static final long MONITORING_INTERVAL = 3000;
 	private static final String AUTOMATION_RESULTS_PATH = "logs" + File.separator +
 		"runs";
 	private static final String AUTOMATION_RESULTS_FILE = AUTOMATION_RESULTS_PATH + 
 		File.separator + "Run 1" + File.separator + "Automation Results.plist";
 	private static final String TEST_REPORT_PATH = "reports" + File.separator +
 		"junitReport.xml";
 		
 	
 	/**
 	 * The suite we will run in our tests
 	 */
 	public static final String SUITE_FILE = "-suite";
 	@Parameter(names = {SUITE_FILE, "-s"},
 			description = "The filename of the suite file to run. Note, this path " +
			"should be relative to the current directory.",
 			validateWith = JavaScriptFileValidator.class)
 	public String suite;
 	
 	/**
 	 * The template file we will use in our tests.
 	 */
 	public static final String TEMPLATE_FILE = "-template";
 	@Parameter(names = {TEMPLATE_FILE, "-t"},
 			description = "The filename of the template to run.",
 			converter = FileConverter.class)
 	public File template;
 	
 	public static final String NAME = "run-tests";
 	private static final int MAX_ATTEMPTS_TO_FIND_LOG = 5;
 	private static final long WAIT_PERIOD_FOR_LOG_IN_MILLISECONDS = 5000;
 	
 	private final IApplicationResourceService applicationResourceService;
 	private final IProjectResourceService projResourceService;
 	private final IFileMonitoringService fileMonitoringService;
 	private final ITestMonitor testMonitor;
 	private final IReportingService reportingService;
 	
 	/**
 	 * @param printwriter
 	 * @param logger
 	 * @param scriptLauncher
 	 * @param projResourceService
 	 */
 	CommandRunTests(PrintWriter printwriter, 
 			Logger logger, 
 			IScriptLauncher scriptLauncher, 
 			IApplicationResourceService applicationResourceService,
 			IProjectResourceService projResourceService,
 			IFileMonitoringService fileMonitoringService,
 			ITestMonitor testMonitor,
 			IReportingService reportingService
 			) {
 		super(printwriter, logger, scriptLauncher);
 		this.applicationResourceService = applicationResourceService;
 		this.projResourceService = projResourceService;
 		this.fileMonitoringService = fileMonitoringService;
 		this.testMonitor = testMonitor;
 		this.reportingService = reportingService;
 	}
 
 	
 	
 	/* (non-Javadoc)
 	 * @see com.intuit.tools.imat.ICommand#run()
 	 */
 	public int run() throws MisconfigurationException {
 		exitStatus  = startTests();
 		if (exitStatus == 0 ) {
 			monitorTests();
 			exitStatus = stopTests();
 		}
 		if(testMonitor.testsDidRunToCompletion()) {
 			generateReport();
 		}
 		if (exitStatus == 0 ) {
 			exitStatus = archiveTestResults();
 		}
 		
 		return exitStatus;
 	}
 
 
 	/**
 	 * @throws MisconfigurationException
 	 */
 	private int startTests() throws MisconfigurationException {
 		String suiteFilePath = "";
 		int exitCode = 0;
 		try {
 			LinkedHashMap<String, String> arguments = new LinkedHashMap<String, String>();
 			suiteFilePath = (new File(suite)).getCanonicalPath();
 			arguments.put(SUITE_FILE, suiteFilePath);
 			arguments.put(TEMPLATE_FILE, template.getCanonicalPath());
 			IScriptLauncher scriptLauncher = this.getScriptLauncher();
 			scriptLauncher.setProperties(arguments);
 			scriptLauncher.setScript("StartXCodeInstrument.scpt");
 			exitCode = scriptLauncher.runScript();
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
 	
 	/**
 	 * @throws MisconfigurationException 
 	 * 
 	 */
 	private void monitorTests() throws MisconfigurationException
 	{
 		File testResultLog = null;
 		int tries = 0;
 		while (testResultLog == null & tries < MAX_ATTEMPTS_TO_FIND_LOG)
 		{
 			try {
 				testResultLog = applicationResourceService.getAppResourceFile(
 						AUTOMATION_RESULTS_FILE, true);
 			} catch (FileNotFoundException e) {
 				logger.debug("Could not find log file on attempt: " + String.valueOf(tries));
 				try {
 					Thread.sleep(WAIT_PERIOD_FOR_LOG_IN_MILLISECONDS);
 				} catch (InterruptedException e1) {
 					logger.error("Interupted while waiting for test log file.", e);
 				}
 			}
 			tries++;
 		}
 		if (testResultLog == null) {
 			MisconfigurationException ex = new MisconfigurationException(
 					"Could not find Automation Results file: "
 					+ AUTOMATION_RESULTS_FILE);
 			throw ex;
 		}
 		logger.debug("Monitoring Log file...");
 		fileMonitoringService.monitorFile(testResultLog, MONITORING_INTERVAL, this);
 	}
 	
 	/**
 	 * 
 	 */
 	private synchronized int stopTests()
 	{
 		while(!testMonitor.isExecutionComplete())
 		{
 			try {
 				//wait until the test monitor says that all test execution is complete.
 				logger.debug("Waiting for test execution to complete.");
 				wait(); 
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		fileMonitoringService.stopMonitoring();
 		logger.debug("test execution is complete. Lauching Apple Script to stop Instruments.");
 		IScriptLauncher scriptLauncher = this.getScriptLauncher();
 		scriptLauncher.setScript("StopXCodeInstrument.scpt");
 		return scriptLauncher.runScript();
 	}
 	
 	private void generateReport() {
 		try {
 			File junitXmlResultFile = new File(projResourceService
 					.getProjectResourceFile("").getPath()
 					+ File.separator
 					+ TEST_REPORT_PATH);
 			File testResultLog = applicationResourceService.getAppResourceFile(
 					AUTOMATION_RESULTS_FILE, true);
 			reportingService.setJunitXMLResultFile(junitXmlResultFile);
 			reportingService
 					.convertTestOutputFileToJunitXMLResultFormat(testResultLog);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	private int archiveTestResults() throws MisconfigurationException {
 		String runLogDirPath ="";
 		int exitCode = 0;
 		try {
 			LinkedHashMap<String, String> arguments = new LinkedHashMap<String, String>();
 			runLogDirPath = applicationResourceService.getAppResourceFile(
 					AUTOMATION_RESULTS_PATH, true).getCanonicalPath();
 			arguments.put("RUN_LOG_DIR", runLogDirPath);
 			IScriptLauncher scriptLauncher = this.getScriptLauncher();
 			scriptLauncher.setProperties(arguments);
 			scriptLauncher.setScript("CleanUpXCodeInstrument.scpt");
 			exitCode = scriptLauncher.runScript();
 		} catch (FileNotFoundException e) {
 			MisconfigurationException ex = 
 				new MisconfigurationException("Could not find run log directory: " + runLogDirPath);
 			ex.initCause(e);
 			throw ex;
 		} catch (IOException e) {
 			MisconfigurationException ex = 
 				new MisconfigurationException("Error while resolving path.");
 			ex.initCause(e);
 			throw ex;
 		}
 		return exitCode;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.intuit.tools.imat.ICommand#getName()
 	 */
 	public String getName()
 	{
 		return CommandRunTests.NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.intuit.tools.imat.ICommand#isRunnable()
 	 */
 	public boolean isRunnable() {
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.intuit.tools.imat.commands.Command#expectsProject()
 	 */
 	@Override
 	public boolean expectsProject()
 	{
 		return true;
 	}
 
 
 
 	public synchronized void fileChanged(File file) {
 		this.testMonitor.fileChanged(file);
 		logger.trace("notifying that the file has changed.");
 		notifyAll();
 	}
 }
