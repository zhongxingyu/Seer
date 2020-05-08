 package net.anei.cadpage;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.TextView;
 
 public class CallHistoryActivity extends ListActivity {
   
   private static final int RELEASE_DIALOG = 1;
   
   // keep track of which message text view has opened a context menu
   private HistoryMsgTextView msgTextView = null;
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     // If initialization failure in progress, shut down without doing anything
     if (TopExceptionHandler.isInitFailure()) {
       finish();
       return;
     }
     
     // If preferences have never been initialized, bring up the preference
     // screen to initialize them now.  This is necessary because the new
     // preference retrieval logic throws an exception if any requested preference
     // hasn't been initialized.
     if (! ManagePreferences.initialized()) {
       Intent intent = new Intent(this, SmsPopupConfigActivity.class);
       startActivity(intent);
     }
     
     // If release info for this release has not been displayed, do it now
     
     // Set up list heading
     TextView tv = new TextView(this);
     tv.setText(R.string.call_history);
     getListView().addHeaderView(tv);
     
     // Set up message queue list adapter
     setListAdapter(SmsMessageQueue.getInstance().listAdapter(this));
     
     startup();
   }
   
 
   /* (non-Javadoc)
    * @see android.app.Activity#onNewIntent(android.content.Intent)
    */
   @Override
   protected void onNewIntent(Intent intent) {
     super.onNewIntent(intent);
     setIntent(intent);
     
     startup();
   }
   
   /**
    * Perform initial intent processing logic
    * whether called from onCreate or onNewIntent
    */
   private void startup() {
     Intent intent = getIntent();
     
     SmsMessageQueue msgQueue = SmsMessageQueue.getInstance();
 
     // We do some special processing if the intent was launched by the user
     // instead of through some internal trigger.
    if (Intent.ACTION_MAIN.equals(intent.getAction()) && 
         intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
       
       // First clear any pending notification
       ManageNotification.clear(this);
       
       // Second, launch the release info dialog if it hasn't already been displayed
       String release = getString(R.string.release_version);
       if (! ManagePreferences.release().equals(release)) {
         ManagePreferences.setRelease(release);
         showDialog(RELEASE_DIALOG);
       }
     }
     
     // If popup is enabled, and there is one and only one unread message in the
     // queue, launch a message popup to display it
     SmsMmsMessage msg = msgQueue.getDisplayMsg();
     if (msg != null) SmsPopupActivity.launchActivity(this, msg);
   }
 
   @Override
   protected Dialog onCreateDialog(int id) {
     switch (id) {
 
       case RELEASE_DIALOG:
         return new AlertDialog.Builder(this)
         .setIcon(R.drawable.ic_launcher)
         .setTitle(R.string.release_title)
         .setMessage(R.string.release_text)
         .setPositiveButton(android.R.string.ok, null)
         .create();
     }
     return super.onCreateDialog(id);
   }
 
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.history_menu, menu);
     
     return true;
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
 	  SmsMessageQueue msgQueue = SmsMessageQueue.getInstance();
     switch (item.getItemId()) {
     case R.id.settings_item:
       Intent intent = new Intent(this, SmsPopupConfigActivity.class);
       startActivity(intent);
       return true;
     case R.id.clearall_item:
     	msgQueue.clearAll();
     	return true;
     case R.id.exit_item:
     	this.finish();
     	return true;
     	
     default:
         return super.onOptionsItemSelected(item);
     }
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
    */
   @Override
   public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
     super.onCreateContextMenu(menu, view, menuInfo);
 
     msgTextView = (HistoryMsgTextView)view;
     SmsMmsMessage.createMenu(this, menu, false);
     msgTextView.getMessage().prepareMenu(this, menu);
   }
 
 
   /* (non-Javadoc)
    * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     
     if (msgTextView != null) {
       SmsMmsMessage msg = msgTextView.getMessage();
       if (msg != null) {
         if (msg.menuItemSelected(this, item.getItemId(), false)) return true;
       }
     }
     return super.onContextItemSelected(item);
   }
 
 
   /**
    * Launch activity
    */
   public static void launchActivity(Context context, SmsMmsMessage message) {
     Intent intent = getLaunchIntent(context);
     context.startActivity(intent);
   }
 
   /**
    * Build intent to launch this activity
    * @param context
    * @return
    */
   public static Intent getLaunchIntent(Context context) {
     Intent intent = new Intent(context, CallHistoryActivity.class);
     int flags =
       Intent.FLAG_ACTIVITY_NEW_TASK |
       Intent.FLAG_ACTIVITY_SINGLE_TOP |
       Intent.FLAG_ACTIVITY_CLEAR_TOP;
     intent.setFlags(flags);
     return intent;
   }
 }
