 package mods.nordwest.client.gui;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import mods.nordwest.common.container.ContainerExtractor;
 import mods.nordwest.tileentity.TileEntityExtractor;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiTextField;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 
 public class GuiExtractor extends GuiContainer {
 
 	private TileEntityExtractor tileEntity;
 	private EntityPlayer player;
 	private World world;
 
 	public GuiExtractor(InventoryPlayer par1InventoryPlayer, TileEntity tileEntity, EntityPlayer entityplayer) {
 		super(new ContainerExtractor(par1InventoryPlayer, (TileEntityExtractor) tileEntity));
 		this.tileEntity = (TileEntityExtractor) tileEntity;
 		player = entityplayer;
 		world = entityplayer.worldObj;
 		this.ySize+=5;
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		this.mc.renderEngine.bindTexture("/mods/nordwest/textures/gui/GuiExtractor.png");
 		int k = (this.width - this.xSize) / 2;
 		int l = (this.height - this.ySize) / 2;
 		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
 		int i1;
 		if (this.tileEntity.isBurning())
         {
             i1 = this.tileEntity.getBurnTimeRemainingScaled(12);
             this.drawTexturedModalRect(k + 54, l + 54 - i1, 176, 12 - i1, 14, i1 + 2);
         }
 		i1 = this.tileEntity.getCookProgressScaled(24);
        this.drawTexturedModalRect(k + 79, l + 37, 176, 16, i1 + 1, 20);
 
 	}
 
 	@Override
 	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
 		String s = this.tileEntity.isInvNameLocalized() ? this.tileEntity.getInvName() : StatCollector.translateToLocal(this.tileEntity.getInvName());
 		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2 - 50, 6, 4210752);
 		//	this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
 		
 	}
 
 }
