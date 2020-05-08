 package assets.levelup;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.UUID;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCrops;
 import net.minecraft.block.BlockDirt;
 import net.minecraft.block.BlockGravel;
 import net.minecraft.block.BlockLog;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.BlockRedstoneOre;
 import net.minecraft.block.BlockStem;
 import net.minecraft.block.BlockStone;
 import net.minecraft.block.BlockWood;
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
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSpade;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.IExtendedEntityProperties;
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
 
 public class PlayerEventHandler implements ICraftingHandler,IPlayerTracker{
 	
 	public final static UUID speedID = UUID.randomUUID();
 	public final static UUID sneakID = UUID.randomUUID();
 	private static Map<String, int[]> deathNote = new HashMap();
 	@ForgeSubscribe
 	public void onPlayerConstruction(EntityEvent.EntityConstructing event)
 	{
 		if(event.entity instanceof EntityPlayer)
 		{
 			IExtendedEntityProperties skills = event.entity.getExtendedProperties(ClassBonus.SKILL_ID);
 			if(skills == null)
 			{
 				skills = new PlayerExtendedProperties();
 				event.entity.registerExtendedProperties(ClassBonus.SKILL_ID, skills);
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onBreak(PlayerEvent.BreakSpeed event)
 	{
         ItemStack itemstack = event.entityPlayer.getCurrentEquippedItem();
         if (itemstack != null)
 	        if (itemstack.getItem() instanceof ItemSpade)
 	        {
 	            if(event.block instanceof BlockDirt || event.block instanceof BlockGravel)
 	            {
 	            	event.newSpeed = event.originalSpeed*itemstack.getStrVsBlock(Block.dirt)/0.5F;
 	            }
 	        }
 	        else if (itemstack.getItem() instanceof ItemPickaxe && event.block instanceof BlockRedstoneOre)	
 	        {
 	            event.newSpeed = event.originalSpeed*itemstack.getStrVsBlock(Block.oreRedstone)/3F;
 	        }
         if (event.block instanceof BlockStone || event.block.blockID == Block.cobblestone.blockID 
         		|| event.block.blockID == Block.obsidian.blockID || (event.block instanceof BlockOre))
         {
         	event.newSpeed = event.originalSpeed+ (float)(getSkill(event.entityPlayer,0) / 5) * 0.2F;
         }
         else if (event.block instanceof BlockLog || event.block instanceof BlockWood)
         {
         	event.newSpeed = event.originalSpeed+ (float)(getSkill(event.entityPlayer,3) / 5) * 0.2F;
         }
 	}
 	
 	@ForgeSubscribe(receiveCanceled=true)
 	public void onInteract(PlayerInteractEvent event)
 	{
 		if(event.useItem != Event.Result.DENY && event.action==Action.RIGHT_CLICK_AIR && event.entityPlayer.fishEntity!=null)
 		{
 			EntityFishHook hook = event.entityPlayer.fishEntity;
 			int loot = getFishingLoot(event.entityPlayer);
 			if(loot>=0)
 			{
 				ItemStack stack = event.entityPlayer.inventory.getCurrentItem();
 				int i = stack.stackSize;
 		        int j = stack.getItemDamage();
 	            stack.damageItem(loot, event.entityPlayer);
 	            event.entityPlayer.swingItem();
 	        	event.entityPlayer.inventory.mainInventory[event.entityPlayer.inventory.currentItem] = stack;
 
 	            if (event.entityPlayer.capabilities.isCreativeMode)
 	            {
 	                stack.stackSize = i;
 
 	                if (stack.isItemStackDamageable())
 	                {
 	                    stack.setItemDamage(j);
 	                }
 	            }
 	            if (stack.stackSize == 0)
 	            {
 	            	event.entityPlayer.inventory.mainInventory[event.entityPlayer.inventory.currentItem] = null;
 	                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.entityPlayer, stack));
 	            }
	            if (!event.entityPlayer.isUsingItem() && event.entityPlayer instanceof EntityPlayerMP)
 	            {
 	                ((EntityPlayerMP)event.entityPlayer).sendContainerToPlayer(event.entityPlayer.inventoryContainer);
 	            }
 				event.useItem = Event.Result.DENY;
 				if(!hook.worldObj.isRemote)
 				{
 					EntityItem entityitem = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, lootList[loot]);
 	                double d5 = hook.angler.posX - hook.posX;
 	                double d6 = hook.angler.posY - hook.posY;
 	                double d7 = hook.angler.posZ - hook.posZ;
 	                double d8 = (double)MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);
 	                double d9 = 0.1D;
 	                entityitem.motionX = d5 * d9;
 	                entityitem.motionY = d6 * d9 + (double)MathHelper.sqrt_double(d8) * 0.08D;
 	                entityitem.motionZ = d7 * d9;
 	                hook.worldObj.spawnEntityInWorld(entityitem);
 	                hook.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(hook.angler.worldObj, hook.angler.posX, hook.angler.posY + 0.5D, hook.angler.posZ + 0.5D, new Random().nextInt(6) + 1));              
 				}
 			}
 		}
 		else if(event.action==Action.LEFT_CLICK_BLOCK && event.entityPlayer instanceof EntityPlayerMP)
 		{
 			World world = event.entityPlayer.worldObj;
 			Collection<BlockPosition> map = TickHandler.blockClicked;
 			BlockPosition pos = new BlockPosition(event.entityPlayer.entityId,world.provider.dimensionId,event.x,event.y,event.z,world.getBlockId(event.x,event.y,event.z),world.getBlockMetadata(event.x,event.y,event.z));
 			synchronized(map)
 			{
 				Iterator<BlockPosition> itr = map.iterator();
 				while(itr.hasNext())
 				{
 					BlockPosition block = itr.next();
 					if(block!=null && block.getData()[0]==event.entityPlayer.entityId)
 					{
 						if(!block.equals(pos))
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
 
 	public static int getFishingLoot(EntityPlayer player)
     {
         if (new Random().nextDouble() > (getSkill(player, 10) / 5) * 0.05D)
         {
             return -1;
         }
         else
         {
             return new Random().nextInt(lootList.length);
         }
     }
 	
 	private static ItemStack lootList[]= (new ItemStack[]
             {
             new ItemStack(Item.bone), new ItemStack(Item.reed), new ItemStack(Item.arrow), new ItemStack(Item.appleRed), new ItemStack(Item.bucketEmpty), new ItemStack(Item.boat), new ItemStack(Item.enderPearl), new ItemStack(Item.fishingRod), new ItemStack(Item.plateChain), new ItemStack(Item.ingotIron)
         });
 	
 	@ForgeSubscribe(receiveCanceled=true)
 	public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event)
 	{
 		if(event.entityLiving instanceof EntityPlayer)
 		{
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			if(!player.worldObj.isRemote && player.openContainer instanceof ContainerFurnace)
 			{
 				TileEntityFurnace furnace = ((ContainerFurnace)player.openContainer).furnace;
 				if(furnace!=null && furnace.isBurning())
 				{
 					if(furnace.canSmelt())
 					{
 						ItemStack stack = furnace.furnaceItemStacks[0];
 						if (stack!=null && furnace.furnaceCookTime < 199)
 						{
 							Random rand = new Random();
 							if (stack.getItem() instanceof ItemFood)
 			                {
 								int cook = getSkill(player, 7);
 								if(cook>10)
 									furnace.furnaceCookTime +=rand.nextInt(cook/10);
 			                }
 			                else
 			                {
 			                	int smelt = getSkill(player, 4);
 			                	if(smelt>10)
 			                		furnace.furnaceCookTime +=rand.nextInt(smelt/10);
 			                }
 						}
 						if(furnace.furnaceCookTime > 200)
 							furnace.furnaceCookTime = 199;
 					}
 				}
 			}
 			if(PlayerExtendedProperties.getPlayerClass(player)!=0 && PlayerExtendedProperties.getSkillPoints(player)<3*player.experienceLevel+8)
 			{
 				ClassBonus.addBonusToSkill(player, "XP", 3, true);
 			}
 			int skill = getSkill(player,9);
 			if(skill !=0 && new Random().nextFloat()<=skill/2500F)
 			{
 				growCropsAround(player.worldObj,(int) player.posX,(int) player.posY,(int) player.posZ, (int)skill/4);
 			}
 			AttributeInstance atinst = player.func_110148_a(SharedMonsterAttributes.field_111263_d);
 			AttributeModifier mod;
 			skill = getSkill(player,6);
 			if(skill!=0)
 			{
 				mod = new AttributeModifier(speedID,"SprintingSkillSpeed",skill/100F,2);
 				if(player.isSprinting())
 				{
 					if(atinst.func_111127_a(speedID) == null)
 					{
 						atinst.func_111121_a(mod);
 					}
 				}
 				else if(atinst.func_111127_a(speedID) != null)
 				{
 					atinst.func_111124_b(mod);
 				}
 				if(player.fallDistance>0)
 				{
 					player.fallDistance*=1-(int)(skill/5)/100F;
 				}
 			}
 			skill = getSkill(player,8);
 			if(skill!=0)
 			{
 				mod = new AttributeModifier(sneakID,"SneakingSkillSpeed",2*skill/100F,2);
 				if(player.isSneaking())
 				{
 					if(atinst.func_111127_a(sneakID) == null)
 					{
 						atinst.func_111121_a(mod);
 					}
 				}
 				else if(atinst.func_111127_a(sneakID) != null)
 				{
 					atinst.func_111124_b(mod);
 				}
 			}
 			
 		}
 	}
 	
 	private static void growCropsAround(World world, int posX, int posY, int posZ, int range) 
 	{
 		int dist = range/2+2;
 		for(int x=posX-dist;x<posX+dist+1;x++)
 			for(int z=posZ-dist;z<posZ+dist+1;z++)
 				for(int y=posY-dist;y<posY+dist+1;y++)
 				{
 					if(world.canBlockSeeTheSky(x, y, z))
 					{
 						Block block = Block.blocksList[world.getBlockId(x, y, z)];
 						if(block instanceof BlockCrops || block instanceof BlockStem)
 						{
 							int meta = world.getBlockMetadata(x, y, z);
 							if(meta<7)
 							{
 								world.setBlockMetadataWithNotify(x, y, z, meta+1, 2);
 							}
 						}
 						break;
 					}
 				}
 	}
 
 	@ForgeSubscribe
 	public void onDeath(LivingDeathEvent event)
 	{
 		if(event.entityLiving instanceof EntityPlayerMP)
 		{
 			deathNote.put(((EntityPlayer)event.entityLiving).username,PlayerExtendedProperties.getPlayerData((EntityPlayer)event.entityLiving,true));
 		}
 		else if(event.entityLiving instanceof EntityMob && event.source.getEntity() instanceof EntityPlayer)
 		{
 			giveBonusFightingXP((EntityPlayer) event.source.getEntity());
 		}
 	}
 	
 	public static void giveBonusFightingXP(EntityPlayer player)
     {
     	byte pClass = PlayerExtendedProperties.getPlayerClass(player);
         if (pClass == 2 || pClass == 5 || pClass == 8 || pClass == 11)
         {
             player.addExperience(2);
         }
     }
 	
 	public static int getSkill(EntityPlayer player, int id)
 	{
 		return PlayerExtendedProperties.getSkillFromIndex(player, id);
 	}
 	
 	@Override
 	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) 
 	{
 		LevelUp.takenFromCrafting(player, item, craftMatrix);
 	}
 
 	@Override
 	public void onSmelting(EntityPlayer player, ItemStack item) 
 	{
 		Random random = new Random();
 		if (item.getItem() instanceof ItemFood)
         {
             if (random.nextFloat() <= (float)getSkill(player,7) / 200F)
             {
                 item.stackSize++;
             }
         }
         else if (random.nextFloat() <= (float)getSkill(player, 4) / 200F)
         {
         	item.stackSize++;
         }
 	}
 
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		loadPlayer(player);
 	}
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) {
 		if(deathNote.containsKey(player.username))
 		{
 			PlayerExtendedProperties.setPlayerData(player, deathNote.get(player.username));
 			deathNote.remove(player.username);
 		}
 		loadPlayer(player);
 	}
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {
 		loadPlayer(player);
 	}
 	public static void loadPlayer(EntityPlayer player) {
 		byte cl = PlayerExtendedProperties.getPlayerClass(player);
 		int[] data = PlayerExtendedProperties.getPlayerData(player,false);
 		((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(SkillPacketHandler.getPacket("LEVELUPINIT", player.entityId, cl, data));
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {
 	}
 }
