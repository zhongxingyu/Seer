 package net.cscott.sdr.calls.grm;
 
 import net.cscott.sdr.util.Fraction;
 
 /** Grammar rule: a right-hand side, left-hand side,
  *  and a precedence level. */
 public class Rule {
     public final String lhs;
     public final Grm rhs;
     public final Fraction prec; // precedence level
     
     public Rule(String lhs, Grm rhs, Fraction prec) {
	assert prec != null : "null precedence";
         this.lhs = lhs;
         this.rhs = rhs;
         this.prec = prec;
     }
     
     public String toString() {
         return lhs+" -> "+rhs+(prec==null?"":" // prec "+prec.toProperString());
     }
 }
