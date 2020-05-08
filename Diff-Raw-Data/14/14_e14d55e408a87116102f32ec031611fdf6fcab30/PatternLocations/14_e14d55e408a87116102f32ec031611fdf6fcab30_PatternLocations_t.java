 package org.geworkbench.util.patterns;
 
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
 import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;
 
 /**
  * 
  * @author not attributable
  * @version $Id$
  */
 public class PatternLocations implements Comparable<PatternLocations> {
	private final String ascii;
 
 	private CSSeqRegistration registration;
 	private int idForDisplay;
 	private String patternType;
 
 	public static final String DEFAULTTYPE = "splash";
 	public static final String TFTYPE = "TFBS";
 
 	public PatternLocations(DSPattern<DSSequence, CSSeqRegistration> tf,
 			CSSeqRegistration _registration) {
 
 		registration = _registration;
 		ascii = tf.toString();
 	}
 
 	public PatternLocations(String _ascii, CSSeqRegistration _registration) {
 		ascii = _ascii;
 		registration = _registration;
 		patternType = DEFAULTTYPE;
 	}
 
 	public int getIdForDisplay() {
 		return idForDisplay;
 	}
 
 	public String getAscii() {
 		return ascii;
 	}
 
	public int getAsciiLength() {
		return ascii.replaceAll("\\[.+?\\]", " ").length();
	}

 	public String getPatternType() {
 		return patternType;
 	}
 
 	public CSSeqRegistration getRegistration() {
 		return registration;
 	}
 
 	public void setIDForDisplay(int hashcode) {
 		this.idForDisplay = hashcode;
 	}
 
 	public void setPatternType(String patternType) {
 		this.patternType = patternType;
 	}
 
 	public int compareTo(PatternLocations o) {
 		if (((PatternLocations) o).getRegistration().x1 == registration.x1
 				&& ((PatternLocations) o).getRegistration().x2 == registration.x2) {
 			return 0;
 		} else {
 			return registration.x1
 					- ((PatternLocations) o).getRegistration().x1;
 		}
 	}
 }
