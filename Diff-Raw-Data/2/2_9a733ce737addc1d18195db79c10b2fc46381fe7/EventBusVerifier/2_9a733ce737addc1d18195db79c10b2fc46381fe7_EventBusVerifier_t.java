 package net.premereur.mvp.core.impl;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 
 import net.premereur.mvp.core.Event;
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.UsesView;
 import net.premereur.mvp.core.View;
 import net.premereur.mvp.util.reflection.ReflectionUtil;
 
 public class EventBusVerifier {
 
 	public <E extends EventBus> void verify(Class<E> eventBusClass) {
 		for (Method eventMethod : ReflectionUtil.annotatedMethods(eventBusClass, Event.class)) {
 			Event eventAnt = eventMethod.getAnnotation(Event.class);
 			verifyHandlers(eventAnt.handlers(), eventBusClass);
 			verifyEventBusMethods(eventMethod);
 		}
 	}
 
 	private void verifyHandlers(Class<? extends Presenter<? extends View, ? extends EventBus>>[] handlers, Class<? extends EventBus> eventBusClass) {
 		for (Class<? extends Presenter<? extends View, ? extends EventBus>> handlerClass : handlers) {
 			verifyHasUseViewAnnotation(handlerClass);
 		}
 	}
 
 	private void verifyHasUseViewAnnotation(Class<? extends Presenter<? extends View, ? extends EventBus>> handlerClass) {
 		UsesView viewAnnot = handlerClass.getAnnotation(UsesView.class);
 		if (viewAnnot == null || viewAnnot.value() == null) {
 			throw new IllegalArgumentException("Should use " + UsesView.class.getName() + " annotation to declare view class on " + handlerClass);
 		}
 	}
 
 	private void verifyEventBusMethods(Method m) {
 		verifyNoPrimitiveArguments(m);
 		verifyOnlyVoidMethod(m);
 	}
 
 	private void verifyOnlyVoidMethod(Method m) {
 		if (m.getReturnType().getName() != "void") {
 			throw new IllegalArgumentException("Found a method " + m.getName() + " with non-void return type");
 		}
 	}
 
	private void verifyNoPrimitiveArguments(Method m) {
 		for (Type t : m.getGenericParameterTypes()) {
 			if (t instanceof Class<?> && ((Class<?>) t).isPrimitive()) {
 				throw new IllegalArgumentException("Found a method " + m.getName() + " with primitive argument");
 			}
 		}
 	}
 
 }
