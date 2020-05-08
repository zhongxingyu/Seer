 package de.uni_hannover.android.artifactextract.artifacts;
 
 /**
  * Represents one call entry
  * @author Jannis Koenig
  *
  */
 public class Call implements Artifact {
 
 	private String name, number, numberLabel, numberType;
 	private long duration, date;
 	private int type;
 	private boolean newCall;
 
 	public static String FILENAME = "CallLogs";
 
 	/**
 	 * 
 	 * @param name
 	 *            cached name (null if number is unknown)
 	 * @param number
 	 * @param date
 	 *            time in milliseconds since epoch
 	 * @param duration
 	 *            time in seconds
 	 * @param type
 	 *            1 if incoming, 2 if outgoing, 3 if missed (see
 	 *            https://developer
 	 *            .android.com/reference/android/provider/CallLog.Calls.html)
 	 * @param newCall
 	 *            true if call was not acknowledged yet
 	 * 
 	 * @param numberLabel
 	 *            cached label for a custom number type, associated with the
 	 *            phone number (null if it doesn't exist)
 	 * @param numberType
 	 *            cached number type (Home, Work, etc) associated with the phone
 	 *            number (null if number unknown)
 	 */
 	public Call(String name, String number, long date, long duration, int type, boolean newCall,
 			String numberLabel, String numberType) {
 		this.name = name;
 		this.number = number;
 		this.date = date;
 		this.duration = duration;
 		this.type = type;
 		this.newCall = newCall;
 		this.numberLabel = numberLabel;
 		this.numberType = numberType;
 	}
 
 	@Override
 	public String getCSV() {
 		String res = (name != null) ? name.replace(",", "ESCAPED_COMMA") + "," : " ,";
 		res += number + "," + date + "," + duration + "," + newCall + "," + type + ",";
		res += (numberLabel != null) ? numberLabel.replace(",", "ESCAPED_COMMA") + "," : " " + ",";
 		res += (numberType != null) ? numberType.replace(",", "ESCAPED_COMMA") : " ";
 		return res;
 
 	}
 }
