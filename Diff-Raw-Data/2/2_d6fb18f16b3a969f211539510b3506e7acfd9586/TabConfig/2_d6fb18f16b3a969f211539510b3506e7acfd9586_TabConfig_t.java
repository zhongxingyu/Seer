 package org.mcsg.double0negative.tabconfig;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcsg.double0negative.tabapi.TabAPI;
 
 public class TabConfig extends JavaPlugin implements Listener, CommandExecutor{
 
 
 
 	String[][] tab;
 
 	private boolean updateAllOnPlayerLogin = false;;
 	private boolean updateAllOnPlayerLogout = false;
 	private int updateTimer = -1;
 	
 	private HashMap<String, int[]>ping = new HashMap<String, int[]>();
 
 	public void onEnable(){
 
 		File y = this.getDataFolder();
 		File f = new File(this.getDataFolder(), "config.yml");
 		if(!f.exists()){y.mkdirs(); loadFile("config.yml");}
 		
 		
 		if(Bukkit.getPluginManager().getPlugin("TabAPI") == null){
 
 			System.out.println(ChatColor.DARK_RED + "[TabConfig] TabAPI not detected! -  disabling");
 			return;
 		}		
 
 		tab = new String[TabAPI.getVertSize()][TabAPI.getHorizSize()];
 
 
 
 		this.getCommand("tabconfig").setExecutor(this);
 
 		Bukkit.getServer().getPluginManager().registerEvents(this, this);
 		
 		
 		load();
 
 
 		System.out.println("[TabConfig] Loaded!");
 
 		if(updateTimer != -1){
 			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 				public void run(){
 					updateAll();
 				}
 			}, 10, updateTimer);
 		}
 
 	}
 	
 	public void load(){
 		
 		this.reloadConfig();
 		FileConfiguration f = this.getConfig();
 
 
 		updateAllOnPlayerLogin = f.getBoolean("updateAllOnPlayerLogin");
 		updateAllOnPlayerLogout = f.getBoolean("updateAllOnPlayerLogout");
 		updateTimer = f.getInt("updateTimer");
 		
 		for(int a = 0; a < TabAPI.getVertSize(); a++){
 			int b = 0; 
 			for(String s: f.getStringList("tab."+(a+1))){
 				//System.out.println(a+" "+b+" "+s);
 				int c = 0;
 				while((c=s.indexOf("{ping",c)) != -1){
 					int v = s.indexOf("}", c);
 					String t = s.substring(c, v);
 					String[] spl = t.split("!");
 					ping.put(spl[1], new int[2]);
 					
 					c = v;
 					
 				}
 				tab[a][b] = s;
 				b++;
 
 				if(b > 2) break;
 			}
 		}
 		
 		
 		if(ping.size() > 0){
 			Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
 				public void run(){
 					for(String s: ping.keySet()){
 						String full = (s.contains(":"))?s:s+":25565";
 						String[] ip = full.split(":");
 						
 						try {
 						    ping.put(s, Pinger.ping(ip[0],Integer.parseInt(ip[1])));
 						} catch (NumberFormatException
 							| IOException exception) {
 						    exception.printStackTrace();
 						}
 						
 						
 					}
 				}
 			}, 100, 100);
 		}
 		
 		updateAll();
 	}
 
 	
 	
     @Override
     public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args){
         PluginDescriptionFile pdfFile = this.getDescription();
 
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
             if(player.hasPermission("tabapi.reload")){
             	load();
             	updateAll();
             	player.sendMessage(ChatColor.GOLD+"TabConfig reloaded!");
                 player.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabConfig - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
             }
             else{
                 player.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabAPI - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
 
             }
         }
         else{
             sender.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabAPI - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
             return true;
         }
         
         return true;
         
     }
 
 	public String replaceVars(String s, Player p){
 		String r = s;
 
 		r = r.replace("{online}", Bukkit.getOnlinePlayers().length+"");
 		r = r.replace("{max}", Bukkit.getMaxPlayers()+"");
 		r = r.replace("{player}", p.getName());
 		r = r.replace("{displayname}", p.getDisplayName());
 		r = r.replace("{tabname}", p.getPlayerListName());
 		r = r.replace("{servername}", Bukkit.getServerName());
 
 		r = ChatColor.translateAlternateColorCodes('&', r);
 
 		
 		int c = 0;
 		while((c=r.indexOf("{ping",c)) != -1){
 			int v = r.indexOf("}", c);
 			String t = r.substring(c, v);
 			String[] spl = t.split("!");
 			String sy = "";
 			if(spl[2].equalsIgnoreCase("online")){
 				sy = ping.get(spl[1])[0] +"";
 			}
 			if(spl[2].equalsIgnoreCase("max")){
 				sy = ping.get(spl[1])[1] +"";
 			}
 			//System.out.println(c+"  "+v + "   "+r.length() + "  "+r);
 			r = r.substring(0,c) 
 					+ sy 
 					+ r.substring(v+1);
 
 			
 			c = c + sy.length();
 			
 			
 		}
 		
 		
 		return r;
 
 	}
 
 	public void updateAll(){
 		for(Player p: Bukkit.getOnlinePlayers()){
 			update(p);
 		}
 	}
 
 	public void update(Player p){
 
 		for(int a = 0; a < tab.length; a++){
 			for(int b = 0 ;b < tab[a].length;b++){
 
 				//System.out.println(a + " "+b);
 				if(tab[a][b] != null){
 					String y = replaceVars(tab[a][b], p);
 
 					if(y.equalsIgnoreCase("{fillplayers}")){ 
 						fillPlayers(p, a, b);
 						break;
 					}
 
 
 					TabAPI.setTabString(this, p, a, b, y + ((y.length() < 15) ? TabAPI.nextNull(): "" ));
 				}
 			}
 		}
 
 		TabAPI.updatePlayer(p);
 
 
 	}
 
 
 	private void fillPlayers(Player p, int a, int b) {
 		for(Player pl:Bukkit.getOnlinePlayers()){
 			TabAPI.setTabString(this, p, a, b, pl.getName()+ ((pl.getName().length() < 15) ? TabAPI.nextNull(): ""));
 
 			b++;
 			if(b == 3){
 				b = 0; 
 				a++;
 			}
			if(a > TabAPI.getVertSize() - 1){
 				return;
 			}
 
 		}
 
 	}
 
 	@EventHandler
 	public void playerLogin(PlayerLoginEvent e){
 		final Player p = e.getPlayer();
 		TabAPI.setPriority(this, p, 0);
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 			public void run(){
 				if(updateAllOnPlayerLogin){
 					updateAll();
 				}
 				else{
 					update(p.getPlayer());
 				}
 			}
 		}, 2);
 
 
 	}
 
 	@EventHandler
 	public void playerLogout(PlayerQuitEvent e){
 		if(updateAllOnPlayerLogout){
 			updateAll();
 		}
 
 	}
 	
 	
 	public void loadFile(String file){
 		File t = new File(this.getDataFolder(), file);
 		System.out.println("Writing new file: "+ t.getAbsolutePath());
 			
 			try {
 				t.createNewFile();
 				FileWriter out = new FileWriter(t);
 				System.out.println(file);
 				InputStream is = getClass().getResourceAsStream(file);
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				String line;
 				while ((line = br.readLine()) != null) {
 					out.write(line+"\n");
 					System.out.println(line);
 				}
 				out.flush();
 				is.close();
 				isr.close();
 				br.close();
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		
 	}
 
 
 }
