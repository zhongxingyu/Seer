 package friskstick.cops.drugs;
 
 import friskstick.cops.plugin.FriskStick;
 import friskstick.cops.drugs.effects.*;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import java.util.Random;
 
 /**
  * 
  * The main class where drugs and their effects are handled.
  * 
  */
 
 public class Drugs implements Listener {
 
 	private FriskStick plugin;
 	private Random random;
 
 	/**
 	 * The constructor for {@link Drugs this} class.
	 * @param plugin The {@link FriskStick} object required for the jailing feature to function.
 	 */
 	
 	public Drugs(FriskStick plugin) {
 
 		this.plugin = plugin;
 
 	}
 	
 	/**
 	 * The main method that applies the potion effects.
 	 * @param inflict The {@link Inflictable} object (the effect) to apply.
 	 * @param player The {@link Player} to apply it to.
 	 */
 
 	public void inflict(Inflictable inflict, Player player) {
 
 		inflict.inflict(player);
 
 	}
 
 	/**
 	 * The method that will detect if a player uses a drug. Currently does not work.
 	 * @param event The event for Bukkit to handle.
 	 */
 	
 	// Currently NOT implemented(Still does not work)
 	@EventHandler
 	public void drugUse(PlayerInteractEvent event) { // Try to use PlayerInteractEntityEvent and if entity == player return null, else continue with effects
 
 		for(String drugIDRaw : plugin.getConfig().getStringList("drug-ids")) {
 			
 			String[] drugIDs = drugIDRaw.split(":");
 			String drugID = drugIDs[0];
 
 			if(event.getItem().getType().equals(Material.getMaterial(Integer.parseInt(drugID)))) {
 
 				int i = random.nextInt(DrugEffect.values().length);
 
 				DrugEffect effect = DrugEffect.values()[i];
 				Player player = event.getPlayer();
 
 				switch(effect) {
 
 				case NAUSEA:
 					this.inflict(new DrugEffectNausea(), player);
 
 				case HALLUCINATION:
 					this.inflict(new DrugEffectHallucination(), player);
 
 				case ILLNESS:
 					this.inflict(new DrugEffectIllness(), player);
 
 				case DIZZINESS:
 					this.inflict(new DrugEffectDizziness(), player);
 
 				case DEATH:
 					DrugEffectDeath.kill(player);
 
 				}
 
 			}
 
 		}
 
 	}
 
 }
