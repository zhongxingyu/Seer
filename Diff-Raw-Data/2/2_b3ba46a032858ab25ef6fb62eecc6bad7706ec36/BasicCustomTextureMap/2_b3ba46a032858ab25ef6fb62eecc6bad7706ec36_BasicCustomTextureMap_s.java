 package ml.core.texture.maps;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ml.core.texture.CustomTextureMap;
 import ml.core.texture.CustomTextureMapManager;
 import ml.core.texture.IIconProvider;
 import net.minecraft.util.ResourceLocation;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class BasicCustomTextureMap extends CustomTextureMap {
 
	public static final BasicCustomTextureMap GUI = new BasicCustomTextureMap(CustomTextureMapManager.getNextMapId(), "textures/gui/", "MLCore:textures/atlas/icons.png");
 	
 	
 	public List<IIconProvider> providers = new ArrayList<IIconProvider>();
 	
 	public void addProvider(IIconProvider pvdr) {
 		providers.add(pvdr);
 	}
 	
 	public BasicCustomTextureMap(int par1, String basePath, ResourceLocation resLoc) {
 		super(par1, basePath, resLoc);
 		
 		CustomTextureMapManager.instance.registerMap(this);
 	}
 	
 	public BasicCustomTextureMap(int par1, String basePath, String resLoc) {
 		this(par1, basePath, new ResourceLocation(resLoc));
 	}
 	
 	@Override
 	public void reregisterIcons() {
 		for (IIconProvider IIP : providers) {
 			IIP.registerIcons(this);
 		}
 	}
 	
 }
