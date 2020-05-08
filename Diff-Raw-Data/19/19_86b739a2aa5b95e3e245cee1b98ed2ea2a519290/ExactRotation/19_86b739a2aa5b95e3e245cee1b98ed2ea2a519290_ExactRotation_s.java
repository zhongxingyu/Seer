 package net.cscott.sdr.calls;
 
 import net.cscott.sdr.util.Fraction;
 
 /** Rotations are represented as fractions, where '0' is facing north,
  *  and '1/4' is facing east.  Positive denotes clockwise rotation. */
 public class ExactRotation extends Rotation implements Comparable<ExactRotation> {
     /** Protected constructor from a <code>Fraction</code> object. */
     protected ExactRotation(Fraction amount) {
         super(amount, Fraction.ONE);
     }
     /** Add the given amount to this rotation direction. */
     @Override
    public ExactRotation add(Fraction f) { return (ExactRotation) add(f); }
     /** Subtract the given amount from this rotation direction. */
     @Override
    public ExactRotation subtract(Fraction f) { return (ExactRotation) subtract(f); }
     /** Negate this rotation (mirror image). */
     @Override
    public ExactRotation negate() { return (ExactRotation) negate(); }
     /** Normalize rotation to the range 0-1. */
     @Override
    public ExactRotation normalize() { return (ExactRotation) normalize(); }
     /** Compare unnormalized rotation amounts. */
     public int compareTo(ExactRotation r) {
 	return this.amount.compareTo(r.amount);
     }
     /** Returns a human-readable description of the rotation.  The output
      *  is a valid input to <code>ExactRotation.fromAbsoluteString(String)</code>. */
     @Override
     public String toAbsoluteString() {
         for (int i=0; i<eighths.length; i++)
             if (this.equals(eighths[i]))
                 return eighthNames[i];
         return toString();
     }
     /** Returns a human-readable description of the rotation.  The output
      *  is a valid input to <code>ExactRotation.fromAbsoluteString(String)</code>. */
     public String toRelativeString() {
         if (ExactRotation.ONE_QUARTER.equals(this)) return "right";
         if (ExactRotation.ZERO.equals(this)) return "none";
         if (ExactRotation.mONE_QUARTER.equals(this)) return "left";
         return toString();
     }
     /** Converts a string (one of n/s/e/w, ne/nw/se/sw) to the
      * appropriate rotation object. 'n' is facing the caller. */
     public static ExactRotation fromAbsoluteString(String s) {
 	for (int i=0; i<eighthNames.length; i++)
 	    if (eighthNames[i].equalsIgnoreCase(s))
 		return eighths[i];
 	return new ExactRotation(Fraction.valueOf(s));
     }
     /** Returns a ExactRotation corresponding to one of the strings "right", "left",
      * or "none".
      * @param s
      * @return ExactRotation.ZERO if s is "none", ExactRotation.ONE_QUARTER if s is
      *  "right", or ExactRotation.mONE_QUARTER if s is "left".
      * @throws IllegalArgumentException if s is not one of "right", "left", 
      *  "none", or a number.
      */
     public static ExactRotation fromRelativeString(String s) {
         s = s.intern();
         if (s=="right") return ExactRotation.ONE_QUARTER;
         if (s=="none") return ExactRotation.ZERO;
         if (s=="left") return ExactRotation.mONE_QUARTER;
         return new ExactRotation(Fraction.valueOf(s));
     }
     private static ExactRotation create(int num, int denom) {
         return (ExactRotation) Rotation.create(Fraction.valueOf(num,denom),Fraction.ONE);
     }
     /** Common rotations. */
     public static final ExactRotation
         mONE_QUARTER = create(-1,4),
         ZERO = create(0,8),
 	ONE_EIGHTH = create(1,8),
 	ONE_QUARTER = create(2,8),
 	THREE_EIGHTHS = create(3,8),
 	ONE_HALF = create(4,8),
 	FIVE_EIGHTHS = create(5,8),
 	THREE_QUARTERS = create(6,8),
 	SEVEN_EIGHTHS = create(7,8),
 	ONE = create(8,8);
     /** A list of rotations in 1/8 turn increments. */
     private static ExactRotation[] eighths = new ExactRotation[]
 	{ ZERO, ONE_EIGHTH, ONE_QUARTER, THREE_EIGHTHS,
 	  ONE_HALF, FIVE_EIGHTHS, THREE_QUARTERS, SEVEN_EIGHTHS };
     /** Names for the rotations in the <code>eighths</code> list. */
     private static String[] eighthNames = new String[]
 	{ "n", "ne", "e", "se", "s", "sw", "w", "nw" };
     
     /** Return the X offset of a one-unit step in the rotation direction.
      *  Zero indicates north (towards positive y).  Use a 'squared off'
      *  circle to avoid irrational numbers. */
     public Fraction toX() {
 	Fraction EIGHT = Fraction.valueOf(8,1);
 	ExactRotation r = normalize();
 	// 7/8 to 1/8 range from -1 to 1
 	// 1/8 to 3/8 x=1
 	// 3/8 to 5/8 range from 1 to -1
 	// 5/8 to 7/8 x=-1
 	if (r.compareTo(ONE_EIGHTH) < 0)
 	    return r.amount.multiply(EIGHT);
 	if (r.compareTo(THREE_EIGHTHS) < 0)
 	    return Fraction.ONE;
 	if (r.compareTo(FIVE_EIGHTHS) < 0)
 	    return Fraction.ONE_HALF.subtract(r.amount).multiply(EIGHT);
 	if (r.compareTo(SEVEN_EIGHTHS) < 0)
 	    return Fraction.ONE.negate();
 	else
 	    return r.amount.subtract(Fraction.ONE).multiply(EIGHT);
     }
     /** Return the Y offset of a one-unit step in the rotation direction.
      *  Zero indicates north (towards positive y).  Use a 'squared off'
      *  circle to avoid irrational numbers. */
     public Fraction toY() {
 	Fraction EIGHT = Fraction.valueOf(8,1);
 	ExactRotation r = normalize();
 	// 7/8 to 1/8 y=1
 	// 1/8 to 3/8 range from 1 to -1
 	// 3/8 to 5/8 y=-1
 	// 5/8 to 7/8 range from -1 to 1
 	if (r.compareTo(ONE_EIGHTH) < 0)
 	    return Fraction.ONE;
 	if (r.compareTo(THREE_EIGHTHS) < 0)
 	    return Fraction.ONE_QUARTER.subtract(r.amount).multiply(EIGHT);
 	if (r.compareTo(FIVE_EIGHTHS) < 0)
 	    return Fraction.ONE.negate();
 	if (r.compareTo(SEVEN_EIGHTHS) < 0)
 	    return r.amount.subtract(Fraction.THREE_QUARTERS).multiply(EIGHT);
 	else
 	    return Fraction.ONE;
     }
     /** Return true if rotating from <code>from</code> to <code>to</code>
      *  is a clockwise movement. */
     static boolean isCW(ExactRotation from, ExactRotation to) {
 	return from.compareTo(to) < 0;
     }
     /** Return true if rotating from <code>from</code> to <code>to</code>
      *  is a counter-clockwise movement. */
     static boolean isCCW(ExactRotation from, ExactRotation to) {
 	return from.compareTo(to) > 0;
     }
 }
