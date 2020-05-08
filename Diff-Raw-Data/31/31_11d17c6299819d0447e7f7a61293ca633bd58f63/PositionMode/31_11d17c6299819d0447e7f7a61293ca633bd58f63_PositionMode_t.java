 package de.ptb.epics.eve.data.scandescription;
 
 import javax.xml.bind.annotation.XmlEnumValue;
 
 /**
  * <code>PositionMode</code> represents the position modes available for a 
  * motor axis.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld (-at-) ptb.de>
  * @author Marcus Michalsky
  */
 public enum PositionMode {
 	
 	/**
 	 * The positions are understood as relative to the axis position at the 
 	 * beginning of the scan module.
 	 */
 	@XmlEnumValue("relative")
 	RELATIVE,
 	
 	/**
 	 * The positions are absolute.
 	 */
 	@XmlEnumValue("absolute")
 	ABSOLUTE;
 	
 	/**
 	 * Converts a <code>PositionMode</code> to a {@link java.lang.String}.
 	 * 
 	 * @param positionMode the position mode that should be converted
 	 * @return the <code>String</code> corresponding to the position mode
 	 */
 	public static String typeToString(final PositionMode positionMode) {
 		switch(positionMode) {
 			case RELATIVE:
 				return "relative";
 			case ABSOLUTE:
 				return "absolute";
 		}
 		return null;
 	}
 	
 	/**
 	 * Converts a {@link java.lang.String} to its corresponding 
 	 * {@link de.ptb.epics.eve.data.scandescription.PositionMode}
 	 * 
 	 * @param name the {@link java.lang.String} that should be converted
 	 * @return The corresponding
 	 * 		   {@link de.ptb.epics.eve.data.scandescription.PositionMode}
 	 * @throws IllegalArgumentException if the argument is <code>null</code> 
 	 */
 	public static PositionMode stringToType(final String name) {
 		if(name == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'name' must not be null!");
 		}
 		
 		if( name.equals("relative")) {
 			return PositionMode.RELATIVE;
 		}
 		if( name.equals("absolute")) {
 			return PositionMode.ABSOLUTE;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns all available position modes.
 	 * 
 	 * @return all available position modes
 	 */
 	public static String[] getPossiblePositionModes() {
		return new String[] {"relative", "absolute"}; 
 	}
 }
