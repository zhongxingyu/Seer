 package music;
 /* NoteLetter is an enum type whose fields represent the different possible pitches of a note, 
  * excluding accidentals.
  */
 public enum NoteLetter {
 	C(0), D(1), E(2), F(3), G(4), A(5), B(6);
 	
 	private int _intValue;
 	
	public NoteLetter(int intValue) {
 		this._intValue = intValue;
 	}
 	
 	public int intValue() {
 		return _intValue;
 	}
 }
