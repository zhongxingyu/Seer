 package com.pcpsoftware.tipped;
 
 
 import android.app.AlertDialog;
 //import android.app.ListActivity;
 //import android.app.LoaderManager;
 import android.content.Context;
 //import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 //import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 //import android.view.ContextMenu;
 //import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 //import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 import com.pcpsoftware.tipped.database.DatabaseOpenHelper;
 import com.pcpsoftware.tipped.database.Shift;
 import com.pcpsoftware.tipped.database.ShiftTable;
 //import com.pcpsoftware.tipped.contentprovider.ShiftsContentProvider;
 //import com.pcpsoftware.tipped.contentprovider.TipContentProvider;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 
 public class MainView extends FragmentActivity implements
 		LoaderCallbacks<Cursor> {
 	long shift;
 	private DatabaseOpenHelper dbOpenHelper;
     private ShiftListAdapter shiftAdapter;
 //    private SimpleCursorAdapter adapter;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main_view);
         
         //Get references to button and fake tabs
         Button mainShiftsTab = (Button) findViewById(R.id.mainShiftsTab);
         Button mainStatsTab = (Button) findViewById(R.id.mainStatsTab);
         Button mainOptionsTab = (Button) findViewById(R.id.mainOptionsTab);
         ImageButton addShiftButton = (ImageButton) findViewById(R.id.addShiftButton);
         
         // set up the ListView for shifts
         Tipped.getInstance().tipdb.open();
 //        Cursor shiftCursor = Tipped.getInstance().tipdb.getAllShifts();
        
 //        startManagingCursor(shiftCursor);
         //TODO: use cursorLoader
         
 //        shiftAdapter = new SimpleCursorAdapter(
 //        		this,
 //        		android.R.layout.two_line_list_item,
 //        		allShifts,
 //        		new String [] {"time"},
 //        		new int[] {android.R.id.text1}
 //        		);
 //        getListView().setLongClickable(true);
 //        getListView().setOnItemClickListener(editShiftItemListener);
 //        getListView().setOnItemLongClickListener(longDeleteShiftItemListener);
         
 //        shiftAdapter = new ShiftListAdapter(this, shiftCursor);
 //        setListAdapter(shiftAdapter);
         
         // set up itemClickListener for list items
         ListView list = (ListView) findViewById(R.id.mainListView);
         list.setOnItemClickListener(editShiftItemListener);
         list.setOnItemLongClickListener(longDeleteShiftItemListener);
         
        fillData();
         
         //Set listeners for buttons
         mainShiftsTab.setOnClickListener(mainShiftsTabListener);
         mainStatsTab.setOnClickListener(mainStatsTabListener);
         mainOptionsTab.setOnClickListener(mainOptionsTabListener);
         addShiftButton.setOnClickListener(mainAddListener);
         
         //refreshGUI(null); // refresh the GUI
         
     }
     
     @Override
     protected void onResume() {
     	super.onResume(); // call super's onResume method
     } // end method onResume
      
     
     
     //***BEGIN LISTENER DEFINITIONS***
     
     // listener for main shifts button
     // FIXME: Do we need this? On main view, this button will do nothing, so probably not.
     public OnClickListener mainShiftsTabListener = new OnClickListener()
     {
        @Override
        public void onClick(View v) 
        {
     	   //FIXME: Implement
        } // end method onClick
     }; // end OnClickListener anonymous inner class
     
     // listener for main stats button
     // switches to stats view
     public OnClickListener mainStatsTabListener = new OnClickListener()
     {
        @Override
        public void onClick(View v) 
        {
     	   //Create an intent for the stats view, and launch it
     	   Intent statsIntent = new Intent(MainView.this, MainStats.class);
     	   MainView.this.startActivity(statsIntent);
     	   
     	   //FIXME: Is there anything else we need to do, or is it really that easy?
     	   
        } // end method onClick
     }; // end OnClickListener anonymous inner class
     
     // listener for main options button
     // switches to options view
     public OnClickListener mainOptionsTabListener = new OnClickListener()
     {
        @Override
        public void onClick(View v) 
        {
     	   //Create an intent for the options view, and launch it
     	   Intent optionsIntent = new Intent(MainView.this, Options.class);
     	   MainView.this.startActivity(optionsIntent);
     	   
        } // end method onClick
     }; // end OnClickListener anonymous inner class
     
     // listener for main add button
     // adds a new shift entry
     public OnClickListener mainAddListener = new OnClickListener()
     {
        @Override
        public void onClick(View v) 
        {
     	   
     	   
     	   //Create a new shift and add to the database
     	   Shift s = new Shift();
     	   long id = Tipped.getInstance().tipdb.addShift(s);
     	   
     	   //Launch shift view
     	   Intent shiftIntent = new Intent(MainView.this, ShiftView.class);
     	   shiftIntent.putExtra("shift_id", id); //this will pass the shift to the shift view
     	   MainView.this.startActivity(shiftIntent);
     	   
        } // end method onClick
     }; // end OnClickListener anonymous inner class
     
     public OnClickListener deleteShiftListener = new OnClickListener() {
     	@Override
     	public void onClick(View v) {
     		Tipped.getInstance().tipdb.deleteShift(shift);	//remove shift from database
     		
     		// TODO comment
     		shiftAdapter.changeCursor(Tipped.getInstance().tipdb.getAllShifts());
             shiftAdapter.notifyDataSetChanged();
     	}
     };
     
     public OnItemClickListener editShiftItemListener = new OnItemClickListener() {
     	@Override
     	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
     		//get the shift id
     		Cursor c = (Cursor) parent.getItemAtPosition(position);
     		shift = c.getLong(c.getColumnIndex("_id"));
     		//String[] projection = new String[] {ShiftTable.COLUMN_ID};
     		//Cursor c = getContentResolver().query(ShiftsContentProvider.CONTENT_URI, projection, null, null, null);
     		
     		
     		// Launch shiftView
     		Intent shiftIntent = new Intent(MainView.this, ShiftView.class);
 //    		Uri shiftUri = Uri.parse(ShiftsContentProvider.CONTENT_URI + "/" + id);
 //    		shiftIntent.putExtra(ShiftsContentProvider.CONTENT_ITEM_TYPE, shiftUri);
     		shiftIntent.putExtra("shift_id", id); // this should, if I read it right, pass the shift id of the clicked shift and delete it
     		MainView.this.startActivity(shiftIntent);
     		
     		//TODO
     	}
     };
     
     public OnItemLongClickListener longDeleteShiftItemListener = new OnItemLongClickListener() {
     	@Override
     	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
     		
     		Shift s = new Shift();
     		s.setId(id);
     		
     		final long itemId = shift;
     		
     		AlertDialog.Builder builder = 
     				new AlertDialog.Builder(MainView.this);
     		builder.setTitle(R.string.delete_title);
     		builder.setMessage(R.string.delete_message);
     		
     		builder.setNegativeButton(R.string.cancel, null);
     		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
 				
     			final long idToRemove = itemId;
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					Tipped.getInstance().tipdb.deleteShift(idToRemove);
 					shiftAdapter.changeCursor(Tipped.getInstance().tipdb.getAllShifts());
 					shiftAdapter.notifyDataSetChanged();
 				}
 			});
     		builder.show();
     		
 //    		LinearLayout shiftTableItem = (LinearLayout) v.getParent();
 //    		((ViewGroup) shiftTableItem.getParent()).removeView(shiftTableItem);
     		
     		//Tipped.getInstance().tipdb.deleteShift(id);	//remove shift from database
     		
     		return true;
     	}
     };
     //***END LISTENER DEFINITIONS***
 
     //  fills the list with data
     private void fillData() {
     	// Fields from the database (projection) must include 
     	// the _id column for the adapter to work
 //    	String[] from = new String[] {ShiftTable.COLUMN_DATE, ShiftTable.COLUMN_TOTAL};
     	// Fields on the UI to which we map
 //    	int[] to = new int[] {R.id.newShiftDateTextView, R.id.newShiftTotalTextView};
     	
     	getSupportLoaderManager().initLoader(0, null, this);
 //    	adapter = new SimpleCursorAdapter(this, R.layout.main_view_item, null, from, to, 0);
     	shiftAdapter = new ShiftListAdapter(this, null);
     	
 //    	setListAdapter(adapter);
     }
     
     public static final class ShiftCursorLoader extends SimpleCursorLoader {
     	private DatabaseOpenHelper mHelper;
     	Context context;
     	
     	public ShiftCursorLoader(Context context, DatabaseOpenHelper helper) {
     		super(context);
     		mHelper = helper;
     		this.context = context;
     	}
     	
     	@Override
     	public Cursor loadInBackground() {
     		Cursor cursor = null;
     		DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(context);
     		SQLiteDatabase db = dbHelper.getReadableDatabase();
     		
     		cursor = db.query(ShiftTable.TABLE_SHIFT, new String[] {ShiftTable.COLUMN_DATE, ShiftTable.COLUMN_TOTAL}, null, null, null, null, null);
     		
     		return cursor;
     	}
     }
     
     /*Start Loader Classes */
     // Creates a new loader after the initLoader() call
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
     	String[] projection = {ShiftTable.COLUMN_ID, ShiftTable.COLUMN_DATE, ShiftTable.COLUMN_TOTAL };
 //    	CursorLoader cursorLoader = new CursorLoader(this, 
 //    			ShiftsContentProvider.CONTENT_URI, projection, null, null, null);
     	
     	return new ShiftCursorLoader(this, null);
 //    	return cursorLoader;
     }
     
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	shiftAdapter.swapCursor(data);
     }
     
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
     	// Data is not available anymore, delete reference
     	shiftAdapter.swapCursor(null);
     }
     /*End Loader Classes*/
     
     // custom adapter to build the listview and interact with database
     public class ShiftListAdapter extends CursorAdapter {
     	private final LayoutInflater lInflater;
     	private Context mContext;
     	
     	public ShiftListAdapter(Context context, Cursor c) {
     		super(context, c, false);
     		mContext = context;
     		lInflater = LayoutInflater.from(mContext);
     	}
     	
     	@Override
     	public View newView(Context context, Cursor cursor, ViewGroup parent) {
     		return lInflater.inflate(R.layout.main_view_item, parent, false);
     	}
     	
     	@Override
         public View getView(final int position, View convertView, final ViewGroup parent) 
         {
         	
     		Log.d("trace","getView called");
     		
         	if(convertView == null)
         	{
         		convertView = lInflater.inflate(R.layout.main_view_item, parent, false);
         	}
 
         	
         	 ImageButton deleteShiftButton = (ImageButton) convertView.findViewById(R.id.newDeleteShiftButton);
         	       	 
              deleteShiftButton.setOnClickListener(new OnClickListener()
              {
             	 @Override
             	 public void onClick(View v) {
             		//get id of current tip
              		Cursor c = (Cursor) ((AdapterView<?>) parent).getItemAtPosition(position);
             		shift = c.getLong(c.getColumnIndex("_id"));
             		
          			Tipped.getInstance().tipdb.deleteShift(shift); //delete shift from database
 
          	    	shiftAdapter.changeCursor(Tipped.getInstance().tipdb.getAllShifts()); //refresh cursor
          	    	shiftAdapter.notifyDataSetChanged(); //refresh ListView 
             	 }
              });
              
              return super.getView(position, convertView, parent);
 
         }
     	
     	@Override
     	public void bindView(View view, Context context, Cursor cursor) {
     		// load shift amounts and times in the background.
     		//new LoadShiftsTask().execute(view, context, cursor);
     		
     		long time = cursor.getLong(cursor.getColumnIndex("time"));
 //    		int type = cursor.getInt(cursor.getColumnIndex("shift_type"));
 //    		double shiftTotal = Tipped.getInstance().tipdb.getShiftTotal(cursor.getLong(cursor.getColumnIndex("_id")));
     		double shiftTotal = cursor.getDouble(cursor.getColumnIndex(ShiftTable.COLUMN_TOTAL));
     		
     		Calendar calendar = Calendar.getInstance();
     		calendar.setTimeInMillis(time);
     		
     		String dateFormat = "MM/dd \nhh:mm:ss a";
     		DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
     		String dateString = formatter.format(calendar.getTime());
     		
 //    		String shiftTypeString;
 //    		switch (type) {
 //    		case 1:
 //    			shiftTypeString = "Breakfast";
 //    			break;
 //    		case 2:
 //    			shiftTypeString = "Lunch";
 //    			break;
 //    		case 3:
 //    			shiftTypeString = "Dinner";
 //    			break;
 //    		default:
 //    			shiftTypeString =  "";
 //    			break;
 //    		}
     		
     		String amountString = "$ " + String.valueOf(shiftTotal);
     		
     		((TextView) view.findViewById(R.id.newShiftDateTextView)).setText(dateString);
 //    		((TextView) view.findViewById(R.id.newShiftTypeTextView)).setText(shiftTypeString);
     		((TextView) view.findViewById(R.id.newShiftTotalTextView)).setText(amountString);
     		
     		//ImageButton newDeleteButton = (ImageButton) view.findViewById(R.id.newDeleteShiftButton);
     		//newDeleteButton.setOnClickListener(deleteShiftListener);
     	}
     }  
     
 //    private class LoadShiftsTask extends AsyncTask<Object, Void, Object[]> {
 //    	@Override
 //    	protected Object[] doInBackground(Object...params) {
 //    		
 //    		// an array to hold string values to pass to onPostExecute
 //    		//String[] textViews = null; 
 //    		
 //    		// grab the Cursor passed into the AsyncTask via Object array
 //    		Cursor cursor = (Cursor) params[2];
 //    		
 //    		// get the unix Timstamp associated with this shift
 //    		long time = cursor.getLong(cursor.getColumnIndex("time"));
 //    		
 //    		// get the total associated with shift
 //    		double shiftTotal = Tipped.getInstance().tipdb.getShiftTotal(
 //    				cursor.getLong(cursor.getColumnIndex("_id")));
 //    		
 //    		Calendar calendar = Calendar.getInstance(); // get an instance of Calendar to set and get time
 //    		calendar.setTimeInMillis(time); // set the time with the long type unix Timestamp stored in the database
 //    		
 //    		String dateFormat = "MM/dd \nhh:mm:ss a";
 //    		DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
 //    		String dateString = formatter.format(calendar.getTime());
 //    		
 //    		String amountString = "$ " + String.valueOf(shiftTotal);
 //    		
 //    		//textViews[0] = dateString;
 //    		//textViews[1] = amountString;
 //    		
 //    		Object[] guiInfo = {params[0], dateString, amountString};
 //    		
 //    		return (Object[]) guiInfo;
 //    		//return (Cursor) Tipped.getInstance().tipdb.getAllShifts();
 //    	}
 //    	
 //    	protected void onPostExecute(Object[] result) {
 //    		
 //    		String date = (String) result[1];
 //    		String amount = (String) result[2];
 //    		
 //    		((TextView) ((View) result[0]).findViewById(R.id.newShiftDateTextView)).setText(date);
 //    		((TextView) ((View) result[0]).findViewById(R.id.newShiftTotalTextView)).setText(amount);
 //    		
 //    	}
 //    	
 //    }
 }
