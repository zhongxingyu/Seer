 package com.turt2live.antishare.client;
 
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.AntiShare.LogType;
 import com.turt2live.antishare.ErrorLog;
 
 public class SimpleNotice {
 
 	public void onEnable(){
 		AntiShare.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(AntiShare.getInstance(), "SimpleNotice");
 	}
 
 	public boolean sendPluginMessage(Player player, String message){
 		if(player == null || message == null){
 			return false;
 		}
 		if(!player.getListeningPluginChannels().contains("SimpleNotice")){
 			return false;
 		}
 		try{
			player.sendPluginMessage(AntiShare.getInstance(), "SimpleNotice", message.getBytes());
 			return true;
 		}catch(Exception e){
 			AntiShare.getInstance().getMessenger().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE, LogType.ERROR);
 			AntiShare.getInstance().getMessenger().log("Please see " + ErrorLog.print(e) + " for the full error.", Level.SEVERE, LogType.ERROR);
 			return false;
 		}
 	}
 
 }
