 package clashsoft.mods.moretools.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import clashsoft.mods.moretools.common.MTMCommonProxy;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class MTMClientProxy extends MTMCommonProxy
 {
 	public Map<String, Integer>	armorIndices	= new HashMap();
 	
 	public static int			space;
 	public static int			hallowed;
 	public static int			goddawn;
 	public static int			invisibility;
 	public static int			glowstone;
 	
 	@Override
 	public void registerRenderers()
 	{
 		space = getArmorIndex("space");
 		hallowed = getArmorIndex("hallowed");
 		goddawn = getArmorIndex("goddawn");
 		invisibility = getArmorIndex("invisibility");
 		glowstone = getArmorIndex("glowstone");
 	}
 	
 	@Override
 	public int getArmorIndex(String name)
 	{
 		Integer integer = armorIndices.get(name);
 		if (integer == null)
 		{
			int i = RenderingRegistry.addNewArmourRendererPrefix("moretools:" + name);
 			integer = Integer.valueOf(i);
 			armorIndices.put(name, integer);
 		}
 		return integer.intValue();
 	}
 }
