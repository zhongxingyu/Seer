 package edu.gatech.oad.rocket.findmythings.Helpers;
 
 import android.content.Context;
 
 public final class EnumHelper {
 
 	private EnumHelper() {}
 	
 	/**
 	 * Simply unpacks an enumerated type for a given integer.
 	 * @param value An enumerated type expressed as an int
 	 * @param aClass The enumerated type in question
 	 * @return The corresponding enumerated type for the given integer
 	 */
 	public static <T extends Enum<?>> T forInt(int value, Class<T> aClass) {
 		return aClass.getEnumConstants()[value];
 	}
 	
 	/**
 	 * Simply unpacks an enumerated for a given integer-containing string.
 	 * @param value An enumerated type expressed as an int, inside a string
 	 * @param aClass The enumerated type in question
 	 * @return The corresponding enumerated type for the given integer string
 	 */
 	public static <T extends Enum<?>> T forIntString(String value, Class<T> aClass) {
		int intValue = 0;
 		return aClass.getEnumConstants()[intValue];
 	}
 	
 	public static <T extends Enum<?>> String localizedFromArray(Context context, int rID, T value) {
 		return context.getResources().getStringArray(rID)[value.ordinal()];
 	}
 	
 	public static <T extends Enum<?>> String toIntString(T value) {
 		return ((Integer)value.ordinal()).toString();
 	}
 
 }
