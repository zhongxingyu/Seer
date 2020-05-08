 package com.tabbie.android.radar;
 
 /**
  *  EventDetailsActivity.java
  *
  *  Created on: July 25, 2012
  *      Author: Valeri Karpov
  *      
  *  Super simple activity for displaying a more detailed view of an event.
  *  All we do is just set a bunch of layout views to match our event model
  */
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.tabbie.android.radar.http.ServerDeleteRequest;
 import com.tabbie.android.radar.http.ServerPostRequest;
 import com.tabbie.android.radar.http.ServerResponse;
 
 public class EventDetailsActivity extends ServerThreadActivity
 	implements EventDetailsPagerAdapter.RadarSelectedListener {
 	
   private Event e;
   private RadarCommonController commonController;
   private UnicornSlayerController tutorialController;
   private String token;
 
   private boolean tutorialMode = false;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.event_details_activity);
 
     tutorialController = new UnicornSlayerController(new AlertDialog.Builder(this));
 
     Bundle starter = getIntent().getExtras();
     if (null != starter && starter.containsKey("eventId")) {
       final String eventId = starter.getString("eventId");
       commonController = starter.getParcelable("controller");
       e = commonController.getEvent(eventId);
       token = starter.getString("token");
       tutorialMode = starter.getBoolean("virgin", false);
     } else {
       // No event, nothing to display
       // Also, fatal error currently
       this.finish();
       return;
     }
     
     final ViewPager pager = (ViewPager) findViewById(R.id.details_event_pager);
     new EventDetailsPagerAdapter(this, commonController, R.layout.event_details_element, pager);
     pager.setCurrentItem(commonController.eventsList.indexOf(e));
     
     
     if (tutorialMode) {
       tutorialController.showDetailsTutorial();
       tutorialMode = false;
     }
 
   }
 
   @Override
   public void onBackPressed() {
     Intent intent = new Intent();
     intent.putExtra("controller", commonController);
     setResult(RESULT_OK, intent);
     super.onBackPressed();
   }
 
   @Override
   protected boolean handleServerResponse(ServerResponse resp) {
     // Assume that ADD_TO_RADAR and REMOVE_FROM_RADAR always succeed
     return false;
   }
   
   
 	@Override
 	public void onRadarSelected(final View v, final Event e) {
 	    final TextView radarCount = (TextView) v.findViewById(R.id.details_event_num_radar);
 	    final ImageView radarButton = (ImageView) v.findViewById(R.id.add_to_radar_image);
 	
         if (e.isOnRadar() && commonController.removeFromRadar(e)) {
           Log.v("EventDetailsActivity", "Removing event from radar");
           radarButton.setSelected(false);
           radarCount.setText(Integer.toString(e.radarCount));
 
           ServerDeleteRequest req = new ServerDeleteRequest(
               ServerThread.TABBIE_SERVER + "/mobile/radar/" + e.id
                   + ".json?auth_token=" + token, MessageType.ADD_TO_RADAR);
 
           serverThread.sendRequest(req);
         } else if (!e.isOnRadar()) {
           Log.v("EventDetailsActivity", "Adding event to radar");
           if (commonController.addToRadar(e)) {
             radarButton.setSelected(true);
             radarCount.setText(Integer.toString(e.radarCount));
 
             ServerPostRequest req = new ServerPostRequest(
                 ServerThread.TABBIE_SERVER + "/mobile/radar/" + e.id + ".json",
                 MessageType.ADD_TO_RADAR);
             req.params.put("auth_token", token);
             serverThread.sendRequest(req);
           } else {
             Toast.makeText(EventDetailsActivity.this,
                 "You can only add 3 events to your radar!", Toast.LENGTH_SHORT).show();
             return;
           }
         }
 	}
 }
