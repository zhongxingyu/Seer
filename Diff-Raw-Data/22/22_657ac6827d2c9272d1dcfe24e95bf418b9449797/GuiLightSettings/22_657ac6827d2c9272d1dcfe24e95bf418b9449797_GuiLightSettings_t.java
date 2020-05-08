 package modJam;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.GuiTextField;
 import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.settings.GameSettings;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.world.World;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 
 public class GuiLightSettings extends GuiContainer{
 
 	private World world;
 	private int x, y, z;
 	private EntityPlayer entityPlayer;
 	
 	private ContainerDummy container;
 	
 	private GuiNumberField lightField;
 	private GuiLightButton buttonSubtract;
 	private GuiLightButton buttonAdd;
 	private GuiLightButton buttonSet;
 	
 	public int[] themePrefs = ModJam.themeHandler.readConfiguration();
 	
 	public GuiLightSettings(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
 		super(new ContainerDummy());
 		container = (ContainerDummy)this.inventorySlots;
 		world = par1World;
 		x = par2;
 		y = par3;
 		z = par4;
 		entityPlayer = par5EntityPlayer;
 		xSize = 176;
 		ySize = 66;
 	}
 
 	@Override
 	protected void drawGuiContainerForegroundLayer(int par1, int par2){
		RenderHelper.disableStandardItemLighting();
         fontRenderer.drawString("Light Settings", 8, 5, 0xFFFFFF);
        RenderHelper.enableStandardItemLighting();
 	}
 	
 	@Override
     public void initGui() {
 		super.initGui();
 		buttonSubtract = new GuiLightButton(0, this.width / 2 - 40, this.height / 2 - 18, 20, 20, "-");
 		buttonAdd = new GuiLightButton(1, this.width / 2 + 20, this.height / 2 - 18, 20, 20, "+");
 		buttonSet = new GuiLightButton(2, this.width / 2 - 35, this.height / 2 + 5, 70, 20, "Set");
 		this.buttonList.add(buttonSubtract);
 		this.buttonList.add(buttonAdd);
 		this.buttonList.add(buttonSet);
 		this.lightField = new GuiNumberField(this.fontRenderer, this.width / 2 - 15, this.height / 2 - 18, 30, 20);
 		this.lightField.setMaxNumber(15);
 		this.lightField.setMaxStringLength(2);
         this.lightField.setVisible(true);
         this.lightField.setTextColor(16777215);
 		this.lightField.setText(Integer.toString(world.getBlockMetadata(x, y, z)));
 	}
 	
 	@Override
     protected void actionPerformed(GuiButton par1GuiButton) {
 		switch(par1GuiButton.id){
 		case 0: 
 			this.lightField.decrement();
 			break;
 		case 1:
 			this.lightField.increment();
 			break;
 		case 2:
 			set_DO();
 			break;
 		}
 	}
 	
 	@Override
 	public void handleMouseInput(){
 		int DWheelRotation = Mouse.getDWheel();
 		if(DWheelRotation != 0){
 			if(DWheelRotation > 0){
 				this.lightField.increment();
 			}else if(DWheelRotation < 0){
 				this.lightField.decrement();
 			}
 		}else{
 			super.handleMouseInput();
 		}
 	}
 	
 	protected boolean set_DO(){
 		if(this.lightField.getText().isEmpty() || this.lightField.getText() == null|| this.lightField.getText() == ""){
 			this.lightField.setText("0");
 		}
 		//PACKET
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 		try {
 			outputStream.writeShort(Short.parseShort(this.lightField.getText()));
 			outputStream.writeInt(x);
 			outputStream.writeInt(y);
 			outputStream.writeInt(z);
 		} catch (Exception ex) {
 		        ex.printStackTrace();
 		        return false;
 		}
 
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = "fuj1nAMetaPacket";
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 		//END PACKET
 		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
 			PacketDispatcher.sendPacketToServer(packet);
 		}
 		this.entityPlayer.closeScreen();
 		return true;
 	}
 	
 	protected boolean checkHotkeys(int par1){
 		switch(par1){
 		case 20:
 			this.lightField.setText("");
 			this.lightField.setFocused(true);
 			return true;
 		case 28:
 			return set_DO();
 		case 30:
 			return this.lightField.decrement();
 		case 203:
 			return this.lightField.decrement();
 		case 32:
 			return this.lightField.increment();
 		case 205:
 			return this.lightField.increment();
 		case 17:
 			return this.lightField.minimum();
 		case 200:
 			return this.lightField.minimum();
 		case 31:
 			return this.lightField.maximum();
 		case 208:
 			return this.lightField.maximum();
 		default:
 			return false;
 		}
 	}
 	
 	protected void keyTyped(char par1, int par2) {
 		if (!this.checkHotbarKeys(par2)) {
 			if (!this.checkHotkeys(par2)) {
 				if (this.lightField.textboxKeyTyped(par1, par2)) {
 				} else {
 					super.keyTyped(par1, par2);
 				}
 			}
 		}
 	}
 	
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         String textureFilePostfix = themePrefs[0] == 0 ? "" : Integer.toString(themePrefs[0] + 1);
         this.mc.renderEngine.bindTexture("/gui/lightSettings" + textureFilePostfix + ".png");
         int x = (width - xSize) / 2;
         int y = (height - ySize) / 2;
         this.drawTexturedModalRect(x, y, 0, this.themePrefs[1] * ySize, xSize, ySize);
         this.lightField.setFocused(true);
         this.lightField.drawTextBox();
 	}
 
 }
