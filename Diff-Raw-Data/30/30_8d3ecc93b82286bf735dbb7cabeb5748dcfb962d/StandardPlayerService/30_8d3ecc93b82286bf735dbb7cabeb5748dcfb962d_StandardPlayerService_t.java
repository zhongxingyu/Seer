 package com.comphenix.xp.expressions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.bukkit.entity.Player;
 
 import com.comphenix.xp.Action;
 import com.comphenix.xp.lookup.LevelingRate;
 import com.comphenix.xp.parser.Utility;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.comphenix.xp.rewards.RewardTypes;
 import com.comphenix.xp.rewards.xp.ExperienceManager;
 import com.comphenix.xp.rewards.xp.RewardEconomy;
 import com.comphenix.xp.rewards.xp.RewardVirtual;
 
 public class StandardPlayerService implements ParameterService<Player> {
 
 	public static String NAME = "STANDARD_PLAYER_PARAMETERS";
 	
 	/**
 	 * List of every standard player parameter.
 	 * 
 	 * @author Kristian
 	 */
 	public enum PlayerParameters {
 		TOTAL_EXPERIENCE,
 		LEVEL_EXPERIENCE,
 		EXPERIENCE,
 		CURRENCY
 	}
 	
 	// The name of every player parameter
 	private static String[] PARAM_NAMES = Utility.toStringArray(PlayerParameters.values());
 	
 	// Used to support the currency parameter
	protected RewardEconomy economy = null;
 	
	public StandardPlayerService() {
	}
	
	public void setEconomy(RewardEconomy economy) {
		this.economy = economy;
 	}
 	
 	@Override
 	public String getServiceName() {
 		return NAME;
 	}
 
 	@Override
 	public String[] getParameterNames() {
 		return PARAM_NAMES;
 	}
 
 	@Override
 	public Collection<NamedParameter> getParameters(final Action action, final Player player) {
 
 		final Collection<NamedParameter> standard = new ArrayList<NamedParameter>(PARAM_NAMES.length);
 		final ExperienceManager manager = new ExperienceManager(player);
 		
 		// Retrieve th
 		final LevelingRate rate = getRate(action);
 		
 		// Create short-lived named parameters
 		for (PlayerParameters parameter : PlayerParameters.values()) {
 			final PlayerParameters current = parameter;
 			
 			standard.add(new NamedParameter(parameter.toString()) {
 				@Override
 				public Double call() throws Exception {
 				
 					// This is an approximation. We can't "undo" the leveling amount correctly.
 					double rateFactor = rate != null ? RewardVirtual.getLevelingFactor(rate, player, manager) : 1;
 					
 					// Use a good ol' switch to execute the different functions
 					switch (current) {
 					case EXPERIENCE:
 					case TOTAL_EXPERIENCE:
 						return (double) manager.getCurrentExp() / rateFactor;
 					case LEVEL_EXPERIENCE:
 						return (double) (player.getExp() * manager.getXpNeededToLevelUp(player.getLevel()) / rateFactor);
 					case CURRENCY:
 						if (economy != null) 
 							return economy.getBalance(player);
 						else
 							return 0.0; // Default
 					default:
 						throw new IllegalArgumentException("Unknown player parameter attribute detected.");
 					}
 				}
 			});
 		}
 		
 		return standard;
 	}
 	
 	/**
 	 * Retrieve the leveling rate from a given action.
 	 * @param action - action containing a leveling rate in its virtual reward.
 	 * @return The current leveling rate, or NULL if none were found.
 	 */
 	private LevelingRate getRate(Action action) {
 		RewardVirtual virtual = (RewardVirtual) action.getReward(RewardTypes.VIRTUAL);
 
 		if (virtual != null) {
 			return virtual.getLevelingRate();
 		} else {
 			return null;
 		}
 	}
 }
