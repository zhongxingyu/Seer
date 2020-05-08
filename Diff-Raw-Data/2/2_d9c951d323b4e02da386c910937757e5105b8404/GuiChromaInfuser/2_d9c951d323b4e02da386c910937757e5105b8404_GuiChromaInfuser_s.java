 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
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
 import com.qzx.au.core.UI;
 
 @SideOnly(Side.CLIENT)
 public class GuiChromaInfuser extends GuiContainerAU {
 	private UI ui = new UI();
 
 	public GuiChromaInfuser(InventoryPlayer inventoryPlayer, TileEntityChromaInfuser tileEntity){
 		super(inventoryPlayer, tileEntity);
 	}
 
 //	@Override
 //	protected void drawGuiContainerForegroundLayer(int cursor_x, int cursor_y){
 //		super.drawGuiContainerForegroundLayer(cursor_x, cursor_y);
 //	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int cursor_x, int cursor_y){
 		super.drawGuiContainerBackgroundLayer(f, cursor_x, cursor_y);
 		this.fontRenderer.drawString("Chroma Infuser", this.upperX, this.upperY-11, 0xffffff);
 
 		TileEntityChromaInfuser tileEntity = (TileEntityChromaInfuser)this.tileEntity;
 
 		// water gauge
 		int x = this.upperX + 5*18;
 		int y = this.upperY + 1*18+9 + 1 - 5;
 		int width = 18;
 		int height = 33;
 		UI.drawBorder(x, y, width, height, 0xff373737, 0xffffffff, 0xff8b8b8);
 		// draw water
 		if(tileEntity.getWater()){
 			if(tileEntity.getDyeVolume() > 0){
 //				Color waterColor = (new Color(ItemDye.dyeColors[tileEntity.getDyeColor()])).anaglyph();
 //				UI.setColor(waterColor.r, waterColor.g, waterColor.b, 1.0F);
 //				Icon waterIcon = BlockChromaInfuser.getWaterIcon();
 //				UI.bindTexture(this.mc, "au_extras", AUExtras.texturePath+"/blocks/chromaInfuser_water.png");
 
 				// colored water box
 				int water_height = (height-2) * tileEntity.getDyeVolume() / 8;
 				int water_top = (y+1) + (height-2 - water_height);
 				UI.drawRect(x+3, water_top, width-6, water_height, 0xff000000+ItemDye.dyeColors[tileEntity.getDyeColor()]);
 //				UI.drawTexturedRect(x+3, water_top, waterIcon, width-6, water_height, 0.0F);
 			} else {
 				// blue interior stripe to represent uncolored water
 				UI.drawBorder(x+7, y+7, width-14, height-14, 0xff345fda, 0xff345fda, 0xff345fda);
 				UI.drawBorder(x+8, y+8, width-16, height-16, 0xff345fda, 0xff345fda, 0xff345fda);
 			}
 		} // else no water, show nothing in the gauge
 		// draw level marks
 		for(int i = 1; i < 8; i++){
 			int yy = y + i*height/8;
 			int w = 4 + (i == 4 ? 2 : (i == 2 || i == 6 ? 1 : 0));
 			UI.drawLineH(x+1, yy, w, 0xff373737);
 			UI.drawLineH(x+width-1-w, yy, w, 0xff6e6e6e);
 		}
 
 		// arrow image from input to output
 		UI.setColor(1.0F, 1.0F, 1.0F, 1.0F);
 		int arrow_y_offset = tileEntity.getLocked() ? 0 : (int)(12.0F*tileEntity.getOutputTick());
 		// white arrow image from input to output
 		if(!tileEntity.getLocked()){
 			UI.bindTexture(this.mc, "au_extras", AUExtras.texturePath+"/gui/container.png");
 			UI.drawTexturedRect(this.upperX+1*18+4, this.upperY+2*18-3, 10, 0, 10, arrow_y_offset, 0.0F);
 		}
 		// arrow image from input to output
 		if(arrow_y_offset < 12){
 			UI.bindTexture(this.mc, "au_extras", AUExtras.texturePath+"/gui/container.png");
 			UI.drawTexturedRect(this.upperX+1*18+4, this.upperY+2*18-3+arrow_y_offset, 0, arrow_y_offset, 10, 12-arrow_y_offset, 0.0F);
 		}
 
 		// output patterns for each recipe button
 		RenderItem itemRenderer = new RenderItem();
 		itemRenderer.zLevel = 200.0F;
 
 		this.drawOutputPattern(tileEntity, ChromaButton.BUTTON_BLANK,		recipeButtons[0]);
 		this.drawOutputPattern(tileEntity, ChromaButton.BUTTON_SQUARE,		recipeButtons[1]);
 		this.drawOutputPattern(tileEntity, ChromaButton.BUTTON_SQUARE_DOT,	recipeButtons[2]);
 		this.drawOutputPattern(tileEntity, ChromaButton.BUTTON_DOT,			recipeButtons[3]);
 
 		this.updateRecipeButtons();
 	}
 
 	private void drawOutputPattern(TileEntityChromaInfuser te, ChromaButton recipeButton, Button button){
 		ItemStack input = te.getInput();
 		if(input != null){
 			// lookup recipe
 			ChromaRecipe recipe = ChromaRegistry.getRecipe(recipeButton, input);
 			ItemStack output
 				= (recipe == null || !te.getWater() || te.getDyeVolume() == 0
 				? null
 				: new ItemStack(recipe.output.getItem().itemID, recipe.output.stackSize, recipe.getOutputColor(te.getDyeColor())));
 			// draw itemstack
 			if(output != null){
 				RenderItem itemRenderer = new RenderItem();
 				itemRenderer.zLevel = 200.0F;
 				try {
 					UI.drawItemStack(this.mc, itemRenderer, output, button.xPosition+15, button.yPosition-2, true);
 				} catch(Exception e){
					// this can happen
 				}
 			}
 		}
 	}
 
 	private Button addRecipeButton(ChromaButton id, int textureX, int textureXactive){
 		Button button = this.ui.newButton(UI.ALIGN_LEFT, id.ordinal(), null, 12, 12, 0)
 						.initState(((TileEntityChromaInfuser)this.tileEntity).getRecipeButton() == id)
 						.initImage("au_extras", AUExtras.texturePath+"/gui/container.png", textureX, 0, textureXactive, 0);
 		this.buttonList.add(button);
 		return button;
 	}
 	private Button addLockButton(int id, int textureX, int textureXactive){
 		Button button = this.ui.newButton(UI.ALIGN_LEFT, id, null, 12, 12, 0)
 						.initState(!((TileEntityChromaInfuser)this.tileEntity).getLocked())
 						.initImage("au_extras", AUExtras.texturePath+"/gui/container.png", textureX, 0, textureXactive, 0);
 		this.buttonList.add(button);
 		return button;
 	}
 
 	private Button[] recipeButtons = new Button[4];
 	private void updateRecipeButtons(){
 		int button = ((TileEntityChromaInfuser)this.tileEntity).getRecipeButton().ordinal();
 		for(int i = 0; i < this.recipeButtons.length; i++)
 			this.recipeButtons[i].active = (this.recipeButtons[i].id == button ? true : false);
 	}
 
 	private static final int BUTTON_LOCKED = 100;
 
 	@Override
 	public void initGui(){
 		super.initGui();
 		this.buttonList.clear();
 
 		this.ui.setCursor(this.upperX + 2*18 + 9, this.upperY + 10);
 		recipeButtons[0] = this.addRecipeButton(ChromaButton.BUTTON_BLANK,		20+0*12, 20+1*12);
 		this.ui.lineBreak(17);
 		recipeButtons[1] = this.addRecipeButton(ChromaButton.BUTTON_SQUARE,		20+2*12, 20+3*12);
 		this.ui.lineBreak(17);
 		recipeButtons[2] = this.addRecipeButton(ChromaButton.BUTTON_SQUARE_DOT,	20+4*12, 20+5*12);
 		this.ui.lineBreak(17);
 		recipeButtons[3] = this.addRecipeButton(ChromaButton.BUTTON_DOT,		20+6*12, 20+7*12);
 
 		this.ui.setCursor(this.upperX+2, this.upperY+2*18-3);
 		this.addLockButton(GuiChromaInfuser.BUTTON_LOCKED,						20+8*12, 20+9*12);
 	}
 
 	@Override
 	public void actionPerformed(GuiButton button){
 		if(button.id == GuiChromaInfuser.BUTTON_LOCKED){
 			((Button)button).active = ((Button)button).active ? false : true;
 			PacketDispatcher.sendPacketToServer(
 				PacketUtils.createPacket(AUExtras.packetChannel, Packets.LOCKED_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, ((Button)button).active)
 			);
 		} else if(!((Button)button).active){
 			PacketDispatcher.sendPacketToServer(
 				PacketUtils.createPacket(AUExtras.packetChannel, Packets.RECIPE_BUTTON, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord, (byte)button.id)
 			);
 		}
 	}
 
 /*
 	protected void mouseClicked(int x, int y, int button){
 		int x = this.upperX + 5*18;
 		int y = this.upperY + 1*18+9 + 1 - 5;
 
 	}
 */
 }
