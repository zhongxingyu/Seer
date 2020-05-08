 package math.algebra.permgroups.permutation;
 
 
public class DomainMismatchException extends RuntimeException {
   public DomainMismatchException(Permutation p, Permutation q) {
    super(p + " has a domain mismatch with " + q);
   }
 
   private static final long serialVersionUID = 1L;
 }
