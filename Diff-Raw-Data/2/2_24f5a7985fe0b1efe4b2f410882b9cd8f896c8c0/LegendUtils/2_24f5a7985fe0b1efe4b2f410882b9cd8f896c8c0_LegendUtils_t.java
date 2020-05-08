 package bitlegend.legendutils;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import bitlegend.legendutils.Listeners.*;
 import bitlegend.legendutils.Commands.*;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import net.minecraft.server.EntityEnderman;
 
 import java.lang.reflect.Field;
 
 public class LegendUtils extends JavaPlugin {
 	public PermissionHandler permissionHandler;
 	public String enableOnStart = "Enabled On Startup";
 	public boolean enabled;
 	public Config config = new Config(this);
 	
 	private final LUPlayerListener playerListener = new LUPlayerListener(this);
 	private final LUEntityListener entityListener = new LUEntityListener(this);
 
 	@Override
 	public void onDisable() {
 		playerListener.clearPlayerList();
 		System.out.println("DechUtils has been disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		// Creates an event which is triggered each time a player logs in
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Normal, this);
 		// Same thing, except when they login/quit the server.
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
 		// Create a listener for entity damage
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
 
 		// Alter Enderman pickup list - Cirn9, credit for method to nisovin's
 		// EnderNerf 9/19/2011
 		try {
 			Field f = EntityEnderman.class.getDeclaredField("b");
 			f.setAccessible(true);
 			boolean[] a = (boolean[]) f.get(null);
 			for (int i = 0; i < a.length; i++) {
 				a[i] = false; // can't pick item id i up
 			}
 			String rawList = config.readString("Enderman_Blocks");
 			String[] parsedList = rawList.split(",");
 			for (int i = 0; i < parsedList.length; i++) {
 				a[Integer.parseInt(parsedList[i])] = true; //Can pickup item id put in here
 			}
 			f.set(null, a);
 			System.out.println(pdfFile.getName() + ": Enderman nerfing enabled.");
 		} catch (Exception e) {
 			System.out.println(pdfFile.getName() + ": Could not get enderman declared field!");
 		}
 		
 		//
 		getCommand("ti").setExecutor(new ToggleInventory(this));
		//
 
 		getCommand("xpcleanup").setExecutor(new XpCleanup(this));
 		setupPermissions();
 		config.configCheck();
 
 		System.out.println(pdfFile.getName() + " " + pdfFile.getVersion()
 				+ " started!");
 	}
 
 	private void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager()
 				.getPlugin("Permissions");
 		if (permissionsPlugin == null) {
 			System.out
 					.println("Permission system not detected, defaulting to OP");
 			return;
 		}
 		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 		System.out.println("Found and will use plugin "
 				+ ((Permissions) permissionsPlugin).getDescription()
 						.getFullName());
 	}
 
 }
