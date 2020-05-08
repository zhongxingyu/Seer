 package rsaother;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.restlet.resource.ServerResource;
 
 public class RemoteMethod extends ServerResource implements IRemoteRestCall {
 
 	public Object doCall(Object[] args) {
 		List<String> segments = getOriginalRef().getSegments();
 		Object service = RESTServiceAdmin.servicesByID.get(segments.get(0));
		
 		Method method = null;
 		if(segments.size() > 2) {
 			Class[] argumentClasses = new Class[segments.size()-2];
 			for(int i = 0; i < segments.size()-2; i++) {
 				try {
 					String type = segments.get(i+2);
 					Class claz = null;
 					if(type.equals("float")) {
 						claz = float.class;
 					} else if(type.equals("int")) {
 						claz = int.class;
 					} else if(type.equals("byte")) {
 						claz = byte.class;
 					} else if(type.equals("char")) {
 						claz = char.class;
 					} else if(type.equals("short")) {
 						claz = short.class;
 					} else if(type.equals("long")) {
 						claz = long.class;
 					} else if(type.equals("double")) {
 						claz = double.class;
 					} else if(type.equals("boolean")) {
 						claz = boolean.class;
 					} else {
 						claz = Class.forName(segments.get(i+2));
 					}
 					
 					argumentClasses[i] = claz;
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			try {
 				method = service.getClass().getMethod(segments.get(1), argumentClasses);
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				method = service.getClass().getMethod(segments.get(1));
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
		}
 		
 		try {
 			return method.invoke(service, args);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
