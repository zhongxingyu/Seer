 package com.cocacola.climateambassador.modules;
 
 import android.content.Context;
 
 import com.cocacola.climateambassador.BuildConfig;
 import com.cocacola.climateambassador.CaApplication;
 import com.cocacola.climateambassador.DocumentViewerDelegate;
 import com.cocacola.climateambassador.ui.BusinessCaseModule;
import com.cocacola.climateambassador.ui.CaseFragment;
 import com.cocacola.climateambassador.ui.EngagingSuppliersModule;
 import com.cocacola.climateambassador.ui.InternalTrainingOverview;
 import com.cocacola.climateambassador.ui.MainActivity;
 import com.cocacola.climateambassador.ui.MainFragment;
 import com.cocacola.climateambassador.ui.SupplierOverview;
 import com.cocacola.climateambassador.ui.ValueChainModule;
 import com.cocacola.climateambassador.util.JsonAssetsHelper;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import javax.inject.Singleton;
 
 import dagger.Module;
 import dagger.Provides;
 import timber.log.Timber;
 
 /**
  * Created by andrewlawton on 8/21/13.
  */
 
 @Module(injects = {
         CaApplication.class,
         MainActivity.class,
         MainFragment.class,
        CaseFragment.class,
         BusinessCaseModule.class,
         EngagingSuppliersModule.class,
         ValueChainModule.class,
         SupplierOverview.class,
         DocumentViewerDelegate.class,
         InternalTrainingOverview.class,
         JsonAssetsHelper.class
 })
 public class ClimateAmbassadorModule {
 
     private Context mContext;
 
     public ClimateAmbassadorModule() { }
 
     public ClimateAmbassadorModule(Context context) {
         mContext = context;
     }
 
     @Provides @Singleton
     Context provideContext() {
         return mContext;
     }
 
     @Provides @Singleton
     Timber provideTimber() {
         return BuildConfig.DEBUG ? Timber.DEBUG : Timber.PROD;
     }
 
     @Provides
     Gson provideGson() {
         return new GsonBuilder().create();
     }
 
 }
