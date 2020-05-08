 package com.example.e4.rcp.todo.facade.internal;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IContextFunction;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 
 import com.example.e4.rcp.todo.facade.ModelFacade;
 
 public class TodoServiceContextFunction implements IContextFunction {
 
 	@Override
 	public Object compute(IEclipseContext context) {
 		ModelFacade service = ContextInjectionFactory.make(ModelFacade.class,
 				context);
 		return service;
 	}
 }
