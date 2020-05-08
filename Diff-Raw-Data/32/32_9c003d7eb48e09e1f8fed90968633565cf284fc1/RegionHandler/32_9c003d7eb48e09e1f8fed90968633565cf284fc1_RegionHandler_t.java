 package TotalWizard.Handlers.Regions;
 
 import org.bukkit.entity.Player;
 
 import TotalWizard.TotalWizard;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 public class RegionHandler
 {
 	
 	private TotalWizard Plugin;
 	public RegionHandler(TotalWizard Plugin)
 	{
 		this.Plugin = Plugin;
 	}
 	
 	public boolean isPlayerInRegion(Player Player)
 	{
 		ApplicableRegionSet PlayerRegions = getRegionsPlayerIsIn(Player);
 		if (PlayerRegions != null && PlayerRegions.size() > 0)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public ApplicableRegionSet getRegionsPlayerIsIn(Player Player)
 	{
 		Vector PlayerVector = new Vector(Player.getLocation().getX(),Player.getLocation().getY(),Player.getLocation().getZ());
 		RegionManager RegionMan = TotalWizard.WorldGuard.getRegionManager(Player.getWorld());
 		if (RegionMan != null)
 		{
 			return RegionMan.getApplicableRegions(PlayerVector);
 		}
 		return null;
 	}
 	
 	public boolean isPlayerInNoPVPRegion(Player Player)
 	{
 		if (isPlayerInRegion(Player))
 		{
 			ApplicableRegionSet PlayerRegions = getRegionsPlayerIsIn(Player);
 			if (PlayerRegions.allows(DefaultFlag.PVP))
 			{
 				return false;
 			}
 			else
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 }
