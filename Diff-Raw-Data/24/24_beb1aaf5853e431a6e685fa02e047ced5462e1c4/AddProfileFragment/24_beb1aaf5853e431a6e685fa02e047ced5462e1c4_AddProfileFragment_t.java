 package com.Centaurii.app.RatingCalculator.fragments;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Set;
 
 import com.Centaurii.app.RatingCalculator.GameRatingCalculatorActivity;
 import com.Centaurii.app.RatingCalculator.R;
 import com.Centaurii.app.RatingCalculator.adapters.ColorAdapter;
 import com.Centaurii.app.RatingCalculator.model.Profile;
 import com.Centaurii.app.RatingCalculator.util.Tags;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 
 public class AddProfileFragment extends DialogFragment
 {
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         
         Set<String> colorSet = Tags.getColorMap().keySet();
         ArrayList<String> colorList = new ArrayList<String>(colorSet);
         ColorAdapter adapter = new ColorAdapter(getActivity(), R.layout.color_spinner, colorList);
         
         LayoutInflater inflater = getActivity().getLayoutInflater();
         final View view = inflater.inflate(R.layout.fragment_add_profile, null);
         
         final EditText ratingTextBox = (EditText) view.findViewById(R.id.profile_rating);
         ratingTextBox.setHint("Rating (Default is " + GameRatingCalculatorActivity.DEFAULT_RATING() + ")");
         
         //Make the whole checkbox area pressable
         final CheckBox checkBox = (CheckBox) view.findViewById(R.id.profile_provisional);
         final LinearLayout myCheckBox = (LinearLayout) view.findViewById(R.id.my_check_box);
         myCheckBox.setOnClickListener(new OnClickListener()
         {
 
             @Override
             public void onClick(View view)
             {
                 if(checkBox.isChecked())
                 {
                     checkBox.setChecked(false);
                 }
                 else
                 {
                     checkBox.setChecked(true);
                 }
             }
             
         });
         
         final Spinner colorSpinner = (Spinner) view.findViewById(R.id.profile_color);
         colorSpinner.setAdapter(adapter);
         
         
         /* Handles the creation of the new profile through an AlertDialog */
         DialogInterface.OnClickListener buttonPress = new DialogInterface.OnClickListener()
         {
             @Override
             public void onClick(DialogInterface dialog, int id)
             {
                 switch(id)
                 {
                     case DialogInterface.BUTTON_POSITIVE:
                         Random rand = new Random();
                         EditText name = (EditText) view.findViewById(R.id.profile_name);
                         EditText rating = (EditText) view.findViewById(R.id.profile_rating);
                         Spinner color = (Spinner) view.findViewById(R.id.profile_color);
                         CheckBox provisional = (CheckBox) view.findViewById(R.id.profile_provisional);
                         
                         String profileName = name.getText().toString();
                         if(profileName.equals(""))
                         {
                             profileName = Tags.DEFAULT_NAME + (rand.nextInt() % 10000);
                         }
                         int profileRating;
                         try
                         {
                             profileRating = Integer.valueOf(rating.getText().toString());
                         }
                         catch(NumberFormatException e)
                         {
                             profileRating = GameRatingCalculatorActivity.DEFAULT_RATING();
                         }
                         int profileColor = Tags.getColorMap().get(color.getSelectedItem());
                         
                         Profile newProfile = new Profile(profileName,
                                                          profileRating,
                                                          provisional.isChecked(),
                                                          profileColor);
                         
                         ArrayList<Profile> profiles = ((GameRatingCalculatorActivity) AddProfileFragment.this.getActivity())
                                                     .getSavedProfiles();
                         profiles.add(newProfile);
                         ((ProfileViewerFragment) AddProfileFragment.this.getActivity()
                                                                    .getSupportFragmentManager()
                                                                    .findFragmentByTag(Tags.PROFILE_FRAGMENT))
                                                                    .notifyAdapter();
                        
                         break;
                         
                     case DialogInterface.BUTTON_NEGATIVE:
                         AddProfileFragment.this.getDialog().cancel();
                         break;
                 }
                 
             }
         };
         builder.setView(view)
                .setPositiveButton("Add", buttonPress)
                .setNegativeButton("Cancel", buttonPress);
         
         return builder.create();
     }
 }
