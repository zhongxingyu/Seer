 package fr.vaevictis.vvcdb;
 
import java.util.HashMap;

 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.massivecraft.factions.FPlayers;
 
 public class VVCdB extends JavaPlugin
 {
 	private VVCdBListener listener;
 	
 	@Override
 	public void onEnable()
 	{
		ChampDeBataille.champsdebataille = new HashMap<String, ChampDeBataille>();
		ChampDeBataille.usedCdB = new String("");
 		ChampDeBataille.cdbstarted = false;
 		
 		FileConfiguration config = this.getConfig();
 		for (int nbchampsdebataille = 0 ; nbchampsdebataille < config.getInt("champsdebataille") ; nbchampsdebataille++)
 		{
 			ChampDeBataille cdb = new ChampDeBataille(config.getString("champsdebataille." + String.valueOf(nbchampsdebataille)));
 			cdb.setDefaultSpawn(new Location(Bukkit.getServer().getWorld(config.getString("champsdebataille." + String.valueOf(nbchampsdebataille) + ".defaultspawn.world")), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".defaultspawn.X"), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".defaultspawn.Y"), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".defaultspawn.Z")));
 			for (int nbpositions = 0 ; nbpositions < config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions") ; nbpositions++)
 			{
 				cdb.getPositions().add(new Position(new Location(Bukkit.getServer().getWorld(config.getString("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions." + String.valueOf(nbpositions) + ".location.world")), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions." + String.valueOf(nbpositions) + ".location.X"), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions." + String.valueOf(nbpositions) + ".location.Y"), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions." + String.valueOf(nbpositions) + ".location.Z")), config.getBoolean("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions." + String.valueOf(nbpositions) + ".couverte"), config.getString("champsdebataille." + String.valueOf(nbchampsdebataille) + ".positions." + String.valueOf(nbpositions)), cdb));
 			}
 			for (int nbspawns = 0 ; nbspawns < config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".spawns") ; nbspawns++)
 			{
 				cdb.getSpawns().add(new Location(Bukkit.getServer().getWorld(config.getString("champsdebataille." + String.valueOf(nbchampsdebataille) + ".spawns." + String.valueOf(nbspawns) + ".world")), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".spawns." + String.valueOf(nbspawns) + ".X"), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".spawns." + String.valueOf(nbspawns) + ".Y"), config.getInt("champsdebataille." + String.valueOf(nbchampsdebataille) + ".spawns." + String.valueOf(nbspawns) + ".Z")));
 			}
 		}
 		
 	}
 	@Override
 	public void onDisable()
 	{
 		ChampDeBataille.saveAll(this);
 		this.saveConfig();
 	}
 	
 	public static String getArmure(Player p)
 	{
 		String armorpath = "armures." + p.getName();
 		FileConfiguration config = Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig();
 		return (config.get(armorpath) == null || config.getString(armorpath) == "") ? "basic" : config.getString(armorpath);
 	}
 	public static void setArmure(Player p, String s)
 	{
 		String armorpath = "armures." + p.getName();
 		FileConfiguration config = Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig();
 		config.set(armorpath, s);
 		Bukkit.getServer().getPluginManager().getPlugin("VVCdB").saveConfig();
 	}
 	public static void equipPlayerArmor(Player p)
 	{
 		ItemStack bowb = new ItemStack(261);
 		bowb.addEnchantment(Enchantment.ARROW_INFINITE, 1);
 		ItemStack bow1 = new ItemStack(261);
 		bow1.addEnchantment(Enchantment.ARROW_INFINITE, 1);
 		bow1.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
 		ItemStack bow2 = new ItemStack(261);
 		bow2.addEnchantment(Enchantment.ARROW_INFINITE, 1);
 		bow2.addEnchantment(Enchantment.ARROW_DAMAGE, 3);
 		switch (getArmure(p))
 		{
 			case "basic":
 				p.getInventory().setHelmet(new ItemStack(596));
 				p.getInventory().setChestplate(new ItemStack(597));
 				p.getInventory().setLeggings(new ItemStack(598));
 				p.getInventory().setBoots(new ItemStack(599));
 				p.getInventory().addItem(new ItemStack(267));
 				p.getInventory().addItem(bowb);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "ahmosis1":
 				p.getInventory().setHelmet(new ItemStack(600));
 				p.getInventory().setChestplate(new ItemStack(601));
 				p.getInventory().setLeggings(new ItemStack(602));
 				p.getInventory().setBoots(new ItemStack(603));
 				p.getInventory().addItem(new ItemStack(276));
 				p.getInventory().addItem(bow1);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "ahmosis2":
 				p.getInventory().setHelmet(new ItemStack(604));
 				p.getInventory().setChestplate(new ItemStack(605));
 				p.getInventory().setLeggings(new ItemStack(606));
 				p.getInventory().setBoots(new ItemStack(607));
 				p.getInventory().addItem(new ItemStack(591));
 				p.getInventory().addItem(bow2);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "embrosia1":
 				p.getInventory().setHelmet(new ItemStack(608));
 				p.getInventory().setChestplate(new ItemStack(609));
 				p.getInventory().setLeggings(new ItemStack(610));
 				p.getInventory().setBoots(new ItemStack(611));
 				p.getInventory().addItem(new ItemStack(276));
 				p.getInventory().addItem(bow1);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "embrosia2":
 				p.getInventory().setHelmet(new ItemStack(612));
 				p.getInventory().setChestplate(new ItemStack(613));
 				p.getInventory().setLeggings(new ItemStack(614));
 				p.getInventory().setBoots(new ItemStack(615));
 				p.getInventory().addItem(new ItemStack(592));
 				p.getInventory().addItem(bow2);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "irilis1":
 				p.getInventory().setHelmet(new ItemStack(616));
 				p.getInventory().setChestplate(new ItemStack(617));
 				p.getInventory().setLeggings(new ItemStack(618));
 				p.getInventory().setBoots(new ItemStack(619));
 				p.getInventory().addItem(new ItemStack(276));
 				p.getInventory().addItem(bow1);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "irilis2":
 				p.getInventory().setHelmet(new ItemStack(620));
 				p.getInventory().setChestplate(new ItemStack(621));
 				p.getInventory().setLeggings(new ItemStack(622));
 				p.getInventory().setBoots(new ItemStack(623));
 				p.getInventory().addItem(new ItemStack(593));
 				p.getInventory().addItem(bow2);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "koshaimas1":
 				p.getInventory().setHelmet(new ItemStack(624));
 				p.getInventory().setChestplate(new ItemStack(625));
 				p.getInventory().setLeggings(new ItemStack(626));
 				p.getInventory().setBoots(new ItemStack(627));
 				p.getInventory().addItem(new ItemStack(276));
 				p.getInventory().addItem(bow1);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "koshaimas2":
 				p.getInventory().setHelmet(new ItemStack(628));
 				p.getInventory().setChestplate(new ItemStack(629));
 				p.getInventory().setLeggings(new ItemStack(630));
 				p.getInventory().setBoots(new ItemStack(631));
 				p.getInventory().addItem(new ItemStack(640));
 				p.getInventory().addItem(bow2);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "skaahnorik1":
 				p.getInventory().setHelmet(new ItemStack(632));
 				p.getInventory().setChestplate(new ItemStack(633));
 				p.getInventory().setLeggings(new ItemStack(634));
 				p.getInventory().setBoots(new ItemStack(635));
 				p.getInventory().addItem(new ItemStack(276));
 				p.getInventory().addItem(bow1);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 			case "skaahnorik2":
 				p.getInventory().setHelmet(new ItemStack(636));
 				p.getInventory().setChestplate(new ItemStack(637));
 				p.getInventory().setLeggings(new ItemStack(638));
 				p.getInventory().setBoots(new ItemStack(639));
 				p.getInventory().addItem(new ItemStack(594));
 				p.getInventory().addItem(bow2);
 				p.getInventory().addItem(new ItemStack(262));
 				p.getInventory().addItem(new ItemStack(364, 5));
 				break;
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label,String[] args)
 	{
 		Player p = (Player) sender;
 		if (label.equalsIgnoreCase("cdbjoin") && args.length <= 1 && p.hasPermission("vvcdb.basic"))
 		{
 			if (ChampDeBataille.usedCdB != "" || ChampDeBataille.getMap(args[0].toLowerCase()) != null)
 			{
 				if (ChampDeBataille.usedCdB == "")
 				{
 					ChampDeBataille.usedCdB = args[0].toLowerCase();
 				}
 				ChampDeBataille.getMap(ChampDeBataille.usedCdB).playerJoin(p);
 			}
 			return true;
 		}
 		else if (label.equalsIgnoreCase("cdbcreate") && args.length == 1 && p.hasPermission("vvcdb.admin"))
 		{
 			new ChampDeBataille(args[0]).ajouterMap();
 			return true;
 		}
 		else if (label.equalsIgnoreCase("cdbaddposition") && args.length == 3 && p.hasPermission("vvcdb.admin"))
 		{
 			ChampDeBataille.getMap(args[0]).getPositions().add(new Position(p.getLocation(), (args[1] == "couverte"), args[2], ChampDeBataille.getMap(args[0])));
 			return true;
 		}
 		else if (label.equalsIgnoreCase("cdbsetdefaultspawn") && args.length == 1 && p.hasPermission("vvcdb.admin"))
 		{
 			ChampDeBataille.getMap(args[0]).setDefaultSpawn(p.getLocation());
 			return true;
 		}
 		else if (label.equalsIgnoreCase("cdbaddspawn") && args.length == 1 && p.hasPermission("vvcdb.admin"))
 		{
 			ChampDeBataille.getMap(args[0]).getSpawns().add(p.getLocation());
 			return true;
 		}
 		else if (label.equalsIgnoreCase("cdbleave") && args.length == 0 && p.hasPermission("vvcdb.basic"))
 		{
 			if (ChampDeBataille.getMap(ChampDeBataille.usedCdB).getJoueurs().contains(p))
 			{
 				ChampDeBataille.getMap(ChampDeBataille.usedCdB).playerLeave(p);
 				if (ChampDeBataille.getMap(ChampDeBataille.usedCdB).getJoueurs().isEmpty())
 				{
 					ChampDeBataille.arret();
 				}
 			}
 			return true;
 		}
 		else if (label.equalsIgnoreCase("acheterarmure") && args.length == 1 && p.hasPermission("vvcdb.basic"))
 		{
 			if (args[0].equalsIgnoreCase("lgre") || args[0].equalsIgnoreCase("legere") || args[0].equalsIgnoreCase("1"))
 			{
 				switch (FPlayers.i.get(p).getFaction().getTag().toLowerCase())
 				{
 					case "Ahmosis":
 						setArmure(p, "ahmosis1");
 						// Faire Payer
 						break;
 					case "Embrosia":
 						setArmure(p, "embrosia1");
 						// Faire Payer
 						break;
 					case "Irilis":
 						setArmure(p, "irilis1");
 						// Faire Payer
 						break;
 					case "Skaahnorik":
 						setArmure(p, "skaahnorik1");
 						// Faire Payer
 						break;
 					case "Koshaimas":
 						setArmure(p, "koshaimas1");
 						// Faire Payer
 						break;
 				}
 			}
 			else if (args[0].equalsIgnoreCase("lourde") || args[0].equalsIgnoreCase("2"));
 			{
 				switch (FPlayers.i.get(p).getFaction().getTag())
 				{
 					case "Ahmosis":
 						setArmure(p, "ahmosis2");
 						// Faire Payer
 						break;
 					case "Embrosia":
 						setArmure(p, "embrosia2");
 						// Faire Payer
 						break;
 					case "Irilis":
 						setArmure(p, "irilis2");
 						// Faire Payer
 						break;
 					case "Skaahnorik":
 						setArmure(p, "skaahnorik2");
 						// Faire Payer
 						break;
 					case "Koshaimas":
 						setArmure(p, "koshaimas2");
 						// Faire Payer
 						break;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 }
