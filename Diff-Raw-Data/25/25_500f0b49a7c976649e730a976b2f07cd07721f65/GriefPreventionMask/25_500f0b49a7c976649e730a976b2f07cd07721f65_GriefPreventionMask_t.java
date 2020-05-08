 package me.meta1203.plugins.gpextras;
 
 import me.ryanhamshire.GriefPrevention.Claim;
 import me.ryanhamshire.GriefPrevention.DataStore;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.EditSession;
 import com.sk89q.worldedit.LocalPlayer;
 import com.sk89q.worldedit.LocalSession;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.masks.Mask;
 
 public class GriefPreventionMask implements Mask {
 	private Player player;
 	private DataStore ds = Gpextras.gp.dataStore;
 	
 	GriefPreventionMask(Player p) {
 		player = p;
 	}
 	
 	public boolean matches(EditSession arg0, Vector arg1) {
 		if (!Gpextras.gp.claimsEnabledForWorld(player.getWorld())) {
 			return true;
 		}
 		Location loc = new Location(player.getWorld(), arg1.getBlockX(), arg1.getBlockY(), arg1.getBlockZ());
 		Claim claim = ds.getClaimAt(loc, true, null);
 		if (claim == null) {
 			return false;
 		}
 		if (claim.allowBuild(player) == null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void prepare(LocalSession arg0, LocalPlayer arg1, Vector arg2) {
		System.out.println("Activating WorldEdit!");
 	}
 
 }
