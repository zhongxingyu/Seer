 package ch.cern.atlas.apvs.client.settings;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
import java.util.Map.Entry;
 
 public class AudioSettings implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private Map<String, VoipAccount> entries = new HashMap<String, VoipAccount>();
 
 	public AudioSettings() {
 	}
 
 	/*********************************************
 	 * Methods
 	 *********************************************/
 	
 	// Username Methods
 	public String getUsername(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getUsername() : "");
 	}
 
 	public boolean setUsername(String ptuId, String username) {
 		if (entries.get(ptuId).getUsername().equals(username)) {
 			return false;
 		}
 		entries.get(ptuId).setUsername(username);
 		return true;
 	}
 
 	
 	// Number Methods
 	public String getNumber(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getAccount() : "");
 	}
 
 	public void setNumber(String ptuId, String number) {
 		entries.get(ptuId).setAccount(number);
 	}
 
 	
 	// Channel Methods
 	public String getChannel(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getChannel() : "");
 	}
 
 	public void setChannel(String ptuId, String channel) {
 		entries.get(ptuId).setChannel(channel);
 	}
 
 	
 	// Destination User Methods
 	public String getDestUser(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getDestUser() : "");
 	}
 
 	public String getDestPtu(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getDestPTU() : "");
 	}
 	
 	public void setDestUser(String ptuId, String userDest) {
 		entries.get(ptuId).setDestUser(userDest);
 	}
 	
 	public void setDestPTUser(String ptuId, String userDest, String PTUDest) {
 		entries.get(ptuId).setDestUser(userDest);
 		entries.get(ptuId).setDestPTU(PTUDest);
 	}
 
 	
 	// Status Methods
 	public Boolean getStatus(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getStatus() : false);
 	}
 
 	public void setStatus(String ptuId, Boolean status) {
 		entries.get(ptuId).setStatus(status);
 	}
	
	// Define all PTU as status unknown
	public void setUnkwonStatus(){
		for(String ptuId : entries.keySet())
			setStatus(ptuId, false);
	}
 
 	
 	// On Call Status Methods
 	public Boolean getOnCall(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getOnCall() : false);
 	}
 
 	public void setOnCall(String ptuId, Boolean onCall) {
 		entries.get(ptuId).setOnCall(onCall);
 	}
 
 	
 	// On Conference Status Methods
 	public Boolean getOnConference(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getOnConference() : false);
 	}
 
 	public void setOnConference(String ptuId, Boolean onConference) {
 		entries.get(ptuId).setOnConference(onConference);
 	}
 
 	
 	// Room Methods
 	public String getRoom(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getRoom() : "");
 	}
 
 	public void setRoom(String ptuId, String conferenceRoom) {
 		entries.get(ptuId).setRoom(conferenceRoom);
 	}
 	
 	
 	// Mute Methods
 	public boolean getMute(String ptuId){
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null? entry.getMute() : true);
 	}
 	
 	public void setMute(String ptuId, boolean mute){
 		entries.get(ptuId).setMute(mute);
 	}
 	
 	
 	// Activity Methods
 	public String getActivity(String ptuId) {
 		VoipAccount entry = entries.get(ptuId);
 		return (entry != null ? entry.getActivity() : "");
 	}
 
 	public void setActivity(String ptuId, String activity) {
 		entries.get(ptuId).setActivity(activity);
 	}
 
 	
 	// PTU Ids
 	public List<String> getPtuIds() {
 		List<String> list = new ArrayList<String>();
 		list.addAll(entries.keySet());
 		return list;
 	}
 
 	
 	// Add PTU
 	public boolean add(String ptuId) {
 		System.err.println("Adding " + ptuId);
 		if (!entries.containsKey(ptuId)) {
 			entries.put(ptuId, new VoipAccount("SIP/1000", false));
 			return true;
 		}
 		return false;
 	}
 
 	
 	// Check PTU is in the list
 	public boolean contains(String ptuId) {
 		if (entries.containsKey(ptuId)) {
 			return true;
 		}
 		return false;
 	}
 
 	
 	// Return PTU ID of given number
 	public String getPtuId(String number) {
 		List<String> ptuList = new ArrayList<String>(this.getPtuIds());
 		for (int i = 0; i < ptuList.size(); i++) {
 			if (this.getNumber(ptuList.get(i)).equals(number)) {
 				return ptuList.get(i);
 			}
 		}
 		return null;
 	}
 
 	// Return users phone numbers of given activity
 	public List<String> getNumbersActivity(String activity) {
 		List<String> ptuList = new ArrayList<String>(this.getPtuIds());
 		List<String> numbers = new ArrayList<String>();
 		for (int i = 0; i < ptuList.size(); i++) {
 			if (this.getActivity(ptuList.get(i)).equals(activity)) {
 				numbers.add(this.getNumber(ptuList.get(i)));
 			}
 		}
 		return numbers;
 	}
 	
 	// Return active channels of given activity
 	public List<String> getActiveChannelsActivity(String activity) {
 		List<String> ptuList = new ArrayList<String>(this.getPtuIds());
 		List<String> channels = new ArrayList<String>();
 		for (int i = 0; i < ptuList.size(); i++) {
 			if (this.getActivity(ptuList.get(i)).equals(activity) && !this.getChannel(ptuList.get(i)).isEmpty()) {
 				channels.add(this.getChannel(ptuList.get(i)));
 			}
 		}
 		return channels;
 	}
 }
