 package Abascus.DLCCraft.common.Client;
 
 import java.util.EnumSet;
import java.util.List;

import Abascus.DLCCraft.common.DLCCraft;
import Abascus.DLCCraft.common.DLCManager;
 
 import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundPool;
 import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.ImageBufferDownload;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
 import net.minecraft.util.EnumChatFormatting;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class TickHandlerClient implements ITickHandler
 {
 
 	public TickHandlerClient()
 	{
 	}
 
 	private boolean nagged = false;
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData)
 	{
 		EntityPlayer player = (EntityPlayer)tickData[0];
 		if(type.equals(EnumSet.of(TickType.PLAYER)))
 		{
 			DLCManager dlcs = DLCCraft.playerTracker.getPlayerDLCStats(player).dlcManager;
 			if(dlcs.getState("inventory") != 2)
 			{
 				if(Minecraft.getMinecraft().currentScreen instanceof GuiInventory)
 				{
 					player.closeScreen();
 				}
 			}
 			
			if(dlcs.getState("Sounds") != 2)
 			{
				Minecraft.getMinecraft().sndManager.stopAllSounds();
 			}
 		}
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData)
 	{
 		if(!nagged && DLCCraft.instance.startUpInfo)
 		{
 
 			EntityPlayer player = (EntityPlayer) tickData[0];
 			String[] s = DLCCraft.instance.Msg;
 			for(int i = 0; s.length > i; i++)
 			{
 				player.addChatMessage(EnumChatFormatting.BOLD+s[i]);
 			}
 
 			nagged = true;
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks()
 	{
 		return EnumSet.of(TickType.PLAYER);
 	}
 
 	@Override
 	public String getLabel()
 	{
 		return "DLC Craft - Player update tick - Client";
 	}
 
 }
