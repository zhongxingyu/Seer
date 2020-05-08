 package com.tehbeard.forge.schematic.shell.render;
 
 import org.lwjgl.opengl.GL11;
 
 import com.tehbeard.forge.schematic.SchVector;
 import com.tehbeard.forge.schematic.shell.LibSchematicShell;
 
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraftforge.client.event.RenderWorldLastEvent;
 import net.minecraftforge.event.ForgeSubscribe;
 
 
 public class AABBRender {
 
     private double playerX;
     private double playerY;
     private double playerZ;
 
 
 
 
     //Color
 
     private Col area = new Col(1.0f,0.0f,0.0f);
 
     private Col corner1 = new Col(1.0f,0.8f,0.07f);
 
     //003F87
     private Col corner2 = new Col(0.0f,0.25f,0.53f);
 
     public float colorA = 1.0f;
 
 
     private class Col{
         public float colorR = 1.0f;
         public float colorG = 0.0f;
         public float colorB = 0.0f;
         public Col(float colorR, float colorG, float colorB) {
             super();
             this.colorR = colorR;
             this.colorG = colorG;
             this.colorB = colorB;
         }
 
 
     }
 
 
 
     //public boolean active = true; 
 
 
     /**
      * Convert an bounding box relative to the player position for rendering.
      * 
      * @param bb
      * @param playerX
      * @param playerY
      * @param playerZ
      * @return
      */
     private AxisAlignedBB getRenderBoundingBox(BBoxInt bb) {
         AxisAlignedBB aabb = bb.toAxisAlignedBB();
         aabb.maxX += 1;
         aabb.maxY += 1;
         aabb.maxZ += 1;
         double expandBy = 0.005F;
         return aabb.expand(expandBy, expandBy, expandBy).getOffsetBoundingBox(-playerX, -playerY, -playerZ);
     }
 
     @ForgeSubscribe
     public void renderWorldLastEvent(RenderWorldLastEvent event) {
         // determine current player location
         EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
         playerX = entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * (double) event.partialTicks;
         playerY = entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * (double) event.partialTicks;
         playerZ = entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * (double) event.partialTicks;
 
 
         ItemStack currentItem = entityPlayer.inventory.getCurrentItem();
         if(currentItem == null){return;}
         if(currentItem.itemID == LibSchematicShell.setSquareItem.itemID){
             SchVector p1 = LibSchematicShell.setSquareItem.getPos1(currentItem);
             SchVector p2 = LibSchematicShell.setSquareItem.getPos2(currentItem);
 
             SchVector min = SchVector.min(p1, p2);
             SchVector max = SchVector.max(p1, p2);
 
             renderSelection(new BBoxInt(min, max),area,true);
             renderSelection(new BBoxInt(min, min),corner1,false);
             renderSelection(new BBoxInt(max, max),corner2,false);
 
         }
     }
 
     private void renderSelection(BBoxInt bBoxInt,Col col,boolean fill){
         //initialise
 
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glLineWidth(3.0f);
         GL11.glDisable(GL11.GL_TEXTURE_2D);
         GL11.glDisable(GL11.GL_CULL_FACE);
         GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
 
        //TODO - Render shit here


         AxisAlignedBB aabb = getRenderBoundingBox(bBoxInt);
         //Render fill
         if(fill){
             GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
             colorA  = 0.2f;
             renderOutline(aabb,col);
             GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
             GL11.glPolygonOffset(-1.f,-1.f);
             colorA = 1.0f;
         }
         //Render outline
         GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
         renderOutline(aabb,col);
 
 
         //Reset openGL 
         GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
         GL11.glEnable(GL11.GL_CULL_FACE);
         GL11.glEnable(GL11.GL_TEXTURE_2D);
         GL11.glDisable(GL11.GL_BLEND);
 
     }
 
     private void renderOutline(AxisAlignedBB bb,Col col) {
         Tessellator tessellator = Tessellator.instance;
         //System.out.println(bb);
 
         tessellator.startDrawing(GL11.GL_QUADS);
         tessellator.setColorRGBA_F(col.colorR, col.colorG, col.colorB, colorA);
         tessellator.addVertex(bb.minX, bb.minY, bb.minZ);
         tessellator.addVertex(bb.maxX, bb.minY, bb.minZ);
         tessellator.addVertex(bb.maxX, bb.minY, bb.maxZ);
         tessellator.addVertex(bb.minX, bb.minY, bb.maxZ);
         tessellator.draw();
 
         tessellator.startDrawing(GL11.GL_QUADS);
         tessellator.setColorRGBA_F(col.colorR, col.colorG, col.colorB, colorA);
         tessellator.addVertex(bb.minX, bb.maxY, bb.minZ);
         tessellator.addVertex(bb.maxX, bb.maxY, bb.minZ);
         tessellator.addVertex(bb.maxX, bb.maxY, bb.maxZ);
         tessellator.addVertex(bb.minX, bb.maxY, bb.maxZ);
         tessellator.draw();
 
         tessellator.startDrawing(GL11.GL_QUADS);
         tessellator.setColorRGBA_F(col.colorR, col.colorG, col.colorB, colorA);
         tessellator.addVertex(bb.minX, bb.minY, bb.maxZ);
         tessellator.addVertex(bb.minX, bb.maxY, bb.maxZ);
         tessellator.addVertex(bb.maxX, bb.maxY, bb.maxZ);
         tessellator.addVertex(bb.maxX, bb.minY, bb.maxZ);
         tessellator.draw();
 
         tessellator.startDrawing(GL11.GL_QUADS);
         tessellator.setColorRGBA_F(col.colorR, col.colorG, col.colorB, colorA);
         tessellator.addVertex(bb.minX, bb.minY, bb.minZ);
         tessellator.addVertex(bb.minX, bb.maxY, bb.minZ);
         tessellator.addVertex(bb.maxX, bb.maxY, bb.minZ);
         tessellator.addVertex(bb.maxX, bb.minY, bb.minZ);
         tessellator.draw();
 
         tessellator.startDrawing(GL11.GL_QUADS);
         tessellator.setColorRGBA_F(col.colorR, col.colorG, col.colorB, colorA);
         tessellator.addVertex(bb.minX,bb.minY, bb.minZ);
         tessellator.addVertex(bb.minX,bb.minY, bb.maxZ);
         tessellator.addVertex(bb.minX,bb.maxY, bb.maxZ);
         tessellator.addVertex(bb.minX,bb.maxY, bb.minZ);
         tessellator.draw();
 
         tessellator.startDrawing(GL11.GL_QUADS);
         tessellator.setColorRGBA_F(col.colorR, col.colorG, col.colorB, colorA);
         tessellator.addVertex(bb.maxX,bb.minY, bb.minZ);
         tessellator.addVertex(bb.maxX,bb.minY, bb.maxZ);
         tessellator.addVertex(bb.maxX,bb.maxY, bb.maxZ);
         tessellator.addVertex(bb.maxX,bb.maxY, bb.minZ);
         tessellator.draw();
     }
 }
