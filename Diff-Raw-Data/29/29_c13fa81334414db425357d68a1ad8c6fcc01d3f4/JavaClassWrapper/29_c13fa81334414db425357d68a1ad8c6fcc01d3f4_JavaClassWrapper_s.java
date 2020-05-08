 package net.primitive.javascript.core.jdk;
 
 import static net.primitive.javascript.core.Convertions.toObject;
 import static org.apache.commons.beanutils.ConstructorUtils.invokeConstructor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import net.primitive.javascript.core.Function;
 import net.primitive.javascript.core.Scope;
 import net.primitive.javascript.core.Scriptable;
 import net.primitive.javascript.core.ScriptableObject;
 
 /**
  * 
  * @author jpalka@gmail.com
  * 
  */
 public class JavaClassWrapper extends ScriptableObject implements Function {
 
 	private final Class<?> javaClass;
 
 	public JavaClassWrapper(Class<?> javaClass) {
 		super();
 		this.javaClass = javaClass;
 	}
 
 	@Override
 	public Object call(Scope scope, Scriptable thisObj, Object[] args) {
 		return null;
 	}
 
 	@Override
 	public Object[] bindParameters(Object[] actualParameters) {
 		return actualParameters;
 	}
 
 	@Override
 	public Scope getScope() {
 		return null;
 	}
 
 	@Override
 	public Scriptable construct(Scope scope, Object[] args) {
 		// unwrap arguments
 		Object[] convertedArgs = new Object[args.length];
 		for (int i = 0; i < args.length; i++) {
			convertedArgs[i] = JavaHost.unwrap((args[i]));
 		}
 
 		try {
 			Object object = invokeConstructor(javaClass, convertedArgs);
 			return new JavaObjectWrapper(object);
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	@Override
 	public Object hasInstance(Object lvar) {
 		return null;
 	}
 
 	@Override
 	public List<String> getParameterList() {
 		return null;
 	}
 
 }
