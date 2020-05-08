 package de.jwhy.chattykitten;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChattyKitten extends JavaPlugin implements Listener {
 	private String header_before = ChatColor.AQUA + "======[";
 	private String header_after = ChatColor.AQUA + "]======";
 	
     public void onDisable() {
         
     }
 
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityDamageByEntityEvent(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
     	if(event.getEntity() instanceof org.bukkit.entity.LivingEntity){
     		LivingEntity receiver = (LivingEntity) event.getEntity();
     		if(event.getDamager() instanceof Player){
     			Player sender = (Player)event.getDamager();
     			if((sender.hasPermission("chattykitten.type." + receiver.getType().toString())
     					|| sender.hasPermission("chattykitten.type.*"))
     					&& sender.isSneaking()) {
     				if(receiver instanceof Player){
     					if((sender.hasPermission("chattykitten.player." + ((Player) receiver).getName().toString())
     							|| sender.hasPermission("chattykitten.player.*"))
     							&& (sender.hasPermission("chattykitten.admin")
     									|| ((Player) receiver).hasPermission("chattykitten.exempt") == false)){
     						event.setDamage(0);
     						event.setCancelled(true);
 	    	    			
     						getChatty(sender, receiver);
     					}
     				} else {
     					event.setDamage(0);
     					event.setCancelled(true);
     	    		
     					getChatty(sender, receiver);
     				}
     			}
     		}
     	}
     }
     
     private void getChatty(Player sender, LivingEntity receiver) {
 		String mobname = receiver.getType().toString();
 		
 		sender.sendMessage("");
 		sender.sendMessage(
 				header_before + ChatColor.GOLD + "Chatty" + ChatColor.YELLOW 
 				+ firstUpper(mobname) + header_after
 		);
 		
 		if(receiver instanceof Player){
 			Player receiverplayer = ((Player) receiver).getPlayer();
 
 			String rname = receiverplayer.getName().toString();
 			String rnick = receiverplayer.getDisplayName().toString();
 			getLogger().info("Name: " + rname + "; Nick: " + rnick);
 			if(rname != null) sender.sendMessage("Name: " + rname);
 			if(rnick != null && rnick != rname) sender.sendMessage("Nickname: " + rnick);
 
 			String playergm = receiverplayer.getGameMode().toString();
 			ChatColor colgm = null;
 			if(playergm == "SURVIVAL"){
 				colgm = ChatColor.DARK_RED;
 			}else if(playergm == "CREATIVE"){
 				colgm = ChatColor.DARK_GREEN;
 			}else{
 				colgm = ChatColor.DARK_RED;
 			}
 			sender.sendMessage("Gamemode: " + colgm + firstUpper(receiverplayer.getGameMode().toString()));
 		}                 
 		
		int maxh = (int) receiver.getMaxHealth();
		int acth = (int) receiver.getHealth();
 		
		String healthbar = this.getChatBarAndRaw(ColorPurpose.HEALTH, acth, maxh);
 		sender.sendMessage("Health: " + healthbar);
 		
 		if(receiver instanceof Ageable){
			int ageticks = ((Ageable) receiver).getAge();
 			String agemessage;
 			if((int)ageticks >= 0){
 				agemessage = ChatColor.DARK_GREEN + "Adult";
 			}else{
 				agemessage = ChatColor.GOLD + "Baby (" 
 						+ ageticks / -20 / 60
 						+ "m left)";
 				
 			}
     		sender.sendMessage("Growth: " + agemessage);
 		}
 		
 		if(receiver instanceof Tameable){
 			String ownermessage = "Owner: ";
 			if((((Tameable) receiver).isTamed()) == false){
 				ownermessage = ownermessage + ChatColor.RED + "Not tamed yet";
 			}else{
 				ownermessage = ownermessage
 						+ getDisplayNameAll(((OfflinePlayer)((Tameable) receiver).getOwner()).getName());
 			}
 			sender.sendMessage(ownermessage);
 		}    	
     }
     
     private String firstUpper(String content){
 		content = content.substring(0, 1).toUpperCase() + content.substring(1).toLowerCase();
     	return content;
     }
     
     private String floatFormatter(float content){
     	if(((int) content) == content){
     		return ((Integer)((int)content)).toString();  
     	}else{
     		return ((Float)content).toString();
     	}
     }
     
     private String getDisplayNameAll(String playername){
     	if(Bukkit.getOfflinePlayer(playername).isOnline()){
     		return ((Player)Bukkit.getPlayer(playername)).getDisplayName();
     	}else{
     		return Bukkit.getOfflinePlayer(playername).getName();	
     	}
     }
     
    private String getChatBarAndRaw(ColorPurpose purpose, int current, int maximal){
 		String chatbarandraw = this.getChatBar(purpose, current, maximal);
 		Double part = (double) current / maximal;
 		ChatColor partcolor;
 		
 		if(part <= 0.3){
     		partcolor = ChatColor.DARK_RED;
 		}else if(part <= 0.75){
 			partcolor = ChatColor.GOLD;
 		}else{
 			partcolor = ChatColor.DARK_GREEN;
 		}
 		
 		chatbarandraw = chatbarandraw + partcolor + " (" + floatFormatter(((float)current)/2) 
 				+ "/" + floatFormatter(((float)maximal)/2) + ")";
 		
     	return chatbarandraw;
     }
     
    private String getChatBar(ColorPurpose purpose, int current, int maximal){
 
     	String healthsymbol = "|";
     	String chatbar = "";
 		Double part = (double) current / maximal;
 		ChatColor partcolor = null;
     	HashMap<String, Float> divs = this.getColorDivision(purpose);
     	
     	if(part <= divs.get("MIDDLE")){
     		partcolor = ChatColor.DARK_RED;
 		}else if(part <= divs.get("HIGH")){
 			partcolor = ChatColor.GOLD;
 		}else{
 			partcolor = ChatColor.DARK_GREEN;
 		}
     	
     	
     	for(int i=1; i < (maximal + 1); i++){
 			if(i <= current){
 				//Progresspoint available
 				chatbar = chatbar + partcolor.toString() + healthsymbol;
 			}else{
 				//Progresspoint not available
 				chatbar = chatbar + ChatColor.GRAY + healthsymbol;
 			}
 			if(i % 2 == 0){
 				chatbar = chatbar + " ";
 			}
 		}
     	
 		return chatbar;    	
     }
     
    private HashMap<String, Float> getColorDivision(ColorPurpose purpose){
     	HashMap<String, Float> divs = new HashMap<String, Float>();
    	switch(purpose){
    		case HEALTH:
     			divs.put("LOW", 0f);
     			divs.put("MIDDLE", 0.3f);
     			divs.put("HIGH", 0.75f);
    		case HUNGER:
     			divs.put("LOW", 0f);
     			divs.put("MIDDLE", 0.3f);
     			divs.put("HIGH", 0.75f);
     		default:
     			divs.put("LOW", 0f);
     			divs.put("MIDDLE", 0.33f);
     			divs.put("HIGH", 0.66f);
     	}
     	return divs;
     }
    
    private enum ColorPurpose{
    	HEALTH, HUNGER, OTHER
    }
 }
 
