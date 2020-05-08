 /*
  * Shredzone Commons
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://commons.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Library General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.shredzone.commons.view.manager;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import javax.servlet.ServletRequest;
 import javax.swing.text.View;
 
 import org.shredzone.commons.view.ViewContext;
 import org.shredzone.commons.view.annotation.Attribute;
 import org.shredzone.commons.view.annotation.Optional;
 import org.shredzone.commons.view.annotation.Parameter;
 import org.shredzone.commons.view.annotation.PathPart;
 import org.shredzone.commons.view.annotation.ViewHandler;
 import org.shredzone.commons.view.exception.PageNotFoundException;
 import org.shredzone.commons.view.exception.ViewContextException;
 import org.shredzone.commons.view.exception.ViewException;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.core.convert.TypeDescriptor;
 import org.springframework.util.ReflectionUtils;
 
 /**
  * Keeps a reference to and invokes a view handler.
  * <p>
  * {@link ViewInvoker ViewInvokers} are immutable.
  *
  * @author Richard "Shred" Körber
  */
 public class ViewInvoker {
     private final Object bean;
     private final Method method;
     private final ConversionService conversionService;
     private final Annotation[] viewAnnotations;
     private final boolean[] optionals;
 
     /**
      * Creates a new {@link ViewInvoker}.
      *
      * @param bean
      *            target Spring bean to be invoked
      * @param method
      *            target method to be invoked
      * @param conversionService
      *            {@link ConversionService} to be used for parameter conversion
      */
     public ViewInvoker(Object bean, Method method, ConversionService conversionService) {
         this.bean = bean;
         this.method = method;
         this.conversionService = conversionService;
 
         Annotation[][] annotations = method.getParameterAnnotations();
         viewAnnotations = new Annotation[annotations.length];
         optionals = new boolean[annotations.length];
 
         for (int ix = 0; ix < annotations.length; ix++) {
             for (Annotation sub : annotations[ix]) {
                 if (sub instanceof Optional) {
                     optionals[ix] = true;
                 } else if (
                            sub instanceof PathPart
                         || sub instanceof Parameter
                         || sub instanceof Attribute) {
                     if (viewAnnotations[ix] != null) {
                         throw new IllegalArgumentException("Conflicting annotations "
                                 + sub + " and " + viewAnnotations[ix] + " in view handler "
                                 + bean.getClass().getName() + "#" + method.getName() + "()");
                     }
                     viewAnnotations[ix] = sub;
                 }
             }
         }
     }
 
     /**
      * The Spring bean that was annotated with {@link ViewHandler}.
      */
     public Object getBean() { return bean; }
 
     /**
      * The target view handler method that was annotated with {@link View}.
      */
     public Method getMethod() { return method; }
 
     /**
      * Invokes the view handler.
      *
      * @param context
      *            {@link ViewContext} containing all necessary data for invoking the view
      * @return String returned by the view handler. Usually this is a reference to a JSP
      *         that is used for rendering the result. If {@code null}, the view handler
      *         took care for sending a response itself.
      */
     public String invoke(ViewContext context) throws ViewException {
         Class<?>[] types = method.getParameterTypes();
         Object[] values = new Object[types.length];
 
         for (int ix = 0; ix < types.length; ix++) {
             Object result = evaluateParameter(types[ix], viewAnnotations[ix], optionals[ix], context);
             if (result == null && !optionals[ix]) {
                 throw new PageNotFoundException("Argument " + ix + " is required but missing.");
             }
             values[ix] = result;
         }
 
         try {
             Object renderViewName = ReflectionUtils.invokeMethod(method, bean, values);
             return (renderViewName != null ? renderViewName.toString() : null);
        } catch (IllegalStateException ex) {
             Throwable cause = ex.getCause();
             if (cause instanceof ViewException) {
                 throw (ViewException) cause;
             } else {
                 throw ex;
             }
         }
     }
 
     /**
      * Evaluates a single parameter of the handler method's parameter list.
      *
      * @param type
      *            Expected parameter type
      * @param anno
      *            {@link Annotation} of this parameter
      * @param optional
      *            if this parameter is optional and may be {@code null}
      * @param context
      *            {@link ViewContext} containing all necessary data for invoking the view
      * @return Parameter value to be passed to the method
      */
     private Object evaluateParameter(Class<?> type, Annotation anno, boolean optional, ViewContext context)
     throws ViewException {
 
         if (anno instanceof Parameter) {
             String name = ((Parameter) anno).value();
             String value = context.getParameter(name);
             if (value == null && !optional) {
                 throw new ViewContextException("Missing parameter " + name);
             }
             return conversionService.convert(value, type);
         }
 
         if (anno instanceof PathPart) {
             String part = ((PathPart) anno).value();
             String value = context.getPathParts().get(part);
             if (value != null) {
                 return conversionService.convert(value, type);
             } else if (optional) {
                 return conversionService.convert(null,
                         TypeDescriptor.valueOf(String.class),
                         TypeDescriptor.valueOf(type));
             } else {
                 throw new ViewException("Unsatisfied path part: " + part);
             }
         }
 
         if (anno instanceof Attribute) {
             String name = ((Attribute) anno).value();
             ServletRequest req = context.getValueOfType(ServletRequest.class);
             Object value = req.getAttribute(name);
             if (value == null && !optional) {
                 throw new ViewContextException("Missing attribute " + name);
             }
             return conversionService.convert(value, type);
         }
 
         // Finally, try to get an object of that type from the data provider
         try {
             return context.getValueOfType(type);
         } catch (ViewContextException ex) {
             // ignore and continue...
         }
 
         // Who the heck would need this...
         if (ViewContext.class.isAssignableFrom(type)) {
             return context;
         }
 
         // Alas, we cannot find anything to satisfy this parameter
         throw new ViewContextException("Unknown parameter type " + type.getName());
     }
 
 }
