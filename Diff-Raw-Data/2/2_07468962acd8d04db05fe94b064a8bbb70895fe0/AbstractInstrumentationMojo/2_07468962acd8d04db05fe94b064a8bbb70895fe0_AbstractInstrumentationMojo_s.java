 /*
  * Copyright (C) 2009-2011 Jayway AB
  * Copyright (C) 2007-2008 JVending Masa
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jayway.maven.plugins.android;
 
 import com.android.ddmlib.AdbCommandRejectedException;
 import com.android.ddmlib.IDevice;
 import com.android.ddmlib.ShellCommandUnresponsiveException;
 import com.android.ddmlib.TimeoutException;
 import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
 import com.android.ddmlib.testrunner.ITestRunListener;
 import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
 import com.android.ddmlib.testrunner.TestIdentifier;
 import com.jayway.maven.plugins.android.asm.AndroidTestFinder;
 import com.jayway.maven.plugins.android.common.DeviceHelper;
 import com.jayway.maven.plugins.android.configuration.Test;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import static com.android.ddmlib.testrunner.ITestRunListener.TestFailure.ERROR;
 
 /**
  * AbstractInstrumentationMojo implements running the instrumentation
  * tests.
  *
  * @author hugo.josefson@jayway.com
  * @author Manfred Moser <manfred@simpligility.com>
  */
 public abstract class AbstractInstrumentationMojo extends AbstractAndroidMojo {
 
     /**
      * -Dmaven.test.skip is commonly used with Maven to skip tests. We honor it too.
      *
      * @parameter expression="${maven.test.skip}" default-value=false
      * @readonly
      */
     private boolean mavenTestSkip;
 
     /**
      * -DskipTests is commonly used with Maven to skip tests. We honor it too.
      *
      * @parameter expression="${skipTests}" default-value=false
      * @readonly
      */
     private boolean mavenSkipTests;
 
     /**
      * The configuration to use for running instrumentation tests. Complete configuration
      * is possible in the plugin configuration:
      * <pre>
      * &lt;test&gt;
      *   &lt;skip&gt;true|false|auto&lt;/skip&gt;
      *   &lt;instrumentationPackage&gt;packageName&lt;/instrumentationPackage&gt;
      *   &lt;instrumentationRunner&gt;className&lt;/instrumentationRunner&gt;
      *   &lt;debug&gt;true|false&lt;/debug&gt;
      *   &lt;coverage&gt;true|false&lt;/coverage&gt;
     *   &lt;logonly&gt;true|false&lt;/logonly&gt;  avd
      *   &lt;testSize&gt;small|medium|large&lt;/testSize&gt;
      *   &lt;createReport&gt;true|false&lt;/createReport&gt;
      *   &lt;classes&gt;
      *     &lt;class&gt;your.package.name.YourTestClass&lt;/class&gt;
      *   &lt;/classes&gt;
      *   &lt;packages&gt;
      *     &lt;package&gt;your.package.name&lt;/package&gt;
      *   &lt;/packages&gt;
      * &lt;/test&gt;
      * </pre>
      *
      * @parameter
      */
     private Test test;
 
 
     /**
      * Enables or disables integration test related goals. If <code>true</code> they will be run; if <code>false</code>,
      * they will be skipped. If <code>auto</code>, they will run if any of the classes inherit from any class in
      * <code>junit.framework.**</code> or <code>android.test.**</code>.
      *
      * @parameter expression="${android.test.skip}" default-value="auto"
      */
     private String testSkip;
 
     /**
      * Package name of the apk we wish to instrument. If not specified, it is inferred from
      * <code>AndroidManifest.xml</code>.
      *
      * @optional
      * @parameter expression="${android.test.instrumentationPackage}
      */
     private String testInstrumentationPackage;
 
     /**
      * Class name of test runner. If not specified, it is inferred from <code>AndroidManifest.xml</code>.
      *
      * @optional
      * @parameter expression="${android.test.instrumentationRunner}"
      */
     private String testInstrumentationRunner;
 
     /**
      * Enable debug causing the test runner to wait until debugger is
      * connected with the Android debug bridge (adb).
      *
      * @optional
      * @parameter default-value=false expression="${android.test.debug}"
      */
     private boolean testDebug;
 
 
     /**
      * Enable or disable code coverage for this instrumentation test
      * run.
      *
      * @optional
      * @parameter default-value=false expression="${android.test.coverage}"
      */
     private boolean testCoverage;
 
     /**
      * Enable this flag to run a log only and not execute the tests.
      *
      * @optional
      * @parameter default-value=false expression="${android.test.logonly}"
      */
     private boolean testLogOnly;
 
     /**
      * If specified only execute tests of certain size as defined by
      * the Android instrumentation testing SmallTest, MediumTest and
      * LargeTest annotations. Use "small", "medium" or "large" as values.
      *
      * @see com.android.ddmlib.testrunner.IRemoteAndroidTestRunner
      *
      * @optional
      * @parameter expression="${android.test.testsize}"
      */
     private String testTestSize;
 
     /**
      * Create a junit xml format compatible output file containing
      * the test results for each device the instrumentation tests run
      * on.
      * <br /><br />
      * The files are stored in target/surefire-reports and named TEST-deviceid.xml.
      * The deviceid for an emulator is deviceSerialNumber_avdName_manufacturer_model.
      * The serial number is commonly emulator-5554 for the first emulator started
      * with numbers increasing. avdName is as defined in the SDK tool. The
      * manufacturer is typically "unknown" and the model is typically "sdk".
      * The deviceid for an actual devices is
      * deviceSerialNumber_manufacturer_model.
      * <br /><br />
      * The file contains system properties from the system running
      * the Android Maven Plugin (JVM) and device properties from the
      * device/emulator the tests are running on.
      * <br /><br />
      * The file contains a single TestSuite for all tests and a
      * TestCase for each test method. Errors and failures are logged
      * in the file and the system log with full stack traces and other
      * details available.
      *
      * @optional
      * @parameter default-value=true expression="${android.test.createreport}"
      */
     private boolean testCreateReport;
 
     /**
      * <p>Whether to execute tests only in given packages as part of the instrumentation tests.</p>
      * <pre>
      * &lt;packages&gt;
      *     &lt;package&gt;your.package.name&lt;/package&gt;
      * &lt;/packages&gt;
      * </pre>
      * or as e.g. -Dandroid.test.packages=package1,package2
      *
      * @optional
      * @parameter expression="${android.test.packages}
      */
     protected List testPackages;
 
     /**
      * <p>Whether to execute test classes which are specified as part of the instrumentation tests.</p>
      * <pre>
      * &lt;classes&gt;
      *     &lt;class&gt;your.package.name.YourTestClass&lt;/class&gt;
      * &lt;/classes&gt;
      * </pre>
      * or as e.g. -Dandroid.test.classes=class1,class2
      *
      * @optional
      * @parameter expression="${android.test.classes}
      */
     protected List testClasses;
 
     private boolean classesExists;
     private boolean packagesExists;
 
     // the parsed parameters from the plugin config or properties from command line or pom or settings
     private String parsedSkip;
     private String parsedInstrumentationPackage;
     private String parsedInstrumentationRunner;
     private List parsedClasses;
     private List parsedPackages;
     private String parsedTestSize;
     private boolean parsedCoverage;
     private boolean parsedDebug;
     private boolean parsedLogOnly;
     private boolean parsedCreateReport;
 
     private String packagesList;
 
     protected void instrument() throws MojoExecutionException, MojoFailureException {
         parseConfiguration();
 
         if (parsedInstrumentationPackage == null) {
             parsedInstrumentationPackage = extractPackageNameFromAndroidManifest(androidManifestFile);
         }
 
         if (parsedInstrumentationRunner == null) {
             parsedInstrumentationRunner = extractInstrumentationRunnerFromAndroidManifest(androidManifestFile);
         }
 
         // only run Tests in specific package
         packagesList = buildCommaSeparatedString(parsedPackages);
         packagesExists = StringUtils.isNotBlank(packagesList);
 
         if (parsedClasses != null) {
             classesExists = parsedClasses.size() > 0;
         } else {
             classesExists = false;
         }
 
         if(classesExists && packagesExists) {
             // if both packages and classes are specified --> ERROR
             throw new MojoFailureException("packages and classes are mutually exclusive. They cannot be specified at " +
                     "the same time. Please specify either packages or classes. For details, " +
                     "see http://developer.android.com/guide/developing/testing/testing_otheride.html");
         }
 
         doWithDevices(new DeviceCallback() {
             public void doWithDevice(final IDevice device) throws MojoExecutionException, MojoFailureException {
                 RemoteAndroidTestRunner remoteAndroidTestRunner =
                     new RemoteAndroidTestRunner(parsedInstrumentationPackage, parsedInstrumentationRunner, device);
 
                 if(packagesExists) {
                     remoteAndroidTestRunner.setTestPackageName(packagesList);
                     getLog().info("Running tests for specified test packages: " + packagesList);
                 }
 
                 if(classesExists) {
                     remoteAndroidTestRunner.setClassNames((String[]) parsedClasses.toArray());
                     getLog().info("Running tests for specified test classes/methods: " + parsedClasses);
                 }
 
                 remoteAndroidTestRunner.setDebug(parsedDebug);
                 remoteAndroidTestRunner.setCoverage(parsedCoverage);
                 remoteAndroidTestRunner.setLogOnly(parsedLogOnly);
 
                 if (StringUtils.isNotBlank(parsedTestSize)) {
                     IRemoteAndroidTestRunner.TestSize validSize =
                         IRemoteAndroidTestRunner.TestSize.getTestSize(parsedTestSize);
                     remoteAndroidTestRunner.setTestSize(validSize);
                 }
 
                 getLog().info("Running instrumentation tests in " + parsedInstrumentationPackage + " on " +
                     device.getSerialNumber() + " (avdName=" + device.getAvdName() + ")");
                 try {
                     AndroidTestRunListener testRunListener = new AndroidTestRunListener(project, device);
                     remoteAndroidTestRunner.run(testRunListener);
                     if (testRunListener.hasFailuresOrErrors()) {
                         throw new MojoFailureException("Tests failed on device.");
                     }
                     if (testRunListener.testRunFailed()) {
                         throw new MojoFailureException("Test run failed to complete: "+testRunListener.getTestRunFailureCause());
                     }
                     if (testRunListener.threwException()) {
                         throw new MojoFailureException(testRunListener.getExceptionMessages());
                     }
                 } catch (TimeoutException e) {
                     throw new MojoExecutionException("timeout", e);
                 } catch (AdbCommandRejectedException e) {
                     throw new MojoExecutionException("adb command rejected", e);
                 } catch (ShellCommandUnresponsiveException e) {
                     throw new MojoExecutionException("shell command " +
                         "unresponsive", e);
                 } catch (IOException e) {
                     throw new MojoExecutionException("IO problem", e);
                 }
             }
         });
     }
 
     private void parseConfiguration() {
         // we got config in pom ... lets use it,
         if (test != null) {
             parsedSkip = test.getSkip();
             parsedInstrumentationPackage = test.getInstrumentationPackage();
             parsedInstrumentationRunner = test.getInstrumentationRunner();
             parsedClasses = test.getClasses();
             parsedPackages = test.getPackages();
             parsedTestSize = test.getTestSize();
             parsedCoverage= test.isCoverage();
             parsedDebug= test.isDebug();
             parsedLogOnly = test.isLogOnly();
             parsedCreateReport = test.isCreateReport();
         }
         // no pom, we take properties
         else {
             parsedSkip = testSkip;
             parsedInstrumentationPackage = testInstrumentationPackage;
             parsedInstrumentationRunner = testInstrumentationRunner;
             parsedClasses = testClasses;
             parsedPackages = testPackages;
             parsedTestSize = testTestSize;
             parsedCoverage= testCoverage;
             parsedDebug= testDebug;
             parsedLogOnly = testLogOnly;
             parsedCreateReport = testCreateReport;
         }
     }
 
     /**
      * Whether or not to execute integration test related goals. Reads from configuration parameter
      * <code>enableIntegrationTest</code>, but can be overridden with <code>-Dmaven.test.skip</code>.
      *
      * @return <code>true</code> if integration test goals should be executed, <code>false</code> otherwise.
      */
     protected boolean isEnableIntegrationTest() throws MojoFailureException, MojoExecutionException {
         parseConfiguration();
         if (mavenTestSkip) {
             getLog().info("maven.test.skip set - skipping tests");
             return false;
         }
 
         if (mavenSkipTests) {
             getLog().info("maven.skip.tests set - skipping tests");
             return false;
         }
 
         if ("true".equalsIgnoreCase(parsedSkip)) {
             getLog().info("android.test.skip set - skipping tests");
             return false;
         }
 
         if ("false".equalsIgnoreCase(parsedSkip)) {
             return true;
         }
 
         if (parsedSkip == null || "auto".equalsIgnoreCase(parsedSkip)) {
             if (extractInstrumentationRunnerFromAndroidManifest(androidManifestFile) == null) {
                 getLog().info("No InstrumentationRunner found - skipping tests");
                 return false;
             }
             return AndroidTestFinder.containsAndroidTests(new File(project.getBuild().getDirectory(), "android-classes"));
         }
 
         throw new MojoFailureException("android.test.skip must be configured as 'true', 'false' or 'auto'.");
 
     }
 
     /**
      * Helper method to build a comma separated string from a list.
      * Blank strings are filtered out
      *
      * @param lines A list of strings
      * @return Comma separated String from given list
      */
     protected static String buildCommaSeparatedString(List<String> lines) {
     	if(lines == null || lines.size() == 0) {
     		return null;
     	}
 
     	List<String> strings = new ArrayList<String>(lines.size());
     	for(String str : lines) { // filter out blank strings
     		if(StringUtils.isNotBlank(str)) {
     			strings.add(StringUtils.trimToEmpty(str));
     		}
     	}
 
     	return StringUtils.join(strings, ",");
     }
 
     /**
      * AndroidTestRunListener produces a nice output for the log for the test
      * run as well as an xml file compatible with the junit xml report file
      * format understood by many tools.
      *
      * It will do so for each device/emulator the tests run on.
      */
     private class AndroidTestRunListener implements ITestRunListener {
         /** the indent used in the log to group items that belong together visually **/
         private static final String INDENT = "  ";
 
         /**
          * Junit report schema documentation is sparse. Here are some hints
          * @see "http://mail-archives.apache.org/mod_mbox/ant-dev/200902.mbox/%3Cdffc72020902241548l4316d645w2e98caf5f0aac770@mail.gmail.com%3E"
          * @see "http://junitpdfreport.sourceforge.net/managedcontent/PdfTranslation"
          */
         private static final String TAG_TESTSUITES = "testsuites";
 
         private static final String TAG_TESTSUITE = "testsuite";
         private static final String ATTR_TESTSUITE_ERRORS = "errors";
         private static final String ATTR_TESTSUITE_FAILURES = "failures";
         private static final String ATTR_TESTSUITE_HOSTNAME = "hostname";
         private static final String ATTR_TESTSUITE_NAME = "name";
         private static final String ATTR_TESTSUITE_TESTS = "tests";
         private static final String ATTR_TESTSUITE_TIME = "time";
         private static final String ATTR_TESTSUITE_TIMESTAMP = "timestamp";
 
         private static final String TAG_PROPERTIES = "properties";
         private static final String TAG_PROPERTY = "property";
         private static final String ATTR_PROPERTY_NAME = "name";
         private static final String ATTR_PROPERTY_VALUE = "value";
 
         private static final String TAG_TESTCASE = "testcase";
         private static final String ATTR_TESTCASE_NAME = "name";
         private static final String ATTR_TESTCASE_CLASSNAME = "classname";
         private static final String ATTR_TESTCASE_TIME = "time";
 
         private static final String TAG_ERROR = "error";
         private static final String TAG_FAILURE = "failure";
         private static final String ATTR_MESSAGE = "message";
         private static final String ATTR_TYPE = "type";
 
 
         /** time format for the output of milliseconds in seconds in the xml file **/
         private  final NumberFormat timeFormatter = new DecimalFormat("#0.0000");
 
         private int testCount = 0;
         private int testFailureCount = 0;
         private int testErrorCount = 0;
         private String testRunFailureCause = null;
 
         private final MavenProject project;
         /** the emulator or device we are running the tests on **/
         private final IDevice device;
 
 
         // junit xml report related fields
         private Document junitReport;
         private Node testSuiteNode;
 
         /** node for the current test case for junit report */
         private Node currentTestCaseNode;
         /** start time of current test case in millis, reset with each test start */
         private long currentTestCaseStartTime;
 
         // we track if we have problems and then report upstream
         private boolean threwException = false;
         private final StringBuilder exceptionMessages = new StringBuilder();
 
         public AndroidTestRunListener(MavenProject project, IDevice device) {
             this.project = project;
             this.device = device;
         }
 
         public void testRunStarted(String runName, int testCount) {
             this.testCount = testCount;
             getLog().info(INDENT + "Run started: " + runName + ", " + testCount + " tests:");
 
             if (parsedCreateReport) {
                 try {
                     DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
                     DocumentBuilder parser = null;
                     parser = fact.newDocumentBuilder();
                     junitReport = parser.newDocument();
 
                     Node testSuitesNode = junitReport.createElement(TAG_TESTSUITES);
                     junitReport.appendChild(testSuitesNode);
 
                     testSuiteNode = junitReport.createElement(TAG_TESTSUITE);
                     NamedNodeMap testSuiteAttributes = testSuiteNode.getAttributes();
 
                     Attr nameAttr = junitReport.createAttribute(ATTR_TESTSUITE_NAME);
                     nameAttr.setValue(runName);
                     testSuiteAttributes.setNamedItem(nameAttr);
 
                     Attr hostnameAttr = junitReport.createAttribute(ATTR_TESTSUITE_HOSTNAME);
                     hostnameAttr.setValue(DeviceHelper.getDescriptiveName(device));
                     testSuiteAttributes.setNamedItem(hostnameAttr);
 
                     Node propertiesNode = junitReport.createElement(TAG_PROPERTIES);
                     Node propertyNode;
                     NamedNodeMap propertyAttributes;
                     Attr propNameAttr;
                     Attr propValueAttr;
                     for (Map.Entry<Object, Object> systemProperty : System.getProperties().entrySet()) {
                         propertyNode = junitReport.createElement(TAG_PROPERTY);
                         propertyAttributes = propertyNode.getAttributes();
 
                         propNameAttr = junitReport.createAttribute(ATTR_PROPERTY_NAME);
                         propNameAttr.setValue(systemProperty.getKey().toString());
                         propertyAttributes.setNamedItem(propNameAttr);
 
                         propValueAttr = junitReport.createAttribute(ATTR_PROPERTY_VALUE);
                         propValueAttr.setValue(systemProperty.getValue().toString());
                         propertyAttributes.setNamedItem(propValueAttr);
 
                         propertiesNode.appendChild(propertyNode);
 
                     }
                     Map<String, String> deviceProperties = device.getProperties();
                     for (Map.Entry<String, String> deviceProperty : deviceProperties.entrySet()) {
                         propertyNode = junitReport.createElement(TAG_PROPERTY);
                         propertyAttributes = propertyNode.getAttributes();
 
                         propNameAttr = junitReport.createAttribute(ATTR_PROPERTY_NAME);
                         propNameAttr.setValue(deviceProperty.getKey());
                         propertyAttributes.setNamedItem(propNameAttr);
 
                         propValueAttr = junitReport.createAttribute(ATTR_PROPERTY_VALUE);
                         propValueAttr.setValue(deviceProperty.getValue());
                         propertyAttributes.setNamedItem(propValueAttr);
 
                         propertiesNode.appendChild(propertyNode);
                     }
 
                     testSuiteNode.appendChild(propertiesNode);
 
                     testSuitesNode.appendChild(testSuiteNode);
 
                 } catch (ParserConfigurationException e) {
                     threwException = true;
                     exceptionMessages.append("Failed to create document");
                     exceptionMessages.append(e.getMessage());
                 }
             }
         }
 
         public void testStarted(TestIdentifier test) {
            getLog().info(INDENT + INDENT +"Start: " + test.toString());
 
             if (parsedCreateReport) {
                 // reset start time for each test run
                 currentTestCaseStartTime = new Date().getTime();
 
                 currentTestCaseNode = junitReport.createElement(TAG_TESTCASE);
                 NamedNodeMap testCaseAttributes = currentTestCaseNode.getAttributes();
 
                 Attr classAttr = junitReport.createAttribute(ATTR_TESTCASE_CLASSNAME);
                 classAttr.setValue(test.getClassName());
                 testCaseAttributes.setNamedItem(classAttr);
 
                 Attr methodAttr = junitReport.createAttribute(ATTR_TESTCASE_NAME);
                 methodAttr.setValue(test.getTestName());
                 testCaseAttributes.setNamedItem(methodAttr);
             }
         }
 
         public void testFailed(TestFailure status, TestIdentifier test, String trace) {
             if (status==ERROR) {
                 ++testErrorCount;
             } else {
                 ++testFailureCount;
             }
             getLog().info(INDENT + INDENT + status.name() + ":" + test.toString());
             getLog().info(INDENT + INDENT + trace);
 
             if (parsedCreateReport) {
                 Node errorFailureNode;
                 NamedNodeMap errorfailureAttributes;
                 if (status == ERROR) {
                     errorFailureNode = junitReport.createElement(TAG_ERROR);
                     errorfailureAttributes = errorFailureNode.getAttributes();
                 } else {
                     errorFailureNode = junitReport.createElement(TAG_FAILURE);
                     errorfailureAttributes= errorFailureNode.getAttributes();
                 }
 
                 errorFailureNode.setTextContent(trace);
 
                 Attr msgAttr = junitReport.createAttribute(ATTR_MESSAGE);
                 msgAttr.setValue(parseForMessage(trace));
                 errorfailureAttributes.setNamedItem(msgAttr);
 
                 Attr typeAttr = junitReport.createAttribute(ATTR_TYPE);
                 typeAttr.setValue(parseForException(trace));
                 errorfailureAttributes.setNamedItem(typeAttr);
 
                 currentTestCaseNode.appendChild(errorFailureNode);
             }
         }
 
         public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
             getLog().info( INDENT + INDENT +"End: " + test.toString());
             logMetrics(testMetrics);
 
             if (parsedCreateReport) {
                 testSuiteNode.appendChild(currentTestCaseNode);
                 NamedNodeMap testCaseAttributes = currentTestCaseNode.getAttributes();
 
                 Attr timeAttr = junitReport.createAttribute(ATTR_TESTCASE_TIME);
 
                 long now = new Date().getTime();
                 double seconds = (now - currentTestCaseStartTime)/1000.0;
                 timeAttr.setValue(timeFormatter.format(seconds));
                 testCaseAttributes.setNamedItem(timeAttr);
             }
         }
 
         public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
             getLog().info(INDENT +"Run ended: " + elapsedTime + " ms");
             if (hasFailuresOrErrors()) {
                 getLog().error(INDENT + "FAILURES!!!");
             }
             getLog().info(INDENT + "Tests run: " + testCount + ",  Failures: "
                     + testFailureCount + ",  Errors: " + testErrorCount);
             if (parsedCreateReport) {
                 NamedNodeMap testSuiteAttributes = testSuiteNode.getAttributes();
 
                 Attr testCountAttr = junitReport.createAttribute(ATTR_TESTSUITE_TESTS);
                 testCountAttr.setValue(Integer.toString(testCount));
                 testSuiteAttributes.setNamedItem(testCountAttr);
 
                 Attr testFailuresAttr = junitReport.createAttribute(ATTR_TESTSUITE_FAILURES);
                 testFailuresAttr.setValue(Integer.toString(testFailureCount));
                 testSuiteAttributes.setNamedItem(testFailuresAttr);
 
                 Attr testErrorsAttr = junitReport.createAttribute(ATTR_TESTSUITE_ERRORS);
                 testErrorsAttr.setValue(Integer.toString(testErrorCount));
                 testSuiteAttributes.setNamedItem(testErrorsAttr);
 
                 Attr timeAttr = junitReport.createAttribute(ATTR_TESTSUITE_TIME);
                 timeAttr.setValue(timeFormatter.format(elapsedTime / 1000.0));
                 testSuiteAttributes.setNamedItem(timeAttr);
 
                 Attr timeStampAttr = junitReport.createAttribute(ATTR_TESTSUITE_TIMESTAMP);
                 timeStampAttr.setValue(new Date().toString());
                 testSuiteAttributes.setNamedItem(timeStampAttr);
             }
 
             logMetrics(runMetrics);
 
             if (parsedCreateReport) {
                 writeJunitReportToFile();
             }
         }
 
         public void testRunFailed(String errorMessage) {
             testRunFailureCause = errorMessage;
             getLog().info(INDENT +"Run failed: " + errorMessage);
         }
 
         public void testRunStopped(long elapsedTime) {
             getLog().info(INDENT +"Run stopped:" + elapsedTime);
         }
 
 
         /**
          * Parse a trace string for the message in it. Assumes that the message is located after ":" and before
          * "\r\n".
          * @param trace
          * @return message or empty string
          */
         private String parseForMessage(String trace) {
             if (StringUtils.isNotBlank(trace)) {
                 String newline = "\r\n";
                 // if there is message like
                 // junit.junit.framework.AssertionFailedError ... there is no message
                 int messageEnd = trace.indexOf(newline);
                 boolean hasMessage = !trace.startsWith("junit.") && messageEnd > 0;
                 if (hasMessage) {
                     int messageStart = trace.indexOf(":") + 2;
                     if (messageStart > messageEnd) {
                         messageEnd = trace.indexOf(newline+"at"); // match start of stack trace "\r\nat org.junit....."
                     }
                     return trace.substring(messageStart, messageEnd);
                 } else {
                     return StringUtils.EMPTY;
                 }
             } else {
                 return StringUtils.EMPTY;
             }
         }
 
         /**
          * Parse a trace string for the exception class. Assumes that it is the start of the trace and ends at the first
          * ":".
          * @param trace
          * @return  Exception class as string or empty string
          */
         private String parseForException(String trace) {
             if (StringUtils.isNotBlank(trace)) {
                 return trace.substring(0, trace.indexOf(":"));
             } else {
                 return StringUtils.EMPTY;
             }
         }
 
         /**
          * Write the junit report xml file.
          */
         private void writeJunitReportToFile() {
             TransformerFactory xfactory = TransformerFactory.newInstance();
             Transformer xformer = null;
             try {
                 xformer = xfactory.newTransformer();
             } catch (TransformerConfigurationException e) {
                 e.printStackTrace();
             }
             Source source = new DOMSource(junitReport);
 
             FileWriter writer = null;
             try {
                 String directory =  new StringBuilder()
                         .append(project.getBuild().getDirectory())
                         .append("/surefire-reports")
                         .toString();
 
                 FileUtils.forceMkdir(new File(directory));
 
                 String fileName = new StringBuilder()
                         .append(directory)
                         .append("/TEST-")
                         .append(DeviceHelper.getDescriptiveName(device))
                         .append(".xml")
                         .toString();
                 File reportFile = new File(fileName);
                 writer = new FileWriter(reportFile);
                 Result result = new StreamResult(writer);
 
                 xformer.transform(source, result);
                 getLog().info("Report file written to " + reportFile.getAbsolutePath());
             } catch (IOException e) {
                 threwException = true;
                 exceptionMessages.append("Failed to write test report file");
                 exceptionMessages.append(e.getMessage());
             } catch (TransformerException e) {
                 threwException = true;
                 exceptionMessages.append("Failed to transform document to write to test report file");
                 exceptionMessages.append(e.getMessage());
             } finally {
                 IOUtils.closeQuietly(writer);
             }
         }
 
         /**
          * Log all the metrics out in to key: value lines.
          * @param metrics
          */
         private void logMetrics(Map<String, String> metrics) {
             for (Map.Entry<String, String> entry : metrics.entrySet()) {
                 getLog().info(INDENT + INDENT + entry.getKey() + ": "
                     + entry.getValue());
             }
         }
 
         /**
          * @return if any failures or errors occurred in the test run.
          */
         public boolean hasFailuresOrErrors() {
             return testErrorCount > 0 || testFailureCount > 0;
         }
 
         /**
          * @return if the test run itself failed - a failure in the test infrastructure, not a test failure.
          */
         public boolean testRunFailed() {
             return testRunFailureCause != null;
         }
 
         public String getTestRunFailureCause() {
             return testRunFailureCause;
         }
 
         /**
          * @return if any exception was thrown during the test run
          * on the build system (not the Android device or emulator)
          */
         public boolean threwException() {
             return threwException;
         }
 
         /**
          * @return all exception messages thrown during test execution
          * on the test run time (not the Android device or emulator)
          */
         public String getExceptionMessages() {
             return exceptionMessages.toString();
         }
     }
 }
