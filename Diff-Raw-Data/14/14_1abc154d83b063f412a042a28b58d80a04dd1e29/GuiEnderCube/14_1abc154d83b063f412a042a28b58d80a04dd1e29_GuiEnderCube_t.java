 package com.qzx.au.extras;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 import org.lwjgl.opengl.GL11;
 
 import com.qzx.au.core.Button;
 import com.qzx.au.core.Color;
 import com.qzx.au.core.GuiContainerAU;
 import com.qzx.au.core.PacketUtils;
 import com.qzx.au.core.TextField;
 import com.qzx.au.core.UI;
 
 @SideOnly(Side.CLIENT)
 public class GuiEnderCube extends GuiContainerAU {
 	private UI ui = new UI();
 	public static boolean update_pcl;
 
 	public GuiEnderCube(InventoryPlayer inventoryPlayer, TileEntityEnderCube tileEntity){
 		super(inventoryPlayer, tileEntity);
 	}
 
 //	@Override
 //	protected void drawGuiContainerForegroundLayer(int cursor_x, int cursor_y){
 //		super.drawGuiContainerForegroundLayer(cursor_x, cursor_y);
 //	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int cursor_x, int cursor_y){
 		super.drawGuiContainerBackgroundLayer(f, cursor_x, cursor_y);
 		this.fontRenderer.drawString("Ender Cube", this.upperX, this.upperY-11, 0xffffff);
 
 		TileEntityEnderCube tileEntity = (TileEntityEnderCube)this.tileEntity;
 
 		this.updateButtons();
 
 		if(GuiEnderCube.update_pcl){
 			this.playerList.setText(tileEntity.getPlayerControlWhitelist());
 			GuiEnderCube.update_pcl = false;
 		}
 
 		UI.bindTexture(this.mc, "au_extras", AUExtras.texturePath+"/gui/container.png");
 		UI.drawTexturedRect(this.playerButtons[0].xPosition-17, this.playerButtons[0].yPosition, 2*12,2*12, 12,12, 0.0F);
 		UI.drawTexturedRect(this.directionButtons[0].xPosition-17, this.directionButtons[0].yPosition, 2*12,2*12, 12,12, 0.0F);
 
 		// obstructed warning
 		if(tileEntity.isObstructed(tileEntity.worldObj, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord))
 			this.fontRenderer.drawString("teleport area is obstructed", this.upperX, this.upperY, 0xff0000);
 	}
 
 	private Button addPlayerButton(EnderButton id, int textureX, int textureXactive, String tooltip){
 		Button button = this.ui.newButton(UI.ALIGN_LEFT, id.ordinal(), null, 12, 12, 0)
 						.initState(((TileEntityEnderCube)this.tileEntity).getPlayerDirection() == id)
 						.initImage("au_extras", AUExtras.texturePath+"/gui/container.png", textureX, 2*12, textureXactive, 2*12)
 						.setTooltip(tooltip);
 		this.buttonList.add(button);
 		return button;
 	}
 	private Button addDirectionButton(EnderButton id, int textureX, int textureXactive, String tooltip){
 		Button button = this.ui.newButton(UI.ALIGN_LEFT, id.ordinal(), null, 12, 12, 0)
 						.initState(((TileEntityEnderCube)this.tileEntity).getTeleportDirection() == id)
 						.initImage("au_extras", AUExtras.texturePath+"/gui/container.png", textureX, 1*12, textureXactive, 1*12)
 						.setTooltip(tooltip);
 		this.buttonList.add(button);
 		return button;
 	}
 	private Button addControlButton(EnderButton id, int textureX, int textureXactive, boolean state, String tooltip){
 		Button button = this.ui.newButton(UI.ALIGN_LEFT, id.ordinal(), null, 12, 12, 0)
 						.initState(state)
 						.initImage("au_extras", AUExtras.texturePath+"/gui/container.png", textureX, 2*12, textureXactive, 2*12)
 						.setTooltip(tooltip);
 		this.buttonList.add(button);
 		return button;
 	}
 
 	private Button[] playerButtons = new Button[3];
 	private Button[] directionButtons = new Button[6];
 	private Button playerButton = null;
 	private Button playerRedstoneButton = null;
 	private Button redstoneButton = null;
 	private TextField playerList = null;
 
 	private void updateButtons(){
 		TileEntityEnderCube tileEntity = (TileEntityEnderCube)this.tileEntity;
 
 		this.playerButton.active = tileEntity.getPlayerControl();
 		this.playerRedstoneButton.active = tileEntity.getPlayerRedstoneControl();
 		this.redstoneButton.active = tileEntity.getRedstoneControl();
 
 		int button = tileEntity.getPlayerDirection().ordinal();
 		for(int i = 0; i < this.playerButtons.length; i++)
 			this.playerButtons[i].active = (this.playerButton.active && this.playerButtons[i].id == button ? true : false);
 
 		button = tileEntity.getTeleportDirection().ordinal();
 		for(int i = 0; i < this.directionButtons.length; i++)
 			this.directionButtons[i].active = (this.redstoneButton.active && this.directionButtons[i].id == button ? true : false);
 	}
 
 	@Override
 	public void initGui(){
 		super.initGui();
 		this.buttonList.clear();
 
 		TileEntityEnderCube tileEntity = (TileEntityEnderCube)this.tileEntity;
 
 		this.ui.setCursor(this.upperX + 10, this.upperY + 10);
 
 		// redstone control directions
 		redstoneButton = this.addControlButton(EnderButton.BUTTON_CONTROL_REDSTONE,	4*12, 5*12,		tileEntity.getRedstoneControl(),	"redstone signal teleports all entities above block");
 		this.ui.drawSpace(22);
 		directionButtons[0] = this.addDirectionButton(EnderButton.BUTTON_DOWN,	0*12, 1*12,		"teleport down");
 		this.ui.drawSpace(5);
 		directionButtons[1] = this.addDirectionButton(EnderButton.BUTTON_UP,	2*12, 3*12,		"teleport up");
 		this.ui.drawSpace(5);
 		directionButtons[2] = this.addDirectionButton(EnderButton.BUTTON_NORTH,	4*12, 5*12,		"teleport north");
 		this.ui.drawSpace(5);
 		directionButtons[3] = this.addDirectionButton(EnderButton.BUTTON_SOUTH,	6*12, 7*12,		"teleport south");
 		this.ui.drawSpace(5);
 		directionButtons[4] = this.addDirectionButton(EnderButton.BUTTON_WEST,	8*12, 9*12,		"teleport west");
 		this.ui.drawSpace(5);
 		directionButtons[5] = this.addDirectionButton(EnderButton.BUTTON_EAST,	10*12, 11*12,	"teleport east");
 		this.ui.lineBreak(17);
 
 		// player control directions
 		playerButton = this.addControlButton(EnderButton.BUTTON_CONTROL_PLAYER,				0*12, 1*12,		tileEntity.getPlayerControl(),	"player controls teleport with JUMP and SNEAK");
 		this.ui.drawSpace(5);
 		playerRedstoneButton = this.addControlButton(EnderButton.BUTTON_CONTROL_PLAYER_RS,	4*12, 5*12,		tileEntity.getPlayerRedstoneControl(),	"require redstone signal for player control");
 		this.ui.drawSpace(22);
 		playerButtons[0] = this.addPlayerButton(EnderButton.BUTTON_PLAYER_UD,	6*12, 7*12,		"up/down directions");
 		this.ui.drawSpace(5);
 		playerButtons[1] = this.addPlayerButton(EnderButton.BUTTON_PLAYER_NS,	8*12, 9*12,		"north/south directions");
 		this.ui.drawSpace(5);
 		playerButtons[2] = this.addPlayerButton(EnderButton.BUTTON_PLAYER_EW,	10*12, 11*12,	"east/west directions");
 		this.ui.lineBreak(17);
 
 		// player list
 		this.ui.lineBreak(11);
 		this.ui.x -= 6;
 		String pcl = tileEntity.getPlayerControlWhitelist();
 		playerList = this.ui.newTextField(pcl == null ? "" : pcl, 1000, 120, TextField.DEFAULT_STYLE);
 		playerList.setTooltip("player control whitelist\n- leave blank to allow all\n- commas between names\n- press ENTER to save");
 		this.textFieldList.add(playerList);
 		GuiEnderCube.update_pcl = false;
 	}
 
 	@Override
 	public void onTextFieldChanged(TextField field){
 		TileEntityEnderCube tileEntity = (TileEntityEnderCube)this.tileEntity;
 		String newPCL = field.getText().trim();
 		String oldPCL = tileEntity.getPlayerControlWhitelist();
 		if(!oldPCL.equals(newPCL))
 			PacketUtils.sendToServer(AUExtras.packetChannel, Packets.SERVER_ENDER_SET_PCL, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (String)newPCL);
 	}
 
 	@Override
 	public void actionPerformed(GuiButton button){
 		TileEntityEnderCube tileEntity = (TileEntityEnderCube)this.tileEntity;
 
 		switch(EnderButton.values()[button.id]){
 		case BUTTON_PLAYER_UD:
 		case BUTTON_PLAYER_NS:
 		case BUTTON_PLAYER_EW:
 			if(tileEntity.getPlayerControl() && tileEntity.getPlayerDirection().ordinal() != button.id)
 				PacketUtils.sendToServer(AUExtras.packetChannel, Packets.SERVER_ENDER_SET_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (byte)button.id);
 			break;
 		case BUTTON_DOWN:
 		case BUTTON_UP:
 		case BUTTON_NORTH:
 		case BUTTON_SOUTH:
 		case BUTTON_WEST:
 		case BUTTON_EAST:
 			if(tileEntity.getRedstoneControl() && tileEntity.getTeleportDirection().ordinal() != button.id)
 				PacketUtils.sendToServer(AUExtras.packetChannel, Packets.SERVER_ENDER_SET_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (byte)button.id);
 			break;
 		case BUTTON_CONTROL_PLAYER:
 			PacketUtils.sendToServer(AUExtras.packetChannel, Packets.SERVER_ENDER_SET_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (byte)button.id);
 			break;
 		case BUTTON_CONTROL_PLAYER_RS:
 			PacketUtils.sendToServer(AUExtras.packetChannel, Packets.SERVER_ENDER_SET_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (byte)button.id);
 			break;
 		case BUTTON_CONTROL_REDSTONE:
 			PacketUtils.sendToServer(AUExtras.packetChannel, Packets.SERVER_ENDER_SET_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (byte)button.id);
 			break;
 		}
 	}
 }
