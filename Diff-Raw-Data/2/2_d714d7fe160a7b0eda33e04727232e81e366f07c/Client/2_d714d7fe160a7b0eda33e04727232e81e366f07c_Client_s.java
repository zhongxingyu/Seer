 package com.sandwich.client;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.Inet6Address;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import com.sandwich.PeerList;
 import com.sandwich.Settings;
 import com.sandwich.client.PeerSet.Peer;
 import com.sandwich.player.MediaMimeInfo;
 
 import android.annotation.TargetApi;
 import android.app.DownloadManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDoneException;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteStatement;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Environment;
 
 public class Client {
 	private Context context;
 	PeerSet peers;
 	
 	private AtomicBoolean killSearch, killDownload;
 	
 	private HashMap<PeerSet.Peer, SQLiteDatabase> peerDatabases;
 	
 	// Accessed by child tasks
 	ConcurrentHashMap<PeerSet.Peer, Runnable> downloadTasks;
 	ConcurrentHashMap<PeerSet.Peer, Runnable> searchTasks;
 	SQLiteDatabase database;
 	
 	private ThreadPoolExecutor searchPool;
 	private ThreadPoolExecutor downloadPool;
 	
 	private static final String CREATE_PEER_TABLE = "CREATE TABLE IF NOT EXISTS peers (IP TEXT PRIMARY KEY, IndexHash INT, LastSeen TEXT);";
 
 	static final String PEER_TABLE = "peers";
 	static final String PEER_DB = "peers.db";
 	
 	public Client(Context context)
 	{
 		this.context = context;
 		this.peerDatabases = new HashMap<PeerSet.Peer, SQLiteDatabase>();
 		this.peers = new PeerSet();
 		this.killSearch = new AtomicBoolean();
 		this.killDownload = new AtomicBoolean();
 		this.searchPool = new ThreadPoolExecutor(1, Integer.MAX_VALUE, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>());
 		this.downloadPool = new ThreadPoolExecutor(1, Integer.MAX_VALUE, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>());
 		this.downloadTasks = new ConcurrentHashMap<PeerSet.Peer, Runnable>();
 		this.searchTasks = new ConcurrentHashMap<PeerSet.Peer, Runnable>();
 	}
 	
 	// Holy fucking shit
 	private static int unsign(byte signed)
 	{
 		if (signed >= 0)
 			return signed;
 		else
 			return signed+256;
 	}
 	
 	public Set<Peer> getPeerSet()
 	{
 		HashSet<Peer> peers = new HashSet<Peer>();
 		Iterator<Peer> i = this.peers.getPeerListIterator();
 		
 		while (i.hasNext())
 		{
 			peers.add(i.next());
 		}
 		
 		return peers;
 	}
 	
 	private void checkNetworkOK()
 	{
 		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo active = mgr.getActiveNetworkInfo();
 		
 		if (!isNetworkActive())
 		{
 			throw new IllegalStateException("No network connection");
 		}
 		
 		if (active != null)
 		{
 			// Everything but these types are "mobile data"
 			if (active.getType() != ConnectivityManager.TYPE_WIFI &&
 				active.getType() != ConnectivityManager.TYPE_BLUETOOTH &&
 				active.getType() != ConnectivityManager.TYPE_ETHERNET)
 			{
 				// If mobile data is disabled, don't let them continue
 				if (!Settings.isMobileDataEnabled(context))
 				{
 					throw new IllegalStateException("Mobile data disabled");
 				}
 			}
 		}
 	}
 	
 	private boolean isNetworkActive()
 	{
 		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo active = mgr.getActiveNetworkInfo();
 
 		return (active != null && active.isConnected());
 	}
 	
 	public static int getPortNumberFromIPAddress(InetAddress address) throws NoSuchAlgorithmException
 	{
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		byte[] hash;
 		byte[] addr;
 		int port;
 		
 		// We need an IPv6 address in all cases
 		if (address instanceof Inet4Address)
 		{
 			// If it's an IPv4 address, we need to add 2 octets of 255
 			addr = new byte[address.getAddress().length+12];
 			
 			// Copy the IPv4 portion over at the 12th octet
 			System.arraycopy(address.getAddress(), 0, addr, 12, address.getAddress().length);
 			
 			// Copy the FF octets from the 10th octet
 			addr[10] = (byte) 0xFF;
 			addr[11] = (byte) 0xFF;
 		}
 		else if (address instanceof Inet6Address)
 		{
 			// It's fine the way it is
 			addr = address.getAddress();
 		}
 		else
 		{
 			throw new IllegalArgumentException("Not an IPv4 or IPv6 address");
 		}
 		
 		// Compute the MD5 of the address in byte form
 		hash = md.digest(addr);
 		
 		// Create the port number by bit-shifting the hash
 		port = (unsign(hash[0]) + unsign(hash[3])) << 8;
 		port += unsign(hash[1]) + unsign(hash[2]);
 		port &= 0xFFFF;
 		
 		// Keep the port number of out the sub-1024 range
 		if (port < 1024)
 			port += 1024;
 		
 		return port;
 	}
 	
 	static HttpURLConnection createHttpConnection(URL url, boolean useCaches) throws IOException
 	{
 		HttpURLConnection conn;
 		
 		System.out.println("Connecting to "+url.toExternalForm());
 		conn = (HttpURLConnection) url.openConnection();
 		
 		// Set cache options
 		conn.setUseCaches(useCaches);
 		
 		// Set timeouts of 5 seconds
 		conn.setConnectTimeout(5000);
 		conn.setReadTimeout(5000);
 		
 		return conn;
 	}
 	
 	private SQLiteDatabase getPeerDatabase(PeerSet.Peer peer)
 	{
 		synchronized (peerDatabases) {
 			if (peerDatabases.containsKey(peer))
 				return peerDatabases.get(peer);
 			
 			SQLiteDatabase db = context.openOrCreateDatabase(peer.getIpAddress(), Context.MODE_PRIVATE, null);
 			peerDatabases.put(peer, db);
 			return db;
 		}
 	}
 	
 	private static int sendGetRequest(HttpURLConnection conn) throws IOException
 	{		
 		// Send the get request
 		conn.setRequestMethod("GET");
 
 		// Wait for the response
 		return conn.getResponseCode();
 	}
 	
 	static String createPeerUrlString(String peer, String path, String parameters) throws URISyntaxException, UnknownHostException, NoSuchAlgorithmException
 	{
 		return new URI("http", null, peer, getPortNumberFromIPAddress(Inet6Address.getByName(peer)), path, parameters, null).toString();
 	}
 	
 	static URL createQueryUrl(URL peerUrl, String querySuffix) throws MalformedURLException
 	{
 		String urlString = peerUrl.toExternalForm();
 		
 		// If it doesn't end with a slash, normalize it by adding a slash
 		if (!urlString.endsWith("/"))
 			urlString += "/";
 		
 		// Now append the query suffix
 		urlString += querySuffix;
 		
 		return new URL(urlString);
 	}
 	
 	static InputStream getInputStreamFromConnection(HttpURLConnection conn) throws IOException, ClientForbiddenException
 	{
 		int responseCode;
 
 		// Send the GET request and get the response code
 		responseCode = sendGetRequest(conn);
 		if (responseCode == HttpURLConnection.HTTP_FORBIDDEN)
 		{
 			throw new ClientForbiddenException();
 		}
 		else if (responseCode != HttpURLConnection.HTTP_OK)
 		{
 			throw new ConnectException("Failed to get peer list from bootstrap peer: "+responseCode);
 		}
 		
 		return conn.getInputStream();
 	}
 	
 	private PeerSet getPeerList(URL bootstrapUrl) throws IOException, JSONException, ClientForbiddenException
 	{
 		URL queryUrl;
 		Scanner in;
 		PeerSet peerSet;
 		JSONArray jsonPeerList;
 		HttpURLConnection conn;
 		String json;
 		
 		// Build the query peer list URL
 		queryUrl = createQueryUrl(bootstrapUrl, "peerlist");
 		System.out.println("Getting peerlist from "+queryUrl.toExternalForm());
 		
 		// Create a URL connection to the peer
 		conn = Client.createHttpConnection(queryUrl, false);
 		
 		// Get an input stream from the GET request on this URL
 		in = new Scanner(getInputStreamFromConnection(conn));
 		
 		// Read the JSON response
 		json = "";
 		while (in.hasNext())
 		{
 			json += in.next();
 		}
 		
 		// Close the scanner and the connection
 		in.close();
 		conn.disconnect();
 		
 		jsonPeerList = new JSONArray(json);
 		peerSet = new PeerSet();
 		for (int i = 0; i < jsonPeerList.length(); i++)
 		{
 			JSONObject jsonPeer = jsonPeerList.getJSONObject(i);
 
 			peerSet.updatePeer(jsonPeer.getString("IP"), jsonPeer.getString("LastSeen"), jsonPeer.getLong("IndexHash"), Peer.STATE_UNKNOWN);
 		}
 		
 		return peerSet;
 	}
 	
 	private PeerSet getPeerSetFromDatabase(SQLiteDatabase database) throws SQLiteException
 	{
 		PeerSet peers = new PeerSet();
 		Cursor c = database.query(PEER_TABLE, new String[] {"IP", "IndexHash", "LastSeen"}, null, null, null, null, null, null);
 		
 		c.moveToFirst();
 		while (!c.isAfterLast())
 		{
 			peers.updatePeer(c.getString(0), c.getString(2), c.getLong(1), Peer.STATE_UNKNOWN);
 			c.moveToNext();
 		}
 		
 		c.close();
 		
 		return peers;
 	}
 	
 	boolean deletePeerFromDatabase(PeerSet.Peer peer) throws SQLiteException
 	{
 		boolean ret = false;
 		
 		// Delete them from the peer table first
 		try {
 			ret = database.delete(PEER_TABLE, "IP=?", new String[] {peer.getIpAddress()}) > 0;
 		} catch (SQLiteException e) {
 			// It's ok for this to fail
 		}
 		
 		// Delete their index database
 		try {
 			getPeerDatabase(peer).execSQL("DROP TABLE IF EXISTS"+getTableNameForPeer(peer));
 		} catch (SQLiteException e) {
 			// It's ok for this to fail
 		}
 		
 		return ret;
 	}
 	
 	static String getTableNameForPeer(PeerSet.Peer peer)
 	{
 		return '[' + peer.getIpAddress() + ']';
 	}
 	
 	long getOldHashOfPeerIndex(PeerSet.Peer peer)
 	{
 		Cursor c = database.query(PEER_TABLE, new String[] {"IP", "IndexHash"}, null, null, null, null, null, null);
 		
 		c.moveToFirst();
 		while (!c.isAfterLast())
 		{
 			if (c.getString(0).equals(peer.getIpAddress()))
 			{
 				String hash = c.getString(1);
 
 				c.close();
 				return Long.parseLong(hash, 10);
 			}
 
 			c.moveToNext();
 		}
 
 		c.close();
 		return -1;
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public void initialize()
 	{
 		// Open or create it
 		database = context.openOrCreateDatabase(PEER_DB, Context.MODE_PRIVATE, null);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Enable write-ahead logging if possible
 			database.enableWriteAheadLogging();
 		}
 		
 		// Create our table
 		database.execSQL(CREATE_PEER_TABLE);
 		
 		// Setup the database to be fast
 		database.execSQL("PRAGMA synchronous=OFF");
 		database.execSQL("PRAGMA count_changes=OFF");
 		database.execSQL("PRAGMA temp_store=MEMORY");
 	}
 	
 	public void release()
 	{
 		// Kill search threads
 		endSearch();
 		
 		// Kill bootstrap threads
 		endBootstrap();
 		
 		// Wait for search to finish
 		waitForSearch();
 		
 		// Wait for bootstrap to finish
 		waitForBootstrap();
 		
 		// Close all the peer databases
 		synchronized (peerDatabases) {
 			for (SQLiteDatabase db : peerDatabases.values())
 				db.close();
 		}
 		
 		// Close the peer list db
 		if (database != null)
 			database.close();
 	}
 	
 	private boolean readCachedBootstrapData()
 	{
 		boolean success;
 
 		System.out.println("Loading cached bootstrap data");
 		
 		try {
 			// Try to read the peer set from the database
 			peers.updatePeerSet(getPeerSetFromDatabase(database));
 			success = peers.getPeerListLength() != 0;
 		} catch (SQLiteException e) {
 			// An exception here means that the SQL database was probably corrupt
 			System.err.println("Rebuilding peer table");
 			database.execSQL("DROP TABLE IF EXISTS "+PEER_TABLE);
 			database.execSQL(CREATE_PEER_TABLE);
 			
 			// Failed to read peer set
 			success = false;
 		}
 
 		return success;
 	}
 	
 	public boolean bootstrapFromCache()
 	{
 		// Just read the bootstrap data in from the database
 		return readCachedBootstrapData();
 	}
 	
 	private void downloadPeerList(String initialPeer) throws NoSuchAlgorithmException, URISyntaxException, IOException, JSONException
 	{
 		URL bootstrapUrl;
 		Random rand = new Random();
 		ArrayList<PeerSet.Peer> availablePeers = new ArrayList<PeerSet.Peer>(peers.getPeerListLength());
 		
 		checkNetworkOK();
 		
 		System.out.println("Downloading the peer list");
 		
 		// Add peers to our temporary list
 		Iterator<PeerSet.Peer> iterator = peers.getPeerListIterator();
 		while (iterator.hasNext())
 			availablePeers.add(iterator.next());
 		
 		// Bootstrap from a random peer
 		rand.setSeed(System.currentTimeMillis());
 		while (availablePeers.size() > 0)
 		{
 			// Generate a random peer to bootstrap from
 			int nextPeer = rand.nextInt(availablePeers.size());
 			System.out.println("Bootstrapping from peer index: "+nextPeer);
 			
 			// Get the random peer
 			PeerSet.Peer selectedPeer = availablePeers.get(nextPeer);
 				
 			try {
 				// Resolve address and create a URL
 				bootstrapUrl = new URL(createPeerUrlString(selectedPeer.getIpAddress(), null, null));
 				
 				// Download the peer list
 				peers.updatePeerSet(getPeerList(bootstrapUrl));
 				
 				// If we get here, bootstrapping was successful
 				return;
 			} catch (ClientForbiddenException e) {
 				System.err.println("Peer list download forbidden for peer: "+selectedPeer.getIpAddress());
 				selectedPeer.setState(Peer.STATE_UPDATE_FORBIDDEN);
 				peers.updatePeer(selectedPeer);
 				PeerList.updateListView();
 			} catch (Exception e) {
 				// Make sure the network is available
 				if (!isNetworkActive())
 				{
 					throw new IllegalStateException("No network connection available");
 				}
 				
 				// Update failed
 				selectedPeer.setState(Peer.STATE_UPDATE_FAILED);
 				peers.updatePeer(selectedPeer);
 				PeerList.updateListView();
 				
 				// Failed to connect to this one, so prune it
 				selectedPeer.remove();
 				
 				// Drop it from the database
 				deletePeerFromDatabase(selectedPeer);
 			}
 			
 			// Something failed if we got here, so remove them from our available peer list
 			availablePeers.remove(selectedPeer);
 		}
 
 		// Try the initial peer if all else fails
 		try {
 			System.out.println("Trying initial peer: "+initialPeer);
 			
 			// Resolve address and create a URL
 			bootstrapUrl = new URL(createPeerUrlString(initialPeer, null, null));
 			
 			// Download the peer list
 			peers.updatePeerSet(getPeerList(bootstrapUrl));
 			
 			// It worked
 			return;
 		} catch (Exception e) {
 			// Failed to connect to the initial peer
 		}
 		
 		// We're out of people to bootstrap from
 		throw new IllegalStateException("No peers online");
 	}
 	
 	public void waitForBootstrap()
 	{
 		try {
 			downloadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
 		} catch (InterruptedException e) { }
 	}
 	
 	public void endBootstrap()
 	{
 		for (Map.Entry<PeerSet.Peer, Runnable> entries : downloadTasks.entrySet())
 		{
 			// Remove all entries we can
 			if (downloadPool.remove(entries.getValue())) {
 				// Task didn't run, so we need to remove it from our search tasks map
 				downloadTasks.remove(entries.getKey());
 			}
 		}
 		
 		// Kill tasks in progress
 		killDownload.set(true);
 		
 		// Purge the pool
 		downloadPool.purge();
 	}
 	
 	public static String getUriForResult(Peer peer, ResultListener.Result result) throws UnknownHostException, NoSuchAlgorithmException, URISyntaxException
 	{
 		return getPeerUrlForFile(peer, result.result);
 	}
 	
 	public static boolean isResultStreamable(ResultListener.Result result)
 	{
 		String mimeType;
 
 		mimeType = MediaMimeInfo.getMimeTypeForPath(result.result);
 		if (mimeType == null)
 		{
 			// Undetermined MIME type
 			return false;
 		}
 		else if (mimeType.startsWith("audio/"))
 		{
 			// Streamable via the audio player
 			return true;
 		}
 		else if (mimeType.startsWith("video/"))
 		{
 			// Streamable via the video player
 			return true;
 		}
 		else
 		{
 			// No player for this
 			return false;
 		}
 	}
 	
 	public int bootstrapFromNetwork(String initialPeer, IndexDownloadListener listener, Set<String> blacklist) throws IOException, JSONException, InterruptedException, NoSuchAlgorithmException, URISyntaxException
 	{
 		Iterator<PeerSet.Peer> iterator;
 		int downloads = 0;
 
 		// Download the peer list
 		downloadPeerList(initialPeer);
 		
 		// Stop killing downloads
 		killDownload.set(false);
 		
 		// Iterate the peer list and download indexes for each
 		iterator = peers.getPeerListIterator();
 		while (iterator.hasNext())
 		{
 			PeerSet.Peer peer = iterator.next();
 			long oldHash;
 			
 			// Check if an updater is already running for this peer
 			if (!downloadTasks.containsKey(peer))
 			{
 				// Check if the peer is blacklisted
 				if (blacklist.contains(peer.getIpAddress()))
 				{
 					System.out.println(peer.getIpAddress()+" is blacklisted");
 					
 					// Index is going away
 					peer.updateIndexHash(0);
 					
 					// Peer is blacklisted
 					peer.setState(Peer.STATE_BLACKLISTED);
 					
 					// Create an empty table for them
 					getPeerDatabase(peer).execSQL("DROP TABLE IF EXISTS "+Client.getTableNameForPeer(peer)+";");
 					getPeerDatabase(peer).execSQL("CREATE TABLE "+Client.getTableNameForPeer(peer)+" (FileName TEXT PRIMARY KEY COLLATE NOCASE, Size INTEGER, CheckSum INTEGER);");
 					
 					// Create the values to be stored in the SQL database
 					ContentValues vals = new ContentValues();
 					vals.put("IP", peer.getIpAddress());
 					vals.put("IndexHash", peer.getIndexHash());
 					vals.put("LastSeen", peer.getTimestamp());
 
 					// Update the peer entry in the database
 					database.insertWithOnConflict(Client.PEER_TABLE, null, vals, SQLiteDatabase.CONFLICT_REPLACE);
 					
 					// Don't download anything
 					continue;
 				}
 				
 				// Check if the old index hash is valid
 				oldHash = getOldHashOfPeerIndex(peer);
 				if (oldHash == peer.getIndexHash())
 				{
 					// Peer is up to date
 					peer.setState(Peer.STATE_UP_TO_DATE);
 					
 					System.out.println(peer.getIpAddress()+" index is up to date (hash: "+peer.getIndexHash()+")");
 					
 					// Don't download anything
 					continue;
 				}
 				
 				// No updater running and index is not up to date
 				peer.setState(Peer.STATE_UPDATING);
 				
 				Runnable task = new IndexDownloadTask(this, getPeerDatabase(peer), peer, listener, killDownload);
 				downloadTasks.put(peer, task);
 				downloadPool.execute(task);
 				downloads++;
 			}
 		}
 		
 		// Update the list view
 		PeerList.updateListView();
 		
 		return downloads;
 	}
 	
 	public void waitForSearch()
 	{
 		try {
 			searchPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
 		} catch (InterruptedException e) { }
 	}
 	
 	public void endSearch()
 	{		
 		for (Map.Entry<PeerSet.Peer, Runnable> entries : searchTasks.entrySet())
 		{
 			// Remove all entries we can
 			if (searchPool.remove(entries.getValue())) {
 				// Task didn't run, so we need to remove it from our search tasks map
 				searchTasks.remove(entries.getKey());
 			}
 		}
 		
 		// Kill tasks in progress
 		killSearch.set(true);
 		
 		// Purge the pool
 		searchPool.purge();
 	}
 	
 	public int getPeerCount()
 	{
 		return peers.getPeerListLength();
 	}
 	
 	public boolean getPeerIndex(String peerIp, ResultListener listener)
 	{
 		if (peers == null)
 		{
 			throw new IllegalStateException("Not bootstrapped");
 		}
 		
 		// Stop killing searches
 		killSearch.set(false);
 		
 		// Start search threads for each peer
 		Iterator<PeerSet.Peer> peerIterator = peers.getPeerListIterator();
 		while (peerIterator.hasNext())
 		{
 			PeerSet.Peer peer = peerIterator.next();
 			
 			// Check if this is the peer we want
 			if (peerIp.equals(peer.getIpAddress()))
 			{
 				// Start the search thread
 				System.out.println("Queueing search task to search "+peer.getIpAddress());
 				Runnable task = new SearchTask(this, getPeerDatabase(peer), peer, listener, null, killSearch);
 				searchTasks.put(peer, task);
 				searchPool.execute(task);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public int beginSearch(String query, ResultListener listener) throws IOException
 	{
 		int searches = 0;
 		
 		if (peers == null)
 		{
 			throw new IllegalStateException("Not bootstrapped");
 		}
 		
 		// Stop killing searches
 		killSearch.set(false);
 		
 		// Start search threads for each peer
 		Iterator<PeerSet.Peer> peerIterator = peers.getPeerListIterator();
 		while (peerIterator.hasNext())
 		{
 			PeerSet.Peer peer = peerIterator.next();
 
 			// Start the search thread
 			System.out.println("Queueing search task to search "+peer.getIpAddress());
			Runnable task = new SearchTask(this, getPeerDatabase(peer), peer, listener, null, killSearch);
 			searchTasks.put(peer, task);
 			searchPool.execute(task);
 			searches++;
 		}
 		
 		return searches;
 	}
 
 	@TargetApi(11)
 	public void startFileDownloadFromPeer(Peer peer, String file) throws URISyntaxException, UnknownHostException, NoSuchAlgorithmException
 	{
 		DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
 		DownloadManager.Request request;
 		String title;
 		String url;
 		
 		// Make a title from the last component of the file path
 		title = file;
 		if (title.lastIndexOf("/") > 0)
 			title = title.substring(title.lastIndexOf("/")+1);
 
 		url = getPeerUrlForFile(peer, file);
 		
 		System.out.println("Downloading URL: "+url+" ("+title+")");
 
 		request = new DownloadManager.Request(Uri.parse(url));
 		
 		// Download to the external downloads folder
 		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
 		
 		// Force download over WiFi if mobile data is disabled
 		if (!Settings.isMobileDataEnabled(context)) {
 			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
 		}
 		
 		// DownloadManager was enhanced with Honeycomb with useful features we want to activate
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Allow the media scanner to pick this file up
 			request.allowScanningByMediaScanner();
 		
 			// Workaround for issue 28015 (security exception when using VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
 			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2)
 			{
 				// Continue showing the notification even after the download finishes
 				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 			}
 		}
 		
 		// Give it our title
 		request.setTitle(title);
 		
 		// Fire the download
 		downloader.enqueue(request);
 	}
 	
 	public static String getPeerUrlForFile(Peer peer, String file) throws UnknownHostException, NoSuchAlgorithmException, URISyntaxException
 	{
 		return createPeerUrlString(peer.getIpAddress(), "/files/"+file, null);
 	}
 	
 	public boolean startFileStreamFromPeer(Context context, Peer peer, String file) throws NoSuchAlgorithmException, URISyntaxException, IOException
 	{
 		String url;
 		String mimeType;
 		
 		checkNetworkOK();
 
 		mimeType = MediaMimeInfo.getMimeTypeForPath(file);
 		url = getPeerUrlForFile(peer, file);
 		if (mimeType == null)
 		{
 			// Undetermined MIME type
 			return false;
 		}
 		else if (mimeType.startsWith("audio/"))
 		{
 			// Create the audio player activity
 			Intent i = new Intent(context, com.sandwich.AudioPlayer.class);
 			i.putExtra("URL", url);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(i);
 		}
 		else if (mimeType.startsWith("video/"))
 		{
 			// Create the video player activity
 			Intent i = new Intent(context, com.sandwich.VideoPlayer.class);
 			i.putExtra("URL", url);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(i);
 		}
 		else
 		{
 			// No player for this
 			return false;
 		}
 		
 		return true;
 	}
 }
 
 class IndexDownloadTask implements Runnable {
 	private SQLiteDatabase peerindex;
 	private PeerSet.Peer peer;
 	private Client client;
 	private IndexDownloadListener listener;
 	private AtomicBoolean interrupt;
 	
 	public IndexDownloadTask(Client client, SQLiteDatabase peerindex, PeerSet.Peer peer, IndexDownloadListener listener, AtomicBoolean interrupt)
 	{
 		this.client = client;
 		this.peerindex = peerindex;
 		this.peer = peer;
 		this.listener = listener;
 		this.interrupt = interrupt;
 	}
 
 	@Override
 	public void run() {
 		URL queryUrl;
 		HttpURLConnection conn;
 		InputStream in;
 		long indexHash;
 		String timeStamp;
 		boolean gotIndexHash;
 		
 		conn = null;
 		in = null;
 		timeStamp = null;
 		indexHash = 0;
 		gotIndexHash = false;
 		
 		// Drop the tables and rows for this peer
 		client.deletePeerFromDatabase(peer);
 		try {
 			// Build the query index URL
 			queryUrl = new URL(Client.createPeerUrlString(peer.getIpAddress(), "/fileindex", null));
 			
 			// Connect a URL connection
 			conn = Client.createHttpConnection(queryUrl, false);
 			
 			// Get an input stream from the GET request on this URL
 			in = Client.getInputStreamFromConnection(conn);
 			
 			// Create the required table
 			peerindex.execSQL("CREATE TABLE IF NOT EXISTS "+Client.getTableNameForPeer(peer)+" (FileName TEXT PRIMARY KEY COLLATE NOCASE, Size INTEGER, CheckSum INTEGER);");
 			
 			// Compile the database insert so we don't have to do it each time
 			SQLiteStatement insertStmt = peerindex.compileStatement("INSERT OR REPLACE INTO "+Client.getTableNameForPeer(peer)+" VALUES (?1, ?2, ?3);");
 
 			// Read from the GET response
 			JsonFactory jfactory = new JsonFactory();
 			JsonParser parser = jfactory.createJsonParser(new BufferedInputStream(in));
 			
 			// Begin transaction
 			peerindex.execSQL("BEGIN TRANSACTION");
 			try {
 				// Parse the index object
 				parser.nextToken();
 				while (parser.nextToken() != JsonToken.END_OBJECT)
 				{
 					String ioname = parser.getCurrentName();
 					
 					if (ioname.equals("IndexHash"))
 					{
 						parser.nextToken();
 						indexHash = parser.getLongValue();
 						gotIndexHash = true;
 					}
 					else if (ioname.equals("TimeStamp"))
 					{
 						parser.nextToken();
 						timeStamp = parser.getText();
 					}
 					else if (ioname.equals("List"))
 					{
 						parser.nextToken();
 						while (parser.nextToken() != JsonToken.END_ARRAY)
 						{							
 							while (parser.nextToken() != JsonToken.END_OBJECT)
 							{
 								String foname = parser.getCurrentName();
 								
 								if (foname.equals("FileName"))
 								{
 									parser.nextToken();
 									insertStmt.bindString(1, parser.getText());
 								}
 								else if (foname.equals("CheckSum"))
 								{
 									parser.nextToken();
 									insertStmt.bindLong(3, parser.getLongValue());
 								}
 								else if (foname.equals("Size"))
 								{
 									parser.nextToken();
 									insertStmt.bindLong(2, parser.getLongValue());
 								}
 								else
 								{
 									parser.nextToken();
 								}
 							}
 							
 							insertStmt.execute();
 							
 							if (interrupt.get()) {
 								throw new InterruptedException();
 							}
 						}
 					}
 					else
 					{
 						parser.nextToken();
 					}
 				}
 			} finally {
 				// Commit transaction
 				peerindex.execSQL("END TRANSACTION");
 				
 				// Index this table's file names
 				peerindex.execSQL("CREATE UNIQUE INDEX file_index ON "+Client.getTableNameForPeer(peer)+" (FileName)");
 				
 				// Delete the precompiled statement
 				insertStmt.close();
 				
 				// Close the JSON parser
 				parser.close();
 			}
 			
 			// Check if we got the header properly
 			if (timeStamp == null || !gotIndexHash)
 				throw new IllegalStateException("No header found in JSON file index");
 			
 			// Create the values to be stored in the SQL database
 			ContentValues vals = new ContentValues();
 			vals.put("IP", peer.getIpAddress());
 			vals.put("IndexHash", indexHash);
 			vals.put("LastSeen", timeStamp);
 
 			// Update the peer entry in the database
 			client.database.insert(Client.PEER_TABLE, null, vals);
 			
 			// Update the peer table in memory
 			peer.updateIndexHash(indexHash);
 			peer.updateTimestamp(timeStamp);
 			peer.setState(Peer.STATE_UP_TO_DATE);
 			client.peers.updatePeer(peer);
 			
 			System.out.println("Index for "+peer.getIpAddress()+" downloaded (hash: "+peer.getIndexHash()+")");
 		} catch (ClientForbiddenException e) {
 			System.err.println("Index download forbidden on peer: "+peer.getIpAddress());
 			
 			// Update was forbidden
 			peer.setState(Peer.STATE_UPDATE_FORBIDDEN);
 			client.peers.updatePeer(peer);
 		} catch (Exception e) {
 			System.err.println("Failed to download index for peer: "+peer.getIpAddress());
 			
 			if (e.getMessage() != null)
 				System.err.println(e.getMessage());
 			
 			// Update failed
 			peer.setState(Peer.STATE_UPDATE_FAILED);
 			client.peers.updatePeer(peer);
 			
 			// Remove this peer from the peer set
 			peer.remove();
 
 			// Remove them from the database
 			client.deletePeerFromDatabase(peer);
 		} finally {
 			// Update the peer list
 			PeerList.updateListView();
 			
 			// Cleanup the socket and reader
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException e) {}
 						
 			if (conn != null)
 				conn.disconnect();
 			
 			if (listener != null) {
 				// Notify the listener
 				listener.indexDownloadComplete(peer.getIpAddress());
 			}
 			
 			// Remove us from the download list
 			client.downloadTasks.remove(peer);
 		}
 	}
 }
 
 class SearchTask implements Runnable {
 	private PeerSet.Peer peer;
 	private SQLiteDatabase peerindex;
 	private String query;
 	private ResultListener listener;
 	private Client client;
 	private AtomicBoolean interrupt;
 	
 	private static final int LIMIT_PER_QUERY = 10000;
 	
 	public SearchTask(Client client, SQLiteDatabase peerindex, PeerSet.Peer peer, ResultListener listener, String query, AtomicBoolean interrupt)
 	{
 		this.client = client;
 		this.peerindex = peerindex;
 		this.peer = peer;
 		this.query = query;
 		this.listener = listener;
 		this.interrupt = interrupt;
 	}
 
 	@Override
 	public void run() {
 		try {
 			int offset = 0, count;
 			do
 			{
 				Cursor c;
 				try {
 					// Execute the query
 					c = peerindex.query(Client.getTableNameForPeer(peer),
 							new String[] {"FileName", "Size", "CheckSum"},
 							query != null ? "FileName LIKE '%"+query+"%'" : null,
 							null, null, null, null, offset+", "+LIMIT_PER_QUERY);
 				} catch (SQLiteDoneException e) {
 					// No more entries
 					break;
 				}
 				
 				// Iterate the cursor
 				while (c.moveToNext())
 				{
 					String file = c.getString(0);
 					long size = c.getLong(1);
 					int checksum = c.getInt(2);
 					if (!interrupt.get())
 						listener.foundResult(query, new ResultListener.Result(peer, file, size, checksum));
 					else
 						throw new InterruptedException();
 				}
 				
 				// Grab the row count
 				count = c.getCount();
 				
 				// Close this cursor
 				c.close();
 				
 				// Increment the offset
 				offset += count;
 			}
 			while (count == LIMIT_PER_QUERY);
 		} catch (SQLiteException e) {
 			System.err.println("Failed to search index for peer: "+peer.getIpAddress());
 			e.printStackTrace();
 			
 			// Remove this peer from the peer set
 			peer.remove();
 			
 			// Update peer list
 			PeerList.updateListView();
 			
 			// Drop them from the database
 			client.deletePeerFromDatabase(peer);
 		} catch (InterruptedException e) {
 			System.out.println("Search on "+peer.getIpAddress()+" was interrupted");
 		}
 		
 		// Search finished
 		listener.searchComplete(query, peer);
 		
 		// Remove us from the hash map
 		client.searchTasks.remove(peer);
 	}
 }
