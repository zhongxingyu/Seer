 package com.github.generaldon.Drugs;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.github.generaldon.Druggie;
 import com.github.generaldon.SLAPI;
 import com.github.generaldon.General.ChatVariables;
 import com.github.generaldon.General.ConfigManager;
 
 public class Full {
 	
 	int l = 0;
 	
 	@EventHandler
 	  public void useDrug(PlayerInteractEvent event) {
 	    Druggie.l += 1;
 	    Player p = event.getPlayer();
 	    if (!Druggie.Addict.containsKey(p.getName())) {
 	      Druggie.Addict.put(p.getName(), Integer.valueOf(0));
 	    }
 	    int i = ((Integer)Druggie.Addict.get(p.getName())).intValue();
 	    if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
 	      if (p.getGameMode() == GameMode.SURVIVAL) {
 	        if (Druggie.l == 5) {
 	          Druggie.l = 0;
 	          try {
	            Druggie.logger.info("Saving Stimulatory data...");
	            SLAPI.save(Druggie.Addict, "Stimulatory.DAT");
 	            Druggie.logger.info("Save succesful!");
 	          } catch (Exception e) {
 	            e.printStackTrace();
 	          }
 	        }
	        if ((event.getMaterial() == Material.INK_SACK) && (p.getItemInHand().getData().getData() == 2) && (p.hasPermission("Stimulatory.murk")))
 	          if (p.hasPotionEffect(PotionEffectType.CONFUSION)) {
 	            p.sendMessage(ChatVariables.LANGUAGE_DATA[13]); // Here it has no errors
 	          } else {
 	            p.getActivePotionEffects().clear();
 	            p.playEffect(p.getLocation(), Effect.SMOKE, 7000);
 	            int MULTIPLIER = p.getInventory().getItemInHand().getAmount();
 	            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(750.0D * Math.floor(Math.sqrt(MULTIPLIER))), 1 * (MULTIPLIER / 5)));
 	            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)(750.0D * Math.floor(Math.sqrt(MULTIPLIER))), 2 * MULTIPLIER));
 	            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2 * MULTIPLIER));
	            p.sendMessage((ChatVariables.LANGUAGE_DATA[5]), MULTIPLIER);
 	         // Right here there is an error on sendMessage, saying "The method sendMessage(String) in the type CommandSender is not applicable for the arguments (String, int)"
 	         // With one fix, to remove , MULTIPLIER so that it matches sendMessage(String)
 	            Druggie.logger.info(p.getName() + " has smoked " + MULTIPLIER + " spliff(s) of " + ConfigManager.Murk() + ".");
 	            p.setItemInHand(null);
 	          }
 }
 	    }
 	}
 }
