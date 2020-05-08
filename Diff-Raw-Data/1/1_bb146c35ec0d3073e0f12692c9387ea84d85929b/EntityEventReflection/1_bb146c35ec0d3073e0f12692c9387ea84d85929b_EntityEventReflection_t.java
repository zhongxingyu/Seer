 package pl.shockah.easyslick;
 
 import java.lang.reflect.Method;
 
 public class EntityEventReflection extends EntityEvent {
 	protected final Method method;
 	protected final String methodName;
 	
 	public EntityEventReflection(String methodName) {this(true,false,methodName);}
 	public EntityEventReflection(Method method) {this(true,false,method);}
 	public EntityEventReflection(boolean tick, boolean render, String methodName) {
 		super(tick,render);
 		this.methodName = methodName;
 		method = null;
 	}
 	public EntityEventReflection(boolean tick, boolean render, Method method) {
 		super(tick,render);
 		this.method = method;
 		methodName = null;
 		
 		method.setAccessible(true);
 	}
 	
 	protected void onEvent(Entity e) {
 		if (method == null) {
 			Class<?> cls = e.getClass();
 			while (true) {
 				if (cls == Entity.class) return;
 				try {
 					Method m = cls.getDeclaredMethod(methodName);
 					m.setAccessible(true);
 					m.invoke(e);
					return;
 				} catch (NoSuchMethodException e1) {
 					cls = cls.getSuperclass();
 					continue;
 				} catch (Exception e1) {App.getApp().handle(e1);}
 			}
 		} else {
 			try {
 				method.invoke(e);
 			} catch (Exception e1) {App.getApp().handle(e1);}
 		}
 	}
 	protected boolean eventCheck(Entity e) {
 		return true;
 	}
 }
