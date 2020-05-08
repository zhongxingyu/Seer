 package shadowhax.crystalluscraft.block.renderer;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 import org.lwjgl.opengl.GL11;
 import shadowhax.crystalluscraft.block.BlockCrystalOre;
 import shadowhax.crystalluscraft.block.model.ModelCrystalStage1;
 import shadowhax.crystalluscraft.block.model.ModelCrystalStage2;
 import shadowhax.crystalluscraft.block.model.ModelCrystalStage3;
 import shadowhax.crystalluscraft.block.model.ModelCrystalStage4;
 
 public class RenderCrystalOre extends TileEntitySpecialRenderer {
 
     private ModelCrystalStage1 crystalStage1;
     private ModelCrystalStage2 crystalStage2;
     private ModelCrystalStage3 crystalStage3;
     private ModelCrystalStage4 crystalStage4;
 
     public RenderCrystalOre() {
 
         crystalStage1 = new ModelCrystalStage1();
         crystalStage2 = new ModelCrystalStage2();
         crystalStage3 = new ModelCrystalStage3();
         crystalStage4 = new ModelCrystalStage4();
     }
 
     @Override
     public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f) {
 
        int meta = tileentity.blockMetadata;

         if(tileentity.blockType instanceof BlockCrystalOre) {
 
             switch(((BlockCrystalOre)tileentity.blockType).growthStage) {
 
                 case 0: {
 
                     Minecraft.getMinecraft().renderEngine.func_110577_a(CrystalTextureManager.getRLForCrystal(1, meta));
                     GL11.glPushMatrix();
                     GL11.glTranslatef((float)d0 + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
                     GL11.glScalef(1.0F, -1F, -1F);
                     crystalStage1.render(0.0625F);
                     GL11.glPopMatrix();
 
                     break;
                 }
 
                 case 1: {
 
                     Minecraft.getMinecraft().renderEngine.func_110577_a(CrystalTextureManager.getRLForCrystal(2, meta));
                     GL11.glPushMatrix();
                     GL11.glTranslatef((float)d0 + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
                     GL11.glScalef(1.0F, -1F, -1F);
                     crystalStage2.render(0.0625F);
                     GL11.glPopMatrix();
 
                     break;
                 }
 
                 case 2: {
 
                     Minecraft.getMinecraft().renderEngine.func_110577_a(CrystalTextureManager.getRLForCrystal(3, meta));
                     GL11.glPushMatrix();
                     GL11.glTranslatef((float)d0 + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
                     GL11.glScalef(1.0F, -1F, -1F);
                     crystalStage3.render(0.0625F);
                     GL11.glPopMatrix();
 
                     break;
                 }
 
                 case 3: {
 
                     Minecraft.getMinecraft().renderEngine.func_110577_a(CrystalTextureManager.getRLForCrystal(4, meta));
                     GL11.glPushMatrix();
                     GL11.glTranslatef((float)d0 + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
                     GL11.glScalef(1.0F, -1F, -1F);
                     crystalStage4.render(0.0625F);
                     GL11.glPopMatrix();
 
                     break;
                 }
             }
         }
     }
 }
