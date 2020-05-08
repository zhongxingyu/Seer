 package core.scientrix.parsing;
 
 import core.scientrix.syntax.Operation;
 
 public class Tester 
 {
 	public static void main(String[] args)
 	{
		Operation o = Parser.makeOperation("cos4+2*1!");
 		System.out.println(o);
 		System.out.println(o.evaluate());
 	}
 }
