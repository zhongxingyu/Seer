 package music;
 
 /* Clef represents a musical clef within the piece, used for specifying the locations of specific
  * pitches within the staff.
  */
 public class Clef extends Duration {
 	ClefName _clefName;			// Specifies what type of clef to draw
 	int _centerLine;			// Specifies line of the staff the clef centers upon; center line is 0, negative transposes down, positive transposes up
 	
 	// constructor must take in the duration, name, and center line of the clef
 	public Clef(Rational duration, ClefName clefName, int centerLine) {
 		super(duration);
 		_clefName = clefName;
 		_centerLine = centerLine;
 	}
 	
 	public ClefName getClefName() {
 		return _clefName;
 	}
 	
 	public int getCenterLine() {
 		return _centerLine;
 	}
 	
 	// getLineNumber takes in a pitch and returns an int representing the pitch's line number.
 	public int getLineNumber(Pitch pitch) {
 		if (_centerLine < -4 || _centerLine > 4) {
 			throw new RuntimeException("Clef _centerLine out of bounds (");
 		}
 		
 		int lineNumber;
 		
 		switch (_clefName){
 			case GCLEF:
					int clefCenterValue = G.intValue() + 4 * 7;		// G clef centers on G4
 			case FCLEF:
					int clefCenterValue = F.intValue() + 3 * 7;		// F clef centers on F3
 			case CCLEF:
					int clefCenterValue = C.intValue() + 4 * 7;		// C clef centers on C4
 			default:
 				throw new RuntimeException("Clef not recognized.");				
 			}
 		
 		int pitchValue = pitch.getNoteLetter().intValue() + pitch.getOctave() * 7;	// finds int value of given pitch
 		lineNumber = pitchValue - clefCenterValue + _centerLine;					// line number is the difference, plus the offset of the centerLine
 		
 		return lineNumber;
 	}
}
