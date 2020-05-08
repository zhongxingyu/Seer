 package globals;
 
 import util.KawaWrap;
 
 public class WLists extends Globals {
     /**
      * Add methods related to lists.
      * 
      * @param kawa The interpreter to add them to.
      */
     public void addMethods(final KawaWrap kawa) throws Throwable {
 	kawa.eval("(define (atom? x) (not (pair? x)))");
	kawa.eval(
"(define make-list " +
"  (case-lambda " +
"    [(n) (map (lambda (_) 0) (iota n))] " +
"    [(n i) (map (lambda (_) i) (iota n))]))");
 
 	kawa.eval(
 "(define (filter ? ls)" +
 " (cond" +
 "  [(null? ls) '()]" +
 "    [(? (car ls)) (cons (car ls) (filter ? (cdr ls)))]" +
 "    [else (filter ? (cdr ls))]))");
 
 	kawa.eval("(define (id x) x)");
 	kawa.eval("(define (any p ls) (and (not (null? ls)) (or (p (car ls)) (any p (cdr ls)))))");
 	kawa.eval("(define (all p ls) (or (null? ls) (and (p (car ls)) (all p (cdr ls)))))");
 
 	kawa.eval(
 "(define (fold-left p z . lol)" + 
 "  (let loop ([acc z]" +
 "             [lol lol])" +
 "    (let ([nulls (map null? lol)])" +
 "      (cond" +
 "        [(all id nulls) acc]" +
 "        [(any id nulls) (error 'fold-left \"all lists must be the same length\")]" +
 "	 [else (loop" +
 "                (apply p (cons acc (map car lol)))" +
 "                (map cdr lol))]))))");
 
 	kawa.eval(
 "(define (fold-right p z . lol)" + 
 " (let loop ([lol lol])" + 
 "  (let ([nulls (map null? lol)])" + 
 "   (cond" + 
 "    [(all id nulls) z]" +
 "    [(any id nulls) (error 'fold-right \"all lists must be the same length\")]" +
 "    [else (apply p (append (map car lol) (list (loop (map cdr lol)))))]))))");
     }
 }
