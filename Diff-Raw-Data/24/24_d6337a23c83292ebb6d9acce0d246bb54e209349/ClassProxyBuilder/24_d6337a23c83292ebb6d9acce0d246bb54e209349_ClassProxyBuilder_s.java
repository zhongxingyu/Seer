 package org.jackie.event.impl.proxygen;
 
 import org.jackie.asmtools.ClassBuilder;
 import org.jackie.asmtools.ConstructorBuilder;
 import org.jackie.asmtools.MethodBuilder;
 import org.jackie.event.Event;
 import org.jackie.event.EventManagerException;
 import org.jackie.event.impl.MethodDef;
 import static org.jackie.utils.JavaHelper.FALSE;
 import org.jackie.utils.Log;
 import org.objectweb.asm.Type;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class ClassProxyBuilder extends ClassBuilder {
 
 	static public String SUFFIX = "$$EventProxy$$";
 
 	Class<?> type;
 
 	public ClassProxyBuilder(Class<? extends Event> type) {
 		validateEventClass(type);
 		this.type = type;
 	}
 
 	public String classname() {
 		return type.getName() + SUFFIX;
 	}
 
 	protected Class superclass() {
		return type;
 	}
 
 	protected void constructors() {
 		execute(new ConstructorBuilder(this) {
 			protected void body() {
 				// todo provide API for constructors
 				mv.visitVarInsn(ALOAD, 0);
 				mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(superclass()), "<init>", "()V");
 				mv.visitInsn(RETURN);
 			}
 		});
 	}
 
 	protected void methods() {
		List<MethodDef> methods = gatherValidMethods(superclass());
 
 		for (MethodDef mdef : methods) {
 			final Method m = mdef.getMethod();
 			Log.debug("Generating event proxy bytecode for %s.%s", superclass().getName(), mdef);
 			execute(new MethodBuilder(this, m.getName(), m.getReturnType(), m.getParameterTypes()) {
 				protected void body() {
					execute(new InvokeEvent(this, superclass(), m));
 					doreturn();
 				}
 			});
 		}
 	}
 
 	List<MethodDef> gatherValidMethods(Class cls) {
 		List<MethodDef> mdefs = new LinkedList<MethodDef>();
 		for (Method method : cls.getDeclaredMethods()) {
 			checkValidEventMethod(method);
 			mdefs.add(new MethodDef(method));
 		}
 		for (Class iface : cls.getInterfaces()) {
 			mdefs.addAll(gatherValidMethods(iface));
 		}
 		return mdefs;
 	}
 
 	void validateEventClass(Class cls) {
 		boolean valid = true;
 
 		valid &= Event.class.isAssignableFrom(cls);
 		valid &= Modifier.isPublic(cls.getModifiers());
 
 		if (!valid) {
 			throw new EventManagerException(
 					"Invalid event class: %s. Must be declared \"public\" and \"extends/implements %s\"",
 					cls, Event.class);
 		}
 	}
 
 	void checkValidEventMethod(Method method) {
 		boolean valid = true;
 
 		valid &= FALSE(Modifier.isStatic(method.getModifiers()));
 		valid &= FALSE(Modifier.isNative(method.getModifiers()));
 		valid &= Modifier.isPublic(method.getModifiers());
 		valid &= method.getReturnType().equals(void.class);
 
 		if (!valid) {
 			throw new EventManagerException(
 					"Invalid event method: %s. Must be declared \"public void\" and cannot be static or native.",
 					method);
 		}
 	}
 
 }
