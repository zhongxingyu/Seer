 package de.uni.stuttgart.informatik.ToureNPlaner.Net;
 
 import android.util.Log;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.NodeModel;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Result;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.ServerInfo;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.User;
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
 import java.util.ArrayList;
 import java.util.UUID;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 public class Session implements Serializable {
 	public static final String IDENTIFIER = "session";
 	public static final String DIRECTORY = "session";
 
 	private static class Data implements Serializable {
 		private ServerInfo serverInfo;
 		private String username;
 		private String password;
 		private AlgorithmInfo selectedAlgorithm;
 		private User user;
 	}
 
 	private final UUID uuid;
 	private static transient Data d;
 	private static transient NodeModel nodeModel = new NodeModel();
 	private static transient Result result;
 
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
 
 	private void safe(Object o, String name) {
 		try {
 			File dir = new File(openCacheDir(), uuid.toString());
			dir.mkdir();
 
 			FileOutputStream outputStream = new FileOutputStream(new File(dir, name));
 			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(outputStream)));
 
 			try {
 				out.writeObject(o);
 			} finally {
 				out.close();
 			}
 		} catch (Exception e) {
 			Log.e("ToureNPLaner", "Session saving failed", e);
 		}
 	}
 
 	private void safeData() {
 		safe(d, "data");
 	}
 
 	private void safeNodeModel() {
 		safe(nodeModel, "nodeModel");
 	}
 
 	private void safeResult() {
 		safe(result, "result");
 	}
 
 	private void loadAll() {
 		d = (Data) load("data");
 		result = (Result) load("result");
 		nodeModel = (NodeModel) load("nodeModel");
 		if (nodeModel == null)
 			nodeModel = new NodeModel();
 	}
 
 	private Object load(String name) {
 		try {
 			File dir = new File(openCacheDir(), uuid.toString());
 
 			FileInputStream inputStream = new FileInputStream(new File(dir, name));
 
 			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
 			try {
 				return in.readObject();
 			} finally {
 				in.close();
 			}
 		} catch (Exception e) {
 			Log.e("ToureNPLaner", "Session loading failed", e);
 		}
 		return null;
 	}
 
 	public final static int MODEL_CHANGE = 1;
 	public final static int RESULT_CHANGE = 2;
 	public final static int NNS_CHANGE = 4;
 	public final static int ADD_CHANGE = 8;
 
 	public interface Listener {
 		void onChange(int change);
 	}
 
 	private transient ArrayList<Listener> listeners = new ArrayList<Listener>();
 
 	private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {
 		in.defaultReadObject();
 		if (d == null)
 			loadAll();
 		listeners = new ArrayList<Listener>();
 	}
 
 	public void registerListener(Listener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(Listener listener) {
 		listeners.remove(listener);
 	}
 
 	public void notifyChangeListerners(final int change) {
 		for (int i = 0; i < listeners.size(); i++) {
 			listeners.get(i).onChange(change);
 		}
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (Session.class) {
 					if (0 < (change & MODEL_CHANGE)) {
 						safeNodeModel();
 					}
 					if (0 < (change & RESULT_CHANGE)) {
 						safeResult();
 					}
 				}
 			}
 		}).start();
 	}
 
 	public Result getResult() {
 		return result;
 	}
 
 	public void setResult(Result result) {
 		Session.result = result;
 	}
 
 	public void setUser(User user) {
 		d.user = user;
 		safeData();
 	}
 
 	public User getUser() {
 		return d.user;
 	}
 
 	public void setNodeModel(NodeModel nodeModel) {
 		Session.nodeModel = nodeModel;
 	}
 
 	public NodeModel getNodeModel() {
 		return nodeModel;
 	}
 
 	public void setUrl(String url) {
 		try {
 			URL uri = new URL(url);
 			d.serverInfo.setHostname(uri.getHost());
 			d.serverInfo.setPort(uri.getPort());
 			uri.getProtocol();
 			safeData();
 		} catch (MalformedURLException e) {
 			// Should never happen
 			e.printStackTrace();
 		}
 	}
 
 	public String getUrl() {
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
 		if (acceptSmile)
 			acceptString = Util.ContentType.SMILE.identifier + ", " + Util.ContentType.JSON.identifier;
 		else
 			acceptString = Util.ContentType.JSON.identifier;
 		con.setRequestProperty("Accept", acceptString);
 
 		return con;
 	}
 
 	public String getUsername() {
 		return d.username;
 	}
 
 	public String getPassword() {
 		return d.password;
 	}
 
 	public AlgorithmInfo getSelectedAlgorithm() {
 		return d.selectedAlgorithm;
 	}
 
 	public void setSelectedAlgorithm(AlgorithmInfo selectedAlgorithm) {
 		d.selectedAlgorithm = selectedAlgorithm;
 		safeData();
 	}
 
 	public void setServerInfo(ServerInfo serverInfo) {
 		d.serverInfo = serverInfo;
 		safeData();
 	}
 
 	public ServerInfo getServerInfo() {
 		return d.serverInfo;
 	}
 
 	public void setUsername(String username) {
 		d.username = username;
 		safeData();
 	}
 
 	public void setPassword(String password) {
 		d.password = password;
 		safeData();
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
