 package biotech.client;
 
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 
 import universalelectricity.core.electricity.ElectricInfo;
 import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;
 import biotech.Biotech;
 import biotech.container.CowMilkerContainer;
 import biotech.tileentity.CowMilkerTileEntity;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class CowMilkerGui extends GuiContainer {
 	private CowMilkerTileEntity tileEntity;
 
 	public static String COWMILKER_GUI = Biotech.FILE_PATH
 			+ "GUI_CowMilker.png";
 
 	private int containerWidth;
 	private int containerHeight;
 
 	public CowMilkerGui(InventoryPlayer playerInventory,
 			CowMilkerTileEntity tileEntity) {
 		super(new CowMilkerContainer(playerInventory, tileEntity));
 
 		this.tileEntity = tileEntity;
 	}
 
 	/**
 	 * Draw the foreground layer for the GuiContainer (everything in front of
 	 * the items)
 	 */
 	@Override
 	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRenderer.drawString(this.tileEntity.getInvName(), 65, 4,
 				4210752);
 
 		String displayText = "";
 
 		if (this.tileEntity.isDisabled()) {
 			displayText = "Disabled!";
 		} else if (this.tileEntity.hasRedstone) {
 			displayText = "Working";
 		} else {
 			displayText = "Idle";
 		}
 
 		this.fontRenderer
 				.drawString("Status: " + displayText, 32, 17, 0x00CD00);
 		this.fontRenderer.drawString(
 				"Voltage: "
 						+ ElectricInfo.getDisplayShort(
 								this.tileEntity.getVoltage(),
 								ElectricUnit.VOLTAGE), 32, 27, 0x00CD00);
 		this.fontRenderer.drawString(
 				"Storage: "
 						+ ElectricInfo.getDisplayShort(
 								this.tileEntity.getElectricityStored(),
 								ElectricUnit.JOULES), 32, 37, 0x00CD00);
 		this.fontRenderer.drawString("Milk: " + this.tileEntity.getMilkStored()
 				+ "/" + this.tileEntity.getMaxMilk(), 32, 47, 0x00CD00);
 
 		this.fontRenderer.drawString(
 				StatCollector.translateToLocal("container.inventory"), 8,
 				this.ySize - 96 + 2, 4210752);
 	}
 
 	/**
 	 * Draw the background layer for the GuiContainer (everything behind the
 	 * items)
 	 */
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
 		int picture = mc.renderEngine.getTexture(this.COWMILKER_GUI);
 		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		this.mc.renderEngine.bindTexture(picture);
 
 		containerWidth = (this.width - this.xSize) / 2;
 		containerHeight = (this.height - this.ySize) / 2;
 
 		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0,
 				xSize, ySize);
 
 		int scale = (int) (((double) this.tileEntity.getMilkStored() / this.tileEntity
 				.getMaxMilk()) * 100);
 
 		this.drawTexturedModalRect(containerWidth + 215, containerHeight + 141
 				- scale, 352, 101 - scale, 15, scale);
 		if (this.tileEntity.bucketIn) {
 			this.drawTexturedModalRect(containerWidth + 153,
 					containerHeight + 33, 176, 51, 13,
 					this.tileEntity.bucketTimeMax);
 		}
 	}
 }
