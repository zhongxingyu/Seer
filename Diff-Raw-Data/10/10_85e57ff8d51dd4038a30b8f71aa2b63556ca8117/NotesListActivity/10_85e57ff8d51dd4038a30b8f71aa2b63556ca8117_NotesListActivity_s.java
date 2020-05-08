 package com.codeminders.inotes.ui;
 
 import java.util.*;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.*;
 import android.content.*;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.*;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.*;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 import com.codeminders.inotes.*;
 import com.codeminders.inotes.db.DBManager;
 import com.codeminders.inotes.model.AccountInfo;
 import com.codeminders.inotes.model.Note;
 import com.codeminders.inotes.sync.SyncHelper;
 
 public class NotesListActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
     private final static String TIME_FORMAT12 = "MM/dd/yy hh:mmaa";
     private final static String TIME_FORMAT24 = "MM/dd/yy kk:mm";
     private static final int ACTION_DIALOG = 0;
     private static final int CHANGE_TITLE_DIALOG = 1;
     private static final int EMPTY_TITLE_DIALOG = 2;
     private static final int CHOOSE_ACCOUNT_DIALOG = 3;
     private static final int NOTHING_TO_SHARE = 4;
     private final static int EMPTY_NOTES_ID = 777;
     private List<Note> notes;
     private int position;
     private AccountInfo accountInfo;
     private boolean update;
     private TextView syncView;
     private ConnectivityManager connectivityManager;
     SharedPreferences prefs;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.notes_list);
         syncView = (TextView) findViewById(R.id.sync_info);
         setTitle(R.string.notes);
         connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Intent intent = getIntent();
         AccountInfo account;
         if (intent != null && (account = intent.getParcelableExtra("accountInfo")) != null) {
             this.accountInfo = account;
             setTitle(account.getEmail() + " " + getString(R.string.notes));
         } else {
             setTitle(R.string.all_notes);
         }
 
         loadNoteList();
         update = true;
         startUpdateThread();
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         update = false;
         synchronized (this) {
             notify();
         }
     }
 
     public void startUpdateThread() {
         new Thread(new Runnable() {
             NetworkInfo.State currentWifiState, currentMobileState;
             SyncHelper syncHelper;
             boolean sync;
 
             public void run() {
                 if (accountInfo == null) {
                     syncHelper = new SyncHelper(NotesListActivity.this, null);
                 } else {
                     syncHelper = new SyncHelper(NotesListActivity.this, accountInfo.getEmail());
                 }
                 sync = syncHelper.isSyncActive();
                 currentWifiState = connectivityManager.getNetworkInfo(1).getState();
                 currentMobileState = connectivityManager.getNetworkInfo(0).getState();
                 showSyncInfo();
 
                 while (update) {
                     handler.sendEmptyMessage(0);
                     boolean newSync = syncHelper.isSyncActive();
                     NetworkInfo.State newWifiState = connectivityManager.getNetworkInfo(1).getState();
                     NetworkInfo.State newMobileState = connectivityManager.getNetworkInfo(0).getState();
                     if (sync != newSync || currentWifiState != newWifiState
                             || (!prefs.getBoolean(Constants.SYNC_TYPE, false) && currentMobileState != newMobileState)) {
                         sync = newSync;
                         currentWifiState = newWifiState;
                         currentMobileState = newMobileState;
                         showSyncInfo();
                     }
                     try {
                         synchronized (this) {
                             wait(1000);
                         }
                     } catch (InterruptedException e) {
                         Log.d(Constants.TAG, e.toString());
                         update = false;
                     }
                 }
             }
 
             void showSyncInfo() {
                 if (sync) {
                     if (currentWifiState == NetworkInfo.State.CONNECTED ||
                             (!prefs.getBoolean(Constants.SYNC_TYPE, false) && currentMobileState == NetworkInfo.State.CONNECTED)) {
                         handler.sendEmptyMessage(1);
                     } else {
                         handler.sendEmptyMessage(3);
                     }
                 } else {
                     handler.sendEmptyMessage(2);
                 }
             }
 
         }).start();
     }
 
    public Handler handler = new Handler() {
         @Override
        public void handleMessage(Message msg) {
             if (msg.what == 0) {
                 loadNoteList();
             }
             if (msg.what == 1) {
                 syncView.setText(R.string.sync_in_progress);
                 RelativeLayout layout = ((RelativeLayout) syncView.getParent());
                 layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                 layout.getChildAt(1).setVisibility(View.VISIBLE);
             }
             if (msg.what == 2) {
                 ((RelativeLayout) syncView.getParent()).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 0));
             }
             if (msg.what == 3) {
                 syncView.setText(R.string.wait_for_sync);
                 RelativeLayout layout = ((RelativeLayout) syncView.getParent());
                 layout.getChildAt(1).setVisibility(View.INVISIBLE);
                 layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
             }
         }
    };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.note_list_menu, menu);
 
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (notes != null && notes.size() > 0) {
             menu.findItem(R.id.menu_delete_all).setEnabled(true);
         } else {
             menu.findItem(R.id.menu_delete_all).setEnabled(false);
         }
 
         if (accountInfo != null && Constants.LOCAL_ACCOUNT_NAME.equals(accountInfo.getEmail())) {
             menu.findItem(R.id.menu_reload).setVisible(false);
         } else {
             if (accountInfo == null) {
                 AccountManager accountManager = AccountManager.get(this);
                 Account[] accounts = accountManager.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
                 if (accounts.length > 0) {
                     menu.findItem(R.id.menu_reload).setEnabled(true);
                 } else {
                     menu.findItem(R.id.menu_reload).setEnabled(false);
                 }
             } else {
                 menu.findItem(R.id.menu_reload).setEnabled(true);
             }
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent;
         switch (item.getItemId()) {
             case R.id.menu_add:
                 intent = new Intent(this, NoteEditorActivity.class);
                 intent.setAction(Constants.NOTE_CREATE_ACTION);
                 if (accountInfo != null) {
                     intent.putExtra("account", accountInfo.getEmail());
                 }
                 startActivity(intent);
                 return true;
             case R.id.menu_delete_all:
                 DBManager dbManager = new DBManager(this);
                 if (accountInfo != null) {
                     dbManager.deleteNotes(accountInfo.getEmail());
                 } else {
                     dbManager.deleteNotes();
                 }
                 loadNoteList();
                 return true;
             case R.id.menu_reload:
                 AccountManager accountManager = AccountManager.get(this);
                 Account[] accounts = accountManager.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
                 Bundle extras = new Bundle();
                 extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
                 if (accountInfo != null) {
                     for (Account account : accounts) {
                         if (account.name.equals(this.accountInfo.getEmail())) {
                             ContentResolver.requestSync(account, getString(R.string.authorities), extras);
                         }
                     }
                 } else {
                     for (Account account : accounts) {
                         ContentResolver.requestSync(account, getString(R.string.authorities), extras);
                     }
                 }
                 return true;
             case R.id.menu_choose_account:
                 intent = new Intent(this, AccountsListActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         super.onCreateDialog(id);
 
         switch (id) {
             case ACTION_DIALOG:
                 final CharSequence[] items = {
                         getString(R.string.menu_open),
                         getString(R.string.resolve_title),
                         getString(R.string.menu_move),
                         getString(R.string.menu_share),
                         getString(R.string.menu_delete)
                 };
                 return new AlertDialog.Builder(this)
                         .setTitle(R.string.actions)
                         .setItems(items, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int item) {
                                 if (item == 0) {
                                     onItemClick(null, null, position, 0);
                                 } else if (item == 1) {
                                     showDialog(CHANGE_TITLE_DIALOG);
                                 } else if (item == 2) {
                                     showDialog(CHOOSE_ACCOUNT_DIALOG);
                                 } else if (item == 3) {
                                     String noteBody = notes.get(position).getNote();
                                     if (noteBody != null) {
                                         Utils.share(NotesListActivity.this, noteBody);
                                     } else {
                                         showDialog(NOTHING_TO_SHARE);
                                     }
                                 } else {
                                     DBManager dbManager = new DBManager(NotesListActivity.this);
                                     dbManager.deleteNote(notes.get(position).getId());
                                     loadNoteList();
                                 }
                             }
                         }).create();
             case CHANGE_TITLE_DIALOG:
                 LayoutInflater factory = LayoutInflater.from(this);
                 final View view = factory.inflate(R.layout.title_editor_dialog, null);
                 ((EditText) view.findViewById(R.id.input_text)).setText(notes.get(position).getTitle());
 
                 return new AlertDialog.Builder(this)
                         .setTitle(R.string.resolve_title)
                         .setView(view)
                         .setOnCancelListener(new DialogInterface.OnCancelListener() {
                             public void onCancel(DialogInterface dialog) {
                                 dialog.cancel();
                             }
                         })
                         .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 Note note = notes.get(position);
                                 String title = ((EditText) view.findViewById(R.id.input_text)).getText().toString();
                                 if (title.replaceAll("\\s", "").length() != 0) {
                                     note.setTitle(title);
                                     note.setDate(new Date());
                                     DBManager dbManager = new DBManager(NotesListActivity.this);
                                     dbManager.writeNote(note);
                                     loadNoteList();
                                 } else {
                                     showDialog(EMPTY_TITLE_DIALOG);
                                 }
                             }
                         })
                         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.cancel();
                             }
                         }).create();
             case EMPTY_TITLE_DIALOG:
                 return new AlertDialog.Builder(this)
                         .setTitle(R.string.empty_title)
                         .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                         }).create();
             case CHOOSE_ACCOUNT_DIALOG:
                 final String[] accounts = Utils.getAccounts(this);
                 return new AlertDialog.Builder(this)
                         .setTitle(R.string.location)
                         .setItems(accounts, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int item) {
                                 Note note = notes.get(position);
                                 String account = getString(R.string.local_account).equals(accounts[item]) ? Constants.LOCAL_ACCOUNT_NAME : accounts[item];
                                 note.setAccount(account);
                                 if (account != null && !account.equals(Constants.LOCAL_ACCOUNT_NAME)) {
                                     note.setNewNote(true);
                                 }
                                 note.setDate(new Date());
                                 DBManager dbManager = new DBManager(NotesListActivity.this);
                                 dbManager.writeNote(note);
                                 loadNoteList();
                             }
                         }).create();
             case NOTHING_TO_SHARE:
                 return new AlertDialog.Builder(this)
                         .setTitle(R.string.nothing_to_share)
                         .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                         }).create();
         }
         return null;
     }
 
     private void loadNoteList() {
         List<Map<String, ?>> data = getData();
         LinearLayout linearLayout = (LinearLayout) findViewById(R.id.notes_list);
         if (data.size() > 0) {
             if (linearLayout.getChildCount() > 0) {
                 linearLayout.removeView(findViewById(EMPTY_NOTES_ID));
             }
             addToNoteList(data);
         } else {
             if (linearLayout.getChildCount() == 0) {
                 TextView emptyNotes = new TextView(NotesListActivity.this);
                 emptyNotes.setText(getString(R.string.notes_empty));
                 emptyNotes.setId(EMPTY_NOTES_ID);
                 emptyNotes.setTextColor(getResources().getColor(R.color.text_color));
                 emptyNotes.setPadding(10, 0, 0, 0);
 
                 linearLayout.addView(emptyNotes);
                 addToNoteList(data);
             }
         }
     }
 
     private void addToNoteList(List<Map<String, ?>> data) {
         SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.notes_list_item,
                 new String[]{"time", "title"},
                 new int[]{R.id.list_item_time, R.id.list_item_name}
         );
         ListView list = (ListView) findViewById(R.id.notes_listview);
 
         int index = list.getFirstVisiblePosition();
         View v = list.getChildAt(0);
         int top = (v == null) ? 0 : v.getTop();
 
         list.setAdapter(adapter);
         list.setOnItemClickListener(this);
         list.setOnItemLongClickListener(this);
         adapter.notifyDataSetChanged();
         if (index != 0 && top != 0) {
             list.setSelectionFromTop(index, top);
         }
     }
 
     private List<Map<String, ?>> getData() {
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
         DBManager dbManager = new DBManager(this);
         if (accountInfo != null) {
             notes = dbManager.getNotes(accountInfo.getEmail());
         } else {
             notes = dbManager.getNotes();
         }
         Map<String, Object> map;
 
         for (Note note : notes) {
             map = new HashMap<String, Object>();
             if (DateFormat.is24HourFormat(this)) {
                 map.put("time", DateFormat.format(TIME_FORMAT24, note.getDate()));
             } else {
                 map.put("time", DateFormat.format(TIME_FORMAT12, note.getDate()));
             }
             map.put("title", note.getTitle());
 
             items.add(map);
         }
 
         return items;
     }
 
 
     public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         Intent intent = new Intent(this, NoteEditorActivity.class);
         intent.setAction(Constants.NOTE_EDIT_ACTION);
         intent.putExtra("note", notes.get(i));
         startActivity(intent);
     }
 
     public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
         position = i;
         showDialog(ACTION_DIALOG);
         return true;
     }
 
 }
