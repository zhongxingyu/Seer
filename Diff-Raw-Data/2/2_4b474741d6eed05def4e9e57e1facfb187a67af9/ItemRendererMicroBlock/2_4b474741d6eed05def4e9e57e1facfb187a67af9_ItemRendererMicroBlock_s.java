 
 package me.heldplayer.mods.Smartestone.client.renderer;
 
 import me.heldplayer.api.Smartestone.micro.IMicroBlockMaterial;
 import me.heldplayer.api.Smartestone.micro.IMicroBlockSubBlock;
 import me.heldplayer.api.Smartestone.micro.MicroBlockAPI;
 import me.heldplayer.mods.Smartestone.client.ClientProxy;
 import me.heldplayer.mods.Smartestone.item.ItemMicroBlock;
 import me.heldplayer.util.HeldCore.client.MC;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.Icon;
 import net.minecraftforge.client.IItemRenderer;
 
 import org.lwjgl.opengl.GL11;
 
 public class ItemRendererMicroBlock implements IItemRenderer {
 
     public static final double[] defaultBounds = new double[] { 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D };
 
     public ItemRendererMicroBlock() {}
 
     @Override
     public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP && type != ItemRenderType.FIRST_PERSON_HOLDING;
     }
 
     @Override
     public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
         return true;
     }
 
     @Override
     public void renderItem(ItemRenderType renderType, ItemStack stack, Object... data) {
         if (!(data[0] instanceof RenderBlocks)) {
             return;
         }
         RenderBlocks render = (RenderBlocks) data[0];
 
         if (!(stack.getItem() instanceof ItemMicroBlock)) {
             return;
         }
 
         ItemMicroBlock item = (ItemMicroBlock) stack.getItem();
 
         NBTTagCompound compound = stack.getTagCompound();
 
         IMicroBlockSubBlock type = null;
         IMicroBlockMaterial material = null;
         if (compound != null) {
             type = MicroBlockAPI.getSubBlock(compound.getString("Type"));
             material = MicroBlockAPI.getMaterial(compound.getString("Material"));
         }
 
         Icon[] icons = new Icon[6];
 
         for (int i = 0; i < icons.length; i++) {
             if (material != null) {
                 icons[i] = material.getIcon(i);
             }
             else {
                 icons[i] = ClientProxy.missingTextureIcon;
             }
         }
 
         if (icons[0] == null) {
             return;
         }
 
         double[] bounds;
         if (type != null) {
             bounds = type.getRenderBounds();
         }
         else {
             bounds = defaultBounds;
         }
 
         render.setRenderBounds(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
         render.uvRotateBottom = 0;
         render.uvRotateTop = 0;
         render.uvRotateNorth = 0;
         render.uvRotateSouth = 0;
         render.uvRotateWest = 0;
         render.uvRotateEast = 0;
         render.flipTexture = false;
 
         Tessellator tes = Tessellator.instance;
 
         GL11.glPushMatrix();
 
         if (renderType == ItemRenderType.INVENTORY) {
             GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         }
         if (renderType == ItemRenderType.ENTITY) {
             GL11.glScalef(0.5F, 0.5F, 0.5F);
             GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         }
 
         if (item.getSpriteNumber() == 0) {
             MC.getRenderEngine().bindTexture(TextureMap.locationBlocksTexture);
         }
         else {
             MC.getRenderEngine().bindTexture(TextureMap.locationItemsTexture);
         }
 
         if (renderType != ItemRenderType.INVENTORY && material != null && material.getRenderPass() > 0) {
             GL11.glEnable(GL11.GL_BLEND);
             GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
         }
 
         tes.startDrawingQuads();
 
         tes.setNormal(0.0F, -1.0F, 0.0F);
         render.renderFaceYNeg(null, 0, 0, 0, icons[0]);
 
         tes.setNormal(0.0F, 1.0F, 0.0F);
         render.renderFaceYPos(null, 0, 0, 0, icons[1]);
 
         tes.setNormal(0.0F, 0.0F, -1.0F);
         render.renderFaceZNeg(null, 0, 0, 0, icons[2]);
 
         tes.setNormal(0.0F, 0.0F, 1.0F);
         render.renderFaceZPos(null, 0, 0, 0, icons[3]);
 
         tes.setNormal(-1.0F, 0.0F, 0.0F);
         render.renderFaceXNeg(null, 0, 0, 0, icons[4]);
 
         tes.setNormal(1.0F, 0.0F, 0.0F);
         render.renderFaceXPos(null, 0, 0, 0, icons[5]);
 
         tes.draw();
 
         if (renderType != ItemRenderType.INVENTORY && material != null && material.getRenderPass() > 0) {
             GL11.glDisable(GL11.GL_BLEND);
         }
 
         GL11.glPopMatrix();
     }
 
 }
