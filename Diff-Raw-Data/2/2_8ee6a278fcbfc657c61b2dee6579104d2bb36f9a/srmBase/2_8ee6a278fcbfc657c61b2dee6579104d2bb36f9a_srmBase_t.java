 package srm;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 
 @Mod(modid = "Sustainable Resources Mod")
 
 public class srmBase
 {
 	
 	@Init
 	
 	public void init(FMLInitializationEvent event)
 	{
 		System.out.println("Initilizing Sustainable Resources Mod  (SMR)");
		System.out.println("You are using a pre-alpha build!");
 	}
 	
 }
