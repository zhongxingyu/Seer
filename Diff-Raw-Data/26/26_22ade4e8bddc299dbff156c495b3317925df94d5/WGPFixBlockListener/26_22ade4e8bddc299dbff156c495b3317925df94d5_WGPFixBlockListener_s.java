 package asofold.fix.wgp;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class WGPFixBlockListener extends BlockListener {
 	WGPFix plugin;
 	public WGPFixBlockListener(WGPFix plugin){
 		this.plugin = plugin;
 	}
 	long tsWG = 0;
 	WorldGuardPlugin wg = null;
 	// config settings:
 	long tsThreshold = 4000;
 	boolean monitorPistons = true;
 	boolean preventNonStickyRetract = false;
 	boolean popDisallowed = false;
 	public final Set<Integer> denySticky = new HashSet<Integer>();
 	public final Set<Integer> denyAll = new HashSet<Integer>();
 	@Override
 	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
 		if ( !monitorPistons) return;
 		if ( event.isCancelled()) return;
 		Block pistonBlock = event.getBlock();
 		List<Block> affectedBlocks = event.getBlocks();

 		List<Location> locs = new LinkedList<Location>();
 		BlockFace dir = event.getDirection();
 		locs.add(pistonBlock.getRelative(dir).getLocation());
 		int bSize = affectedBlocks.size();
 		boolean isSticky = event.isSticky();
 		if ( (affectedBlocks!=null) && (bSize>0) ){
 			for ( Block block : affectedBlocks){
 				int id = block.getTypeId();
				if ( denyAll.contains(id)){
					event.setCancelled(true);
					if (popDisallowed) pop(pistonBlock, null, isSticky);
					return;
				} else if (isSticky && denySticky.contains(id)){
 					event.setCancelled(true);
 					if (popDisallowed) pop(pistonBlock, null, isSticky);
 					return;
 				}
 				locs.add(block.getLocation());
 			}
 			// add empty block at end
 			Block endBlock = pistonBlock.getRelative(dir,bSize+1 );
 			locs.add(endBlock.getLocation());
 		}
 		Location pistonLoc = pistonBlock.getLocation();
 		if ( !sameOwners(pistonLoc, locs)){
 			event.setCancelled(true);
 			if (popDisallowed) pop(pistonBlock, null, isSticky);
 		} 
 	}
 
 	@Override
 	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
 		if ( !monitorPistons) return;
 		if ( event.isCancelled()) return;
 		boolean isSticky = event.isSticky() ;
 		boolean check = isSticky|| preventNonStickyRetract;
 		if (check){
 			Block pistonBlock = event.getBlock();
 			List<Location> affected = new LinkedList<Location>();
 			Location affectedLoc = event.getRetractLocation(); // pulled if sticky
 			if ( isSticky && (affectedLoc != null)){
 				affected.add(affectedLoc);
 				if ( denySticky.contains(affectedLoc.getBlock().getTypeId())){
 					event.setCancelled(true);
 					if (popDisallowed) pop(pistonBlock, pistonBlock.getRelative(event.getDirection()), isSticky );
 					return;
 				}
 			}
 			// TODO: bug search
 			affected.add(pistonBlock.getRelative(event.getDirection()).getLocation()); // piston extension
 			Location pistonLoc = pistonBlock.getLocation();
 			if ( !sameOwners(pistonLoc, affected)){
 				event.setCancelled(true);	
 				if (popDisallowed) pop(pistonBlock, pistonBlock.getRelative(event.getDirection()), isSticky );
 			} 
 		}
 	}
 
 	void pop(Block pistonBlock, Block extensionBlock, boolean isSticky) {
 		int itemId = isSticky ? 29:33;
 		pistonBlock.setType(Material.AIR);
 		pistonBlock.getState().update();
 		if (extensionBlock!=null){
 			extensionBlock.setType(Material.AIR);
 			extensionBlock.getState().update();
 		}
 		World world = pistonBlock.getWorld();
 		double x = .5 + pistonBlock.getX();
 		double y = .5 + pistonBlock.getY();
 		double z = .5 + pistonBlock.getZ();
 		world.dropItemNaturally(new Location(world,x,y,z), new ItemStack(itemId,1));
 	}
 	
 	boolean sameOwners(Location refLoc, List<Location> locs){
 		WorldGuardPlugin wg = getWorldGuard();
 		if ( wg == null) return false; // security option.
 		RegionManager mg = wg.getRegionManager(refLoc.getWorld());
 		ApplicableRegionSet set = mg.getApplicableRegions(refLoc);
 		boolean isRegion = set.size() != 0;
 		boolean hasEmpty = !isRegion;
 		// TODO: use some caching ?
 		Set<String> mustMatch = getUserSet(set);
 		int size = mustMatch.size();
 		boolean hasCheckers = !WGPFix.regionCheckers.isEmpty();
 		List<ApplicableRegionSet> applicableSets  = null;
 		if ( hasCheckers){ 
 			applicableSets = new LinkedList<ApplicableRegionSet>();
 			if (isRegion) applicableSets.add(set);
 		}
 		for ( Location loc : locs){
 			set = mg.getApplicableRegions(loc);
 			if (set.size()==0){ // ok.
 				hasEmpty = true;
 			} else if ( isRegion ){
 				// compare owner sets:
 				Set<String> ref = getUserSet(set);
 				if ( size != ref.size() ) return false;
 				if ( !mustMatch.containsAll(ref)) return false;
 				if (hasCheckers) applicableSets.add(set);
 			} else {
 				// disallow from no region to unowned region !
 				return false; 
 			}
 			
 		}
 		if (hasCheckers){
 			String worldName = refLoc.getWorld().getName();
 			for ( WGPRegionChecker checker : WGPFix.regionCheckers){
 				locs.add(refLoc);
 				if ( !checker.checkRegions(worldName, applicableSets, hasEmpty)) return false;
 			}
 		}
 		return true;
 	}
 	Set<String> getUserSet(ApplicableRegionSet rs){
 		Set<String> set = new HashSet<String>();
 		if ( rs != null ){
 			for ( ProtectedRegion region : rs){
 				DefaultDomain dom = region.getOwners();
 				for ( String p : dom.getPlayers()){
 					set.add(p.toLowerCase());
 				}
 				for ( String p : dom.getGroups()){
 					set.add("g:"+p.toLowerCase());
 				}
 				dom = region.getMembers();
 				for ( String p : dom.getPlayers()){
 					set.add(p.toLowerCase());
 				}
 				for ( String p : dom.getGroups()){
 					set.add("g:"+p.toLowerCase());
 				}
 			}
 		}
 		
 		return set;
 	}
 	final boolean setWG() {
 		Plugin temp = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
 		boolean ok = true;
 		if ( temp == null ) ok = false;
 		if ( !temp.isEnabled() ) ok = false;
 		if ( !ok){
 			resetWG();
 			return false;
 		} 
 		WorldGuardPlugin wg = (WorldGuardPlugin) temp;
 		this.wg = wg;
 		this.tsWG = System.currentTimeMillis();
 		// TODO: maybe get region managers already here
 		return true;
 	}
 	final WorldGuardPlugin getWorldGuard(){
 		if (System.currentTimeMillis()-this.tsWG > this.tsThreshold) this.setWG();
 		return this.wg;
 	}
 	final void resetWG(){
 		this.wg = null;
 		this.tsWG = 0;
 	}
 }
