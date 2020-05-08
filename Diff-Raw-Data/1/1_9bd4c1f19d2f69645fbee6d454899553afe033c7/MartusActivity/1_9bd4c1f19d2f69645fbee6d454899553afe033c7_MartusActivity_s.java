 package org.martus.android;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.util.Arrays;
 
 import org.martus.android.dialog.CreateAccountDialog;
 import org.martus.android.dialog.LoginDialog;
 import org.martus.android.dialog.MagicWordDialog;
 import org.martus.clientside.ClientSideNetworkGateway;
 import org.martus.common.crypto.MartusSecurity;
 import org.martus.common.network.NetworkResponse;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.bugsense.trace.BugSenseHandler;
 import info.guardianproject.onionkit.ui.OrbotHelper;
 
 public class MartusActivity extends BaseActivity implements LoginDialog.LoginDialogListener,
         CreateAccountDialog.CreateAccountDialogListener, MagicWordDialog.MagicWordDialogListener {
 
     public final static String PROXY_HOST = "127.0.0.1"; //test the local device proxy provided by Orbot/Tor
     public final static int PROXY_HTTP_PORT = 8118; //default for Orbot/Tor
     public final static int PROXY_SOCKS_PORT = 9050; //default for Orbot/Tor
 
     public static final int MAX_LOGIN_ATTEMPTS = 3;
     public static final int MIN_PASSWORD_SIZE = 8;
     private String serverPublicKey;
 
     private MartusSecurity martusCrypto;
     private static Activity myActivity;
     private ClientSideNetworkGateway gateway = null;
     private String serverIP;
     private int invalidLogins;
 
     static final int ACTIVITY_DESKTOP_KEY = 2;
     public static final int ACTIVITY_BULLETIN = 3;
     public static final String RETURN_TO = "return_to";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         BugSenseHandler.initAndStartSession(MartusActivity.this, ExternalKeys.BUGSENSE_KEY);
         setContentView(R.layout.main);
 
         myActivity = this;
         updateSettings();
 
         martusCrypto = AppConfig.getInstance().getCrypto();
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (martusCrypto.hasKeyPair()) {
             checkDesktopKey();
             if (!confirmServerPublicKey()) {
                 Intent intent = new Intent(MartusActivity.this, ServerActivity.class);
                 startActivity(intent);
                 return;
             }
 
             SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(this);
             boolean canUpload = mySettings.getBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
             if (!canUpload) {
                 showMagicWordDialog();
             }
 
         } else {
             if (isAccountCreated()) {
                 invalidLogins = 0;
                 showLoginDialog();
             } else {
                 showCreateAccountDialog();
             }
         }
         updateSettings();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         BugSenseHandler.closeSession(MartusActivity.this);
     }
 
     public void sendBulletin(View view) {
         Intent intent = new Intent(MartusActivity.this, BulletinActivity.class);
         startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent;
 
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.settings_menu_item:
                 intent = new Intent(MartusActivity.this, SettingsActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.quit_menu_item:
                 logout(MartusActivity.this);
                 finish();
                 return true;
             case R.id.server_menu_item:
                 intent = new Intent(MartusActivity.this, ServerActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public static void logout(Context context) {
         AppConfig.getInstance().getCrypto().clearKeyPair();
         deleteCache(context);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
 
         String filePath = intent.getStringExtra(BulletinActivity.EXTRA_ATTACHMENT);
         if (null != filePath) {
             Intent bulletinIntent = new Intent(MartusActivity.this, BulletinActivity.class);
             bulletinIntent.putExtra(BulletinActivity.EXTRA_ATTACHMENT, filePath);
             startActivity(bulletinIntent);
         }
     }
 
     public void onTorChecked(View view) {
         boolean checked = ((CheckBox) view).isChecked();
 
         if  (checked) {
             System.setProperty("proxyHost", PROXY_HOST);
             System.setProperty("proxyPort", String.valueOf(PROXY_HTTP_PORT));
 
             System.setProperty("socksProxyHost", PROXY_HOST);
             System.setProperty("socksProxyPort", String.valueOf(PROXY_SOCKS_PORT));
 
             try {
 
                 OrbotHelper oc = new OrbotHelper(this);
 
                 if (!oc.isOrbotInstalled())
                 {
                     oc.promptToInstall(this);
                 }
                 else if (!oc.isOrbotRunning())
                 {
                     oc.requestOrbotStart(this);
                 }
             } catch (Exception e) {
                 Log.e(AppConfig.LOG_LABEL, "Tor check failed", e);
             }
 
         } else {
             System.clearProperty("proxyHost");
             System.clearProperty("proxyPort");
 
             System.clearProperty("socksProxyHost");
             System.clearProperty("socksProxyPort");
         }
     }
 
     private boolean confirmServerPublicKey() {
         updateSettings();
         if (serverPublicKey.isEmpty()) {
             return false;
         }
         gateway = ClientSideNetworkGateway.buildGateway(serverIP, serverPublicKey);
         return true;
     }
 
     private void checkDesktopKey() {
         SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(this);
         String desktopPublicKeyString = mySettings.getString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, "");
         if (desktopPublicKeyString.length() < 1) {
             Intent intent = new Intent(MartusActivity.this, DesktopKeyActivity.class);
             startActivityForResult(intent, ACTIVITY_DESKTOP_KEY);
         }
     }
 
     private boolean isAccountCreated() {
         SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(MartusActivity.this);
 
         // attempt to read keypair from prefs
         String keyPairString = mySettings.getString(SettingsActivity.KEY_KEY_PAIR, "");
         return keyPairString.length() > 1;
     }
 
     private boolean confirmAccount(char[] password)  {
 
         SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(MartusActivity.this);
         String keyPairString = mySettings.getString(SettingsActivity.KEY_KEY_PAIR, "");
 
         // construct keypair from value read from prefs
         byte[] decodedKeyPair = Base64.decode(keyPairString, Base64.NO_WRAP);
         InputStream is = new ByteArrayInputStream(decodedKeyPair);
         try {
             martusCrypto.readKeyPair(is, password);
         } catch (Exception e) {
             Log.e(AppConfig.LOG_LABEL, "Problem confirming password", e);
             return false;
         }
         //martusCrypto.setShouldWriteAuthorDecryptableData(false);
         return true;
     }
 
     private void createAccount(char[] password)  {
         SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(MartusActivity.this);
         // create new keypair and store in prefs
         martusCrypto.createKeyPair();
 
         try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             martusCrypto.writeKeyPair(out, password);
             out.close();
             byte[] keyPairData = out.toByteArray();
 
             // write keypair to prefs
             // need to first base64 encode so we can write to prefs
             String encodedKeyPair = Base64.encodeToString(keyPairData, Base64.NO_WRAP);
 
             // write to prefs
             SharedPreferences.Editor editor = mySettings.edit();
             editor.putString(SettingsActivity.KEY_KEY_PAIR, encodedKeyPair);
             editor.commit();
         } catch (Exception e) {
             Log.e(AppConfig.LOG_LABEL, "Problem creating account", e);
             showMessage(MartusActivity.this, getString(R.string.error_create_account), getString(R.string.error_message));
         }
     }
 
     private void updateSettings() {
         SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(this);
         serverPublicKey = mySettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
         serverIP = mySettings.getString(SettingsActivity.KEY_SERVER_IP, "");
     }
 
     public static void showMessage(Context context, String msg, String title){
         AlertDialog.Builder alert = new AlertDialog.Builder(context);
         alert.setIcon(android.R.drawable.ic_dialog_alert)
              .setTitle(title)
              .setMessage(msg)
              .show();
     }
 
     public static void deleteCache(Context context) {
         File dir = context.getCacheDir();
         if (dir != null && dir.isDirectory()) {
             deleteDir(dir);
         }
     }
 
     public static boolean deleteDir(File dir) {
         if (dir != null && dir.isDirectory()) {
             String[] children = dir.list();
             for (String aChildren : children) {
                 boolean success = deleteDir(new File(dir, aChildren));
                 if (!success) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 
     void showLoginDialog() {
         LoginDialog loginDialog = LoginDialog.newInstance();
         loginDialog.show(getSupportFragmentManager(), "dlg_login");
     }
 
     @Override
     public void onFinishPasswordDialog(TextView passwordText) {
         char[] password = passwordText.getText().toString().trim().toCharArray();
         boolean confirmed = confirmAccount(password);
         if (!confirmed) {
             if (++invalidLogins == MAX_LOGIN_ATTEMPTS) {
                 finish();
             }
             Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
             showLoginDialog();
         }
 
         SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(MartusActivity.this);
         serverPublicKey = mySettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
         gateway = ClientSideNetworkGateway.buildGateway(serverIP, serverPublicKey);
 
         Intent intent = getIntent();
         int returnTo = intent.getIntExtra(RETURN_TO, 0);
         if (returnTo == ACTIVITY_BULLETIN) {
             Intent destination = new Intent(MartusActivity.this, BulletinActivity.class);
             destination.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
             destination.putExtras(intent);
             startActivity(destination);
         }
         onResume();
     }
 
     public void onCancelPasswordDialog() {
         this.finish();
     }
 
 
     void showCreateAccountDialog() {
         CreateAccountDialog newAccountDialog = CreateAccountDialog.newInstance();
         newAccountDialog.show(getSupportFragmentManager(), "dlg_new_account");
     }
 
     public void onFinishNewAccountDialog(TextView passwordText, TextView confirmPasswordText) {
 
         boolean failed = false;
         char[] password = passwordText.getText().toString().trim().toCharArray();
         char[] confirmPassword = confirmPasswordText.getText().toString().trim().toCharArray();
         if (password.length < MIN_PASSWORD_SIZE) {
             Toast.makeText(MartusActivity.this,
             R.string.invalid_password, Toast.LENGTH_SHORT).show();
             failed = true;
         }
         if (!Arrays.equals(password, confirmPassword)) {
             Toast.makeText(MartusActivity.this,
             R.string.settings_pwd_not_equal, Toast.LENGTH_SHORT).show();
             failed = true;
         }
 
         if (failed) {
             showCreateAccountDialog();
         } else {
             createAccount(password);
             checkDesktopKey();
             //newAccountDialog.dismiss();
         }
     }
 
     public void onCancelNewAccountDialog() {
         this.finish();
     }
 
     private void showMagicWordDialog() {
         MagicWordDialog magicWordDialog = MagicWordDialog.newInstance();
         magicWordDialog.show(getSupportFragmentManager(), "dlg_magicWord");
     }
 
     public void onFinishMagicWordDialog(TextView magicWordText) {
         String magicWord = magicWordText.getText().toString().trim();
         if (magicWord.isEmpty()) {
             Toast.makeText(this, "Invalid Magic Word!", Toast.LENGTH_SHORT).show();
             showMagicWordDialog();
             return;
         }
         try {
              final AsyncTask<Object, Void, NetworkResponse> rightsTask = new UploadRightsTask().execute(gateway, martusCrypto, magicWord);
              final NetworkResponse response = rightsTask.get();
              if (!response.getResultCode().equals("ok")) {
                  Toast.makeText(this, getString(R.string.no_upload_rights), Toast.LENGTH_SHORT).show();
                  showMagicWordDialog();
              } else {
                  Toast.makeText(this, "Success - can now upload bulletins!", Toast.LENGTH_SHORT).show();
                  SharedPreferences mySettings = PreferenceManager.getDefaultSharedPreferences(this);
                  SharedPreferences.Editor editor = mySettings.edit();
                  editor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, true);
                  editor.commit();
              }
         } catch (Exception e) {
              Log.e(AppConfig.LOG_LABEL, "Problem verifying upload rights", e);
              Toast.makeText(this, "Problem confirming magic word", Toast.LENGTH_SHORT).show();
         }
     }
 
 }
