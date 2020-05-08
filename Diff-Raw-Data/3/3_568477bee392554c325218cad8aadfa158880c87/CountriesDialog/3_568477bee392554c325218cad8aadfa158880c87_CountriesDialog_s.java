 package com.source.tripwithme.main_ui;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.graphics.Color;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.GridLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import com.source.tripwithme.TripWithMeMain;
 import com.source.tripwithme.components.Country;
 import com.source.tripwithme.components.CountryFactory;
 import com.source.tripwithme.components.ListWithListeners;
 import com.source.tripwithme.components.PointWithDistance;
 import com.source.tripwithme.components.PointWithID;
 
 import java.util.List;
 
 public class CountriesDialog {
 
     private final Activity activity;
     private final CountryFactory countryFactory;
     private final ListWithListeners<PointWithID> interests;
     private static View lastViewSelected;
     private Country countrySelected;
 
     private CountriesDialog(Activity activity, CountryFactory countryFactory,
                             ListWithListeners<PointWithID> interests) {
         this.activity = activity;
         this.countryFactory = countryFactory;
         this.interests = interests;
         countrySelected = null;
     }
 
     public static Dialog getNewDialog(Activity activity, CountryFactory countryFactory,
                                       ListWithListeners<PointWithID> interests) {
         return new CountriesDialog(activity, countryFactory, interests).getDialog();
     }
 
     private Dialog getDialog() {
         final Dialog dialog = new Dialog(activity);
         LinearLayout linearLayout = getCountriesLayout(dialog);
         dialog.setContentView(linearLayout);
         dialog.setTitle("All Countries");
         dialog.setCancelable(true);
         return dialog;
     }
 
     private LinearLayout getCountriesLayout(final Dialog dialog) {
         final List<Country> countries = countryFactory.getAllCountries();
         final Button button = new Button(activity);
         button.setText("Click flag!");
         button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (countrySelected != null) {
                     String fullName = countrySelected.getFullName();
                     activity.setTitle(TripWithMeMain.APP_NAME + " - " + fullName);
                     interests.add(
                         PointWithID.generate(new PointWithDistance(countrySelected.getGeoPoint(),
                                                                    TripWithMeMain.NEW_COUNTRY_INTEREST_DISTANCE,
                                                                    fullName)));
                     dialog.dismiss();
                 }
             }
         });
         GridLayout countryGrid = new GridLayout(activity);
         countryGrid.setVerticalScrollBarEnabled(true);
         countryGrid.setHorizontalScrollBarEnabled(true);
         countryGrid.setRowCount(16);
         countryGrid.setColumnCount(15);
         int lastSelectedInt = -1;
         if (lastViewSelected != null) {
             lastSelectedInt = (Integer)lastViewSelected.getTag();
         }
         for (int i = 0; i < countries.size(); i++) {
             final Country c = countries.get(i);
             ImageView imageView = new ImageView(activity);
             imageView.setTag(i);
             imageView.setImageResource(c.resource());
             imageView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if (lastViewSelected != null) {
                         lastViewSelected.setBackgroundColor(Color.WHITE);
                     }
                     lastViewSelected = v;
                     lastViewSelected.setBackgroundColor(Color.CYAN);
                     if (c.getFullName() != null) {
                         countrySelected = c;
                         button.setText("Click to Jump to " + c.getFullName().toUpperCase() + "!");
                     } else {
                         countrySelected = null;
                         button.setText("Not supported Country: " + c.name() + "!");
                     }
                 }
             });
             imageView.setPadding(3, 3, 3, 3);
             if (lastSelectedInt == i) {
                 imageView.setBackgroundColor(Color.CYAN);
                 lastViewSelected = imageView;
             }
             countryGrid.addView(imageView, i);
         }
         LinearLayout countriesLayout = new LinearLayout(activity);
         countriesLayout.setOrientation(LinearLayout.VERTICAL);
         countriesLayout.addView(countryGrid);
         countriesLayout.addView(button);
         return countriesLayout;
     }
 
 }
