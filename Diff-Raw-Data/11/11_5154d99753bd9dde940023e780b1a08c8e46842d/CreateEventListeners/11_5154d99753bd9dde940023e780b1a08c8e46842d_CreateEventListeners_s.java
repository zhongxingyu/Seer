 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.events.visitor;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.eclipse.swt.widgets.Widget;
 
 import com.swtxml.definition.IAttributeDefinition;
 import com.swtxml.events.EventListenerException;
 import com.swtxml.events.impl.Events;
 import com.swtxml.events.registry.WidgetEvent;
 import com.swtxml.swt.metadata.WidgetTag;
 import com.swtxml.tinydom.ITagVisitor;
 import com.swtxml.tinydom.Tag;
 import com.swtxml.util.lang.ContractProof;
 import com.swtxml.util.reflector.Reflector;
 import com.swtxml.util.reflector.ReflectorException;
 import com.swtxml.util.reflector.Subclasses;
 import com.swtxml.util.reflector.Visibility;
 
 /**
  * CreateEventListenersVisitor processes attributes like
  * on:<eventName>="<viewMethod>" and delegates the event to a method
  * <viewObject>.<viewMethod>.
  * 
  * @author Ralf Ebert <info@ralfebert.de>
  */
 public class CreateEventListeners implements ITagVisitor {
 
 	private Object viewObject;
 
 	public CreateEventListeners(Object viewObject) {
 		this.viewObject = viewObject;
 	}
 
 	public void visit(Tag tag) {
 		if (!(tag.getTagDefinition() instanceof WidgetTag)) {
 			return;
 		}
 
 		Class<? extends Widget> widgetClass = ((WidgetTag) tag.getTagDefinition()).getWidgetClass();
 
 		for (IAttributeDefinition event : tag.getAttributes(Events.NAMESPACE)) {
 			wireViewMethodListener(tag.getAttribute(Events.NAMESPACE, event), tag
 					.adaptTo(widgetClass), event.getName());
 		}
 	}
 
 	/**
 	 * Creates and adds a <listenerProxy> object implementing the SWT listener
 	 * interface to <widget> which calls this.viewObject.<viewMethodName> when
 	 * listener.<eventName> is called.
 	 */
 	public void wireViewMethodListener(String viewMethodName, Widget widget, final String eventName) {
 
 		WidgetEvent event = Events.EVENTS.getWidgetEvent(widget.getClass(), eventName);
 		ContractProof.notNull(event, "event");
 
 		final Method viewMethod = Reflector.findMethods(Visibility.PRIVATE, Subclasses.INCLUDE)
 				.name(viewMethodName.trim()).optionalParameter(event.getEventParamType()).exactOne(
 						viewObject.getClass());
 
 		InvocationHandler handler = new InvocationHandler() {
 
 			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 				if (!method.getName().equals(eventName)) {
 					return null;
 				}
 				viewMethod.setAccessible(true);
 				Object[] parameters = args;
 				if (viewMethod.getParameterTypes().length == 0) {
 					parameters = new Object[0];
 				}
 				try {
 					return viewMethod.invoke(viewObject, parameters);
				} catch (RuntimeException e) {
					throw e;
 				} catch (Exception e) {
 					throw new EventListenerException(e);
 				}
 			}
 
 		};
 
 		Class<?> listenerType = event.getListenerType();
 		Object listenerProxy = Proxy.newProxyInstance(viewObject.getClass().getClassLoader(),
 				new Class[] { listenerType }, handler);
 
 		Method addMethod = Reflector.findMethods(Visibility.PUBLIC, Subclasses.INCLUDE)
 				.nameStartsWith("add").parameters(listenerType).exactOne(widget.getClass());
 		try {
 			addMethod.invoke(widget, listenerProxy);
 		} catch (Exception e) {
 			new ReflectorException(e);
 		}
 	}
 }
