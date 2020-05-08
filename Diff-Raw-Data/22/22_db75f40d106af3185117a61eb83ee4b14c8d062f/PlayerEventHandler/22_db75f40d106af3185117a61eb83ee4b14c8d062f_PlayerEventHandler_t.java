 package assets.levelup;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.UUID;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockDirt;
 import net.minecraft.block.BlockGravel;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.BlockRedstoneOre;
 import net.minecraft.block.BlockStone;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.entity.ai.attributes.AttributeModifier;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.item.EntityXPOrb;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.projectile.EntityFishHook;
 import net.minecraft.inventory.ContainerFurnace;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSpade;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IExtendedEntityProperties;
 import net.minecraftforge.common.IPlantable;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.Event;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingEvent;
 import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
 import cpw.mods.fml.common.ICraftingHandler;
 import cpw.mods.fml.common.IPlayerTracker;
 
 public class PlayerEventHandler implements ICraftingHandler, IPlayerTracker {
 	public static int xpPerLevel = 3;
 	public final static UUID speedID = UUID.randomUUID();
 	public final static UUID sneakID = UUID.randomUUID();
 	private static Map<String, int[]> deathNote = new HashMap<String, int[]>();
 	private static ItemStack lootList[] = (new ItemStack[] { new ItemStack(Item.bone), new ItemStack(Item.reed), new ItemStack(Item.arrow), new ItemStack(Item.appleRed),
 			new ItemStack(Item.bucketEmpty), new ItemStack(Item.boat), new ItemStack(Item.enderPearl), new ItemStack(Item.fishingRod), new ItemStack(Item.plateChain), new ItemStack(Item.ingotIron) });
 
 	public PlayerEventHandler(){
 		ContainerFurnace.class.getDeclaredFields()[0].setAccessible(true);
 		TileEntityFurnace.class.getDeclaredMethods()[15].setAccessible(true);
 		TileEntityFurnace.class.getDeclaredFields()[3].setAccessible(true);
 	}
 	
 	@ForgeSubscribe
 	public void onBreak(PlayerEvent.BreakSpeed event) {
 		ItemStack itemstack = event.entityPlayer.getCurrentEquippedItem();
 		if (itemstack != null)
 			if (itemstack.getItem() instanceof ItemSpade) {
 				if (event.block instanceof BlockDirt || event.block instanceof BlockGravel) {
 					event.newSpeed = event.originalSpeed * itemstack.getStrVsBlock(Block.dirt) / 0.5F;
 				}
 			} else if (itemstack.getItem() instanceof ItemPickaxe && event.block instanceof BlockRedstoneOre) {
 				event.newSpeed = event.originalSpeed * itemstack.getStrVsBlock(Block.oreRedstone) / 3F;
 			}
 		if (event.block instanceof BlockStone || event.block.blockID == Block.cobblestone.blockID || event.block.blockID == Block.obsidian.blockID || (event.block instanceof BlockOre)) {
 			event.newSpeed = event.originalSpeed + getSkill(event.entityPlayer, 0) / 5 * 0.2F;
 		} else if (event.block.blockMaterial == Material.wood) {
 			event.newSpeed = event.originalSpeed + getSkill(event.entityPlayer, 3) / 5 * 0.2F;
 		}
 	}
 
 	@Override
 	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) {
 		LevelUp.takenFromCrafting(player, item, craftMatrix);
 	}
 
 	@ForgeSubscribe
 	public void onDeath(LivingDeathEvent event) {
 		if (event.entityLiving instanceof EntityPlayerMP) {
 			deathNote.put(((EntityPlayer) event.entityLiving).username, PlayerExtendedProperties.getPlayerData((EntityPlayer) event.entityLiving, true));
 		} else if (event.entityLiving instanceof EntityMob && event.source.getEntity() instanceof EntityPlayer) {
 			giveBonusFightingXP((EntityPlayer) event.source.getEntity());
 		}
 	}
 
 	@ForgeSubscribe(receiveCanceled = true)
 	public void onInteract(PlayerInteractEvent event) {
 		if (event.useItem != Event.Result.DENY && event.action == Action.RIGHT_CLICK_AIR && event.entityPlayer.fishEntity != null) {
 			EntityFishHook hook = event.entityPlayer.fishEntity;
 			int loot = getFishingLoot(event.entityPlayer);
 			if (loot >= 0) {
 				ItemStack stack = event.entityPlayer.inventory.getCurrentItem();
 				int i = stack.stackSize;
 				int j = stack.getItemDamage();
 				stack.damageItem(loot, event.entityPlayer);
 				event.entityPlayer.swingItem();
 				event.entityPlayer.inventory.mainInventory[event.entityPlayer.inventory.currentItem] = stack;
 				if (event.entityPlayer.capabilities.isCreativeMode) {
 					stack.stackSize = i;
 					if (stack.isItemStackDamageable()) {
 						stack.setItemDamage(j);
 					}
 				}
 				if (stack.stackSize == 0) {
 					event.entityPlayer.inventory.mainInventory[event.entityPlayer.inventory.currentItem] = null;
 					MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.entityPlayer, stack));
 				}
 				if (!event.entityPlayer.isUsingItem() && event.entityPlayer instanceof EntityPlayerMP) {
 					((EntityPlayerMP) event.entityPlayer).sendContainerToPlayer(event.entityPlayer.inventoryContainer);
 				}
 				event.useItem = Event.Result.DENY;
 				if (!hook.worldObj.isRemote) {
 					EntityItem entityitem = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, lootList[loot]);
 					double d5 = hook.angler.posX - hook.posX;
 					double d6 = hook.angler.posY - hook.posY;
 					double d7 = hook.angler.posZ - hook.posZ;
 					double d8 = MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);
 					double d9 = 0.1D;
 					entityitem.motionX = d5 * d9;
 					entityitem.motionY = d6 * d9 + MathHelper.sqrt_double(d8) * 0.08D;
 					entityitem.motionZ = d7 * d9;
 					hook.worldObj.spawnEntityInWorld(entityitem);
 					hook.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(hook.angler.worldObj, hook.angler.posX, hook.angler.posY + 0.5D, hook.angler.posZ + 0.5D, new Random().nextInt(6) + 1));
 				}
 			}
 		} else if (event.action == Action.LEFT_CLICK_BLOCK && event.entityPlayer instanceof EntityPlayerMP) {
 			World world = event.entityPlayer.worldObj;
 			Collection<BlockPosition> map = TickHandler.blockClicked;
 			BlockPosition pos = new BlockPosition(event.entityPlayer.entityId, world.provider.dimensionId, event.x, event.y, event.z, world.getBlockId(event.x, event.y, event.z),
 					world.getBlockMetadata(event.x, event.y, event.z));
 			synchronized (map) {
 				Iterator<BlockPosition> itr = map.iterator();
 				while (itr.hasNext()) {
 					BlockPosition block = itr.next();
 					if (block != null && block.getData()[0] == event.entityPlayer.entityId) {
 						if (!block.equals(pos))
 							itr.remove();
 						else
 							return;
 						break;
 					}
 				}
 				map.add(pos);
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {
 		loadPlayer(player);
 	}
 
 	@ForgeSubscribe
 	public void onPlayerConstruction(EntityEvent.EntityConstructing event) {
 		if (event.entity instanceof EntityPlayer) {
 			IExtendedEntityProperties skills = event.entity.getExtendedProperties(ClassBonus.SKILL_ID);
 			if (skills == null) {
 				skills = new PlayerExtendedProperties();
 				event.entity.registerExtendedProperties(ClassBonus.SKILL_ID, skills);
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		loadPlayer(player);
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {
 	}
 
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) {
 		if (deathNote.containsKey(player.username)) {
 			PlayerExtendedProperties.setPlayerData(player, deathNote.get(player.username));
 			deathNote.remove(player.username);
 		}
 		loadPlayer(player);
 	}
 
 	@ForgeSubscribe(receiveCanceled = true)
 	public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
 		if (event.entityLiving instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			try{
 				if (!player.worldObj.isRemote && player.openContainer instanceof ContainerFurnace) {
 					TileEntityFurnace furnace = 
							(TileEntityFurnace) ContainerFurnace.class.getDeclaredFields()[0].get(player.openContainer);
 					if (furnace != null && furnace.isBurning()) {
						if (Boolean.class.cast(TileEntityFurnace.class.getDeclaredMethods()[15].invoke(furnace)).booleanValue()) {
 							ItemStack stack = ItemStack[].class.cast(TileEntityFurnace.class.getDeclaredFields()[3].get(furnace))[0];
 							if (stack != null && furnace.furnaceCookTime < 199) {
 								Random rand = new Random();
 								if (stack.getItem().getItemUseAction(stack) == EnumAction.eat) {
 									int cook = getSkill(player, 7);
 									if (cook > 10)
 										furnace.furnaceCookTime += rand.nextInt(cook / 10);
 								} else {
 									int smelt = getSkill(player, 4);
 									if (smelt > 10)
 										furnace.furnaceCookTime += rand.nextInt(smelt / 10);
 								}
 							}
 							if (furnace.furnaceCookTime > 200)
 								furnace.furnaceCookTime = 199;
 						}
 					}
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			if (PlayerExtendedProperties.getPlayerClass(player) != 0 && PlayerExtendedProperties.getSkillPoints(player) < xpPerLevel * (player.experienceLevel - 4) + ClassBonus.bonusPoints) {
 				ClassBonus.addBonusToSkill(player, "XP", xpPerLevel, true);
 			}
 			int skill = getSkill(player, 9);
 			if (skill != 0 && new Random().nextFloat() <= skill / 2500F) {
 				growCropsAround(player.worldObj, skill / 4, player);
 			}
 			AttributeInstance atinst = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
 			AttributeModifier mod;
 			skill = getSkill(player, 6);
 			if (skill != 0) {
 				mod = new AttributeModifier(speedID, "SprintingSkillSpeed", skill / 100F, 2);
 				if (player.isSprinting()) {
 					if (atinst.getModifier(speedID) == null) {
 						atinst.applyModifier(mod);
 					}
 				} else if (atinst.getModifier(speedID) != null) {
 					atinst.removeModifier(mod);
 				}
 				if (player.fallDistance > 0) {
 					player.fallDistance *= 1 - skill / 5 / 100F;
 				}
 			}
 			skill = getSkill(player, 8);
 			if (skill != 0) {
 				mod = new AttributeModifier(sneakID, "SneakingSkillSpeed", 2 * skill / 100F, 2);
 				if (player.isSneaking()) {
 					if (atinst.getModifier(sneakID) == null) {
 						atinst.applyModifier(mod);
 					}
 				} else if (atinst.getModifier(sneakID) != null) {
 					atinst.removeModifier(mod);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onSmelting(EntityPlayer player, ItemStack item) {
 		Random random = new Random();
 		if (item.getItem().getItemUseAction(item) == EnumAction.eat) {
 			if (random.nextFloat() <= getSkill(player, 7) / 200F) {
 				item.stackSize += 1;
 			}
 		} else if (random.nextFloat() <= getSkill(player, 4) / 200F) {
 			item.stackSize += 1;
 		}
 	}
 
 	public static int getFishingLoot(EntityPlayer player) {
 		if (new Random().nextDouble() > (getSkill(player, 10) / 5) * 0.05D) {
 			return -1;
 		} else {
 			return new Random().nextInt(lootList.length);
 		}
 	}
 
 	public static int getSkill(EntityPlayer player, int id) {
 		return PlayerExtendedProperties.getSkillFromIndex(player, id);
 	}
 
 	public static void giveBonusFightingXP(EntityPlayer player) {
 		byte pClass = PlayerExtendedProperties.getPlayerClass(player);
 		if (pClass == 2 || pClass == 5 || pClass == 8 || pClass == 11) {
 			player.addExperience(2);
 		}
 	}
 
 	public static void loadPlayer(EntityPlayer player) {
 		byte cl = PlayerExtendedProperties.getPlayerClass(player);
 		int[] data = PlayerExtendedProperties.getPlayerData(player, false);
 		((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(SkillPacketHandler.getPacket("LEVELUPINIT", player.entityId, cl, data));
 	}
 
 	private static void growCropsAround(World world, int range, EntityPlayer player) {
 		int posX = (int) player.posX;
 		int posY = (int) player.posY;
 		int posZ = (int) player.posZ;
 		int dist = range / 2 + 2;
 		for (int x = posX - dist; x < posX + dist + 1; x++) {
 			for (int z = posZ - dist; z < posZ + dist + 1; z++) {
 				for (int y = posY - dist; y < posY + dist + 1; y++) {
 					if (world.isAirBlock(x, y + 1, z)) {
 						Block block = Block.blocksList[world.getBlockId(x, y, z)];
 						if (block instanceof IPlantable) {
 							Block soil = Block.blocksList[world.getBlockId(x, y - 1, z)];
 							if (soil != null && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (IPlantable) block)) {
 								ItemDye.applyBonemeal(new ItemStack(Item.dyePowder), world, x, y, z, player);
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 	}
 }
