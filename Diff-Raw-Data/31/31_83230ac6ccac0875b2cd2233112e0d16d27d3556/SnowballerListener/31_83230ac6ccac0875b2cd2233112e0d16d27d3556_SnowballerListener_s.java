 package co.networkery.uvbeenzaned;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.Timer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
  
  public class SnowballerListener implements Listener
  {
 	 public static String pg = ChatColor.AQUA + "[Snowballer] " +  ChatColor.RESET;
 	 public static ConfigAccessor config;
 	 public static ConfigAccessor scores;
 	 public static boolean gameon = false;
 	 public static boolean timergame = false;
 	 public static List<String> teamcyan = new ArrayList<String>();
 	 public static List<String> teamlime = new ArrayList<String>();
 	 public static List<String> teamcyaninarena = new ArrayList<String>();
 	 public static List<String> teamlimeinarena = new ArrayList<String>();
 	 
 	 public SnowballerListener(JavaPlugin jp)
 	 {
 		 config = new ConfigAccessor(jp, "config.yml");
 		 scores = new ConfigAccessor(jp, "scores.yml");
 	 }
 	 
 	 @EventHandler
 	 public void playerLeave(PlayerQuitEvent event)
 	 {
 		 String pll = event.getPlayer().getName();
 		 if(gameon)
 		 {
 				if(teamcyaninarena.contains(pll))
 				{
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamcyaninarena.remove(pll);
 				}
 				if(teamlimeinarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pll);
 				}
 				checkTeamsInArena();
 				if(teamcyan.contains(pll))
 				{
 					scores.saveConfig();
 					teamcyan.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 				if(teamlime.contains(pll))
 				{
 					scores.saveConfig();
 					teamlime.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 				terminateAll();
 		 }
 		 else
 		 {
 				if(teamcyaninarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamcyaninarena.remove(pll);
 				}
 				if(teamlimeinarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pll);
 				}
 				if(teamcyan.contains(pll))
 				{
 					scores.saveConfig();
 					teamcyan.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 				if(teamlime.contains(pll))
 				{
 					scores.saveConfig();
 					teamlime.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 				terminateAll();
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void playerDeath(PlayerDeathEvent event)
 	 {
 		 String pld = event.getEntity().getName();
 		 if(gameon)
 		 {
 			    if(teamcyaninarena.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 			    	teamcyaninarena.remove(pld);
 				}
 				if(teamlimeinarena.contains(pld))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pld);
 				}
 				checkTeamsInArena();
 			    if(teamcyan.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	teamcyan.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 				}
 				if(teamlime.contains(pld))
 				{
 					scores.saveConfig();
 					teamlime.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 				}
 				terminateAll();
 		 }
 		 else
 		 {
 			    if(teamcyaninarena.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 			    	teamcyaninarena.remove(pld);
 				}
 				if(teamlimeinarena.contains(pld))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pld);
 				}
 			    if(teamcyan.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	teamcyan.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamcyan.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 				}
 				if(teamlime.contains(pld))
 				{
 					scores.saveConfig();
 					teamlime.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 				}
 				terminateAll();
 		 }
 	 }
 	 
 	 @SuppressWarnings("deprecation")
 	@EventHandler
 	 public void playerDropItem(PlayerDropItemEvent event)
 	 {
 		 if(gameon)
 		 {
 			 for(String pl : teamcyan)
 			 {
 				 if(pl == event.getPlayer().getName())
 				 {
 					 event.setCancelled(true);
 					 event.getPlayer().updateInventory();
 				 }
 			 }
 			 for(String pl : teamlime)
 			 {
 				 if(pl == event.getPlayer().getName())
 				 {
 					 event.setCancelled(true);
 					 event.getPlayer().updateInventory();
 				 }
 			 }
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void entityDamage(EntityDamageByEntityEvent event)
 	 {
 		 if(gameon)
 		 {
 			 if ((event.getEntity() instanceof Player && event.getDamager() instanceof Snowball))
 			 {
 				 Player plhit = (Player)event.getEntity();
 				 Snowball sb = (Snowball)event.getDamager();
 				 Player plenemy = (Player)sb.getShooter();
 				 if(plhit != plenemy)
 				 {
 					 if(teamcyaninarena.contains(plhit.getName()) || teamlimeinarena.contains(plhit.getName()))
 					 {
 						 if(teamcyaninarena.contains(plenemy.getName()) || teamlimeinarena.contains(plenemy.getName()))
 						 {
 							 if(!teamcyaninarena.contains(plhit.getName()) || !teamcyaninarena.contains(plenemy.getName()))
 							 {
 								 if(!teamlimeinarena.contains(plhit.getName()) || !teamlimeinarena.contains(plenemy.getName()))
 								 {
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 1);
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 2);
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 3);
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 4);
 									 plhit.getWorld().playEffect(plhit.getLocation(), Effect.ENDER_SIGNAL, 0);
 									 plhit.teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 									 plhit.getInventory().clear();
 									 if(teamcyaninarena.contains(plhit.getName()))
 									 {
 										 teamcyaninarena.remove(plhit.getName());
 										 giveTeamArmor(plhit, "cyan");
 									 }
 									 if(teamlimeinarena.contains(plhit.getName()))
 									 {
 										 teamlimeinarena.remove(plhit.getName());
 										 giveTeamArmor(plhit, "lime");
 									 }
 									 scores.getConfig().set(plhit.getName(), scores.getConfig().getInt(plhit.getName()) - 1);
 									 plhit.sendMessage(pg + "-1 point!  Your score is now " + String.valueOf(scores.getConfig().getInt(plhit.getName())) + ".");
 									 scores.getConfig().set(plenemy.getName(), scores.getConfig().getInt(plenemy.getName()) + 1);
 									 plenemy.sendMessage(pg + "+1 point!  Your score is now " + String.valueOf(scores.getConfig().getInt(plenemy.getName())) + ".");
 									 scores.saveConfig();
 									 sendAllTeamsMsg(pg + "There are now " + teamcyaninarena.size() + " players on team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET + " left in the arena!");
 									 sendAllTeamsMsg(pg + "There are now " + teamlimeinarena.size() + " players on team " + ChatColor.GREEN + "LIME" + ChatColor.RESET + " left in the arena!");
 									 sendAllTeamsMsg(pg + ChatColor.RED + plenemy.getName() + ChatColor.BLUE + " snowbrawled " + ChatColor.GREEN + plhit.getName() + ".");
 									 checkTeamsInArena();
 									 event.setCancelled(true);
 								 }
 							 }
 						 }
 					 }
 				 }
 			 }
 		 }
 	 }
 	 
      public static ActionListener taskPerformer = new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
          	if(timergame == true && gameon == false)
          	{
          		if(!teamcyan.isEmpty() && !teamlime.isEmpty())
          		{
 	            		sendAllTeamsMsg(pg + "Starting next round....");
 		            	randomMap();
 		            	gameon = true;
          		}
          		else
          		{
          			if(teamlime.isEmpty())
          			{
          				cyanMsg(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
          				cyanMsg(pg + "Stopping game and waiting for another player to join....");
          				timer.stop();
          				gameon = false;
          				timergame = false;
          			}
          			if(teamcyan.isEmpty())
          			{
          				limeMsg(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
          				limeMsg(pg + "Stopping game and waiting for another player to join....");
          				timer.stop();
          				gameon = false;
          				timergame = false;
          			}
          		}
          	}
          }
          };
          
          public static Timer timer;
 	 
 	 public static void startIndependentTimerRound()
 	 {
 		 if(timergame == true && gameon == false)
 		 {
 			 if(!teamcyan.isEmpty() && !teamlime.isEmpty())
 			 {
 				 timer = new Timer(config.getConfig().getInt("timerdelay"), taskPerformer);
 				 timer.setRepeats(false);
 			     timer.start();
 			     sendAllTeamsMsg(pg + "Next round starts in " + Integer.toString(config.getConfig().getInt("timerdelay") / 1000) + " seconds!");
 			 }
 			 else
 			 {
       			if(teamlime.isEmpty())
       			{
       				cyanMsg(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
       				cyanMsg(pg + "Waiting for another player to join....");
       			}
       			if(teamcyan.isEmpty())
       			{
       				limeMsg(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
       				limeMsg(pg + "Waiting for another player to join....");
       			}
 			 }
 		 }
 	 }
 	 
 	 public static Random r = new Random();
 	 
 	 public static void randomMap()
 	 {
 		r.setSeed(System.currentTimeMillis());
 		int mapnum = r.nextInt(config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false).size());
 		int i = 0;
 		for(String key : config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false))
 		{
 			if(mapnum == i)
 			{
 				for(String pl : teamcyan)
 				{
 					if(Bukkit.getServer().getPlayer(pl).getGameMode() == GameMode.CREATIVE)
 					{
 						Bukkit.getServer().getPlayer(pl).setGameMode(GameMode.SURVIVAL);
 					}
 					Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					giveSnowballs(Bukkit.getServer().getPlayer(pl));
 					teamcyaninarena.add(pl);
 					Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("teamcyanarenasides." + key)));
 				}
 				for(String pl : teamlime)
 				{
 					if(Bukkit.getServer().getPlayer(pl).getGameMode() == GameMode.CREATIVE)
 					{
 						Bukkit.getServer().getPlayer(pl).setGameMode(GameMode.SURVIVAL);
 					}
 					Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					giveSnowballs(Bukkit.getServer().getPlayer(pl));
 					teamlimeinarena.add(pl);
 					Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("teamlimearenasides." + key)));
 				}
 			}
 			i++;
 		}
 	 }
 	 
 	 public static void terminateAll()
 	 {
 		checkTeamsInArena();
		if(teamcyan.isEmpty() && teamlime.isEmpty())
 		{
 			gameon = false;
 			timergame = false;
 			if(timer != null && timer.isRunning())
 			{
 				timer.stop();
 			}
 			teamcyaninarena.clear();
 			teamlimeinarena.clear();
			teamcyan.clear();
			teamlime.clear();
 		}
 	 }
 	 
 	 public static void checkTeamsInArena()
 	 {
 		 if(gameon == true)
 		 {
 			 if(teamcyaninarena.size() == 0)
 			 {
 				 for(String pl : teamlimeinarena)
 				 {
 					 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 				 }
 				 for(String pl : teamcyaninarena)
 				 {
 					 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 				 }
 				 limeMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 				 cyanMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 				 teamcyaninarena.clear();
 				 teamlimeinarena.clear();
 				 for(String pl : teamlime)
 				 {
 					 scores.getConfig().set(pl, scores.getConfig().getInt(pl) + config.getConfig().getInt("teampoints"));
 				 }
 				 limeMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints")) + " points for all of team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + ".");
 				 cyanMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints")) + " points for all of team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + ".");
 				 scores.saveConfig();
 				 gameon = false;
 			 }
 			 else
 			 {
 				 if(teamlimeinarena.size() == 0)
 				 {
 					 for(String pl : teamcyaninarena)
 					 {
 						 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 						 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					 }
 					 for(String pl : teamlimeinarena)
 					 {
 						 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 						 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					 }
 					 cyanMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 					 limeMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 					 teamcyaninarena.clear();
 					 teamlimeinarena.clear();
 					 for(String pl : teamcyan)
 					 {
 						 scores.getConfig().set(pl, scores.getConfig().getInt(pl) + config.getConfig().getInt("teampoints"));
 					 }
 					 limeMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints")) + " points for all of team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + ".");
 					 cyanMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints")) + " points for all of team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + ".");
 					 scores.saveConfig();
 					 gameon = false;
 				 } 
 			 }
 			 if(timergame == true && gameon == false)
 			 {
 				 startIndependentTimerRound();
 			 }
 		 }
 	 }
 	 
 	 public static void giveSnowballs(Player pl)
 	 {
 		 for(int x = 0; x < 9; x++)
 		 {
 			 pl.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
 		 }
 	 }
 	 
 	 public static void giveTeamArmor(Player pl, String team)
 	 {
 		switch(team)
 		{
 			case "cyan":
 			try {
 				pl.getInventory().setChestplate(Armor.setColor(new ItemStack(Material.LEATHER_CHESTPLATE), ArmorColor.CYAN));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 				break;
 			case "lime":
 			try {
 				pl.getInventory().setChestplate(Armor.setColor(new ItemStack(Material.LEATHER_CHESTPLATE), ArmorColor.LIME));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 				break;
 		}
 	 }
 	 
 	 public static void sendAllTeamsMsg(String msg)
 	 {
 		 for(String pl : teamlime)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 }
 		 for(String pl : teamcyan)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 } 
 	 }
 	 
 	 public static void cyanMsg(String msg)
 	 {
 		 for(String pl : teamcyan)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 }
 	 }
 	 
 	 public static void limeMsg(String msg)
 	 {
 		 for(String pl : teamlime)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 }
 	 }
  }
