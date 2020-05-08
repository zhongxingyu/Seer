 /**
  * Copyright (C) 2014 Kolmogorov Alexey
  * 
  * This file part of ulmc.ru ModPack
  * 
  * ulmc.ru ModPack is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ulmc.ru ModPack is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see [http://www.gnu.org/licenses/].
  * 
  */
 package ru.ulmc.extender.tickhandler;
 
 import java.util.*;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import ru.ulmc.extender.UltimateExtender;
 import ru.ulmc.extender.gui.SurvivalGui;
 import ru.ulmc.extender.item.IWarmArmor;
 import ru.ulmc.extender.util.UDamageSource;
 import cpw.mods.fml.common.IScheduledTickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class WarmTickSheduler implements IScheduledTickHandler {
 
 	private int tickStep = 200;
 	private int underGroundPosition = 52;
 	private float underGroundBonus = 0.3f;
 	private float underGroundCoolBonus = 0.4f;
 	private float defaultArmorWarmness = 0.5f;
 	private float defaultArmorCoolness = 0.3f;
 	private float defaultNoneArmorCoolness = 0.5f;
 	private float inWaterPenalty = 0.5f;
 	private float waterNearBonus = 0.3f;
 	private float fireResistanceBonus = 0.5f;
 	private float nightPenalty = 0.2f;
 	private float dayPenalty = 0.3f;
 	private float stormPenalty = 0.3f;
 	private float rainPenalty = 0.15f;
 	private float torchBonus = 0.2f;
 	private float coldItemBonus = 0.2f;
 	private String handlerLabel = "ulmc SurviveSeason SheduledHandler";
 	private EnumSet<TickType> tick = EnumSet.of(TickType.PLAYER);
 	private static Map<BiomeGenBase, Float> biomeToCold = new HashMap<BiomeGenBase, Float>();
 	private static Map<BiomeGenBase, Float> biomeToHot = new HashMap<BiomeGenBase, Float>();
     private Timer timer = new Timer();
 
 	public final int BOOTS = 0; 
 	public final int PANTS = 1; 
 	public final int BODY = 2; 
 	public final int HAT = 3;
 
 
 	public WarmTickSheduler() {
 		super();
 
 		biomeToCold.put(BiomeGenBase.frozenOcean, 1.0f);
 		biomeToCold.put(BiomeGenBase.frozenRiver, 0.9f);
 		biomeToCold.put(BiomeGenBase.icePlains, 0.8f);
 		biomeToCold.put(BiomeGenBase.iceMountains, 1.2f);
 		biomeToCold.put(BiomeGenBase.taigaHills, 0.7f);
 		biomeToCold.put(BiomeGenBase.taiga, 0.7f);
 
 		biomeToHot.put(BiomeGenBase.hell, 0.8f);
 		biomeToHot.put(BiomeGenBase.desert, 0.5f);
 		biomeToHot.put(BiomeGenBase.desertHills, 0.5f);
 		biomeToHot.put(BiomeGenBase.jungle, 0.3f);
 		biomeToHot.put(BiomeGenBase.jungleHills, 0.3f);
 	}
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 		if (type.equals(tick)) {
 			doTheWork(tickData);
 		}
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 	}
 
 	private void doTheWork(Object... tickData) {
 		if (tickData[0] instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) tickData[0];
 
 			if (player.isBurning()) {
 				return;
 			}
 			
 			
 			World world = player.worldObj;
 			int currentX = MathHelper.floor_double(player.posX);
 			int currentZ = MathHelper.floor_double(player.posZ);
 			int currentY = MathHelper.floor_double(player.posY);
 			//if (!world.isRemote && player != null && !player.capabilities.isCreativeMode) {
 			if (player != null && !player.capabilities.isCreativeMode) {
 				BiomeGenBase biome = world.getBiomeGenForCoords(currentX, currentZ);
 
 				Float coldStrength = biomeToCold.get(biome);
 				Float warmnessDelta;
 				//UltimateExtender.logger.info("biome:" + biome.biomeName);
 				if (coldStrength != null && coldStrength > 0.0f) {
 					if (player.isInWater()) {
 						coldStrength += inWaterPenalty;
 					}
 
 					
 					if (player.posY < underGroundPosition || haveRoofOrSo(player, world)) {
 						coldStrength -= underGroundBonus;
 					} else {
 						if (world.getLightBrightness(currentX, currentY, currentZ) < 0.78F) {
 							coldStrength += nightPenalty;
 						}
 						if (world.isThundering()) {
 							coldStrength += stormPenalty;
 						} else if (world.isRaining()) {
 							coldStrength += rainPenalty;
 						}
 					}
 					if (player.getCurrentEquippedItem() != null
 							&& (player.getCurrentEquippedItem().itemID == Item.bucketLava.itemID || player
 									.getCurrentEquippedItem().itemID == Block.torchWood.blockID)) {
 						coldStrength -= torchBonus;
 					}
 					warmnessDelta  = getWarmness(player, coldStrength, HAT, true);
 					warmnessDelta += getWarmness(player, coldStrength, BODY, true);
 					warmnessDelta += getWarmness(player, coldStrength, PANTS, true);
 					warmnessDelta += getWarmness(player, coldStrength, BOOTS, true);
 					
 					warmnessDelta = (float)Math.rint(warmnessDelta * 100) / 100;
 					UltimateExtender.logger.info("warmnessDelta: " + warmnessDelta);
 					if (warmnessDelta < 0) {
 						boolean noWarmNear = !isBoxWarm(world,
 								currentX - 5, currentY - 4, currentZ - 5, 
 								currentX + 5, currentY + 3, currentZ + 5);
 
 						if (noWarmNear) {
							player.attackEntityFrom(UDamageSource.cold, -warmnessDelta);
                            timer.schedule(new FrostRenderTask(-warmnessDelta), 0, 50);
 						}
 					}
 				} else {
 					if(player.isImmuneToFire()) {return;}					
 					
 					Float hotStrength = biomeToHot.get(biome);
 					if (hotStrength != null && hotStrength > 0.0f) {
 						if (player.isInWater()) {
 							hotStrength -= inWaterPenalty;
 						}
 						
 						if (player.posY < underGroundPosition) {
 							hotStrength -= underGroundCoolBonus - nightPenalty;							
 						} else {
 							if (world.isThundering()) {
 								hotStrength -= stormPenalty;
 							} else if (world.isRaining()) {
 								hotStrength -= rainPenalty;
 							}
 							if(!world.isDaytime()) {
 								hotStrength -= nightPenalty;
 							} else if (world.getLightBrightness(currentX, currentY, currentZ) > 0.78F) {							
 								hotStrength += dayPenalty;
 							}
 						}
 						
 						if(player.isPotionActive(Potion.fireResistance)){
 							hotStrength -= fireResistanceBonus;
 						}
 						
 						if (player.getCurrentEquippedItem() != null
 								&& (player.getCurrentEquippedItem().itemID == Item.bucketWater.itemID ||
 									player.getCurrentEquippedItem().itemID == Item.snowball.itemID)) {
 							hotStrength -= coldItemBonus;
 						}						
 						boolean isWaterNear = isBoxHaveWater(world,
 								currentX - 3, currentY - 3, currentZ - 3, 
 								currentX + 3, currentY + 2, currentZ + 3);
 						
 						if(isWaterNear) {
 							hotStrength -= waterNearBonus;
 						}
 						
 						warmnessDelta = getWarmness(player, hotStrength, HAT, false);
 						warmnessDelta += getWarmness(player, hotStrength, BODY, false);
 						warmnessDelta += getWarmness(player, hotStrength, PANTS, false);
 						warmnessDelta += getWarmness(player, hotStrength, BOOTS, false);
 						warmnessDelta = (float)Math.rint(warmnessDelta * 100) / 100;
 						UltimateExtender.logger.info("warmnessDelta: " + warmnessDelta);
 						
 						if (warmnessDelta < 0) {
 							player.attackEntityFrom(UDamageSource.hot, -warmnessDelta);
                            timer.schedule(new HeatRenderTask(-warmnessDelta), 0, 50);
 						}
 					}
 				}
 
 			}
 		} else {
 			UltimateExtender.logger.log(Level.WARNING, "WinterTickSheduler.doTheWork() recived not expected argument!");
 		}
 	}
 
 	private float getWarmness(EntityPlayer player, float cold, int armorType, boolean isWinter) {
 		ItemStack armor = player.getCurrentArmor(armorType);
 		
 		if (armor != null && armor.getItem() instanceof IWarmArmor) {
 			
 			if(isWinter) {
 				if(((IWarmArmor) armor.getItem()).itWarms()) {
 					return ((IWarmArmor) armor.getItem()).getWarmLevel() - cold;
 				} else {
 					return ((IWarmArmor) armor.getItem()).getCoolLevel() + cold;
 				}				
 			} else {
 				if(((IWarmArmor) armor.getItem()).itCools()) {
 					return ((IWarmArmor) armor.getItem()).getCoolLevel() - cold;
 				} else {					
 					return ((IWarmArmor) armor.getItem()).getCoolLevel() - cold;					
 				}				
 			}
 			
 		} else if (armor != null) {
 			if(isWinter) {
 				return defaultArmorWarmness - cold;
 			} else {
 				return defaultArmorCoolness - cold;
 			}
 		} 
 		if(isWinter) {
 			return -cold;
 		} else {
 			if(armorType != HAT) {
 				return defaultNoneArmorCoolness - cold;
 			} else {
 				return -cold;
 			}
 		}				
 	}
 
 	private boolean haveRoofOrSo(EntityPlayer player, World world) {
 
 		int currentX = MathHelper.floor_double(player.posX);
 		int currentZ = MathHelper.floor_double(player.posZ);
 
 		int minY = MathHelper.floor_double(player.posY);
 		int maxY = MathHelper.floor_double(player.posY + 5.0D);
 
 		/* *
 		 * * * *
 		 * *
 		 */
 		for (int i = minY; minY < maxY; minY++) {
 			if (!world.isAirBlock(currentX, i, currentZ) && !world.isAirBlock(currentX + 2, i, currentZ)
 					&& !world.isAirBlock(currentX - 2, i, currentZ) && !world.isAirBlock(currentX, i, currentZ + 2)
 					&& !world.isAirBlock(currentX, i, currentZ - 2)) {
 				return true;
 			}
 
 		}
 
 		return false;
 	}
 
 	/**
 	 * Just like World's method, but can insert custom warm bloc id's
 	 *
 	 * @param world
 	 * @return
 	 */
 	private boolean isBoxWarm(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
 
 		if (world.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
 			for (int k1 = minX; k1 < maxX; ++k1) {
 				for (int l1 = minY; l1 < maxY; ++l1) {
 					for (int i2 = minZ; i2 < maxZ; ++i2) {
 						int j2 = world.getBlockId(k1, l1, i2);
 
 						if (j2 == Block.fire.blockID || j2 == Block.lavaMoving.blockID || j2 == Block.lavaStill.blockID) {
 							return true;
 						} else {
 							Block block = Block.blocksList[j2];
 							if (block != null && block.isBlockBurning(world, k1, l1, i2)) {
 								return true;
 							}
 						}
 					}
 				}
 			}
 		} else {
 			UltimateExtender.logger.log(Level.WARNING, "Chunk doesn't exist! ");
 		}
 
 		return false;
 	}
 
 	private boolean isBoxHaveWater(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
 
 		if (world.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
 			for (int k1 = minX; k1 < maxX; ++k1) {
 				for (int l1 = minY; l1 < maxY; ++l1) {
 					for (int i2 = minZ; i2 < maxZ; ++i2) {
 						int j2 = world.getBlockId(k1, l1, i2);
 
 						if (j2 == Block.waterStill.blockID || j2 == Block.waterMoving.blockID) {
 							return true;
 						} 
 					}
 				}
 			}
 		} else {
 			UltimateExtender.logger.log(Level.WARNING, "Chunk doesn't exist! ");
 		}
 
 		return false;
 	}
 	
 	@Override
 	public EnumSet<TickType> ticks() {
 		return tick;
 	}
 
 	@Override
 	public String getLabel() {
 
 		return handlerLabel;
 	}
 
 	@Override
 	public int nextTickSpacing() {
 		return tickStep;
 	}
 
     /**
      * Управляет задачей по рендеру эффекта.
      */
     private class FrostRenderTask extends TimerTask {
         private float power = 1.05F;
         public float powerStep = 0.01F;
 
         private FrostRenderTask(float power) {
             if(SurvivalGui.isDoRenderFrost() || SurvivalGui.isDoRenderHeat()) {
                 power = -0.1F;
             }
             if(power> 1.5F) {
                 this.power = 1.5F;
             } else {
                 this.power = power;
             }
         }
 
         @Override
         public void run() {
             power = power - powerStep;
             if(power >= 0.0F) {
                 SurvivalGui.setDoRenderFrost(true);
                 SurvivalGui.setPower(power);
             } else {
                 SurvivalGui.setDoRenderFrost(false);
                 this.cancel();
             }
         }
         public int getShedulePeriod() {
             return (int) (1.0F / powerStep * (250 *  powerStep));
         }
     }
     /**
      * Управляет задачей по рендеру эффекта.
      */
     private class HeatRenderTask extends TimerTask {
         private float power = 1.05F;
         public float powerStep = 0.01F;
 
         private HeatRenderTask(float power) {
             if(SurvivalGui.isDoRenderHeat() || SurvivalGui.isDoRenderFrost()) {
                 power = -0.1F;
             }
             if(power> 1.5F) {
                 this.power = 1.5F;
             } else {
                 this.power = power;
             }
         }
 
         @Override
         public void run() {
             power = power - powerStep;
             if(power >= 0.0F) {
                 SurvivalGui.setDoRenderHeat(true);
                 SurvivalGui.setPower(power);
             } else {
                 SurvivalGui.setDoRenderHeat(false);
                 this.cancel();
             }
         }
         public int getShedulePeriod() {
             return (int) (1.0F / powerStep * (250 *  powerStep));
         }
     }
 }
