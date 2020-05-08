 /**
  * Jun 10, 2013
  */
 package com.appspot.anaki808built.utils.converters;
 
 import java.util.Locale;
 
 import com.appspot.anaki808built.utils.Assertive;
 
 /**
  * @author andi Jun 10, 2013 A null safe converter for booleans
  */
 public final class BooleanConverter {
 
 	private BooleanConverter() {
 		// hide utility class
 	}
 
 	/**
 	 * @param toConvert
 	 *            {@link CharSequence}
 	 * @return true for y, Y, t, T, true, TRUE, yes, YES, 1. false for all else
 	 */
 	public static boolean convert(final CharSequence toConvert) {
 		if (Assertive.isNull(toConvert)) {
 			return false;
 		}
 		final String toCovertUpper = toConvert.toString()
 				.toUpperCase(Locale.US);
 		if ("Y".equals(toCovertUpper)) {
 			return true;
 		}
 		return false;
 	}
 }
