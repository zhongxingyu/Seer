 package co.networkery.uvbeenzaned;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.Effect;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
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
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 
 
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
 	public static HashMap<String, String> deadplayers = new HashMap<String, String>();
 	public static ScoreboardManager manager = Bukkit.getScoreboardManager();
 	public static Scoreboard board = manager.getNewScoreboard();
 	public static org.bukkit.scoreboard.Team teamcyanboard = board.registerNewTeam("CYAN");
 	public static org.bukkit.scoreboard.Team teamlimeboard = board.registerNewTeam("LIME");
 	public static Objective objective;
 
 	public SnowballerListener(JavaPlugin jp)
 	{
 		config = new ConfigAccessor(jp, "config.yml");
 		scores = new ConfigAccessor(jp, "scores.yml");
 		objective = board.registerNewObjective("score", "dummy");
 		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 		objective.setDisplayName(ChatColor.AQUA + "==== " + ChatColor.GOLD + "Score" + ChatColor.AQUA + " ====");
 		teamcyanboard.setDisplayName("CYAN");
 		teamcyanboard.setPrefix(ChatColor.GOLD + "[" + ChatColor.AQUA + "C" + ChatColor.GOLD + "]" + ChatColor.RESET);
 		teamlimeboard.setDisplayName("LIME");
 		teamlimeboard.setPrefix(ChatColor.GOLD + "[" + ChatColor.GREEN + "L" + ChatColor.GOLD + "]" + ChatColor.RESET);
 		
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
 	}
 
 	@EventHandler
 	public void playerDeath(PlayerDeathEvent event)
 	{
 		String pld = event.getEntity().getName();
 		if(teamcyan.contains(pld))
 		{
 			event.getDrops().clear();
 			deadplayers.put(pld, "cyan");
 		}
 		else
 		{
 			if(teamlime.contains(pld))
 			{
 				event.getDrops().clear();
 				deadplayers.put(pld, "lime");
 			}
 		}
 		Team.Leave(pld, true);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void playerRespawn(PlayerRespawnEvent event)
 	{
 		if(deadplayers.containsKey(event.getPlayer().getName()))
 		{
 			event.setRespawnLocation(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 			Team.Join(event.getPlayer(), deadplayers.get(event.getPlayer().getName()).toString(), true);
 			deadplayers.remove(event.getPlayer().getName());
 		}
 		else
 		{
 			event.setRespawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		Player p = event.getPlayer();
 		if((event.getCause() != TeleportCause.PLUGIN) && (teamcyan.contains(p.getName()) || teamlime.contains(p.getName())) && (p.getWorld().getName() != LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")).getWorld().getName()))
 		{
 			Team.Leave(p.getName(), true);
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
 				if(teamcyaninarena.contains(plenemy.getName()) || teamlimeinarena.contains(plenemy.getName()))
 				{
 					mob.remove();
 					Firework fw = mob.getWorld().spawn(mob.getLocation(), Firework.class);
 					FireworkMeta fwm = fw.getFireworkMeta();
 					FireworkEffect effect = FireworkEffect.builder().withColor(Color.AQUA).with(Type.BALL_LARGE).build();
 					if(teamcyaninarena.contains(plenemy.getName()))
 					{
 						effect = FireworkEffect.builder().withColor(Color.AQUA).with(Type.BALL_LARGE).withFlicker().withTrail().build();
 					}
 					if(teamlimeinarena.contains(plenemy.getName()))
 					{
 						effect = FireworkEffect.builder().withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().withTrail().build();
 					}
 					fwm.addEffects(effect);
					fwm.setPower(10);
 					fw.setFireworkMeta(fwm);
 					scores.getConfig().set(plenemy.getName(), scores.getConfig().getInt(plenemy.getName()) + 1);
 					Score hscore = objective.getScore(plenemy);
 					hscore.setScore(scores.getConfig().getInt(plenemy.getName()));
 					plenemy.sendMessage(pg + ChatColor.GOLD + "+1" + ChatColor.RESET + " bonus point for mob hit!");
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
 									Score hscore = objective.getScore(plhit);
 									hscore.setScore(scores.getConfig().getInt(plhit.getName()));
 									plhit.sendMessage(pg + ChatColor.GOLD + "-1" + ChatColor.RESET + " point!");
 									plhit.setScoreboard(board);
 									if(!hitcnts.containsKey(plenemy.getName()))
 									{
 										hitcnts.put(plenemy.getName(), 1);
 									}
 									else
 									{
 										hitcnts.put(plenemy.getName(), hitcnts.get(plenemy.getName()) + 1);
 									}
 									scores.getConfig().set(plenemy.getName(), scores.getConfig().getInt(plenemy.getName()) + 1);
 									Score escore = objective.getScore(plenemy);
 									escore.setScore(scores.getConfig().getInt(plenemy.getName()));
 									plenemy.sendMessage(pg + ChatColor.GOLD + "+1" + ChatColor.RESET + " point!");
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
 		if(teamcyan.isEmpty() || teamlime.isEmpty())
 		{
 			hitcnts.clear();
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
 					Score score = objective.getScore(Bukkit.getPlayer(pl));
 					score.setScore(scores.getConfig().getInt(pl));
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
 					Score score = objective.getScore(Bukkit.getPlayer(hskiller));
 					score.setScore(scores.getConfig().getInt(hskiller));
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
 						Score score = objective.getScore(Bukkit.getPlayer(pl));
 						score.setScore(scores.getConfig().getInt(Bukkit.getPlayer(pl).getName()));
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
 						Score score = objective.getScore(Bukkit.getPlayer(hskiller));
 						score.setScore(scores.getConfig().getInt(hskiller));
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
 
 	public static void giveSnowballs(Player pl)
 	{
 		for(int x = 0; x < 9; x++)
 		{
 			pl.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
 		}
 	}
 }
