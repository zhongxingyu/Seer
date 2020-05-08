 package me.Kruithne.MinecartsMod;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.util.Vector;
 
 public class MMPoweredMinecart {
 
 	public PoweredMinecart cart;
 	boolean powered = false;
 	Server server;
 	
 	public MMPoweredMinecart(PoweredMinecart cart, Server server)
 	{
 		this.cart = cart;
 		this.server = server;
 	}
 	
 	public boolean isOnStraightRail()
 	{
 		Block blockBelow = this.cart.getLocation().getBlock();
 		
 		if (blockBelow.getType() == Material.RAILS)
 		{
 			byte dataValue = blockBelow.getData();
 			if (dataValue == 0 || dataValue == 1)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isOnSlopedRail()
 	{
 		Block blockBelow = this.cart.getLocation().getBlock();
 		
 		if (blockBelow.getType() == Material.RAILS)
 		{
 			byte dataValue = blockBelow.getData();
 			if (dataValue == 2 || dataValue == 3 || dataValue == 4 || dataValue == 5)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isPowered()
 	{
 		return this.powered;
 	}
 	
 	public void gainSpeed(Double speed)
 	{
 		Vector direction = this.cart.getLocation().getDirection();	
 		if (this.isOnSlopedRail() || this.isOnStraightRail())
 		{
 			//this.server.broadcastMessage("Minecart (" + this.cart.getEntityId() + ") was on slope/straight, setting speed.");		
 			this.cart.setVelocity(new Vector(direction.getX() * speed, direction.getY(), direction.getZ() * speed));
 		}
 		else
 		{
 			//this.server.broadcastMessage("Minecart (" + this.cart.getEntityId() + ") was not on valid track, MAKING IT SLOW.");	
 			this.cart.setVelocity(new Vector(direction.getX() * 0.1, direction.getY(), direction.getZ() * 0.1));
 		}
 		this.server.broadcastMessage("Minecart (" + this.cart.getEntityId() + ") going in direction:");
 		this.server.broadcastMessage("X: " + direction.getX());
		this.server.broadcastMessage("Z: " + direction.getZ());
 	}
 }
