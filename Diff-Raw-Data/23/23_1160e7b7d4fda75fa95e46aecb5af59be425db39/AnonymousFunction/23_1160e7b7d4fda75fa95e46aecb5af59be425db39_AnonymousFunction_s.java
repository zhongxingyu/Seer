 package syntax;
 
 import semantic.Block;
 import semantic.Env;
 import semantic.UnexpectedException;
 import type.FunType;
 import type.NullType;
 import type.Type;
 
 public class AnonymousFunction extends Value {
 	Variable arg;
 	Expression body;
 	Block execBlock;
 
 	public AnonymousFunction(String x, int xl, int xc, Object e, int l, int c) {
 		super(l, c);
 		arg = new Variable(x, xl, xc);
 		body = (Expression) e;
 		type = new FunType(NullType.getInstance(), NullType.getInstance());
 		execBlock = new Block();
 	}
 
 	public Variable getArg() {
 		return arg;
 	}
 
 	public Expression getBody() {
 		return body;
 	}
 
 	public String toString() {
 		return "fun " + arg.toString() + " -> " + body.toString();
 	}
 
 	private Type type;
 
 	@Override
 	public Type getType() {
 		return type;
 	}
 
 	@Override
 	public Value execute(Env env) {
 		report();
		execBlock = env.getCurrentBlock();
 		return this;
 	}
 
 	@Override
 	public boolean equals(Value other) {
 		if (this == other) {
 			return true;
 		}
 		throw new UnexpectedException("cannot compare two AnonymousFunction values");
 	}
 }
