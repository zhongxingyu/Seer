 package php.runtime.invoke;
 
 import php.runtime.Memory;
 import php.runtime.common.Messages;
 import php.runtime.env.Environment;
 import php.runtime.env.TraceInfo;
 import php.runtime.exceptions.FatalException;
 import php.runtime.exceptions.support.ErrorType;
import php.runtime.lang.Closure;
 import php.runtime.lang.IObject;
 import php.runtime.memory.ArrayMemory;
 import php.runtime.memory.ObjectMemory;
 import php.runtime.memory.ReferenceMemory;
 import php.runtime.memory.StringMemory;
 import php.runtime.reflection.*;
 
 import java.lang.reflect.InvocationTargetException;
 
 final public class InvokeHelper {
 
     private InvokeHelper() { }
 
     public static void checkAccess(Environment env, TraceInfo trace, MethodEntity method)
             throws InvocationTargetException, IllegalAccessException {
         int access = method.canAccess(env);
         if (access == 0)
             return;
 
         ClassEntity contextCls = env.getLastClassOnStack();
         String context = contextCls == null ? "" : contextCls.getName();
 
         switch (access){
             case 1: throw new FatalException(
                     Messages.ERR_CALL_TO_PROTECTED_METHOD.fetch(
                             method.getClazz().getName() + "::" + method.getName(), context
                     ),
                     trace
             );
             case 2: throw new FatalException(
                     Messages.ERR_CALL_TO_PRIVATE_METHOD.fetch(
                             method.getClazz().getName() + "::" + method.getName(), context
                     ),
                     trace
             );
         }
     }
 
     public static void checkAccess(Environment env, TraceInfo trace, PropertyEntity property)
             throws InvocationTargetException, IllegalAccessException {
         switch (property.canAccess(env)){
             case 1: throw new FatalException(
                     Messages.ERR_ACCESS_TO_PROTECTED_PROPERTY.fetch(
                             property.getClazz().getName(), property.getName()
                     ),
                     trace
             );
             case 2: throw new FatalException(
                     Messages.ERR_ACCESS_TO_PRIVATE_PROPERTY.fetch(
                             property.getClazz().getName(), property.getName()
                     ),
                     trace
             );
         }
     }
 
     public static void invalidTypeHinting(Environment env,
                                           TraceInfo trace, ParameterEntity param, int index, Memory passed,
                                           String originClassName, String originMethodName){
         String given;
         if (passed == null){
             given = "none";
         } else if (passed.isObject()) {
             given = "instance of " + passed.toValue(ObjectMemory.class).getReflection().getName();
         } else {
             given = passed.getRealType().toString();
         }
 
         String method = originMethodName == null ? originClassName : originClassName + "::" + originMethodName;
 
         if (param.getTypeClass() == null){
             env.error(
                     param.getTrace(),
                     ErrorType.E_RECOVERABLE_ERROR,
                     "Argument %s passed to %s() must be of the type %s, %s given, called in %s on line %d, position %d and defined",
                     index,
                     method,
                     param.getType().toString(), given,
 
                     trace.getFileName(),
                     trace.getStartLine() + 1,
                     trace.getStartPosition() + 1
             );
         } else {
             ClassEntity need = env.fetchClass(param.getTypeClass(), false);
             String what = "";
             if (need == null || need.isClass()){
                 what = "be an instance of";
             } else if (need.isInterface()){
                 what = "implement interface";
             }
 
             what = what + " " + param.getTypeClass();
             env.error(
                     param.getTrace(),
                     ErrorType.E_RECOVERABLE_ERROR,
                     "Argument %s passed to %s() must %s, %s given, called in %s on line %d, position %d and defined",
                     index,
                     method,
 
                     what, given,
                     trace.getFileName(),
                     trace.getStartLine() + 1,
                     trace.getStartPosition() + 1
             );
         }
     }
 
     public static Memory[] makeArguments(Environment env, Memory[] args,
                                        ParameterEntity[] parameters,
                                        String originClassName, String originMethodName,
                                        TraceInfo trace){
         if (parameters == null)
             return args;
 
         Memory[] passed = args;
         if ((args == null && parameters.length > 0) || (args != null && args.length < parameters.length)){
             passed = new Memory[parameters.length];
             if (args != null && args.length > 0){
                 System.arraycopy(args, 0, passed, 0, args.length);
             }
         }
 
         int i = 0;
         if (passed != null)
         for(ParameterEntity param : parameters){
             Memory arg = passed[i];
             if (arg == null) {
                 Memory def = param.getDefaultValue();
                 if (def != null){
                     if (!param.isReference())
                         passed[i] = def.toImmutable(env, trace);
                     else
                         passed[i] = new ReferenceMemory(def.toImmutable(env, trace));
 
                 } else {
                     if (param.getTypeClass() != null)
                         invalidTypeHinting(env, trace, param, i + 1, null, originClassName, originMethodName);
 
                     env.error(trace, ErrorType.E_ERROR,
                             Messages.ERR_MISSING_ARGUMENT, (i + 1) + " ($" + param.getName() + ")",
                             originMethodName == null ? originClassName : originClassName + "::" + originMethodName
                     );
                     passed[i] = param.isReference() ? new ReferenceMemory() : Memory.NULL;
                 }
             } else {
                 if (param.isReference()) {
                     if (!arg.isReference() && !arg.isObject()){
                         env.error(trace, ErrorType.E_ERROR, "Only variables can be passed by reference");
                         passed[i] = new ReferenceMemory(arg);
                     }
                 } else
                     passed[i] = arg.toImmutable();
             }
 
             if (!param.checkTypeHinting(env, passed[i])){
                 invalidTypeHinting(env, trace, param, i + 1, passed[i], originClassName, originMethodName);
             }
             i++;
         }
         return passed;
     }
 
     public static Memory callAny(Memory method, Memory[] args, Environment env, TraceInfo trace)
             throws Throwable {
         method = method.toImmutable();
         if (method.isObject()){
             return ObjectInvokeHelper.invokeMethod(method, null, null, env, trace, args);
         } else if (method.isArray()){
             Memory one = null, two = null;
             for(Memory el : (ArrayMemory)method){
                 if (one == null)
                     one = el;
                 else if (two == null)
                     two = el;
                 else
                     break;
             }
 
             if (one == null || two == null) {
                 env.error(trace, Messages.ERR_CALL_TO_UNDEFINED_FUNCTION.fetch(method.toString()));
                 return Memory.NULL;
             }
 
             String methodName = two.toString();
             if (one.isObject())
                 return ObjectInvokeHelper.invokeMethod(one, methodName, methodName.toLowerCase(), env, trace, args);
             else {
                 String className = one.toString();
                 ClassEntity magic = env.fetchMagicClass(className);
                 if (magic != null)
                     className = magic.getName();
 
                 return InvokeHelper.callStaticDynamic(
                         env,
                         trace,
                         className, className.toLowerCase(),
                         methodName, methodName.toLowerCase(),
                         args
                 );
             }
         } else {
             String methodName = method.toString();
             int p;
             if ((p = methodName.indexOf("::")) > -1) {
                 String className = methodName.substring(0, p);
                 methodName = methodName.substring(p + 2, methodName.length());
                 return InvokeHelper.callStaticDynamic(
                         env, trace,
                         className, className.toLowerCase(),
                         methodName, methodName.toLowerCase(),
                         args
                 );
             } else {
                 return InvokeHelper.call(env, trace, methodName.toLowerCase(), methodName, args);
             }
         }
     }
 
     public static Memory call(Environment env, TraceInfo trace, FunctionEntity function, Memory[] args)
             throws Throwable {
         Memory[] passed = makeArguments(env, args, function.parameters, function.getName(), null, trace);
 
         Memory result = function.getImmutableResult();
         if (result != null) return result;
 
         if (trace != null) env.pushCall(trace, null, args, function.getName(), null, null);
         try {
             result = function.invoke(env, trace, passed);
         } finally {
             if (trace != null)
                 env.popCall();
         }
         return result;
     }
 
     public static Memory call(Environment env, TraceInfo trace, String sign, String originName,
                               Memory[] args) throws Throwable {
         FunctionEntity function = env.functionMap.get(sign);
         if (function == null) {
             env.error(trace, Messages.ERR_CALL_TO_UNDEFINED_FUNCTION.fetch(originName));
             return Memory.NULL;
         }
         return call(env, trace, function, args);
     }
 
     public static Memory callStaticDynamic(Environment env, TraceInfo trace,
                                            String originClassName, String className,
                                            String originMethodName, String methodName,
                                            Memory[] args) throws Throwable {
         return callStatic(
                 env, trace,
                 className, methodName,
                 originClassName, originMethodName,
                 args
         );
     }
 
     public static Memory callStatic(Environment env, TraceInfo trace,
                                     String className, String methodName, String originClassName, String originMethodName,
                                     Memory[] args)
             throws Throwable {
         ClassEntity classEntity = env.fetchClass(originClassName, className, true);
 
         MethodEntity method = classEntity == null ? null : classEntity.findMethod(methodName);
         Memory[] passed = null;
 
         IObject maybeObject = env.getLateObject();
         if (method == null){
             if (maybeObject != null && maybeObject.getReflection().isInstanceOf(classEntity))
                 return ObjectInvokeHelper.invokeMethod(
                         new ObjectMemory(maybeObject), originMethodName, methodName, env, trace, args
                 );
 
             if (classEntity != null && classEntity.methodMagicCallStatic != null){
                 method = classEntity.methodMagicCallStatic;
                 passed = new Memory[]{
                         new StringMemory(originMethodName),
                         new ArrayMemory(true, args)
                 };
             } else {
                 if (classEntity == null) {
                     env.error(trace, Messages.ERR_CLASS_NOT_FOUND.fetch(originClassName));
                     return Memory.NULL;
                 }
             }
         }
 
         if (method == null){
             env.error(trace, Messages.ERR_CALL_TO_UNDEFINED_METHOD.fetch(originClassName + "::" + originMethodName));
             return Memory.NULL;
         }
 
         if (!method.isStatic()) {
            if (maybeObject != null && !(maybeObject instanceof Closure)
                     && maybeObject.getReflection().isInstanceOf(classEntity))
                 return ObjectInvokeHelper.invokeMethod(maybeObject, method, env, trace, args);
 
             env.error(trace,
                     ErrorType.E_STRICT,
                     Messages.ERR_NON_STATIC_METHOD_CALLED_DYNAMICALLY,
                     originClassName, originMethodName
             );
         }
 
         checkAccess(env, trace, method);
         if (passed == null)
             passed = makeArguments(env, args, method.parameters, originClassName, originMethodName, trace);
 
         Memory result = method.getImmutableResult();
         if (result != null) return result;
 
         try {
             if (trace != null)
                 env.pushCall(trace, null, args, originMethodName, method.getClazz().getName(), originClassName);
 
             result = method.invokeStatic(env, passed);
         } finally {
             if (trace != null)
                 env.popCall();
         }
 
         return result;
     }
 
     public static Memory callStatic(Environment env, TraceInfo trace,
                                     MethodEntity method,
                                     Memory[] args)
             throws Throwable {
         String originClassName = method.getClazz().getName();
         String originMethodName = method.getName();
 
         checkAccess(env, trace, method);
 
         Memory[] passed = makeArguments(env, args, method.parameters, originClassName, originMethodName, trace);
         Memory result = method.getImmutableResult();
         if (result != null)
             return result;
 
         try {
             if (trace != null)
                 env.pushCall(trace, null, args, originMethodName, method.getClazzName(), originClassName);
 
             result = method.invokeStatic(env, passed);
         } finally {
             if (trace != null)
                 env.popCall();
         }
 
         return result;
     }
 
     public static void checkReturnReference(Memory memory, Environment env, TraceInfo trace){
         if (memory.isImmutable()){
             env.warning(trace, Messages.ERR_RETURN_NOT_REFERENCE.fetch());
         }
     }
 }
