 package ua.avtopoisk.activites;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import com.google.inject.Inject;
 import com.googlecode.androidannotations.annotations.*;
 import de.akquinet.android.androlog.Log;
 import parsers.AvtopoiskParserImpl;
 import ua.avtopoisk.BrandsAndRegionsHolder;
 import ua.avtopoisk.R;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 /**
  * Start activity. Search params located here
  *
  * @author ibershadskiy <a href="mailto:iBersh20@gmail.com">Ilya Bershadskiy</a>
  * @since 12.10.12
  */
 @EActivity(R.layout.search)
 @RoboGuice
 public class SearchActivity extends Activity {
     public static final String BRAND_ID_KEY = "brand";
     public static final String MODEL_ID_KEY = "model";
     public static final String REGION_ID_KEY = "region";
     public static final String YEAR_FROM_KEY = "yearFrom";
     public static final String YEAR_TO_KEY = "yearTo";
     public static final String PRICE_FROM_KEY = "priceFrom";
     public static final String PRICE_TO_KEY = "priceTo";
 
     @Bean
     BrandsAndRegionsHolder brandsAndRegionsHolder;
 
     @ViewById(R.id.brands)
     protected Spinner brands;
 
     @ViewById(R.id.models)
     protected Spinner models;
 
     @ViewById(R.id.regions)
     protected Spinner regions;
 
     @ViewById(R.id.year_from)
     protected Spinner yearFrom;
 
     @ViewById(R.id.year_to)
     protected Spinner yearTo;
 
     @ViewById(R.id.price_from)
     protected Spinner priceFrom;
 
     @ViewById(R.id.price_to)
     protected Spinner priceTo;
 
     private ArrayAdapter<String> adapter;
 
     LinkedHashMap<String, Integer> brandsMap;
     LinkedHashMap<String, Integer> regionsMap;
 
     private LinkedHashMap<String, Integer> modelsMap;
 
     @Inject
     private AvtopoiskParserImpl parser;
     private ProgressDialog progressDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         brandsMap = brandsAndRegionsHolder.brandsMap;
         regionsMap = brandsAndRegionsHolder.regionsMap;
 
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
     }
 
     @AfterViews
     protected void init() {
         populateBrands();
         populateRegions();
         populateYears();
         populatePrices();
 
         brands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 models.setEnabled(position > 0);
                 if (models.isEnabled()) {
                     progressDialog = ProgressDialog.show(SearchActivity.this, "", getString(R.string.dlg_progress_data_loading), true);
                     getModels(brandsMap.get(brands.getSelectedItem().toString()));
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
             }
         });
 
         models.setEnabled(false);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_layout);
     }
 
     @Override
     protected void onDestroy() {
         if (progressDialog != null) {
             progressDialog.dismiss();
         }
         super.onDestroy();
     }
 
     @Click(R.id.btn_find)
     protected void btnFindOnClick(View view) {
         final Intent intent = new Intent(SearchActivity.this, SearchResultActivity_.class);
         intent.putExtra(BRAND_ID_KEY, brandsMap.get(brands.getSelectedItem().toString()));
         int modelId = (models.isEnabled()) ? modelsMap.get(models.getSelectedItem().toString()) : 0;
         intent.putExtra(MODEL_ID_KEY, modelId);
         intent.putExtra(YEAR_FROM_KEY, yearFrom.getSelectedItem().toString());
         intent.putExtra(YEAR_TO_KEY, yearTo.getSelectedItem().toString());
         intent.putExtra(PRICE_FROM_KEY, priceFrom.getSelectedItem().toString());
         intent.putExtra(PRICE_TO_KEY, priceTo.getSelectedItem().toString());
         intent.putExtra(REGION_ID_KEY, regionsMap.get(regions.getSelectedItem().toString()));
         startActivity(intent);
     }
 
     @Background
     protected void getModels(int brandId) {
         LinkedHashMap<String, Integer> aModels = null;
         try {
             aModels = parser.getModels(brandId);
         } catch (IOException e) {
             Log.e(getString(R.string.app_name), e.getMessage());
         }
         populateModels(aModels);
     }
 
     protected void populateBrands() {
         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>(brandsMap.keySet()));
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         brands.setAdapter(adapter);
         brands.setPrompt(getString(R.string.brands_prompt));
     }
 
     protected void populateYears() {
         ArrayAdapter yearsAdapter = ArrayAdapter.createFromResource(this, R.array.years, android.R.layout.simple_spinner_item);
         yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         yearFrom.setAdapter(yearsAdapter);
         yearFrom.setPrompt(getString(R.string.year_prompt));
         yearTo.setAdapter(yearsAdapter);
         yearTo.setPrompt(getString(R.string.year_prompt));
     }
 
     protected void populatePrices() {
         ArrayAdapter pricesAdapter = ArrayAdapter.createFromResource(this, R.array.prices, android.R.layout.simple_spinner_item);
         pricesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         priceFrom.setAdapter(pricesAdapter);
        priceFrom.setPrompt(getString(R.string.price_prompt));
         priceTo.setAdapter(pricesAdapter);
        priceTo.setPrompt(getString(R.string.price_prompt));
     }
 
    @UiThread
     protected void populateModels(LinkedHashMap<String, Integer> aModels) {
         modelsMap = aModels;
         adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_spinner_item, new ArrayList<String>(modelsMap.keySet()));
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         models.setPrompt(getString(R.string.models_prompt));
         models.setAdapter(adapter);
         models.invalidate();
         if (progressDialog != null) {
             progressDialog.dismiss();
         }
     }
 
     protected void populateRegions() {
         adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_spinner_item, new ArrayList<String>(regionsMap.keySet()));
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         regions.setPrompt(getString(R.string.regions_prompt));
         regions.setAdapter(adapter);
         regions.invalidate();
     }
 }
