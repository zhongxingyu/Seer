 package music;
 
 import java.util.ArrayList;
 
 /* Staff represents a single musical staff within the piece, which contains information regarding
  * four separate voices, as well as the clef(s) assigned to the staff.
  */
 public class Staff{
 	ArrayList<Voice> _voices;		// Voices contained within the staff.
 	ArrayList<Clef> _clefs;			// List of Clefs and their durations.
 
	public Staff(ArrayList<Voice> voices, ArrayList<Clef> clefs){
 		_voices = new ArrayList<Voice>();
 		_clefs = new ArrayList<Clef>();
 	}
 	
 	public ArrayList<Voice> getVoices() {
 		return _voices;
 	}
 	
 	public ArrayList<Clef> getClefs() {
 		return _clefs;
 	}
 }
