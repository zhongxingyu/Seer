 package globals;
 
 import util.KawaWrap;
 
 public class WMath extends Globals {
 	/**
 	 * Add methods related to (random n)
 	 * 
 	 * @param kawa The interpreter to add them to.
 	 * @throws Throwable If anything breaks while adding them.
 	 */
 	@Override
 	public void addMethods(KawaWrap kawa) throws Throwable {
     	kawa.eval("(define (sub1 n) (- n 1))");
     	kawa.eval("(define (add1 n) (+ n 1))");
 	}
 }
