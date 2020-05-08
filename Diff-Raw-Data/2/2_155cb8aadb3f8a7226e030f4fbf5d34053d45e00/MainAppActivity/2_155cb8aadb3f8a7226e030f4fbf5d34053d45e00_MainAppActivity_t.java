 package team2.mainapp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 
 import team2.mainapp.PullToRefreshListView.OnRefreshListener;
 
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainAppActivity extends ListActivity {
 	static String s;
 	static String sectors;
 	static int incrementing;
 	static boolean started;
 	private ArrayList<String[]> mListItems;
 	int ready;
 	/** Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		started = true;
 		ready = 0;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Log.d("debug","Hello");
 		
 		GlobalState gState = (GlobalState) getApplication();
 				
 		gState.setSectors(new LinkedList<Sector>());
 		gState.getAllSectors().add(new Sector("oil"));
 		gState.getAllSectors().add(new Sector("currency"));
 		
 		Log.d("Debug", "Bye bye");
 		
 		sectors = "";
 		for(Sector sector : gState.getAllSectors())
 		{
 			sectors += sector.getName() + ";";
 		}
 		
 		Log.d("debug",sectors);
 
 		mListItems = new ArrayList<String[]>();
 
   		MyArrayAdapter adapter = new MyArrayAdapter(this,
   				 mListItems);
 
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
 	
 	public void myClickHandler(View view) {
 		TextView tv = (TextView) view.findViewById(R.id.uid);
 		Toast.makeText(this, tv.getText(), Toast.LENGTH_SHORT).show();
 		Intent myIntent = new Intent(this, SingleTopic.class);
 		myIntent.putExtra("EXTRA_UID", tv.getText());
 		startActivity(myIntent);
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuitem1:
 			Toast.makeText(this, "Menu Item 1 selected", Toast.LENGTH_SHORT)
 					.show();
 			Intent myIntent = new Intent(this, GoogleNews.class);
 			startActivity(myIntent);
 			break;
 		case R.id.menuitem2:
 			Toast.makeText(this, "Menu item 2 selected", Toast.LENGTH_SHORT)
 					.show();
 			break;
 
 		default:
 			break;
 		}
 
 		return true;
 	}
 	
 	public void createNotification(String title, int uid) {
 		Log.d("Debug", "Creating Notification");
 		NotificationManager notificationManager = (NotificationManager) 
 					getSystemService(NOTIFICATION_SERVICE);
 		// Construction
 		Notification.Builder not = new Notification.Builder(this);
 		not.setSmallIcon(R.drawable.psyduck2);
 		not.setContentTitle(title);
 		not.setContentText("Blah");
 		Intent intent = new Intent(this, SingleTopic.class);
 		PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
 		not.setContentIntent(activity);
 		// Hide the notification after its selected
 		not.setAutoCancel(true);
 		Notification notification = not.getNotification();
 		Log.d("Debug", "Sending Notification");
 		notificationManager.notify(uid, notification);
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
 			GlobalState gState = (GlobalState) getApplication();
 			// Go through the allTopics data structure, pasting title & date
 			for (Sector topicsector : gState.getAllSectors()) {
 				java.util.Collections.sort(topicsector.getTopicData());
 				for (Topic topic : topicsector.getTopicData()) {
 					String[] allInfo = new String[4];
 					allInfo[0] = topic.getTitle();
 					allInfo[1] = Integer.toString(topic.getArtsLastHour()) + " - " + topic.getDate();
 					allInfo[2] = topic.getWords();
 					allInfo[3] = Integer.toString(topic.getUid());
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
 				Looper.prepare();
 				// Initialise by always-in-past date
 				String date = "1212/12/12 12:12:12";
 				String query = sectors + ";" + date;
 				for (int i = 0; i >= 0; i++) {
 					try {
 						if(i > 0)
 							Thread.sleep(60000);
 						// Get new information from remote server
 						Date dateType = new Date();
 						Log.d("Debug1", query);
 						s = TCPClient.go(query);
 						
 						// Parse retrieved information
 						parseInput(s);
 						
 						// Reset date to current
 						Log.d("Debug2", s);
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
 		try {
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
 
 				if (rawData.length < 5)
 					break;
 				
 				// Splits the URLS and keyWords into individual parts
 				String[] links = rawData[4].split(";\n");
 				String[] titles = rawData[5].split(";\n");
 				String[] words = rawData[6].split(";\n");
 
 				// Creates an arraylist to hold the URLs
 				ArrayList<String> URLS = new ArrayList<String>();
 				for (String link : links)
 				{
 					// Adds each link to the list
 					URLS.add(link);
 				}
 				
 				ArrayList<String> Titles = new ArrayList<String>();
 				for (String title : titles)
 				{
 					// Adds each link to the list
 					Titles.add(title);
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
 				GlobalState gState = (GlobalState) getApplication();
 				// Add the topic info to the sector info
 				boolean alert = gState.getAllSectors().get(i).addTopic(
 						new Topic(rawData[1], rawData[2], Integer.parseInt(rawData[3]), URLS, KeyWords, rawData[0], Titles));
 				if (alert) {
 					createNotification(rawData[1], Integer.parseInt(rawData[0])); 
 				}
 			}
 			// Add the sectorInfo to the parseInfo
 			i++;
 			ready = 1;
 		}
 		
 		// Parse Google News
 		// Split sectors
 		String[] newssectors = type[0].split("NEWSTOP\n");
 		GlobalState gState = (GlobalState) getApplication();
 		int j = 0;
 		for (String sector : newssectors)
 		{
 			String[] stories = sector.split("SPECNEWS\n");
 			LinkedList<GoogleStory> tempStories = new LinkedList<GoogleStory>();
 			for (String story : stories)
 			{
 				// Split story in to fields
 				String[] rawData = story.split(";;\n");
 				
 				// Splits the keyWords into individual parts
 				String[] words = rawData[4].split(";\n");
 				
 				ArrayList<KeyWord> keyWords = new ArrayList<KeyWord>();
 				for (String word : words)
 				{
 					// Split each keyword into its word and its sentiment
 					String[] bits = word.split("@");
 					// Put each bit into the list
 					keyWords.add(new KeyWord(bits[0], bits[1]));
 				}
 				tempStories.add(new GoogleStory(
 						Double.parseDouble(rawData[3]), rawData[0], rawData[1], keyWords, rawData[2]));
 				
 			}
			gState.getAllSectors().get(j).setGoogStories(tempStories);
 			j++;
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
