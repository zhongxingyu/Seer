 package com.marwinxxii.ccardstats;
 
 import java.util.HashMap;
 
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 import com.marwinxxii.ccardstats.helpers.DateHelper;
 import com.marwinxxii.ccardstats.helpers.MoneyHelper;
 
 
 public class Application extends android.app.Application {
     
     @Override
     public void onCreate() {
         DateHelper.setMonthNames(getResources().getStringArray(R.array.month_names));
         SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
         HashMap<String, Double> rates = new HashMap<String, Double>(2);
        rates.put("usd", (double)prefs.getFloat("exchange_rates_usd", 30.0f));
        rates.put("eur", (double)prefs.getFloat("exchange_rates_eur", 39.0f));
         MoneyHelper.setExchangeRates(rates);
     }
 }
