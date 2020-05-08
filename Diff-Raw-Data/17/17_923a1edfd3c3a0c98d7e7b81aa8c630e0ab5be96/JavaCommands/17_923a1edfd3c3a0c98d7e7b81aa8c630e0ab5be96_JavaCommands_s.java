 package posl.lang;
 
 
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.List;
 
 import posl.engine.Interpreter;
 import posl.engine.annotation.Command;
 import posl.engine.core.Scope;
 import posl.engine.error.PoslException;
 import posl.engine.type.Atom;
 import posl.engine.type.MultiLineStatement;
 import posl.engine.type.SingleStatement;
 import posl.java.Java;
 import posl.java.JavaInvocationHandler;
 
 public class JavaCommands {
 
 	
 	@Command("java")
 	//TODO - fix
 	public static Object ceateJava(Scope scope, SingleStatement statement)
 			throws PoslException, ClassNotFoundException {
 		if(scope.containsKey(statement.get(1).toString())){
 			ArrayList<Object> derivedArgs = new ArrayList<Object>();
 			derivedArgs.add(statement.get(0));
 			derivedArgs.add(scope.get(statement.get(1).toString()));
 			SingleStatement newArgs = new SingleStatement(derivedArgs);
 			return new Java(scope, newArgs);
 		}
 		return new Java(scope, statement);
 	}
 	
 	@Command("importJava")
 	//TODO - fix this with the new @Collection object
 	public static void alias(Scope scope, SingleStatement args) throws Exception {
 		String []pathElements = args.get(1).toString().split("[.]");
 		String className = pathElements[pathElements.length - 1];
 		scope.put(className, new Atom(args.get(1).toString()));
 	}
 	
 	@Command("proxy")
	public static Object proxy(final Scope scope, List<?> klassNames, MultiLineStatement statement)
 			throws Exception, PoslException {
 		Scope child = scope.createChildScope();
		Interpreter.processList(child, statement);
 		Class<?>[] klasses = new Class[klassNames.size()];
 		int index = 0;
 		for (Object object : klassNames) {
 			if (!(object instanceof String)) {
 				return new Error("expected identifier, found "
 						+ object.toString() + "\n");
 			}
 			klasses[index++] = Class.forName((String)object);
 		}
 		return Proxy.newProxyInstance(JavaCommands.class.getClassLoader(),
 				klasses,new JavaInvocationHandler(child));
 	}
 
 }
