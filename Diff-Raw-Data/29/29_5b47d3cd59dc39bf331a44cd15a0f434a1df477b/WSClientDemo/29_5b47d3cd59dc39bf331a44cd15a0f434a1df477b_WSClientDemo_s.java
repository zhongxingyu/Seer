 package client;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.UnknownHostException;
 import java.security.KeyStore;
 import java.security.KeyStore.Builder;
 import java.security.cert.Certificate;
 import java.security.cert.X509Certificate;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.activation.DataHandler;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.KeyStoreBuilderParameters;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManagerFactory;
 import javax.xml.ws.BindingProvider;
 
 import org.apache.cxf.configuration.jsse.TLSClientParameters;
 import org.apache.cxf.configuration.security.FiltersType;
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.frontend.ClientProxy;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.security.provider.X509Factory;
 
 import ch.admin.vbs.cube.common.crypto.Base64;
 import ch.admin.vbs.cube.common.crypto.PemToolkit;
 import ch.admin.vbs.cube.common.keyring.IIdentityToken.KeyType;
 import ch.admin.vbs.cube.common.keyring.impl.IdentityToken;
 import ch.admin.vbs.cube.cubemanage.CubeManage;
 import ch.admin.vbs.cube.cubemanage.CubeManagePortType;
 
 public class WSClientDemo {
 	private static final Logger LOG = LoggerFactory.getLogger(WSClientDemo.class);
 
 	public WSClientDemo() {
 	}
 
 	public void https() throws Exception {
 		// TrustStore & TrustManagerFactory
 		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(new FileInputStream(new File(System.getProperty("user.home"), "cube-pki/truststore.jks")), "123456".toCharArray());
 		tmf.init(ts);
 		// KeyStore & KeyManagerFactory
 		KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
 		Builder clientCrtBuilder = KeyStore.Builder.newInstance("JKS", //
 				null, //
				new File(System.getProperty("user.home"), "cube-pki/client0.jks"), //
 				new KeyStore.PasswordProtection("123456".toCharArray()));
 		KeyStoreBuilderParameters keyStoreBuilderParameters = new KeyStoreBuilderParameters(clientCrtBuilder);
 		kmf.init(keyStoreBuilderParameters);
 		// SSL Context
 		SSLContext ctx = SSLContext.getInstance("TLS");
 		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
 		// SocketFactory
 		SSLSocketFactory factory = ctx.getSocketFactory();
 		// test request
 		// testHttpsRequest(factory); System.exit(0);
 		// web service (use local wdsl since it will not work online with client
 		// auth)
 		CubeManage service = new CubeManage(getClass().getResource("/CubeManage.wsdl"));
 		CubeManagePortType port = service.getCubeManagePort();
 		// update proxy to enforce ssl client certificate
 		Client proxy = ClientProxy.getClient(port);
 		((BindingProvider) port).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
 		HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
 		TLSClientParameters tlsParam = new TLSClientParameters();
 		tlsParam.setDisableCNCheck(false);
 		tlsParam.setSSLSocketFactory(factory);
 		addFilters(tlsParam);
 		conduit.setTlsClientParameters(tlsParam);
 		// load all certificates in token object
 		IdentityToken token = new IdentityToken(clientCrtBuilder.getKeyStore(), clientCrtBuilder, "123456".toCharArray());
 		// WebService: login command is used to send our public encryption key
 		port.login(token.getCertificate(KeyType.ENCIPHERMENT).getEncoded());
 		// WebService: report some message.
 		port.report("login", System.currentTimeMillis());
 		// WebService: list user VMs
 		DataHandler dh = port.listVMs();
 		ZipInputStream zis = new ZipInputStream(dh.getInputStream());
 		ZipEntry entry = zis.getNextEntry();
 		while (entry != null) {
 			try {
 				System.out.println("Entry [" + entry.getName() + "] <<");
 			} catch (Exception e) {
 				LOG.error("Failed to proces entry.", e);
 			}
 			entry = zis.getNextEntry();
 		}
 		//
 		port.report("logout", System.currentTimeMillis());
 	}
 
 	private void addFilters(TLSClientParameters tlsClientParameters) {
 		/*
 		 * these filters ensure that a ciphersuite with export-suitable or null
 		 * encryption is used, but exclude anonymous Diffie-Hellman key change
 		 * as this is vulnerable to man-in-the-middle attacks
 		 */
 		FiltersType filter = new FiltersType();
 		filter.getInclude().add(".*_EXPORT_.*");
 		filter.getInclude().add(".*_EXPORT1024_.*");
 		filter.getInclude().add(".*_WITH_DES_.*");
 		filter.getInclude().add(".*_WITH_AES_.*");
 		filter.getInclude().add(".*_WITH_NULL_.*");
 		filter.getExclude().add(".*_DH_anon_.*");
 		tlsClientParameters.setCipherSuitesFilter(filter);
 	}
 
 	private void testHttpsRequest(SSLSocketFactory factory) throws UnknownHostException, IOException {
 		// test request
 		SSLSocket socket = (SSLSocket) factory.createSocket("server.cube.com", 8443);
 		/*
 		 * send http request See SSLSocketClient.java for more information about
 		 * why there is a forced handshake here when using PrintWriters.
 		 */
 		socket.startHandshake();
 		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
 		out.println("GET /cube-server/services HTTP/1.0");
 		out.println();
 		out.flush();
 		/*
 		 * Make sure there were no surprises
 		 */
 		if (out.checkError())
 			System.out.println("SSLSocketClient: java.io.PrintWriter error");
 		/* read response */
 		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		String inputLine;
 		while ((inputLine = in.readLine()) != null)
 			System.out.println(inputLine);
 		in.close();
 		out.close();
 		socket.close();
 	}
 
 	public static void main(String[] args) throws Exception {
 		WSClientDemo c = new WSClientDemo();
 		// System.out.println("## HTTP ############");
 		// c.http();
 		System.out.println("## HTTPS ###########");
 		c.https();
 	}
 }
