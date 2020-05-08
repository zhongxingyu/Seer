 package com.nullblock.vemacs.trans;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Trans extends JavaPlugin implements Listener {
     public void onDisable() {
     }
 
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         this.saveDefaultConfig();
     }
 
    @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event) {
     	String lang = this.getConfig().getString("lang");
     	Boolean uppercase = this.getConfig().getBoolean("uppercase");
     	Boolean bigben = false;
         String message = event.getMessage();
         HashMap hm = new HashMap(); 
         Pattern p = Pattern
 				.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?������]))");
 		Matcher m = p.matcher(message);
 		StringBuffer sb = new StringBuffer();
 		String urlTmp = "";
 		while (m.find()) {
 			urlTmp = m.group(1);
 			String uuid = UUID.randomUUID().toString().replace("-", "");
 			hm.put(uuid, urlTmp);
             message.replace(urlTmp, uuid);
 			m.appendReplacement(sb, "");
 			sb.append(urlTmp);
 		}
 		m.appendTail(sb);
 		message = sb.toString();
         message = TransUtil.getTranslation(message, lang);
         Set set = hm.entrySet(); 
         Iterator i = set.iterator(); 
         while(i.hasNext()) { 
         	Map.Entry me = (Map.Entry)i.next(); 
         	message.replace(me.getKey().toString(), me.getValue().toString()); 
         	} 
 
         if( uppercase == true ){
         message = message.toUpperCase(new Locale(lang));
         }
         event.setMessage(message);
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	if(cmd.getName().equalsIgnoreCase("trans") && sender.hasPermission("trans.reload")){
     		this.reloadConfig();
     		sender.sendMessage(ChatColor.GREEN + "Trans has been reloaded.");
     		return true;
     	} 
     	return false; 
     }
 
 }
 
