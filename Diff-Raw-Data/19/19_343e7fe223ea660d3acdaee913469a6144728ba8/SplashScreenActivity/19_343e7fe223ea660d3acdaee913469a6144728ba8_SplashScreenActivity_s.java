 package ua.avtopoisk.activites;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import com.google.inject.Inject;
 import com.googlecode.androidannotations.annotations.*;
 import de.akquinet.android.androlog.Log;
 import parsers.AvtopoiskBaseParser;
 import parsers.AvtopoiskParser;
 import ua.avtopoisk.BrandsAndRegionsHolder;
 import ua.avtopoisk.R;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 
 /**
  * Splash screen activity
  *
  * @author ibershadskiy <a href="mailto:iBersh20@gmail.com">Ilya Bershadskiy</a>
  * @since 19.10.12
  */
 @EActivity(R.layout.layout_splash)
 @RoboGuice
 public class SplashScreenActivity extends Activity {
 
     @Inject
     private AvtopoiskParser parser;
 
     @Bean
     BrandsAndRegionsHolder brandsAndRegionsHolder;
 
     @ViewById(R.id.splash_progress)
     ProgressBar progressBar;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         loadData();
     }
 
     @UiThread
     protected void showSplashError() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.splash_error_text)
                 .setTitle(R.string.error)
                 .setCancelable(false)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         finish();
                     }
                 });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     @Background
     protected void loadData() {
         publishProgress(10);
         LinkedHashMap<String, Integer> brands = null;
         for (int i = 0; i < 1; ++i) {
             try {
                 brands = parser.getBrands();
                 break;
            } catch (Throwable e) {
                Log.e(e.getMessage());
             }
         }
 
         if(brands == null) {
             showSplashError();
             return;
         }
 
         publishProgress(50);
 
         LinkedHashMap<String, Integer> regions = null;
 
         for (int i = 0; i < 1; ++i) {
             try {
                 regions = parser.getRegions();
                 break;
            } catch (Throwable e) {
                Log.e(e.getMessage());
             }
         }
 
         if(regions == null) {
             showSplashError();
             return;
         }
 
         publishProgress(70);
         populateData(brands, regions);
     }
 
     @UiThread
     protected void populateData(LinkedHashMap<String, Integer> brands, LinkedHashMap<String, Integer> regions) {
         Intent intent = new Intent(SplashScreenActivity.this, SearchActivity_.class);
         brandsAndRegionsHolder.brandsMap = brands;
         brandsAndRegionsHolder.regionsMap = regions;
         publishProgress(100);
         startActivity(intent);
         finish();
     }
 
     @UiThread
     void publishProgress(int progress) {
         progressBar.setProgress(progress);
     }
 
 }
