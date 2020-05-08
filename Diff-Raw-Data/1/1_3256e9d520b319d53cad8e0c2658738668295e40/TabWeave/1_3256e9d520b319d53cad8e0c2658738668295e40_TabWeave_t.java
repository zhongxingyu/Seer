 package nl.terr.tabweave;
 
 import info.elebescond.weave.exception.WeaveException;
 
 import java.io.IOException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.spec.InvalidKeySpecException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.crypto.NoSuchPaddingException;
 
 import nl.terr.weave.Config;
 import nl.terr.weave.CryptoWeave;
 import nl.terr.weave.SyncWeave;
 import nl.terr.weave.impl.CryptoWeaveImpl;
 import nl.terr.weave.impl.SyncWeaveImpl;
 import nl.terr.weave.impl.UserWeaveImpl;
 
 import org.apache.http.client.HttpResponseException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import biz.source_code.base64Coder.Base64Coder;
 
 public class TabWeave extends ListActivity {
 
 //    private TabWeaveDbAdapter mWeaveTabsDbAdapter;
 //  private CryptoWeave mCryptoWeave;
     private UserWeaveImpl mUserWeave;
     private SyncWeaveImpl mSyncWeave;
 
     public static final int ACTIVITY_EDIT_SETTINGS = 1;
 
     byte[] bytePrivateKeyDecrypted;
     byte[] byteSymmetricKeyDecrypted;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
 //        mWeaveTabsDbAdapter = new TabWeaveDbAdapter(this);
 //        mWeaveTabsDbAdapter.open();
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         refreshTabList();
     }
 
     public void fillData(List<JSONObject> lTabs)
     {
         // Create an array to specify the fields we want to display in the list (only TITLE)
         String[] from = new String[]{SyncWeave.KEY_TITLE, SyncWeave.KEY_URL, SyncWeave.KEY_ROWID};
 
         // and an array of the fields we want to bind those fields to (in this case just text1)
         int[] to = new int[]{ R.id.title, R.id.url};
 
         int iBrowserInstances   = lTabs.size();
         MatrixCursor matrixCursor   = new MatrixCursor(from);
 
         int iTabId  = 0;
         
         // Show "No tabs" message
         TextView    tvNoTabs    = (TextView)findViewById(R.id.no_tabs);
         tvNoTabs.setVisibility(View.VISIBLE);
         
 
         try {
             for(int iWeaveBrowserInstance = 0; iWeaveBrowserInstance < iBrowserInstances; iWeaveBrowserInstance++)
             {
                 int iNumberTabs = lTabs.get(iWeaveBrowserInstance).getJSONArray("tabs").length();
                 
                 // Hide "No tabs" message
                 if(iNumberTabs > 0) {
                     tvNoTabs.setVisibility(View.INVISIBLE);
                 }
 
                 for(int iWeaveTab = 0; iWeaveTab < iNumberTabs; iWeaveTab++)
                 {
                     matrixCursor.newRow()
                         .add(lTabs.get(iWeaveBrowserInstance).getJSONArray("tabs").getJSONObject(iWeaveTab).getString("title"))
                         .add(lTabs.get(iWeaveBrowserInstance).getJSONArray("tabs").getJSONObject(iWeaveTab).getJSONArray("urlHistory").getString(0))
                         .add(iTabId);
 
                     iTabId++;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             
             AlertDialog alert   = createAlertDialog(this, "Warning", e.getMessage());
             alert.show();
         }
 
         ListAdapter listAdapter    = new SimpleCursorAdapter(
           this,                 // Context
           R.layout.tab_row,     // Specify the row template to use (here, two columns bound to the two retrieved cursor rows).
           matrixCursor,
           from,
           to
         );
 
         setListAdapter(listAdapter);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
 
         TextView viewText   = (TextView)v.findViewById(R.id.url);
         Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(viewText.getText().toString()));
         startActivity(viewIntent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case R.id.menuSettings:
                 showSettings();
                 return true;
 
             case R.id.menuRefresh:
                 refreshTabList();
                 return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
 
         if(resultCode == RESULT_OK)
         {
             Bundle extras   = intent.getExtras();
         }
 
         switch(requestCode)
         {
             case ACTIVITY_EDIT_SETTINGS:
 
                 if(resultCode != RESULT_OK)
                 {
                     Log.d("ACTIVITY_EDIT_SETTINGS", "Result from ACTIVITY_EDIT_SETTINGS was not 'OK'");
                 }
 
                 // Recalculte crypto keys if necessary
                 //prepareCryptoKeys();
                 //refreshTabList();
 
                 break;
         }
     }
 
     /**
      * Checks if all the required preferences for getting the tab data
      * are present. If not, launches settings activity
      */
     private void checkPreferencesComplete() {
         Config mConfig  = Config.getConfig(this);
 
         String sUsername    = mConfig.getUsername();
         String sPassword    = mConfig.getPassword();
         String sPassphrase  = mConfig.getPassphrase();
         String sSyncServerUrl = mConfig.getWeaveNode();
 
         // Redirect the user to the settings panel if any of the credentials are missing
         if(sUsername == "" || sPassword == "" || sPassphrase == "") {
             showSettings();
 
            Log.d("checkPreferencesComplete", "This log is placed after showSettings()");
             Log.d("chechkPreferencesComplete", "This log is placed after showSettings()");
         }
 
         // Check the settings if the weaveNode is already known. If not, request it
         if(sSyncServerUrl == "")
         {
             mUserWeave      = new UserWeaveImpl();
 
             try {
                 mConfig.setWeaveNode(mUserWeave.getUserStorageNode(sUsername, null));
                 mConfig.commit();
 
             } catch (WeaveException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     private void showSettings() {
         Intent i    = new Intent(this, TabWeaveSettingsActivity.class);
         startActivityForResult(i, ACTIVITY_EDIT_SETTINGS);
     }
 
     private void prepareCryptoKeys()
         throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, WeaveException, JSONException, IOException, InvalidKeySpecException {
 
         checkPreferencesComplete();
 
         Config mConfig = Config.getConfig(this);
 
         mSyncWeave      = new SyncWeaveImpl(mConfig.getWeaveNode(), mConfig.getUsername(), mConfig.getPassword(), mConfig.getPassphrase());
 
         // Check settings storage for the user's private key
         if(mConfig.getPrivateKey() == "") {
 
             bytePrivateKeyDecrypted  = mSyncWeave.getPrivateKey();
 
             // Save the generated private key
             mConfig.setPrivateKey(new String(Base64Coder.encode(bytePrivateKeyDecrypted)));
             mConfig.commit();
         }
 
         bytePrivateKeyDecrypted     = Base64Coder.decode(mConfig.getPrivateKey());
 
         if(mConfig.getSymmetricKey() == "")
         {
             byteSymmetricKeyDecrypted   = mSyncWeave.getSymmetricKey(bytePrivateKeyDecrypted);
 
             mConfig.setSymmetricKey(new String(Base64Coder.encode(byteSymmetricKeyDecrypted)));
             mConfig.commit();
         }
 
         byteSymmetricKeyDecrypted   = Base64Coder.decode(mConfig.getSymmetricKey());
     }
 
     public void refreshTabList() {
         new RefreshTabList().execute();
     }
 
     private AlertDialog createAlertDialog(Context context, String sTitle, String sMessage) {
         AlertDialog.Builder builder = new AlertDialog.Builder(TabWeave.this);
         builder.setTitle(sTitle)
             .setMessage(sMessage)
             .setCancelable(false)
             .setIcon(android.R.drawable.ic_dialog_alert)
             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                }
             });
         AlertDialog alert = builder.create();
 
         return alert;
     }
 
     private class RefreshTabList extends AsyncTask<Void, String, List<JSONObject>> {
         private JSONObject oTabPayload;
         private String sTabCipherText;
         private byte[] byteTabCipherText;
         private byte[] byteTabCipherIV;
 
         private CryptoWeave mCryptoWeave    = new CryptoWeaveImpl();
 
         private TextView statusMessage;
 
         private Exception exception;
 
         @Override
         protected void onPreExecute() {
 //          ImageView logoImageView = (ImageView)findViewById(R.id.weaveLogoImageView);
           setContentView(R.layout.loading_screen);
 
           statusMessage = (TextView)findViewById(R.id.statusMessage);
         }
 
         @Override
         protected List<JSONObject> doInBackground(Void... params) {
 
             Log.d("RefreshTabList", "Starting doInBackground()");
 
             List<JSONObject> lTabs      = new ArrayList<JSONObject>();
 
             try
             {
                 publishProgress(getString(R.string.status_loading_crypto_keys));
 
                 // Retrieve or calculate the crypto keys
                 prepareCryptoKeys();
 
                 publishProgress(getString(R.string.status_retrieving_tabs));
 
                 // Request all tabs objects
                 JSONArray aCollection   = mSyncWeave.getCollection("tabs?full=1");
                 JSONObject[] oTabs      = new JSONObject[32];
 
                 publishProgress(getString(R.string.status_decrypting_data));
 
                 int iTabCount           = aCollection.length();
                 if(iTabCount > oTabs.length)
                 {
                     iTabCount   = oTabs.length;
                 }
 
                 for(int x = 0; x < iTabCount; x++)
                 {
                     //oTabs[x]    = mSyncWeave.getItem("tabs", aCollection.getString(x)); // Only needed when not requesting ?full=1
                     oTabs[x]         = aCollection.getJSONObject(x);
 
                     // Decrypt the ciphertext of this tab
                     oTabPayload         = new JSONObject(oTabs[x].getString("payload"));
                     byteTabCipherText   = Base64Coder.decode(oTabPayload.getString("ciphertext"));
                     byteTabCipherIV     = Base64Coder.decode(oTabPayload.getString("IV"));
                     sTabCipherText      = new String(mCryptoWeave.decryptCipherText(byteSymmetricKeyDecrypted, byteTabCipherIV, byteTabCipherText));
 
                     lTabs.add(x, new JSONObject(sTabCipherText));
                 }
             }
             catch(HttpResponseException e)
             {
                 if(e.getStatusCode() == 401)
                 {
                     exception   = new HttpResponseException(401, getString(R.string.http_unauthorized));
                 }
             }
             catch(Exception e)
             {
                 exception   = e;
 
                 cancel(true);
             }
 
             return lTabs;
         }
 
         @Override
         protected void onProgressUpdate(String... params) {
 
             statusMessage.setText(params[0]);
         }
 
         @Override
         protected void onPostExecute(List<JSONObject> lTabs) {
             setContentView(R.layout.main);
 
             if(exception != null) {
 
                 exception.printStackTrace();
 
                 AlertDialog alert   = createAlertDialog(TabWeave.this, "Warning", exception.getMessage());
                 alert.show();
             }
             else
             {
                 fillData(lTabs);
             }
         }
     }
 }
