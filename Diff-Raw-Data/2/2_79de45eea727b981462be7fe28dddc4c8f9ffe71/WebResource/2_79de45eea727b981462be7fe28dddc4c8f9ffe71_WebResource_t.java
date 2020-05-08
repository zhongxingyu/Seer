 package org.dentleisen.appening2;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.coobird.thumbnailator.Thumbnails;
 
 import org.apache.http.Header;
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 
 public class WebResource {
 
 	private static Logger log = Logger.getLogger(WebResource.class);
 
 	public enum Type {
 		webpage, image
 	}
 
 	private String shortenedUrl;
 	private Type type = Type.webpage;
 	private String url;
 	private String title;
 	private boolean hadErrors = false;
 	private String imageUrl;
 	private int placeId;
 	private Date tweeted;
 
 	private static Pattern pageTitlePattern = Pattern.compile(
 			"<title>(.*?)</title>", Pattern.DOTALL);
 
 	public WebResource(String shortenedUrl, Place p, Message m) {
 		this.shortenedUrl = shortenedUrl;
 		this.tweeted = m.getCreated();
 		this.placeId = p.id;
 	}
 
 	public WebResource(int placeId, Date tweeted, String url, Type type,
 			String title, String mediaUrl) {
 		this.placeId = placeId;
 		this.tweeted = tweeted;
 		this.url = url;
 		this.type = type;
 		this.title = title;
 		this.imageUrl = mediaUrl;
 	}
 
 	public static Map<String, Pattern> imageScrapers = new HashMap<String, Pattern>();
 	static {
 		imageScrapers
 				.put("twitpic",
 						Pattern.compile(
 								" <img src=\"(http://[^\"]*\\.cloudfront\\.net/photos/large/[^\"]*)\" ",
 								Pattern.DOTALL));
 		imageScrapers
 				.put("instagram",
 						Pattern.compile(
 								"<img class=\"photo\" src=\"(http://[^\"]*\\.instagram.com/[^\"]*)\"",
 								Pattern.DOTALL));
 		imageScrapers.put("lockerz", Pattern.compile(
 				"<img id=\"photo\" src=\"(http://[^\"]*)\"", Pattern.DOTALL));
 		imageScrapers.put("img.ly", Pattern.compile(
 				"<img [^>]* id=\"the-image\" src=\"(http://[^\"]*)\"",
 				Pattern.DOTALL));
 		imageScrapers.put("yfrog", Pattern.compile(
 				"<img [^>]* id=\"main_image\" src=\"(http://[^\"]*)\"",
 				Pattern.DOTALL));
 	}
 
 	public WebResource resolve() {
 		try {
 			HttpContext localContext = new BasicHttpContext();
 
 			HttpGet get = new HttpGet(shortenedUrl);
 			HttpResponse getResponse = httpClient.execute(get, localContext);
 
 			url = getUrlAfterRedirects(localContext);
 			URL u = new URL(url);
 
 			String pageContent = EntityUtils.toString(getResponse.getEntity());
 
 			Matcher m = pageTitlePattern.matcher(pageContent);
 			if (m.find()) {
 				title = m.group(1);
 				title = title.replace("\n", " ");
 				title = title.replace("\t", " ");
 				title = title.replaceAll(" {2,}", " ");
 				title = title.trim();
 			}
 
 			for (Entry<String, Pattern> scraper : imageScrapers.entrySet()) {
 				if (u.getHost().contains(scraper.getKey())) {
 					Matcher sm = scraper.getValue().matcher(pageContent);
 					if (sm.find()) {
 						imageUrl = sm.group(1);
 						type = Type.image;
 					}
 				}
 			}
 
 			return this;
 		} catch (Exception e) {
 			log.debug("Failed to resolve URL", e);
 		}
 		hadErrors = true;
 		return null;
 	}
 
 	public File downloadImageAndResize(int thumbnailSize) {
 		if (this.type != Type.image) {
 			log.warn("Cannot download non-images!");
 			return null;
 		}
 		try {
 			File f = File.createTempFile(this.getClass().getSimpleName() + "-",
 					".image");
 			HttpResponse getResponse = httpClient
 					.execute(new HttpGet(imageUrl));
 			getResponse.getEntity().writeTo(new FileOutputStream(f));
 
 			File imageFile = File.createTempFile(this.getClass()
 					.getSimpleName() + "-", ".png");
 			Thumbnails.of(f).size(thumbnailSize, thumbnailSize)
 					.outputFormat("png").toFile(imageFile);
 			f.delete();
 
 			return imageFile;
 		} catch (Exception e) {
 			log.warn(this + ": Unable to download file", e);
 			hadErrors = true;
 		}
 		return null;
 
 	}
 
 	private static HttpParams httpParams = new BasicHttpParams();
 	static {
 		HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
 	}
 
 	private static DefaultHttpClient httpClient = new DefaultHttpClient(
 			new ThreadSafeClientConnManager(), httpParams);
 	public static final String LAST_REDIRECT_URL = "last_redirect_url";
 
 	static {
 		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
 			@Override
 			public void process(HttpResponse response, HttpContext context)
 					throws HttpException, IOException {
 				if (response.containsHeader("Location")) {
 					Header[] locations = response.getHeaders("Location");
 					if (locations.length > 0)
 						context.setAttribute(LAST_REDIRECT_URL,
 								locations[0].getValue());
 				}
 			}
 		});
 	}
 
 	private static String getUrlAfterRedirects(HttpContext context) {
 		String lastRedirectUrl = (String) context
 				.getAttribute(LAST_REDIRECT_URL);
 		if (lastRedirectUrl != null)
 			return lastRedirectUrl;
 		else {
 			HttpUriRequest currentReq = (HttpUriRequest) context
 					.getAttribute(ExecutionContext.HTTP_REQUEST);
 			HttpHost currentHost = (HttpHost) context
 					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
 			String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq
 					.getURI().toString() : (currentHost.toURI() + currentReq
 					.getURI());
 			return currentUrl;
 		}
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public String getImageUrl() {
 		return imageUrl;
 	}
 
 	public URL getUrlObj() {
 		try {
 			return new URL(url);
 		} catch (MalformedURLException e) {
 			log.debug("Malformed URL", e);
 			return null;
 		}
 	}
 
 	public Date getTweeted() {
 		return tweeted;
 	}
 
 	public void setImageUrl(String newUrl) {
 		imageUrl = newUrl;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	@Override
 	public String toString() {
 		return type + ": " + title + " (" + url + ")";
 	}
 
 	public boolean hadErrors() {
 		return hadErrors;
 	}
 
 	public boolean isImage() {
 		return this.type.equals(Type.image);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object toJSON() {
 		JSONObject msgObj = new JSONObject();
 		msgObj.put("type", type.toString());
 		msgObj.put("url", url);
 		msgObj.put("title", title);
 		msgObj.put("mediaUrl", imageUrl);
 		msgObj.put("tweeted", Utils.jsonDateFormat.format(tweeted));
 		msgObj.put("place", placeId);
 		return msgObj;
 	}
 
 	public void save() {
 		try {
 			Connection c = Utils.getConnection();
 			PreparedStatement s = c
 					.prepareStatement("INSERT DELAYED IGNORE INTO `urls` (`place`,`tweeted`,`url`,`type`,`title`,`mediaUrl`) VALUES (?,?,?,?,?,?)");
 
 			s.setInt(1, placeId);
 			s.setString(2, Utils.sqlDateTimeFormat.format(tweeted));
 			s.setString(3, getUrl());
 			s.setString(4, getType().toString());
 			s.setString(5, getTitle());
 			s.setString(6, getImageUrl());
 
 			s.executeUpdate();
 			s.close();
 			c.close();
 		} catch (SQLException e) {
 			log.warn("Failed to save web resource " + toString() + " to db", e);
 		}
 	}
 
 	private static final ExecutorService resolverThreadPool = Executors
 			.newFixedThreadPool(10);
 
 	public static Collection<WebResource> resolveLinks(List<Message> messages,
 			final Place p) {
 
 		List<Future<WebResource>> futures = new ArrayList<Future<WebResource>>();
 
 		for (final Message m : messages) {
 			for (final String url : m.findLinks()) {
 				futures.add(resolverThreadPool
 						.submit(new Callable<WebResource>() {
 							@Override
 							public WebResource call() throws Exception {
 								return new WebResource(url, p, m).resolve();
 							}
 						}));
 			}
 		}
 		Map<String, WebResource> resources = new HashMap<String, WebResource>();
 		do {
 			Iterator<Future<WebResource>> futureIterator = futures.iterator();
 			while (futureIterator.hasNext()) {
 				Future<WebResource> wrf = futureIterator.next();
 				if (wrf.isDone()) {
 					futureIterator.remove();
 					try {
 						WebResource wr = wrf.get();
 						if (wr == null) {
 							continue;
 						}
 						URL u = wr.getUrlObj();
 						if (u == null) {
 							continue;
 						}
 						if (!resources.containsKey(wr.getUrl())) {
 							resources.put(wr.getUrl(), wr);
 						}
 
 					} catch (Exception e) {
 						// should not happen, we have already checked if
 						// the
 						// result is ready.
 						e.printStackTrace();
 					}
 				}
 			}
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				log.warn("Unable to sleep");
 			}
 
 		} while (futures.size() > 0);
 		return resources.values();
 	}
 
 	// TODO: how to handle numImages?
 	public static List<WebResource> loadResources(Place p, int numLinks,
 			int numImages) {
 		List<WebResource> resources = new ArrayList<WebResource>();
 		Connection c = null;
 		PreparedStatement s = null;
 		ResultSet rs = null;
 		try {
			c = Utils.getConnection();			
 			s = c.prepareStatement("SELECT DISTINCT `place`,`tweeted`,`url`,`type`,`title`,`mediaUrl` FROM `urls` WHERE `place`=? ORDER BY `tweeted` DESC LIMIT ?;");
 			s.setInt(1, p.id);
 			s.setInt(2, numLinks);
 
 			rs = s.executeQuery();
 
 			while (rs.next()) {
 				try {
 					WebResource wr = new WebResource(rs.getInt("place"),
 							Utils.sqlDateTimeFormat.parse(rs
 									.getString("tweeted")),
 							rs.getString("url"), Type.valueOf(rs
 									.getString("type")), rs.getString("title"),
 							rs.getString("mediaUrl"));
 					resources.add(wr);
 				} catch (ParseException e) {
 					log.warn(e);
 				}
 			}
 
 		} catch (SQLException e) {
 			log.warn("Failed to run statement", e);
 		} finally {
 			try {
 				rs.close();
 			} catch (SQLException e) {
 				log.warn("Failed to clean up after statement", e);
 			}
 			try {
 				s.close();
 			} catch (SQLException e) {
 				log.warn("Failed to clean up after statement", e);
 			}
 			try {
 				c.close();
 			} catch (SQLException e) {
 				log.warn("Failed to clean up after statement", e);
 			}
 		}
 
 		return resources;
 	}
 }
