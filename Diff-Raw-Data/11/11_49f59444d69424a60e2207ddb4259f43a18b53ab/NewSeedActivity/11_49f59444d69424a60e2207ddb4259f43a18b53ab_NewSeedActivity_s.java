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
 
 import java.util.ArrayList;
 
 import org.gots.R;
 import org.gots.action.bean.BuyingAction;
 import org.gots.help.HelpUriBuilder;
 import org.gots.seed.BaseSeedInterface;
 import org.gots.seed.GrowingSeed;
 import org.gots.seed.GrowingSeedInterface;
 import org.gots.seed.adapter.ListSpeciesAdapter;
 import org.gots.seed.adapter.PlanningHarvestAdapter;
 import org.gots.seed.adapter.PlanningSowAdapter;
 import org.gots.seed.sql.VendorSeedDBHelper;
 import org.gots.seed.view.PlanningWidget;
 import org.gots.seed.view.SeedWidgetLong;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.Gallery;
 import android.widget.GridView;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class NewSeedActivity extends SherlockActivity implements OnClickListener {
 	private static final String SELECTED_SPECIE = "selectedSpecie";
 	private View currentView;
 	private PlanningWidget planningSow;
 	private PlanningWidget planningHarvest;
 	private AutoCompleteTextView autoCompleteVariety;
 	// private AutoCompleteTextView autoCompleteSpecie;
 	private Gallery gallerySpecies;
 	private SeedWidgetLong seedWidgetLong;
 	private BaseSeedInterface newSeed;
 	private TextView textViewBarCode;
 	private boolean isNewSeed = true;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.inputseed);
 
 		ActionBar bar = getSupportActionBar();
 		bar.setDisplayHomeAsUpEnabled(true);
 		bar.setTitle(R.string.seed_register_title);
 
 		findViewById(R.id.imageBarCode).setOnClickListener(this);
 
 		findViewById(R.id.buttonStock).setOnClickListener(this);
 		findViewById(R.id.buttonCatalogue).setOnClickListener(this);
 
 		textViewBarCode = (TextView) findViewById(R.id.textViewBarCode);
 
 		if (getIntent().getIntExtra("org.gots.seedid", -1) != -1) {
 			VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
 			newSeed = helper.getSeedById(getIntent().getIntExtra("org.gots.seedid", -1));
 			isNewSeed = false;
 
 		} else {
 			newSeed = new GrowingSeed();
 		}
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (gallerySpecies != null)
 			outState.putInt(SELECTED_SPECIE, gallerySpecies.getSelectedItemPosition());
 	}
 
 	private void initview() {
 
 		/*
 		 * PLANNING
 		 */
 		planningSow = (PlanningWidget) findViewById(R.id.IdSeedEditSowingPlanning);
 		planningSow.setAdapter(new PlanningSowAdapter(newSeed));
 		planningSow.setEditable(true);
 
 		Button validateSowing = (Button) findViewById(R.id.buttonUpdateSeed);
 
 		validateSowing.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (planningSow.getSelectedMonth().size() > 0) {
 					newSeed.setDateSowingMin(planningSow.getSelectedMonth().get(0));
 					newSeed.setDateSowingMax(planningSow.getSelectedMonth().get(
 							planningSow.getSelectedMonth().size() - 1));
 
 					ArrayList<Integer> harvestMonth = planningHarvest.getSelectedMonth();
 					if (harvestMonth.size() == 0) {
 						Toast.makeText(getApplicationContext(), "Please select month to harvest", 3000).show();
 						return;
 					}
 
 					int durationmin = harvestMonth.get(0) - newSeed.getDateSowingMin();
 					newSeed.setDurationMin(durationmin * 30);
 
 					int durationmax = harvestMonth.get(harvestMonth.size() - 1) - newSeed.getDateSowingMax();
 					newSeed.setDurationMax(durationmax * 30);
 
 					seedWidgetLong.setSeed(newSeed);
 					seedWidgetLong.invalidate();
 				}
 			}
 		});
 
 		planningHarvest = (PlanningWidget) findViewById(R.id.IdSeedEditHarvestPlanning);
 		planningHarvest.setAdapter(new PlanningHarvestAdapter(newSeed));
 		planningHarvest.setEditable(true);
 
 		seedWidgetLong = (SeedWidgetLong) findViewById(R.id.idSeedWidgetLong);
 
 		/*
 		 * VARIETIES
 		 */
 		autoCompleteVariety = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewVariety);
 		initVarietyList();
 		autoCompleteVariety.setText(newSeed.getVariety());
 		autoCompleteVariety.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				initVarietyList();
 				if (autoCompleteVariety != null)
 					autoCompleteVariety.showDropDown();
 			}
 		});
 
 		ImageButton clearVariety = (ImageButton) findViewById(R.id.buttonClearVariety);
 		clearVariety.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				autoCompleteVariety.setText("");
 			}
 		});
 
 		/*
 		 * SPECIES
 		 */
 		gallerySpecies = (Gallery) findViewById(R.id.layoutSpecieGallery);
 		initSpecieList();
 
 		/*
 		 * BARCODE
 		 */
 		textViewBarCode.setText(newSeed.getBareCode());
 
 		// if (savedInstanceState != null &&
 		// savedInstanceState.getInt(SELECTED_SPECIE) != 0)
 		// gallerySpecies.setSelection(savedInstanceState.getInt(SELECTED_SPECIE));
 
 	}
 
 	// private void showDropdown() {
 	// autoCompleteSpecie.showDropDown();
 	// }
 
 	@Override
 	public void onClick(View v) {
 		currentView = v;
 		switch (v.getId()) {
 		case R.id.imageBarCode:
 			scanBarCode();
 			break;
 
 		case R.id.buttonStock:
 			if (validateSeed()) {
 				long seedId = insertSeed();
 				addToStock(seedId);
 				finish();
 			}
 			break;
 		case R.id.buttonCatalogue:
 			if (validateSeed()) {
 				insertSeed();
 				finish();
 			}
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	private void addToStock(long seedId) {
 		VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
 		if (seedId >= 0) {
 			GrowingSeedInterface seed = (GrowingSeedInterface) helper.getSeedById((int) seedId);
 			BuyingAction buy = new BuyingAction(this);
 			buy.execute(seed);
 		}
 
 	}
 
 	private boolean validateSeed() {
 		boolean isValidate = true;
 
 		if (newSeed.getVariety() == null || "".equals(newSeed.getVariety()))
 			isValidate = false;
 		if (newSeed.getFamily() == null || "".equals(newSeed.getFamily()))
 			isValidate = false;
 		if (newSeed.getDateSowingMin() == -1 || newSeed.getDateSowingMax() == -1)
 			isValidate = false;
 		if (!isValidate) {
 			Toast.makeText(this, "All fields should be defined", 3000).show();
 		}
 
 		return isValidate;
 	}
 
 	private long insertSeed() {
 
 		VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
 		if (isNewSeed)
 
 			return helper.insertSeed(newSeed);
 		else
 			return helper.updateSeed(newSeed);
 	}
 
 	/**
 	 * 
 	 */
 	private void initSpecieList() {
 		final VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
 		String[] specieList = helper.getArraySpecie();
 
 		ListSpeciesAdapter listSpeciesAdapter = new ListSpeciesAdapter(this, specieList, newSeed.getSpecie());
 
 		gallerySpecies.setAdapter(listSpeciesAdapter);
 		gallerySpecies.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				gallerySpecies.dispatchSetSelected(false);
 				arg1.setSelected(!arg1.isSelected());
 				newSeed.setSpecie((String) arg1.getTag());
 				String family = helper.getFamilyBySpecie(newSeed.getSpecie());
 				newSeed.setFamily(family);
 				seedWidgetLong.setSeed(newSeed);
 				seedWidgetLong.invalidate();
 				// arg1.setBackgroundColor(getResources().getColor(R.color.action_warning_color));
 			}
 		});
 
 	}
 
 	/**
 	 * 
 	 */
 	private void initVarietyList() {
 		VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
 
 		String[] referenceList = null;
 		if (newSeed.getSpecie() != null)
 			referenceList = helper.getArrayVarietyBySpecie(newSeed.getSpecie());
 		else
 			referenceList = helper.getArrayVariety();
 
 		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, referenceList);
 		autoCompleteVariety.setAdapter(adapter);
 		autoCompleteVariety.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				String variety = autoCompleteVariety.getText().toString();
 				newSeed.setVariety(variety);
 				seedWidgetLong.setSeed(newSeed);
 				seedWidgetLong.invalidate();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 
 		autoCompleteVariety.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				String variety = adapter.getItem(arg2);
 				newSeed.setVariety(variety);
 				seedWidgetLong.setSeed(newSeed);
 				seedWidgetLong.invalidate();
 
 			}
 		});
 		autoCompleteVariety.invalidate();
 	}
 
 	private void scanBarCode() {
 		IntentIntegrator integrator = new IntentIntegrator(this);
 		integrator.initiateScan();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
 		if (scanResult != null && scanResult.getContents() != "") {
 			Log.i("Scan result", scanResult.toString());
 			textViewBarCode.setText(scanResult.getContents());
 			newSeed.setBareCode(textViewBarCode.getText().toString());
			seedWidgetLong.setSeed(newSeed);
			seedWidgetLong.invalidate();
 		}
 		// super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu_newseed, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 
 		case R.id.help:
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HelpUriBuilder.getUri(getClass()
 					.getSimpleName())));
 			startActivity(browserIntent);
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 
 		super.onConfigurationChanged(newConfig);
		seedWidgetLong.setSeed(newSeed);
		seedWidgetLong.invalidate();
 	}
 
 	@Override
 	protected void onResume() {
 		initview();
 
 		autoCompleteVariety.clearFocus();
 		gallerySpecies.post(new Runnable() {
 			public void run() {
 				gallerySpecies.requestFocus();
 
 			}
 		});
 		super.onResume();
 	}
 }
