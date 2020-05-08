 package de.dariusmewes.TimoliaCustom.commands;
 
 import org.bukkit.command.CommandSender;
 
 import de.dariusmewes.TimoliaCustom.TimoliaCustom;
 
 public class tcustom extends TCommand {
 
 	public tcustom(String name) {
 		super(name);
		setMaxArgs(0);
 		setUsage("/tcustom <reload>");
 	}
 
 	public void perform(CommandSender sender, String[] args) {
 		if (args[0].equalsIgnoreCase("reload")) {
 			instance.reloadConfig();
 			sender.sendMessage(prefix + "Konfiguration neu geladen!");
 		} else if (args[0].equalsIgnoreCase("debug") && TimoliaCustom.debug) {
 			CommandHandler.list();
 		} else
 			sender.sendMessage(prefix + usage);
 	}
 
 }
