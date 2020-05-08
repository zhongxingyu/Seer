 package cooler.utils;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 public class Config {
 
    public static int fleshID = 8000, coolerID = 8001;
 
     public static void init(FMLPreInitializationEvent event) {
 
         File configFile = new File(
                 event.getModConfigurationDirectory(),
                 Registry.name + ".cfg");
 
         Configuration configuration = new Configuration(
                 configFile);
 
         try{
             configuration.load();
 
             // *************************Item config's***********************
 
            fleshID = configuration.getItem(
                     Registry.flesh + " Item", 9000 - 256,
                     Registry.flesh + " Item Id:").getInt();
 
            coolerID = configuration.getItem(
                     Registry.cooler + " Item", 9001 - 256,
                     Registry.cooler + " Item Id:").getInt();
             
         }catch(Exception e){
             FMLLog.log(
                     Level.SEVERE,
                     e,
                     Registry.name
                             + " has had a problem loading its configuration");
         }finally{
             configuration.save();
         }
     }
 }
