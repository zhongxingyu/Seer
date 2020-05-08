 package syntax;
 
 import semantic.Env;
 import type.BoolType;
 
 public class WhileDoEnd extends Expression{
 	Expression condition;
 	Expression body;
 	
 	public WhileDoEnd(Object e1, Object e2, int l, int c) {
 		super(l, c);
 		condition = (Expression)e1;
 		body = (Expression)e2;
 	}
 
 	public String toString(){
 		return "while " + condition.toString() + " do " + body.toString() + " end";
 	}
 
	@Override
	public Value execute(Env env) {
 		Value condval = condition.execute(env);
 		condval.check(BoolType.getInstance(), false);
 		BoolValue bcondValue = (BoolValue)condval;
		Value returnValue = condval;
		while (bcondValue.value) {
			returnValue = body.execute(env);
 		}
		return returnValue;
 	}
 }
