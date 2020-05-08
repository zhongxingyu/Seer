 package holo.sojourn.util;
 
 import net.minecraft.util.ResourceLocation;
 
 public class Textures 
 {
	public static final String texLoc = "texture/";
 	
     public static final ResourceLocation glyphTex = getResource(texLoc + "glyphs.png");
     public static final ResourceLocation essenceBar = getResource(texLoc + "essenceBar.png");
     public static final ResourceLocation playerInventory = new ResourceLocation("textures/gui/container/inventory.png");
     
     public static ResourceLocation getResource(String path)
     {
     	return new ResourceLocation(Strings.MAIN_MOD_ID.toLowerCase(), path);
     }
 }
