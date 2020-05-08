 package ash.vm;
 
 import java.io.Serializable;
 
 public final class Scope implements Serializable {
 	private static final long serialVersionUID = 569126524646085504L;
 	
 	final Scope prevScope;
 	final Object[] environment;
 	
 	public Scope(Scope srcScope, Object[] env) {
 		prevScope = srcScope;
 		environment = env;
 	}
 
 	public Object queryArg(int argIndex) {
 		if (argIndex < environment.length)
 			return environment[argIndex];
 		else
			return prevScope.queryArg(argIndex - environment.length);
 	}
 }
