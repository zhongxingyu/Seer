 package net.cscott.sdr.calls;
 
 import net.cscott.sdr.util.Fraction;
 
 /** Rotations are represented as fractions, where '0' is facing north,
  *  and '1/4' is facing east.  The also have a 'modulus', since they can
  *  represent "general" directions.  For example, "1/4 modulo 1/2" means
  *  facing east or west (but not north, south, or any other direction).
  *  A rotation modulo 0 matches any direction.  A rotation modulo 1
  *  indicates an 'exact' rotation; the modulus can not exceed 1. */
 public class Rotation {
     /** The amount of the rotation. */
     public final Fraction amount;
     /** The 'modulus' of the rotation: indicates the amount of uncertainty
      * in the direction.  The modulus cannot exceed 1. */
     public final Fraction modulus;
     
     /** Private constructor from a <code>Fraction</code> object. */
     protected Rotation(Fraction amount, Fraction modulo) {
 	this.amount = amount;  this.modulus = modulo;
 	assert this.modulus.compareTo(Fraction.ONE)==0 ?
             this instanceof ExactRotation : true;
         assert this.modulus.compareTo(Fraction.ONE) <= 0;
         assert this.modulus.compareTo(Fraction.ZERO) >= 0;
         assert this.modulus.getNumerator()==0 || this.modulus.getNumerator()==1;
         assert this.modulus.compareTo(Fraction.ZERO)==0 ?
                this.amount.compareTo(Fraction.ZERO)==0 : true;
     }
     public static final Rotation create(Fraction amount, Fraction modulo) {
         // Effective modulus is always 1/N for some N -- reduce fraction to
         // lowest terms, then use denom.  For example, a modulus of 2/3 is
         // equivalent to 1/3, since the sequence goes:
         //            2/3, 4/3 == 1/3 mod 1, 6/3 == 0 mod 1.
         if (modulo.compareTo(Fraction.ZERO)==0)
             amount = Fraction.ZERO;
         else
             modulo = Fraction.valueOf(1, modulo.getDenominator());
         return (modulo.compareTo(Fraction.ONE)==0) ?
                 new ExactRotation(amount) : new Rotation(amount, modulo);
     }
     /** Add the given amount to this rotation direction. */
     public Rotation add(Fraction f) {
 	return create(this.amount.add(f), this.modulus);
     }
     /** Subtract the given amount from this rotation direction. */
     public Rotation subtract(Fraction f) {
         return create(this.amount.subtract(f), this.modulus);
     }
     /** Negate this rotation (mirror image). */
     public Rotation negate() {
         return create(this.amount.negate(), this.modulus);
     }
     /** Normalize rotation to the range 0-modulus. */
     public Rotation normalize() {
         if (this.modulus.compareTo(Fraction.ZERO)==0) return this;
         // make rotation positive.
         Fraction abs = this.amount;
         if (abs.compareTo(Fraction.ZERO) < 0)
             abs = abs.add(Fraction.valueOf(1-abs.getProperWhole()));
         assert abs.compareTo(Fraction.ZERO) >= 0;
         // now reduce by modulus.
         Fraction f = abs.divide(this.modulus);
         // just want the fractional part.
         f = Fraction.valueOf(f.getProperNumerator(), f.getDenominator())
                     .multiply(this.modulus);
         return create(f, this.modulus);
     }
     /** Rotations are equal iff their (unnormalized) rotation amount and
      * modulus are exactly equal. */
     @Override
     public boolean equals(Object o) {
 	if (!(o instanceof Rotation)) return false;
         Rotation r = (Rotation) o;
         return this.amount.equals(r.amount) && this.modulus.equals(r.modulus);
     }
     /** Hashcode of the unnormalized amount & modulus. */
     @Override
     public int hashCode() {
 	return 51 + this.amount.hashCode() + 7*this.modulus.hashCode();
     }
     /** Returns true iff all the rotations possible with the given {@code r}
      * are included within the set of rotations possible with {@code this}.
      * For example, the Rotation {@code 0 mod 1/4} (ie, north, east, south, or
      * west, but no intermediate directions) includes {@code 3/4 mod 1}
      * (ie, exactly west), but the reverse is not true: {@code 3/4 mod 1}
      * includes {@code 7/4 mod 1}, but does not include {@code 0 mod 1/4}.
      * Formally, returns true iff the congruence class of {@code this} is
      * a superset of the congruence class of {@code r}.
      */
     public boolean includes(Rotation r) {
         Rotation r1 = this.normalize(), r2 = r.normalize();
         // check for an exact match.
         if (r1.equals(r2)) return true; // exact match.
         // "all rotations" includes everything.
         if (r1.modulus.equals(Fraction.ZERO)) return true;
         // but nothing (other than "all rotations") includes "all rotations"
         if (r2.modulus.equals(Fraction.ZERO)) return false;
         // check that moduli are compatible: this.modulo < r.modulo, etc.
         if (r2.modulus.divide(r1.modulus).getProperNumerator() != 0)
             return false; // incompatible moduli
         assert r1.modulus.compareTo(r2.modulus) <= 0;
         r2 = create(r2.amount, r1.modulus).normalize();
         return r1.equals(r2);
     }
     /** Returns a human-readable description of the rotation.  The output
      *  is a valid input to <code>ExactRotation.valueOf(String)</code>. */
     @Override
     public String toString() {
 	return this.amount.toProperString()+" mod "+this.modulus;
     }
     /** Returns a human-readable description of the rotation, similar to the
      *  input to <code>ExactRotation.fromAbsoluteString(String)</code>. */
     public String toAbsoluteString() {
         if (this.modulus.compareTo(Fraction.ZERO)==0) return "o";
         else if (this.modulus.compareTo(Fraction.ONE_QUARTER)==0) {
             if (this.amount.compareTo(Fraction.ZERO)==0) return "+";
             else if (this.amount.compareTo(Fraction.ONE_EIGHTH)==0) return "x";
         }
         else if (this.modulus.compareTo(Fraction.ONE_HALF)==0) {
             if (this.amount.compareTo(Fraction.ZERO)==0) return "|";
             else if (this.amount.compareTo(Fraction.ONE_QUARTER)==0) return "-";
         } else if (this.modulus.compareTo(Fraction.ONE)==0)
             assert false : "we should have invoked ExactRotation.toAbsoluteString()";
         return toString();
     }
     /** Converts a string (one of n/s/e/w, ne/nw/se/sw) to the
      * appropriate rotation object. 'n' is facing the caller.
      * The string '-' means "east or west", and the string '|' means
      * "north or south".  The string "+" means "north, south, east, or west".
      * The string 'o' means "any rotation". */
     public static Rotation fromAbsoluteString(String s) {
         if (s.equals("|")) return create(Fraction.ZERO, Fraction.ONE_HALF);
         if (s.equals("-")) return create(Fraction.ONE_QUARTER, Fraction.ONE_HALF);
         if (s.equals("+")) return create(Fraction.ZERO, Fraction.ONE_QUARTER);
         if (s.equals("x")) return create(Fraction.ONE_EIGHTH, Fraction.ONE_QUARTER);
         if (s.equalsIgnoreCase("o")) return create(Fraction.ZERO,Fraction.ZERO);
         return ExactRotation.fromAbsoluteString(s);
     }
 }
