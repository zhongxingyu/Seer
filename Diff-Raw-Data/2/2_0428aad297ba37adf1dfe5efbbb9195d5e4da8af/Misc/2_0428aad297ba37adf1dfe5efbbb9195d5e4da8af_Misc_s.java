 package no.HON95.ButtonCommands;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Misc
 {
 
 	public static String insertAll(String line, Player player, Block block)
 	{
 
 		line = insertServerVariables(line);
 		line = insertBlockVariables(line, block);
 		line = insertPlayerVariables(line, player);
 
 		return line;
 	}
 
 	public static String insertAll(String line, Player player)
 	{
 
 		line = insertServerVariables(line);
 		line = insertPlayerVariables(line, player);
 
 		return line;
 	}
 
 	public static String insertAll(String line, CommandSender sender, Block block)
 	{
 
 		line = insertServerVariables(line);
 		line = insertBlockVariables(line, block);
 		line = line.replace("{N}", sender.getName());
 
 		return line;
 	}
 
 	public static String insertServerVariables(String line)
 	{
 
 		line = line.replace("{SBV}", Bukkit.getServer().getBukkitVersion());
 		line = line.replace("{SV}", Bukkit.getServer().getVersion());
 		line = line.replace("{SIP}", (Bukkit.getServer().getIp() + "").replace("/", "").split(":")[0]);
 		line = line.replace("{SIPP}", (Bukkit.getServer().getIp() + "").replace("/", ""));
 		line = line.replace("{SP}", Bukkit.getServer().getPort() + "");
 		line = line.replace("{SN}", Bukkit.getServer().getServerName());
 
 		return line;
 	}
 
 	public static String insertBlockVariables(String line, Block block)
 	{
 
 		line = line.replace("{BW}", block.getWorld().getName());
 		line = line.replace("{BX}", block.getX() + "");
 		line = line.replace("{BY}", block.getY() + "");
 		line = line.replace("{BZ}", block.getZ() + "");
 
 		return line;
 	}
 
 	public static String insertPlayerVariables(String line, Player player)
 	{
 
 		line = line.replace("{N}", player.getName());
 		line = line.replace("{PW}", player.getWorld().getName());
 		line = line.replace("{DN}", player.getDisplayName());
 		line = line.replace("{PX}", player.getLocation().getBlockX() + "");
 		line = line.replace("{PY}", player.getLocation().getBlockY() + "");
 		line = line.replace("{PZ}", player.getLocation().getBlockZ() + "");
 		line = line.replace("{PIPP}", (player.getAddress() + "").replace("/", ""));
 		line = line.replace("{PIP}", ((player.getAddress() + "").replace("/", "")).split(":")[0]);
 		line = line.replace("{XP}", player.getExp() + "");
 		line = line.replace("{GM}", player.getGameMode().name());
 		line = line.replace("{ID}", player.getEntityId() + "");
 
 		return line;
 	}
 
 	public static String insertColors(String line)
 	{
 		return ChatColor.translateAlternateColorCodes('&', line);
 	}
 
 	public static String[] concatCmd(String[] stArr)
 	{
 		StringBuilder cmdBuild = new StringBuilder();
 		String cmdFull;
 		String cmdName;
 
 		for (int c = 1; c < stArr.length; c++)
			cmdBuild.append(stArr[c]);
 		cmdFull = cmdBuild.toString().trim();
 		cmdName = cmdFull.split(" ", 2)[0];
 
 		return new String[] { cmdName, cmdFull };
 	}
 
 	public static void checkVersion(final JavaPlugin plugin, final String address)
 	{
 
 		if (plugin == null || address == null)
 			throw new IllegalArgumentException();
 
 		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
 		{
 
 			@Override
 			public void run()
 			{
 				String cv = plugin.getDescription().getVersion().trim();
 				String lv = null;
 				String li;
 				String[] lp;
 
 				try
 				{
 					URL url = new URL(address);
 					BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 					while (true)
 					{
 						li = in.readLine();
 						if (li == null)
 							break;
 						if (!li.startsWith(plugin.getName()))
 							continue;
 						lp = li.split(": ");
 						if (lp.length == 2)
 						{
 							if (lp[0].trim().equalsIgnoreCase(plugin.getName()))
 							{
 								lv = lp[1].trim();
 								break;
 							}
 						}
 					}
 					in.close();
 				}
 				catch (Exception ex)
 				{
 					plugin.getLogger().warning("Failed to check for updates: " + ex.getMessage());
 					return;
 				}
 
 				if (lv == null)
 				{
 					plugin.getLogger().warning("Failed to check for updates: Didn't find plugin on file!");
 					return;
 				}
 
 				if (!cv.equalsIgnoreCase(lv))
 				{
 					try
 					{
 						double dc = Double.parseDouble(cv);
 						double dl = Double.parseDouble(lv);
 						if (dl <= dc)
 							return;
 					}
 					catch (Exception ex)
 					{
 					}
 
 					plugin.getLogger().warning("Please update from " + cv + " to " + lv);
 				}
 			}
 		});
 	}
 }
