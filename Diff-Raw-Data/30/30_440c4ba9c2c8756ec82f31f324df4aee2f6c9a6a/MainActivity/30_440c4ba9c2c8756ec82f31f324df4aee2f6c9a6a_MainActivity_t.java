 package com.ecn.urbapp.activities;
 import java.io.File;
 import java.util.Vector;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.widget.ImageView;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.db.LocalDataSource;
 import com.ecn.urbapp.dialogs.ConfirmPhotoDialogFragment;
 import com.ecn.urbapp.fragments.CharacteristicsFragment;
 import com.ecn.urbapp.fragments.HomeFragment;
 import com.ecn.urbapp.fragments.InformationFragment;
 import com.ecn.urbapp.fragments.SaveFragment;
 import com.ecn.urbapp.fragments.ZoneFragment;
 import com.ecn.urbapp.listener.MyTabListener;
 import com.ecn.urbapp.utils.ConnexionCheck;
 import com.ecn.urbapp.zones.Zone;
 
 /**
  * @author	COHENDET Sébastien
  * 			DAVID Nicolas
  * 			GUILBART Gabriel
  * 			PALOMINOS Sylvain
  * 			PARTY Jules
  * 			RAMBEAU Merwan
  * 
  * MainActivity class
  * 
  * This is the main activity of the application.
  * It contains an action bar filled with the different fragments
  * 			
  */
 
 public class MainActivity extends Activity {
 
 	/**
 	 * bar represent the action bar of the application
 	 */
 	ActionBar bar;
 	public Zone zoneToDelete ;
 	
 	/**
 	 * attributs for the local database
 	 */
 	public static LocalDataSource datasource;
 
 	
 	/**
 
 	 * baseContext to get the static context of app anywhere (for file)
 	 */
     public static Context baseContext;
 
 	private static Builder alertDialog;
     
     public static final String CONNECTIVITY_URL="http://clients3.google.com/generate_204";
     
     /**
 	 * Attributs for the project information
 	 */
 
 	public static String author = "";
 	public static String device = "";
 	public static String project = "";
 	public static String address = "";
 
 	public static Vector<Zone> zones=null;
 	public static ImageView myImage=null;
 	
 	public static String pathImage=null;
 	public static String pathTampon=null;
 	public static File photo=null;
 	//TODO add the set of this bool into each function loading a photo
 	public static boolean isPhoto=false;
 	public static boolean start = true;
 	
 	private Vector<Fragment> fragments=null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		fragments=new Vector<Fragment>();
 		
 		//Setting the Context of app
 		baseContext = getBaseContext();
 		
 		alertDialog = new AlertDialog.Builder(MainActivity.this);
 		isInternetOn();
 		
 		//Setting the Activity bar
 		bar = getActionBar();
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		bar.setDisplayHomeAsUpEnabled(true);
 		
 		//Setting the Context of app
 		baseContext = getBaseContext();
 		
 		//initialization of the local database
 		datasource = new LocalDataSource(this);
 		
 		//Setting of the different tab of the bar
 		
 		//Home tab
 		Tab tabHome =  bar.newTab();
 		tabHome.setText(R.string.homeFragment);
 		HomeFragment home = new HomeFragment();
 		tabHome.setTabListener(new MyTabListener(home, this));
 		bar.addTab(tabHome);
 		fragments.add(home);
 		
 		//Information tab
 		Tab tabInformation =  bar.newTab();
 		tabInformation.setText(R.string.informationFragment);
 		InformationFragment information = new InformationFragment();
 		tabInformation.setTabListener((new MyTabListener(information, this)));
 		bar.addTab(tabInformation);
 		fragments.add(information);
 		
 		//Zone tab
 		Tab tabZone =  bar.newTab();
 		tabZone.setText(R.string.zoneFragment);
 		ZoneFragment zone = new ZoneFragment();
 		tabZone.setTabListener(new MyTabListener(zone, this));
 		bar.addTab(tabZone);
 		fragments.add(zone);
 		
 		//Definition tab
 		Tab tabDefinition =  bar.newTab();
 		tabDefinition.setText(R.string.definitionFragment);
 		CharacteristicsFragment definition = new CharacteristicsFragment();
 		tabDefinition.setTabListener(new MyTabListener(definition, this));
 		bar.addTab(tabDefinition);
 		fragments.add(definition);
 		
 		//Save tab
 		Tab tabSave =  bar.newTab();
 		tabSave.setText(R.string.saveFragment);
 		SaveFragment save = new SaveFragment();
 		tabSave.setTabListener(new MyTabListener(save, this));
 		bar.addTab(tabSave);
 		fragments.add(save);
 		
 		//create zones' list for new image
 		zones = new Vector<Zone>();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu, this adds items to the action bar if it is present.
 		MenuInflater inflater =	getMenuInflater();
 		inflater.inflate(R.menu.menu_main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * Method to check if internet is available (and no portal !)
 	 */
 	public final void isInternetOn() {
 
 		ConnectivityManager con=(ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
         boolean wifi=con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
         boolean mobile = false;
         
         try {
          mobile=con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
         }
         catch (NullPointerException e){
         	mobile=false;
         }
         boolean internet=wifi|mobile;
         if (internet)
         	 new ConnexionCheck().Connectivity();
 	}
 
 	/**
 	 * Method if no internet connectivity to print a Dialog.
 	 */
 	public static void errorConnect() {
 		alertDialog.setTitle("Pas de connexion internet de disponible. Relancer l'application, une fois internet fonctionnel");
 		alertDialog.show();		
 	}
 
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == 0) {
             if (resultCode == RESULT_OK) {
             	confirm();
             	FragmentManager fragmentManager = getFragmentManager();
             	FragmentTransaction transaction = fragmentManager.beginTransaction();
             	transaction.replace(android.R.id.content, fragments.get(1));
                 transaction.addToBackStack(null);
                 transaction.commit();
                 getActionBar().setSelectedNavigationItem(1);
                 MainActivity.isPhoto=true;
             }
         }
             if (requestCode == 1) {
                 if (pathImage != null) {
             	//TODO check that this is not a crash
                 	confirm();
                                 	FragmentManager fragmentManager = getFragmentManager();
                 	FragmentTransaction transaction = fragmentManager.beginTransaction();
                 	transaction.replace(android.R.id.content, fragments.get(2));
                     transaction.addToBackStack(null);
                     transaction.commit();
                     getActionBar().setSelectedNavigationItem(2);
                     MainActivity.isPhoto=true;
                 }
         }
             if (requestCode == 2) {
                 if (resultCode == RESULT_OK) {
                 	confirm();
                 	MainActivity.pathImage = getRealPathFromURI(baseContext, data.getData());
                 	FragmentManager fragmentManager = getFragmentManager();
                 	FragmentTransaction transaction = fragmentManager.beginTransaction();
                 	transaction.replace(android.R.id.content, fragments.get(1));
                     transaction.addToBackStack(null);
                     transaction.commit();
                     getActionBar().setSelectedNavigationItem(1);
                     MainActivity.isPhoto=true;
                 }
             }
     }
 	
 	public void confirm(){
 		if(MainActivity.pathTampon!=null){
 			ConfirmPhotoDialogFragment typedialog = new ConfirmPhotoDialogFragment();
 			typedialog.show(getFragmentManager(), "CharacteristicsDialogFragment");
 		}
 	}
 	
 	
 	public String getRealPathFromURI(Context context, Uri contentUri) {
 		  Cursor cursor = null;
 		  try { 
 		    String[] proj = { MediaStore.Images.Media.DATA };
 		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
 		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		    cursor.moveToFirst();
 		    return cursor.getString(column_index);
 		  } finally {
 		    if (cursor != null) {
 		      cursor.close();
 		    }
 		}
 	}
 }
