 package syntax;
 
 import semantic.Env;
 import type.BoolType;
import type.UnitType;
 
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
 
	private boolean executeCondition(Env env){
 		Value condval = condition.execute(env);
 		condval.check(BoolType.getInstance(), false);
 		BoolValue bcondValue = (BoolValue)condval;
		return bcondValue.value;
	}
	@Override
	public Value execute(Env env) {
		Value executeValue ;
		while (executeCondition(env)) {
			executeValue = body.execute(env);
			executeValue.check(UnitType.getInstance(), false);
 		}
		return new UnitValue(line, column);
 	}
 }
