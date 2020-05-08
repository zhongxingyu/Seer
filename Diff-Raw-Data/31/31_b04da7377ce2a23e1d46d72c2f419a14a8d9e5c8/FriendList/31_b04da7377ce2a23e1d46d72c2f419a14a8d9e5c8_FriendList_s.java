 package com.porknbunny.friendcompass;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pigsnowball
  * Date: 20/11/2011
  * Time: 01:51
  * To change this template use File | Settings | File Templates.
  */
 public class FriendList extends FragmentActivity implements LocationListener{
     private static final String TAG = "FriendList";
     ArrayList<Friend> friendList;
     FriendListAdapter friendAdapter;
     ListView listView;
     private String userName;
     LocationManager locationManager;
     private Criteria criteria;
 
 
     private Location location;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.friends);
 
 
         //--- friendsList ---
         friendList = new ArrayList<Friend>();
 
         friendAdapter = new FriendListAdapter();
 
         listView = (ListView) findViewById(R.id.friend_list);
         listView.setAdapter(friendAdapter);
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
                 Friend friend = friendList.get(position);
                 
                 if(friend.getBizID().length() > 0){
                     Intent intent = new Intent(getApplicationContext(), NavigateActivity.class);
                     intent.putExtra("business", friend.getBizID());
                     intent.putExtra("friend", friend);
                     startActivity(intent);
                 }
                 else{
                     Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                     intent.putExtra("friend", friend);
                     startActivity(intent);
                 }
                 
             }
         });
 
         //getuser
         userName = getUsername();
 
         //-- location --
         locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
         List<String> providers = locationManager.getAllProviders();
 
         double accuracy = -1;
         if (providers.size() == 0) {
             Log.w(TAG, "No providers available");
         }
         for (String provider : providers) {
             Location tempLoc = locationManager.getLastKnownLocation(provider);
             if (tempLoc != null && (accuracy < 0 || tempLoc.getAccuracy() < accuracy)) {
                 location = tempLoc;
             }
         }
 
         criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
 
        new FriendQuery().execute("");
     }
 
     @Override
     public void onStart() {
         super.onStart();
         locationManager.requestLocationUpdates(0, 0, criteria, this, null);
 
     }
 
     @Override
     public void onStop() {
         super.onStop();
         locationManager.removeUpdates(this);
 
     }
 
     public String getUsername(){
         AccountManager manager = AccountManager.get(this);
         Account[] accounts = manager.getAccountsByType("com.google");
         List<String> possibleEmails = new LinkedList<String>();
 
         for (Account account : accounts) {
             // TODO: Check possibleEmail against an email regex or treat
             // account.name as an email address only for certain account.type values.
             possibleEmails.add(account.name);
         }
 
         if(!possibleEmails.isEmpty() && possibleEmails.get(0) != null){
             String email = possibleEmails.get(0);
             String[] parts = email.split("@");
             if(parts.length > 0 && parts[0] != null)
                 return parts[0];
             else
                 return null;
         }else
             return null;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         this.location = location;
         new FriendQuery().execute("");
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onProviderEnabled(String s) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onProviderDisabled(String s) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private class FriendListAdapter extends BaseAdapter{
         LayoutInflater inflateService;
 
         public FriendListAdapter() {
             inflateService = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         }
 
         @Override
         public int getCount () {
             return friendList.size();
         }
 
         @Override
         public Object getItem ( int i){
             return i;
         }
 
         @Override
         public long getItemId ( int i){
             return i;
         }
 
         @Override
         public View getView ( int position, View convertView, ViewGroup parent){
             if (convertView == null) {
                 convertView = inflateService.inflate(R.layout.friend_item, parent, false);
                 int resourceList[] = {R.id.fl_name,
                         R.id.fl_dist,
                         R.id.fl_business_set,
                         R.id.fl_time,
                         R.id.fl_userset
                         };
                 for (int res : resourceList) {
                     convertView.setTag(res, convertView.findViewById(res));
                 }
             }
 
             Friend friend = friendList.get(position);
             if (friend != null) {
                 ((TextView) convertView.getTag(R.id.fl_name)).setText(friend.getUserid());
                ((TextView) convertView.getTag(R.id.fl_dist)).setText(""+friend.getLocation().distanceTo(location));
                ((TextView) convertView.getTag(R.id.fl_business_set)).setText(""+friend.getBizID());
                ((TextView) convertView.getTag(R.id.fl_time)).setText(""+friend.getTime());
                ((TextView) convertView.getTag(R.id.fl_userset)).setText(""+friend.getFriend());
             }
             return convertView;
         }
     }
 
 
         //--- AsyncDoSearch ---
         private class FriendQuery extends AsyncTask<String, Void, String> {
             private String url;
             private final int BUFF_SIZE = 16384;
 
             @Override
             protected String doInBackground(String... searchTerms) {
                 for (String searchTerm : searchTerms) {
 
                         url = "http://friendcompass.porknbunny.com/?user="
                         + userName +"&lat="+location.getLatitude()+"&long="+location.getLongitude()+"&biz=&friend=";
 
 
                     BufferedInputStream inputStream = getUrl(url);
 
                     if (inputStream != null) {
 
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFF_SIZE);
                         try {
                             if (inputStream != null) {
                                 byte[] tempBuff = new byte[BUFF_SIZE];
                                 int readCount;
                                 while ((readCount = inputStream.read(tempBuff)) != -1) {
                                     byteArrayOutputStream.write(tempBuff, 0, readCount);
                                 }
                                 byte[] newBuff = byteArrayOutputStream.toByteArray();
                                 inputStream.close();
                                 byteArrayOutputStream.close();
                                 String temp = new String(newBuff, "US_ASCII");
                                 return temp;
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
 
                     }
 
 
                 }
                 return null;
             }
 
             @Override
             protected void onPostExecute(String result) {
                 if (result != null) {
                     friendList = new ArrayList<Friend>();
                     //time to parse some JSON!
                     try {
                         JSONArray list = new JSONArray(result);
                         for (int i = 0; i < list.length(); i++) {
                             JSONObject jsonFriend = list.getJSONObject(i);
                             Friend tempFriend = new Friend(jsonFriend.getString("userid"),
                                     jsonFriend.getString("friend"),
                                     jsonFriend.getString("businessid"),
                                     jsonFriend.getDouble("lat"),
                                     jsonFriend.getDouble("long"),
                                     jsonFriend.getInt("time"));
                             //if(tempFriend.getUserid().compareTo(userName) != 0){
                                 friendList.add(tempFriend);
                             //}
                         }
                         
                     }
                     catch (Exception e){
                         
                     }
                     friendAdapter.notifyDataSetChanged();
                 }
             }
         }
 
 
 
     private BufferedInputStream getUrl(String url) {
         int TEMP_BUFF_SIZE = 16384;
         try {
             HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
             connection.setReadTimeout(5000);
             InputStream inputStream = (InputStream) connection.getContent();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, TEMP_BUFF_SIZE);
             int responseCode = connection.getResponseCode();
             if (responseCode == HttpURLConnection.HTTP_OK) {
 
                 return bufferedInputStream;
             }
             Log.w(TAG, "Incorrect response for " + url + " : " + connection.getResponseCode());
         } catch (Exception e) {
             e.printStackTrace();
             //Log.w(TAG, e.getMessage());
             Log.w(TAG, "Could not get " + url);
         }
         return null;
     }
 
 
 }
