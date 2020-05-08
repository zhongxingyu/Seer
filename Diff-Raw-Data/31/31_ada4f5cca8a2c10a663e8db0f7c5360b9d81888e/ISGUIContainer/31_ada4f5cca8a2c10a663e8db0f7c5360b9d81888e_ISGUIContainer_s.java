 package industrialscience;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.inventory.Container;
 import net.minecraft.util.ResourceLocation;
 @SideOnly(Side.CLIENT)
 public class ISGUIContainer extends GuiContainer {
 	ResourceLocation texture;
 	public ISGUIContainer(Container par1Container, ResourceLocation rl) {
 		super(par1Container);
 		xSize=182;
 		ySize=171;
 		texture=rl;
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
 		GL11.glColor4f(1, 1, 1, 1);
		Minecraft.getMinecraft().func_110434_K().func_110577_a(texture);
 		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
 	}
 
 }
