 package inter;
 
 public class Stmt extends Node {
 	public Stmt() {
 	}
 
 	public static Stmt Null = new Stmt();
 
 	/**
 	 * @param b label "begin"
 	 * @param a label "after"
 	 */
 	public void gen(int b, int a) {
 	}
 
 	/**
 	 * Saves label "after".
 	 */
 	int after = 0;
 
 	/**
 	 * Used for break statements.
 	 */
 	public static Stmt Enclosing = Stmt.Null;
	
 	public static void reset() {
 		Stmt.Enclosing = Stmt.Null;
 	}
 }
