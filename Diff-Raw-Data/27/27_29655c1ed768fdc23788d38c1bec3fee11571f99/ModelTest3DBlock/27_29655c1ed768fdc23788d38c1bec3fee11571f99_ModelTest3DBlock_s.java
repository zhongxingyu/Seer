 package org.pixotic.xcraft.client.model;
 
 import org.lwjgl.opengl.GL11;
 import org.pixotic.xcraft.lib.Reference;
 import org.pixotic.xcraft.tileentity.TileTest3DBlock;
 
 import cpw.mods.fml.client.FMLClientHandler;
 
 import net.minecraftforge.client.model.AdvancedModelLoader;
 import net.minecraftforge.client.model.IModelCustom;
 
 public class ModelTest3DBlock {
     
     private IModelCustom modelTest3DBlock;
     
     public ModelTest3DBlock() {
         modelTest3DBlock = AdvancedModelLoader.loadModel("/mods/xcraft/models/TutBox.obj");
     }
     
     public void render() {
         modelTest3DBlock.renderAll();
     }
     
     public void render(TileTest3DBlock block, double x, double y, double z) {
         
         //Push blank matrix onto stack
         GL11.glPushMatrix();
         
         //Move object to right position on block (true obj origin is center of object)
         GL11.glTranslatef((float)x + 0.5f, (float)y + 0.5f, (float)z + 0.5f);
         
         //Scale object 50%
         GL11.glScalef(0.5f,  0.5f, 0.5f);
        GL11.glColor3f(0, 0, 0);
         
         //Bind texture so ogl properly uses it
         FMLClientHandler.instance().getClient().renderEngine.bindTexture("/mods/xcraft/textures/models/TutBox.png");
         
         //Render!
         this.render();
         
         //Pop matrix from stack
         GL11.glPopMatrix();
         
     }
     
 }
