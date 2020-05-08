 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.*;
 import android.view.View.OnTouchListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Note;
 
 public class ListActivity extends Activity {
 
     public final static String EXTRA_WRITENOTE_NoteId = "NoteId";
     public final static String EXTRA_WRITENOTE_EntryId = "EntryId";
     public final static String EXTRA_MESSAGE = "com.github.groupENIGMA.journalEgocentrique.MESSAGE";
 
     private final static String PREF_SELECTED_ENTRY = "selectedEntry_id";
 
     private DB dataBase;
     private DaysArrayAdapter daysListArrayAdapter;
     private Entry selectedEntry = null;
     private SharedPreferences sharedPreferences;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         dataBase = new DB(getApplicationContext());
 
         // Open the shared preferences file
         sharedPreferences = getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 MODE_PRIVATE
         );
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // Open database connection
         dataBase.open();
         // Display the list of days with an Entry
         displayDaysList();
 
         // Display the last viewed Entry (if any)
         SharedPreferences pref = getPreferences(MODE_PRIVATE);
         long id = pref.getLong(PREF_SELECTED_ENTRY, -1L);
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
 
         // If the Entry for today already exists disable the AddEntry button
         if (dataBase.existsEntry()) {
             Button addEntry = (Button)findViewById(R.id.ListDaysAddEntryButton);
             addEntry.setEnabled(false);
         }
     }
 
     /**
      * Display the list of all Days having an associated Entry
      * It is also created a OnItemClickListener that at the click will display
      * the details of the day.
      */
     private void displayDaysList(){
         // Get the ListView that display the days
         ListView daysListView = (ListView)findViewById(R.id.daysList);
 
         // Get the list of available days from the database
         List<Calendar> daysList = dataBase.getDays();
 
         // Create and set the custom ArrayAdapter DaysArrayAdapter
         daysListArrayAdapter = new DaysArrayAdapter(
                 this, R.layout.row, daysList
         );
         daysListView.setAdapter(daysListArrayAdapter);
 
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
                 // Adds the header with the photos
                 View headerView = ((LayoutInflater) getApplicationContext()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                         .inflate(R.layout.main_detail_photos, null, false);
                 notesListView.addHeaderView(headerView);
             }
         };
         daysListView.setOnItemClickListener(clickListener);
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
                         intent.putExtra("EntryId", selectedEntry.getId());
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
         // Get the preference file
         SharedPreferences pref = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor edit = pref.edit();
         // Save selected Entry (if any)
         if (selectedEntry == null) {
             edit.putLong(PREF_SELECTED_ENTRY, -1L);
         }
         else {
             edit.putLong(PREF_SELECTED_ENTRY, selectedEntry.getId());
         }
         edit.commit();
         // Close database connection
         dataBase.close();
     }
 
     /**
      * Adds an Entry for today to the database and to the displayed list.
      * Used by ListDaysAddEntryButton in main.xml
      *
      * @param view as required by android:onClick xml attribute. Not used.
      */
     public void addTodayEntry(View view) {
         // New Entry in the database
         selectedEntry = dataBase.createEntry();
         // Entry to the beginning of the displayed list
         daysListArrayAdapter.insert(selectedEntry.getDay(), 0);
         // Disable the ListDaysAddEntryButton
         Button addEntry = (Button)findViewById(R.id.ListDaysAddEntryButton);
         addEntry.setEnabled(false);
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
                     // Removed it from the database
 	                dataBase.deleteEntry(selectedEntry);
                     // Remove it from the displayed list
                     daysListArrayAdapter.remove(selectedEntry.getDay());
                     selectedEntry = null;
 	                return true;
 	            }
 	        case R.id.gallery:
 	        	Intent gallery = new Intent(getApplicationContext(), GalleryActivity.class);
 	        	startActivity(gallery);
                 return true;
 	        case R.id.share:
 	        	Intent share = new Intent(getApplicationContext(), ShareActivity.class);
 	        	share.putExtra("EntryId", selectedEntry.getId());
 	        	startActivity(share);
                 return true;
 	    }
 		return false;
 	}
 }
