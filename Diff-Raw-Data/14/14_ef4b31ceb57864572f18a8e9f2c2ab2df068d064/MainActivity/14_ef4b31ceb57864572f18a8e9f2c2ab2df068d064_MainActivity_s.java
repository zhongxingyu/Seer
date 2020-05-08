 
 package ualberta.g12.adventurecreator.views;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import ualberta.g12.adventurecreator.R;
 import ualberta.g12.adventurecreator.controllers.StoryListController;
 import ualberta.g12.adventurecreator.data.AdventureCreator;
 import ualberta.g12.adventurecreator.data.Fragment;
 import ualberta.g12.adventurecreator.data.OfflineIOHelper;
 import ualberta.g12.adventurecreator.data.Story;
 import ualberta.g12.adventurecreator.data.StoryList;
 import ualberta.g12.adventurecreator.tasks.TryPublishStoryTask;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 /**
  * Activity displayed for the start of the application. Allows the user to
  * browse through available stories, and edit or read those stories depending on
  * what mode they are in.
  */
 public class MainActivity extends Activity implements LView<StoryList>, OnItemClickListener {
 
     private List<Story> stories;
     private static StoryList storyList;
     private static StoryListController storyListController;
 
     private ListView listView;
     private Button onlineButton;
     private Button randOfflineButton;
     private StoryListArrayAdapter adapter;
     private OfflineIOHelper offlineHelper;
     private Story currentRandStory;
 
     public static final String IS_AUTHOR_FLAG = "isAuthor";
     private static boolean isAuthor = false;
 
     private static final int help = Menu.FIRST;
 
     // Logging
     private static final boolean DEBUG_LOG = true;
     private static final String TAG = "MainActivity";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Get our storyList instance from the application
         storyList = AdventureCreator.getStoryList();
 
         // Get some storyListController
         storyListController = AdventureCreator.getStoryListController();
 
         // Load our local stories from the StoryList Model
         stories = storyList.getAllStories();
         listView = (ListView) findViewById(R.id.main_activity_listview);
         onlineButton = (Button) findViewById(R.id.main_activity_start_online_mode);
         onlineButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 startActivity(new Intent(getApplicationContext(), OnlineStoryViewActivity.class));
             }
         });
 
         randOfflineButton = (Button) findViewById(R.id.main_activity_random_story);
         randOfflineButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 if (!isAuthor && storyList.getAllStories().size() > 0) {
                     // http://stackoverflow.com/questions/363681/generating-random-numbers-in-a-range-with-java
                     Random random = new Random();
                     int max = storyList.getAllStories().size() - 1;
                     int randPos = random.nextInt((max - 0) + 1) + 0;

                    currentRandStory = storyList.getStoryAtPos(randPos);
                    int fragPos = currentRandStory.getStartFragPos();

                    Fragment goToFrag = currentRandStory.getFragmentAtPos(fragPos);
                     Intent i = new Intent(getApplicationContext(), FragmentViewActivity.class);
                    // http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
                    AlertDialog.Builder popup = new AlertDialog.Builder(getApplicationContext());

                    popup.setTitle("Title");
                    popup.setMessage("Message");
                    i.putExtra("Fragment", goToFrag);
                     startActivity(i);
                 }
             }
         });
 
         setupListView();
 
         // Lets see who started us
         handleIntent(getIntent());
 
         offlineHelper = AdventureCreator.getOfflineIOHelper();
     }
 
     private void setupListView() {
         // Set up ListView Stuff
         adapter = new StoryListArrayAdapter(this, R.layout.listview_story_list, stories);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(this);
 
         listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                     long id) {
                 AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                 builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         tryPublishStory(position);
                     }
                 });
                 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         // Cancel the dialog.
 
                     }
                 });
                 builder.setTitle("Do you want to publish this story?");
                 AlertDialog dialog = builder.create();
                 dialog.show();
                 // We don't want onItemClickListener to fire
                 return true;
             }
 
         });
     }
 
     /**
      * @param position the position of the story
      */
     private void tryPublishStory(int position) {
         Story s = stories.get(position);
         TryPublishStoryTask tryPublishTask = new TryPublishStoryTask(this);
         tryPublishTask.execute(new Story[] {
                 s
         });
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         // If we got searched lets search
         setIntent(intent);
         handleIntent(intent);
     }
 
     private void handleIntent(Intent intent) {
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             String query = intent.getStringExtra(SearchManager.QUERY).toLowerCase(Locale.CANADA);
             Intent searchIntent = new Intent(getApplicationContext(), StorySearchActivity.class);
             searchIntent.putExtra(SearchManager.QUERY, query);
             searchIntent.putExtra(MainActivity.IS_AUTHOR_FLAG, isAuthor);
             startActivity(searchIntent);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void onStart() {
         super.onStart();
         // Add ourself to the StoryList Model
         storyList.addView(this);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void onDestroy() {
         super.onDestroy();
         // Remove ourselves from the story list
         storyList.deleteView(this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         isAuthor = settings.getBoolean(IS_AUTHOR_FLAG, false);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean(IS_AUTHOR_FLAG, isAuthor);
 
         // Commit the edits
         editor.commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
 
         // Associate searchable configuration with the searchview
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView = (SearchView) menu.findItem(R.id.story_search).getActionView();
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 
         super.onCreateOptionsMenu(menu);
         menu.add(0, help, 0, R.string.help);
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item)
     {
         switch (item.getItemId())
         {
             case help:
                 Intent i = new Intent(this, HelpScreen.class);
                 startActivity(i);
                 return true;
         }
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // Make sure check box is checked if it needs to be
         menu.findItem(R.id.menu_check_box_author).setChecked(isAuthor);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_add_story:
                 // Launch the StoryEditActivity with the id of NEW_STORY
                 int storyPos = storyList.getAllStories().size();
                 storyListController.createBlankStory();
 
                 // save the newly added story
                 offlineHelper.saveOfflineStories(storyList);
 
                 Intent i = new Intent(this, StoryEditActivity.class);
                 i.putExtra("StoryPos", storyPos);
                 startActivity(i);
                 return true;
 
             case R.id.menu_check_box_author:
                 if (DEBUG_LOG)
                     Log.d(TAG,
                             String.format("Checkbox checked, current value: %s", item.isChecked()));
 
                 boolean alreadyChecked = item.isChecked();
                 // Set it to the opposite of what it previously was
                 item.setChecked(!alreadyChecked);
 
                 // Set the flag now
                 isAuthor = item.isChecked();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     /**
      * Reloads the story listview from StoryList Model by notifying the ListView that 
      * a change has occurred and will refresh all stories. The refreshing is done via 
      * an adapter.
      * 
      *  @param model current model that is being used     
      */
     public void update(StoryList model) {
         // Reload our stories from StoryList Model
         stories = model.getAllStories();
         // Notify Our ListView that our array has changed
         adapter.notifyDataSetChanged();
     }
 
     @Override
     /**
      * Function that deals with listview clicks within the list of stories. 
      * Will open either edit or view story activities depending whether or 
      * not "Author Mode" is enabled thought the menu.
      * 
      *  @param v    mode that is currently selected (either author or reader)
      *  @param pos  position that the stories is in within the listview
      *  @param id   id of the selected story
      */
     public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
         // Open Edit/View story Activity with
         Intent i;
         if (isAuthor) {
             i = new Intent(this, StoryEditActivity.class);
             i.putExtra("StoryPos", pos);
             startActivity(i);
         } else {
             Story story = storyList.getStoryAtPos(pos);
             i = new Intent(this, FragmentViewActivity.class);
             i.putExtra("Story", story);
             startActivity(i);
         }
     }
 }
