 package team.GunsPlus.API;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.GunsPlusPlayer;
 import team.GunsPlus.Block.TripodData;
 import team.GunsPlus.Item.Addition;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Util.GunUtils;
 import team.GunsPlus.Util.PlayerUtils;
 
 public class GunsPlusAPI {
 	private GunsPlus plugin;
 	
 	public GunsPlusAPI(GunsPlus _plus) {
 		plugin = _plus;
 	}
 	
 	public String getVersion() {
 		return plugin.getDescription().getVersion();
 	}
 	
 	public GunsPlusPlayer getPlayer(Player player) {
		GunsPlusPlayer p = null;
		p = PlayerUtils.getPlayerBySpoutPlayer((SpoutPlayer)p);
		return p;
 	}
 	
 	public List<Gun> getAllGuns() {
 		return GunsPlus.allGuns;
 	}
 	
 	public Gun getGunByName(String name){
 		return GunUtils.getGun(name);
 	}
 	
 	public Gun getGunByItemStack(ItemStack itemstack){
 		return GunUtils.getGun(itemstack);
 	}
 	
 	public boolean isGun(ItemStack itemstack){
 		return GunUtils.isGun(itemstack);
 	}
 	
 	public List<Ammo> getAllAmmo(){
 		return GunsPlus.allAmmo;
 	}
 	
 	public List<Addition> getAllAdditions(){
 		return GunsPlus.allAdditions;
 	}
 	
 	public List<Block> getAllTripodBlocks(){
 		List<Block> blocklist = new ArrayList<Block>();
 		for(TripodData td : GunsPlus.allTripodBlocks){
 			blocklist.add(td.getLocation().getBlock());
 		}
 		return blocklist;
 	}
 	
 	public List<GunsPlusPlayer> getOnlineGunsPlusPlayers(){
 		return GunsPlus.GunsPlusPlayers;
 	}
 	
 }
