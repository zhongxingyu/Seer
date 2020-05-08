 package cudl.script;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 public class Scripting {
	private Context context;
 	private Map<Scope, ScriptableObject> newScopes = new HashMap<Scope, ScriptableObject>();
 	private ScriptableObject currentScope;
 	private Logger LOGGER = Logger.getRootLogger();
 
 	public Scripting() {
		context = ContextFactory.getGlobal().enterContext();
 		enterScope(Scope.SESSION);
 	}
 
 	public void put(String name, String value) {
 		currentScope.put(name, currentScope, eval(value));
 	}
 
 	public void set(String name, String value) {
 		context = ContextFactory.getGlobal().enterContext();
 		context.evaluateString(currentScope, name, name, 1, null);
 		Scriptable declarationScope = searchDeclarationScope(name);
 		if(declarationScope != null){
 			declarationScope.put(name, declarationScope, eval(value));
 		} else {
 			context.evaluateString(currentScope, name + " = " + value, name + " = " + value, 1, null);
 		}
 	}
 
 	public Object get(String name) {
 		context = ContextFactory.getGlobal().enterContext();
 		return context.evaluateString(currentScope, name, name, 1, null);
 	}
 
 	public Object eval(String script) {
 		context = ContextFactory.getGlobal().enterContext();
 		Object evaluateString = context.evaluateString(currentScope, script, script, 1, null);
 		return evaluateString;
 	}
 
 	public void enterScope(Scope scope) {
 		LOGGER.debug("entering scope " + scope);
 		currentScope = createScope(getParentScope(scope));
 		currentScope.put(scope.toString(), currentScope, currentScope);
 		newScopes.put(scope, currentScope);
 	}
 
 	private ScriptableObject getParentScope(Scope scope) {
 		return newScopes.get(scope.parent());
 	}
 
 	private ScriptableObject createScope(ScriptableObject parentScope) {
 		ScriptableObject newScope = context.initStandardObjects();
 		newScope.setParentScope(parentScope);
 		return newScope;
 	}
 
 	private Scriptable searchDeclarationScope(String name) {
 		Scriptable declarationScope = currentScope;
 		while (declarationScope != null && !declarationScope.has(name, declarationScope)) {
 			declarationScope = declarationScope.getParentScope();
 		}
 		return declarationScope;
 	}
 
 	public enum Scope {
 		SESSION {
 			@Override
 			Scope parent() {
 				return null;
 			}
 		},
 		APPLICATION {
 			@Override
 			Scope parent() {
 				return SESSION;
 			}
 		},
 		DOCUMENT {
 			@Override
 			Scope parent() {
 				return APPLICATION;
 			}
 		},
 		DIALOG {
 			@Override
 			Scope parent() {
 				return DOCUMENT;
 			}
 		},
 		ANONYME {
 			@Override
 			Scope parent() {
 				return DIALOG;
 			}
 		};
 
 		abstract Scope parent();
 
 		@Override
 		public String toString() {
 			return super.toString().toLowerCase();
 		}
 	};
 }
