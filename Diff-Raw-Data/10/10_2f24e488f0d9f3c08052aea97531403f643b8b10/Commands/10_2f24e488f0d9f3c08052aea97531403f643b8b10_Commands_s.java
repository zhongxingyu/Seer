 /**
  *  Name: Commands.java
  *  Date: 16:28:41 - 16 aug 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *  Filedescription:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.register;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class Commands implements CommandExecutor {
 	
 	private ConsoleLogger	logger;
 	private Main			plugin;
 	
 	@SuppressWarnings("serial")
 	private final HashMap<String, Integer>	idlist	= new HashMap<String, Integer>() {{
 			put("bannad", 7);
 			put("medlem", 49);
 			put("vipa", 43);
 			put("vipb", 44);
 			put("vipc", 45);
 			put("helper", 46);
 			put("byggare", 47);
 	}};
 	
 	public Commands(Main instance) {
 		this.plugin = instance;
 		this.logger = new ConsoleLogger("CommandExecutor");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if (cmd.getName().toLowerCase().equals("reg")) {
 			
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(ChatColor.RED + "Du mste vara en spelare fr att kunna anvnda detta kommando!");
 				return true;
 			}
 			
 			Player player = (Player) sender;
 			
 			if (!isInRegion(player.getLocation())) {
 				sender.sendMessage(ChatColor.RED + "Du mste befinna dig vid regelskyltarna fr att kunna anvnda detta kommando!");
 				return true;
 			}
 			
 			if (args.length != 2) {
 				player.sendMessage(ChatColor.RED + "Du mste skriva bde email och lsenord!");
 				return false;
 			}
 			
 			String email = args[0];
 			String password = args[1];
 			String name = player.getName();
 			
			if (!emailValidator(email)) {
 				player.sendMessage(ChatColor.RED + "Eposten " + ChatColor.GREEN + email + ChatColor.RED + " r inte giltig, var god kolla s du skrivit rtt." + ChatColor.BLUE + " Exempel: dittnamn@gmail.com");
 				return true;
 			}
			
			else if (!passwordValidator(password)) {
 				player.sendMessage(ChatColor.RED + "Lsenordet " + ChatColor.BLUE + password + ChatColor.RED + " mste ha minst en siffra och vara minst 5-20 tecken lng!");
 				return true;
 			}
 			
 			logger.debug("Registering user: " + name);
 			String urlString = this.plugin.getConfig().getString("scripts.register") + "?key=" + this.plugin.getConfig().getString("APIkeys.register") + "&username=" + name + "&email=" + email + "&pass=" + password;
 			new RegisterThread(plugin, player, email, password, urlString);
 			return true;
 		}
 		else if (cmd.getName().toLowerCase().equals("mpromote")) {
 			return promoteDemote(sender, args);
 		}
 		else if (cmd.getName().toLowerCase().equals("mdemote")) {
 			return promoteDemote(sender, args);
 		}
 		
 		return false;
 	}
 	
 	private boolean promoteDemote(CommandSender sender, String[] args) {
 		
 		String playername = args[0];
 		
 		if (args.length != 2) {
 			sender.sendMessage(ChatColor.RED + "Felaktig anvndning!");
 			return false;
 		}
 		
 		if (!this.idlist.containsKey(args[1])) {
 			sender.sendMessage(ChatColor.RED + args[1] + " ranken existerar inte!");
 			return true;
 		}
 		
 		int rank = this.idlist.get(args[1]);
 		
 		String urlString = this.plugin.getConfig().getString("scripts.promote") + "?key=" + this.plugin.getConfig().getString("APIkeys.promote") + "&username=" + playername + "&rank=" + rank;
 		String answer = sendGETdata(urlString);
 		
 		logger.debug("Answer from sendGETdata: '" + answer + "'");
 		
 		if (answer != null) {
 			switch (answer) {
 			
 				case "0":
 					sender.sendMessage(ChatColor.GREEN + "Rankndring lyckad!");
 					
 					Player player = Bukkit.getPlayer(playername);
 					if (player != null)
 						player.chat("/sync");
 					else
 						sender.sendMessage(ChatColor.GREEN + "Spelaren verkar inte vara online! Sg t denna att anvnda /sync nsta gng denne logger in!");
 					
 					break;
 				
 				case "1":
 					sender.sendMessage(ChatColor.RED + "Rankndringen kunde inte genomfras!");
 					break;
 				
 				case "2":
 					sender.sendMessage(ChatColor.RED + playername + " r redan " + args[1] + " ingen ndring gjordes.");
 					break;
 				
 				case "3":
 					sender.sendMessage(ChatColor.RED + playername + " r en moderator. Du kan inte promota/demota en sdan medlem");
 					break;
 				
 				case "4":
 					sender.sendMessage(ChatColor.RED + playername + " finns inte!");
 					break;
 				
 				default:
 					sender.sendMessage(ChatColor.RED + "Felaktigt svar frn hemsidan!");
 					break;
 			}
 		}
 		else {
 			sender.sendMessage(ChatColor.RED + "Verkar inte ha ftt ngot svar frn hemsidan! Kolla loggen och frsk igen.");
 		}
 		
 		return true;
 	}
 	
 	private String sendGETdata(String urlString) {
 		
 		String answer = null;
 		
 		try {
 			
 			logger.debug("Sending url: " + urlString);
 			
 			URL url = new URL(urlString);
 			URLConnection connection = url.openConnection();
 			
 			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			answer = in.readLine();
 			
 			logger.debug("Answer: " + answer);
 			
 			in.close();
 			
 		}
 		catch (IOException e) {
 			logger.severe(e.getMessage());
 			return null;
 		}
 		
 		return answer;
 	}
 	
 	/**
 	 * Validiera lsenordet
 	 * 
 	 * @param password
 	 * @return true giltigt lsenord, false ogiltigt lsenord
 	 */
 	
 	private boolean passwordValidator(final String password) {
 		
 		final Pattern pattern;
 		final Matcher matcher;
 		
 		final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z]).{5,20})";
 		
 		pattern = Pattern.compile(PASSWORD_PATTERN);
 		matcher = pattern.matcher(password);
 		return matcher.matches();
 		
 	}
 	
 	/**
 	 * Validiera emailadressen som personen skriver in
 	 * 
 	 * @param email
 	 * @return true giltig email, false ogiltig email Taget frn: http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
 	 */
 	private boolean emailValidator(final String email) {
 		
 		final Pattern pattern;
 		final Matcher matcher;
 		
 		final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 		
 		pattern = Pattern.compile(EMAIL_PATTERN);
 		matcher = pattern.matcher(email);
 		return matcher.matches();
 		
 	}
 	
 	private boolean isInRegion(Location playerlocation) {
 		
 		String regionname = this.plugin.getConfig().getString("commandRegionName");
 		
 		if (regionname == null) {
 			return true;
 		}
 		ApplicableRegionSet set = getWGSet(playerlocation);
 		if (set == null) {
 			return false;
 		}
 		for (ProtectedRegion r : set) {
 			if (r.getId().equalsIgnoreCase(regionname)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private static ApplicableRegionSet getWGSet(Location loc) {
 		WorldGuardPlugin wg = getWorldGuard();
 		if (wg == null) {
 			return null;
 		}
 		RegionManager rm = wg.getRegionManager(loc.getWorld());
 		if (rm == null) {
 			return null;
 		}
 		return rm.getApplicableRegions(com.sk89q.worldguard.bukkit.BukkitUtil.toVector(loc));
 	}
 	
 	public static WorldGuardPlugin getWorldGuard() {
 		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
 		
 		// WorldGuard may not be loaded
 		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
 			return null;
 		}
 		return (WorldGuardPlugin) plugin;
 	}
 }
