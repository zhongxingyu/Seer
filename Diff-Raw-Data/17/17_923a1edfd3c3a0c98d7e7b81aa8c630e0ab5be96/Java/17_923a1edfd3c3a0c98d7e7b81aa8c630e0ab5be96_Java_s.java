 package posl.java;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import posl.engine.api.Executable;
 import posl.engine.core.Scope;
 import posl.engine.error.PoslException;
 import posl.engine.type.Atom;
 import posl.engine.type.SingleStatement;
 
 public class Java implements Executable {
 
 	private Object object;
 	private Class<?> klass;
 	private Map<String, Executable> names;
 	//private boolean isStatic;
 
 	/**
 	 * 
 	 * 
 	 * @param args
 	 * @param scope
 	 * @throws ClassNotFoundException 
 	 * @throws Exception
 	 */
 	public Java(Scope scope, SingleStatement args) throws PoslException, ClassNotFoundException {
 		klass = Class.forName(scope.get(Atom.class,args.get(1)).toString());
 		//Constructor<?>[] constructors = klass.getConstructors();
 		try {
 			setObject(klass.newInstance());
 		} catch(Exception e){
 			//in this case do nothing as this indicates that the constructor
 			//in question is private and we are treating as a static member
 		}
 		
 		populateClassInfo();
 	}
 
 
 	
 	public Java(Object instance) throws Exception {
 		klass = instance.getClass();
 		setObject(instance);
 
 		populateClassInfo();
 	}
 	
 	private void populateClassInfo() {
 		Field[] fields = klass.getFields();
 		Method[] methods = klass.getMethods();
 		
 		names = new HashMap<String,Executable>();
 		
 		for (Field field:fields){
 			names.put(field.getName(), new JavaField(getObject(),field));
 		}
 		
 		for (Method method:methods){
 			String methodName = method.getName();
 			if (names.containsKey(methodName)) {
 				((JavaMethod)names.get(methodName)).addMethod(method);
 			} else {
 				names.put(methodName, new JavaMethod(getObject(),method));
 			}
 		}
 	}
 
 	@Override
 	public Object execute(Scope scope, SingleStatement tokens) throws PoslException {
 		if(tokens.size() == 1) return this;
 		String methodName = scope.get(Atom.class,tokens.get(1)).toString();
		SingleStatement args = new SingleStatement(tokens.startLineNumber());
 		for(int i = 2; i < tokens.size(); i++){
 			args.add(tokens.get(i));
 		}
 		return names.get(methodName).execute(scope, args);
 	}
 
 	public void setObject(Object object) {
 		this.object = object;
 	}
 
 	public Object getObject() {
 		return object;
 	}
 
 }
