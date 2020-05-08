 package biotech.client;
 
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.client.IItemRenderer;
 
 import org.lwjgl.opengl.GL11;
 
 import biotech.Biotech;
 import biotech.client.model.ModelCheese;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class ItemRenderHandler implements IItemRenderer
 {
 	public ModelCheese	cheese;
 	
 	public ItemRenderHandler()
 	{
 		cheese = new ModelCheese();
 	}
 	
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type)
 	{
 		return true;
 	}
 	
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
 	{
 		return true;
 	}
 	
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
 	{
 		switch (type) {
             case ENTITY: {
             	renderCheeseEntity(0.0F, 0.0F, 0.0F, cheese.cheeseScale);
                 return;
             }
             case EQUIPPED: {
             	renderCheeseEquipped(3.0F, -2.0F, 3.5F, cheese.cheeseScale);
                 return;
             }
            case EQUIPPED_FIRST_PERSON: {
            	renderCheeseEquipped(3.0F, -2.0F, 3.5F, cheese.cheeseScale);
            	return;
            }
             case INVENTORY: {
             	renderCheeseInventory(0.75F, -1.25F, 0.0F, 0.4F);
                 return;
             }
             default:
                 return;
         }
 	}
 	
 	private void renderCheeseEntity(float x, float y, float z, float scale)
 	{
 		GL11.glPushMatrix();
     	GL11.glDisable(GL11.GL_LIGHTING);
 		
     	GL11.glScalef(scale, scale, scale);
     	GL11.glTranslatef(x, y, z);
 
 		FMLClientHandler.instance().getClient().renderEngine.bindTexture(Biotech.BioCheeseTexture);
 
 		cheese.render();
 		
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glPopMatrix();
 	}
 	
 	private void renderCheeseEquipped(float x, float y, float z, float scale)
 	{
 		GL11.glPushMatrix();
     	GL11.glDisable(GL11.GL_LIGHTING);
 		
     	GL11.glScalef(scale, scale, scale);
     	GL11.glRotatef(105.0F, 0.0F, 0.0F, 1.0F);
     	GL11.glRotatef(24.0F, 1.0F, 0.0F, 0.0F);
     	GL11.glRotatef(5.0F, 0.0F, 1.0F, 0.0F);
     	GL11.glTranslatef(x, y, z);
 
 		FMLClientHandler.instance().getClient().renderEngine.bindTexture(Biotech.BioCheeseTexture);
 
 		cheese.render();
 		
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glPopMatrix();
 	}
 	
 	private void renderCheeseInventory(float x, float y, float z, float scale)
 	{
 		GL11.glPushMatrix();
     	GL11.glDisable(GL11.GL_LIGHTING);
 		
     	GL11.glScalef(scale, scale, scale);
     	GL11.glTranslatef(x, y, z);
 
 		FMLClientHandler.instance().getClient().renderEngine.bindTexture(Biotech.BioCheeseTexture);
 
 		cheese.render();
 		
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glPopMatrix();
 	}
 }
