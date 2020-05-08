 package main.scala.com.dindane.mireille;
 
 import java.lang.invoke.*;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class RunTime {
     public MethodHandle FALLBACK;
     public HashMap<String, ArrayList<CallSiteInformation>> callsInfo = new HashMap<>();
 
     private static RunTime instance = new RunTime();
 
     private RunTime() {
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 String stringBuffer;
                 int nonOptimCounter = 0;
 
                 System.out.println("\n\nNon-optimized method calls:");
                 System.out.println("===========================");
                 for (Map.Entry<String, ArrayList<CallSiteInformation>> info : callsInfo.entrySet()) {
                     if (info.getValue().size() > 2) {
                         System.out.println();
                         for (CallSiteInformation callInfo : info.getValue()) {
                             stringBuffer = String.format("=> %s.%s() in %s:%d",
                                     callInfo.obj.getName(),
                                     callInfo.methodName,
                                     callInfo.fileName,
                                     callInfo.lineNumber);
                             nonOptimCounter++;
                             System.out.println(stringBuffer);
                         }
                     }
                 }
 
                 System.out.println(String.format("\nNon-optimized method calls: %d", nonOptimCounter));
                 System.out.println(String.format("Total method calls:         %d", callsInfo.size()));
             }
         });
     }
 
     public static CallSite bootstrap(MethodHandles.Lookup lookUp, String methodName, MethodType methodType,
                                      String fileName, Integer lineNumber) {
         return instance.bsm(lookUp, methodName, methodType, fileName, lineNumber);
     }
 
     public static RunTime getInstance() {
         return instance;
     }
 
     private CallSite bsm(MethodHandles.Lookup lookUp, String methodName, MethodType methodType,
                          String fileName, Integer lineNumber) {
         try {
             FALLBACK = MethodHandles.lookup().findVirtual(InliningCacheCallSite.class,
                     "fallback",
                     MethodType.methodType(Object.class, Object[].class));
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
 
         CallSite callSite = new InliningCacheCallSite(lookUp, methodName, methodType, FALLBACK);
         try {
             callsInfo.get(methodName).add(new CallSiteInformation(callSite,
                     callSite.type().parameterType(0),
                     methodName,
                     fileName,
                     lineNumber));
         } catch (NullPointerException e) {
             callsInfo.put(methodName, new ArrayList<CallSiteInformation>());
             callsInfo.get(methodName).add(new CallSiteInformation(callSite,
                     callSite.type().parameterType(0),
                     methodName,
                     fileName,
                     lineNumber));
         }
 
         return callSite;
     }
 }
 
 class InliningCacheCallSite extends MutableCallSite {
     private MethodHandles.Lookup lookUp;
     private String methodName;
     private MethodType methodType;
 
     public InliningCacheCallSite(MethodHandles.Lookup lookUp, String methodName, MethodType methodType, MethodHandle fallBack) {
         super(methodType);
 
         this.lookUp = lookUp;
         this.methodName = methodName;
         this.methodType = methodType;
 
         MethodHandle fallbackMethodHandle = fallBack
                 .bindTo(this)
                 .asCollector(Object[].class, methodType.parameterCount())
                 .asType(methodType);
 
         setTarget(fallbackMethodHandle);
     }
 
     public Object fallback(Object... args) throws Throwable {
         Class[] parameterTypes = methodType
                 .dropParameterTypes(0, 1)
                 .parameterArray();
         Method method = buildMethodObject(args[0].getClass(), methodName, parameterTypes);
         method.setAccessible(true);
         MethodHandle methodHandle = MethodHandles.publicLookup().unreflect(method);
 
         return methodHandle.invokeWithArguments(args);
     }
 
     /**
      * Given a Class object, a method name, and the parameters' types of the latter,
      * this method returns a well constructed Method object.
      *
      * The case where the specified method doesn't exist in the Class object,
      * but in one of its parents, is supported.
      *
     * @param klass The class where to loop for the method
      * @param methodName
      * @param parameterTypes The types of the method's arguments
      * @return A well constructed Method object
      */
     private Method buildMethodObject(Class klass, String methodName, Class[] parameterTypes) {
         Method method;
 
         try {
             method = klass.getDeclaredMethod(methodName, parameterTypes);
         } catch (NoSuchMethodException e) {
             method = buildMethodObject(klass.getSuperclass(), methodName, parameterTypes);
         }
 
         return method;
     }
 
 }
 
 /**
  * An object representing a CallSite and some information related to it, such as
  * the method name, the file name and the line number of the original call
  */
 class CallSiteInformation {
     public CallSite callSite;
     public Class obj;
     public String methodName;
     public String fileName;
     public int lineNumber;
 
     public CallSiteInformation(CallSite callSite, Class obj, String methodName, String fileName, int lineNumber) {
         this.callSite = callSite;
         this.obj = obj;
         this.methodName = methodName;
         this.fileName = fileName;
         this.lineNumber = lineNumber;
     }
 
     public String toString() {
         return new StringBuilder("CallSite:    ").append(callSite).append("\n")
                 .append("Object:      ").append(obj).append("\n")
                 .append("Method name: ").append(methodName).append("\n")
                 .append("File name:   ").append(fileName).append("\n")
                 .append("Line number: ").append(lineNumber).append("\n")
                 .toString();
     }
 }
