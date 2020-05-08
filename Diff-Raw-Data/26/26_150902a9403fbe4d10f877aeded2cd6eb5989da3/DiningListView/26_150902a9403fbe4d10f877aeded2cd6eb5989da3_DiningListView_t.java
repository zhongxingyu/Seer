 package org.vandymobile.dining;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.Calendar;
 
 import org.vandymobile.dining.util.Locations;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 
 /**
  * @author Matthew Lavin
  */
 
 public class DiningListView extends ListActivity {
     public static class ViewHolder {
         public TextView tvTitle, tvDesc, tvDist, tvStatus;
         private ImageView imgView;
         }
     public static Integer[] RestaurantMap = {24,25,26,7,27,28,29,4,30,3,31,11,12,23,32,22,5,18,33,34,17,35,36,37,38,39,40,9,8,1,10,41,42,43,44,45,46,15,16,14,47,2,6,13,19,21,20,48,49,50};
     private static DatabaseHelper myDbHelper;
     private static SQLiteDatabase diningDatabase;
     private String[] adapterInput;
     public static Cursor locCursor;
     private GeoPoint curLoc = null;
     private Time now;
     private static Locations loc;
     int curDay;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_dining_list_view);
         now = new Time();
         now.setToNow();
 
         curDay = now.weekDay + 1;// to match up with the Calendar class' day numbering scheme
         
         myDbHelper = new DatabaseHelper(this);
          
         try {
             myDbHelper.createDataBase();
         } catch (IOException ioe) {
             throw new Error("Unable to create database");
         }
          
         try {
             myDbHelper.openDataBase();
         } catch(SQLException sqle) {
             throw sqle;
         }
         diningDatabase = myDbHelper.getReadableDatabase();
         
         String[] tmp2 = {"_id"};
         Cursor adapterCursor = diningDatabase.query("dining", tmp2, null, null, null, null, null);
         adapterInput = new String[adapterCursor.getCount()];
         adapterCursor.close();
         
         String[] tmp = {"lat", "long", "name"};
         locCursor = diningDatabase.query("dining", tmp, null, null, null, null, "name");
         
         loc = Locations.getInstance(getApplicationContext());
         
         LocationManager _locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         Location x = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if (x == null){
             x = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         }
         if (x == null){
             x = _locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
         }
         if (x != null){
             curLoc = new GeoPoint((int)(x.getLatitude()*1000000), 
                              (int)(x.getLongitude()*1000000));//current position
         } else {
             Toast.makeText(getApplicationContext(), "Couldn't get location - defaulting", Toast.LENGTH_SHORT).show();
             curLoc = new GeoPoint(36143091, -86804699); //defaults to Vanderbilt if the current position cannot be determined
         }        
         setListAdapter(new IconicAdapter(this));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_dining_list_view, menu);
         return true;
     }
     
     public void onListItemClick(ListView parent, View v, int position, long id) {
         startRestaurantDetails(position, id);
     }
     
     public void startRestaurantDetails(int position, long id){
         //Toast.makeText(getApplicationContext(), "["+position+"]:["+id+"]", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(getApplicationContext(), LocationDetails.class).putExtra("id", id);
         startActivity(intent);
     }
 
     public void homeClick(View v){
         // Already at home - do nothing
     }
     public void mapsClick(View v){
         Intent _int = new Intent(getApplicationContext(), DiningMap.class);
         startActivity(_int);
     }
     public void menuClick(View v){
         //TODO implement this
     }
     public void happyClick(View v){
         //TODO implement this
     }
     
     /**
      * getDistance: determines the distance, in miles, between two GeoPoint objects
      * @param pointA: the first GeoPoint
      * @param pointB: the second GeoPoint
      * @return: the distance, in miles, between the two points. Is a float. 
      */
     public double getDistance(GeoPoint pointA, GeoPoint pointB){
         Location locationA = new Location("point A");
     
         locationA.setLatitude(pointA.getLatitudeE6() / 1E6);
         locationA.setLongitude(pointA.getLongitudeE6() / 1E6);
     
         Location locationB = new Location("point B");
     
         locationB.setLatitude(pointB.getLatitudeE6() / 1E6);
         locationB.setLongitude(pointB.getLongitudeE6() / 1E6);
     
         return (locationA.distanceTo(locationB)/1609.34);//return in miles, not meters
     }
 
     /**
      * roundDouble: Takes in a Double value and rounds it to a single decimal place
      * @param d: the Double value
      * @return: The double value, rounded to have one place after the decimal
      */
     double roundDouble(double d) {
         DecimalFormat twoDForm = new DecimalFormat("#.#");
     return Double.valueOf(twoDForm.format(d));
 }
     
     /**
      * getHours: Queries the database and returns the hours for a location for the current day
      * @param i: an integer representing the current day, from Sunday to Saturday, where Sunday is '1' and Saturday is '7'
      * @param _db: the diningDatabase which should be queried
      * @param id: the id of the location whose hours are being requested
      * @return: a Cursor which contains the hours for the given location and day
      */
     private Cursor getHours(int i, SQLiteDatabase _db, int id){
         switch (i){
             case Calendar.SUNDAY:
                 String[] tmp = {"sunday_hours"};
                 return _db.query("dining", tmp, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             case Calendar.MONDAY:
                 String[] tmp1 = {"monday_hours"};
                 return _db.query("dining", tmp1, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             case Calendar.TUESDAY:
                 String[] tmp2 = {"tuesday_hours"};
                 return _db.query("dining", tmp2, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             case Calendar.WEDNESDAY:
                 String[] tmp3 = {"wednesday_hours"};
                 return _db.query("dining", tmp3, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             case Calendar.THURSDAY:
                 String[] tmp4 = {"thursday_hours"};
                 return _db.query("dining", tmp4, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             case Calendar.FRIDAY:
                 String[] tmp5 = {"friday_hours"};
                 return _db.query("dining", tmp5, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             case Calendar.SATURDAY:
                 String[] tmp6 = {"saturday_hours"};
                 return _db.query("dining", tmp6, "_id = "+DiningListView.RestaurantMap[id], null, null, null, null);
             default:
                 return null;
         }
     }
     
     /** 
      * Generates a String array from a String containing a comma- and semicolon-separated list of times
      * @param _in: a String containing a list of open and close times (e.g. "10:00,14:00;17:30,23:30")
      * @return: a String array containing the time values split up (e.g. {"10:00","14:00","17:30","23:30"})
      */
     private String[] parseHours(String _in){
         String[] ret = {null,null,null,null};
         if (_in == "null"){
             return ret;
         }
         for (int i = 0; i < _in.length(); i++){
             if (_in.charAt(i)== ','){
                 ret[0]= _in.substring(0, i);//once you reach the first comma, break off the first start hour into ret[0]
                 for (int x = i; x < _in.length();x++){
                     if (_in.charAt(x) == ';'){//if you hit a semicolon, there are two start and end times today. 
                                               //Don't worry, we'll use recursion!
                         ret[1] = _in.substring(i+1,x);
                         String[] tmp2 = parseHours(_in.substring(x+1));
                         ret[2] = tmp2[0]; ret[3] = tmp2[1];
                         break;
                     } else if (x == _in.length() -1){//if you hit the end of the string and haven't found a semicolon, don't worry! 
                                                      //there are only one set of hours for today.
 
                         ret[1] = _in.substring(i+1);//grab the second hour string into ret[2] - 
                                                                     //don't worry about ret[3] and ret[4], we'll check for null later
                     }
                 }
                 break;
             }
         }
         
         return ret;
     }
     
     /** 
      * Generates a Time object from a given String containing a time (e.g. "7:00")
      * @param sTime: a string containing one time value
      * @param cur: A Time object which contains the current time
      * @return: a Time object which is set to the time sTime represents and either today's or tomorrow's date (see below)
      */
     private Time calcTime(String sTime, Time cur){
         //TODO: make this function actually figure out which day it should be (possible?)
         
         // *****************************************************************************************************************
         // BE CAREFUL USING THIS METHOD. Currently it assumes that any time which is before 5:00am is a closing time and 
         //         occurs on the following day (if the current time is after 5:00am)
         // *****************************************************************************************************************
             // (e.g. if it is currently 11pm and Sam's Sports Bar closes at 3am today, 
                     //the Time object returned will be referencing tomorrow's date)
         
         Time iTime = new Time(Time.getCurrentTimezone());
         Integer hour = null;
         Integer minute = null;
         for (int i = 0; i < sTime.length(); i++){
             if (sTime.charAt(i)==':'){
                 hour = Integer.parseInt(sTime.substring(0, i));
                 minute = Integer.parseInt(sTime.substring(i+1));
                 break;
             }
         }
         if (hour <= 4){ //if the hour time is before 5am...
             if (cur.hour <= 4){// ...and the current time is also, assume it is for today
                 iTime.set(0, minute, hour, cur.monthDay, cur.month, cur.year);
             } else { // ...and the current time is after 5am, assume it is for tomorrow
                 iTime.set(0, minute, hour, cur.monthDay+1, cur.month, cur.year);
             }
         } else {// if time is after 5am, assume it is for today
             iTime.set(0, minute, hour, cur.monthDay, cur.month, cur.year);
         }
         iTime.normalize(true); //fix the object if, for example, adding 1 to the monthDay made it December 32 or April 31.
         return iTime;
     }
     
     /**
      * isOpen: determines whether a location is open using its open/close times and the current time
      * @param hours: a String array containing the open and close times of a restaurant
      * @param cur: a Time object which represents the current time
      * @return: a String which represents the current state of the location
      */
     private String isOpen(String[] hours, Time cur){
         if (hours[0] == null){
             return "Closed";
         }
         
         if (hours[0].equals(hours[1]) && hours[0].equals("7:00")){
             return "Open 24/7!";
         }
         
         boolean isSimple = (hours[2] == null); // this will determine if the restaurant has more than one opening/closing time today
         Time firstOpen = calcTime(hours[0], cur);
         Time firstClose = calcTime(hours[1], cur);
         
         Time secondOpen = null;
         Time secondClose = null;
         if (!isSimple){
             secondOpen = calcTime(hours[2], cur);
             secondClose = calcTime(hours[3], cur);
         }
 
         Time temp = new Time(Time.getCurrentTimezone());
         temp.set(cur.second, cur.minute+30, cur.hour, cur.monthDay, cur.month, cur.year); // set to 30 minutes in the future
         temp.normalize(false); //this temp var will be used to check for things which are opening or closing soon
         
         if (firstOpen.after(cur)){
             if (firstOpen.after(temp)){
                 return "Closed";
             } else {
                 return "Opening in " + ((firstOpen.toMillis(false) - cur.toMillis(false))/60000 ) + " minutes!";
             }
         } else if ((firstOpen.before(cur)) && (firstClose.after(cur))){
             if (firstClose.after(temp)){
                 return "Open!";
             } else {
                 return "Closing in " + ((firstClose.toMillis(false) - cur.toMillis(false))/60000 ) + " minutes!";
             }
         } else if (firstClose.before(cur)){
             if (isSimple){
                 return "Closed";
             } else {
                 if (secondOpen.after(cur)){
                     if (secondOpen.after(temp)){
                         return "Closed";
                     } else {
                         return "Opening in " + ((secondOpen.toMillis(false) - cur.toMillis(false))/60000 ) + " minutes!";
                     }
                 } else if ((secondOpen.before(cur)) && (secondClose.after(cur))){
                     if (secondClose.after(temp)){
                         return "Open!";
                     } else {
                         return "Closing in " + ((secondClose.toMillis(false) - cur.toMillis(false))/60000 ) + " minutes!";
                     }
                 } else if (secondClose.before(cur)){
                     return "Closed";
                 }
             }
         } 
         return "Invalid hours input";
     }
     
     
     class IconicAdapter extends ArrayAdapter<String> { 
         Activity context;
         
         IconicAdapter(Activity context) {
             super(context, R.layout.row, adapterInput);
             this.context=context; 
             String[] tmp = {"name","type"};
             }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder;
             if (position == 0){
                 LayoutInflater inflater=context.getLayoutInflater();
                 convertView=inflater.inflate(R.layout.onerow, null);
                 holder = new ViewHolder();
                 holder.tvTitle = (TextView) convertView.findViewById(R.id.tvtitle);
                 holder.tvDesc = (TextView) convertView.findViewById(R.id.tvdesc);
                 holder.tvDist = (TextView) convertView.findViewById(R.id.tvdist);
                 holder.tvStatus = (TextView) convertView.findViewById(R.id.tvstatus);
                 holder.imgView = (ImageView) convertView.findViewById(R.id.image);
                 convertView.setTag(holder);
             } else {
                 if (convertView == null) {
                     LayoutInflater inflater=context.getLayoutInflater();
                     convertView=inflater.inflate(R.layout.row, null);
                     holder = new ViewHolder();
                     holder.tvTitle = (TextView) convertView.findViewById(R.id.tvtitle);
                     holder.tvDesc = (TextView) convertView.findViewById(R.id.tvdesc);
                     holder.tvDist = (TextView) convertView.findViewById(R.id.tvdist);
                     holder.tvStatus = (TextView) convertView.findViewById(R.id.tvstatus);
                     holder.imgView = (ImageView) convertView.findViewById(R.id.image);
                     convertView.setTag(holder);
                     
                 } else if (convertView.getId() == R.id.badid) {
                     LayoutInflater inflater=context.getLayoutInflater();
                     convertView=inflater.inflate(R.layout.row, null);
                     holder = new ViewHolder();
                     holder.tvTitle = (TextView) convertView.findViewById(R.id.tvtitle);
                     holder.tvDesc = (TextView) convertView.findViewById(R.id.tvdesc);
                     holder.tvDist = (TextView) convertView.findViewById(R.id.tvdist);
                     holder.tvStatus = (TextView) convertView.findViewById(R.id.tvstatus);
                     holder.imgView = (ImageView) convertView.findViewById(R.id.image);
                     convertView.setTag(holder);
                     
                 } else {
                     holder = (ViewHolder) convertView.getTag();
                 }
             }
 
             
             String[] tempHours;
             int hoursDay = curDay;
             if (now.hour <= 4){ //this getHours call assumes that if it is before 5am you want the hours for YESTERDAY
                                 //e.g. if it is 1:00am on a Tuesday, you want to look at Monday's hours for locations
                 hoursDay--;
             }
             Cursor hoursCursor = getHours(hoursDay, diningDatabase, position);
             hoursCursor.moveToFirst();//initialize the cursor
             tempHours = parseHours(hoursCursor.getString(0));
             hoursCursor.close();
             
             String tempName = "this is a default value";

            tempName = loc.mLocations[position].mName;
            String description = loc.mLocations[position].mDescription;
 
             if (locCursor.moveToPosition(position)){
                 GeoPoint thisLocation = new GeoPoint((int)(locCursor.getFloat(0)*1000000), (int)(locCursor.getFloat(1)*1000000));
                 holder.tvDist.setText(roundDouble(getDistance(thisLocation, curLoc))+" mi away");
             }
             
             holder.tvTitle.setText(tempName);
             holder.tvStatus.setText(isOpen(tempHours, now));
             holder.tvDesc.setText(description); 
             holder.imgView.setImageResource(R.drawable.compass); //TODO fix temp value
 
             return(convertView);         
         }
     }
 
     
     
 }
