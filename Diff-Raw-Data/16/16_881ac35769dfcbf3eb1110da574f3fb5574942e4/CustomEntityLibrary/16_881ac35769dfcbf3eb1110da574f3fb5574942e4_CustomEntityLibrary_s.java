 package kabbage.customentitylibrary;
 
 import kabbage.customentitylibrary.listeners.LibraryEntityListener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CustomEntityLibrary
 {	
 	public static JavaPlugin plugin;
 	
 	public static void load(JavaPlugin plugin)
 	{
 		CustomEntityLibrary.plugin = plugin;
 		LibraryEntityListener eListener = new LibraryEntityListener();
 		PluginManager pm = Bukkit.getServer().getPluginManager();
 		eListener.registerEvents(pm, plugin);
 		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				onTick();
 			}
 		}, 1L, 1L);
 		
 	}
 	
 	private static int tick = 0;
 	private static void onTick()
 	{
 		if(tick % 20 == 0)
 		{
 			for(CustomEntityWrapper ent : CustomEntityWrapper.customEntities.values())
				ent.getType().showSpecialEffects((LivingEntity) ent.getEntity().getBukkitEntity());
 		}
 		tick++;
 	}
 }
