 package core.scientrix.parsing;
 
 import core.scientrix.syntax.Operation;
 
 public class Tester 
 {
 	public static void main(String[] args)
 	{
		Operation o = Parser.makeOperation("pi/e*2+6");
 		System.out.println(o);
 		System.out.println(o.evaluate());
 	}
 }
