 package sfs.cpuinfo.pattern;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import sfs.entry.Entry;
 
 public class CPUInfoPattern {
 
 	private final Pattern pattern;
 	private final String CPUINFO_PATTERN = "^.*:.*";
 	private final String CPUINFO_SEPERATOR = ":";
	private final String CPUINFO_KEY_REPLACEMENT = "\\s{2,}\\t*";
 	private Matcher matcher;
 
 	public CPUInfoPattern() {
 		pattern = Pattern.compile( CPUINFO_PATTERN );
 	}
 
 	/**
 	 * Checks input string against a pattern to extract information about each entry from '/proc/cpuinfo'.
 	 * Assumes the input is one of the entries from '/proc/cpuinfo'.
 	 * 
 	 * @param cpuInfoElement
 	 *            Each entry from '/proc/cpuinfo'.
 	 * @return Matched part as CPUInfoEntry object against the pattern.
 	 */
 	public Entry check(String cpuInfoElement) {
 
 		if ( cpuInfoElement.isEmpty() ) {
 			return null;
 		}
 
 		Entry entry = null;
 		matcher = pattern.matcher( cpuInfoElement );
 
 		while ( matcher.find() ) {
 			String[] each = matcher.group().split( CPUINFO_SEPERATOR );
 			try {
 				entry = new Entry( each[0].replaceAll( CPUINFO_KEY_REPLACEMENT, "" ), each[1] );
 			}
 			catch ( ArrayIndexOutOfBoundsException ex ) {
 				entry = new Entry( each[0].replaceAll( CPUINFO_KEY_REPLACEMENT, "" ), "" );
 			}
 		}
 
 		return entry;
 	}
 }
