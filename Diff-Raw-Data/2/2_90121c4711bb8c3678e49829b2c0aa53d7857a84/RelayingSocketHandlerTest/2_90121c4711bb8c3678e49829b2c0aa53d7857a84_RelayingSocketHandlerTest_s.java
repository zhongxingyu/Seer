 package org.littleshoot.util;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.security.KeyStore;
 import java.security.Security;
 import java.util.Arrays;
 
 import javax.net.ServerSocketFactory;
 import javax.net.SocketFactory;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.TrustManager;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RelayingSocketHandlerTest {
     
     private static final int PORT = 8889;
     private static final int RELAY_PORT = 8899;
     private final Logger log = LoggerFactory.getLogger(getClass());
     private final InetSocketAddress serverAddressSsl = 
         new InetSocketAddress("127.0.0.1", PORT);
     private final InetSocketAddress serverAddressUdp = 
         new InetSocketAddress("127.0.0.1", PORT+1);
     private final SocketAddress relayServerSsl = 
         new InetSocketAddress("127.0.0.1", RELAY_PORT); 
     
     private final SocketAddress relayServerUdp = 
         new InetSocketAddress("127.0.0.1", RELAY_PORT+1); 
 
     /**
      * Tests SSL sockets as opposed to custom SSL-like stuff.
      * 
      * @throws Exception if anything goes wrong.
      */
     @Test 
     public void testRelaySsl() throws Exception {
         startEchoServer(serverAddressSsl);
         //byte[] readKey = CommonUtils.generateKey();
         //byte[] writeKey = CommonUtils.generateKey();
         
         startRelayServer(true, relayServerSsl);
         Thread.yield();
         Thread.sleep(400);
         
         //if (true)  return;
         final SSLSocket sock = (SSLSocket) newTlsSocketFactory().createSocket();
         System.out.println("CLIENT: "+Arrays.asList(sock.getEnabledCipherSuites()));
         //Thread.sleep(2000);
         sock.connect(relayServerSsl, 4000);
         //final CipherSocket cipher = new CipherSocket(sock, writeKey, readKey);
         final OutputStream os = sock.getOutputStream();
         final String msg = "what up my cracka?";
         log.info("Original message length: {}", msg.length());
         final byte[] msgBytes = msg.getBytes();
         os.write(msgBytes);
         
         final byte[] readBuf = new byte[100];
         final InputStream is = sock.getInputStream();
         final int read = is.read(readBuf);
         assertEquals(msgBytes.length, read);
         System.out.println("READ "+read);
         final String received = new String(readBuf, 0, read);
         System.out.println("'"+msg+"'");
         System.out.println("'"+received+"'");
         assertEquals(msg, received);
     }
     
     /**
      * Tests relaying with custom cipher code.
      * 
      * @throws Exception If any error happens.
      */
     @Test
     public void testRelayCipher() throws Exception {
         startEchoServer(serverAddressUdp);
 
         //byte[] readKey = CommonUtils.generateKey();
         //byte[] writeKey = CommonUtils.generateKey();
         
         startRelayServer(false, relayServerUdp);
         Thread.yield();
         Thread.sleep(400);
         
         final Socket sock = new Socket();
         sock.connect(relayServerUdp, 4000);
         //final CipherSocket cipher = new CipherSocket(sock, writeKey, readKey);
         final OutputStream os = sock.getOutputStream();
         final String msg = "what up my cracka?";
         log.info("Original message length: {}", msg.length());
         final byte[] msgBytes = msg.getBytes();
         os.write(msgBytes);
         
         final byte[] readBuf = new byte[100];
         final InputStream is = sock.getInputStream();
         final int read = is.read(readBuf);
         assertEquals(msgBytes.length, read);
         System.out.println("READ "+read);
         final String received = new String(readBuf, 0, read);
         System.out.println("'"+msg+"'");
         System.out.println("'"+received+"'");
         assertEquals(msg, received);
     }
 
     private void startRelayServer(final boolean ssl, 
         final SocketAddress relayServer) throws Exception {
         final ServerSocket server;
         if (ssl) {
             server = newTlsServerSocketFactory().createServerSocket();
         } else {
             server = new ServerSocket();
         }
         server.bind(relayServer);
         final Runnable runner = new Runnable() {
             public void run() {
                 try {
                     
                     final Socket sock = server.accept();
                     
                     if (ssl) {
                         System.out.println("SERVER: "+Arrays.asList(((SSLSocket)sock).getEnabledCipherSuites()));
                         final String[] suites = new String[] {
                            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA"
                         };
                         ((SSLSocket)sock).setEnabledCipherSuites(suites);
                     }
                     final InetSocketAddress isa;
                     if (ssl) {
                         isa = serverAddressSsl;
                     } else {
                         isa = serverAddressUdp;
                     }
                     final RelayingSocketHandler relay = 
                         new RelayingSocketHandler(isa);;
                     log.info("Notifying relay of socket");
                     relay.onSocket("testing", sock);
                 } catch (final Exception e) {
                     e.printStackTrace();
                 }
             }
         };
         final Thread t = new Thread(runner);
         t.setDaemon(true);
         t.start();
     }
     
     private KeyManagerFactory kmf;
     
     {
     try {
         kmf = newKmf();
     } catch (final Exception e) {
         e.printStackTrace();
     }
     }
     
     
     private ServerSocketFactory newTlsServerSocketFactory() throws Exception {
         // Initialize the SSLContext to work with our key managers.
         final SSLContext serverContext = SSLContext.getInstance("TLS");
         serverContext.init(kmf.getKeyManagers(), null, null);
         return serverContext.getServerSocketFactory();
     }
     
     private KeyManagerFactory newKmf() throws Exception {
         String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
         if (algorithm == null) {
             algorithm = "SunX509";
         }
 
         final SelfSignedKeyStoreManager ksm = new SelfSignedKeyStoreManager();
         final KeyStore ks = KeyStore.getInstance("JKS");
         ks.load(ksm.keyStoreAsInputStream(),
                 ksm.getKeyStorePassword());
 
         // Set up key manager factory to use our key store
         final KeyManagerFactory keyManagerFactory =
             KeyManagerFactory.getInstance(algorithm);
         keyManagerFactory.init(ks, ksm.getCertificatePassword());
         return keyManagerFactory;
     }
 
     private SocketFactory newTlsSocketFactory() throws Exception {
         final SSLContext clientContext = SSLContext.getInstance("TLS");
         //final SSLContext clientContext = SSLContext.getDefault();
         //clientContext.init(null, null, null);
         clientContext.init(kmf.getKeyManagers(), null, null);
         //final SSLContext clientContext = SSLContext.getDefault();
         return clientContext.getSocketFactory();
     }
 
     private void startEchoServer(final InetSocketAddress serverAddress) throws IOException {
         final ServerSocket server = new ServerSocket();
         server.bind(serverAddress);
         final Runnable runner = new Runnable() {
             public void run() {
                 try {
                     final Socket sock = server.accept();
 
                     final InputStream is = sock.getInputStream();
                     final byte[] plainText = new byte[1024*1024];
                     final int read = is.read(plainText);
                     log.info("Bytes read on echo server: {}", read);
                     final String originalString = 
                         new String(plainText, 0, read);
                     log.info("Got original string: {}", originalString);
                     final OutputStream os = sock.getOutputStream();
                     final byte[] bytes = originalString.getBytes();
                     log.info("Echoing array with length: {}", bytes.length);
                     os.write(bytes);
                     server.close();
               
                 } catch (final Exception e) {
                     e.printStackTrace();
                 }
             }
         };
         final Thread t = new Thread(runner);
         t.setDaemon(true);
         t.start();
     }
     
     /**
      * KeyStore manager that automatically generates a self-signed certificate
      * on startup if it doesn't already exit.
      */
     public class SelfSignedKeyStoreManager  {
         
         private final Logger log = LoggerFactory.getLogger(getClass());
         
         private final File KEYSTORE_FILE = new File("bop.jks");
         
         private final String AL = "littleproxy";
         
         private static final String PASS = "Be Your Own Lantern";
 
         public SelfSignedKeyStoreManager() {
             this(true);
         }
         
         public SelfSignedKeyStoreManager(final boolean regenerate) {
             System.setProperty("javax.net.ssl.keyStore", KEYSTORE_FILE.getName());
             System.setProperty("javax.net.ssl.trustStore", KEYSTORE_FILE.getName());
             resetStores();
         }
         
         private void resetStores() {
             if (KEYSTORE_FILE.isFile()) {
                 log.info("Not deleting keystore");
                 return;
             }
             
             nativeCall("keytool", "-genkey", "-alias", AL, "-keysize", 
                 "4096", "-validity", "36500", "-keyalg", "RSA", "-dname", 
                 "CN=littleproxy", "-keypass", PASS, "-storepass", 
                 PASS, "-keystore", KEYSTORE_FILE.getName());
             
             nativeCall("keytool", "-exportcert", "-alias", AL, "-keystore", 
                 KEYSTORE_FILE.getName(), "-storepass", PASS, "-file", 
                 "littleproxy_cert");
         }
 
         public String getBase64Cert() {
             return "";
         }
 
         public InputStream keyStoreAsInputStream() {
             try {
                 return new FileInputStream(KEYSTORE_FILE);
             } catch (final FileNotFoundException e) {
                 throw new Error("Could not find keystore file!!");
             }
         }
         
         public InputStream trustStoreAsInputStream() {
             return null;
         }
 
         public char[] getCertificatePassword() {
             return PASS.toCharArray();
         }
 
         public char[] getKeyStorePassword() {
             return PASS.toCharArray();
         }
         
         private String nativeCall(final String... commands) {
             log.info("Running '{}'", Arrays.asList(commands));
             final ProcessBuilder pb = new ProcessBuilder(commands);
             try {
                 final Process process = pb.start();
                 final InputStream is = process.getInputStream();
                 final String data = IOUtils.toString(is);
                 log.info("Completed native call: '{}'\nResponse: '"+data+"'", 
                     Arrays.asList(commands));
                 return data;
             } catch (final IOException e) {
                 log.error("Error running commands: " + Arrays.asList(commands), e);
                 return "";
             }
         }
 
         public TrustManager[] getTrustManagers() {
             // We don't use client authentication, so we should not need trust
             // managers.
             return null;
         }
 
     }
 }
