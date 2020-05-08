 package com.mvanveggel.gordijnapp;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 public class Splash extends Activity {
 
 	boolean interupted = false;
 	DataManager sqlData;
 	Thread timer;
	long sleepTime = 1000;
 
 	int aantalStoffen = 1;
 	long stoffenVersie = 0;
 
 	int aantalMaakwijzen = 1;
 	long maakwijzenVersie = 0;
 
 	int aantalPrijzen = 1;
 	long prijzenVersie = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 		sqlData = new DataManager(this);
 		
 		setupTimer();
 		setHeaders();
 
 		loadData();
 
 	}
 
 	private void setupTimer() {
 		timer = new Thread() {
 			public void run() {
 				try {
 					sleep(sleepTime);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
 					if (!interupted) {
 						Intent openHoofdscherm = new Intent(Splash.this, Hoofdscherm.class);
 						startActivity(openHoofdscherm);
 					}
 				}
 			}
 		};
 	}
 
 	private void setHeaders() {
 		try {
 			Resources res = getResources();
 			XmlPullParser xpp = res.getXml(R.xml.stoffen);
 			int eventType = xpp.getEventType();
 			while (eventType != XmlPullParser.END_DOCUMENT) {
 				if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("header")) {
 					aantalStoffen = Integer.valueOf(xpp.getAttributeValue(null, "aantal_artikelen"));
 					stoffenVersie = Long.valueOf(xpp.getAttributeValue(null, "version"));
 					break;
 				}
 				eventType = xpp.next();
 			}
 			xpp = res.getXml(R.xml.maakwijzen);
 			eventType = xpp.getEventType();
 			while (eventType != XmlPullParser.END_DOCUMENT) {
 				if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("header")) {
 					aantalMaakwijzen = Integer.valueOf(xpp.getAttributeValue(null, "aantal_maakwijzen"));
 					maakwijzenVersie = Long.valueOf(xpp.getAttributeValue(null, "version"));
 					break;
 				}
 				eventType = xpp.next();
 			}
 			xpp = res.getXml(R.xml.maakprijzen);
 			eventType = xpp.getEventType();
 			while (eventType != XmlPullParser.END_DOCUMENT) {
 				if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("header")) {
 					aantalPrijzen = Integer.valueOf(xpp.getAttributeValue(null, "aantal_maakprijzen"));
 					prijzenVersie = Long.valueOf(xpp.getAttributeValue(null, "version"));
 					break;
 				}
 				eventType = xpp.next();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void loadData() {
 		boolean loadDataStoffen = false;
 		boolean loadDataMaakwijzen = false;
 		boolean loadDataMaakPrijzen = false;
 		sqlData.open();
 		if (!sqlData.tableExists(DataManager.DATABASE_TABLE_STOFFEN)) {
 			String[] values = { DataManager.VALUE_ARTIKELVERSIE, String.valueOf(stoffenVersie), "Versie gebaseerd op datum van aanmaken", "", "", "",
 					"", "", "" };
 			sqlData.deleteTable(3001337);
 			sqlData.setEntryStof(values);
 			loadDataStoffen = true;
 		} else if (stoffenVersie > sqlData.getStoffenResourceVersie()) {
 			String[] values = { DataManager.VALUE_ARTIKELVERSIE, String.valueOf(stoffenVersie), "Versie gebaseerd op datum van aanmaken", "", "", "",
 					"", "", "" };
 			sqlData.deleteTable(3001337);
 			sqlData.setEntryStof(values);
 			loadDataStoffen = true;
 		}
 		if (!sqlData.tableExists(DataManager.DATABASE_TABLE_MAAKWIJZEN)) {
 			String[] values = { DataManager.VALUE_MAAKWIJZENVERSIE, String.valueOf(maakwijzenVersie), "", "", "", "", "", "", "", "", "", "", "" };
 			sqlData.deleteTable(4001337);
 			sqlData.setEntryMaakwijze(values);
 			loadDataMaakwijzen = true;
 		} else if (maakwijzenVersie > sqlData.getMaakwijzenesourceVersie()) {
 			String[] values = { DataManager.VALUE_MAAKWIJZENVERSIE, String.valueOf(maakwijzenVersie), "", "", "", "", "", "", "", "", "", "", "" };
 			sqlData.deleteTable(4001337);
 			sqlData.setEntryMaakwijze(values);
 			loadDataMaakwijzen = true;
 		}
 		if (!sqlData.tableExists(DataManager.DATABASE_TABLE_MAAKPRIJZEN)) {
 			String[] values = new String[62];
 			values[0] = DataManager.VALUE_MAAKPRIJZENVERSIE;
 			values[1] = String.valueOf(prijzenVersie);
 			for (int i = 2; i < 62; i++)
 				values[i] = "";
 			sqlData.deleteTable(5001337);
 			sqlData.setEntryMaakPrijs(values);
 			loadDataMaakPrijzen = true;
 		} else if (prijzenVersie > sqlData.getMaakprijzenesourceVersie()) {
 			String[] values = new String[62];
 			values[0] = DataManager.VALUE_MAAKPRIJZENVERSIE;
 			values[1] = String.valueOf(prijzenVersie);
 			for (int i = 2; i < 62; i++)
 				values[i] = "";
 			sqlData.deleteTable(5001337);
 			sqlData.setEntryMaakPrijs(values);
 			loadDataMaakPrijzen = true;
 		}
 		sqlData.close();
 
 		if (loadDataStoffen || loadDataMaakwijzen || loadDataMaakPrijzen)
 			new loadXmlInBackground().execute(loadDataStoffen, loadDataMaakwijzen, loadDataMaakPrijzen);
 		else
 			timer.start();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		interupted = true;
 		finish();
 	}
 
 	private class loadXmlInBackground extends AsyncTask<Boolean, Integer, String> {
 
 		ProgressDialog dialog;
 		DataManager sqlData;
 
 		protected void onPreExecute() {
 			sqlData = new DataManager(getBaseContext());
 			dialog = new ProgressDialog(Splash.this);
 			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			dialog.setMessage("Bezig met inladen van artikelen\nResterende tijd wordt berekend");
 			dialog.setMax(aantalStoffen);
 			dialog.show();
 		}
 
 		@Override
 		protected String doInBackground(Boolean... params) {
 			if (params[0]) {
 				try {
 					Resources res = getBaseContext().getResources();
 					XmlPullParser xpp = res.getXml(R.xml.stoffen);
 					int eventType = xpp.getEventType();
 					int counter = 0;
 					long begin, end;
 					begin = System.currentTimeMillis();
 					sqlData.open();
 					while (eventType != XmlPullParser.END_DOCUMENT) {
 						if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("Artikel")) {
 							String[] values = new String[xpp.getAttributeCount()];
 							for (int i = 0; i < xpp.getAttributeCount(); i++)
 								values[i] = xpp.getAttributeValue(i);
 							sqlData.setEntryStof(values);
 							counter++;
 							if (counter % 100 == 0 && counter > 100) {
 								int timeLeftSeconds = 0;
 								end = System.currentTimeMillis();
 								long timeLapsed = end - begin;
 								long timeLeft = ((aantalStoffen - counter) / 100) * timeLapsed;
 								timeLeftSeconds = (int) timeLeft / 1000;
 								publishProgress(1, timeLeftSeconds);
 								begin = end;
 							} else
 								publishProgress(1, 0);
 						}
 						eventType = xpp.next();
 					}
 					sqlData.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			if (params[1]) {
 				try {
 					publishProgress(0, -1);
 					Resources res = getBaseContext().getResources();
 					XmlPullParser xpp = res.getXml(R.xml.maakwijzen);
 					int eventType = xpp.getEventType();
 					sqlData.open();
 					while (eventType != XmlPullParser.END_DOCUMENT) {
 						if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("Maakwijze")) {
 							String[] values = new String[xpp.getAttributeCount()];
 							for (int i = 0; i < xpp.getAttributeCount(); i++)
 								values[i] = xpp.getAttributeValue(i);
 							sqlData.setEntryMaakwijze(values);
 							publishProgress(1, 0);
 						}
 						eventType = xpp.next();
 					}
 					sqlData.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			if (params[2]) {
 				try {
 					publishProgress(0, -2);
 					Resources res = getBaseContext().getResources();
 					XmlPullParser xpp = res.getXml(R.xml.maakprijzen);
 					int eventType = xpp.getEventType();
 					sqlData.open();
 					while (eventType != XmlPullParser.END_DOCUMENT) {
 						if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("Prijs")) {
 							String[] values = new String[xpp.getAttributeCount()];
 							for (int i = 0; i < xpp.getAttributeCount(); i++)
 								values[i] = xpp.getAttributeValue(i);
 							sqlData.setEntryMaakPrijs(values);
 							publishProgress(1, 0);
 						}
 						eventType = xpp.next();
 					}
 					sqlData.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 			if (progress[1] == -1) {
 				dialog.setMessage("Bezig met inladen van maakwijzen");
 				dialog.setMax(aantalMaakwijzen);
 				dialog.setProgress(0);
 			} else if (progress[1] == -2) {
 				dialog.setMessage("Bezig met inladen van prijslijst");
 				dialog.setMax(aantalPrijzen);
 				dialog.setProgress(0);
 			} else {
 				dialog.incrementProgressBy(progress[0]);
 				if (progress[1] > 0) {
 					dialog.setMessage("Bezig met inladen van artikelen\n" + progress[1] + " seconden resterend (+/-)");
 				}
 			}
 		}
 
 		protected void onPostExecute(String result) {
 			dialog.dismiss();
 			timer.start();
 		}
 	}
 }
