 package couk.MineCode.MineMail;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import couk.MineCode.Commands.CommandCore;
 import couk.MineCode.Composition.MailComposer;
 import couk.MineCode.Database.MailDatabase;
 import couk.MineCode.Hooks.Hooks;
 
 public class MineMail extends JavaPlugin {
 	
 	public static Plugin MineMail;
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static String version;
 	public static Economy economy = null;
 	public static Permission permission = null;
 
 	@Override
 	public void onDisable() {
 		//unload code
 		System.out.println("[MineMail] MineMail un-loaded successfully.");//MineLog class unloads before this method can be called
 	}
 
 	@Override
 	public void onEnable() {
 		//load code
 		version = this.getDescription().getVersion();
 		MineMail = this;
 		this.getCommand("mail").setExecutor(new CommandCore());
		new MailComposer(this); //Setup Chat Listener
 		MailDatabase.createFolders();	
 		new Hooks();
 	}
 
 }
