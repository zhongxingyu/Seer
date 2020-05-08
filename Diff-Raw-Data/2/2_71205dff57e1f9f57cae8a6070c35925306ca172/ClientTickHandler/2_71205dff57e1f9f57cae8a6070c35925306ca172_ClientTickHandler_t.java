 package assets.fyresmodjam;
 
 import java.awt.Color;
 import java.util.EnumSet;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class ClientTickHandler implements ITickHandler {
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 		
 	}
 	
 	@Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
         if (type.equals(EnumSet.of(TickType.CLIENT))) {
             GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
 
             if (guiscreen != null) {
                 onTickInGUI(guiscreen);
             } else {
                 onTickInGame();
             }
 
             onClientTick();
         } else if (type.equals(EnumSet.of(TickType.RENDER))) {onRenderTick();}
     }
 	
 	private void onRenderTick() {
 		MovingObjectPosition mouse = Minecraft.getMinecraft().objectMouseOver;
 		
		if(mouse != null && Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().isGuiEnabled() && Minecraft.getMinecraft().theWorld != null && mouse.typeOfHit == EnumMovingObjectType.TILE && Minecraft.getMinecraft().theWorld.getBlockId(mouse.blockX, mouse.blockY, mouse.blockZ) == ModjamMod.blockPillar.blockID) {
 	        ScaledResolution scaledRes = new ScaledResolution(Minecraft.getMinecraft().gameSettings, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
 	        int screenWidth = scaledRes.getScaledWidth();
 	        int screenHeight = scaledRes.getScaledHeight();
 	        
 	        String string = "Press x to Examine";
 	        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(string, (screenWidth / 2) - (Minecraft.getMinecraft().fontRenderer.getStringWidth(string) / 2), screenHeight / 2 + 16, Color.WHITE.getRGB());
 		}
 	}
 
 	private void onClientTick() {
 		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
 		
 		if(player != null) {
 			if(player.openContainer != null) {
 				boolean sendPacket = false;
 				
 				for(Object object : player.inventory.mainInventory) {
 					if(object == null || !(object instanceof ItemStack)) {continue;}
 					
 					ItemStack stack = (ItemStack) object;
 					if(stack.getTagCompound() == null || !stack.getTagCompound().hasKey("processed") || stack.getTagCompound().getString("processed").equals("false")) {
 						sendPacket = true;
 					}
 				}
 				
 				if(sendPacket) {PacketDispatcher.sendPacketToServer(PacketHandler.newPacket(PacketHandler.UPDATE_PLAYER_ITEMS));}
 			}
 			
 			player.triggerAchievement(ModjamMod.startTheGame);
 		}
 	}
 
 	private void onTickInGame() {
 		
 	}
 
 	private void onTickInGUI(GuiScreen guiscreen) {
 		
 	}
 
 	@Override
     public EnumSet<TickType> ticks() {
         return EnumSet.of(TickType.RENDER, TickType.CLIENT);
     }
 	
 	@Override
 	public String getLabel() {
 		return "FyresModJamClientTicker";
 	}
 	
 }
