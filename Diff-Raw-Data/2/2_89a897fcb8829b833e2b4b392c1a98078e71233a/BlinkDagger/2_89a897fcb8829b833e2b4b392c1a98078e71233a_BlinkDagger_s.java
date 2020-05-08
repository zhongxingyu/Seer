 package hunternif.mc.dota2items.item;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.effect.Effect;
 import hunternif.mc.dota2items.effect.EffectInstance;
 import hunternif.mc.dota2items.event.UseItemEvent;
 import hunternif.mc.dota2items.network.EffectPacket;
 import hunternif.mc.dota2items.util.BlockUtil;
 import hunternif.mc.dota2items.util.SideHit;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlinkDagger extends CooldownItem {
 
 	public static final double maxDistance = 50;
 	public static final float hurtCooldown = 3;
 	public static final float usualCooldown = 14;
 	
 	private Random rand;
 	
 	public BlinkDagger(int id) {
 		super(id);
 		rand = new Random();
 		setCooldown(usualCooldown);
 		setManaCost(75);
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public boolean isFull3D() {
 		return true;
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
 		if (!tryUse(itemStack, player)) {
 			return itemStack;
 		}
 		MinecraftForge.EVENT_BUS.post(new UseItemEvent(player, this));
 		
 		int destX;
 		int destY;
 		int destZ;
 		
 		// Allow landing on all solid and liquid blocks, but the latter only if the player is not in water
 		Vec3 position = world.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
 		if (!world.isRemote) {
 			// Because in server worlds the Y coordinate of a player is his feet's coordinate, without yOffset.
 			position = position.addVector(0, 1.62D, 0);
 		}
         Vec3 look = player.getLook(1.0F);
         Vec3 lookFar = position.addVector(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance);
         boolean isUnderWater = player.isInsideOfMaterial(Material.water);
 		MovingObjectPosition hit = world.clip(position, lookFar, !isUnderWater); // raytrace
 		
 		if (hit != null) {
 			destX = hit.blockX;
 			destY = hit.blockY;
 			destZ = hit.blockZ;
 			
 			// Only blink on top when there's a block of air 1 block above
 			// target block AND it's reachable straight,
 			// like this:       or this:  0
 			//             0             00
 			//            00             0#
 			//    ray ->  0#     ray ->  0#
 			// (0 = air, # = block)
 			
 			if (hit.sideHit != SideHit.BOTTOM && hit.sideHit != SideHit.TOP) {
 				if (BlockUtil.isReachableAirAbove(world, hit.sideHit, destX, destY, destZ, 1)) {
 					// Blink on top of that block
 					destY += 1;
 				} else if (BlockUtil.isReachableAirAbove(world, hit.sideHit, destX, destY, destZ, 2)) {
 					// ...or the one above it
 					destY += 2;
 				} else {
 					// There's no reachable air above, move back 1 block
 					switch (hit.sideHit) {
 					case SideHit.NORTH:
 						destX--;
 						break;
 					case SideHit.SOUTH:
 						destX++;
 						break;
 					case SideHit.EAST:
 						destZ--;
 						break;
 					case SideHit.WEST:
 						destZ++;
 						break;
 					}
 				}
 			} else {
 				switch (hit.sideHit) {
 				case SideHit.BOTTOM:
 					destY -= 2;
 					break;
 				case SideHit.TOP:
 					destY++;
 					break;
 				}
 			}
 			// Safeguard in case of an infinite loop.
 			int timesSubtracted = 0;
 			while (!blink(itemStack, world, player, destX, destY, destZ) &&
 					(double) timesSubtracted < maxDistance) {
 				// Something is obstructing the ray, trace a step back
 				hit.hitVec = hit.hitVec.addVector(-look.xCoord, -look.yCoord, -look.zCoord);
 				timesSubtracted ++;
 				destX = Math.round((float) hit.hitVec.xCoord);
 				destY = Math.round((float) hit.hitVec.yCoord);
 				destZ = Math.round((float) hit.hitVec.zCoord);
 			}
 		} else {
 			// Hit empty air
 			destX = Math.round((float) lookFar.xCoord);
 			destY = Math.round((float) lookFar.yCoord);
 			destZ = Math.round((float) lookFar.zCoord);
 			blink(itemStack, world, player, destX, destY, destZ);
 		}
 		return itemStack;
 	}
 	
 	/**
 	 * Blinks player to target coordinates, but first looks up then down for
 	 * empty space. Returns true if could blink without moving the player UP
 	 * from the given destination. Otherwise returns false and doesn't blink.
 	 */
 	private boolean blink(ItemStack itemStack, World world, EntityPlayer player, int x, int y, int z) {
 		Entity blinkingEntity;
 		if (player.ridingEntity != null) {
 			blinkingEntity = player.ridingEntity;
 		} else {
 			blinkingEntity = player;
 		}
 		// First of all, check if we are hanging in the air; if so, land.
 		Material material = world.getBlockMaterial(x, y-1, z);
 		boolean isUnderWater = blinkingEntity.isInsideOfMaterial(Material.water);
 		while (!(material.isSolid() || (!isUnderWater && material.isLiquid()))) {
 			y--;
 			material = world.getBlockMaterial(x, y-1, z);
 			if (y <= 0) {
 				// Reached minimum Y
 				return false;
 			}
 		}
 		double destX = (double) x + 0.5D;
 		double destY = (double) y + blinkingEntity.yOffset;
 		double destZ = (double) z + 0.5D;
 		
 		// Special care must be taken with fences which are 1.5 blocks high
 		int landingBlockId = world.getBlockId(x, y-1, z);
 		if (landingBlockId == Block.fence.blockID || landingBlockId == Block.fenceGate.blockID) {
 			destY += 0.5;
 		}
 		// Also slabs
 		if (landingBlockId == Block.stoneSingleSlab.blockID || landingBlockId == Block.woodSingleSlab.blockID) {
 			destY -= 0.5;
 		}
 		
 		// Keep previous coordinates
 		double srcX = blinkingEntity.posX;
 		double srcY = blinkingEntity.posY;
 		double srcZ = blinkingEntity.posZ;
 		
 		blinkingEntity.setPosition(destX, destY, destZ);
 		blinkingEntity.updateRiderPosition();
 		
 		// If colliding with something right now, return false immediately:
		if (!world.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty()) {
 			// Reset player position
 			blinkingEntity.setPosition(srcX, srcY, srcZ);
 			blinkingEntity.updateRiderPosition();
 			return false;
 		}
 		
 		//------------------------ Successful blink ------------------------
 		
 		if (!player.capabilities.isCreativeMode) {
 			Dota2Items.stats.getOrCreateEntityStats(player).removeMana(getManaCost());
 		}
 		blinkingEntity.motionX = 0;
 		blinkingEntity.motionY = 0;
 		blinkingEntity.motionZ = 0;
 		blinkingEntity.fallDistance = 0;
 
 		EffectInstance srcEffect = new EffectInstance(Effect.blink, srcX, srcY, srcZ);
 		EffectInstance destEffect = new EffectInstance(Effect.blink, destX, destY, destZ);
 		
 		if (!world.isRemote) {
 			startCooldown(itemStack, player);
 			// Server side. Play sounds and send packets about the player blinking.
 			// Play sound both at the initial position and the blink destination,
 			// if they're far apart enough.
 			double distance = player.getDistance(srcX, srcY, srcZ);
 			if (distance < 12) {
 				world.playSoundToNearExcept(player, Sound.BLINK_OUT.getName(), 1.0F, 1.0F);
 			} else {
 				// Sounds for other players to hear:
 				world.playSoundToNearExcept(player, Sound.BLINK_IN.getName(), 1.0F, 1.0F);
 				world.playSoundEffect(srcX, srcY, srcZ, Sound.BLINK_OUT.getName(), 1.0F, 1.0F);
 			}
 			// Send effect packets to other players
 			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
 			if (server != null) {
 				server.getConfigurationManager().sendToAllNearExcept(player, srcX, srcY, srcZ, 256, player.dimension, new EffectPacket(srcEffect).makePacket());
 				server.getConfigurationManager().sendToAllNearExcept(player, destX, destY, destZ, 256, player.dimension, new EffectPacket(destEffect).makePacket());
 			}
 		} else {
 			// Client side. Render blink effect.
 			Minecraft.getMinecraft().sndManager.playSoundFX(Sound.BLINK_OUT.getName(), 1.0F, 1.0F);
 			srcEffect.perform();
 			destEffect.perform();
 		}
 		
 		return true;
 	}
 }
