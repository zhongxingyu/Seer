 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.timolia.custom.cmds;
 
 import org.bukkit.command.CommandSender;
 
public final class tcustom extends TCommand {
 
 	protected void prepare() {
 		permission();
 		minArgs(1);
 		maxArgs(1);
 	}
 
 	public void perform(final CommandSender sender, String[] args) {
 		if (args[0].equalsIgnoreCase("reload")) {
 			instance.reloadConfig();
 			sender.sendMessage(prefix + "Konfiguration neu geladen!");
 		} else
 			sender.sendMessage(usage);
 	}
 
 }
