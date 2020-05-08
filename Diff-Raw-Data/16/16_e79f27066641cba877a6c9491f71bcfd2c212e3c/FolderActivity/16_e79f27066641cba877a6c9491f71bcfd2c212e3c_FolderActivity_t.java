 package gr.valor.mediafire;
 
 import eu.erikw.PullToRefreshListView;
 import eu.erikw.PullToRefreshListView.OnRefreshListener;
 import gr.valor.mediafire.api.ApiUrls;
 import gr.valor.mediafire.api.Connection;
 import gr.valor.mediafire.api.MyFilesJSONParser;
 import gr.valor.mediafire.api.MyOfflineFiles;
 import gr.valor.mediafire.database.Mediabase;
 import gr.valor.mediafire.helpers.FileIcon;
 import gr.valor.mediafire.helpers.SwipeInterface;
 import gr.valor.mediafire.parser.Elements;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FolderActivity extends BaseActivity implements SwipeInterface {
 	private static final String TAG = "FolderActivity";
 	public static final String FOLDERKEY = "gr.valor.mediafire.FOLDERKEY";
 	public static final String ONLINE = "gr.valor.mediafire.ONLINE";
 	public static final String EMPTY_DB = "gr.valor.mediafire.EMPTY_DB";
 	public static final String FULL_IMPORT = "gr.valor.mediafire.FULL_IMPORT";
 	public Folder folder;
 
 	private static final String[] FOLDER_FROM = { Folder.TYPE, Folder.NAME, Folder.CREATED, File.DOWNLOAD_ICON, File.DOWNLOADS, File.SIZE,
 			File.PRIVACY };
 	private static final int[] FOLDER_TO = { R.id.folder_item_type, R.id.folder_item_name, R.id.folder_item_created,
 			R.id.folder_item_downicon, R.id.folder_item_downloads, R.id.folder_item_size, R.id.folder_item_privacy };
 	List<Map<String, String>> folderItems = new ArrayList<Map<String, String>>();
 	SimpleAdapter folderAdapter;
 	private int folderResource = R.layout.folder_item;
 
 	View.OnTouchListener gestureListener;
 	private PullToRefreshListView listView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_folder);
 		requestFolder();
 		// Gesture detection
 		ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
 		RelativeLayout lowestLayout = (RelativeLayout) this.findViewById(R.id.activity_folder_layout);
 		lowestLayout.setOnTouchListener(activitySwipeDetector);
 		listView = (PullToRefreshListView) findViewById(R.id.listView_items);
 		listView.setOnRefreshListener(new OnRefreshListener() {
 
 			@Override
 			public void onRefresh() {
 				// Your code to refresh the list contents goes here
 
 				// Make sure you call listView.onRefreshComplete()
 				// when the loading is done. This can be done from here or any
 				// other place, like on a broadcast receive from your loading
 				// service or the onPostExecute of your AsyncTask.
 
 				// For the sake of this sample, the code will pause here to
 				// force a delay when invoking the refresh
 				listView.postDelayed(new Runnable() {
 
 					@Override
 					public void run() {
 						Log.d(TAG, "Start Refreshing");
 						mediafire.setForceOnline(true);
 						requestFolder();
 					}
 				}, 2000);
 			}
 		});
 	}
 
 	private void requestFolder() {
 		Log.d(TAG, "Showing folder " + mediafire.getCurrentFolder() + " force online: " + mediafire.isForceOnline());
 		if (!mediafire.isOnline()) {
 			if (mediafire.isForceOnline()) {
 				Toast.makeText(this, "There's no internet connection", Toast.LENGTH_SHORT).show();
 				mediafire.setForceOnline(false);
 				listView.onRefreshComplete();
 				return;
 			}
 			if (!mediafire.isEmptyDb()) {
 				createOfflineList();
 			} else {
 				Toast.makeText(this, "There's no internet connection", Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			if (mediafire.getCurrentFolder().isCached(mediafire.getCacheDuration()) && !mediafire.isForceOnline()) {
 				createCachedList();
 			} else {
 				boolean validToken = false;
 				try {
 					validToken = mediafire.isTokenValid();
 				} catch (Exception e) {
 					Intent login = new Intent(this, LoginActivity.class);
 					startActivity(login);
 				}
 				if (!validToken) {
 					if (mediafire.isFullImport()) {
 						mediafire.setCurrentFolder(Folder.createRootFolder());
 						createOfflineList();
 					} else {
 						createOfflineList();
 					}
 				} else {
 					if (mediafire.isFullImport()) {
 						createFullImportList();
 					} else {
 						createOnlineList();
 					}
 				}
 			}
 		}
 	}
 
 	private void createOfflineList() {
 		Log.d(TAG, "Creating an offline list of folder " + mediafire.getCurrentFolder());
 		getOfflineFolderItems();
 		createList();
 	}
 
 	private void createCachedList() {
 		Log.d(TAG, "Creating a cached list of folder " + mediafire.getCurrentFolder());
 		getOfflineFolderItems();
 		createList();
 	}
 
 	private void createOnlineList() {
 		Log.d(TAG, "Creating an online list of folder " + mediafire.getCurrentFolder());
 		getOnlineFolderItems();
 	}
 
 	private void createFullImportList() {
 		showAlertDialog();
 
 	}
 
 	private void dofullImport() {
 		Log.d(TAG, "Creating full import");
 		mediafire.setFullImport(true);
 		getOnlineFolderItems();
 	}
 
 	private void createList() {
 		Mediabase m = new Mediabase(this);
 		setTitle("Mediafire - " + mediafire.getCurrentFolder().name);
 		SQLiteDatabase db = m.getReadableDatabase();
 		String path = mediafire.getCurrentFolder().getFullPath(db);
 		((TextView) findViewById(R.id.listView_title)).setText(path);
 		db.close();
 		createAdapter();
 		populateList();
 	}
 
 	private void showAlertDialog() {
 		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 		alertDialog.setTitle("Import folders and files list from mediafire?");
 		alertDialog.setMessage("This may take a while depending on the numbers of folders");
 		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dofullImport();
 			}
 		});
 		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				createOfflineList();
 
 			}
 		});
 
 		alertDialog.show();
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mediafire.getCurrentFolder().folderKey.equals(Folder.ROOT_KEY)) {
 			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 			alertDialog.setTitle("Exit Application?");
 			alertDialog.setMessage("Do you want to exit the application?");
 			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Log.d(TAG, "Exiting app");
 					mediafire.setCloseApp(true);
 					FolderActivity.this.finish();
 				}
 			});
 			alertDialog.show();
 		} else {
 			Mediabase mb = new Mediabase(this);
 			SQLiteDatabase db = mb.getReadableDatabase();
 			try {
 				mediafire.setCurrentFolder(Folder.getByFolderKey(db, mediafire.getCurrentFolder().parent));
 				createOfflineList();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				db.close();
 			}
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	private void getOnlineFolderItems() {
 		Connection connection = new Connection(this);
 		MyOnlineFilesTask onlineFiles = new MyOnlineFilesTask(this, connection);
 		onlineFiles.execute(new Folder[] { mediafire.getCurrentFolder() });
 	}
 
 	private void getOfflineFolderItems() {
 		Mediabase mb = new Mediabase(this);
 		SQLiteDatabase db = mb.getReadableDatabase();
 
 		MyOfflineFiles myFiles = new MyOfflineFiles(this, db);
 		myFiles.setParent(mediafire.getCurrentFolder().folderKey);
 		try {
 			Folder curFolder = myFiles.getFiles(null);
 			createFolderItems(curFolder);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private boolean filesChanged() {
 		Mediabase mb = new Mediabase(this);
 		SQLiteDatabase db = mb.getReadableDatabase();
 		int rev = mb.getLatestRevision(db);
 		db.close();
 		return false;
 	}
 
 	private void createFolderItems(Folder curFolder) {
 		if (curFolder != null) {
 			folderItems = curFolder.getFolderItems();
 		}
 
 	}
 
 	private void createAdapter() {
 		folderAdapter = new SimpleAdapter(this, folderItems, folderResource, FOLDER_FROM, FOLDER_TO);
 		folderAdapter.setViewBinder(new FolderViewBinder());
 	}
 
 	private void populateList() {
 		ListView listFolders = (ListView) findViewById(R.id.listView_items);
 		listFolders.setAdapter(folderAdapter);
 		listFolders.setOnItemClickListener(new ListItemListener());
 	}
 
 	public class ListItemListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			@SuppressWarnings("unchecked")
 			Map<String, String> fi = (Map<String, String>) parent.getItemAtPosition(position);
 			if (fi.get(FolderItem.TYPE).equals(FolderItem.TYPE_BACK)) {
 				onBackPressed();
 			} else if (fi.get(FolderItem.TYPE).equals(FolderItem.TYPE_FOLDER)) {
 				Mediabase mb = new Mediabase(FolderActivity.this);
 				SQLiteDatabase db = mb.getReadableDatabase();
 				try {
 					Folder newFolder = Folder.getByFolderKey(db, fi.get(FolderItem.FOLDERKEY));
 					mediafire.setCurrentFolder(newFolder);
 					requestFolder();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} finally {
 					db.close();
 				}
 			} else if (fi.get(FolderItem.TYPE).equals(FolderItem.TYPE_FILE)) {
 				String quickkey = fi.get(FolderItem.QUICKKEY);
 				Log.d(TAG, "Clicked on " + quickkey);
 			}
 		}
 	}
 
 	public class FolderViewBinder implements SimpleAdapter.ViewBinder {
 
 		public boolean setViewValue(View view, Object data, String textRepresentation) {
 			if (view.getId() == R.id.folder_item_type) {
 				ImageView im = (ImageView) view;
 				Resources res = getResources();
 				Drawable d = null;
 				if (textRepresentation.equals(FolderItem.TYPE_FOLDER) || textRepresentation.equals(FolderItem.TYPE_BACK)) {
 					d = res.getDrawable(R.drawable.icon_folder);
 					im.setImageDrawable(d);
 				} else if (textRepresentation.equals(FolderItem.TYPE_FILE)) {
 					d = res.getDrawable(R.drawable.icon_file);
 					im.setImageDrawable(d);
 				} else {
 					String img = textRepresentation;
 					img = img.replaceAll(" ", "").toLowerCase();
 					FileIcon icon = new FileIcon(img, FolderActivity.this);
 					int r = icon.getIcon();
 					if (r == 0) {
 						d = res.getDrawable(R.drawable.icon_file);
 						im.setImageDrawable(d);
 					} else {
 						((ImageView) view).setImageResource(r);
 					}
 				}
 
 				return true;
 			} else if (view.getId() == R.id.folder_item_privacy) {
 				String img = "privacy_" + textRepresentation;
 				img = img.replaceAll(" ", "").toLowerCase();
 				FileIcon icon = new FileIcon(img, FolderActivity.this);
 				int r = icon.getIcon();
 				if (r == 0) {
 					((ImageView) view).setVisibility(View.INVISIBLE);
 				} else {
 					((ImageView) view).setImageResource(r);
 				}
 				return true;
 			} else if (view.getId() == R.id.folder_item_downicon) {
 				if (textRepresentation.equals(ItemConstants.NO)) {
 					view.setVisibility(View.INVISIBLE);
 				} else if (textRepresentation.equals(ItemConstants.YES)) {
 					view.setVisibility(View.VISIBLE);
 				}
 				return true;
 			}
 			return false;
 		}
 
 	}
 
 	public void right2left(View v) {
 
 	}
 
 	@Override
 	public void bottom2top(View v) {
 
 	}
 
 	@Override
 	public void left2right(View v) {
 		onBackPressed();
 
 	}
 
 	@Override
 	public void top2bottom(View v) {
 
 	}
 
 	class MyOnlineFilesTask extends AsyncTask<Folder, Void, Folder> implements ApiUrls, Elements {
 		public static final String TAG = "MyOnlineFiles";
 		public String content_filter;
 		public static final String ALL = "all";
 		public static final String FILES = "files";
 		public static final String FOLDERS = "folders";
 
 		public String content_format;
 		public static final String TREE = "tree";
 		public static final String LIST = "list";
 
 		public static final String YES = "yes";
 		public static final String NO = "no";
 
 		public int start;
 		public int limit;
 
 		protected FolderActivity activity;
 		protected Connection connection;
 		private SQLiteDatabase db;
 		private ProgressDialog dialog;
 		private Mediafire mediafire;
		private String currentFolder;
 
 		public MyOnlineFilesTask(FolderActivity activity, Connection connection) {
 			this.connection = connection;
 			this.activity = activity;
 			this.dialog = new ProgressDialog(activity);
 			mediafire = (Mediafire) activity.getApplication();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
			this.dialog.setMessage("Fetching " + mediafire.getCurrentFolder().name + "...");
			if (!this.dialog.isShowing()) {
 				this.dialog.show();
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Folder result) {
 			super.onPostExecute(result);
 
 			try {
 				Mediabase mb = new Mediabase(activity);
 				SQLiteDatabase db = mb.getWritableDatabase();
 				result.updateDb(db, mediafire.isFullImport());
 				getOfflineFolderItems();
 				db.close();
 				mediafire.setForceOnline(false);
 				listView.onRefreshComplete();
 				createList();
 				mediafire.setFullImport(false);
 				Toast.makeText(FolderActivity.this, "Folder updated", Toast.LENGTH_SHORT).show();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				if (this.dialog.isShowing()) {
 					this.dialog.dismiss();
 				}
 			}
 
 		}
 
 		@Override
 		protected Folder doInBackground(Folder... params) {
 			Folder folder = params[0];
 			return getFolderContent(folder);
 		}
 
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			this.dialog.setMessage("Fetching " + this.currentFolder + "...");
		}

 		private Folder getFolderContent(Folder folder) {
 			String[] types = new String[] { "folders", "files" };
			currentFolder = folder.name;
			publishProgress();
 			folder.subFolders = new ArrayList<Folder>();
 			folder.files = new ArrayList<File>();
 			folder.inserted = System.currentTimeMillis() / 1000;
 			for (int i = 0; i < types.length; i++) {
 				InputStream in = null;
 				try {
 					in = connection.connect(DOMAIN + "/" + MYFILES_URL, new String[] { "session_token=" + mediafire.getSessionToken(),
 							"folder_key=" + folder.folderKey, "response_format=json", "content_type=" + types[i] });
 
 					if (in == null) {
 						Log.e(TAG, "Could not read from " + GET_LOGIN_TOKEN_URL);
 					}
 
 					StringBuilder builder = new StringBuilder();
 					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 					String line;
 					while ((line = reader.readLine()) != null) {
 						builder.append(line);
 					}
 					String response = builder.toString();
 					MyFilesJSONParser p = new MyFilesJSONParser(response);
 					Folder current = p.folder;
 					for (int j = 0; j < current.subFolders.size(); j++) {
 						Folder f = current.subFolders.get(j);
 						f.parent = folder.folderKey;
 						folder.subFolders.add(f);
 						if (mediafire.isFullImport()) {
 							getFolderContent(f);
 						}
 					}
 					for (int k = 0; k < current.files.size(); k++) {
 						File f = current.files.get(k);
 						f.parent = folder.folderKey;
 						folder.files.add(f);
 					}
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} finally {
 					try {
 						in.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			return folder;
 		}
 	}
 
 }
