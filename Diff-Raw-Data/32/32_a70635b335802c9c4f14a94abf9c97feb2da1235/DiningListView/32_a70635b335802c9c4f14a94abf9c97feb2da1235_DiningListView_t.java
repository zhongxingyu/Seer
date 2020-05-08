 package org.vandymobile.dining;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.vandymobile.dining.util.Locations;
 import org.vandymobile.dining.util.Restaurant;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
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
         private int id;
         }
     //public static Integer[] planFilter = {3,7,9,11,12,13,15,16,20,27,28,29,30,37,38,39,41,42,43,44,45};
     private IconicAdapter mCurAdapter;
     private GeoPoint curLoc = null;
     private Time now;
     private static Locations loc;
     private int mClosestLoc;
     private boolean sortByOpen;
     private boolean sortByName;
     private boolean sortByDistance;
     private boolean sortByPlan;
     private int[] curIdList;
     private int secondPartitionId;
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_dining_list_view);
         now = new Time();
         now.setToNow();
         
         loc = Locations.getInstance(getApplicationContext());
         
         LocationManager _locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         
         MyLocationListener _LocationListener = new MyLocationListener();
         _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, _LocationListener);//get current GPS location into a listener
         
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
             Toast.makeText(getApplicationContext(), "Couldn't resolve location - using default", Toast.LENGTH_LONG).show();
             curLoc = new GeoPoint(36143091, -86804699); //defaults to Vanderbilt if the current position cannot be determined
         }
         
         //Add 3 for the 2 partitions and the firstloc
         String[] adapterInput = new String[loc.mCount + 3];
         mCurAdapter = new IconicAdapter(this, adapterInput);
         setListAdapter(mCurAdapter);
         setSortOpen();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_dining_list_view, menu);
         
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         if (item.getItemId() == R.id.menu_sort_open){
             setSortOpen();
         } else if (item.getItemId() == R.id.menu_sort_name){
             setSortName();
         } else if (item.getItemId() == R.id.menu_sort_distance){
             setSortDistance();
         } else if (item.getItemId() == R.id.menu_sort_plan){
             setSortPlan();
         }
         return true;
     }
     
    
    /**
     * This method is triggered when an item in the list is clicked
     * @param v The view which was clicked
     */
     public void onListItemClick(ListView parent, View v, int position, long id) {
         ViewHolder holder = (ViewHolder)v.getTag();
         int _id = holder.id;
         startRestaurantDetails(_id);
     }
     
    /**
     * This method is used when an item in the list is clicked.
     * It launches a detail activity with more details about the clicked item
     * @param id The id for the restaurant we are launching
     */
     public void startRestaurantDetails(int id){
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
         Intent intent = new Intent(getApplicationContext(), Menus.class);
         startActivity(intent);
     }
     public void happyClick(View v){
         //TODO implement this
     }
     
     /**
      * Determines which is the closest open dining location to your current position.
      * @return: the id of the closest open dining location
      */
     private int getClosestOpen(){
    	Restaurant current = loc.mLocations[0];
        int id = current.mId;
        double smallest = getDistance(curLoc, current.mLocation);
        for (int i = 1; i < loc.mCount; i++){
            current = loc.mLocations[i];
             if (current.isOpen()){
                 double distance = getDistance(curLoc, current.mLocation);
                 if (distance < smallest){
                     smallest = distance;
                     id = current.mId;
                 }
             }
         }
         return id;
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
      * Generates a String array from a String containing a comma- and semicolon-separated list of times
      * @param _in: a String containing a list of open and close times (e.g. "10:00,14:00;17:30,23:30")
      * @return: a String array containing the time values split up (e.g. {"10:00","14:00","17:30","23:30"})
      */
     public static String[] parseHours(String _in){
         String[] ret = {null,null,null,null};
         if (_in.equals("null")){
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
     public static Time calcTime(String sTime, Time cur){
         //TODO: make this function actually figure out which day it should be (possible?)
         
         // *****************************************************************************************************************
         // BE CAREFUL USING THIS METHOD. Currently it assumes that any time which is before 5:00am is a closing time and 
         //         occurs on the following day (if the current time is after 5:00am)
         // *****************************************************************************************************************
             // (e.g. if it is currently 11pm and Sam's Sports Bar closes at 3am "today", 
            //    the Time object returned will be referencing tomorrow's date)
         
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
     public static String isOpen(String[] hours, Time cur){ //TODO: there is an error in the logic of this method (or calcTime, perhaps)
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
     
     private int[] getOpenList(){
         int[] ret = new int[loc.mCount];
         int[] tmp = new int[loc.mCount];
         int retcount = 0;
         int tmpcount = 0;
         for (int i = 0; i < loc.mCount; i++){
             Restaurant cur = loc.mLocations[i];
             if (cur.isOpen()){
                 ret[retcount] = cur.mId;
                 retcount++;
             } else {
                 tmp[tmpcount] = cur.mId;
                 tmpcount++;
             }
         }
         //add 2 for the initial 2 views in the list 
         //(closest and first partition)
         secondPartitionId = retcount + 2;
         for (int i = 0; i < tmpcount; i++){
             ret[retcount] = tmp[i];
             retcount++;
         }
         return ret;
     }
     
     private int[] getNameList(){
         int[] ret = new int[loc.mCount];
         for (int i = 0; i < loc.mCount; i++){
             ret[i] = loc.mLocations[i].mId;
         }
         secondPartitionId = loc.mCount + 2;
         return ret;
     }
     
     private int[] getDistanceList(){
         Map<Double,Integer> values = new HashMap<Double,Integer>();
         double[] dists = new double[loc.mCount];
         for (int i = 0; i < loc.mCount; i++){
             dists[i] = getDistance(curLoc, loc.mLocations[i].mLocation);
             values.put(dists[i], loc.mLocations[i].mId);
         }
         Arrays.sort(dists);
         int[] ret = new int[loc.mCount];
         for (int i = 0; i < loc.mCount; i++){
             ret[i] = values.get(dists[i]);
         }
         secondPartitionId = loc.mCount + 2;
         return ret;
     }
     
     private int[] getPlanList(){
         int[] ret = new int[loc.mCount];
         int[] tmp = new int[loc.mCount];
         int retcount = 0;
         int tmpcount = 0;
         for (int i = 0; i < loc.mCount; i++){
             Restaurant cur = loc.mLocations[i];
             if (cur.mMealPlan){
                 ret[retcount] = cur.mId;
                 retcount++;
             } else {
                 tmp[tmpcount] = cur.mId;
                 tmpcount++;
             }
         }
         //add 2 for the initial 2 views in the list 
         //(closest and first partition)
         secondPartitionId = retcount + 2;
         for (int i = 0; i < tmpcount; i++){
             ret[retcount] = tmp[i];
             retcount++;
         }
         return ret;
     }
     
     private String firstPartitionTitle;
     private String secondPartitionTitle;
     
     private void setSortOpen(){
         if (!sortByOpen){
             curIdList = getOpenList();
             sortByName = false;
             sortByDistance = false;
             sortByPlan = false;
             sortByOpen = true;
             firstPartitionTitle = "Open";
             secondPartitionTitle = "Closed";
             mClosestLoc = getClosestOpen();
             mCurAdapter.notifyDataSetChanged();
         }
     }
     
     private void setSortName(){
         if (!sortByName){
             curIdList = getNameList();
             sortByName = true;
             sortByDistance = false;
             sortByPlan = false;
             sortByOpen = false;
             mClosestLoc = getClosestOpen();
             mCurAdapter.notifyDataSetChanged();
         }
     }
     
     private void setSortDistance(){
         if (!sortByDistance){
             curIdList = getDistanceList();
             sortByName = false;
             sortByDistance = true;
             sortByPlan = false;
             sortByOpen = false;
             mClosestLoc = getClosestOpen();
             mCurAdapter.notifyDataSetChanged();
         }
     }
     
     private void setSortPlan(){
         if (!sortByPlan){
             curIdList = getPlanList();
             sortByName = false;
             sortByDistance = false;
             sortByPlan = true;
             sortByOpen = false;
             firstPartitionTitle = "Meal Plan";
             secondPartitionTitle = "Not Meal Plan";
             mClosestLoc = getClosestOpen();
             mCurAdapter.notifyDataSetChanged();
         }
     }
     
     class IconicAdapter extends ArrayAdapter<String> {
         Activity context;
         
         IconicAdapter(Activity context, String[] adapterInput) {
             super(context, R.layout.row, adapterInput);
             this.context=context;
             mClosestLoc = getClosestOpen();
             }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder;
             
             //The adapter is created with enough slots for all the locations, plus 2 partitions and the firstloc
             //if we are on a sort mode which only requires one partition, return an empty view for the very last slot.
             if (position == (loc.mCount + 2) && (!sortByPlan || !sortByOpen)){
             	return new View(getApplicationContext());
             }
             
             if (position == 0){
                 //inflate the special layout for the top item
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
                 //if there is no view or if the view is not the normal row view
                 if (convertView == null || convertView.getId() == R.id.badid || convertView.getTag() == null) {
                     LayoutInflater inflater=context.getLayoutInflater();
                     convertView=inflater.inflate(R.layout.row, null);
                     holder = new ViewHolder();
                     holder.tvTitle = (TextView) convertView.findViewById(R.id.tvtitle);
                     holder.tvDesc = (TextView) convertView.findViewById(R.id.tvdesc);
                     holder.tvDist = (TextView) convertView.findViewById(R.id.tvdist);
                     holder.tvStatus = (TextView) convertView.findViewById(R.id.tvstatus);
                     holder.imgView = (ImageView) convertView.findViewById(R.id.image);
                     convertView.setTag(holder);
                     
                 /*} else if (convertView.getId() == R.id.badid) {
                     //TODO test if this is necessary
                     LayoutInflater inflater=context.getLayoutInflater();
                     convertView=inflater.inflate(R.layout.row, null);
                     holder = new ViewHolder();
                     holder.tvTitle = (TextView) convertView.findViewById(R.id.tvtitle);
                     holder.tvDesc = (TextView) convertView.findViewById(R.id.tvdesc);
                     holder.tvDist = (TextView) convertView.findViewById(R.id.tvdist);
                     holder.tvStatus = (TextView) convertView.findViewById(R.id.tvstatus);
                     holder.imgView = (ImageView) convertView.findViewById(R.id.image);
                     convertView.setTag(holder);
                     
                 */} else {
                     //if we got back a usable normal recycled view, grab the tag
                     holder = (ViewHolder) convertView.getTag();
                 }
             }
             
             
             /*
             //TODO clean this mess up
             //TODO possible solution: have the closest location show up at the top but ALSO
             // in its normal position in the list. This should make sorting much easier
             boolean isFirstPos = false;
             if (position == 0){
                 position = mClosestLoc;
                 isFirstPos = true;
             } else if(position == 1){
                 TextView partition;
                 partition = createPartition(firstPartitionTitle);
                 return partition;
             } else if (position > 0 && position <= mClosestLoc){// to account for the closest loc box
                 position--;
             }//TODO clean this mess up
             
             if (!isFirstPos && position >= 1){// to account for the 'Open' partition
                 position--;
             }//TODO clean this mess up
             
             if (!isFirstPos && position == secondPartitionId){ //this goes after the others because the value needs to be checked 
                                                 //after position is adjusted for the other things which were added
                 TextView partition;
                 partition = createPartition(secondPartitionTitle);
                 return partition;
             } else if (!isFirstPos && position > secondPartitionId){
                 position--;
             }//TODO clean this mess up*/
             
             String[] tempHours;
             int hoursDay = now.weekDay;
             if (now.hour <= 4){ //this call assumes that if it is before 5am you want the hours for YESTERDAY
                                 //e.g. if it is 1:00am on a Tuesday, you want to look at Monday's hours for locations
                 hoursDay--;
                 if (hoursDay == -1){
                     hoursDay = 6;
                 }
             }
 
             
             /*
              * Pseudo-code logic for determining position
              * if position is 0, calculate everything using mClosestLoc instead of position
              * if position is 1, make a partition and nothing else
              * if position is 2 or greater, but less than the position of the second partition, 
              *     calculate using (position-2)
              * if position is equal to the position of the second partition, make a partition
              * if position is greater than the position of the second partition, calculate using (position-3)
              */
             if (position < 0){
             	//This should never happen
             	Toast.makeText(getApplicationContext(), "Bad Position; aborting", Toast.LENGTH_LONG).show();
             	return createPartition("ERROR!!");
             }
             
             
             boolean isFirstPos = false;
             if (position == 0){
             	position = mClosestLoc;
             	isFirstPos = true;
             } else if (position == 1){
                 TextView partition;
                 partition = createPartition(firstPartitionTitle);
                 return partition;
             } else if (position > 1 && position < secondPartitionId){
             	position = position - 2;
             } else if (position == secondPartitionId){
                 TextView partition;
                 partition = createPartition(secondPartitionTitle);
                 return partition;
             } else if (position > secondPartitionId){
             	position = position - 3;
             }
             
             
             Restaurant currentRestaurant;
             if (isFirstPos){
                 currentRestaurant = loc.findRestaurantById(mClosestLoc); 
                 //if we enter this block then position = mClosestLoc
             }else{
                 currentRestaurant = loc.findRestaurantById(curIdList[position]);
             }       
             
             tempHours = parseHours(currentRestaurant.getHours(hoursDay));//using our already-initialized Time object is 
                                                                                 //less resource-intensive than having the Location
                                                                                 //object make its own
             String tempName = "this is a default value";
 
             tempName = currentRestaurant.mName;
             String description = currentRestaurant.mDescription;
             
             holder.tvDist.setText(currentRestaurant.getDistance(curLoc) + " mi away");
             holder.id = currentRestaurant.mId;
             holder.tvTitle.setText(tempName);
             holder.tvStatus.setText(isOpen(tempHours, now));
             holder.tvDesc.setText(description); 
             holder.imgView.setImageResource(R.drawable.compass); //TODO fix temp value
 
             return(convertView);         
         }
         
         private TextView createPartition(String text){
             TextView partition;
             partition = new TextView(context);
             partition.setBackgroundResource(android.R.drawable.dark_header);
             partition.setGravity(Gravity.CENTER_VERTICAL);
             partition.setFocusable(false);
             partition.setClickable(false);//TODO figure out why the partition is clickable...
             partition.setLongClickable(false);
             partition.setOnClickListener(new View.OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {//Temporary so the app doesn't crash when partitions are clicked
                 }
             });
             partition.setTextSize((float) 14.0);
             partition.setTypeface(Typeface.DEFAULT_BOLD);
             partition.setText(text);
             return partition;
         }
         
         @Override
         public boolean isEnabled(int i) {
             if (i == 1){
                 return false;
             } else if (i == 0){
                 return true;
             }
             if (i < mClosestLoc){
                 i = i - 2;
             } else if (i > mClosestLoc){
                 i--;
             }
             if (i == secondPartitionId){
                 return false;
             }
             return true;
         }
     }
 
     public class MyLocationListener implements LocationListener{
         int count = 0;
         public void onLocationChanged(Location loc) {
             count++;
             if (count > 5){
                 GeoPoint locPoint = new GeoPoint((int)(loc.getLatitude()*1000000),(int)(loc.getLongitude()*1000000));
                 curLoc = locPoint;
                 count = 0;
             }
         }
         
         public void onProviderDisabled(String provider) {
         }
 
         public void onProviderEnabled(String provider) {
         }
 
         public void onStatusChanged(String provider,
             int status, Bundle extras) {
         }
     }
 }
