 package org.Bukkitdev.Xon;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.herocraftonline.squallseed31.heroicdeath.HeroicDeath;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
 
 /**
  * KeepChunkInMemory - keeps chunks in server memory which are linked by portals
  *
  * @author Xon
  */
 public class HeroicDeathToJSONAPI extends JavaPlugin {
 	private final static Logger logger = Logger.getLogger("Minecraft");
 	protected MyCraftIRCListener ircListener = null;
 	protected JSONAPI JSONAPIHandle;
 	protected HeroicDeath HeroicDeathHandle;	
 	protected String StreamName = "deaths";
 	
 	public JSONAPI getJSONAPI()
 	{
 		return JSONAPIHandle;
 	}
 	
 	public HeroicDeath getHeroicDeath()
 	{
 		return HeroicDeathHandle;
 	}	
 
 	public String getStreamName()
 	{
 		return StreamName;
 	}	
     
 	public void log(String text)
 	{
 		logger.log(Level.INFO, text);
 	}	
 
 	public void logWarning(String text)
 	{
 		logger.log(Level.WARNING, text);
 	}
 	
 	
 	public void onEnable() 
 	{		
 		String name = this.getDescription().getName();
 		String version = this.getDescription().getVersion();
 	    Plugin checkplugin = this.getServer().getPluginManager().getPlugin("JSONAPI");
 	    Plugin checkplugin2 = this.getServer().getPluginManager().getPlugin("HeroicDeath");
         if (checkplugin == null || checkplugin2 == null) 
         {
         	logWarning(name + " cannot be loaded because JSONAPI or HeroicDeath is not enabled on the server!");
             getServer().getPluginManager().disablePlugin(this);
         } 
         else 
         {
         	log( name + " version " + version + " enabled" );
             try 
             {       	
             	HeroicDeathHandle = (HeroicDeath) checkplugin2;   
                 // Get handle to JSONAPI, add&register your custom listener
             	JSONAPIHandle = (JSONAPI) checkplugin;                
                 ircListener = new MyCraftIRCListener(this);
                 this.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, ircListener, Priority.Normal, this);
             
             } 
             catch (ClassCastException ex) 
             {
                 ex.printStackTrace();
                 logWarning(name + " can't cast plugin handle as JSONAPI or HeroicDeath  plugin!");
                 getServer().getPluginManager().disablePlugin(this);
             }
             
         }
 	}
 	
 	public void onDisable() 
 	{	
 		// NOTE: All registered events are automatically unregistered when a plugin is disabled
 		log("Unloading "+this.getDescription().getName());			
 	}   		
     
 }
 
