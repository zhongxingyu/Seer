 package grisu.frontend.tests.utils;
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginException;
 import grisu.frontend.control.login.LoginManager;
 import grisu.jcommons.constants.GridEnvironment;
 import grisu.settings.Environment;
 import grith.jgrith.cred.AbstractCred;
 import grith.jgrith.credential.CredentialLoader;
 import groovy.util.ConfigObject;
 import groovy.util.ConfigSlurper;
 
 import java.io.File;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TestConfig {
 
 	public static final Logger myLogger = LoggerFactory
 			.getLogger(TestConfig.class);
 
 	private static TestConfig testconfig = null;
 
 	private static TestConfig create() throws Exception {
 
 		File testConfigFile = new File(Environment.getGrisuDirectory(), "integrationtest.groovy");
 
 		if (!testConfigFile.exists()) {
 			return new TestConfig();
 		}
 
 		myLogger.debug("Parsing test config file: " + testConfigFile);
 
 		ConfigObject credConfig = new ConfigSlurper().parse(testConfigFile
 				.toURL());
 
 		TestConfig config = (TestConfig) credConfig.getProperty("config");
 
 		String myproxy = config.getMyProxyServer();
 		if (StringUtils.isNotBlank(myproxy)) {
 			GridEnvironment.MYPROXY_SERVER = myproxy;
 		}
 
 		return config;
 
 	}
 
 	public static synchronized TestConfig getTestConfig() {
 		if (testconfig == null) {
 			try {
 				testconfig = create();
 			} catch (Exception e) {
 				throw new RuntimeException("Can't create testconfig: "
 						+ e.getLocalizedMessage(), e);
 			}
 		}
 		return testconfig;
 	}
 
 	private ServiceInterface si;
 	private String fqan = "/test/nesi";
 	private String content = "HELLO WORLD";
 	private String inputFileName = "inputFile.txt";
 	private String inputFileName2 = "inputFile2.txt";
 	private String gsiftpRemoteInputParent = "gsiftp://globus.test.nesi.org.nz/home/test1/";
 	private Set<String> backends = Sets.newHashSet("local", "testbed");
 	private String credentialConfigFile = Input.getFile("cred.groovy");
 	private String jobname = "testjob";
 	private String subLoc10minMax = "test1:globus.test.nesi.org.nz";
 	private final String pythonFileName = "pytest.py";
 	private final String killmeScriptName = "kill_me.sh";
 	private final String killJobManagerScriptName = "kill_job_managers.sh";
 	private String myproxyServer;
 
 
 	private AbstractCred cred = null;
 
 	private Map<String, ServiceInterface> backendCache = null;
 
 	public TestConfig() {
 	}
 
 	public Set<String> getBackends() {
 		return backends;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public AbstractCred getCredential() {
 		if (cred == null) {
 
 			try {
 				cred = CredentialLoader
 						.loadCredentials(credentialConfigFile)
 						.values().iterator().next();
 			} catch (Exception e) {
 				throw new RuntimeException(
 						"Can't create credential to run tests: "
 								+ e.getLocalizedMessage(), e);
 			}
 
 		}
 		return cred;
 	}
 
 	public String getCredentialConfigFile() {
 		return credentialConfigFile;
 	}
 
 	public String getFqan() {
 		return fqan;
 	}
 
 	public String getGsiftpRemoteInputFile() {
 		return gsiftpRemoteInputParent + "/" + inputFileName;
 	}
 
 	public String getGsiftpRemoteInputParent() {
 		return gsiftpRemoteInputParent;
 	}
 
 	public String getInputFile() {
 		return Input.getFile(inputFileName);
 	}
 
 	public String getInputFile2() {
 		return Input.getFile(inputFileName2);
 	}
 
 	public String getInputFileName() {
 		return inputFileName;
 	}
 
 	public String getInputFileName2() {
 		return inputFileName2;
 	}
 
 	public String getJobname() {
 		return jobname;
 	}
 
 	public String getKillJobManagersScript() {
 		return Input.getFile(this.killJobManagerScriptName);
 	}
 
 	public String getKillJobManagersScriptName() {
 		return this.killJobManagerScriptName;
 	}
 
 	public String getKillmeScript() {
 		return Input.getFile(this.killmeScriptName);
 	}
 
 	public String getKillmeScriptName() {
 		return this.killmeScriptName;
 	}
 
 	public String getMyproxyServer() {
 		return myproxyServer;
 	}
 
 	public String getMyProxyServer() {
 		return this.myproxyServer;
 	}
 
 	public String getPythonScript() {
 		return Input.getFile(pythonFileName);
 	}
 
 	public String getPythonScriptName() {
 		return pythonFileName;
 	}
 
 	public synchronized Map<String, ServiceInterface> getServiceInterfaces() {
 
 		if (backendCache == null) {
 			backendCache = Maps.newTreeMap();
 
 			for (String b : backends) {
 
 				ServiceInterface si = null;
 
 				try {
 					si = LoginManager.login(b, getCredential(), false);
 					backendCache.put(b, si);
 				} catch (LoginException e) {
 					throw new RuntimeException("Can't login: "
 							+ e.getLocalizedMessage(), e);
 				}
 			}
 
 		}
 
 		return backendCache;
 	}
 
 	public String getSubLoc10minMax() {
 		return subLoc10minMax;
 	}
 
 	public void setBackend(Set<String> backends) {
 		this.backends = backends;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public void setCredentialConfigFile(String credentialConfiFile) {
 		this.credentialConfigFile = credentialConfiFile;
 	}
 
 	public void setFqan(String fqan) {
 		this.fqan = fqan;
 	}
 
 
 	public void setGsiftpRemoteInputParent(String gsiftpRemoteInputParent) {
 		this.gsiftpRemoteInputParent = gsiftpRemoteInputParent;
 	}
 
 	public void setInputFileName(String inputFileName) {
 		this.inputFileName = inputFileName;
 	}
 
 	public void setInputFileName2(String inputFileName2) {
 		this.inputFileName2 = inputFileName2;
 	}
 
 	public void setJobname(String jobname) {
 		this.jobname = jobname;
 	}
 
 	public void setMyproxyServer(String myproxyServer) {
 		this.myproxyServer = myproxyServer;
 	}
 
 	public void setSubLoc10minMax(String subLoc) {
 		this.subLoc10minMax = subLoc;
 	}
 
 }
