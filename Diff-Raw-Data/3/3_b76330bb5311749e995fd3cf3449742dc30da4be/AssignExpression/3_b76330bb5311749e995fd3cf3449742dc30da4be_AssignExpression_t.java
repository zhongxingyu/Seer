 package com.madisp.bad.expr;
 
 import com.madisp.bad.eval.BadConverter;
 import com.madisp.bad.eval.Scope;
 import com.madisp.bad.eval.Watcher;
 
 /**
  * Created with IntelliJ IDEA.
  * User: madis
  * Date: 3/27/13
  * Time: 7:02 PM
  */
 public class AssignExpression implements Expression {
 	private final Expression expr;
 	private final VarExpression var;
 
 	public AssignExpression(VarExpression var, Expression expr) {
 		this.var = var;
 		this.expr = expr;
 	}
 
 	@Override
 	public Object value(Scope scope) {
 		Object newValue = BadConverter.object(expr.value(scope));
 		scope.setVar(var.getBase(scope), var.getIdentifier(), newValue);
 		return newValue;
 	}
 
 	@Override
 	public void addWatcher(Scope scope, Watcher w) {
 		expr.addWatcher(scope, w);
		// we don't need to watch var as it will trigger re-evaluation loop when this expression is watched
//		var.addWatcher(scope, w);
 	}
 }
