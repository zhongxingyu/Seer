 //Based on SnifferSSLSocketFactory.java from The Grinder distribution.
 // The Grinder distribution is available at http://grinder.sourceforge.net/
 
 package mitm;
 
 import iaik.asn1.structures.AlgorithmID;
 import iaik.x509.X509Certificate;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.security.GeneralSecurityException;
 import java.security.KeyStore;
 import java.security.Principal;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.Certificate;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.net.ServerSocketFactory;
 import javax.net.SocketFactory;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.X509TrustManager;
 
 
 /**
  * MITMSSLSocketFactory is used to create SSL sockets.
  *
  * This is needed because the javax.net.ssl socket factory classes don't
  * allow creation of factories with custom parameters.
  *
  */
 public final class MITMSSLSocketFactory implements MITMSocketFactory
 {
     ServerSocketFactory m_serverSocketFactory;
     SocketFactory m_clientSocketFactory;
     SSLContext m_sslContext;
 
     public KeyStore ks = null;
 
     /*
      *
      * We can't install our own TrustManagerFactory without messing
      * with the security properties file. Hence we create our own
      * SSLContext and initialise it. Passing null as the keystore
      * parameter to SSLContext.init() results in a empty keystore
      * being used, as does passing the key manager array obtain from
      * keyManagerFactory.getInstance().getKeyManagers(). To pick up
      * the "default" keystore system properties, we have to read them
      * explicitly. UGLY, but necessary so we understand the expected
      * properties.
      *
      */
 
     /**
      * This constructor will create an SSL server socket factory
      * that is initialized with a fixed CA certificate
      */
     public MITMSSLSocketFactory()
 	throws IOException,GeneralSecurityException
     {
 	m_sslContext = SSLContext.getInstance("SSL");
 
 	final KeyManagerFactory keyManagerFactory =
 	    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
 
 	final String keyStoreFile = System.getProperty(JSSEConstants.KEYSTORE_PROPERTY);
 	final char[] keyStorePassword = System.getProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "").toCharArray();
 	final String keyStoreType = System.getProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "jks");
 
 	final KeyStore keyStore;
 	
 	if (keyStoreFile != null) {
 	    keyStore = KeyStore.getInstance(keyStoreType);
 	    keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
 
 	    this.ks = keyStore;
 	} else {
 	    keyStore = null;
 	}
 
 	keyManagerFactory.init(keyStore, keyStorePassword);
 
 	m_sslContext.init(keyManagerFactory.getKeyManagers(),
 			  new TrustManager[] { new TrustEveryone() },
 			  null);
 
 	m_clientSocketFactory = m_sslContext.getSocketFactory();
 	m_serverSocketFactory = m_sslContext.getServerSocketFactory(); 
     }
 
     /**
      * This constructor will create an SSL server socket factory
      * that is initialized with a dynamically generated server certificate
      * that contains the specified Distinguished Name.
      */
     public MITMSSLSocketFactory(Principal serverDN, BigInteger serialNumber)
 	throws IOException,GeneralSecurityException, Exception
     {
 	this();
 	// begin Borui Wang implementation
     // TODO(cs255): replace this with code to generate a new (forged) server certificate with a DN of serverDN
     //   and a serial number of serialNumber.
 
 	// You may find it useful to work from the comment skeleton below.
 	final String keyStoreFile = System.getProperty(JSSEConstants.KEYSTORE_PROPERTY);
 	final char[] keyStorePassword = System.getProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "").toCharArray();
 	final String keyStoreType = System.getProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "jks");
 	
 	// The "alias" is the name of the key pair in our keystore. (default: "mykey")
 	String alias = System.getProperty(JSSEConstants.KEYSTORE_ALIAS_PROPERTY);
 	if(alias == null){
 		alias = JSSEConstants.DEFAULT_ALIAS;
 	}
 	System.out.println("keyStoreFile "+keyStoreFile);
 	System.out.println("keyStorePassword "+keyStorePassword.toString());
 	System.out.println("keyStoreType "+keyStoreType);
 	System.out.println("keyStoreAlias "+alias);
 
 	final KeyStore keyStore;
 	if (keyStoreFile != null) {
 	    keyStore = KeyStore.getInstance(keyStoreType);
 	    keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
 	    this.ks = keyStore;
 	} else {
 	    keyStore = null;
 	}
 	
 	// Get our key pair and our own DN (not the remote server's DN) from the keystore.
 	PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyStorePassword);
 	iaik.x509.X509Certificate certificate = new iaik.x509.X509Certificate(keyStore.getCertificate(alias).getEncoded());
 	PublicKey publicKey = certificate.getPublicKey();
 	Principal ourDN = certificate.getIssuerDN();
 	System.out.println("private key: " + privateKey);
 	System.out.println("public key: " + publicKey);
 	System.out.println("ourDN: " + ourDN);
 	System.out.println("serverDN: " + serverDN);
 
 	// create server certificate
 	iaik.x509.X509Certificate serverCertificate = new iaik.x509.X509Certificate(certificate.getEncoded());
 	serverCertificate.setIssuerDN(ourDN);
 	serverCertificate.setPublicKey(publicKey);
 	serverCertificate.setSubjectDN(serverDN);
 	//int regularInt = 123; // some value to make sure the certificate has a unique serial number
 	//BigInteger bigInt = new BigInteger(String.valueOf(regularInt));
 	serverCertificate.setSerialNumber(serialNumber); 
 	// make sure we use a deterministic signing algorithm - from piazza
 	serverCertificate.sign(AlgorithmID.sha1WithRSAEncryption,privateKey);
 	X509Certificate[] certChain = {serverCertificate};
 	System.out.println("chain length: " + certChain.length);
 	keyStore.setKeyEntry(alias, privateKey, keyStorePassword, certChain);
 	
	// according to piazza, it's ok to use the keystore to update, which is faster than creating a new one
 	//KeyStore serverKeyStore = KeyStore.getInstance(keyStoreType);
 	//serverKeyStore.load(null,keyStorePassword);
 	//serverKeyStore.setKeyEntry(alias, privateKey, keyStorePassword, certChain);
 	//serverKeyStore.setKeyEntry(alias, privateKey, keyStorePassword, certChain);
 	//serverKeyStore.setEntry(alias, pkEntry, new KeyStore.PasswordProtection(keyStorePassword));
 	
 	// setup new certificate's key factory
 	
 	final KeyManagerFactory keyManagerFactory =
 	    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
 	keyManagerFactory.init(keyStore, keyStorePassword);
 
 	m_sslContext = SSLContext.getInstance("SSL");
 	m_sslContext.init(keyManagerFactory.getKeyManagers(),
 			  new TrustManager[] { new TrustEveryone() },
 			  null);
 
 	m_clientSocketFactory = m_sslContext.getSocketFactory();
 	m_serverSocketFactory = m_sslContext.getServerSocketFactory();
 	
 	// end Borui Wang implementationw
     }
 
     public final ServerSocket createServerSocket(String localHost,
 						 int localPort,
 						 int timeout)
 	throws IOException
     {
 	final SSLServerSocket socket =
 	    (SSLServerSocket)m_serverSocketFactory.createServerSocket(
 		localPort, 50, InetAddress.getByName(localHost));
 
 	socket.setSoTimeout(timeout);
 
 	socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
 
 	return socket;
     }
 
     public final Socket createClientSocket(String remoteHost, int remotePort)
 	throws IOException
     {
 	final SSLSocket socket =
 	    (SSLSocket)m_clientSocketFactory.createSocket(remoteHost,
 							  remotePort);
 
 	socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
 	
 	socket.startHandshake();
 
 	return socket;
     }
 
     /**
      * We're carrying out a MITM attack, we don't care whether the cert
      * chains are trusted or not ;-)
      *
      */
     private static class TrustEveryone implements javax.net.ssl.X509TrustManager
     {
 	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
 				       String authenticationType) {
 	}
 	
 	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
 				       String authenticationType) {
 	}
 
 	public java.security.cert.X509Certificate[] getAcceptedIssuers()
 	{
 	    return null;
 	}
     }
 }
     
