 package org.vpac.grisu.frontend.control.login;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.globus.gsi.GlobusCredential;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.grisu.utils.GrisuPluginFilenameFilter;
 import org.vpac.security.light.control.CertificateFiles;
 
 import au.org.arcs.jcommons.dependencies.ClasspathHacker;
 import au.org.arcs.jcommons.dependencies.DependencyManager;
 
 public class LoginManager {
 
 	/**
 	 * One-for-all method to login to a Grisu backend.
 	 * 
 	 * Specify nothing except the loginParams (without the myproxy username &
 	 * password) in order to use a local proxy. If you specify the password in
 	 * addition to that the local x509 cert will be used to create a local proxy
 	 * which in turn will be used to login to the Grisu backend.
 	 * 
 	 * If you specify the myproxy username & password in the login params those
 	 * will be used for a simple myproxy login to the backend.
 	 * 
 	 * In order to use shibboleth login, you need to specify the password, the
 	 * idp-username and the name of the idp.
 	 * 
 	 * @param password
 	 *            the password or null
 	 * @param username
 	 *            the shib-username or null
 	 * @param idp
 	 *            the name of the idp or null
 	 * @param loginParams
 	 *            the login parameters
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 * @throws IOException
 	 *             if necessary plugins couldn't be downloaded/stored in the
 	 *             .grisu/plugins folder
 	 */
 	public static ServiceInterface login(GlobusCredential cred, char[] password, String username,
			String idp, LoginParams loginParams) throws LoginException {
 
 		try {
 			addPluginsToClasspath();
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 			throw new RuntimeException(e2);
 		}
 		
 		try {
 			CertificateFiles.copyCACerts();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		// do the cacert thingy
 		try {
 			URL cacertURL = LoginManager.class.getResource("/ipsca.pem");
 			HttpSecureProtocol protocolSocketFactory = new HttpSecureProtocol();
 
 			TrustMaterial trustMaterial = null;
 			trustMaterial = new TrustMaterial(cacertURL);
 
 			// We can use setTrustMaterial() instead of addTrustMaterial()
 			// if we want to remove
 			// HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
 			protocolSocketFactory.addTrustMaterial(trustMaterial);
 
 			// Maybe we want to turn off CN validation (not recommended!):
 			protocolSocketFactory.setCheckHostname(false);
 
 			Protocol protocol = new Protocol("https",
 					(ProtocolSocketFactory) protocolSocketFactory, 443);
 			Protocol.registerProtocol("https", protocol);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		DependencyManager.checkForDependency(
 				"org.bouncycastle.jce.provider.BouncyCastleProvider",
 				"http://www.bouncycastle.org/download/bcprov-jdk15-143.jar",
 				new File(Environment.getGrisuPluginDirectory(),
 						"bcprov-jdk15-143.jar"));
 
 		String serviceInterfaceUrl = loginParams.getServiceInterfaceUrl();
 
 		if ("Local".equals(serviceInterfaceUrl)
 				|| "Dummy".equals(serviceInterfaceUrl)) {
 			DependencyManager
 					.checkForDependency(
 							"org.vpac.grisu.control.serviceInterfaces.LocalServiceInterface",
 							"https://code.arcs.org.au/hudson/job/Grisu-SNAPSHOT-binaries/lastSuccessfulBuild/artifact/backend/grisu-core/target/local-backend.jar",
 							new File(Environment.getGrisuPluginDirectory(),
 									"local-backend.jar"));
 		} else if (serviceInterfaceUrl.startsWith("http")) {
 
 			// assume xfire -- that needs to get smarter later on
 			DependencyManager
 					.checkForDependency(
 							"org.vpac.grisu.client.control.XFireServiceInterfaceCreator",
 							"https://code.arcs.org.au/hudson/job/Grisu-connectors-SNAPSHOT-binaries/lastSuccessfulBuild/artifact/frontend-modules/xfire-frontend/target/xfire-frontend.jar",
 							new File(Environment.getGrisuPluginDirectory(),
 									"xfire-frontend.jar"));
 
 			// also try to use client side mds
 			DependencyManager
 					.checkForDependency(
 							"org.vpac.grisu.frontend.info.clientsidemds.ClientSideGrisuRegistry",
 							"https://code.arcs.org.au/hudson/job/Grisu-SNAPSHOT-binaries/lastSuccessfulBuild/artifact/frontend/client-side-mds/target/client-side-mds.jar",
 							new File(Environment.getGrisuPluginDirectory(),
 									"client-side-mds.jar"));
 		}
 
 		if (StringUtils.isBlank(username)) {
 
 			if (StringUtils.isBlank(loginParams.getMyProxyUsername())) {
 
 				if ( cred != null ) {
 					try {
 						return LoginHelpers.globusCredentialLogin(loginParams, cred);
 					} catch (Exception e) {
 						throw new LoginException("Could not login: "
 								+ e.getLocalizedMessage(), e);
 					}
 				} else if (password == null || password.length == 0) {
 					// means certificate auth
 					try {
 						// means try to load local proxy
 						if ( loginParams == null ) {
 							return LoginHelpers.defaultLocalProxyLogin();
 						} else {
 							return LoginHelpers.defaultLocalProxyLogin(loginParams);
 						}
 					} catch (Exception e) {
 						throw new LoginException("Could not login: "
 								+ e.getLocalizedMessage(), e);
 					}
 				} else {
 					// means to create local proxy
 					try {
 						return LoginHelpers.localProxyLogin(password,
 								loginParams);
 					} catch (ServiceInterfaceException e) {
 						throw new LoginException("Could not login: "
 								+ e.getLocalizedMessage(), e);
 					}
 				}
 
 			} else {
 				// means myproxy login
 				try {
 					return LoginHelpers.myProxyLogin(loginParams);
 				} catch (ServiceInterfaceException e) {
 					throw new LoginException("Could not login: "
 							+ e.getLocalizedMessage(), e);
 				}
 			}
 		} else {
 			// means shib login
 			throw new RuntimeException("Shib login not supported yet...");
 		}
 
 	}
 
 	public static void addPluginsToClasspath() throws IOException {
 
 		ClasspathHacker.initFolder(new File(Environment
 				.getGrisuPluginDirectory()), new GrisuPluginFilenameFilter());
 
 	}
 
 }
