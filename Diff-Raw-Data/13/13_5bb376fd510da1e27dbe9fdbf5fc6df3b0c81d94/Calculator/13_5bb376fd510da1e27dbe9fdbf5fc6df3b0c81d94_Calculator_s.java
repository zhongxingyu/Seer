 package edu.grinnell.csc207.nikakath.hw4;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import edu.grinnell.csc207.nikakath.hw4.Fraction;
 
 public class Calculator {
 	static String[] rs = { "0", "0", "0", "0", "0", "0", "0", "0" };
 
 	public static Fraction evaluate(String expression) {
		System.out.println("    expression sent is " + expression);
 		String[] expressions = expression.split(" ");
 		if (expression.contains("=")) {
 			String substring = expression.substring(expression.indexOf("="));
 			rs[Character.getNumericValue(expression.charAt(1))] = substring;
 		}
 
 		else if (expressions.length <= 2) {
 			if (expression.contains("r")) {
 				return (evaluate(rs[Character.getNumericValue(expression
 						.charAt(1))]));
 			} else {
 				return new Fraction(expressions[0]);
 			}
 		}
 
 		else {
 			Fraction first = evaluate(expressions[0]);
 			Fraction other = evaluate(expressions[2]);
 			String rest = "";
 
 			for (int i = 3; i < expressions.length; i++) {
 				rest = rest.concat(" ").concat(expressions[i]);
 			}
			System.out.println("    right before division " + expression);

 			if (expressions[1].equals("/")) {
 				return evaluate(first.divide(other).toString().concat(rest));
 			} else if (expressions[1].equals("+")) {
 				return evaluate(first.add(other).toString().concat(rest));
 			} else if (expressions[1].equals("*")) {
 				return evaluate(first.multiply(other).toString().concat(rest));
 			} else if (expressions[1].equals("-")) {
 				return evaluate(first.subtract(other).toString().concat(rest));
 			} // IMPLEMENT POW ("^")
 
 		}
 
 		return null;
 	}
 
 	public static void main(String[] args) {
 		String in = "";
 		PrintWriter pen = new PrintWriter(System.out, true);
 		java.io.BufferedReader eyes;
 		java.io.InputStreamReader istream;
 		istream = new java.io.InputStreamReader(System.in);
 		eyes = new java.io.BufferedReader(istream);
 
 		pen.print("Input expression: ");
 		pen.flush();
 		try {
 			in = eyes.readLine();
 		} catch (IOException e) {
 			pen.println("I'm sorry; something was wrong with your input. "
 					+ e.getMessage());
 		}
 		Fraction result = evaluate(in);
		pen.print(in + " = " + result.toString());
 		pen.close();
 		try {
 			eyes.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
