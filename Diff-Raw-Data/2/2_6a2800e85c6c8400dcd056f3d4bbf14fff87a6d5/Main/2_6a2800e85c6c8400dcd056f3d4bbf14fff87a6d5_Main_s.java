 package net.othercraft.steelsecurity;
 
 import net.othercraft.steelsecurity.commands.Sts;
 import net.othercraft.steelsecurity.listeners.ChatFilter;
 import net.othercraft.steelsecurity.listeners.JoinMessage;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 
 	public static Main instance;
 
 	private Sts base;
 	
 	@SuppressWarnings("unused")
 	private ChatFilter cf;
	@SuppressWarnings("unused")
 	private JoinMessage jm;
 
 	public void onEnable(){
 		new Config(this).loadConfiguration();
 		instance = this;
 		listeners();
 		commands();
 	}
 	private void commands() {//register commands here
 		base = new Sts("base");
 		getCommand("sts").setExecutor(base);
 	}
 	private void listeners() {//register listeners here
 		cf = new ChatFilter(null, this);
 		jm = new JoinMessage(null, this);
 	}
 	public void onDisable(){
 	}
 }
