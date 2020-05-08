 package me.G4meM0ment.DeathAndRebirth.Handler;
 

 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 
 import me.G4meM0ment.DeathAndRebirth.Types.DARPlayer;
 import me.G4meM0ment.DeathAndRebirth.Types.Grave;
 
 public class GraveHandler {
 	
 	private GhostHandler gH;
 	
 	public GraveHandler(GhostHandler gH)
 	{
 		this.gH = gH;
 	}
 	public GraveHandler()
 	{
 		gH = new GhostHandler();
 	}
 	
 	/**
 	 * To check click events in purpose of resurrecting
 	 * @param p
 	 * @param loc
 	 * @return
 	 */
 	public boolean isPlayersGrave(DARPlayer p, Location loc)
 	{
 		if(p.getGrave().getLocation().distance(loc) <= ConfigHandler.maxGraveDistance)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * For definite location checks
 	 * @param loc
 	 * @return
 	 */
 	public boolean isGrave(Location loc)
 	{
 		for(DARPlayer p : gH.getDARPlayers(loc.getWorld().getName()))
 		{
 			if(p.getGrave() == null) continue;
 			if(p.getGrave().getLocation().equals(loc))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Places the players grave sign
 	 * @param g
 	 * @param b
 	 */
 	public void placeSign(Grave g, Block b)
 	{
 		if(g == null || b == null) return;
 		
 		Material m = b.getType();
 		
 		/*
 		 * TODO algorithm to place sign next to the water 
 		 */
 		if(m.equals(Material.LAVA) || m.equals(Material.WATER)) return;
 
 		//save the information of the old block
 		g.setBlockMaterial(b.getType());
 		g.setData(b.getData());
 		
 		//create the sign and edit its lines
 		b.setType(Material.SIGN_POST);
 		Sign sign = (Sign) b.getState();
 		sign.setLine(1, ConfigHandler.signText);
 		sign.setLine(2, g.getPlayer().getName());
 		sign.update(true);
 		
 		//save the sign in the grave
 		g.setSign(sign);
 	}
 	
 	/**
 	 * Removes a players sign
 	 * @param g
 	 */
 	public void removeSign(Grave g)
 	{
 		if(g == null) return;
 		if(g.getBlockMaterial() == null) return;
 		
 		g.getLocation().getBlock().setType(g.getBlockMaterial());
 		g.getLocation().getBlock().setData((byte) g.getData());
 		
 		g.setBlockMaterial(null);
 		g.setData(0);
 	}
 	
 	public void removeOldSigns()
 	{
 		for(String world : gH.getDARPlayerLists().keySet())
 		{
 			for(DARPlayer p : gH.getDARPlayers(world))
 			{
 				if(p.getGrave() == null) continue;
 				if(p.getGrave().getPlacedMillis() > 0)
 					if(System.currentTimeMillis()-p.getGrave().getPlacedMillis() > ConfigHandler.autoSignRemove)
 						removeSign(p.getGrave());
 			}
 		}
 	}
 
 }
