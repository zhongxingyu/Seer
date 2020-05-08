 package com.Cayviel.FallBreaks;
 
//import net.minecraft.server.Item;
//import org.bukkit.inventory.PlayerInventory;
//import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.World;
 import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
 
 
 public class FallListener extends EntityListener{
 	PluginManager pm;
 	public FallListener(PluginManager pmo){
 		pm=pmo;
 	}
 	String world = FallBreaks.properties.getProperty("level-name");
 	World w;
	
 	public void onEntityDamage(EntityDamageEvent Ifell){
 		if (Ifell.getEntity() instanceof Player){
 			if (Ifell.getCause().equals(DamageCause.FALL)){
 
 				Player player = (Player)Ifell.getEntity();

 				if (w == null){	w = player.getWorld();}
 					
 					Block BlockOn = player.getLocation().subtract(0, 1, 0).getBlock();
 					
 					if (FallBreaks.breakconfig.getbreak(BlockOn.getType().toString())){
 
 						int booteq = getbooteq(player.getInventory().getBoots().getTypeId());
 						int Blockreq = matreq(BlockOn.getTypeId());
 						
 						if ((BreakConfig.bootreq)&&(booteq==0)){return;}
 						
 						if (BreakConfig.bootmining){
							bootmine(Blockreq, booteq, BlockOn);
 						}						
 						if (BreakConfig.boottier){
 							if (Blockreq<=booteq){
 								BlockOn.setTypeId(0);
 							}
 						}else{BlockOn.setTypeId(0);}
 					}
 				}
 			}
 		}
	
	void bootmine (int Blockreq, int booteq, Block BlockOn){
		int BlockOnData = BlockOn.getData();		
			if (Blockreq<=booteq){
				net.minecraft.server.Block.byId[BlockOn.getTypeId()].g(((CraftWorld)w).getHandle(), BlockOn.getX(), BlockOn.getY(), BlockOn.getZ(), BlockOnData);
			}
 	}
 	
 	 int matreq(int itemid){
 		switch (itemid){
 		case 45: return 1;
 		case 24: return 1;
 		case 01: return 1;
 		case 30: return 1;
 		case 48: return 1;
 		case 16: return 1;
 		case 43: return 1;
 		case 44: return 1;
 		case 15: return 2;
 		case 22: return 2;
 		case 21: return 2;
 		case 14: return 3;
 		case 73: return 3;
 		case 74: return 3;
 		case 56: return 3;
 		case 42: return 3;		
 		case 41: return 3;
 		case 57: return 3;
 		case 49: return 4;
 		default: return 0;
 		}
 	 }
 	 
 	int getbooteq(int itemid){
 		switch (itemid) {
 		case 301: return 1;
 		case 305: return 2;
 		case 309: return 3;
 		case 313: return 4;
 		case 317: return 1;
 		default:  return 0;
 		}
 	}
 }
