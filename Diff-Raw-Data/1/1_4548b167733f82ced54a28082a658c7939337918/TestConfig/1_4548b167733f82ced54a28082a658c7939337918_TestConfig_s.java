 package grisu.frontend.tests.utils;
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginException;
 import grisu.frontend.control.login.LoginManager;
 import grisu.settings.Environment;
import grisu.tests.util.Input;
 import grith.jgrith.credential.Credential;
 import grith.jgrith.credential.CredentialLoader;
 import groovy.util.ConfigObject;
 import groovy.util.ConfigSlurper;
 
 import java.io.File;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TestConfig {
 
 	public static final Logger myLogger = LoggerFactory
 			.getLogger(TestConfig.class);
 
 	public static TestConfig create() throws Exception {
 
 		File testConfigFile = new File(Environment.getGrisuDirectory(), "integrationtest.groovy");
 
 		if (!testConfigFile.exists()) {
 			return new TestConfig();
 		}
 
 		myLogger.debug("Parsing test config file: " + testConfigFile);
 
 		ConfigObject credConfig = new ConfigSlurper().parse(testConfigFile
 				.toURL());
 
 		TestConfig config = (TestConfig) credConfig.getProperty("config");
 		return config;
 
 	}
 
 	private ServiceInterface si;
 	private String fqan = "/test/nesi";
 	private String content = "HELLO WORLD";
 	private String inputFileName = "inputFile.txt";
 
 	private String inputFileName2 = "inputFile2.txt";
 
 	private String gsiftpRemoteInputParent = "gsiftp://globus.test.nesi.org.nz/home/test1/";
 	private String backend = "testbed2";
 
 	private String credentialConfigFile = Input.getFile("cred.groovy");
 
 	private String jobname = "testjob";
 
 	private final String pythonFileName = "pytest.py";
 
 	private Credential cred = null;
 
 	public TestConfig() {
 	}
 
 	public String getBackend() {
 		return backend;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public Credential getCredential() {
 		if (cred == null) {
 
 			try {
 				cred = (Credential) CredentialLoader
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
 		return gsiftpRemoteInputParent+"/"+inputFileName;
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
 
 	public String getPythonScript() {
 		return Input.getFile(pythonFileName);
 	}
 
 	public String getPythonScriptName() {
 		return pythonFileName;
 	}
 
 	public synchronized ServiceInterface getServiceInterface() {
 		if ( si == null ) {
 			try {
 				si = LoginManager.login(getCredential(), backend, false);
 			} catch (LoginException e) {
 				throw new RuntimeException("Can't login: "
 						+ e.getLocalizedMessage(), e);
 			}
 		}
 		return si;
 	}
 
 	public void setBackend(String backend) {
 		this.backend = backend;
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
 
 }
