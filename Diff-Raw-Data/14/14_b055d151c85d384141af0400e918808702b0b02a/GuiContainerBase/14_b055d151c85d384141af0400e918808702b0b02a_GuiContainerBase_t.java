 package spacecraft.core.gui;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.inventory.Container;
 
 public abstract class GuiContainerBase extends GuiContainer {
	private static final String PATH_PREFIX = "/mods/spacecraft/textures/gui/";
 	private static final String PATH_SUFFIX = ".png";
 	
 
 	public GuiContainerBase(Container par1Container) {
 		super(par1Container);
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		this.guiLeft = (this.width - this.xSize) / 2;
 		this.guiTop = (this.height - this.ySize) / 2;
 	}
 	
 	protected String makeTexturePath(String name) {
 		return PATH_PREFIX + name + PATH_SUFFIX;
 	}
 }
