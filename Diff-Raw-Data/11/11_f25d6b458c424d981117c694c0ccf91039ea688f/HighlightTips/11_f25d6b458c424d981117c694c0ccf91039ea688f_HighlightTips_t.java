 package agaricus.mods.highlighttips;
 
 import cpw.mods.fml.client.registry.KeyBindingRegistry;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.FMLRelauncher;
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraftforge.common.Configuration;
 
 import java.util.EnumSet;
 import java.util.logging.Level;
 
 @Mod(modid = "HighlightTips", name = "HighlightTips", version = "1.0-SNAPSHOT") // TODO: version from resource
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 public class HighlightTips implements ITickHandler {
 
     private static final int DEFAULT_KEY_TOGGLE = 62; // F4 - see http://www.minecraftwiki.net/wiki/Key_codes
     private static final double DEFAULT_RANGE = 300;
     private static final int DEFAULT_X = 0;
     private static final int DEFAULT_Y = 0;
     private static final int DEFAULT_COLOR = 0xffffff;
 
     private boolean enable = true;
     private int keyToggle = DEFAULT_KEY_TOGGLE;
     private double range = DEFAULT_RANGE;
     private int x = DEFAULT_X;
     private int y = DEFAULT_Y;
     private int color = DEFAULT_COLOR;
 
     private ToggleKeyHandler toggleKeyHandler;
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
 
         try {
             cfg.load();
 
             enable = cfg.get(Configuration.CATEGORY_GENERAL, "enable", true).getBoolean(true);
             keyToggle = cfg.get(Configuration.CATEGORY_GENERAL, "key.toggle", DEFAULT_KEY_TOGGLE).getInt(DEFAULT_KEY_TOGGLE);
             range = cfg.get(Configuration.CATEGORY_GENERAL, "range", DEFAULT_RANGE).getDouble(DEFAULT_RANGE);
             x = cfg.get(Configuration.CATEGORY_GENERAL, "x", DEFAULT_X).getInt(DEFAULT_X);
             y = cfg.get(Configuration.CATEGORY_GENERAL, "y", DEFAULT_Y).getInt(DEFAULT_Y);
             color = cfg.get(Configuration.CATEGORY_GENERAL, "color", DEFAULT_COLOR).getInt(DEFAULT_COLOR);
         } catch (Exception e) {
             FMLLog.log(Level.SEVERE, e, "HighlightTips had a problem loading it's configuration");
         } finally {
             cfg.save();
         }
 
         if (!FMLRelauncher.side().equals("CLIENT")) {
             // gracefully disable on non-client (= server) instead of crashing
             enable = false;
         }
 
         if (!enable) {
             FMLLog.log(Level.INFO, "HighlightTips disabled");
             return;
         }
 
         TickRegistry.registerTickHandler(this, Side.CLIENT);
         KeyBindingRegistry.registerKeyBinding(toggleKeyHandler = new ToggleKeyHandler(keyToggle));
     }
 
     private String describeBlock(int id, int meta) {
         Block block = Block.blocksList[id];
         StringBuilder sb = new StringBuilder();
 
         if (block == null) {
             return "block #"+id;
         }
 
         // block info
         sb.append(id);
         sb.append(':');
         sb.append(meta);
         sb.append(' ');
         String blockName = block.getLocalizedName();
         sb.append(blockName);
 
         // item info, if it was mined (this often has more user-friendly information, but sometimes is identical)
         sb.append("  ");
         int itemDamage = block.damageDropped(meta);

         ItemStack itemStack = new ItemStack(id, 1, itemDamage);
         String itemName = itemStack.getDisplayName();
         if (!blockName.equals(itemName)) {
             sb.append(itemName);
         }
         if (itemDamage != meta) {
             sb.append(' ');
             sb.append(itemDamage);
         }
 
         return sb.toString();
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
         if (!toggleKeyHandler.showInfo) return;
 
         Minecraft mc = Minecraft.getMinecraft();
         GuiScreen screen = mc.currentScreen;
         if (screen != null) return;
 
         float partialTickTime = 1;
         MovingObjectPosition mop = mc.thePlayer.rayTrace(range, partialTickTime);
         String s;
 
         if (mop == null) {
             return;
         } else if (mop.typeOfHit == EnumMovingObjectType.ENTITY) {
             // TODO: find out why this apparently never triggers
             s = "entity " + mop.entityHit.getClass().getName();
         } else if (mop.typeOfHit == EnumMovingObjectType.TILE) {
             int id = mc.thePlayer.worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
             int meta = mc.thePlayer.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
 
            try {
                s = describeBlock(id, meta);
            } catch (Throwable t) {
                s = id + ":" + meta + "  - " + t;
                t.printStackTrace();
            }
         } else {
             s = "unknown";
         }
 
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
