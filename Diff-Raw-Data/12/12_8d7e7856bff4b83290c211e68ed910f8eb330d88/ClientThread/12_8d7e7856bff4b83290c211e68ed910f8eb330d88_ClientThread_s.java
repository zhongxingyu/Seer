 package com.sandwich;
 
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.text.ClipboardManager;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.sandwich.client.Client;
 import com.sandwich.client.PeerSet.Peer;
 import com.sandwich.client.ResultListener;
 import com.sandwich.player.MediaMimeInfo;
 
 @SuppressWarnings("deprecation") // Needed to avoid stupid warnings for older ClipboardManager class
 public class ClientThread implements Runnable {
 	private Activity activity;
 	private Client client;
 	private SearchListener listener;
 	private ProgressUpdater updater;
 	
 	public ClientThread(Activity activity)
 	{
 		this.activity = activity;
 		this.client = null;
 	}
 	
 	public void doSearch(String query)
 	{
 		// A bit of a hack
 		listener.onQueryTextSubmit(query);
 	}
 	
 	public boolean isResultStreamable(ResultListener.Result result)
 	{
 		return client.isResultStreamable(result);
 	}
 	
 	public void copyUrl(ResultListener.Result result) throws UnknownHostException, NoSuchAlgorithmException, URISyntaxException
 	{
 		String url = null;
 		Iterator<Peer> peers = result.getPeerIterator();
 		
 		// This class was deprecated in API level 11, but we need compatibility for API level 9
 		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
 		
 		while (peers.hasNext())
 		{
 			try {
 				 url = Client.getUriForResult(peers.next(), result);
 				 break;
 			} catch (NoSuchAlgorithmException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (URISyntaxException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (UnknownHostException e) {
 				if (!peers.hasNext())
 					throw e;
 			}
 		}
 		
 		// Write to the clipboard
 		clipboard.setText(url);
 	}
 	
 	public void download(ResultListener.Result result) throws NoSuchAlgorithmException, URISyntaxException, IOException
 	{
 		Iterator<Peer> peers = result.getPeerIterator();
 		
 		while (peers.hasNext())
 		{
 			try {
 				client.startFileDownloadFromPeer(peers.next(), result.result);
 				break;
 			} catch (NoSuchAlgorithmException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (URISyntaxException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (IOException e) {
 				if (!peers.hasNext())
 					throw e;
 			}
 		}
 	}
 	
 	public void stream(ResultListener.Result result) throws NoSuchAlgorithmException, URISyntaxException, IOException
 	{
 		Iterator<Peer> peers = result.getPeerIterator();
 		
 		while (peers.hasNext())
 		{
 			try {
 				client.startFileStreamFromPeer(activity, peers.next(), result.result);
 				break;
 			} catch (NoSuchAlgorithmException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (URISyntaxException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (IOException e) {
 				if (!peers.hasNext())
 					throw e;
 			}
 		}
 	}
 	
 	public void share(ResultListener.Result result) throws UnknownHostException, NoSuchAlgorithmException, URISyntaxException
 	{
 		Intent shareIntent = new Intent();
 		String url = null;
 		Iterator<Peer> peers = result.getPeerIterator();
 		
 		while (peers.hasNext())
 		{
 			try {
 				 url = Client.getUriForResult(peers.next(), result);
 				 break;
 			} catch (NoSuchAlgorithmException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (URISyntaxException e) {
 				if (!peers.hasNext())
 					throw e;
 			} catch (UnknownHostException e) {
 				if (!peers.hasNext())
 					throw e;
 			}
 		}
 		
 		shareIntent.setAction(Intent.ACTION_SEND);
 		shareIntent.putExtra(Intent.EXTRA_TEXT, url);
 		shareIntent.setType("text/plain");
 
 		activity.startActivity(Intent.createChooser(shareIntent, "Share to..."));
 	}
 	
 	public void endSearch() {
 		client.endSearch();
 	}
 	
 	public void initialize()
 	{    	
     	if (client != null)
     		throw new IllegalStateException("Bootstrap thread was already initialized");
     	
     	// Create the client
     	client = new Client(activity);
     	client.initialize();
 		
         // Create our search listener
         listener = new SearchListener(activity, client);
         
         // Add our array adapter to the list view
         ListView results = (ListView)activity.findViewById(R.id.resultsListView);
         results.setAdapter(new ResultAdapter(activity, R.layout.simplerow));
         results.setOnItemClickListener(listener);
         
     	// Create the progress bar updater
         ProgressBar progress = (ProgressBar)activity.findViewById(R.id.updateBar);
     	updater = new ProgressUpdater(activity, progress);
     	
     	// Register our package manager with the MIME class
     	MediaMimeInfo.registerPackageManager(activity.getPackageManager());
        
        // Bootstrap from the cache initially
        client.bootstrapFromCache();
 	}
 	
 	public void release()
 	{
 		if (client != null)
 		{
 			client.release();
 		}
 	}
 	
 	@Override
 	public void run() {
 		if (client == null)
 			throw new IllegalStateException("Bootstrap thread was not initialized");
 
 		try {
 			String initialHost = "isys-ubuntu.case.edu";
 
 			// Bootstrap from network
 			updater.reset();
 			updater.updateMax(client.bootstrapFromNetwork(initialHost, updater));
 		} catch (Exception e) {
 			Dialog.displayDialog(activity, "Bootstrap Error", e.getMessage(), true);
 			e.printStackTrace();
 		}
 	}
 }
