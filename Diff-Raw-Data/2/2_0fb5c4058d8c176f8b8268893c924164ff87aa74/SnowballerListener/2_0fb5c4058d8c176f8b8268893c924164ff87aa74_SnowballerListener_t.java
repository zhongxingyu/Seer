 package co.networkery.uvbeenzaned;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Map.Entry;
 
 import javax.swing.Timer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
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
 	 public configOps cO;
 	 public static boolean gameon = false;
 	 public static boolean timergame = false;
 	 public static Location lobbyspawnlocation = null;
 	 public static HashMap<String, Integer> teamcyan = new HashMap<String, Integer>();
 	 public static HashMap<String, Integer> teamlime = new HashMap<String, Integer>();
 	 public static List<String> teamcyaninarena = new ArrayList<String>();
 	 public static List<String> teamlimeinarena = new ArrayList<String>();
 	 public static HashMap<String, Location> teamcyanarenasides = new HashMap<String, Location>();
 	 public static HashMap<String, Location> teamlimearenasides = new HashMap<String, Location>();
 	 public static boolean startwithoutop = false;
 	 public static int timerdelay = 0;
 	 public static int teampoints = 0;
 	 
 	 public SnowballerListener(JavaPlugin jp)
 	 {
 		 cO = new configOps(jp);
 	 }
 	 
 	 @EventHandler
 	 public void playerLeave(PlayerQuitEvent event)
 	 {
 		 String pll = event.getPlayer().getName();
 		 if(gameon)
 		 {
 				if(teamcyaninarena.contains(pll))
 				{
 					configOps.saveScores();
 					Bukkit.getServer().getPlayer(pll).teleport(lobbyspawnlocation);
 					teamcyaninarena.remove(pll);
 				}
 				if(teamlimeinarena.contains(pll))
 				{
 					configOps.saveScores();
 					Bukkit.getServer().getPlayer(pll).teleport(lobbyspawnlocation);
 					teamlimeinarena.remove(pll);
 				}
 				checkTeamsInArena();
 				if(teamcyan.containsKey(pll))
 				{
 					configOps.saveScores();
 					teamcyan.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getPlayer().teleport(lobbyspawnlocation);
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 				if(teamlime.containsKey(pll))
 				{
 					configOps.saveScores();
 					teamlime.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getPlayer().teleport(lobbyspawnlocation);
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 		 }
 		 else
 		 {
 				if(teamcyaninarena.contains(pll))
 				{
 					configOps.saveScores();
 					Bukkit.getServer().getPlayer(pll).teleport(lobbyspawnlocation);
 					teamcyaninarena.remove(pll);
 				}
 				if(teamlimeinarena.contains(pll))
 				{
 					configOps.saveScores();
 					Bukkit.getServer().getPlayer(pll).teleport(lobbyspawnlocation);
 					teamlimeinarena.remove(pll);
 				}
 				if(teamcyan.containsKey(pll))
 				{
 					configOps.saveScores();
 					teamcyan.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getPlayer().teleport(lobbyspawnlocation);
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
 				if(teamlime.containsKey(pll))
 				{
 					configOps.saveScores();
 					teamlime.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getPlayer().teleport(lobbyspawnlocation);
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 				}
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
 			    	configOps.saveScores();
 			    	Bukkit.getServer().getPlayer(pld).teleport(lobbyspawnlocation);
 			    	teamcyaninarena.remove(pld);
 				}
 				if(teamlimeinarena.contains(pld))
 				{
 					configOps.saveScores();
 					Bukkit.getServer().getPlayer(pld).teleport(lobbyspawnlocation);
 					teamlimeinarena.remove(pld);
 				}
 				checkTeamsInArena();
 			    if(teamcyan.containsKey(pld))
 			    {
 			    	configOps.saveScores();
 			    	teamcyan.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 				}
 				if(teamlime.containsKey(pld))
 				{
 					configOps.saveScores();
 					teamlime.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 				}
 		 }
 		 else
 		 {
 			    if(teamcyaninarena.contains(pld))
 			    {
 			    	configOps.saveScores();
 			    	Bukkit.getServer().getPlayer(pld).teleport(lobbyspawnlocation);
 			    	teamcyaninarena.remove(pld);
 				}
 				if(teamlimeinarena.contains(pld))
 				{
 					configOps.saveScores();
 					Bukkit.getServer().getPlayer(pld).teleport(lobbyspawnlocation);
 					teamlimeinarena.remove(pld);
 				}
 			    if(teamcyan.containsKey(pld))
 			    {
 			    	configOps.saveScores();
 			    	teamcyan.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamcyan.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 				}
 				if(teamlime.containsKey(pld))
 				{
 					configOps.saveScores();
 					teamlime.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 				}
 		 }
 	 }
 	 
 	 @SuppressWarnings("deprecation")
 	@EventHandler
 	 public void playerDropItem(PlayerDropItemEvent event)
 	 {
 		 if(gameon)
 		 {
 			 for(Entry<String, Integer> entry : teamcyan.entrySet())
 			 {
 				 if(entry.getKey() == event.getPlayer().getName())
 				 {
 					 event.setCancelled(true);
 					 event.getPlayer().updateInventory();
 				 }
 			 }
 			 for(Entry<String, Integer> entry : teamlime.entrySet())
 			 {
 				 if(entry.getKey() == event.getPlayer().getName())
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
 									 plhit.teleport(lobbyspawnlocation);
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
 									 for(Entry<String, Integer> entry : teamcyan.entrySet())
 									 {
 										 if(entry.getKey() == plenemy.getName())
 										 {
 											 entry.setValue(entry.getValue() + 1);
 											 plenemy.sendMessage(pg + "+1 point!  Your score is now " + teamcyan.get(plenemy.getName()) + ".");
 											 configOps.saveScores();
 										 }
 										 if(entry.getKey() == plhit.getName())
 										 {
 											 if(entry.getValue() != 0)
 											 {
 												 entry.setValue(entry.getValue() - 1);
 												 plhit.sendMessage(pg + "-1 point!  Your score is now " + teamcyan.get(plhit.getName()) + ".");
 												 configOps.saveScores();
 											 }
 										 }
 									 }
 									 for(Entry<String, Integer> entry : teamlime.entrySet())
 									 {
 										 if(entry.getKey() == plenemy.getName())
 										 {
 											 entry.setValue(entry.getValue() + 1);
 											 plenemy.sendMessage(pg + "+1 point!  Your score is now " + teamlime.get(plenemy.getName()) + ".");
 											 configOps.saveScores();
 										 }
 										 if(entry.getKey() == plhit.getName())
 										 {
 											 if(entry.getValue() != 0)
 											 {
 												 entry.setValue(entry.getValue() - 1);
 												 plhit.sendMessage(pg + "-1 point!  Your score is now " + teamlime.get(plhit.getName()) + ".");
 												 configOps.saveScores();
 											 }
 										 }
 									 }
 									 sendAllTeamsMsg(pg + "There are now " + teamcyaninarena.size() + " players on team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET + " left in the arena!");
 									 sendAllTeamsMsg(pg + "There are now " + teamlimeinarena.size() + " players on team " + ChatColor.GREEN + "LIME" + ChatColor.RESET + " left in the arena!");
 									 sendAllTeamsMsg(pg + ChatColor.RED + plhit.getName() + ChatColor.BLUE + " was hit by " + ChatColor.GREEN + plenemy.getName() + ".");
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
 				 timer = new Timer(timerdelay, taskPerformer);
 				 timer.setRepeats(false);
 			     timer.start();
 			     sendAllTeamsMsg(pg + "Next round starts in " + Integer.toString(timerdelay / 1000) + " seconds!");
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
 		int mapnum = r.nextInt(teamcyanarenasides.entrySet().size());
 		int i = 0;
 		for(Entry<String, Location> entry : teamcyanarenasides.entrySet())
 		{
 			if(mapnum == i)
 			{
 				for(Entry<String, Integer> pl : teamcyan.entrySet())
 				{
 					if(Bukkit.getServer().getPlayer(pl.getKey()).getGameMode() == GameMode.CREATIVE)
 					{
 						Bukkit.getServer().getPlayer(pl.getKey()).setGameMode(GameMode.SURVIVAL);
 					}
 					Bukkit.getServer().getPlayer(pl.getKey()).getInventory().clear();
 					giveSnowballs(Bukkit.getServer().getPlayer(pl.getKey()));
 					teamcyaninarena.add(pl.getKey());
 					Bukkit.getServer().getPlayer(pl.getKey()).teleport(entry.getValue());
 				}
 				for(Entry<String, Integer> pl : teamlime.entrySet())
 				{
 					if(Bukkit.getServer().getPlayer(pl.getKey()).getGameMode() == GameMode.CREATIVE)
 					{
 						Bukkit.getServer().getPlayer(pl.getKey()).setGameMode(GameMode.SURVIVAL);
 					}
 					Bukkit.getServer().getPlayer(pl.getKey()).getInventory().clear();
 					giveSnowballs(Bukkit.getServer().getPlayer(pl.getKey()));
 					teamlimeinarena.add(pl.getKey());
 					Bukkit.getServer().getPlayer(pl.getKey()).teleport(teamlimearenasides.get(entry.getKey()));
 				}
 			}
 			i++;
 		}
 	 }
 	 
 	 public static void checkTeamsInArena()
 	 {
 		 if(teamcyaninarena.size() == 0)
 		 {
 			 for(String pl : teamlimeinarena)
 			 {
 				 Bukkit.getServer().getPlayer(pl).teleport(lobbyspawnlocation);
 				 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 			 }
 			 for(String pl : teamcyaninarena)
 			 {
 				 Bukkit.getServer().getPlayer(pl).teleport(lobbyspawnlocation);
 				 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 			 }
 			 limeMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 			 cyanMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 			 teamcyaninarena.clear();
 			 teamlimeinarena.clear();
 			 for(Entry<String, Integer> entry : teamlime.entrySet())
 			 {
 				 entry.setValue(entry.getValue() + teampoints);
 			 }
 			 limeMsg(pg + "+4 points for all of team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + ".");
 			 cyanMsg(pg + "+4 points for all of team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + ".");
 			 configOps.saveScores();
 			 gameon = false;
 		 }
 		 else
 		 {
 			 if(teamlimeinarena.size() == 0)
 			 {
 				 for(String pl : teamcyaninarena)
 				 {
 					 Bukkit.getServer().getPlayer(pl).teleport(lobbyspawnlocation);
 					 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 				 }
 				 for(String pl : teamlimeinarena)
 				 {
 					 Bukkit.getServer().getPlayer(pl).teleport(lobbyspawnlocation);
 					 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 				 }
 				 cyanMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 				 limeMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 				 teamcyaninarena.clear();
 				 teamlimeinarena.clear();
 				 for(Entry<String, Integer> entry : teamcyan.entrySet())
 				 {
 					 entry.setValue(entry.getValue() + teampoints);
 					 //Bukkit.getServer().getPlayer(entry.getKey()).sendMessage(pg + "Your score is now " + entry.getValue() + ".");
 				 }
 				 limeMsg(pg + "+4 points for all of team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + ".");
 				 cyanMsg(pg + "+4 points for all of team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + ".");
 				 configOps.saveScores();
 				 gameon = false;
 			 }
 		 }
 		 if(timergame == true && gameon == false)
 		 {
 			 startIndependentTimerRound();
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
 		 for(Entry<String, Integer> pl : teamlime.entrySet())
 		 {
 			 Bukkit.getServer().getPlayer(pl.getKey()).sendMessage(msg);
 		 }
 		 for(Entry<String, Integer> pl : teamcyan.entrySet())
 		 {
 			 Bukkit.getServer().getPlayer(pl.getKey()).sendMessage(msg);
 		 } 
 	 }
 	 
 	 public static void cyanMsg(String msg)
 	 {
 		 for(Entry<String, Integer> pl : teamcyan.entrySet())
 		 {
 			 Bukkit.getServer().getPlayer(pl.getKey()).sendMessage(msg);
 		 }
 	 }
 	 
 	 public static void limeMsg(String msg)
 	 {
 		 for(Entry<String, Integer> pl : teamlime.entrySet())
 		 {
 			 Bukkit.getServer().getPlayer(pl.getKey()).sendMessage(msg);
 		 }
 	 }
  }
