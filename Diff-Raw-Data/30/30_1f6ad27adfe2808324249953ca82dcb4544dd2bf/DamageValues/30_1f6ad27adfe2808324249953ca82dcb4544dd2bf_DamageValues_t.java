 package org.shininet.bukkit.itemrenamer.configuration;
 
 import java.util.Deque;
 import java.util.List;
 
 import com.google.common.collect.Range;
 import com.google.common.collect.Ranges;
 
 /**
  * Can represent a range of damage values, or one of the two special types.
  * 
  * @author Kristian
  */
 public class DamageValues {
 	public static final DamageValues ALL = new DamageValues((byte) -1);
 	public static final DamageValues OTHER = new DamageValues((byte) -2);
 
 	private final Range<Integer> range;
 	
 	/**
 	 * Construct a special damage value that is not otherwise legal.
 	 * @param key - special damage value.
 	 */
 	private DamageValues(byte key) {
 		this.range = Ranges.singleton((int) key);
 	}
 	
 	public DamageValues(int value) {
 		this(value, value);
 	}
 
 	public DamageValues(int minimumValue, int maximumValue) {
 		validateValue(minimumValue, "minimumValue");
 		validateValue(maximumValue, "maximumValue");
 		
 		this.range = Ranges.closed(minimumValue, maximumValue);
 	}
 	
 	private void validateValue(int value, String name) {
 		if (value < 0)
 			throw new IllegalArgumentException("Value " + name + " cannot be less than zero (" + value + ")");
 		if (value > Short.MAX_VALUE)
 			throw new IllegalArgumentException("Value " + name + " cannot be greater than SHORT.MAX_VALUE (" + value + ")");
 	}
 
 	public Range<Integer> getRange() {
 		return range;
 	}
 	
 	/**
 	 * Parse a given argument list to a damage value.
 	 * @param arguments - the argument list.
 	 * @return The parsed damage value.
 	 * @throws IllegalArgumentException If we were unable to find a constant or integer range.
  	 */
 	public static DamageValues parse(Deque<String> arguments) {
		if (arguments.isEmpty())
			throw new IllegalArgumentException("Must specify a damage value.");
		
 		String clean = arguments.peekFirst().trim();
 		
 		if (clean.equalsIgnoreCase("ALL")) {
 			return ALL;
 		} else if (clean.equalsIgnoreCase("OTHER")) {
 			return OTHER;
 		} else {
 			List<Integer> range = ConfigParsers.getIntegers(arguments, 2, Ranges.closed(0, (int) Short.MAX_VALUE));
 			
 			if (range.size() == 1)
 				return new DamageValues(range.get(0));
 			else if (range.size() == 2)
 				return new DamageValues(range.get(0), range.get(1));
 			else
 				throw new IllegalArgumentException("No integer range or value found.");
 		}
 	}
 	
 	@Override
 	public String toString() {
 		if (this == ALL)
 			return "ALL";
 		else if (this == OTHER)
 			return "OTHER";
 		else
 			return range.toString();
 	}
 }
