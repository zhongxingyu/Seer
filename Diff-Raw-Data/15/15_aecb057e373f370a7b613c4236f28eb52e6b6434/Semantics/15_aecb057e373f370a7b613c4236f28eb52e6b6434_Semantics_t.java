 import java.util.*;
 
 // Following is the semantics class:
 // The meaning M of a Statement is a State
 // The meaning M of a Expression is a Value
 
 public class Semantics {
 
     private boolean saw_ret = false; // a global flag which causes return statements to stop execution of a block
 
     State M (Program p) { 
 	// The meaning of a program is the meaning of main with both the globals and main's StackFrames on the state's stack.
 
 	State state = initialState(p);
 	state.push(new StackFrame("main", state));
 	return M (state.get_instrs(), state);
 
     }
   
     /* returns the initial state of a program
        pushes globals then main onto stack */
     State initialState (Program p) {
 	StackFrame globals = new StackFrame("globals", p.globals);
 
     	State state = new State(p.functions, globals);
 	
 	//Function main_func = p.functions.get("main");
 	//StackFrame main = new StackFrame("main", globals, null, main_func.params, main_func.locals);
 
 	//state.push(globals);
 	//state.push(main);	
 
 	return state;
 			
 	/*
         State state = new State();
         Value intUndef = new IntValue();
         for (Declaration decl : d)
 	    if (decl instanceof ArrayDecl) {
 	    	ArrayDecl adecl = (ArrayDecl) decl;
 	    	for (int i=0; i<(adecl.size.intValue());i++)
 			state.put(new ArrayRef(adecl.v.toString(), new IntValue(i)), Value.mkValue(adecl.t));
 		}
 	    else 
             	state.put(decl.v, Value.mkValue(decl.t));
         return state;
     	*/
     }
 
     State byValue (Declarations params, ArrayList<Value> args, State state) {
 	for (int i=0; i<params.size(); i++) 
 		state.set(params.get(i).v, args.get(i));	
 	return state;
     }
  
     State M (Statement s, State state) {
         if (s instanceof Skip) return M((Skip)s, state);
         if (s instanceof Assignment)  return M((Assignment)s, state);
         if (s instanceof Conditional)  return M((Conditional)s, state);
         if (s instanceof Loop)  return M((Loop)s, state);
         if (s instanceof Block)  return M((Block)s, state);
 	if (s instanceof CallStatement) return M((CallStatement)s, state);
 	if (s instanceof Return) return M((Return)s, state);
         throw new IllegalArgumentException("should never reach here");
     }
   
     State M (Skip s, State state) {
         return state;
     }
   
     State M (Assignment a, State state) {
     	if (a.target instanceof ArrayRef) {
 		ArrayRef b = (ArrayRef) a.target;
 		ArrayRef r = new ArrayRef(b.id, M(b.index, state));
         	State st = state.set(r, M (a.source, state));
 		return st;
 	}
 	State st = state.set(a.target, M(a.source, state));
 	return st;
     }
   
     State M (Block b, State state) {
         for (Statement s : b.members) {
 	    if (saw_ret) // if the last statement contained a return
 	    	return state;
             state = M (s, state);
 	}
         return state;
     }
   
     State M (Conditional c, State state) {
         if (M(c.test, state).boolValue( ))
             return M (c.thenbranch, state);
         else
             return M (c.elsebranch, state);
     }
   
     State M (Loop l, State state) {
         if (M (l.test, state).boolValue( ) && !saw_ret)
             return M(l, M (l.body, state));
         else return state;
     }
 
     State M (CallStatement c, State state) {
 	//Determine the value of c's args
 	ArrayList<Value> args = new ArrayList<Value>();
 	for (Expression expr : c.args) {
 		Value val = M(expr, state);
 		args.add(val);
 	}
	System.out.println("[ACT ARGS]: " + c.args.toString());
 
     	// push c's stackframe onto stack
 	state.push(new StackFrame(c.name, state));
 
	state.debug();
	System.out.println("[ARG VALS]: " + args.toString());

 	// assign the arguments to the values of the parameters on c's stackframe
 	byValue(state.get_params(), args, state);
 
 	// interpret called funcs body
 	M (state.get_instrs(), state);
 
 	// pop called func's stackframe
 	state.pop();
 
 	// reset saw_ret to catch next function call's return
 	saw_ret = false;
 
 	return state;
     }
 
     State M (Return r, State state) {
 	saw_ret = true;
     	return state.set(r.target, M(r.result, state));
     }
 
     Value applyBinary (Operator op, Value v1, Value v2) {
         StaticTypeCheck.check( ! v1.isUndef( ) && ! v2.isUndef( ),
                "reference to undef value");
         if (op.val.equals(Operator.INT_PLUS)) 
             return new IntValue(v1.intValue( ) + v2.intValue( ));
         if (op.val.equals(Operator.INT_MINUS)) 
             return new IntValue(v1.intValue( ) - v2.intValue( ));
         if (op.val.equals(Operator.INT_TIMES)) 
             return new IntValue(v1.intValue( ) * v2.intValue( ));
         if (op.val.equals(Operator.INT_DIV)) 
             return new IntValue(v1.intValue( ) / v2.intValue( ));
         // student exercise
 	if (op.val.equals(Operator.INT_LT))
 	    return new BoolValue(v1.intValue() < v2.intValue());
 	if (op.val.equals(Operator.INT_GT))
 	    return new BoolValue(v1.intValue() > v2.intValue());
 	if (op.val.equals(Operator.INT_EQ))
 	    return new BoolValue(v1.intValue() == v2.intValue());
 	if (op.val.equals(Operator.INT_NE))
 	    return new BoolValue(v1.intValue() != v2.intValue());
 
 	if (op.val.equals(Operator.FLOAT_LT))
 	    return new BoolValue(v1.floatValue() < v2.floatValue());
 	if (op.val.equals(Operator.FLOAT_GT))
 	    return new BoolValue(v1.floatValue() > v2.floatValue());
 	if (op.val.equals(Operator.FLOAT_EQ))
 	    return new BoolValue(v1.floatValue() == v2.floatValue());
 	if (op.val.equals(Operator.FLOAT_NE))
 	    return new BoolValue(v1.floatValue() != v2.floatValue());
 
 	if (op.val.equals(Operator.FLOAT_PLUS)) 
             return new FloatValue(v1.floatValue( ) + v2.floatValue( ));
         if (op.val.equals(Operator.FLOAT_MINUS)) 
             return new FloatValue(v1.floatValue( ) - v2.floatValue( ));
         if (op.val.equals(Operator.FLOAT_TIMES)) 
             return new FloatValue(v1.floatValue( ) * v2.floatValue( ));
         if (op.val.equals(Operator.FLOAT_DIV)) 
             return new FloatValue(v1.floatValue( ) / v2.floatValue( ));
 
 	if (op.val.equals(Operator.BOOL_LT)) 
             return new BoolValue(v1.intValue( ) < v2.intValue( ));
         if (op.val.equals(Operator.BOOL_GT)) 
             return new BoolValue(v1.intValue( ) > v2.intValue( ));
         if (op.val.equals(Operator.BOOL_EQ)) 
             return new BoolValue(v1.boolValue( ) == v2.boolValue( ));
         if (op.val.equals(Operator.BOOL_NE)) 
             return new BoolValue(v1.boolValue( ) != v2.boolValue( ));
 		if (op.val.equals(Operator.AND))
 			return new BoolValue(v1.boolValue( ) && v2.boolValue( ));
 		if (op.val.equals(Operator.OR))
 			return new BoolValue(v1.boolValue( ) || v2.boolValue( ));
         throw new IllegalArgumentException("should never reach here");
 	
     } 
     
     Value applyUnary (Operator op, Value v) {
         StaticTypeCheck.check( ! v.isUndef( ),
                "reference to undef value");
         if (op.val.equals(Operator.NOT))
             return new BoolValue(!v.boolValue( ));
         else if (op.val.equals(Operator.INT_NEG))
             return new IntValue(-v.intValue( ));
         else if (op.val.equals(Operator.FLOAT_NEG)) {
             return new FloatValue(-v.floatValue( ));
         } else if (op.val.equals(Operator.I2F)) 
             return new FloatValue((float)(v.intValue( ))); 
         else if (op.val.equals(Operator.F2I))
             return new IntValue((int)(v.floatValue( )));
         else if (op.val.equals(Operator.C2I))
             return new IntValue((int)(v.charValue( )));
         else if (op.val.equals(Operator.I2C))
             return new CharValue((char)(v.intValue( )));
         throw new IllegalArgumentException("should never reach here");
     } 
 
     Value M (Expression e, State state) {
         if (e instanceof Value) 
             return (Value)e;
 	if (e instanceof ArrayRef) {
 	    ArrayRef a = (ArrayRef) e;
 	    ArrayRef key = new ArrayRef(a.id, M(a.index, state));
 	    return (Value)(state.get(key));
 	}
         if (e instanceof VariableRef) { 
             return (Value)(state.get((VariableRef)e));
 	    }
         if (e instanceof Binary) {
             Binary b = (Binary)e;
             return applyBinary (b.op, 
                                 M(b.term1, state), M(b.term2, state));
         }
         if (e instanceof Unary) {
             Unary u = (Unary)e;
             return applyUnary(u.op, M(u.term, state));
         }
 	if (e instanceof CallExpression) {
 	    CallExpression c = (CallExpression) e;
 
 		//Determine the value of c's args
 		ArrayList<Value> args = new ArrayList<Value>();
 		for (Expression expr : c.args) {
 			Value val = M(expr, state);
 			args.add(val);
 		}
 
 		// push c's stackframe onto stack
 		state.push(new StackFrame(c.name, state));
 
 		// assign the arguments to the values of the parameters on c's stackframe
 		byValue(state.get_params(), args, state);
 
 		// interpret called funcs body
 		M (state.get_instrs(), state);
 	
 		Value ret = state.get(new Variable("$ret"));
 
 		// pop called func's stackframe
 		state.pop();
 
 		return ret;
     	}
         throw new IllegalArgumentException("should never reach here");
     }
 
     public static void main(String args[]) {
         Parser parser  = new Parser(new Lexer(args[0]));
         Program prog = parser.program();
         prog.display();    // student exercise
         System.out.println("\nBegin type checking...");
         System.out.println("Type map:");
         TypeMap map = StaticTypeCheck.typing(prog.globals, prog.functions);
         map.display();    // student exercise
         StaticTypeCheck.V(prog);
         Program out = TypeTransformer.T(prog, map);
         System.out.println("Output AST");
         out.display();    // student exercise
         Semantics semantics = new Semantics( );
         State state = semantics.M(out);
         System.out.println("Final State");
         state.display( );  // student exercise
     }
 }
