 package grisu.control;
 
 import grisu.backend.model.User;
 import grisu.control.exceptions.NoSuchTemplateException;
 import grisu.control.serviceInterfaces.AbstractServiceInterface;
 import grisu.control.serviceInterfaces.LocalServiceInterface;
 import grisu.settings.Environment;
 import grisu.settings.ServiceTemplateManagement;
 import grith.jgrith.credential.Credential;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import javax.jws.WebService;
 import javax.ws.rs.Path;
 import javax.xml.ws.soap.MTOM;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * This abstract class implements most of the methods of the
  * {@link ServiceInterface} interface. This way developers don't have to waste
  * time to implement the whole interface just for some things that are site/grid
  * specific. Currently there are two classes that extend this abstract class:
  * {@link LocalServiceInterface} and WsServiceInterface (which can be found in
  * the grisu-ws module).
  * 
  * The {@link LocalServiceInterface} is used to work with a small local database
  * like hsqldb so a user has got the whole grisu framework on his desktop. Of
  * course, all required ports have to be open from the desktop to the grid. On
  * the other hand no web service server is required.
  * 
  * The WsServiceInterface is the main one and it is used to set up a web service
  * somewhere. So light-weight clients can talk to it.
  * 
  * @author Markus Binsteiner
  * 
  */
 
 @Path("/grisu")
 @WebService(endpointInterface = "grisu.control.ServiceInterface")
 @MTOM(enabled = true)
 // @StreamingAttachment(parseEagerly = true, memoryThreshold = 40000L)
 public class EnunciateServiceInterfaceImpl extends AbstractServiceInterface
 implements ServiceInterface {
 
 	static {
 		// System.out.println("INHERITABLETHREAD");
 		SecurityContextHolder
 		.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
 	}
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(EnunciateServiceInterfaceImpl.class.getName());
 
 	private String username;
 	private char[] password;
 
 	private static String hostname = null;
 
 	protected synchronized Credential getCredential() {
 
 		final GrisuUserDetails gud = getSpringUserDetails();
 		if (gud != null) {
 			// myLogger.debug("Found user: " + gud.getUsername());
 			return gud.fetchCredential();
 		} else {
 			myLogger.error("Couldn't find user...");
 			return null;
 		}
 
 	}
 
 	// protected final ProxyCredential getCredential(String fqan,
 	// int lifetimeInSeconds) {
 	//
 	// final String myProxyServer = MyProxyServerParams.getMyProxyServer();
 	// final int myProxyPort = MyProxyServerParams.getMyProxyPort();
 	//
 	// ProxyCredential temp;
 	// try {
 	// myLogger.debug("Getting delegated proxy from MyProxy...");
 	// temp = new ProxyCredential(MyProxy_light.getDelegation(
 	// myProxyServer, myProxyPort, username, password,
 	// lifetimeInSeconds));
 	// myLogger.debug("Finished getting delegated proxy from MyProxy. DN: "
 	// + temp.getDn());
 	//
 	// if (StringUtils.isNotBlank(fqan)) {
 	//
 	// final VO vo = getUser().getFqans().get(fqan);
 	// myLogger.debug(temp.getDn() + ":Creating voms proxy for fqan: "
 	// + fqan);
 	// final ProxyCredential credToUse = CertHelpers
 	// .getVOProxyCredential(vo, fqan, temp);
 	// return credToUse;
 	// } else {
 	// return temp;
 	// }
 	//
 	// } catch (final Exception e) {
 	// throw new RuntimeException(e);
 	// }
 	//
 	// }
 
 	public long getCredentialEndTime() {
 		final GrisuUserDetails gud = getSpringUserDetails();
 		if (gud == null) {
 			return -1L;
 		} else {
 			return getSpringUserDetails().getCredentialEndTime();
 		}
 	}
 
 	@Override
 	public String getInterfaceInfo(String key) {
 		if ("HOSTNAME".equalsIgnoreCase(key)) {
 			if (hostname == null) {
 				try {
 					final InetAddress addr = InetAddress.getLocalHost();
 					final byte[] ipAddr = addr.getAddress();
 					hostname = addr.getHostName();
 					if (StringUtils.isBlank(hostname)) {
 						hostname = "";
 					} else {
 						hostname = hostname + " / ";
 					}
 					hostname = hostname + addr.getHostAddress();
 				} catch (final UnknownHostException e) {
 					hostname = "Unavailable";
 				}
 			}
 			return hostname;
 		} else if ("VERSION".equalsIgnoreCase(key)) {
 			return Integer.toString(ServiceInterface.API_VERSION);
 		} else if ("NAME".equalsIgnoreCase(key)) {
			return "Local serviceinterface";
 		} else if ("BACKEND_VERSION".equalsIgnoreCase(key)) {
 			return BACKEND_VERSION;
 		}
 
 		return null;
 	}
 
 	private GrisuUserDetails getSpringUserDetails() {
 
 		final SecurityContext securityContext = SecurityContextHolder
 				.getContext();
 		final Authentication authentication = securityContext
 				.getAuthentication();
 
 		if (authentication != null) {
 			final Object principal = authentication.getPrincipal();
 			if (principal instanceof GrisuUserDetails) {
 				return (GrisuUserDetails) principal;
 			} else {
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	public String getTemplate(String name) throws NoSuchTemplateException {
 
 		final File file = new File(
 				Environment.getAvailableTemplatesDirectory(), name
 				+ ".template");
 
 		String temp;
 		try {
 			temp = FileUtils.readFileToString(file);
 		} catch (final IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		return temp;
 
 	}
 
 	@Override
 	protected synchronized User getUser() {
 
 		final GrisuUserDetails gud = getSpringUserDetails();
 		if (gud != null) {
 			// myLogger.debug("Found user: "+gud.getUsername());
 			return gud.getUser(this);
 		} else {
 			myLogger.error("Couldn't find user...");
 			throw new RuntimeException("Can't get user for session.");
 		}
 
 	}
 
 	public String[] listHostedApplicationTemplates() {
 		return ServiceTemplateManagement.getAllAvailableApplications();
 	}
 
 	public void login(String username, String password) {
 
 		this.username = username;
 		if (StringUtils.isNotBlank(password)) {
 			this.password = password.toCharArray();
 		}
 
 		getCredential();
 
 		// // load archived jobs in background
 		// new Thread() {
 		// @Override
 		// public void run() {
 		// getArchivedJobs(null);
 		// }
 		// }.start();
 
 	}
 
 	public String logout() {
 
 		myLogger.debug("Logging out user: " + getDN());
 
 		// HttpServletRequest req = HTTPRequestContext.get().getRequest();
 		// req.getSession().setAttribute("credential", null);
 
 		return "Logged out.";
 
 	}
 }
