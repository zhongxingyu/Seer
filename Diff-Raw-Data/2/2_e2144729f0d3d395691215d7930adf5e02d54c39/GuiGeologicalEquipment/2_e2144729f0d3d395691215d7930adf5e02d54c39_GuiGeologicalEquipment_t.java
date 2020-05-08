 package slimevoid.tmf.client.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.opengl.GL11;
 
 import slimevoid.tmf.machines.inventory.ContainerGeologicalEquipment;
 import slimevoid.tmf.machines.tileentities.TileEntityGeologicalEquipment;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 
 public class GuiGeologicalEquipment extends GuiContainer {
 	private TileEntityGeologicalEquipment geoEquip;
 
 	public GuiGeologicalEquipment(EntityPlayer entityplayer, TileEntityGeologicalEquipment geoEquip) {
 		super(new ContainerGeologicalEquipment(entityplayer.inventory, geoEquip));
 		this.geoEquip = geoEquip;
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float var1, int var2,int var3) {
 		int tex = mc.renderEngine.getTexture("/TheMinersFriend/gui/geoequip.png");
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		mc.renderEngine.bindTexture(tex);
 		int sizeX = (width - xSize) / 2;
 		int sizeY = (height - ySize) / 2;
 		drawTexturedModalRect(sizeX, sizeY, 0, 0, xSize, ySize);
 
 		int var7;
 		
 		if ( geoEquip.isBurning() ) {
 			var7 = this.geoEquip.getBurnTimeRemainingScaled(12);
 			this.drawTexturedModalRect(sizeX + 9, sizeY + 52 + 12 - var7, 176, 12 - var7, 14, var7 + 2);
 		}
 		var7 = this.geoEquip.getCookProgressScaled(24);
 		this.drawTexturedModalRect(sizeX + 31, sizeY + 51, 176, 14, var7 + 1, 16);
 
 		fontRenderer.drawString(
 				"Level: "+geoEquip.yCoord,
 				sizeX + 81,
 				sizeY + 15,
 				0x404040
 		);
 		
 		drawResultList(
 				sizeX + 62, 
 				sizeY + 32,
 				sizeX + 77, 
 				sizeY + 135
 		);
 	}
 
 	private void drawResultList(int x1, int y1, int x2, int y2) {
 		int length = geoEquip.yCoord;
 		float levelHeight = (float)(y2-y1) / (float)length;
 		for ( int depth = length-1; depth >= 0; depth-- ) {
 			// Assemble colors
 			List<Integer> colorMap = new ArrayList<Integer>();
 			Block[] blocks = geoEquip.getSurveyResult(depth);
 			if ( blocks != null ) {
 				for ( Block block: blocks ) {
 					if ( block != null )
 						colorMap.add(getBlockColor(block));
 				}
 			}
 			
 			// Blend colors
 			int color = 0;
 			for ( int c: colorMap ) {
 				color += c/colorMap.size();
 			}
 
 			// Draw color
 			drawRect(
 					x1, 
 					y1 + (int)(levelHeight*(float)(length-depth)),
 					x2, 
					y1 + ((int)(levelHeight*(float)(length-depth+1))),
 					color
 			);
 		}
 	}
 	
 	private int getBlockColor(Block block) {
 		if ( block == null )
 			return 0xff000000;
 		
 		if ( block instanceof BlockOre )
 			return 0xffff0000;
 		
 		return block.blockMaterial.materialMapColor.colorValue | 0xff000000;
 	}
 }
