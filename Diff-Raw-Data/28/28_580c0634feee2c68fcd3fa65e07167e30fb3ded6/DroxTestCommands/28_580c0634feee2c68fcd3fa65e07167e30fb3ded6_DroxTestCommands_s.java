 package de.hydrox.bukkit.DroxPerms;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import de.hydrox.bukkit.DroxPerms.test.TestClassTemplate;
 import de.hydrox.bukkit.DroxPerms.test.TestFlatFilePermissions;
 
 public class DroxTestCommands implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		TestClassTemplate.runTests(new TestFlatFilePermissions());
		return false;
 	}
 
 }
