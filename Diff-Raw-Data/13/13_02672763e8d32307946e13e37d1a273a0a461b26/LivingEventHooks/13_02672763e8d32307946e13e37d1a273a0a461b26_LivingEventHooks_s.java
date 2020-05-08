 package ak.EnchantChanger;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import net.minecraft.client.entity.EntityPlayerSP;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 
 public class LivingEventHooks
 {
 	private boolean allowLevitatiton = false;
 	private boolean isLevitation = false;
 	private int flyToggleTimer = 0;
 	private int sprintToggleTimer = 0;
 	private int FlightMptime=20*3;
 	private int GGMptime = 20*1;
 	private int AbsorpMptime = 20*3;
     private int[] Count= new int[]{0,0,0,0,0,0,0,0,0,0};
 	private int mptimer = this.FlightMptime;
 	@ForgeSubscribe
 	public void LivingUpdate(LivingUpdateEvent event)
 	{
 		if(event.entityLiving != null && event.entityLiving instanceof EntityPlayer)
 		{
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			this.Flight(player);
 			this.GreatGospel(player);
 			this.Absorption(player.worldObj, player);
 		}
 	}
 	@ForgeSubscribe
 	public void LivingDeath(LivingDeathEvent event)
 	{
 		DamageSource killer = event.source;
 		EntityLiving entity = event.entityLiving;
 		if(killer.getEntity() != null && killer.getEntity() instanceof EntityPlayer 
 				&& ((EntityPlayer)killer.getEntity()).getCurrentEquippedItem() != null
 				&& ((EntityPlayer)killer.getEntity()).getCurrentEquippedItem().isItemEnchanted()
 				&& !entity.worldObj.isRemote)
 		{
 			int exp = entity.experienceValue;
 			entity.worldObj.spawnEntityInWorld(new EcEntityApOrb(entity.worldObj, entity.posX,entity.posY, entity.posZ, exp / 2));
 		}
 	}
 	public void Flight(EntityPlayer player)
 	{
 		this.allowLevitatiton = this.checkFlightIteminInv(player) && !(player.capabilities.isCreativeMode || player.capabilities.allowFlying || (player.getFoodStats().getFoodLevel() < 0 && !EnchantChanger.YouAreTera));
 		if(!this.allowLevitatiton)
 		{
 			this.isLevitation = false;
 			return;
 		}
 		player.fallDistance = 0.0f;
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
 		if(side == Side.CLIENT)
 		{
 			boolean jump = ((EntityPlayerSP)player).movementInput.jump;
             float var2 = 0.8F;
             boolean var3 = ((EntityPlayerSP)player).movementInput.moveForward >= var2;
 			((EntityPlayerSP)player).movementInput.updatePlayerMoveState();
 			if (this.allowLevitatiton && !jump && ((EntityPlayerSP)player).movementInput.jump)
 			{
 				if (this.flyToggleTimer == 0)
 				{
 					this.flyToggleTimer = 7;
 				}
 				else
 				{
 					this.isLevitation = !this.isLevitation;
 					this.flyToggleTimer = 0;
 				}
 			}
 			boolean var4 = (float)((EntityPlayerSP)player).getFoodStats().getFoodLevel() > 6.0F;
 			if (((EntityPlayerSP)player).onGround && !var3 && ((EntityPlayerSP)player).movementInput.moveForward >= var2 && !((EntityPlayerSP)player).isSprinting() && var4 && !((EntityPlayerSP)player).isUsingItem() && !((EntityPlayerSP)player).isPotionActive(Potion.blindness))
 			{
 				if (this.sprintToggleTimer == 0)
 				{
 					this.sprintToggleTimer = 7;
 				}
 				else
 				{
 					((EntityPlayerSP)player).setSprinting(true);
 					this.sprintToggleTimer = 0;
 				}
 			}
 			if (this.sprintToggleTimer > 0)
 			{
 				--this.sprintToggleTimer;
 			}
 			if (this.flyToggleTimer > 0)
 			{
 				--this.flyToggleTimer;
 			}
 			if (player.onGround && this.isLevitation)
 			{
 				this.isLevitation = false;
 			}
 			if (this.isLevitation)
 			{
 				player.motionY = 0D;
 				player.jumpMovementFactor = 0.1f;
 				if (((EntityPlayerSP)player).movementInput.sneak)
 				{
 					player.motionY -= 0.4D;
 				}
 
 				if (((EntityPlayerSP)player).movementInput.jump)
 				{
 					player.motionY += 0.4D;
 				}
 
 			}
 			else
 				player.jumpMovementFactor = 0.02f;
 	    	if (player.onGround && this.isLevitation)
 	    	{
 	    		this.isLevitation = false;
 	    	}
 	    	PacketDispatcher.sendPacketToServer(Packet_EnchantChanger.getPacketLevi(this));
 		}
 		if(this.isLevitation)
 			if(this.mptimer ==0)
 			{
 				this.mptimer = this.FlightMptime;
 				player.getFoodStats().addStats(-1, 1.0F);
 			}
 			else
 				--this.mptimer;
 	}
 	public void GreatGospel(EntityPlayer player)
 	{
 		if(player.capabilities.isCreativeMode)
 		{
 			return;
 		}
 		if((player.getFoodStats().getFoodLevel() < 0 && !EnchantChanger.YouAreTera) || !EcItemMateria.GGEnable)
 		{
 			player.capabilities.disableDamage = false;
 			return;
 		}
 		ItemStack playerItem = player.getCurrentEquippedItem();
 		if(playerItem !=null && playerItem.getItem() instanceof EcItemMateria && playerItem.getItemDamage() == 2)
 		{
 			player.capabilities.disableDamage = true;
 			if(MpCount(1,GGMptime))
 				player.getFoodStats().addStats(-1, 1.0f);
 		}
 		else
 		{
 			player.capabilities.disableDamage = false;
 		}
 	}
 	public void Absorption(World world,EntityPlayer player)
 	{
 		if(player.getFoodStats().getFoodLevel() < 20)
 		{
 			if(!MpCount(3,AbsorpMptime))
 			{
 				return;
 			}
 			ItemStack playerItem = player.getCurrentEquippedItem();
 			if(playerItem !=null && playerItem.getItem() instanceof EcItemMateria && playerItem.getItemDamage() == 8)
 			{
 				List EntityList = world.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.expand(EnchantChanger.AbsorpBoxSize, EnchantChanger.AbsorpBoxSize, EnchantChanger.AbsorpBoxSize));
 				for (int i=0; i < EntityList.size();i++)
 				{
 					Entity entity=(Entity) EntityList.get(i);
 					if(entity instanceof EntityLiving)
 					{
 						((EntityLiving) entity).attackEntityFrom(DamageSource.generic, 1);
 						player.getFoodStats().addStats(1, 1.0f);
 					}
 				}
 			}
 		}
 	}
 	public boolean MpCount(int par1, int par2)
 	{
 		Count[par1]++;
 		if(Count[par1] > par2)
 		{
 			Count[par1] =0;
 			//System.out.println("MPDecrease");
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
     public static boolean checkFlightItem(ItemStack itemstack)
 	{
 		if(itemstack == null)
 		{
 			return false;
 		}
 		else if(itemstack.getItem() instanceof EcItemMateria || itemstack.getItem() instanceof EcItemSword)
 		{
 			if(itemstack.getItem() instanceof EcItemMateria)
 			{
 				if(itemstack.getItemDamage() == 4)
 				{
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 			}
 			else
 			{
 				return EcItemSword.hasFloat(itemstack);
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 	public void checkMagic(World world, EntityPlayer player)
 	{
 		ItemStack itemstack = player.getHeldItem();
 		if(itemstack != null && itemstack.getItem() instanceof EcItemSword)
 		{
 			EcItemSword.doMagic(itemstack, world, player);
 		}
 	}
     public static boolean checkFlightIteminInv(EntityPlayer entityplayer)
 	{
 		boolean ret=false;
     	for(int i=0;i<9;i++)
 		{
 			ItemStack var1 = entityplayer.inventory.getStackInSlot(i);
 			if( checkFlightItem(var1))
 				ret=checkFlightItem(var1);
 		}
 		return ret;
 	}
     public void sendLevitation()
     {
     	ByteArrayOutputStream bos = new ByteArrayOutputStream();
     	DataOutputStream dos = new DataOutputStream(bos);
     	try {
     		dos.writeBoolean(isLevitation);
     		PacketDispatcher.sendPacketToServer(new Packet250CustomPayload("EC|Levi", bos.toByteArray()));
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
     }
  	public void readPacketData(ByteArrayDataInput data)
  	{
  		try
  		{
  			this.isLevitation = data.readBoolean();
  		}
  		catch (Exception e)
  		{
  			e.printStackTrace();
  		}
  	}
  	public void writePacketData(DataOutputStream dos)
  	{
  		try
  		{
  			dos.writeBoolean(this.isLevitation);
  		}
  		catch (Exception e)
  		{
  			e.printStackTrace();
  		}
  	}
 }
