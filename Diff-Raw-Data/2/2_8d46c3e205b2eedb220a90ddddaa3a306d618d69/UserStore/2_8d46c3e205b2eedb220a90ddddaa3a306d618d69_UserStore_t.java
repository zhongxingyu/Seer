 package com.eit.minimap.datastructures;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.location.Location;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 import android.widget.Toast;
 import com.eit.minimap.R;
 import com.eit.minimap.gps.LocationProcessor;
 import com.eit.minimap.network.ClientConnectThread;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.eit.minimap.network.JsonTcpClient;
 import com.eit.minimap.network.NetworkListener;
 import com.google.android.gms.maps.model.LatLng;
 
 public class UserStore implements NetworkListener {
     /** Map containing all the users of this application. The key is the mac adress of the phone. */
     private Map<String, User> users = new HashMap<String, User>();;
     /** Network communicator. Not always set, so check if it's null when using it. */
     private JsonTcpClient network;
     /** Location resolver. Calls this store periodically to update current users' position */
     private LocationProcessor processor;
     private UserStoreListener listener;
     private final static int MIN_POS_SEND_INTERVAL = 1000;
     private final static String TAG = "com.eit.minimap.datastructures.UserStore";
     /** Mac adress of the phone running this application. */
     private String myMac;
     private Context context;
 
     private long timeSinceLastSentPacket;
 
     public UserStore(Context c){
         this.context = c;
         // Get MAC address:
         WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
         String mac = wifiManager.getConnectionInfo().getMacAddress();
         myMac = mac;
         //Add our user!
         users.put(myMac,new User(myMac,"TODO: Screenname here"));
 
         timeSinceLastSentPacket = 0;
 
     }
 
     public void init() {
         //Starts the connection process to host. Tcp client is received on completion.
         new ClientConnectThread(context,this).execute();
         processor = new LocationProcessor(context,this);
         processor.initializeProvider();
         processor.startProvider();
     }
 
     public void addUser(User usr){
         users.put(usr.getMacAddr(),usr);
     }
     public void delUser(User usr){
         users.remove(usr.getMacAddr());
         
     }
     @Override
     public void packageReceived(JSONObject pack) {
         try{
             String mcAdr = pack.getString("macAddr");
             String type = pack.getString("type");
             if(users.containsKey(mcAdr) && type == "pos"){
                 User usr = users.get(mcAdr);
                 //update Coordinate
                 Coordinate newCord = new Coordinate(pack);
                 usr.addPosition(newCord);
                 if(listener!=null) {
                     listener.userPositionsChanged(this);
                 }
             }
             else if(type == "pInfo"){
                 User newUser = new User(mcAdr,pack.getString("screenName"));
                 addUser(newUser);
                 if(listener!=null) {
                     listener.usersChanged(this);
                 }
            }else if(users.containsKey(mcAdr) && type == "disc"){
                 User discUser = users.get(mcAdr);
                 delUser(discUser);
             }else{
                 Log.e(TAG,"Received unknown packet or failed to receive packet");
             }
         }catch(JSONException error){
             Log.e(TAG,"Error! Certain fields missing in received pack (missing MacAddr or type?)\n"+pack.toString());
         }
     }
 
     public void locationChanged(Location location){
         // Re-wrap location
         Coordinate coord = new Coordinate(
                 new LatLng(location.getLatitude(),location.getLongitude()),
                 System.currentTimeMillis());
         users.get(myMac).addPosition(coord);
         try{
             if(System.currentTimeMillis()- timeSinceLastSentPacket > MIN_POS_SEND_INTERVAL ){
                 sendPosPacket(coord);
                 timeSinceLastSentPacket = System.currentTimeMillis();
             }
         }catch(JSONException error){
             Log.e(TAG,"Error constructing JSON packet");
         }
         if(listener!=null) {
             listener.userPositionsChanged(this);
         }
     }
 
     public void sendPosPacket(Coordinate coord) throws JSONException{
         JSONObject posPacket = coord.convertToJSON();
         // call network.sendData(JSON json)
         network.sendData(posPacket);
     }
 
     @Override
     public void receiveTcpClient(JsonTcpClient client) {
         this.network = client;
         network.addListener(this);
     }
 
     @Override
     public void connectionChanged(Change c) {
         listener.connectionChanged(c);
         if(c == Change.DISCONNECTED) { //If disconnected, reconnect.
             new ClientConnectThread(context,this).execute();
         }
     }
 
     public Collection<User> getUsers(){
         return Collections.unmodifiableCollection(users.values());
     }
 
     public void registerListener(UserStoreListener listener) {
         this.listener=listener;
     }
 
     public interface UserStoreListener {
         void userPositionsChanged(UserStore store);
         void usersChanged(UserStore store);
         void connectionChanged(Change c);
     }
 }
 
 
 
