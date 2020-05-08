 package com.FriedTaco.taco.MorePhysics;
 
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 
 
 public class MorePhysicsPlayerListener extends PlayerListener 
 {
 	final MorePhysics plugin;
 	double tollerance = .08;
 	double min = .001;
 
     public MorePhysicsPlayerListener(MorePhysics instance) 
     {
         plugin = instance;
     }
     double weight(int id)
     {
     	switch(id)
     	{
     		case 298:
     			return .002;	
     		case 299:
     			return .01;	
     		case 300:
     			return .008;	
     		case 301:
     			return .002;	
     		case 302:
     			return .01;
     		case 303:
     			return .05;
     		case 304:
     			return .03;
     		case 305:
     			return .01;
     		case 306:
     			return .02;
     		case 307:
     			return .06;
     		case 308:
     			return .04;
     		case 309:
     			return .02;
     		case 310:
     			return .005;
     		case 311:
     			return .03;
     		case 312:
     			return .02;
     		case 313:
     			return .005;
     		case 314:
     			return .04;
     		case 315:
     			return .08;
     		case 316:
     			return .07;
     		case 317:
     			return .04;
     		default:
     			return 0;
     	}
     }
     public void onPlayerMove(PlayerMoveEvent event)
     {
     	Block on = event.getPlayer().getWorld().getBlockAt(event.getTo());
     	Block under = event.getPlayer().getWorld().getBlockAt(new Location(event.getPlayer().getWorld(), event.getTo().getBlockX(),event.getTo().getBlockY()-1,event.getTo().getBlockZ()));
     	
     	if(on != null && under != null)
     	{
     		if((on.getTypeId() == 8 || on.getTypeId() == 9) && (under.getTypeId() == 8 || under.getTypeId() == 9) && plugin.swimming)
     		{
     			double modifier = 0;
     			for(ItemStack i : event.getPlayer().getInventory().getArmorContents())
     			{
     				if(i != null)
     					modifier += weight(i.getTypeId());
     			}
     			if(modifier > 0)
     				if(event.getTo().getY() > event.getFrom().getY())
     					event.getPlayer().setVelocity(new Vector(event.getPlayer().getVelocity().getX(),0-modifier,event.getPlayer().getVelocity().getZ()));
     				else
     					event.getPlayer().setVelocity(new Vector(event.getPlayer().getVelocity().getX(),0-(modifier*4),event.getPlayer().getVelocity().getZ()));
     			
     		}
     		else if(on.getTypeId() == 0)
     		{
     			double modifier = 0;
     			for(ItemStack i : event.getPlayer().getInventory().getArmorContents())
     			{
     				if(i != null)
     					modifier += weight(i.getTypeId());
     			}
     			if(modifier >= 0)
     			{
     				//double x = event.getPlayer().getVelocity().getX();
     				double x = event.getPlayer().getVelocity().getX();
     				double z = event.getPlayer().getVelocity().getZ();
     				double diffX = event.getTo().getX() - event.getFrom().getX();
     				double diffZ = event.getTo().getZ() - event.getFrom().getZ();
     				Vector v = event.getPlayer().getVelocity();
     				if((!(event.getTo().getY() > event.getFrom().getY())) && (under.getTypeId() != 79) && plugin.movement && !event.getPlayer().isSneaking())
     				{
 	    				if(diffX > tollerance && Math.abs(diffX) < 1)
 	    					v.setX(x-(modifier/8));
 	    				else if(diffX < tollerance && Math.abs(diffX) < 1)
 	    					v.setX(x+(modifier/8));
 	    				if(diffZ > tollerance && Math.abs(diffZ) < 1)
 	    					v.setX(z-(modifier/8));
 	    				else if(diffZ < tollerance && Math.abs(diffZ) < 1)
 	    					v.setX(z+(modifier/8));
 	    				event.getPlayer().setVelocity(v);
     				}
     					
     			}
     		}
     	}
     }
    }
 
 
