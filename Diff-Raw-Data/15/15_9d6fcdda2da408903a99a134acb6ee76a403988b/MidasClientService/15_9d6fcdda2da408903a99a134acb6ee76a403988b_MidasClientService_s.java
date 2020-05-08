 package com.robonobo.midas.client;
 
 import static com.robonobo.common.util.CodeUtil.*;
 import static com.robonobo.common.util.TextUtil.*;
 import static java.lang.Math.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.*;
 import org.apache.http.auth.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 import com.google.protobuf.AbstractMessage;
 import com.google.protobuf.GeneratedMessage;
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.Errot;
 import com.robonobo.common.http.PreemptiveHttpClient;
 import com.robonobo.common.serialization.*;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.metadata.*;
 
 public class MidasClientService extends AbstractMetadataService {
 	static final Pattern URL_PATTERN = Pattern.compile("^http://([\\w\\.]+?):?(\\d*)/.*$");
 	LinkedList<Request> requests = new LinkedList<Request>();
 	PreemptiveHttpClient http;
 	Log log = LogFactory.getLog(getClass());
 	private MidasClientConfig cfg;
 	AuthScope midasAuthScope;
 	int numThreads;
 	int runningTasks = 0;
 	ExecutorService executor;
 	long nextFetchTaskId = 1;
 	Map<Long, FetchTask> fetchTasks = new HashMap<Long, MidasClientService.FetchTask>();
 	boolean stopped = false;
 
 	public MidasClientService() {
 		addHardDependency("core.http");
 	}
 
 	@Override
 	public String getName() {
 		return "Midas Metadata Service";
 	}
 
 	@Override
 	public void startup() throws Exception {
 		String baseUrl = rbnb.getConfig().getMidasUrl();
 		Matcher m = URL_PATTERN.matcher(baseUrl);
 		if (!m.matches())
 			throw new Errot("midas url " + baseUrl + "does not match expected pattern");
 		cfg = new MidasClientConfig(baseUrl);
 		String midasHost = m.group(1);
 		String portStr = m.group(2);
 		int midasPort;
 		// Note that the httpclient preemptive auth breaks if we set this to 80, instead we have to use -1 :-P :-P :-P
 		if (isEmpty(portStr) || portStr.equals("80"))
 			midasPort = -1;
 		else
 			midasPort = Integer.parseInt(portStr);
		log.debug("Setting up midas http auth scope on " + midasHost + ":" + midasPort);
 		midasAuthScope = new AuthScope(midasHost, midasPort);
 		// Run midas requests in a different thread pool
 		numThreads = rbnb.getConfig().getMidasThreadPoolSize();
 		executor = Executors.newFixedThreadPool(numThreads);
 		http = rbnb.getHttpService().getClient();
 		// Initially we handle requests serially - makes for a better ux to have the playlists load one at a time rather
 		// than a big pause then all at once
 		fetchOrder = RequestFetchOrder.Serial;
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		stopped = true;
 		executor.shutdownNow();
 	}
 
 	@Override
 	public void setCredentials(String username, String password) {
 		// Set these details on our http client
 		http.getCredentialsProvider().setCredentials(midasAuthScope, new UsernamePasswordCredentials(username, password));
 		// Tell our fetchers to refresh their httpcontexts (which contain cached creds)
 		synchronized (this) {
 			for (FetchTask ft : fetchTasks.values()) {
 				ft.refreshContext = true;
 			}
 		}
 	}
 
 	@Override
 	public void fetchStreams(Collection<String> sids, StreamCallback callback) {
 		addRequest(new GetStreamRequest(cfg, sids, callback));
 	}
 
 	@Override
 	public void putStream(Stream s, StreamCallback callback) {
 		addRequest(new PutStreamRequest(cfg, s, callback));
 	}
 
 	@Override
 	public void fetchUser(long userId, UserCallback callback) {
 		addRequest(new GetUsersRequest(cfg, userId, callback));
 	}
 
 	@Override
 	public void fetchUsers(Collection<Long> userIds, UserCallback callback) {
 		addRequest(new GetUsersRequest(cfg, userIds, callback));
 	}
 
 	@Override
 	public void fetchUserForLogin(String email, String password, UserCallback callback) {
 		addRequest(new LoginRequest(cfg, email, password, callback));
 	}
 
 	@Override
 	public void fetchUserConfig(long userId, UserConfigCallback callback) {
 		addRequest(new GetUserConfigRequest(cfg, userId, callback));
 	}
 
 	@Override
 	public void updateUserConfig(UserConfig uc, UserConfigCallback callback) {
 		addRequest(new PutUserConfigRequest(cfg, uc, callback));
 	}
 
 	@Override
 	public void fetchPlaylist(long playlistId, PlaylistCallback callback) {
 		addRequest(new GetPlaylistRequest(cfg, playlistId, callback));
 	}
 
 	@Override
 	public void fetchPlaylists(Collection<Long> playlistIds, PlaylistCallback callback) {
 		addRequest(new GetPlaylistRequest(cfg, playlistIds, callback));
 	}
 
 	@Override
 	public void updatePlaylist(Playlist p, PlaylistCallback callback) {
 		addRequest(new PutPlaylistRequest(cfg, p, callback));
 	}
 
 	@Override
 	public void postPlaylistUpdateToService(String service, long playlistId, String msg, PlaylistCallback callback) {
 		addRequest(new PlaylistServiceUpdateRequest(cfg, service, playlistId, msg, callback));
 	}
 
 	@Override
 	public void deletePlaylist(Playlist p, PlaylistCallback callback) {
 		addRequest(new DeletePlaylistRequest(cfg, p.getPlaylistId(), callback));
 	}
 
 	@Override
 	public void sharePlaylist(Playlist p, Collection<Long> shareFriendIds, Collection<String> friendEmails, PlaylistCallback callback) {
 		addRequest(new SharePlaylistRequest(cfg, p.getPlaylistId(), shareFriendIds, friendEmails, callback));
 	}
 
 	@Override
 	public void addFriends(Collection<String> friendEmails, GeneralCallback callback) {
 		addRequest(new AddFriendsRequest(cfg, friendEmails, callback));
 	}
 	
 	@Override
 	public void fetchLibrary(long userId, Date lastUpdated, LibraryCallback callback) {
 		addRequest(new GetLibraryRequest(cfg, userId, lastUpdated, callback));
 	}
 
 	@Override
 	public void addToLibrary(long userId, Library addedLib, LibraryCallback callback) {
 		addRequest(new AddToLibraryRequest(cfg, userId, addedLib, callback));
 	}
 
 	@Override
 	public void deleteFromLibrary(long userId, Library delLib, LibraryCallback callback) {
 		addRequest(new DeleteFromLibraryRequest(cfg, userId, delLib, callback));
 	}
 
 	@Override
 	public void search(String query, int firstResult, SearchCallback callback) {
 		addRequest(new SearchRequest(cfg, query, firstResult, callback));
 	}
 
 	private synchronized void addRequest(Request r) {
 		// Add to the front for best responsiveness
 		requests.addFirst(r);
 		int numToStart = min(r.remaining(), (numThreads - runningTasks));
 		runningTasks += numToStart;
		log.debug("Midas client added request with " + r.remaining() + " urls remaining: starting " + numToStart + " fetchers");
 		for (int i = 0; i < numToStart; i++) {
 			executor.execute(new FetchTask());
 		}
 	}
 
 	private void getFromUrl(HttpContext context, AbstractMessage.Builder<?> bldr, String url, String un, String pwd) throws IOException, SerializationException {
 		log.debug("Getting object from " + url);
 		HttpGet get = new HttpGet(url);
 		Credentials restoreCreds = null;
 		// If we are being supplied with a username & password, store our old creds and restore them afterwards, and use
 		// a different httpcontext for this request to avoid using cached auth
 		if (un != null) {
 			restoreCreds = http.getCredentialsProvider().getCredentials(midasAuthScope);
 			http.getCredentialsProvider().setCredentials(midasAuthScope, new UsernamePasswordCredentials(un, pwd));
 		}
 		HttpEntity body = null;
 		try {
 			HttpResponse resp = http.execute(get, context);
 			body = resp.getEntity();
 			switch (resp.getStatusLine().getStatusCode()) {
 			case 200:
 				if (bldr != null) {
 					InputStream is = body.getContent();
 					try {
 						bldr.mergeFrom(is);
 					} finally {
 						is.close();
 					}
 				}
 				return;
 			case 404:
 				throw new ResourceNotFoundException("Server could not find resource for " + url);
 			case 401:
 				throw new UnauthorizedException("Server did not allow us to access url " + url + " with supplied credentials");
 			case 500:
 				throw new IOException("Unable to get object from url '" + url + "', server said: " + EntityUtils.toString(body));
 			default:
 				throw new IOException("Url '" + url + "' replied with status " + resp.getStatusLine().getStatusCode());
 			}
 		} finally {
 			if (restoreCreds != null)
 				http.getCredentialsProvider().setCredentials(midasAuthScope, restoreCreds);
 			if (body != null)
 				EntityUtils.consume(body);
 		}
 	}
 
 	private void putToUrl(HttpContext context, GeneratedMessage msg, String url, AbstractMessage.Builder bldr) throws IOException {
 		log.debug("Putting object to " + url);
 		HttpPut put = new HttpPut(url);
 		put.setEntity(new ByteArrayEntity(msg.toByteArray()));
 		HttpEntity body = null;
 		try {
 			HttpResponse resp = http.execute(put, context);
 			body = resp.getEntity();
 			switch (resp.getStatusLine().getStatusCode()) {
 			case 200:
 				if (bldr != null) {
 					InputStream is = body.getContent();
 					try {
 						bldr.mergeFrom(is);
 					} finally {
 						is.close();
 					}
 				}
 				return;
 			default:
 				throw new IOException("Server replied with status " + resp.getStatusLine().getStatusCode() + ": " + EntityUtils.toString(body));
 			}
 		} finally {
 			if (body != null)
 				EntityUtils.consume(body);
 		}
 	}
 
 	private void deleteAtUrl(HttpContext context, String url) throws IOException {
 		log.debug("Deleting object at " + url);
 		HttpDelete del = new HttpDelete(url);
 		HttpEntity body = null;
 		try {
 			HttpResponse resp = http.execute(del, context);
 			body = resp.getEntity();
 			if (resp.getStatusLine().getStatusCode() != 200)
 				throw new IOException("Failed to delete object at " + url + ", status code was " + resp.getStatusLine().getStatusCode());
 		} finally {
 			if (body != null)
 				EntityUtils.consume(body);
 		}
 	}
 
 	class FetchTask extends CatchingRunnable {
 		long taskId;
 		HttpContext context;
 		boolean refreshContext = true;
 
 		public FetchTask() {
 			synchronized (MidasClientService.this) {
 				taskId = nextFetchTaskId++;
 				fetchTasks.put(taskId, this);
 			}
 		}
 
 		@Override
 		public String toString() {
 			return "Midas fetcher " + taskId;
 		}
 
 		@Override
 		public void doRun() throws Exception {
			log.debug(this + " starting");
 			while (true) {
 				if (refreshContext) {
					log.debug(this + " refreshing httpcontext");
 					context = http.newPreemptiveContext(new HttpHost(midasAuthScope.getHost(), midasAuthScope.getPort()));
 					refreshContext = false;
 				}
 				Request r;
 				Params p;
 				synchronized (MidasClientService.this) {
 					if (requests.size() == 0) {
 						runningTasks--;
						log.debug(this + " stopping");
 						fetchTasks.remove(taskId);
 						return;
 					}
 					r = requests.removeFirst();
 					p = r.getNextParams();
 					if (p == null)
 						continue;
 					if (r.remaining() > 0) {
 						if (fetchOrder == RequestFetchOrder.Serial)
 							requests.addFirst(r);
 						else
 							requests.addLast(r); // Parallel
 					}
 				}
				log.debug(this + " running " + p.op + " for " + p.url);
 				try {
 					switch (p.op) {
 					case Get:
 						// If we are passing different credentials, use a different httpcontext to avoid reusing the old
 						// ones
 						HttpContext c = context;
 						if (p.username != null)
 							c = http.newPreemptiveContext(new HttpHost(midasAuthScope.getHost(), midasAuthScope.getPort()));
 						getFromUrl(c, p.resultBldr, p.url, p.username, p.password);
 						break;
 					case Put:
 						putToUrl(context, p.sendMsg, p.url, p.resultBldr);
 						break;
 					case Delete:
 						deleteAtUrl(context, p.url);
 						break;
 					}
					log.debug(this + " ran successful " + p.op + " for " + p.url);
 					if (p.resultBldr == null)
 						r.success(null);
 					else
 						r.success(p.resultBldr.build());
 				} catch (Exception e) {
 					if(stopped)
 						return;
					log.debug(this + " caught " + shortClassName(e.getClass()) + " running " + p.op + " for " + p.url + " with msg: " + e.getMessage());
 					r.error(p, e);
 				}
 			}
 		}
 	}
 }
