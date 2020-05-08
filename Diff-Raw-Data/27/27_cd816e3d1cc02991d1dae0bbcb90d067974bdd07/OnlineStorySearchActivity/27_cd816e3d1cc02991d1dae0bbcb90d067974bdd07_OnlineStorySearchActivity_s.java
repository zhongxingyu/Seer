 
 package ualberta.g12.adventurecreator.views;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import ualberta.g12.adventurecreator.R;
 import ualberta.g12.adventurecreator.data.TitleAuthor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 public class OnlineStorySearchActivity extends Activity implements OnItemClickListener {
 
     private List<TitleAuthor> titleAuthors;
     private ListView listView;
     private StoryAuthorMapListAdapter adapter;
     private static boolean downloadMode;
     private String query;
 
     private static final String TAG = "OnlineStorySearchActivity";
     private static final boolean DEBUG = true;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_online_story_search);
 
         setupActionBar();
 
         // Load our title authors from buddy
         loadTitleAuthors();
 
         setUpUi();
 
         // See who started us
         handleIntent(getIntent());
     }
 
     private void setupActionBar() {
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     private void loadTitleAuthors() {
         titleAuthors = new ArrayList<TitleAuthor>();
         titleAuthors.add(new TitleAuthor("And who", "Are you"));
         titleAuthors.add(new TitleAuthor("The proud lord said", "That I must bow so low?"));
         titleAuthors.add(new TitleAuthor("Only a cat", "of a different coat,"));
         titleAuthors.add(new TitleAuthor("that's all", "the truth I know."));
         titleAuthors.add(new TitleAuthor("In a coat of gold", "or a coat of red"));
         titleAuthors.add(new TitleAuthor("a lion still has claws", ""));
         titleAuthors.add(new TitleAuthor("And mine are long and sharp", "my lord"));
         titleAuthors.add(new TitleAuthor("as long and sharp as yours", ""));
         titleAuthors.add(new TitleAuthor("And so he spoke", "and so he spoke"));
         titleAuthors.add(new TitleAuthor("that lord of", "Castamere"));
     }
 
     private void setUpUi() {
         listView = (ListView) findViewById(R.id.online_story_search_listview);
         adapter = new StoryAuthorMapListAdapter(this, R.layout.listview_story_list, titleAuthors);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(this);
     }
 
     private void handleIntent(Intent i) {
         // Set up the DownloadMode flag
         downloadMode = i.getBooleanExtra(OnlineStoryViewActivity.DOWNLOAD_MODE, true);
 
         /*
          * If this is null then we don't have a search term and we don't have to
          * change anything
          */
         if (i.getStringExtra(SearchManager.QUERY) != null) {
             if (DEBUG)
                 Log.d(TAG, "There was some stuff to search");
             String query = i.getStringExtra(SearchManager.QUERY).toLowerCase(Locale.CANADA);
             if (DEBUG)
                 Log.d(TAG, String.format("Search query was: %s", query));
             this.query = query;
             performSearch();
         }
     }
 
     private void performSearch() {
         if (this.titleAuthors != null && this.query != null) {
             if (DEBUG)
                 Log.d(TAG, String.format("Searching for query: %s", this.query));
 
             List<TitleAuthor> tas = new ArrayList<TitleAuthor>();
             tas.addAll(titleAuthors);
             titleAuthors.clear();
             if (DEBUG)
                 Log.d(TAG, String.format("%d TitleAuthors to search from", tas.size()));
             for (TitleAuthor t : tas) {
                 if (t.title == null || t.author == null) {
                     // Do nothing as this is a title author with a null title or
                     // author
                 } else {
                     if (t.title.toLowerCase(Locale.CANADA).contains(this.query)
                            || t.author.toLowerCase(Locale.CANADA).contains(this.query))
                        ;
                    if (DEBUG)
                        Log.d(TAG, String.format("Adding story %s", t.title));
                    titleAuthors.add(t);
                 }
             }
             adapter.notifyDataSetChanged();
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         downloadMode = settings.getBoolean(OnlineStoryViewActivity.DOWNLOAD_MODE, true);
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean(OnlineStoryViewActivity.DOWNLOAD_MODE, downloadMode);
 
         editor.commit();
     }
 
     @Override
     protected void onNewIntent(Intent i) {
         setIntent(i);
         handleIntent(i);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.online_story_search, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
         // Download or stream story
         TitleAuthor ta = titleAuthors.get(pos);
         if (downloadMode) {
             // Download story lol
             Toast.makeText(this, String.format("Downloading story %s", ta.title),
                     Toast.LENGTH_SHORT).show();
 
             /*
              * DownloadStory ds = new DownloadStory(); ds.execute(new
              * TitleAuthor[] {ta});
              */
 
         } else {
             // Stream story
             Toast.makeText(this, String.format("Loading story %s", ta.title), Toast.LENGTH_SHORT)
                     .show();
             // Send some stuff to FragmentViewActivity
         }
     }
 }
