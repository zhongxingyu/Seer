 package com.chips.homebar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 
 import com.chips.ApplicationHubActivity;
import com.chips.AddMealToFavoritesActivity;
 import com.chips.FavoritesActivity;
 import com.chips.R;
 
 public class HomeBarAction {
     public static void goHomeClicked(Activity callingActivity, View view) {
         Intent applicationHubActivityIntent 
             = new Intent(callingActivity, ApplicationHubActivity.class);
         applicationHubActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
         callingActivity.startActivity(applicationHubActivityIntent);
     }
     
     public static void addFavoriteClicked(Activity callingActivity, View view) {
         Intent favoriteActivityIntent 
             = new Intent(callingActivity, FavoritesActivity.class);
         favoriteActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         
         callingActivity.startActivity(favoriteActivityIntent);
     }
     
     public static void inflateHomeBarView(Activity activity, int innerView) {
         activity.setContentView(R.layout.home_bar);
         LayoutInflater inflater 
             = (LayoutInflater)activity.getSystemService(
                     Context.LAYOUT_INFLATER_SERVICE
               );
         LinearLayout mainView 
             = (LinearLayout) activity.findViewById(R.id.homeBarMainView);
         
         View subView = inflater.inflate(innerView, mainView, false);
         mainView.addView(subView);
     }
 }
