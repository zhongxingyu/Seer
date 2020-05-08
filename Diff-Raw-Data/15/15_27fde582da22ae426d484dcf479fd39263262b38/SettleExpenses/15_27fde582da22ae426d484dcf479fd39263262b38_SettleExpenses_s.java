 package com.app.settleexpenses;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 public class SettleExpenses extends ListActivity {
     private static final int ACTIVITY_CREATE = 0;
 
     private Activity currentActivity = this;
     private DbAdapter mDbHelper;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.events_list);
 
         View header = getLayoutInflater().inflate(R.layout.event_add_button, getListView(), false);
         getListView().addHeaderView(header);
 
         mDbHelper = new DbAdapter(this, new ContactsAdapter(this));
         mDbHelper.open();
         fillData();
     }
 
     public boolean onAddEventClick(View view) {
         startActivityForResult(new Intent(this, CreateEvent.class), ACTIVITY_CREATE);
         return true;
     }
 
     private void fillData() {
         Cursor c = mDbHelper.fetchAllEvents();
         startManagingCursor(c);
 
         String[] from = new String[]{DbAdapter.EVENT_TITLE};
         int[] to = new int[]{R.id.text1};
 
         SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.event_row, c, from, to);
         setListAdapter(notes);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         final CharSequence[] items = {"Show final settlements", "Delete Event"};
         final long eventId = id;
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Events");
         builder.setItems(items, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 switch (item) {
                     case 0:
                         Intent addExpensesIntent = new Intent(currentActivity, ShowSettlements.class);
                         addExpensesIntent.putExtra(DbAdapter.EVENT_ID, eventId);
                         startActivityForResult(addExpensesIntent, 1);
                         break;
                     case 1:
                         mDbHelper.deleteEvent(eventId);
                         Toast.makeText(getApplicationContext(), "Event is deleted", Toast.LENGTH_SHORT).show();
                         fillData();
                         break;
                 }
 
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (intent == null) {
             fillData();
             return;
         }
         super.onActivityResult(requestCode, resultCode, intent);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mDbHelper.close();
     }
 
 }
