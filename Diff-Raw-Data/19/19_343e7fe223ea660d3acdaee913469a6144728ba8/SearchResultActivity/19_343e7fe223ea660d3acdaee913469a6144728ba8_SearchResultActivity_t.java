 package ua.avtopoisk.activites;
 
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.google.inject.Inject;
 import com.googlecode.androidannotations.annotations.*;
 import com.googlecode.androidannotations.annotations.res.StringRes;
 import de.akquinet.android.androlog.Log;
 import domain.Car;
 import org.apache.commons.lang.StringUtils;
 import parsers.AvtopoiskBaseParser;
 import parsers.AvtopoiskParser;
 import ua.avtopoisk.AvtopoiskApplication;
 import ua.avtopoisk.CarAdapter;
 import ua.avtopoisk.R;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 /**
  * Search result activity. List of cars returned by parser
  *
  * @author ibershadskiy <a href="mailto:iBersh20@gmail.com">Ilya Bershadskiy</a>
  * @since 12.10.12
  */
 
 @EActivity(R.layout.layout_search_result)
 @RoboGuice
 public class SearchResultActivity extends ListActivity {
     public static final int CARS_PER_PAGE = 10;
     private ProgressDialog progressDialog;
 
     @Extra(SearchActivity.BRAND_ID_KEY)
     int brandId;
 
     @Extra(SearchActivity.MODEL_ID_KEY)
     int modelId;
 
     @Extra(SearchActivity.REGION_ID_KEY)
     int regionId;
 
     @Extra(SearchActivity.YEAR_FROM_KEY)
     String yearFrom;
 
     @Extra(SearchActivity.YEAR_TO_KEY)
     String yearTo;
 
     @Extra(SearchActivity.PRICE_FROM_KEY)
     String priceFrom;
 
     @Extra(SearchActivity.PRICE_TO_KEY)
     String priceTo;
 
     @StringRes(R.string.any)
     String anyString;
 
     @StringRes(R.string.any2)
     String anyString2;
 
     @Inject
     private AvtopoiskParser parser;
 
     @App
     AvtopoiskApplication application;
 
     private View loadMoreView;
     private CarAdapter adapter;
     private ArrayList<Car> currentResults = new ArrayList<Car>();
     private int loadedCount;
 
     private DialogInterface.OnClickListener dataLoadingErrorDialogClickListener = new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
             switch (which) {
                 case Dialog.BUTTON_POSITIVE:
                     loadResults();
                     break;
                 case Dialog.BUTTON_NEGATIVE:
                     finish();
                     break;
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.layout_title);
         loadMoreView = ((LayoutInflater) this
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                 .inflate(R.layout.loadmore_item, null, false);
     }
 
     @AfterViews
     protected void init() {
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.layout_title);
         getListView().setDividerHeight(0);
         getListView().setFooterDividersEnabled(false);
         loadMoreView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 loadResults();
             }
         });
 
         loadResults();
     }
 
     @Override
     public void onBackPressed() {
         parser.resetCurrentPage();
         super.onBackPressed();
     }
 
     @Override
     protected void onDestroy() {
         if (progressDialog != null) {
             progressDialog.dismiss();
         }
         super.onDestroy();
     }
 
     @ItemClick
     public void listItemClicked(Car clicked) {
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clicked.getLinkToDetails()));
         startActivity(intent);
     }
 
     @UiThread
     void showProgressDialog() {
         progressDialog = ProgressDialog.show(SearchResultActivity.this, "", getString(R.string.dlg_progress_data_loading), true);
     }
 
     @UiThread
     protected void showDataLoadingErrorDialog() {
         if (progressDialog != null) {
             progressDialog.dismiss();
         }
         application.showDataLoadingErrorDialog(this, dataLoadingErrorDialogClickListener);
     }
 
     @Background
     void loadResults() {
         showProgressDialog();
         ArrayList<Car> cars;
         int aYearFrom = StringUtils.isEmpty(yearFrom) || yearFrom.equals(anyString) ? 0 : Integer.parseInt(yearFrom);
         int aYearTo = StringUtils.isEmpty(yearTo) || yearTo.equals(anyString) ? 0 : Integer.parseInt(yearTo);
         int aPriceFrom = StringUtils.isEmpty(priceFrom) || priceFrom.equals(anyString2) ? 0 : Integer.parseInt(priceFrom);
         int aPriceTo = StringUtils.isEmpty(priceTo) || priceTo.equals(anyString2) ? 0 : Integer.parseInt(priceTo);
         try {
             cars = parser.parse(brandId, modelId, regionId, aYearFrom, aYearTo, aPriceFrom, aPriceTo);
         } catch (Throwable e) {
            String err = (e.getMessage()==null)? "No message" : e.getMessage();
            Log.e(err);
             showDataLoadingErrorDialog();
             return;
         }
 
         for (Car car : cars) {
             URL url;
             Bitmap bmp = null;
             if (car.getImageUrl().contains("no_foto")) {  //if no photo load default image
                 bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_photo);
             } else {  //if no photo present load it from net
                 try {
                     url = new URL(car.getImageUrl());
                     bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                 } catch (Exception e) {
                     Log.e(e.getMessage());
                 }
             }
             car.setImage(bmp);
         }
 
         populateResults(cars);
     }
 
 
     @UiThread
     void populateResults(ArrayList<Car> cars) {
         ListView listView = getListView();
         loadedCount += cars.size();
         int resultsCount = parser.getLastRequestResultsCount();
 
         //add load more button and text
         TextView loadedCountText = (TextView) loadMoreView.findViewById(R.id.loaded_count_text);
         loadedCountText.setText(String.format(getString(R.string.loaded_count_text), loadedCount, resultsCount));
 
         if (listView.getFooterViewsCount() == 0) {
             listView.addFooterView(loadMoreView);
         }
         if ((cars.isEmpty() || cars.size() < CARS_PER_PAGE || loadedCount == resultsCount) && listView.getFooterViewsCount() > 0) {
             View loadTenMoreText = loadMoreView.findViewById(R.id.load_ten_more_text);
             ((LinearLayout) loadMoreView).removeView(loadTenMoreText);
             loadedCountText.setPadding(0, 10, 0, 10);
             loadMoreView.setOnClickListener(null);
         }
         currentResults.addAll(cars);
 
         if (adapter == null) {
             adapter = new CarAdapter(this, R.layout.cars_list_item, currentResults);
             listView.setAdapter(adapter);
         } else {
             adapter.notifyDataSetChanged();
         }
 
         progressDialog.dismiss();
     }
 }
