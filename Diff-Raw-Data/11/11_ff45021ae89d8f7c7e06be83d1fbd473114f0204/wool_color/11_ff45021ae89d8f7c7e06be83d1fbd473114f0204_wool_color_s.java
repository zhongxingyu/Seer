 package com.firestar.wool_color;
 
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.io.*;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 /**
 * @author Firestar
 */
 
 public class wool_color extends JavaPlugin{
 	protected static final Logger log = Logger.getLogger("Minecraft");
 	public Hashtable<String,Integer> colors= new Hashtable<String,Integer>();
 	public wool_color(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File  folder, File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
         System.out.println(desc.getName() + " version " + desc.getVersion() + " initialized");
     }
 	public int color_to_num(String s){
 		if(colors.containsKey(s)){
 			return colors.get(s);
 		}
		return Integer.valueOf(s);
 	}
 	public void give_wool(Player player, String color, Integer amount){
 		Byte data = null;
 		String s= String.valueOf(color_to_num(color));
 		data = Byte.valueOf(s);
 		player.getInventory().all(35);
 		for (int i = 0; i < amount; i++){
 			player.getInventory().addItem(new ItemStack(35, (int) 64, (byte)0, data));
 		}
 		player.sendMessage("giving yourself "+(amount*64)+" "+color+" wool");
 	}
 	public boolean onCommand(Player player, Command command, String commandLabel, String[] args) {
 		if(player.isOp()){
 	        String commandName = command.getName().toLowerCase();
 	        if(commandName.equalsIgnoreCase("gw")){
 	        	if(args.length==1){
 	        		if(args[0].equalsIgnoreCase("list")){
 		        		String mi="";
 		        		for ( Map.Entry<String, Integer> entry : colors.entrySet() ){
 							String key = entry.getKey();
 							mi=mi+key+",";
 						}
 		        		player.sendMessage(mi);
 	        		}else{
 	        			give_wool(player,args[0],1);
 	        		}
 	        	}else if(args.length==2){
 	        		give_wool(player,args[0],Integer.valueOf(args[1]));
 	        	}else{
 	        		give_wool(player,"white",1);
 	        	}
 	        	return true;
 	        }
 		}
         return false;
     }
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 		colors.put("orange",1);
 		colors.put("white",0);
 		colors.put("magenta",2);
 		colors.put("light_blue",3);
 		colors.put("yellow",4);
 		colors.put("lime",5);
 		colors.put("pink",6);
 		colors.put("gray",7);
 		colors.put("light_gray",8);
 		colors.put("cyan",9);
 		colors.put("purple",10);
 		colors.put("blue",11);
 		colors.put("brown",12);
 		colors.put("green",13);
 		colors.put("red",14);
 		colors.put("black",15);
 	}
 	public void onDisable() {
 	}
 }
