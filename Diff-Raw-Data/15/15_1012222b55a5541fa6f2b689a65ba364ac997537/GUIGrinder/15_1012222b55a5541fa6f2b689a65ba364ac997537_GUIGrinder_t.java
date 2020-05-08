 package elex.client.gui;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import elex.inventory.ContainerGrinder;
 import elex.lib.Reference;
 import elex.tileentity.TileEntityGrinder;
 
 /**
  * Elemental Experimentation
  * 
  * GUIGrinder
  * 
  * @author Myo-kun
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 
 @SideOnly(Side.CLIENT)
 public class GUIGrinder extends GuiContainer {
     
     public GUIGrinder(InventoryPlayer player, TileEntityGrinder grinder) {
         super(new ContainerGrinder(player, grinder));
         xSize = 189;
         ySize = 183;
     }
     
    private static final ResourceLocation texture = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "textures/gui/grinder.png");
     
     @Override
     protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1, 1, 1, 1);
         
         Minecraft.getMinecraft().func_110434_K().func_110577_a(texture);
         drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
     }
 
 }
