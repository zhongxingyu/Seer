 package abc.notation;
 
 /** This class enables you to describe any time signatures like 4/4, 6/8 ...*/
 public class TimeSignature extends Fraction implements ScoreElementInterface {
 	
 	/** The 4/4 time signature constant. */
 	public static final TimeSignature SIGNATURE_4_4 = new TimeSignature(4,4);
 	
 	/** The 3/4 time signature constant. */
 	public static final TimeSignature SIGNATURE_3_4 = new TimeSignature(3,4);
 	
 	/** The 6/8 time signature constant. */
 	public static final TimeSignature SIGNATURE_6_8 = new TimeSignature(6,8);
 	
   /** Creates a new time signature with the specified parameters.
    * @param num The number of beat in a bar.
    * @param den The type of those beats. */
   public TimeSignature (int num, int den)
   { super (num, den); }
 
   /** Returns the default note length for this time signature.
    * @return The default note length for this time signature.
    * The default note length is equals to <TT>Note.SIXTEENTH</TT> when the
    * time signature decimal conversion value is strictly less than 0.75.
    * If it's higher, the default is <TT>Note.EIGHTH</TT>. */
   public short getDefaultNoteLength()
   {
     short currentNoteLength;
     if (this.floatValue() < 0.75)
       currentNoteLength = Note.SIXTEENTH;
     else
       currentNoteLength = Note.EIGHTH;
     return currentNoteLength;
   }
 
   /** Return <TT>true</TT> if this time signature if compound, <TT>false</TT>
    * otherwise.
    * @return <TT>true</TT> if this time signature if compound, <TT>false</TT>
   * otherwise. Compound time signatures are 3/4, 3/8, 9/8 etc... simple time
   * signatures are C, 4/4, 2/4 etc... */
   public boolean isCoumpound()
  { return (getNumerator()%2!=0); }
 
   public int getNumberOfDefaultNotesPerBeat(short defaultLength)
   {
     // The reference length for meter
     short meterDefLength = Note.convertToNoteLengthStrict(1, getDenominator());
     return meterDefLength / defaultLength;
   }
   
   	public boolean equals(Object o) {
   		if (o instanceof TimeSignature){
   			return ( 
   			((TimeSignature)o).getDenominator()==this.getDenominator()
   				&& 
   			((TimeSignature)o).getNumerator()==this.getNumerator()
   			);
   		}
   		else
   			return super.equals(o);
   	}
 
 }
