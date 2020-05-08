 package net.slipcor.pvparena.modules.regiontool;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.neworder.ArenaModule;
 import net.slipcor.pvparena.neworder.ArenaRegion;
 
 public class RegionTool extends ArenaModule {
 	public RegionTool() {
 		super("RegionTool");
 	}
 
 	@Override
 	public String version() {
 		return "v0.8.6.14";
 	}
 
 	public boolean onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getPlayer().getItemInHand() == null
 				|| event.getPlayer().getItemInHand().getType() == Material.AIR) {
 			return false;
 		}
 
 		if (event.getPlayer().getItemInHand().getType() == Material.AIR) {
 			return false;
 		}
 
 		for (Arena arena : Arenas.getArenas()) {
 			Material mMat = Material.STICK;
			// TODO REPLACE WITH WAND!"
 			if (arena.cfg.get("setup.wand") != null) {
 				db.i("reading wand");
 				try {
 					mMat = Material.getMaterial(arena.cfg.getInt("setup.wand"));
 				} catch (Exception e) {
 					db.i("exception reading ready block");
 					String sMat = arena.cfg.getString("setup.wand");
 					Language.log_warning("matnotfound", sMat);
 					return false;
 				}
 				db.i("mMat now is " + mMat.name());
 				if (event.getPlayer().getItemInHand().getType() == mMat) {
 					Location loc = event.getPlayer().getLocation();
 					if (event.getClickedBlock() != null) {
 						loc = event.getClickedBlock().getLocation();
 					}
 					for (ArenaRegion region : arena.regions.values()) {
 						if (region.contains(loc)) {
 							Arenas.tellPlayer(event.getPlayer(), "fArena b"
 									+ arena.name + "f: region b"
 									+ region.name);
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
