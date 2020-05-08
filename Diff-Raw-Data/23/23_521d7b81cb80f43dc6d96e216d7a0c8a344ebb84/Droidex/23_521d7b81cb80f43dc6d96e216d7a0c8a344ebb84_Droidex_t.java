 package org.droidex;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.droidex.adapters.MenuListAdapter;
 import org.droidex.adapters.MealListAdapter;
 import org.droidex.os.AsyncTaskAware;
 import org.droidex.os.DownloadPlainTextTask;
 import org.droidex.util.L;
 import org.droidex.util.MenuParser;
 import org.droidex.util.NameOrderedDict;
 import org.droidex.util.text.Meal;
 import org.droidex.util.text.WeekDay;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 public class Droidex
 	extends ListActivity
 	implements AsyncTaskAware<Void, byte[]>
 {
 	private static final String MENU_FEED_URL = "http://www.pcasc.usp.br/restaurante.xml";
 	private static final String MENU_XML_ENCODING = "ISO-8859-1";
 
 	private enum Screen { MENU, DAY_MENU }
 
 	private MenuListAdapter adapter;
 	private NameOrderedDict menu;
 
 	private ProgressDialog loadingDialog;
 
 	private Screen currentScreen;
 	private int selectedDay;
 	private Map<Screen, View> screenLayouts;
 
 	private SharedPreferences prefs;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 		
 		prefs = getPreferences(Context.MODE_PRIVATE);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.main);
 		currentScreen = Screen.MENU;
 		screenLayouts = new HashMap<Screen, View>();
 		screenLayouts.put(Screen.MENU, findViewById(R.id.menu_rootLayout));
 		screenLayouts.put(Screen.DAY_MENU, findViewById(R.id.dayMenu_rootLayout));
 
 		try {
 			int savedWeekOfYear = prefs.getInt("week_of_year", -1);
 			int todayWeekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
 
 			if (savedWeekOfYear != todayWeekOfYear) {
 				downloadXML();
 			} else {
 				parseMenu();
 				showMenu();
 			}
 		} catch (Exception e) {
 			hideLoading();
 			L.e("exc", e);
 			showError(R.string.err_exception);
 			e.printStackTrace();
 		}
     }
 
 	public void onTaskPreExecute() {
 		throw new UnsupportedOperationException();
 	}
 
 	public void onTaskProgressUpdate(Void... values) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void onTaskCancelled(byte[] result) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void onTaskCancelled() {
 		throw new UnsupportedOperationException();
 	}
 
 	public void onTaskPostExecute(byte[] buffer)
 	{
 		hideLoading();
 
		if (buffer == null) {
			showError(R.string.err_network);
			return;
		}

 		String xml = "";
 
 		try {
 			xml = new String(buffer, MENU_XML_ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			showError(R.string.err_exception);
 			e.printStackTrace();
 			L.e("exc", e);
 		}
 
 		if ("".equals(xml)) {
 			showError(R.string.err_network);
 			return;
 		}
 
 		saveXML(xml);
 		parseMenu();
 		showMenu();
 	}
 
	public void onBackPressed()
 	{
 		L.d("backPressed");
 		switch(currentScreen) {
 			case MENU:
 				L.d("finish()");
 				finish();
 				break;
 			case DAY_MENU:
 				switchToScreen(Screen.MENU);
 				break;
 		}
 	}
 
	protected void onListItemClick(ListView l, View v, int pos, long id)
	{
		L.d("listItemClick " + pos);
		selectedDay = pos;
		switchToScreen(Screen.DAY_MENU);
	}

 	private void downloadXML()
 		throws MalformedURLException
 	{
 		L.d("starting asynctask...");
 		showLoading();
 		URL xmlUrl = new URL(MENU_FEED_URL);
 		new DownloadPlainTextTask(this).execute(xmlUrl);
 	}
 
 	private void saveXML(String xml)
 	{
 		L.d("saving data...");
 		SharedPreferences.Editor editor = prefs.edit();
 		int woy = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
 		editor.putString("xml_menu", xml);
 		editor.putInt("week_of_year", woy);
 		L.d("WoY: " + woy);
 		editor.commit();
 	}
 
 	private String readXML()
 	{
 		L.d("reading data");
 		return prefs.getString("xml_menu", "");
 	}
 
 	private void parseMenu()
 	{
 		L.d("parsing menu");
 		try {
 			menu = MenuParser.parseXML(readXML());
 		} catch (XmlPullParserException e) {
 			showError(R.string.err_xml);
 			e.printStackTrace();
 			L.e("exc", e);
 		} catch (Exception e) {
 			showError(R.string.err_exception);
 			e.printStackTrace();
 			L.e("exc", e);
 		}
 	}
 
 	private void switchToScreen(Screen scr)
 	{
 		L.d("switchToScreen(" + scr + ")");
 		if (scr == Screen.DAY_MENU)
 			showDayMenu();
 
 		for (View v : screenLayouts.values())
 			v.setVisibility(View.GONE);
 		screenLayouts.get(scr).setVisibility(View.VISIBLE);
 
 		currentScreen = scr;
 	}
 
 	private void showMenu()
 	{
 		L.d("showMenu()");
 		adapter = new MenuListAdapter(this, R.layout.menu_list_item, menu);
 		setListAdapter(adapter);
 	}
 
 	private void showDayMenu()
 	{
 		L.d("showDayMenu()");
 		WeekDay weekDay = (WeekDay) adapter.getItem(selectedDay);
 		NameOrderedDict<Meal> dayMenu = adapter.getDayMenu(selectedDay);
 
 		TextView dayLabel = (TextView) findViewById(R.id.dayMenu_labelText);
 		dayLabel.setText(weekDay.getDisplayName());
 		TextView dayDate = (TextView) findViewById(R.id.dayMenu_dateText);
 		dayDate.setText(weekDay.getDate());
 		ListView mealList = (ListView) findViewById(R.id.dayMenu_mealList);
 		mealList.setAdapter(new MealListAdapter(this, R.layout.meal_list_item, R.layout.dish_list_item, dayMenu));
 	}
 
 	private void showLoading()
 	{
 		L.d("showLoading()");
 		loadingDialog = ProgressDialog.show(
 							Droidex.this,
 							getResources().getText(R.string.loading_title),
 							getResources().getText(R.string.loading),
 							true
 						);
 	}
 
 	private void hideLoading()
 	{
 		L.d("hideLoading()");
 		if ((loadingDialog != null) && (loadingDialog.isShowing()))
 			loadingDialog.dismiss();
 	}
 
 	private void showError(int stringId)
 	{
 		L.d("showError()");
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getResources().getText(stringId))
 			.setCancelable(false)
 			.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					Droidex.this.finish();
 				}
 			});
 		AlertDialog alert = builder.show();
 	}
 }
