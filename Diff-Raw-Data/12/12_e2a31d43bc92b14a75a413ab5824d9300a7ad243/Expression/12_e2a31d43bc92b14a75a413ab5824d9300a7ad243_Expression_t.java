 public class Expression {
 	
 	private subExpression root;
 
 	public Expression () {
 		root = null;
 	}
 	
 	public Expression (String s) throws IllegalLineException {
 		if (s != null) {
 			if (s.contains(" ")) {
 				throw new IllegalLineException ("Error: expression cannot have spaces");
 			} else {
 				root = constructorHelper(s);
 			}
 		}
 	}
 	
 	private subExpression constructorHelper (String s) throws IllegalLineException{
     	subExpression node;
 		if (s.charAt (0) != '(') {
 	    	if ((s.charAt (0) == '~') && (s.length() > 1)) {
 	    		node = new subExpression (s,"~", constructorHelper(s.substring(1,s.length())),null);
 	    	} else if ((s.length() == 1) && (Character.isLowerCase(s.charAt(0)))) {
 	    		node = new subExpression (s,s);
 	    	} else {
	    		throw new IllegalLineException("Error: unbalanced parentheses or proposition is not single lowercase letter");
 	    	}
 	         return node;
 	    } else {							// finds top-level operand to be root node of the Expression Tree
 	        int nesting = 0;
 	        String opnd1 = null;
 	        String opnd2 = null;
 	        String op = null;
 	        for (int k=1; k<s.length()-1; k++) {
 	            if (s.charAt(k) == '(') {
 	            	nesting++;
 	            } else if (s.charAt(k) == ')') {
 	            	nesting--;
 	            } else if ((s.charAt(k) == '&' || s.charAt(k) == '|') && (nesting == 0)) {
 	    	        opnd1 = s.substring (1, k);
 	    	        opnd2 = s.substring (k+1, s.length()-1);
 	    	        op = s.substring (k, k+1);
 	            	break;
 	            } else if ((s.charAt(k) == '=') && (nesting == 0)) {
 	    	        opnd1 = s.substring (1, k);
 	    	        opnd2 = s.substring (k+2, s.length()-1);
 	    	        op = s.substring (k, k+2);
 	    	        break;
 	            }
 	        }
 	        if ((opnd1 == null) || (opnd2 == null || (op == null))) {
	        	if ((s.charAt(1) == '~') && (s.length() > 3)) {
	        		return new subExpression(s, "~", constructorHelper(s.substring(2,s.length()-1)), null);
	        	} else {
	        		throw new IllegalLineException("Error: unbalanced parentheses or no operands");
	        	}
 	        }
 	        if (ProofChecker.iAmDebugging) {
 	        	System.out.println ("expression = " + s);
 	        	System.out.println ("operand 1  = " + opnd1);
 	        	System.out.println ("operator   = " + op);
 	        	System.out.println ("operand 2  = " + opnd2);
 	        	System.out.println ( );
 	        }
 	        return new subExpression(s,op, constructorHelper(opnd1), constructorHelper(opnd2));
 	    }
 	}
 	public void print ( ) {
 	    if (root != null) {
 	        printHelper (root, 0);
 	    }
 	    System.out.println("");
 	}
 		
 	private static final String indent1 = "    ";
 		
 	private static void printHelper (subExpression cur, int indent) {
 	    if (cur.myRight != null) {
 	    	Expression.printHelper(cur.myRight, indent+1);
 	    }
 	    println (cur.name, indent);
 	    if (cur.myLeft != null) {
 	    	Expression.printHelper(cur.myLeft, indent+1);;
 	    }
 	}
 			
 	private static void println (Object obj, int indent) {
 	    for (int k=0; k<indent; k++) {
 	        System.out.print (indent1);
 	    }
 	    System.out.println (obj);
 	}
 	
 	public subExpression getRoot() {
 		return root;
 	}
 	public boolean equals(Expression e) {
 		return root.equals(e.getRoot());
 	}
 	
 	// invariants: no parentheses in subExpression names
 	// no operands at leaves
 	// if subExpression has myLeft, but no myRight, name must be "~"
 	public boolean isOK () {
 		try {
 			check(root);
 		} catch(IllegalLineException e){
 			System.out.println(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	private void check (subExpression x) throws IllegalLineException {
 		if ((x.name.contains("(")) || (x.name.contains(")"))) {
 			throw new IllegalLineException("parentheses in proposition");
 		}
 		
 		if (x.myLeft != null) {
 			if ((x.myRight == null) && (x.name != "~")) {
 				throw new IllegalLineException("unbalanced expression");
 			}
 			check(x.myLeft);
 		}
 		if (x.myRight != null) {
 			if (x.myLeft == null) {
 				throw new IllegalLineException("unbalanced expression");
 			}
 			check(x.myRight);
 		}
 	}
 
 	
 	public static class subExpression {
 		
 		private String name;	// string representation of the proposition or operand
 		private String total;	// string representation of expression of which this node is the highest level operand
 		private subExpression myLeft;
 		private subExpression myRight;
 		
 		public subExpression (String t, String s) {
 			name = s;
 			total = t;
 			myLeft = myRight = null;
 		}
 		
 		public subExpression (String t, String s, subExpression left, subExpression right) {
 			name = s;
 			total = t;
 			myLeft = left;
 			myRight = right;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		public String getTotal() {
 			return total;
 		}
 		public subExpression getLeft() {
 			return myLeft;
 		}
 		public subExpression getRight() {
 			return myRight;
 		}
 		public boolean isVariable() {
 			if (myLeft == null && myRight == null) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		public boolean equals(subExpression e) {
 			boolean lReturn = false;
 			boolean rReturn = false;
 			if (e == null) {
 				return false;
 			}
 			if (!(name.equals(e.name))){
 				if (ProofChecker.iAmDebugging) {
 					System.out.println("My name"+ name);
 					System.out.println("compare"+e.name);
 				}
 				return false;
 			} else {
 				if (myLeft != null && e.myLeft != null) {
 					lReturn = myLeft.equals(e.myLeft);
 				} else if (myLeft == null && e.myLeft == null) {
 					lReturn = true;
 				}
 				if (myRight != null && e.myRight != null) {
 					rReturn = myRight.equals(e.myRight);
 				} else if (myRight == null && e.myRight == null) {
 					rReturn = true;
 				}
 			}
 			return lReturn && rReturn;
 		}
 
 	}
 }
