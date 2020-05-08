 package in.manki.android.callcounter;
 
 import java.util.Date;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import android.app.AlertDialog;
 import android.app.NotificationManager;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.PhoneLookup;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.widget.CursorAdapter;
 import android.text.format.DateFormat;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.QuickContactBadge;
 import android.widget.TextView;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableSet;
 
 public class CallCounterActivity extends FragmentActivity {
 
   private static final String TOP_UP_DIALOG_TAG = "top-up";
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     refreshView();
 
     CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
     enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
       @Override
       public void onCheckedChanged(CompoundButton button, boolean isChecked) {
         setTrackingEnabled(isChecked);
       }
     });
 
     final EditText prefixes = (EditText) findViewById(R.id.prefixes);
     prefixes.setOnKeyListener(new OnKeyListener() {
       @Override
       public boolean onKey(View v, int keyCode, KeyEvent event) {
         Storage storage = getStorage(CallCounterActivity.this);
         storage.setTrackableNumberPrefixes(split(prefixes.getText().toString()));
         return false;
       }
     });
 
     final ListView callHistory = (ListView) findViewById(R.id.call_history);
     callHistory.setOnItemClickListener(new OnItemClickListener() {
       @Override
       public void onItemClick(
           AdapterView<?> parent, View view, int position, long id) {
         Cursor c = (Cursor) callHistory.getItemAtPosition(position);
         String number = c.getString(3);    // Number is 3rd column in selection.
         findContact(number);
       }
     });
   }
 
   private void findContact(String number) {
     Uri query = Uri.withAppendedPath(
         PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
     Cursor results = getContentResolver().query(
         query, new String[] {PhoneLookup._ID}, null, null, null);
     if (!results.moveToFirst()) {
       return;
     }
 
     long contactId = results.getLong(0);
     Intent ci = new Intent(Intent.ACTION_VIEW);
     ci.setData(Uri.withAppendedPath(
         ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId)));
     startActivity(ci);
   }
 
   private Set<String> split(String str) {
     return ImmutableSet.copyOf(
         Splitter.on(Pattern.compile(",|;|\\s"))
             .trimResults()
             .omitEmptyStrings()
             .split(str));
   }
 
   @Override
   protected void onStart() {
     super.onStart();
     clearNotification();
     refreshView();
   }
 
   private void clearNotification() {
     NotificationManager nm =
         (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
     nm.cancel(NotificationId.LOW_BALANCE.get());
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     MenuInflater mi = getMenuInflater();
     mi.inflate(R.menu.main_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
       case R.id.reset_meter:
         confirmAndReset();
         return true;
       case R.id.add_minutes:
         showTopUpDialog();
         return true;
       default:
         return super.onOptionsItemSelected(item);
     }
   }
 
   public void topUp(int minutes) {
     Storage storage = getStorage(this);
     storage.setCreditMinutes(storage.getCreditMinutes() + minutes);
     refreshView();
   }
 
   private void showTopUpDialog() {
     TopUpMinutesDialogFragment df = new TopUpMinutesDialogFragment(this);
     df.show(getSupportFragmentManager(), TOP_UP_DIALOG_TAG);
   }
 
   private void confirmAndReset() {
     AlertDialog alert = new AlertDialog.Builder(this)
         .setMessage(R.string.reset_warning)
         .setPositiveButton(R.string.clear_data, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
             CallCounterActivity activity = CallCounterActivity.this;
             activity.getStorage(activity).resetDuration();
             activity.refreshView();
           }
         })
         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
             dialog.cancel();
           }
         })
         .create();
     alert.show();
   }
 
   private void setTrackingEnabled(boolean enabled) {
     getStorage(this).setTrackingEnabled(enabled);
     refreshView();
   }
 
  @SuppressWarnings("deprecation")
  private void startManaging(Cursor cursor) {
    startManagingCursor(cursor);
  }

   private void refreshView() {
     Storage storage = getStorage(this);
 
     TextView remaining = (TextView) findViewById(R.id.remaining_minutes);
     remaining.setText(String.valueOf(storage.getRemainingMinutes()));
 
     CheckBox enabled = (CheckBox) findViewById(R.id.enabled);
     enabled.setChecked(storage.isTrackingEnabled());
 
     EditText prefixes = (EditText) findViewById(R.id.prefixes);
     prefixes.setText(
         Joiner.on('\n').join(storage.getTrackabelNumberPrefixes()));
 
     Cursor cursor = storage.getTrackedCalls();
    startManaging(cursor);
    CursorAdapter ad = new CursorAdapter(this, cursor, false) {
       @Override
       public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
         LayoutInflater inflater = LayoutInflater.from(ctx);
         return inflater.inflate(R.layout.call_log_entry, parent, false);
       }
 
       private static final long TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;
 
       private String getString(Cursor cursor, String colName) {
         return cursor.getString(cursor.getColumnIndexOrThrow(colName));
       }
 
       @Override
       public void bindView(View view, Context ctx, Cursor cursor) {
         TextView name = (TextView) view.findViewById(R.id.name);
         name.setText(getString(cursor, CallLogDatabaseOpenHelper.NAME_COLUMN));
 
         final String phoneNumber =
             getString(cursor, CallLogDatabaseOpenHelper.NUMBER_COLUMN);
         configureContactBadge(view, phoneNumber);
         TextView number = (TextView) view.findViewById(R.id.number);
         number.setText(phoneNumber);
 
         TextView callTimeView = (TextView) view.findViewById(R.id.call_time);
         long callTime = Long.parseLong(
             getString(cursor, CallLogDatabaseOpenHelper.CALL_TIME_COLUMN));
         long now = new Date().getTime();
         if (now - callTime < TWENTY_FOUR_HOURS) {
           callTimeView.setText(
               DateFormat.getTimeFormat(CallCounterActivity.this)
                   .format(new Date(callTime)));
         } else {
           callTimeView.setText(
               DateFormat.getDateFormat(CallCounterActivity.this)
                   .format(new Date(callTime)));
         }
 
         TextView callDuration = (TextView) view.findViewById(R.id.duration);
         callDuration.setText(
             getString(cursor, CallLogDatabaseOpenHelper.CALL_DURATION_COLUMN));
 
         ImageButton call = (ImageButton) view.findViewById(R.id.call);
         call.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
             Intent intent = new Intent(Intent.ACTION_CALL);
             intent.setData(Uri.parse("tel:" + phoneNumber));
             startActivity(intent);
           }
         });
       }
 
       private void configureContactBadge(View parent, String phoneNumber) {
         QuickContactBadge badge =
             (QuickContactBadge) parent.findViewById(R.id.contact_badge);
         badge.assignContactFromPhone(phoneNumber, true);
 
         Uri uri = Uri.withAppendedPath(
             PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
         String[] projection = new String[] {PhoneLookup.PHOTO_ID};
         Cursor results =
             getContentResolver().query(uri, projection, null, null, null);
         if (results.moveToFirst()) {
           Uri imageUri = ContentUris.withAppendedId(
               ContactsContract.Data.CONTENT_URI, results.getLong(0));
           badge.setImageURI(imageUri);
         }
       }
     };
     ListView callHistory = (ListView) findViewById(R.id.call_history);
     callHistory.setAdapter(ad);
   }
 
   private Storage getStorage(Context ctx) {
     return Storage.get(ctx);
   }
 }
