 package org.shininet.bukkit.itemrenamer.configuration;
 
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.comphenix.protocol.concurrency.AbstractIntervalTree;
 import com.google.common.base.Function;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Range;
 
 /**
  * Represents a damage lookup interval tree.
  * @author Kristian
  */
 class MemoryDamageLookup implements DamageLookup {
 	/**
 	 * Represents an integer interval tree.
 	 * 
 	 * @author Kristian
 	 */
 	private class IntegerInterval extends AbstractIntervalTree<Integer, RenameRule> {
 		@Override
 		protected Integer decrementKey(Integer key) {
 			return key - 1;
 		}
 
 		@Override
 		protected Integer incrementKey(Integer key) {
 			return key + 1;
 		}
 	}
 	
 	// Used to store our data
 	private AbstractIntervalTree<Integer, RenameRule> tree = new IntegerInterval();
 	
 	private RenameRule all;
 	private RenameRule other;
 
 	// Whether or not the lookup has changed
 	private boolean changed;
 	
 	/**
 	 * Construct a new memory damage lookup.
 	 */
 	public MemoryDamageLookup() {	
 		// Use default values
 	}
 	
 	/**
 	 * Clone a memory damage lookup from a given lookup.
 	 * @param lookup - the other lookup.
 	 */
 	public MemoryDamageLookup(DamageLookup other) {
 		setAllRule(other.getAllRule());
 		setOtherRule(other.getOtherRule());
 		
 		// Use the lookup method
 		for (Entry<Range<Integer>, RenameRule> entry : other.toLookup().entrySet()) {
 			Range<Integer> range = entry.getKey();
 			RenameRule rule = entry.getValue();
 			setRule(range.lowerEndpoint(), range.upperEndpoint(), rule);
 		}
 		changed = false;
 	}
 	
 	@Override
 	public RenameRule getAllRule() {
 		return all;
 	}
 
 	@Override
 	public void setAllRule(RenameRule rule) {
 		this.changed = true;
 		this.all = rule;
 	}
 	
 	@Override
 	public RenameRule getOtherRule() {
 		return other;
 	}
 	
 	@Override
 	public void setOtherRule(RenameRule rule) {
 		this.changed = true;
 		this.other = rule;
 	}
 
 	@Override
 	public void setTransform(DamageValues value, Function<RenameRule, RenameRule> function) {
 		if (value == DamageValues.ALL) {
 			setAllRule(function.apply(getAllRule() != null ? getAllRule() : new RenameRule()));	
 		} else if (value == DamageValues.OTHER) {
 			setOtherRule(function.apply(getOtherRule() != null ? getOtherRule() : new RenameRule()));	
 		} else {
 			setTransformed(value.getRange().lowerEndpoint(), value.getRange().upperEndpoint(), function);
 		}
 	}
 	
 	/**
 	 * Set all the rules in a given range by applying a transform to any existing rules.
 	 * <p>
 	 * Ranges that are not already defined will be set with the default rule after a transform.
 	 * @param minimum - the minimum value in the range.
 	 * @param maximum - the maximum value in the range
 	 * @param ruleTransform - the transform to apply in this range, including the default value.
 	 */
 	public void setTransformed(int minimum, int maximum, Function<RenameRule, RenameRule> ruleTransform) {
 		Set<IntegerInterval.Entry> removed = tree.remove(minimum, maximum, true);
 		RenameRule defaultRule = ruleTransform.apply(new RenameRule(null, null));
 		
 		// It's been changed
 		changed = true;
 		
 		// Set everything to default
 		setRule(minimum, maximum, defaultRule);
 		
 		// Then set the applied version of every previous rule in this range
 		for (IntegerInterval.Entry rule : removed) {
 			setRule(rule.getKey().lowerEndpoint(), rule.getKey().upperEndpoint(),
 					ruleTransform.apply(rule.getValue()));
 		}
 	}
 	
 	@Override
 	public RenameRule getRule(int damage) {
 		RenameRule value = getDefinedRule(damage);
 		RenameRule all = getAllRule();
 		
 		// All is our fallback
 		if (value == null) {
 			return RenameRule.merge(getOtherRule(), all);
 		} else {
 			return RenameRule.merge(value, all);
 		}
 	}
 	
 	@Override
 	public RenameRule getDefinedRule(int damage) {
 		return tree.get(damage);
 	}
 	
 	@Override
 	public Map<Range<Integer>, RenameRule> toLookup() {
 		Map<Range<Integer>, RenameRule> lookup = Maps.newLinkedHashMap();
 		
 		// Enumerate and create the resulting set
 		for (IntegerInterval.Entry entry : tree.entrySet()) {
 			lookup.put(entry.getKey(), entry.getValue());
 		}
 		return lookup;
 	}
 	
 	@Override
 	public void setRule(int damage, RenameRule rule) {
 		if (damage < 0)
 			throw new IllegalArgumentException("Damage cannot be less than zero.");
 		
 		changed = true;
 		tree.put(damage, damage, rule);
 	}
 	
 	@Override
 	public void setRule(int lowerDamage, int upperDamage, RenameRule rule) {
 		if (lowerDamage < 0 || upperDamage < 0)
 			throw new IllegalArgumentException("Damage cannot be less than zero.");
 		if (lowerDamage > upperDamage)
 			throw new IllegalArgumentException("Lower damage must be less than upper damage.");
 		
 		changed = true;
 		tree.put(lowerDamage, upperDamage, rule);
 	}
 	
 	/**
 	 * Determine if the current lookup has changed.
 	 * @return TRUE if it has, FALSE otherwise.
 	 */
 	@Override
 	public boolean hasChanged() {
 		return changed;
 	}
 }
