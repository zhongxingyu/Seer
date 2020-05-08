 package org.alabs.nolotiro;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 
 public class AdsFragment extends ListFragment {
 
     private static final String TAG = "AdsFragment";
     private static final Integer DEFAULT_WOEID = 766356;
     private NolotiroAPI nolotiro;
 
     public AdsFragment() {
         nolotiro = NolotiroAPI.getInstance();
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         refreshAds();
         // TODO: Restore position
     }
 
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         // TODO: Save position
     }
 
     public void onListItemClick(ListView listView, View view, int position, long id) {
         Ad ad = (Ad) listView.getAdapter().getItem(position);
         Log.i(TAG, "Clicked on " + ad.getTitle());
 
         startActivity(new Intent(getActivity(), AdViewActivity.class).putExtra("id", ad.getId()));
     }
 
     public void refreshAds() {
         UpdateAdsTask updateTask = new UpdateAdsTask(nolotiro, this);
        updateTask.execute(DEFAULT_WOEID);
     }
 }
