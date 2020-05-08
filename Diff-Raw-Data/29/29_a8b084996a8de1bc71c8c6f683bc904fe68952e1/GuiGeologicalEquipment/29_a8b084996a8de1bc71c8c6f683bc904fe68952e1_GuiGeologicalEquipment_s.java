 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.client.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import slimevoid.tmf.core.lib.ResourceLib;
import slimevoid.tmf.machines.inventory.ContainerGeologicalEquipment;
import slimevoid.tmf.machines.tileentities.TileEntityGeologicalEquipment;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class GuiGeologicalEquipment extends GuiContainer {
 	private TileEntityGeologicalEquipment	geoEquip;
 	// Set this to true to draw blocks instead of colors.
 	private boolean							drawBlock	= false;
 
 	public GuiGeologicalEquipment(EntityPlayer entityplayer, TileEntityGeologicalEquipment geoEquip) {
 		super(new ContainerGeologicalEquipment(entityplayer.inventory, geoEquip));
 		this.geoEquip = geoEquip;
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
 		GL11.glColor4f(	1.0F,
 						1.0F,
 						1.0F,
 						1.0F);
 		mc.renderEngine.bindTexture(ResourceLib.GUI_GEOEQUIP);
 		int sizeX = (width - xSize) / 2;
 		int sizeY = (height - ySize) / 2;
 		drawTexturedModalRect(	sizeX,
 								sizeY,
 								0,
 								0,
 								xSize,
 								ySize);
 
 		int var7;
 
 		if (geoEquip.isBurning()) {
 			var7 = this.geoEquip.getBurnTimeRemainingScaled(12);
 			this.drawTexturedModalRect(	sizeX + 9,
 										sizeY + 52 + 12 - var7,
 										176,
 										12 - var7,
 										14,
 										var7 + 2);
 		}
 		var7 = this.geoEquip.getCookProgressScaled(24);
 		this.drawTexturedModalRect(	sizeX + 31,
 									sizeY + 51,
 									176,
 									14,
 									var7 + 1,
 									16);
 
 		fontRenderer.drawString("Level: " + geoEquip.yCoord,
 								sizeX + 81,
 								sizeY + 15,
 								0x404040);
 
 		drawResultList(	sizeX + 62,
 						sizeY + 32,
 						sizeX + 77,
 						sizeY + 135);
 
 		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
 		int mouseY = this.height - Mouse.getEventY() * this.height
 						/ this.mc.displayHeight - 1;
 
 		int level = getLevel(	mouseX,
 								mouseY,
 								sizeX + 62,
 								sizeY + 32,
 								sizeX + 77,
 								sizeY + 135);
 
 		fontRenderer.drawString("Level: " + level,
 								sizeX + 102,
 								sizeY + 36,
 								0x404040);
 
 		drawLevel(	sizeX + 98,
 					sizeY + 51,
 					geoEquip.getBlocksAt(level));
 	}
 
 	private int getLevel(int mouseX, int mouseY, int listX1, int listY1, int listX2, int listY2) {
 		if (mouseX >= listX1 && mouseX <= listX2 && mouseY >= listY1
 			&& mouseY <= listY2) {
 			int length = geoEquip.yCoord;
 			float levelHeight = (float) (listY2 - listY1) / (float) length;
 			for (int depth = length - 1; depth >= 0; depth--) {
				if (mouseY >= listY1
								+ (int) (levelHeight * (length - depth))
 					&& mouseY <= listY1
									+ ((int) (levelHeight * (length
																		- depth + 1)))) return depth;
 			}
 		}
 
 		return geoEquip.currentLevel;
 	}
 
 	private void drawResultList(int x1, int y1, int x2, int y2) {
 		int length = geoEquip.yCoord;
 		float levelHeight = (float) (y2 - y1) / (float) length;
		for (int depth = length - 1; depth >= 0; depth--) {
 			// Assemble colors
 			List<Integer> colorMap = new ArrayList<Integer>();
 			Block[] blocks = geoEquip.getSurveyResult(depth);
 			if (blocks != null) {
 				for (Block block : blocks) {
 					if (block != null) colorMap.add(getBlockColor(block));
 				}
 			}
 
 			// Blend colors
 			int color = 0xff000000;
 			for (int c : colorMap) {
 				if (c == getBlockColor(Block.oreCoal)) {
 					color = getBlockColor(Block.oreCoal);
 					break;
 				}
 				color += c / colorMap.size();
 			}
 
 			// Draw color
 			drawRect(	x1,
 						y1 + (int) (levelHeight * (length - depth)),
 						x2,
						y1
								+ ((int) (levelHeight * (length - depth + 1))),
 						color);
 		}
 	}
 
 	private void drawLevel(int x, int y, Block[] blocks) {
 		if (blocks != null && blocks.length == 9) {
 			drawBlock(	x + 18,
 						y + 18,
 						15,
 						15,
 						blocks[0]);
 
 			drawBlock(	x + 18,
 						y,
 						15,
 						15,
 						blocks[1]);
 			drawBlock(	x + 18,
 						y + 36,
 						15,
 						15,
 						blocks[2]);
 
 			drawBlock(	x,
 						y + 18,
 						15,
 						15,
 						blocks[3]);
 			drawBlock(	x + 36,
 						y + 18,
 						15,
 						15,
 						blocks[4]);
 
 			drawBlock(	x,
 						y,
 						15,
 						15,
 						blocks[5]);
 			drawBlock(	x,
 						y + 36,
 						15,
 						15,
 						blocks[6]);
 
 			drawBlock(	x + 36,
 						y,
 						15,
 						15,
 						blocks[7]);
 			drawBlock(	x + 36,
 						y + 36,
 						15,
 						15,
 						blocks[8]);
 		} else {
 			drawBlock(	x + 18,
 						y + 18,
 						15,
 						15,
 						null);
 
 			drawBlock(	x + 18,
 						y,
 						15,
 						15,
 						null);
 			drawBlock(	x + 18,
 						y + 36,
 						15,
 						15,
 						null);
 
 			drawBlock(	x,
 						y + 18,
 						15,
 						15,
 						null);
 			drawBlock(	x + 36,
 						y + 18,
 						15,
 						15,
 						null);
 
 			drawBlock(	x,
 						y,
 						15,
 						15,
 						null);
 			drawBlock(	x,
 						y + 36,
 						15,
 						15,
 						null);
 
 			drawBlock(	x + 36,
 						y,
 						15,
 						15,
 						null);
 			drawBlock(	x + 36,
 						y + 36,
 						15,
 						15,
 						null);
 		}
 	}
 
 	private void drawBlock(int x, int y, int width, int height, Block block) {
 		if (drawBlock && block != null) {
 			IInventory inv = new IInventory() {
 				@Override
 				public ItemStack getStackInSlot(int i) {
 					return new ItemStack(Block.blocksList[i]);
 				}
 
 				@Override
 				public int getSizeInventory() {
 					return 0;
 				}
 
 				@Override
 				public ItemStack decrStackSize(int var1, int var2) {
 					return null;
 				}
 
 				@Override
 				public ItemStack getStackInSlotOnClosing(int var1) {
 					return null;
 				}
 
 				@Override
 				public void setInventorySlotContents(int var1, ItemStack var2) {
 				}
 
 				@Override
 				public String getInvName() {
 					return null;
 				}
 
 				@Override
 				public int getInventoryStackLimit() {
 					return 0;
 				}
 
 				@Override
 				public void onInventoryChanged() {
 				}
 
 				@Override
 				public boolean isUseableByPlayer(EntityPlayer var1) {
 					return false;
 				}
 
 				@Override
 				public void openChest() {
 				}
 
 				@Override
 				public void closeChest() {
 				}
 
 				@Override
 				public boolean isInvNameLocalized() {
 					return false;
 				}
 
 				@Override
 				public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 					return false;
 				}
 			};
 			Slot slot = new Slot(inv, block.blockID, x, y);
 			this.drawSlotInventory(slot);
 		} else {
 			drawRect(	x,
 						y,
 						x + width,
 						y + height,
 						getBlockColor(block));
 		}
 	}
 
 	private int getBlockColor(Block block) {
 		if (block == null) return 0xff000000;
 
 		if (block instanceof BlockOre) return 0xffff0000;
 
 		return block.blockMaterial.materialMapColor.colorValue | 0xff000000;
 	}
 }
