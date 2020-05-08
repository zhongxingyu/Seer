 package com.minecraftdimensions.bungeesuite.managers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.concurrent.TimeUnit;
 
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.configs.TeleportConfig;
 import com.minecraftdimensions.bungeesuite.objects.Location;
 import com.minecraftdimensions.bungeesuite.objects.Messages;
 import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;
 
 public class TeleportManager {
 	public static HashMap<BSPlayer, BSPlayer> pendingTeleportsTPA; // Player ----teleported---> player
 	public static HashMap<BSPlayer, BSPlayer> pendingTeleportsTPAHere; // Player ----teleported---> player
 	public static HashMap<BSPlayer, Location> backLocations;
 	public static String OUTGOING_CHANNEL = "BungeeSuiteTP";
 	static int expireTime;
 
 	public static void initialise(){
 		pendingTeleportsTPA = new HashMap<BSPlayer,BSPlayer>();
 		pendingTeleportsTPAHere = new HashMap<BSPlayer,BSPlayer>();
 		expireTime =TeleportConfig.expireTime;
 	}
 	
 	public static void requestToTeleportToPlayer(String player, String target){
 		final BSPlayer bp = PlayerManager.getPlayer(player);
 		final BSPlayer bt = PlayerManager.getSimilarPlayer(target);
 		if(playerHasPendingTeleport(bp)){
 			bp.sendMessage(Messages.PLAYER_TELEPORT_PENDING);
 			return;
 		}
 		if(bt==null){
 			bp.sendMessage(Messages.PLAYER_NOT_ONLINE);
 			return;
 		}
 		if(!playerIsAcceptingTeleports(bt)){
 			bp.sendMessage(Messages.TELEPORT_UNABLE);
 			return;
 		}
 		if(playerHasPendingTeleport(bt)){
 			bp.sendMessage(Messages.PLAYER_TELEPORT_PENDING_OTHER);
 			return;
 		}
 		pendingTeleportsTPA.put(bt, bp);
 		bp.sendMessage(Messages.TELEPORT_REQUEST_SENT);
 		bt.sendMessage(Messages.PLAYER_REQUESTS_TO_TELEPORT_TO_YOU.replace("{player}", bp.getDisplayingName()));
 		ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable() {
 					@Override
 					public void run() {
 						if (pendingTeleportsTPA.containsKey(bt)) {
 							if(!pendingTeleportsTPA.get(bt).equals(bp)){
 								return;
 							}
 							if(bp!=null){
 							bp.sendMessage(Messages.TPA_REQUEST_TIMED_OUT.replace("{player}", bt.getDisplayingName()));
 							}
 							pendingTeleportsTPA.remove(bt);
 							if(bt!=null){
 								bt.sendMessage(Messages.TP_REQUEST_OTHER_TIMED_OUT.replace("{player}", bp.getDisplayingName()));
 							}
 						}
 					}
 				}, expireTime, TimeUnit.SECONDS);
 	}
 	
 	public static void requestPlayerTeleportToYou(String player, String target){
 		final BSPlayer bp = PlayerManager.getPlayer(player);
 		final BSPlayer bt = PlayerManager.getSimilarPlayer(target);
 		if(playerHasPendingTeleport(bp)){
 			bp.sendMessage(Messages.PLAYER_TELEPORT_PENDING);
 			return;
 		}
 		if(bt==null){
 			bp.sendMessage(Messages.PLAYER_NOT_ONLINE);
 			return;
 		}
 		if(!playerIsAcceptingTeleports(bt)){
 			bp.sendMessage(Messages.TELEPORT_UNABLE);
 			return;
 		}
 		if(playerHasPendingTeleport(bt)){
 			bp.sendMessage(Messages.PLAYER_TELEPORT_PENDING_OTHER);
 			return;
 		}
 		pendingTeleportsTPAHere.put(bt,bp);
 		bp.sendMessage(Messages.TELEPORT_REQUEST_SENT);
 		bt.sendMessage(Messages.PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM.replace("{player}",bp.getDisplayingName()));
 		ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable() {
 					@Override
 					public void run() {
 						if (pendingTeleportsTPAHere.containsKey(bt)) {
							if(!pendingTeleportsTPAHere.get(bp).equals(bt)){
 								return;
 							}
 							if(bp!=null){
 							bp.sendMessage(Messages.TPAHERE_REQUEST_TIMED_OUT.replace("{player}", bt.getDisplayingName()));
 							}
 							pendingTeleportsTPAHere.remove(bt);
 							if(bt!=null){
 								bt.sendMessage(Messages.TP_REQUEST_OTHER_TIMED_OUT.replace("{player}", bp.getDisplayingName()));
 							}
 						}
 					}
 				}, expireTime, TimeUnit.SECONDS);
 	}
 	
 	public static void acceptTeleportRequest(BSPlayer player){
 		if(pendingTeleportsTPA.containsKey(player)){
 			BSPlayer target = pendingTeleportsTPA.get(player);
 			target.sendMessage(Messages.TELEPORTED_TO_PLAYER.replace("{player}", player.getDisplayingName()));
 			player.sendMessage(Messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}", target.getDisplayingName()));
 			teleportPlayerToPlayer(target,player);
 			pendingTeleportsTPA.remove(player);
 		}else if(pendingTeleportsTPAHere.containsKey(player)){
 			BSPlayer target = pendingTeleportsTPAHere.get(player);
 			player.sendMessage(Messages.TELEPORTED_TO_PLAYER.replace("{player}", target.getDisplayingName()));
 			target.sendMessage(Messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}", player.getDisplayingName()));
 			teleportPlayerToPlayer(player,target);
 			pendingTeleportsTPAHere.remove(player);
 		}else{
 			player.sendMessage(Messages.NO_TELEPORTS);
 		}
 	}
 	
 	public static void denyTeleportRequest(BSPlayer player){
 		if(pendingTeleportsTPA.containsKey(player)){
 			BSPlayer target = pendingTeleportsTPA.get(player);
 			player.sendMessage(Messages.TELEPORT_REQUEST_DENIED.replace("{player}", target.getDisplayingName()));
 			target.sendMessage(Messages.TELEPORT_DENIED.replace("{player}", player.getDisplayingName()));
 			pendingTeleportsTPA.remove(player);
 		}else if(pendingTeleportsTPAHere.containsKey(player)){
 			BSPlayer target = pendingTeleportsTPA.get(player);
 			player.sendMessage(Messages.TELEPORT_REQUEST_DENIED.replace("{player}", target.getDisplayingName()));
 			target.sendMessage(Messages.TELEPORT_DENIED.replace("{player}", player.getDisplayingName()));
 			pendingTeleportsTPAHere.remove(player);
 		}else{
 			player.sendMessage(Messages.NO_TELEPORTS);
 		}
 	}
 
 	public static boolean playerHasPendingTeleport(BSPlayer player){
 		return pendingTeleportsTPA.containsKey(player) || pendingTeleportsTPAHere.containsKey(player);
 	}
 	
 	public static boolean playerIsAcceptingTeleports(BSPlayer player){
 		return player.acceptingTeleports();
 	}
 	
 	public static boolean playerHasDeathBackLocation(BSPlayer player){
 		return player.hasDeathBackLocation();
 	}
 	public static boolean playerHasTeleportBackLocation(BSPlayer player){
 		return player.hasTeleportBackLocation();
 	}
 	
 	public static void setPlayersDeathBackLocation(BSPlayer player, Location loc){
 		player.setDeathBackLocation(loc);
 	}
 	public static void setPlayersTeleportBackLocation(BSPlayer player, Location loc){
 		player.setTeleportBackLocation(loc);
 	}	
 	
 	public static void sendPlayerToLastBack(BSPlayer player, boolean death, boolean teleport){
 		if(player.hasDeathBackLocation() || player.hasTeleportBackLocation()){
 			player.sendMessage(Messages.SENT_BACK);
 		}else{
 			player.sendMessage(Messages.NO_BACK_TP);
 		}
 		if(death && teleport){
 			if(player.hasDeathBackLocation() || player.hasTeleportBackLocation()){
 		teleportPlayerToLocation(player,player.getLastBackLocation());
 			}
 		}else if(death){
 			teleportPlayerToLocation(player,player.getDeathBackLocation());
 		}else if(teleport){
 			teleportPlayerToLocation(player,player.getDeathBackLocation());
 		}
 	}
 	
 	public static void togglePlayersTeleports(BSPlayer player){
 		if(player.acceptingTeleports()){
 			player.setAcceptingTeleports(false);
 			player.sendMessage(Messages.TELEPORT_TOGGLE_OFF);
 		}else{
 			player.setAcceptingTeleports(true);
 			player.sendMessage(Messages.TELEPORT_TOGGLE_ON);
 		}
 	}
 		
 	public static void teleportPlayerToPlayer(BSPlayer p, BSPlayer t){
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportToPlayer");
 			out.writeUTF(p.getName());
 			out.writeUTF(t.getName());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		sendPluginMessageTaskTP(t.getServer().getInfo(),b);
 		if(!p.getServer().getInfo().equals(t.getServer().getInfo())){
 			p.getProxiedPlayer().connect(t.getServer().getInfo());
 		}
 	}
 
 	public static void tpAll(String sender, String target) {
 		BSPlayer p = PlayerManager.getPlayer(sender);
 		BSPlayer t = PlayerManager.getPlayer(target);
 		if(t==null){
 			p.sendMessage(Messages.PLAYER_NOT_ONLINE);
 			return;
 		}
 		for(BSPlayer player: PlayerManager.getPlayers()){
 			if(!player.equals(p)){
 			teleportPlayerToPlayer(player,t);
 			}
 			player.sendMessage(Messages.ALL_PLAYERS_TELEPORTED.replace("{player}", t.getDisplayingName()));
 		}	
 	}
 
 	public static void teleportPlayerToLocation(BSPlayer p, Location t) {
 
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportToLocation");
 			out.writeUTF(p.getName());
 			out.writeUTF(t.serialise());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		sendPluginMessageTaskTP(t.getServer(),b);
 		if(!p.getServer().getInfo().equals(t.getServer())){
 			p.getProxiedPlayer().connect(t.getServer());
 		}
 	}
 	
 	public static void sendPluginMessageTaskTP(ServerInfo server, ByteArrayOutputStream b){
 		BungeeSuite.proxy.getScheduler().runAsync(BungeeSuite.instance, new SendPluginMessage(OUTGOING_CHANNEL,server, b));
 	}
 
 	public static void teleportPlayerToPlayer(String sender, String player, String target, boolean silent, boolean bypass) {
 		BSPlayer s = PlayerManager.getPlayer(sender);
 		BSPlayer p = PlayerManager.getSimilarPlayer(player);
 		BSPlayer t = PlayerManager.getSimilarPlayer(target);
 		if(p==null || t==null){
 			s.sendMessage(Messages.PLAYER_NOT_ONLINE);
 			return;
 		}
 		if(!bypass){
 			if(!playerIsAcceptingTeleports(p) || !playerIsAcceptingTeleports(t)){
 				s.sendMessage(Messages.TELEPORT_UNABLE);
 				return;
 			}
 		}
 		if(!(sender.equals(player) || sender.equals(target))){
 			s.sendMessage(Messages.PLAYER_TELEPORTED.replace("{player}", p.getName()).replace("{target}", t.getName()));
 		}
 		teleportPlayerToPlayer(p, t);
 		if(!silent){
 			t.sendMessage(Messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}",p.getName()));
 		}
 		p.sendMessage(Messages.TELEPORTED_TO_PLAYER.replace("{player}",t.getName()));	
 	}
 
 }
 
 
