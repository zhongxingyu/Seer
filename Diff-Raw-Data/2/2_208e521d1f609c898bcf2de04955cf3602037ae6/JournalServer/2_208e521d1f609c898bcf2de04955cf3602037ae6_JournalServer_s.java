 /*
  * documentation for java ssl stuff can be found here:
  * http://docs.oracle.com/javase/1.4.2/docs/api/javax/net/ssl/package-summary.html
  */
 
 import java.security.KeyStore;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSession;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import javax.security.cert.X509Certificate;
 import java.security.KeyStoreException;
 import javax.net.ssl.*;
 import javax.net.*;
 
 public class JournalServer {
     private static final int LENGTH_LENGTH = 4; // length of the length field, bytes
     protected KeyStore keyStore;
 
     public JournalServer() {
 	// System.setProperty("javax.net.ssl.trustStore", "myTrustStore");
 	// System.setProperty("javax.net.ssl.trustStorePassword", "password3");
 	// System.setProperty("javax.net.ssl.keyStore", "../keystore");
 	// System.setProperty("javax.net.ssl.keyStorePassword", "password1");
 	// System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
 	// System.setProperty("javax.net.ssl.keyStore", "new.p12");
 	// System.setProperty("javax.net.ssl.keyStorePassword", "newpasswd");
 
 	try {
 	    this.keyStore = KeyStore.getInstance("JKS");
 	} catch (KeyStoreException e) {
 	    log("could not open keystore. exiting (" + e + ")");
 	}		
     }
 
     private void log(String msg) {
 	System.out.println("SERVER:\t" + msg);
     }
 
     // ugly pos
     public void start(int port) {
 	SSLServerSocketFactory ssf = null;
 	SSLServerSocket ss;
 
 	char[] keyStorePasswd = "password1".toCharArray();
 	char[] keyPasswd = "password2".toCharArray();
 	char[] trustPasswd = "password3".toCharArray();
 
 	SSLContext ctx;
 	KeyManagerFactory kmf;
 	KeyStore ks, ts;
 	TrustManagerFactory tmf;
 	try {
 	    ctx = SSLContext.getInstance("TLS");
 	    kmf = KeyManagerFactory.getInstance("SunX509");
 	    ks = KeyStore.getInstance("JKS");
 	    ks.load(new FileInputStream("./keystore"), keyStorePasswd);
 	    kmf.init(ks, keyPasswd);
 
 	    tmf = TrustManagerFactory.getInstance("SunX509");
 	    ts = KeyStore.getInstance("JKS");
 	    ts.load(new FileInputStream("./truststore"), trustPasswd);
 	    tmf.init(ts);
 	    
 	    ctx = SSLContext.getInstance("TLS");
 	    ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
 	    ssf = ctx.getServerSocketFactory();
 	} catch (Exception e) {
 	    this.log("shit went south, bailing. (" + e + ")");
 	    System.exit(1);
 	}
 		
 	try {
 	    ss = (SSLServerSocket) ssf.createServerSocket(port);
 	} catch (Exception e) {
 	    log("could not bind to port. " + e);
 	    return;
 	}
 
 	ss.setNeedClientAuth(true);
 
 	// accept clients, maybe sould be multithreaded later
 	while (true) {
 	    log("waiting for incomming connection");
 	    SSLSocket sock;
 
 	    try {
 		sock = (SSLSocket) ss.accept();
 	    } catch (java.io.IOException e) {
 		log("failed to accept connection");
 		continue;
 	    }
 
 	    log("accepted incomming connection");
 
 	    // String[] suites = {"TLS_DHE_DSS_WITH_AES_256_CBC_SHA"};
 	    // sock.setEnabledCipherSuites(suites);
 	    SSLSession sess = sock.getSession();
 	    X509Certificate cert;
 
 	    try {
 		cert = (X509Certificate)sess.getPeerCertificateChain()[0];
 	    } catch (javax.net.ssl.SSLPeerUnverifiedException e) {
 		log("client not verified");
 		try {
 		    sock.close();
 		} catch (java.io.IOException e2) {
 		    log("failed closing socket, w/e");
 		}
 		continue;
 	    }
 
 	    String subj = cert.getSubjectDN().getName();
 	    System.out.println("SERVER:\tclient DN: " + subj);
 
 	    int readBytes = 0;
 	    int tmp, tmp_shift;
 	    int length = 0;
 	    InputStream in;
 	    try {
 		in = sock.getInputStream();
 	    } catch (java.io.IOException e) {
 		log("failed to get inputstream");
 		try {
 		    sock.close();
 		} catch (java.io.IOException e2) {
 		    log("failed closing socket, w/e");
 		}
 		continue;
 	    }
 
 	    while (readBytes < LENGTH_LENGTH) {
 		try {
 		    tmp = in.read();
 		} catch (java.io.IOException e) {
 		    continue;
 		}
 		readBytes += 1;
 		tmp_shift = tmp << (LENGTH_LENGTH - readBytes);
		length += tmp_shift;
 		System.out.printf("raw:%s shifted:%d addedToLength:%d\n", Integer.toHexString(tmp), tmp_shift, length);
 	    }
 
 	    if (readBytes == LENGTH_LENGTH) {
 		log("the msg is " + length + " bytes long");
 	    } else {
 		log("SERVER:\tfailed to read length field");
 		continue;
 	    }
 	    // got length, do work.
 	    InputStreamReader reader = new InputStreamReader(in);
 	    char[] message = new char[length];
 	    int ret;
 	    int offset = 0;
 	    while (offset < length) {
 		try {
 		    ret = reader.read(message, offset, (length - offset));
 		} catch(Exception e) {
 		    this.log("got exception while reading message: " + e.toString());
 		    break;
 		}
 
 		if (ret == -1) {
 		    this.log("fuck. something went south. breaking the parsing of message.");
 		    break;
 		}
 		offset += ret;
 	    }
 	    if (offset < length) {
 		this.log("could not read complete message");
 		break;
 	    }
 	    this.parseCmd(message);
 	}
     }
 
     protected String parseCmd(char[] cmd) {
 	return "You wrote " + cmd + "\n";
     }
 
     public static void main(String args[]) {
 	JournalServer js;
 
 	js = new JournalServer();
 	js.start(8080);
     }
 }
