 package demonmodders.Crymod.Client;
 
 import java.util.EnumSet;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Gui;
 import net.minecraft.src.ScaledResolution;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 import demonmodders.Crymod.Common.PlayerKarma;
 import demonmodders.Crymod.Common.PlayerKarmaManager;
 
 public class ClientTickHandler extends Gui implements ITickHandler {
 
 	private ClientTickHandler() {}
 	
 	private static final ClientTickHandler instance = new ClientTickHandler();
 	
 	public static ClientTickHandler instance() {
 		return instance;
 	}
 	
 	private final Minecraft mc = FMLClientHandler.instance().getClient();
 	
 	private PlayerKarma clientKarma;
 	
 	public void setClientKarma(PlayerKarma karma) {
 		clientKarma = karma;
 	}
 	
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 		
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		ScaledResolution scaler = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
         int width = scaler.getScaledWidth();
         int height = scaler.getScaledHeight();
 		if (type.contains(TickType.RENDER) && mc.theWorld != null && clientKarma != null) {
 			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/crymodResource/tex/gui.png"));
 			int barXStart = width / 2 - 182 / 2;
 			
 			// background of the bar
 			drawTexturedModalRect(barXStart, 10, 0, 0, 182, 5);
 
 			int karma = clientKarma.getKarma();
 			
 			// the bar itself
 			if (karma != 0) {
 				int rescaledKarmaWidth = (int)(Math.abs(clientKarma.getKarma()) / (float)PlayerKarmaManager.MAX_KARMA_VALUE * (float)182);
				if (rescaledKarmaWidth > 91) {
					rescaledKarmaWidth = 91;
				}
 				
 				int barTextureXStart = karma > 0 ? 91 : 91 - rescaledKarmaWidth;
 				
 				drawTexturedModalRect(barXStart + barTextureXStart, 10, barTextureXStart, 5, rescaledKarmaWidth, 5);
 			}
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.RENDER);
 	}
 
 	@Override
 	public String getLabel() {
 		return "SummoningModClientTicker";
 	}
 
 }
