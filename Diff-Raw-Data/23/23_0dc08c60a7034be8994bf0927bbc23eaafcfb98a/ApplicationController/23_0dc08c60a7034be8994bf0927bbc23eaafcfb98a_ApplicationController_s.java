 package com.example.frisbeegolfscores;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.preference.PreferenceManager;
 
 public class ApplicationController extends Application {
 	//Application wide instance variables
 	private static ApplicationController singleton = null;
 	private Context appContext;
 	
 	private SQLiteDatabase database;
 	private DBAvaus dbAvaus;  
     
     //Yleiset muuttujat
     //public int[] aktiivisetPelaajat;
     List<Integer> aktiivisetPelaajat = new ArrayList<Integer>();
     private int aktiivinenRata = 0;
     private int aktiivinenVayla = 0;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 	    //mritelln context
 		singleton = this;
 	    appContext = this.getApplicationContext();
 	    
 	    //avataan kantayhteys
 		openDB();
 		
 		//mritelln asetukset
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(singleton);
 	}
 	
 	public ApplicationController getInstance() {
 		if (singleton == null) {
 			singleton = new ApplicationController();
 		}
 		return singleton;
 	}
 	
 	public Context getAppInstance() {
 		return appContext;
 	}
 
 	public void openDB() {
         dbAvaus = new DBAvaus(appContext);
         if (!dbAvaus.status()){
 	        database = dbAvaus.open();
         }
 	}
 	
 	public void closeDB() {
         if (database != null && dbAvaus.status()){
         	dbAvaus.close();
         }		
 	}
 
 	public SQLiteDatabase getDBInstance() {
 		return database;
 	}
 	
 	public boolean getDBstatus() {
 		if (database == null) {
 			return false;
 		}
 		return database.isOpen();
 	}
 /*	
 	public DBAsetukset openDBAsetukset() {
 		if (dbAvaus.status()) {
 			DBAsetukset dbAsetukset = new DBAsetukset(appContext);
 			dbAsetukset = new DBAsetukset(appContext);
 			dbAsetukset.setDBInstance(database);
 		}
 		return dbAsetukset;
 	}
 
 	public void openDBPelaajat() {
 		if (dbAvaus.status()) {
 			DBPelaajat dbPelaajat = new DBPelaajat(appContext);
 			dbPelaajat = new DBPelaajat(appContext);
 			dbPelaajat.setDBInstance(database);
 		}
 	}
 
 	public void openDBJoukkueet() {
 		if (dbAvaus.status()) {
 			DBJoukkueet dbJoukkueet = new DBJoukkueet(appContext);
 			dbJoukkueet = new DBJoukkueet(appContext);
 			dbJoukkueet.setDBInstance(database);
 		}
 	}
 
 	public void openDBKaupungit() {
 		if (dbAvaus.status()) {
 			DBKaupungit dbKaupungit = new DBKaupungit(appContext);
 			dbKaupungit = new DBKaupungit(appContext);
 			dbKaupungit.setDBInstance(database);
 		}
 	}
 
 	public void openDBRadat() {
 		if (dbAvaus.status()) {
 			DBRadat dbRadat = new DBRadat(appContext);
 			dbRadat = new DBRadat(appContext);
 			dbRadat.setDBInstance(database);
 		}
 	}
 
 	public void openDBVaylat() {
 		if (dbAvaus.status()) {
 			DBVaylat dbVaylat = new DBVaylat(appContext);
 			dbVaylat = new DBVaylat(appContext);
 			dbVaylat.setDBInstance(database);
 		}
 	}
 
 	public void openDBKierros() {
 		if (dbAvaus.status()) {
 			DBKierros dbKierros = new DBKierros(appContext);
 			dbKierros = new DBKierros(appContext);
 			dbKierros.setDBInstance(database);
 		}
 	}
 */
     public List<Integer> getAktiivisetPelaajat() {
 		return aktiivisetPelaajat;
 	}
 
 	public void addAktiivisetPelaajat(int pelaaja) {
 		this.aktiivisetPelaajat.add(pelaaja);
 	}
 
 	public void deleteAktiivisetPelaajat(int pelaaja) {
 		this.aktiivisetPelaajat.remove(pelaaja);
 	}
 
 	public void clearAktiivisetPelaajat() {
 		this.aktiivisetPelaajat.clear();
 	}
 
 	public int getAktiivinenRata() {
 		return aktiivinenRata;
 	}
 
 	public void setAktiivinenRata(int aktiivinenRata) {
 		this.aktiivinenRata = aktiivinenRata;
 	}
 
 	public int getAktiivinenVayla() {
 		return aktiivinenVayla;
 	}
 
 	public void setAktiivinenVayla(int aktiivinenVayla) {
 		this.aktiivinenVayla = aktiivinenVayla;
 	}
 
 }
