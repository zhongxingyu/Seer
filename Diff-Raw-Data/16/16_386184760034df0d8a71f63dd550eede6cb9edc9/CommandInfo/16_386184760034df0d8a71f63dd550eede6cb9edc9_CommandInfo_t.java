 package sk.tomsik68.helpplus;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.bukkit.command.PluginCommand;
 
 import com.avaje.ebean.validation.Length;
 import com.avaje.ebean.validation.NotNull;
 @Entity
 @Table(name = "help_plus")
 public class CommandInfo implements Comparable<CommandInfo> {
	
 	@NotNull
 	@Length(max = 32)
 	@Id
 	public String name;
	@Length(max=32)
 	public String plugin,permission;
	@Length(max=768)
 	public String usage, description;
	@Length(max = 128)
 	public String aliases;
 
 	public CommandInfo(PluginCommand command) {
 		name = command.getName();
 		usage = command.getUsage();
 		description = command.getDescription();
 		plugin = command.getPlugin().getDescription().getName();
 		if (command.getAliases() != null) {
 			StringBuilder sb = new StringBuilder();
 			for (String alias : command.getAliases()) {
 				sb = sb.append(alias).append(',');
 			}
 			if (sb.length() > 0)
 				sb = sb.deleteCharAt(sb.length() - 1);
 
 			aliases = sb.toString();
 		}
 		permission = command.getPermission();
 	}
 
 	public CommandInfo(String name, String usage, String desc, String[] aliases, String perm, String plugin) {
 		this.name = name;
 		this.usage = usage;
 		this.description = desc;
 		if (aliases != null && aliases.length > 0) {
 			StringBuilder sb = new StringBuilder();
 			for (String alias : aliases) {
 				sb = sb.append(alias).append(',');
 			}
 			if (sb.length() > 0)
 				sb = sb.deleteCharAt(sb.length() - 1);
 
 			this.aliases = sb.toString();
 		}
 		this.permission = perm;
 		this.plugin = plugin;
 	}
 	/** EBean usage only.
 	 * 
 	 */
 	public CommandInfo(){
 		
 	}
 	
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the usage
 	 */
 	public String getUsage() {
 		return usage;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @return the plugin
 	 */
 	public String getPlugin() {
 		return plugin;
 	}
 
 	public String getAliases() {
 		return aliases;
 	}
 
 	public String getPermission() {
 		return permission;
 	}
 
 	public void setName(String name2) {
 		name = name2;
 	}
 
 	public void setUsage(String usage2) {
 		usage = usage2;
 	}
 
 	public void setDescription(String desc) {
 		description = desc;
 	}
 
 	public void setPlugin(String plug) {
 		plugin = plug;
 	}
 
 	public void setPermission(String perm) {
 		permission = perm;
 	}
 
 	public void setAliases(String aliases) {
 		this.aliases = aliases;
 	}
 
 	@Override
 	public int compareTo(CommandInfo o) {
 		return name.compareTo(o.getName());
 	}
 
 	@Override
 	public String toString() {
 		return "CommandInfo[name=" + name + "]";
 	}
 }
