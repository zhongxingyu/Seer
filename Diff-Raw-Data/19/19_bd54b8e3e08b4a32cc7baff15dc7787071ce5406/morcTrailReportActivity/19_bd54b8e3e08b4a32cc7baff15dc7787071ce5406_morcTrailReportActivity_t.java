 package org.dsanderson.morctrailreport;
 
 import org.dsanderson.morctrailreport.R;
 import org.dsanderson.morctrailreport.AboutActivity;
 import org.dsanderson.morctrailreport.parser.MorcFactory;
 import org.dsanderson.morctrailreport.parser.MorcSpecificTrailInfo;
 
 import org.dsanderson.xctrailreport.application.ReportListCreator;
 import org.dsanderson.xctrailreport.core.ISourceSpecificTrailInfo;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.core.android.LoadReportsTask;
 import org.dsanderson.xctrailreport.core.android.TrailReportList;
 import org.dsanderson.xctrailreport.core.android.TrailReportPrinter;
 import org.dsanderson.android.util.AndroidIntent;
 import org.dsanderson.android.util.AndroidProgressBar;
 import org.dsanderson.android.util.Dialog;
 import org.dsanderson.android.util.Maps;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class morcTrailReportActivity extends ListActivity {
 
 	private TrailReportList trailReports;
 	private TrailReportFactory factory = new TrailReportFactory(this);
 	ReportListCreator listCreator = new ReportListCreator(factory);
 	private String appName;
 	private TrailReportPrinter printer;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		appName = getString(R.string.app_name);
 		registerForContextMenu(getListView());
 
 		trailReports = (TrailReportList) factory.getTrailReportList();
 		printer = new TrailReportPrinter(this, factory, trailReports, appName,
 				ListEntryFactory.getInstance());
 		factory.getUserSettingsSource().loadUserSettings();
 
 		if (savedInstanceState == null)
 			refresh(false);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		TrailReport report = getObjectFromMenuItemInfo((AdapterView.AdapterContextMenuInfo) menuInfo);
 		if (report != null) {
 			menu.add(Menu.NONE, R.id.openMapMenu, Menu.NONE, "Open Map");
 			ISourceSpecificTrailInfo morcInfo = report.getTrailInfo()
 					.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 
 			if (morcInfo != null) {
 
 				String allReportUrl = ((MorcSpecificTrailInfo) morcInfo)
 						.getAllTrailReportUrl();
 
 				if (allReportUrl != null && allReportUrl.length() != 0) {
 					menu.add(Menu.NONE, R.id.viewAllReports, Menu.NONE,
 							"View All Reports");
 				}
 
 				if (morcInfo.getComposeUrl() != null
 						&& morcInfo.getComposeUrl().length() != 0) {
 					menu.add(Menu.NONE, R.id.composeReportItem, Menu.NONE,
 							"Compose Report");
 				}
 
 				if (morcInfo.getTrailInfoUrl() != null
 						&& morcInfo.getTrailInfoUrl().length() != 0) {
 					menu.add(Menu.NONE, R.id.trailInfoMenu, Menu.NONE,
 							"Trail Info");
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		TrailReport trailReport = getObjectFromMenuItemInfo((AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo());
 
 		if (trailReport != null) {
 			TrailInfo info = trailReport.getTrailInfo();
 
 			switch (item.getItemId()) {
 			case R.id.viewAllReports: {
 				ISourceSpecificTrailInfo sourceSpecific = info
 						.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 				if (sourceSpecific != null) {
 					String viewAllReportUrl = ((MorcSpecificTrailInfo) sourceSpecific)
 							.getAllTrailReportUrl();
 					if (viewAllReportUrl != null
 							&& viewAllReportUrl.length() > 0)
 						openAllReports(trailReport.getTrailInfo());
 				}
 			}
 				return true;
 
 			case R.id.trailInfoMenu: {
 				ISourceSpecificTrailInfo sourceSpecific = info
 						.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 				if (sourceSpecific != null) {
 					String trailInfoUrl = sourceSpecific.getTrailInfoUrl();
 					if (trailInfoUrl != null && trailInfoUrl.length() > 0)
 						AndroidIntent.launchIntent(trailInfoUrl, this);
 				}
 			}
 				return true;
 			case R.id.openMapMenu:
 				Maps.launchMap(info.getLocation(), info.getName(),
 						info.getSpecificLocation(), this);
 				return true;
 			case R.id.composeReportItem: {
 				ISourceSpecificTrailInfo sourceSpecific = info
 						.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 				if (sourceSpecific != null) {
 					String composeUrl = sourceSpecific.getComposeUrl();
 					if (composeUrl != null && composeUrl.length() > 0)
 						AndroidIntent.launchIntent(composeUrl, this);
 				}
 			}
 				return true;
 			default:
 				break;
 			}
 		}
 
 		return super.onContextItemSelected(item);
 	}
 
 	private TrailReport getObjectFromMenuItemInfo(AdapterContextMenuInfo info) {
 		if (trailReports.size() > 0) {
 			return (TrailReport) trailReports.getById(info.id);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.preferencesMenuItem:
 			openPreferencesMenu();
 			return true;
 		case R.id.aboutMenuItem:
 			openAbout();
 			return true;
 		case R.id.refresh:
 			refresh(true);
 			return true;
 		case R.id.sortBy:
 			MenuItem distance = item.getSubMenu().getItem(0).setChecked(false);
 			MenuItem date = item.getSubMenu().getItem(1).setChecked(false);
 			MenuItem duration = item.getSubMenu().getItem(2).setChecked(false);
 			MenuItem condition = item.getSubMenu().getItem(3).setChecked(false);
 
 			switch (factory.userSettings.getSortMethod()) {
 			case SORT_BY_DISTANCE:
 				distance.setChecked(true);
 				return true;
 			case SORT_BY_DATE:
 				date.setChecked(true);
 				return true;
 			case SORT_BY_DURATION:
 				duration.setChecked(true);
 				return true;
 			case SORT_BY_CONDITION:
 				condition.setChecked(true);
 				return true;
 			}
 			return false;
 		case R.id.sortByDuration:
 		case R.id.sortByDate:
 		case R.id.sortByDistance:
 		case R.id.sortByCondition:
 
 			String sortMethodString = "";
 
 			switch (item.getItemId()) {
 			case R.id.sortByDuration:
 				sortMethodString = "sortByDuration";
 				break;
 			case R.id.sortByDate:
 				sortMethodString = "sortByDate";
 				break;
 			case R.id.sortByDistance:
 				sortMethodString = "sortByDistance";
 				break;
 			case R.id.sortByCondition:
 				sortMethodString = "sortByCondition";
 				break;
 			}
 
 			Editor edit = PreferenceManager.getDefaultSharedPreferences(
 					getApplication()).edit();
 			edit.putString("sortMethod", sortMethodString);
 			edit.commit();
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	// / Launch Preference activity
 	private void openPreferencesMenu() {
 		Intent i = new Intent(this, PreferencesActivity.class);
 		startActivity(i);
 	}
 
 	// / Launch about menu activity
 	private void openAbout() {
 		Intent i = new Intent(this, AboutActivity.class);
 		startActivity(i);
 	}
 
 	// / Launch all reports activity
 	private void openAllReports(TrailInfo info) {
 		MorcFactory.getInstance().setAllReportsInfo(info);
 		Intent i = new Intent(this, AllReportActivity.class);
 		startActivity(i);
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus && factory.getUserSettings().getRedrawNeeded()) {
 			try {
 				factory.getUserSettings().setRedrawNeeded(false);
 				printer.printTrailReports();
 			} catch (Exception e) {
 				e.printStackTrace();
 				Dialog dialog = new Dialog(this, e);
 				dialog.show();
 			}
 		}
 	}
 
 	// / adding this to prevent rescrolling on orientation changed
 	@Override
 	public void onConfigurationChanged(Configuration config) {
 		super.onConfigurationChanged(config);
 	}
	
	@Override
	protected void onDestroy ()
	{
		trailReports.close();
		super.onDestroy();
	}
 
 	private void refresh(boolean forced) {
 		factory.getUserSettings().setForcedRefresh(forced);
 
 		factory.getLocationSource().updateLocation();
 
 		new LoadReportsTask(this, factory, listCreator, trailReports,
 				factory.getTrailInfoList(), printer, new AndroidProgressBar(
 						this)).execute();
 	}
 
 }
