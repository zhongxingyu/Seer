 /*
  $Id$
 
  Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 
  Redistribution and use of this software and associated documentation
  ("Software"), with or without modification, are permitted provided
  that the following conditions are met:
 
  1. Redistributions of source code must retain copyright
     statements and notices.  Redistributions must also contain a
     copy of this document.
 
  2. Redistributions in binary form must reproduce the
     above copyright notice, this list of conditions and the
     following disclaimer in the documentation and/or other
     materials provided with the distribution.
 
  3. The name "groovy" must not be used to endorse or promote
     products derived from this Software without prior written
     permission of The Codehaus.  For written permission,
     please contact info@codehaus.org.
 
  4. Products derived from this Software may not be called "groovy"
     nor may "groovy" appear in their names without prior written
     permission of The Codehaus. "groovy" is a registered
     trademark of The Codehaus.
 
  5. Due credit should be given to The Codehaus -
     http://groovy.codehaus.org/
 
  THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
  ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
  NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
  THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  OF THE POSSIBILITY OF SUCH DAMAGE.
 
  */
 package groovy.lang;
 
 import java.beans.BeanInfo;
 import java.beans.EventSetDescriptor;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.ast.CompileUnit;
 import org.codehaus.groovy.classgen.CompilerFacade;
 import org.codehaus.groovy.runtime.InvokerException;
 import org.codehaus.groovy.runtime.InvokerHelper;
 import org.codehaus.groovy.runtime.InvokerInvocationException;
 import org.codehaus.groovy.runtime.MethodClosure;
 import org.codehaus.groovy.runtime.MethodHelper;
 import org.objectweb.asm.ClassWriter;
 
 /**
  * Allows methods to be dynamically added to existing classes at runtime
  * 
  * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
  * @version $Revision$
  */
 public class MetaClass {
 
     protected static final Object[] EMPTY_ARRAY = {
     };
     protected static final Object[] ARRAY_WITH_NULL = { null };
 
     private MetaClassRegistry registry;
     private Class theClass;
     private ClassNode classNode;
     private Map methodIndex = new HashMap();
     private Map staticMethodIndex = new HashMap();
     private Map newStaticInstanceMethodIndex = new HashMap();
     private Map propertyDescriptors = Collections.synchronizedMap(new HashMap());
     private Map listeners = new HashMap();
     private Method genericGetMethod;
     private Method genericSetMethod;
     private List constructors;
 
     public MetaClass(MetaClassRegistry registry, Class theClass) throws IntrospectionException {
         this.registry = registry;
         this.theClass = theClass;
 
         constructors = Arrays.asList(theClass.getDeclaredConstructors());
         addMethods(theClass);
 
         // introspect
         BeanInfo info = Introspector.getBeanInfo(theClass);
         PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
         for (int i = 0; i < descriptors.length; i++) {
             PropertyDescriptor descriptor = descriptors[i];
             propertyDescriptors.put(descriptor.getName(), descriptor);
         }
         EventSetDescriptor[] eventDescriptors = info.getEventSetDescriptors();
         for (int i = 0; i < eventDescriptors.length; i++) {
             EventSetDescriptor descriptor = eventDescriptors[i];
             Method[] listenerMethods = descriptor.getListenerMethods();
             for (int j = 0; j < listenerMethods.length; j++) {
                 Method listenerMethod = listenerMethods[j];
                 listeners.put(listenerMethod.getName(), descriptor.getAddListenerMethod());
             }
         }
 
         // now lets see if there are any methods on one of my interfaces
         Class[] interfaces = theClass.getInterfaces();
         for (int i = 0; i < interfaces.length; i++) {
             addNewStaticMethodsFrom(interfaces[i]);
         }
         // lets add all the base class methods
         Class c = theClass;
         while (true) {
             c = c.getSuperclass();
             if (c == null) {
                 break;
             }
             addNewStaticMethodsFrom(c);
             addMethods(c);
         }
     }
 
     /**
      * @return all the normal instance methods avaiable on this class for the given name
      */
     public List getMethods(String name) {
         List answer = (List) methodIndex.get(name);
         if (answer == null) {
             return Collections.EMPTY_LIST;
         }
         return answer;
     }
 
     /**
      * @return all the normal static methods avaiable on this class for the given name
      */
     public List getStaticMethods(String name) {
         List answer = (List) staticMethodIndex.get(name);
         if (answer == null) {
             return Collections.EMPTY_LIST;
         }
         return answer;
     }
 
     /**
      * Allows static method definitions to be added to a meta class as if it was an instance 
      * method
      * 
      * @param method
      */
     public void addNewStaticInstanceMethod(Method method) {
         String name = method.getName();
         List list = (List) newStaticInstanceMethodIndex.get(name);
         if (list == null) {
             list = new ArrayList();
             newStaticInstanceMethodIndex.put(name, list);
         }
         list.add(method);
     }
 
     public Object invokeMethod(Object object, String methodName, Object arguments) {
         return invokeMethod(object, methodName, arguments, InvokerHelper.asList(arguments));
     }
 
     /**
      * Invokes the given method on the object. 
      * 
      * @param object
      * @param methodName
      * @param arguments
      * @return
      */
     public Object invokeMethod(Object object, String methodName, Object arguments, List argumentList) {
         if (object == null) {
             throw new InvokerException("Cannot invoke method: " + methodName + " on null object");
         }
 
         Class theClass = object.getClass();
         List methods = getMethods(methodName);
         if (!methods.isEmpty()) {
             Method method = (Method) chooseMethod(methods, arguments, argumentList);
             if (method != null) {
                 return doMethodInvoke(object, method, arguments, argumentList);
             }
         }
 
         // lets see if there's a new static method we've added in groovy-land to this class
         List newStaticInstanceMethods = getNewStaticInstanceMethods(methodName);
         List staticArgumentList = new ArrayList(argumentList.size() + 1);
         staticArgumentList.add(object);
         staticArgumentList.addAll(argumentList);
         Method method = (Method) chooseMethod(newStaticInstanceMethods, staticArgumentList, staticArgumentList);
         if (method == null) {
             method = findNewStaticInstanceMethod(methodName, staticArgumentList);
         }
         if (method != null) {
             return doMethodInvoke(null, method, staticArgumentList.toArray());
         }
 
         // lets try a static method then
         return invokeStaticMethod(object, methodName, arguments, argumentList);
     }
 
     public Object invokeStaticMethod(Object object, String methodName, Object arguments, List argumentList) {
         List methods = getStaticMethods(methodName);
 
         if (!methods.isEmpty()) {
             Method method = (Method) chooseMethod(methods, arguments, argumentList);
             if (method != null) {
                 return doMethodInvoke(theClass, method, argumentList.toArray());
             }
         }
 
         if (theClass != Class.class) {
             try {
                 return registry.getMetaClass(Class.class).invokeMethod(object, methodName, arguments, argumentList);
             }
             catch (InvokerException e) {
                 // ignore
             }
         }
         throw new InvokerException(
             "Could not find matching method called: " + methodName + " for class: " + theClass.getName());
     }
 
     public Object invokeConstructor(Object arguments, List argumentList) {
         Constructor constructor = (Constructor) chooseMethod(constructors, arguments, argumentList);
         if (constructor != null) {
             return doConstructorInvoke(constructor, argumentList.toArray());
         }
         throw new InvokerException("Could not find matching constructor for class: " + theClass.getName());
     }
 
     /**
      * @return the currently registered static methods against this class
      */
     public List getNewStaticInstanceMethods(String methodName) {
         List newStaticInstanceMethods = (List) newStaticInstanceMethodIndex.get(methodName);
         if (newStaticInstanceMethods == null) {
             return Collections.EMPTY_LIST;
         }
         return newStaticInstanceMethods;
     }
 
     /**
      * @return the given property's value on the object
      */
     public Object getProperty(final Object object, final String property) {
         PropertyDescriptor descriptor = (PropertyDescriptor) propertyDescriptors.get(property);
         if (descriptor != null) {
             Method method = descriptor.getReadMethod();
             if (method == null) {
                 throw new InvokerException("Cannot read property: " + property);
             }
             return doMethodInvoke(object, method, EMPTY_ARRAY);
         }
 
         if (genericGetMethod != null) {
             Object[] arguments = { property };
             Object answer = doMethodInvoke(object, genericGetMethod, arguments);
             if (answer != null) {
                 return answer;
             }
         }
 
         // is the property the name of a method - in which case return a 
         // closure
         List methods = getMethods(property);
         if (!methods.isEmpty()) {
             return new MethodClosure(object, property);
         }
 
         /** @todo or are we an extensible groovy class? */
         if (genericGetMethod != null) {
             return null;
         }
         else {
             if (object instanceof Class) {
                 // lets try a static field
                 return getStaticProperty((Class) object, property);
             }
            throw new InvokerException("Unknown property: " + property);
         }
     }
 
     /**
      * Sets the property value on an object
      */
     public void setProperty(Object object, String property, Object newValue) {
         PropertyDescriptor descriptor = (PropertyDescriptor) propertyDescriptors.get(property);
 
         if (descriptor != null) {
             Method method = descriptor.getWriteMethod();
             if (method == null) {
                 throw new InvokerException("Cannot set read-only property: " + property);
             }
             Object[] arguments = { newValue };
             try {
                 doMethodInvoke(object, method, arguments);
             }
             catch (InvokerException e) {
                 // if the value is a List see if we can construct the value 
                 // from a constructor
                 if (newValue instanceof List) {
                     List list = (List) newValue;
                     int params = list.size();
                     Constructor[] constructors = descriptor.getPropertyType().getConstructors();
                     for (int i = 0; i < constructors.length; i++) {
                         Constructor constructor = constructors[i];
                         if (constructor.getParameterTypes().length == params) {
                             //System.out.println("Found constructor: " + constructor);
                             Object value = doConstructorInvoke(constructor, list.toArray());
                             doMethodInvoke(object, method, new Object[] { value });
                             return;
                         }
                     }
                 }
                 throw e;
             }
             return;
         }
 
         Method addListenerMethod = (Method) listeners.get(property);
         if (addListenerMethod != null && newValue instanceof Closure) {
             // lets create a dynamic proxy
             Object proxy = createListenerProxy(addListenerMethod.getParameterTypes()[0], property, (Closure) newValue);
             doMethodInvoke(object, addListenerMethod, new Object[] { proxy });
             return;    
         } 
         
         if (genericSetMethod != null) {
             Object[] arguments = { property, newValue };
             doMethodInvoke(object, genericSetMethod, arguments);
             return;
         }
 
         /** @todo or are we an extensible class? */
 
         // lets try invoke the set method
         String method = "set" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
         invokeMethod(object, method, newValue);
     }
 
     public ClassNode getClassNode() {
         if (classNode == null && GroovyObject.class.isAssignableFrom(theClass)) {
             // lets try load it from the classpath
             String className = theClass.getName();
             String groovyFile = className;
             int idx = groovyFile.indexOf('$');
             if (idx > 0) {
                 groovyFile = groovyFile.substring(0, idx);
             }
             groovyFile = groovyFile.replace('.', '/') + ".groovy";
 
             //System.out.println("Attempting to load: " + groovyFile);
             URL url = theClass.getClassLoader().getResource(groovyFile);
             if (url == null) {
                 url = Thread.currentThread().getContextClassLoader().getResource(groovyFile);
             }
             if (url != null) {
                 try {
                     InputStream in = url.openStream();
                     
                     /** @todo there is no CompileUnit in scope so class name 
                      * checking won't work but that mostly affects the bytecode generation
                      * rather than viewing the AST
                      */
                     CompilerFacade compiler = new CompilerFacade(theClass.getClassLoader(), new CompileUnit()) {
                         protected void onClass(ClassWriter classWriter, ClassNode classNode) {
                             if (classNode.getName().equals(theClass.getName())) {
                                 //System.out.println("Found: " + classNode.getName());
                                 MetaClass.this.classNode = classNode;
                             }
                         }
                     };
                     compiler.parseClass(in, groovyFile);
                 }
                 catch (Exception e) {
                     throw new InvokerException("Exception thrown parsing: " + groovyFile + ". Reason: " + e, e);
                 }
             }
 
         }
         return classNode;
     }
 
     public String toString() {
         return super.toString() + "[" + theClass + "]";
     }
 
     // Implementation methods
     //-------------------------------------------------------------------------
 
     /**
      * @param listenerType the interface of the listener to proxy
      * @param listenerMethodName the name of the method in the listener API to call the closure on
      * @param closure the closure to invoke on the listenerMethodName method invocation
      * @return a dynamic proxy which calls the given closure on the given method name
      */
     protected Object createListenerProxy(Class listenerType, final String listenerMethodName, final Closure closure) {
         InvocationHandler handler = new InvocationHandler() {
             public Object invoke(Object object, Method method, Object[] arguments) throws Throwable {
                 if (listenerMethodName.equals(method.getName())) {
                     /** @todo hack! */
                     closure.call(arguments[0]);
                 }
                 return null;
             }};
         return Proxy.newProxyInstance(listenerType.getClassLoader(), new Class[] { listenerType }, handler);
     }
 
     /**
      * Adds all the methods declared in the given class to the metaclass
      * ignoring any matching methods already defined by a derived class
      * 
      * @param theClass
      */
     protected void addMethods(Class theClass) {
         Method[] methodArray = theClass.getDeclaredMethods();
         for (int i = 0; i < methodArray.length; i++) {
             Method method = methodArray[i];
 
             String name = method.getName();
             if (isGenericGetMethod(method) && genericGetMethod == null) {
                 genericGetMethod = method;
             }
             else if (isGenericSetMethod(method) && genericSetMethod == null) {
                 genericSetMethod = method;
             }
             if (MethodHelper.isStatic(method)) {
                 List list = (List) staticMethodIndex.get(name);
                 if (list == null) {
                     list = new ArrayList();
                     staticMethodIndex.put(name, list);
                     list.add(method);
                 }
                 else {
                     if (!containsMatchingMethod(list, method)) {
                         list.add(method);
                     }
                 }
             }
             else {
                 List list = (List) methodIndex.get(name);
                 if (list == null) {
                     list = new ArrayList();
                     methodIndex.put(name, list);
                     list.add(method);
                 }
                 else {
                     if (!containsMatchingMethod(list, method)) {
                         list.add(method);
                     }
                 }
             }
         }
     }
 
     /**
      * @return true if a method of the same matching prototype was found in the list
      */
     protected boolean containsMatchingMethod(List list, Method method) {
         for (Iterator iter = list.iterator(); iter.hasNext();) {
             Method aMethod = (Method) iter.next();
             Class[] params1 = aMethod.getParameterTypes();
             Class[] params2 = method.getParameterTypes();
             if (params1.length == params2.length) {
                 boolean matches = true;
                 for (int i = 0; i < params1.length; i++) {
                     if (params1[i] != params2[i]) {
                         matches = false;
                         break;
                     }
                 }
                 if (matches) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Adds all of the newly defined methods from the given class to this
      * metaclass
      * 
      * @param theClass
      */
     protected void addNewStaticMethodsFrom(Class theClass) {
         MetaClass interfaceMetaClass = registry.getMetaClass(theClass);
         Iterator iter = interfaceMetaClass.newStaticInstanceMethodIndex.entrySet().iterator();
         while (iter.hasNext()) {
             Map.Entry entry = (Map.Entry) iter.next();
             String name = (String) entry.getKey();
             List values = (List) entry.getValue();
 
             if (values != null) {
                 // lets add these methods to me
                 List list = (List) newStaticInstanceMethodIndex.get(name);
                 if (list == null) {
                     list = new ArrayList();
                     newStaticInstanceMethodIndex.put(name, list);
                 }
                 list.addAll(values);
             }
         }
     }
 
     /**
      * @return the value of the static property of the given class
      */
     protected Object getStaticProperty(Class aClass, String property) {
         try {
             Field field = aClass.getField(property);
             if (field != null) {
                 return field.get(null);
             }
         }
         catch (Exception e) {
             throw new InvokerException("Could not evaluate property: " + property, e);
         }
         throw new InvokerException("No such property: " + property);
     }
 
     /**
      * Lets walk the base class & interfaces list to see if we can find the method
      */
     protected Method findNewStaticInstanceMethod(String methodName, List staticArgumentList) {
         if (theClass.equals(Object.class)) {
             return null;
         }
         MetaClass superClass = registry.getMetaClass(theClass.getSuperclass());
         List list = superClass.getNewStaticInstanceMethods(methodName);
         if (!list.isEmpty()) {
             Method method = (Method) chooseMethod(list, staticArgumentList, staticArgumentList);
             if (method != null) {
                 // lets cache it for next invocation
                 addNewStaticInstanceMethod(method);
             }
 
             return method;
         }
         return superClass.findNewStaticInstanceMethod(methodName, staticArgumentList);
     }
 
     protected Object doMethodInvoke(Object object, Method method, Object arguments, List argumentList) {
         Object[] argumentArray = EMPTY_ARRAY;
         int length = method.getParameterTypes().length;
         // null will generate a zero sized list
         if (length > 0) {
             if (length == 1) {
                 if (argumentList.isEmpty()) {
                     argumentArray = ARRAY_WITH_NULL;
                 }
                 else if (argumentList.size() > 0) {
                     argumentArray = new Object[] { arguments };
                 }
             }
             else {
                 argumentArray = argumentList.toArray();
             }
         }
 
         return doMethodInvoke(object, method, argumentArray);
     }
 
     protected Object doMethodInvoke(Object object, Method method, Object[] argumentArray) {
         //System.out.println("Evaluating method: " + method);
         //System.out.println("on object: " + object + " with arguments: " + InvokerHelper.toString(argumentArray));
         //System.out.println(this.theClass);
 
         try {
             if (registry.useAccessible()) {
                 method.setAccessible(true);
             }
             return method.invoke(object, argumentArray);
         }
         catch (InvocationTargetException e) {
             Throwable t = e.getTargetException();
             if (t instanceof Error) {
                 Error error = (Error) t;
                 throw error;
             }
             if (t instanceof RuntimeException) {
                 RuntimeException runtimeEx = (RuntimeException) t;
                 throw runtimeEx;
             }
             throw new InvokerInvocationException(e);
         }
         catch (IllegalAccessException e) {
             throw new InvokerException(
                 "could not access method: "
                     + method
                     + " on: "
                     + object
                     + " with arguments: "
                     + InvokerHelper.toString(argumentArray)
                     + " reason: "
                     + e,
                 e);
         }
         catch (Exception e) {
             throw new InvokerException(
                 "failed to invoke method: "
                     + method
                     + " on: "
                     + object
                     + " with arguments: "
                     + InvokerHelper.toString(argumentArray)
                     + " reason: "
                     + e,
                 e);
         }
     }
 
     protected Object doConstructorInvoke(Constructor constructor, Object[] argumentArray) {
         //System.out.println("Evaluating constructor: " + constructor + " with arguments: " + InvokerHelper.toString(argumentArray));
         //System.out.println(this.theClass);
 
         try {
             return constructor.newInstance(argumentArray);
         }
         catch (InvocationTargetException e) {
             Throwable t = e.getTargetException();
             if (t instanceof Error) {
                 Error error = (Error) t;
                 throw error;
             }
             if (t instanceof RuntimeException) {
                 RuntimeException runtimeEx = (RuntimeException) t;
                 throw runtimeEx;
             }
             throw new InvokerInvocationException(e);
         }
         catch (IllegalAccessException e) {
             throw new InvokerException(
                 "could not access constructor: "
                     + constructor
                     + " with arguments: "
                     + InvokerHelper.toString(argumentArray)
                     + " reason: "
                     + e,
                 e);
         }
         catch (Exception e) {
             throw new InvokerException(
                 "failed to invoke constructor: "
                     + constructor
                     + " with arguments: "
                     + InvokerHelper.toString(argumentArray)
                     + " reason: "
                     + e,
                 e);
         }
     }
 
     /**
      * Chooses the correct method to use from a list of methods which match by name.
      * 
      * @param methods the possible methods to choose from
      * @param arguments the original argument to the method
      * @param argumentList the argument to the method morphed into a List (maybe of 1 item)
      * @return
      */
     protected Object chooseMethod(List methods, Object arguments, List argumentList) {
         int methodCount = methods.size();
         if (methodCount <= 0) {
             return null;
         }
         else if (methodCount == 1) {
             return methods.get(0);
         }
         Object answer = null;
         if (arguments == null) {
             answer = chooseMostGeneralMethodWith1Param(methods);
         }
         else if (argumentList.isEmpty()) {
             answer = chooseEmptyMethodParams(methods);
         }
         else {
 
             // lets look for methods with 1 argument which matches the type of the arguments
             Class closestClass = null;
             Object singleParameterMatch = null;
             boolean wasMethodWithMultiArgs = false;
 
             for (Iterator iter = methods.iterator(); iter.hasNext();) {
                 Object method = iter.next();
                 Class[] paramTypes = getParameterTypes(method);
                 if (paramTypes.length == 1) {
                     Class theType = paramTypes[0];
                     if (isCompatibleInstance(theType, arguments)) {
                         if (closestClass == null || !theType.isAssignableFrom(closestClass)) {
                             closestClass = theType;
                             singleParameterMatch = method;
                         }
                     }
                     else {
                         wasMethodWithMultiArgs = true;
                     }
                 }
             }
 
             // lets compare number of arguments
             Object multiParamMatch = null;
             int size = argumentList.size();
             int matches = 0;
             if (size > 1) {
                 for (Iterator iter = methods.iterator(); iter.hasNext();) {
                     Object method = iter.next();
                     Class[] paramTypes = getParameterTypes(method);
                     if (paramTypes.length == size) {
                         // lets check the parameter types match
                         boolean validMethod = true;
                         for (int i = 0; i < size; i++) {
                             Object value = argumentList.get(i);
                             if (!isCompatibleInstance(paramTypes[i], value)) {
                                 validMethod = false;
                             }
                         }
                         if (validMethod && multiParamMatch == null) {
                             multiParamMatch = method;
                             matches++;
                         }
                     }
                 }
             }
             if (singleParameterMatch != null) {
                 if (multiParamMatch == null) {
                     return singleParameterMatch;
                 }
 
                 // how do we choose between them...
                 int answerSize = getParameterTypes(singleParameterMatch).length;
                 int matchingArgSize = getParameterTypes(multiParamMatch).length;
 
                 if (answerSize == matchingArgSize) {
                     return singleParameterMatch;
                 }
                 else {
                     return multiParamMatch;
                 }
             }
             else if (multiParamMatch != null) {
                 return multiParamMatch;
             }
         }
         if (answer != null) {
             return answer;
         }
         throw new InvokerException(
             "Could not find which method to invoke from this list: " + methods + " for arguments: " + arguments);
     }
 
     protected Class[] getParameterTypes(Object methodOrConstructor) {
         if (methodOrConstructor instanceof Method) {
             Method method = (Method) methodOrConstructor;
             return method.getParameterTypes();
         }
         if (methodOrConstructor instanceof Constructor) {
             Constructor constructor = (Constructor) methodOrConstructor;
             return constructor.getParameterTypes();
         }
         throw new IllegalArgumentException("Must be a Method or Constructor");
     }
 
     /**
      * @return the method with 1 parameter which takes the most general type of object (e.g. Object)
      */
     protected Object chooseMostGeneralMethodWith1Param(List methods) {
         // lets look for methods with 1 argument which matches the type of the arguments
         Class closestClass = null;
         Object answer = null;
 
         for (Iterator iter = methods.iterator(); iter.hasNext();) {
             Object method = iter.next();
             Class[] paramTypes = getParameterTypes(method);
             int paramLength = paramTypes.length;
             if (paramLength == 1) {
                 Class theType = paramTypes[0];
                 if (closestClass == null || theType.isAssignableFrom(closestClass)) {
                     closestClass = theType;
                     answer = method;
                 }
             }
         }
         return answer;
     }
 
     /**
      * @return the method with 1 parameter which takes the most general type of object (e.g. Object)
      */
     protected Object chooseEmptyMethodParams(List methods) {
         for (Iterator iter = methods.iterator(); iter.hasNext();) {
             Object method = iter.next();
             Class[] paramTypes = getParameterTypes(method);
             int paramLength = paramTypes.length;
             if (paramLength == 0) {
                 return method;
             }
         }
         return null;
     }
 
     protected boolean isCompatibleInstance(Class type, Object value) {
         boolean answer = value == null || type.isInstance(value);
         if (!answer) {
             if (type.isPrimitive() && value instanceof Number) {
                 if (type == int.class) {
                     return value instanceof Integer;
                 }
                 else if (type == double.class) {
                     return value instanceof Double;
                 }
                 else if (type == long.class) {
                     return value instanceof Long;
                 }
                 else if (type == float.class) {
                     return value instanceof Float;
                 }
                 else if (type == char.class) {
                     return value instanceof Character;
                 }
                 else if (type == byte.class) {
                     return value instanceof Byte;
                 }
                 else if (type == short.class) {
                     return value instanceof Short;
                 }
             }
         }
         return answer;
     }
 
     protected boolean isGenericSetMethod(Method method) {
         return method.getName().equals("set") && method.getParameterTypes().length == 2;
     }
 
     protected boolean isGenericGetMethod(Method method) {
         return method.getName().equals("get") && method.getParameterTypes().length == 1;
     }
 
 }
