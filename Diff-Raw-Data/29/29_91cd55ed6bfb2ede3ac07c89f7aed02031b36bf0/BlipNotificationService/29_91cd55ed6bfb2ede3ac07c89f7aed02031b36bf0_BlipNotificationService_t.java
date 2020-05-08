 /*
  * Copyright (c) 2010 BlipIt Committers
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package com.thoughtworks.blipit.services;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.os.SystemClock;
 import android.util.Log;
 import com.google.android.maps.GeoPoint;
 import com.thoughtworks.blipit.utils.BlipItServiceHelper;
 import com.thoughtworks.blipit.utils.BlipItUtils;
 import com.thoughtworks.contract.Blip;
 import com.thoughtworks.contract.BlipItRequest;
 import com.thoughtworks.contract.BlipItResponse;
 import com.thoughtworks.contract.BlipItSubscribeResource;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
import static com.thoughtworks.blipit.utils.BlipItUtils.APP_TAG;
 import static com.thoughtworks.blipit.utils.BlipItUtils.getMessageWithBlips;
 
 public class BlipNotificationService extends IntentService {
     private final List<Messenger> clients;
     private final Messenger clientMessenger;
     private PendingIntent pendingIntent;
     private AlarmManager alarmManager;
     private GeoPoint currentUserLocation;
 
     public BlipNotificationService() {
         super("BlipNotificationServiceThread");
         clients = new ArrayList<Messenger>();
         clientMessenger = new Messenger(new BlipNotificationServiceHandler(this));
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return clientMessenger.getBinder();
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         scheduleNotificationService();
        Log.i(APP_TAG, "Blip notification service started");
     }
 
     private void scheduleNotificationService() {
         pendingIntent = PendingIntent.getService(this, 0, new Intent(this, BlipNotificationService.class), 0);
         alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
         long firstTime = SystemClock.elapsedRealtime();
         alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 5 * 1000, pendingIntent);
     }
 
     @Override
     public void onDestroy() {
         alarmManager.cancel(pendingIntent);
        Log.i(APP_TAG, "Blip notification service stopped");
     }
 
     public void addClient(Messenger messenger) {
         this.clients.add(messenger);
     }
 
     public void removeClient(Messenger messenger) {
         this.clients.remove(messenger);
     }
 
     public synchronized void setCurrentUserLocation(GeoPoint currentUserLocation) {
         this.currentUserLocation = currentUserLocation;
        Log.i(APP_TAG, "User location updated");
     }
 
     public synchronized GeoPoint getCurrentUserLocation() {
         return currentUserLocation;
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         try {
             BlipItSubscribeResource blipItSubscribeResource = new BlipItServiceHelper().getBlipItResource();
             // TODO: Construct the correct request here with user prefs
             BlipItRequest blipItRequest = new BlipItRequest();
             BlipItResponse blipItResponse = blipItSubscribeResource.getBlips(blipItRequest);
             if (blipItResponse.hasNoError()) {
                 Message message = getMessageWithBlips((ArrayList<Blip>) blipItResponse.getBlips(), BlipItUtils.MSG_BLIPS_UPDATED);
                 for (Iterator<Messenger> iterator = clients.iterator(); iterator.hasNext();) {
                     Messenger client = iterator.next();
                     try {
                         client.send(message);
                     } catch (RemoteException e) {
                         Log.e(BlipItUtils.APP_TAG, "An error occurred while sending blips to client", e);
                         // The client is dead.  Remove it from the list;
                         iterator.remove();
                     }
                 }
             }
         } catch (Exception e) {
             Log.e(BlipItUtils.APP_TAG, "An error occurred while retrieving blips", e);
         }
     }
 
 }
