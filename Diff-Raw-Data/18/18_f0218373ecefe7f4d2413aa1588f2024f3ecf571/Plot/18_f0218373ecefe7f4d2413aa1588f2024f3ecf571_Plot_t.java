 package net.lotrcraft.minepermit.plot;
 
 import java.util.ArrayList;
 
import net.lotrcraft.minepermit.MinePermit;
 import net.lotrcraft.minepermit.world.BlockPriceDefinition;
 import net.lotrcraft.minepermit.world.PermitWorld;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class Plot {
 
 	private Location location1;
 	private Location location2;
 	private String owner;
 	private ArrayList<String> allowed_players;
 	private String name;
 	private PermitWorld pw;
 
 	public Plot(Location location1, Location location2, String owner, String name, PermitWorld permitWorld) {
 		if(location1 == null || location2 == null)
 			throw new IllegalArgumentException("Locations cannot be null!");
 		
 		if(!location1.getWorld().equals(location2.getWorld()))
 			throw new IllegalArgumentException("Must be in same world!");
 		
 		if(permitWorld == null)
 			throw new IllegalArgumentException("Supplied PermitWorld cannot be null!");
 		
 		this.setLocation1(location1);
 		this.setLocation2(location2);
 		
 		this.owner = owner;
 		this.name = name;
 		this.pw = permitWorld;
 		
 		allowed_players = new ArrayList<String>();
 	}
 
 	/**
 	 * Checks if either this or the given plot intersects each other.
 	 * @param p Plot to check
 	 * @param spacing Spacing to include incase this is for testing for a valid new plot
 	 * @return True if there is any intersection.
 	 */
 	public boolean intersects(Plot p, int spacing){
 		
 		return contains(p, spacing) || p.contains(this, spacing);
 	}
 	
 	/**
 	 * Only checks if the Plot is contained at all in this plot.
 	 * @param p Plot to check locations of.
 	 * @param spacing Optional spacing to include outside this plot's bounds.
 	 * @return true if p1 is contained in p2, false otherwise.
 	 */
 	public boolean contains(Plot p, int spacing){
 		
 		int higherX = Math.max(p.location1.getBlockX(), p.location2.getBlockX()) + spacing;
 		int lowerX = Math.min(p.location1.getBlockX(), p.location2.getBlockX()) - spacing;
 		
 		int higherZ = Math.max(p.location1.getBlockZ(), p.location2.getBlockZ()) + spacing;
 		int lowerZ = Math.min(p.location1.getBlockZ(), p.location2.getBlockZ()) - spacing;
 		
 		Location l1 = new Location(p.location1.getWorld(), higherX, 0, higherZ);
 		Location l2 = new Location(p.location1.getWorld(), higherX, 0, lowerZ);
 		Location l3 = new Location(p.location1.getWorld(), lowerX, 0, higherZ);
 		Location l4 = new Location(p.location1.getWorld(), lowerX, 0, lowerZ);
 		
 		return contains(l1, spacing) || contains(l2, spacing) || contains(l3, spacing) || contains(l4, spacing);
 	}
 	
 	/**
 	 * Checks if the given point is contained in the plot. And they better be in the same world.
 	 * @param l Location being checked
 	 * @return True if it is contained in the plot.3
 	 */
 	public boolean contains(Location l){
 		return contains(l, 0);
 	}
 	
 	/**
 	 * Checks if the given point is contained in the plot. And they better be in the same world.
 	 * @param l Location being checked
 	 * @param spacing Layer aound the plot to also search.
 	 * @return True if it is contained in the plot.3
 	 */
 	public boolean contains(Location l, int spacing){
 		
 		if(!location1.getWorld().equals(l.getWorld()))
 			throw new IllegalArgumentException("Not in the same world!");
 		
 		int higherX = Math.max(location1.getBlockX(), location2.getBlockX()) + spacing;
 		int lowerX = Math.min(location1.getBlockX(), location2.getBlockX()) - spacing;
 		
 		int higherZ = Math.max(location1.getBlockZ(), location2.getBlockZ()) + spacing;
 		int lowerZ = Math.min(location1.getBlockZ(), location2.getBlockZ()) - spacing;
 		
		if(l.getBlockX() >= lowerX && l.getBlockX() <= higherX && l.getBlockZ() >= lowerZ && l.getBlockZ() <= higherZ)
			return true;
 		
		return false;
 	}
 	
 	/**
 	 * Adds a Player to this plot's list of people who are allowed to mine and build on it,
 	 * though they do not have owner privileges.
 	 * @param p The player to add
 	 */
 	public void addPlayer(Player p){
 		addPlayer(p.getName());
 	}
 	
 	/**
 	 * Adds a Player to this plot's list of people who are allowed to mine and build on it,
 	 * though they do not have owner privileges.
 	 * @param name name of the Player to add
 	 */
 	public void addPlayer(String name){
 		allowed_players.add(name);
 	}
 	
 	/**
 	 * Removes a player from this plot's list of trusted players.
 	 * @param p Player to remove
 	 * @return Whether the player was able to be removed
 	 */
 	public boolean removePlayer(Player p){
 		return removePlayer(p.getName());
 	}
 	
 	/**
 	 * Removes a player from this plot's list of trusted players.
 	 * @param name Name of the Player to remove
 	 * @return Whether the player was able to be removed
 	 */
 	public boolean removePlayer(String name){
 		return allowed_players.remove(name);
 	}
 
 	/**
 	 * Checks if a player is allowed to use (mine and build on) this plot. 
 	 * Includes both the owner and trusted players.
 	 * @param player Player to check
 	 * @return true if they do have permission
 	 */
 	public boolean canUse(Player player) {
 		return canUse(player.getName());
 	}
 	
 	/**
 	 * Checks if a player is allowed to use (mine and build on) this plot. 
 	 * Includes both the owner and trusted players.
 	 * @param name Name of the Player to check
 	 * @return true if they do have permission
 	 */
 	public boolean canUse(String name){
 		return name.equals(owner) || allowed_players.contains(name);
 	}
 	
 
 	/**
 	 * Puts a block at each of the four corners to mark this Plot.
 	 */
 	public void createCorners() {
 		location1.getWorld().getHighestBlockAt(location1).setType(Material.DIAMOND_BLOCK);
 		location2.getWorld().getHighestBlockAt(location2).setType(Material.DIAMOND_BLOCK);
 		
 	}
 	
 	public int calculateCost(){
 		if(pw.isPlotCostCalculated()){
 			
 			int cost = 0;
 			
 			int higherX = Math.max(location1.getBlockX(), location2.getBlockX());
 			int lowerX = Math.min(location1.getBlockX(), location2.getBlockX());
 			
 			int higherZ = Math.max(location1.getBlockZ(), location2.getBlockZ());
 			int lowerZ = Math.min(location1.getBlockZ(), location2.getBlockZ());
 			
 			BlockPriceDefinition bpd = pw.getPlotBlockPrices();
 			
 			World w = location1.getWorld();
 			for(int x = lowerX; x <= higherX; x++){
 				
 				for(int z = lowerZ; z <= higherZ; z++){
 					
 					for(int y = 0; y <= w.getHighestBlockYAt(x, z); y++){
 						
 						Block b = w.getBlockAt(x, y, z);
 						
 						if(b.getTypeId() != Material.AIR.getId())
 							cost += bpd.getBlockPrice(b.getTypeId());
 					}
 					
 				}
 				
 			}
 			
 			return cost;
 			
 		} else {
 			return pw.getUncalculatedCostPerBlock() * Math.abs((location1.getBlockX() - location2.getBlockX()) * (location1.getBlockZ() - location2.getBlockZ()));
 		}
 	}
 
 	@Override
 	public boolean equals(Object o){
 		
 		Plot p = (Plot)o;
 		
 		return p.location1.equals(location1) && p.location2.equals(location2);
 	}
 	
 	/**
 	 * @return the location1
 	 */
 	public Location getLocation1() {
 		return location1;
 	}
 
 	/**
 	 * @param location1 the location1 to set
 	 */
 	public void setLocation1(Location location1) {
 		this.location1 = location1;
 	}
 
 	/**
 	 * @return the location2
 	 */
 	public Location getLocation2() {
 		return location2;
 	}
 
 	/**
 	 * @param location2 the location2 to set
 	 */
 	public void setLocation2(Location location2) {
 		this.location2 = location2;
 	}
 
 	/**
 	 * @return the owner
 	 */
 	public String getOwner() {
 		return owner;
 	}
 
 	/**
 	 * @param owner the owner to set
 	 */
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	/**
 	 * Gets this plot's name, if set.
 	 * @return The name or null
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Sets this plot's name
 	 * @param name Name to set to.
 	 */
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	@Override
 	public String toString(){
 		return "Plot " + getName() + " in world " + location1.getWorld().getName() + " at coords (" + 
 				getLocation1().getBlockX() + "," + getLocation1().getBlockZ() + ") (" + getLocation2().getBlockX() + "," + getLocation2().getBlockZ() + ")";
 	}
 
 }
