 package de.uni.stuttgart.informatik.ToureNPlaner.Net;
 
 import android.util.Log;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.*;
 import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
 import de.uni.stuttgart.informatik.ToureNPlaner.Util.Base64;
 
 import javax.net.ssl.*;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.UUID;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 public class Session implements Serializable {
     public static final String IDENTIFIER = "session";
 	public static final String DIRECTORY = "session";
 
 	private static class Data implements Serializable
 	{
 		private ServerInfo serverInfo;
 	    private String username;
 	    private String password;
 	    private AlgorithmInfo selectedAlgorithm;
 	    private NodeModel nodeModel = new NodeModel();
 		private Result result;
 		private User user;
 	}
 
 	private final UUID uuid;
 	private static transient Data d;
 
 	private static HostnameVerifier acceptAllHostnameVerifier = new HostnameVerifier() {
 		@Override
 		public boolean verify(String s, SSLSession sslSession) {
 			return true;
 		}
 	};
 
 	private static TrustManager[] acceptAllTrustManager = new TrustManager[]{new X509TrustManager() {
 		@Override
 		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
 
 		}
 
 		@Override
 		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
 
 		}
 
 		@Override
 		public X509Certificate[] getAcceptedIssuers() {
 			return null;
 		}
 	}};
 
 	private static SSLContext sslContext;
 
 	static {
 		try {
 			sslContext = SSLContext.getInstance("TLS");
 			sslContext.init(null, acceptAllTrustManager, null);
 		} catch (NoSuchAlgorithmException e) {
 			Log.e("TP", "SSL", e);
 		} catch (KeyManagementException e) {
 			Log.e("TP", "SSL", e);
 		}
 	}
 
 	public static File openCacheDir() {
 		return new File(ToureNPlanerApplication.getContext().getCacheDir(), DIRECTORY);
 	}
 
 	public Session() {
 		this.uuid = UUID.randomUUID();
 		d = new Data();
 	}
 
 	public void safe() {
 		try {
 			File dir = openCacheDir();
 			dir.mkdir();
             FileOutputStream outputStream = new FileOutputStream(new File(dir, uuid.toString()));
 
 			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(outputStream)));
             try {
                 out.writeObject(d);
             } finally {
                 out.close();
             }
         } catch (Exception e) {
			Log.e("ToureNPLaner","Session loading failed", e);
         }
 	}
 
 	private void load() {
 		try {
 			File dir = openCacheDir();
             FileInputStream inputStream = new FileInputStream(new File(dir, uuid.toString()));
 
 			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
             try {
                 d = (Data) in.readObject();
             } finally {
                 in.close();
             }
         } catch (Exception e) {
 			Log.e("ToureNPLaner","Session loading failed", e);
 			// If we can't load load an empty session
 			// Can happen if user deletes cache and tries to restore session afterwards
 			// TODO won't work still need to initialise members
 			d = new Data();
         }
 	}
 
 	private void checkData() {
 		if(d == null)
 			load();
 	}
 
 	public Result getResult() {
 	    checkData();
         return d.result;
     }
 
     public void setResult(Result result) {
 	    checkData();
 	    d.result = result;
 	    safe();
     }
 
 	public void setUser(User user) {
 		checkData();
 	    d.user = user;
 	    safe();
 	}
 
 	public User getUser() {
 		checkData();
 	    return d.user;
 	}
 
     public void setNodeModel(NodeModel nodeModel) {
 	    checkData();
         d.nodeModel = nodeModel;
 	    safe();
     }
 
 	public void setUrl(String url) {
 		checkData();
 		try {
 			URL uri = new URL(url);
 			d.serverInfo.setHostname(uri.getHost());
 			d.serverInfo.setPort(uri.getPort());
 			uri.getProtocol();
 			safe();
 		} catch (MalformedURLException e) {
 			// Should never happen
 			e.printStackTrace();
 		}
 	}
 
     public String getUrl() {
 	    checkData();
         return d.serverInfo.getURL();
 	}
 
 	public HttpURLConnection openGetConnection(String path) throws IOException {
 		URL uri = new URL(getUrl() + path);
 
 		if (d.serverInfo.getServerType() == ServerInfo.ServerType.PRIVATE) {
 			try {
 				HttpsURLConnection con = (HttpsURLConnection) uri.openConnection();
 				con.setSSLSocketFactory(sslContext.getSocketFactory());
 				con.setHostnameVerifier(acceptAllHostnameVerifier);
 
 				String userPassword = getUsername() + ":" + getPassword();
 				String encoding = Base64.encodeString(userPassword);
 				con.setRequestProperty("Authorization", "Basic " + encoding);
 				con.setRequestProperty("Accept", Util.ContentType.JSON.identifier);
 				return con;
 			} catch (Exception e) {
 				Log.e("TP", "SSL", e);
 			}
 		}
 
 		return (HttpURLConnection) uri.openConnection();
     }
 
 	public HttpURLConnection openPostConnection(String path, boolean acceptSmile) throws IOException {
 		HttpURLConnection con = openGetConnection(path);
 
 		con.setDoOutput(true);
 		con.setChunkedStreamingMode(0);
 		con.setRequestProperty("Content-Type", "application/json;");
 		String acceptString;
 		if(acceptSmile)
 			acceptString = Util.ContentType.SMILE.identifier + ", " + Util.ContentType.JSON.identifier;
 		else
 			acceptString = Util.ContentType.JSON.identifier;
 		con.setRequestProperty("Accept", acceptString);
 
 		return con;
 	}
 
     public String getUsername() {
 	    checkData();
         return d.username;
     }
 
     public String getPassword() {
 	    checkData();
         return d.password;
     }
 
     public NodeModel getNodeModel() {
 	    checkData();
         return d.nodeModel;
     }
 
     public AlgorithmInfo getSelectedAlgorithm() {
 	    checkData();
         return d.selectedAlgorithm;
     }
 
     public void setSelectedAlgorithm(AlgorithmInfo selectedAlgorithm) {
 	    checkData();
         d.selectedAlgorithm = selectedAlgorithm;
 	    safe();
     }
 
 	public void setServerInfo(ServerInfo serverInfo) {
 		checkData();
         d.serverInfo = serverInfo;
 		safe();
     }
 
     public ServerInfo getServerInfo() {
 	    checkData();
         return d.serverInfo;
     }
 
     public void setUsername(String username) {
 	    checkData();
         d.username = username;
 	    safe();
     }
 
     public void setPassword(String password) {
 	    checkData();
         d.password = password;
 	    safe();
     }
 
     /**
      * @param url      The URL to connect to
      * @param listener the Callback listener
      * @return Use this to cancel the task with cancel(true)
      */
     public static ServerInfoHandler createSession(String url, Observer listener) {
         ServerInfoHandler handler = new ServerInfoHandler(listener, url);
         handler.execute();
         return handler;
     }
 }
