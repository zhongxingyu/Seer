 package com.comphenix.xp.listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 
 import com.comphenix.xp.Configuration;
 import com.comphenix.xp.Debugger;
 import com.comphenix.xp.Presets;
 import com.comphenix.xp.lookup.LevelingRate;
 import com.comphenix.xp.rewards.xp.ExperienceManager;
 
 public class ExperienceLevelListener extends AbstractExperienceListener {
 
 	protected Debugger debugger;
 	
 	public ExperienceLevelListener(Debugger debugger, Presets presets) {
 		this.debugger = debugger;
 		this.presets = presets;
 	}	
 
 	// Invoked after the player picks up an experience orb, but before he or she is awarded any experience
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onEnchantItemEvent(PlayerExpChangeEvent event) {
 		
 		try {
 			handleEnchantItemEvent(event);
 			
 		} catch (Exception e) {
 			ErrorReporting.DEFAULT.reportError(debugger, this, e, event);
 		}
 	}
 	
 	private void handleEnchantItemEvent(PlayerExpChangeEvent event) {
 		
 		Player player = event.getPlayer();
 		Configuration config = getConfiguration(player);
 		
 		ExperienceManager manager = new ExperienceManager(player);
 		LevelingRate rate = config.getLevelingRate();
 		
 		if (rate == null) {
 			if (hasDebugger())
 				debugger.printDebug(this, "No leveling rate detected.");
 			return;
 		}
 		
 		// Retrieve the desired amount of experience required to level up
		int desiredLevelUp = rate.get(player.getLevel());
		int defaultLevelUp = manager.getXpNeededToLevelUp(player.getLevel());
 		
 		// See if we need to modify the experience gained
		if (desiredLevelUp != defaultLevelUp) {
 			// Make experience drops correspond to the desired level rate
 			double factor = (double)defaultLevelUp / (double)desiredLevelUp;
 			
 			// We can't give fractional values with ordinary experience orbs, but we can approximate it
 			double exact = event.getAmount() * factor;
 			int oldXP = event.getAmount();
 			int newXP = (int) exact;
 			
 			event.setAmount(newXP);
 			
 			// Give the last fraction
 			if (exact > newXP) {
 				manager.changeExp(exact - newXP);
 			}
 			
 			if (hasDebugger())
 				debugger.printDebug(this, "Changed xp orb from %s to %.2f. Factor: %.2f", 
 						oldXP, exact, factor);
 		}
 	}
 	
 	// Determine if a debugger is attached and is listening
 	private boolean hasDebugger() {
 		return debugger != null && debugger.isDebugEnabled();
 	}
 }
