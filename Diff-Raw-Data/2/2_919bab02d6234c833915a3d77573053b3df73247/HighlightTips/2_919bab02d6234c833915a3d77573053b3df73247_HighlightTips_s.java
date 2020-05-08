 package agaricus.mods.highlighttips;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 
 import java.util.EnumSet;
 
 @Mod(modid = "HighlightTips", name = "HighlightTips", version = "1.0-SNAPSHOT") // TODO: version from resource
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 public class HighlightTips implements ITickHandler {
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         TickRegistry.registerTickHandler(this, Side.CLIENT);
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
         Minecraft mc = Minecraft.getMinecraft();
         GuiScreen screen = mc.currentScreen;
         if (screen != null) return;
 
         int x = 0;
         int y = 0;
         int color = 0xffffff;
         mc.fontRenderer.drawStringWithShadow("hello world", x, y, color);
     }
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
     }
 
     @Override
     public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
     }
 
     @Override
     public String getLabel() {
         return "HighlightTips";
     }
 }
