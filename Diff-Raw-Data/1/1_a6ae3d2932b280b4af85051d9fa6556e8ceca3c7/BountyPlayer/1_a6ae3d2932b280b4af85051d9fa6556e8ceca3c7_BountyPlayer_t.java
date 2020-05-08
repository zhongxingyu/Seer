 package uk.co.spudstabber.bountyhunter;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class BountyPlayer {
 	
 	public BountyHunter plugin;
 	
 	private Player player;
 	
 	private int x;
 	private int y;
 	private int z;
 	private int bounty;
 	
 	public BountyPlayer(BountyHunter plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	public void updateBounty(Player player, Location location)
 	{
 		this.player = player;
 		
 		this.x = location.getBlockX();
 		this.y = location.getBlockY();
 		this.z = location.getBlockZ();
 		
 		plugin.bountytimer.getBountyLocation();
 	}
 	
 	public void newBounty(Player player, Location location)
 	{
		if (plugin.isPlayerInHashMap(player)) return;
 		this.updateBounty(player, location);
 		
 		this.bounty = plugin.getConfig().getInt("money.start-bounty");
 		plugin.addPlayerToHashMap(player, location);
 		
 	}
 	
 	public void updateMoney(int money)
 	{
 		this.bounty = money;
 	}
 		
 	public Player getPlayer()
 	{
 		return player;
 	}
 	
 	public int getX()
 	{
 		return x;
 	}
 	
 	public int getY()
 	{
 		return y;
 	}
 	
 	public int getZ()
 	{
 		return z;
 	}
 	
 	public Double getBounty()
 	{
 		return Double.parseDouble(Integer.toString(bounty));
 	}
 	
 	
 }
