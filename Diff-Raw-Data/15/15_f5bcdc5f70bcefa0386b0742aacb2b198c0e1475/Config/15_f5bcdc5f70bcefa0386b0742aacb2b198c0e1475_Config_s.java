 package newbieprotect;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Config {
 
 	private File configfile;
 	public Config(Nprotect plugin)
 	{
 		configfile = new File(plugin.getDataFolder(),"config.yml");
 	}
 	
 	protected long protecttime = 60*60*1000;//1 hour
 	protected String protectMessage = ChatColor.GREEN+"Защита новичка активирована на "+protecttime/1000+" секунд. Внимание, защита не работает на специальных пвп регионах";
 	protected String unprotectMessage = ChatColor.GREEN+"Вы сняли с себя защиту новичка";
 	protected String youCantAttackWhileProtected = ChatColor.GREEN+"Вы не можете атаковать других игроков пока у вас активирована защита новичка, напишите /pvp-on чтобы снять её";
 	protected String youCantAttackProtected = ChatColor.GREEN+"Вы не можете атаковать этого игрока, у него активна защита новичка";
 
	protected List<String> disabledegions = new ArrayList<String>();
 	
 	protected void loadConfig()
 	{
 		FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
 		protecttime = config.getLong("protecttime",protecttime);
 		protectMessage = config.getString("protectMessage",protectMessage);
 		unprotectMessage = config.getString("unprotectMessage",unprotectMessage);
 		youCantAttackWhileProtected = config.getString("youCantAttaclWhileProtected",youCantAttackWhileProtected);
 		youCantAttackProtected = config.getString("youCantAttackProtected", youCantAttackProtected);
		disabledegions = config.getStringList("disabledegions");
 		saveConfig();
 	}
 	
 	private void saveConfig()
 	{
 		FileConfiguration config = new YamlConfiguration();
 		config.set("protecttime",protecttime);
 		config.set("protectMessage",protectMessage);
 		config.set("unprotectMessage",unprotectMessage);
 		config.set("youCantAttaclWhileProtected",youCantAttackWhileProtected);
 		config.set("youCantAttackProtected", youCantAttackProtected);
		config.set("disabledegions",disabledegions);
 		try {
 			config.save(configfile);
 		} catch (IOException e) {}
 	}
 	
 }
