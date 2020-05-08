 package mcextreme.core.utils;
 
 import java.lang.reflect.Field;
 import java.util.logging.Level;
 
 import mcextreme.core.MCExtremeCore;
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemBlock;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class MCExtremeUtils 
 {    
 	public static void printModInfo()
 	{
		String pluginsFound = "[MC-X] Installed plugins:";
     	boolean first = true;
     	
     	String[] classes = new String[] { "magic.MCExtremeMagic", "tech.MCExtremeTech", "vtweaks.MCExtremeVTweaks" };
     	
     	for (int i = 0; i < classes.length; i++)
     	{
     		String pluginVersion = "", label = "";
     		boolean failed = false;
     		
     		//Kodehawa code: Fixed reflection usage for independent versioning system
    		//To General: DO NOT CHANGE THIS CODE, pleeease :)
     		
     		try{
     			Class clazz = Class.forName("mcextreme."+classes[i]);
     			if(clazz != null){
     				Field name = clazz.getDeclaredField("pluginName");
     			    Field version = clazz.getDeclaredField("version");
     			    if(name != null && version != null){
     			    	name.setAccessible(true);
     			        version.setAccessible(true);
     			        pluginVersion = (String)version.get(null);
     			        label = (String)name.get(null);
    	    		    label = (first ? " " : ", ")+label+"["+pluginVersion+"]";
        			    first = false;
     			    }
     			}
     		}
     		catch(Exception exception){
     			exception.printStackTrace();
     			failed = true;
     		}
     		
     		if(!failed){
     			pluginsFound = pluginsFound + label;
     		}
     	}
     	
     	FMLLog.log(Level.INFO, "[MC-X] Loaded MC to the Extreme version " + MCExtremeCore.version + " for MC " + MCExtremeCore.mcversion);
     	FMLLog.log(Level.INFO, pluginsFound); 
 	}
 
     public static void registerDefaultBlocks(Block... blocks)
     {
         for (int i = 0; i < blocks.length; i++)
         {
             registerDefaultBlock(blocks[i]);
         }
     }
     
     public static void registerDefaultBlock(Block block)
     {
         GameRegistry.registerBlock(block, ItemBlock.class, null);
     }
 }
