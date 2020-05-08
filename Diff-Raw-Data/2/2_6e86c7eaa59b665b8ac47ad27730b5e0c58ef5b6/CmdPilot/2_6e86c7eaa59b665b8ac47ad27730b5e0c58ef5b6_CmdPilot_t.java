 package net.dmulloy2.autocraft.commands;
 
 import net.dmulloy2.autocraft.AutoCraft;
 import net.dmulloy2.autocraft.permissions.Permission;
 import net.dmulloy2.autocraft.ships.Ship;
 
 public class CmdPilot extends AutoCraftCommand {
 
 	public CmdPilot(AutoCraft plugin) {
 		super(plugin);
 		this.name = "pilot";
 		this.description = "Use to pilot ships";
 		this.mustBePlayer = true;
 		this.permission = Permission.CMD_PILOT;
 		this.requiredArgs.add("ship type");
 		this.aliases.add("p");
 	}
 	
 	@Override
 	public void perform() {
		if (plugin.getShipManager().isPilotingShip(player)) {
 			err("You are already piloting a ship!");
 			return;
 		}
 		
 		String shipName = args[0].toLowerCase();
 		if (! plugin.getDataHandler().isValidShip(shipName)) {
 			err("Could not find a ship by the name of {0}", shipName);
 			return;
 		}
 		
 		if (! player.hasPermission("autocraft." + shipName)) {
 			err("You do not have permission to fly this ship!");
 			return;
 		}
 		
 		new Ship(player, plugin.getDataHandler().getData(shipName), plugin);
 	}
 }
