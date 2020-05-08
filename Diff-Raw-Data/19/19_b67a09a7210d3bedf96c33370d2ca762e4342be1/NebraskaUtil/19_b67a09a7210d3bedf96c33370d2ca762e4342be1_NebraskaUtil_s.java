 package org.thera_pi.nebraska.crypto;
 
 import java.text.Normalizer;
 import java.util.Calendar;
 import java.util.Date;
 
 public class NebraskaUtil {
 	
 	/**
 	 * This is the set of charactes allowes by ITSG Trust Center.
 	 * It is a subset of PrintableString.
 	 */
 	private static final String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 /.()-";
 	
 	/**
 	 * Create Subject DN string from variable parts and constant part.
 	 * X500Principal is used to normalize the string.
 	 * 
 	 * @param IK the institutions ID number (IK)
 	 * @param institutionName name of the institution
 	 * @param personName name of the responsible person
 	 * @return Subject DN string
 	 */
 	public static String getSubjectDN(String IK, String institutionName, String personName) {
 		NebraskaPrincipal principal = new NebraskaPrincipal(
 				NebraskaConstants.X500_PRINCIPAL_COUNTRY, 
 				NebraskaConstants.X500_PRINCIPAL_ORGANIZATION, 
 				institutionName, 
 				IK, 
 				personName);
 		return principal.getDistinguishedName();
 	}
 
 	/**
 	 * Create a String that contains only characters of the PrintableString 
 	 * subset allowed by ITSG Trust Center.
 	 * @param input string to normalize
 	 * @return normalized string
 	 */
 	public static String normalizeDnField(String input) {
 		String result;
 		// change unicode characters to composite form if possible
 		result = Normalizer.normalize(input, Normalizer.Form.NFKC);
 		// special replacement sequences, mainly for German
 		result = result.replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue");
 		result = result.replaceAll("Ä",	"AE").replaceAll("Ö", "OE").replaceAll("Ü", "UE");
 		result = result.replaceAll("ß",	"ss").replaceAll("æ", "ae").replaceAll("ø", "o");
 		result = result.replaceAll("Ø","O").replaceAll("ł", "l").replaceAll("Ł", "L");
 		// change unicode characters to combining sequence, that means
 		// accents are separated from base characters
 		result = Normalizer.normalize(result, Normalizer.Form.NFKD);
 		// remove all accents
 		result = result.replaceAll("[\\p{M}]", "");
 		// remove all illegal characters following or preceding a space
 		result = result.replaceAll(" [^" + allowedCharacters + "]+"," ");
 		result = result.replaceAll("[^" + allowedCharacters + "]+ "," ");
 		// replace all illegal characters with space
 		result = result.replaceAll("[^" + allowedCharacters + "]+"," ");
 		
 		return result;
 	}
 	
 	/**
 	 * Remove leading IK from input.
 	 * @param input an institution ID string, optionally with leading "IK"
 	 * @return normalized institution ID
 	 */
 	public static String normalizeIK(String input) {
 		return input.trim().replaceFirst("^[iI][kK]", "").trim();
 	}
 	
 	/**
 	 * Get alias for certificate (optionally with private key) for specified institution ID.
 	 * Currently the alias is identical to the institution ID.
 	 * 
 	 * @param institutionID the institution ID (IK)
 	 * @return the alias
 	 */
 	public static String getCertAlias(String institutionID) {
		return institutionID.trim();
 	}
 	
 	/**
 	 * Get alias for self-signed key pair for specified institution ID.
 	 * Currently the alias the institution ID prepended with new.
 	 * 
 	 * @param institutionID the institution ID (IK)
 	 * @return the alias
 	 */
 	public static String getNewKeyAlias(String institutionID) {
		return "new." + institutionID.trim();
 	}
 	
 	/**
 	 * Generate a start date for the validity of a certificate.
 	 * Use the beginning of the specified day as start date.
 	 * 
 	 * @param date specifies the beginning day of the certificate's validity
 	 * @return The beginning of the specified day.
 	 */
 	static Date certificateStart(Date date) {
 		return certificateStartOrEnd(date, false);
 	}
 
 	/**
 	 * Generate an end date for the validity of a certificate.
 	 * The specified day defines the start date. The end date will be calculated
 	 * from the start date with the default validity duration.
 	 * 
 	 * @param date specifies the beginning day of the certificate's validity
 	 * @return The end of the day CERTIFICATE_YEARS after the start of validity.
 	 */
 	static Date certificateEnd(Date date) {
 		return certificateStartOrEnd(date, true);
 	}
 
 	/**
 	 * Generate a start or end date for the validity of a certificate.
 	 * Use the beginning of the specified day as start date or for an end date add 
 	 * the certificate duration in years and subtract one millisecond to get the
 	 * end of the day before.
 	 *  
 	 * @param date the date to be used as a start date
 	 * @param end flag to select end date instead of start date 
 	 * @return the start or end date for a certificate's validity
 	 */
 	static private Date certificateStartOrEnd(Date date, boolean end) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		calendar.set(Calendar.HOUR_OF_DAY, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 		if(end) {
 			calendar.setLenient(true);
 			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + NebraskaConstants.CERTIFICATE_YEARS);
 			calendar.set(Calendar.MILLISECOND, -1);
 		}
 		return calendar.getTime();
 	}
 
 	/**
 	 * Convert a byte array to a hexadecimal string representation
 	 * and use a separator between the bytes for better readability.
 	 * Intended to be used for MD5 fingerprints.
 	 * 
 	 * @param bytes the byte array to be converted
 	 * @return the string representation
 	 */
 	public static String toHexString(byte[] bytes) {
 		StringBuffer result = new StringBuffer();
 		for(int i = 0; i < bytes.length; i++) {
 			if(i>0) {
 				result.append(':');
 			}
 			String hex = Integer.toHexString(0xFF & (int)(bytes[i]));
 			if(hex.length() < 2){
 				result.append('0');
 			}
 			result.append(hex);
 		}
 		return result.toString();
 	}
 
 	
 //	public static void main(String args[]) {
 //		for(int i = 0; i < args.length; i++) {
 //			System.out.println("" + args[i] + " -> " + normalizeDnField(args[i]));
 //		}
 //	}
 }
