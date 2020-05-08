 package syntax;
 
 import symbol.SymbolTable;
 import interpreter.Interpreter;
 
 
 public class AnonymousFunction extends Value{
 	public Variable arg;
 	public Expression body;
 	public SymbolTable localTable=null;

	public String totalToString(){
		return "fun " + arg.toString() + " -> " + body.toString() + " under environment "+ localTable.toString()+ "";
	}
 	
	/*
	 * since symbolTable may contain AnonymousFunction function, if this happens, it will enter a call circle, then stack overflow
	 */
 	public String toString(){
		return "fun " + arg.toString() + " -> " + body.toString();
 	}
 	private AnonymousFunction(){};
 	
 	public AnonymousFunction(Object yysv, Object yysv2) {
 		this.arg = (Variable) yysv;
 		this.body = (Expression) yysv2;
 		localTable = (SymbolTable) Interpreter.symbolTable.clone();
 	}
 	
 	
 	/*
 	 * this method has some problem, it make all AnonymousFunctin the same type.
 	 * actually this is not true, because you can't do such thing:
 	 * let f1 = fun x -> fun y -> x+y
 	 * let f2 = fux x -> x && false
 	 * you can't apply f2 to where should be f1
 	 */
 	public Object eval() {
 		return this;
 	}
 	
 	public Object apply(Expression e){
 		Object result = null;
 		if ( body instanceof AnonymousFunction){
 			AnonymousFunction resultFun = new AnonymousFunction();
 			resultFun.arg = ((AnonymousFunction)body).arg;
 			resultFun.body = ((AnonymousFunction)body).body;
 			resultFun.localTable = (SymbolTable) ((AnonymousFunction)this).localTable.clone();
 			((AnonymousFunction)resultFun).localTable.push(arg, e.eval());
			result = resultFun.eval();
			System.out.println("application apply: "+ resultFun.totalToString());
 		} else {
 			this.localTable.push(arg, e.eval());
 			SymbolTable tmp = Interpreter.symbolTable;
 			Interpreter.symbolTable = localTable;
 			result = body.eval();
 			Interpreter.symbolTable = tmp;
 		}
 		return result;
 	}
 }
