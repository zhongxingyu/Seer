 /**
  * 
  */
 package cz.cuni.mff.odcleanstore.engine.common;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
import cz.cuni.mff.odcleanstore.vocabulary.XMLSchema;

 /**
  * 
  * Helper class for processing some formats.
  * 
  *  @author Petr Jerman
  */
 public class FormatHelper {
 
 	private final static SimpleDateFormat SIMPLEDATEFORMAT_W3CDTF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 
 	/**
 	 * Transform date to W3CDTF format.
 	 * 
 	 * @param date
 	 *            The time value to be formatted into a W3CDTF format.
 	 * @return The formatted string.
 	 */
	public static String getTypedW3CDTF(Date date) {
		String formattedDate = SIMPLEDATEFORMAT_W3CDTF.format(date);
		
		// Fix that the timezone in xsd:dateTime must be formatted as e.g.  +02:00 instead of +0200 
		// given by SimpleDateFormat
		StringBuilder result = new StringBuilder();
		result.append('"');
		result.append(formattedDate, 0, formattedDate.length() - 2);
		result.append(':');
		result.append(formattedDate, formattedDate.length() - 2, formattedDate.length());
		result.append("\"<");
		result.append(XMLSchema.dateTimeType);
		result.append('>');
		return result.toString();
 	}
 
 	/**
 	 * Get current date in W3CDTF format.
 	 * 
 	 * @return The formatted string.
 	 */
	public static String getTypedW3CDTFCurrent() {
		return getTypedW3CDTF(new Date());
 	}
 
 	// TODO will be moved
 	// /**
 	// * Get <http://opendata.cz/TripleGroups/random/{uuid}> uri
 	// */
 	// public final static String generateOpenDataTripleGroupsRandomUuidUri() {
 	// return "<http://opendata.cz/TripleGroups/random/" + UUID.randomUUID().toString() + ">";
 	// }
 }
