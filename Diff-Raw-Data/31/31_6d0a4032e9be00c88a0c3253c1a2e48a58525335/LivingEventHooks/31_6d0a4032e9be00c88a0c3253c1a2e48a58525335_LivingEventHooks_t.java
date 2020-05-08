 package Booster;
 
 import java.io.DataOutputStream;
 
 import net.minecraft.client.entity.EntityPlayerSP;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class LivingEventHooks
 {
 	private int CanBoost = Booster.BoostPower;
 	private boolean boosterSwitch = Booster.BoosterDefaultSwitch;
 	private boolean toggle = false;
 	private boolean spawnCloud = false;
 	@ForgeSubscribe
 	public void LivingUpdate(LivingUpdateEvent event)
 	{
 		if(event.entityLiving != null && event.entityLiving instanceof EntityPlayer)
 		{
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 
 			if(player.worldObj.isRemote)
 			{
 				if(toggle && !BoosterKeyHandler.boosterKeydown)
 					boostKeyCheck(player);
 				toggle = BoosterKeyHandler.boosterKeydown;
 			}
 			boost(player, player.worldObj);
 		}
 	}
 	@SideOnly(Side.CLIENT)
 	public void boostKeyCheck(EntityPlayer player)
 	{
 		boosterSwitch =!boosterSwitch;
 //		boosterSwitch = BoosterKeyHandler.boosterKeydown;
 		String switchdata="";
 		if(boosterSwitch)
 		{
 			switchdata ="ON";
 		}
 		else
 		{
 			switchdata ="OFF";
 		}
 		player.addChatMessage("BoosterSwitch-" + switchdata);
 		PacketDispatcher.sendPacketToServer(PacketHandler.getPacket(this));
 	}
 	public void boost(EntityPlayer ep, World world)
 	{
 		if(ep.inventory.armorInventory[2]!=null || Booster.Alwaysflying)
 		{
 			if(!ep.onGround && boosterSwitch)
 			{
 				boolean canjump = false;
 				try {
 					canjump =(Integer)ModLoader.getPrivateValue(EntityLiving.class, ep, 88)==0;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				if(CanBoost > 0 || Booster.Alwaysflying)
 				{
 					if(world.isRemote)
 					{
 						EntityPlayerSP epsp = (EntityPlayerSP) ep;
 						this.spawnCloud = false;
 						if(Booster.Alwaysflying ||ep.inventory.armorInventory[2].itemID == Booster.BoosterID+1)
 						{
 							if(epsp.movementInput.moveForward > 0)
 							{
 								float f1 = ep.rotationYaw * 0.01745329F;
 								ep.motionY=ep.motionX=ep.motionZ =0;
 								ep.motionX -= MathHelper.sin(f1) * getmove();
 								ep.motionZ += MathHelper.cos(f1) * getmove();
 								this.spawnCloud = true;
 							}
 							else if(epsp.movementInput.moveForward < 0)
 							{
 								float f1 = ep.rotationYaw * 0.01745329F;
 								ep.motionY=ep.motionX=ep.motionZ =0;
 								ep.motionX -= MathHelper.sin(f1) * -getmove();
 								ep.motionZ += MathHelper.cos(f1) * -getmove();
 								this.spawnCloud = true;
 							}
 							else if(epsp.movementInput.moveStrafe < 0)
 							{
 								float f1 = ep.rotationYaw * 0.01745329F;
 								ep.motionY=ep.motionX=ep.motionZ =0;
 								ep.motionX -= MathHelper.sin(f1+(float)(Math.PI/2)) * getmove();
 								ep.motionZ += MathHelper.cos(f1+(float)(Math.PI/2)) * getmove();
 								this.spawnCloud = true;
 							}
 							else if(epsp.movementInput.moveStrafe > 0)
 							{
 								float f1 = ep.rotationYaw * 0.01745329F;
 								ep.motionY=ep.motionX=ep.motionZ =0;
 
 								ep.motionX -= MathHelper.sin(f1-(float)(Math.PI/2)) * getmove();
 								ep.motionZ += MathHelper.cos(f1-(float)(Math.PI/2)) * getmove();
 								this.spawnCloud = true;
 							}
 
 							else if(ep.isJumping && canjump)
 							{
 								ep.motionY=ep.motionX=ep.motionZ =0;
 								ep.motionY += getmove();
 								this.spawnCloud = true;
 							}
 							else if(ep.isSneaking())
 							{
 								ep.motionY=ep.motionX=ep.motionZ =0;
 								ep.motionY -= getmove();
 								this.spawnCloud = true;
 							}
 						}
 						else if(ep.inventory.armorInventory[2].itemID == Booster.BoosterID)
 						{
 							if(ep.isJumping == true && !ep.onGround && canjump)
 							{
 								ep.motionY=ep.motionX=ep.motionZ =0;
 								ep.motionY += getmove();
 								this.spawnCloud = true;
 							}
 						}
 						PacketDispatcher.sendPacketToServer(PacketHandler.getPacket(this));
 					}

 					if(this.spawnCloud)
 						commonprocess(ep,world);
 				}
 				else
 				{
 					world.spawnParticle("smoke", ep.posX, ep.posY + 0.1D, ep.posZ, 0.0D, 0.0D, 0.0D);
 				}
 			}
 			else if(boosterSwitch)
 			{
 				CanBoost = Booster.BoostPower;
 			}
			if((ep.inventory.armorInventory[2].itemID == Booster.BoosterID || ep.inventory.armorInventory[2].itemID == Booster.BoosterID + 1) && ep.isSneaking())
 			{
 				ep.fallDistance = 0F;
 			}
 		}
 	}
	private void commonprocess(EntityPlayer ep,World world)
 	{
 		world.spawnParticle("cloud", ep.posX, ep.posY + 0.1D, ep.posZ, 0.0D, 0.0D, 0.0D);
 		CanBoost--;
 		ep.fallDistance = 0F;
 	}
	private double getmove()
 	{
 		return Booster.movement * 0.5d;
 	}
  	public void readPacketData(ByteArrayDataInput data)
  	{
  		try
  		{
  			this.spawnCloud = data.readBoolean();
  			this.boosterSwitch = data.readBoolean();
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
  			dos.writeBoolean(this.spawnCloud);
  			dos.writeBoolean(boosterSwitch);
  		}
  		catch (Exception e)
  		{
  			e.printStackTrace();
  		}
  	}
 }
