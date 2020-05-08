 package srt.tool;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import srt.ast.Expr;
 
 public class SMTLIBConverter {
 	
 	private ExprToSmtlibVisitor exprConverter;
 	private StringBuilder query;
 	
 	public SMTLIBConverter(Set<String> variableNames, List<Expr> transitionExprs, List<Expr> propertyExprs) {
 		
 		if(propertyExprs.size() == 0)
 		{
 			throw new IllegalArgumentException("No assertions.");
 		}
 		
 		// QF_BV allows quantiﬁer-free expressions, including the family of bit-vector sorts and all of the
 		// functions deﬁned in the Fixed_Size_BitVectors theory (but no other new sorts or functions).
 		
 		exprConverter = new ExprToSmtlibVisitor();
 		query = new StringBuilder("(set-logic QF_BV)\n" +
 				"(declare-sort Int 0)\n"+
 				"(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n" +
				"(define-fun bv32tobool ((b  (_ BitVec 32))) (Bool) (= b (_ bv1 32) ))\n");
 		// TODO: Define more functions above (for convenience), as needed.
 
 		// TODO: Add constraints, add properties to check
 		// here.
 		
 		// Declare Variables
 		for (String var : variableNames) {
 			String line = "(declare-fun " + var + " () (_ BitVec 32))\n";
 			query.append(line);
 		}
 		
 		// Add constraints
 		for (Expr e : transitionExprs) {
 			String line = "(assert (bv32tobool "+exprConverter.visit(e) + "))\n";
 			query.append(line);
 		}
 		
 		// Check that one of the assertion properties can fail
 		// TODO what if no properties?
 		
 		query.append(buildProperties(propertyExprs));
 		
 		query.append("(check-sat)\n");
 		
 		query.append(buildGetValues(propertyExprs));
 		
 		System.out.println(query.toString());
 	}
 
 	public String getQuery() {
 		return query.toString();
 	}
 	
 	/**
 	 * 
 	 * @param queryResult
 	 * 
 	 * @return a list of integers such that if i belongs to this list then 
 	 * the ith Expr in propertyExprs list that was passed into the constructor
 	 * is the guard of an assertion that may fail
 	 */
 	public List<Integer> getPropertiesThatFailed(String queryResult) {
 		List<Integer> res = new ArrayList<Integer>();
 		
 		// keep track of property number
 		int i = 0;
 		
 		// remove 'sat' line
 		String rest = queryResult.substring(queryResult.indexOf("sat\n")+4);
 
 		// find end of first line
 		int endOfLine = rest.indexOf('\n');
 		
 		while (endOfLine >= 0) {
 			
 			// get current line
 			String line = rest.substring(0, endOfLine);
 			
 			// if this property may fail (==true), add it to list
 			if (line.contains("true")) {
 				res.add(i);
 			}
 			i++;
 			
 			// remove current line from string
 			rest = rest.substring(endOfLine+1);
 			
 			// get end of next line
 			endOfLine = rest.indexOf('\n');
 		}
 		
 //		int i = 0;
 //		int pos = queryResult.lastIndexOf("prop" + i);
 //		while (pos >= 0) {
 //			
 //			char c = queryResult.charAt(pos + 6); // TODO bad! only works if less than 10 assertions
 //			if (c == 't') {
 //				res.add(i);
 //			}
 //			
 //			// get next prop
 //			i++;
 //			pos = queryResult.lastIndexOf("prop" + i);
 //		}
 		
 		return res;
 	}
 	
 	/**
 	 * 
 	 * @param propertyExprs
 	 * @return a string declaring and specifying the assertions in SMT-LIB format
 	 * for given propertyExprs
 	 */
 	private String buildProperties(List<Expr> propertyExprs) {
 		String props = "";
 		for (int i=0; i < propertyExprs.size(); i++) {
 			props += "(declare-fun prop" + i + " () (Bool))\n";
 		}
 		
 		for (int i=0; i < propertyExprs.size(); i++) {
 			props += "(assert  (= prop" + i + " (not (bv32tobool " + 
 					exprConverter.visit(propertyExprs.get(i)) +"))))\n";
 		}
 		
 		
 		props += "(assert (or ";
 		for (int i=0; i < propertyExprs.size(); i++) {
 			props += "prop" + i + " ";	
 		}
 		props = props.trim() + ("))\n"); // performance
 		
 		return props;
 
 	}
 	
 	/**
 	 * 
 	 * @param propertyExprs
 	 * 
 	 * @return a string of the form (get-value (prop0 prop1 ... propN) where
 	 * N+1 is the number of propertyExprs given
 	 */
 	private String buildGetValues(List<Expr> propertyExprs) {
 		String line = "";
 		if (!propertyExprs.isEmpty()) {
 			line = "(get-value (";
 			for (int i=0; i < propertyExprs.size(); i++) {
 				line += "prop"+i+" ";
 			}
 			
 			line = line.trim() + "))\n"; // not really cool for performance, looks nicer though
 		}
 		return line;
 	}
 	
 	
 	
 }
