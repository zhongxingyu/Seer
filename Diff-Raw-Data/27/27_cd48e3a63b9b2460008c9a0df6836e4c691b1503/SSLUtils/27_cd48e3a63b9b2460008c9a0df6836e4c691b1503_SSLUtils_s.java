 package org.fedoraproject.eclipse.packager;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.security.cert.X509Certificate;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.commons.ssl.KeyMaterial;
 import org.apache.commons.ssl.TrustChain;
 import org.apache.commons.ssl.TrustMaterial;
import org.eclipse.core.runtime.Path;
 
 public class SSLUtils {
 	public static void initSSLConnection() throws GeneralSecurityException,
 			IOException {
 		TrustChain tc = getTrustChain();
 
 		KeyMaterial kmat = getKeyMaterial();
 
		SSLContext sc = SSLContext.getInstance("SSL");
 
 		// Create empty HostnameVerifier
 		HostnameVerifier hv = new HostnameVerifier() {
 			public boolean verify(String arg0, SSLSession arg1) {
 				return true;
 			}
 		};
 
 		sc.init((KeyManager[]) kmat.getKeyManagers(), (TrustManager[]) tc
 				.getTrustManagers(), new java.security.SecureRandom());
 		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		HttpsURLConnection.setDefaultHostnameVerifier(hv);
 
 	}
 
 	public static KeyMaterial getKeyMaterial()
 			throws GeneralSecurityException, IOException {
		String file = System.getProperty("user.home") + Path.SEPARATOR
				+ ".fedora.cert";
 		KeyMaterial kmat = new KeyMaterial(new File(file), new File(file),
 				new char[0]);
 		return kmat;
 	}
 
 	protected static TrustManager[] getTrustManager() {
 		// Create a trust manager that does not validate certificate chains
 		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
 			public X509Certificate[] getAcceptedIssuers() {
 				return null;
 			}
 
 			public void checkClientTrusted(X509Certificate[] certs,
 					String authType) {
 				// Trust always
 			}
 
 			public void checkServerTrusted(X509Certificate[] certs,
 					String authType) {
 				// Trust always
 			}
 		} };
 		return trustAllCerts;
 	}
 
 	protected static TrustChain getTrustChain()
 			throws GeneralSecurityException, IOException {
		String file = System.getProperty("user.home") + Path.SEPARATOR
				+ ".fedora-upload-ca.cert";
 		TrustChain tc = new TrustChain();
 		tc.addTrustMaterial(new TrustMaterial(file));
		file = System.getProperty("user.home") + Path.SEPARATOR
				+ ".fedora-server-ca.cert";
 		tc.addTrustMaterial(new TrustMaterial(file));
 		return tc;
 	}
 }
