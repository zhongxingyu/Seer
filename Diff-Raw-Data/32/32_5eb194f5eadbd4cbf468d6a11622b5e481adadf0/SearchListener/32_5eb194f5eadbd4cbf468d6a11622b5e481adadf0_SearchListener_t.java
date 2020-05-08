 package com.sandwich;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.sandwich.client.Client;
 import com.sandwich.client.PeerSet.Peer;
 import com.sandwich.client.ResultListener;
 
 import android.app.Activity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 public class SearchListener implements ResultListener,OnItemClickListener,Runnable  {
 	private Client sandwichClient;
 	private Activity activity;
 	
 	private ListView resultsView;
 	private ProgressBar updateBar;
 	
 	private ConcurrentLinkedQueue<ResultListener.Result> results;
 	private AtomicInteger searchesFinished;
 	private AtomicBoolean scheduledRun;
 
 	public static final int MAX_RESULTS = 10000;
 		
 	public SearchListener(Activity activity, Client client)
 	{
 		this.sandwichClient = client;
 		this.activity = activity;
 
 		// Fetch these here so we don't have to do it later
 		this.resultsView = (ListView)activity.findViewById(R.id.resultsListView);
 		this.updateBar = (ProgressBar)activity.findViewById(R.id.updateBar);
 		
 		// Initialize the results list
 		results = new ConcurrentLinkedQueue<ResultListener.Result>();
 		searchesFinished = new AtomicInteger();
 		scheduledRun = new AtomicBoolean();
 	}
 	
 	// Called when the search button is clicked
 	public boolean onQueryTextSubmit(String query)
 	{
 		ResultAdapter listAdapter;
 		
 		// End any pending search to prevent modification of these result lists
 		// after we've cleared them, but before beginSearch() kills them.
 		sandwichClient.endSearch();
 		
 		// Clear the results list
 		listAdapter = (ResultAdapter)resultsView.getAdapter();
 		listAdapter.clear();
 		
 		// Initialize the results list
 		results.clear();
 		searchesFinished.set(0);
 		scheduledRun.set(false);
 		
 		// Start progress bar at 0
 		updateBar.setProgress(0);
 		
 		// Execute the asynchronous search with the client
 		try {
 			updateBar.setMax(sandwichClient.beginSearch(query, this));
			System.out.println("Search started: "+updateBar.getMax());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Dialog.displayDialog(activity, "Search Error", e.getMessage(), false);
 		}
 		
 		// We handled the event so return true
 		return true;
 	}
 	
 	@Override
 	// Called in UI thread to add search result
 	public void run() {
 		ResultAdapter listAdapter = (ResultAdapter)resultsView.getAdapter();
 		ResultListener.Result result;
 
		// Now running
		scheduledRun.set(false);
 
 		// Add all results
 		for (;;) {
 			result = results.poll();
 			if (result == null)
 				break;
 			listAdapter.add(result);
 		}
 
 		// If we've added too many results, terminate the search
 		if (listAdapter.getCount() > MAX_RESULTS)
 			sandwichClient.endSearch();

 		// Update the progressbar
 		int capturedSearchesFinished = searchesFinished.get();
 		if (updateBar.getProgress() != capturedSearchesFinished)
 		{
 			updateBar.setProgress(capturedSearchesFinished);
 
 			// If nothing was found, just add an entry to say nothing was found
 			if ((listAdapter.getCount() == 0) && (capturedSearchesFinished == updateBar.getMax()))
 				listAdapter.add(null);
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 		activity.openContextMenu(view);
 	}
 
 	@Override
 	public void searchComplete(String query, Peer peer) {
 		searchesFinished.incrementAndGet();
 		
		if (scheduledRun.getAndSet(true) == false) {
 			activity.runOnUiThread(this);
 		}
 	}
 
 	@Override
 	public void foundResult(String query, Result result) {
 		results.add(result);
 		
		if (scheduledRun.getAndSet(true) == false) {
 			activity.runOnUiThread(this);
 		}
 	}
 }
