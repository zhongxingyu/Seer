 package fi.testbed2;
 
 import com.google.inject.AbstractModule;
 import fi.testbed2.android.ui.dialog.AlertDialogBuilder;
 import fi.testbed2.android.ui.dialog.DialogBuilder;
 import fi.testbed2.service.*;
 import fi.testbed2.service.impl.*;
 
 /**
  * Module used by the RoboGuice IoC framework.
  */
 public class MainModule extends AbstractModule {
 
    public MainModule() {

    }

     @Override
     protected void configure() {
         bind(DialogBuilder.class).to(AlertDialogBuilder.class);
         bind(MunicipalityService.class).to(InlineMunicipalityService.class);
         bind(LocationService.class).to(PreferenceBasedLocationService.class);
         bind(CoordinateService.class).to(MercatorCoordinateService.class);
         bind(SettingsService.class).to(SharedPreferenceSettingsService.class);
         bind(BitmapService.class).to(LruCacheBitmapService.class);
         bind(PageService.class).to(LruCachePageService.class);
         bind(HttpUrlService.class).to(ApacheHttpUrlService.class);
     }
 
 }
