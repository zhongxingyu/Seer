 //Based on SnifferSSLSocketFactory.java from The Grinder distribution.
 // The Grinder distribution is available at http://grinder.sourceforge.net/
 
 package mitm;
 
 import iaik.asn1.ObjectID;
 import iaik.asn1.structures.AlgorithmID;
 import iaik.x509.X509Certificate;
 import iaik.asn1.structures.Name;
 
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
 import java.security.KeyStore.PrivateKeyEntry;
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
     final ServerSocketFactory m_serverSocketFactory;
     final SocketFactory m_clientSocketFactory;
     final SSLContext m_sslContext;
 
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
 
             PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)
            keyStore.getEntry("cs255key", new KeyStore.PasswordProtection(keyStorePassword));
 
             iaik.x509.X509Certificate X509cert = new iaik.x509.X509Certificate(pkEntry.getCertificate().getEncoded());
 
             Name subject = new Name();
             fillRDN(subject, serverDN.toString());
             X509cert.setSubjectDN(subject);
 
             Name issuer = new Name();
            fillRDN(issuer, serverDN.toString());
             X509cert.setIssuerDN(issuer);
 
             X509cert.setSerialNumber(serialNumber);
             X509cert.setPublicKey(pkEntry.getCertificate().getPublicKey());
             X509cert.sign(AlgorithmID.sha1WithRSAEncryption, pkEntry.getPrivateKey());
 
             Certificate[] originalChain = pkEntry.getCertificateChain();
             originalChain[0] = X509cert;
            keyStore.setKeyEntry("cs255key", pkEntry.getPrivateKey(), keyStorePassword, originalChain);
 
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
 
     public void fillRDN(Name subject, String dname) {
             String[] dnameKeys = {
                 "CN=",
                 "O=",
                 "OU=",
                 "L=",
                 "EMAIL=",
                 "ST=",
                 "C="
             };
             ObjectID[] dnameObjectIDs = {
                 ObjectID.commonName,
                 ObjectID.organization,
                 ObjectID.organizationalUnit,
                 ObjectID.locality,
                 ObjectID.emailAddress,
                 ObjectID.stateOrProvince,
                 ObjectID.country
             };
 
             dname += ",";
             for (int i = 0; i < dnameKeys.length; i++)
             {
                 String dnameKey = dnameKeys[i];
                 int startIndex = dname.indexOf(dnameKey);
                 if (startIndex >= 0) {
                     int boundingIndex = dname.indexOf("=", startIndex + dnameKey.length());
                     if (boundingIndex == -1) {
                         boundingIndex = dname.length() - 1;
                     }
                     int endIndex = dname.lastIndexOf(",", boundingIndex);
                     subject.addRDN(dnameObjectIDs[i], dname.substring(startIndex + dnameKey.length(), endIndex));
                 }
             }
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
     
