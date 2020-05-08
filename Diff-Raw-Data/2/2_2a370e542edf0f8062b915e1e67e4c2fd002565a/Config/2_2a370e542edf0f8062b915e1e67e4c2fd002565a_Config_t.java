 package com.lala.wordrank;
 
 import java.io.File;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.Group;
 import com.nijiko.permissions.User;
 
 public class Config extends Configuration{
 	public Config(File file){
 		super(file);		
 	}
 	private static Config getYML(){
 		File d = WordRank.data;
 		final File yaml = new File(d, "config.yml");
 		if (!d.exists()){
 			d.mkdirs();
 		}
 		final Config yml = new Config(yaml);
 		if (yaml.exists()){
 			yml.load();
 		}
 		return yml;	
 	}
 	private static Config getGYML(String world){
 		File d = new File("/plugins/Permissions/" + world + "/groups.yml");		
 		if (!d.exists()){
 			d.mkdirs();
 		}
 		final Config yml = new Config(d);
 		if (d.exists()){
 			yml.load();
 		}
 		return yml;	
 	}
 	public static void addWord(String word, String group){
 		final Config yml = getYML();
 		yml.setProperty("config.wordlist." + word + ".group", group);
 		yml.save();
 	}
 	public static boolean exists(String word){
 		final Config yml = getYML();
 		Object j;
 		j = yml.getProperty("config.wordlist." + word);
 		if (j == null){
 			return false;
 		}else{
 			return true;
 		}
 	}
 	public static void removeall(){
 		final Config yml = getYML();
 		yml.removeProperty("config.wordlist");
 		yml.save();
 	}
 	public static void remove(String word){
 		final Config yml = getYML();
		yml.removeProperty("config.wordlist." + word);
 		yml.save();
 		return;
 	}
 	public static String getWordGroup(String word){
 		final Config yml = getYML();
 		String f;
 		f = yml.getString("config.wordlist." + word + ".group");
 		if (f != null){
 			return f;
 		}else{
 			return null;
 		}
 	}
 	public static void loadPluginSettings(){
 		final Config yml = getYML();
 		yml.getString("config.congrats-msg", "Congrats %player%! You are now in the group %group%");
 		yml.save();
 	}
 	public static String getCongratsMsg(){
 		final Config yml = getYML();
 		String c = yml.getString("config.congrats-msg", "Congrats %player%! You are now in the group %group%");
 		return c;
 	}
 	public static boolean groupExists(String group, World world){
 		final Config yml = getGYML(world.getName());
 		Object o = yml.getString("groups." + group, "");
 		if (o != null){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	public static boolean addParent(Player player, String group) {
 		User user = WordRank.permissionHandler.getUserObject(player.getWorld().getName(), player.getName());
 		Group groups = WordRank.permissionHandler.getGroupObject(player.getWorld().getName(), group);
 		user.addParent(groups);
 		WordRank.permissionHandler.reload();
 		return true;
 	}
 }
