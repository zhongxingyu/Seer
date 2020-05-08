 package uk.codingbadgers.benchanted;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class bEnchanted extends Module implements Listener {
 
 	private HashMap<Enchantment, String> m_enchantmentNames = new HashMap<Enchantment, String>();
 	private HashMap<Enchantment, EnchantmentConfig> m_enchantmentConfig = new HashMap<Enchantment, EnchantmentConfig>();
 	
 	/**
 	 * Called when the module is disabled.
 	 */
 	public void onDisable() {
 
 	}
 
 	/**
 	 * Called when the module is loaded.
 	 */
 	public void onEnable() {
 		
 		// register events
 		register(this);
 		
 		// Arrows
 		m_enchantmentNames.put(Enchantment.ARROW_DAMAGE, "Arrow_Punch");
 		m_enchantmentNames.put(Enchantment.ARROW_FIRE , "Arrow_Flame"); 
 		m_enchantmentNames.put(Enchantment.ARROW_INFINITE , "Arrow_Infinity");
 		m_enchantmentNames.put(Enchantment.ARROW_KNOCKBACK, "Arrow_Knockback"); 
 		
 		// Swords
 		m_enchantmentNames.put(Enchantment.DAMAGE_ALL, "Sword_Sharpness");
 		m_enchantmentNames.put(Enchantment.DAMAGE_ARTHROPODS, "Sword_BaneOfAnthropods");
 		m_enchantmentNames.put(Enchantment.DAMAGE_UNDEAD, "Sword_Smite"); 
 		m_enchantmentNames.put(Enchantment.FIRE_ASPECT, "Swords_FireAspect"); 
 		m_enchantmentNames.put(Enchantment.KNOCKBACK, "Swords_Knockback");
 		m_enchantmentNames.put(Enchantment.LOOT_BONUS_MOBS, "Swords_Looting");
 		
 		// Pick
 		m_enchantmentNames.put(Enchantment.DIG_SPEED, "Pickaxe_Efficiancy");
 		m_enchantmentNames.put(Enchantment.DURABILITY, "Pickaxe_Unbreaking"); 
 		m_enchantmentNames.put(Enchantment.LOOT_BONUS_BLOCKS, "Pickaxe_Fortune");
 		m_enchantmentNames.put(Enchantment.SILK_TOUCH, "Pickaxe_SilkTouch"); 
 		m_enchantmentNames.put(Enchantment.WATER_WORKER, "Pickaxe_AquaAffinity"); 
 		
 		// Armour
 		m_enchantmentNames.put(Enchantment.OXYGEN, "Armour_Resperation"); 
 		m_enchantmentNames.put(Enchantment.PROTECTION_ENVIRONMENTAL, "Armour_Protection");
 		m_enchantmentNames.put(Enchantment.PROTECTION_EXPLOSIONS, "Armour_BlastProtection"); 
 		m_enchantmentNames.put(Enchantment.PROTECTION_FALL, "Armour_FeatherFalling"); 
 		m_enchantmentNames.put(Enchantment.PROTECTION_FIRE, "Armour_FireProtection"); 
 		m_enchantmentNames.put(Enchantment.PROTECTION_PROJECTILE, "Armour_ProjectileProtection");
 		m_enchantmentNames.put(Enchantment.THORNS, "Armour_Thorns");
 		
 		CreateConfig();
 		LoadConfigs();
 	}
 	
 	/**
 	 * Create the config files
 	 */
 	private void CreateConfig() {
 		
 		File folder = this.getDataFolder();
 		for (Entry<Enchantment, String> enchantmentSet : m_enchantmentNames.entrySet())
 		{
			File configFile = new File(folder + File.pathSeparator + enchantmentSet.getValue() + ".yml");
 			if (!configFile.exists()) {
 				try {
 					configFile.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 					continue;
 				}
 				
 				FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
 				config.addDefault("enabled", true);
 			}
 		}
 	}
 	
 	/**
 	 * Create the config files
 	 */
 	private void LoadConfigs() {
 		
 		File folder = this.getDataFolder();
 		for (Entry<Enchantment, String> enchantmentSet : m_enchantmentNames.entrySet())
 		{
			File configFile = new File(folder + File.pathSeparator + enchantmentSet.getValue() + ".yml");
 			if (!configFile.exists()) {
 				continue;
 			}
 			
 			EnchantmentConfig config = new EnchantmentConfig(enchantmentSet.getKey(), YamlConfiguration.loadConfiguration(configFile));
 			m_enchantmentConfig.put(enchantmentSet.getKey(), config);
 		}
 		
 	}
 	
 	/**
 	 * Handle the command
 	 */
 	public boolean onCommand(CommandSender sender, String label, String[] args) {	
 		return false;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerEnchant(EnchantItemEvent event) {
 		Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
 		
 		for (Entry<Enchantment, Integer> enchantmentSet : enchants.entrySet())
 		{
 			Enchantment enchantment = enchantmentSet.getKey();
 			EnchantmentConfig enchantmentConfig = m_enchantmentConfig.get(enchantment);	
 			if (enchantmentConfig == null) {
 				continue;
 			}
 			
 			FileConfiguration config = enchantmentConfig.GetConfig();
 			if (config.getBoolean("enabled") == false) {
 				enchants.remove(enchantment);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(BlockBreakEvent event) {
 		final ItemStack tool = event.getPlayer().getItemInHand();
 		if (tool == null) {
 			return;			
 		}
 		
 		Map<Enchantment, Integer> enchants = tool.getEnchantments();
 		if (enchants.isEmpty()) {
 			return;			
 		}
 		
 		for (Entry<Enchantment, Integer> enchantmentSet : enchants.entrySet())
 		{
 			Enchantment enchantment = enchantmentSet.getKey();
 			EnchantmentConfig enchantmentConfig = m_enchantmentConfig.get(enchantment);	
 			if (enchantmentConfig == null) {
 				continue;
 			}
 			
 			FileConfiguration config = enchantmentConfig.GetConfig();
 			if (config.getBoolean("enabled") == false) {
 				tool.removeEnchantment(enchantment);
 			}
 		}		
 	}
 }
