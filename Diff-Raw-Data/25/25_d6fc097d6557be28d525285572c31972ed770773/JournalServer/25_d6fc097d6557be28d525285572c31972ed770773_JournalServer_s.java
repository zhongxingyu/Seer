 /*
  * Documentation for Jessy:
  * http://docs.oracle.com/javase/1.4.2/docs/api/javax/net/ssl/package-summary.html
  */
 
 import java.security.*;
 import javax.security.cert.X509Certificate;
 import javax.net.*;
 import javax.net.ssl.*;
 import java.io.*;
 import java.util.logging.*;
 
 public class JournalServer {
 	private static final int LENGTH_LENGTH = 4; // length of the length field, bytes
 	private KeyStore keyStore;
 	private Logger log;
 
 	public static final int USER_PATIENT = 0;
 	public static final int USER_NURSE   = 1;
 	public static final int USER_DOCTOR  = 2;
 	public static final int USER_AGENCY  = 3;
 
 	public static void main(String args[]) {
 		new JournalServer().start(8080);
 	}
 
 	public JournalServer() {
 		try {
 			keyStore = KeyStore.getInstance("JKS");
 		} catch (KeyStoreException e) {
 			trace("could not open keystore. exiting (" + e + ")");
 		}		
 
 		FileHandler fileHandler = null;
 		try {
 			fileHandler = new FileHandler("logs/audit.log", 10*1024*1024, 100, true);
 		} catch (IOException ioe) {
 			trace("Could not open log file.");
 		}
 		fileHandler.setFormatter(new SimpleFormatter());
 		log = Logger.getLogger("auditLog");
 		log.addHandler(fileHandler);
 	}
 
 	private void trace(String msg) {
 		System.out.println("SERVER:\t" + msg);
 	}
 
 	private void log(String logmsg) {
 		log.info(logmsg);
 	}
 
 	public void start(int port) {
 		SSLServerSocketFactory ssf = null;
 		SSLServerSocket ss;
 
		// char[] keyStorePasswd = "password1".toCharArray();
		// char[] keyPasswd = "password2".toCharArray();
		// char[] trustPasswd = "password3".toCharArray();
 		String passwds[];
 
 		SSLContext ctx;
 		KeyManagerFactory kmf;
 		KeyStore ks, ts;
 		TrustManagerFactory tmf;
		char[] keyStorePasswd = "password1".toCharArray();
		char[] keyPasswd = "password2".toCharArray();
		char[] trustPasswd = "password3".toCharArray();
 
 		try {
 			passwds = this.readPassword();
			keyStorePasswd = passwds[0].toCharArray();
			keyPasswd = passwds[1].toCharArray();
			trustPasswd = passwds[2].toCharArray();
 		} catch (java.io.IOException e) {
 			this.trace("could not read passwords (" + e + ")");
 			System.exit(1);
 		}
 
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
 			trace("shit went south, bailing. (" + e + ")");
 			System.exit(1);
 		}
 
 		try {
 			ss = (SSLServerSocket) ssf.createServerSocket(port);
 		} catch (Exception e) {
 			trace("could not bind to port. " + e);
 			return;
 		}
 
 		ss.setNeedClientAuth(true);
 
 		// accept clients, maybe sould be multithreaded later
 		while (true) {
 			trace("waiting for incomming connection");
 			SSLSocket sock;
 
 			try {
 				sock = (SSLSocket) ss.accept();
 			} catch (java.io.IOException e) {
 				trace("failed to accept connection");
 				continue;
 			}
 
 			trace("accepted incomming connection");
 
 			// String[] suites = {"TLS_DHE_DSS_WITH_AES_256_CBC_SHA"};
 			// sock.setEnabledCipherSuites(suites);
 			SSLSession sess = sock.getSession();
 			X509Certificate cert;
 
 			try {
 				cert = (X509Certificate)sess.getPeerCertificateChain()[0];
 			} catch (javax.net.ssl.SSLPeerUnverifiedException e) {
 				trace("client not verified");
 				try {
 					sock.close();
 				} catch (java.io.IOException e2) {
 					trace("failed closing socket, w/e");
 				}
 				continue;
 			}
 
 			String subj = cert.getSubjectDN().getName();
 			trace("client DN: " + subj);
 			InputStream in;
 			try {
 				in = sock.getInputStream();
 			} catch (java.io.IOException e) {
 				trace("failed to get inputstream");
 				try {
 					sock.close();
 				} catch (java.io.IOException e2) {
 					trace("failed closing socket, w/e");
 				}
 				continue;
 			}
 			boolean terminated = false;
 			while (!terminated) {
 				int readBytes = 0;
 				int tmp, tmp_shift;
 				int length = 0;
 
 				while (readBytes < LENGTH_LENGTH) {
 					try {
 						tmp = in.read();
 					} catch (java.io.IOException e) {
 						continue;
 					}
 					++readBytes;
 					tmp_shift = tmp << (LENGTH_LENGTH - readBytes);
 					length |= tmp_shift;
 					System.out.printf("raw:%s shifted:%d addedToLength:%d\n", Integer.toHexString(tmp), tmp_shift, length);
 				}
 				if (length < 0) {
 					trace("the client is fucking w/ us. Alternativly the connection died");
 					terminated = true;
 					continue;
 				} else if (readBytes == LENGTH_LENGTH) {
 					trace("the msg is " + length + " bytes long");
 				} else {
 					trace("failed to read length field");
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
 						trace("got exception while reading message: " + e.toString());
 						break;
 					}
 
 					if (ret == -1) {
 						trace("fuck. something went south. breaking the parsing of message.");
 						break;
 					}
 					offset += ret;
 				}
 				if (offset < length) {
 					trace("could not read complete message");
 					terminated = true;
 					break;
 				}
 
 				Command command;
 				try {
 					// TODO get Journal.USER_XXX from db.getuserType(subj) or something
 					//command = CommandFactory.makeCommand(message, userType)
 					command = CommandFactory.makeCommand(message, JournalServer.USER_PATIENT);
 				} catch (UnknownCommandException uce) {
 					trace("Got unparsable command.");
 					terminated = true;
 					break;
 				}
 			}
 			if (terminated)
 				trace("terminated");
 		}
 	}
 
 	private void parseCmd(char[] cmd) {
 		System.out.println("---- cmd ----");
 		for (int i = 0; i < cmd.length; i++)
 			System.out.print(cmd[i]);
 		System.out.print('\n');
 	}
 
 	private String[] readPassword() throws IOException {
 		// while (keystorePassword == null || keystorePassword.length() == 0) {
 		// 	System.out.print("Server keystore password:");
 		// 	keystorePassword = new String(System.console().readPassword());
 		// }
 
 		// while (keyPassword == null || keyPassword.length() == 0) {
 		// 	System.out.print("Server keystore password:");
 		// 	keystorePassword = new String(System.console().readPassword());
 		// }
 
 		// while (truststorePassword == null || truststorePassword.length() == 0) {
 		// 	System.out.print("Server truststore password:");
 		// 	truststorePassword = new String(System.console().readPassword());
 		// }
 		String pws[] = new String[3];
 	    pws[0] = "password1";
 	    pws[1] = "password2";
 	    pws[2] = "password3";
 		return pws;
 	}
 }
