 package me.asofold.bpl.contextmanager.core;
 
import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import me.asofold.bpl.contextmanager.ContextManager;
 import me.asofold.bpl.contextmanager.config.ChannelSettings;
 import me.asofold.bpl.contextmanager.util.Utils;
 
 import org.bukkit.ChatColor;
 
 public class PlayerData {
 	/**
 	 * Lower case name.
 	 */
 	public final String lcName;
 	
 	private  String extraFormat = "";
 	
 	public boolean permAnnounce = false;
 	public boolean permMuted = false;
 	
 	public String normalFormat = null;
 	public String announceFormat = null;
 	public String partyFormat = null;
 	
 	public  PlayerData(String lcName){
 		this.lcName = lcName;
 		setExtraFormat();
 	}
 	
 	/**
 	 * simplistic by now.
 	 */
 	public String channel = null;
 	
	public Set<String> recipients = Collections.synchronizedSet(new HashSet<String>());
 	
 //	/**
 //	 * 
 //	 */
 //	public boolean muted = false;
 	
 	/**
 	 * Players to ignore.
 	 */
	public Set<String> ignored = Collections.synchronizedSet(new HashSet<String>());
 	
 
 	public void resetAll(){
 		resetContexts();
 		resetIgnored(); //
 	}
 
 	public void resetContexts() {
 		resetChannel();
 		resetRecipients();
 	}
 	
 	public void resetIgnored() {
 		ignored.clear();
 	}
 
 	public void resetRecipients(){
 		if (!recipients.isEmpty()) Utils.tryMessage(lcName, ChatColor.DARK_GRAY+ContextManager.plgLabel+" Resetting recipients.");
 		recipients.clear();
 		setExtraFormat();
 	}
 
 	public void resetChannel() {
 		if (channel != null) Utils.tryMessage(lcName, ChatColor.DARK_GRAY+ContextManager.plgLabel+" Leaving channel: "+channel);
 		channel = null;
 		setExtraFormat();
 	}
 	
 	public Set<ContextType> greedy = null;
 	
 	/**
 	 * Add the recipients, i.e. check if +- is used in front !
 	 * @param args
 	 * @param startIndex
 	 */
 	public void addRecipients(String[] args, int startIndex) {
 		// TODO Auto-generated method stub
 		// use -* to remove all
 		for (int i = startIndex; i<args.length; i++){
 			String n = args[i].trim().toLowerCase();
 			if (n.isEmpty()) continue;
 			if (n.equals("-*")){
 				recipients.clear();
 			}
 			else if (n.startsWith("-")) recipients.remove(n.substring(1));
 			else recipients.add(n);
 		}
 		setExtraFormat(); // also resets format
 	}
 	
 	
 	public void setChannel(String channel){
 		if (channel.equals(ChannelSettings.getDefaultChannelName())) channel = null;
 		this.channel = channel;
 		setExtraFormat(); // also resets format.
 	}
 	
 
 	
 	/**
 	 * Shortcut method to check if the other player is heard by this one.<br>
 	 * NOTE: this does not check broadcast and party chat.
 	 * @param other
 	 * @param isAnnounce 
 	 * @return
 	 */
 	public final boolean canHear(final PlayerData other, boolean isAnnounce){
 		// TODO: maybe make more efficient ways ...
 		
 		// greedy flag
 		final boolean isGreedy = greedy != null;
 		// ignore check
 		if (!isAnnounce && ignored.contains(other.lcName)) return false; // allow to ignore oneself.
 		
 		if (other == this) return true; // important check.
 	
 		if (!isAnnounce){
 			// recipients checks:
 			boolean orne = !other.recipients.isEmpty(); 
 			boolean rne = !recipients.isEmpty();
 			if (orne && !other.recipients.contains(lcName)){
 				if (!isGreedy) return false;
 				else if (!greedy.contains(ContextType.PRIVATE)) return false;
 			}
 			if (rne && !recipients.contains(other.lcName)){
 				if (!isGreedy) return false;
 			}
 			if (orne && rne) return true; // always allow to hear
 		}
 		
 		// channel:
 		if (channel == null){
 			if (isGreedy && (greedy.contains(ContextType.CHANNEL) || greedy.contains(ContextType.DEFAULT))) return true;
 			if (other.channel != null) return false;
 		} else{
 			if (isGreedy && greedy.contains(ContextType.CHANNEL)) return true;
 			if (other.channel == null) return false;
 			else if (!channel.equals(other.channel)) return false;
 		}
 		// TODO: more aspects.
 		return true;
 	}
 	
 	private String evaluateExtraFormat(){
 		if (!recipients.isEmpty()) return ChatColor.DARK_GRAY + "@"+ ChatColor.DARK_PURPLE + Utils.join(recipients, ",");
 		if (channel!=null) return ChatColor.DARK_GRAY + "@" + channel;
 		if (!ChannelSettings.getDefaultChannelName().isEmpty()) return ChatColor.DARK_GRAY + "@"+ChannelSettings.getDefaultChannelName();
 		else return "";
 	}
 
 	private void setExtraFormat() {
 		extraFormat = evaluateExtraFormat();
 	}
 	
 	public String getExtraFormat(){
 		return extraFormat;
 	}
 	
 }
