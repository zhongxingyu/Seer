 package team2.mainapp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 
 import team2.mainapp.PullToRefreshListView.OnRefreshListener;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class MainAppActivity extends ListActivity {
 	static LinkedList<Sector> allTopics; 
 	static String s;
 	static String sectors;
 	private LinkedList<String> mListItems;
 	int ready;
 	/** Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		ready = 0;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Log.d("debug","Hello");
 				
 		allTopics = new LinkedList<Sector>();
 		allTopics.add(new Sector("oil"));
 		allTopics.add(new Sector("currency"));
 		
 		Log.d("Debug","Bye bye");
 		
 		sectors = "";
 		for(Sector sector : allTopics)
 		{
 			sectors += sector.getName() + ";";
 		}
 		
 		Log.d("debug",sectors);
 
 		mListItems = new LinkedList<String>();
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				R.layout.rowlayout, R.id.labelo, mListItems);
 
 		setListAdapter(adapter);
 
 		// Set a listener to be invoked when the list should be refreshed.
 		((PullToRefreshListView) getListView()).setOnRefreshListener(new OnRefreshListener() {
 			@Override
 			public void onRefresh() {
 				// Do work to refresh the list here.
 				GetDataTask task = new GetDataTask();
 				task.execute();
 			}
 		});
 
 		// Start the process of polling the server
 		startProgress();
 		// Do an initial refresh straight away without user input
 		GetDataTask task = new GetDataTask();
 		task.execute();
 	}
 
 	private class GetDataTask extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Do nothing
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void x) {
 			// Clear current list
 			mListItems.clear();
 			// Wait for data to exist
 			while (ready == 0) {}
 			
 			
 
 			// Go through the allTopics data structure, pasting title & date
 			for (Sector topicsector : allTopics) {
 				java.util.Collections.sort(topicsector.getTopicData());
 				for (Topic topic : topicsector.getTopicData()) {
					String allInfo = topic.getTitle() + "\n@ " + topic.getDate() + "\n " + topic.getArtsLastHour();
 					mListItems.add(allInfo);
 				}
 			} 
 			// Complete the refresh
 			((PullToRefreshListView) getListView()).onRefreshComplete();
 		}
 	}
 
 	public void startProgress() {
 		// Do something long
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				// Initialise by always-in-past date
 				String date = "1212/12/12 12:12:12";
 				String query = sectors + ";" + date;
 				for (int i = 0; i >= 0; i++) {
 					try {
 						if(i > 0)
 							Thread.sleep(60000);
 						// Get new information from remote server
 						Date dateType = new Date();
 						s = TCPClient.go(query);
 						
 						// Parse retrieved information
 						parseInput(s);
 						
 						// Reset date to current
 						
 						// Turns date into string
 						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/d HH:mm:ss");
 						query = sectors + ";" + dateFormat.format(dateType);
 						
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 				}
 			}
 		};
 		new Thread(runnable).start();
 	}
 
 	protected void parseInput(String s2) {
 		try{
 		// Read in flag which splits between feedReader and Parse infos
 		String[] type = s2.split("SPLITINFO\n");
 
 		// Read in flag which splits between each sector in the Parse information
 		String[] topicsectors = type[1].split("TOPSTOP\n");
 		Log.d("debug",type[1]);
 		int i = 0;
 		for (String sector : topicsectors)
 		{
 			// Read in flag which splits between each topic in the sector
 			String[] topics = sector.split("SPECTOPS\n");
 			for (String topic : topics)
 			{
 				// Splits the topic data into parts
 				String[] rawData = topic.split(";;\n");
 
 				if(rawData.length < 5)
 					break;
 				
 				// Splits the URLS and keyWords into individual parts
 				String[] links = rawData[4].split(";\n");
 				String[] words = rawData[5].split(";\n");
 
 				// Creates an arraylist to hold the URLs
 				ArrayList<String> URLS = new ArrayList<String>();
 				for (String link : links)
 				{
 					// Adds each link to the list
 					URLS.add(link);
 				}
 
 				// Creates an arraylist to hold the keyWords
 				ArrayList<KeyWord> KeyWords = new ArrayList<KeyWord>();
 				for (String word : words)
 				{
 					// Split each keyword into its word and its sentiment
 					String[] bits = word.split("@");
 					// Put each bit into the list
 					KeyWords.add(new KeyWord(bits[0], bits[1]));
 				}
 
 				// Add the topic info to the sector info
 				allTopics.get(i).addTopic(new Topic(rawData[1],rawData[2],Integer.parseInt(rawData[3]),URLS,KeyWords,rawData[0]));
 			}
 			// Add the sectorInfo to the parseInfo
 			i++;
 			ready = 1;
 		}
 		// Replaces allTopics with parseInf
 		Log.d("debug","New info received");
 		}
 		catch (Exception e){
 			e.printStackTrace();
 			Log.d("Debug",e.toString());
 		}
 	}
 
 }
