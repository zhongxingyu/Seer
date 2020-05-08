 package com.github.thelonedevil.TLDScape.skills;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import org.spout.api.entity.Player;
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.Listener;
 import org.spout.api.event.player.Action;
 import org.spout.api.event.player.PlayerInteractBlockEvent;
 import org.spout.api.geo.cuboid.Block;
 import org.spout.api.inventory.ItemStack;
 import org.spout.api.inventory.Slot;
 import org.spout.api.material.Material;
 import org.spout.vanilla.material.VanillaMaterials;
 import org.spout.vanilla.material.item.tool.Tool;
 import org.spout.vanilla.util.PlayerUtil;
 
 import com.github.thelonedevil.TLDCommonlib.DataBase;
 import com.github.thelonedevil.TLDCommonlib.Lib;
 import com.github.thelonedevil.TLDScape.TLDScapePlugin;
 
 public class WoodCuttingListener implements Listener {
 
 	private DataBase database = new DataBase();
 	private TLDScapePlugin plugin;
 
 	public WoodCuttingListener(TLDScapePlugin instance) {
 		this.plugin = instance;
 	}
 
 	@EventHandler
 	public void onChop(PlayerInteractBlockEvent event) {
 		Action action = event.getAction();
 		Player p = event.getEntity();
 		String name = p.getName();
 		HashMap<String, Object> skills = null;
 		try {
 			skills = database.getRow(Lib.statement, "ScapeSkills", name, "Player");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (skills != null) {
 			Block block = event.getInteracted();
 			Material material = block.getMaterial();
 			if (material.isMaterial(VanillaMaterials.LOG)) {
 				Slot slot = PlayerUtil.getHeldSlot(p);
 				ItemStack held = slot.get();
 				Tool tool;
 				String axe = null;
 				if (held.getMaterial() instanceof Tool) {
 					tool = (Tool) held.getMaterial();
 					int id = tool.getMinecraftId();
 					switch (id) {
 					case 271:
 						axe = "Wood";
 					case 275:
 						axe = "Stone";
 					case 258:
 						axe = "Iron";
 					case 279:
 						axe = "Diamond";
 					case 286:
 						axe = "Gold";
 					default:
 						axe = "Other";
 					}
 				}
 				if (axe != null) {
 					// TODO sort per tool
 					int xp = (Integer) skills.get("WoodCutting");
 
					if (Levels.L1 < xp && xp < Levels.L7) {
 						// wooden
 						if (axe.equalsIgnoreCase("Wood")) {
 							if (xp >= Levels.L1) {
 							} else if (xp >= Levels.L2) {
 							} else if (xp >= Levels.L3) {
 							} else if (xp >= Levels.L4) {
 							} else if (xp >= Levels.L5) {
 							} else if (xp >= Levels.L6) {
 							}
 						}
 
 					} else if (Levels.L7 < xp && xp < Levels.L13) {
 						// stone
 						if (axe.equalsIgnoreCase("Wood") || axe.equalsIgnoreCase("Stone")) {
 							if (xp >= Levels.L7) {
 							} else if (xp >= Levels.L8) {
 							} else if (xp >= Levels.L9) {
 							} else if (xp >= Levels.L10) {
 							} else if (xp >= Levels.L11) {
 							} else if (xp >= Levels.L12) {
 							}
 						}
 
 					} else if (Levels.L13 <= xp && xp < Levels.L19) {
 						// gold
 						if (axe.equalsIgnoreCase("Wood") || axe.equalsIgnoreCase("Stone") || axe.equalsIgnoreCase("Gold")) {
 							if (xp >= Levels.L13) {
 							} else if (xp >= Levels.L14) {
 							} else if (xp >= Levels.L15) {
 							} else if (xp >= Levels.L16) {
 							} else if (xp >= Levels.L17) {
 							} else if (xp >= Levels.L18) {
 							}
 						}
 					} else if (Levels.L19 <= xp && xp < Levels.L15) {
 						// iron
 						if (axe.equalsIgnoreCase("Wood") || axe.equalsIgnoreCase("Stone") || axe.equalsIgnoreCase("Gold") || axe.equalsIgnoreCase("Iron")) {
 							if (xp >= Levels.L19) {
 							} else if (xp >= Levels.L20) {
 							} else if (xp >= Levels.L21) {
 							} else if (xp >= Levels.L22) {
 							} else if (xp >= Levels.L23) {
 							} else if (xp >= Levels.L24) {
 							}
 						}
 					} else if (Levels.L25 <= xp && xp < Levels.L30) {
 						// diamond
 						if (axe.equalsIgnoreCase("Wood") || axe.equalsIgnoreCase("Stone") || axe.equalsIgnoreCase("Gold") || axe.equalsIgnoreCase("Iron") || axe.equalsIgnoreCase("Diamond")) {
 							if (xp >= Levels.L25) {
 							} else if (xp >= Levels.L26) {
 							} else if (xp >= Levels.L27) {
 							} else if (xp >= Levels.L28) {
 							} else if (xp >= Levels.L29) {
 							}
 						}
 					} else if (xp >= Levels.L30) {
 						if (axe.equalsIgnoreCase("Wood") || axe.equalsIgnoreCase("Stone") || axe.equalsIgnoreCase("Gold") || axe.equalsIgnoreCase("Iron") || axe.equalsIgnoreCase("Diamond")) {
 						}
 					}
 				}
 			}
 		} else
 			throw new IllegalStateException("Player has no skill levels");
 
 	}
 }
