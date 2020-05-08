 package syntax;
 
 import symbol.SymbolTable;
 import interpreter.Interpreter;
 
 
 public class AnonymousFunction extends Value{
 	public Variable arg;
 	public Expression body;
 	public SymbolTable localTable=null;
 	
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
 		AnonymousFunction result = new AnonymousFunction();
 		result.arg = this.arg;
 		result.body = this.body;
 		result.localTable = (SymbolTable) Interpreter.symbolTable.clone();
 		return this;
 	}
 	
 	public Object apply(Expression e){
		int enter = localTable.getSize();
 		Object result = null;
 		Interpreter.symbolTable.push(arg, e.eval());
 		result = body.eval();
		localTable.popTo(enter);
 		System.out.println("application apply: "+ result.toString());
 		return result;
 	}
 }
