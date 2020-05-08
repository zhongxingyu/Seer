 package rebelkeithy.mods.keithyutils.capes;
 
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.AbstractClientPlayer;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.renderer.ImageBufferDownload;
 import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureObject;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
 import rebelkeithy.mods.keithyutils.particleregistry.ParticleRegistry;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class CapeTickHandler implements ITickHandler
 {
 	private boolean raining;
 	private boolean overlay;
 	public static int defaultFog;
 
 	public static String CapeUrl;
 	
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) 
 	{
 		if(type.equals(EnumSet.of(TickType.PLAYER)))
 		{
 			if(Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().theWorld.loadedEntityList.size() > 0)
 			{
 				List<EntityPlayer> players = Minecraft.getMinecraft().theWorld.playerEntities;
 				for(EntityPlayer player : players)
 				{
 					if(player != null && player.getEntityName().equals("RebelKeithy") || player.getEntityName().equals("Shadowclaimer"))
 					{
 						//System.out.println("tick");
 						String cloakURL = "http://i.imgur.com/AMVu0m2.png";
 						
 						if(player.getEntityData().hasKey(EntityPlayer.PERSISTED_NBT_TAG))
 						{
 							if(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).hasKey("CapeUrl"))
 							{
 								cloakURL = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getString("CapeUrl");
 							}
 						}
 						
 						//if(((AbstractClientPlayer)player).cloakUrl != cloakURL)
 						//	player.cloakUrl = cloakURL;
 
 						//Minecraft.getMinecraft().renderEngine.obtainImageData(player.cloakUrl, new ImageBufferDownload());
 					}
 				}
 			}
 		}
 
 	}
 
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) 
     {
     }
 
 	@Override
 	public EnumSet<TickType> ticks() 
 	{
 		return EnumSet.of(TickType.PLAYER);
 	}
 
 	@Override
 	public String getLabel() {
 		return "Atum.TickHandler.Player";
 	}
 

    public static void registerCapes()
    {
        String cloakURL = "http://i.imgur.com/AMVu0m2.png";
        String[] modders = { "RebelKeithy" , "Shadowclaimer" };
        
        for(String modder : modders) {
            ThreadDownloadImageData object = new ThreadDownloadImageData(cloakURL, null, null);
            Minecraft.getMinecraft().renderEngine.func_110579_a(new ResourceLocation("cloaks/" + modder), (TextureObject) object);
        }
        
    }

 }
