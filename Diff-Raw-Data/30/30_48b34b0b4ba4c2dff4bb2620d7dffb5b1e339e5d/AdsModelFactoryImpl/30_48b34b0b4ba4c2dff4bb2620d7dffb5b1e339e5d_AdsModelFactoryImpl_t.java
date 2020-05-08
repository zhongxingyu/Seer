 package com.dosolves.gym.app.ads;
 
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Context;
 
 import com.dosolves.gym.ads.AdsController;
 import com.dosolves.gym.ads.AdsInitializer;
 import com.dosolves.gym.ads.AdsRemovalBoughtStorer;
 import com.dosolves.gym.ads.AdsRemovalBuyer;
 import com.dosolves.gym.ads.AdsRemovalPurchasedListener;
 import com.dosolves.gym.ads.ApplicationRestarter;
 import com.dosolves.gym.ads.MenuSetter;
 import com.dosolves.gym.ads.UserSpecificPayloadValidator;
 import com.dosolves.gym.ads.UserThanker;
 import com.dosolves.gym.ads.ViewSetter;
 import com.dosolves.gym.app.ContextPreferenceRetriever;
 import com.dosolves.gym.app.PreferenceAdsShouldBeDisplayedDecider;
 import com.dosolves.gym.app.PreferenceRetriever;
 import com.dosolves.gym.app.gui.AdsInitializerImpl;
 import com.dosolves.gym.app.gui.CategoryAndExerciseViewSetter;
 import com.dosolves.gym.app.gui.PerformaceViewSetter;
 import com.dosolves.gym.app.gui.UserUpdateableItemsActivity;
 import com.dosolves.gym.app.performance.gui.PerformanceActivity;
 import com.dosolves.gym.domain.AdsShouldBeDisplayedDecider;
 import com.dosolves.gym.inappbilling.IabHelper;
 
 public class AdsModelFactoryImpl implements AdsModelFactory {
 	
 	private IabHelper iabHelperInstance;
 	private AdsRemovalBuyerAdapter adsRemovalBuyerInstance;
 	
 	private boolean testMode;
 
 	public AdsModelFactoryImpl(boolean testMode){
 		this.testMode = testMode;		
 	}
 	
 	@Override
 	public AdsController createController(Activity activity) {
 		ViewSetter viewSetter = createViewSetter(activity);
 		AdsInitializer adsInitializer = new AdsInitializerImpl(activity);
 		MenuSetter menuSetter = (MenuSetter)activity;
 		PreferenceRetriever preferenceRetriever = new ContextPreferenceRetriever(activity);
 		AdsShouldBeDisplayedDecider adsShouldBeDisplayedDecider = new PreferenceAdsShouldBeDisplayedDecider(preferenceRetriever);
 		AdsRemovalBuyer removalBuyer = getAdsRemovalBuyer(activity);
 		
 		return new AdsController(adsShouldBeDisplayedDecider, viewSetter, menuSetter, adsInitializer,removalBuyer);		
 	}
 
 	private ViewSetter createViewSetter(Activity activity) {
 		ViewSetter ret = null;
 		
 		if(activity instanceof UserUpdateableItemsActivity){
 			ret = new CategoryAndExerciseViewSetter(activity);
 		}
 		else if(activity instanceof PerformanceActivity){
 			ret = new PerformaceViewSetter(activity);
 		}
 		
 		return ret;
 	}
 
 	@Override
 	public AdsRemovalBuyerAdapter getAdsRemovalBuyer(Context context) {
 		if(adsRemovalBuyerInstance == null)
 			adsRemovalBuyerInstance = createNewAdsRemovalBuyer(context);
 		return adsRemovalBuyerInstance;
 
 	}
 
 	private AdsRemovalBuyerAdapter createNewAdsRemovalBuyer(Context context) {
 		IabHelper iabHelper = getIabHelper(context);
 		RouterActivityStarter routerActivityStarter = new ContextRouterActivityStarter(context);
 		UserSpecificPayloadGenerator payloadGenerator = new GoogleAcountPayloadGenerator(AccountManager.get(context));
 		PreferenceRetriever preferenceRetriver = new ContextPreferenceRetriever(context);
 		AdsRemovalBoughtStorer adsRemovalBoughtStorer = new PreferensesAdsRemovalBoughtStorer(preferenceRetriver);
 		UserThanker userThanker = new ToastUserThanker(context);
 		ApplicationRestarter restarter = new ContextApplicationRestarter(context); 
 		AdsRemovalPurchasedListener adsRemovalPurchasedListener = new AdsRemovalBoughtController(adsRemovalBoughtStorer, userThanker, restarter);  
 		UserSpecificPayloadValidator userSpecificPayloadValidator = new GoogleAccountUserSpecificPayloadValidator(payloadGenerator);
 	
 		if(testMode)
 			return new AdsRemovalBuyerAdapterForTest(iabHelper, routerActivityStarter, payloadGenerator, adsRemovalPurchasedListener, userSpecificPayloadValidator);
 		else			
 			return new AdsRemovalBuyerAdapter(iabHelper, routerActivityStarter, payloadGenerator, adsRemovalPurchasedListener, userSpecificPayloadValidator);
 	}
 
 	@Override
 	public IabHelper getIabHelper(Context context) {
 		if(iabHelperInstance == null)
			iabHelperInstance = new IabHelper(context, PublicKeyConstructor.constructPublicKey(), testMode);
 		return iabHelperInstance;
 	}
 	
 }
