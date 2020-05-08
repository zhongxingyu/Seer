 package org.minyanmate.minyanmate;
 
 import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.minyanmate.minyanmate.adapters.ParticipantsExpandableListAdapter;
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanEventsTable;
 import org.minyanmate.minyanmate.database.MinyanGoersTable;
 import org.minyanmate.minyanmate.models.InviteStatus;
 import org.minyanmate.minyanmate.models.MinyanGoer;
 
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 public class ActiveMinyanFragment extends Fragment implements
 	LoaderManager.LoaderCallbacks<Cursor>{
 	
 	private static final int EVENT = 1;
 	private static final int PARTICIPANTS = 2;
 	
 	ParticipantsExpandableListAdapter listAdapter;
 	ExpandableListView expListView;
 	
	private int mEventId = 0;
 	
 	
 	public ActiveMinyanFragment() {	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(
 				R.layout.fragment_active_minyan, container, false);
 		
 		Button btn = (Button) rootView.findViewById(R.id.addUninvitedPersonButton);
 		btn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				addUninvited();
 			}
 		});
 
 		getLoaderManager().initLoader(EVENT, null, this);
 		getLoaderManager().initLoader(PARTICIPANTS, null, this);
 		
 		expListView = (ExpandableListView) rootView.findViewById(R.id.activeMinyanParticipantsList);
 		
 		Log.d("Active Minyan", "" + expListView);
 		
 		HashMap<String, List<MinyanGoer>> map = new HashMap<String, List<MinyanGoer>>();
 		List<String> list = new ArrayList<String>();
 		listAdapter = new ParticipantsExpandableListAdapter(getActivity(), list, map);
 		
 		expListView.setAdapter(listAdapter);
 		
 		return rootView;
 	}
 	
 	public void addUninvited() {
 		// add an uninvited minyangoer to the table
 		
		if (mEventId > 0) {
 			
 			final int eventId = mEventId;
 		
 			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
 			alert.setTitle("Add a Congregant");
 			alert.setMessage("Quickly jot down a name for whoever you want to count");
 	
 			// Set an EditText view to get user input 
 			final EditText input = new EditText(getActivity());
 			alert.setView(input);
 	
 			alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 			  String name = input.getText().toString();
 			  
 			  ContentValues values = new ContentValues();
 			  values.put(MinyanGoersTable.COLUMN_GENERAL_NAME, name);
 			  values.put(MinyanGoersTable.COLUMN_IS_INVITED, 0);
 			  values.put(MinyanGoersTable.COLUMN_MINYAN_EVENT_ID, eventId);
 			  values.put(MinyanGoersTable.COLUMN_INVITE_STATUS, InviteStatus.toInteger(InviteStatus.ATTENDING));
 			  
 			  getActivity().getContentResolver().insert(MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS, values);
 			  }
 			});
 	
 			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			  public void onClick(DialogInterface dialog, int whichButton) {
 			    // Canceled.
 			  }
 			});
 	
 			alert.show();
 		}
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
 		
 		CursorLoader cursorLoader = null;
 		String query;
 		switch (id) {
 		
 		case EVENT:
 			query = MinyanGoersTable.COLUMN_ID + "= (SELECT MAX(" 
 							+ MinyanGoersTable.COLUMN_MINYAN_EVENT_ID + ") FROM " 
 							+ MinyanGoersTable.TABLE_MINYAN_INVITEES + ")";
 			
 			Log.d("Active minyan", query);
 			
 			cursorLoader = new CursorLoader(getActivity(),
 					MinyanMateContentProvider.CONTENT_URI_EVENTS, null, 
 					query, null, null);
 			break;
 			
 		case PARTICIPANTS:
 			query = MinyanGoersTable.COLUMN_MINYAN_EVENT_ID + "= (SELECT MAX(" 
 					+ MinyanGoersTable.COLUMN_MINYAN_EVENT_ID + ") FROM " 
 					+ MinyanGoersTable.TABLE_MINYAN_INVITEES + ")";
 			
 			Log.d("Active Minyan", query);
 			
 			cursorLoader = new CursorLoader(getActivity(),
 					MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS,
 					null, query, null, null);
 			
 			break;		
 		}
 		
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		
 		switch (loader.getId()) {
 		case EVENT:		
 			// TODO fix? on first app load, this table is empty so the cursor result will be empty
 			if (cursor.moveToFirst()) {
 				long startTime = cursor.getLong(cursor.getColumnIndex(MinyanEventsTable.COLUMN_MINYAN_START_TIME));
 				
 				Log.d("Active Minyan", "" + startTime);
 				
				Calendar cal = new GregorianCalendar();
				cal.setTimeInMillis(startTime);
				int minute = cal.get(Calendar.MINUTE);
				int hour = cal.get(Calendar.HOUR);
 				
 				String formattedTime = MinyanScheduleSettingsActivity.formatTimeTextView(getActivity(), hour, minute);
 				Log.d("Active Minyan", formattedTime);
 				TextView timeTextView = (TextView) getActivity().findViewById(R.id.activeMinyanTime);
 				timeTextView.setText(formattedTime);
 			}
 
 			break;
 			
 		case PARTICIPANTS:
 			
 			List<String> categories = new ArrayList<String>();
 			categories.add(InviteStatus.ATTENDING.toString());
 			categories.add(InviteStatus.AWAITING_RESPONSE.toString());			
 			categories.add(InviteStatus.NOT_ATTENDING.toString());
 
 			
 			HashMap<String, List<MinyanGoer>> goers = new HashMap<String, List<MinyanGoer>>();
 			for (String cat : categories) 
 				goers.put(cat, new ArrayList<MinyanGoer>());
 			
 			while (cursor.moveToNext()) {
 				MinyanGoer goer = MinyanGoer.cursorToMinyanGoer(cursor);
 				mEventId = mEventId > 0 ? mEventId : goer.getEventId();
 				goers.get(goer.getInviteStatus().toString()).add(goer);
 			}
 			
 			
 			Log.d("Headers", categories.toString());
 			Log.d("Goers", goers.toString());
 
 			listAdapter.setListDataHeader(categories);
 			listAdapter.setDataChildren(goers);
 			listAdapter.notifyDataSetChanged();
 			
 			break;
 		}
 		
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		loader = null; 
 	}
 }
