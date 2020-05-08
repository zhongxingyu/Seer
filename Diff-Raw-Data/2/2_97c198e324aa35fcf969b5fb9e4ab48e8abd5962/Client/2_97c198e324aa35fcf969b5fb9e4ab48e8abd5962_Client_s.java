 package com.sandwich.client;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.Inet6Address;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.gson.stream.JsonReader;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Environment;
 
 public class Client {
 	private Context context;
 	private PeerSet peers;
 	private SQLiteDatabase database;
 	
 	private ArrayList<Thread> searchThreads;
 	private HashMap<PeerSet.Peer, Thread> indexDownloadThreads;
 	
 	private static final String CREATE_PEER_TABLE = "CREATE TABLE peers (IP TEXT PRIMARY KEY, IndexHash INT);";
 
 	static final String PEER_TABLE = "peers";
 	static final String PEER_DB = "peers.db";
 	
 	static final int READ_BLOCK_SIZE = 4096;
 	
 	public Client(Context context)
 	{
 		this.context = context;
 		this.searchThreads = new ArrayList<Thread>();
 		this.indexDownloadThreads = new HashMap<PeerSet.Peer, Thread>();
 		this.peers = new PeerSet();
 	}
 	
 	// Holy fucking shit
 	private static int unsign(byte signed)
 	{
 		if (signed >= 0)
 			return signed;
 		else
 			return signed+256;
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
 	
 	static BufferedInputStream getInputStreamFromConnection(HttpURLConnection conn) throws IOException
 	{
 		int responseCode;
 
 		// Send the GET request and get the response code
 		responseCode = sendGetRequest(conn);
 		if (responseCode != HttpURLConnection.HTTP_OK)
 		{
 			throw new ConnectException("Failed to get peer list from bootstrap peer");
 		}
 
 		return new BufferedInputStream(conn.getInputStream());
 	}
 	
 	private PeerSet getPeerList(URL bootstrapUrl) throws IOException, JSONException
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
 
 			peerSet.addPeer(jsonPeer.getString("IP"), jsonPeer.getString("LastSeen"), jsonPeer.getLong("IndexHash"));
 		}
 		
 		return peerSet;
 	}
 	
 	private Thread startDownloadIndexThreadForPeer(SQLiteDatabase database, PeerSet.Peer peer)
 	{
		Thread t = new Thread(new IndexDownloadThread(database, peer));
 		t.start();
 		return t;
 	}
 	
 	private PeerSet getPeerSetFromDatabase(SQLiteDatabase database) throws SQLiteException
 	{
 		PeerSet peers = new PeerSet();
 		Cursor c = database.query(PEER_TABLE, new String[] {"IP", "IndexHash"}, null, null, null, null, null, null);
 		
 		c.moveToFirst();
 		while (!c.isAfterLast())
 		{
 			peers.addPeer(c.getString(0), "FIXME", Long.parseLong(c.getString(1), 10));
 			c.moveToNext();
 		}
 		
 		c.close();
 		
 		return peers;
 	}
 	
 	static boolean deletePeerFromDatabase(SQLiteDatabase database, PeerSet.Peer peer) throws SQLiteException
 	{
 		boolean ret = false;
 		
 		// Delete them from the peer table first
 		try {
 			ret = database.delete(PEER_TABLE, "IP=?", new String[] {peer.getIpAddress()}) > 0;
 			System.out.println("Dropped peers row for: "+peer.getIpAddress()+" ("+ret+")");
 		} catch (SQLiteException e) {
 			// It's ok for this to fail
 		}
 		
 		// Delete their index table
 		try {
 			database.execSQL("DROP TABLE "+getTableNameForPeer(peer));
 			System.out.println("Dropped index table for: "+peer.getIpAddress());
 		} catch (SQLiteException e) {
 			// It's ok for this to fail
 		}
 		
 		return ret;
 	}
 	
 	static String getTableNameForPeer(PeerSet.Peer peer)
 	{
 		return '[' + peer.getIpAddress() + ']';
 	}
 	
 	static long getOldHashOfPeerIndex(SQLiteDatabase database, PeerSet.Peer peer)
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
 		boolean databaseCreated;
 		
 		// Check if the database already exists
 		databaseCreated = true;
 		for (String db : context.databaseList())
 		{
 			if (db.equals(PEER_DB))
 			{
 				databaseCreated = false;
 				break;
 			}
 		}
 
 		// Open or create it
 		database = context.openOrCreateDatabase(PEER_DB, Context.MODE_PRIVATE, null);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Enable write-ahead logging if possible
 			database.enableWriteAheadLogging();
 		}
 		
 		// Create our table if it's a new database
 		if (databaseCreated)
 		{
 			database.execSQL(CREATE_PEER_TABLE);
 		}
 	}
 	
 	public void release()
 	{
 		// Start killing of search threads
 		endSearch();
 		
 		try {
 			// Stop bootstrapping
 			stopBootstrap();
 			
 			// Wait for the search threads to die
 			for (Thread t : searchThreads)
 			{
 				t.join();
 			}
 		} catch (InterruptedException e) { }
 		
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
 	
 	private boolean downloadPeerList(String initialPeer) throws NoSuchAlgorithmException, URISyntaxException, IOException, JSONException
 	{
 		URL bootstrapUrl;
 		Iterator<PeerSet.Peer> iterator;
 		Random rand = new Random();
 		
 		System.out.println("Downloading the peer list");
 		
 		// Bootstrap from a random peer
 		rand.setSeed(System.currentTimeMillis());
 		while (peers.getPeerListLength() > 0)
 		{
 			// Generate a random peer to bootstrap from
 			int nextPeer = rand.nextInt(peers.getPeerListLength());
 			System.out.println("Bootstrapping from peer index: "+nextPeer);
 			
 			// Get the peer object for the index
 			iterator = peers.getPeerListIterator();
 			PeerSet.Peer selectedPeer = null;
 			for (int i = 0; iterator.hasNext(); i++)
 			{
 				PeerSet.Peer currentPeer = iterator.next();
 				
 				if (i == nextPeer)
 				{
 					selectedPeer = currentPeer;
 					break;
 				}
 			}
 			
 			// Make sure it worked
 			if (selectedPeer == null)
 			{
 				break;
 			}
 				
 			try {
 				// Resolve address and create a URL
 				bootstrapUrl = new URL(createPeerUrlString(selectedPeer.getIpAddress(), null, null));
 				
 				// Download the peer list
 				peers.updatePeerSet(getPeerList(bootstrapUrl));
 				
 				// If we get here, bootstrapping was successful
 				return true;
 			} catch (Exception e) {
 				// Failed to connect to this one, so prune it
 				selectedPeer.remove();
 				
 				// Drop it from the database
 				Client.deletePeerFromDatabase(database, selectedPeer);
 			}
 		}
 
 		// Try the initial peer if all else fails
 		try {
 			System.out.println("Trying initial peer: "+initialPeer);
 			
 			// Resolve address and create a URL
 			bootstrapUrl = new URL(createPeerUrlString(initialPeer, null, null));
 			
 			// Download the peer list
 			peers.updatePeerSet(getPeerList(bootstrapUrl));
 			
 			// It worked
 			return true;
 		} catch (Exception e) {
 			// Failed to connect to the initial peer, so let's try another one
 		}
 		
 		// We're out of people to bootstrap from
 		return false;
 	}
 	
 	public void stopBootstrap() throws InterruptedException
 	{
 		synchronized (indexDownloadThreads) {
 			// Interrupt threads
 			for (Thread t : indexDownloadThreads.values())
 			{
 				t.interrupt();
 			}
 			
 			// Wait for them to die
 			for (Thread t : indexDownloadThreads.values())
 			{
 				t.join();
 			}
 		}
 	}
 	
 	public void bootstrapFromNetwork(String initialPeer) throws IOException, JSONException, InterruptedException, NoSuchAlgorithmException, URISyntaxException
 	{
 		Iterator<PeerSet.Peer> iterator;
 
 		// Download the peer list
 		if (!downloadPeerList(initialPeer))
 		{
 			throw new IllegalStateException("No peers available");
 		}
 		
 		// Reap threads that aren't still downloading
 		ArrayList<PeerSet.Peer> reapList = new ArrayList<PeerSet.Peer>();
 		synchronized (indexDownloadThreads) {
 			// Add dead threads to the reap list
 			for (Map.Entry<PeerSet.Peer, Thread> entry : indexDownloadThreads.entrySet())
 			{
 				if (entry.getValue().getState() == Thread.State.TERMINATED)
 					reapList.add(entry.getKey());
 			}
 			
 			// Reap dead threads
 			for (PeerSet.Peer p : reapList)
 			{
 				indexDownloadThreads.remove(p);
 			}
 		}
 		
 		// We have to do this in a synchronized block so our index download threads
 		// can't touch it while we're downloading
 		synchronized (peers) {
 			// Iterate the peer list and download indexes for each
 			iterator = peers.getPeerListIterator();
 			while (iterator.hasNext())
 			{
 				PeerSet.Peer peer = iterator.next();
 				long oldHash;
 				
 				synchronized (indexDownloadThreads) {
 					// Check if an updater is already running for this peer
 					if (!indexDownloadThreads.containsKey(peer))
 					{
 						// Check if the old index hash is valid
 						oldHash = getOldHashOfPeerIndex(database, peer);
 						if (oldHash == peer.getIndexHash())
 						{
 							System.out.println(peer.getIpAddress()+" index is up to date (hash: "+peer.getIndexHash()+")");
 							
 							// Don't download anything
 							continue;
 						}
 						
 						// No updater running and index is not up to date
 						indexDownloadThreads.put(peer, startDownloadIndexThreadForPeer(database, peer));
 					}
 				}
 			}
 		}
 	}
 	
 	public void endSearch()
 	{
 		ArrayList<Thread> reapList = new ArrayList<Thread>();
 
 		synchronized (searchThreads) {	
 			// Interrupt existing search threads
 			for (Thread t : searchThreads)
 			{
 				if (t.getState() == Thread.State.TERMINATED)
 					reapList.add(t);
 				else
 					t.interrupt();
 			}
 			
 			// Reap dead threads
 			for (Thread t : reapList)
 			{
 				searchThreads.remove(t);
 			}
 		}
 	}
 	
 	public void beginSearch(String query, ResultListener listener) throws IOException
 	{		
 		if (peers == null)
 		{
 			throw new IllegalStateException("Not bootstrapped");
 		}
 		
 		// End an existing search
 		endSearch();
 		
 		// Make sure the peer list isn't modified by the search threads while we're iterating
 		synchronized (peers) {
 			// Start search threads for each peer
 			Iterator<PeerSet.Peer> peerIterator = peers.getPeerListIterator();
 			while (peerIterator.hasNext())
 			{
 				PeerSet.Peer peer = peerIterator.next();
 
 				// Start the search thread
 				System.out.println("Spawning thread to search "+peer.getIpAddress());
 				Thread t = new SearchThread(database, peer, listener, query);
 				synchronized (searchThreads) {
 					searchThreads.add(t);
 				}
 				t.start();
 			}
 		}
 	}
 
 	@TargetApi(11)
 	public void startFileDownloadFromPeer(String peer, String file) throws URISyntaxException, UnknownHostException, NoSuchAlgorithmException
 	{
 		DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
 		DownloadManager.Request request;
 		String title;
 		String url;
 		
 		// Make a title from the last component of the file path
 		title = file;
 		if (title.lastIndexOf("/") > 0)
 			title = title.substring(title.lastIndexOf("/")+1);
 
 		url = createPeerUrlString(peer, "/file", "path="+file);
 		
 		System.out.println("Downloading URL: "+url+" ("+title+")");
 
 		request = new DownloadManager.Request(Uri.parse(url));
 		
 		// Download to the external downloads folder
 		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
 		
 		// DownloadManager was enhanced with Honeycomb with useful features we want to activate
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Allow the media scanner to pick this file up
 			request.allowScanningByMediaScanner();
 		
 			// Continue showing the notification even after the download finishes
 			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 		}
 		
 		// Give it our title
 		request.setTitle(title);
 		
 		// Fire the download
 		downloader.enqueue(request);
 	}
 	
 	public boolean startFileStreamFromPeer(Activity activity, String peer, String file) throws NoSuchAlgorithmException, URISyntaxException, MalformedURLException, IOException
 	{
 		String url;
 		String mimeType;
 
 		mimeType = URLConnection.guessContentTypeFromName(file);
 		url = createPeerUrlString(peer, "/file", "path="+file);
 		if (mimeType == null)
 		{
 			// Undetermined MIME type
 			return false;
 		}
 		else if (mimeType.startsWith("audio/"))
 		{
 			// Create the audio player activity
 			Intent i = new Intent(activity, com.sandwich.AudioPlayer.class);
 			i.putExtra("URL", url);
 			activity.startActivity(i);
 		}
 		else if (mimeType.startsWith("video/"))
 		{
 			// Create the video player activity
 			Intent i = new Intent(activity, com.sandwich.VideoPlayer.class);
 			i.putExtra("URL", url);
 			activity.startActivity(i);
 		}
 		else
 		{
 			// No player for this
 			return false;
 		}
 		
 		return true;
 	}
 }
 
 class IndexDownloadThread extends Thread {
 	SQLiteDatabase database;
 	PeerSet.Peer peer;
 	
 	public IndexDownloadThread(SQLiteDatabase database, PeerSet.Peer peer)
 	{
 		this.database = database;
 		this.peer = peer;
 	}
 
 	@Override
 	public void run() {
 		URL queryUrl;
 		HttpURLConnection conn;
 		BufferedInputStream in;
 		long oldHash;
 		
 		conn = null;
 		in = null;
 		
 		// Drop the tables and rows for this peer
 		Client.deletePeerFromDatabase(database, peer);
 		try {
 			// Build the query index URL
 			queryUrl = new URL(Client.createPeerUrlString(peer.getIpAddress(), "/indexfor", null));
 			
 			// Connect a URL connection
 			conn = Client.createHttpConnection(queryUrl, false);
 			
 			// Get an input stream from the GET request on this URL
 			in = Client.getInputStreamFromConnection(conn);
 			
 			// Create the required table
 			database.execSQL("CREATE TABLE "+Client.getTableNameForPeer(peer)+" (FileName TEXT PRIMARY KEY);");
 			
 			// Read from the GET response
 			JsonReader reader = new JsonReader(new InputStreamReader(in));
 			try {
 				// Start reading the index object
 				reader.beginObject();
 				
 				// Read the index object
 				while (reader.hasNext())
 				{
 					String ioname = reader.nextName();
 					if (ioname.equals("List"))
 					{
 						// Now start reading the array
 						reader.beginArray();
 						
 						while (reader.hasNext())
 						{
 							String file = null;
 							
 							// Start reading the file object
 							reader.beginObject();
 							ContentValues vals = new ContentValues();
 							
 							// Read the file object
 							while (reader.hasNext())
 							{
 								String foname = reader.nextName();
 								if (foname.equals("FileName"))
 								{
 									// Read the file name
 									file = reader.nextString();
 									vals.put("FileName", file);
 								}
 								else
 								{
 									// Otherwise just skip it
 									reader.skipValue();
 								}
 							}
 							
 							// End of file object
 							reader.endObject();
 							
 							// If file object had data, add it
 							if (vals.size() != 0)
 							{
 								// Insert it into the database
 								database.insertWithOnConflict(Client.getTableNameForPeer(peer), null, vals, SQLiteDatabase.CONFLICT_REPLACE);
 							}
 							
 							// If we've been interrupted, let's die
 							if (isInterrupted())
 							{
 								throw new InterruptedException();
 							}
 						}
 						
 						// Done reading array
 						reader.endArray();
 					}
 					else
 					{
 						// Skip it
 						reader.skipValue();
 					}
 				}
 
 				// End of index object
 				reader.endObject();
 			} finally {
 				// Close the JSON reader
 				reader.close();
 			}
 			
 			// Create the values to be stored in the SQL database
 			ContentValues vals = new ContentValues();
 			vals.put("IP", peer.getIpAddress());
 			vals.put("IndexHash", ""+peer.getIndexHash());
 			
 			oldHash = Client.getOldHashOfPeerIndex(database, peer);
 			if (oldHash == -1)
 			{
 				System.out.println(peer.getIpAddress()+" has never been seen before (new hash: "+peer.getIndexHash()+")");
 
 				// We need to insert this into the list
 				database.insert(Client.PEER_TABLE, null, vals);
 			}
 			else
 			{
 				System.out.println(peer.getIpAddress()+" has a newer index (old hash: "+oldHash+" | new hash: "+peer.getIndexHash()+")");
 
 				// We need to replace this peer's existing values
 				database.replace(Client.PEER_TABLE, null, vals);
 			}
 			
 			System.out.println("Index for "+peer.getIpAddress()+" downloaded (hash: "+peer.getIndexHash()+")");
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 			
 			// Remove this peer from the peer set
 			peer.remove();
 
 			// Remove them from the database
 			Client.deletePeerFromDatabase(database, peer);
 		} finally {
 			// Cleanup the socket and reader
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException e) {}
 						
 			if (conn != null)
 				conn.disconnect();
 		}
 	}
 }
 
 class SearchThread extends Thread {
 	PeerSet.Peer peer;
 	SQLiteDatabase database;
 	String query;
 	ResultListener listener;
 	
 	public SearchThread(SQLiteDatabase database, PeerSet.Peer peer, ResultListener listener, String query)
 	{
 		this.database = database;
 		this.peer = peer;
 		this.query = query;
 		this.listener = listener;
 	}
 
 	@Override
 	public void run() {
 		try {
 			Cursor c = database.query(Client.getTableNameForPeer(peer), new String[] {"FileName"}, "FileName LIKE '%"+query+"%'" , null, null, null, null, null);
 			
 			// Iterate the cursor
 			c.moveToFirst();
 			while (!c.isAfterLast())
 			{
 				String file = c.getString(0);
 				if (!isInterrupted())
 					listener.foundResult(query, peer.getIpAddress(), file);
 				else
 				{
 					System.out.println("Search on "+peer.getIpAddress()+" was interrupted");
 					break;
 				}
 				c.moveToNext();
 			}
 			c.close();
 		} catch (SQLiteException e) {
 			System.err.println(e.getMessage());
 			
 			// Remove this peer from the peer set
 			peer.remove();
 			
 			// Drop them from the database
 			Client.deletePeerFromDatabase(database, peer);
 		}
 	}
 }
