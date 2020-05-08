 /**
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.tooling.jubula;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.mule.tooling.jubula.cliexecutor.JubulaCliExecutor;
 import org.mule.tooling.jubula.cliexecutor.JubulaCliExecutorFactory;
 import org.mule.tooling.jubula.cliexecutor.SyncCallback;
 import org.mule.tooling.jubula.results.TestSuiteResult;
 import org.mule.tooling.jubula.xmlgenerator.XMLSurefireGenerator;
 import org.mule.tooling.jubula.xmlgenerator.XMLSurefireGeneratorException;
 import org.mule.tooling.jubula.xmlparser.XMLJubulaParser;
 import org.mule.tooling.jubula.xmlparser.XMLJubulaParserException;
 
 /**
  * Goal that runs Jubula Functional Tests.
  * 
  * @goal test
  * @phase integration-test
  */
 public class JubulaMojo extends AbstractMojo {
 
 	/**
 	 * Project's target folder.
 	 * 
 	 * @parameter expression="${project.build.directory}"
 	 * @readonly
 	 * @required
 	 */
 	private File buildDirectory;
 
 	/**
 	 * The id for the aut being run.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String autId;
 
 	/**
 	 * The working directory of the RCP under test.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String rcpWorkingDir;
 
 	/**
 	 * The name of the executable file of the RCP.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String executableFileName;
 
 	/**
 	 * The keyboard layout
 	 * 
 	 * @parameter default-value="en_US"
 	 * @required
 	 */
 	private String keyboardLayout;
 
 	/**
 	 * The address where the aut agent will be listening (localhost:60000 by
 	 * default)
 	 * 
 	 * @parameter default-value="localhost:60000"
 	 * @required
 	 */
 	private String autAgentAddress;
 
 	/**
 	 * The test project name
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String projectName;
 
 	/**
 	 * The test project version (1.0 by default)
 	 * 
 	 * @parameter default-value="1.0"
 	 * @required
 	 */
 	private String projectVersion;
 
 	/**
 	 * The address of the database containing the jubula data (jdbc:..).
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String databaseUrl;
 
 	/**
 	 * The username to access the db
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String databaseUser;
 
 	/**
 	 * The password to access the db
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String databasePassword;
 
 	/**
 	 * The job to run
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String testJob;
 
 	/**
 	 * Path to external test data directory
 	 * 
 	 * @parameter default-value="."
 	 */
 	private String datadir;
 
 	/**
 	 * Amount of milliseconds to wait for the AUT to be considered as started
 	 * 
 	 * @parameter default-value="20000"
 	 */
 	private int delayAfterAutStart;
 
 	/**
 	 * Amount of milliseconds to wait for the AUT agent to be considered as
 	 * started (this exists because autagent and startaut share the same config
 	 * area and if there is not some amount of waiting between the starting of
 	 * those two programs, some file locking might get in the way)
 	 * 
 	 * @parameter default-value="5000"
 	 */
 	private int delayAfterAutAgentStart;
 
 	@Override
 	public void execute() throws MojoExecutionException {
 		validateParameters();
 
 		String jubulaInstallationPath = JubulaBootstrapUtils.pathToJubulaInstallationDirectory(buildDirectory);
 		String resultsDir = new File(buildDirectory, JubulaBootstrapUtils.RESULTS_DIRECTORY_NAME).getAbsolutePath();
 		String workspacePath = new File(buildDirectory, JubulaBootstrapUtils.RCPWORKSPACE_DIRECTORY_NAME).getAbsolutePath();
 		String autAgentHost = autAgentAddress.split(":")[0];
 		String autAgentPort = autAgentAddress.split(":")[1];
 		JubulaCliExecutor jubulaCliExecutor = new JubulaCliExecutorFactory().getNewInstance(jubulaInstallationPath);
 		SyncCallback startAutAgentCallback = new SyncCallback();
 		SyncCallback startAutCallback = new SyncCallback();
 
 		// start the aut agent
 		this.getLog().info("Starting AUT Agent...");
 		jubulaCliExecutor.startAutAgent(startAutAgentCallback);
 		this.safeSleep(this.delayAfterAutAgentStart);
 
 		try {
 			getLog().info("Starting AUT...");
 			jubulaCliExecutor.startAut(autId, rcpWorkingDir, executableFileName, workspacePath, keyboardLayout, autAgentHost, autAgentPort, startAutCallback);
 
 			safeSleep(delayAfterAutStart);
 			getLog().debug("Considered AUT as fully initialized");
 
 			getLog().info("Connect to database and start running tests...");
			boolean runTests = jubulaCliExecutor.runTests(projectName, projectVersion, workspacePath, databaseUrl, databaseUser, databasePassword, autAgentHost, autAgentPort,
 					keyboardLayout.toUpperCase(), testJob, datadir, resultsDir);
 
 			getLog().info("Finished running tests");
 			if (!runTests)
 				throw new MojoExecutionException("There were errors running the tests");
 		} finally {
 			reportResults();
 			jubulaCliExecutor.stopAutAgent();
 		}
 
 	}
 
 	private void validateParameters() throws MojoExecutionException {
 		String[] hostAndPort = autAgentAddress.split(":");
 		if (hostAndPort.length != 2)
 			throw new MojoExecutionException("Please provide the AUT Agent address as <host>:<port>");
 	}
 
 	private void reportResults() throws MojoExecutionException {
 		String jubulaResultsFolder = buildDirectory + File.separator + JubulaBootstrapUtils.RESULTS_DIRECTORY_NAME;
 		String surefireResultsFolder = buildDirectory + File.separator + JubulaBootstrapUtils.SUREFIRE_RESULTS_DIRECTORY_NAME;
 
 		XMLJubulaParser jubulaParser = new XMLJubulaParser();
 		XMLSurefireGenerator surefireGenerator = new XMLSurefireGenerator(surefireResultsFolder);
 
 		try {
 			getLog().debug("Reading surefire reports from Jubula results files...");
 			List<TestSuiteResult> testSuites = jubulaParser.generateSuitesFromFolder(jubulaResultsFolder);
 			getLog().debug("Writing surefire reports from Jubula results files...");
 			surefireGenerator.generateXML(testSuites);
 		} catch (XMLJubulaParserException e) {
 			throw new MojoExecutionException("There was a problem parsing the Jubula results", e);
 		} catch (XMLSurefireGeneratorException e) {
 			throw new MojoExecutionException("There was a problem generating the surefire results", e);
 		}
 	}
 
 	private void safeSleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
