 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.notification;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.manager.MoneyManager;
 import com.turt2live.antishare.metrics.ActionsTracker;
 import com.turt2live.antishare.metrics.Tracker;
 import com.turt2live.antishare.metrics.TrackerList.TrackerType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.util.ASUtils;
 
 /**
  * Alerts people
  * 
  * @author turt2live
  */
 public class Alert {
 
 	/**
 	 * An enum to represent what type of alert is being sent
 	 * 
 	 * @author turt2live
 	 */
 	public static enum AlertType{
 		ILLEGAL,
 		LEGAL,
 		GENERAL,
 		REGION;
 	}
 
 	/**
 	 * An enum to better determine what alerts to send based on configuration
 	 * 
 	 * @author turt2live
 	 */
 	public static enum AlertTrigger{
 		BLOCK_BREAK("types.block-break"),
 		BLOCK_PLACE("types.block-place"),
 		PLAYER_DEATH("types.player-death"),
 		ITEM_DROP("types.item-drop"),
 		ITEM_PICKUP("types.item-pickup"),
 		RIGHT_CLICK("types.right-click"),
 		USE_ITEM("types.use-item"),
 		CREATIVE_BLOCK("types.creative-block-break"),
 		SURVIVAL_BLOCK("types.survival-block-break"),
 		ADVENTURE_BLOCK("type.adventure-block-break"),
 		HIT_PLAYER("types.hit-player"),
 		HIT_MOB("types.hit-mob"),
 		COMMAND("types.command"),
 		GENERAL("send-general-notifications"),
 		CRAFTING("types.crafting"),
 		CREATE_MOB("types.create-mob"),
 		CLOSE_TO_WORLD_SPLIT(null);
 
 		private String node;
 
 		private AlertTrigger(String node){
 			this.node = node;
 		}
 
 		/**
 		 * Determines whether or not to show this alert
 		 * 
 		 * @return true if it should be shown
 		 */
 		public boolean show(){
 			if(node == null){
 				return false;
 			}
 			EnhancedConfiguration notifications = new EnhancedConfiguration(new File(AntiShare.getInstance().getDataFolder(), "notifications.yml"), AntiShare.getInstance());
 			notifications.loadDefaults(AntiShare.getInstance().getResource("resources/notifications.yml"));
 			if(!notifications.fileExists() || !notifications.checkDefaults()){
 				notifications.saveDefaults();
 			}
 			notifications.load();
 			return notifications.getBoolean(node);
 		}
 	}
 
 	/**
 	 * A class to represent alert details
 	 * 
 	 * @author turt2live
 	 */
 	private class AlertDetails {
 		public long admin_last_sent = 0L;
 		public long player_last_sent = 0L;
 	}
 
 	private HashMap<String, AlertDetails> alerts = new HashMap<String, AlertDetails>();
 	private boolean send = true;
 	private boolean toConsole = true;
 	private boolean toPlayers = true;
 	private boolean sendIllegal = true;
 	private boolean sendLegal = false;
 	private boolean sendGeneral = false;
 	private Map<AlertTrigger, Tracker> legal = new HashMap<AlertTrigger, Tracker>();
 	private Map<AlertTrigger, Tracker> illegal = new HashMap<AlertTrigger, Tracker>();
 
 	/**
 	 * Creates a new Alerter
 	 */
 	public Alert(){
 		reload();
 		for(AlertTrigger trigger : AlertTrigger.values()){
 			switch (trigger){
 			case GENERAL:
 			case CLOSE_TO_WORLD_SPLIT:
 				break;
 			default:
 				ActionsTracker legal = new ActionsTracker(ASUtils.capitalize(trigger.name()), TrackerType.SPECIAL, "Legal Actions");
 				ActionsTracker illegal = new ActionsTracker(ASUtils.capitalize(trigger.name()), TrackerType.SPECIAL, "Illegal Actions");
 				this.legal.put(trigger, legal);
 				this.illegal.put(trigger, illegal);
 				AntiShare.getInstance().getTrackers().add(illegal);
 				AntiShare.getInstance().getTrackers().add(legal);
 			}
 		}
 	}
 
 	/**
 	 * Sends an alert
 	 * 
 	 * @param message the message
 	 * @param sender the sender
 	 * @param playerMessage the player message
 	 * @param type the Alert Type
 	 * @param trigger the Alert Trigger
 	 */
 	public void alert(String message, CommandSender sender, String playerMessage, AlertType type, AlertTrigger trigger){
 		alert(message, sender, playerMessage, type, trigger, 1000, true);
 	}
 
 	/**
 	 * Sends an alert
 	 * 
 	 * @param message the message
 	 * @param sender the sender
 	 * @param playerMessage the player message
 	 * @param type the Alert Type
 	 * @param trigger the Alert Trigger
 	 * @param reward true to send rewards/fines, false to disable them
 	 */
 	public void alert(String message, CommandSender sender, String playerMessage, AlertType type, AlertTrigger trigger, boolean reward){
 		alert(message, sender, playerMessage, type, trigger, 1000, reward);
 	}
 
 	/**
 	 * Sends an alert
 	 * 
 	 * @param message the message
 	 * @param sender the sender
 	 * @param playerMessage the player message
 	 * @param type the Alert Type
 	 * @param trigger the Alert Trigger
 	 * @param time the time between alerts
 	 */
 	public void alert(String message, CommandSender sender, String playerMessage, AlertType type, AlertTrigger trigger, long time){
 		alert(message, sender, playerMessage, type, trigger, time, true);
 	}
 
 	/**
 	 * Sends an alert
 	 * 
 	 * @param message the message
 	 * @param sender the sender
 	 * @param playerMessage the player message
 	 * @param type the Alert Type
 	 * @param trigger the Alert Trigger
 	 * @param time the time between alerts
 	 * @param reward true to send rewards/fines, false to disable them
 	 */
 	public void alert(String message, CommandSender sender, String playerMessage, AlertType type, AlertTrigger trigger, long time, boolean reward){
 		String hashmapKey = type.name() + message + playerMessage + sender.hashCode();
 		boolean sendToPlayer = true;
 		boolean sendToAdmins = true;
 
 		// Add to Metrics
 		if(type == AlertType.ILLEGAL){
 			if(illegal.containsKey(trigger)){
 				illegal.get(trigger).increment(1);
 			}
 		}else if(type == AlertType.LEGAL){
 			if(legal.containsKey(trigger)){
 				legal.get(trigger).increment(1);
 			}
 		}//else ignore
 
 		// Determine if we should even send the message
 		if(!send || AntiShare.getInstance().getPermissions().has(sender, PermissionNodes.SILENT_NOTIFICATIONS)){
 			if(type != AlertType.REGION){
 				return;
 			}else{
 				sendToAdmins = false;
 			}
 		}
 
 		// Alert Switch
 		switch (type){
 		case ILLEGAL:
 			if(!sendIllegal){
 				sendToAdmins = false;
 			}
 			break;
 		case LEGAL:
 			if(!sendLegal){
 				sendToAdmins = false;
 			}
 			break;
 		case GENERAL:
 		case REGION:
 			if(!sendGeneral){
 				sendToAdmins = false;
 			}
 			break;
 		}
 
 		// Check who we need to send to
 		AlertDetails details = alerts.get(hashmapKey);
 		if(details == null){
 			details = new AlertDetails();
 			details.admin_last_sent = System.currentTimeMillis();
 			details.player_last_sent = System.currentTimeMillis();
 		}else{
 			long now = System.currentTimeMillis();
 			if((now - details.admin_last_sent) < time){
 				sendToAdmins = false;
 			}
 			if((now - details.player_last_sent) < time){
 				sendToPlayer = false;
 			}
 		}
 
 		// Check trigger
 		if(sendToAdmins){
 			sendToAdmins = trigger.show();
 		}
 
 		// Send if needed
 		if(sendToPlayer && (type == AlertType.ILLEGAL || type == AlertType.GENERAL || type == AlertType.REGION)){
 			details.player_last_sent = System.currentTimeMillis();
 			ASUtils.sendToPlayer(sender, playerMessage, true);
 		}
		if(sendToAdmins && toPlayers){
 			details.admin_last_sent = System.currentTimeMillis();
 			for(Player player : Bukkit.getOnlinePlayers()){
 				if(AntiShare.getInstance().getPermissions().has(player, PermissionNodes.GET_NOTIFICATIONS)){
 					if(!player.getName().equalsIgnoreCase(sender.getName())){
 						ASUtils.sendToPlayer(player, message, true);
 					}
 				}
 			}
 		}
		if(sendToAdmins && toConsole){
 			ASUtils.sendToPlayer(Bukkit.getConsoleSender(), "[" + type.name() + "] " + message, true);
 		}
 
 		// Send fine/reward
 		if(sender instanceof Player && reward){
 			Player player = (Player) sender;
 			((MoneyManager) AntiShare.getInstance().getSystemsManager().getManager(Manager.MONEY)).fire(trigger, type, player);
 		}
 
 		// Reinsert (or insert if not found before) into the hashmap
 		alerts.put(hashmapKey, details);
 	}
 
 	/**
 	 * Reloads the alerter
 	 */
 	public void reload(){
 		// Setup configurations
 		EnhancedConfiguration notifications = new EnhancedConfiguration(new File(AntiShare.getInstance().getDataFolder(), "notifications.yml"), AntiShare.getInstance());
 		notifications.loadDefaults(AntiShare.getInstance().getResource("resources/notifications.yml"));
 		if(!notifications.fileExists() || !notifications.checkDefaults()){
 			notifications.saveDefaults();
 		}
 		notifications.load();
 
 		// Setup settings
 		send = notifications.getBoolean("send-notifications");
 		toConsole = notifications.getBoolean("send-to-console");
 		toPlayers = notifications.getBoolean("send-to-players");
 		sendIllegal = notifications.getBoolean("send-illegal-notifications");
 		sendLegal = notifications.getBoolean("send-legal-notifications");
 		sendGeneral = notifications.getBoolean("send-general-notifications");
 	}
 
 }
