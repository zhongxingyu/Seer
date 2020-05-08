 package assets.fyresmodjam;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.client.model.ModelSign;
 import net.minecraft.client.renderer.OpenGlHelper;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import org.lwjgl.opengl.GL11;
 
 public class TileEntityTrapRenderer extends TileEntitySpecialRenderer {
 	
     private ModelBase[] models = {new ModelSpikes(), new ModelTrap2()};
 
     public static ResourceLocation[] textures = {new ResourceLocation("fyresmodjam", "textures/blocks/spikes.png"), new ResourceLocation("fyresmodjam", "textures/blocks/trap2.png")};
     
     public void renderTileEntityAt(TileEntity tileEntity, double d, double d1, double d2, float f)
     {
         GL11.glPushMatrix();
         GL11.glTranslatef((float)d, (float)d1, (float)d2);
         TileEntityTrap tileEntityYour = (TileEntityTrap) tileEntity;
         renderBlockYour(tileEntityYour, tileEntity.worldObj, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, ModjamMod.blockTrap);
         GL11.glPopMatrix();
     }
 
     public void renderBlockYour(TileEntityTrap tl, World world, int i, int j, int k, Block block)
     {
         Tessellator tessellator = Tessellator.instance;
         float f = block.getBlockBrightness(world, i, j, k);
         int l = world.getLightBrightnessForSkyBlocks(i, j, k, 0);
         int l1 = l % 65536;
         int l2 = l / 65536;
         tessellator.setColorOpaque_F(f, f, f);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)l1, (float)l2);
        
         EntityPlayer player = Minecraft.getMinecraft().thePlayer;
 		boolean active = player != null && (player.isSneaking() || (player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals("Scout")));
 		
 		int type = world.getBlockMetadata(i, j, k);
 		
 		if(active) {
 	        GL11.glPushMatrix();
 	        
	        //GL11.glEnable(GL11.GL_BLEND);
	        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 	        
 	        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
 	        GL11.glTranslatef(0.5F, -1.5F, -0.5F);
 	        
 	        this.tileEntityRenderer.renderEngine.func_110577_a(textures[type % BlockTrap.trapTypes]);
 	        this.models[type % BlockTrap.trapTypes].render((Entity) null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
 	        
	        //GL11.glDisable(GL11.GL_BLEND);
 	        
 	        GL11.glPopMatrix();
 		}
     }
 }
