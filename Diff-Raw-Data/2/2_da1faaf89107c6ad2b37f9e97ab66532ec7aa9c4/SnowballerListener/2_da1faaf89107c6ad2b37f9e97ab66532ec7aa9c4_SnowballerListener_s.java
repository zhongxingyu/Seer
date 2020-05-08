 package co.networkery.uvbeenzaned;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.tag.PlayerReceiveNameTagEvent;
  
  public class SnowballerListener implements Listener
  {
 	 public static String pg = ChatColor.GOLD + "[" + ChatColor.AQUA + "Snowballer" + ChatColor.GOLD + "] " + ChatColor.RESET;
 	 public static ConfigAccessor config;
 	 public static ConfigAccessor scores;
 	 public static boolean gameon = false;
 	 public static boolean timergame = false;
 	 public static List<String> teamcyan = new ArrayList<String>();
 	 public static List<String> teamlime = new ArrayList<String>();
 	 public static List<String> teamcyaninarena = new ArrayList<String>();
 	 public static List<String> teamlimeinarena = new ArrayList<String>();
 	 public static HashMap<String, Integer> hitcnts = new HashMap<String, Integer>();
 	 public static List<String> deadplayers = new ArrayList<String>();
 	 
 	 public SnowballerListener(JavaPlugin jp)
 	 {
 		 config = new ConfigAccessor(jp, "config.yml");
 		 scores = new ConfigAccessor(jp, "scores.yml");
 	 }
 	 
 	 @EventHandler(priority = EventPriority.HIGHEST)
 	 public void onPlayerCommand(PlayerCommandPreprocessEvent e)
 	 {
 		 if(!e.getPlayer().isOp())
 		 {
 			 if(teamcyan.contains(e.getPlayer().getName()) || teamlime.contains(e.getPlayer().getName()))
 			 {
 				 String cmdname = e.getMessage().split(" ")[0].replace("/", "");
 				 if(!cmdname.equalsIgnoreCase("sbr") && !cmdname.equalsIgnoreCase("snowballer"))
 				 {
 					 e.getPlayer().sendMessage((pg + "Leave your team to execute other commands!"));
 					 e.setCancelled(true);
 				 }
 			 } 
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void playerLeave(PlayerQuitEvent event)
 	 {
 		 String pll = event.getPlayer().getName();
 		 Team.Leave(pll, true);
 		 event.getPlayer().setPlayerListName(null);
 	 }
 	 
 	 @EventHandler
 	 public void playerDeath(PlayerDeathEvent event)
 	 {
 		 String pld = event.getEntity().getName();
 		 if(teamcyan.contains(pld) || teamlime.contains(pld))
 		 {
 			 event.getDrops().clear();
 			 deadplayers.add(pld); 
 		 }
 		 Team.Leave(pld, true);
 	 }
 	 
 	 @EventHandler(priority = EventPriority.HIGHEST)
 	 public void playerRespawn(PlayerRespawnEvent event)
 	 {
 		 if(deadplayers.contains(event.getPlayer().getName()))
 		 {
 			 event.setRespawnLocation(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 			 deadplayers.remove(event.getPlayer().getName());
 		 }
 		 else
 		 {
 			 event.setRespawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void playerInvClick(InventoryClickEvent event)
 	 {
 		 if(teamcyan.contains(event.getWhoClicked().getName()) || teamlime.contains(event.getWhoClicked().getName()))
 		 {
 			 if(event.getSlotType() == SlotType.ARMOR)
 			 {
 				 event.setCancelled(true); 
 			 }
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void onSnowballThrow(ProjectileLaunchEvent event)
 	 {
 		 if(gameon)
 		 {
 			 if(event.getEntityType() == EntityType.SNOWBALL)
 			 {
 				 if(event.getEntity().getShooter().getType() == EntityType.PLAYER)
 				 {
 					 if(teamcyan.contains(((Player)event.getEntity().getShooter()).getName()) || teamlime.contains(((Player)event.getEntity().getShooter()).getName()))
 					 {
 						 config.getConfig().set("snowballthrowncount", config.getConfig().getInt("snowballthrowncount") + 1);
 					 }
 				 }
 			 }
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void entityDamage(EntityDamageByEntityEvent event)
 	 {
 		 if(gameon)
 		 {
 			 if(event.getEntity() instanceof Player && event.getDamager() instanceof Player)
 			 {
 				 Player plhit = (Player)event.getEntity();
 				 Player plenemy = (Player)event.getDamager();
 				 if(teamcyaninarena.contains(plhit.getName()) || teamlimeinarena.contains(plhit.getName()))
 				 {
 					 if(teamcyaninarena.contains(plenemy.getName()) || teamlimeinarena.contains(plenemy.getName()))
 					 {
 						 if(!teamcyaninarena.contains(plhit.getName()) || !teamcyaninarena.contains(plenemy.getName()))
 						 {
 							 if(!teamlimeinarena.contains(plhit.getName()) || !teamlimeinarena.contains(plenemy.getName()))
 							 {
 								 event.setCancelled(true);
 							 }
 						 }
 					 }
 				 }
 			 }
 			 if(event.getEntity() instanceof Entity && event.getDamager() instanceof Snowball && !(event.getEntity() instanceof Player))
 			 {
 				 Entity mob = event.getEntity();
 				 Snowball sb = (Snowball)event.getDamager();
 				 Player plenemy = (Player)sb.getShooter();
 				 if(gameon == true)
 				 {
 					 if(teamcyaninarena.contains(plenemy.getName()) || teamlimeinarena.contains(plenemy.getName()))
 					 {
 						 mob.remove();
 						 plenemy.getWorld().createExplosion(mob.getLocation(), 0F);
 						 scores.getConfig().set(plenemy.getName(), scores.getConfig().getInt(plenemy.getName()) + 1);
						 plenemy.sendMessage(pg + ChatColor.GOLD + "+1" + ChatColor.RESET + "bonus point for mob hit!");
 					 }
 				 }
 			 }
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
 									 Location locyplus1 = plhit.getLocation();
 									 locyplus1.setY(locyplus1.getY() + 1);
 									 plhit.getWorld().playEffect(locyplus1, Effect.ENDER_SIGNAL, 0);
 									 plhit.getWorld().playEffect(plhit.getLocation(), Effect.ENDER_SIGNAL, 0);
 									 plhit.setRemoveWhenFarAway(true);
 									 plhit.teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 									 plhit.getInventory().clear();
 									 if(teamcyaninarena.contains(plhit.getName()))
 									 {
 										 teamcyaninarena.remove(plhit.getName());
 										 Rank.giveRank(plhit);
 									 }
 									 if(teamlimeinarena.contains(plhit.getName()))
 									 {
 										 teamlimeinarena.remove(plhit.getName());
 										 Rank.giveRank(plhit);
 									 }
 									 scores.getConfig().set(plhit.getName(), scores.getConfig().getInt(plhit.getName()) - 1);
 									 plhit.sendMessage(pg + ChatColor.GOLD + "-1" + ChatColor.RESET + " point!  Your score is now " + ChatColor.GOLD + String.valueOf(scores.getConfig().getInt(plhit.getName())) + ChatColor.RESET + ".");
 									 if(!hitcnts.containsKey(plenemy.getName()))
 									 {
 										 hitcnts.put(plenemy.getName(), 1);
 									 }
 									 else
 									 {
 										 hitcnts.put(plenemy.getName(), hitcnts.get(plenemy.getName()) + 1);
 									 }
 									 scores.getConfig().set(plenemy.getName(), scores.getConfig().getInt(plenemy.getName()) + 1);
 									 plenemy.sendMessage(pg + ChatColor.GOLD + "+1" + ChatColor.RESET + " point!  Your score is now " + ChatColor.GOLD + String.valueOf(scores.getConfig().getInt(plenemy.getName())) + ChatColor.RESET + ".");
 									 scores.saveConfig();
 									 Chat.sendAllTeamsMsg(pg + teamcyaninarena.size() + " " + ChatColor.AQUA + "CYAN" + ChatColor.RESET + " vs " + teamlimeinarena.size() + " " + ChatColor.GREEN + "LIME");
 									 Chat.sendAllTeamsMsg(pg + Utils.getNamewColor(plenemy) + ChatColor.RED + " snowbrawled " + Utils.getNamewColor(plhit) + ".");
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
 	 
 		@EventHandler
 		 public void onNameTag(PlayerReceiveNameTagEvent event)
 		 {
 			 if(teamcyan.contains(event.getNamedPlayer().getName()))
 			 {
 				 event.setTag(ChatColor.AQUA + event.getNamedPlayer().getName());
 			 }
 			 else
 			 {
 				 if(teamlime.contains(event.getNamedPlayer().getName()))
 				 {
 					 event.setTag(ChatColor.GREEN + event.getNamedPlayer().getName());
 				 }
 				 else
 				 {
 					 event.setTag(event.getNamedPlayer().getName());
 				 }
 			 }
 		 }
 		
 		@EventHandler
 		public void onNewSign(SignChangeEvent event)
 		{
 			if(event.getPlayer().isOp())
 			{
 				if(event.getLine(0).equalsIgnoreCase("[sbr]"))
 				{
 					event.setLine(0, ChatColor.GOLD + "[" + ChatColor.AQUA + "SBR" + ChatColor.GOLD + "]");
 					event.setLine(1, ChatColor.DARK_RED + "(click here)");
 					event.setLine(2, ChatColor.BLUE + "([RANK])");
 					event.setLine(3, ChatColor.GOLD + "(points)");
 				}
 			}
 		}
 		
 		public static ArrayList<String> punchcounts = new ArrayList<String>();
 		
 		@EventHandler
 		public void onSignClick(PlayerInteractEvent event)
 		{
 			if(teamcyan.contains(event.getPlayer().getName()) || teamlime.contains(event.getPlayer().getName()))
 			{
 				if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
 				{
 					if(event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
 					{
 						Sign s = (org.bukkit.block.Sign)event.getClickedBlock().getState();
 						if(s.getLine(0).equalsIgnoreCase(ChatColor.GOLD + "[" + ChatColor.AQUA + "SBR" + ChatColor.GOLD + "]"))
 						{
 							s.setLine(1, ChatColor.DARK_RED + event.getPlayer().getName());
 							s.setLine(2, ChatColor.BLUE + Rank.getRankName(event.getPlayer().getName()));
 							s.setLine(3, ChatColor.GOLD + String.valueOf(SnowballerListener.scores.getConfig().getInt(event.getPlayer().getName())));
 							s.update();
 						}
 					}
 				}
 				if(event.getAction() == Action.LEFT_CLICK_BLOCK)
 				{
 					if(event.getPlayer().isSneaking())
 					{
 						if(!punchcounts.contains(event.getPlayer().getName()))
 						{
 							punchcounts.add(event.getPlayer().getName());
 						}
 						else
 						{
 							punchcounts.remove(event.getPlayer().getName());
 							Chat.getTeamStats(event.getPlayer().getName());
 						}
 					}
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onChat(AsyncPlayerChatEvent event)
 		{
 			ChatColor cc = null;
 			if(teamcyan.contains(event.getPlayer().getName()))
 			 {
 				cc = ChatColor.AQUA;
 				event.setFormat(ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(event.getPlayer().getName()) + ChatColor.RESET + ChatColor.GOLD + "]" + ChatColor.RESET + "<" + cc + event.getPlayer().getName() + ChatColor.RESET + "> " + event.getMessage());
 			 }
 			 if(teamlime.contains(event.getPlayer().getName()))
 			 {
 				 cc = ChatColor.GREEN;
 				 event.setFormat(ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(event.getPlayer().getName()) + ChatColor.RESET + ChatColor.GOLD + "]" + ChatColor.RESET + "<" + cc + event.getPlayer().getName() + ChatColor.RESET + "> " + event.getMessage());
 			 }
 		}
 	 
 
 	 
 	 public static void terminateAll()
 	 {
 		checkTeamsInArena();
 		hitcnts.clear();
 		if(teamcyan.isEmpty() || teamlime.isEmpty())
 		{
 			gameon = false;
 			timergame = false;
 			if(Round.timer != null && Round.timer.isRunning())
 			{
 				Round.timer.stop();
 			}
 			teamcyaninarena.clear();
 			teamlimeinarena.clear();
   			if(teamlime.isEmpty())
   			{
   				Chat.cyanMsg(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
   				Chat.cyanMsg(pg + "Waiting for another player to join....");
   			}
   			if(teamcyan.isEmpty())
   			{
   				Chat.limeMsg(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
   				Chat.limeMsg(pg + "Waiting for another player to join....");
   			}
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
 					 Bukkit.getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					 Bukkit.getPlayer(pl).getInventory().clear();
 				 }
 				 for(String pl : teamcyaninarena)
 				 {
 					 Bukkit.getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					 Bukkit.getPlayer(pl).getInventory().clear();
 				 }
 				 Chat.sendAllTeamsMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 				 teamcyaninarena.clear();
 				 teamlimeinarena.clear();
 				 for(String pl : teamlime)
 				 {
 					 scores.getConfig().set(pl, scores.getConfig().getInt(pl) + config.getConfig().getInt("teampoints") * teamcyan.size());
 					 Bukkit.getPlayer(pl).setRemoveWhenFarAway(true);
 				 }
 				 Chat.sendAllTeamsMsg(pg + ChatColor.GOLD + "+" + String.valueOf(config.getConfig().getInt("teampoints") * teamcyan.size()) + ChatColor.RESET + " points for all of team" + ChatColor.GREEN + " LIME" + ChatColor.RESET + ".");
 				 if(!hitcnts.isEmpty())
 				 {
 					 String hskiller = "";
 					 int hshits = 0;
 					 for(Entry<String, Integer> e : hitcnts.entrySet())
 					 {
 						 if(e.getValue() > hshits)
 						 {
 							 hskiller = e.getKey();
 							 hshits = e.getValue();
 						 }
 					 }
 					 scores.getConfig().set(hskiller, scores.getConfig().getInt(hskiller) + hshits * hshits);
 					 Chat.sendAllTeamsMsg(pg + Utils.getNamewColor(hskiller) + " was awarded " + ChatColor.GOLD + hshits * hshits + ChatColor.RESET + " points for the most player hits!");
 					 hitcnts.clear();
 				 }
 				 scores.saveConfig();
 				 config.saveConfig();
 				 gameon = false;
 			 }
 			 else
 			 {
 				 if(teamlimeinarena.size() == 0)
 				 {
 					 for(String pl : teamcyaninarena)
 					 {
 						 Bukkit.getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 						 Bukkit.getPlayer(pl).getInventory().clear();
 					 }
 					 for(String pl : teamlimeinarena)
 					 {
 						 Bukkit.getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 						 Bukkit.getPlayer(pl).getInventory().clear();
 					 }
 					 Chat.sendAllTeamsMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 					 teamcyaninarena.clear();
 					 teamlimeinarena.clear();
 					 for(String pl : teamcyan)
 					 {
 						 scores.getConfig().set(pl, scores.getConfig().getInt(pl) + config.getConfig().getInt("teampoints") * teamlime.size());
 						 Bukkit.getPlayer(pl).setRemoveWhenFarAway(true);
 					 }
 					 Chat.sendAllTeamsMsg(pg + ChatColor.GOLD + "+" + String.valueOf(config.getConfig().getInt("teampoints") * teamlime.size()) + ChatColor.RESET + " points for all of team" + ChatColor.AQUA + " CYAN" + ChatColor.RESET + ".");
 					 if(!hitcnts.isEmpty())
 					 {
 						 String hskiller = "";
 						 int hshits = 0;
 						 for(Entry<String, Integer> e : hitcnts.entrySet())
 						 {
 							 if(e.getValue() > hshits)
 							 {
 								 hskiller = e.getKey();
 								 hshits = e.getValue();
 							 }
 						 }
 						 scores.getConfig().set(hskiller, scores.getConfig().getInt(hskiller) + hshits * hshits);
 						 Chat.sendAllTeamsMsg(pg + Utils.getNamewColor(hskiller) + " was awarded " + ChatColor.GOLD + hshits * hshits + ChatColor.RESET + " points for the most player hits!");
 						 hitcnts.clear();
 					 }
 					 scores.saveConfig();
 					 config.saveConfig();
 					 gameon = false;
 				 } 
 			 }
 			 if(timergame == true && gameon == false)
 			 {
 				 Round.startIndependentTimerRound();
 			 }
 		 }
 	 }
 	 
 	 public static String checkPlayerLead()
 	 {
 		 String player = null;
 		 int points = 0;
 		 for(Entry<String, Integer> e : hitcnts.entrySet())
 		 {
 			 if(player == null && points == 0)
 			 {
 				 player = e.getKey();
 				 points = e.getValue();
 			 }
 			 else
 			 {
 				 if(e.getValue() > points)
 				 {
 					 player = e.getKey();
 					 points = e.getValue();
 				 }
 			 }
 		 }
 		 return player + "," + String.valueOf(points);
 	 }
 	 
 	 public static void giveSnowballs(Player pl)
 	 {
 		 for(int x = 0; x < 9; x++)
 		 {
 			 pl.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
 		 }
 	 }
  }
