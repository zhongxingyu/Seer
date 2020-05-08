 package org.jnizon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class FunctionCall implements Expression {
 
 	private Identifier functionId;
 	private List<Expression> arguments;
 
 	public FunctionCall(Identifier functionId, List<Expression> arguments) {
 		this.functionId = functionId;
 		this.arguments = arguments;
 	}
 
 	@Override
 	public Expression evaluate(Context ctx) {
 		Expression functionResult;//TODO not very clean, must find a way to conceal javafuncs & inline funcs
 		List<Expression> args = new ArrayList<Expression>();
 		for (Expression arg : arguments) {
 			Expression result = arg.evaluate(ctx);
 			args.add(result);
 		}
 		Expression function = ctx.get(functionId);
 		if (function instanceof JavaFunction) {
 			functionResult = ((JavaFunction)function).execute(ctx, args);
 		} else if (function instanceof InlineFunction){
 			Identifier[] argumentIds = ((InlineFunction)function).getArguments();
 			Context funcContext = ctx.derivate();
 			for (int i = 0; i < argumentIds.length; i++) {
 				Identifier argid = argumentIds[i];
				funcContext.put(argid, args.get(i));
 			}
 
 			functionResult = function.evaluate(funcContext);
 		} else {
 			return new FunctionCall(functionId, args);
 		}
 		if (functionResult == function) {
 			return new FunctionCall(functionId, args);
 		}
 		return functionResult;
 	}
 
 	@Override
 	public int getChildCount() {
 		return 1 + arguments.size();
 	}
 
 	@Override
 	public Expression getChild(int index) {
 		if (index == 0)
 			return functionId;
 		else if (index < arguments.size() + 1)
 			return arguments.get(index - 1);
 		throw new RuntimeException("Ouf of bounds");
 	}
 
 	public Identifier getFunctionId() {
 		return functionId;
 	}
 
 	public List<Expression> getArguments() {
 		return arguments;
 	}
 
 	@Override
 	public boolean equals(Expression expr) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
