 package org.minyanmate.minyanmate;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 import org.minyanmate.minyanmate.adapters.ParticipantsExpandableListAdapter;
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanEventsTable;
 import org.minyanmate.minyanmate.database.MinyanGoersTable;
 import org.minyanmate.minyanmate.dialogs.OnMessageParticipantsSpinnerItemClicked;
 import org.minyanmate.minyanmate.models.InviteStatus;
 import org.minyanmate.minyanmate.models.MinyanGoer;
 import org.minyanmate.minyanmate.services.HeadcountUpdater;
 import org.minyanmate.minyanmate.services.sms_services.SendSmsService;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 public class ActiveMinyanFragment extends Fragment implements
 	LoaderManager.LoaderCallbacks<Cursor>{
 
     // Codes for LoaderManager
 	private static final int EVENT = 1;
 	private static final int PARTICIPANTS = 2;
 
     // Codes for ContextMenu of ExpandableListView
     private static final int MENUITEM_MARK_ATTENDING = 1;
     private static final int MENUITEM_MARK_AWAITING = 2;
     private static final int MENUITEM_MARK_NOT_ATTENDING = 3;
 
     // ExpandableListView and adapter
 	private ParticipantsExpandableListAdapter listAdapter;
 	private ExpandableListView expListView;
 
     // Data about the latest event, if possible
     private boolean isEventCurrent = false;
 	private int mEventId = 0;
     private int mScheduleId = 0;
 	private long scheduleTime;
     private long startTime;
     private long endTime;
 
     // Codes and data for moreOptsBtn menu
 
 
 	public ActiveMinyanFragment() {
         super();
     }
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(
 				R.layout.fragment_active_minyan, container, false);
 
         // Use this to declare the use of a custom menu, thus using onCreateOptionsMenu
         setHasOptionsMenu(true);
 
 
         // Restore instance state if previously instantiated, also
         // LoaderManager is not guaranteed to run before onActivityResult, therefore restore state
         if (savedInstanceState != null)
         {
             mEventId = savedInstanceState.getInt("mEventId");
             mScheduleId = savedInstanceState.getInt("mScheduleId");
             startTime = savedInstanceState.getLong("startTime");
             endTime = savedInstanceState.getLong("endTime");
 
         }
 
         // Initialize the LoaaderManager
 		getLoaderManager().initLoader(EVENT, null, this);
 		getLoaderManager().initLoader(PARTICIPANTS, null, this);
 
 
 
         // Set the ExpandableListView and its adapter
 		expListView = (ExpandableListView) rootView.findViewById(R.id.activeMinyanParticipantsList);
 		HashMap<String, List<MinyanGoer>> map = new HashMap<String, List<MinyanGoer>>();
 		List<String> list = new ArrayList<String>();
 		listAdapter = new ParticipantsExpandableListAdapter(getActivity(), list, map);
 		expListView.setAdapter(listAdapter);
         // Set the ExpandableListView context menu
         expListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
 
             @Override
             public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
                 ExpandableListView.ExpandableListContextMenuInfo info=
                         (ExpandableListView.ExpandableListContextMenuInfo)menuInfo;
 
                 if (ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
                 {
                     int groupNum = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                     Log.d("Group Number in onCreateContextMenu", String.valueOf(groupNum));
 
                     menu.setHeaderTitle("Move participant");
 
                     switch (groupNum) {
                         case 0: // group 0 => marked as attending
                             menu.add(0, MENUITEM_MARK_AWAITING, 1, "Mark as awaiting response");
                             menu.add(0, MENUITEM_MARK_NOT_ATTENDING, 2, "Mark as not attending");
                             break;
                         case 1: // group 1 => marked as awaiting response
                             menu.add(0, MENUITEM_MARK_ATTENDING, 1, "Mark as attending");
                             menu.add(0, MENUITEM_MARK_NOT_ATTENDING, 2, "Mark as not attending");
                             break;
 
                         case 2: // group 2 => marked as not attending
                             menu.add(0, MENUITEM_MARK_ATTENDING, 1, "Mark as attending");
                             menu.add(0, MENUITEM_MARK_AWAITING, 2, "Mark as awaiting response");
                             break;
 
                         default:
                             break;
                     }
                 }
             }
         });
 
 		return rootView;
 	}
 
     @Override
     public void onStart() {
         super.onStart();
 
         // If the latest info says the event hasn't yet expired, check again
         if (isEventCurrent)
             refreshEventData();
     }
 
     /**
      * Call this whenever this Fragment enters visibility, when the event data
      * is known to have changed, or otherwise time-sensitive controls need to be checked.
      * This function calls {@link android.app.Activity#invalidateOptionsMenu()} to force
      * a refresh of the time-sensitive menu buttons.
      */
     private void refreshEventData() {
 
         // Disable buttons if context is invalid
         long currTime = System.currentTimeMillis();
 
         isEventCurrent = ((scheduleTime <= currTime) && (currTime <= endTime)) ? true : false;
         getActivity().invalidateOptionsMenu();
     }
 
     /**
      * This is used to disable the availability of certain menu items based on time sensitivity
      */
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         setMoreMenuOptionsEnabled(menu, isEventCurrent);
         super.onPrepareOptionsMenu(menu);
     }
 
     /**
      * Call this function to disable/enable menu buttons relating to time-sensitive data
      * @param menu the menu to modify
      * @param isEnabled whether to enable or disable the menu selections
      */
     private void setMoreMenuOptionsEnabled(Menu menu, boolean isEnabled) {
 
         List<MenuItem> menuItems = new ArrayList<MenuItem>();
         menuItems.add(menu.findItem(R.id.moreOptsMenu_AddPerson));
         menuItems.add(menu.findItem(R.id.moreOptsMenu_MessageParticipants));
         menuItems.add(menu.findItem(R.id.moreOptsMenu_ShareHeadcount));
 
         for (MenuItem item : menuItems) {
             item.setEnabled(isEnabled);
             item.getIcon().setAlpha(isEnabled ? 255 : 64);
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
 
 
         // First inflate the new menu options, and only afterwards inflate the base menu
         // Diagram below, reference http://stackoverflow.com/a/4954800/1993865
         /*
             Menu:    ()
             inflate new: ( new )
             inflate base: ( new, old)
         */
         menuInflater.inflate(R.menu.minyan_more_options, menu);
         super.onCreateOptionsMenu(menu, menuInflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         getActivity().invalidateOptionsMenu();
         if (isEventCurrent) {
 
             switch (item.getItemId()) {
                 case R.id.moreOptsMenu_AddPerson:
                     addUninvited();
                     return true;
 
                 case R.id.moreOptsMenu_MessageParticipants:
                     buildMessageParticipantsDialog();
                     return true;
 
                 case R.id.moreOptsMenu_ShareHeadcount:
                     shareHeadcount();
                     return true;
 
                 default:
                     break;
             }
 
         } else
             Toast.makeText(getActivity(), "Minyan expired!", Toast.LENGTH_SHORT);
 
         return false;
     }
 
     private void shareHeadcount() {
         Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         sharingIntent.setType("text/plain");
         sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Minyan Headcount");
 
         String headcount = HeadcountUpdater.formattedHeadcountMessage(getActivity(), mEventId);
 
         sharingIntent.putExtra(Intent.EXTRA_TEXT, headcount);
         sharingIntent.putExtra("exit_on_sent", true);
         startActivity(Intent.createChooser(sharingIntent, "Share using"));
     }
 
     // TODO clean this up, the UI is pretty harsh
     private void buildMessageParticipantsDialog() {
         if (mEventId > 0) {
 
             View promptsView = LayoutInflater.from(getActivity()).
                     inflate(R.layout.message_participants_dialog, null);
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             AlertDialog d = builder.setView(promptsView)
                     .setTitle("Choose whom to message")
                     .create();
 
             Spinner spinner = (Spinner) promptsView.findViewById(R.id.sendMessageParticipantsList);
             spinner.setOnItemSelectedListener(new OnMessageParticipantsSpinnerItemClicked(d));
             d.show();
 
         }
         else
             Toast.makeText(getActivity(), "There isn't a minyan!", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onSaveInstanceState(Bundle screenState) {
         super.onSaveInstanceState(screenState);
         screenState.putInt("mEventId", mEventId);
         screenState.putInt("mScheduleId", mScheduleId);
         screenState.putLong("endTime",endTime);
         screenState.putLong("startTime",startTime);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         ExpandableListView.ExpandableListContextMenuInfo info =
                 (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
 
         int groupNum = ExpandableListView.getPackedPositionGroup(info.packedPosition);
         int childNum = ExpandableListView.getPackedPositionChild(info.packedPosition);
         Log.d("Group Number in onContextItemSelected", String.valueOf(groupNum));
         Log.d("Child Number in onContextItemSelected", String.valueOf(childNum));
 
         MinyanGoer goer = (MinyanGoer) listAdapter.getChild(groupNum, childNum);
         Log.d("Goer", String.valueOf(goer));
 
         ContentValues values = new ContentValues();
         values.put(MinyanGoersTable.COLUMN_GOER_ID, goer.getMinyanGoerId());
 
         switch (item.getItemId()) {
             case MENUITEM_MARK_ATTENDING:
                 values.put(MinyanGoersTable.COLUMN_INVITE_STATUS, InviteStatus.toInteger(InviteStatus.ATTENDING));
                 break;
 
             case MENUITEM_MARK_NOT_ATTENDING:
                 values.put(MinyanGoersTable.COLUMN_INVITE_STATUS, InviteStatus.toInteger(InviteStatus.NOT_ATTENDING));
                 break;
 
             case MENUITEM_MARK_AWAITING:
                 values.put(MinyanGoersTable.COLUMN_INVITE_STATUS, InviteStatus.toInteger(InviteStatus.AWAITING_RESPONSE));
                 break;
 
             default:
                 break;
         }
 
         getActivity().getContentResolver().update(MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS,
                 values, MinyanGoersTable.COLUMN_GOER_ID + "=?", new String[]{String.valueOf(goer.getMinyanGoerId())});
 
         HeadcountUpdater.checkMinyanCompletionChange(getActivity(),mEventId, true);
 
         return super.onContextItemSelected(item);
     }
 	
 	public void addUninvited() {
 		// add an uninvited minyangoer to the table
 
         final int eventId = mEventId;
         // Set an EditText view to get user input
         final EditText input = new EditText(getActivity());
 
         final AlertDialog alert = new AlertDialog.Builder(getActivity())
             .setView(input)
             .setTitle("Count a Non-Contact")
             .setMessage("Write down the name of someone you didn't invite who you want to count.")
 
             .setPositiveButton("Save", new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String name = input.getText().toString();
 
                     ContentValues values = new ContentValues();
                     values.put(MinyanGoersTable.COLUMN_DISPLAY_NAME, name);
                     values.put(MinyanGoersTable.COLUMN_IS_INVITED, 0);
                     values.put(MinyanGoersTable.COLUMN_MINYAN_EVENT_ID, eventId);
                     values.put(MinyanGoersTable.COLUMN_INVITE_STATUS, InviteStatus.toInteger(InviteStatus.ATTENDING));
 
                     getActivity().getContentResolver().insert(MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS, values);
 
                     HeadcountUpdater.checkMinyanCompletionChange(getActivity(),mEventId,true);
                 }
             })
 
             .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int whichButton) {
                     // Canceled.
                 }
             })
 
             .create();
 
         input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus)
                     alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
             }
         });
 
         alert.show();
     }
 
     @Override
     public void onActivityResult(int reqCode, int resultCode, Intent data) {
 
         switch(reqCode) {
             case (MinyanScheduleSettingsActivity.PICK_CONTACT) :
                 if (resultCode == Activity.RESULT_OK && mEventId > 0 && mScheduleId > 0) {
 
                     Uri result = data.getData();
                     String phoneNumberId = result.getLastPathSegment();
 
                     // check whether person was already invited
                     String selection = MinyanGoersTable.COLUMN_PHONE_NUMBER_ID + " = ?"
                             + " AND " + MinyanGoersTable.QUERY_LATEST_GOERS;
                     Cursor c = getActivity().getContentResolver().query(
                             MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS,
                             null, selection, new String[] { phoneNumberId }, null
                     );
 
                     // If count is 0, person hasn't been invited yet, so invite them
                     if (c.getCount() == 0) {
                         Intent i = new Intent(getActivity(), SendSmsService.class);
                         i.putExtra(SendSmsService.REQUEST_CODE, SendSmsService.SEND_INVITE_TO_CONTACT);
                         i.putExtra(SendSmsService.EVENT_ID, mEventId);
                         i.putExtra(SendSmsService.SCHEDULE_ID, mScheduleId);
                         i.putExtra(SendSmsService.PHONE_NUMBER_ID, Long.parseLong(phoneNumberId));
                         WakefulIntentService.sendWakefulWork(getActivity(), i);
                     } else {
                         Toast.makeText(getActivity(), "Contact already invited!", Toast.LENGTH_SHORT).show();
                         Log.d("onActivityResult succeeded", "Event Id: " + mEventId);
                         Log.d("onActivityResult succeeded", "Schedule Id: " + mScheduleId);
                         Log.d("onActivityResult succeeded", "Person was already invited!");
                     }
 
                     c.close();
                 }
                 else {
                     Toast.makeText(getActivity(), "Failed to invite contact!", Toast.LENGTH_SHORT).show();
                     Log.d("onActivityResult failed", "Event Id: " + mEventId);
                     Log.d("onActivityResult failed", "Schedule Id: " + mScheduleId);
                 }
                 break;
 
             default:
                 break;
         }
     }
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
 		
 		CursorLoader cursorLoader = null;
 		String query;
 		switch (id) {
 
 		case EVENT:
 			
 			cursorLoader = new CursorLoader(getActivity(),
 					MinyanMateContentProvider.CONTENT_URI_EVENTS, null, 
 					MinyanEventsTable.QUERY_LATEST_EVENT, null, null);
 			break;
 			
 		case PARTICIPANTS:
 			
 			cursorLoader = new CursorLoader(getActivity(),
 					MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS,
 					null, MinyanGoersTable.QUERY_LATEST_GOERS, null, null);
 			
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
 				scheduleTime = cursor.getLong(cursor.getColumnIndex(MinyanEventsTable.COLUMN_MINYAN_SCHEDULE_TIME));
                 startTime = cursor.getLong(cursor.getColumnIndex(MinyanEventsTable.COLUMN_MINYAN_START_TIME));
                 endTime = cursor.getLong(cursor.getColumnIndex(MinyanEventsTable.COLUMN_MINYAN_END_TIME));
                 mEventId = cursor.getInt(cursor.getColumnIndex(MinyanEventsTable.COLUMN_EVENT_ID));
                 mScheduleId = cursor.getInt(cursor.getColumnIndex(MinyanEventsTable.COLUMN_MINYAN_SCHEDULE_ID));
 
                 String day = cursor.getString(cursor.getColumnIndex(MinyanEventsTable.COLUMN_DAY_NAME));
                 String prayerName = cursor.getString(cursor.getColumnIndex(MinyanEventsTable.COLUMN_PRAYER_NAME));
 
                 // Print latest event data
                 Calendar cal = new GregorianCalendar();
                 cal.setTimeInMillis(startTime);
                 int minute = cal.get(Calendar.MINUTE);
                 int hour = cal.get(Calendar.HOUR_OF_DAY);
 
                 String formattedTime = MinyanScheduleSettingsActivity.formatTimeTextView(getActivity(), hour, minute);
 				TextView timeTextView = (TextView) getActivity().findViewById(R.id.activeMinyanTime);
 				timeTextView.setText(day + " " + prayerName + " begins at " + formattedTime);
 
                 // If minyan info is old, don't even bother enabling or setting callbacks
                 long currTime = System.currentTimeMillis();
 
                 Log.d("Event Info", "Event Id: " + mEventId);
                 Log.d("Event Info", "Schedule Time: " + scheduleTime);
                 Log.d("Event Info", "Current Time:  " + currTime);
                 Log.d("Event Info", "End Time:      " + endTime);
 
                 refreshEventData();
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
 				goers.get(goer.getInviteStatus().toString()).add(goer);
 			}
 
 
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
