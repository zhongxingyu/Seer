 /**
  * 
  */
 package cz.cuni.mff.odcleanstore.engine.common;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
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
	public static String getW3CDTF(Date date) {
		return "\"" + SIMPLEDATEFORMAT_W3CDTF.format(date) + "\"";
 	}
 
 	/**
 	 * Get current date in W3CDTF format.
 	 * 
 	 * @return The formatted string.
 	 */
	public static String getW3CDTFCurrent() {
		return getW3CDTF(new Date());
 	}
 
 	// TODO will be moved
 	// /**
 	// * Get <http://opendata.cz/TripleGroups/random/{uuid}> uri
 	// */
 	// public final static String generateOpenDataTripleGroupsRandomUuidUri() {
 	// return "<http://opendata.cz/TripleGroups/random/" + UUID.randomUUID().toString() + ">";
 	// }
 }
