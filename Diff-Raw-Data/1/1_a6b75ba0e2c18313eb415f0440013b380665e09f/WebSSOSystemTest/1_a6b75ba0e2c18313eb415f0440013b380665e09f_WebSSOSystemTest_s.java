 package org.cagrid.gaards.websso.test.system;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.gaards.authentication.BasicAuthentication;
 import org.cagrid.gaards.authentication.common.AuthenticationProfile;
 import org.cagrid.gaards.authentication.faults.InvalidCredentialFault;
 import org.cagrid.gaards.authentication.test.system.steps.AuthenticationStep;
 import org.cagrid.gaards.authentication.test.system.steps.InvalidAuthentication;
 import org.cagrid.gaards.authentication.test.system.steps.SuccessfullAuthentication;
 import org.cagrid.gaards.authentication.test.system.steps.ValidateSupportedAuthenticationProfilesStep;
 import org.cagrid.gaards.dorian.federation.AutoApprovalPolicy;
 import org.cagrid.gaards.dorian.federation.GridUserStatus;
 import org.cagrid.gaards.dorian.federation.TrustedIdPStatus;
 import org.cagrid.gaards.dorian.idp.Application;
 import org.cagrid.gaards.dorian.idp.CountryCode;
 import org.cagrid.gaards.dorian.idp.LocalUserRole;
 import org.cagrid.gaards.dorian.idp.LocalUserStatus;
 import org.cagrid.gaards.dorian.idp.StateCode;
 import org.cagrid.gaards.dorian.test.system.steps.CleanupDorianStep;
 import org.cagrid.gaards.dorian.test.system.steps.ConfigureGlobusToTrustDorianStep;
 import org.cagrid.gaards.dorian.test.system.steps.CopyConfigurationStep;
 import org.cagrid.gaards.dorian.test.system.steps.FindGridUserStep;
 import org.cagrid.gaards.dorian.test.system.steps.FindLocalUserStep;
 import org.cagrid.gaards.dorian.test.system.steps.GetAsserionSigningCertificateStep;
 import org.cagrid.gaards.dorian.test.system.steps.GridCredentialRequestStep;
 import org.cagrid.gaards.dorian.test.system.steps.RegisterUserWithDorianIdentityProviderStep;
 import org.cagrid.gaards.dorian.test.system.steps.SuccessfullGridCredentialRequest;
 import org.cagrid.gaards.dorian.test.system.steps.UpdateLocalUserStatusStep;
 import org.cagrid.gaards.dorian.test.system.steps.VerifyTrustedIdPStep;
 import org.cagrid.gaards.websso.test.system.steps.AssertWebSSOApplicationStep;
 import org.cagrid.gaards.websso.test.system.steps.ChangeAcegiCASClientPropertiesStep;
 import org.cagrid.gaards.websso.test.system.steps.ChangeCASPropertiesStep;
 import org.cagrid.gaards.websso.test.system.steps.ChangeCatalinaPropertiesStep;
 import org.cagrid.gaards.websso.test.system.steps.ChangeJasigCASClientPropertiesStep;
 import org.cagrid.gaards.websso.test.system.steps.ChangeTomcatServerConfigurationStep;
 import org.cagrid.gaards.websso.test.system.steps.ChangeWebSSOPropertiesStep;
 import org.cagrid.gaards.websso.test.system.steps.CleanupGlobusCertificatesStep;
 import org.cagrid.gaards.websso.test.system.steps.CopyCAStep;
 import org.cagrid.gaards.websso.test.system.steps.HostCertificatesStep;
 import org.cagrid.gaards.websso.test.system.steps.InstallCertStep;
 import org.cagrid.gaards.websso.test.system.steps.WebSSOClientCertificatesStep;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainer;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerFactory;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerType;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.CopyServiceStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.DeleteServiceStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.DeployServiceStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.DestroyContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.StartContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.StopContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.UnpackContainerStep;
 import gov.nih.nci.cagrid.testing.system.haste.Step;
 import gov.nih.nci.cagrid.testing.system.haste.Story;
 
 public class WebSSOSystemTest extends Story {
 
 	private static Log log = LogFactory.getLog(WebSSOSystemTest.class);
 	private ServiceContainer dorianServiceContainer = null;
 	private ServiceContainer cdsServiceContainer = null;
 	private ServiceContainer webSSOJasigClientServiceContainer = null;
 	private ServiceContainer webSSOAcegiClientServiceContainer = null;
 	private ServiceContainer webSSOServiceContainer = null;
 	private File dorianProperties = new File("resources/dorian.properties");
 	private ConfigureGlobusToTrustDorianStep trust;
 	private File tempDorianService;
 	private File tempWebSSOService;
 	private File tempcdsService;
 	private File tempwebssoJasigClientService;
 	private File tempwebssoAcegiClientService;
 	private File configuration;
 	private String systemName="localhost";
 	private String webSSOClientAcegiURL = null;
 	private String webSSOClientJasigURL = null;
 	private int httpsAcegiPortNumber = 38443;
 	private int httpsJasigPortNumber = 28443;
 	private int httpsWebSSOPortNumber = 18443;
 	private static String projectVersion=null;
 	
 	@Override
 	public String getDescription() {
 		return "WebSSO System Test";
 	}
 
    @Override
     public String getName() {
         return getDescription();
     }
    
    private static void loadProjectVersion() {
 		File asLocation = new File("project.properties");
 		Resource resource = new FileSystemResource(asLocation);
 		Properties properties = new Properties();
 		try {
 			properties.load(resource.getInputStream());
 		} catch (IOException ex) {
 			String message = "Error loading project.properties to get project version"+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		projectVersion = (String) properties.get("project.version");
 	}
 
 	public static String getProjectVersion(){
 		if (projectVersion == null) {
 			loadProjectVersion();
 		}
 		return projectVersion;
 	}
 	
 	@Override
 	protected boolean storySetUp() {
 		//api call to load project version
 		WebSSOSystemTest.loadProjectVersion();
 		// set up the dorian service container
 		try {
 			log.debug("Creating container for dorian service");
 			dorianServiceContainer = ServiceContainerFactory
 					.createContainer(ServiceContainerType.SECURE_TOMCAT_CONTAINER);
 			new UnpackContainerStep(dorianServiceContainer).runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating container for dorian service: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 
 		// set up a websso server
 		try {
 			log.debug("Creating container for WebSSO service");
 			webSSOServiceContainer = ServiceContainerFactory
 					.createContainer(ServiceContainerType.TOMCAT_CONTAINER);
 			new UnpackContainerStep(webSSOServiceContainer).runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating container for websso server: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		// set up a websso jasig server
 		try {
 			log.debug("Creating container for WebSSO Client Jasig service");
 			webSSOJasigClientServiceContainer = ServiceContainerFactory
 					.createContainer(ServiceContainerType.TOMCAT_CONTAINER);
 			new UnpackContainerStep(webSSOJasigClientServiceContainer).runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating container for websso client jasig server: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		
 		// set up a websso acegi client server
 		try {
 			log.debug("Creating container for WebSSO Client acegi service");
 			webSSOAcegiClientServiceContainer = ServiceContainerFactory
 					.createContainer(ServiceContainerType.TOMCAT_CONTAINER);
 			new UnpackContainerStep(webSSOAcegiClientServiceContainer).runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating container for websso client acegi server: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 
 		//set up cds server service container
 		try {
 			log.debug("Creating container for cds service");
 			cdsServiceContainer = ServiceContainerFactory
 					.createContainer(ServiceContainerType.SECURE_TOMCAT_CONTAINER);
 			new UnpackContainerStep(cdsServiceContainer).runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating container for dorian service: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		try {
 			this.tempDorianService = new File("tmp/dorian");
 			File asLocation = new File("../../../caGrid/projects/dorian");
 			CopyServiceStep copyService = new CopyServiceStep(asLocation,
 					tempDorianService);
 			copyService.runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating temp service for dorian: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		try {
 			this.tempWebSSOService= new File("tmp/websso");
 			File asLocation = new File("../../../caGrid/projects/websso");
 			CopyServiceStep copyService = new CopyServiceStep(asLocation,
 					tempWebSSOService);
 			copyService.runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating temp service for websso: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		
 		try {
 			this.tempcdsService= new File("tmp/cds");
 			File asLocation = new File("../../../caGrid/projects/cds");
 			CopyServiceStep copyService = new CopyServiceStep(asLocation,
 					tempcdsService);
 			copyService.runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating temp service for cds: "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		try {
 			this.tempwebssoJasigClientService= new File("tmp/websso-client-example");
 			File asLocation = new File("../../../examples/projects/websso-client-example");
 			CopyServiceStep copyService = new CopyServiceStep(asLocation,
 					tempwebssoJasigClientService);
 			copyService.runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating temp service for websso-client : "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		try {
 			this.tempwebssoAcegiClientService= new File("tmp/websso-client-example");
 			File asLocation = new File("../../../examples/projects/websso-client-example");
 			CopyServiceStep copyService = new CopyServiceStep(asLocation,
 					tempwebssoAcegiClientService);
 			copyService.runStep();
 		} catch (Throwable ex) {
 			String message = "Error creating temp service for websso-client : "
 					+ ex.getMessage();
 			log.error(message, ex);
 			fail(message);
 		}
 		return true;
 	}
 
 	@Override
 	protected Vector<Step> steps() {
 		Vector<Step> steps = new Vector<Step>();
 		List<Application> users = configureDorianSteps(steps);
 		configureCDSSteps(steps);
 		configureWebSSOServerSteps(steps, users);
 		configureWebSSOJasigClientServerSteps(steps,users);
 		configureWebSSOAcegiClientServerSteps(steps,users);
 		steps.add(new AssertWebSSOApplicationStep(webSSOClientJasigURL,
 				webSSOClientAcegiURL + "/protected", httpsWebSSOPortNumber,
 				httpsJasigPortNumber, httpsAcegiPortNumber));
 
 		return steps;
 	}
 	
 	private void configureWebSSOAcegiClientServerSteps(Vector<Step> steps,List<Application> users){
 		try {
 			String webssoClientHostname="webssoclient2";			
 			File webssoClientServiceDir=webSSOAcegiClientServiceContainer.getProperties().getContainerDirectory();
 			configureWebSSOTomcatContainer(steps, users,webSSOAcegiClientServiceContainer,webssoClientHostname,httpsAcegiPortNumber,webssoClientServiceDir);
 			
 			String globusDirPath=System.getProperty("user.home")+File.separator+".globus"+File.separator+"certificates";
 			File globusDir = new File(globusDirPath);
 
 			steps.add(new CopyCAStep(dorianServiceContainer, globusDir));
 
 			List<String> antTargets=new ArrayList<String>();
 			antTargets.add("deploySystemTestingTomcatAcegiClient");
 			
 			Integer httpPort=webSSOAcegiClientServiceContainer.getProperties().getPortPreference().getPort();
 			String webSSOServerURL="https://localhost:18443/webssoserver";
 			webSSOClientAcegiURL="http://localhost:"+httpPort+"/webssoclientacegiexample-"+WebSSOSystemTest.getProjectVersion();
 			
 			String	tomcatCertsDir =webSSOAcegiClientServiceContainer.getProperties().getContainerDirectory()+ File.separator + "certificates";
 			String hostKey = new File(tomcatCertsDir, webssoClientHostname + "-key.pem").getAbsolutePath();
 			String hostCert = new File(tomcatCertsDir, webssoClientHostname + "-cert.pem").getAbsolutePath();
 						
 			steps.add(new ChangeAcegiCASClientPropertiesStep(
 					tempwebssoAcegiClientService, webSSOServerURL,webSSOClientAcegiURL,hostCert,hostKey,httpPort,httpsAcegiPortNumber));			
 			
 			String cacertsFilePath = tempwebssoAcegiClientService
 					.getAbsolutePath()
 					+ File.separator
 					+ "ext"
 					+ File.separator
 					+ "dependencies-cert"
 					+ File.separator +"cert"+File.separator+"cacerts-"+WebSSOSystemTest.getProjectVersion()+".cert";
 			steps.add(new InstallCertStep(new File(cacertsFilePath),systemName, httpsWebSSOPortNumber, 1));
 			
 			steps.add(new WebSSOClientCertificatesStep(tempwebssoAcegiClientService,hostCert,hostKey));
 			steps.add(new DeployServiceStep(webSSOAcegiClientServiceContainer,
 					this.tempwebssoAcegiClientService.getAbsolutePath(),antTargets));
 			steps.add(new ChangeCatalinaPropertiesStep(
 					webSSOAcegiClientServiceContainer.getProperties(),"cacerts.cert"));
 
 			steps.add(new StartContainerStep(webSSOAcegiClientServiceContainer));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void configureWebSSOJasigClientServerSteps(Vector<Step> steps,List<Application> users){
 		try {
 			String webssoClientHostname="webssoclient1";
 			File webssoClientServiceDir=webSSOJasigClientServiceContainer.getProperties().getContainerDirectory();			
 			configureWebSSOTomcatContainer(steps, users,webSSOJasigClientServiceContainer,webssoClientHostname,httpsJasigPortNumber,webssoClientServiceDir);
 			
 			String globusDirPath=System.getProperty("user.home")+File.separator+".globus"+File.separator+"certificates";
 			File globusDir = new File(globusDirPath);
 
 			steps.add(new CopyCAStep(dorianServiceContainer, globusDir));
 			
 			List<String> antTargets=new ArrayList<String>();
 			antTargets.add("deploySystemTestingTomcatJasigClient");
 			Integer httpPort=webSSOJasigClientServiceContainer.getProperties().getPortPreference().getPort();
 			String webSSOServerURL="https://localhost:18443/webssoserver";
 			webSSOClientJasigURL="http://localhost:"+httpPort+"/webssoclientjasigexample-"+WebSSOSystemTest.getProjectVersion()+"/protected/";
 			
 			String	tomcatCertsDir =webSSOJasigClientServiceContainer.getProperties().getContainerDirectory()+ File.separator + "certificates";
 			String hostKey = new File(tomcatCertsDir, webssoClientHostname + "-key.pem").getAbsolutePath();
 			String hostCert = new File(tomcatCertsDir, webssoClientHostname + "-cert.pem").getAbsolutePath();
 						
 			steps.add(new ChangeJasigCASClientPropertiesStep(
 					tempwebssoJasigClientService, webSSOServerURL,webSSOClientJasigURL));			
 			
 			steps.add(new WebSSOClientCertificatesStep(tempwebssoJasigClientService,hostCert,hostKey));
 			String cacertsFilePath = tempwebssoJasigClientService
 									.getAbsolutePath()
 									+ File.separator
 									+ "ext"
 									+ File.separator
 									+ "dependencies-cert"
 									+ File.separator+ "cert"+ File.separator + "cacerts-"+WebSSOSystemTest.getProjectVersion()+".cert";
 			steps.add(new InstallCertStep(new File(cacertsFilePath),systemName, httpsWebSSOPortNumber, 1));
 			steps.add(new DeployServiceStep(webSSOJasigClientServiceContainer,
 					this.tempwebssoJasigClientService.getAbsolutePath(),antTargets));
 			steps.add(new ChangeCatalinaPropertiesStep(
 					webSSOJasigClientServiceContainer.getProperties(),"cacerts.cert"));
 			steps.add(new StartContainerStep(webSSOJasigClientServiceContainer));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void configureCDSSteps(Vector<Step> steps) {
 		steps.add(new DeployServiceStep(cdsServiceContainer,
 				this.tempcdsService.getAbsolutePath()));
 	    
 		File outPutDir = new File(cdsServiceContainer.getProperties()
 					.getContainerDirectory().getAbsolutePath()
 					+ File.separator
 					+ "certificates"
 					+ File.separator
 					+ "ca");
 
 		steps.add(new CopyCAStep(dorianServiceContainer, outPutDir));
 		steps.add(new StartContainerStep(cdsServiceContainer));
 	}
 
 	private void configureWebSSOServerSteps(Vector<Step> steps,
 			List<Application> users) {
 		try {
 			String webssoServerHostname="webssoserver";
 			File webssoServiceDir=webSSOServiceContainer.getProperties().getContainerDirectory();
 			configureWebSSOTomcatContainer(steps, users,webSSOServiceContainer,webssoServerHostname,httpsWebSSOPortNumber,webssoServiceDir);
 			configureWebSSO(steps,webssoServerHostname);
 			
 			steps.add(new DeployServiceStep(webSSOServiceContainer,
 					this.tempWebSSOService.getAbsolutePath()));
 			
 			String globusDirPath=System.getProperty("user.home")+File.separator+".globus"+File.separator+"certificates";
 			File globusDir = new File(globusDirPath);
 
 			steps.add(new CopyCAStep(dorianServiceContainer, globusDir));
 			steps.add(new StartContainerStep(webSSOServiceContainer));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void configureWebSSO(Vector<Step> steps,String hostName) {
 		
 		String tomcatCertsDir = webSSOServiceContainer.getProperties().getContainerDirectory().getAbsolutePath()+File.separator+"certificates";
 		String keyPath = tomcatCertsDir + File.separator + hostName+ "-key.pem";
 		String certPath =tomcatCertsDir + File.separator + hostName+ "-cert.pem";
 		String dorianServiceURL=null;
 		String webSSOServiceURL=null;
 		String cdsServiceURL=null;
 		try {
 			dorianServiceURL = dorianServiceContainer.getContainerBaseURI().toString()+ "cagrid/Dorian";
 			webSSOServiceURL = "https://localhost:18443/"+hostName;
 			cdsServiceURL = cdsServiceContainer.getContainerBaseURI().toString()+ "cagrid/CredentialDelegationService";
 			
 		} catch (MalformedURIException e) {
 			throw new RuntimeException("error retrieving dorian service URL or webSSOServiceURL",e);
 		}
 		
 		String delegatedApplicationHostIdentity = "/C=US/O=abc/OU=xyz/OU=caGrid/OU=Services/CN=webssoclient";
 		String dorianHostIdentity="/O=osu/CN=host/localhost";
 		String cdsHostIdentity="/O=osu/CN=host/localhost";
 		steps.add(new ChangeWebSSOPropertiesStep(tempWebSSOService, certPath,
 				keyPath, dorianServiceURL, cdsServiceURL, delegatedApplicationHostIdentity,cdsHostIdentity,dorianHostIdentity));
 
 		steps.add(new ChangeCASPropertiesStep(tempWebSSOService, webSSOServiceURL));
 	}
 
 	private void configureWebSSOTomcatContainer(Vector<Step> steps,
 		List<Application> users,ServiceContainer tomcatContainer,String hostname,int httpsPortNumber,File serviceDir) throws MalformedURIException {
 		
 		// authenticate with identity provider
 		String serviceURL = dorianServiceContainer.getContainerBaseURI().toString()+ "cagrid/Dorian";
 		Application user = users.get(0);
 		BasicAuthentication userCredential = new BasicAuthentication();
 		userCredential.setUserId(user.getUserId());
 		userCredential.setPassword(user.getPassword());
 		BasicAuthentication adminCredential = new BasicAuthentication();
 		adminCredential.setUserId("dorian");
 		adminCredential.setPassword("DorianAdmin$1");
 		
 		File tomcatCertsDir=new File(serviceDir.getAbsoluteFile()+File.separator+"certificates");
 		
 		if(!tomcatCertsDir.exists()){
 			tomcatCertsDir.mkdir();
 		}
 		steps.add(new HostCertificatesStep(tomcatCertsDir, serviceURL,
 				userCredential, adminCredential, hostname));
 	   steps.add(new ChangeTomcatServerConfigurationStep(tomcatContainer.getProperties(), hostname,httpsPortNumber));
 	}
 
 	private List<Application> configureDorianSteps(Vector<Step> steps) {
 		// Create Users
 		List<Application> users = new ArrayList<Application>();
 
 		try {
 			steps.add(new CopyConfigurationStep(tempDorianService,
 					this.configuration, this.dorianProperties));
 
 			steps.add(new DeployServiceStep(dorianServiceContainer,
 					this.tempDorianService.getAbsolutePath()));
 
 			trust = new ConfigureGlobusToTrustDorianStep(dorianServiceContainer);
 
 			steps.add(trust);
 
 			steps.add(new StartContainerStep(dorianServiceContainer));
 
 			GetAsserionSigningCertificateStep signingCertStep = new GetAsserionSigningCertificateStep(
 					dorianServiceContainer);
 
 			steps.add(signingCertStep);
 
 			String serviceURL = dorianServiceContainer.getContainerBaseURI()
 					.toString()
 					+ "cagrid/Dorian";
 
 			// Test Get supported authentication types
 			Set<QName> expectedProfiles = new HashSet<QName>();
 			expectedProfiles.add(AuthenticationProfile.BASIC_AUTHENTICATION);
 			steps.add(new ValidateSupportedAuthenticationProfilesStep(
 					serviceURL, expectedProfiles));
 
 			GridCredentialRequestStep admin = getGridAdminCredential(steps,
 					signingCertStep, serviceURL);
 			
 			steps.add(admin);
 
 			// Test that the Dorian Idp is properly registered.
 			VerifyTrustedIdPStep idp = new VerifyTrustedIdPStep(serviceURL,
 					admin, "Dorian");
 			idp.setDisplayName("Dorian");
 			idp.setStatus(TrustedIdPStatus.Active);
 			idp.setUserPolicyClass(AutoApprovalPolicy.class.getName());
 			idp.setAuthenticationServiceURL(serviceURL);
 			steps.add(idp);
 
 			// Create Users
 			for (int i = 0; i < 3; i++) {
 				Application a = new Application();
 				a.setUserId("jdoe" + i);
 				a.setPassword("K00lM0N$$" + i);
 				a.setFirstName("John" + i);
 				a.setLastName("Doe" + i);
 				a.setEmail(a.getUserId() + "@cagrid.org");
 				a.setOrganization("cagrid.org");
 				a.setAddress("123" + i + " Grid Way");
 				a.setCity("Columbus");
 				a.setState(StateCode.OH);
 				a.setCountry(CountryCode.US);
 				a.setZipcode("43210");
 				a.setPhoneNumber("(555) 555-555" + i);
 				users.add(a);
 				steps.add(new RegisterUserWithDorianIdentityProviderStep(
 						serviceURL, a));
 			}
 
 			// Test that the user accounts were create correctly
 			for (int i = 0; i < 3; i++) {
 				steps.add(new FindLocalUserStep(serviceURL, admin,
 						users.get(i), LocalUserStatus.Pending,
 						LocalUserRole.Non_Administrator));
 			}
 
 			// Test that the users cannot authenticate until they are approved.
 			for (int i = 0; i < users.size(); i++) {
 				BasicAuthentication auth = new BasicAuthentication();
 				auth.setUserId(users.get(i).getUserId());
 				auth.setPassword(users.get(i).getPassword());
 				steps
 						.add(new AuthenticationStep(
 								serviceURL,
 								new InvalidAuthentication(
 										"The application for this account has not yet been reviewed.",
 										InvalidCredentialFault.class), auth));
 			}
 
 			// Approve the user accounts
 			for (int i = 0; i < users.size(); i++) {
 				steps.add(new UpdateLocalUserStatusStep(serviceURL, admin,
 						users.get(i).getUserId(), LocalUserStatus.Active));
 			}
 
 			// Test that the user accounts were approved correctly
 			for (int i = 0; i < users.size(); i++) {
 				steps.add(new FindLocalUserStep(serviceURL, admin,
 						users.get(i), LocalUserStatus.Active,
 						LocalUserRole.Non_Administrator));
 			}
 
 			// Test successful Authentication
 			List<GridCredentialRequestStep> userCredentials = new ArrayList<GridCredentialRequestStep>();
 
 			for (int i = 0; i < users.size(); i++) {
 				SuccessfullAuthentication sa = new SuccessfullAuthentication(
 						users.get(i).getUserId(), users.get(i).getFirstName(),
 						users.get(i).getLastName(), users.get(i).getEmail(),
 						signingCertStep);
 
 				// Test Successful authentication
 				BasicAuthentication ba = new BasicAuthentication();
 				ba.setUserId(users.get(i).getUserId());
 				ba.setPassword(users.get(i).getPassword());
 				AuthenticationStep userAuth = new AuthenticationStep(
 						serviceURL, sa, ba);
 				steps.add(userAuth);
 				GridCredentialRequestStep proxy = new GridCredentialRequestStep(
 						serviceURL, userAuth,
 						new SuccessfullGridCredentialRequest());
 				steps.add(proxy);
 				userCredentials.add(proxy);
 
 				FindGridUserStep gridUser = new FindGridUserStep(serviceURL,
 						admin, proxy);
 				gridUser.setExpectedEmail(users.get(i).getEmail());
 				gridUser.setExpectedFirstName(users.get(i).getFirstName());
 				gridUser.setExpectedLastName(users.get(i).getLastName());
 				gridUser.setExpectedLocalUserId(users.get(i).getUserId());
 				gridUser.setExpectedStatus(GridUserStatus.Active);
 				steps.add(gridUser);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	private GridCredentialRequestStep getGridAdminCredential(
 			Vector<Step> steps,
 			GetAsserionSigningCertificateStep signingCertStep, String serviceURL) {
 		SuccessfullAuthentication success = new SuccessfullAuthentication(
 				"dorian", "Mr.", "Administrator", "dorian@dorian.org",
 				signingCertStep);
 
 		// Test Successful authentication
 		BasicAuthentication cred = new BasicAuthentication();
 		cred.setUserId("dorian");
 		cred.setPassword("DorianAdmin$1");
 		AuthenticationStep adminAuth = new AuthenticationStep(serviceURL,
 				success, cred);
 		steps.add(adminAuth);
 
 		// Get Admin's Grid Credentials
 		GridCredentialRequestStep admin = new GridCredentialRequestStep(
 				serviceURL, adminAuth,
 				new SuccessfullGridCredentialRequest());
 		return admin;
 	}
 
 	public void storyTearDown() throws Throwable {
 		new CleanupDorianStep(dorianServiceContainer,trust).runStep();
 		tearDownServer(dorianServiceContainer,tempDorianService);
 		tearDownServer(cdsServiceContainer,tempcdsService);
 		new CleanupGlobusCertificatesStep().runStep();		
 		tearDownServer(webSSOJasigClientServiceContainer,tempwebssoJasigClientService);
 		tearDownServer(webSSOAcegiClientServiceContainer,tempwebssoAcegiClientService);
 		webSSOServiceContainer.getProperties().setMaxShutdownWaitTime(200);
 		tearDownServer(webSSOServiceContainer,tempWebSSOService);
 		
 	}
 
 	private void tearDownServer(ServiceContainer serviceContainer,File service) throws Throwable {
 		if (serviceContainer.isStarted()) {
 			new StopContainerStep(serviceContainer).runStep();
 		}
 		new DestroyContainerStep(serviceContainer).runStep();
 		if (service != null) {
 			new DeleteServiceStep(service).runStep();
 		}
 	}
 	
 	public static void main(String[] args) {
 		System.out.println(WebSSOSystemTest.getProjectVersion());
 	}	
 }
