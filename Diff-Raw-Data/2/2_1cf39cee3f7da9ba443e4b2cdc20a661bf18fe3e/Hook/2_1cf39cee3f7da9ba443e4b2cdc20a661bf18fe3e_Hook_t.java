 package gmod;
 
 import java.lang.reflect.Method;
 import java.util.Set;
 
 import com.google.common.collect.Sets;
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 public class Hook {
 
 	private static final Handler handler = new Handler();
 	private static final EventBus eventBus = new EventBus();
 	private static final Set<String> registeredHooks = Sets.newHashSet();
 
 	public static void register(Object o) {
 		eventBus.register(o);
 
 		for (Method m : o.getClass().getMethods()) {
 			if (!m.isAnnotationPresent(Subscribe.class)) {
 				System.out.println("Continuing because no subscribe");
 				continue;
 			}
 
 			if (m.getParameterTypes().length != 1) {
 				System.out.println("Continuing because params != 1");
 				continue;
 			}
 
 			Class<?> parmClass = m.getParameterTypes()[0];
			if (!Event.class.isAssignableFrom(parmClass)) {
 				System.out.println("Continuing because parm not event");
 				continue;
 			}
 
 			Class<? extends Event> eventClass = parmClass.asSubclass(Event.class);
 			if (!eventClass.isAnnotationPresent(Event.Info.class)) {
 				System.out.println("Continuing because no Event.Info");
 				continue;
 			}
 
 			Event.Info eventInfo = eventClass.getAnnotation(Event.Info.class);
 			if (!registeredHooks.contains(eventInfo.name())) {
 				Lua.getglobal("hook");
 				Lua.getfield(-1, "Add");
 				Lua.pushstring(eventInfo.name());
 				Lua.pushstring("java." + eventClass.getName());
 				Lua.pushobject(eventClass);
 				Lua.pushclosure(handler, 1);
 				System.out.println("Hooking " + eventInfo.name() + " (" + eventClass.getName() + ")...");
 				Lua.call(3, 0);
 				registeredHooks.add(eventInfo.name());
 			}
 		}
 	}
 
 	public static void unregister(Object o) {
 		eventBus.unregister(o);
 	}
 
 	private static class Handler implements Lua.Function {
 
 		@Override
 		public int invoke() throws Exception {
 			Class<? extends Event> eventClass = ((Class<?>) Lua.toobject(Lua.upvalueindex(1))).asSubclass(Event.class);
 			if (eventClass != null) {
 				Event event = eventClass.newInstance();
 				eventBus.post(event);
 			}
 
 			return 0;
 		}
 
 	}
 
 }
