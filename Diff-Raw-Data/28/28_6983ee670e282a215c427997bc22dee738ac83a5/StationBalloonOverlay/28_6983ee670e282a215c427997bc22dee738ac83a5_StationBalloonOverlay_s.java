 /**
  * *******************************************************************************************************************
  * <p/>
  * Copyright (C) 8/7/12 by Manuel Palacio
  * <p/>
  * **********************************************************************************************************************
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * <p/>
  * **********************************************************************************************************************
  */
 package net.palacesoft.cngstation.client.mapoverlay;
 
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.net.Uri;
 import android.widget.Toast;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
 import net.palacesoft.cngstation.client.StationActivity;
 
 import java.util.*;
 
 public class StationBalloonOverlay extends BalloonItemizedOverlay<OverlayItem> {
 
     private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
 
     private StationActivity stationActivity;
 
 
     public StationBalloonOverlay(Drawable defaultMarker, StationActivity context, MapView mapView) {
         super(boundCenterBottom(defaultMarker), mapView);
         stationActivity = context;
         populate();
     }
 
     @Override
     protected OverlayItem createItem(int i) {
         return overlayItems.get(i);
     }
 
     @Override
     public int size() {
         return overlayItems.size();
     }
 
     @Override
     protected boolean onBalloonTap(int index, OverlayItem item) {
 
         AlertDialog.Builder dialog = buildDialog((StationOverlayItem) item);
         dialog.show();
         return true;
 
     }
 
     private AlertDialog.Builder buildDialog(final StationOverlayItem item) {
         AlertDialog.Builder dialog = new AlertDialog.Builder(stationActivity);
         dialog.setTitle(item.getTitle());
         final List<CharSequence> items = getItemsToShow(item);
 
 
         setDialogContent(dialog, items);
         setDialogPositiveButton(item, dialog);
         setDialogNegativeButton(dialog);
         return dialog;
     }
 
     private void setDialogNegativeButton(AlertDialog.Builder dialog) {
         dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 dialogInterface.dismiss();
             }
         });
     }
 
     private void setDialogPositiveButton(final StationOverlayItem item, AlertDialog.Builder dialog) {
         dialog.setPositiveButton("Directions", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 try {
                     Intent intent = new Intent(Intent.ACTION_VIEW,
                             Uri.parse("http://maps.google.com/maps?saddr=" + stationActivity.getCurrentLocation().getLatitude()
                                     + "," + stationActivity.getCurrentLocation().getLongitude() + "&daddr="
                                     + item.getLatitude() + "," + item.getLongitude()));
                     stationActivity.startActivity(intent);
                 } catch (Exception e) {
                     Toast.makeText(stationActivity, "Could not show map", Toast.LENGTH_SHORT).show();
 
                 }
             }
         });
     }
 
     private void setDialogContent(AlertDialog.Builder dialog, final List<CharSequence> items) {
         dialog.setItems(items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 if (items.get(item).toString().startsWith("Tel")) {
                     try {
                         String number = items.get(item).toString().split(":")[1];
                         if (number.length() > 1) {
                             Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number.trim()));
                             stationActivity.startActivity(intent);
                         }
                     } catch (Exception e) {
                         Toast.makeText(stationActivity, "Could not make call", Toast.LENGTH_SHORT).show();
                     }
                 }
             }
         });
     }
 
     private List<CharSequence> getItemsToShow(StationOverlayItem item) {
         List<CharSequence> items = new ArrayList<CharSequence>();
         String[] phoneNo = item.getPhoneNo().split(",");
 
         for (String aPhoneNo : phoneNo) {
             items.add("Tel: " + aPhoneNo);
         }
         items.add("Price: " + item.getPrice());
         items.add("Opening hours: " + item.getOpeningHours());
 
         return items;
     }
 
     public void removeOverlay(OverlayItem overlay) {
         overlayItems.remove(overlay);
         populate();
     }
 
     public void clear() {
         overlayItems.clear();
         populate();
     }
 
     public void popupCheapest() {
         Set<OverlayItem> copy = new TreeSet<OverlayItem>(overlayItems);
        OverlayItem cheapest = copy.iterator().next();
        tapOverlay(cheapest);
     }
 
     public void popupClosest() {
         Map<Float, StationOverlayItem> distances = new TreeMap<Float, StationOverlayItem>();
         Location current = stationActivity.getCurrentLocation();
 
         for (OverlayItem overlayItem : overlayItems) {
             StationOverlayItem next = (StationOverlayItem) overlayItem;
             float distance = current.distanceTo(next.getLocation());
             distances.put(distance, next);
         }
 
        tapOverlay(distances.get(distances.keySet().iterator().next()));
     }
 
     private void tapOverlay(OverlayItem item) {
         int index = overlayItems.lastIndexOf(item);
         onTap(index);
     }
 
 
     public void addOverlayItems(List<StationOverlayItem> overlayItems) {
         this.overlayItems.addAll(overlayItems);
         setLastFocusedIndex(-1);
         populate();
     }
 }
