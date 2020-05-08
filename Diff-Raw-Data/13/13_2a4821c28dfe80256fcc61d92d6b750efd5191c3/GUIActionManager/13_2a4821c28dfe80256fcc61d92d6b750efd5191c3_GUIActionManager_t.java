 package org.micoli.phone.tools;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 import net.sourceforge.peers.Logger;
 
 import org.micoli.phone.ccphone.remote.VertX;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonObject;
 
 
 public class GUIActionManager {
 	public static HashMap<String, Method> listCommand = new HashMap<String, Method>();
 
 	public static void scan(final Object container,Logger logger) {
 		for (Method method : container.getClass().getMethods()) {
 			if (method.isAnnotationPresent(GUIAction.class)) {
 				GUIAction annotation = method.getAnnotation(GUIAction.class);
 				annotation.annotationType().toString();
 				listCommand.put(method.getName(), method);
 				final String MethodName = method.getName();
 				logger.info("init GUIAction."+method.getName());
 				VertX.vertx.eventBus().registerHandler("guiaction."+method.getName(), new Handler<Message<JsonObject>>() {
 					public void handle(Message<JsonObject> event) {
 						try {
 							GUIActionManager.listCommand.get(MethodName).invoke(container, event);
 						} catch (IllegalAccessException
 								| IllegalArgumentException
 								| InvocationTargetException e) {
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		}
 	}
 }
