 package de.rentoudu.mensa;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 import de.rentoudu.mensa.model.Diet;
 import de.rentoudu.mensa.rss.DownloadRssTask;
 
 public class MainActivity extends FragmentActivity {
 
 	private static final String FEED_URL = "http://www.swfr.de/essen-trinken/speiseplaene/speiseplan-rss/?no_cache=1&Tag={day}&Ort_ID=641";
 	private static final String DIET_FILE = "diet_offline";
 	
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide
      * fragments for each of the sections.
      */
     private DayPagerAdapter dayPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     private ViewPager viewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         Diet diet = loadDietFromInternalStorage();
         if(diet == null) {
         	// Never instantiated activity.
         	diet = fetchDiet();
         	saveDiet(diet);
         } else {
         	
         	// AUTO UPDATE LOGIC
         	Calendar currentDate = Calendar.getInstance();
         	Calendar lastSyncedDate = Calendar.getInstance();
         	lastSyncedDate.setTime(diet.getLastSynced());
         	
             if(
             	// New year, update app.
             	( currentDate.get(Calendar.YEAR)  != lastSyncedDate.get(Calendar.YEAR) )
             	// New week, update app.
             	|| ( currentDate.get(Calendar.WEEK_OF_YEAR)  > lastSyncedDate.get(Calendar.WEEK_OF_YEAR) )
             ) {
             	diet = fetchDiet();
             	saveDiet(diet);
             } else {
             	// Nothing to update.
             }
             
         }
         
        // Create the adapter that will return a DayFragment for each of the pages.
         dayPagerAdapter = new DayPagerAdapter(diet, getResources(), getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         viewPager = (ViewPager) findViewById(R.id.pager);
         viewPager.setAdapter(dayPagerAdapter);
         viewPager.setCurrentItem(getCurrentDayIndex());
     }
     
     /**
      * Returns the current day index.
      */
     protected int getCurrentDayIndex() {
    	int dateBalancer = -2;
    	if(Utils.isAfter(13, 40)) {
    		dateBalancer = -1;
    	}
		return Utils.getDay() + dateBalancer; // by default -2 because an we want to start counting with 0
     }
     
 	/**
 	 * Refreshes the view.
 	 */
     protected void displayDiet(Diet diet) {
     	dayPagerAdapter.setDiet(diet);
     	viewPager.setAdapter(dayPagerAdapter);
         viewPager.setCurrentItem(getCurrentDayIndex());
     }
     
     /**
      * Builds the feed URL to fetch based on the current weekday.
      */
     protected String buildFeedUrl(int startDay) {
     	return FEED_URL.replace("{day}", String.valueOf(startDay));
     }
     
     /**
      * Loads the remote RSS file and parses the current diet.
      * 
      * @see DownloadRssTask#parseRss(InputStream)
      */
     protected Diet fetchDiet() {
     	showToast(getString(R.string.text_diet_fetch));
     	try {
 			String firstWeekFeedUrl = buildFeedUrl( - getCurrentDayIndex() );
 			String secondWeekFeedUrl = buildFeedUrl( - getCurrentDayIndex() + 7 );
 			DownloadRssTask task = new DownloadRssTask();
 			Diet diet = task.execute(firstWeekFeedUrl, secondWeekFeedUrl).get();
 			return diet;
 		} catch (Exception e) {
 			showToast(e.getMessage());
 			return new Diet();
 		}
     }
     
     /**
      * Updates the saved/display diet using the SWFR RSS.
      * 
      * @see DownloadRssTask#parseRss(InputStream)
      */
     protected void updateDiet() {
     	Diet diet = fetchDiet();
     	saveDiet(diet);
     	displayDiet(diet);
     	showToast(getString(R.string.text_diet_synced));
     }
     
     /**
      * Saves the passed diet using the internal storage.
      */
     protected boolean saveDiet(Diet diet) {
     	try {
 			FileOutputStream fos = openFileOutput(DIET_FILE, Context.MODE_PRIVATE);
 			ObjectOutputStream os = new ObjectOutputStream(fos);
 			os.writeObject(diet);
 		    os.close();
 		    fos.close();
 		    return true;
 		} catch (Exception e) {
 			showToast(e.getMessage());
 			return false;
 		}
     }
     
     /**
      * Loads the diet from the internal storage.
      */
     protected Diet loadDietFromInternalStorage() {
     	try {
     		FileInputStream fis = openFileInput(DIET_FILE);
         	ObjectInputStream is = new ObjectInputStream(fis);
         	Diet diet = (Diet) is.readObject();
 			is.close();
 			return diet;
 		} catch (Exception e) {
 			return null;
 		}
     }
 
 	/**
 	 * Inflate the menu; this adds items to the action bar if it is present.
 	 */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.menu_sync:
 	            updateDiet();
 	            return true;
 	        case R.id.menu_about:
 	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        	builder.setMessage(R.string.text_about).setTitle(R.string.menu_about).create().show();
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
     }
     
     /**
      * Shows a toast notification.
      */
     public void showToast(String message) {
     	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
     
     protected void log(String message) {
     	//Log.v("gimmesomebug", message);
     }
 }
