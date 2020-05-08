 package pl.tabhero.net;
 
 import pl.tabhero.R;
 import pl.tabhero.TabHero;
 import pl.tabhero.db.DBAdapter;
 import pl.tabhero.utils.MyTelephonyManager;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
  
 public class TabViewActivity extends Activity {
 	
 	private WakeLock mWakeLock = null;
 	private ImageButton btnPlus;
 	private ImageButton btnMinus;
 	private TextView tab;
 	private LinearLayout buttons;
 	private boolean max;
 	private boolean lock = false;
 	private MyTelephonyManager device = new MyTelephonyManager(this);
 	
 	private int scaleText = 12;
 	
 	DBAdapter db = new DBAdapter(this); 
 	String performer;
 	String title;
 	String listOfSections;
 	String songUrl;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tabview);
         
         device.setHomeButtonEnabledForICS();
         
         PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
         mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "lockScreenApp");
         mWakeLock.acquire();
 
         
     	TextView head = (TextView) findViewById(R.id.performerAndTitle);
     	tab = (TextView) findViewById(R.id.tabInTabView);
     	buttons = (LinearLayout) findViewById(R.id.buttons);
     	buttons.setVisibility(View.GONE);
     	
         Intent i = getIntent();
         Bundle extras = i.getExtras();
         
         max = extras.getBoolean("max");
 		if(max) {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
         
     	performer = extras.getString("performerName");
     	title = extras.getString("songTitle");
     	listOfSections = extras.getString("tab");
     	songUrl = extras.getString("songUrl");
     	Log.d("URL", songUrl);
 
     	head.setText(performer + " - " + title);
     	tab.setText(listOfSections);
     	
     	tab.setOnLongClickListener(new AdapterView.OnLongClickListener() {
     		public boolean onLongClick(View v) {
     			if(!lock) {
     				int result = TabViewActivity.this.getResources().getConfiguration().orientation;
     				if(result == 1) {
     					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     				} else {
     					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     				}
     				lock = true;
     				Toast.makeText(getApplicationContext(), R.string.lockOn, Toast.LENGTH_LONG).show();
     			} else {
     				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
     				lock = false;
     				Toast.makeText(getApplicationContext(), R.string.lockOff, Toast.LENGTH_LONG).show();
     			}
 				return false;
 			}
         });
     	
     }
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		buttons.setVisibility(View.VISIBLE);
 		initBtnPlusOnClick();
     	initBtnMinusOnClick();
     	new Handler().postDelayed(new Runnable() {
             public void run() {
             	buttons.setVisibility(View.GONE);
             }
         }, 3000);
 		return true;
 	}
 	
 	private void initBtnPlusOnClick() {
 		
 		btnPlus = (ImageButton) findViewById(R.id.btnPlus);
 		
 		btnPlus.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				scaleText++;
 				tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaleText);
 			}
 			
 		});
 	}
 	
 	private void initBtnMinusOnClick() {
 		
 		btnMinus = (ImageButton) findViewById(R.id.btnMinus);
 		
 		btnMinus.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				scaleText--;
 				tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaleText);
 			}
 			
 		});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    MyTelephonyManager manager = new MyTelephonyManager(this);
 	    if(manager.isTablet()) {
	    	inflater.inflate(R.menu.tabviewiftablet, menu);
 	    } else {
	    	inflater.inflate(R.menu.tabview, menu);
 	    }
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case android.R.id.home:
 	    	device.goHomeScreen();
 	    	return true;
 	    case R.id.addToFav:
 	        addToFav();
 	        return true;
 	    case R.id.openWebBrowser:
 	    	openWebBrowser();
 	    	return true;
 	    case R.id.minmax:
 	    	minMax();
 	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	private void minMax() {
 		boolean fullScreen = (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
 		if(fullScreen) {
     	   getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	   getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
     	   max = false;
         }
         else {
         	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
         	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         	max = true;
         }
     }
 	
 	public void addToFav() {
 		
 		if(isExistRecord() == false) {
 			if(isIdExist()) {
 				buildAlertDialogToAddTabWhithSameId();
 			} else { 
 				db.open();
 				db.insertRecord(performer, title, listOfSections, songUrl);
 				db.close();
 				Toast.makeText(getApplicationContext(), R.string.addToBaseSuccess, Toast.LENGTH_LONG).show();
 			}
 		} else {
 			Toast.makeText(getApplicationContext(), R.string.recordExist, Toast.LENGTH_LONG).show();
 		}
 		
 	}
 	
 	private void buildAlertDialogToAddTabWhithSameId() {
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.tabExist);
 		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				db.open();
 				db.insertRecord(performer, title, listOfSections, songUrl);
 				db.close();
 				Toast.makeText(getApplicationContext(), R.string.addToBaseSuccess, Toast.LENGTH_LONG).show();
 				dialog.dismiss();
 			}
 		});
 
 		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Toast.makeText(getApplicationContext(), R.string.notAddTab, Toast.LENGTH_LONG).show();
 				dialog.dismiss();
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
     }
 	
 	private boolean isIdExist() {
 		String url;
 		String idFromBase;
 		String idFromNet;
 		idFromNet = getIdFromUrl(songUrl);
 		db.open();
 		Cursor c = db.getAllRecords();
         if (c.moveToFirst())
         {
             do {
             	url = c.getString(4);
             	idFromBase = getIdFromUrl(url);
             	if(idFromNet.equals(idFromBase)) {
             		return true;
             	}
             } while (c.moveToNext());
         }
         db.close();
 		return false;
 	}
 	
 	private String getIdFromUrl(String url) {
 		String [] tab1;
 		String[] tab2;
 		tab1 = url.split("/");
     	tab2 = tab1[5].split(",");
     	String id = tab2[0];
 		return id;
 	}
 	
 	public boolean isExistRecord() {
 		db.open();
         Cursor c = db.getAllRecords();
         if (c.moveToFirst())
         {
             do {
             	if(songUrl.equals(c.getString(4))) {
             		return true;
             	}
             } while (c.moveToNext());
         }
         db.close();
 		return false;
 	}
 	
 	private void openWebBrowser() {
 		String perf = performer.replaceAll(" ", "%20");
 		String tit = title.replaceAll(" ", "%20");
 		String question = "http://www.google.com/search?q=" + perf + "%20" + tit + "%20lyrics";
 		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(question));
 		startActivityForResult(browserIntent, 600);
 		overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
 	}
     
 	 public void onPause() {
 		 super.onPause();
 		 mWakeLock.release();
 	 }
 	 
 	 public void onResume() {
 		 super.onResume();
 		 mWakeLock.acquire();
 	 }
 	 
 	 public void onDestroy() {
 		 mWakeLock.release();
 		 super.onDestroy();
 	 }
 	 
 	 @Override
 	 public void onBackPressed() {
 		 super.onBackPressed();
 		 overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
 	 }
 }
