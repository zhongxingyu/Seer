 package ghost606.networkcraft.gui.elements.tab;
 
 import ghost606.networkcraft.gui.GuiNetworkCraft;
 import ghost606.networkcraft.gui.elements.IGuiElement;
 import ghost606.networkcraft.resources.ResourceManager;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.util.Icon;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class GuiTab extends Gui implements IGuiElement {
 	protected int backgroundColor = 0xffffff;
 	protected int x;
 	protected int y;
 	protected int maxWidth = 124;
 	protected int maxHeight = 124;
 	protected GuiNetworkCraft gui;
 	
 	private boolean open;
 	private int minWidth = 24;
 	private int currentWidth;
 	private int minHeight = 24;
 	private int currentHeight;
 	
 	public GuiTab(GuiNetworkCraft gui, int x, int y)
 	{
 		this.gui = gui;
 		this.x = x;
 		this.y = y;
 		
 		this.currentHeight = this.minHeight;
 		this.currentWidth = this.minWidth;
 	}
 
 	@Override
 	public void initGui() {
 		
 	}
 	
 	public int getHeight()
 	{
 		return this.currentHeight;
 	}
 	
 	public int getWidth()
 	{
 		return this.currentWidth;
 	}
 	
 	public void setFullyOpen() {
 		this.open = true;
 		this.currentWidth = this.maxWidth;
 		this.currentHeight = this.maxHeight;
 	}
 
 	public void toggleOpen() {
 		if (this.open) {
 			this.open = false;
 		} else {
 			this.open = true;
 		}
 	}
 
 	public boolean isOpen() {
 		return this.open;
 	}
 
 	public boolean isFullyOpened() {
 		return this.currentWidth >= this.maxWidth;
 	}
 	
 	public void update() {
 		// Width
 		if (this.open && this.currentWidth < this.maxWidth) {
 			this.currentWidth += 4;
 		} else if (!this.open && this.currentWidth > this.minWidth) {
 			this.currentWidth -= 4;
 		}
 
 		// Height
 		if (this.open && this.currentHeight < this.maxHeight) {
 			this.currentHeight += 4;
 		} else if (!this.open && this.currentHeight > this.minHeight) {
 			this.currentHeight -= 4;
 		}
 	}
 	
 	@Override
 	public Boolean collide(int mouseX, int mouseY, int mouseButton)
 	{
 		if (mouseButton == 0)
 		{
 			mouseX = mouseX - this.gui.getOffsetLeft();
 			mouseY = mouseY - this.gui.getOffsetTop();
 			if (mouseX >= this.x && mouseX <= this.x + this.currentWidth && mouseY >= this.y && mouseY <= this.y + this.currentHeight)
 			{
 				this.toggleOpen();
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void drawIcon(Icon icon, int x, int y) {
 		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
 		drawTexturedModelRectFromIcon(x, y, icon, 16, 16);
		Minecraft.getMinecraft().func_110434_K().func_110577_a(TextureMap.field_110576_c);
 	}
 
 	@Override
 	public void draw() {
 		float colorR = (this.backgroundColor >> 16 & 255) / 255.0F;
 		float colorG = (this.backgroundColor >> 8 & 255) / 255.0F;
 		float colorB = (this.backgroundColor & 255) / 255.0F;
 		
 		GL11.glColor4f(colorR, colorG, colorB, 1.0F);
 
 		Minecraft.getMinecraft().renderEngine.func_110577_a(ResourceManager.Gui_Textures.Tab);
 		drawTexturedModalRect(this.x, this.y, 0, 256 - this.currentHeight, 4, this.currentHeight);
 		drawTexturedModalRect(this.x + 4, this.y, 256 - this.currentWidth + 4, 0, currentWidth - 4, 4);
 		drawTexturedModalRect(this.x, this.y, 0, 0, 4, 4);
 		drawTexturedModalRect(this.x + 4, this.y + 4, 256 - this.currentWidth + 4, 256 - this.currentHeight + 4, this.currentWidth - 4, this.currentHeight - 4);
 		
 		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
 	}
 }
