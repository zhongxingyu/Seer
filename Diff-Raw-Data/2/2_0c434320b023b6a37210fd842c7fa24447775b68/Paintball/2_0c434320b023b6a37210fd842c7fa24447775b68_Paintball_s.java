 package fr.jules_cesar.Paintball;
 
 import java.io.File;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import fr.aumgn.bukkitutils.command.CommandsRegistration;
 
 public class Paintball extends JavaPlugin implements Listener{
 
 	private static Arene arene = new Arene();
 	private static Partie partie;
 	private Logger log = getLogger();
 	
 	public void onEnable(){
 		// Events
 		getServer().getPluginManager().registerEvents(new PaintballListener(this), this);
 		
 		// Configuration
 		if(!this.getDataFolder().exists()) this.getDataFolder().mkdir();
 		arene = (Arene) new GsonUtil(log, getDataFolder().getPath()).lire("location", Arene.class);
 
 		// Commandes
 		CommandsRegistration register = new CommandsRegistration(this, Locale.FRANCE);
 		register.register(new PaintballCommands(this));
 	}
 	
 	public void onDisable(){
 		new GsonUtil(log, getDataFolder().getPath()).ecrire("location", arene);
 	}
 	
 	public static Arene getArene(){
 		return arene;
 	}
 	
 	public static Partie getPartie(){
 		return partie;
 	}
 	
 	public static void setPartie(Partie p){
 		partie = p;
 	}
 	
 	public static void saveInventory(Player joueur){
 		new GsonUtil(Bukkit.getPluginManager().getPlugin("Paintball").getLogger(), Bukkit.getPluginManager().getPlugin("Paintball").getDataFolder().getPath()).ecrire(joueur.getName(), new Inventaire(joueur.getInventory()));
 		joueur.getInventory().clear();
 		joueur.getInventory().addItem(new ItemStack(332, 128));
 	}
 	
 	public static void loadInventoryIfNecessary(Player joueur){
 		if(new File("plugins/Paintball/" + joueur.getName() + ".json").exists()){
			Inventaire inventaire = (Inventaire)new GsonUtil(Bukkit.getPluginManager().getPlugin("Paintball").getLogger(), Bukkit.getPluginManager().getPlugin("Paintball").getDataFolder().getPath()).lire(joueur.getName(), Inventory.class);
 			joueur.getInventory().setContents(inventaire.getItems());
 			joueur.getInventory().setArmorContents(inventaire.getArmor());
 			new File("plugins/Paintball/" + joueur.getName() + ".json").delete();
 		}
 	}
 }
