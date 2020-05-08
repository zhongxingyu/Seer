 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.modwizcode.plugin.colorCom;
 
 import java.util.regex.Pattern;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Starbuck Johnson
  */
 public class ColorCom extends JavaPlugin implements Listener{
     @Override
     public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
     }
     
     @Override
     public void onDisable() {
         
     }
      
     @EventHandler
     public void onChat(AsyncPlayerChatEvent event) {
         String finishedProduct = event.getMessage();
         
        Pattern blackColor = Pattern.compile("&0",Pattern.CASE_INSENSITIVE);
         Pattern blueColor = Pattern.compile("&1",Pattern.CASE_INSENSITIVE);
         Pattern greenColor = Pattern.compile("&2",Pattern.CASE_INSENSITIVE);
         Pattern darkAquaColor = Pattern.compile("&3",Pattern.CASE_INSENSITIVE);
         Pattern darkRedColor = Pattern.compile("&4",Pattern.CASE_INSENSITIVE);
         Pattern purpleColor = Pattern.compile("&5",Pattern.CASE_INSENSITIVE);
         Pattern orangeColor = Pattern.compile("&6",Pattern.CASE_INSENSITIVE);
         Pattern greyColor = Pattern.compile("&7",Pattern.CASE_INSENSITIVE);
         Pattern darkGrayColor = Pattern.compile("&8",Pattern.CASE_INSENSITIVE);
         Pattern lightBlueColor = Pattern.compile("&9",Pattern.CASE_INSENSITIVE);
         Pattern lightGreenColor = Pattern.compile("&a",Pattern.CASE_INSENSITIVE);
         Pattern lightAquaColor = Pattern.compile("&b",Pattern.CASE_INSENSITIVE);
         Pattern redColor = Pattern.compile("&c",Pattern.CASE_INSENSITIVE);
         Pattern lightPurpleColor = Pattern.compile("&d",Pattern.CASE_INSENSITIVE);
         Pattern yellowColor = Pattern.compile("&e",Pattern.CASE_INSENSITIVE);
         Pattern whiteColor = Pattern.compile("&f",Pattern.CASE_INSENSITIVE);
         
         Pattern magicColor = Pattern.compile("&k",Pattern.CASE_INSENSITIVE);
	    Pattern boldColor = Pattern.compile("&l",Pattern.CASE_INSENSITIVE);
	    Pattern strikeThroughColor = Pattern.compile("&m",Pattern.CASE_INSENSITIVE);
	    Pattern underlineColor = Pattern.compile("&n",Pattern.CASE_INSENSITIVE);
	    Pattern italicColor = Pattern.compile("&o",Pattern.CASE_INSENSITIVE);
	    Pattern resetColor = Pattern.compile("&r",Pattern.CASE_INSENSITIVE);
         
         Pattern escapeCode = Pattern.compile("&§|&&");
         
         
         finishedProduct = blackColor.matcher(finishedProduct).replaceAll(ChatColor.BLACK.toString());
         finishedProduct = blueColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_BLUE.toString());
         finishedProduct = greenColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_GREEN.toString());
         finishedProduct = darkAquaColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_AQUA.toString());
         finishedProduct = darkRedColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_RED.toString());
         finishedProduct = purpleColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_PURPLE.toString());
         finishedProduct = orangeColor.matcher(finishedProduct).replaceAll(ChatColor.GOLD.toString());
         finishedProduct = greyColor.matcher(finishedProduct).replaceAll(ChatColor.GRAY.toString());
         finishedProduct = darkGrayColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_GRAY.toString());
         finishedProduct = lightBlueColor.matcher(finishedProduct).replaceAll(ChatColor.BLUE.toString());
         finishedProduct = lightGreenColor.matcher(finishedProduct).replaceAll(ChatColor.GREEN.toString());
         finishedProduct = lightAquaColor.matcher(finishedProduct).replaceAll(ChatColor.AQUA.toString());
         finishedProduct = redColor.matcher(finishedProduct).replaceAll(ChatColor.RED.toString());
         finishedProduct = lightPurpleColor.matcher(finishedProduct).replaceAll(ChatColor.LIGHT_PURPLE.toString());
         finishedProduct = yellowColor.matcher(finishedProduct).replaceAll(ChatColor.YELLOW.toString());
         finishedProduct = whiteColor.matcher(finishedProduct).replaceAll(ChatColor.WHITE.toString());
         
         finishedProduct = magicColor.matcher(finishedProduct).replaceAll(ChatColor.MAGIC.toString());
         finishedProduct = boldColor.matcher(finishedProduct).replaceAll(ChatColor.BOLD.toString());
         finishedProduct = strikeThroughColor.matcher(finishedProduct).replaceAll(ChatColor.STRIKETHROUGH.toString());
         finishedProduct = underlineColor.matcher(finishedProduct).replaceAll(ChatColor.UNDERLINE.toString());
         finishedProduct = italicColor.matcher(finishedProduct).replaceAll(ChatColor.ITALIC.toString());
         finishedProduct = resetColor.matcher(finishedProduct).replaceAll(ChatColor.RESET.toString());
         
         finishedProduct = escapeCode.matcher(finishedProduct).replaceAll("&");
         
         event.setMessage(finishedProduct);
     }
     
     public String onChat(String event) {
         String finishedProduct = event;
         
         Pattern blackColor = Pattern.compile("&0",Pattern.CASE_INSENSITIVE);
         Pattern blueColor = Pattern.compile("&1",Pattern.CASE_INSENSITIVE);
         Pattern greenColor = Pattern.compile("&2",Pattern.CASE_INSENSITIVE);
         Pattern darkAquaColor = Pattern.compile("&3",Pattern.CASE_INSENSITIVE);
         Pattern darkRedColor = Pattern.compile("&4",Pattern.CASE_INSENSITIVE);
         Pattern purpleColor = Pattern.compile("&5",Pattern.CASE_INSENSITIVE);
         Pattern orangeColor = Pattern.compile("&6",Pattern.CASE_INSENSITIVE);
         Pattern greyColor = Pattern.compile("&7",Pattern.CASE_INSENSITIVE);
         Pattern darkGrayColor = Pattern.compile("&8",Pattern.CASE_INSENSITIVE);
         Pattern lightBlueColor = Pattern.compile("&9",Pattern.CASE_INSENSITIVE);
         Pattern lightGreenColor = Pattern.compile("&a",Pattern.CASE_INSENSITIVE);
         Pattern lightAquaColor = Pattern.compile("&b",Pattern.CASE_INSENSITIVE);
         Pattern redColor = Pattern.compile("&c",Pattern.CASE_INSENSITIVE);
         Pattern lightPurpleColor = Pattern.compile("&d",Pattern.CASE_INSENSITIVE);
         Pattern yellowColor = Pattern.compile("&e",Pattern.CASE_INSENSITIVE);
         Pattern whiteColor = Pattern.compile("&f",Pattern.CASE_INSENSITIVE);
         
         Pattern magicColor = Pattern.compile("&k",Pattern.CASE_INSENSITIVE);
 	Pattern boldColor = Pattern.compile("&l",Pattern.CASE_INSENSITIVE);
 	Pattern strikeThroughColor = Pattern.compile("&m",Pattern.CASE_INSENSITIVE);
 	Pattern underlineColor = Pattern.compile("&n",Pattern.CASE_INSENSITIVE);
 	Pattern italicColor = Pattern.compile("&o",Pattern.CASE_INSENSITIVE);
 	Pattern resetColor = Pattern.compile("&r",Pattern.CASE_INSENSITIVE);
         
         Pattern escapeCode = Pattern.compile("&§|&&");
         
         
         finishedProduct = blackColor.matcher(finishedProduct).replaceAll(ChatColor.BLACK.toString());
         finishedProduct = blueColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_BLUE.toString());
         finishedProduct = greenColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_GREEN.toString());
         finishedProduct = darkAquaColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_AQUA.toString());
         finishedProduct = darkRedColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_RED.toString());
         finishedProduct = purpleColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_PURPLE.toString());
         finishedProduct = orangeColor.matcher(finishedProduct).replaceAll(ChatColor.GOLD.toString());
         finishedProduct = greyColor.matcher(finishedProduct).replaceAll(ChatColor.GRAY.toString());
         finishedProduct = darkGrayColor.matcher(finishedProduct).replaceAll(ChatColor.DARK_GRAY.toString());
         finishedProduct = lightBlueColor.matcher(finishedProduct).replaceAll(ChatColor.BLUE.toString());
         finishedProduct = lightGreenColor.matcher(finishedProduct).replaceAll(ChatColor.GREEN.toString());
         finishedProduct = lightAquaColor.matcher(finishedProduct).replaceAll(ChatColor.AQUA.toString());
         finishedProduct = redColor.matcher(finishedProduct).replaceAll(ChatColor.RED.toString());
         finishedProduct = lightPurpleColor.matcher(finishedProduct).replaceAll(ChatColor.LIGHT_PURPLE.toString());
         finishedProduct = yellowColor.matcher(finishedProduct).replaceAll(ChatColor.YELLOW.toString());
         finishedProduct = whiteColor.matcher(finishedProduct).replaceAll(ChatColor.WHITE.toString());
         
         finishedProduct = magicColor.matcher(finishedProduct).replaceAll(ChatColor.MAGIC.toString());
         finishedProduct = boldColor.matcher(finishedProduct).replaceAll(ChatColor.BOLD.toString());
         finishedProduct = strikeThroughColor.matcher(finishedProduct).replaceAll(ChatColor.STRIKETHROUGH.toString());
         finishedProduct = underlineColor.matcher(finishedProduct).replaceAll(ChatColor.UNDERLINE.toString());
         finishedProduct = italicColor.matcher(finishedProduct).replaceAll(ChatColor.ITALIC.toString());
         finishedProduct = resetColor.matcher(finishedProduct).replaceAll(ChatColor.RESET.toString());
         
         finishedProduct = escapeCode.matcher(finishedProduct).replaceAll("&");
        
         
         return finishedProduct;
     }
 }
