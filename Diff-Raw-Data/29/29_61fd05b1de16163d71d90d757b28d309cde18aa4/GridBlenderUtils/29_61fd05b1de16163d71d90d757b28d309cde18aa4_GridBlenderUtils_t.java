 package org.vpac.grisu.clients.blender;
 
 import java.io.File;
 import java.io.IOException;
 
 import jline.ConsoleReader;
 
 import org.apache.commons.lang.StringUtils;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.GlobusCredentialException;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.login.LoginException;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.security.light.CredentialHelpers;
 import org.vpac.security.light.myProxy.LocalMyProxy;
 import org.vpac.security.light.plainProxy.LocalProxy;
 import org.vpac.security.light.plainProxy.PlainProxy;
 
 public class GridBlenderUtils {
 
 	public static char[] askForPassword(String question) {
 
 		char[] password = null;
 		try {
 			ConsoleReader consoleReader = new ConsoleReader();
 			password = consoleReader.readLine(question, new Character('*'))
 					.toCharArray();
 		} catch (Exception e) {
 			System.err.println("Couldn't read password input: "
 					+ e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		return password;
 
 	}
 
 	public static ServiceInterface login(BlenderCommandLineArgs args) {
 
 		LoginParams loginParams = null;
 		ServiceInterface si = null;
 
 		String username = null;
 		
 		if ( args.isUsername() ) {
 			username = args.getUsername();
 		}
 
 		if (StringUtils.isNotBlank(username)) {
 			// means myproxy or shib login
 			if ( ! args.isIdp() ) {
 				// means myproxy login
 				char[] password = askForPassword("Please enter your myproxy password: ");
 
 				loginParams = new LoginParams(
 				// "http://localhost:8080/xfire-backend/services/grisu",
 						// "https://ngportal.vpac.org/grisu-ws/soap/EnunciateServiceInterfaceService",
 						// "https://ngportal.vpac.org/grisu-ws/services/grisu",
 						// "https://ngportal.vpac.org/grisu-ws/soap/GrisuService",
 						// "http://localhost:8080/enunciate-backend/soap/GrisuService",
 						"Local",
 						// "Dummy",
 						username, password);
 
 				try {
 					si = LoginManager
 							.login(null, null, null, null, loginParams);
 					return si;
				} catch (RuntimeException e) {
 					System.err.println(e.getLocalizedMessage());
 					System.exit(1);
 				} catch (LoginException e) {
 					System.err.println(e.getLocalizedMessage());
 					System.exit(1);
 				}
 				
 				// possibly store local proxy
 				if ( args.isSaveLocalProxy() ) {
 					try {
 						LocalMyProxy.getDelegationAndWriteToDisk(username, password, 3600*24);
 					} catch (Exception e) {
 						System.err.println("Couldn't write myproxy credential to disk.");
 					}
 				}
 				
 			} else {
 				// means shib login
 				loginParams = new LoginParams(
 				// "http://localhost:8080/xfire-backend/services/grisu",
 						// "https://ngportal.vpac.org/grisu-ws/soap/EnunciateServiceInterfaceService",
 						// "https://ngportal.vpac.org/grisu-ws/services/grisu",
 						// "https://ngportal.vpac.org/grisu-ws/soap/GrisuService",
 						// "http://localhost:8080/enunciate-backend/soap/GrisuService",
 						"Local",
 						// "Dummy",
 						null, null);
 
 				char[] password = askForPassword("Please enter your idp password: ");
 
 				try {
 					si = LoginManager.login(null, password, args.getUsername(),
 							args.getIdp(), loginParams);
 					
 					if ( args.isSaveLocalProxy() ) {
 						System.err.println("Saving shib proxy credentials to disk not supported yet.");
 					}
 					
 					return si;
				} catch (RuntimeException e) {
 					System.err.println(e.getLocalizedMessage());
 					System.exit(1);
 				} catch (LoginException e) {
 					System.err.println(e.getLocalizedMessage());
 					System.exit(1);
 				}
 
 			}
 		} else {
 			// means certlogin
 
 			loginParams = new LoginParams(
 			// "http://localhost:8080/xfire-backend/services/grisu",
 					// "https://ngportal.vpac.org/grisu-ws/soap/EnunciateServiceInterfaceService",
 					// "https://ngportal.vpac.org/grisu-ws/services/grisu",
 					// "https://ngportal.vpac.org/grisu-ws/soap/GrisuService",
 					// "http://localhost:8080/enunciate-backend/soap/GrisuService",
 					"Local",
 					// "Dummy",
 					null, null);
 
 			GlobusCredential cred = null;
 			if (LocalProxy.validGridProxyExists()) {
 				try {
 					cred = LocalProxy.loadGlobusCredential();
 				} catch (GlobusCredentialException e) {
 					System.err.println(e.getLocalizedMessage());
 					System.exit(1);
 				}
 			} else {
 				char[] password = askForPassword("Please enter you private key passphrase: ");
 
 				try {
 					cred = CredentialHelpers.unwrapGlobusCredential(PlainProxy
 							.init(password, 24));
 				} catch (Exception e) {
 					System.err.println(e.getLocalizedMessage());
 					System.exit(1);
 				}
 				
 				if ( args.isSaveLocalProxy() ) {
 					try {
 						CredentialHelpers.writeToDisk(cred, new File(LocalProxy.PROXY_FILE));
 					} catch (IOException e) {
 						System.err.println("Could not write proxy credential to disk: "+e.getLocalizedMessage());
 					}
 				}
 
 			}
 			try {
 				// means using existing proxy
 				si = LoginManager.login(cred, null, null, null, loginParams);
 				return si;
			} catch (RuntimeException e) {
 				System.err.println(e.getLocalizedMessage());
 				System.exit(1);
 			} catch (LoginException e) {
 				System.err.println(e.getLocalizedMessage());
 				System.exit(1);
 			}
 			
 		}
 		throw new RuntimeException("Could not login for some unknown reason. This is most probably the result of markus.binsteiner@arcs.org.au being stupid. Please contact him.");
 	}
 
 }
