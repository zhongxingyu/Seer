 package com.zeyomir.ocfun.gui;
 
 import android.content.Intent;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.method.LinkMovementMethod;
 import android.view.View;
 import android.widget.ImageView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.zeyomir.ocfun.LocationProvider;
 import com.zeyomir.ocfun.LocationUser;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.controller.DisplayCache;
 import com.zeyomir.ocfun.controller.helper.LocationHelper;
 import com.zeyomir.ocfun.dao.InternalResourceMapper;
 import com.zeyomir.ocfun.dao.PreferencesDAO;
 import com.zeyomir.ocfun.model.Cache;
 import org.holoeverywhere.app.Activity;
 import org.holoeverywhere.widget.TextView;
 
 public class SingleCache extends Activity implements LocationUser {
 	private Cache cache;
 	private ActionBar actionBar;
 	private Location cacheLocation;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.cache);
 
 		cache = DisplayCache.getCache(getIntent());
 		actionBar = getSupportActionBar();
 		setActionBarOptions();
 		fillData();
 		((LocationProvider) getApplicationContext())
 				.registerForFrequentlyLocationUpdates(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		((LocationProvider) getApplicationContext())
 				.unregister(this);
 	}
 
 	private void setActionBarOptions() {
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setTitle(R.string.cache);
 		actionBar.setDisplayShowHomeEnabled(true);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setHomeButtonEnabled(true);
 	}
 
 	private void fillData() {
 		((ImageView) findViewById(R.id.type_ico))
 				.setImageResource(InternalResourceMapper.map(cache.type));
 		((TextView) findViewById(R.id.cache_name)).setText(cache.name);
 		((TextView) findViewById(R.id.cache_owner)).setText(cache.owner);
 		String[] coords = cache.coords.split("\\|");
 		cacheLocation = new Location("???");
 		cacheLocation.setLatitude(Double.parseDouble(coords[0]));
 		cacheLocation.setLongitude(Double.parseDouble(coords[1]));
 		double d1 = (Math.round(cacheLocation.getLatitude() * 100000000)) / 100000000.0;
 		double d2 = (Math.round(cacheLocation.getLongitude() * 100000000)) / 100000000.0;
 		((TextView) findViewById(R.id.cache_location)).setText(d1 + ", " + d2);
 		((TextView) findViewById(R.id.cache_description))
 				.setMovementMethod(LinkMovementMethod.getInstance());
 		((TextView) findViewById(R.id.cache_description))
 				.setText(android.text.Html
 						.fromHtml("Opis:" + cache.description));
 
 		if (cache.type != InternalResourceMapper.custom.id()) {
 			((TextView) findViewById(R.id.size)).setText(Cache
 					.getSizeResource(cache.size));
 			((TextView) findViewById(R.id.diff)).setText(cache.difficulty + "");
 			((TextView) findViewById(R.id.last_found))
 					.setText(cache.lastFoundOn);
 			((TextView) findViewById(R.id.terrain)).setText(cache.terrain + "");
 			((TextView) findViewById(R.id.cache_code)).setText(cache.code);
 			if (cache.attributes.length() > 0)
 				((TextView) findViewById(R.id.attributes))
 						.setText("Atrybuty:\n" + cache.attributes);
 			else
 				((TextView) findViewById(R.id.attributes))
 						.setVisibility(View.INVISIBLE);
 			if (cache.hint.length() > 0)
 				((TextView) findViewById(R.id.cache_hint)).setText(DisplayCache
 						.rot13(cache.hint));
 			else {
 				((TextView) findViewById(R.id.cache_hint))
 						.setVisibility(View.INVISIBLE);
 				((TextView) findViewById(R.id.hint_phrase))
 						.setVisibility(View.INVISIBLE);
 			}
 		} else {
 			((TextView) findViewById(R.id.size_phrase))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.size)).setVisibility(View.GONE);
 			((TextView) findViewById(R.id.diff_phrase))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.slash)).setVisibility(View.GONE);
 			((TextView) findViewById(R.id.diff)).setVisibility(View.GONE);
 			((TextView) findViewById(R.id.last_found_phrase))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.last_found))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.terrain))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.cache_code))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.attributes))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.hint_phrase))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.cache_hint))
 					.setVisibility(View.GONE);
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		if (cache.type != InternalResourceMapper.custom.id())
 			inflater.inflate(R.menu.single, menu);
 		else
 			inflater.inflate(R.menu.custom_single, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				i = new Intent(this, OCFun.class);
 				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(i);
 				return true;
 			case R.id.menu_photo:
 				startActivity(DisplayCache.createImagesIntent(this, cache.id));
 				return true;
 			case R.id.menu_logs:
 				startActivity(DisplayCache.createLogsIntent(this, cache.id));
 				return true;
 			case R.id.menu_web:
 				i = new Intent(Intent.ACTION_VIEW, Uri.parse(cache.getUrl(new PreferencesDAO(this).getMobile())));
 				startActivity(i);
 				return true;
 			case R.id.menu_coords:
 				sendCoordsToClipboard();
 				return true;
 			case R.id.menu_map:
 				i = new Intent(DisplayCache.createMapIntent(this, cache.coords));
 				startActivity(i);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void sendCoordsToClipboard() {
 		int sdk = android.os.Build.VERSION.SDK_INT;
 		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
 			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			clipboard.setText(((TextView) findViewById(R.id.cache_location)).getText());
 		} else {
 			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			android.content.ClipData clip = android.content.ClipData.newPlainText("coords", ((TextView) findViewById(R.id.cache_location)).getText());
 			clipboard.setPrimaryClip(clip);
 		}
 	}
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.cache_hint:
 				encodeHint();
 				break;
 		}
 	}
 
 	private void encodeHint() {
 		TextView tv = ((TextView) findViewById(R.id.cache_hint));
 		String s = tv.getText().toString();
 		tv.setText(DisplayCache.rot13(s));
 	}
 
 	@Override
 	public void locationFound(Location l) {
 		String s = LocationHelper.getDistance(l, cacheLocation) + " "
				+ LocationHelper.getAzimuth(l, cacheLocation) + " (b��d: " + l.getAccuracy() + ")";
 		((TextView) findViewById(R.id.distance)).setText(s);
 	}
 }
