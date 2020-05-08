 package mods.scourgecraft.potion;
 
 import java.util.List;
 
 import mods.scourgecraft.ScourgeCraftCore;
 import mods.scourgecraft.core.ArmorHandler;
 import mods.scourgecraft.core.ArmorSet;
 import mods.scourgecraft.core.BuffHandler;
 import mods.scourgecraft.core.armorbuffs.BuffVenomSet;
 import net.minecraft.block.Block;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 
 public class PotionEffectHandler implements IPotionEffectHandler
 {
 	private static boolean hasJumped = false;
 	private float tick = 0;
 
 	public void onPotionUpdate(EntityLiving living, PotionEffect effect)
 	{
 		if (effect.getPotionID() == Potion.poison.id) 
 		{
 			if (living instanceof EntityPlayer)
 			{
 				EntityPlayer ep = (EntityPlayer)living;
 				if (ep.isPotionActive(ScourgeCraftCore.configPotion.poisonResist.id))
 				{
 					ScourgeCraftCore.potionHandler.removeEffectQueue.add(ScourgeCraftCore.configPotion.poisonResist.id);
 					ScourgeCraftCore.potionHandler.removeEffectQueue.add(Potion.poison.id);
 					ep.sendChatToPlayer("ScourgeCraft : Your armor has eaten your poison!");
 				}
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.fireLegs.id)
 		{
 			int x = (int) Math.floor(living.posX);
 			int y = (int) (living.posY - living.getYOffset());
 			int z = (int) Math.floor(living.posZ);
 			int id = living.worldObj.getBlockId(x, y - 1, z);
 			if (Block.blocksList[id] != null && Block.blocksList[id].isBlockSolidOnSide(living.worldObj, x, y - 1, z, ForgeDirection.UP) && living.worldObj.getBlockId(x, y, z) == 0)
 			{
 				living.worldObj.setBlock(x, y, z, Block.fire.blockID);
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.aquaLegs.id)
 		{
 			int x = (int) Math.floor(living.posX);
 			int y = (int) (living.posY - living.getYOffset());
 			int z = (int) Math.floor(living.posZ);
 			
 			if ((living.worldObj.getBlockId(x, y - 1, z) == Block.waterStill.blockID || living.worldObj.getBlockId(x, y - 1, z) == Block.waterMoving.blockID) && living.worldObj.getBlockId(x, y, z) == 0)
 			{
 				if (living.motionY < 0 && living.boundingBox.minY < y)
 				{
 						living.motionY = 0;
 						living.fallDistance = 0;
 						living.onGround = true;
 						ScourgeCraftCore.potionHandler.addEffectQueue.add(new PotionEffect(Potion.moveSpeed.id, 20 * 5, 0));
 						if (living.isSneaking())
 						{
 							living.motionY -= 0.1F;
 						}
 				}
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.lavaLegs.id)
 		{
 			int x = (int) Math.floor(living.posX);
 			int y = (int) (living.posY - living.getYOffset());
 			int z = (int) Math.floor(living.posZ);
 			
 			if ((living.worldObj.getBlockId(x, y - 1, z) == Block.lavaMoving.blockID || living.worldObj.getBlockId(x, y - 1, z) == Block.lavaStill.blockID) && living.worldObj.getBlockId(x, y, z) == 0)
 			{
 				if (living.motionY < 0 && living.boundingBox.minY < y)
 				{
 						living.motionY = 0;
 						living.fallDistance = 0;
 						living.onGround = true;
 						ScourgeCraftCore.potionHandler.addEffectQueue.add(new PotionEffect(Potion.moveSpeed.id, 20 * 5, 0));
 						if (living.isSneaking())
 						{
 							living.motionY -= 0.1F;
 						}
 				}
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.antiHunger.id)
 		{
 			if (living instanceof EntityPlayer && ((int)tick) % 40 == 0) //True every 2 seconds
 			{
 				((EntityPlayer)living).getFoodStats().addStats(1, 0.1F);
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.nausea.id)
 		{
 			if (living instanceof EntityPlayer & ((int)tick) % 20 == 0) //True every 1 seconds
 			{
 				EntityPlayer ep = (EntityPlayer)living;
 				if (ep.isPotionActive(ScourgeCraftCore.configPotion.nauseaResist.id))
 				{
 					ScourgeCraftCore.potionHandler.removeEffectQueue.add(ScourgeCraftCore.configPotion.nausea.id);
 					ep.sendChatToPlayer("ScourgeCraft : Your armor has eaten your Nausea!");
 					return;
 				}
 				ep.getFoodStats().addStats(-1, -0.1F);
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.aquaAura.id)
 		{
 			ScourgeCraftCore.potionHandler.addEffectQueue.add(new PotionEffect(Potion.moveSlowdown.id, 20 * 3, 1));
 		}
 		else if (effect.getPotionID() == Potion.weakness.id)
 		{
			if (living instanceof EntityPlayer)
 			{
				EntityPlayer ep = (EntityPlayer)living;
				if (ep.isPotionActive(ScourgeCraftCore.configPotion.weaknessResist.id))
				{
					ScourgeCraftCore.potionHandler.removeEffectQueue.add(Potion.weakness.id);
					ep.sendChatToPlayer("ScourgeCraft : Your armor has eaten your Weakness!");
				}
 			}
 		}
 		else if (effect.getPotionID() == ScourgeCraftCore.configPotion.stepUp.id)
 		{
 			living.landMovementFactor = 0.15F;
 			living.jumpMovementFactor = living.landMovementFactor * 0.5F;
 
 			living.stepHeight = 1.003F;
 		}
 	}
 
 	@Override
 	public boolean canHandle(PotionEffect effect) {
 		return true;
 	}
 }
