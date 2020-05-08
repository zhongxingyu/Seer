 package com.github.mineGeek.ItemRules.Rules;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.entity.Player;
 
 import com.github.mineGeek.ItemRules.Integration.McMMOPlayer;
 import com.github.mineGeek.ItemRules.Store.IRPlayer;
import com.gmail.nossr50.skills.SkillType;
 
 
 /**
  * Qualifies application of rule based on mcMMO criteria
  *
  */
 public class ConditionMcMMO implements Applicator {
 
 	/**
 	 * List of mapped skills to min levels
 	 */
 	private Map<SkillType, ConditionBetween> skills = new HashMap<SkillType, ConditionBetween>();
 	
 	
 	/**
 	 * DO they need to be in a party?
 	 */
 	private boolean requiresParty = false;
 	
 	
 	/**
 	 * Constructor
 	 */
 	public ConditionMcMMO() {}
 	
 	
 	/**
 	 * Constructor taking the party requirement aspect
 	 * @param requiresParty
 	 */
 	public ConditionMcMMO( boolean requiresParty ) {
 		this.requiresParty = requiresParty;
 	}
 	
 	
 	/**
 	 * Sets the pary requirement
 	 * @param value
 	 */
 	public void setRequiresParty( boolean value ) {
 		this.requiresParty = value;
 	}
 	
 	
 	/**
 	 * returns party requirement
 	 * @return
 	 */
 	public boolean getRequiresParty() {
 		return this.requiresParty;
 	}
 	
 	
 	/**
 	 * Add a skill/min level requirement
 	 * @param skill
 	 * @param min
 	 */
 	public void add( SkillType skill, int min ) {
 	
 		this.skills.put( skill , new ConditionBetween( min, null ) );
 		
 	}
 	
 	
 	/**
 	 * Returns ApplicationResult of evaluation
 	 * @param value
 	 * @return
 	 */
 	private ApplicationResult isApplicable( Player player, int adjustedLevel ) {
 		
 		if ( !this.skills.isEmpty() ) {
 			
 			Iterator<Entry<SkillType, ConditionBetween>> skills = this.skills.entrySet().iterator();
 			while ( skills.hasNext() ) {
 				
 				Entry<SkillType, ConditionBetween> row = skills.next();
 				
 				SkillType skill = row.getKey();
 				ConditionBetween condition = row.getValue();
 				
 				if ( !condition.isMinOk( McMMOPlayer.getSkillLevel( player, skill) + adjustedLevel ) ) {
 					return ApplicationResult.YES;
 				}
 				
 			}
 			
 		}
 		
 		return ApplicationResult.NONE;
 		
 	}
 	
 	
 	/**
 	 * Returns ApplicationResult of evaluation
 	 * @param value
 	 * @return
 	 */	
 	@Override
 	public ApplicationResult isApplicable(IRPlayer player) {
 		
 		
 		if ( this.requiresParty && !McMMOPlayer.isPlayerInParty(player.getPlayer()) ) return ApplicationResult.YES;
 		
 		return this.isApplicable( player.getPlayer(), 0 );
 
 	}
 
 	
 	/**
 	 * Good Guy Bye.
 	 */
 	public void close() {
 		if ( this.skills != null ) this.skills.clear();		
 	}
 
 	
 	/**
 	 * Returns ApplicationResult of evaluation
 	 * @param value
 	 * @return
 	 */	
 	@Override
 	public ApplicationResult willBeApplicable(IRPlayer player) {
 		return this.isApplicable(player.getPlayer(), 1 );
 	}
 
 	
 	/**
 	 * Returns ApplicationResult of evaluation
 	 * @param value
 	 * @return
 	 */	
 	@Override
 	public ApplicationResult wasApplicable(IRPlayer player) {
 		return this.isApplicable(player.getPlayer(), -1 );
 	}
 	
 
 }
