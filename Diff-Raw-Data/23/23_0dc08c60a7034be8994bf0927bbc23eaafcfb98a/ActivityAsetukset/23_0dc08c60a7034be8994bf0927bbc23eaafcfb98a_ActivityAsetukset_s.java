 package com.example.frisbeegolfscores;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class ActivityAsetukset extends PreferenceActivity {
 	
     protected Method mLoadHeaders = null;
     protected Method mHasHeaders = null;
 	
 	private Kielisyys kielisyys = new Kielisyys();
 	private SQLiteDatabase database;
 	private DBAsetukset dbAsetukset;// = new DBAsetukset(this);
     private Asetukset asetukset;
     private Context appContext;
 	private Context actContext;
 	private Context sqlContext;
 	private Context mContext;
 	private ApplicationController mApplication;
 	private SharedPreferences sharedPrefs;
 	private SharedPreferences.Editor sharedPrefsEditor;
 	
 	//asetukset
 	public int muutoksia = 0;
 	public int kieli = 0;
 	public boolean metric = false;
 	public boolean usegps = false;
 	public boolean vuoro = false;
 	public boolean jarjestys = false;
 	public int raportti = 0;
 	
 	//nytn objektit
 	ListPreference settings_languages;
 	
 	/*
 	String[] spinnerKieliValues = new String[]{};
 	
 	//nytn objektit
 	private TextView txtOtsikkoVersio;
 	private TextView txtVersio;
 	private TextView txtOtsikkoKieli;
 	private TextView txtKieli;
     private Spinner spinnerKieli;
 	private TextView txtOtsikkoGps;
 	private CheckedTextView chktxtGps;
 	private TextView txtOtsikkoVuoro;
 	private CheckedTextView chktxtVuoro;
 	private TextView txtOtsikkoJarjestys;
 	private CheckedTextView chktxtJarjestys;
 	private TextView txtOtsikkoRaportti;
 	private CheckedTextView chktxtRaportti;
 	private ArrayAdapter <CharSequence> adapter;
 */
 
     @SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		try {
 			mLoadHeaders = getClass().getMethod("loadHeadersFromResource",int.class, List.class);
 			mHasHeaders = getClass().getMethod("hasHeaders");
 		} catch (NoSuchMethodException e) {
 		} 
 		super.onCreate(savedInstanceState);
 		//setContentView(R.layout.activity_asetukset);
 		if (!isNewV11Prefs()) {
 			addPreferencesFromResource(R.xml.app_prefs);
//			addPreferencesFromResource(R.xml.app_prefs_cat1);
//			addPreferencesFromResource(R.xml.app_prefs_cat2);
//			addPreferencesFromResource(R.xml.app_prefs_cat3);
 		}
 
         //application controller
         mApplication = ((ApplicationController)getApplicationContext());
         mContext = mApplication.getInstance();
         sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
         sharedPrefsEditor = sharedPrefs.edit();
 	}
 
     @Override
     protected void onStart() {
     	// The activity is about to become visible.
         super.onStart();
         
         //mritelln context
         actContext = this;
         appContext = this.getApplicationContext();
         sqlContext = this.getApplication();
         /*
         //nytn objektien alustukset
         txtOtsikkoVersio = (TextView) findViewById(R.id.txtOtsikkoVersio);
         txtVersio = (TextView) findViewById(R.id.txtVersio);
         txtOtsikkoKieli = (TextView) findViewById(R.id.txtOtsikkoKieli);
         txtKieli = (TextView) findViewById(R.id.txtKieli);
         spinnerKieli = (Spinner) findViewById(R.id.spinnerKieli);
         txtOtsikkoGps = (TextView) findViewById(R.id.txtOtsikkoGps);
         chktxtGps = (CheckedTextView) findViewById(R.id.chktxtGps);
         txtOtsikkoVuoro = (TextView) findViewById(R.id.txtOtsikkoVuoro);
         chktxtVuoro = (CheckedTextView) findViewById(R.id.chktxtVuoro);
         txtOtsikkoJarjestys = (TextView) findViewById(R.id.txtOtsikkoJarjestys);
         chktxtJarjestys = (CheckedTextView) findViewById(R.id.chktxtJarjestys);
         txtOtsikkoRaportti = (TextView) findViewById(R.id.txtOtsikkoRaportti);
         chktxtRaportti = (CheckedTextView) findViewById(R.id.chktxtRaportti);
         */
 
         database = mApplication.getInstance().getDBInstance();
         dbAsetukset = new DBAsetukset(mContext);
 	    dbAsetukset.setDBInstance(database);
         asetukset = dbAsetukset.getAsetus(1);
         kieli = asetukset.getKieli();
     	usegps = asetukset.getUseGPS();
     	vuoro = asetukset.getVuoronvaihto();
     	jarjestys = asetukset.getPelijarjestys();
     	raportti = asetukset.getRaportinmuoto();
 
     	//settings_languages;
     	
         // Reading all asetukset
         Log.d("FrisbeeGolfScores: ", "Haetaan kaikki asetukset..");
         List<Asetukset> Listasetukset = dbAsetukset.haeAsetukset();
 
         for (Asetukset cn : Listasetukset) {
             String log = "Id: "+cn.getId()+" ,Kieli: " + cn.getKieli() + " ,Jarjestys: " + cn.getPelijarjestys() + " ,Metric: " + cn.getMetric() + " ,Vuoro: " + cn.getVuoronvaihto();
                 // Writing Contacts to log
             Log.d("Asetukset (settings) : ", log);
         }
 /*
     	//mritelln layoutin tekstit kielisyyden mukaisesti
         txtOtsikkoVersio.setText(kielisyys.strKieli_txtAsetuksetOtsikkoVersio[kieli]);
         txtVersio.setText(kielisyys.strKieli_txtAsetuksetVersio[kieli] + " " + asetukset.getVersio());
         txtOtsikkoKieli.setText(kielisyys.strKieli_txtAsetuksetOtsikkoKieli[kieli]);
         txtKieli.setText(kielisyys.strKieli_txtAsetuksetKieli[kieli]);
         txtOtsikkoGps.setText(kielisyys.strKieli_txtAsetuksetOtsikkoGps[kieli]);
         chktxtGps.setText(kielisyys.strKieli_txtAsetuksetGps[kieli]);
         txtOtsikkoVuoro.setText(kielisyys.strKieli_txtAsetuksetOtsikkoVuoro[kieli]);
         chktxtVuoro.setText(kielisyys.strKieli_txtAsetuksetVuoro[kieli]);
         txtOtsikkoJarjestys.setText(kielisyys.strKieli_txtAsetuksetOtsikkoJarjestys[kieli]);
         chktxtJarjestys.setText(kielisyys.strKieli_txtAsetuksetJarjestys[kieli]);
         txtOtsikkoRaportti.setText(kielisyys.strKieli_txtAsetuksetOtsikkoRaportti[kieli]);
         chktxtRaportti.setText(kielisyys.strKieli_txtAsetuksetRaportti[kieli]);
         
         adapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         for (int x=0; x<=1;x++) {
         	adapter.add(kielisyys.kielet[x]);
         }
         
         //luodaan kenttien listenerit
 		spinnerKieli.setOnItemSelectedListener(new OnItemSelectedListener() {
 			// @Override
 			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 				if (kieli != position) {
 					muutoksia = 1;
 					kieli = position;
 				}
 			}
 
 			// @Override
 			public void onNothingSelected(AdapterView<?> parentView) {
 				// your code here
 			}
 		});
         spinnerKieli.setAdapter(adapter);
         
 		chktxtGps.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				muutoksia = 1;
 				CheckedTextView tt = (CheckedTextView) v.findViewById(R.id.chktxtGps);
 				if (!tt.isChecked()) {
 					tt.setChecked(true);
 					gps = 1;
 				} else {
 					tt.setChecked(false);
 					gps = 0;
 				}
 			}
         });
 
 		chktxtVuoro.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				muutoksia = 1;
 				CheckedTextView tt = (CheckedTextView) v.findViewById(R.id.chktxtVuoro);
 				if (!tt.isChecked()) {
 					tt.setChecked(true);
 					vuoro = 1;
 				} else {
 					tt.setChecked(false);
 					vuoro = 0;
 				}
 			}
         });
 
 		chktxtJarjestys.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				muutoksia = 1;
 				CheckedTextView tt = (CheckedTextView) v.findViewById(R.id.chktxtJarjestys);
 				if (!tt.isChecked()) {
 					tt.setChecked(true);
 					jarjestys = 1;
 				} else {
 					tt.setChecked(false);
 					jarjestys = 0;
 				}
 			}
         });
 
 		chktxtRaportti.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				muutoksia = 1;
 				CheckedTextView tt = (CheckedTextView) v.findViewById(R.id.chktxtRaportti);
 				if (!tt.isChecked()) {
 					tt.setChecked(true);
 					raportti = 1;
 				} else {
 					tt.setChecked(false);
 					raportti = 0;
 				}
 			}
         });
 		
         //mritelln valinnat asetuksien mukaisiksi
 		spinnerKieli.setSelection(kieli, true);
 		
         if (gps == 1) {
         	chktxtGps.setChecked(true);
         }
         
         if (vuoro == 1) {
         	chktxtVuoro.setChecked(true);
         }
         
         if (jarjestys == 1) {
         	chktxtJarjestys.setChecked(true);
         }
         
         if (raportti == 1) {
         	chktxtRaportti.setChecked(true);
         } */       
     }
 /*
     @Override
     protected void onRestart() {
     	// The activity is about to become visible again.
         super.onRestart();
     }
     
     @Override
     protected void onResume() {
     	// The activity has become visible (it is now "resumed").
     	super.onResume();
     }
 
     @Override
     protected void onPause() {
     	// Another activity is taking focus (this activity is about to be "paused").
     	super.onPause();
     }*/
 
     @Override
     protected void onStop() {
     	// The activity is no longer visible (it is now "stopped")
         super.onStop();
         
         //tallennetaan asetukset
         asetukset.setKieli(Integer.parseInt(sharedPrefs.getString("settings_language",String.valueOf(kieli))));
        	asetukset.setMetric(sharedPrefs.getBoolean("settings_metric",metric));
        	asetukset.setUseGPS(sharedPrefs.getBoolean("settings_usegps",usegps));
        	asetukset.setVuoronvaihto(sharedPrefs.getBoolean("settings_vuoro",vuoro));
        	asetukset.setPelijarjestys(sharedPrefs.getBoolean("settings_jarjestys",jarjestys));
        	asetukset.setRaportinmuoto(Integer.parseInt(sharedPrefs.getString("settings_report",String.valueOf(raportti))));
       	dbAsetukset.updateAsetukset(asetukset);
     }
 /*    
     @Override
     protected void onDestroy() {
     	// The activity is about to be destroyed.
         super.onDestroy();
     }
 */
     
     /**
      * Checks to see if using new v11+ way of handling PrefsFragments.
      * @return Returns false pre-v11, else checks to see if using headers.
      */
     public boolean isNewV11Prefs() {
         if (mHasHeaders!=null && mLoadHeaders!=null) {
             try {
                 return (Boolean)mHasHeaders.invoke(this);
             } catch (IllegalArgumentException e) {
             } catch (IllegalAccessException e) {
             } catch (InvocationTargetException e) {
             }
         }
         return false;
     }
     
     @Override
     public void onBuildHeaders(List<Header> aTarget) {
         try {
             //mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers,aTarget});
             mLoadHeaders.invoke(this,new Object[]{R.xml.app_prefs,aTarget});
         } catch (IllegalArgumentException e) {
         } catch (IllegalAccessException e) {
         } catch (InvocationTargetException e) {
         }  
     }
  
     @SuppressLint("NewApi")
     static public class PrefsFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle aSavedState) {
             super.onCreate(aSavedState);
             Context anAct = getActivity().getApplicationContext();
             int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),"xml",anAct.getPackageName());
             addPreferencesFromResource(thePrefRes);
         }
     }
 }
