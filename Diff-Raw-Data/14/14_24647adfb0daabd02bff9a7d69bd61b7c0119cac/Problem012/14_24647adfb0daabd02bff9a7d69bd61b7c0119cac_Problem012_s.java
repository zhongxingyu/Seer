 package euler.level1;
 
 import euler.Problem;
 import euler.sequence.Primes;
 import euler.sequence.TriangleNumbers;
 
 public class Problem012 extends Problem<Long> {
     @Override
     public Long solve() {
         final TriangleNumbers nrs = new TriangleNumbers();
         for (long nr = nrs.next();; nr = nrs.next()) {
            if (Primes.nrOfDivisors(nr) >= 250) {
                 return nr;
             }
         }
     }
 }
