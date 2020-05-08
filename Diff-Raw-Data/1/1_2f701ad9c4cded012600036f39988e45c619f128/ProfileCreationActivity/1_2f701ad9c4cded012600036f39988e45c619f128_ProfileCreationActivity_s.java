 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.ui;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.gots.R;
 import org.gots.action.BaseActionInterface;
 import org.gots.action.GardeningActionInterface;
 import org.gots.action.sql.ActionDBHelper;
 import org.gots.action.sql.ActionSeedDBHelper;
 import org.gots.allotment.sql.AllotmentDBHelper;
 import org.gots.analytics.GotsAnalytics;
 import org.gots.bean.Allotment;
 import org.gots.bean.BaseAllotmentInterface;
 import org.gots.bean.Garden;
 import org.gots.garden.GardenInterface;
 import org.gots.garden.GardenManager;
 import org.gots.help.HelpUriBuilder;
 import org.gots.seed.GrowingSeedInterface;
 import org.gots.seed.sql.VendorSeedDBHelper;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class ProfileCreationActivity extends SherlockActivity implements LocationListener, OnClickListener {
 	public static final int OPTION_EDIT = 1;
 	private LocationManager mlocManager;
 	private Location location;
 	private Address address;
 	private String tag = "ProfileActivity";
 	// EditText locality;
 	private String choix_source = "";
 	private ProgressDialog pd;
 	private int gardenId;
 	private GardenManager gardenManager;
 	GardenInterface garden = new Garden();
 	private int mode = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (getIntent().getExtras() != null)
 			mode = getIntent().getExtras().getInt("option");
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.profilecreation);
 
 		ActionBar bar = getSupportActionBar();
 		bar.setDisplayHomeAsUpEnabled(false);
 		bar.setTitle(R.string.profile_menu_localize);
 		// bar.setDisplayShowCustomEnabled(true);
 
 		// getSupportActionBar().setIcon(R.drawable.bt_update);
 
 		GotsAnalytics.getInstance(getApplication()).incrementActivityCount();
 		GoogleAnalyticsTracker.getInstance().trackPageView(getClass().getSimpleName());
 
 		garden.setLocality("");
 
 		buildProfile();
 
 	}
 
 	private void buildProfile() {
 
 		findViewById(R.id.buttonValidatePosition).setOnClickListener(this);
 
 		// findViewById(R.id.buttonAddGarden).setOnClickListener(this);
 		gardenManager = new GardenManager(this);
 		if (gardenManager.getcurrentGarden() != null)
 			((CheckBox) findViewById(R.id.checkboxSamples)).setChecked(false);
 
 		mlocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
 		if (mode == OPTION_EDIT)
 			((TextView) findViewById(R.id.editTextLocality)).setText(gardenManager.getcurrentGarden().getLocality());
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 	
 		if (mode != OPTION_EDIT)
 			getPosition();
 	}
 
 	private void getPosition() {
 		setProgressBarIndeterminateVisibility(true);
 
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		
 		pd = ProgressDialog.show(this, "", getResources().getString(R.string.gots_loading), false);
 		pd.setCanceledOnTouchOutside(true);
 
 		String bestProvider = mlocManager.getBestProvider(criteria, true);
 
 		mlocManager.requestLocationUpdates(bestProvider, 60000, 0, this);
 
 	}
 
 	private void displayAddress() {
 
 		// Le geocoder permet de récupérer ou chercher des adresses
 		// gràce à un mot clé ou une position
 		Geocoder geo = new Geocoder(ProfileCreationActivity.this);
 		try {
 			// Ici on récupère la premiere adresse trouvé gràce à la
 			// position
 			// que l'on a récupéré
 			List<Address> adresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 
 			if (adresses != null && adresses.size() == 1) {
 				address = adresses.get(0);
 				TextView location = (TextView) findViewById(R.id.editTextLocality);
 				
 				if ("".equals(location.getText().toString()))
 					location.setHint(String.format("%s", address.getLocality()));
 				else
 					location.setText(String.format("%s", address.getLocality()));
				Log.i("address", address.getLocality());
 			} else {
 				// sinon on affiche un message d'erreur
 				((TextView) findViewById(R.id.editTextLocality)).setHint("L'adresse n'a pu être déterminée");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			((TextView) findViewById(R.id.editTextLocality)).setHint("L'adresse n'a pu être déterminée");
 		}
 		// on stop le cercle de chargement
 		setProgressBarIndeterminateVisibility(false);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 
 		setProgressBarIndeterminateVisibility(false);
 		this.location = location;
 		displayAddress();
 		pd.dismiss();
 		mlocManager.removeUpdates(this);
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		/* this is called if/when the GPS is disabled in settings */
 		Log.v(tag, "Disabled");
 
 		/* bring up the GPS settings */
 		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Log.v(tag, "Enabled");
 		Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		switch (status) {
 		case LocationProvider.OUT_OF_SERVICE:
 			Log.v(tag, "Status Changed: Out of Service");
 			Toast.makeText(this, "Status Changed: Out of Service", Toast.LENGTH_SHORT).show();
 			break;
 		case LocationProvider.TEMPORARILY_UNAVAILABLE:
 			Log.v(tag, "Status Changed: Temporarily Unavailable");
 			Toast.makeText(this, "Status Changed: Temporarily Unavailable", Toast.LENGTH_SHORT).show();
 			break;
 		case LocationProvider.AVAILABLE:
 			Log.v(tag, "Status Changed: Available");
 			Toast.makeText(this, "Status Changed: Available", Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		// mlocManager.removeUpdates(this);
 		super.onPause();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 
 		case R.id.buttonValidatePosition:
 			if (mode == OPTION_EDIT)
 				updateProfile();
 			else
 				createNewProfile();
 			this.finish();
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void updateProfile() {
 
 		String locality = ((TextView) (findViewById(R.id.editTextLocality))).getText().toString();
 
 		if ("".equals(locality))
 			locality = ((TextView) (findViewById(R.id.editTextLocality))).getHint().toString();
 
 		garden = gardenManager.getcurrentGarden();
 		garden.setLocality(locality);
 		gardenManager.updateCurrentGarden(garden);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu_profilecreation, menu);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.help:
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HelpUriBuilder.getUri(getClass()
 					.getSimpleName())));
 			startActivity(browserIntent);
 
 			return true;
 		case R.id.localize_gaden:
 			getPosition();
 			buildProfile();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void createNewProfile() {
 
 		if (location != null) {
 			garden.setGpsLatitude(location.getLatitude());
 			garden.setGpsLongitude(location.getLongitude());
 			garden.setGpsAltitude(location.getAltitude());
 		}
 
 		String locality = ((TextView) (findViewById(R.id.editTextLocality))).getText().toString();
 
 		if ("".equals(locality))
 			locality = ((TextView) (findViewById(R.id.editTextLocality))).getHint().toString();
 
 		garden.setLocality(locality);
 		garden.setCountryName(Locale.getDefault().getDisplayCountry());
 
 		gardenManager.addGarden(garden, true);
 
 		// SAMPLE GARDEN
 		CheckBox samples = (CheckBox) findViewById(R.id.checkboxSamples);
 		if (samples.isChecked()) {
 			GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
 			tracker.trackEvent("Garden", "sample", garden.getLocality(), 0);
 
 			// Allotment
 			BaseAllotmentInterface newAllotment = new Allotment();
 			newAllotment.setName("" + new Random().nextInt());
 
 			AllotmentDBHelper helper = new AllotmentDBHelper(this);
 			helper.insertAllotment(newAllotment);
 
 			// Seed
 			VendorSeedDBHelper seedHelper = new VendorSeedDBHelper(this);
 			// seedHelper.loadFromXML(this);
 			int nbSeed = seedHelper.getArraySeeds().length;
 			Random random = new Random();
 			for (int i = 1; i <= 5 && i < nbSeed; i++) {
 				int alea = random.nextInt(nbSeed);
 
 				GrowingSeedInterface seed = (GrowingSeedInterface) seedHelper.getSeedById(alea % nbSeed + 1);
 				if (seed != null) {
 					seed.setNbSachet(alea % 3 + 1);
 					seedHelper.updateSeed(seed);
 
 					ActionDBHelper actionHelper = new ActionDBHelper(this);
 					BaseActionInterface bakering = actionHelper.getActionByName("beak");
 					GardeningActionInterface sowing = (GardeningActionInterface) actionHelper.getActionByName("sow");
 
 					sowing.execute(newAllotment, seed);
 
 					Calendar cal = new GregorianCalendar();
 					cal.setTime(Calendar.getInstance().getTime());
 					cal.add(Calendar.MONTH, -3);
 					seed.setDateSowing(cal.getTime());
 
 					ActionSeedDBHelper actionsHelper = new ActionSeedDBHelper(this);
 					actionsHelper.insertAction(bakering, seed);
 				}
 			}
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		GotsAnalytics.getInstance(getApplication()).decrementActivityCount();
 		super.onDestroy();
 	}
 }
