 package universalelectricity;
 
 import java.io.File;
 
 import net.minecraft.src.MapColor;
 import net.minecraft.src.Material;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.ForgeVersion;
 import cpw.mods.fml.common.Loader;
 
 public class UniversalElectricity
 {
 	public static final int MAJOR_VERSION = 0;
 	public static final int MINOR_VERSION = 9;
 	public static final int REVISION_VERSION = 2;
 	
 	public static final String VERSION = MAJOR_VERSION+"."+MINOR_VERSION+"."+REVISION_VERSION;
 	
 	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "UniversalElectricity/UniversalElectricity.cfg"));
     
     //EU to Watts ratio
  	public static final float IC2_RATIO = (float)UEConfig.getConfigData(CONFIGURATION, "IndustrialCraft Conversion Ratio", 300);
   	
   	//MJ to Watts ratio.
  	public static final float BC3_RATIO = (float)UEConfig.getConfigData(CONFIGURATION, "BuildCraft Conversion Ratio", 5000);;
   	
   	public static final float TO_IC2_RATIO = 1/IC2_RATIO;
   	
   	public static final float TO_BC_RATIO = 1/BC3_RATIO;
     
     /**
 	 * Use this material for all your machine blocks. It can be breakable by hand.
 	 */
 	public static final Material machine = new Material(MapColor.ironColor);
     
 	public static void versionLock(int major, int minor, int revision, boolean strict)
     {
     	if(MAJOR_VERSION != major)
 		{
 			throw new RuntimeException("Universal Electricity wrong version! Require v"+major+"."+minor+"."+revision);
 		}
 		
     	if(MINOR_VERSION < minor)
 		{
 			throw new RuntimeException("Universal Electricity minor version is too old! Require v"+major+"."+minor+"."+revision);
 		}
     	
     	if(REVISION_VERSION < revision)
 		{
     		if(strict)
     		{
     			throw new RuntimeException("Universal Electricity is too old! Require v"+major+"."+minor+"."+revision);
     		}
     		else
     		{
     			System.out.println("Universal Electricity is not the specified version. Odd things might happen. Require "+major+"."+minor+"."+revision);
     		}
 		}
     }
 	
     public static void forgeLock(int major, int minor, int revision, boolean strict)
     {
     	if(ForgeVersion.getMajorVersion() != major)
 		{
 			throw new RuntimeException("Universal Electricity: Wrong Minecraft Forge version! Require "+major+"."+minor+"."+revision);
 		}
 		
     	if(ForgeVersion.getMinorVersion() < minor)
 		{
 			throw new RuntimeException("Universal Electricity: Minecraft Forge minor version is too old! Require "+major+"."+minor+"."+revision);
 		}
     	
     	if(ForgeVersion.getRevisionVersion() < revision)
 		{
     		if(strict)
     		{
     			throw new RuntimeException("Universal Electricity: Minecraft Forge revision version is too old! Require "+major+"."+minor+"."+revision);
     		}
     		else
     		{
     			System.out.println("Universal Electricity Warning: Minecraft Forge is not the specified version. Odd things might happen. Require "+major+"."+minor+"."+revision);
     		}
 		}
     }
     
     public static void forgeLock(int major, int minor, int revision)
     {
     	forgeLock(major, minor, revision, false);
     }
 }
