 package syntax;
 
 import interpreter.Interpreter;
 import syntax.BinaryOperation.BinaryOperator;
 import syntax.UnaryOperation.UnaryOperator;
 
 public class Util {
 
 	
 	public static boolean twoExprTypeEqual(Expression e1, Expression e2) {
 		if (e1 == null || e2 == null){
 			System.out.println("Runtime Error");
 			if (Interpreter.debug){
 				System.out.println("In twoExprTypeEqual: " + e1 + e2);
 			}
 			System.exit(-1);
 		}
 		
 		Value t1 = getType(e1);
 		Value t2 = getType(e2);
 		
 		// at least one is variable
 		if (t1 == null || t2 == null)
 			return true;
 		
 		if (t1 instanceof ListValue && t2 instanceof Nil){
 			return true;
 		}
 		if (t1 instanceof Nil && t2 instanceof ListValue){
 			return true;
 		}
 	
 		if (!t1.getClass().equals(t2.getClass())){
 			return false;
 		}
 		if (t1 instanceof AnonymousFunction){
 			return twoAnonFunTypeEqual((AnonymousFunction)t1, (AnonymousFunction)t2);
 		}
 		if (t1 instanceof PairValue){
 			PairValue pv1 = (PairValue) t1;
 			PairValue pv2 = (PairValue) t2;
 			return twoPairValueTypeEqual(pv1, pv2);
 		}
 		
 		if (t1 instanceof ListValue){
 			ListValue lv1 = (ListValue) t1;
 			ListValue lv2 = (ListValue) t2;
 			return twoListValueTypeEqual(lv1, lv2);
 		}
 		return true;
 	}
 	
 	public static boolean twoRetTypeEqual(Expression e1, Expression e2) {
 		if (e1 == null || e2 == null){
 			System.out.println("Runtime Error");
 			if (Interpreter.debug){
 				System.out.println("In twoExprTypeEqual: " + e1 + e2);
 			}
 			System.exit(-1);
 		}
 		Value t1 = getType(e1);
 		Value t2 = getType(e2);
 		
 		if (t1 == null && t2 == null){
 			return true;
 		}else if (t1 == null || t2 == null){
 			return false;
 		}
 		
 		if (t1 instanceof ListValue && t2 instanceof Nil){
 			return true;
 		}
 		if (t1 instanceof Nil && t2 instanceof ListValue){
 			return true;
 		}
 	
 		if (!t1.getClass().equals(t2.getClass())){
 			return false;
 		}
 		if (t1 instanceof AnonymousFunction){
 			return twoAnonFunTypeEqual((AnonymousFunction)t1, (AnonymousFunction)t2);
 		}
 		if (t1 instanceof PairValue){
 			PairValue pv1 = (PairValue) t1;
 			PairValue pv2 = (PairValue) t2;
 			return twoPairValueTypeEqual(pv1, pv2);
 		}
 		
 		if (t1 instanceof ListValue){
 			ListValue lv1 = (ListValue) t1;
 			ListValue lv2 = (ListValue) t2;
 			return twoListValueTypeEqual(lv1, lv2);
 		}
 		return true;
 	}
 
 	private static boolean twoListValueTypeEqual(ListValue lv1, ListValue lv2){
 		/*
 		if (!(lv1.head.getClass().equals(lv2.head.getClass())))
 			return false;
 		*/
 		if (!twoExprTypeEqual(lv1.head,lv2.head))
 			return false;
 		return true;
 	}
 	private static boolean twoPairValueTypeEqual(PairValue pv1, PairValue pv2){
 		/*
 		if (!pv1.first.getClass().equals(pv1.first.getClass()))
 			return false;
 		if (!pv1.second.getClass().equals(pv1.second.getClass()))
 			return false;
 		*/
 		if (!twoExprTypeEqual(pv1.first,pv2.first))
 			return false;		
 		if (!twoExprTypeEqual(pv1.second,pv2.second))
 			return false;
 		return true;		
 	}
 	
 	private static boolean twoAnonFunTypeEqual(AnonymousFunction a1, AnonymousFunction a2){
 		int argc1=1, argc2=1;
 		Expression e1 = a1.body;		
 		while ( e1 instanceof AnonymousFunction) {
 			argc1++;
 			e1 = ((AnonymousFunction)e1).body;			
 		}
 		Expression e2 = a2.body;		
 		while ( e2 instanceof AnonymousFunction) {
 			argc2++;
 			e2 = ((AnonymousFunction)e2).body;			
 		}
 		
 		if (argc1 != argc2)
 			return false;		
 		return (twoRetTypeEqual(e1, e2));
 		
 	}
 	
 	private static Value getType(Expression e){
 		try {
 			while (e instanceof Sequence){
 				e = ((Sequence)(e)).e2;
 			}
 			if (e instanceof AnonymousFunction){
 				return ((AnonymousFunction)(e));
 			}
 			if (e instanceof Application){
				//return getType(((Application)(e)).func);
				return null;
 			}
 			if (e instanceof Assignment){
 				return new Nop();
 			}
 			if (e instanceof BinaryOperation){
 				if (((BinaryOperation)(e)).op == BinaryOperator.plus ||
 					((BinaryOperation)(e)).op == BinaryOperator.minus ||
 					((BinaryOperation)(e)).op == BinaryOperator.times ||
 					((BinaryOperation)(e)).op == BinaryOperator.devide
 				   )
 					return new IntValue(1);
 				else
 					return new BoolValue(false);
 			}
 			if (e instanceof BoolValue){
 				return new BoolValue(false);
 			}
 			if (e instanceof Bracket){
 				return getType(((Bracket)(e)).e);
 			}
 			if (e instanceof First){
 				PairValue p = (PairValue) getType(((First)(e)).e);
 				return getType(p.first);
 			}
 			if (e instanceof Head){
 				Head tmp = (Head) e;
 				ListValue lv = (ListValue)getType(tmp.e);
 				return getType( lv.head );
 			}
 			if (e instanceof IfThenElse){
 				return getType(((IfThenElse)(e)).thenClause);
 			}
 			if (e instanceof IntValue){
 				return new IntValue(1);
 			}
 			if (e instanceof LetInEnd){
 				return getType(((LetInEnd)(e)).body);
 			}
 			
 			/*
 			 * two expr return list
 			 */
 			if (e instanceof List){
 				List l = (List) e;
 				return new ListValue(getType(l.head),getType(l.tail));
 			}
 			if (e instanceof ListValue){
 				return ((ListValue)e);
 			}
 			if (e instanceof Tail){
 				Tail t = (Tail) e;
 				ListValue lv = (ListValue) getType(t.e);
 				return getType(lv.tail);
 			}
 			if ( e instanceof Nil){
 				return new Nil();
 			}
 			
 			
 			
 			if (e instanceof Nop){
 				return new Nop();
 			}
 			
 			/*
 			 * pair
 			 */
 			if (e instanceof Pair){
 				return new PairValue(getType(((Pair)(e)).e1), getType(((Pair)(e)).e2));
 			}
 			if (e instanceof PairValue){
 				return ((PairValue)(e));
 			}
 			
 			if(e instanceof Second){
 				Second tmp = (Second) e;
 				PairValue pv = (PairValue) getType(tmp.e);
 				return getType( pv.second );
 			}
 			
 			if (e instanceof UnaryOperation){
 				if (((UnaryOperation)(e)).op == UnaryOperator.negative)
 					return new IntValue(1);
 				else
 					return new BoolValue(false);
 			}
 			
 			if (e instanceof Variable){
				//this may return null, but it is no problem
 				return null;
				//return Interpreter.symbolTable.get((Variable)e);
			}	
 			
 			if (e instanceof WhileDoEnd){
 				return new Nop();
 			}
 		} catch (NullPointerException npe){
 			if (Interpreter.debug)
 			{
 				npe.printStackTrace();
 			}
 			return null;
 		} catch (Exception ex){
 			System.out.println("Type Error!");
 			if (Interpreter.debug)
 			{
 				ex.printStackTrace();
 			}
 			System.exit(-1);
 		}
 		return null;
 	}
 }
