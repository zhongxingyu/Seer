 /*
  * The MIT License
  *
  * Copyright (c) <2012> <Bruno P. Kinoshita>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package jenkins.plugins.testthemall;
 
 import hudson.AbortException;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.EnvironmentContributingAction;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStep;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.mozilla.testopia.model.TestCase;
 import org.mozilla.testopia.service.xmlrpc.XmlRpcMiscService;
 import org.mozilla.testopia.service.xmlrpc.XmlRpcTestRunService;
 import org.mozilla.testopia.transport.TestopiaXmlRpcClient;
 
 /**
  * Testopia Builder.
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  */
 public class TestopiaBuilder extends Builder {
 	// Used for HTTP basic auth
 	private static final String BASIC_HTTP_PASSWORD = "basicPassword";
 	
 	private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.testopia");
 	
 	/**
 	 * Testopia installation name.
 	 */
 	protected final String testopiaInstallationName;
 	/**
 	 * Testopia test run ID.
 	 */
 	protected final Integer testRunId;
 	/**
 	 * List of build steps that are executed only once per job execution. 
 	 */
 	protected final List<BuildStep> singleBuildSteps;
 	/**
 	 * List of build steps that are executed before iterating all test cases.
 	 */
 	protected final List<BuildStep> beforeIteratingAllTestCasesBuildSteps;
 	/**
 	 * List of build steps that are executed for each test case.
 	 */
 	protected final List<BuildStep> iterativeBuildSteps;
 	/**
 	 * List of build steps that are executed after iterating all test cases.
 	 */
 	protected final List<BuildStep> afterIteratingAllTestCasesBuildSteps;
 	/**
 	 * Le descriptor.
 	 */
 	@Extension
 	public static final TestopiaBuilderDescriptor DESCRIPTOR = new TestopiaBuilderDescriptor();
 	/**
 	 * This constructor is bound to a stapler request. The parameters are 
 	 * passed from Jenkins UI.
 	 * @param testopiaInstallationName
 	 * @param testRunId
 	 * @param singleBuildSteps
 	 * @param beforeIteratingAllTestCasesBuildSteps
 	 * @param iterativeBuildSteps
 	 * @param afterIteratingAllTestCasesBuildSteps
 	 */
 	@DataBoundConstructor
 	public TestopiaBuilder(String testopiaInstallationName, 
 			Integer testRunId,
 			List<BuildStep> singleBuildSteps, 
 			List<BuildStep> beforeIteratingAllTestCasesBuildSteps, 
 			List<BuildStep> iterativeBuildSteps,
 			List<BuildStep> afterIteratingAllTestCasesBuildSteps) {
 		this.testopiaInstallationName = testopiaInstallationName;
 		this.testRunId = testRunId;
 		this.singleBuildSteps = singleBuildSteps;
 		this.beforeIteratingAllTestCasesBuildSteps = beforeIteratingAllTestCasesBuildSteps;
 		this.iterativeBuildSteps = iterativeBuildSteps;
 		this.afterIteratingAllTestCasesBuildSteps = afterIteratingAllTestCasesBuildSteps;
 	}
 	/**
 	 * @return the testopiaInstallationName
 	 */
 	public String getTestopiaInstallationName() {
 		return testopiaInstallationName;
 	}
 	/**
 	 * @return the testRunId
 	 */
 	public Integer getTestRunId() {
 		return testRunId;
 	}
 	/**
 	 * @return the singleBuildSteps
 	 */
 	public List<BuildStep> getSingleBuildSteps() {
 		return singleBuildSteps;
 	}
 	/**
 	 * @return the beforeIteratingAllTestCasesBuildSteps
 	 */
 	public List<BuildStep> getBeforeIteratingAllTestCasesBuildSteps() {
 		return beforeIteratingAllTestCasesBuildSteps;
 	}
 	/**
 	 * @return the iterativeBuildSteps
 	 */
 	public List<BuildStep> getIterativeBuildSteps() {
 		return iterativeBuildSteps;
 	}
 	/**
 	 * @return the afterIteratingAllTestCasesBuildSteps
 	 */
 	public List<BuildStep> getAfterIteratingAllTestCasesBuildSteps() {
 		return afterIteratingAllTestCasesBuildSteps;
 	}
 	/* (non-Javadoc)
 	 * @see hudson.tasks.BuildStepCompatibilityLayer#getProjectAction(hudson.model.AbstractProject)
 	 */
 	@Override
 	public Action getProjectAction(AbstractProject<?, ?> project) {
 		return new TestopiaProjectAction(project);
 	}
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * Executes Testopia automated tests.
 	 */
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) throws InterruptedException, IOException {
 		listener.getLogger().println("Connecting to Testopia to retrieve automated test cases.");
 		TestopiaInstallation installation = DESCRIPTOR.getInstallationByName(this.testopiaInstallationName);
 		if(installation == null) {
 			throw new AbortException("Invalid Testopia installation.");
 		}
 		if(StringUtils.isNotBlank(installation.getProperties())) {
 			listener.getLogger().println("Preparing Testopia connection properties.");
 			setProperties(installation.getProperties(), listener);
 		}
 		URL url = new URL(installation.getUrl());
         TestopiaXmlRpcClient xmlRpcClient = new TestopiaXmlRpcClient(url);
         XmlRpcMiscService misc = new XmlRpcMiscService(xmlRpcClient);
         XmlRpcTestRunService testRunSvc = new XmlRpcTestRunService(xmlRpcClient);
 		try {
 			misc.login(installation.getUsername(), installation.getPassword());
 		} catch (Exception e) {
 			e.printStackTrace(listener.getLogger());
 			throw new AbortException(e.getMessage());
 		}
 		//TestRun testRun = testRunSvc.get(this.getTestRunId());
 		if(LOGGER.isLoggable(Level.FINE)) {
 			LOGGER.log(Level.FINE, "Filtering for automated test cases...");
 		}
 		TestCase[] testCases = filter(testRunSvc.getTestCases(this.getTestRunId()));
 		// sort and filter test cases
 		listener.getLogger().println("Executing single build steps");
 		this.executeSingleBuildSteps(build, launcher, listener);
 		listener.getLogger().println("Executing iterative build steps");
 		this.executeIterativeBuildSteps(testCases, build, launcher, listener);
 		//TODO: look for results
 		//TODO: create report
 		//TODO: create graphs
 		return Boolean.TRUE;
 	}
 	/**
 	 * Filter an array of test cases for automated test cases only.
 	 * @param testCases array of test cases
 	 * @return filtered array of automated test cases
 	 */
 	private TestCase[] filter(TestCase[] testCases) {
 		List<TestCase> automatedTestCases = new ArrayList<TestCase>();
 		if(testCases != null) {
 			for(TestCase testCase : testCases) {
				if(testCase.getAutomated()) {
 					automatedTestCases.add(testCase);
 				} // else drop it
 			}
 		}
 		return automatedTestCases.toArray(new TestCase[0]);
 	}
 	/**
 	 * Executes the list of single build steps.
 	 * 
 	 * @param build
 	 *            Jenkins build.
 	 * @param launcher
 	 * @param listener
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	protected void executeSingleBuildSteps(AbstractBuild<?, ?> build,
 			Launcher launcher, BuildListener listener) throws IOException,
 			InterruptedException {
 		if (singleBuildSteps != null) {
 			for (BuildStep b : singleBuildSteps) {
 				boolean success = b.perform(build, launcher, listener);
 				if(!success) {
 					build.setResult(Result.UNSTABLE);
 				}
 			}
 		}
 	}
 	/**
 	 * <p>
 	 * Executes iterative build steps. For each automated test case found in the
 	 * array of automated test cases, this method executes the iterative builds
 	 * steps using Jenkins objects.
 	 * </p>
 	 * 
 	 * @param testCases
 	 *            array of automated test cases
 	 * @param launcher
 	 * @param listener
 	 * @throws InterruptedException
 	 * @throws IOException
 	 */
 	protected void executeIterativeBuildSteps(TestCase[] testCases,
 			AbstractBuild<?, ?> build,
 			Launcher launcher, BuildListener listener) throws IOException,
 			InterruptedException {
 		if (beforeIteratingAllTestCasesBuildSteps != null) {
 			for (BuildStep b : beforeIteratingAllTestCasesBuildSteps) {
 				final boolean success = b.perform(build, launcher, listener);
 				if(!success) {
 					build.setResult(Result.UNSTABLE);
 				}
 			}
 		}
 		if (iterativeBuildSteps != null) {
 			for (TestCase automatedTestCase : testCases) {
 				if(automatedTestCase == null) {
 					continue;
 				}
 				if(LOGGER.isLoggable(Level.FINE)) {
 					LOGGER.log(Level.FINE, "Executing iterative build step");
 					LOGGER.log(Level.FINE, "TestCase: id["+automatedTestCase.getId()+"], script["+automatedTestCase.getScript()+"]");
 				}
 				final EnvVars iterativeEnvVars = Utils.buildTestCaseEnvVars(automatedTestCase);
 				build.addAction(new EnvironmentContributingAction() {
 					public void buildEnvVars(AbstractBuild<?, ?> build,
 							EnvVars env) {
 						env.putAll(iterativeEnvVars);
 					}
 
 					public String getUrlName() {
 						return null;
 					}
 
 					public String getIconFileName() {
 						return null;
 					}
 
 					public String getDisplayName() {
 						return null;
 					}
 				});
 				for (BuildStep b : iterativeBuildSteps) {
 					final boolean success = b
 							.perform(build, launcher, listener);
 					if(!success) {
 						build.setResult(Result.UNSTABLE);
 					}
 				}
 			}
 		}
 		if (afterIteratingAllTestCasesBuildSteps != null) {
 			for (BuildStep b : afterIteratingAllTestCasesBuildSteps) {
 				final boolean success = b.perform(build, launcher, listener);
 				if(!success) {
 					build.setResult(Result.UNSTABLE);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * <p>Define properties. Following is the list of available properties.</p>
 	 * 
 	 * <ul>
 	 *  	<li>xmlrpc.basicEncoding</li>
  	 *  	<li>xmlrpc.basicPassword</li>
  	 *  	<li>xmlrpc.basicUsername</li>
  	 *  	<li>xmlrpc.connectionTimeout</li>
  	 *  	<li>xmlrpc.contentLengthOptional</li>
  	 *  	<li>xmlrpc.enabledForExceptions</li>
  	 *  	<li>xmlrpc.encoding</li>
  	 *  	<li>xmlrpc.gzipCompression</li>
  	 *  	<li>xmlrpc.gzipRequesting</li>
  	 *  	<li>xmlrpc.replyTimeout</li>
  	 *  	<li>xmlrpc.userAgent</li>
 	 * </ul>
 	 * 
 	 * @param properties List of comma separated properties
 	 * @param listener Jenkins Build listener
 	 */
 	public static void setProperties(String properties, BuildListener listener) {
 		if (StringUtils.isNotBlank(properties)) {
 			final StringTokenizer tokenizer = new StringTokenizer(properties, ",");
 
 			if (tokenizer.countTokens() > 0) {
 				while (tokenizer.hasMoreTokens()) {
 					String systemProperty = tokenizer.nextToken();
 					maybeAddSystemProperty(systemProperty, listener);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Maybe adds a system property if it is in format <key>=<value>.
 	 * 
 	 * @param systemProperty System property entry in format <key>=<value>.
 	 * @param listener Jenkins Build listener
 	 */
 	public static void maybeAddSystemProperty(String systemProperty, BuildListener listener) {
 		final StringTokenizer tokenizer = new StringTokenizer(systemProperty, "=:");
 		if (tokenizer.countTokens() == 2) {
 			final String key = tokenizer.nextToken();
 			final String value = tokenizer.nextToken();
 
 			if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
 				if (key.contains(BASIC_HTTP_PASSWORD)) {
 					listener.getLogger().println("Setting key " + key + "=********");
 				} else {
 					listener.getLogger().println("Setting key " + key + "=" + value);
 				}
 				try {
 					System.setProperty(key, value);
 				} catch (SecurityException se) {
 					se.printStackTrace(listener.getLogger());
 				}
 
 			}
 		}
 	}
 }
