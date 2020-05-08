 package com.gildorymrp.api.core;
 
 import java.util.Map;
 
 /**
  * Represents a passive feat
  * @author Lucariatias
  *
  */
 public interface PassiveFeat extends Feat {
 	
 	/**
 	 * Gets a map containing any stat bonuses this feat grants
 	 * 
	 * @return a map of skills to the amount this feat boosts that skill
 	 */
 	public Map<Skill, Integer> getStatBonuses();
 
 }
