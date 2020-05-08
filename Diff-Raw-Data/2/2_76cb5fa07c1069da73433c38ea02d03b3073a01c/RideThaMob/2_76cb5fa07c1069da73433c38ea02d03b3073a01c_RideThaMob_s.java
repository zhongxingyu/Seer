 package de.MiniDigger.RideThaMob;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class RideThaMob extends JavaPlugin implements Listener {
 	private String prefix;
 	private String cprefix;
 	private String lang;
 	private double defaultspeed;
 	private double maxspeed;
 	private ArrayList<String> speed;
 	private ArrayList<String> sneak;
 	private ArrayList<String> control;
 	private ArrayList<String> player;
 
 	public void onEnable() {
 		saveDefaultConfig();
 
 		getServer().getPluginManager().registerEvents(this, this);
 
 		this.prefix = ("[" + getDescription().getName() + "] ");
 		this.cprefix = (ChatColor.AQUA + "[" + ChatColor.RED
 				+ getDescription().getName() + ChatColor.AQUA + "] " + ChatColor.RESET);
 
 		loadConfig();
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			getLogger().warning("Failed to load the Metrics :(");
 		}
 
 		this.speed = new ArrayList<String>();
 		this.sneak = new ArrayList<String>();
 		this.control = new ArrayList<String>();
 		this.player = new ArrayList<String>();
 	}
 
 	public void loadConfig() {
 		this.lang = getConfig().getString("lang");
 		this.defaultspeed = getConfig().getDouble("defaultspeed");
 		this.maxspeed = getConfig().getDouble("maxspeed");
 	}
 
 	public void onDisable() {
 		saveConfig();
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		if (((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK))
 				&& (e.getPlayer().getVehicle() != null)
 				&& (e.getPlayer().getVehicle().getType() == EntityType.ENDER_DRAGON)) {
 			System.out.println("1");
 			EnderDragon dragon = (EnderDragon) e.getPlayer().getVehicle();
 			dragon.launchProjectile(Fireball.class);
 		}
 	}
 
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent e) {
 		if ((e.getEntity().getPassenger() != null)
 				&& ((e.getEntity().getPassenger() instanceof Player))) {
 			Player p = (Player) e.getEntity().getPassenger();
 			if (p.hasPermission("ridethamob.god")) {
 				e.setDamage(0.0);
 				e.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerSneak(PlayerToggleSneakEvent e) {
 		if (this.sneak.contains(e.getPlayer().getName()))
 			this.sneak.remove(e.getPlayer().getName());
 		else
 			this.sneak.add(e.getPlayer().getName());
 	}
 
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent e) {
 		Player p = e.getPlayer();
 		double d = this.defaultspeed;
 		if (this.speed.contains(e.getPlayer().getName())) {
 			d = this.maxspeed;
 		}
 		if ((p.getVehicle() != null) && (this.sneak.contains(p.getName()))
 				&& (this.control.contains(p.getName()))
 				&& (this.player.contains(p.getName()))) {
 			Entity v = p.getVehicle();
 			Vector f = p.getEyeLocation().getDirection().multiply(d);
 			v.setVelocity(f);
 			v.teleport(new Location(v.getWorld(), v.getLocation().getX(), v
 					.getLocation().getY(), v.getLocation().getZ(), p
 					.getEyeLocation().getPitch(), p.getEyeLocation().getYaw()));
 			p.setFallDistance(0.0F);
 			v.setFallDistance(0.0F);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("ridethamob")) {
 			if ((sender instanceof Player)) {
 				Player p = (Player) sender;
 				if (args.length != 0) {
 					if (args.length == 1) {
 						if (args[0].equalsIgnoreCase("speed")) {
 							if (!p.hasPermission("ridethamob.speed")) {
 								if (this.lang.equalsIgnoreCase("de"))
 									p.sendMessage(this.cprefix
 											+ "Du hast keinen Zufriff auf den Speed Modus!");
 								else {
 									p.sendMessage(this.cprefix
 											+ "You are not allowed to use the speed mode!");
 								}
 								return true;
 							}
 							if (!this.speed.contains(p.getName())) {
 								if (this.lang.equalsIgnoreCase("de"))
 									p.sendMessage(this.cprefix
 											+ "Speed Modus aktiviert");
 								else {
 									p.sendMessage(this.cprefix
 											+ "Speed Mode activated");
 								}
 								this.speed.add(p.getName());
 							} else {
 								if (this.lang.equalsIgnoreCase("de"))
 									p.sendMessage(this.cprefix
 											+ "Speed Modus deaktiviert");
 								else {
 									p.sendMessage(this.cprefix
 											+ "Speed Mode deactivated");
 								}
 								this.speed.remove(p.getName());
 							}
 
 						} else if (args[0].equalsIgnoreCase("reload")) {
 							if (p.hasPermission("ridethamob.reload")) {
 								reloadConfig();
 								loadConfig();
 								if (this.lang.equalsIgnoreCase("de"))
 									p.sendMessage(this.cprefix
 											+ "Config neu geladen");
 								else
 									p.sendMessage(this.cprefix
 											+ "Config redloaded");
 							} else {
 								if (this.lang.equalsIgnoreCase("de"))
 									p.sendMessage(this.cprefix
 											+ "Du hast keinen Zufriff auf diesen Befehl!");
 								else {
 									p.sendMessage(this.cprefix
 											+ "You are not allowed to use this command!");
 								}
 								return true;
 							}
 
 						} else if (args[0].equalsIgnoreCase("control")) {
 							if (p.hasPermission("ridethamob.control")) {
 								if (!this.control.contains(p.getName())) {
 									if (this.lang.equalsIgnoreCase("de"))
 										p.sendMessage(this.cprefix
 												+ "Control Modus aktiviert");
 									else {
 										p.sendMessage(this.cprefix
 												+ "Control Mode activated");
 									}
 									this.control.add(p.getName());
 								} else {
 									if (this.lang.equalsIgnoreCase("de"))
 										p.sendMessage(this.cprefix
 												+ "Control Modus deaktiviert");
 									else {
 										p.sendMessage(this.cprefix
 												+ "Control Mode deactivated");
 									}
 									this.control.remove(p.getName());
 								}
 							} else if (this.lang.equalsIgnoreCase("de"))
 								p.sendMessage(this.cprefix
 										+ "Du hast keinen Zufriff auf den Control Modus!");
 							else {
 								p.sendMessage(this.cprefix
 										+ "You are not allowed to use the control mode!");
 							}
 
 						} else if (this.lang.equalsIgnoreCase("de")) {
 							p.sendMessage(this.cprefix
 									+ "Befehl nicht gefunden!");
 						} else {
							p.sendMessage(this.cprefix + "Commadn not found!");
 						}
 
 					} else if (this.lang.equalsIgnoreCase("de"))
 						p.sendMessage(this.cprefix + "Zu viele Argumente!");
 					else {
 						p.sendMessage(this.cprefix + "Too much arguments!");
 					}
 
 					return true;
 				}
 				if (p.getVehicle() == null) {
 					this.player.add(p.getName());
 					List<?> l = new ArrayList<Object>();
 					for (int i = 1; i < 11; i++) {
 						l = p.getNearbyEntities(i, i, i);
 						if (!l.isEmpty()) {
 							Entity e = (Entity) l.get(0);
 							if (e.getType() == EntityType.BAT) {
 								if (p.hasPermission("ridethamob.mob.bat"))
 									break;
 							} else if (e.getType() == EntityType.BLAZE) {
 								if (p.hasPermission("ridethamob.mob.blaze"))
 									break;
 							} else if (e.getType() == EntityType.CAVE_SPIDER) {
 								if (p.hasPermission("ridethamob.mob.cavespider"))
 									break;
 							} else if (e.getType() == EntityType.CHICKEN) {
 								if (p.hasPermission("ridethamob.mob.chicken"))
 									break;
 							} else if (e.getType() == EntityType.COW) {
 								if (p.hasPermission("ridethamob.mob.cow"))
 									break;
 							} else if (e.getType() == EntityType.CREEPER) {
 								if (p.hasPermission("ridethamob.mob.creeper"))
 									break;
 							} else if (e.getType() == EntityType.ENDER_DRAGON) {
 								if (p.hasPermission("ridethamob.mob.dragon"))
 									break;
 							} else if (e.getType() == EntityType.ENDERMAN) {
 								if (p.hasPermission("ridethamob.mob.enderman"))
 									break;
 							} else if (e.getType() == EntityType.GHAST) {
 								if (p.hasPermission("ridethamob.mob.ghast"))
 									break;
 							} else if (e.getType() == EntityType.GIANT) {
 								if (p.hasPermission("ridethamob.mob.giant"))
 									break;
 							} else if (e.getType() == EntityType.IRON_GOLEM) {
 								if (p.hasPermission("ridethamob.mob.irongolem"))
 									break;
 							} else if (e.getType() == EntityType.MAGMA_CUBE) {
 								if (p.hasPermission("ridethamob.mob.magmacube"))
 									break;
 							} else if (e.getType() == EntityType.MUSHROOM_COW) {
 								if (p.hasPermission("ridethamob.mob.mushroomcow"))
 									break;
 							} else if (e.getType() == EntityType.OCELOT) {
 								if (p.hasPermission("ridethamob.mob.ocelot"))
 									break;
 							} else if (e.getType() == EntityType.PIG) {
 								if (p.hasPermission("ridethamob.mob.pig"))
 									break;
 							} else if (e.getType() == EntityType.PIG_ZOMBIE) {
 								if (p.hasPermission("ridethamob.mob.pigzombie"))
 									break;
 							} else {
 								if (e.getType() == EntityType.PLAYER)
 									break;
 								if (e.getType() == EntityType.SHEEP ? !p
 										.hasPermission("ridethamob.mob.sheep")
 										: e.getType() == EntityType.SILVERFISH ? !p
 												.hasPermission("ridethamob.mob.silverfish")
 												: e.getType() == EntityType.SKELETON ? !p
 														.hasPermission("ridethamob.mob.skeleton")
 														: e.getType() == EntityType.SLIME ? !p
 																.hasPermission("ridethamob.mob.slime")
 																: e.getType() == EntityType.SNOWMAN ? !p
 																		.hasPermission("ridethamob.mob.snowman")
 																		: e.getType() == EntityType.SPIDER ? !p
 																				.hasPermission("ridethamob.mob.spider")
 																				: e.getType() == EntityType.SQUID ? !p
 																						.hasPermission("ridethamob.mob.squid")
 																						: e.getType() == EntityType.VILLAGER ? !p
 																								.hasPermission("ridethamob.mob.villager")
 																								: e.getType() == EntityType.WITCH ? !p
 																										.hasPermission("ridethamob.mob.witch")
 																										: e.getType() == EntityType.WITHER ? !p
 																												.hasPermission("ridethamob.mob.wither")
 																												: (e.getType() == EntityType.ZOMBIE)
 																														&& (p.hasPermission("ridethamob.mob.zombie"))) {
 									break;
 								}
 							}
 							l.clear();
 						}
 					}
 
 					if (l.isEmpty()) {
 						if (this.lang.equalsIgnoreCase("de"))
 							p.sendMessage(this.cprefix
 									+ "Es ist kein Reittier im Umkreis von 10 Blcken das du reiten darfst!");
 						else
 							p.sendMessage(this.cprefix
 									+ "There is not mob in a radius of 10 blocks that you allowed to ride!");
 					} else {
 						Entity e = (Entity) l.get(0);
 						if (e.getType() == EntityType.ENDER_DRAGON) {
 							EnderDragon dr = (EnderDragon) e;
 							dr.setPassenger(p);
 							if (this.lang.equalsIgnoreCase("de"))
 								p.sendMessage(this.cprefix
 										+ "Du reitest jetzt einen Enderdrachen!");
 							else {
 								p.sendMessage(this.cprefix
 										+ "You are now riding a EnderDragon!");
 							}
 							return true;
 						}
 						if (e.getType() == EntityType.GIANT) {
 							Giant g = (Giant) e;
 							g.setPassenger(p);
 							if (this.lang.equalsIgnoreCase("de"))
 								p.sendMessage(this.cprefix
 										+ "Du reitest jetzt einen Giganten!");
 							else {
 								p.sendMessage(this.cprefix
 										+ "You are now riding a Giant!");
 							}
 						}
 						if (e.getType() == EntityType.PLAYER) {
 							Player o = (Player) e;
 							if ((p.hasPermission("ridethamob.player.*"))
 									|| (p.hasPermission("ridethamob.player."
 											+ p.getName()))) {
 								o.setPassenger(p);
 
 								if (this.lang.equalsIgnoreCase("de"))
 									p.sendMessage(this.cprefix
 											+ "Du reitest jetzt "
 											+ o.getDisplayName());
 								else {
 									p.sendMessage(this.cprefix
 											+ "You are now riding "
 											+ o.getDisplayName() + "!");
 								}
 								return true;
 							}
 							if (this.lang.equalsIgnoreCase("de"))
 								p.sendMessage(this.cprefix + "Du darfst "
 										+ o.getDisplayName() + " nicht reiten!");
 							else {
 								p.sendMessage(this.cprefix
 										+ "You are not allowed to ride "
 										+ o.getDisplayName() + "!");
 							}
 						}
 
 						e.setPassenger(p);
 
 						if (this.lang.equalsIgnoreCase("de"))
 							p.sendMessage(this.cprefix
 									+ "Du reitest jetzt ein(e)" + e.getType());
 						else
 							p.sendMessage(this.cprefix
 									+ "You are now riding a " + e.getType());
 					}
 				} else {
 					this.player.remove(p.getName());
 					p.getVehicle().eject();
 					if (this.lang.equalsIgnoreCase("de"))
 						p.sendMessage(this.cprefix
 								+ "Du kann nun wieder alleine gehen :D");
 					else
 						p.sendMessage(this.cprefix
 								+ "Now u can walk on your own feeds again");
 				}
 			} else {
 				sender.sendMessage(this.prefix + "---------RideThaMob---------");
 
 				sender.sendMessage(this.prefix + "Version "
 						+ getDescription().getVersion() + " by "
 						+ (String) getDescription().getAuthors().get(0));
 
 				sender.sendMessage(this.prefix + "----------------------------");
 			}
 		}
 		return true;
 	}
 }
