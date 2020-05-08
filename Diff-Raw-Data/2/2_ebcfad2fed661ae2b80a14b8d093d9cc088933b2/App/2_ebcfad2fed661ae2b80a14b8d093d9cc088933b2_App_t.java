 package models;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import play.db.jpa.GenericModel;
 
 @Entity
 public class App extends GenericModel {
 	@Id
 	public String name;
 
 	public String apikey;
 
 	public int frequency;
 
 	public Date lastMeasured;
 
 	public App(String name, String apikey, int frequency) {
 		this.name = name;
 		this.apikey = apikey;
 		this.frequency = frequency;
 	}
 
 	public Collection<Result> analyse() throws ClientProtocolException,
 			IOException {
 		InputStream in = snapshot();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		Measure measure = new Measure(this, new Date());
 		Result result = new Result(measure, "all");
 		HashMap<String, Result> results = new HashMap<String, Result>();
 		while (true) {
 			String line = reader.readLine();
 			if (line == null)
 				break;
 			try {
 				LogLine logLine = new LogLine(line);
 				result.add(logLine);
 				Result r = results.get(logLine.uri);
 				if (r == null) {
 					r = new Result(measure, logLine.uri);
 					results.put(logLine.uri, r);
 				}
 				r.add(logLine);
 			} catch (Throwable t) {
 				continue;
 			}
 		}
 		results.put(result.uri, result);
 		Collection<Result> list = results.values();
 		measure.save();
 		for (Result r : list) {
 			r.save();
 		}
 		lastMeasured = measure.date;
 		save();
 		return list;
 	}
 
 	public InputStream snapshot() throws ClientProtocolException, IOException {
 		DefaultHttpClient client = createClient();
 		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("",
 				apikey);
 		CredentialsProvider credsProvider = new BasicCredentialsProvider();
 		credsProvider.setCredentials(AuthScope.ANY, creds);
 		client.setCredentialsProvider(credsProvider);
 		HttpGet logurl = new HttpGet("https://api.heroku.com/apps/" + name
 				+ "/logs?logplex=true&ps=router&num=1500");
 		logurl.addHeader("Accept", "application/xml");
 		HttpResponse response = client.execute(logurl);
 		String logsessionURL = EntityUtils.toString(response.getEntity());
 		HttpGet logsession = new HttpGet(logsessionURL);
 		logsession.addHeader("Accept", "application/xml");
 		response = client.execute(logsession);
 		return response.getEntity().getContent();
 	}
 
 	public static DefaultHttpClient createClient() {
 		try {
 			DefaultHttpClient client = new DefaultHttpClient();
 			SSLContext ctx = SSLContext.getInstance("TLS");
 			X509TrustManager tm = new X509TrustManager() {
 
 				public void checkClientTrusted(X509Certificate[] xcs,
 						String string) throws CertificateException {
 				}
 
 				public void checkServerTrusted(X509Certificate[] xcs,
 						String string) throws CertificateException {
 				}
 
 				public X509Certificate[] getAcceptedIssuers() {
 					return null;
 				}
 			};
 			ctx.init(null, new TrustManager[] { tm }, null);
 			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
 			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 			ClientConnectionManager ccm = client.getConnectionManager();
 			SchemeRegistry sr = ccm.getSchemeRegistry();
 			sr.register(new Scheme("https", ssf, 443));
 			return client;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Possible queries:
 	 * <ul>
 	 * <li>empty - same as since 10 Days back</li>
 	 * <li>days=10 - last 10 days</li>
 	 * <li>url=/ - only one url, "all" is allowed</li>
 	 * <li>mode=avg|max - show mode, max is default</li>
 	 * 
 	 * @param q
 	 * @return
 	 */
 	public Collection<Result> query(Integer days, String url) {
 		if (days == null)
 			days = 10;
 		Date date = new Date(System.currentTimeMillis() - days * 24 * 60 * 60
 				* 1000L);
 		if (url == null) {
 			return Result.find("app=? and date>?", this, date).fetch();
 		} else {
 			return Result.find("app=? and date>? and uri=?", this, date, url)
 					.fetch();
 		}
 	}
 
 	public static Collection<App> findWaiting() {
 		List<App> all = findAll();
 		Collection<App> result = new ArrayList<App>();
 		long now = System.currentTimeMillis();
 		for (App app : all) {
 			if (app.lastMeasured == null
					|| now - app.lastMeasured.getTime() > app.frequency * 60 * 1000L)
 				result.add(app);
 		}
 		return result;
 	}
 
 }
