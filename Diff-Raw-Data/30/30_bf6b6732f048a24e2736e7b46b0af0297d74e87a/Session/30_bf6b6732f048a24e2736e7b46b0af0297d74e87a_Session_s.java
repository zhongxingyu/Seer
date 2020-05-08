 package de.uni.stuttgart.informatik.ToureNPlaner.Net;
 
 import android.util.Log;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.*;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.NodeModel;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RequestHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.ServerInfoHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
 import de.uni.stuttgart.informatik.ToureNPlaner.Util.Base64;
 import org.mapsforge.core.GeoPoint;
 
 import javax.net.ssl.HttpsURLConnection;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.UUID;
 import java.util.WeakHashMap;
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
 		private ArrayList<Constraint> constraints;
 		private int nameCounter;
 	}
 
 	private final UUID uuid;
 	private static transient Data d;
 	private static transient NodeModel nodeModel = new NodeModel();
 	private static transient Result result;
 
 	public static File openCacheDir() {
 		return new File(ToureNPlanerApplication.getContext().getCacheDir(), DIRECTORY);
 	}
 
 	public Session() {
 		this.uuid = UUID.randomUUID();
 		d = new Data();
 		nodeModel = new NodeModel();
 		result = new Result();
 		// Also initialize the files on the disc
 		safeData();
 		safeNodeModel();
 		safeResult();
 	}
 
 	private void safe(Object o, String name) {
 		try {
 			File dir = new File(openCacheDir(), uuid.toString());
 			dir.mkdirs();
 
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
 		nodeModel = (NodeModel) load("nodeModel");
 		if (nodeModel == null)
 			nodeModel = new NodeModel();
 		result = (Result) load("result");
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
 
 	public static final int MODEL_CHANGE = 1;
 	public static final int RESULT_CHANGE = 2;
 	public static final int NNS_CHANGE = 4;
 	public static final int ADD_CHANGE = 8;
 	public static final int DND_CHANGE = 16;
 
 	public interface Listener {
 		void onChange(int change);
 	}
 
 	private transient WeakHashMap<Object, Listener> listeners = new WeakHashMap<Object, Listener>();
 
 	private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {
 		in.defaultReadObject();
 		if (d == null)
 			loadAll();
 		listeners = new WeakHashMap<Object, Listener>();
 	}
 
 	public void registerListener(Listener listener) {
 		listeners.put(listener.getClass(), listener);
 	}
 
 	public void removeListener(Listener listener) {
 		listeners.remove(listener.getClass());
 	}
 
 	public void notifyChangeListerners(final int change) {
 		if (0 < (change & MODEL_CHANGE) || 0 < (change & DND_CHANGE)) {
 			nodeModel.incVersion();
 		}
 
 		if (nodeModel.size() == 0)
 			d.nameCounter = 0;
 
 		for (Listener listener : listeners.values()) {
 			listener.onChange(change);
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
 
 	public boolean canPerformRequest() {
 		// Check if every algorithm constrain is set
 		for (int i = 0; i < d.constraints.size(); i++) {
 			if (d.constraints.get(i).getValue() == null)
 				return false;
 		}
 
 		// Check if every point constraint is set
 		if (d.selectedAlgorithm.getPointConstraintTypes().isEmpty())
 			if (!nodeModel.allSet())
 				return false;
 
 		return nodeModel.size() >= d.selectedAlgorithm.getMinPoints() &&
 				nodeModel.size() <= d.selectedAlgorithm.getMaxPoints();
 	}
 
 	public String getUrl() {
 		return d.serverInfo.getURL();
 	}
 
 	public HttpURLConnection openGetConnection(String path) throws IOException {
 		URL uri = new URL(getUrl() + path);
 
 		if (d.serverInfo.getServerType() == ServerInfo.ServerType.PRIVATE) {
 			try {
 				HttpsURLConnection con = (HttpsURLConnection) uri.openConnection();
 				con.setSSLSocketFactory(ToureNPlanerApplication.getSslContext().getSocketFactory());
 
 				String userPassword = getUsername() + ":" + getPassword();
 				String encoding = Base64.encodeString(userPassword);
 				con.setRequestProperty("Authorization", "Basic " + encoding);
 				con.setRequestProperty("Accept",
 						JacksonManager.ContentType.SMILE.identifier + ", " + JacksonManager.ContentType.JSON.identifier);
 				return con;
 			} catch (Exception e) {
 				Log.e("TP", "SSL", e);
 			}
 		}
 
 		return (HttpURLConnection) uri.openConnection();
 	}
 
 	public HttpURLConnection openPostConnection(String path) throws IOException {
 		HttpURLConnection con = openGetConnection(path);
 
 		con.setDoOutput(true);
 		con.setChunkedStreamingMode(0);
 		con.setRequestProperty("Content-Type", "application/json;");
 
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
		d.nameCounter = 0;
 		if (!selectedAlgorithm.equals(d.selectedAlgorithm)) {
 			d.selectedAlgorithm = selectedAlgorithm;
 			d.constraints = new ArrayList<Constraint>(selectedAlgorithm.getConstraintTypes().size());
 			for (int i = 0; i < selectedAlgorithm.getConstraintTypes().size(); i++) {
 				d.constraints.add(new Constraint(selectedAlgorithm.getConstraintTypes().get(i)));
 			}
 			safeData();
 		}
 	}
 
 	private static String createName(int num) {
 		String str = "";
 
 		int modulo;
 		num++;
 		while (num > 0) {
 			modulo = (num - 1) % 26;
 			str = Character.toString((char) (modulo + 'A')) + str;
 			num = (num - modulo) / 26;
 		}
 
 		return str;
 	}
 
 	public Node createNode(GeoPoint geoPoint) {
 		return new Node(nodeModel.getVersion(), createName(d.nameCounter++), geoPoint, d.selectedAlgorithm.getPointConstraintTypes());
 	}
 
 	public ArrayList<Constraint> getConstraints() {
 		return d.constraints;
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
 
 	public RequestHandler performRequest(Observer requestListener, boolean force) {
 		if (canPerformRequest() && (force || result == null || nodeModel.getVersion() != result.getVersion())) {
 			return (RequestHandler) new RequestHandler(requestListener, this).execute();
 		} else {
 			return null;
 		}
 	}
 }
