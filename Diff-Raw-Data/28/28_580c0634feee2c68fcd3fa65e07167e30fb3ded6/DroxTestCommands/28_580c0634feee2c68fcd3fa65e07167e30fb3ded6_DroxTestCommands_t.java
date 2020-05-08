 package de.hydrox.bukkit.DroxPerms;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import de.hydrox.bukkit.DroxPerms.test.TestClassTemplate;
 import de.hydrox.bukkit.DroxPerms.test.TestFlatFilePermissions;
 
 public class DroxTestCommands implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender.hasPermission("droxperms.test"))) {
			sender.sendMessage("You don't have permission to run the TestSuite.");
			return true;
		}
 		TestClassTemplate.runTests(new TestFlatFilePermissions());
		return true;
 	}
 
 }
