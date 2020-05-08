 package org.martus.android;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.martus.android.dialog.ConfirmationDialog;
 import org.martus.client.bulletinstore.ClientBulletinStore;
 import org.martus.clientside.ClientSideNetworkGateway;
 import org.martus.common.HQKey;
 import org.martus.common.HQKeys;
 import org.martus.common.bulletin.AttachmentProxy;
 import org.martus.common.bulletin.Bulletin;
 import org.martus.common.crypto.MartusCrypto;
 import org.martus.common.crypto.MartusSecurity;
 import org.martus.common.packet.UniversalId;
 
 import android.app.ActionBar;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.bugsense.trace.BugSenseHandler;
 import com.ipaulpro.afilechooser.utils.FileUtils;
 
 /**
  * @author roms
  *         Date: 10/25/12
  */
 public class BulletinActivity extends BaseActivity implements BulletinSender, ConfirmationDialog.ConfirmationDialogListener {
 
     final int ACTIVITY_CHOOSE_ATTACHMENT = 2;
     public static final String EXTRA_ATTACHMENT = "org.martus.android.filePath";
     public static final String EXTRA_ATTACHMENTS = "org.martus.android.filePaths";
     public static final String EXTRA_ACCOUNT_ID = "org.martus.android.accountId";
     public static final String EXTRA_LOCAL_ID = "org.martus.android.localId";
     public static final String EXTRA_BULLETIN_TITLE = "org.martus.android.title";
 
     private SharedPreferences mySettings;
     private ClientBulletinStore store;
     private HQKey hqKey;
     private String serverPublicKey;
     private ClientSideNetworkGateway gateway = null;
     private String serverIP;
     private boolean autoLogout;
 
     private Bulletin bulletin;
     private EditText titleText;
     private EditText summaryText;
     private ProgressDialog dialog;
     private ArrayAdapter<String> attachmentAdapter;
     private boolean shouldShowInstallExplorer = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         BugSenseHandler.initAndStartSession(BulletinActivity.this, ExternalKeys.BUGSENSE_KEY);
         setContentView(R.layout.send_bulletin_linear);
 
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
 
         MartusSecurity martusCrypto = AppConfig.getInstance().getCrypto();
         if (!martusCrypto.hasKeyPair()) {
             showLoginRequiredDialog();
         }
 
         mySettings = PreferenceManager.getDefaultSharedPreferences(this);
         hqKey = new HQKey(mySettings.getString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, ""));
         store = AppConfig.getInstance().getStore();
         updateSettings();
         gateway = ClientSideNetworkGateway.buildGateway(serverIP, serverPublicKey);
 
         if (null == bulletin) {
             try {
                 bulletin = createBulletin();
             } catch (Exception e) {
                 Log.e(AppConfig.LOG_LABEL, "problem creating bulletin", e);
                 MartusActivity.showMessage(this, "couldn't create new bulletin", "Error");
             }
         }
 
         titleText = (EditText)findViewById(R.id.createBulletinTitle);
         summaryText = (EditText)findViewById(R.id.bulletinSummary);
 
         attachmentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
         ListView list = (ListView)findViewById(android.R.id.list);
         list.setTextFilterEnabled(true);
         list.setAdapter(attachmentAdapter);
 
         addAttachmentFromIntent();
     }
 
     public void chooseAttachment() {
         shouldShowInstallExplorer = false;
         try {
             Intent chooseFile = FileUtils.createGetContentIntent();
             Intent intent = Intent.createChooser(chooseFile, "Choose an attachment");
             startActivityForResult(intent, ACTIVITY_CHOOSE_ATTACHMENT);
         } catch (ActivityNotFoundException e) {
             Log.e(AppConfig.LOG_LABEL, "Failed choosing file", e);
             e.printStackTrace();
         }
     }
 
     public void sendBulletin() {
         try {
             zipBulletin(bulletin);
         } catch (Exception e) {
             Log.e(AppConfig.LOG_LABEL, "Failed sending bulletin", e);
         }
     }
 
     private void addAttachmentFromIntent() {
 
         Intent intent = getIntent();
         ArrayList<File> attachments = getFilesFromIntent(intent);
 
         try {
             for (File attachment : attachments) {
                 addAttachmentToBulletin(attachment);
             }
         } catch (Exception e) {
             Log.e(AppConfig.LOG_LABEL, "problem adding attachment to bulletin", e);
             MartusActivity.showMessage(this, "problem adding attachment to bulletin", "Error");
         }
     }
 
     private void addAttachmentToBulletin(File attachment) throws IOException, MartusCrypto.EncryptionException {
         AttachmentProxy attProxy = new AttachmentProxy(attachment);
         bulletin.addPublicAttachment(attProxy);
         attachmentAdapter.add(attachment.getName());
     }
 
     private ArrayList<File> getFilesFromIntent(Intent intent) {
         ArrayList<File> attachments = new ArrayList<File>(1);
         String filePath;
         String[] filePaths;
         filePath = intent.getStringExtra(EXTRA_ATTACHMENT);
         filePaths = intent.getStringArrayExtra(EXTRA_ATTACHMENTS);
 
         try {
             if (null != filePath) {
                 attachments.add(new File(filePath));
             } else if (null != filePaths) {
                 for (String path : filePaths) {
                     attachments.add(new File(path));
                 }
             } else {
                 //check if file uri was passed via Android Send
                 Bundle bundle = intent.getExtras();
                 if (null != bundle) {
                     if (bundle.containsKey(Intent.EXTRA_STREAM)) {
                         ArrayList<Uri> uris;
                         Object payload = bundle.get(Intent.EXTRA_STREAM);
                         if (payload instanceof Uri) {
                             uris = new ArrayList<Uri>(1);
                             uris.add((Uri)payload);
                         } else {
                             uris = (ArrayList<Uri>)payload;
                         }
                         for (Uri uri : uris) {
                             attachments.add(getFileFromUri(uri));
                         }
                     }
                 }
             }
 
 
         } catch (Exception e) {
             Log.e(AppConfig.LOG_LABEL, "problem getting files for attachments", e);
             MartusActivity.showMessage(this, "problem getting files for attachments", "Error");
         }
 
         return attachments;
     }
 
     private File getFileFromUri(Uri uri) throws URISyntaxException {
         String filePath = FileUtils.getPath(this, uri);
         return new File(filePath);
     }
 
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode) {
             case ACTIVITY_CHOOSE_ATTACHMENT: {
                 if (resultCode == RESULT_OK) {
                     if (null != data) {
                         Uri uri = data.getData();
                         try {
                             String filePath = FileUtils.getPath(this, uri);
                             File file = new File(filePath);
                             addAttachmentToBulletin(file);
                         } catch (Exception e) {
                             Log.e(AppConfig.LOG_LABEL, "problem getting attachment", e);
                             Toast.makeText(this, "problem getting attachment", Toast.LENGTH_SHORT).show();
                         }
                     }
                 } else if (resultCode == RESULT_CANCELED) {
                     shouldShowInstallExplorer = true;
                 }
                 break;
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.send_bulletin, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, MartusActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             case R.id.send_bulletin_menu_item:
                 sendBulletin();
                 return true;
             case R.id.cancel_bulletin_menu_item:
                 showConfirmationDialog();
                 return true;
             case R.id.add_attachment_menu_item:
                 chooseAttachment();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     @Override
     public void onResume() {
         super.onResume();
         if (shouldShowInstallExplorer) {
             showInstallExplorerDialog();
            shouldShowInstallExplorer = false;
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         BugSenseHandler.closeSession(BulletinActivity.this);
     }
 
     private void zipBulletin(Bulletin bulletin)  {
 
         dialog = new ProgressDialog(this);
         dialog.setTitle("Packaging...");
         dialog.setIndeterminate(true);
         dialog.setCanceledOnTouchOutside(false);
         dialog.show();
 
         String author = mySettings.getString(SettingsActivity.KEY_AUTHOR, "Unknown author");
         bulletin.set(Bulletin.TAGAUTHOR, author);
         String title = titleText.getText().toString().trim();
         String summary = summaryText.getText().toString().trim();
         bulletin.set(Bulletin.TAGTITLE, title);
         bulletin.set(Bulletin.TAGSUMMARY, summary);
         stopInactivityTimer();
         setIgnoreInactivity(true);
 
         final AsyncTask<Object, Integer, File> zipTask = new ZipBulletinTask(bulletin, this);
         zipTask.execute(getCacheDir(), store);
 
     }
 
     private Bulletin createBulletin() throws Exception
     {
         Bulletin b = store.createEmptyBulletin();
         b.set(Bulletin.TAGLANGUAGE, getDefaultLanguageForNewBulletin());
         b.setAuthorizedToReadKeys(new HQKeys(hqKey));
         b.setDraft();
         b.setAllPrivate(true);
         return b;
     }
 
     private String getDefaultLanguageForNewBulletin()
     {
         return mySettings.getString(SettingsActivity.KEY_DEFAULT_LANGUAGE, Locale.getDefault().getLanguage());
     }
 
     private void updateSettings() {
         serverIP = mySettings.getString(SettingsActivity.KEY_SERVER_IP, "");
         serverPublicKey = mySettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
         autoLogout = mySettings.getBoolean(SettingsActivity.KEY_AUTO_LOGOUT, false);
     }
 
     @Override
     public void onSent() {
         dialog.dismiss();
         Toast.makeText(this, "Bulletin Sent!", Toast.LENGTH_LONG).show();
         if (autoLogout) {
             MartusActivity.logout(BulletinActivity.this);
         }
         finish();
     }
 
     @Override
     public void onZipped(File zippedFile) {
         dialog.dismiss();
         sendZippedBulletin(zippedFile);
     }
 
     private void sendZippedBulletin(File zippedFile) {
         dialog = new ProgressDialog(this);
         dialog.setTitle("Sending...");
         dialog.setIndeterminate(false);
         dialog.setCanceledOnTouchOutside(false);
         dialog.setMax(100);
         dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         dialog.setProgress(0);
         dialog.show();
 
         String bulletinTitle = bulletin.get(Bulletin.TAGTITLE);
         UniversalId bulletinId = bulletin.getUniversalId();
         try {
             removeCachedUriAttachments();
             store.destroyBulletin(bulletin);
         } catch (IOException e) {
             Log.e(AppConfig.LOG_LABEL, "problem destroying bulletin", e);
         }
         final AsyncTask<Object, Integer, String> uploadTask = new UploadBulletinTask((MartusApplication)getApplication(),
                 bulletinTitle, this, bulletinId);
         uploadTask.execute(bulletin.getUniversalId(), zippedFile, gateway, AppConfig.getInstance().getCrypto());
     }
 
     private void removeCachedUriAttachments() {
         AttachmentProxy[] attachmentProxies = bulletin.getPublicAttachments();
         for (AttachmentProxy proxy : attachmentProxies) {
             String label = proxy.getLabel();
             File file = new File(getCacheDir(), label);
             if (file.exists()) {
                 file.delete();
             }
         }
     }
 
     @Override
     public void onProgressUpdate(int progress) {
         dialog.setProgress(progress);
     }
 
     @Override
     public void onConfirmationAccepted() {
         this.finish();
     }
 
 }
