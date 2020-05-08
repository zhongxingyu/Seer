 package in.uncod.android.droidbooru;
 
 import in.uncod.android.net.DownloadFilesTask;
 import in.uncod.android.util.threading.TaskWithResultListener.OnResultListener;
 import in.uncod.nativ.AbstractNetworkCallbacks;
 import in.uncod.nativ.HttpClientNetwork;
 import in.uncod.nativ.INetworkHandler;
 import in.uncod.nativ.Image;
 import in.uncod.nativ.KeyPredicate;
 import in.uncod.nativ.ORMDatastore;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpHost;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 
 public class MainActivity extends SherlockActivity {
     private final class AuthCallback extends AbstractNetworkCallbacks {
         @Override
         public void onError(INetworkHandler handler, int errorCode, String message) {
             runOnUiThread(new Runnable() {
                 public void run() {
                     Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                 }
             });
         }
 
         @Override
         public void finished(final String extras) {
             downloadFiles(10, 0, true);
         }
     }
 
     private static final String TAG = "MainActivity";
 
     /**
      * Request code for uploading a file
      */
     private static final int REQ_CODE_UPLOAD_FILE = 0;
 
     private ORMDatastore mDatastore;
     private File mDataDirectory;
     private Intent mLaunchIntent;
     private URI mServerAddress;
     private URI mServerNativApiUrl;
     private URI mServerFilePostUrl;
     private URI mServerFileRequestUrl;
     private URI mServerThumbRequestUrl;
     private Account mAccount;
     private SharedPreferences mPrefs;
     private GridView mGridView;
 
     private ArrayAdapter<BooruFile> mBooruFileAdapter;
 
     private boolean mDownloading;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Upload
         menu.add(R.string.upload).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                     public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                         showFileChooser();
 
                         return true;
                     }
                 });
 
         // Login settings
         menu.add(R.string.settings).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                     public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                         startActivity(new Intent(MainActivity.this, LoginSettingsActivity.class));
 
                         return true;
                     }
                 });
 
         return super.onCreateOptionsMenu(menu);
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Resources resources = getResources();
 
         mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         getAndStoreAccount(resources);
 
         // Get server address
         mServerAddress = URI.create(HttpHost.DEFAULT_SCHEME_NAME
                 + "://"
                 + mPrefs.getString(resources.getString(R.string.pref_selected_server),
                         resources.getString(R.string.dv_pref_selected_server)));
 
         // Setup various endpoints
         mServerNativApiUrl = URI.create(mServerAddress + "/v2/api");
         mServerFilePostUrl = URI.create(mServerAddress + "/upload/curl");
         mServerFileRequestUrl = URI.create(mServerAddress + "/img/");
         mServerThumbRequestUrl = URI.create(mServerAddress + "/thumb/");
 
         // Set data directory
         mDataDirectory = getExternalFilesDir(null);
         Log.d(TAG, "Using data directory: " + mDataDirectory.getAbsolutePath());
 
        initAndAuthNativ();

         Intent intent = getIntent();
         if (Intent.ACTION_SEND.equals(intent.getAction())) {
             // Started via Share request
             Log.d(TAG, "Received single file upload request");
 
             // Uploading a single file
            uploadFiles(new File[] { getFileForUri((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM)) });
 
             finish();
         }
         else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
             // Started via Share request
             Log.d(TAG, "Received multiple file upload request");
 
             // Uploading multiple files
             ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
             File[] files = new File[fileUris.size()];
 
             int i = 0;
             for (Uri uri : fileUris) {
                 files[i] = getFileForUri(uri);
 
                 i++;
             }
 
            uploadFiles(files);
 
             finish();
         }
         else {
             // Normal launch
             initializeUI();
         }
     }
 
     private void getAndStoreAccount(Resources resources) {
         // Get stored account if we have one
         String storedAccountName = mPrefs.getString(resources.getString(R.string.pref_account_name), "");
         if (storedAccountName != "") {
             mAccount = new Account(storedAccountName, mPrefs.getString(
                     resources.getString(R.string.pref_account_type), ""));
         }
         else {
             Account[] accounts = AccountManager.get(this).getAccounts();
             if (accounts.length > 0) {
                 // TODO Let user pick account
                 for (Account account : accounts) {
                     if (account.name.endsWith("ironclad.mobi")) {
                         mAccount = account;
                     }
                 }
 
                 if (mAccount == null)
                     mAccount = createDefaultAccount();
 
                 storeAccountInPrefs(mAccount, resources);
             }
             else {
                 // Proceed with a default account
                 mAccount = createDefaultAccount();
             }
         }
 
         Log.d(TAG, "Using account " + mAccount.name);
     }
 
     private void storeAccountInPrefs(Account account, Resources resources) {
         Editor editor = mPrefs.edit();
 
         editor.putString(resources.getString(R.string.pref_account_name), account.name);
         editor.putString(resources.getString(R.string.pref_account_type), account.type);
 
         editor.commit();
     }
 
     private Account createDefaultAccount() {
         return new Account("droidbooru@ironclad.mobi", "Google");
     }
 
     private void initAndAuthNativ() {
         mDatastore = ORMDatastore.create(new File(mDataDirectory, "droidbooru.db").getAbsolutePath());
         mDatastore.setDownloadPathPrefix(mDataDirectory.getAbsolutePath() + File.separator);
         mDatastore.setNetworkHandler(new HttpClientNetwork(mServerNativApiUrl.toString()));
         mDatastore.authenticate("test", "test", new AuthCallback());
     }
 
     private void initializeUI() {
         mLaunchIntent = new Intent();
         mLaunchIntent.setAction(android.content.Intent.ACTION_VIEW);
 
         mBooruFileAdapter = new ArrayAdapter<BooruFile>(this, 0) {
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 return displayThumbInView(convertView, mBooruFileAdapter.getItem(position));
             }
         };
 
         mGridView = (GridView) findViewById(R.id.images);
         mGridView.setAdapter(mBooruFileAdapter);
 
         mGridView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 try {
                     // Launch a viewer for the file
                     BooruFile bFile = mBooruFileAdapter.getItem(position);
 
                     mLaunchIntent.setDataAndType(Uri.parse(bFile.getActualUrl().toString()),
                             bFile.getMimeForLaunch());
 
                     startActivity(mLaunchIntent);
                 }
                 catch (ActivityNotFoundException e) {
                     e.printStackTrace();
 
                     Toast.makeText(MainActivity.this, "Sorry, your device can't view the original file!",
                             Toast.LENGTH_LONG).show();
                 }
             }
         });
 
         mGridView.setOnScrollListener(new OnScrollListener() {
             public void onScrollStateChanged(AbsListView view, int scrollState) {
                 // Unused for now
             }
 
             public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                     int totalItemCount) {
                 if (totalItemCount - (firstVisibleItem + visibleItemCount) < visibleItemCount) {
                     // User has scrolled through most of the items; load three more pages
                     downloadFiles(visibleItemCount * 3, mBooruFileAdapter.getCount(), false);
                 }
             }
         });
     }
 
     private void showFileChooser() {
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("*/*");
         intent.addCategory(Intent.CATEGORY_OPENABLE);
 
         startActivityForResult(intent, REQ_CODE_UPLOAD_FILE);
     }
 
     private File getFileForUri(Uri uri) {
         File retFile = null;
 
         if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
             try {
                 // Copy the file to our cache
                 retFile = File.createTempFile("upload_", ".tmp", getExternalCacheDir());
 
                 InputStream inStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outStream = new FileOutputStream(retFile);
 
                 byte[] buffer = new byte[1024];
                 while (inStream.read(buffer) != -1) {
                     outStream.write(buffer);
                 }
 
                 inStream.close();
                 outStream.close();
             }
             catch (IOException e) {
                 e.printStackTrace();
 
                 // Delete the temp file and return null
                 retFile.delete();
                 retFile = null;
             }
         }
         else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
             retFile = new File(uri.getPath());
         }
 
         return retFile;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
         case REQ_CODE_UPLOAD_FILE:
             if (resultCode == RESULT_OK) {
                 // Upload chosen file
                 Uri uri = data.getData();
                 Log.d(TAG, "Upload request for " + uri);
 
                 File uploadFile = getFileForUri(uri);
 
                 if (uploadFile != null) {
                    uploadFiles(new File[] { uploadFile });
                 }
             }
             break;
         default:
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
    private void uploadFiles(final File[] files) {
         String email = mAccount.name;
         String tags = "droidbooru,"
                 + new SimpleDateFormat("MM-dd-yy").format(Calendar.getInstance().getTime());
 
         new BooruUploadTask(mServerFilePostUrl, email, tags, new OnResultListener<Void>() {
             public void onTaskResult(Void result) {
                // Download and display the image(s) that were just uploaded
                downloadFiles(files.length, 0, true);
             }
         }, null).execute(files);
     }
 
     private BooruFile[] createBooruFilesForFiles(Image[] files) {
         BooruFile[] bFiles = new BooruFile[files.length];
 
         int i = 0;
         for (Image file : files) {
             try {
                 bFiles[i] = BooruFile.create(mDatastore, file, mServerThumbRequestUrl.toURL(),
                         mServerFileRequestUrl.toURL());
             }
             catch (MalformedURLException e) {
                 Log.e(TAG,
                         "Couldn't parse URL for file " + file.getFilehash() + "."
                                 + BooruFile.getFileExtension(file));
 
                 e.printStackTrace();
             }
 
             i++;
         }
 
         return bFiles;
     }
 
     private View displayThumbInView(View convertView, BooruFile booruFile) {
         String filePath = booruFile.getThumbPath();
 
         ImageView imageView;
 
         // Determine if we can reuse the view
         if (convertView == null) {
             imageView = (ImageView) LayoutInflater.from(this).inflate(R.layout.view_gallery_thumbnail, null);
         }
         else {
             imageView = (ImageView) convertView;
         }
 
         // Load image
         imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
 
         return imageView;
     }
 
     private URL[] getThumbUrlsForBooruFiles(BooruFile[] bFiles) {
         URL[] urls = new URL[bFiles.length];
 
         int i = 0;
         for (BooruFile bFile : bFiles) {
             urls[i] = bFile.getThumbUrl();
             i++;
         }
 
         return urls;
     }
 
     /**
      * Queries the nodebooru service for files, ordered by uploaded date, descending
      * 
      * @param number
      *            The number of files to retrieve
      * @param offset
      *            The number of files to skip
      * @param showProgressDialog
      *            If true, a progress dialog will be displayed while downloading
      */
     private void downloadFiles(int number, final int offset, final boolean showProgressDialog) {
         if (!mDownloading) {
             mDownloading = true;
 
             mDatastore.externalQueryImage(KeyPredicate.defaultPredicate().orderBy("uploadedDate", true)
                     .limit(number).offset(offset), null, new AbstractNetworkCallbacks() {
                 @Override
                 public void onReceivedImage(ORMDatastore ds, String queryName, final Image[] files) {
                     if (files.length > 0) {
                         final BooruFile[] bFiles = createBooruFilesForFiles(files);
 
                         runOnUiThread(new Runnable() {
                             public void run() {
                                 ProgressDialog dialog = null;
 
                                 if (showProgressDialog) {
                                     // Setup progress dialog
                                     dialog = new ProgressDialog(MainActivity.this);
                                     dialog.setTitle(R.string.downloading);
                                     dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                     dialog.setCancelable(false);
                                 }
 
                                 // Download thumbnails
                                 new DownloadFilesTask(mDataDirectory, false,
                                         new OnResultListener<List<File>>() {
                                             public void onTaskResult(List<File> dFiles) {
                                                 // Make sure the files appear in order
                                                 // Add the files to the end of the list
                                                 int i = Math.min(mBooruFileAdapter.getCount(), offset);
                                                 int j = 0;
                                                 for (BooruFile file : bFiles) {
                                                     mBooruFileAdapter.insert(file, i); // addAll() is API level 11
 
                                                     // Associate the files with each BooruFile
                                                     bFiles[j].setThumbFile(dFiles.get(j));
                                                     i++;
                                                     j++;
                                                 }
                                                 mBooruFileAdapter.notifyDataSetChanged();
 
                                                 mDownloading = false;
                                             }
                                         }, dialog).execute(getThumbUrlsForBooruFiles(bFiles));
                             }
                         });
                     }
                     else {
                         runOnUiThread(new Runnable() {
                             public void run() {
                                 Toast.makeText(MainActivity.this, "No images", Toast.LENGTH_LONG).show();
                             }
                         });
                     }
                 }
             });
         }
     }
 }
