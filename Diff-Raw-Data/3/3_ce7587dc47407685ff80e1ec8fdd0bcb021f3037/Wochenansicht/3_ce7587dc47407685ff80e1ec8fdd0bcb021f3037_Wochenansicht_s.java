 package dhbw.stundenplan;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import android.accounts.Account;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.CursorIndexOutOfBoundsException;
 import android.database.SQLException;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.Html;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 import dhbw.stundenplan.database.TerminDBAdapter;
 import dhbw.stundenplan.database.TerminFarbeDBAdapter;
 import dhbw.stundenplan.database.TerminNotizDBAdapter;
 import dhbw.stundenplan.database.UserDBAdapter;
 import dhbw.stundenplan.google.GoogleKalender;
 import dhbw.stundenplan.google.OAuthManager;
 
 /**
  * Zeigt die Termine wochenweise an
  * 
  * @author DH10HAH
  */
 public class Wochenansicht extends OptionActivity
 {
 	final TextView[] tvNotizen = new TextView[this.anzahlMoeglicherTextViews];
 
 	private View viewPage;
 
 	float downXValue;
 	final int layout = R.layout.wochenansicht3;
 	final String VERGLEICHS_DATUM = "04.01.2010";
 	// final private float textSize = 16;
 	final private int textColor = R.color.black;
 	private int _naechsteWoche = 0;
 	private int _naechsteWoche2 = 0;
 	private int _letzterTermin = 1;
 	private String vorlesungStr;
 	private ViewPager pagerView;
 	final private int defaultPage = 2; // Startseite fr ViewPager
 	final private int anzahlSeiten = 5; // Seitenzahl fr ViewPager
 	private boolean currentDatePassed = false;
 	final private int anzahlMoeglicherTextViews = 1000;
 
 	HashMap<String, String> vorlesungHash;
 	HashMap<String, String> datumHash;
 	HashMap<String, String> startzeitHash;
 	HashMap<String, String> endzeitHash;
 	HashMap<String, String> raumHash;
 	HashMap<String, String> wochentagHash;
 	public ProgressDialog progressDialog;
 	Context context;
 	Activity activity;
 	Button button1;
 	Button button2;
 	TerminDBAdapter terminDBAdapter;
 
 	@SuppressWarnings("unchecked")
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(layout);
 		vorlesungHash = (HashMap<String, String>) getIntent().getSerializableExtra("vorlesung");
 		datumHash = (HashMap<String, String>) getIntent().getSerializableExtra("datum");
 		startzeitHash = (HashMap<String, String>) getIntent().getSerializableExtra("startzeit");
 		endzeitHash = (HashMap<String, String>) getIntent().getSerializableExtra("endzeit");
 		raumHash = (HashMap<String, String>) getIntent().getSerializableExtra("raum");
 		wochentagHash = (HashMap<String, String>) getIntent().getSerializableExtra("wochentag");
 
 		pagerView = (ViewPager) findViewById(R.id.viewPager);
 		pagerView.setAdapter(new AwesomePagerAdapter());
 		pagerView.setCurrentItem(defaultPage);
 
 		context = this;
 		activity = this;
 
 		button1 = (Button) findViewById(R.id.button1);
 		button2 = (Button) findViewById(R.id.button2);
 
 		terminDBAdapter = new TerminDBAdapter(this);
 
 		for (int i = 0; i < this.anzahlMoeglicherTextViews; i++)
 		{
 			tvNotizen[i] = new TextView(context);
 			registerForContextMenu(tvNotizen[i]);
 		}
 	}
 
 	/**
 	 * Ermittelt aus einem Datum(String) den entsprechenden Wochentag. Der Tag
 	 * wird als int zurckgegeben 0: Fehler 1: Montag 2: Dienstag 3: Mittwoch 4:
 	 * Donnerstag 5: Freitag 6: Samstag 7: Sonntag
 	 * 
 	 * @param checkDatum
 	 *            Datum welches auf seinen Wochentag kontrolliert werden soll
 	 * @return Liefert den etnsprechenden Wochentag als int zurck
 	 */
 	public int ermittleWochentag(String checkDatum)
 	{
 		String datumStr = "";
 		int datumInt = 0;
 		try
 		{
 			Date datum = new SimpleDateFormat("dd.MM.yyyy").parse(checkDatum);
 			datumStr = datum.toString();
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (datumStr.contains("Mon"))
 		{
 			datumInt = 1;
 		}
 		else
 		{
 			if (datumStr.contains("Tue"))
 			{
 				datumInt = 2;
 			}
 			else
 			{
 				if (datumStr.contains("Wed"))
 				{
 					datumInt = 3;
 				}
 				else
 				{
 					if (datumStr.contains("Thu"))
 					{
 						datumInt = 4;
 					}
 					else
 					{
 						if (datumStr.contains("Fri"))
 						{
 							datumInt = 5;
 						}
 						else
 						{
 							if (datumStr.contains("Sat"))
 							{
 								datumInt = 6;
 							}
 							else
 							{
 								if (datumStr.contains("Sun"))
 									datumInt = 7;
 							}
 						}
 					}
 				}
 			}
 		}
 		return datumInt;
 	}
 
 	/**
 	 * Die Methode fllt die Vorhanden Termine als neue textView in die
 	 * Vorhandenen Layouts
 	 */
 	private void fuelleTermine()
 	{
 		final TextView[] tv = new TextView[anzahlMoeglicherTextViews];
 		int i2 = 0;
 		int dbGroesse = terminDBAdapter.gibDBGroesse();
 		boolean abbrechen = false;
 		TerminFarbeDBAdapter terminFarbeDBAdapter = new TerminFarbeDBAdapter(context);
 		int i = _letzterTermin;
 		boolean wochenstart = false;
 		boolean ersterTerminInDB = false;
 
 		final LinearLayout linearLayout2 = (LinearLayout) viewPage.findViewById(R.id.linearLayout2);
 		linearLayout2.removeAllViewsInLayout();
 		final LinearLayout linearLayout3 = (LinearLayout) viewPage.findViewById(R.id.linearLayout3);
 		linearLayout3.removeAllViewsInLayout();
 		final LinearLayout linearLayout4 = (LinearLayout) viewPage.findViewById(R.id.linearLayout4);
 		linearLayout4.removeAllViewsInLayout();
 		final LinearLayout linearLayout5 = (LinearLayout) viewPage.findViewById(R.id.linearLayout5);
 		linearLayout5.removeAllViewsInLayout();
 		final LinearLayout linearLayout6 = (LinearLayout) viewPage.findViewById(R.id.linearLayout6);
 		linearLayout6.removeAllViewsInLayout();
 		final LinearLayout linearLayout7 = (LinearLayout) viewPage.findViewById(R.id.linearLayout7);
 		linearLayout7.removeAllViewsInLayout();
 
 		final TextView tv01 = (TextView) viewPage.findViewById(R.id.textView01);
 		tv01.setText("");
 		final TextView tv02 = (TextView) viewPage.findViewById(R.id.textView02);
 		tv02.setText("");
 		final TextView tv03 = (TextView) viewPage.findViewById(R.id.textView03);
 		tv03.setText("");
 		final TextView tv04 = (TextView) viewPage.findViewById(R.id.textView04);
 		tv04.setText("");
 		final TextView tv05 = (TextView) viewPage.findViewById(R.id.textView05);
 		tv05.setText("");
 		final TextView tv06 = (TextView) viewPage.findViewById(R.id.textView06);
 		tv06.setText("");
 
 		Calendar cal = null;
 
 		SimpleDateFormat curDate = new SimpleDateFormat("dd.MM.yyyy");
 
 		while (i != dbGroesse && !abbrechen)
 		{
			String datumStr = datumHash.get(String.valueOf(i));// c.getString(0);//0=Datum
 																// 1=Startzeit
 																// 2=Endzeit
 																// 3=Vorlesung
 																// 4=Raum
 																// 5=Wochentag
 			vorlesungStr = vorlesungHash.get(String.valueOf(i));// c.getString(3);
 			String startzeitStr = startzeitHash.get(String.valueOf(i));
 			String endzeitStr = endzeitHash.get(String.valueOf(i));
 			String raumStr = raumHash.get(String.valueOf(i));
 			String wochentagStr = wochentagHash.get(String.valueOf(i));
 
 			int wochentag = 0;
 
 			if (i < 1)
 			{
 				i = 1;
 				ersterTerminInDB = true;
 			}
 			if (!wochenstart && i > 0 && !ersterTerminInDB)
 			{
 				cal = Calendar.getInstance();
 				cal.setTime(cal.getTime());
 				if (!ersteSeiteAktuell)
 				{
 					cal.add(Calendar.DATE, -7);
 				}
 				Date datum = cal.getTime();
 				String curDateStr = curDate.format(datum);
 				if (datumStr.contains(curDateStr))
 				{
 					currentDatePassed = true;
 				}
 				if (currentDatePassed && wochentagStr.contains("7"))
 				{
 					wochenstart = true;
 				}
 				else
 				{
 					if (currentDatePassed)
 					{
 						i = i - 2;
 					}
 				}
 			}
 			else
 			{
 				if (i < 1)
 				{
 					i = 1;
 					;
 				}
 				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 				params.setMargins(2, 1, 2, 1);
 				wochentag = ermittleWochentag(datumStr);
 				switch (wochentag)
 				// 1=Montag 2=Dienstag 3=Mittwoch 4=Donnerstag 5=Freitag
 				{
 					case 1:
 					{
 						_naechsteWoche = 1;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							tv01.setText(this.getString(R.string.monday) + " " + datumStr.substring(0, 6));
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								tv[i2].setBackgroundColor(terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr));
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout2.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					case 2:
 					{
 						_naechsteWoche = 2;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							tv02.setText(this.getString(R.string.tuesday) + " " + datumStr.substring(0, 6));
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								int farbe = terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr);
 								tv[i2].setBackgroundColor(farbe);
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout3.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					case 3:
 					{
 						_naechsteWoche = 3;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							tv03.setText(this.getString(R.string.wednesday) + " " + datumStr.substring(0, 6));
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								tv[i2].setBackgroundColor(terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr));
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout4.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					case 4:
 					{
 						_naechsteWoche = 4;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							tv04.setText(this.getString(R.string.thursday) + " " + datumStr.substring(0, 6));
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								tv[i2].setBackgroundColor(terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr));
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout5.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					case 5:
 					{
 						_naechsteWoche = 5;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							tv05.setText(this.getString(R.string.friday) + " " + datumStr.substring(0, 6));
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								tv[i2].setBackgroundColor(terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr));
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout6.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					case 6:
 					{
 						_naechsteWoche = 6;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							if (tv06.getText() == "")
 							{
 								tv06.setText(datumStr.substring(0, 3));
 							}
 							else
 							{
 								tv06.setText(this.getString(R.string.weekend) + " " + datumStr.substring(0, 3) + "/" + tv06.getText());
 							}
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								tv[i2].setBackgroundColor(terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr));
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout7.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					case 7:
 					{
 						_naechsteWoche = 7;
 						if (_naechsteWoche < _naechsteWoche2)
 						{
 							abbrechen = true;
 							break;
 						}
 						else
 						{
 							if (tv06.getText() != "")
 							{
 								tv06.setText(this.getString(R.string.weekend) + " " + tv06.getText() + "/" + datumStr.substring(0, 6));
 							}
 							else
 							{
 								tv06.setText(datumStr.substring(0, 6));
 							}
 							if (vorlesungStr.length() != 0)
 							{
 								tv[i2] = new TextView(this);
 								tv[i2].setText(Html.fromHtml("" + vorlesungStr + "<br />" + startzeitStr + "-" + endzeitStr + "<br />" + raumStr));
 								tv[i2].setBackgroundColor(terminFarbeDBAdapter.gibVorelsungsFarbe(vorlesungStr));
 								tv[i2].setTextColor(textColor);
 								tv[i2].setLayoutParams(params);
 								linearLayout7.addView(tv[i2]);
 								final int x = i2;
 								tv[i2].setOnClickListener(new View.OnClickListener()
 								{
 									public void onClick(View v)
 									{
 										terminOeffnen(tv[x].getText());
 									}
 								});
 							}
 							_naechsteWoche2 = _naechsteWoche;
 							break;
 						}
 					}
 					default:
 					{
 						_naechsteWoche = 0;
 						break;
 					}
 				}
 			}
 			_letzterTermin = i;
 			i++;
 			i2 = i2 + 1;
 		}
 		_naechsteWoche2 = 0;
 	}
 
 	/**
 	 * Wechselt die anzeige zur nchste Woche Methode ntig fr Verknpfung mit
 	 * wochenansicht3.xml: android:onClick sendet View mit !!!Beim Aufruf der
 	 * aktuellen Seite: dies Seite als Nchste
 	 * 
 	 * @param view
 	 * @throws ParseException
 	 */
 	public void naechsteWoche(View view) throws ParseException
 	{
 		if (letzteRichtungVorherige && !letzteRichtungNaechste)
 		{
 			Date datum;
 			String datumStr = datumHash.get(String.valueOf(_letzterTermin));
 			datum = new SimpleDateFormat("dd.MM.yyyy").parse(datumStr);
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(datum);
 			calendar.add(Calendar.DATE, 7 * (positionDiff - 1));
 			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
 			datum = calendar.getTime();
 			datumStr = sdf.format(datum);
 			int idNaechsteWoche = terminDBAdapter.gibID(datumStr);
 			_letzterTermin = idNaechsteWoche;
 		}
 		fuelleTermine();
 		letzteRichtungNaechste = true;
 	}
 
 	private boolean letzteRichtungVorherige = false;
 	private boolean letzteRichtungNaechste = false;
 	private boolean ersteSeiteAktuell = false;
 
 	/**
 	 * Wechselt die anzeige zur vorherigen Woche Methode ntig fr Verknpfung
 	 * mit wochenansicht3.xml: android:onClick sendet View mit !!!Beim Aufruf
 	 * der aktuellen Seite: dies Seite als Aktuelle
 	 * 
 	 * @param view
 	 * @throws ParseException
 	 */
 	public void vorherigeWoche(View view) throws ParseException
 	{
 		Date datum;
 		String datumStr = datumHash.get(String.valueOf(_letzterTermin));
 		datum = new SimpleDateFormat("dd.MM.yyyy").parse(datumStr);
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(datum);
 
 		if ((positionDiff == 1) || (naechste && !vorherige))
 		{
 			calendar.add(Calendar.DATE, -(7 * (positionDiff + 1) + 1));
 		}
 		else
 		{
 			calendar.add(Calendar.DATE, -(7 * (positionDiff + 1) + 1));
 		}
 
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
 		datum = calendar.getTime();
 		datumStr = sdf.format(datum);
 
 		// TODO: KALENDERWOCHEN berechnung
 
 		int idVorherigeWoche = terminDBAdapter.gibID(datumStr);
 
 		_letzterTermin = idVorherigeWoche;
 		letzteRichtungNaechste = false;
 		letzteRichtungVorherige = true;
 		fuelleTermine();
 	}
 
 	/**
 	 * !!!Beim Aufruf der aktuellen Seite: diese Seite als Aktuelle
 	 * 
 	 * @param view
 	 * @throws ParseException
 	 */
 	public void aktuelleWoche(View view) throws ParseException
 	{
 		currentDatePassed = false;
 		_letzterTermin = 1;
 		ersteSeiteAktuell = true;
 		positionTmp = defaultPage;
 		start = true;
 		naechste = false;
 		vorherige = false;
 		onCreate(null);
 	}
 
 	/**
 	 * ffnet die Notizbersicht in einer neuen Activity
 	 * 
 	 * @param view
 	 */
 	public void listeNotizenAuf2(View view)
 	{
 		intent.setClass(context, NotizUebersicht.class);
 		this.startActivity(intent);
 	}
 
 	/**
 	 * ffnet die Notizbersicht in einem PopupWindow
 	 * 
 	 * @param view
 	 */
 	public void listeNotizenAuf(View view)
 	{
 		// We need to get the instance of the LayoutInflater, use the context of
 		// this activity
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
 		// Inflate the view from a predefined XML layout
 		View layout = inflater.inflate(R.layout.popup_layout_notizuebersicht, (ViewGroup) findViewById(R.id.popup_element));
 		// create a 300px width and 470px height PopupWindow
 		Display display = getWindowManager().getDefaultDisplay();
 		int displayWidth = display.getWidth();
 		int displayHeight = display.getHeight();
 		popupWindow = new PopupWindow(layout, displayWidth - displayWidth / 10, displayHeight - displayHeight / 10, true);
 		// display the popup in the center
 		popupWindow.showAtLocation(layout, Gravity.CENTER, 0, +20);
 
 		TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 		Cursor c = terminNotizDBAdapter.fetchAll();
 
 		int i = 1;
 		LinearLayout ll = (LinearLayout) layout.findViewById(R.id.notizContainer);
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		params.setMargins(2, 8, 0, 6);
 		params2.setMargins(0, 0, 0, 0);
 
 		if (c.moveToFirst())
 		{
 			for (int y = 0; y < c.getCount(); y++)
 			{
 				String str = c.getString(0);
 				String str2 = c.getString(1);
 				if (str2.length() > 2)
 				{
 					tvNotizen[i].setText(str);
 					tvNotizen[i].setTextSize(22);
 					tvNotizen[i].setPadding(10, 0, 0, -2);
 					tvNotizen[i].setLayoutParams(params);
 					tvNotizen[i].setTypeface(Typeface.SERIF, Typeface.BOLD);
 					tvNotizen[i].setTextColor(Color.BLACK);
 					tvNotizen[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.background_notiz));
 					ll.addView(tvNotizen[i]);
 					final int x1 = i;
 					tvNotizen[i].setOnClickListener(new View.OnClickListener()
 					{
 						public void onClick(View v)
 						{
 							popupWindow.dismiss();
 							terminOeffnen(tvNotizen[x1].getText());
 						}
 					});
 					i++;
 
 					tvNotizen[i].setText(str2);
 					tvNotizen[i].setTextSize(18);
 					tvNotizen[i].setMaxLines(3);
 					tvNotizen[i].setPadding(10, 0, 25, 10);
 					tvNotizen[i].setTextColor(Color.BLACK);
 					tvNotizen[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.background_repeat));
 					tvNotizen[i].setLayoutParams(params2);
 					ll.addView(tvNotizen[i]);
 					tvNotizen[i].setOnClickListener(new View.OnClickListener()
 					{
 						public void onClick(View v)
 						{
 							popupWindow.dismiss();
 							terminOeffnen(tvNotizen[x1].getText());
 						}
 					});
 					i++;
 				}
 				c.moveToNext();
 			}
 			if (tvNotizen[1] == null)
 			{
 				popupWindow.dismiss();
 				Toast.makeText(getApplicationContext(), "Keine Notizen vorhanden. Klicke eine Vorlesung an um eine Notiz zu erstellen", Toast.LENGTH_LONG).show();
 			}
 		}
 		else
 		{
 			popupWindow.dismiss();
 			Toast.makeText(getApplicationContext(), "Keine Notizen vorhanden. Klicke eine Vorlesung an um eine Notiz zu erstellen", Toast.LENGTH_LONG).show();
 		}
 		c.close();
 		tvNotizen[0].setMinHeight(display.getHeight());
 		tvNotizen[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.background_repeat));
 		ll.addView(tvNotizen[0]);
 		Button cancelButton = (Button) layout.findViewById(R.id.end_data_send_button);
 		cancelButton.setOnClickListener(new OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				popupWindow.dismiss();
 			}
 		});
 	}
 
 	private PopupWindow popupWindow;
 	private EditText editText;
 	private TextView textViewVorlesung;
 
 	/**
 	 * Wird aufgerufen wenn man auf einen Termin klickt. Zeigt diesen in einer
 	 * neuen Activity an und bietet einem die Mglichkeit Notizen zu einem
 	 * Termin einzutragen
 	 * 
 	 * @param daten
 	 *            Vorlesung zu welcher die Notizen geffnet werden sollen
 	 */
 	public void terminOeffnen(final CharSequence daten)
 	{
 
 		// We need to get the instance of the LayoutInflater, use the context of
 		// this activity
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
 		// Inflate the view from a predefined XML layout
 		View layout = inflater.inflate(R.layout.popup_layout_notiz, (ViewGroup) findViewById(R.id.popup_element));
 		// create a 300px width and 470px height PopupWindow
 		Display display = getWindowManager().getDefaultDisplay();
 		int displayWidth = display.getWidth();
 		int displayHeight = display.getHeight();
 		popupWindow = new PopupWindow(layout, displayWidth - displayWidth / 10, displayHeight - displayHeight / 10, true);
 		// display the popup in the center
 		popupWindow.showAtLocation(layout, Gravity.CENTER, 0, +20);
 
 		EditText editTextNotiz = (EditText) layout.findViewById(R.id.editTextNotiz);
 		editTextNotiz.setMinHeight(displayHeight - displayHeight / 10);
 
 		Button speichern = (Button) layout.findViewById(R.id.end_data_send_button);
 		textViewVorlesung = (TextView) layout.findViewById(R.id.textViewNotiz);
 		editText = (EditText) layout.findViewById(R.id.editTextNotiz);
 		if (daten.toString().contains("\n"))
 		{
 			final String str[] = daten.toString().split("\n");
 
 			textViewVorlesung.setText(str[0]);
 			try
 			{
 				TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 				String vorlesungStr = str[0].toString();
 				Cursor c = terminNotizDBAdapter.fetchTerminNotiz(vorlesungStr);
 				c.moveToFirst();
 				editText.setText(c.getString(1));
 				c.close();
 				terminNotizDBAdapter.close();
 			}
 			catch (SQLException se)
 			{
 				editText.setText("");
 			}
 			catch (CursorIndexOutOfBoundsException ce)
 			{
 				editText.setText("");
 			}
 			speichern.setOnClickListener(new OnClickListener()
 			{
 				public void onClick(View v)
 				{
 					try
 					{
 						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 						if (!(terminNotizDBAdapter.updateTerminNotiz(str[0], editText.getText().toString()))) // Macht update und liefert true falls der Vorgang scheitert
 						{
 							terminNotizDBAdapter.createTerminNotiz(str[0], editText.getText().toString());
 						}
 						terminNotizDBAdapter.close();
 					}
 					catch (SQLException se)
 					{
 						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 						terminNotizDBAdapter.createTerminNotiz(str[0], editText.getText().toString());
 						terminNotizDBAdapter.close();
 					}
 					popupWindow.dismiss();
 				}
 			});
 		}
 		else
 		{
 			textViewVorlesung.setText(daten.toString());
 			try
 			{
 				TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 				Cursor c = terminNotizDBAdapter.fetchTerminNotiz(daten.toString());
 				c.moveToFirst();
 				editText.setText(c.getString(1));
 				c.close();
 				terminNotizDBAdapter.close();
 			}
 			catch (SQLException se)
 			{
 				editText.setText("");
 			}
 			catch (CursorIndexOutOfBoundsException ce)
 			{
 				editText.setText("");
 			}
 
 			editText = (EditText) layout.findViewById(R.id.editTextNotiz);
 
 			speichern.setOnClickListener(new OnClickListener()
 			{
 				public void onClick(View v)
 				{
 					try
 					{
 						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 						if (!(terminNotizDBAdapter.updateTerminNotiz(daten.toString(), editText.getText().toString())))  // Macht update und liefert true falls der Vorgang scheitert
 						{
 							terminNotizDBAdapter.createTerminNotiz(daten.toString(), editText.getText().toString());
 						}
 						terminNotizDBAdapter.close();
 					}
 					catch (SQLException se)
 					{
 						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
 						terminNotizDBAdapter.createTerminNotiz(daten.toString(), editText.getText().toString());
 						terminNotizDBAdapter.close();
 					}
 					popupWindow.dismiss();
 				}
 			});
 		}
 		Button cancel = (Button) layout.findViewById(R.id.button1);
 		cancel.setOnClickListener(new OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				popupWindow.dismiss();
 			}
 		});
 	}
 
 	/**
 	 * Klasse die die aktuelle angezeigte Seite zurck gibt
 	 * 
 	 * @author DH10HAH
 	 */
 	private class AwesomePagerAdapter extends PagerAdapter
 	{
 		@Override
 		public void destroyItem(View collection, int position, Object view)
 		{
 			((ViewPager) collection).removeView((View) view);
 		}
 
 		@Override
 		public void finishUpdate(View arg0)
 		{
 			// setPageTitles(getPageNumber());
 		}
 
 		@Override
 		public int getCount()
 		{
 			return anzahlSeiten;
 		}
 
 		@Override
 		public Object instantiateItem(View collection, int position)
 		{
 			View view = getViewToShow(position);
 			((ViewPager) collection).addView(view, 0);
 
 			return view;
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object)
 		{
 			return view == ((View) object);
 		}
 
 		@Override
 		public void restoreState(Parcelable arg0, ClassLoader arg1)
 		{
 		}
 
 		@Override
 		public Parcelable saveState()
 		{
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View arg0)
 		{
 		}
 	}
 
 	private int positionTmp = 0; // Zwischenspeicher fr die Postion der
 									// Seiten/Pages
 	private int positionDiff = 0; // Differenz zwischen der letzten Position und
 									// der aktuellen position ( zur berechnung
 									// der richtigen Wochen)
 	private boolean start = true; // Wenn die ansicht geffnet wird muss sie
 									// anderst gestartet werden, dient zur
 									// unterscheidung
 	private boolean naechste = false; // gibt an ob naechste woche schoneinmal
 										// aufgerufen wurde
 	private boolean vorherige = false; // gibt an ob vorherige woche schoneinmal
 										// aufgerufen wurde
 
 	Calendar kw;
 
 	/**
 	 * Liefert die neue View die an "position" angezeigt werden soll
 	 * 
 	 * @param position
 	 * @return
 	 */
 	private View getViewToShow(int position)
 	{
 		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
 
 		if (start)
 		{
 			start = false;
 			viewPage = mInflater.inflate(R.layout.wochenansicht0, null);
 			kw = Calendar.getInstance();
 			kw.setFirstDayOfWeek(Calendar.MONDAY);
 			kw.setTime(new Date());
 			TextView statusLeiste = (TextView) findViewById(R.id.statusleiste);
 			statusLeiste.setText("Aktuelle Kalenderwoche: " + kw.get(Calendar.WEEK_OF_YEAR));
 			fuelleTermine();
 		}
 		else
 		{
 			if (positionTmp < position)
 			{
 				viewPage = mInflater.inflate(R.layout.wochenansicht0, null);
 				positionDiff = position - positionTmp;
 				try
 				{
 					naechsteWoche(null);
 					kw.add(Calendar.DATE, 7);
 				}
 				catch (ParseException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				naechste = true;
 
 			}
 			else
 			{
 				viewPage = mInflater.inflate(R.layout.wochenansicht0, null);
 				positionDiff = positionTmp - position;
 				try
 				{
 					vorherigeWoche(null);
 					kw.add(Calendar.DATE, -7);
 				}
 				catch (ParseException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				vorherige = true;
 
 			}
 			positionTmp = position;
 
 		}
 
 		return viewPage;
 	}
 
 	Online online;
 
 	/**
 	 * Startet einen ProgressDialog und startet das Downloaden der Termine
 	 * 
 	 * @param view
 	 */
 	public void aktualisieren(View view)
 	{
 		progressDialog = new ProgressDialog(context);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressDialog.setMessage("Aktualisiere Termine");
 		progressDialog.setCancelable(false);
 		progressDialog.show();
 		new DownloadTermine().execute("");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.item1:
 			{
 				/*
 				 * LOGOUT UserDB wird geleert(Leerer User eingetragen) und
 				 * LoginScreen aufgerufen
 				 */
 				UserDBAdapter userDBAdapter = new UserDBAdapter(this);
 				userDBAdapter.deleteUserDB();
 				intent.setClass(this, Login.class);
 				startActivity(intent);
 				finish();
 				break;
 			}
 
 			case R.id.item2:
 			{
 				intent.setClass(this, Wochenansicht.class);
 
 				progressDialog = new ProgressDialog(context);
 				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 				progressDialog.setMessage("Aktualisiere Termine");
 				progressDialog.setCancelable(false);
 				progressDialog.show();
 				new DownloadTermine().execute("");
 				break;
 			}
 
 			case R.id.item3:
 			{
 				intent.setClass(this, Settings.class);
 				startActivity(intent);
 				break;
 			}
 
 			case R.id.item4:
 			{
 				intent.setClass(this, Noten.class);
 				startActivity(intent);
 				break;
 			}
 
 		}
 		return true;
 	}
 
 	/**
 	 * Startet einen Ladedialog, und aktualisiert die TerminDB
 	 * 
 	 * @author DH10HAH
 	 */
 	private class DownloadTermine extends AsyncTask<String, Integer, Object>
 	{
 		boolean internetConnection;
 
 		@Override
 		protected Object doInBackground(String... arg)
 		{
 
 			Looper.prepare();
 			if (checkInternetConnection())
 			{
 
 				final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 				final String anzahlMonate = sharedPrefs.getString("anzahlmonate", "2");
 
 				UserDBAdapter userDBAdapter = new UserDBAdapter(context);
 				final String password = userDBAdapter.getPassword();
 				final String username = userDBAdapter.getUsername();
 
 				Online online = new Online();
 				online.ladeTermineInDB(username, password, Integer.parseInt(anzahlMonate), context);
 				ladeTermineInHash();
 				internetConnection = true;
 			}
 			else
 			{
 				internetConnection = false;
 			}
 			return null;
 		}
 
 		/**
 		 * 
 		 */
 		public void onPostExecute(Object result)
 		{
 			if (internetConnection)
 			{
 				progressDialog.dismiss();
 				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
 				if (sharedPreferences.getBoolean("googleCalendarSync", false))
 				{
 					progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 					progressDialog.setMessage("Lade Termine in Googlekalender");
 					progressDialog.setCancelable(false);
 					progressDialog.show();
 					OAuthManager.getInstance().doLogin(true, activity, new OAuthManager.AuthHandler()
 					{
 						public void handleAuth(Account accountU, String authTokenU)
 						{
 							account = accountU;
 							authToken = authTokenU;
 							new TermineInKalender().execute("");
 						}
 					});
 
 				}
 				else
 				{
 					intent.setClass(context, Wochenansicht.class);
 					startActivityForResult(intent, 0);
 					finish();
 				}
 			}
 			else
 			{
 				progressDialog.dismiss();
 				Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	protected Account account;
 	protected String authToken;
 
 	/**
 	 * Startet einen Ladedialog und schreibt die Termine in den Google-Kalender
 	 * 
 	 * @author DH10HAH
 	 */
 	private class TermineInKalender extends AsyncTask<String, Integer, Object>
 	{
 		boolean internetConnection;
 		GoogleKalender googleKalender;
 
 		@Override
 		protected Object doInBackground(String... arg)
 		{
 			if (checkInternetConnection())
 			{
 				googleKalender = new GoogleKalender(activity, context);
 				googleKalender.ladeTermineInKalender(account, authToken);// schreibeTermineInKalender();
 
 				internetConnection = true;
 			}
 			else
 			{
 				internetConnection = false;
 			}
 			return null;
 		}
 
 		/**
 		 * 
 		 */
 		public void onPostExecute(Object result)
 		{
 			while (!googleKalender.ready)
 			{
 
 			}
 			if (internetConnection)
 			{
 				intent.setClass(context, Wochenansicht.class);
 				startActivityForResult(intent, 0);
 				progressDialog.dismiss();
 				finish();
 			}
 			else
 			{
 				progressDialog.dismiss();
 				Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 }
