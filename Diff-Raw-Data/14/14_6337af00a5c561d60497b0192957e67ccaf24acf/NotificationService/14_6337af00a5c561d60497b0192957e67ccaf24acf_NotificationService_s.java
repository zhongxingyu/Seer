 package com.moac.android.amilate;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.http.AndroidHttpClient;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.IBinder;
 import android.text.format.Time;
 import android.util.Log;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class NotificationService extends Service {
 
     private static final String TAG = NotificationService.class.getSimpleName();
 
     private static final long FAILURE = -1;
 
     private Calendar calendar = null;
 
     Timer timer;
 
     private Handler handler;
 
     LocationManager locationManager = null;
     LocationProvider locationProvider = null;
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
 
         locationManager = (LocationManager) this
           .getSystemService(Context.LOCATION_SERVICE);
         locationProvider = locationManager
           .getProvider(LocationManager.GPS_PROVIDER);
 
         handler = new Handler();
 
         Log.w(getClass().getName(), "Got to start()!");
         // Kick off a timer that will check calendar every minute
         timer = new Timer();
         timer.schedule(new CheckWatchTimerTask(),
           0,        //initial delay
           120 * 1000);  //subsequent rate
 
         return (START_NOT_STICKY);
     }
 
     @Override
     public void onDestroy() {
         stop();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return (null);
     }
 
     private void showNotification(CalendarEvent event, String text) {
 
         Notification.Builder whiteRabbitNotification = new Notification.Builder(this)
         .setSmallIcon(R.drawable.white_rabbit)
         .setContentTitle(event.getWhat() + " in " + event.getWhere())
         .setContentText(text);
 
         NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         // let's keep it simple
         // use the time as the unique id for the Notification
         int id = ((Long) event.getWhen()).intValue();
         mNotificationManager.notify(id, whiteRabbitNotification.build());
     }
 
     private void stop() {
         Log.w(getClass().getName(), "Got to stop()!");
         timer.cancel();
         stopForeground(true);
     }
 
     class CheckWatchTimerTask extends TimerTask {
 
         public void run() {
 
             Runnable runnable = new Runnable() {
                 @Override
                 public void run() {
                     handler.post(new Runnable() {
                         @Override
                         public void run() {
                             QueryForDirectionsTask task;
                             calendar = new Calendar(getContentResolver());
 
                             ArrayList<CalendarEvent> myEvents = calendar.getAllEvents();
                             for(int i = 0; i < myEvents.size(); i++) {
 
                                 CalendarEvent event = myEvents.get(i);
                                 if(event.isComplete()) {
                                     task = new QueryForDirectionsTask(event);
                                     task.execute();
                                 }
                             }
                         }
                     });
                 }
             };
             new Thread(runnable).start();
         }
     }
 
     private class QueryForDirectionsTask extends AsyncTask<Void, Void, NotificationEvent> {
 
         private CalendarEvent calendarEvent;
         public String eventLocation;
         public long eventTime;
 
         public QueryForDirectionsTask(CalendarEvent event) {
             this.calendarEvent = event;
             this.eventLocation = event.getWhere();
             this.eventTime = event.getWhen() / 1000;
         }
 
 			/*@Override
 			protected void onPreExecute(){
 				answerText.setText("Checking...");
 			}*/
 
         @Override
         protected NotificationEvent doInBackground(Void... params) {
             Log.i(TAG, "Running background directions query with params");
 
             // Let's ask the location provider for our current location
             Location lastKnown = locationManager
               .getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
             Log.i(TAG, "Last known location was: " + lastKnown);
 
             // origin - The address or textual latitude/longitude value from
             // which you wish to calculate directions. If you pass an address as
             // a string, the Directions service will geocode the string and
             // convert it to a latitude/longitude coordinate to calculate
             // directions. If you pass coordinates, ensure that no space exists
             // between the latitude and longitude values.
             //
             // destination - The address or textual latitude/longitude value
             // from which you wish to calculate directions. If you pass an
             // address as a string, the Directions service will geocode the
             // string and convert it to a latitude/longitude coordinate to
             // calculate directions. If you pass coordinates, ensure that no
             // space exists between the latitude and longitude values.
             //
             // arrival_time - specifies the desired time of arrival for transit
             // directions as seconds since midnight, January 1, 1970 UTC. One of
             // departure_time or arrival_time must be specified when requesting
             // transit directions.
 
             // HTTP get
             // Check response code
             // Parse JSON
             // Check directions status
             // Extract time.
             // Compare to current time
             // If event time - travel time > current time .. then YOU'RE
             // LATE!!!
             AndroidHttpClient client = AndroidHttpClient
               .newInstance("com.moac.android.amilate");
 
             try {
 
                 List<NameValuePair> qparams = new ArrayList<NameValuePair>();
                 qparams.add(new BasicNameValuePair("origin", lastKnown
                   .getLatitude() + "," + lastKnown.getLongitude()));
                 qparams.add(new BasicNameValuePair("destination", this.eventLocation));
                 qparams.add(new BasicNameValuePair("arrival_time", Long
                   .toString(this.eventTime)));
                 qparams.add(new BasicNameValuePair("sensor", Boolean.TRUE
                   .toString()));
 
                 URI uri = URIUtils.createURI("http", "maps.googleapis.com", -1,
                   "/maps/api/directions/json",
                   URLEncodedUtils.format(qparams, "UTF-8"), null);
                 HttpGet httpget = new HttpGet(uri);
 
                 Log.i(TAG, "About to make query: "
                   + httpget.getRequestLine().getUri());
 
                 HttpResponse response = client.execute(httpget);
 
                 int statusCode = response.getStatusLine().getStatusCode();
 
                 if(statusCode == 200) {
                     Log.i(TAG, "Successful HTTP Response Google");
 
                     // Now check the JSON value
                     HttpEntity entity = response.getEntity();
                     String jsonString = EntityUtils.toString(entity);
                     JSONObject json = new JSONObject(jsonString);
                     // Log.i(TAG, json.toString());
 
                     String status = json.getString("status");
                     if(status.equals("OK")) {
                         // Let's extract the values of interest.
                         Log.i(TAG, "Got good status from Google: " + status);
 
                         long totalValue = 0;
                         JSONArray routes = json.getJSONArray("routes");
                         // TODO Assumptions about length etc here.
                         JSONArray legs = routes.getJSONObject(0).getJSONArray(
                           "legs");
                         for(int i = 0; i < legs.length(); i++) {
                             // Get the time - DURATION IN SECONDS!!
                             JSONObject duration = legs.getJSONObject(i)
                               .getJSONObject("duration");
                             long value = duration.getLong("value");
                             totalValue += value;
                         }
 
                         Log.i(TAG, "Calculated total duration (sec): "
                           + totalValue);
                         return new NotificationEvent(calendarEvent, Long.valueOf(totalValue));
                     } else {
                         Log.e(TAG, "Got bad status from Google: " + status);
                     }
                 } else {
                     Log.e(TAG, "Got bad status code from Google: " + statusCode);
                 }
             } catch(URISyntaxException e1) {
                 // TODO Auto-generated catch block
                 e1.printStackTrace();
             } catch(IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch(JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } finally {
                 client.close();
             }
 
             return new NotificationEvent(calendarEvent, Long.valueOf(FAILURE));
         }
 
         public void onPostExecute(NotificationEvent notificationEvent) {
 
             long travelTime = notificationEvent.getTravelTime();
             if(travelTime != FAILURE) {
                 Time t = new Time();
                 t.setToNow();
                 long currentSec = t.toMillis(false) / 1000;
 
                 Log.i(TAG, "Current time in seconds since epoch is: " + currentSec);
 
                 // Now we work out if we're ok.
                 // Event Time - travel time
                 long leaveTime = eventTime - travelTime;
                 if(leaveTime > currentSec) {
                     Log.i(TAG, notificationEvent.getCalendarEvent().getEventDetails() + " On time! :)");
                     // answerText.setText("You're on time");
                 } else {
                     long lateBy = currentSec - leaveTime;
                     String lateText = "Let's go!\n"
                       + "It takes " + String.valueOf(travelTime / 60) + " minutes to get there!";
                     Log.i(TAG, notificationEvent.getCalendarEvent().getEventDetails() + " " + lateText);
                     showNotification(calendarEvent, lateText);
                    //answerText.setText("You're late by " + (lateBy / 60) + " minutes.\n"
                    //	+ "It takes " + (travelTime.longValue() / 60) + " minutes to get there!");
                 }
             } else {
                 Log.i(TAG, "Sorry, I can't tell you...");
                //answerText.setText("Sorry, I can't tell you...");
             }
         }
     }
 }
