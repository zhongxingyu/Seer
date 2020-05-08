 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Note;
 
 public class ListActivity extends Activity {
 
     public final static String EXTRA_WRITENOTE_NoteId = "NoteId";
     public final static String EXTRA_WRITENOTE_EntryId = "EntryId";
     public final static String EXTRA_MESSAGE = "com.github.groupENIGMA.journalEgocentrique.MESSAGE";
 
     private List<Calendar> daysList;
     private DB dataBase;
     private Entry selectedEntry = null;
     private SharedPreferences sharedPreferences;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         setView();
         dataBase = new DB(getApplicationContext());
 
         // Open the shared preferences file
         sharedPreferences = getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 MODE_PRIVATE
         );
     }
     
     /**
      * Sets dinamically proportioned the size of the Entries, Images and Notes
      */
     private void setView(){
     	Display display = getWindowManager().getDefaultDisplay();
     	int width = display.getWidth();
     	int height = display.getHeight();
     	ListView list = (ListView)findViewById(R.id.list);
     	ImageView photo = (ImageView)findViewById(R.id.dailyPhoto);
     	ImageView mood = (ImageView)findViewById(R.id.emoticon);
     	ListView notes = (ListView)findViewById(R.id.notes);
     	FrameLayout frame = (FrameLayout)findViewById(R.id.frameLayout);
     	// Set the ListView size
     	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)list.getLayoutParams();
     	params.height = height;
     	params.width = width/4;
     	list.setLayoutParams(params);
     	// Set the FrameLayout
     	params = (RelativeLayout.LayoutParams)frame.getLayoutParams();
     	params.height = height;
     	params.width = width*3/4;
     	frame.setLayoutParams(params);
     	// Set the photo
     	FrameLayout.LayoutParams param = (FrameLayout.LayoutParams)photo.getLayoutParams();
     	param.width = width/2;
     	param.height = height/2;
     	photo.setLayoutParams(param);
     	// Set the mood
     	param = (FrameLayout.LayoutParams)mood.getLayoutParams();
     	param.width = width/2;
     	param.height = height/3;
     	mood.setLayoutParams(param);
     	// Set the notes
     	param = (FrameLayout.LayoutParams)notes.getLayoutParams();
     	param.width = width/4;
     	param.height = height;
     	notes.setLayoutParams(param);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // Open database connection
         dataBase.open();
         // Display the list of days with an Entry
         daysList = dataBase.getDays();
         ListView daysListView = (ListView)findViewById(R.id.list);
         displayDaysList(daysListView, daysList);
 
         // Display the last viewed Entry (if any)
         SharedPreferences pref = getPreferences(MODE_PRIVATE);
         long id = pref.getLong("Id", -1);
         if(id != -1) {
             selectedEntry = dataBase.getEntry(id);
             // Display the Photo and Mood Image
             displayImages();
             // Display the Notes
             ListView notesListView = (ListView)findViewById(R.id.notes);
             displayNotes(notesListView);
         }
         else {
             selectedEntry = null;
         }
     }
 
     /**
      * Display the list of all Days having an associated Entry
      * It is also created a OnItemClickListener that at the click will display
      * the details of the day.
      *
      * @param list The list to populate.
      * @param entry With this List we will populate the ListView
      */
     private void displayDaysList(ListView list, List<Calendar> entry){
         // Create and set the custom ArrayAdapter DaysArrayAdapter
         DaysArrayAdapter arrayAdapter = new DaysArrayAdapter(
                 this, R.layout.row, entry
         );
         list.setAdapter(arrayAdapter);
 
         // Set the listener
         OnItemClickListener clickListener = new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapter, View view,
                                     int position, long id) {
                 selectedEntry = dataBase.getEntry(
                         (Calendar)adapter.getItemAtPosition(position)
                 );
                 // Refresh notes and images
                 displayImages();
                 // Display the Notes
                 ListView notesListView = (ListView)findViewById(R.id.notes);
                 displayNotes(notesListView);
             }
         };
         list.setOnItemClickListener(clickListener);
     }
 
     /**
      * Displays the Notes of the selectedEntry
      *
      * @param list The ListView that will be used to display the Entry
      */
     private void displayNotes(ListView list) {
         // Display the Notes
         List<Note> notes = selectedEntry.getNotes();
         ArrayAdapter<Note> arrayAdapter = new ArrayAdapter<Note>(
                 this, R.layout.row, R.id.textViewList, notes
         );
         list.setAdapter(arrayAdapter);
 
         // Add the onLongClickListener that activates the WriteNote activity
         // that can be used to update the Note text
         OnItemLongClickListener clickListener = new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> adapter, View view,
                 int position, long id) {
                 // Enable the onLongClickListener only if the Note can be
                 // updated.
                 Note selectedNote = (Note) adapter.getItemAtPosition(position);
                 if (selectedNote.canBeUpdated(sharedPreferences)) {
                     Intent intent = new Intent(
                             getApplicationContext(), WriteNote.class
                     );
                     intent.putExtra(
                             EXTRA_WRITENOTE_NoteId, selectedNote.getId()
                     );
                     startActivity(intent);
                     return true;
                 }
                 // The Note can't be updated
                 else {
                     return false;
                 }
             }
         };
         list.setOnItemLongClickListener(clickListener);
     }
 
     /**
      * Sets the correct image for photo and mood selected by the user.
      */
     private void displayImages(){
         /*
         * No selected entry; display the default images
         */
         if(selectedEntry == null){
             ImageView img = (ImageView) findViewById(R.id.dailyPhoto);
             img.setImageResource(R.drawable.ic_launcher);
             img = (ImageView)findViewById(R.id.emoticon);
             img.setImageResource(R.drawable.ic_launcher);
         }
         /*
          * Entry selected: display its images (if any) or the default ones
          * If the Entry is editable also add the listeners that activate
          * MoodActivity and PhotoActivity.to change the Mood and Photo
          */
         else{
             boolean editable = selectedEntry.canBeUpdated();
             ImageView img = (ImageView) findViewById(R.id.dailyPhoto);
             if(selectedEntry.getPhoto() != null)
                 img.setImageURI(Uri.parse(selectedEntry.getPhoto().getPath()));
             else
                 img.setImageResource(R.drawable.ic_launcher);
             ImageView mood = (ImageView)findViewById(R.id.emoticon);
             if(selectedEntry.getMood() == null)
                 mood.setImageResource(R.drawable.ic_launcher);
             else
                 mood.setImageResource((selectedEntry.getMood().getEmoteId(getApplicationContext())));
             if(editable){
                 img.setOnTouchListener(new OnTouchListener()
                 {
                     @Override
                     public boolean onTouch(View v, MotionEvent event)
                     {
                         // qui carica la vista per la fotoCamera
                         Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);//ho messo PhotoActivity.class
                         intent.putExtra(EXTRA_MESSAGE, selectedEntry.getId());
                         startActivity(intent);
                         return false;
                     }
                 });
                 mood.setOnTouchListener(new OnTouchListener()
                 {
                     @Override
                     public boolean onTouch(View v, MotionEvent event)
                     {
                         // qui carica la vista per il moood
                         Intent intent = new Intent(getApplicationContext(), MoodActivity.class);//ho messo MoodActivity.class
                        intent.putExtra(EXTRA_MESSAGE, selectedEntry.getId());
                         startActivity(intent);
                         return false;
                     }
                 });
             }
         }
     }
 
     @Override
     protected void onPause(){
         super.onPause();
         // Save selected Entry
         SharedPreferences pref = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor edit = pref.edit();
         edit.putLong("Id", selectedEntry.getId());
         edit.commit();
         // Close database connection
         dataBase.close();
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.option, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.newEntry:
 	            selectedEntry = dataBase.createEntry();
 	            Log.e("New Entry", selectedEntry.getId()+"");//debug
 	            daysList = dataBase.getDays();
 	    	    ListView list = (ListView)findViewById(R.id.list);
 	    	    displayDaysList(list, daysList);
 	            return true;
             case R.id.newNote:
                 Intent intent = new Intent(
                         getApplicationContext(), WriteNote.class
                 );
                 intent.putExtra(EXTRA_WRITENOTE_NoteId, -1L);
                 intent.putExtra(EXTRA_WRITENOTE_EntryId, selectedEntry.getId());
                 startActivity(intent);
                 return true;
 	        case R.id.settings:
 	        	Intent settings = new Intent(getApplicationContext(), Settings.class);
 	        	startActivity(settings);
 	        	return true;
 	        case R.id.deleteEntry:
 	        	if(selectedEntry != null){
 	        		//dataBase.deleteEntry(selectedEntry);
 	        		return true;
 	        	}
 	        case R.id.gallery:
 	        	Intent gallery = new Intent(getApplicationContext(), GalleryActivity.class);
 	        	startActivity(gallery);
 	    }
 		return false;
 	}
 }
