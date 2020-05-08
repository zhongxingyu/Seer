 //TODO: refresh button at main setting panel
 //TODO: batched contact list update
 //TODO: display message when attempting to add freemail account: Freemail has no IMAP support
 package hu.rgai.android.test;
 
import android.app.ActionBar;
 import hu.rgai.android.services.MainService;
 import hu.rgai.android.services.schedulestarters.MainScheduler;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.facebook.AccessToken;
 import com.facebook.AccessTokenSource;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import hu.rgai.android.config.Settings;
 import hu.rgai.android.intent.beens.FullMessageParc;
 import hu.rgai.android.intent.beens.MessageListElementParc;
 import hu.rgai.android.intent.beens.account.AccountAndr;
 import hu.rgai.android.store.StoreHandler;
 import hu.rgai.android.test.settings.AccountSettingsList;
 import hu.uszeged.inf.rgai.messagelog.beans.account.FacebookAccount;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import android.support.v7.app.ActionBarActivity;
 
 public class MainActivity extends ActionBarActivity {
 
 //  private Boolean isInternetAvailable = null;
   
   
   public static final int PICK_CONTACT = 101;
   
   private boolean serviceConnectionEstablished = false;
   private List<MessageListElementParc> messages;
   private LazyAdapter adapter;
   private MainService s;
   private DataUpdateReceiver serviceReceiver;
   private SystemBroadcastReceiver systemReceiver;
   private ProgressDialog pd = null;
   private ServiceConnection serviceConnection = new ServiceConnection() {
     public void onServiceConnected(ComponentName className, IBinder binder) {
       s = ((MainService.MyBinder) binder).getService();
 
       updateList(s.getEmails());
       if ((messages == null || !messages.isEmpty()) && pd != null) {
         pd.dismiss();
       }
       serviceConnectionEstablished = true;
     }
 
     public void onServiceDisconnected(ComponentName className) {
       s = null;
     }
   };
 
   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 //    setContentView(R.layout.main);
    getActionBar().setDisplayShowTitleEnabled(false);
     final String fbToken = StoreHandler.getFacebookAccessToken(this);
     if (fbToken != null) {
       Session.openActiveSessionWithAccessToken(this,
               AccessToken.createFromExistingAccessToken(fbToken, new Date(2014, 1, 1), new Date(2013, 1, 1), AccessTokenSource.FACEBOOK_APPLICATION_NATIVE, Settings.getFacebookPermissions()),
               new Session.StatusCallback() {
         public void call(Session sn, SessionState ss, Exception excptn) {
           Log.d("rgai", "REOPENING SESSION WITH ACCESS TOKEN -> " + fbToken);
           Log.d("rgai", sn.toString());
           Log.d("rgai", ss.toString());
         }
       });
     }
     bindService(new Intent(this, MainService.class), serviceConnection, Context.BIND_AUTO_CREATE);
     
     
 //    Log.d("rgai", "myService.running -> " + MyService.RUNNING);
 //    emails = new ArrayList<Map<String, String>>();
     if (!MainService.RUNNING) {
       Intent intent = new Intent(this, MainScheduler.class);
       intent.setAction(Context.ALARM_SERVICE);
       this.sendBroadcast(intent);
       // disaplying loading dialog, since the mails are not ready, but the user opened the list
       pd = new ProgressDialog(this);
       pd.setMessage("Fetching emails...");
       pd.setCancelable(false);
       pd.show();
     }
 //    setContent();
 //    setListAdapter(adapter);
 //    set
 
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.main_settings_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
     Intent intent;
     switch (item.getItemId()) {
       case R.id.accounts:
         intent = new Intent(this, AccountSettingsList.class);
         startActivityForResult(intent, Settings.ActivityRequestCodes.ACCOUNT_SETTING_RESULT);
         return true;
       case R.id.message_send_new:
         intent = new Intent(this, MessageReply.class);
         startActivity(intent);
         return true;
       default:
         return super.onOptionsItemSelected(item);
     }
   }
   
   
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     switch (requestCode) {
       case (Settings.ActivityRequestCodes.FULL_MESSAGE_RESULT):
         if (resultCode == Activity.RESULT_OK) {
           // TODO: only saving simple string content
           FullMessageParc fm = data.getParcelableExtra("message_data");
           String messageId = data.getStringExtra("message_id");
           AccountAndr acc = data.getParcelableExtra("account");
           
 //          String emailID = data.getIntExtra("email_id", 0) + "";
           
 //          String content = data.getStringExtra("email_content");
           s.setMessageContent(messageId, acc, fm);
         }
         break;
       case (Settings.ActivityRequestCodes.ACCOUNT_SETTING_RESULT):
         if (resultCode == Activity.RESULT_OK) {
           Log.d("rgai", "email setting result");
           Intent intent = new Intent(this, MainScheduler.class);
           intent.setAction(Context.ALARM_SERVICE);
           this.sendBroadcast(intent);
           
 //          pd = new ProgressDialog(this);
 //          pd.setMessage("Fetching emails...");
 //          pd.setCancelable(false);
 //          pd.show();
         }
         break;
 //      case (PICK_CONTACT):
 //        if (resultCode == Activity.RESULT_OK) {
 //          Uri contactData = data.getData();
 //          Cursor c =  getContentResolver().query(contactData, null, null, null, null);
 //          if (c.moveToFirst()) {
 //            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 //            AccountAndr account = StoreHandler.getAccounts(this).get(0);
 //            Intent intent = new Intent(this, MessageReply.class);
 ////            Source source = new Source("");
 //            intent.putExtra("content", "");
 //            intent.putExtra("subject", "");
 //            intent.putExtra("account", (Parcelable)account);
 //            intent.putExtra("from", new PersonAndr("1", name, name));
 //            startActivityForResult(intent, EmailDisplayer.MESSAGE_REPLY_REQ_CODE);
 //          }
 //        }
 //        
 //        break;
       default:
         break;
     }
   }
 
   private void updateList(MessageListElementParc[] newMessages) {
     Log.d("rgai", "updating list...");
     if (messages != null) {
       
       messages.clear();
       if (newMessages != null) {
         for (int i = 0; i < newMessages.length; i++) {
           messages.add(newMessages[i]);
         }
         adapter.notifyDataSetChanged();
       }
     }
   }
   
   private void setMessageSeen(MessageListElementParc message) {
     for (MessageListElementParc mlep : messages) {
       if(mlep.equals(message)) {
         mlep.setSeen(true);
         break;
       }
     }
   }
 
   @Override
   protected void onResume() {
     super.onResume();
 //    getFbMessages(this);
     // register service broadcast receiver
     if (serviceReceiver == null) {
       serviceReceiver = new DataUpdateReceiver(this);
     }
     IntentFilter intentFilter = new IntentFilter(Constants.MAIL_SERVICE_INTENT);
     registerReceiver(serviceReceiver, intentFilter);
     
     // register system broadcast receiver for internet connection state change
     if (systemReceiver == null) {
       systemReceiver = new SystemBroadcastReceiver(this);
     }
     IntentFilter systemIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
     registerReceiver(systemReceiver, systemIntentFilter);
     
     // setting content
     setContent();
   }
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
     if (systemReceiver != null) {
       unregisterReceiver(systemReceiver);
     }
     if (serviceConnection != null) {
       unbindService(serviceConnection);
     }
   }
   
   private void setContent() {
     // TODO: itt is kell ellenorizni, hogy van-e jelszo, mer ha nincs akkor nem lehet csinalni semmit...
     boolean isNet = isNetworkAvailable();
 //    if (isNet == falseInternetAvailable == null || isInternetAvailable != isNet) {
 //      isInternetAvailable = isNetworkAvailable();
       if (isNet) {
         View currentView = this.findViewById(R.id.list);
         if (currentView == null || currentView.getId() != R.id.list) {
           setContentView(R.layout.main);
           messages = new ArrayList<MessageListElementParc>();
 
           ListView lv = (ListView) findViewById(R.id.list);
   //        String[] from = {"subject", "from"};
   //        int[] to = {android.R.id.text1, android.R.id.text2};
   //        adapter = new SimpleAdapter(this, emails, android.R.layout.simple_list_item_2, from, to);
           adapter = new LazyAdapter(this, messages);
           lv.setAdapter(adapter);
           lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> av, View arg1, int itemIndex, long arg3) {
 
               MessageListElementParc message = (MessageListElementParc) av.getItemAtPosition(itemIndex);
 
 //              String messageId = (String)message.getId();
               AccountAndr a = (AccountAndr)message.getAccount();
               Intent intent = null;
 //              if (a instanceof FacebookAccount) {
                 Class classToLoad = Settings.getAccountTypeToMessageDisplayer().get(a.getAccountType());
                 intent = new Intent(MainActivity.this, classToLoad);
                 
                 intent.putExtra("msg_list_element", (Parcelable)message);
                 intent.putExtra("account", (Parcelable)a);
 //              } else {
 //                Class classToLoad = Settings.getAccountTypeToMessageDisplayer().get(a.getAccountType());
 //                intent = new Intent(MainActivity.this, classToLoad);
 //                
 //                intent.putExtra("msg_list_element", (Parcelable)message);
 //                intent.putExtra("account", (Parcelable)a);
 //                
 //              }
               boolean changed = s.setMessageSeen(message);
               if (changed) {
                 setMessageSeen(message);
                 adapter.notifyDataSetChanged();
               }
               startActivityForResult(intent, Settings.ActivityRequestCodes.FULL_MESSAGE_RESULT);
             }
           });
           if (serviceConnectionEstablished) {
             updateList(s.getEmails());
           }
         } else {
           updateList(s.getEmails());
           adapter.notifyDataSetChanged();
         }
         
       } else {
         TextView text = new TextView(this);
         text.setText(getString(R.string.no_internet_access));
         text.setGravity(Gravity.CENTER);
         this.setContentView(text);
       }
 //    }
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     if (serviceReceiver != null) {
       unregisterReceiver(serviceReceiver);
     }
   }
   
   public boolean isNetworkAvailable() {
     ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
     return activeNetworkInfo != null && activeNetworkInfo.isConnected();
   }
 
   private class SystemBroadcastReceiver extends BroadcastReceiver {
 
 //    private MainActivity activity = null;
 
     public SystemBroadcastReceiver(MainActivity activity) {
 //      this.activity = activity;
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
       // listening for internet access change
       if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
         NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
         String typeName = info.getTypeName();
         String subtypeName = info.getSubtypeName();
         System.out.println("Network is up ******** " + typeName + ":::" + subtypeName);
 
 //        activity.setContent("onInternetBroadcast receive");
       }
     }
   }
   
   private class DataUpdateReceiver extends BroadcastReceiver {
 
     private MainActivity activity;
     
     public DataUpdateReceiver(MainActivity activity) {
       this.activity = activity;
     }
     
     @Override
     public void onReceive(Context context, Intent intent) {
       if (intent.getAction().equals(Constants.MAIL_SERVICE_INTENT)) {
         if (intent.getExtras().getInt("result") != MainService.OK) {
           int result = intent.getExtras().getInt("result");
           String msg = null;
           switch (result) {
             case MainService.AUTHENTICATION_FAILED_EXCEPTION:
               msg = "Authentication failed: " + intent.getExtras().getString("errorMessage");
               break;
             case MainService.UNKNOWN_HOST_EXCEPTION:
               msg = getString(R.string.exception_unknown_host);
               break;
             case MainService.IOEXCEPTION:
               msg = getString(R.string.exception_io);
               break;
             case MainService.CONNECT_EXCEPTION:
               msg = getString(R.string.exception_connect);
               break;
             case MainService.NO_SUCH_PROVIDER_EXCEPTION:
               msg = getString(R.string.exception_nosuch_provider);
               break;
             case MainService.MESSAGING_EXCEPTION:
               msg = getString(R.string.exception_messaging);
               break;
             case MainService.SSL_HANDSHAKE_EXCEPTION:
               msg = getString(R.string.exception_ssl_handshake);
               break;
             case MainService.NO_INTERNET_ACCESS:
               msg = getString(R.string.no_internet_access);
               break;
             case MainService.NO_ACCOUNT_SET:
               TextView text = new TextView(context);
               text.setText(getString(R.string.no_account_set));
               text.setGravity(Gravity.CENTER);
               activity.setContentView(text);
               break;
             default:
               msg = getString(R.string.exception_unknown);
               break;
           }
           if (msg != null) {
             Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
           }
           if (pd != null) {
             pd.dismiss();
           }
         } else {
           Parcelable[] messagesParc = intent.getExtras().getParcelableArray("messages");
           MessageListElementParc[] messages = new MessageListElementParc[messagesParc.length];
           for (int i = 0; i < messagesParc.length; i++) {
             messages[i] = (MessageListElementParc) messagesParc[i];
           }
 
           updateList(messages);
           if (pd != null) {
             pd.dismiss();
           }
         }
       }
     }
   }
 }
