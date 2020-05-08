 package net.milkycraft.em.config;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.milkycraft.em.EntityManager;
 
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.potion.Potion;
 
 public class WorldConfiguration extends ConfigLoader {
 
 	private final String world;
 	private String confRev;
 	private final String localRev = "1.4";
 	private boolean[] b = new boolean[20];
 	private long[] l = new long[2];
 	private Set<Material> usageBlock = new HashSet<Material>();
 	private Set<Material> disBlock = new HashSet<Material>();
 	private Set<EntityType> disEggs = new HashSet<EntityType>();
 	private Set<EntityType> disMobs = new HashSet<EntityType>();
 	private Set<SpawnReason> disReasons = new HashSet<SpawnReason>();
 	private Set<Integer> dispots = new HashSet<Integer>();
 	private Set<Integer> disDpots = new HashSet<Integer>();
 
 	public WorldConfiguration(EntityManager plugin, String world) {
 		super(plugin, world + ".yml");
 		super.saveIfNotExist();
 		this.world = world;
 		super.load();
 	}
 
 	@Override
 	protected void loadKeys() {
 		confRev = config.getString("Settings.Config_Revision", "0.1");
 		performUpdate(confRev);
 		b[0] = config.getBoolean("Settings.Admin-Alerts", true);
 		b[1] = config.getBoolean("Settings.Player-Alerts", true);
 		b[2] = config.getBoolean("Settings.Logging", true);
 		b[3] = config.getBoolean("Disable.Weather.Rain", false);
 		b[4] = config.getBoolean("Disable.Weather.Thunder", false);
 		b[5] = config.getBoolean("Disable.Weather.Lightning", false);
 		b[6] = config.getBoolean("Disable.Interaction.PVP", false);
 		b[7] = config.getBoolean("Disable.Interaction.Fishing", false);
 		b[8] = config.getBoolean("Disable.Interaction.Shooting", false);
 		b[9] = config.getBoolean("Disable.Interaction.Enchanting", false);
 		b[10] = config.getBoolean("Disable.Interaction.Fireworks", false);
 		b[11] = config.getBoolean("Disable.Interaction.Trading", false);
 		b[12] = config.getBoolean("Disable.Other.Monster_Spawner_Exp", false);
 		b[13] = config.getBoolean("Disable.Other.Monster_Spawner_Drops", false);
 		b[14] = config.getBoolean("TimeManager.Enabled", false);
 		b[15] = config.getBoolean("DeathManager.Player.Keep_Exp", false);
 		b[16] = config.getBoolean("DeathManager.Entity.Drop_Exp", true);
 		b[17] = config.getBoolean("DeathManager.Entity.Drop_Items", true);
 		l[0] = config.getLong("TimeManager.Target_Time", 12000L);
 		l[1] = config.getLong("TimeManager.Set_Every", 100L);
		if(loadLists()) {
			plugin.getLogger().severe("Configuration has invalid values in the lists, please fix them");
		}
 	}
 
	private boolean loadLists() {
 		final EntityManager em = super.plugin;
		boolean error = false;
 		for (String s : config.getStringList("Disable.Usage.Blocked_Items")) {
 			try {
 				usageBlock.add(Material.valueOf(s.toUpperCase()));
 			} catch (Exception ex) {
 				if (s.toLowerCase().startsWith("potion")) {
 					String[] args = s.split(":");
 					Integer id = Integer.parseInt(args[1]);
 					Potion p = ConfigHelper.fromDamage(id);
 					this.dispots.add(p.getNameId());
 					if (!usageBlock.contains(Material.POTION)) {
 						usageBlock.add(Material.POTION);
 					}
 				} else {
					error = true;
 					em.getLogger().severe("Invalid value: " + s);
 					em.getLogger().severe("Reference: http://goo.gl/f1Nmb");
 				}
 			}
 		}
 		for (String s : config
 				.getStringList("Disable.Dispensing.Blocked_Items")) {
 			try {
 				disBlock.add(Material.valueOf(s.toUpperCase()));
 			} catch (Exception ex) {
 				if (s.toLowerCase().startsWith("potion")) {
 					String[] args = s.split(":");
 					Integer id = Integer.parseInt(args[1]);
 					Potion p = ConfigHelper.fromDamage(id);
 					this.disDpots.add(p.getNameId());
 					if (!disBlock.contains(Material.POTION)) {
 						disBlock.add(Material.POTION);
 					}
 				} else {
					error = true;
 					em.getLogger().severe("Invalid value: " + s);
 					em.getLogger().severe("Reference: http://goo.gl/f1Nmb");
 				}
 			}
 		}
 		for (String s : config.getStringList("EggManager.Disabled_Eggs")) {
 			try {
 				disEggs.add(EntityType.valueOf(s.toUpperCase()));
 			} catch (Exception ex) {
				error = true;
 				em.getLogger().severe("Invalid value: " + s);
 				em.getLogger().severe("Reference: http://goo.gl/E7mVB");
 			}
 		}
 		for (String s : config.getStringList("SpawnManager.Disallowed_Mobs")) {
 			try {
 				disMobs.add(EntityType.valueOf(s.toUpperCase()));
 			} catch (Exception ex) {
				error = true;
 				em.getLogger().severe("Invalid value: " + s);
 				em.getLogger().severe("Reference: http://goo.gl/E7mVB");
 			}
 		}
 		for (String s : config.getStringList("SpawnManager.Disallowed_Reasons")) {
 			try {
 				disReasons.add(SpawnReason.valueOf(s.toUpperCase()));
 			} catch (Exception ex) {
				error = true;
 				em.getLogger().severe("Invalid value: " + s);
 				em.getLogger().severe("Reference: http://goo.gl/a4XRB");
 			}
 		}
		return error;
 	}
 
 	public void performUpdate(String revision) {
 		Logger lg = plugin.getLogger();
 		if (this.localRev.equals(revision)) {
 			return;
 		}
 		if (revision.equals("0.1")) {
 			super.set("Settings.Config_Revision", this.localRev);
 			super.set("Disable.Usage.Potions", false);
 			super.set("Disable.Usage.Splash_Potions", false);
 			super.set("Disable.Interaction.Trading", false);
 			lg.info("Successfully updated " + fileName + " to 1.0");
 		} else if (revision.equals("1.0")) {
 			super.set("Settings.Config_Revision", this.localRev);
 			List<String> list = new ArrayList<String>();
 			list.add(SpawnReason.LIGHTNING.toString().toLowerCase());
 			super.set("SpawnManager.Disallowed_Reasons", list);
 			lg.info("Successfully updated " + fileName + " to 1.1");
 		} else if (revision.equals("1.1")) {
 			super.set("Settings.Config_Revision", this.localRev);
 			super.set("DeathManager.Keep_Exp", false);
 			lg.info("Successfully updated " + fileName + " to 1.2");
 		} else if (revision.equals("1.2")) {
 			super.set("Settings.Config_Revision", this.localRev);
 			super.set("DeathManager.Keep_Exp", null);
 			super.set("DeathManager.Player.Keep_Exp", false);
 			super.set("DeathManager.Entity.Drop_Exp", true);
 			super.set("DeathManager.Entity.Drop_Items", true);
 			lg.info("Successfully updated " + fileName + " to 1.3");
 		} else if (revision.equals("1.3")) {
 			super.set("Settings.Config_Revision", this.localRev);
 			super.set("Usage.Potions", null);
 			super.set("Usage.Splash_Potions", null);
 			List<String> list = config.getStringList("Usage.Blocked_Items");
 			list.add("Potion:16394");
 			super.set("Disable.Usage.Blocked_Items", list);
 			lg.info("Successfully updated " + fileName + " to 1.4");
 		} else if (revision.equals("1.4")) {
 			/*TODO: Add new config options*/
 			return;
 		} else if (revision.equals("1.5")) {			
 			return;
 		} else {
 			lg.warning("Could not update config, mismatched config revision: Local: "
 					+ localRev + " File: " + revision);
 		}
 		this.reload();
 	}
 
 	public void reload() {
 		this.disBlock.clear();
 		this.disEggs.clear();
 		this.disMobs.clear();
 		this.disReasons.clear();
 		this.usageBlock.clear();
 		super.rereadFromDisk();
 		super.load();
 	}
 
 	public String getWorld() {
 		return this.world;
 	}
 
 	public Set<Material> getBlockedDispense() {
 		return this.disBlock;
 	}
 
 	public Set<Material> getBlockedUsage() {
 		return this.usageBlock;
 	}
 
 	public Set<EntityType> getBlockedEggs() {
 		return this.disEggs;
 	}
 
 	public Set<EntityType> getBlockedMobs() {
 		return this.disMobs;
 	}
 
 	public Set<SpawnReason> getBlockedReasons() {
 		return this.disReasons;
 	}
 
 	public Set<Integer> getPotions() {
 		return this.dispots;
 	}
 
 	public Set<Integer> getDPotions() {
 		return this.disDpots;
 	}
 
 	public boolean get(Option op) {
 		return b[op.getId()];
 	}
 
 	public long g(Option op) {
 		return l[op.getId()];
 	}
 }
