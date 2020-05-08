 package za.dats.bukkit.buildingplanner.commands;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import za.dats.bukkit.buildingplanner.BuildingPlanner;
 import za.dats.bukkit.buildingplanner.model.PlanArea;
 import za.dats.bukkit.buildingplanner.model.PlanArea.OpType;
 
 public class PlanPluginCommand implements CommandExecutor {
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 	if (args.length > 0) {
 	    Player player = (Player) sender;
 
 	    if ("report".equals(args[0])) {
 		reportPlan(player, args);
 		return true;
 	    }
 	    
 	    if ("list".equals(args[0])) {
 		listPlans(player);
 		return true;
 	    }
 	    
 	    if ("send".equals(args[0])) {
 		sendReport(player, args);
 		return true;
 	    }
 
 	    if ("invite".equals(args[0])) {
 		invite(player, args);
 		return true;
 	    }
 	    
 	    if ("uninvite".equals(args[0])) {
 		uninvite(player, args);
 		return true;
 	    }
 	    
 	    if ("give".equals(args[0])) {
 		give(player, args);
 		return true;
 	    }
 	    
 	    if ("clear".equals(args[0])) {
 		ItemStack[] contents = player.getInventory().getContents();
 		for (int i = 0; i < contents.length; i++) {
 		    player.getInventory().setItem(i, null);
 		}
 	    }
 	}
 	return true;
     }
 
     private void give(Player player, String[] args) {
 	if (!player.hasPermission("buildingplanner.give")) {
 	    player.sendMessage("You do not have permission to give plan areas");
 	    return;
 	}
 	
 	if (args.length < 2) {
 	    player.sendMessage("You need to specify a player to give to");
 	    return;
 	}
 	Player recipient = BuildingPlanner.plugin.getServer().getPlayer(args[1]);
 	
 	PlanArea area = null;
 	if (args.length > 2) {
 	    String areaName = null;
 	    areaName = args[2];
 	    area = BuildingPlanner.plugin.areaManager.getAreaByName(areaName);
 	}
 
 	if (area == null) {
 	    area = BuildingPlanner.plugin.areaManager.getAffectedArea(player.getLocation().getBlock());
 	}
 	
 	if (area == null) {
 	    player.sendMessage("You need to be in an area or specify an area by name to give to a player");
 	    return;
 	}
 	
 	if (!area.checkPermission(player, OpType.GIVE, true)) {
 	    player.sendMessage("You do not have permission to give plan areas");
 	    return;
 	}
 	
 	area.changeOwner(recipient.getName());
 	player.sendMessage("You have given "+player.getName()+" ownership of the area: "+area.getAreaName());
 	recipient.sendMessage(player.getName()+" has given you ownership of the area: "+area.getAreaName());
     }
 
     private void invite(Player player, String[] args) {
 	if (!player.hasPermission("buildingplanner.invite")) {
 	    player.sendMessage("You do not have permission to invite collaborators");
 	    return;
 	}
 	
 	if (args.length < 2) {
 	    player.sendMessage("You need to specify a player to invite");
 	    return;
 	}
 	Player recipient = BuildingPlanner.plugin.getServer().getPlayer(args[1]);
 	
 	PlanArea area = null;
 	if (args.length > 2) {
 	    String areaName = null;
 	    areaName = args[2];
 	    area = BuildingPlanner.plugin.areaManager.getAreaByName(areaName);
 	}
 
 	if (area == null) {
 	    area = BuildingPlanner.plugin.areaManager.getAffectedArea(player.getLocation().getBlock());
 	}
 	
 	if (area == null) {
 	    player.sendMessage("You need to be in an area or specify an area by name to invite a player");
 	    return;
 	}
 	
 	if (!area.checkPermission(player, OpType.INVITE, true)) {
 	    player.sendMessage("You do not have permission to invite collaborators");
 	    return;
 	}
 	
 	player.sendMessage("You have added "+player.getName()+" as a collaborator for the area: "+area.getAreaName());
 	recipient.sendMessage(player.getName()+" has added you as a collaborator for the area: "+area.getAreaName());
 	area.addCollaborator(recipient.getName());
     }
 
     private void uninvite(Player player, String[] args) {
 	if (args.length < 2) {
 	    player.sendMessage("You need to specify a player to uninvite");
 	    return;
 	}
 	Player recipient = BuildingPlanner.plugin.getServer().getPlayer(args[1]);
 	
 	PlanArea area = null;
 	if (args.length > 2) {
 	    String areaName = null;
 	    areaName = args[2];
 	    area = BuildingPlanner.plugin.areaManager.getAreaByName(areaName);
 	}
 
 	if (area == null) {
 	    area = BuildingPlanner.plugin.areaManager.getAffectedArea(player.getLocation().getBlock());
 	}
 	
 	if (area == null) {
 	    player.sendMessage("You need to be in an area or specify an area by name to uninvite a player");
 	    return;
 	}
 	
 	if (!area.checkPermission(player, OpType.INVITE, true)) {
 	    player.sendMessage("You do not have permission to uninvite collaborators");
 	    return;
 	}
 	
 	player.sendMessage("You have removed "+player.getName()+" as a collaborator for the area: "+area.getAreaName());
 	recipient.sendMessage(player.getName()+" has removed you as a collaborator for the area: "+area.getAreaName());
 	area.removeCollaborator(recipient.getName());
     }
     
     
     private void sendReport(Player player, String[] args) {
 	if (!player.hasPermission("buildingplanner.send")) {
 	    player.sendMessage("You do not have permission to use plan areas");
 	    return;
 	}
 	
 	if (args.length < 2) {
 	    player.sendMessage("You need to specify a player to send this report to");
 	    return;
 	}
 	
 	Player recipient = BuildingPlanner.plugin.getServer().getPlayer(args[1]);
 	
 	PlanArea area = null;
 	if (args.length > 2) {
 	    String areaName = null;
 	    areaName = args[2];
 	    area = BuildingPlanner.plugin.areaManager.getAreaByName(areaName);
 	}
 
 	if (area == null) {
 	    area = BuildingPlanner.plugin.areaManager.getAffectedArea(player.getLocation().getBlock());
 	}
 	
 	if (area == null) {
 	    player.sendMessage("You need to be in an area or specify an area by name to send a report");
 	    return;
 	}
 	
 	player.sendMessage("You have sent "+recipient.getName()+" an area report");
 	recipient.sendMessage(player.getName()+" has sent you a plan area report:");
 	area.reportPlan(recipient);
 
     }
 
     private void listPlans(Player player) {
 	if (!player.hasPermission("buildingplanner.list")) {
 	    player.sendMessage("You do not have permission to list plan areas");
 	    return;
 	}
 	
 	List<PlanArea> planList = BuildingPlanner.plugin.areaManager.getPlanList();
 	if (planList.size() == 0) {
 	    player.sendMessage("There are currently no plan areas setup");
 	    return;
 	}
 	
 	player.sendMessage("Plan Area List:");
 	for (PlanArea planArea : planList) {
 	    player.sendMessage(planArea.getAreaName());
 	}
     }
 
     private void reportPlan(Player player, String[] args) {
 	if (!player.hasPermission("buildingplanner.report")) {
 	    player.sendMessage("You do not have permission to use plan areas");
 	    return;
 	}
 	
 	PlanArea area = null;
 
 	if (args.length > 1) {
 	    String areaName = null;
 	    areaName = args[1];
 	    area = BuildingPlanner.plugin.areaManager.getAreaByName(areaName);
 	}
 
 	if (area == null) {
 	    area = BuildingPlanner.plugin.areaManager.getAffectedArea(player.getLocation().getBlock());
 	}
 	
 	if (area == null) {
 	    player.sendMessage("You need to be in an area or specify an area by name to get a report");
 	    return;
 	}
 	
 	area.reportPlan(player);
     }
     
 }
