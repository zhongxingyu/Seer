 package org.fNordeingang;
 
 // java
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import net.sf.andhsli.SimpleCrypto;
 import org.fNordeingang.util.CommonUtils;
 import org.fNordeingang.util.ServiceClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 
 // android
 // json
 // http
 //SimpleCrypto
 
 public class fNordeingangActivity extends Activity implements OnClickListener {
   public static final String fNordSettingsFilename = "fNordAppSettingsTesting321";
   public static final String fNordCryptoKey = "fNordAppTesting";
   int requestCode;
 
   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     // update label of fNordStatus
     updatefNordStatusLabel();
 
     ImageButton tweetButton = (ImageButton) findViewById(R.id.fNordTweet);
     ImageButton doorButton = (ImageButton) findViewById(R.id.fNordDoor);
     ImageButton statusButton = (ImageButton) findViewById(R.id.fNordStatus);
     ImageButton settingsButton = (ImageButton) findViewById(R.id.fNordSettings);
     ImageButton aboutButton = (ImageButton) findViewById(R.id.fNordAbout);
     ImageButton calendarButton = (ImageButton) findViewById(R.id.fNordCalendar);
     ImageButton powerctrlButton = (ImageButton) findViewById(R.id.fNordPowerCtrl);
     ImageButton cashButton = (ImageButton) findViewById(R.id.fNordCash);
     tweetButton.setOnClickListener(this);
     doorButton.setOnClickListener(this);
     statusButton.setOnClickListener(this);
     settingsButton.setOnClickListener(this);
     aboutButton.setOnClickListener(this);
     calendarButton.setOnClickListener(this);
     powerctrlButton.setOnClickListener(this);
     cashButton.setOnClickListener(this);
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     // If the request went well (OK) and the request was PICK_CONTACT_REQUEST
     super.onActivityResult(requestCode, resultCode, data);
     Log.w("requestCode: ", Integer.toString(requestCode));
     Log.w("resultCode: ", Integer.toString(resultCode));
     if(requestCode == IntentIntegrator.REQUEST_CODE) {
       try {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.w("code: ", result.getContents());
        Log.w("format: ", result.getFormat());
         articleDialog((new ServiceClient()).getArticleInfo(result.getContents()).getJSONObject("eanArticle").getString("detailname"));
      } catch(Throwable e) {
         e.printStackTrace();
       }
     }
     if(resultCode == 1) {
       updatefNordStatusLabel();
     }
   }
 
   public void onClick(View v) {
     switch(v.getId()) {
       case R.id.fNordTweet:
         // Launch tweet activity
         this.startActivity(new Intent(this, fNordTweetActivity.class));
         break;
       case R.id.fNordDoor:
         // missing
         print("not yet implemented!");
         break;
       case R.id.fNordStatus:
         // Launch status dialog
         togglefNordStatusDialog();
         break;
       case R.id.fNordCalendar:
         // missing
         print("not yet implemented!");
         break;
       case R.id.fNordPowerCtrl:
         // missing
         IntentIntegrator.initiateScan(this);
         //print("not yet implemented!");
         break;
       case R.id.fNordCash:
         // missing
         fNordCashDialog();
         break;
       case R.id.fNordSettings:
         // Launch settings activity
         startActivityForResult(new Intent(this, fNordSettingsActivity.class), requestCode);
         break;
       case R.id.fNordAbout:
         // Launch about dialog
         fNordAboutDialog();
         break;
       default:
         // Error here
         print("Error: Unknown Button pressed!");
     }
   }
 
   public static int getfNordStatus() {
     try {
       // get status
       JSONObject status = (new ServiceClient()).getJSON(ServiceClient.Service.STATUS);
       status = status.getJSONObject("status");
 
       if(status.getBoolean("open")) {
         return 1; // open
       } else {
         return 0; // closed
       }
 
     } catch(IOException ioe) {
       Log.v("IOE", ioe.toString());
       return -1;
     } catch(JSONException jsone) {
       Log.v("Json", jsone.toString());
       return -1;
     }
   }
 
   public String getUserProfile(String username, String password) {
     try {
       // get status
       JSONObject profile = (new ServiceClient()).getProfile(username, password, CommonUtils.getDeviceUUID(getBaseContext(),getContentResolver()));
       return profile.getString("balance");
     } catch(JSONException e) {
       e.printStackTrace();
     }
     return "none";
   }
 
   public static int setfNordStatus(final String username, final String password) {
     return (new ServiceClient()).toggleStatus(username, password);
   }
 
   // updates the fNordStatus label
   public void updatefNordStatusLabel() {
     updatefNordStatusLabelThread ufslt = new updatefNordStatusLabelThread(updatefNordStatusLabelHandler);
     ufslt.start();
   }
 
   final Handler updatefNordStatusLabelHandler = new Handler() {
     public void handleMessage(Message msg) {
       int status = msg.getData().getInt("status");
       TextView statusView = (TextView) findViewById(R.id.fNordStatusLabel);
       ImageView imageView = (ImageView) findViewById(R.id.fNordStatusIcon);
       switch(status) {
         case 0:
           statusView.setText(R.string.fNordStatusClosed);
           imageView.setImageResource(R.drawable.closed_icon);
           break;
         case 1:
           statusView.setText(R.string.fNordStatusOpen);
           imageView.setImageResource(R.drawable.open_icon);
           break;
         default: // on error (f.e. no internet connection) just display the label
           statusView.setText(R.string.fNordStatusUnknown);
           imageView.setImageResource(R.drawable.unknownstatus_icon);
           break;
       }
     }
   };
 
   private class updatefNordStatusLabelThread extends Thread {
     Handler handler;
 
     updatefNordStatusLabelThread(Handler h) {
       handler = h;
     }
 
     public void run() {
       // get status
       int status = getfNordStatus();
 
       // send status to main thread
       Message msg = handler.obtainMessage();
       Bundle b = new Bundle();
       b.putInt("status", status);
       msg.setData(b);
       handler.sendMessage(msg);
 
     }
   }
 
   public void togglefNordStatusDialog() {
     int status = getfNordStatus();
     updatefNordStatusLabel();
     Log.v("Status:", Integer.toString(status));
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     switch(status) {
       case 1:
         builder.setMessage("Do you want to close?");
         break;
       case 0:
         builder.setMessage("Do you want to open?");
         break;
       case -1:
         print("Error: IO or JSON Exception!");
         return;
       default:
         print("Error: couldn't get fNordStatus");
         return;
     }
 
     builder.setCancelable(false);
 
     // toggle fNordStatus at yes
     builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
         SharedPreferences settings = getSharedPreferences(fNordSettingsFilename, 0);
         String username;
         String password;
         try {
           username = SimpleCrypto.decrypt(fNordCryptoKey, settings.getString("username", null));
           password = SimpleCrypto.decrypt(fNordCryptoKey, settings.getString("password", null));
         } catch(Exception e) {
           Log.v("Exception e", "Oh noes!");
           print("Please enter username & password in settings!");
           return;
         }
         if(username != null & username.length() != 0 & password.length() != 0) {
           int status;
           // send toggle command to webserver
           status = setfNordStatus(username, password);
           switch(status) {
             case 0:
               print("Wrong Password?");
               break;
             case -1:
               print("IO Exception!");
               break;
             case -2:
               print("General Exception!");
               break;
             default:
               print("fNordStatus successfully changed");
               updatefNordStatusLabel();
           }
         } else {
           print("Please enter username & password in settings!");
         }
       }
     });
 
     // cancel dialog at no
     builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
         dialog.cancel();
       }
     });
 
     AlertDialog dialog = builder.create();
     if(status == 1) { // open
       dialog.setTitle("fnord is open");
     } else if(status == 0) { // closed
       dialog.setTitle("fnord is closed");
     }
 
     dialog.show();
   }
 
   public void fNordAboutDialog() {
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setCancelable(false);
     builder.setMessage("App for the hackerspace fNordeingang");
     builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
         dialog.cancel();
       }
     });
     AlertDialog dialog = builder.create();
     dialog.setTitle("about fNordApp");
     dialog.show();
   }
 
   public void articleDialog(String name) {
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setCancelable(false);
       builder.setMessage("Article: "+name);
       builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
           dialog.cancel();
         }
       });
       AlertDialog dialog = builder.create();
       dialog.setTitle("Article info");
       dialog.show();
     }
 
   public void fNordCashDialog() {
     SharedPreferences settings = getSharedPreferences(fNordSettingsFilename, 0);
     String username = null;
     String password = null;
     try {
       username = SimpleCrypto.decrypt(fNordCryptoKey, settings.getString("username", null));
       password = SimpleCrypto.decrypt(fNordCryptoKey, settings.getString("password", null));
     } catch(Exception e) {
       e.printStackTrace();
     }
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setCancelable(false);
     builder.setMessage("your current balance is: " + getUserProfile(username, password));
     builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
         dialog.cancel();
       }
     });
     AlertDialog dialog = builder.create();
     dialog.setTitle("Profile");
     dialog.show();
   }
 
   // helper function
   void print(String input) {
     Context context = getApplicationContext();
     CharSequence text = input;
     int duration = Toast.LENGTH_SHORT;
 
     Toast toast = Toast.makeText(context, text, duration);
     toast.show();
   }
 
 }
