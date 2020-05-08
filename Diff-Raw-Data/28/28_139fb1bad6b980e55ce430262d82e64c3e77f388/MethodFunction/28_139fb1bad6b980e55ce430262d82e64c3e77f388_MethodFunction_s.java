 /*
  * This file is part of experimaestro.
  * Copyright (c) 2013 B. Piwowarski <benjamin@bpiwowar.net>
  *
  * experimaestro is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * experimaestro is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package sf.net.experimaestro.manager.js;
 
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.NotImplementedException;
 import org.mozilla.javascript.Callable;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ScriptRuntime;
 import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
 import sf.net.experimaestro.utils.JSUtils;
 import sf.net.experimaestro.utils.Output;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 
 /**
  * Wraps a method of an object
  */
 class MethodFunction implements Callable, Function {
     String name;
     ArrayList<Method> methods = new ArrayList<>();
 
     public MethodFunction(String name) {
         this.name = name;
     }
 
     @Override
     public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
         int max = Integer.MIN_VALUE;
         Method argmax = null;
         for (Method method : methods) {
             int score = score(method, args);
             if (score > max) {
                 max = score;
                 argmax = method;
             }
         }
 
         if (argmax == null)
             throw ScriptRuntime.typeError(String.format("Could not find a matching method for %s(%s)", name,
                     Output.toString(", ", args, new Output.Formatter<Object>() {
                         @Override
                         public String format(Object o) {
                             return o.getClass().toString();
                         }
                     })));
 
         // Call the method
 
         try {
             boolean isStatic = (argmax.getModifiers() & Modifier.STATIC) != 0;
             args = transform(cx, scope, argmax, args);
             final Object invoke = argmax.invoke(isStatic ? null : thisObj, args);
             return cx.getWrapFactory().wrap(cx, scope, invoke, null);
         } catch (Throwable e) {
            if (e.getCause() != null)
                e = e.getCause();
            throw new WrappedException(e);
         }
 
     }
 
     @Override
     public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
         // TODO: implement construct
         throw new NotImplementedException();
     }
 
     private Object[] transform(Context cx, Scriptable scope, Method method, Object[] args) {
         final Class<?>[] types = method.getParameterTypes();
         Object methodArgs[] = new Object[types.length];
 
         // --- Add context and scope if needed
         final boolean useScope = method.getAnnotation(JSFunction.class).scope();
         int offset = useScope ? 2 : 0;
         if (useScope) {
             methodArgs[0] = cx;
             methodArgs[1] = scope;
         }
 
         // --- Copy the non vararg parameters
         final int length = types.length - (method.isVarArgs() ? 1 : 0) - offset;
         for (int i = 0; i < length; i++) {
             methodArgs[i + offset] = JSUtils.unwrap(args[i]);
         }
 
         // --- Deals with the vararg pararameters
         if (method.isVarArgs()) {
             final Class<?> varargType = types[length].getComponentType();
             int nbVarargs = args.length - length;
             final Object array[] = (Object[]) Array.newInstance(varargType, nbVarargs);
             for (int i = 0; i < nbVarargs; i++) {
                 array[i] = JSUtils.unwrap(args[i + length]);
             }
             methodArgs[methodArgs.length - 1] = array;
         }
 
         return methodArgs;
     }
 
     private int score(Method method, Object[] args) {
         final boolean scope = method.getAnnotation(JSFunction.class).scope();
         int offset = scope ? 2 : 0;
 
         final Class<?>[] types = method.getParameterTypes();
 
         final int nbArgs = types.length - offset - (method.isVarArgs() ? 1 : 0);
 
         // If the methods is varargs, then we need at least nbArgs - 1 parameters
         if (method.isVarArgs()) {
             if (args.length < nbArgs)
                 return 0;
         } else if (args.length != nbArgs)
             return 0;
 
         int score = Integer.MAX_VALUE;
 
         // Normal arguments
         for (int i = 0; i < nbArgs && score > 0; i++) {
             final Object o = JSUtils.unwrap(args[i]);
             if (o == null) {
                 score--;
                 continue;
             }
 
             Class<?> type = ClassUtils.primitiveToWrapper(types[i + offset]);
             if (type.isAssignableFrom(o.getClass()))
                 continue;
             return Integer.MIN_VALUE;
         }
 
         // Var args
         if (method.isVarArgs()) {
             Class<?> type = ClassUtils.primitiveToWrapper(types[types.length - 1].getComponentType());
             int nbVarArgs = args.length - nbArgs;
             for (int i = 0; i < nbVarArgs && score > 0; i++) {
                 final Object o = JSUtils.unwrap(args[nbArgs + i]);
                 if (o == null || type.isAssignableFrom(o.getClass()))
                     continue;
                 return Integer.MIN_VALUE;
             }
         }
 
 
         return score;
     }
 
     @Override
     public String getClassName() {
         // TODO: implement getClassName
         throw new NotImplementedException();
     }
 
     @Override
     public Object get(String name, Scriptable start) {
         // TODO: implement get
         throw new NotImplementedException();
     }
 
     @Override
     public Object get(int index, Scriptable start) {
         // TODO: implement get
         throw new NotImplementedException();
     }
 
     @Override
     public boolean has(String name, Scriptable start) {
         // TODO: implement has
         throw new NotImplementedException();
     }
 
     @Override
     public boolean has(int index, Scriptable start) {
         // TODO: implement has
         throw new NotImplementedException();
     }
 
     @Override
     public void put(String name, Scriptable start, Object value) {
         // TODO: implement put
         throw new NotImplementedException();
     }
 
     @Override
     public void put(int index, Scriptable start, Object value) {
         // TODO: implement put
         throw new NotImplementedException();
     }
 
     @Override
     public void delete(String name) {
         // TODO: implement delete
         throw new NotImplementedException();
     }
 
     @Override
     public void delete(int index) {
         // TODO: implement delete
         throw new NotImplementedException();
     }
 
     @Override
     public Scriptable getPrototype() {
         // TODO: implement getPrototype
         throw new NotImplementedException();
     }
 
     @Override
     public void setPrototype(Scriptable prototype) {
         // TODO: implement setPrototype
         throw new NotImplementedException();
     }
 
     @Override
     public Scriptable getParentScope() {
         // TODO: implement getParentScope
         throw new NotImplementedException();
     }
 
     @Override
     public void setParentScope(Scriptable parent) {
         // TODO: implement setParentScope
         throw new NotImplementedException();
     }
 
     @Override
     public Object[] getIds() {
         // TODO: implement getIds
         throw new NotImplementedException();
     }
 
     @Override
     public Object getDefaultValue(Class<?> hint) {
         // TODO: implement getDefaultValue
         throw new NotImplementedException();
     }
 
     @Override
     public boolean hasInstance(Scriptable instance) {
         // TODO: implement hasInstance
         throw new NotImplementedException();
     }
 }
