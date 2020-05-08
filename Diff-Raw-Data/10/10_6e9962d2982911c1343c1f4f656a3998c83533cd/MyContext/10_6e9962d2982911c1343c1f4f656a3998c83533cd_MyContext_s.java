 /*
  * MyContext.java
  * 
  * Tobias Janssen, 2013
  * GNU GENERAL PUBLIC LICENSE Version 2
  */
 
 package de.janssen.android.gsoplan;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import com.viewpagerindicator.TitlePageIndicator;
 import de.janssen.android.gsoplan.core.FileOPs;
 import de.janssen.android.gsoplan.core.Stupid;
 import de.janssen.android.gsoplan.runnables.AppendPage;
 import de.janssen.android.gsoplan.runnables.ErrorMessage;
 import de.janssen.android.gsoplan.runnables.SetupPager;
 import de.janssen.android.gsoplan.view.MyArrayAdapter;
 import de.janssen.android.gsoplan.view.PlanActivity;
 import de.janssen.android.gsoplan.view.WeekPlanActivity;
 import de.janssen.android.gsoplan.xml.Xml;
import de.janssen.android.gsoplan.xml.XmlOPs;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.ContextWrapper;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 
 public class MyContext
 {
     public Stupid stupid = new Stupid();
     public Exception exception;
     public MyArrayAdapter adapter;
     public Calendar dateBackup;
     public Handler handler;
     public Boolean selfCheckIsRunning = false;
     public int weekDataIndexToShow;
     public WorkerQueue executor = new WorkerQueue(this);
     public List<View> pages = new ArrayList<View>();
     public List<Calendar> pageIndex = new ArrayList<Calendar>();;
     public List<String> headlines = new ArrayList<String>();
     public LayoutInflater inflater;
     public PagerAdapter pageAdapter;
     public ViewPager viewPager;
     public Boolean disableNextPagerOnChangedEvent = false;
     public TitlePageIndicator pageIndicator;
     public int currentPage;
     public Context context;
     public Activity activity;
     public Boolean weekView = false;
     public Boolean dayView = false;
     public Boolean initialLunch = true;
     public Boolean pagerReady = false;
     public int[] textSizes;
     private Class<?> defaultActivity;
     public Boolean forceView = false;
     public ProgressDialog progressDialog;
     public Boolean newVersionInfo = false;
     public String newVersionMsg = "";
 
     public MyContext(ContextWrapper appctxt, Activity activity)
     {
 	this.context = appctxt;
 	this.activity = activity;
     }
 
     public Class<?> getDefaultActivityClass()
     {
 	return this.defaultActivity;
 
     }
 
     /**
      * @author janssen
      * @return			String mit der Default activity
      */
     public String getDefaultActivity()
     {
 	if (this.defaultActivity.equals(PlanActivity.class))
 	    return "Tag";
 	else if (this.defaultActivity.equals(WeekPlanActivity.class))
 	    return "Woche";
 	else
 	{
 	    try
 	    {
 		setDefaultActivity("Tag");
 	    }
 	    catch (Exception e)
 	    {
 		// Trifft nicht zu!
 	    }
 	    return "Tag";
 	}
 
     }
 
     /**
      * @author Tobias Janssen 
      * @param value		String, der enweder "tag", oder "woche" enthlt
      * @throws Exception	Wenn String ungltig
      */
     public void setDefaultActivity(String value) throws Exception
     {
 	if (value.equalsIgnoreCase("tag"))
 	    this.defaultActivity = PlanActivity.class;
 	else if (value.equalsIgnoreCase("woche"))
 	    this.defaultActivity = WeekPlanActivity.class;
 	else
 	    throw new Exception("Ungltiger Wert! Nur 'tag' & 'woche' sind gltig");
     }
 
     /**
      * @author Tobias Janssen 
      * ldt die einstellungen der applikation
      * 
      * @param context 	Context der Applikation
      */
     public void getPrefs(Context context)
     {
 	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 	
 	//prfen, ob noch alte Settingsversionen vorhanden sind, wenn ja diese konvertieren und lschen
 	Tools.translateOldSettings(this);
 
 	try
 	{
 	    if (stupid.getMyElement().isEmpty())
 	    {
 		String element = prefs.getString("listElement", "");
 		stupid.setMyElementValid(element);
 	    }
 	}
 	catch (Exception e)
 	{
 	    // Element ist ungltig
 	}
 	try
 	{
 	    stupid.setMyType(prefs.getString("listType", "Klassen"));
 	}
 	catch (Exception e)
 	{
 	    // listType ist ungltig
 	}
 	try
 	{
 	    String value = prefs.getString("listResync", "10");
 	    stupid.setMyResync(Long.parseLong(value));
 	}
 	catch (Exception e)
 	{
 	    // Resync ist ungltig
 	}
 	try
 	{
 	    setDefaultActivity(prefs.getString("listActivity", "Tag"));
 	}
 	catch (Exception e)
 	{
 	    // Resync ist ungltig
 	}
 
 	stupid.hideEmptyHours = prefs.getBoolean("boxHide", false);
 	stupid.onlyWlan = prefs.getBoolean("boxWlan", false);
 
     }
 
     /**
      * @author Tobias Janssen
      * prft, ob alle Laufzeitbedrfnisse erfllt sind
      * 
      * @return			Integer mit dem Fehlercode
      */
     public int checkStructure()
     {
 
 	// Prfen, ob die bentigten Dateien existieren:
 	// Elementen Datei beinhaltet die Listen elemente/typ/wochen
 	File elementFile = Tools.getFileSaveElement(this);
 	if (!elementFile.exists())
 	{
 	    return 1;
 	}
 	// die ElementDatei Laden
 	try
 	{
 	    Xml xml = new Xml("root", FileOPs.readFromFile(elementFile));
 	    stupid.clearElements(); // Alle bisherigen Daten entfernen
 	    stupid.fetchElementsFromXml(xml, this); // Daten aus dem
 						    // xml.contaner wandeln
 	    getPrefs(this.context.getApplicationContext()); // Settings laden
 	}
 	catch (Exception e)
 	{
 	    return 1; // Fehler beim Laden der ElementDatei
 	}
 
 	if (stupid.getMyElement().equalsIgnoreCase("")) // prfen, ob ein
 							// Element ausgewhlt
 							// wurde
 	{
 	    return 3;
 	}
 
 	// Prfen, ob der Elementenordner existiert
 	File elementDir = new File(this.context.getFilesDir() + "/" + stupid.getMyElement());
 	if (!elementDir.exists())
 	    return 6;
 
 	// prfen, ob daten fr die ausgewhltes Element vorhanden sind
 	// zhlt wie viele Timetables fr die ausgewhlt Klasse vorhanden sind
 	File[] files = elementDir.listFiles();
 
 	if (files.length == 0)
 	    return 7;
 
 	return 0;
     }
     
     /**
      * @author Tobias Janssen
      * Fhrt die Laufzeitprfung durch, und ergreift ntige Mabahmen im Fehlerfall
      * 
      */
     public void selfCheck()
     {
 	selfCheck(0);
     }
 
 
     /**
      * @author Tobias Janssen
      * Fhrt die Laufzeitprfung durch, und ergreift ntige Mabahmen im Fehlerfall
      * 
      * @param prevErrorCode		Integer der den vorherigen Fehler angibt
      */
     private void selfCheck(int prevErrorCode)
     {
 	selfCheckIsRunning = true;
 	switch (checkStructure())
 	{
 	case 0: // Alles in Ordnung
 	    try
 	    {
 		Tools.loadAllDataFiles(this.context, stupid);
 	    }
 	    catch (Exception e)
 	    {
 		handler.post(new ErrorMessage(this, e.getMessage()));
 	    }
 	    stupid.sort();
 	    initViewPager();
 	    stupid.checkAvailibilityOfWeek(this, Const.THISWEEK);
 	    break;
 
 	case 1: // FILEELEMENT Datei fehlt
 	    if (prevErrorCode == 1) // prfen, wie oft dieser vorgung bereit
 				    // durchgefhrt wurde
 	    {
 		// bereits das zweite Mal, dass der Fehler Auftritt
 		// TODO: was sind die Grnde dafr, dass die Datei nun immer
 		// noch nicht existiert?
 	    }
 	    else
 	    {
 		// Selectoren aus dem Netz laden
 		Tools.fetchOnlineSelectors(this, null);
 		Tools.saveElements(this, new Runnable()
 		{
 		    @Override
 		    public void run()
 		    {
 			selfCheck(1);
 		    }
 		}, true);
 	    }
 	    break;
 	case 3: // Keine Klasse ausgewhlt
 	    OnClickListener onClick = new OnClickListener()
 	    {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which)
 		{
 		    Tools.gotoSetup(MyContext.this, Const.FIRSTSTART, true);
 		}
 	    };
 	    String message = "";
 	    if (this.newVersionInfo)
 	    {
 		message = this.context.getString(R.string.app_newVersion_msg);
 	    }
 	    else
 	    {
 		message = "Es ist noch kein Element festlegt!\nBitte whlen Sie in den Einstellungen Ihr Element aus!";
 	    }
 	    handler.post(new ErrorMessage(this, message, onClick, "Einstellungen ffnen"));
 	    break;
 	case 6: // Elementenordner existiert nicht
 		// neuen anlegen
 	    java.io.File elementDir = new java.io.File(this.context.getFilesDir(), stupid.getMyElement());
 	    elementDir.mkdir();
 	    initViewPager();
 	    stupid.checkAvailibilityOfWeek(this, Const.THISWEEK);
 	    break;
 	case 7: // Keine Daten fr diese Klasse vorhanden
 	    initViewPager();
 	    stupid.checkAvailibilityOfWeek(this, Const.THISWEEK);
 	    break;
 	}
 	selfCheckIsRunning = false;
     }
 
     /*
      * Datum: 11.10.12
      * 
      * @author Tobias Janssen Initialisiert den viewPager, der die Tage des
      * Stundenplans darstellt
      */
     public void initViewPager()
     {
 	currentPage = 0;
 	for (int i = 0; i < stupid.stupidData.size(); i++)
 	{
 	    handler.post(new AppendPage(stupid.stupidData.get(i), this));
 	    // Tools.appendTimeTableToPager(stupid.stupidData.get(i), stupid,
 	    // this);
 
 	}
 	handler.post(new SetupPager(this, pageIndex));
     }
 
 }
