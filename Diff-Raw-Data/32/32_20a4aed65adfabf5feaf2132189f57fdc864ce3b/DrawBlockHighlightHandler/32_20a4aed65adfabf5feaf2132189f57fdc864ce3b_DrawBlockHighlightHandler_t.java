 /**
  * Dark Beam DrawBlockHighlightHandler.java
  * 
  * @author krakel
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 package de.krakel.darkbeam.client.renderer;
 
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.item.Item;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import net.minecraftforge.client.event.DrawBlockHighlightEvent;
 
 import org.lwjgl.opengl.GL11;
 
 import de.krakel.darkbeam.core.DarkLib;
 import de.krakel.darkbeam.lib.BlockType;
 
 public class DrawBlockHighlightHandler {
 	private static void highlight( World world, int x, int y, int z) {
 		int id = world.getBlockId( x, y, z);
 		if (id > 0) {
 			GL11.glEnable( GL11.GL_BLEND);
 			GL11.glDisable( GL11.GL_TEXTURE_2D);
 			GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glColor4f( 0F, 0F, 0F, 0.8F);
 			GL11.glLineWidth( 3F);
 			GL11.glDepthMask( false);
 			double minX = 0D;
 			double maxX = 1D;
 			double posY = -DarkLib.BOX_BORDER_HEIGHT;
 			double minZ = 0D;
 			double maxZ = 1D;
 			Tessellator tess = Tessellator.instance;
 			tess.startDrawing( 3);
 			tess.addVertex( maxX, posY, minZ);
 			tess.addVertex( minX, posY, minZ);
 			tess.addVertex( minX, posY, maxZ);
 			tess.addVertex( maxX, posY, maxZ);
 			tess.draw();
 			GL11.glDepthMask( true);
 			GL11.glEnable( GL11.GL_TEXTURE_2D);
 			GL11.glDisable( GL11.GL_BLEND);
 		}
 	}
 
//	@ForgeSubscribe
 	public void onHighlightEvent( DrawBlockHighlightEvent event) {
 		if (event.currentItem == null) {
 			return;
 		}
 		if (event.target.typeOfHit != EnumMovingObjectType.TILE) {
 			return;
 		}
 		Item item = event.currentItem.getItem();
 		if (item.itemID != BlockType.Masking.getId()) {
 			return;
 		}
 		MovingObjectPosition pos = event.target;
 		highlight( event.player.worldObj, pos.blockX, pos.blockY, pos.blockZ);
 	}
 }
