 package com.squirrel.location;
 
 import android.content.Context;
 import android.location.Location;
 
 import com.google.android.gms.location.Geofence;
 import com.google.android.gms.location.LocationClient;
 import com.squirrel.action.Action;
 import com.squirrel.domain.Reminder;
 
 import java.util.ArrayList;
 
 public class GeofenceCreator
 {
     private final Context context;
     private final GeofenceRegistrar geofenceRegistrar;
 
     public GeofenceCreator(Context context)
     {
         this.context = context;
         this.geofenceRegistrar = new GeofenceRegistrar(context);
     }
 
    private final float GEOFENCE_RADIUS_METRES = 300;
 
     public void CreateFromReminders(ArrayList<Reminder> reminders)
     {
         ArrayList<Geofence> reminderGeofences = new ArrayList<Geofence>();
 
         for(Reminder reminder : reminders)
         {
             if(reminder.action == Action.SmartReminder_Event_LocationEnter || reminder.action == Action.SmartReminder_Event_LocationLeave)
             {
                 Geofence geofence = this.createGeofence(reminder);
 
                 reminderGeofences.add(geofence);
             }
         }
 
         if(!reminderGeofences.isEmpty())
         {
             this.geofenceRegistrar.registerGeofences(reminderGeofences);
         }
     }
 
     private Geofence createGeofence(Reminder reminder)
     {
         double latitude = parseLatitude(reminder.latLong);
         double longitude = parseLongitude(reminder.latLong);
 
         Geofence.Builder builder = new Geofence.Builder();
 
         builder.setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_METRES);
         builder.setRequestId(String.valueOf(reminder.id));
         builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
 
         int transition = reminder.action == Action.SmartReminder_Event_LocationEnter ? Geofence.GEOFENCE_TRANSITION_ENTER : Geofence.GEOFENCE_TRANSITION_EXIT;
         builder.setTransitionTypes(transition);
 
         Geofence geofence = builder.build();
 
         return geofence;
     }
 
     private double parseLatitude(String location)
     {
         return Location.convert(location.split(",")[0]);
     }
 
     private double parseLongitude(String location)
     {
         return Location.convert(location.split(",")[1]);
     }
 }
