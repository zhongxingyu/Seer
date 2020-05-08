 package org.shininet.bukkit.itemrenamer.configuration;
 
 import java.util.Map;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Range;
 
 public interface DamageLookup {
 	/**
 	 * Retrieve the rule that is applied to every damage value, even those that have been set.
 	 * @return The all rule.
 	 */
 	public abstract RenameRule getAllRule();
 
 	/**
 	 * Set the rule that is applied to every damage value, even those that have been set.
 	 * @param rule - the other rule.
 	 */
 	public abstract void setAllRule(RenameRule rule);
 
 	/**
 	 * Retrieve the rename rule that is applied to every value beside the ones that have been set.
 	 * @return Other rename rule.
 	 */
 	public abstract RenameRule getOtherRule();
 
 	/**
 	 * Set the rename rule that is applied to every value beside the ones that have been set.
 	 * @param rule - the other rule.
 	 */
 	public abstract void setOtherRule(RenameRule rule);
 
 	/**
 	 * Apply a transform function to a certain range or class of damage values.
 	 * @param value - the range of damage values to modify.
 	 * @param function - the method to apply to every defined and undefined value in this range.
 	 */
 	public abstract void setTransform(DamageValues value, Function<RenameRule, RenameRule> function);
 	
 	/**
 	 * Associate a given damage value with a certain rename rule
 	 * @param damage - the damage value.
 	 * @param rule - the rename rule.
 	 */
 	public abstract void setRule(int damage, RenameRule rule);
 	
 	/**
 	 * Associate a given range of damage value with a certain rename rule.
 	 * @param lowerDamage - the minimum damage (inclusive).
 	 * @param higherDamage - the maximum damage (inclusive).
 	 * @param rule - the rule to set.
 	 */
 	public abstract void setRule(int lowerDamage, int upperDamage, RenameRule rule);
 
 	/**
 	 * Retrieve every defined range in the lookup tree.
 	 * @return Every defined range.
 	 */
 	public abstract Map<Range<Integer>, RenameRule> toLookup();
 
 	/**
 	 * Get the appropriate defined rename rule for a given damage value, without taking into account OTHER or ALL.
 	 * @param damage - the damage value.
 	 * @return A defined rename rule, or NULL if not found.
 	 */
 	public abstract RenameRule getDefinedRule(int damage);
 	
 	/**
 	 * Get the appropriate rename rule for a given damage value, taking into account the OTHER and ALL rule.
 	 * @param damage - the damage value.
 	 * @return The correct rename rule.
 	 */
 	public abstract RenameRule getRule(int damage);
 
 	/**
 	 * Determine if this lookup has changed.
 	 * @return TRUE if it has, FALSE otherwise.
 	 */
 	public abstract boolean hasChanged();
	
	
	/**
	 * Set whether or not this lookup has changed.
	 * @param value - TRUE if it has, FALSE otherwise.
	 */
	public abstract void setChanged(boolean value);
 }
