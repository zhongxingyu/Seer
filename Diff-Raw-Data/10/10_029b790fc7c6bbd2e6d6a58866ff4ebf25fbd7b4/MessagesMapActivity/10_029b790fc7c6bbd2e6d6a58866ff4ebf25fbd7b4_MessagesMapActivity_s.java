 package com.example;
 
 import android.os.Bundle;
 import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 public class MessagesMapActivity extends MapActivity implements MessagesLoadable, MessageSelectable {
     private DialogOverlay dialogOverlay;
     private MyLocationOverlay myLocationOverlay;
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.messages_map);
         MapView mapView = (MapView) findViewById(R.id.messages_map);
         mapView.setBuiltInZoomControls(true);
         dialogOverlay = new DialogOverlay(getResources().getDrawable(R.drawable.ic_map_marker), this);
         mapView.getOverlays().add(dialogOverlay);
         reloadMarkers();
         myLocationOverlay = new MyLocationOverlay(this, mapView);
         mapView.getOverlays().add(myLocationOverlay);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         myLocationOverlay.enableMyLocation();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         myLocationOverlay.disableMyLocation();
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     public void reloadMarkers() {
         Toast.makeText(this, "Nalagam sporočila...", Toast.LENGTH_SHORT).show();
         new AsyncMessagesLoader(this).execute();
     }
 
     public void loadMessages(List<Message> messages) {
         dialogOverlay.clear();
         for (Message message : messages) {
             dialogOverlay.addItem(message.getLatitude(), message.getLongitude(),
                     MessageFormat.format("{0} - {1}", message.getAuthor(), DATE_FORMAT.format(message.getDate())),
                     message.getMessage());
         }
         Toast.makeText(this, "Sporočila osvežena.", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void showSelectedMessage(Message selectedMessage) {
         MapView mapView = (MapView) findViewById(R.id.messages_map);
        DialogOverlay selectedMessageDialogOverlay = new DialogOverlay(getResources().getDrawable(R.drawable.ic_map_marker_selected), this);
        mapView.getOverlays().add(selectedMessageDialogOverlay);
         selectedMessageDialogOverlay.addItem(selectedMessage.getLatitude(), selectedMessage.getLongitude(),
                 MessageFormat.format("{0} - {1}", selectedMessage.getAuthor(), DATE_FORMAT.format(selectedMessage.getDate())),
                 selectedMessage.getMessage());
         GeoPoint center = new GeoPoint(selectedMessage.getLatitude(), selectedMessage.getLongitude());
         mapView.getController().animateTo(center);
     }
 }
