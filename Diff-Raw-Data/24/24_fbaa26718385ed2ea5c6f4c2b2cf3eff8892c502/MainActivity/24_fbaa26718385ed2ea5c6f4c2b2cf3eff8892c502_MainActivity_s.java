 package cm.aptoide.pt2;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.zip.Adler32;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xmlpull.v1.XmlSerializer;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.util.Xml;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RatingBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import cm.aptoide.pt2.Server.State;
 import cm.aptoide.pt2.adapters.InstalledAdapter;
 import cm.aptoide.pt2.adapters.ViewPagerAdapter;
 import cm.aptoide.pt2.contentloaders.ImageLoader;
 import cm.aptoide.pt2.contentloaders.ImageLoader2;
 import cm.aptoide.pt2.contentloaders.SimpleCursorLoader;
 import cm.aptoide.pt2.preferences.ManagerPreferences;
 import cm.aptoide.pt2.services.MainService;
 import cm.aptoide.pt2.services.MainService.LocalBinder;
 import cm.aptoide.pt2.util.Algorithms;
 import cm.aptoide.pt2.util.Md5Handler;
 import cm.aptoide.pt2.util.NetworkUtils;
 import cm.aptoide.pt2.util.RepoUtils;
 import cm.aptoide.pt2.views.ViewApk;
 import cm.aptoide.pt2.webservices.login.Login;
 
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
 
 	private final static int AVAILABLE_LOADER = 0;
 	private final static int INSTALLED_LOADER = 1;
 	private final static int UPDATES_LOADER   = 2;
 	
 	private final static int LATEST_COMMENTS  = -2;
 	private final static int LATEST_LIKES     = -1;
 	
 	private final String SDCARD = Environment.getExternalStorageDirectory().getPath();
 	private String LOCAL_PATH = SDCARD+"/.aptoide";
 
 	private final Dialog.OnClickListener addRepoListener = new Dialog.OnClickListener() {
 
 		@Override
 		public void onClick(DialogInterface arg0, int arg1) {
 			String url = ((EditText) alertDialog.findViewById(R.id.edit_uri))
 					.getText().toString();
 			dialogAddStore(url, null, null);
 		}
 
 	};
 	int a = 0;
 	private void loadUIEditorsApps() {
 		
 		final ImageLoader2 imageLoader = ImageLoader2.getInstance(mContext);
 		final int[] res_ids = {R.id.central,R.id.topleft,R.id.topright,R.id.bottomleft,R.id.bottomright};
 		final ArrayList<HashMap<String, String>> image_urls  = db.getFeaturedGraphics();
 		HashMap<String,String> image_url_highlight = db.getHighLightFeature();
 		if(image_url_highlight!=null){
 			a=1;
 			ImageView v = (ImageView) featuredView.findViewById(res_ids[0]);
 			imageLoader.DisplayImage(-1, image_url_highlight.get("url"), v, mContext);
 			v.setTag(image_url_highlight.get("id"));
 			v.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					Intent i = new Intent(MainActivity.this,ApkInfo.class);
 					i.putExtra("_id", Long.parseLong((String)arg0.getTag()));
 					i.putExtra("top", false);
 					i.putExtra("category", Category.ITEMBASED.ordinal());
 					startActivity(i);
 				}
 			});
 //			v.setOnClickListener(featuredListener);
 		}
 		
 		Collections.shuffle(image_urls);
 		runOnUiThread(new Runnable() {
 			
 			public void run() {
 				try{
 					for(int i = a; i != res_ids.length;i++){
 						ImageView v = (ImageView) featuredView.findViewById(res_ids[i]);
 						imageLoader.DisplayImage(-1, image_urls.get(i).get("url"), v, mContext);
 						v.setTag(image_urls.get(i).get("id"));
 						v.setOnClickListener(new OnClickListener() {
 							
 							@Override
 							public void onClick(View arg0) {
 								Intent i = new Intent(MainActivity.this,ApkInfo.class);
 								i.putExtra("_id", Long.parseLong((String)arg0.getTag()));
 								i.putExtra("top", false);
 								i.putExtra("category", Category.ITEMBASED.ordinal());
 								startActivity(i);
 							}
 						});
 //						v.setOnClickListener(featuredListener);
 					}
 					
 					
 				}catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	private void loadFeatured() {
 		new Thread(new Runnable() {
 			
 			public void run() {
 				loadUIEditorsApps();
 				try {
 					SAXParserFactory spf = SAXParserFactory.newInstance();
 					SAXParser sp = spf.newSAXParser();
 					ViewApk parent_apk = new ViewApk();
 					parent_apk.setApkid("editorschoice");
 					BufferedInputStream bis = new BufferedInputStream(NetworkUtils.getInputStream(new URL("http://www.aptoide.com/apks/editors.xml"),null,null,mContext),8*1024);
 					File f = File.createTempFile("abc", "abc");
 					OutputStream out=new FileOutputStream(f);
 					  byte buf[]=new byte[1024];
 					  int len;
 					  while((len=bis.read(buf))>0)
 					  out.write(buf,0,len);
 					  out.close();
 					  bis.close();
 					  String hash = Md5Handler.md5Calc(f);
 					  if(!hash.equals(db.getItemBasedApksHash("editorschoice"))){
 						  sp.parse(f,new ItemBasedApkHandler(db, parent_apk));
 						  db.insertItemBasedApkHash(hash, "editorschoice"); 
 						  loadUIEditorsApps();
 					  }
 					  f.delete();
 					  
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 		
 		new Thread(new Runnable() {
 			
 			public void run() {
 				
 					loadUItopapps();
 					try {
 						SAXParserFactory spf = SAXParserFactory.newInstance();
 						SAXParser sp = spf.newSAXParser();
 						Server server = new Server();
 						server.id=0;
 						sp.parse(new BufferedInputStream(NetworkUtils.getInputStream(new URL(
 								"http://apps.store.aptoide.com/top.xml"),null,null,mContext),8*1024),
 								new TopRepoParserHandler(db, server, Category.TOP, true));
 						loadUItopapps();
 					} catch (ParserConfigurationException e) {
 						e.printStackTrace();
 					} catch (SAXException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 			}
 
 			
 		}).start();
 		
 	}
 	
 	private void loadUItopapps() {
 		((ToggleButton) featuredView.findViewById(R.id.toggleButton1)).setOnCheckedChangeListener(null);
 		Cursor c = db.getTopApps(1, 0, joinStores_boolean);
 		
 		values = new ArrayList<HashMap<String,String>>();
 		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
 			HashMap<String, String> item = new HashMap<String, String>();
 			item.put("name", c.getString(1));
 			System.out.println(c.getString(1));
 			item.put("icon", db.getTopIconsPath(c.getLong(3))+c.getString(4));
 			item.put("rating", c.getString(5));
 			item.put("id", c.getString(0));
 			if(values.size()==26){
 				break;
 			}
 			values.add(item);
 			}
 		c.close();
 			
 		
 			
 		 
 		runOnUiThread(new Runnable() {
 			ImageLoader2 imageLoader = ImageLoader2.getInstance(mContext);
 			
 			
 			public void run() {
 				
 				LinearLayout ll = (LinearLayout) featuredView.findViewById(R.id.container); 
 				ll.removeAllViews();
 		        LinearLayout llAlso = new LinearLayout(MainActivity.this);
 		        llAlso.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 		        llAlso.setOrientation(LinearLayout.HORIZONTAL);
 		        for (int i = 0; i!=values.size(); i++) {
 		            RelativeLayout txtSamItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.griditem, null);
 		           	((TextView) txtSamItem.findViewById(R.id.name)).setText(values.get(i).get("name"));
 		           	imageLoader.DisplayImage(-1, values.get(i).get("icon"), (ImageView)txtSamItem.findViewById(R.id.icon), mContext);
 		           	float stars = 0f;
 		           	try{
 		           		stars = Float.parseFloat(values.get(i).get("rating"));
 		           	}catch (Exception e) {
 		           		stars = 0f;
 					}
 		           	((RatingBar) txtSamItem.findViewById(R.id.rating)).setRating(stars);
 		            txtSamItem.setPadding(10, 0, 0, 0);
 		            txtSamItem.setTag(values.get(i).get("id"));
 		            txtSamItem.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 100, 1));
 //		            txtSamItem.setOnClickListener(featuredListener);
 		            txtSamItem.setOnClickListener(new OnClickListener() {
 						
 						@Override
 						public void onClick(View arg0) {
 							Intent i = new Intent(MainActivity.this,ApkInfo.class);
 							long id = Long.parseLong((String)arg0.getTag());
 							i.putExtra("_id", id);
 							i.putExtra("top", true);
 							i.putExtra("category", Category.TOP.ordinal());
 							startActivity(i);
 						}
 					});
 
 		            txtSamItem.measure(0, 0);
 		            
 		            if (i%2==0) {
 		                ll.addView(llAlso);
 
 		                llAlso = new LinearLayout(MainActivity.this);
 		                llAlso.setLayoutParams(new LayoutParams(
 		                        LayoutParams.FILL_PARENT,
 		                        100));
 		                llAlso.setOrientation(LinearLayout.HORIZONTAL);
 		                llAlso.addView(txtSamItem);
 		            } else {
 		                llAlso.addView(txtSamItem);
 		            }
 		        }
 
 		        ll.addView(llAlso);
 		        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mContext);
 //		        System.out.println(sPref.getString("app_rating", "All").equals(
 //						"Mature"));
 		        ((ToggleButton) featuredView.findViewById(R.id.toggleButton1))
 				.setChecked(!sPref.getBoolean("matureChkBox", false));
 		        ((ToggleButton) featuredView.findViewById(R.id.toggleButton1))
 				.setOnCheckedChangeListener(adultCheckedListener);
 			}
 		});
 	}
 	
 	ArrayList<HashMap<String, String>> values;
 
 	private void dialogAddStore(final String url, final String username,
 			final String password) {
 		final ProgressDialog pd = new ProgressDialog(mContext);
 		pd.show();
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					addStore(url, username, password);
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					runOnUiThread(new Runnable() {
 
 						@Override
 						public void run() {
 							pd.dismiss();
 							refreshAvailableList(true);
 						}
 					});
 
 				}
 
 			}
 		}).start();
 	}
 
 	private View addStoreButton;
 	
 	
 
 	private final OnClickListener addStoreListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			showAddStoreDialog();
 		}
 
 	};
 
 	private AlertDialog alertDialog;
 
 	private View alertDialogView;
 	private HashMap<String, Long> serversToParse = new HashMap<String, Long>();
 	private AvailableListAdapter availableAdapter;
 	private ListView availableListView;
 	private Loader<Cursor> availableLoader;
 	private View availableView;
 	private long category_id;
 	private long category2_id;
 	private final ServiceConnection conn = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			MainActivity.this.service = ((LocalBinder) service).getService();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 
 		}
 	};
 
 	private Database db;
 
 	private ListDepth depth = ListDepth.STORES;
 
 	private View featuredView;
 
 	private InstalledAdapter installedAdapter;
 
 	private Loader<Cursor> installedLoader;
 	private ListView installedView;
 
 	private CheckBox joinStores;
 	private boolean joinStores_boolean = false;
 	private Context mContext;
 
 	private TextView pb;
 	private boolean refreshClick = true;
 	private final Dialog.OnClickListener searchStoresListener = new Dialog.OnClickListener() {
 
 		@Override
 		public void onClick(DialogInterface arg0, int arg1) {
 			Uri uri = Uri.parse("http://m.aptoide.com/more/toprepos");
 			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 			startActivity(intent);
 		}
 
 	};
 
 	private MainService service;
 	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (depth.equals(ListDepth.STORES)) {
 				availableLoader.forceLoad();
 				System.out.println("Status broadcast received");
 			}
 		}
 	};
 	private long store_id;
 
 	private CursorAdapter updatesAdapter;
 
 	private Loader<Cursor> updatesLoader;
 
 	private final BroadcastReceiver updatesReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			installedLoader.forceLoad();
 			updatesLoader.forceLoad();
 			if (!depth.equals(ListDepth.STORES)) {
 				Long server_id = intent.getExtras().getLong("server");
 				if (refreshClick && server_id == store_id) {
 					refreshClick = false;
 					availableView.findViewById(R.id.refresh_view_layout)
 							.setVisibility(View.VISIBLE);
 					availableView
 							.findViewById(R.id.refresh_view_layout)
 							.findViewById(R.id.refresh_view)
 							.startAnimation(
 									AnimationUtils.loadAnimation(mContext,
 											android.R.anim.fade_in));
 				}
 			}
 		}
 	};
 
 	private ListView updatesView;
 
 	public class AddStoreCredentialsListener implements
 			DialogInterface.OnClickListener {
 		private String url;
 		private View dialog;
 
 		public AddStoreCredentialsListener(String string,
 				View credentialsDialogView) {
 			this.url = string;
 			this.dialog = credentialsDialogView;
 		}
 
 		@Override
 		public void onClick(DialogInterface arg0, int which) {
 			dialogAddStore(url, ((EditText) dialog.findViewById(R.id.username))
 					.getText().toString(),
 					((EditText) dialog.findViewById(R.id.password)).getText()
 							.toString());
 		}
 
 	}
 
 	public void getAllRepoStatus() {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				String repos = "";
 				String hashes = "";
 				Cursor cursor = db.getStores(false);
 				int i = 0;
 				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
 						.moveToNext()) {
 					String repo;
 					if (i > 0) {
 						repos = repos + ",";
 						hashes = hashes + ",";
 					}
 					repo = cursor.getString(1);
 					repo = RepoUtils.split(repo);
 					repos = repos + repo;
 					hashes = hashes + cursor.getString(2);
 					i++;
 					serversToParse.put(repo, cursor.getLong(0));
 
 				}
 				cursor.close();
 
 				if (!serversToParse.isEmpty()) {
 
 					String url = "https://webservices.aptoide.com/webservices/listRepositoryChange/"
 							+ repos + "/" + hashes + "/json";
 					System.out.println(url);
 					try {
 						HttpURLConnection connection = (HttpURLConnection) new URL(
 								url).openConnection();
 						connection.connect();
 						int rc = connection.getResponseCode();
 						if (rc == 200) {
 
 							JSONObject json = NetworkUtils.getJsonObject(new URL(url), mContext);
 
 							JSONArray array = json.getJSONArray("listing");
 
 							for (int o = 0; o != array.length(); o++) {
 								boolean parse = Boolean.parseBoolean(array.getJSONObject(o).getString("hasupdates"));
 								long id = serversToParse.get(array.getJSONObject(o).getString("repo"));
 								Server server = db.getServer(id,false);
 								if (parse) {
 									service.parseServer(db, server);
 								}
 								service.parseTop(db, server);
 								service.parseLatest(db, server);
 							}
 
 						}
 						connection.disconnect();
 
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 
 	}
 
 	protected void redrawAll() {
 		installedLoader.forceLoad();
 		availableLoader.forceLoad();
 		updatesLoader.forceLoad();
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				loadUItopapps();
 			}
 		}).start();
 	}
 
 	protected void addStore(String uri_str, String username, String password) {
 
 		if (uri_str.contains("http//")) {
 			uri_str = uri_str.replaceFirst("http//", "http://");
 		}
 
 		if (uri_str.length() != 0
 				&& uri_str.charAt(uri_str.length() - 1) != '/') {
 			uri_str = uri_str + '/';
 			Log.d("Aptoide-ManageRepo", "repo uri: " + uri_str);
 		}
 		if (!uri_str.startsWith("http://")) {
 			uri_str = "http://" + uri_str;
 			Log.d("Aptoide-ManageRepo", "repo uri: " + uri_str);
 		}
 		if (username != null && username.contains("@")) {
 			try {
 				password = Algorithms.computeSHA1sum(password);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		final int response = NetworkUtils.checkServerConnection(uri_str, username, password);
 		final String uri = uri_str;
 		switch (response) {
 		case 0:
 			service.addStore(db, uri, username, password);
 			break;
 		case 401:
 			runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					showAddStoreCredentialsDialog(uri);
 				}
 			});
 
 			break;
 		case -1:
 			runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					Toast.makeText(mContext, "An error ocurred. Please check your internet connection.", Toast.LENGTH_LONG).show();
 					showAddStoreDialog();
 				}
 			});
 			break;
 		default:
 			runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					Toast.makeText(mContext, "An error ocurred. Code: "+response, Toast.LENGTH_LONG).show();
 					showAddStoreDialog();
 				}
 			});
 			break;
 		}
 
 	}
 	
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 		menu.add(0,1,0,"Login");
 		menu.add(0,2,0,"Settings");
 		menu.add(0,3,0,"Scheduled");
 		menu.add(0,4,0,"Display Options");
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		switch (item.getItemId()) {
 		case 1:
 			
 			Intent loginIntent = new Intent(this,Login.class);
 			startActivity(loginIntent);
 			break;
 		case 2:
 			Intent settingsIntent = new Intent(this,Settings.class);
 			startActivityForResult(settingsIntent,0);
 			break;
 		case 3:
 			Intent scheduledIntent = new Intent(this,ScheduledDownloads.class);
 			startActivity(scheduledIntent);
 			break;
 		case 4:
 			displayOptionsDialog();
 			break;
 		default:
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
 		super.onActivityResult(arg0, arg1, arg2);
 		
 		installedLoader.forceLoad();
 		updatesLoader.forceLoad();
 		availableLoader.forceLoad();
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				loadUItopapps();
 			}
 		}).start();
 		
 		
 		
 	}
 
 	private void displayOptionsDialog() {
 		
 		final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
 		final Editor editor = sPref.edit();
 		
 		View view = LayoutInflater.from(mContext).inflate(R.layout.orderpopup, null);
 		Builder builder = new AlertDialog.Builder(mContext).setView(view);
 		AlertDialog dialog = builder.create();
 
 		final RadioButton ord_rct = (RadioButton) view.findViewById(R.id.org_rct);
 		final RadioButton ord_abc = (RadioButton) view.findViewById(R.id.org_abc);
 		final RadioButton ord_rat = (RadioButton) view.findViewById(R.id.org_rat);
 		final RadioButton ord_dwn = (RadioButton) view.findViewById(R.id.org_dwn);
 		final RadioButton btn1 = (RadioButton) view.findViewById(R.id.shw_ct);
 		final RadioButton btn2 = (RadioButton) view.findViewById(R.id.shw_all);
 		
 		final ToggleButton adult  = (ToggleButton) view.findViewById(R.id.adultcontent_toggle);
 		
 		dialog.setButton(Dialog.BUTTON_NEUTRAL, "Ok", new Dialog.OnClickListener() {
 			boolean pop_change = false;
 			public void onClick(DialogInterface dialog, int which) {
 				if(ord_rct.isChecked()){
 					pop_change = true;
 					order=Order.DATE;
 				}else if(ord_abc.isChecked()){
 					pop_change = true;
 					order=Order.NAME;
 				}else if(ord_rat.isChecked()){
 					pop_change = true;
 					order=Order.RATING;
 				}else if(ord_dwn.isChecked()){
 					pop_change = true;
 					order=Order.DOWNLOADS;
 				}
 				
 				if(btn1.isChecked()){
 					pop_change = true;
 					editor.putBoolean("orderByCategory", true);
 				}else if(btn2.isChecked()){
 					pop_change = true;
 					editor.putBoolean("orderByCategory", false);
 				}
 				if(adult.isChecked()){
 					pop_change = true;
 					editor.putBoolean("matureChkBox", false);
 				}else{
 					editor.putBoolean("matureChkBox", true);
 				}
 				if(pop_change){
 					editor.putInt("order_list", order.ordinal());
 					editor.commit();
 					redrawAll();
 					
 				}
 			}
 		});
 		
 		
 		
 		if(sPref.getBoolean("orderByCategory", false)){
 			btn1.setChecked(true);
 		}else{
 			btn2.setChecked(true);
 		}
 		adult.setChecked(!sPref.getBoolean("matureChkBox", false));
 //		adult.setOnCheckedChangeListener(adultCheckedListener);
 				switch (order) {
 				case DATE:
 					ord_rct.setChecked(true);
 					break;
 				case DOWNLOADS:
 					ord_dwn.setChecked(true);
 					break;
 				case NAME:
 					ord_abc.setChecked(true);
 					break;
 				case RATING:
 					ord_rat.setChecked(true);
 					break;
 
 				default:
 					break;
 				}
 
 		dialog.show();
 		
 	}
 
 	private void getInstalled() {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				List<PackageInfo> system_installed_list = getPackageManager()
 						.getInstalledPackages(0);
 				List<String> database_installed_list = db.getStartupInstalled();
 				for (PackageInfo pkg : system_installed_list) {
 					if (!database_installed_list.contains(pkg.packageName)) {
 						try {
 							ViewApk apk = new ViewApk();
 							apk.setApkid(pkg.packageName);
 							apk.setVercode(pkg.versionCode);
 							apk.setVername(pkg.versionName);
 							apk.setName((String) pkg.applicationInfo
 									.loadLabel(getPackageManager()));
 							db.insertInstalled(apk);
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 
 						}
 					}
 				}
 
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						installedLoader = getSupportLoaderManager().initLoader(
 								INSTALLED_LOADER, null, MainActivity.this);
 						installedView.setAdapter(installedAdapter);
 						getUpdates();
 					}
 				});
 			}
 		}).start();
 	}
 
 	private void getUpdates() {
 		updatesLoader = getSupportLoaderManager().initLoader(UPDATES_LOADER,null, MainActivity.this);
 		updatesView.setAdapter(updatesAdapter);
 	}
 
 	@Override
 	public boolean onContextItemSelected(final MenuItem item) {
 		final ProgressDialog pd;
 		switch (item.getItemId()) {
 		case 0:
 			pd = new ProgressDialog(mContext);
 			pd.show();
 			pd.setCancelable(false);
 			new Thread(new Runnable() {
 
 				private boolean result = false;
 
 				@Override
 				public void run() {
 					try {
 						result = service.deleteStore(db, ((AdapterContextMenuInfo) item.getMenuInfo()).id);
 					} catch (Exception e) {
 						e.printStackTrace();
 					} finally {
 						runOnUiThread(new Runnable() {
 
 							@Override
 							public void run() {
 								pd.dismiss();
 								if (result) {
 									refreshAvailableList(false);
 									installedLoader.forceLoad();
 									updatesLoader.forceLoad();
 								} else {
 									Toast.makeText(mContext,
 											R.string.error_delete_store,
 											Toast.LENGTH_LONG).show();
 								}
 
 							}
 						});
 					}
 				}
 			}).start();
 			break;
 		case 1:
 			pd = new ProgressDialog(mContext);
 			pd.show();
 			pd.setCancelable(false);
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						service.parseServer(db, db.getServer(((AdapterContextMenuInfo) item.getMenuInfo()).id,false));
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} finally {
 						runOnUiThread(new Runnable() {
 
 							@Override
 							public void run() {
 								pd.dismiss();
 								refreshAvailableList(false);
 							}
 						});
 
 					}
 				}
 			}).start();
 
 			break;
 		}
 
 		return super.onContextItemSelected(item);
 	}
 
 	LinearLayout breadcrumbs;
 	private BroadcastReceiver loginReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 						
 						try {
 							SAXParserFactory factory = SAXParserFactory.newInstance();
 							SAXParser parser = factory.newSAXParser();
 							String token = Login.getToken(mContext);
 							parser.parse(NetworkUtils.getInputStream(new URL("https://webservices.aptoide.com/webservices/listUserBasedApks/"+token+"/10/xml"), null, null, mContext),new DefaultHandler(){
 								
 								ViewApk apk = new ViewApk();
 								StringBuilder sb = new StringBuilder();
 								public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
 									sb.setLength(0);
 									if(localName.equals("package")){
 										apk.clear();
 									}
 								};
 								
 								public void characters(char[] ch, int start, int length) throws SAXException {
 									sb.append(ch,start,length);
 								};
 								
 								public void endElement(String uri, String localName, String qName) throws SAXException {
 									if(localName.equals("apkid")){
 										apk.setApkid(sb.toString());
 									}else if(localName.equals("vercode")){
 										apk.setVercode(Integer.parseInt(sb.toString()));
 									} else if(localName.equals("package")){
 										db.insertUserBasedApk(apk);
 									}
 									
 								};
 							});
 						} catch (MalformedURLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (SAXException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (ParserConfigurationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						
 						
 				}
 			}).start();
 		}
 	};
 	private BroadcastReceiver redrawInstalledReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			installedLoader.forceLoad();
 			updatesLoader.forceLoad();
 		}
 	};
 	protected Order order;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		mContext = this;
 		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mContext);
 		db = Database.getInstance(mContext);
 		Intent i = new Intent(mContext, MainService.class);
 		startService(i);
 		bindService(i, conn, Context.BIND_AUTO_CREATE);
 		
 		order = Order.values()[PreferenceManager.getDefaultSharedPreferences(mContext).getInt("order_list", 0)];
 		
 		registerReceiver(updatesReceiver, new IntentFilter("update"));
 		registerReceiver(statusReceiver, new IntentFilter("status"));
 		registerReceiver(loginReceiver , new IntentFilter("login"));
 		registerReceiver(redrawInstalledReceiver , new IntentFilter("pt.caixamagica.aptoide.REDRAW"));
 		setContentView(R.layout.activity_aptoide);
 		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
 		ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
 
 		featuredView = LayoutInflater.from(mContext).inflate(R.layout.featured, null);
 
 		availableView = LayoutInflater.from(mContext).inflate(R.layout.available_page, null);
 		breadcrumbs = (LinearLayout) availableView.findViewById(R.id.breadcrumb_container);
 		installedView = new ListView(mContext);
 		updatesView = new ListView(mContext);
 
 		availableListView = (ListView) availableView.findViewById(R.id.available_list);
 		availableView.findViewById(R.id.refresh_view_layout)
 				.findViewById(R.id.refresh_view)
 				.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						refreshClick = true;
 						availableView.findViewById(R.id.refresh_view_layout)
 								.setVisibility(View.GONE);
 						refreshAvailableList(false);
 
 					}
 				});
 
 		joinStores = (CheckBox) availableView.findViewById(R.id.join_stores);
 		joinStores.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				joinStores_boolean = isChecked;
 //				if (isChecked) {
 //					addBreadCrumb("All Stores", depth);
 //				} else {
 //					breadcrumbs.removeAllViews();
 //				}
 				refreshAvailableList(true);
 			}
 		});
 
 		
 		availableAdapter = new AvailableListAdapter(mContext, null,
 				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 		installedAdapter = new InstalledAdapter(mContext, null,
 				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, db);
 		updatesAdapter = new InstalledAdapter(mContext, null,
 				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, db);
 
 		pb = (TextView) availableView.findViewById(R.id.loading_pb);
 		addStoreButton = availableView.findViewById(R.id.add_store);
 		addStoreButton.setOnClickListener(addStoreListener);
 
 		availableListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Intent i ;
 				switch (depth) {
 				case STORES:
 					depth = ListDepth.CATEGORY1;
 					store_id = id;
 					break;
 				case CATEGORY1:
 					String category = ((Cursor) parent
 							.getItemAtPosition(position)).getString(1);
 					if (category.equals("Top Apps") || category.equals("Latest Apps")) {
 						depth = ListDepth.TOPAPPS;
 						System.out.println("TopApps");
 					} else if(id==LATEST_LIKES){
 						depth = ListDepth.LATEST_LIKES;
 					} else if(id==LATEST_COMMENTS){
 						depth = ListDepth.LATEST_COMMENTS;
 					} else if(id==-3){
 						depth = ListDepth.RECOMMENDED;
 					} else if(id==-4){
 						depth = ListDepth.ALLAPPLICATIONS;
 					}else {
 						depth = ListDepth.CATEGORY2;
 					}
 					category_id = id;
 					break;
 				
 				case CATEGORY2:
 					depth = ListDepth.APPLICATIONS;
 					category2_id = id;
 					break;
 				case TOPAPPS:
 					i = new Intent(MainActivity.this, ApkInfo.class);
 					i.putExtra("_id", id);
 					i.putExtra("top", true);
 					i.putExtra("category", Category.TOP.ordinal());
 					startActivity(i);
 					return;
 				case APPLICATIONS:
 				case ALLAPPLICATIONS:
 				case RECOMMENDED:
 					i = new Intent(MainActivity.this, ApkInfo.class);
 					i.putExtra("_id", id);
 					i.putExtra("top", false);
 					i.putExtra("category", Category.INFOXML.ordinal());
 					startActivity(i);
 					return;
 				case LATEST_COMMENTS:
 				case LATEST_LIKES:
 					
 					String apkid = ((Cursor) parent.getItemAtPosition(position)).getString(1);
 					
 					latestClick(apkid);
 					return;
 				default:
 					return;
 				}
 				addBreadCrumb(((Cursor) parent.getItemAtPosition(position)).getString(1), depth);
 				refreshAvailableList(true);
 			}
 		});
 		installedView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long id) {
 				Intent i = new Intent(MainActivity.this, ApkInfo.class);
 				i.putExtra("_id", id);
 				i.putExtra("top", false);
 				startActivity(i);
 			}
 		});
 		findViewById(R.id.btsearch).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				onSearchRequested();
 			}
 		});
 		updatesView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long id) {
 				Intent i = new Intent(MainActivity.this, ApkInfo.class);
 				i.putExtra("_id", id);
 				i.putExtra("top", false);
 				startActivity(i);
 			}
 		});
 //		LoaderManager.enableDebugLogging(true);
 		availableLoader = getSupportLoaderManager().initLoader(AVAILABLE_LOADER, null, this);
 		
 		ArrayList<View> views = new ArrayList<View>();
 		views.add(featuredView);
 		views.add(availableView);
 		views.add(installedView);
 		views.add(updatesView);
 		editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
 		pager.setAdapter(new ViewPagerAdapter(mContext, views));
 		indicator.setViewPager(pager);
 		refreshAvailableList(true);
 		getInstalled();
 		getAllRepoStatus();
 		loadFeatured();
 		addBreadCrumb("Stores", ListDepth.STORES);
 		
 		if(sPref.getBoolean("firstrun",true)){
			Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
			shortcutIntent.setClassName("cm.aptoide.pt", "cm.aptoide.pt.Start");
			final Intent intent = new Intent();
			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
			Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.aptoideicon);

			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
			intent.putExtra("duplicate", false);
			intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			sendBroadcast(intent);
 			
 			
 			if(new File(LOCAL_PATH+"/servers.xml").exists()){
 				try{
 
 					SAXParserFactory spf = SAXParserFactory.newInstance();
 					SAXParser sp = spf.newSAXParser();
 
 					MyappHandler handler = new MyappHandler();
 
 					sp.parse(new File(LOCAL_PATH+"/servers.xml"),handler);
 					ArrayList<String> server = handler.getServers();
 					getIntent().putExtra("newrepo", server);
 
 				}catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 			editor.putBoolean("firstrun",false);
 			editor.putBoolean("orderByCategory", true);
 			editor.commit();
 
 		}
 		
 		if(getIntent().hasExtra("newrepo")){
 			ArrayList<String> repos = (ArrayList<String>) getIntent().getSerializableExtra("newrepo");
 			for(final String uri2 : repos){
 			final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
 			alertDialog.setMessage(getString(R.string.newrepo_alrt)+uri2+" ?");
 			alertDialog.setButton(Dialog.BUTTON_POSITIVE,getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialogAddStore(uri2, null, null);
 				}
 				
 			});
 			alertDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no),new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 				
 			});
 			alertDialog.show();
 			
 			
 		}
 		}
 		
 		
 		
 
 	}
 
 	protected void latestClick(final String apkid) {
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				final long id = db.getApkId(apkid,store_id);
 				System.out.println("Getting Latest id"+id);
 				if(id!=-1){
 					runOnUiThread(new Runnable() {
 						
 						@Override
 						public void run() {
 							Intent i = new Intent(MainActivity.this, ApkInfo.class);
 							i.putExtra("_id", id);
 							i.putExtra("top", false);
 							i.putExtra("category", Category.INFOXML.ordinal());
 							startActivity(i);
 						}
 					});
 				}else{
 					runOnUiThread(new Runnable() {
 						
 						@Override
 						public void run() {
 							Toast.makeText(mContext, R.string.error_latest_apk, Toast.LENGTH_LONG).show();
 						}
 					});
 				}
 				
 				
 			}
 		}).start();
 		
 	}
 
 	private class BreadCrumb {
 		ListDepth depth;
 		int i;
 
 		public BreadCrumb(ListDepth depth, int i) {
 			this.depth = depth;
 			this.i = i;
 		}
 	}
 
 	protected void addBreadCrumb(String itemAtPosition, ListDepth depth2) {
 		if (itemAtPosition.contains("http://")) {
 			itemAtPosition = itemAtPosition.split("http://")[1];
 			itemAtPosition = itemAtPosition.split(".store")[0];
 		}
 		Button bt = (Button) LayoutInflater.from(mContext).inflate(
 				R.layout.breadcrumb, null);
 		bt.setText(itemAtPosition);
 		bt.setTag(new BreadCrumb(depth, breadcrumbs.getChildCount() + 1));
 		System.out.println(breadcrumbs.getChildCount() + 1);
 		bt.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				depth = ((BreadCrumb) v.getTag()).depth;
 				breadcrumbs.removeViews(((BreadCrumb) v.getTag()).i,
 						breadcrumbs.getChildCount()
 								- ((BreadCrumb) v.getTag()).i);
 				refreshAvailableList(true);
 			}
 		});
 		breadcrumbs.addView(bt, new LinearLayout.LayoutParams(-2,
 				LayoutParams.WRAP_CONTENT, 1f));
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		Integer tag = (Integer) ((AdapterContextMenuInfo) menuInfo).targetView
 				.getTag();
 		if (tag != null && tag == 1) {
 			menu.add(0, 1, 0, R.string.menu_context_reparse);
 		}
 		menu.add(0, 0, 0, R.string.menu_context_remove);
 		
 		
 		
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		SimpleCursorLoader a = null;
 		switch (id) {
 		case AVAILABLE_LOADER:
 			a = new SimpleCursorLoader(mContext) {
 
 				@Override
 				public Cursor loadInBackground() {
 					switch (depth) {
 					case STORES:
 						return db.getStores(joinStores_boolean);
 					case CATEGORY1:
 						return db.getCategory1(store_id, joinStores_boolean,!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("orderByCategory", true));
 					case CATEGORY2:
 						return db.getCategory2(category_id, store_id, joinStores_boolean);
 					case ALLAPPLICATIONS:
 					case APPLICATIONS:
 						return db.getApps(category2_id, store_id, joinStores_boolean, order,!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("orderByCategory", true));
 					case TOPAPPS:
 						return db.getTopApps(category_id, store_id, joinStores_boolean);
 					case LATEST_LIKES:
 						return new LatestLikesComments(store_id,db,mContext).getLikes();
 					case LATEST_COMMENTS:
 						return new LatestLikesComments(store_id,db,mContext).getComments();
 					case RECOMMENDED:
 						return db.getUserBasedApk(store_id);
 						
 					default:
 						return null;
 					}
 				}
 			};
 			return a;
 		case INSTALLED_LOADER:
 			a = new SimpleCursorLoader(mContext) {
 
 				@Override
 				public Cursor loadInBackground() {
 					return db.getInstalledApps(order);
 				}
 			};
 			return a;
 		case UPDATES_LOADER:
 			a = new SimpleCursorLoader(mContext) {
 
 				@Override
 				public Cursor loadInBackground() {
 					return db.getUpdates(order);
 				}
 			};
 			
 			return a;
 		default:
 			break;
 		}
 		return null;
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		unbindService(conn);
 		unregisterReceiver(updatesReceiver);
 		unregisterReceiver(statusReceiver);
 		unregisterReceiver(redrawInstalledReceiver);
 		unregisterReceiver(loginReceiver);
 		generateXML();
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (!depth.equals(ListDepth.STORES)) {
 				if (depth.equals(ListDepth.TOPAPPS) || 
 						depth.equals(ListDepth.LATEST_LIKES)||
 						depth.equals(ListDepth.LATEST_COMMENTS)||
 						depth.equals(ListDepth.RECOMMENDED)||
 						depth.equals(ListDepth.ALLAPPLICATIONS)) {
 					depth = ListDepth.CATEGORY1;
 				} else {
 					depth = ListDepth.values()[depth.ordinal() - 1];
 				}
 				
 				removeLastBreadCrumb();
 				refreshAvailableList(true);
 				return false;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void removeLastBreadCrumb() {
 		breadcrumbs.removeViewAt(breadcrumbs.getChildCount() - 1);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		((CursorAdapter) availableListView.getAdapter()).swapCursor(null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		switch (loader.getId()) {
 		case AVAILABLE_LOADER:
 			availableAdapter.swapCursor(data);
 			break;
 		case INSTALLED_LOADER:
 			installedAdapter.swapCursor(data);
 			break;
 		case UPDATES_LOADER:
 			updatesAdapter.swapCursor(data);
 			break;
 		default:
 			break;
 		}
 		pb.setVisibility(View.GONE);
 //		if (availableListView.getAdapter().getCount() > 1) {
 //			joinStores.setVisibility(View.VISIBLE);
 //		} else {
 //			joinStores.setVisibility(View.INVISIBLE);
 //		}
 
 	}
 
 	private void refreshAvailableList(boolean setAdapter) {
 		if (depth.equals(ListDepth.STORES)) {
 			availableView.findViewById(R.id.add_store_layout).setVisibility(
 					View.VISIBLE);
 			registerForContextMenu(availableListView);
 		} else {
 			unregisterForContextMenu(availableListView);
 			availableListView.setLongClickable(false);
 				availableView.findViewById(R.id.add_store_layout)
 						.setVisibility(View.GONE);
 		}
 		availableView.findViewById(R.id.refresh_view_layout).setVisibility(
 				View.GONE);
 		refreshClick = true;
 		availableAdapter.changeCursor(null);
 		pb.setVisibility(View.VISIBLE);
 		if (setAdapter) {
 			availableListView.setAdapter(availableAdapter);
 		}
 		availableLoader.forceLoad();
 	}
 
 	private void showAddStoreDialog() {
 		alertDialogView = LayoutInflater.from(mContext).inflate(
 				R.layout.add_store_dialog, null);
 		alertDialog = new AlertDialog.Builder(mContext)
 				.setView(alertDialogView).create();
 		alertDialog.setTitle(getString(R.string.new_store));
 		alertDialog.setButton(Dialog.BUTTON_POSITIVE,
 				getString(R.string.new_store), addRepoListener);
 		alertDialog.setButton(Dialog.BUTTON_NEGATIVE,
 				getString(R.string.search_for_stores), searchStoresListener);
 		((EditText) alertDialogView.findViewById(R.id.edit_uri))
 				.setText("apps.store.aptoide.com");
 		alertDialog.show();
 	}
 
 	private void showAddStoreCredentialsDialog(String string) {
 		View credentialsDialogView = LayoutInflater.from(mContext).inflate(
 				R.layout.add_store_creddialog, null);
 		AlertDialog credentialsDialog = new AlertDialog.Builder(mContext)
 				.setView(credentialsDialogView).create();
 		credentialsDialog.setTitle(getString(R.string.new_store));
 		credentialsDialog.setButton(Dialog.BUTTON_NEUTRAL,
 				getString(R.string.new_store), new AddStoreCredentialsListener(
 						string, credentialsDialogView));
 		credentialsDialog.show();
 	}
 
 	public class AvailableListAdapter extends CursorAdapter {
 
 		ImageLoader loader;
 
 		public AvailableListAdapter(Context context, Cursor c, int flags) {
 			super(context, c, flags);
 			loader = ImageLoader.getInstance(context, db);
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			switch (depth) {
 			case STORES:
 				loader.DisplayImage(-1, cursor.getString(cursor.getColumnIndex("avatar")), (ImageView) view.findViewById(R.id.avatar), context, false,cursor.getString(cursor.getColumnIndex("avatar")).hashCode()+"");
 				((TextView) view.findViewById(R.id.store_name)).setText(cursor
 						.getString(cursor.getColumnIndex("name")));
 				((TextView) view.findViewById(R.id.store_dwn_number))
 						.setText(cursor.getString(cursor.getColumnIndex("status")) + " - "+cursor.getString(cursor.getColumnIndex("downloads")) + " downloads"  );
 				if (cursor.getString(cursor.getColumnIndex("status")).equals(State.FAILED.name())
 						|| cursor.getString(cursor.getColumnIndex("status")).equals(State.PARSED.name())) {
 					view.setTag(1);
 				}
 				break;
 			case TOPAPPS:
 			case APPLICATIONS:
 			case ALLAPPLICATIONS:
 			case RECOMMENDED:
 				ViewHolder holder = (ViewHolder) view.getTag();
 				if (holder == null) {
 					holder = new ViewHolder();
 					holder.name = (TextView) view.findViewById(R.id.app_name);
 					holder.icon = (ImageView) view.findViewById(R.id.app_icon);
 					holder.vername = (TextView) view
 							.findViewById(R.id.installed_versionname);
 					 holder.downloads= (TextView) view.findViewById(R.id.downloads);
 			         holder.rating= (RatingBar) view.findViewById(R.id.stars);
 					view.setTag(holder);
 				}
 				holder.name.setText(cursor.getString(1));
 				loader.DisplayImage(cursor.getLong(3), cursor.getString(4),
 						holder.icon, context, depth == ListDepth.TOPAPPS ? true
 								: false,(cursor.getString(cursor.getColumnIndex("apkid"))+"|"+cursor.getString(cursor.getColumnIndex("vercode"))).hashCode()+"");
 				holder.vername.setText(cursor.getString(2));
 				 try{
 			        	holder.rating.setRating(Float.parseFloat(cursor.getString(5)));	
 			        }catch (Exception e) {
 			        	holder.rating.setRating(0);
 					}
 				 holder.downloads.setText(cursor.getString(6));
 				break;
 			case CATEGORY1:
 				((TextView) view.findViewById(R.id.category_name))
 						.setText(cursor.getString(1));
 				break;
 			case CATEGORY2:
 				((TextView) view.findViewById(R.id.category_name))
 						.setText(cursor.getString(1));
 				break;
 			case LATEST_LIKES:
 				((TextView) view.findViewById(R.id.app_name))
 				.setText(cursor.getString(cursor.getColumnIndex("name")));
 				((TextView) view.findViewById(R.id.app_name)).setCompoundDrawablesWithIntrinsicBounds(0, 0, cursor.getString(cursor.getColumnIndex("like")).equals("TRUE")?R.drawable.up:R.drawable.down, 0);
 				((TextView) view.findViewById(R.id.user_like))
 				.setText("by "+cursor.getString(cursor.getColumnIndex("username")));
 				break;
 			case LATEST_COMMENTS:
 				((TextView) view.findViewById(R.id.comment_on_app))
 				.setText("on "+cursor.getString(cursor.getColumnIndex("name")));
 				((TextView) view.findViewById(R.id.comment))
 				.setText(cursor.getString(cursor.getColumnIndex("text")));
 				((TextView) view.findViewById(R.id.comment_owner))
 				.setText("by: "+cursor.getString(cursor.getColumnIndex("username")));
 				break;
 			default:
 				break;
 			}
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			View v = null;
 			switch (depth) {
 			case STORES:
 				v = LayoutInflater.from(context).inflate(R.layout.stores_row,
 						null);
 				break;
 			case CATEGORY1:
 				v = LayoutInflater.from(context).inflate(R.layout.catg_list,
 						null);
 				break;
 			case CATEGORY2:
 				v = LayoutInflater.from(context).inflate(R.layout.catg_list,
 						null);
 				break;
 			case TOPAPPS:
 			case ALLAPPLICATIONS:
 			case APPLICATIONS:
 			case RECOMMENDED:
 				v = LayoutInflater.from(context)
 						.inflate(R.layout.app_row, null);
 				break;
 			case LATEST_LIKES:
 				v = LayoutInflater.from(context).inflate(R.layout.latest_likes_row,
 						null);
 				break;
 			case LATEST_COMMENTS:
 				v = LayoutInflater.from(context).inflate(R.layout.latest_comments_row,
 						null);
 				break;
 			default:
 				break;
 			}
 			Animation animation = AnimationUtils.loadAnimation(mContext,
 					android.R.anim.fade_in);
 			v.startAnimation(animation);
 			return v;
 		}
 	}
 
 	static class ViewHolder {
 		ImageView icon;
 		TextView name;
 		TextView vername;
 		RatingBar rating;
 		TextView downloads;
 	}
 	Editor editor ;
 	private OnCheckedChangeListener adultCheckedListener = new OnCheckedChangeListener() {
 		
 		ProgressDialog pd;
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 			if (isChecked) {
 				AlertDialog ad = new AlertDialog.Builder(mContext).create();
 				ad.setMessage("Are you at least 21 years old?");
 				ad.setButton(Dialog.BUTTON_POSITIVE,getString(R.string.btn_yes), new Dialog.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 						editor.putBoolean("matureChkBox", false);
 						editor.commit();
 						pd = new ProgressDialog(mContext);
 						pd.setMessage(getString(R.string.please_wait));
 						pd.show();
 						new Thread(new Runnable() {
 							
 							public void run() {
 //								loadUItopapps();
 								redrawAll();
 								runOnUiThread(new Runnable() {
 									
 									public void run() {
 										pd.dismiss();
 										
 									}
 								});
 								
 							}
 						}).start();
 					}
 				});
 				ad.setButton(Dialog.BUTTON_NEGATIVE,getString(R.string.btn_no), new Dialog.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 						((ToggleButton) featuredView.findViewById(R.id.toggleButton1)).setChecked(false);
 //						if(adult!=null){
 //							adult.setChecked(false);
 //						}
 						
 					}
 				});
 				ad.show();
 			} else {
 				editor.putBoolean("matureChkBox", true);
 				editor.commit();
 				pd = new ProgressDialog(mContext);
 				pd.setMessage(getString(R.string.please_wait));
 				pd.show();
 				new Thread(new Runnable() {
 					
 					public void run() {
 //						loadUItopapps();
 						redrawAll();
 						runOnUiThread(new Runnable() {
 							
 							public void run() {
 								pd.dismiss();
 								
 							}
 						});
 						
 					}
 				}).start();
 			}
 			
 		}
 	};
 	
 	
 	
 	protected void generateXML() {
 		System.out.println("Generating servers.xml");
         File newxmlfile = new File(Environment.getExternalStorageDirectory()+"/.aptoide/servers.xml");
         try{
                 newxmlfile.createNewFile();
         }catch(IOException e){
                 Log.e("IOException", "exception in createNewFile() method");
         }
         //we have to bind the new file with a FileOutputStream
         FileOutputStream fileos = null;        
         try{
                 fileos = new FileOutputStream(newxmlfile);
         }catch(FileNotFoundException e){
                 Log.e("FileNotFoundException", "can't create FileOutputStream");
         }
         //we create a XmlSerializer in order to write xml data
         XmlSerializer serializer = Xml.newSerializer();
         try {
                 //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
                         serializer.setOutput(fileos, "UTF-8");
                         //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
                         serializer.startDocument(null, Boolean.valueOf(true));
                         //set indentation option
 //                        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                         //start a tag called "root"
                         serializer.startTag(null, "myapp");
                         //i indent code just to have a view similar to xml-tree
                         Cursor c = db.getStores(false);
                         for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
                         	serializer.startTag(null, "newserver");
 //                            serializer.endTag(null, "child1");
                            
                             serializer.startTag(null, "server");
                             serializer.text(c.getString(1));
                             serializer.endTag(null, "server");
                    
                             //write some text inside <child3>
                             
                             serializer.endTag(null, "newserver");
                         }
                         c.close();
                                 
                                
                         serializer.endTag(null, "myapp");
                         serializer.endDocument();
                         //write xml data into the FileOutputStream
                         serializer.flush();
                         //finally we close the file stream
                         fileos.close();
                         
 //                        <newserver><server>http://islafenice.bazaarandroid.com/</server></newserver></myapp>
                        
 //                TextView tv = (TextView)this.findViewById(R.id.result);
 //                        tv.setText("file has been created on SD card");
                 } catch (Exception e) {
                         Log.e("Exception","error occurred while creating xml file");
                 }
     
 
 	}
 
 }
