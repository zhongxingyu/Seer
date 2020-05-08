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
 import org.martus.common.crypto.MartusCrypto;
 import org.martus.common.crypto.MartusSecurity;
 import org.martus.common.network.NetworkResponse;
 
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
         CreateAccountDialog.CreateAccountDialogListener, MagicWordDialog.MagicWordDialogListener, OrbotHandler {
 
     public final static String PROXY_HOST = "127.0.0.1"; //test the local device proxy provided by Orbot/Tor
     public final static int PROXY_HTTP_PORT = 8118; //default for Orbot/Tor
     public final static int PROXY_SOCKS_PORT = 9050; //default for Orbot/Tor
 
     private static final String PACKETS_DIR = "/packets";
     private static final String PREFS_DIR = "/shared_prefs";
 
     public static final int MAX_LOGIN_ATTEMPTS = 3;
     public static final int MIN_PASSWORD_SIZE = 8;
     private String serverPublicKey;
 
     private MartusSecurity martusCrypto;
     private ClientSideNetworkGateway gateway = null;
     private String serverIP;
     private int invalidLogins;
     private CheckBox torCheckbox;
    private SharedPreferences mySettings;
 
     static final int ACTIVITY_DESKTOP_KEY = 2;
     public static final int ACTIVITY_BULLETIN = 3;
     public static final String RETURN_TO = "return_to";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         BugSenseHandler.initAndStartSession(MartusActivity.this, ExternalKeys.BUGSENSE_KEY);
         setContentView(R.layout.main);
 
        mySettings = PreferenceManager.getDefaultSharedPreferences(this);
         torCheckbox = (CheckBox)findViewById(R.id.checkBox_use_tor);
         updateSettings();
 
         martusCrypto = AppConfig.getInstance().getCrypto();
 
     }
 
     protected void onStart() {
         super.onStart();
         invalidLogins = 0;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (martusCrypto.hasKeyPair()) {
             checkDesktopKey();
             if (!confirmServerPublicKey()) {
                 Intent intent = new Intent(MartusActivity.this, ServerActivity.class);
                 startActivityForResult(intent, EXIT_REQUEST_CODE);
                 return;
             }
 
             boolean canUpload = mySettings.getBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
             if (!canUpload) {
                 showMagicWordDialog();
             }
             OrbotHelper oc = new OrbotHelper(this);
             if (!oc.isOrbotInstalled() || !oc.isOrbotRunning()) {
                 torCheckbox.setChecked(false);
             }
 
         } else {
             if (isAccountCreated()) {
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
 
     @Override
         protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == EXIT_REQUEST_CODE && resultCode == EXIT_RESULT_CODE) {
             finish();
         }
     }
 
     public void sendBulletin(View view) {
         Intent intent = new Intent(MartusActivity.this, BulletinActivity.class);
         startActivityForResult(intent, EXIT_REQUEST_CODE) ;
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
                 logout();
                 finish();
                 return true;
             case R.id.server_menu_item:
                 intent = new Intent(MartusActivity.this, ServerActivity.class);
                 startActivityForResult(intent, EXIT_REQUEST_CODE);
                 return true;
             case R.id.reset_install_menu_item:
                 showConfirmationDialog();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void clearPrefsDir() {
         String prefsDir = getCacheDir().getParent() + PREFS_DIR;
         File prefsDirFile = new File(prefsDir);
         clearDirectory(prefsDirFile);
     }
 
     private void removePacketsDir() {
         String packetsDirectory = getCacheDir().getAbsolutePath() + PACKETS_DIR;
         File packetsDirFile = new File(packetsDirectory);
         clearDirectory(packetsDirFile);
         packetsDirFile.delete();
     }
 
     public static void logout() {
         AppConfig.getInstance().getCrypto().clearKeyPair();
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
         String desktopPublicKeyString = mySettings.getString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, "");
         if (desktopPublicKeyString.length() < 1) {
             Intent intent = new Intent(MartusActivity.this, DesktopKeyActivity.class);
             startActivityForResult(intent, ACTIVITY_DESKTOP_KEY);
         }
     }
 
     private boolean isAccountCreated() {
         String keyPairString = mySettings.getString(SettingsActivity.KEY_KEY_PAIR, "");
         return keyPairString.length() > 1;
     }
 
     private boolean confirmAccount(char[] password)  {
 
         String keyPairString = mySettings.getString(SettingsActivity.KEY_KEY_PAIR, "");
 
         // construct keypair from value read from prefs
         byte[] decodedKeyPair = Base64.decode(keyPairString, Base64.NO_WRAP);
         InputStream is = new ByteArrayInputStream(decodedKeyPair);
         try {
             martusCrypto.readKeyPair(is, password);
         } catch (Exception e) {
             //Log.e(AppConfig.LOG_LABEL, "Problem confirming password", e);
             return false;
         }
 
         martusCrypto.setShouldWriteAuthorDecryptableData(false);
         return true;
     }
 
     private void createAccount(char[] password)  {
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
 
     void showLoginDialog() {
         LoginDialog loginDialog = LoginDialog.newInstance();
         loginDialog.show(getSupportFragmentManager(), "dlg_login");
     }
 
     @Override
     public void onFinishPasswordDialog(TextView passwordText) {
         char[] password = passwordText.getText().toString().trim().toCharArray();
         boolean confirmed = (password.length >= MIN_PASSWORD_SIZE) && confirmAccount(password);
         if (!confirmed) {
             if (++invalidLogins == MAX_LOGIN_ATTEMPTS) {
                 finish();
             }
             Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
             showLoginDialog();
         }
 
         serverPublicKey = mySettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
         gateway = ClientSideNetworkGateway.buildGateway(serverIP, serverPublicKey);
 
         Intent resendService = new Intent(MartusActivity.this, ResendService.class);
         resendService.putExtra(SettingsActivity.KEY_SERVER_IP, serverIP);
         resendService.putExtra(SettingsActivity.KEY_SERVER_PUBLIC_KEY, serverPublicKey);
         startService(resendService);
 
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
             Toast.makeText(this, getString(R.string.invalid_magic_word), Toast.LENGTH_SHORT).show();
             showMagicWordDialog();
             return;
         }
         showProgressDialog(getString(R.string.progress_confirming_magic_word));
 
         final AsyncTask<Object, Void, NetworkResponse> rightsTask = new UploadRightsTask();
         rightsTask.execute(gateway, martusCrypto, magicWord);
     }
 
     private void processMagicWordResponse(NetworkResponse response) {
         dialog.dismiss();
         try {
              if (!response.getResultCode().equals("ok")) {
                  Toast.makeText(this, getString(R.string.no_upload_rights), Toast.LENGTH_SHORT).show();
                  showMagicWordDialog();
              } else {
                  Toast.makeText(this, getString(R.string.success_magic_word), Toast.LENGTH_SHORT).show();
                  SharedPreferences.Editor editor = mySettings.edit();
                  editor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, true);
                  editor.commit();
              }
         } catch (Exception e) {
              Log.e(AppConfig.LOG_LABEL, "Problem verifying upload rights", e);
              Toast.makeText(this, getString(R.string.problem_confirming_magic_word), Toast.LENGTH_SHORT).show();
         }
     }
 
     private static void clearDirectory(final File dir) {
         if (dir!= null && dir.isDirectory()) {
             try {
                 for (File child:dir.listFiles()) {
                     if (child.isDirectory()) {
                         clearDirectory(child);
                     }
                     child.delete();
                 }
             }
             catch(Exception e) {
                 Log.e(AppConfig.LOG_LABEL, String.format("Failed to clean the cache, error %s", e.getMessage()));
             }
         }
     }
 
     @Override
     public void onOrbotInstallCanceled() {
         torCheckbox.setChecked(false);
     }
 
     @Override
     public void onOrbotStartCanceled() {
         torCheckbox.setChecked(false);
     }
 
     @Override
     public String getConfirmationTitle() {
         return getString(R.string.confirm_reset_install_bulletin);
     }
 
     @Override
     public void onConfirmationAccepted() {
         removePacketsDir();
        SharedPreferences.Editor editor = mySettings.edit();
        editor.clear();
        editor.commit();
         logout();
         clearPrefsDir();
         final File cacheDir = getCacheDir();
         final String[] names = cacheDir.list(new ZipFileFilter());
         for (String name : names) {
             File zipFile = new File(cacheDir, name);
             zipFile.delete();
         }
         finish();
     }
 
     private class UploadRightsTask extends AsyncTask<Object, Void, NetworkResponse> {
         @Override
         protected NetworkResponse doInBackground(Object... params) {
 
             final ClientSideNetworkGateway gateway = (ClientSideNetworkGateway)params[0];
             final MartusSecurity signer = (MartusSecurity)params[1];
             final String magicWord = (String)params[2];
 
             NetworkResponse result = null;
 
             try {
                 result = gateway.getUploadRights(signer, magicWord);
             } catch (MartusCrypto.MartusSignatureException e) {
                 Log.e(AppConfig.LOG_LABEL, "problem getting upload rights", e);
             }
 
             return result;
         }
 
         @Override
         protected void onPostExecute(NetworkResponse result) {
             super.onPostExecute(result);
             processMagicWordResponse(result);
         }
     }
 }
