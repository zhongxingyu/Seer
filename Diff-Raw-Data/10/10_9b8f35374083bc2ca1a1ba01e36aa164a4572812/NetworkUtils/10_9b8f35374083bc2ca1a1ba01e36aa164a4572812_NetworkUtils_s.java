 package com.alphabetbloc.accessmrs.utilities;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.security.GeneralSecurityException;
 import java.security.KeyStore;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRoute;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.scheme.SocketFactory;
 import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.AbstractHttpEntity;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.alphabetbloc.accessmrs.R;
 
 /**
  * 
  * @author Louis Fazen (louis.fazen@gmail.com)
  * 
  */
 public class NetworkUtils {
 	public static final String TAG = NetworkUtils.class.getSimpleName();
 	// POST
 	public static final String INSTANCE_UPLOAD_URL = "/moduleServlet/xformshelper/xfhFormUpload";
 
 	// SECURE GET
 	public static final String PATIENT_DOWNLOAD_URL = "/module/odkconnector/download/patients.form";
 
 	// INSECURE GET
 	public static final String FORMLIST_DOWNLOAD_URL = "/moduleServlet/xformshelper/xfhFormList?type=odk_clinic&program=";
 	public static final String FORM_DOWNLOAD_URL = "/moduleServlet/xformshelper/xfhFormDownload?type=odk_clinic";
 
 	private static final int CONNECTION_TIMEOUT = 60000;
 	private static final int MAX_CONN_PER_ROUTE = 20;
 	private static final int MAX_CONNECTIONS = 20;
 
 	private static String mUsername;
 	private static String mPassword;
 
 	private static String getServerUrl() {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getApp());
 		return settings.getString(App.getApp().getString(R.string.key_server), App.getApp().getString(R.string.default_server));
 	}
 
 	public static String getFormUploadUrl() {
 		StringBuilder uploadUrl = new StringBuilder();
 		uploadUrl.append(getServerUrl()).append(INSTANCE_UPLOAD_URL);
 		uploadUrl.append("?uname=").append(getServerUsername());
 		uploadUrl.append("&pw=").append(getServerPassword());
 		return uploadUrl.toString();
 	}
 
 	public static String getPatientDownloadUrl() {
 		StringBuilder patientUrl = new StringBuilder();
 		patientUrl.append(getServerUrl()).append(PATIENT_DOWNLOAD_URL);
 		return patientUrl.toString();
 	}
 
 	public static String getFormListDownloadUrl() {
 		StringBuilder formlistUrl = new StringBuilder();
 		formlistUrl.append(getServerUrl()).append(FORMLIST_DOWNLOAD_URL);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getApp());
 		String program = prefs.getString(App.getApp().getString(R.string.key_program), App.getApp().getString(R.string.default_program));
 		formlistUrl.append(program);
 		return formlistUrl.toString();
 	}
 
 	public static String getFormDownloadUrl() {
 		StringBuilder formUrl = new StringBuilder();
 		formUrl.append(getServerUrl()).append(FORM_DOWNLOAD_URL);
 		return formUrl.toString();
 	}
 
 	public static String getServerUsername() {
 		if (mUsername == null)
 			getServerCredentials();
 		return mUsername;
 	}
 
 	public static String getServerPassword() {
 		if (mPassword == null)
 			getServerCredentials();
 		return mPassword;
 	}
 
 	public static void resetServerCredentials() {
 		mUsername = null;
 		mPassword = null;
 	}
 
 	private static void getServerCredentials() {
 		final AccountManager am = AccountManager.get(App.getApp());
 		Account[] accounts = am.getAccountsByType(App.getApp().getString(R.string.app_account_type));
 
 		if (App.DEBUG)
 			Log.v(TAG, "accounts.length =" + accounts.length);
 		if (accounts.length <= 0) {
 			if (App.DEBUG)
 				Log.v(TAG, "no accounts have been set up");
 
 		} else {
 
 			mUsername = accounts[0].name;
 			String encPwd = am.getPassword(accounts[0]);
 			mPassword = EncryptionUtil.decryptString(encPwd);
 		}
 
 	}
 
 	public static HttpClient getHttpClient() {
 		HttpClient client = null;
 		try {
 
 			if (App.DEBUG)
 				Log.v(TAG, "httpClient is null, download is creating a new client");
 			SSLContext sslContext = createSslContext();
 			MySSLSocketFactory socketFactory = new MySSLSocketFactory(sslContext, new BrowserCompatHostnameVerifier());
 			client = createHttpClient(socketFactory);
			Log.e(TAG, "created the client and now returning it...");
 		} catch (GeneralSecurityException e) {
 			Log.e(TAG, "Could not load the trust manager");
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.e(TAG, "Could not load the trust manager. Ensure credential storage is available");
 			e.printStackTrace();
 		}
 
 		return client;
 	}
 
 	public static SSLContext createSslContext() throws GeneralSecurityException, IOException {
 
 		// TrustStore
 		KeyStore trustStore = FileUtils.loadSslStore(FileUtils.MY_TRUSTSTORE);
 		if (trustStore == null)
 			throw new IOException("Access denied. Ensure credential storage is available.");
 		MyTrustManager myTrustManager = new MyTrustManager(trustStore);
 		TrustManager[] tms = new TrustManager[] { myTrustManager };
 
 		// KeyStore
 		KeyManager[] kms = null;
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getApp());
 		boolean useClientAuth = prefs.getBoolean(App.getApp().getString(R.string.key_client_auth), false);
 		if (useClientAuth) {
 			KeyStore keyStore = FileUtils.loadSslStore(FileUtils.MY_KEYSTORE);
 			if (keyStore == null)
 				throw new IOException("Access denied. Ensure credential storage is available.");
 			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
 			kmf.init(keyStore, EncryptionUtil.getPassword().toCharArray());
 			kms = kmf.getKeyManagers();
 		}
 
 		SSLContext context = SSLContext.getInstance("TLS");
 		context.init(kms, tms, null);
 		return context;
 	}
 
 	public static HttpClient createHttpClient(SocketFactory socketFactory) {
 		HttpParams params = new BasicHttpParams();
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); // yaw
 		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); // yaw
 		HttpProtocolParams.setUseExpectContinue(params, false); // yaw
 		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT); // yaw
 		HttpClientParams.setRedirecting(params, false); // yaw
 
 		ConnManagerParams.setTimeout(params, CONNECTION_TIMEOUT); // yaw
 		ConnPerRoute connPerRoute = new ConnPerRouteBean(MAX_CONN_PER_ROUTE);
 		ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
 		ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTIONS);
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 
 		SocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
 		if (socketFactory != null) {
 			sslSocketFactory = socketFactory;
 		}
 		try {
 			schemeRegistry.register(new Scheme("http", sslSocketFactory, 80));
 			schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
 			schemeRegistry.register(new Scheme("https", sslSocketFactory, 8443));
 		} catch (Exception e) {
 			Log.e(TAG, "Caught an EXCEPTION. could not register the scheme?");
 			e.printStackTrace();
 			// TODO: handle exception
 		}
 		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
 		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
 
 		return httpClient;
 	}
 
 	public static MultipartEntity createMultipartEntity(String path) {
 
 		// find all files in parent directory
 		File file = new File(path);
 		File[] files = file.getParentFile().listFiles();
 		if (App.DEBUG)
 			Log.v(TAG, file.getAbsolutePath());
 
 		// mime post
 		MultipartEntity entity = null;
 		if (files != null) {
 			entity = new MultipartEntity();
 			for (int j = 0; j < files.length; j++) {
 				File f = files[j];
 				FileBody fb;
 				if (f.getName().endsWith(".xml")) {
 					fb = new FileBody(f, "text/xml");
 					entity.addPart("xml_submission_file", fb);
 					if (App.DEBUG)
 						Log.v(TAG, "added xml file " + f.getName());
 				} else if (f.getName().endsWith(".jpg")) {
 					fb = new FileBody(f, "image/jpeg");
 					entity.addPart(f.getName(), fb);
 					if (App.DEBUG)
 						Log.v(TAG, "added image file " + f.getName());
 				} else if (f.getName().endsWith(".3gpp")) {
 					fb = new FileBody(f, "audio/3gpp");
 					entity.addPart(f.getName(), fb);
 					if (App.DEBUG)
 						Log.v(TAG, "added audio file " + f.getName());
 				} else if (f.getName().endsWith(".3gp")) {
 					fb = new FileBody(f, "video/3gpp");
 					entity.addPart(f.getName(), fb);
 					if (App.DEBUG)
 						Log.v(TAG, "added video file " + f.getName());
 				} else if (f.getName().endsWith(".mp4")) {
 					fb = new FileBody(f, "video/mp4");
 					entity.addPart(f.getName(), fb);
 					if (App.DEBUG)
 						Log.v(TAG, "added video file " + f.getName());
 				} else {
 					Log.w(TAG, "unsupported file type, not adding file: " + f.getName());
 				}
 			}
 		} else {
 			if (App.DEBUG)
 				Log.v(TAG, "no files to upload in instance");
 		}
 
 		return entity;
 	}
 
 	// TODO! This should not have to check trusted at every single time...
 	// (maybe need to go into MyTrustManager to fix this?)
 	public static void postEntity(HttpClient client, String url, MultipartEntity entity) throws IOException {
 		HttpPost httppost = new HttpPost(url);
 		httppost.setEntity(entity);
 		HttpResponse response = client.execute(httppost);
 		// verify response is okay
 		int responseCode = response.getStatusLine().getStatusCode();
 		if (App.DEBUG)
 			Log.v(TAG, "httppost response=" + responseCode);
 		if (responseCode != HttpURLConnection.HTTP_OK)
 			throw new IOException(App.getApp().getString(R.string.error_connection));
 	}
 
 	public static InputStream getStream(HttpClient client, String url) throws Exception {
 		HttpResponse response = null;
 		try {
 			HttpGet get = new HttpGet(url);
 			response = client.execute(get);
 		} catch (Exception e) {
 			Log.e(TAG, "Caught Error getStream from Server! ");
 			e.printStackTrace();
 
 		}
 		// verify response is okay
 		if (response.getStatusLine().getStatusCode() != 200) {
 			Log.e(TAG, "Error: " + response.getStatusLine());
 			throw new IOException(App.getApp().getString(R.string.error_connection));
 		} else {
 			if (App.DEBUG)
 				Log.v(TAG, "NO Error!: " + response.getStatusLine());
 		}
 
 		// Get hold of the response entity
 		HttpEntity entity = response.getEntity();
 
 		if (entity != null) {
 			InputStream is = entity.getContent();
 			return is;
 		}
 
 		return null;
 
 	}
 
 	public static DataInputStream getOdkStream(HttpClient client, String url) throws Exception {
 
 		// get prefs
 		HttpPost request = new HttpPost(url);
 		request.setEntity(new OdkAuthEntity());
 		HttpResponse response = client.execute(request);
 		response.getStatusLine().getStatusCode();
 		HttpEntity responseEntity = response.getEntity();
		responseEntity.getContentLength();
 
 		DataInputStream zdis = new DataInputStream(new GZIPInputStream(responseEntity.getContent()));
 
 		int status = zdis.readInt();
 		if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
 			zdis.close();
 			throw new IOException("Access denied. Check your username and password.");
 		} else if (status <= 0 || status >= HttpURLConnection.HTTP_BAD_REQUEST) {
 			zdis.close();
 			throw new IOException(App.getApp().getString(R.string.error_connection));
 		} else {
 			assert (status == HttpURLConnection.HTTP_OK); // success
 			return zdis;
 		}
 	}
 
 	private static class OdkAuthEntity extends AbstractHttpEntity {
 
 		public boolean isRepeatable() {
 			return false;
 		}
 
 		public long getContentLength() {
 			return -1;
 		}
 
 		public boolean isStreaming() {
 			return false;
 		}
 
 		public InputStream getContent() throws IOException {
 			// Should be implemented as well but is irrelevant for this case
 			throw new UnsupportedOperationException();
 		}
 
 		public void writeTo(final OutputStream outstream) throws IOException {
 			DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(outstream));
 			dos.writeUTF(getServerUsername());
 			dos.writeUTF(getServerPassword());
 
 			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getApp());
 			Boolean savedSearch = settings.getBoolean(App.getApp().getString(R.string.key_use_saved_searches), false);
 			Integer cohort = Integer.valueOf(settings.getString(App.getApp().getString(R.string.key_saved_search), "0"));
 			Integer program = Integer.valueOf(settings.getString(App.getApp().getString(R.string.key_program), "0"));
 
 			if (App.DEBUG)
 				Log.v(TAG, "Writing variables to stream \n  cohort=" + cohort + "\n  program=" + program + "\n  savedSearch=" + savedSearch);
 			dos.writeBoolean(savedSearch);
 			if (cohort > 0)
 				dos.writeInt(cohort);
 			if (program > 0)
 				dos.writeInt(program);
 			dos.close();
 		}
 
 	}
 
 }
