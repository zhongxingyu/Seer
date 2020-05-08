 package uk.co.jacekk.bukkit.worldrules;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.bukkit.World;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import uk.co.jacekk.bukkit.baseplugin.v2.BasePlugin;
 import uk.co.jacekk.bukkit.baseplugin.v2.config.DynamicConfigKey;
 import uk.co.jacekk.bukkit.baseplugin.v2.config.PluginConfig;
 import uk.co.jacekk.bukkit.worldrules.commands.RulesExecutor;
 import uk.co.jacekk.bukkit.worldrules.listeners.SendRulesListener;
 
 public class WorldRules extends BasePlugin {
 	
 	public void onEnable(){
 		super.onEnable(true);
 		
 		this.config = new PluginConfig(new File(this.baseDirPath + File.separator + "config.yml"), Config.values(), this.log);
 		
 		if (this.config.getBoolean(Config.SHOW_RULES_ON_ENTER)){
 			this.pluginManager.registerEvents(new SendRulesListener(this), this);
 		}
 		
 		this.commandManager.registerCommandExecutor(new RulesExecutor(this));
 	}
 	
 	public ArrayList<String> getRulesForWorld(final World world){
 		ArrayList<String> rules = new ArrayList<String>();
 		
 		rules.addAll(this.config.getStringList(Config.GLOBAL_RULES));
 		rules.addAll(this.config.getStringList(new DynamicConfigKey(Config.WORLD_RULES.getKey() + "." + world.getName(), Arrays.asList())));
 		
 		return rules;
 	}
 	
 	public void sendRules(CommandSender sender, World world, ArrayList<String> rules){
		ChatColor headerColour = ChatColor.valueOf(this.config.getString(Config.HEADER_COLOUR));
		ChatColor ruleColour = ChatColor.valueOf(this.config.getString(Config.RULE_COLOUR));
 		
 		sender.sendMessage(ChatColor.BLACK + " ");
 		sender.sendMessage(headerColour + "Rules for '" + world.getName() + "':");
 		
 		for (int i = 0; i < rules.size(); ++i){
 			sender.sendMessage(ruleColour + "    " + (i + 1) + ". " + rules.get(i));
 		}
 		
 		sender.sendMessage(ChatColor.BLACK + " ");
 	}
 	
 	public void sendRules(CommandSender sender, World world){
 		this.sendRules(sender, world, this.getRulesForWorld(world));
 	}
 	
 	public void sendRules(Player player){
 		World world = player.getWorld();
 		
 		this.sendRules(player, world, this.getRulesForWorld(world));
 	}
 	
 }
