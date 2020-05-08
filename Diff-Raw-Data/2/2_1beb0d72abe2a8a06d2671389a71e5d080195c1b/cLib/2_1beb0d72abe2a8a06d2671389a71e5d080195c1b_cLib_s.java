 package net.catharos.lib;
 
 import net.catharos.lib.command.CommandManager;
 import net.catharos.lib.plugin.Plugin;
 
 
 public class cLib extends Plugin {
 	protected CommandManager cmdManager;
 	
 	
 	@Override
 	public void onLoad() {
 		this.cmdManager = new CommandManager();
 	}
 	
 	public static cLib getInstance() {
		return (cLib) getInstance();
 	}
 	
 	public CommandManager getCommandManager() {
 		return this.cmdManager;
 	}
 	
 }
