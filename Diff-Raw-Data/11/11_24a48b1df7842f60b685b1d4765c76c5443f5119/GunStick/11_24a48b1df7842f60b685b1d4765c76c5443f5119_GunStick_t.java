 package me.herp.derp.GunStick;
 
 import net.minecraft.server.EntityFireball;
 import net.minecraft.server.EntityLiving;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class GunStick extends JavaPlugin implements Listener {
 	public void onEnable() 
 	{
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	public void fireball(Player player)
 	{
 		CraftPlayer craftPlayer = (CraftPlayer) player;
 		EntityLiving playerEntity = craftPlayer.getHandle();
 		Vector lookat = player.getLocation().getDirection().multiply(10);
 		Location loc = player.getLocation();
 		EntityFireball fball = new EntityFireball(((CraftWorld) player.getWorld()).getHandle(), playerEntity, lookat.getX(), lookat.getY(), lookat.getZ());
 		fball.locX = loc.getX() + (lookat.getX()/5.0) + 0.25;
 		fball.locY = loc.getY() + (player.getEyeHeight()/2.0) + 0.5;
 		fball.locZ = loc.getZ() + (lookat.getZ()/5.0);
 		((CraftWorld) player.getWorld()).getHandle().addEntity(fball);
 	}
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onInteract(PlayerInteractEvent e)
     {
 		Material material = e.getMaterial();
 		if(material == Material.STICK)
 		{
 		boolean hasAmmo = false;
 		if (e.getAction() == Action.RIGHT_CLICK_AIR || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) //So it does not shoot when you "hit"
 		{
 			Player p = e.getPlayer();
 			if (p.hasPermission("gunstick.shoot") || p.isOp())
 			{
 				Integer normInvSize = p.getInventory().getSize();
 	            Integer i = 0;
 	            for (i=0; i<normInvSize; i++) 
 	            {
 	            	ItemStack item = p.getInventory().getItem(i);
 	            	//p.sendMessage(item.toString());
 	            	if (item.toString().toLowerCase().contains("sulphur"))
 	            	{
 	            		hasAmmo = true;
 	            		break;
 	            	}
 	            }
	            if (hasAmmo || p.isOp())
 	            {
	            	if (!p.isOp())
	            	{
 	            	p.getInventory().removeItem(new ItemStack(Material.SULPHUR, 1));
 	            	p.getInventory().setContents(p.getInventory().getContents());
 	            	p.updateInventory();
	            	}
 						fireball(p);
 						hasAmmo = false;
 	            }
 			else
 			{
 				p.sendMessage("You may not shoot guns!");
 			}
 		}
     }
 }
     }   }
