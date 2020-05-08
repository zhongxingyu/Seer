 package com.Cayviel.FallBreaks;
 
 import java.util.Random;
 
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.entity.Player;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 
 
 public class FallListener extends EntityListener{
 
 	public void onEntityDamage(EntityDamageEvent Ifell){
 		if (! (Ifell.getEntity() instanceof Player)) return; //leave if entity isn't player
 		if (! (Ifell.getCause().equals(DamageCause.FALL))) return; //leave if player didn't fall
 
 		//initiate parameters
 		Player player = (Player)Ifell.getEntity();
 		Block blockOn = getOnBlock(player);
 		int booteq = getbooteq(player.getInventory().getBoots().getTypeId());
 		int Blockreq = matreq(blockOn.getTypeId());
 		boolean bootsMeetReq = (Blockreq <= booteq);	
 		boolean drop = true; 
 		
 		if (! BreakConfig.getbreak(blockOn.getType().toString())) return;					// if block is not breakable don't break
		if (BreakConfig.breakReqDamageTier && (Blockreq*2 > Ifell.getDamage())) return; 	// if requires pain, but pain level not met, don't break
 		if (BreakConfig.breakReqBoots && (booteq == 0)) return;								// if boots are required to break, but no boots are present, don't break
 		if (BreakConfig.breakReqBootTier && !bootsMeetReq) return;							// if boots tiering is required for break but boot tier is too low, don't break
 		
 		if (!BreakConfig.dropEnabled) drop = false;										// if drops are not enabled, don't drop 
 		if (BreakConfig.dropReqBootTier && !bootsMeetReq) drop = false;					// if drops require tiered boots but boot tier is too low, leave
 		if (BreakConfig.dropSimPunch && (Blockreq > 0)) drop = false;					// if drops simulate punching but drop requires greater than fists, leave
 		
 		//do the break/dropping
 		if (drop) net.minecraft.server.Block.byId[blockOn.getTypeId()].dropNaturally(((CraftWorld)blockOn.getWorld()).getHandle(), blockOn.getX(), blockOn.getY(), blockOn.getZ(), blockOn.getData(), 1.0F,(new Random()).nextInt());
 		blockOn.setTypeId(0);
 	}
 	
 	void bootmine (Block BlockOn, Player player){
 		if (BlockOn.getTypeId() == 0) return;
 		
 	}
 	
 	 int matreq(int itemid){
 		 
 		switch (itemid){
 		
 		case 01: return 1;
 		case 16: return 1;
 		case 23: return 1;
 		case 24: return 1;
 		case 25: return 1;
 		case 43: return 1;
 		case 30: return 1;
 		case 44: return 1;
 		case 45: return 1;
 		case 48: return 1;
 		case 61: return 1;
 		case 62: return 1;
 		case 71: return 1;
 		case 101: return 1;
 		case 112: return 1;
 		case 113: return 1;
 		case 116: return 1;
 		case 117: return 1;
 		case 121: return 1;
 		
 		//stairs
 		case 53: return 1;
 		case 67: return 1;
 		case 108: return 1;
 		case 109: return 1;
 		case 114: return 1;
 
 		case 15: return 2;
 		case 21: return 2;
 		case 22: return 2;
 		case 118: return 2;
 				
 		case 14: return 3;
 		case 41: return 3;
 		case 42: return 3;
 		case 56: return 3;
 		case 73: return 3;
 		case 74: return 3;
 
 		case 57: return 3;
 		
 		case 49: return 4;
 		case 122: return 4; 
 		
 		case 120: return 5;
 		
 		case 07: return 7;
 		
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
 	
 	boolean isAir(Location loc){
 		Integer id = loc.getBlock().getTypeId();
 		if (net.minecraft.server.Block.byId[id] == null) return true;
 		return !(net.minecraft.server.Block.byId[id].material.isSolid());
 	}
 	
 	Block getOnBlock(Player player){
 		Location bLoc = player.getLocation().subtract(0.0, 1.0, 0.0);
 		Block block = bLoc.getBlock();
 		if (isAir(bLoc)){
 			Double fromCenterX = Math.abs(bLoc.getX()-(int)bLoc.getX())-0.5;
 			Double fromCenterZ = Math.abs(bLoc.getZ()-(int)bLoc.getZ())-0.5;
 
 			if (Math.abs(fromCenterX) >= Math.abs(fromCenterZ)){
 				bLoc = bLoc.add(Math.signum(fromCenterX),0.0D,0.0D);
 				if (isAir(bLoc)){
 					bLoc = bLoc.add(0.0D,0.0D,Math.signum(fromCenterZ));
 					if (isAir(bLoc)){
 						bLoc = bLoc.add(-Math.signum(fromCenterX),0.0D,0.0D);
 						if (isAir(bLoc)) return null;
 					}
 				}
 			}else{
 				bLoc = bLoc.add(0.0D,0.0D,Math.signum(fromCenterZ));
 				if (isAir(bLoc)){
 					bLoc = bLoc.add(Math.signum(fromCenterX),0.0D,0.0D);
 					if (isAir(bLoc)){
 						bLoc = bLoc.add(0.0D,0.0D,-Math.signum(fromCenterZ));
 						if (isAir(bLoc)) return null;
 					}
 				}
 			}
 		}
 		block = bLoc.getBlock();
 		return block;
 	}
 	
 }
