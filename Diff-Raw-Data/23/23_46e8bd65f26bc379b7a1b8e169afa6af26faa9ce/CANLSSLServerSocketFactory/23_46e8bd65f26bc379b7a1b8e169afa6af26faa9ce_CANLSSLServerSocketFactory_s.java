 /*
  * Copyright (c) 2012 Helsinki Institute of Physics All rights reserved.
  * See LICENCE file for licensing information.
  */
 
 package eu.emi.security.canl.tomcat;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.security.KeyStoreException;
 import java.security.PrivateKey;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.net.ssl.SSLException;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 
 import org.apache.tomcat.util.net.ServerSocketFactory;
 
 import eu.emi.security.authn.x509.CrlCheckingMode;
 import eu.emi.security.authn.x509.NamespaceCheckingMode;
 import eu.emi.security.authn.x509.ProxySupport;
 import eu.emi.security.authn.x509.RevocationParameters;
 import eu.emi.security.authn.x509.StoreUpdateListener;
 import eu.emi.security.authn.x509.impl.CertificateUtils;
 import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
 import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
 import eu.emi.security.authn.x509.impl.OpensslCertChainValidator;
 import eu.emi.security.authn.x509.impl.SocketFactoryCreator;
 import eu.emi.security.authn.x509.impl.ValidatorParams;
 import eu.emi.security.authn.x509.impl.X500NameUtils;
 
 /**
  * The Tomcat glue ServerSocketFactory class. This class works as a glue
  * interface that interfaces the TrustManager SSL implementation with the
  * Tomcat.
  * 
  * Created on 2012-06-13
  * 
  * @author Joni Hahkala
  */
 public class CANLSSLServerSocketFactory extends ServerSocketFactory {
     /**
      * The logging facility.
      */
     private static final org.apache.commons.logging.Log LOGGER = org.apache.commons.logging.LogFactory
             .getLog(CANLSSLServerSocketFactory.class);
 
     /** The internal serversocket instance. */
     protected SSLServerSocketFactory _serverSocketFactory = null;
 
     /**
      * Flag to allow the renegotiation, and thus exposing the connection to man
      * in the middle attack.
      */
     private boolean m_allowUnsafeLegacyRenegotiation = false;
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.tomcat.util.net.ServerSocketFactory#acceptSocket(java.net.
      * ServerSocket)
      */
     public Socket acceptSocket(ServerSocket sSocket) throws IOException {
 
         SSLSocket asock = null;
 
         try {
             asock = (SSLSocket) sSocket.accept();
             configureClientAuth(asock);
             String ip = asock.getInetAddress().toString();
             javax.security.cert.X509Certificate[] certs = asock.getSession().getPeerCertificateChain();
             String dn = null;
             if (certs != null) {
                 dn = X500NameUtils.getReadableForm(certs[0].getSubjectDN().toString());
             }
             Date now = new Date();
 
             System.out.println(now.toString() + " : connection from " + ip
                     + ((dn == null) ? " no certificate." : " dn: " + dn + "."));
         } catch (SSLException e) {
             throw new SocketException("SSL handshake error" + e.toString());
         }
 
         return asock;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.tomcat.util.net.ServerSocketFactory#createSocket(int,
      * int, java.net.InetAddress)
      */
     public ServerSocket createSocket(int port, int backlog, InetAddress ifAddress) throws IOException,
             InstantiationException {
         if (_serverSocketFactory == null) {
             initServerSocketFactory();
         }
 
         ServerSocket socket = _serverSocketFactory.createServerSocket(port, backlog, ifAddress);
         initServerSocket(socket);
 
         return socket;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.tomcat.util.net.ServerSocketFactory#createSocket(int,
      * int)
      */
     public ServerSocket createSocket(int port, int backlog) throws IOException, InstantiationException {
         if (_serverSocketFactory == null) {
             initServerSocketFactory();
         }
 
         ServerSocket socket = _serverSocketFactory.createServerSocket(port, backlog);
         initServerSocket(socket);
 
         return socket;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.tomcat.util.net.ServerSocketFactory#createSocket(int)
      */
     public ServerSocket createSocket(int port) throws IOException, InstantiationException {
         if (_serverSocketFactory == null) {
             initServerSocketFactory();
         }
 
         ServerSocket socket = _serverSocketFactory.createServerSocket(port);
         initServerSocket(socket);
 
         return socket;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.tomcat.util.net.ServerSocketFactory#handshake(java.net.Socket)
      */
     public void handshake(Socket socket) throws IOException {
         // We do getSession instead of startHandshake() so we can call this
         // multiple times
         SSLSession session = ((SSLSocket) socket).getSession();
         if (session.getCipherSuite().equals("SSL_NULL_WITH_NULL_NULL"))
             throw new IOException("SSL handshake failed. Ciper suite in SSL Session is SSL_NULL_WITH_NULL_NULL");
 
         if (!m_allowUnsafeLegacyRenegotiation) {
             // Prevent further handshakes by removing all cipher suites
             ((SSLSocket) socket).setEnabledCipherSuites(new String[0]);
         }
     }
 
     /**
      * Initialize the SSL socket factory.
      * 
      * @exception IOException if an input/output error occurs
      */
     private void initServerSocketFactory() throws IOException {
 
         StoreUpdateListener listener = new StoreUpdateListener() {
             public void loadingNotification(String location, String type, Severity level, Exception cause) {
                 if (level != Severity.NOTIFICATION) {
                     System.out.println("Error when creating or using SSL socket. Type " + type + " level: " + level
                            + " cause: " + cause.getClass() + ":" + cause.getMessage());
                 } else {
                     // log successful (re)loading
                 }
             }
         };
 
         ArrayList<StoreUpdateListener> listenerList = new ArrayList<StoreUpdateListener>();
         listenerList.add(listener);
 
         RevocationParameters revParam = new RevocationParameters(CrlCheckingMode.REQUIRE);
         String crlCheckingMode = (String) attributes.get("crlcheckingmode");
         if (crlCheckingMode != null) {
             if (crlCheckingMode.equalsIgnoreCase("ifvalid")) {
                 revParam = new RevocationParameters(CrlCheckingMode.IF_VALID);
             }
             if (crlCheckingMode.equalsIgnoreCase("ignore")) {
                 revParam = new RevocationParameters(CrlCheckingMode.IGNORE);
             }
         }
 
         ProxySupport proxySupport = ProxySupport.ALLOW;
         String proxySupportString = (String) attributes.get("proxysupport");
         if (proxySupportString != null) {
             if (proxySupportString.equalsIgnoreCase("no") || proxySupportString.equalsIgnoreCase("false")) {
                 proxySupport = ProxySupport.DENY;
             }
         }
 
         ValidatorParams validatorParams = new ValidatorParams(revParam, proxySupport, listenerList);
 
         String trustStoreLocation = (String) attributes.get("truststore");
         if (trustStoreLocation == null) {
             throw new IOException("No truststore defined, unable to load CA certificates and thus create SSL socket.");
         }
 
         String namespaceModeString = (String) attributes.get("namespace");
         NamespaceCheckingMode namespaceMode = NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS;
         if (namespaceModeString != null) {
             if (namespaceModeString.equalsIgnoreCase("no") || namespaceModeString.equalsIgnoreCase("false")
                     || namespaceModeString.equalsIgnoreCase("off")) {
                 namespaceMode = NamespaceCheckingMode.IGNORE;
             } else {
                 if (namespaceModeString.equalsIgnoreCase("require")) {
                     namespaceMode = NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE;
                 }
             }
 
         }
 
         String intervalString = (String) attributes.get("updateinterval");
         long intervalMS = 3600000; // update every hour
         if (intervalString != null) {
             intervalMS = Long.parseLong(intervalString);
         }
 
         OpensslCertChainValidator validator = new OpensslCertChainValidator(trustStoreLocation, namespaceMode,
                 intervalMS, validatorParams);
 
         String hostCertLoc = (String) attributes.get("hostcert");
         if (hostCertLoc == null) {
             throw new IOException(
                     "Variable hostcert undefined, cannot start server with SSL/TLS without host certificate.");
         }
         java.security.cert.X509Certificate[] hostCertChain = CertificateUtils.loadCertificateChain(new FileInputStream(
                 hostCertLoc), Encoding.PEM);
 
         String hostKeyLoc = (String) attributes.get("hostkey");
         if (hostKeyLoc == null) {
             throw new IOException(
                     "Variable hostkey undefined, cannot start server with SSL/TLS without host private key.");
         }
         PrivateKey hostKey = CertificateUtils.loadPrivateKey(new FileInputStream(hostKeyLoc), Encoding.PEM, null);
 
         KeyAndCertCredential credentials;
         try {
             credentials = new KeyAndCertCredential(hostKey, hostCertChain);
         } catch (KeyStoreException e) {
             throw new IOException("Error while creating keystore: " + e + ": " + e.getMessage(), e);
         }
 
         _serverSocketFactory = SocketFactoryCreator.getServerSocketFactory(credentials, validator);
 
     }
 
     /**
      * Configures the given SSL server socket with the requested cipher suites
      * and need for client authentication.
      * 
      * @param ssocket the server socket to initialize.
      */
     private void initServerSocket(ServerSocket ssocket) {
         LOGGER.debug("TMSSLServerSocketFactory.initServerSocket:");
 
         SSLServerSocket socket = (SSLServerSocket) ssocket;
 
         // disable RC4 ciphers (Java x Globus problems)
         // disable also ECDH ciphers because of faulty java implementation
         String ciphers[] = socket.getEnabledCipherSuites();
         ArrayList<String> newCiphers = new ArrayList<String>(ciphers.length);
         for (String cipher : ciphers) {
             if (cipher.indexOf("RC4") == -1 && cipher.indexOf("ECDH") == -1) {
                 LOGGER.debug("Enabling cipher: " + cipher);
                 newCiphers.add(cipher);
             } else {
                 LOGGER.debug("Disabling cipher: " + cipher);
             }
         }
         socket.setEnabledCipherSuites(newCiphers.toArray(new String[] {}));
 
         // we don't know if client auth is needed -
         // after parsing the request we may re-handshake
         configureClientAuth(socket);
     }
 
     /**
      * Configure whether the client authentication is wanted, needed or not.
      * 
      * @param socket The socket to configure
      */
     protected void configureClientAuth(SSLServerSocket socket) {
         String clientAuthStr = (String) attributes.get("clientauth");
 
         if (clientAuthStr == null) {
             return;
         }
 
         if ("true".equalsIgnoreCase(clientAuthStr) || "yes".equalsIgnoreCase(clientAuthStr)) {
             socket.setNeedClientAuth(true);
         }
 
         if ("want".equalsIgnoreCase(clientAuthStr)) {
             socket.setWantClientAuth(true);
         }
     }
 
     /**
      * Configure whether the client authentication is wanted, needed or not.
      * 
      * @param socket The socket to configure
      */
     protected void configureClientAuth(SSLSocket socket) {
         String clientAuthStr = (String) attributes.get("clientauth");
 
         if (clientAuthStr == null) {
             return;
         }
 
         if ("true".equalsIgnoreCase(clientAuthStr) || "yes".equalsIgnoreCase(clientAuthStr)) {
             socket.setNeedClientAuth(true);
         }
 
         if ("want".equalsIgnoreCase(clientAuthStr)) {
             socket.setWantClientAuth(true);
         }
     }
 }
