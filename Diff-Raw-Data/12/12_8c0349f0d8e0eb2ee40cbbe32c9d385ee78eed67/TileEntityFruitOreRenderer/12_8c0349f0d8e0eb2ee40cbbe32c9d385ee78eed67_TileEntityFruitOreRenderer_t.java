 package ayamitsu.fruitore.client;
 
 import org.lwjgl.opengl.GL11;
 
 import ayamitsu.fruitore.block.TileEntityFruitOre;
 import ayamitsu.fruitore.object.FruitOreObject;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Vec3;
 import net.minecraft.util.Vec3Pool;
 import net.minecraft.world.World;
 
 public class TileEntityFruitOreRenderer extends TileEntitySpecialRenderer {
 
 	public TileEntityFruitOreRenderer() {
 	}
 
 	@Override
 	public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f) {
 		this.renderFruitOre((TileEntityFruitOre)tileentity, d0, d1, d2, f);
 	}
 
 	public void renderFruitOre(TileEntityFruitOre fruitOre, double x, double y, double z, float f) {
 		GL11.glPushMatrix();
 
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 
 		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
 
 
 		FruitOreObject object = FruitOreObject.fruitsList[fruitOre.getFruitId()];
 
 		if (object != null) {
 			if (fruitOre.getBlockMetadata() >= object.getHarvetableLevel()) {
 				ItemStack renderItem = object.getRenderItem(fruitOre.getFruitMeta());
 
 				if (renderItem != null) {
 					renderItem.stackSize = 1;
 					EntityItem entityItem = new EntityItem(fruitOre.getWorldObj(), 0.0D, 0.0D, 0.0D, renderItem);
 					entityItem.hoverStart = 0;
 
 					GL11.glPushMatrix();
 					RenderItem.renderInFrame = true;
 					RenderManager.instance.renderEntityWithPosYaw(entityItem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
 					RenderItem.renderInFrame = false;
 					GL11.glPopMatrix();
 				}
 			} else {
				float scale = 0.5F + (object.getMaxGrowLevel() != 0 ? ((float)fruitOre.getBlockMetadata() / (float)object.getMaxGrowLevel()) / 2.0F : 0.0F);
				GL11.glTranslatef(0.0F, 0.5F - (scale / 2.0F), 0.0F);
 				GL11.glScalef(scale, scale, scale);
 			}
 		}
 
 
 		GL11.glDisable(GL11.GL_TEXTURE_2D);
 		GL11.glDisable(GL11.GL_LIGHTING);
 
 		GL11.glPushMatrix();
 
 		Tessellator tes = Tessellator.instance;
 		tes.startDrawing(GL11.GL_TRIANGLE_FAN);
 
 		if (object != null) {
 
 			int color = object.getFruitColor(fruitOre.getFruitMeta());
 			float red = (float)(color >> 0x10 & 255) / 255.0F;
 			float green = (float)(color >> 0x08 & 255) / 255.0F;
 			float blue = (float)(color >> 0x00 & 255) / 255.0F;
 			tes.setColorRGBA_F(red, green, blue, object.getFruitAlpha(fruitOre.getFruitMeta()));
 			//tes.setColorRGBA_F(0.77F, 0.24F, 0.16F, 0.9F);
 
 			if (fruitOre.getBlockMetadata() >= object.getHarvetableLevel()) {
 				GL11.glEnable(GL11.GL_BLEND);
 				GL11.glBlendFunc(770, 1);
 			}
 		}
 
 		GL11.glTranslatef(0.0F, -0.5F, 0.0F);
 
 
 		tes.addVertex(0.0F, 1.0F, 0.0F);
 		tes.addVertex(0.0F, 0.9F, 0.30F);
 		tes.addVertex(0.25F, 0.9F, 0.15F);
 		tes.addVertex(0.25F, 0.9F, -0.15F);
 		tes.addVertex(-0.0F, 0.9F, -0.30F);
 		tes.addVertex(-0.25F, 0.9F, -0.15F);
 		tes.addVertex(-0.25F, 0.9F, 0.15F);
 		tes.addVertex(0.0F, 0.9F, 0.30F);
 
 		tes.draw();
 
 		tes.startDrawing(GL11.GL_QUAD_STRIP);
 
 		tes.addVertex(0.0F, 0.9F, 0.30F);
 		tes.addVertex(0.00F, 0.70F, 0.50F);
 		tes.addVertex(0.25F, 0.9F, 0.15F);
 		tes.addVertex(0.433F, 0.70F, 0.25F);
 		tes.addVertex(0.25F, 0.9F, -0.15F);
 		tes.addVertex(0.433F, 0.70F, -0.25F);
 		tes.addVertex(-0.0F, 0.9F, -0.30F);
 		tes.addVertex(0.00F, 0.70F, -0.50F);
 		tes.addVertex(-0.25F, 0.9F, -0.15F);
 		tes.addVertex(-0.433F, 0.70F, -0.25F);
 		tes.addVertex(-0.25F, 0.9F, 0.15F);
 		tes.addVertex(-0.433F, 0.70F, 0.25F);
 		tes.addVertex(0.0F, 0.9F, 0.30F);
 		tes.addVertex(0.00F, 0.70F, 0.50F);
 
 		tes.draw();
 
 		tes.startDrawing(GL11.GL_QUAD_STRIP);
 
 		tes.addVertex(0.00F, 0.70F, 0.50F);
 		tes.addVertex(0.00F, 0.20F, 0.40F);
 		tes.addVertex(0.433F, 0.70F, 0.25F);
 		tes.addVertex(0.3464F, 0.20F, 0.20F);
 		tes.addVertex(0.433F, 0.70F, -0.25F);
 		tes.addVertex(0.3464F, 0.20F, -0.20F);
 		tes.addVertex(0.00F, 0.70F, -0.50F);
 		tes.addVertex(0.00F, 0.20F, -0.40F);
 		tes.addVertex(-0.433F, 0.70F, -0.25F);
 		tes.addVertex(-0.3464F, 0.20F, -0.20F);
 		tes.addVertex(-0.433F, 0.70F, 0.25F);
 		tes.addVertex(-0.3464F, 0.20F, 0.20F);
 		tes.addVertex(0.00F, 0.70F, 0.50F);
 		tes.addVertex(0.00F, 0.20F, 0.40F);
 
 		tes.draw();
 
 		tes.startDrawing(GL11.GL_QUAD_STRIP);
 
 		tes.addVertex(0.00F, 0.20F, 0.40F);
 		tes.addVertex(0.00F, 0.09F, 0.225F);
 		tes.addVertex(0.3464F, 0.20F, 0.20F);
 		tes.addVertex(0.1949F, 0.09F, 0.1125F);
 		tes.addVertex(0.3464F, 0.20F, -0.20F);
 		tes.addVertex(0.1949F, 0.09F, -0.1125F);
 		tes.addVertex(0.00F, 0.20F, -0.40F);
 		tes.addVertex(0.00F, 0.09F, -0.225F);
 		tes.addVertex(-0.3464F, 0.20F, -0.20F);
 		tes.addVertex(-0.1949F, 0.09F, -0.1125F);
 		tes.addVertex(-0.3464F, 0.20F, 0.20F);
 		tes.addVertex(-0.1949F, 0.09F, 0.1125F);
 		tes.addVertex(0.00F, 0.20F, 0.40F);
 		tes.addVertex(0.00F, 0.09F, 0.225F);
 
 		tes.draw();
 
 		tes.startDrawing(GL11.GL_TRIANGLE_FAN);
 
 		tes.addVertex(0.0F, 0.0F, 0.0F);
 		tes.addVertex(0.00F, 0.09F, 0.225F);
 		tes.addVertex(-0.1949F, 0.09F, 0.1125F);
 		tes.addVertex(-0.1949F, 0.09F, -0.1125F);
 		tes.addVertex(0.00F, 0.09F, -0.225F);
 		tes.addVertex(0.1949F, 0.09F, -0.1125F);
 		tes.addVertex(0.1949F, 0.09F, 0.1125F);
 		tes.addVertex(0.00F, 0.09F, 0.225F);
 
 		tes.draw();
 
 		GL11.glDisable(GL11.GL_BLEND);
 
 		GL11.glPopMatrix();
 
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glEnable(GL11.GL_LIGHTING);
 
 		GL11.glPopMatrix();
 
 	}
 
 }
