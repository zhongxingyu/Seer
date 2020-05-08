 package automation.api;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.jws.WebService;
 import automation.api.interfaces.ConnectedDevice;
 
 @WebService(endpointInterface = "automation.api.interfaces.ConnectedDevice")
 public abstract class AbstractDevice implements ConnectedDevice{
 	
 	private Method method;
 	
 	public AbstractDevice() {
 		method = null;
 		
 		// Allows users to perform some actions when object is created
 		onStartup();
 	}
 	
 	protected abstract void onStartup();
 	
 	@Override
 	final public Object processInput(String methodName) {
 		return processInput(methodName, null);
 	}
 	
 	@Override
 	final public Object processInput(String methodName, Object[] parametersArray) {
		Object ret = 0;
 		
 		try {
 			
 			if (parametersArray == null || parametersArray.length == 0) {
 				method = this.getClass().getMethod(methodName);
 			}
 			else {
 				method = this.getClass().getMethod(methodName, findParameterTypes(parametersArray));
 			}
 			
 			ret = method.invoke(this.getClass(), parametersArray);
			if (ret == null) {
				ret = 0;
			}
 		
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		return ret;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	final private Class[] findParameterTypes(Object[] parameters) {
 		Class[] parameterTypes = new Class[parameters.length];
 		for (int i=0; i<parameters.length; i++) {
 			parameterTypes[i] = parameters[i].getClass();
 		}
 		return parameterTypes;
 	}
 }
