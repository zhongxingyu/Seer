 package org.sylvanbytes.test.unit;
 
 import org.mockito.exceptions.base.MockitoException;
 import org.mockito.internal.util.MockUtil;
 import org.sylvanbytes.test.exception.NullArgumentCheckMissingException;
 import org.sylvanbytes.test.exception.UnsupportedMethodTypeRuntimeException;
 import org.sylvanbytes.util.arg.exception.FailedNullCheckException;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.*;
 
 import static org.mockito.Mockito.mock;
 import static org.sylvanbytes.util.arg.precondition.NullCheck.checkNotNull;
 
 /**
  * Ensure that all the methods of a given class <code>S</code> check their arguments
  * against nullity.
  */
 public class NonNullMethodParameterVerifier<S> {
 
     S subject;
     Set<Method> excludeMethods;
 
     public NonNullMethodParameterVerifier(S subject) {
         checkNotNull(subject);
         MockUtil mockUtil = new MockUtil();
         if(mockUtil.isMock(subject)){
              throw new IllegalArgumentException("Cannot check nulls on a Mockito mocked object.");
         }
         this.subject = subject;
 
         excludeMethods = new HashSet<>();
 
     }
 
    public void excludeMethod(Method... method){
         excludeMethods.addAll(Arrays.asList(method));
     }
 
    public void excludeMethod(String... methodNames){
         Set<Method> foundMethods = new HashSet<>();
         Set<String> foundMethodNames = new HashSet<>();
         Set<String> methodNameSet = new HashSet<>();
         methodNameSet.addAll(Arrays.asList(methodNames));
         Method[] methodList = subject.getClass().getMethods();
         for(Method method : methodList){
             if(methodNameSet.contains(method.getName())){
                 if(foundMethodNames.contains(method.getName())){
                     throw new IllegalStateException("Cannot find the correct method to exclude because the method "+method.getName()+" is overloaded. Cannot refer to method only by name when said method is overloaded.");
                 } else {
                     foundMethodNames.add(method.getName());
                     foundMethods.add(method);
                 }
             }
         }
        this.excludeMethod(foundMethods.toArray(new Method[foundMethods.size()]));
     }
 
     /**
      * Examine the subject for whether null parameters are allowed. Examines all methods and constructors declared
      * in the class and reports and methods and argument combinations that did not result in a FailedNullCheckException.
      */
     public void execute() throws NullArgumentCheckMissingException{
 
         StringBuilder errorMessages = new StringBuilder();
 
 
         checkNullConstructorParameters(errorMessages);
 
         checkNullMethodsParameters(errorMessages);
 
         if (errorMessages.length() > 0) {
             throw new NullArgumentCheckMissingException("Found methods that accept null arguments:\n\t" + errorMessages.toString().replaceAll("\n", "\n\t"));
         }
     }
 
     private void checkNullMethodsParameters(StringBuilder errorMessages) {
         Method[] methods = subject.getClass().getDeclaredMethods();
 
         methods = filterMethods(methods);
 
         for (Method method : methods) {
             if(excludeMethods.contains(method)){
                 continue;
             }
 
             Class<?>[] parameterTypeArray = method.getParameterTypes();
 
             int length = parameterTypeArray.length;
             for (int i = 0; i < parameterTypeArray.length; i++) {
 
                 Class<?>[] types = new Class<?>[length];
                 System.arraycopy(parameterTypeArray, 0, types, 0, length);
 
                 if (!types[i].isPrimitive()) {
                     // specify one of the paramters to be null. Later we'll mockup the others
                     types[i] = null;
                 } else {
                     // the argument is a primitive. Because primitives cannot be set to null we skip
                     // the parameter.
                     continue;
                 }
 
                 Object[] arguments = new Object[types.length];
                 arguments = mockUpNonNulls(types, arguments);
 
                 try {
                     int modifiers = method.getModifiers();
 
                     // cannot access final methods from mocks
                     if(Modifier.isFinal(modifiers)){
                        throw new UnsupportedMethodTypeRuntimeException("Cannot test methods or constructors that are declared final.");
                     } else if(Modifier.isStatic(modifiers)){
                         method.invoke(null, arguments);
                     } else {
                         method.invoke(subject, arguments);
                     }
 
                     appendErrorMessage(errorMessages, method, types);
                 } catch (IllegalAccessException e) {
                     throw new RuntimeException(e);
                 } catch (InvocationTargetException e) {
                     if (e.getCause() instanceof FailedNullCheckException) {
                         //expected
                     } else {
                         appendErrorMessage(errorMessages, method, types);
                     }
                 }
             }
         }
     }
 
     /**
      * Remove method objects that should not be examined.
      */
     private Method[] filterMethods(Method[] methods) {
         assert(methods != null);
 
         List<Method> methodList = new ArrayList<>(methods.length);
         for(Method method : methods){
             if(subject.getClass().isEnum()){
                 if(method.getName().equals("valueOf")){
                     continue;
                 }
             }
 
             int modifiers = method.getModifiers();
 
             if(Modifier.isPrivate(modifiers) ||
                     method.getName().equalsIgnoreCase("equals") ||
                     method.getName().equals("hashcode")
                     ){
                 continue;
             }
 
             if(!Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)){
                 // is package local, we skip over it because we are only interested in public methods
                 continue;
             }
             methodList.add(method);
         }
         return methodList.toArray(new Method[methodList.size()]);
     }
 
     private void checkNullConstructorParameters(StringBuilder errorMessages) {
         for(Constructor constructor : subject.getClass().getConstructors()){
             Class<?>[] parameterTypeArray = constructor.getParameterTypes();
             int length = parameterTypeArray.length;
             for (int i = 0; i < parameterTypeArray.length; i++) {
 
                 Class<?>[] types = new Class<?>[length];
                 System.arraycopy(parameterTypeArray, 0, types, 0, length);
 
                 if(subject.getClass().getEnclosingClass() != null){
                     // subject is an inner class, first argument to constructor is the parent class. We skip over
                     // the first argument.
                     continue;
                 } else if (!types[i].isPrimitive()) {
                     // we specify one of the arguments to be null if is not a primitive
                     types[i] = null;
                 } else {
                     // argument is a primitive and so we cannot mock the primitives
                     continue;
                 }
 
 
                 Object[] arguments = new Object[types.length];
                 arguments = mockUpNonNulls(types, arguments);
 
                 try {
 
                    constructor.newInstance(arguments);
 
                     appendErrorMessage(errorMessages, constructor, types);
                 } catch (IllegalAccessException | InstantiationException e) {
                     throw new RuntimeException(e);
                 } catch (InvocationTargetException e) {
                     if (e.getCause() instanceof FailedNullCheckException) {
                         //expected
                     } else {
                         appendErrorMessage(errorMessages, constructor, types);
                     }
                 }
             }
         }
     }
 
     private void appendErrorMessage(StringBuilder errorMessages, Constructor constructor, Class<?>[] types) {
         String parameterTypeStr = buildParameterString(types);
         String msg = "Constructor  " + constructor.getClass().getCanonicalName() + "." + constructor.getName() + "(" + parameterTypeStr + ")";
         errorMessages.append(msg).append("\n");
     }
 
     private void appendErrorMessage(StringBuilder errorMessages, Method method, Class<?>[] types) {
         String parameterTypeStr = buildParameterString(types);
         String msg = subject.getClass().getCanonicalName() + "." + method.getName() + "(" + parameterTypeStr + ")";
         errorMessages.append(msg).append("\n");
     }
 
     private String buildParameterString(Class<?>[] types) {
         StringBuilder builder = new StringBuilder();
         for (Class<?> type : types) {
             if (type == null) {
                 builder.append("null").append(", ");
             } else {
                 builder.append(type.getName()).append(", ");
             }
         }
         String parameterTypeStr = builder.toString();
         parameterTypeStr = parameterTypeStr.substring(0, parameterTypeStr.length() - 2);
         return parameterTypeStr;
     }
 
 
     private Object[] mockUpNonNulls(Class<?>[] types, Object[] arguments) {
 
         int j=0;
         for (Class type : types) {
             if (type != null) {
                 if (type.isPrimitive()) {
 
 
                     switch (type.getName()) {
                         case "int":
                             arguments[j] =  0;
                             break;
                         case "double":
                             arguments[j] = (double) 0;
                             break;
                         case "float":
                             arguments[j] = (float) 0;
                             break;
                         case "short":
                             arguments[j] = (short) 0;
                             break;
                         case "boolean":
                             arguments[j] = false;
                             break;
                         case "byte":
                             arguments[j] = (byte) 0;
                             break;
                     }
                 } else if (type.isArray()) {
 
 
                     switch (type.getCanonicalName()) {
                         case "int[]":
                             arguments[j] = new int[0];
                             break;
                         case "double[]":
                             arguments[j] = new double[0];
                             break;
                         case "float[]":
                             arguments[j] = new float[0];
                             break;
                         case "short[]":
                             arguments[j] = new short[0];
                             break;
                         case "boolean[]":
                             arguments[j] = new boolean[0];
                             break;
                         case "byte[]":
                             arguments[j] = new byte[0];
                             break;
                     }
                 }else if(type.isEnum()){
                     arguments[j] = type.getEnumConstants()[0];
                 } else if(type == String.class){
                     arguments[j] = new String("empty-string");
                 }else {
                     try {
                       arguments[j] = mock(type);
                     } catch(MockitoException ex){
                         throw new RuntimeException("Cannot mock: "+type.getCanonicalName(), ex);
                     }
 
 
                 }
             }
             j++;
         }
         return arguments;
     }
 
 }
