 package grisu.frontend.control;
 
 import grisu.control.ServiceInterface;
 import grisu.control.ServiceInterfaceCreator;
 import grisu.control.exceptions.ServiceInterfaceException;
 import grisu.settings.Environment;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Service;
 import javax.xml.ws.soap.MTOMFeature;
 import javax.xml.ws.soap.SOAPBinding;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.sun.xml.ws.developer.JAXWSProperties;
 
 public class JaxWsServiceInterfaceCreator implements ServiceInterfaceCreator {
 
 	static final Logger myLogger = Logger
 			.getLogger(JaxWsServiceInterfaceCreator.class.getName());
 
 	public static String TRUST_FILE_NAME = Environment
 			.getGrisuClientDirectory().getPath()
 			+ File.separator
 			+ "truststore.jks";
 
 	/**
 	 * configures secure connection parameters.
 	 **/
 	public JaxWsServiceInterfaceCreator() throws ServiceInterfaceException {
 		try {
 			if (!(new File(Environment.getGrisuClientDirectory(),
 					"truststore.jks").exists())) {
 				final InputStream ts = JaxWsServiceInterfaceCreator.class
 						.getResourceAsStream("/truststore.jks");
 				IOUtils.copy(ts, new FileOutputStream(TRUST_FILE_NAME));
 			}
 		} catch (final IOException ex) {
 			throw new ServiceInterfaceException(
 					"cannot copy SSL certificate store into grisu home directory. Does "
 							+ Environment.getGrisuClientDirectory().getPath()
 							+ " exist?", ex);
 		}
 		System.setProperty("javax.net.ssl.trustStore", TRUST_FILE_NAME);
 		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
 
 	}
 
 	public boolean canHandleUrl(String url) {
 		if (StringUtils.isNotBlank(url) && url.startsWith("http")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public ServiceInterface create(String interfaceUrl, String username,
 			char[] password, String myProxyServer, String myProxyPort,
 			Object[] otherOptions) throws ServiceInterfaceException {
 
 		try {
 
 			final QName serviceName = new QName(
 					"http://api.grisu.arcs.org.au/", "GrisuService");
 			final QName portName = new QName("http://api.grisu.arcs.org.au/",
 					// "ServiceInterfaceSOAPPort");
 					"ServiceInterfacePort");
 
 			Service s;
 			try {
 				s = Service.create(
 						new URL(interfaceUrl.replace("soap/GrisuService",
 								"api.wsdl")), serviceName);
 			} catch (final MalformedURLException e) {
 				throw new RuntimeException(e);
 			}
 
 			final MTOMFeature mtom = new MTOMFeature();
 			try {
 				s.getPort(portName, ServiceInterface.class, mtom);
 			} catch (Error e) {
 				// throw new ServiceInterfaceException(
 				// "Could not connect to backend, probably because of frontend/backend incompatibility (Underlying cause: \""
 				// + e.getLocalizedMessage()
 				// +
 				// "\"). Before reporting a bug, please try latest client from: http://code.ceres.auckland.ac.nz/stable-downloads.");
 				throw new ServiceInterfaceException(
 						"Sorry, could not login. Most likely your client version is incompatible with the server.\n"
 								+ "Please download the latest version from:\nhttp://code.ceres.auckland.ac.nz/stable-downloads\n"
 								+ "If you have the latest version and are still experiencing this problem please contact\n"
 								+ "eresearch-admin@list.auckland.ac.nz\n"
								+ "with a description of the issue.\n\nUnderlying cause: "
								+ e.getLocalizedMessage());
 			}
 
 			final ServiceInterface service = s.getPort(portName,
 					ServiceInterface.class);
 
 			final BindingProvider bp = (javax.xml.ws.BindingProvider) service;
 
 			bp.getRequestContext().put(
 					javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
 					interfaceUrl);
 
 			bp.getRequestContext().put(
 					JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 4096);
 
 			// just to be sure, I'll keep that in there as well...
 			bp.getRequestContext()
 			.put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size",
 					new Integer(4096));
 
 			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
 					username);
 			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
 					new String(password));
 
 			bp.getRequestContext().put(
 					BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
 
 			final SOAPBinding binding = (SOAPBinding) bp.getBinding();
 			binding.setMTOMEnabled(true);
 
 			return service;
 
 		} catch (final Exception e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 			throw new ServiceInterfaceException(
 					"Could not create JaxwsServiceInterface: "
 							+ e.getLocalizedMessage(), e);
 		}
 
 	}
 
 	// private SSLSocketFactory createSocketFactory(String interfaceUrl)
 	// throws ServiceInterfaceException {
 	// // Technique similar to
 	// // http://juliusdavies.ca/commons-ssl/TrustExample.java.html
 	// HttpSecureProtocol protocolSocketFactory;
 	// try {
 	// protocolSocketFactory = new HttpSecureProtocol();
 	//
 	// TrustMaterial trustMaterial = null;
 	//
 	// // "/thecertificate.cer" can be PEM or DER (raw ASN.1). Can even
 	// // be several PEM certificates in one file.
 	//
 	// String cacertFilename = System.getProperty("grisu.cacert");
 	// URL cacertURL = null;
 	//
 	// try {
 	// if (cacertFilename != null && !"".equals(cacertFilename)) {
 	// cacertURL = JaxWsServiceInterfaceCreator.class
 	// .getResource("/" + cacertFilename);
 	// if (cacertURL != null) {
 	// myLogger.debug("Using cacert " + cacertFilename
 	// + " as configured in the -D option.");
 	// }
 	// }
 	// } catch (Exception e) {
 	// // doesn't matter
 	// myLogger
 	// .debug("Couldn't find specified cacert. Using default one.");
 	// }
 	//
 	// if (cacertURL == null) {
 	//
 	// cacertFilename = new CaCertManager()
 	// .getCaCertNameForServiceInterfaceUrl(interfaceUrl);
 	// if (cacertFilename != null && cacertFilename.length() > 0) {
 	// myLogger
 	// .debug("Found url in map. Trying to use this cacert file: "
 	// + cacertFilename);
 	// cacertURL = JaxWsServiceInterfaceCreator.class
 	// .getResource("/" + cacertFilename);
 	// if (cacertURL == null) {
 	// myLogger
 	// .debug("Didn't find cacert. Using the default one.");
 	// // use the default one
 	// cacertURL = JaxWsServiceInterfaceCreator.class
 	// .getResource("/cacert.pem");
 	// } else {
 	// myLogger.debug("Found cacert. Using it. Good.");
 	// }
 	// } else {
 	// myLogger
 	// .debug("Didn't find any configuration for a special cacert. Using the default one.");
 	// // use the default one
 	// cacertURL = JaxWsServiceInterfaceCreator.class
 	// .getResource("/cacert.pem");
 	// }
 	//
 	// }
 	//
 	// trustMaterial = new TrustMaterial(cacertURL);
 	//
 	// // We can use setTrustMaterial() instead of addTrustMaterial()
 	// // if we want to remove
 	// // HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
 	// protocolSocketFactory.addTrustMaterial(trustMaterial);
 	//
 	// // Maybe we want to turn off CN validation (not recommended!):
 	// protocolSocketFactory.setCheckHostname(false);
 	//
 	// Protocol protocol = new Protocol("https",
 	// (ProtocolSocketFactory) protocolSocketFactory, 443);
 	// Protocol.registerProtocol("https", protocol);
 	//
 	// return protocolSocketFactory;
 	// } catch (Exception e1) {
 	// // TODO Auto-generated catch block
 	// // e1.printStackTrace();
 	// throw new ServiceInterfaceException(
 	// "Unspecified error while trying to establish secure connection.",
 	// e1);
 	// }
 	// }
 
 }
