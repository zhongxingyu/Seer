 package de.uni_potsdam.hpi.fgnaumann.lsdd.util;
 
 import java.text.Normalizer;
 import java.util.regex.Pattern;
 
 import eu.stratosphere.pact.common.type.base.PactString;
 
 public class UnicodeUtils {
 	/**
 	 * own functions
 	 * 
 	 */
 	static Pattern normalizationFinalizationPattern = Pattern
			.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
 
 	public static void normalizeUnicode(PactString unnormalizedPactString) {
		String normalizedString = Normalizer.normalize(
				unnormalizedPactString.getValue(), Normalizer.Form.NFD);
 		normalizedString = normalizationFinalizationPattern.matcher(
 				normalizedString).replaceAll("");
 		unnormalizedPactString.setValue(normalizedString);
 	}
 }
