 package org.shininet.bukkit.itemrenamer.configuration;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Range;
 import com.google.common.collect.Ranges;
 
 /**
  * Represents a damage lookup from a configuration file.
  * 
  * @author Kristian
  */
 class DamageSerializer {
 	private static final String RULE_NAME = "name";
 	private static final String RULE_LORE = "lore";
 	
 	private static final String DAMAGE_ALL = "all";
 	private static final String DAMAGE_OTHER = "other";
 	
 	private static final String RANGE_DELIMITER = "-";
 	
 	private ConfigurationSection section;
 
 	/**
 	 * Initialize a new damage lookup from a configuration section.
 	 * @param section - the configuration section.
 	 */
 	public DamageSerializer(ConfigurationSection section) {
 		this.section = section;
 	}
 
 	public ConfigurationSection getSection() {
 		return section;
 	}
 
 	/**
 	 * Read a rule from a given key.
 	 * @param key - the key to read.
 	 * @return The read rename rule.
 	 */
 	private RenameRule readRule(String key) {
 		if (key == null)
 			throw new IllegalArgumentException("Key cannot be NULL.");
 		
 		return readRule(getSection(key));
 	}
 		
 	/**
 	 * Read a rule from a given configuration section. 
 	 * @param ruleSection - the section to read from.
 	 * @return The read rename rule.
 	 */
 	private RenameRule readRule(ConfigurationSection ruleSection) {
 		if (ruleSection == null)
 			return null;
 		
 		// Any of these may fail too
 		return new RenameRule(ruleSection.getString(RULE_NAME), 
 							  ruleSection.getStringList(RULE_LORE));
 	}
 	
 	/**
 	 * Read a configuration section at a given location.
 	 * @param key - the key of this section.
 	 * @return The configuration section.
 	 * @IllegalArgumentException If a value at this section is not a configuration section.
 	 */
 	private ConfigurationSection getSection(String key) {
 		Object ruleSection = section.get(key.toLowerCase());
 		
 		// Attempt to get a configuration section
 		if (ruleSection instanceof ConfigurationSection) {
 			return (ConfigurationSection) ruleSection;
 		} else if (ruleSection != null) {
 			// Warn the user about this corrupt file
 			throw new IllegalArgumentException(String.format("Expected a configuration section at %s.%s. Got %s.",
 						section.getCurrentPath(), key, ruleSection));
 		}
 		// Not defined
 		return null;
 	}
 	
 	/**
 	 * Write a given rename rule to a given key.
 	 * @param key - the key.
 	 * @param rule - the rule to write.
 	 */
 	private void writeRule(String key, RenameRule rule) {
 		if (rule != null) {
 			ConfigurationSection ruleSection = section.createSection(key);
 			
 			// Serialize the rename rule
 			if (rule.getName() != null)
 				ruleSection.set(RULE_NAME, rule.getName());
 			if (rule.getLoreSections().size() > 0)
 				ruleSection.set(RULE_LORE, rule.getLoreSections());
 		} else {
 			// Delete it
 			section.set(key, null);
 		}
 	}
 	
 	/**
 	 * Deserialize the content of the configuration section to the given damage lookup.
 	 * @param destination - the input or destination damage lookup.
 	 */
 	public void readLookup(DamageLookup destination) {		
 		destination.setAllRule(readRule(DAMAGE_ALL));
 		destination.setOtherRule(readRule(DAMAGE_OTHER));
 		
 		for (String key : section.getKeys(false)) {
 			if (!isSpecialKey(key)) {
 				// Parse and save
 				Range<Integer> range = parseRange(key);
 				destination.setRule(range.lowerEndpoint(), range.upperEndpoint(), readRule(key));
 			}
 		}
		destination.setChanged(false);
 	}
 
 	/**
 	 * Serialize (write out) the content of the given damage lookup.
 	 * @param source - the damage lookup to write.
 	 */
 	public void writeLookup(DamageLookup source) {
 		// Reset section
 		ConfigurationSection parent = section.getParent();
 		section = parent.createSection(section.getName());
 		
 		writeRule(DAMAGE_ALL, source.getAllRule());
 		writeRule(DAMAGE_OTHER, source.getOtherRule());
 		
 		// Next, sort the ranges
 		List<Entry<Range<Integer>, RenameRule>> entries = Lists.newArrayList(source.toLookup().entrySet());
 		Collections.sort(entries, new Comparator<Entry<Range<Integer>, RenameRule>>() {
 			@Override
 			public int compare(Entry<Range<Integer>, RenameRule> a,
 							   Entry<Range<Integer>, RenameRule> b) {
 				Range<Integer> keyA = a.getKey();
 				Range<Integer> keyB = b.getKey();
 				
 				return ComparisonChain.start().
 					compare(keyA.lowerEndpoint(), keyB.lowerEndpoint()).
 					compare(keyA.upperEndpoint(), keyB.upperEndpoint()).
 				result();
 			}
 		});
 		
 		// Save all the rules
 		for (Entry<Range<Integer>, RenameRule> rules : entries) {
 			Range<Integer> range = rules.getKey();
 			
 			if (range.lowerEndpoint().equals(range.upperEndpoint())) {
 				writeRule(range.lowerEndpoint().toString(), rules.getValue());
 			} else {
 				writeRule(range.lowerEndpoint() + "-" + range.upperEndpoint(), rules.getValue());
 			}
 		}
 	}
 
 	private boolean isSpecialKey(String key) {
 		return DAMAGE_ALL.equalsIgnoreCase(key) || DAMAGE_OTHER.equalsIgnoreCase(key);
  	}
 	
 	/**
 	 * Parse a single number or a range (num-num).
 	 * @param text - the range to parse.
 	 * @return The range.
 	 * @throws IllegalArgumentException If this is not a valid range.
 	 */
 	private static Range<Integer> parseRange(String text) {
 		String[] values = text.split(RANGE_DELIMITER, 2);
 		
 		try {
 			// Parse the range
 			if (values.length == 1) {
 				return Ranges.singleton(Integer.parseInt(values[0].trim()));
 			} else if (values.length == 2) {
 				return Ranges.closed(
 						Integer.parseInt(values[0].trim()), 
 						Integer.parseInt(values[1].trim())
 				);
 			} else {
 				throw new IllegalArgumentException("Cannot parse range: " + text);
 			}
 			
 		} catch (NumberFormatException e) {
 			throw new IllegalArgumentException("Unable to parse range " + text);
 		}
 	}
 }
