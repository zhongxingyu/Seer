 package agaricus.mods.highlighttips;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 
 import java.util.EnumSet;
 
 @Mod(modid = "HighlightTips", name = "HighlightTips", version = "1.0-SNAPSHOT") // TODO: version from resource
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 public class HighlightTips implements ITickHandler {
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         TickRegistry.registerTickHandler(this, Side.CLIENT);
     }
 
     private String describeBlock(int id, int meta) {
         Block block = Block.blocksList[id];
         StringBuilder sb = new StringBuilder();
 
         if (block == null) {
             return "block #"+id;
         }
 
        sb.append(block.getLocalizedName());

        sb.append(' ');

        ItemStack itemStack = new ItemStack(id, 1, meta);

        sb.append(itemStack.getDisplayName());

        sb.append(' ');
         sb.append(id);
         sb.append(':');
         sb.append(meta);
 
         return sb.toString();
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
         Minecraft mc = Minecraft.getMinecraft();
         GuiScreen screen = mc.currentScreen;
         if (screen != null) return;
 
         double range = 300;
         float partialTickTime = 1;
         MovingObjectPosition mop = mc.thePlayer.rayTrace(range, partialTickTime);
         String s;
 
         if (mop == null) {
             s = "nothing";
         } else if (mop.typeOfHit == EnumMovingObjectType.ENTITY) {
             // TODO: find out why this apparently never triggers
             s = "entity " + mop.entityHit.getClass().getName();
         } else if (mop.typeOfHit == EnumMovingObjectType.TILE) {
             int id = mc.thePlayer.worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
             int meta = mc.thePlayer.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
 
             s = describeBlock(id, meta);
         } else {
             s = "unknown";
         }
 
         int x = 0;
         int y = 0;
         int color = 0xffffff;
         mc.fontRenderer.drawStringWithShadow(s, x, y, color);
     }
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
     }
 
     @Override
     public EnumSet<TickType> ticks() {
         return EnumSet.of(TickType.RENDER);
     }
 
     @Override
     public String getLabel() {
         return "HighlightTips";
     }
 }
