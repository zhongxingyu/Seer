 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.tests.helpers;
 
 import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
 import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
 import static org.ops4j.pax.exam.CoreOptions.junitBundles;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.systemProperty;
 import static org.ops4j.pax.exam.CoreOptions.systemTimeout;
 import static org.ops4j.pax.exam.CoreOptions.vmOption;
 import static org.ops4j.pax.exam.CoreOptions.when;
 
 import java.io.File;
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TestName;
 import org.junit.rules.TestRule;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.options.CompositeOption;
 import org.ops4j.pax.exam.options.DefaultCompositeOption;
 import org.ops4j.pax.exam.util.PathUtils;
 import org.osgi.framework.BundleContext;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.ComponentBroker;
 
 public abstract class ExtensionAbstract extends TestUtils {
 
 	// Based on the current running, no test should take longer than 2 minute
 	@Rule
 	public TestRule globalTimeout = new ApamTimeoutRule(
 			isDebugModeOn() ? 3600000 : 60000);
 
 	@Rule
 	public TestName name = new TestName();
 
 	@Inject
 	public BundleContext context;
 
 	// private Logger logger = LoggerFactory.getLogger(getClass());
 	private Logger logger = (Logger) LoggerFactory
 			.getLogger(Logger.ROOT_LOGGER_NAME);
 
 	public ApAMHelper apam;
 
 	protected ComponentBroker broker;
 
 	boolean startObrMan = false;
 
 	protected static Option[] configuration = null;
 
 	@Configuration
 	public Option[] apamConfig() {
 		Option conf[] = config().toArray(new Option[0]);
 
 		configuration = conf;
 
 		return conf;
 	}
 
 	public List<Option> config() {
 
 		return config(null, true);
 
 	}
 
 	public List<Option> config(Map<String, String> testApps, boolean startObrMan) {
		
 		List<Option> config = new ArrayList<Option>();
 		config.add(packInitialConfig());
 		config.add(packOSGi());
 		config.add(packPax());
 		config.add(packApamCore());
 		config.add(packApamShell());
 		this.startObrMan = startObrMan;
 		if (startObrMan) {
 			config.add(packApamObrMan());
 		}
 		if (testApps != null && !testApps.isEmpty()) {
 			for (String artifactID : testApps.keySet()) {
 				config.add(packAppForTestBundles(testApps.get(artifactID),
 						artifactID));
 			}
 		}
 		// config.add(packAppForTestBundles()); Not for all tests
 		config.add(packLog());
 		config.add(junitBundles());
 		config.add(packDebugConfiguration());
 		// config.add(vmOption("-ea"));
 
 		File ref_conf = new File("conf/root.OBRMAN.cfgo");
 		File use_conf = new File("conf/root.OBRMAN.cfg");
 
 		if (startObrMan) {
 			if (!use_conf.exists()) {
 				ref_conf.renameTo(use_conf);
 			}
 		} else if (use_conf.exists()) {
 			use_conf.renameTo(ref_conf);
 		}
 
 		return config;
 	}
 
 	public List<Option> configWithoutTests() {
 
 		List<Option> config = new ArrayList<Option>();
 
 		config.add(packInitialConfig());
 		config.add(packOSGi());
 		config.add(packPax());
 		config.add(packApamCore());
 		config.add(packApamShell());
 		config.add(packLog());
 		config.add(junitBundles());
 		config.add(packDebugConfiguration());
 		// config.add(vmOption("-ea"));
 
 		return config;
 	}
 
 	private boolean isDebugModeOn() {
 		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
 		List<String> arguments = RuntimemxBean.getInputArguments();
 
 		boolean debugModeOn = false;
 
 		for (String string : arguments) {
 			debugModeOn = string.indexOf("jdwp") != -1;
 			if (debugModeOn) {
 				break;
 			}
 		}
 
 		return debugModeOn;
 	}
 
 	protected CompositeOption packApamConflictManager() {
 		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
 				mavenBundle("fr.imag.adele.apam", "manager.conflict")
 						.versionAsInProject());
 
 		return apamObrmanConfig;
 	}
 
 	protected CompositeOption packApamCore() {
 
 		CompositeOption apamCoreConfig = new DefaultCompositeOption(
 				mavenBundle().groupId("fr.imag.adele.apam")
 						.artifactId("apam-bundle").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests", "apam-helpers")
 						.versionAsInProject());
 
 		return apamCoreConfig;
 	}
 
 	protected CompositeOption packApamDistriMan() {
 		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
 				// version as in project
 				// org.ops4j.pax.exam.CoreOptions
 				// .repositories(
 				// "http://repo.maven.apache.org/maven2/",
 				// "https://maven.java.net/content/repositories/releases/",
 				// "https://repository.apache.org/content/groups/public",
 				// "https://repository.apache.org/content/groups/snapshots",
 				//
 				// "http://repository.springsource.com/maven/bundles/release",
 				// "http://repository.springsource.com/maven/bundles/external",
 				// "http://repository.ow2.org/nexus/content/repositories/snapshots",
 				// "http://repository.ow2.org/nexus/content/repositories/releases",
 				// "http://repository.ow2.org/nexus/content/sites/ow2-utilities",
 				// "http://repository.ow2.org/nexus/content/repositories/thirdparty",
 				// "http://repository.ow2.org/nexus/content/repositories/ow2-legacy",
 				// "http://repository.ow2.org/nexus/content/groups/public"),
 
 				mavenBundle("org.ow2.asm", "asm-all").version("4.1"),
 				mavenBundle("javax.mail", "com.springsource.javax.mail")
 						.version("1.4.1"), mavenBundle("javax.wsdl",
 						"com.springsource.javax.wsdl").version("1.6.1"),
 				mavenBundle("javax.xml.stream",
 						"com.springsource.javax.xml.stream").version("1.0.1"),
 				mavenBundle("org.apache.xml",
 						"com.springsource.org.apache.xml.resolver").version(
 						"1.2.0"), mavenBundle("org.dom4j",
 						"com.springsource.org.dom4j").version("1.6.1"),
 				mavenBundle("joda-time", "joda-time").version("1.6.2"),
 				mavenBundle("org.apache.cxf", "cxf-bundle-minimal").version(
 						"2.5.2"), mavenBundle("com.google.guava", "guava")
 						.version("13.0-rc1"), mavenBundle("javax.ws.rs",
 						"javax.ws.rs-api").version("2.0-m09"), mavenBundle(
 						"javax.ws.rs", "jsr311-api").version("1.1"),
 				mavenBundle("org.apache.neethi", "neethi").version("3.0.2"),
 				mavenBundle("org.apache.felix", "org.apache.felix.http.jetty")
 						.version("2.2.0"), mavenBundle(
 						"org.apache.servicemix.bundles",
 						"org.apache.servicemix.bundles.opensaml").version(
 						"2.4.1_1"), mavenBundle("org.apache.ws.xmlschema",
 						"xmlschema-core").version("2.0"), mavenBundle(
 						"org.codehaus.jackson", "jackson-core-asl").version(
 						"1.9.12"), mavenBundle("org.codehaus.jackson",
 						"jackson-mapper-asl").version("1.9.12"), mavenBundle(
 						"fr.imag.adele.apam", "DISTRIMAN").versionAsInProject()
 
 		);
 
 		return apamObrmanConfig;
 	}
 
 	protected CompositeOption packApamObrMan() {
 		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
 				mavenBundle().groupId("fr.imag.adele.apam")
 						.artifactId("obrman").versionAsInProject());
 
 		return apamObrmanConfig;
 	}
 
 	protected CompositeOption packApamShell() {
 		CompositeOption logConfig = new DefaultCompositeOption(
 				mavenBundle("fr.imag.adele.apam", "apam-universal-shell")
 						.versionAsInProject(),
 				mavenBundle("org.apache.felix", "org.apache.felix.gogo.command")
 						.version("0.12.0"),
 				mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime")
 						.version("0.10.0"), mavenBundle("org.apache.felix",
 						"org.apache.felix.gogo.shell").version("0.10.0"),
 				mavenBundle("org.apache.felix",
 						"org.apache.felix.ipojo.arch.gogo").version("1.0.1"),
 				mavenBundle("org.knowhowlab.osgi.shell", "felix-gogo").version(
 						"1.1.0"));
 
 		return logConfig;
 	}
 
 	protected CompositeOption packAppForTestBundles() {
 
 		CompositeOption testAppBundle = new DefaultCompositeOption(mavenBundle(
 				"fr.imag.adele.apam.tests", "apam-helpers")
 				.versionAsInProject(),
 
 		mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private",
 				"APP1-MainImpl").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.obrman.app1.private",
 						"APP1-MainSpec").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app1.private",
 						"APP1-S1-Spec").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app1.private",
 						"APP1-S2-Spec").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app1.private",
 						"APP1-S3-Spec").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app1.public",
 						"APP1-Spec").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app2.private",
 						"APP2-MainImpl").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app2.private",
 						"APP2-MainSpec").versionAsInProject(), mavenBundle(
 						"fr.imag.adele.apam.tests.obrman.app2.public",
 						"APP2-Spec").versionAsInProject(),
 
 				mavenBundle("fr.imag.adele.apam.tests.services",
 						"apam-pax-samples-iface").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services",
 						"apam-pax-samples-impl-s1").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services",
 						"apam-pax-samples-impl-s2").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services",
 						"apam-pax-samples-impl-s3").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.services",
 						"apam-pax-samples-impl-s6").versionAsInProject(),
 
 				mavenBundle("fr.imag.adele.apam.tests.messages",
 						"apam-pax-samples-msg").versionAsInProject(),
 				mavenBundle("fr.imag.adele.apam.tests.messages",
 						"apam-pax-samples-impl-m1").versionAsInProject());
 
 		return testAppBundle;
 
 	}
 
 	protected CompositeOption packAppForTestBundles(String groupID,
 			String artifactID) {
 
 		CompositeOption testAppBundle = new DefaultCompositeOption(mavenBundle(
 				"fr.imag.adele.apam.tests", "apam-helpers")
 				.versionAsInProject(),
 
 		mavenBundle(groupID, artifactID).versionAsInProject());
 
 		return testAppBundle;
 
 	}
 
 	protected CompositeOption packDebugConfiguration() {
 		CompositeOption debugConfig = new DefaultCompositeOption(
 				when(isDebugModeOn())
 						.useOptions(
 								vmOption(String
 										.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%d",
 												Constants.CONST_DEBUG_PORT)),
 								systemTimeout(3600000)));
 
 		return debugConfig;
 	}
 
 	protected CompositeOption packInitialConfig() {
 		Logger root = (Logger) LoggerFactory
 				.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
 		root.setLevel(Level.WARN);
 
 		String logpath = "file:" + PathUtils.getBaseDir() + "/log/logback.xml";
 		File log = new File(logpath);
 
 		boolean includeLog = log.exists() && log.isFile();
 
		CompositeOption initial = new DefaultCompositeOption(
				vmOption("-XX:+UnlockDiagnosticVMOptions"),
				vmOption("-XX:+UnsyncloadClass"),
				frameworkProperty(
 				"org.osgi.service.http.port").value("8280"), cleanCaches(),
 				systemProperty("logback.configurationFile").value(logpath),
 				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
 						.value("WARN"));
 
 		return initial;
 	}
 
 	protected CompositeOption packLog() {
 		CompositeOption logConfig = new DefaultCompositeOption(mavenBundle(
 				"ch.qos.logback", "logback-core").versionAsInProject(),
 				mavenBundle("ch.qos.logback", "logback-classic")
 						.versionAsInProject(), mavenBundle("org.slf4j",
 						"slf4j-api").versionAsInProject(), mavenBundle(
 						"org.apache.felix", "org.apache.felix.log").version(
 						"1.0.1"));
 
 		return logConfig;
 	}
 
 	protected CompositeOption packOSGi() {
 		CompositeOption osgiConfig = new DefaultCompositeOption(mavenBundle()
 				.groupId("org.apache.felix")
 				.artifactId("org.apache.felix.ipojo").versionAsInProject(),
 				mavenBundle().groupId("org.ow2.chameleon.testing")
 						.artifactId("osgi-helpers").versionAsInProject(),
 				mavenBundle().groupId("org.osgi")
 						.artifactId("org.osgi.compendium").version("4.2.0"),
 				mavenBundle().groupId("org.apache.felix")
 						.artifactId("org.apache.felix.bundlerepository")
						.version("1.6.6"),
				frameworkProperty("ipojo.processing.synchronous").value("false"),
				frameworkProperty("org.apache.felix.ipojo.extender.ThreadPoolSize").value("5"));
 
 		return osgiConfig;
 
 	}
 
 	protected CompositeOption packPax() {
 		CompositeOption paxConfig = new DefaultCompositeOption(mavenBundle()
 				.groupId("org.ops4j.pax.url").artifactId("pax-url-mvn")
 				.versionAsInProject());
 		return paxConfig;
 	}
 
 	@Before
 	public void setUp() {
 		waitForApam();
 		apam = new ApAMHelper(context);
 
 		broker = CST.componentBroker;
 		logger.info("***[Run Test : " + name.getMethodName() + "]***");
 		if (startObrMan) {
 			waitForInstByName(null, "OBRMAN-Instance");
 			// apam.waitForIt(1000);
 		}
 	}
 
 	@After
 	public void tearDown() {
 		apam.dispose();
 	}
 
 }
