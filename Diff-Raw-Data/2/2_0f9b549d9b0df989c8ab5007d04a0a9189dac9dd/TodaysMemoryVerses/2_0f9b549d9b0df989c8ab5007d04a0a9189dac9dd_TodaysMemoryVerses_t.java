 package com.app.wordservant;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.HapticFeedbackConstants;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TodaysMemoryVerses extends Activity{
 
     private SQLiteDatabase mDatabaseConnection;
 	private Cursor mScriptureQuery;
 	private java.text.DateFormat dbDateFormat;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_todays_memory_verses);
         mDatabaseConnection = new WordServantDbHelper(this, WordServantContract.DB_NAME, null, WordServantDbHelper.DATABASE_VERSION).getWritableDatabase();
         
     }
 	
 	public void onStart(){
     	super.onStart();
     	LinearLayout allSections = (LinearLayout) findViewById(R.id.test);
     	allSections.removeAllViews();
     	//Query for all scriptures that are due today.
     	String [] queryColumns = {
     			WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE,
     			WordServantContract.ScriptureEntry._ID,
     			WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE,
     			WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE}; 
 		dbDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_format), Locale.US);
 
 		for(int i=0;i<4;i++){
 			String whereClause = WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE+" <= date('now','localtime','-"+i+" day')";
 			whereClause += i==0?" OR "+WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE+" = date('now','localtime')":"";
 			mScriptureQuery = mDatabaseConnection.query(false, getResources().getString(R.string.scripture_table_name), queryColumns, whereClause, null, null, null, null, null);
 			
 			if (mScriptureQuery.getCount()==0 && i>0){
 				continue;
 			}
 			
 			//Make a new scripture section.
 			LinearLayout scriptureSection = (LinearLayout) this.getLayoutInflater().inflate(R.layout.review_scripture_layout, null);
 			
 			//Set the date for the section.
 			TextView currentDateDisplayer = (TextView) scriptureSection.getChildAt(0);
 			Calendar currentCalendar = Calendar.getInstance();
 			currentCalendar.add(Calendar.DATE, i*-1);
 	        currentDateDisplayer.setText(DateFormat.format("EEEE, MMMM dd, yyyy", currentCalendar));
 	        
 	        //Add the section to all the sections.
 			allSections.addView(scriptureSection);
 			
 			//Display the scripture list.
 	    	displayScriptureList((ListView) scriptureSection.getChildAt(1), mScriptureQuery, i==0?true:false);
 		}
     }
 	
 	/**
 	 * @author Lewis Gordon
 	 * Queries the database for any memory verses that are due when the screen is accessed.
 	 */
 	private void displayScriptureList(ListView view, Cursor scriptureQuery, final Boolean enabled) {
 		// Set up the adapter that is going to be displayed in the list view.
 		Context context = this.getApplicationContext();
 		final Bundle bundledScriptureList = new Bundle();
         ArrayAdapter<LinearLayout> scriptureAdapter = new ArrayAdapter<LinearLayout>(context, android.R.layout.simple_list_item_1){
             public View getView (int position, View convertView, ViewGroup parent){
                     return (View) this.getItem(position);
             }
 	    };
 	    view.setAdapter(scriptureAdapter);
 		//Queries the database for any verses that have the same review date as the date when the screen was accessed.
 		try{
 			for(int positionOnScreen=0;positionOnScreen<scriptureQuery.getCount();positionOnScreen++){
 				scriptureQuery.moveToNext();
 				
 				//Get the layout from the XML file for checkbox lists.
 				LinearLayout newLayout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.checkbox_list_layout, null);
 				newLayout.setOrientation(LinearLayout.HORIZONTAL);
 				TextView newLabel = (TextView) newLayout.getChildAt(1);
 				newLabel.setText(scriptureQuery.getString(0));
 				final int position = positionOnScreen;
 				bundledScriptureList.putInt(String.valueOf(positionOnScreen), scriptureQuery.getInt(1));
 				
 				//Display a checkbox.
 				CheckBox newCheckBox = (CheckBox) newLayout.getChildAt(0);
 				newCheckBox.setEnabled(enabled);
				if(dbDateFormat.parse(scriptureQuery.getString(2)).getTime()>Calendar.getInstance(Locale.getDefault()).getTimeInMillis()){
 					newCheckBox.setChecked(true);
 				}
 				
 				newCheckBox.setOnClickListener(new OnClickListener(){
 					@Override
 					public void onClick(View v) {
 						// Reset the NEXT_REVIEW_DATE if we are unchecking, otherwise mark the scripture as reviewed.
 						if(!((CheckBox) v).isChecked()){
 							FlashcardScriptureReviewFragment.updateReviewedScripture(TodaysMemoryVerses.this, bundledScriptureList.getInt(String.valueOf(position)), false);
 						}else{
 							FlashcardScriptureReviewFragment.updateReviewedScripture(TodaysMemoryVerses.this, bundledScriptureList.getInt(String.valueOf(position)), true);
 						}
 					}
 					
 				});
 				scriptureAdapter.add(newLayout);
 			}
 		} catch(SQLiteException e){
 			System.err.println("Database issue...");
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// When any of the entries are pressed they can then be edited.
 		view.setOnItemClickListener(new OnItemClickListener(){
 
 			public void onItemClick(AdapterView<?> parent, View dueTodayView, int position,long id) {
 				// Sends the query row that holds the values we would like to edit.
 				if(enabled){
 					LinearLayout view = (LinearLayout) dueTodayView;
 					CheckBox checkBox = (CheckBox) view.getChildAt(0);
 					if(!checkBox.isPressed()){
 						Intent intent = new Intent(TodaysMemoryVerses.this,ScriptureReviewFragmentActivity.class);
 						intent.putExtra("bundledScriptureList",bundledScriptureList);
 						intent.putExtra("positionOnScreen", position);
 				    	startActivity(intent);
 					}
 				}else{
 					dueTodayView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
 					Toast.makeText(getApplicationContext(), "Scripture is disabled.", Toast.LENGTH_SHORT).show();
 				}
 			}
 			
 		});
 		
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_todays_memory_verses, menu);
         return true;
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.menu_settings:
 	        	Intent intent = new Intent(this, Settings.class);
 	        	startActivity(intent);
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
     
 	protected void onDestroy(){
 		super.onDestroy();
 		mDatabaseConnection.close();
 		mScriptureQuery.close();
 	}
 }
