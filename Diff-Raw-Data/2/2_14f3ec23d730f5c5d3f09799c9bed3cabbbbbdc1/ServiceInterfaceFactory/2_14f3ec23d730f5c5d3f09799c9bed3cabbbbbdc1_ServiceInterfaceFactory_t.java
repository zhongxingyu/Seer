 package grisu.frontend.control.login;
 
 import grisu.control.ServiceInterface;
 import grisu.control.ServiceInterfaceCreator;
 import grisu.control.exceptions.ServiceInterfaceException;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 /**
  * Creates {@link ServiceInterface}s using the url of a ServiceInterface.
  * 
  * At the moment it supports: Local, xfire & cxf serviceInterfaces.
  * 
  * The serviceInterface you want to create needs to be in the classpath.
  * 
  * @author Markus Binsteiner
  * 
  */
 public final class ServiceInterfaceFactory {
 
 	static final Logger myLogger = Logger
 			.getLogger(ServiceInterfaceFactory.class.getName());
 
 	public static final String DEFAULT_SERVICE_INTERFACE = "ARCS";
 
 	public static final String[] KNOWN_SERVICE_INTERFACE_CREATORS = new String[] {
 		"LocalServiceInterfaceCreator",
 		// "DummyServiceInterfaceCreator",
 		"JaxWsServiceInterfaceCreator",
 		// "XfireServiceInterfaceCreator",
 		// the old xfire client...
 		// "XFireServiceInterfaceCreator"
 	};
 
 	/**
 	 * Creates a serviceInterface from a {@link LoginParams} object.
 	 * 
 	 * @param params
 	 *            the login parameters
 	 * @return the serviceInterface
 	 * @throws ServiceInterfaceException
 	 *             if the serviceInterface couldn't be created
 	 */
 	public static ServiceInterface createInterface(final LoginParams params)
 			throws ServiceInterfaceException {
 		return createInterface(params.getServiceInterfaceUrl(),
 				params.getMyProxyUsername(), params.getMyProxyPassphrase(),
 				params.getMyProxyServer(), params.getMyProxyPort(),
 				params.getHttpProxy(), params.getHttpProxyPort(),
 				params.getHttpProxyUsername(), params.getMyProxyPassphrase());
 	}
 
 	/**
 	 * Creates a ServiceInterface using the url that is provided. Most likely
 	 * you want a ServiceInterface to a webservice. But it's also possible to
 	 * create ServiceInterface that's on your local computer (not at the moment,
 	 * though - if you are interested in this contact me via email:
 	 * markus@vpac.org)
 	 * 
 	 * @param interfaceUrl
 	 *            the url of the ServiceInterface
 	 * @param username
 	 *            the username of the MyProxy credential
 	 * @param password
 	 *            the password of the MyProxy credential
 	 * @param myProxyServer
 	 *            the url of the MyProxy server
 	 * @param myProxyPort
 	 *            the port of the MyProxy server
 	 * @param httpProxy
 	 *            the httproxy hostname to use if you want to connect to a web
 	 *            service ServiceInterface or null if you don't
 	 * @param httpProxyPort
 	 *            the httproxy port to use if you want to connect to a web
 	 *            service ServiceInterface or null if you don't
 	 * @param httpProxyUsername
 	 *            the httpproxy username to use if you need to authenticate
 	 *            yourself to the httpproxy server or null if you don't need to
 	 * @param httpProxyPassword
 	 *            the httpproxy password to use if you need to authenticate
 	 *            yourself to the httpproxy server or null if you don't need to
 	 * @return the ServiceInterface to use to stage files/submit jobs
 	 * @throws ServiceInterfaceException
 	 */
 	public static ServiceInterface createInterface(final String interfaceUrl,
 			final String username, final char[] password,
 			final String myProxyServer, final String myProxyPort,
 			final String httpProxy, final int httpProxyPort,
 			final String httpProxyUsername, final char[] httpProxyPassword)
 					throws ServiceInterfaceException {
 
 		final Object[] otherOptions = new Object[4];
 		otherOptions[0] = httpProxy;
 		otherOptions[1] = httpProxyPort;
 		otherOptions[2] = httpProxyUsername;
 		otherOptions[3] = httpProxyPassword;
 
 		final Map<String, Exception> failedCreators = new HashMap<String, Exception>();
 
 		for (final String className : KNOWN_SERVICE_INTERFACE_CREATORS) {
 
 			Class serviceInterfaceCreatorClass = null;
 
 			try {
 				serviceInterfaceCreatorClass = Class
 						.forName("grisu.frontend.control." + className);
 			} catch (final ClassNotFoundException e) {
 				myLogger.warn("Could not find serviceInterfaceCreator class: "
 						+ className);
 				myLogger.warn("Probably not in classpath. No worries, trying next one...");
 				continue;
 			}
 
 			ServiceInterfaceCreator serviceInterfaceCreator;
 			try {
 				serviceInterfaceCreator = (ServiceInterfaceCreator) serviceInterfaceCreatorClass
 						.newInstance();
 			} catch (final Exception e) {
 				// shouldn't really happen
 				continue;
 			}
 
 			if (!serviceInterfaceCreator.canHandleUrl(interfaceUrl)) {
 				myLogger.debug(className + " doesn't handle url: "
 						+ interfaceUrl
 						+ ". Trying next serviceInterfaceCreator...");
 				continue;
 			}
 
 			ServiceInterface serviceInterface = null;
 			try {
 				serviceInterface = serviceInterfaceCreator.create(interfaceUrl,
 						username, password, myProxyServer, myProxyPort,
 						otherOptions);
 			} catch (final ServiceInterfaceException e) {
 				// e.printStackTrace();
 				myLogger.debug("Couldn't connect to url " + interfaceUrl
 						+ " using serviceInterfaceCreator " + className + ": "
 						+ e.getLocalizedMessage());
 				failedCreators.put(className, e);
 				continue;
 			}
 
 			if (serviceInterface == null) {
 				myLogger.debug("Couldn't connect to url "
 						+ interfaceUrl
 						+ " using serviceInterfaceCreator "
 						+ className
 						+ ": "
 						+ "No serviceInterface created/serviceinterface is null");
 				failedCreators.put(className, null);
 				continue;
 			}
 
 			myLogger.info("Successfully created serviceInterface using creator: "
 					+ className);
 
 			try {
 				serviceInterface.login(username, new String(password));
 				int backend_version = serviceInterface.getInterfaceVersion();
 
 				if (backend_version > LoginManager.REQUIRED_BACKEND_API_VERSION) {
 					throw new LoginException(
 							"Sorry, could not login. Your client version is no longer supported by the server.\n"
 									+ "Please download the latest version from:\nhttp://code.ceres.auckland.ac.nz/stable-downloads\n"
 									+ "If you have the latest version and are still experiencing this problem please contact\n"
 									+ "eresearch-admin@list.auckland.ac.nz\n"
 									+ "with a description of the issue.");
 				} else if (backend_version < LoginManager.REQUIRED_BACKEND_API_VERSION) {
 					throw new LoginException(
 							"Sorry, could not login. Your client version is incompatible with the server.\n"
 									+ "Please download the latest version from:\nhttp://code.ceres.auckland.ac.nz/stable-downloads\n"
 									+ "If you have the latest version and are still experiencing this problem please contact\n"
 									+ "eresearch-admin@list.auckland.ac.nz\n"
 									+ "with a description of the issue.");
 				}
 			} catch (final Exception e) {
 				// e.printStackTrace();
 				myLogger.debug("Couldn't login to grisu service on: "
 						+ interfaceUrl + ": " + e.getLocalizedMessage());
 				failedCreators.put(className, e);
 				continue;
 			}
 
 			myLogger.info("Successfully logged in.");
 			return serviceInterface;
 		}
 
 		final StringBuffer failedOnes = new StringBuffer();
 		for (final String name : failedCreators.keySet()) {
 			failedOnes.append(name + ": "
 					+ failedCreators.get(name).getLocalizedMessage() + "\n");
 		}
 
 		if (failedCreators.size() == 1) {
 			final String key = failedCreators.keySet().iterator().next();
 
 			Throwable rootCause = failedCreators.get(key);
 			while (rootCause.getCause() != null) {
 				rootCause = rootCause.getCause();
 			}
 
 			throw new ServiceInterfaceException(rootCause);
 		} else if (failedCreators.size() == 0) {
 			throw new ServiceInterfaceException(
					"Could not establish a connection with backend \""
 							+ interfaceUrl + "\". Maybe a typo?", null);
 		} else {
 			throw new ServiceInterfaceException(
 					"Could not find a single ServiceInterfaceCreator that worked. Tried these:\n"
 							+ failedOnes.toString(), null);
 		}
 
 	}
 
 	private ServiceInterfaceFactory() {
 	}
 
 }
