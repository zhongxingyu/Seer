 package me.silentdojo.prestige;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.gmail.nossr50.Users;
 import com.gmail.nossr50.datatypes.PlayerProfile;
 import com.gmail.nossr50.datatypes.SkillType;
 import com.gmail.nossr50.skills.Skills;
 
 public class PrestigeCommandExecutor implements CommandExecutor{
 
 	
 	private Permission permission;
 	
 	public PrestigeCommandExecutor(Prestige plugin, Permission permission){
 		
 		
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	if(command.getName().equalsIgnoreCase("prestige")){
 	    	if(args.length == 0){
 	    		sender.sendMessage(ChatColor.UNDERLINE + "                                                                              ");
 	    		sender.sendMessage(ChatColor.DARK_GREEN + "Prestige is a new way to advance your skills.");
 	    		sender.sendMessage(" - " + ChatColor.AQUA + "Gain amazing new abilities by focusing on specific Skills.");
 	    		sender.sendMessage(" - " + ChatColor.AQUA + "Choose wisely. Benefits come at a price!");
 	    		sender.sendMessage(" - " + ChatColor.AQUA + "Each Prestige resets your Skill Level.");
 	    		sender.sendMessage(" - " + ChatColor.AQUA + "PvP Skill Prestige will remove a PvE Ability.");
 	    		sender.sendMessage(" - " + ChatColor.AQUA + "PvE Skill Prestige will remove a PvP Ability.");
 	    		sender.sendMessage(" - " + ChatColor.DARK_RED + "Use " + ChatColor.ITALIC + "/prestige <Skillname>");
 	    		sender.sendMessage(ChatColor.UNDERLINE + "                                                                              ");
 	    		sender.sendMessage(" ");
 	    		return false;
 	    	}
 	    	
     	Player player = (Player) sender;
 		PlayerProfile PP = Users.getProfile(player);
     	int skillLevel = 0;
     	SkillType type = null;
     	
 		type = Skills.getSkillType(args[0]);
 		skillLevel = PP.getSkillLevel(type);		
 		
         	PP.modifyskill(type, 0);
         	switch(type){
         	case ACROBATICS: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.acrobatics.gracefulroll");
 				player.setAllowFlight(true);
         		sender.sendMessage("you have upgraded your acrobatics skill");
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;}
         	case UNARMED: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.unarmed.bonusdamage");
         		sender.sendMessage("you have upgraded your unarmed skill");
         		
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case MINING: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.mining.doubledrops");
         		sender.sendMessage("you have upgraded your mining skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case WOODCUTTING: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.woodcutting.doubledrops");
         		sender.sendMessage("you have upgraded your woodcutting skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case HERBALISM: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.herbalism.doubledrops");
         		sender.sendMessage("you have upgraded your herbalism skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case AXES: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.axes.bonusdamage");
         		sender.sendMessage("you have upgraded your axes skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case SWORDS: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.swords.serratedstrikes");
         		sender.sendMessage("you have upgraded your swords skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case TAMING: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.taming.callofthewild");
         		sender.sendMessage("you have upgraded your taming skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case FISHING: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.fishing.shakemob");
         		sender.sendMessage("you have upgraded your fishing skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case EXCAVATION: {
         		if (skillLevel >= 1000){
         		ItemStack itemToGive = new ItemStack(Material.DIAMOND,30);
         		player.getInventory().addItem(itemToGive);
         		sender.sendMessage("you have recieved a bonus for excavation");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case REPAIR: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.repair.armorrepair");
         		sender.sendMessage("you have upgraded your repair skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	case ARCHERY: {
         		if (skillLevel >= 1000){
         		permission.playerAdd(player, "mcmmo.ability.archery.ignition");
         		sender.sendMessage("you have upgraded your archery skill");
 
         		}else
         			sender.sendMessage("Skill Level is Not 1000 yet try again later!!");
         		break;
         		}
         	
         	}
         	return true;
         	}
 		// Test Command
     	if(command.getName().equalsIgnoreCase("test")){
     		if(args.length == 0){
     			sender.sendMessage("Use /test <SkillName>");
     		}else{
     			Player player = (Player) sender;
 	    		PlayerProfile PP = Users.getProfile(player);
 	    		SkillType type2 = Skills.getSkillType(args[0]);
				PP.addXP(type2, 1000, player);
 	    		sender.sendMessage("You added 1000 Levels to " + type2);    	
 	    		}
     		return false;
     		}
     	// PowerUp Command
     	if(command.getName().equalsIgnoreCase("powerup")){
     		if(args.length == 0){
     			final Player player = (Player) sender;
     			float exp = player.getExp();
     	        	if(exp >= 0){
     	            System.out.println("Exp is: " + exp);
     				player.setAllowFlight(true);
     				player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 3));
     			    player.sendMessage("Hey Hey Hey!");
     			    
     			    }
     	        	return false;
     	        		}
     	        	}
 		return false;
     	    
     		}
 }
