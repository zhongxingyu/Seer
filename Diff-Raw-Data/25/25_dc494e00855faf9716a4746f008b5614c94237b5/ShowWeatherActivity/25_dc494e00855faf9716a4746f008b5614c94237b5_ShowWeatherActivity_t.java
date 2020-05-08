 package de.juliai.bavariaweather;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import android.widget.Toast;
 import de.juliai.bavariaweather.AsyncTaskCounter.AsyncTaskCounterCallback;
 import de.juliai.bavariaweather.WeatherSettingsActivity.CheckboxPreferenceData;
 
 /**
  * 
  * @author JuliaI
  * 
  */
 public class ShowWeatherActivity extends FragmentActivity {
 
	// TODO add ordering for regions

 	private int numberOfRegions;
 
 	private SharedPreferences sharedPrefs;
 
 	private RegionsPageAdapter regionsPageAdapter;
 
 	private Menu menu;
 
 	private List<Region> regions;
 
 	private List<RegionFragment> regionFragments;
 
 	private List<String> dataKeys;
 
 	private List<String> selectionKeys;
 
 	private List<CheckboxPreferenceData> checkboxPreferenceDatas;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_show_weather);
 
 		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		initRegions();
 
 		final ViewPager regionsViewPager = (ViewPager) findViewById(R.id.regionsViewPager);
 		regionsPageAdapter = new RegionsPageAdapter(
 				getSupportFragmentManager(), this.regionFragments);
 		regionsViewPager.setAdapter(regionsPageAdapter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.show_weather_menu, menu);
 		this.menu = menu;
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.itemRefresh:
 			reloadWeatherInfo(item);
 			return true;
 		case R.id.dropdown_settings:
 			showWeatherSettings();
 			return true;
 		case R.id.dropdown_delete_cache:
 			deleteCache();
 			return true;
 		case R.id.dropdown_about:
 			showAbout();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * initializes the regions and regionFragments.
 	 */
 	private void initRegions() {
 
 		// check version
 		final String KEY_VERSION = "bavariaWeatherVersion";
 		final String CURRENT_VERSION = "2";
 
 		boolean deleteData;
 		final String version = sharedPrefs.getString(KEY_VERSION, "");
 		if (CURRENT_VERSION.equals(version)) {
 			deleteData = false;
 
 		} else {
 			deleteData = true;
 			final Editor prefsEditor = sharedPrefs.edit();
 			prefsEditor.putString(KEY_VERSION, CURRENT_VERSION);
 			prefsEditor.commit();
 		}
 
 		// init regions
 		final List<RegionDo> regionDos = new LinkedList<RegionDo>();
 
 		final RegionDo bavaria = new RegionDo("weatherData_bavaria",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do",
 				"@drawable/wappenbayern", "weatherSetting_sel_bavaria", true,
 				"Bayern");
 		regionDos.add(bavaria);
 
 		final RegionDo bavariaTrend = new RegionDo("weatherData_bavariaTrend",
 				"http://www.br.de/wetter/action/bayernwetter/siebentage.do",
 				"@drawable/wappenbayern", "weatherSetting_sel_bavariaTrend",
 				false, "Wettertrend für Bayern");
 		regionDos.add(bavariaTrend);
 
 		final RegionDo swabia = new RegionDo(
 				"weatherData_swabia",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Schwaben",
 				"@drawable/wappenschwaben", "weatherSetting_sel_swabia", false,
 				"Schwaben");
 		regionDos.add(swabia);
 
 		final RegionDo upperBavaria = new RegionDo(
 				"weatherData_upperBavaria",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Oberbayern",
 				"@drawable/wappenoberbayern",
 				"weatherSetting_sel_upperBavaria", false, "Oberbayern");
 		regionDos.add(upperBavaria);
 
 		final RegionDo lowerBavaria = new RegionDo(
 				"weatherData_lowerBavaria",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Niederbayern",
 				"@drawable/wappenniederbayern",
 				"weatherSetting_sel_lowerBavaria", false, "Niederbayern");
 		regionDos.add(lowerBavaria);
 
 		final RegionDo upperPalatinate = new RegionDo(
 				"weatherData_upperPalatinate",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Oberpfalz",
 				"@drawable/wappenoberpfalz",
 				"weatherSetting_sel_upperPalatinate", false, "Oberpfalz");
 		regionDos.add(upperPalatinate);
 
 		final RegionDo middleFranconia = new RegionDo(
 				"weatherData_middleFranconia",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Mittelfranken",
 				"@drawable/wappenmittelfranken",
 				"weatherSetting_sel_middleFranconia", false, "Mittelfranken");
 		regionDos.add(middleFranconia);
 
 		final RegionDo upperFranconia = new RegionDo(
 				"weatherData_upperFranconia",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Oberfranken",
 				"@drawable/wappenoberfranken",
 				"weatherSetting_sel_upperFranconia", false, "Oberfranken");
 		regionDos.add(upperFranconia);
 
 		final RegionDo lowerFranconia = new RegionDo(
 				"weatherData_lowerFranconia",
 				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Unterfranken",
 				"@drawable/wappenunterfranken",
 				"weatherSetting_sel_lowerFranconia", false, "Unterfranken");
 		regionDos.add(lowerFranconia);
 
 		final RegionDo mountainWeather = new RegionDo(
 				"weatherData_mountainWeather",
 				"http://www.br.de/wetter/action/reisewetter/bergwetter.do",
 				"@drawable/wappenbayern", "weatherSetting_sel_mountainWeather",
 				false, "Bergwetter");
 		regionDos.add(mountainWeather);
 
 		final RegionDo winterWeather = new RegionDo(
 				"weatherData_winterWeather",
 				"http://www.br.de/wetter/action/reisewetter/wintersport.do",
 				"@drawable/wappenbayern", "weatherSetting_sel_winterWeather",
 				false, "Wintersport-Bericht");
 		regionDos.add(winterWeather);
 
 		numberOfRegions = regionDos.size();
 
 		regions = new ArrayList<Region>(numberOfRegions);
 		regionFragments = new ArrayList<RegionFragment>(numberOfRegions);
 		dataKeys = new LinkedList<String>();
 		selectionKeys = new LinkedList<String>();
 		checkboxPreferenceDatas = new ArrayList<CheckboxPreferenceData>();
 
 		for (final RegionDo regionDo : regionDos) {
 			dataKeys.add(regionDo.getDataKey());
 			selectionKeys.add(regionDo.getSelectionKey());
 
 			String weatherData = "";
 			if (!deleteData) {
 				weatherData = sharedPrefs.getString(regionDo.getDataKey(), "");
 			}
 
 			final RegionFragment fragment = RegionFragment
 					.newInstance(weatherData);
 
 			final boolean selection = sharedPrefs.getBoolean(
 					regionDo.getSelectionKey(), true);
 
 			if (selection) {
 				regionFragments.add(fragment);
 			}
 
 			regions.add(new Region(selection, regionDo.getSelectionKey(),
 					regionDo.getUrl(), regionDo.getDataKey(), regionDo
 							.getCrestDrawableName(), fragment));
 
 			checkboxPreferenceDatas.add(new CheckboxPreferenceData(regionDo
 					.getSelectionKey(), regionDo.getSettingsTitle(), regionDo
 					.isDefaultActive()));
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void reloadWeatherInfo() {
 		final MenuItem item = menu.findItem(R.id.itemRefresh);
 		reloadWeatherInfo(item);
 	}
 
 	/**
 	 * loads the weather-data from the internet and stores it in the shared
 	 * preferences.
 	 */
 	private void reloadWeatherInfo(final MenuItem item) {
 		// start refresh-icon-spinner
 		item.setActionView(R.layout.action_progressbar);
 		item.expandActionView();
 
 		int numberOfActiveRegions = 0;
 		for (final Region region : regions) {
 			if (region.isActive()) {
 				numberOfActiveRegions++;
 			}
 		}
 
 		final AsyncTaskCounter counter = new AsyncTaskCounter(
 				numberOfActiveRegions, new AsyncTaskCounterCallback() {
 					@Override
 					public void onFinishedAllTasks() {
 						// stop refresh-icon-spinner
 						item.collapseActionView();
 						item.setActionView(null);
 					}
 				});
 
 		// refresh weather data for all active regions
 		for (final Region region : regions) {
 			if (region.isActive()) {
 				final DownloadWeatherDataAsyncTask task = new DownloadWeatherDataAsyncTask(
 						this, sharedPrefs, region, counter);
 				task.execute(new URL[] {});
 			}
 		}
 	}
 
 	/**
 	 * opens the settings
 	 */
 	private void showWeatherSettings() {
 		final Intent intent = new Intent();
 		intent.setClass(ShowWeatherActivity.this, WeatherSettingsActivity.class);
 		intent.putExtra(WeatherSettingsActivity.KEY_INTENT_EXTRAS,
 				new WeatherSettingsActivity.IntentExtras(
 						checkboxPreferenceDatas));
 		startActivityForResult(intent, 0);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// after user closed the settings:
 		regionFragments = new ArrayList<RegionFragment>(numberOfRegions);
 
 		for (final String selectionKey : selectionKeys) {
 			final boolean selection = sharedPrefs.getBoolean(selectionKey,
 					false);
 
 			for (final Region region : regions) {
 				if (region.getPreferencesKeySelection().equals(selectionKey)) {
 					region.setActive(selection);
 
 					if (selection) {
 						regionFragments.add(region.getFragment());
 					}
 				}
 			}
 		}
 
		// TODO something is still wrong about this
 		regionsPageAdapter.updateData(regionFragments);
 		regionsPageAdapter.notifyDataSetChanged();
 
 		reloadWeatherInfo();
 	}
 
 	/**
 	 * deletes all the weather-data
 	 */
 	private void deleteCache() {
 		final Editor prefsEditor = sharedPrefs.edit();
 		for (final String dataKey : dataKeys) {
 			prefsEditor.remove(dataKey);
 		}
 		prefsEditor.commit();
 
 		for (final Region region : regions) {
 			region.changeWeatherData("");
 		}
 
 		Toast.makeText(this, "Der Cache wurde gelöscht", Toast.LENGTH_LONG)
 				.show();
 	}
 
 	/**
 	 * shows the about-activity
 	 */
 	private void showAbout() {
 		final Intent intent = new Intent();
 		intent.setClass(ShowWeatherActivity.this, AboutActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * 
 	 * @author JuliaI
 	 * 
 	 */
 	private static class RegionDo {
 
 		private final String dataKey;
 
 		private final String url;
 
 		private final String crestDrawableName;
 
 		private final String selectionKey;
 
 		private final boolean defaultActive;
 
 		private final String settingsTitle;
 
 		public RegionDo(String dataKey, String url, String crestDrawableName,
 				String selectionKey, boolean defaultActive, String settingsTitle) {
 			this.dataKey = dataKey;
 			this.url = url;
 			this.crestDrawableName = crestDrawableName;
 			this.selectionKey = selectionKey;
 			this.defaultActive = defaultActive;
 			this.settingsTitle = settingsTitle;
 		}
 
 		public String getDataKey() {
 			return dataKey;
 		}
 
 		public String getUrl() {
 			return url;
 		}
 
 		public String getCrestDrawableName() {
 			return crestDrawableName;
 		}
 
 		public String getSelectionKey() {
 			return selectionKey;
 		}
 
 		public boolean isDefaultActive() {
 			return defaultActive;
 		}
 
 		public String getSettingsTitle() {
 			return settingsTitle;
 		}
 	}
 
 	/**
 	 * 
 	 * @author JuliaI
 	 * 
 	 */
 	private class RegionsPageAdapter extends FragmentStatePagerAdapter {
 
 		private List<RegionFragment> fragments;
 
 		public RegionsPageAdapter(FragmentManager fm,
 				List<RegionFragment> fragments) {
 			super(fm);
 			this.fragments = fragments;
 		}
 
 		/**
 		 * @param newFragments
 		 */
 		public void updateData(List<RegionFragment> newFragments) {
 			this.fragments = newFragments;
 		}
 
 		@Override
 		public int getCount() {
 			return this.fragments.size();
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			return this.fragments.get(position);
 		}
 
 		@Override
 		public int getItemPosition(Object item) {
 			return POSITION_NONE;
 		}
 
 		@Override
 		public void destroyItem(ViewGroup container, int position, Object object) {
 			try {
 				super.destroyItem(container, position, object);
 			} catch (IndexOutOfBoundsException e) {
 				// seems to be already gone...
 			}
 		}
 	}
 }
