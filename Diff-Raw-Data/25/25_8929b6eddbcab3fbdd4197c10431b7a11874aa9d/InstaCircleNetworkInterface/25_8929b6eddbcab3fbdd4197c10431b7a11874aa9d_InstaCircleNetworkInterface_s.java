 package ch.bfh.evoting.votinglib.network;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.CursorIndexOutOfBoundsException;
 import android.net.DhcpInfo;
 import android.net.wifi.WifiManager;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import ch.bfh.evoting.instacirclelib.Message;
 import ch.bfh.evoting.instacirclelib.db.NetworkDbHelper;
 import ch.bfh.evoting.instacirclelib.service.NetworkService;
 import ch.bfh.evoting.votinglib.entities.Participant;
 import ch.bfh.evoting.votinglib.entities.VoteMessage;
 import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
 
 public class InstaCircleNetworkInterface extends AbstractNetworkInterface {
 
 
 	private static final String PREFS_NAME = "network_preferences";
 
 	private final NetworkDbHelper dbHelper;
 
 	public InstaCircleNetworkInterface (Context context) {
 		super(context);
 
 		dbHelper = NetworkDbHelper.getInstance(context);
 
 		// Listening for arriving messages
 		LocalBroadcastManager.getInstance(context).registerReceiver(
 				mMessageReceiver, new IntentFilter("messageArrived"));
 
 		// Subscribing to the participantJoined and participantChangedState events
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction("participantJoined");
 		intentFilter.addAction("participantChangedState");
 		LocalBroadcastManager.getInstance(context).registerReceiver(participantsDiscoverer, intentFilter);
 	}
 
 	@Override
 	public String getNetworkName() {
 		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
 		preferences = context.getSharedPreferences(PREFS_NAME, 0);
 		return preferences.getString("SSID", "");
 	}
 
 	@Override
 	public String getConversationPassword() {
 		try{
 			return dbHelper.getCipherKey();
 		} catch (CursorIndexOutOfBoundsException e){
 			return null;
 		}
 	}
 
 	@Override
 	public Map<String,Participant> getConversationParticipants(){
 		ArrayList<Participant> participants = new ArrayList<Participant>(); 
 
 		Cursor c = dbHelper.queryParticipants();
 
 		if (c != null){
 			while(c.moveToNext()){
 				Participant p = new Participant(c.getString(c.getColumnIndex("identification")), c.getString(c.getColumnIndex("ip_address")), false, false);
 				participants.add(p);
 			}
 		}
 		c.close();
 
 		Map<String,Participant> map = new HashMap<String,Participant>();
 		for(Participant p:participants){
 			map.put(p.getIpAddress(), p);
 		}
 
 		return map;
 	}
 
 	/**
 	 * This method can be used to send a broadcast message
 	 * 
 	 * @param votemessage The votemessage which should be sent
 	 * @param sender The origin of the message
 	 */
 	@Override
 	public void sendMessage(VoteMessage votemessage){
 		votemessage.setSenderIPAdress(this.getMyIpAddress());
 		votemessage.setTimestamp(System.currentTimeMillis());
 		Message message = new Message(su.serialize(votemessage), Message.MSG_CONTENT, this.getMyIpAddress());
 		Intent intent = new Intent("messageSend");
 		intent.putExtra("message", message);
 		intent.putExtra("broadcast", true);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 	}
 
 	/**
 	 * This method signature can be used to send unicast message to a specific ip address
 	 * 
 	 * 
 	 * @param votemessage The votemessage which should be sent
 	 * @param sender The origin of the message
 	 * @param destinationIPAddress The destination of the message
 	 */
 	@Override
 	public void sendMessage(VoteMessage votemessage, String destinationIPAddress){
 		votemessage.setSenderIPAdress(this.getMyIpAddress());
 		votemessage.setTimestamp(System.currentTimeMillis());
 		Message message = new Message(su.serialize(votemessage), Message.MSG_CONTENT, this.getMyIpAddress());
 		Intent intent = new Intent("messageSend");
 		intent.putExtra("message", message);
 		intent.putExtra("ipAddress", destinationIPAddress);
 		intent.putExtra("broadcast", false);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 	}
 
 	/**
 	 * this broadcast receiver listens for incoming instacircle messages
 	 */
 	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			handleReceivedMessage((Message) intent.getSerializableExtra("message"));
 		}
 	};
 	
 	/**
 	 * This method acts as a wrapper between instacircle messages and vote
 	 * messages by extracting the vote message and rebroadcast the notification.
 	 * 
 	 * @param message
 	 */
 	private void handleReceivedMessage(Message message) {
 		if(message.getMessageType()==Message.MSG_CONTENT){
 			// Extract the votemessage out of the message
 			VoteMessage voteMessage = (VoteMessage) su.deserialize(message.getMessage());
 			if(voteMessage==null) return;
 			this.transmitReceivedMessage(voteMessage);
 		}
 	}
 
 	/**
 	 * this broadcast receiver listens for incoming instacircle broadcast notifying set of participants has changed
 	 */
 	private BroadcastReceiver participantsDiscoverer = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.e("InstaCircleNetworkInterface", "Participant State Update received from IC");
 			Intent participantsUpdate = new Intent(BroadcastIntentTypes.participantStateUpdate);
 			LocalBroadcastManager.getInstance(context).sendBroadcast(participantsUpdate);
 		}
 	};
 
 	@Override
 	public String getMyIpAddress(){
 		return getIPAddress(true);
 	}
 	
 	@Override
 	public void disconnect(){
 		this.dbHelper.closeConversation();
 	}
 
 	/**
 	 * Get IP address from first non-localhost interface
 	 * @param ipv4  true=return ipv4, false=return ipv6
 	 * @return  address or empty string
 	 * @author http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
 	 */
 	private String getIPAddress(boolean useIPv4) {
 
 		WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
 		String ipString = null;
 
 		if(new ch.bfh.evoting.votinglib.network.wifi.WifiAPManager().isWifiAPEnabled(wifiManager)){
 
 			try{
 				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
 						.hasMoreElements();) {
 					NetworkInterface intf = en.nextElement();
 					if (intf.getName().contains("wlan")) {
 						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
 								.hasMoreElements();) {
 							InetAddress inetAddress = enumIpAddr.nextElement();
 							if (!inetAddress.isLoopbackAddress()
 									&& (inetAddress.getAddress().length == 4)) {
 								ipString = inetAddress.getHostAddress();
 								break;
 							}
 						}
 					}
 				}
 			} catch (SocketException e){
 				e.printStackTrace();
 			}
 		} else {
 
 			WifiManager wifi = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
 			DhcpInfo dhcp = wifi.getDhcpInfo();
 
 			InetAddress found_ip_address = null;
 			int ip = dhcp.ipAddress;
 			byte[] quads = new byte[4];
 			for (int k = 0; k < 4; k++)
 				quads[k] = (byte) (ip >> (k * 8));
 			try {
 				found_ip_address =  InetAddress.getByAddress(quads);
 				ipString = found_ip_address.getHostAddress();
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return ipString;
 		
 	}
 	
 	@Override
 	public void joinNetwork(String networkName) {
 		Intent intent = new Intent(context, NetworkService.class);
 
 		context.stopService(intent);
 		context.startService(intent);
 		
 	}
 	
 }
