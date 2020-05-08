 package com.ezhang.pop.settings;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 
 import com.ezhang.pop.R;
 import com.ezhang.pop.core.ICallable;
 import com.ezhang.pop.core.StringUtils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class AppSettings {
     private static final String HAS_DISCOUNT_SETTINGS = "key.has.discount.settings";
     private static final String GPS_AS_LOCATION_TYPE = "com.ezhang.pop.location.type";
     private static final String FIRST_RUN_OF_APPLICATION = "com.ezhang.pop.first.run";
     private static final String CUSTOM_LOCATION = "com.ezhang.pop.customlocations";
     private static final String LAST_SUBURB = "com.ezhang.pop.last.suburb";
     private static final String LOCATION_SEP = "||";
 
     private final Context m_context;
     public int m_colesDiscount = 8;
     public int m_wwsDiscount = 8;
     private SharedPreferences m_settings = null;
     private AlertDialog m_discountSettingsDlg;
     private boolean m_isFirstRun = false;
 
     public AppSettings(Context context) {
         m_context = context;
         m_settings = PreferenceManager.getDefaultSharedPreferences(m_context);
         m_isFirstRun = m_settings.getBoolean(FIRST_RUN_OF_APPLICATION, true);
         if (m_isFirstRun) {
             Editor editor = m_settings.edit();
             editor.putBoolean(FIRST_RUN_OF_APPLICATION, false);
             editor.commit();
         }
     }
 
     public void LoadDiscountSettings(final ICallable<Object, Object> callable) {
         boolean hasDiscountSettings = m_settings.getBoolean(
                 HAS_DISCOUNT_SETTINGS, false);
         if (hasDiscountSettings) {
            String wwsDiscount = m_settings.getString(
                    m_context.getString(R.string.wws_discount_settings), "8");
            m_wwsDiscount = Integer.parseInt(wwsDiscount);

            String colesDiscount = m_settings.getString(
                    m_context.getString(R.string.coles_discount_settings), "8");

            m_colesDiscount = Integer.parseInt(colesDiscount);

             callable.Call(null);
         } else {
             ShowSettingsDialog(callable);
         }
     }
 
     private void ShowSettingsDialog(final ICallable<Object, Object> callable) {
         if (m_discountSettingsDlg != null && m_discountSettingsDlg.isShowing()) {
             return;
         }
 
         if (m_discountSettingsDlg == null) {
             LayoutInflater factory = LayoutInflater.from(this.m_context);
             final View view = factory.inflate(R.layout.discount_settings, null);
             ((EditText) view.findViewById(R.id.coles_voucher)).setText(String
                     .valueOf(m_colesDiscount));
             ((EditText) view.findViewById(R.id.wws_voucher)).setText(String
                     .valueOf(m_wwsDiscount));
             m_discountSettingsDlg = new AlertDialog.Builder(m_context)
                     .setTitle("Discount Settings").setView(view)
                     .setCancelable(false)
                     .setPositiveButton("OK", new OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             SaveSettings(view, callable);
                         }
                     }).create();
         }
         m_discountSettingsDlg.show();
     }
 
     private void SaveSettings(final View view,
                               final ICallable<Object, Object> callable) {
         String colesDiscount = ((EditText) view
                 .findViewById(R.id.coles_voucher)).getText().toString();

         m_colesDiscount = Integer.parseInt(colesDiscount);
 
         String wwsDiscount = ((EditText) view.findViewById(R.id.wws_voucher))
                 .getText().toString();
         m_wwsDiscount = Integer.parseInt(wwsDiscount);
 
         Editor editor = m_settings.edit();
         editor.putBoolean(HAS_DISCOUNT_SETTINGS, true);
         editor.putString(m_context.getString(R.string.wws_discount_settings),
                 wwsDiscount);
         editor.putString(m_context.getString(R.string.coles_discount_settings),
                 colesDiscount);
         editor.commit();
 
         callable.Call(null);
     }
 
     public boolean IncludeSurroundings() {
         return m_settings.getBoolean(
                 m_context.getString(R.string.surrounding_suburbs_settings_key),
                 true);
     }
 
     public int GetFuelType() {
         String fuelType = m_settings.getString(
                 m_context.getString(R.string.fueltype_settings_key), "1");
         return Integer.parseInt(fuelType);
     }
 
     public boolean UseGPSAsLocation() {
         return m_settings.getBoolean(GPS_AS_LOCATION_TYPE, false);
     }
 
     public void UseGPSAsLocation(boolean useGPS) {
         Editor editor = m_settings.edit();
         editor.putBoolean(GPS_AS_LOCATION_TYPE, useGPS);
         editor.commit();
     }
 
     public List<String> GetHistoryLocations() {
         String customLocations = m_settings.getString(CUSTOM_LOCATION, "");
         if (customLocations != "") {
             return new ArrayList<String>(Arrays.asList(customLocations
                     .split(java.util.regex.Pattern.quote(LOCATION_SEP))));
         }
 
         return new ArrayList<String>();
     }
 
     public void SaveLocations(List<String> locations) {
         if (locations != null && locations.size() != 0) {
             String jointLocations = StringUtils.Join(
                     locations.toArray(new String[locations.size()]),
                     LOCATION_SEP);
             Editor editor = m_settings.edit();
             editor.putString(CUSTOM_LOCATION, jointLocations);
             editor.commit();
         }
     }
 
     public String GetLastSuburb() {
         return m_settings.getString(LAST_SUBURB, "");
     }
 
     public void SetLastSuburb(String suburb) {
         Editor editor = m_settings.edit();
         editor.putString(LAST_SUBURB, suburb);
         editor.commit();
     }
 
     public boolean IsFirstRun() {
         return m_isFirstRun;
     }
 }
