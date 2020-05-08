 package dragonfin.templates;
 
 class FunctionCall extends Expression
 {
 	String functionName;
 	List<Argument> arguments;
 
 	FunctionCall(String functionName, List<Argument> arguments)
 	{
 		this.functionName = functionName;
 		this.arguments = arguments;
 	}
 
 	@Override
 	Object evaluate(Context ctx)
 		throws TemplateRuntimeException
 	{
 		throw new TemplateRuntimeException("not implemented");
 	}
 }
