 package openwolves.utils;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.util.Icon;
 import net.minecraft.util.ResourceLocation;
 
 public class RenderUtil 
 {
	  public static void renderIcon(Icon icon, double size, double z, float nx, float ny, float nz) 
	  {
		  renderIcon(icon, 0.0D, 0.0D, size, size, z, nx, ny, nz);
	  }
 
 	public static void renderIcon(Icon icon, double xStart, double yStart, double xEnd, double yEnd, double z, float nx, float ny, float nz) 
 	{
 		if (icon == null) icon = getMissingIcon(TextureMap.locationItemsTexture);
 
 		Tessellator tessellator = Tessellator.instance;
 
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(nx, ny, nz);
 
 		if (nz > 0.0F) 
 		{
 			tessellator.addVertexWithUV(xStart, yStart, z, icon.getMinU(), icon.getMinV());
 			tessellator.addVertexWithUV(xEnd, yStart, z, icon.getMaxU(), icon.getMinV());
 			tessellator.addVertexWithUV(xEnd, yEnd, z, icon.getMaxU(), icon.getMaxV());
 			tessellator.addVertexWithUV(xStart, yEnd, z, icon.getMinU(), icon.getMaxV());
 		} 
 		else 
 		{
 			tessellator.addVertexWithUV(xStart, yEnd, z, icon.getMinU(), icon.getMaxV());
 			tessellator.addVertexWithUV(xEnd, yEnd, z, icon.getMaxU(), icon.getMaxV());
 			tessellator.addVertexWithUV(xEnd, yStart, z, icon.getMaxU(), icon.getMinV());
 			tessellator.addVertexWithUV(xStart, yStart, z, icon.getMinU(), icon.getMinV());
 		}
 
 		tessellator.draw();
 	}
	
 	public static Icon getMissingIcon(ResourceLocation textureSheet) 
 	{
 		return ((TextureMap)Minecraft.getMinecraft().getTextureManager().getTexture(textureSheet)).getAtlasSprite("missingno");
 	}
 }
