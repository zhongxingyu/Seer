 package fi.lolcatz.profiler;
 
 import fi.lolcatz.profiledata.ProfileData;
 import org.apache.log4j.Logger;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 
 
 public abstract class AbstractImpl {
 
     private static Logger logger = Logger.getLogger(AbstractImpl.class);
 
     String methodName, className;
     
     public void setMethodName(String name) {
         this.methodName = name;
     }
 
     public void setClassName(String name) {
         this.className = name;
     }
 
     public String getClassName() {
         return className;
     }
 
     public String getMethodName() {
         return methodName;
     }
     
     private Method getMethod(Class<?>... parameterTypes) throws Exception {
         if (className == null) {
             throw new NullPointerException("className is null");
         }
         if (methodName == null) {
             throw new NullPointerException("methodName is null");
         }
         Class<?> c = Class.forName(className);
         outer:
         for (Method method : c.getMethods()) {
             if (!methodName.equals(method.getName())) continue;
             Class<?>[] methodParameterTypes = method.getParameterTypes();
             if (methodParameterTypes.length != parameterTypes.length) continue;
             for (int i = 0; i < parameterTypes.length; i++) {
                 if (!changeToPrimitive(parameterTypes[i]).equals(changeToPrimitive(methodParameterTypes[i]))) {
                     continue outer;
                 }
             }
             return method;
         }
         throw new NoSuchMethodException("Method with name " + methodName + " and suitable parameters not found from " + className);
     }
     
     /**
      * Run static method with given inputs.
      * @param inputs Input variables. Primitive boxing classes (Integer, Boolean, etc.) must not be nulls.
      * @return Total cost
      * @throws Exception 
      */
     public long runStaticOnce(Object... inputs) throws Exception {
         return runOnce(null, inputs);
     }
     
     /**
      * Run method of given instance with given inputs.
      * @param instance Instance of the class which we are testing.
      * @param inputs Input variables. Primitive boxing classes (Integer, Boolean, etc.) must not be nulls.
      * @return Total cost
      * @throws Exception 
      */
     public long runOnce(Object instance, Object... inputs) throws Exception {
         ProfileData.disallowProfiling();
        Method method;
         try {
            System.gc();
            System.runFinalization();
            ProfileData.resetCounters();
            
             Class<?>[] parameterTypes = new Class<?>[inputs.length];
             for (int i = 0; i < inputs.length; i++) {
                 parameterTypes[i] = inputs[i].getClass();
             }
            method = getMethod(parameterTypes);
            if (!Modifier.isStatic(method.getModifiers()) && instance == null) {
                 throw new NullPointerException("Cannot access instance method of a null instance");
             }
        } finally {
             ProfileData.allowProfiling();
        }
        try {
            method.invoke(instance, (Object[]) inputs);
         } catch (Exception x) {
             logger.fatal("Exception when running", x);
             throw x;
         }
         return Util.getTotalCost();
     }
 
     /**
      * Run method of given instance with given inputs n times. Smallest time will be returned.
      *
      * @param n Number of times test will be run.
      * @param instance Instance of the class which we are testing.
      * @param inputs Input variables. Primitive boxing classes (Integer, Boolean, etc.) must not be nulls.
      * @return Total cost
      * @throws Exception
      */
     public long runNTimes(int n, Object instance, Object... inputs) throws Exception {
         if (n < 1) throw new Exception("runNTimes(): n must be at least 1");
         long[] times = new long[n];
         for (int i = 0; i < n; i++) {
             times[i] = runOnce(instance, inputs);
         }
         Arrays.sort(times);
         return times[0];
     }
 
     /**
      * Run static method with given inputs n times. Smallest time will be returned.
      *
      * @param n Number of times test will be run.
      * @param inputs Input variables. Primitive boxing classes (Integer, Boolean, etc.) must not be nulls.
      * @return Total cost
      * @throws Exception
      */
     public long runStaticNTimes(int n, Object... inputs) throws Exception {
         return runNTimes(n, null, inputs);
     }
 
     /**
      * Run method of given instance with given inputs with runNtimes(2, instance, inputs).
      *
      * @param instance Instance of the class which we are testing.
      * @param inputs Input variables. Primitive boxing classes (Integer, Boolean, etc.) must not be nulls.
      * @return Total cost
      * @throws Exception
      */
     public long run(Object instance, Object... inputs) throws Exception {
         return runNTimes(2, instance, inputs);
     }
 
     /**
      * Run static method with given inputs with runStaticNTimes(2, inputs).
      *
      * @param inputs Input variables. Primitive boxing classes (Integer, Boolean, etc.) must not be nulls.
      * @return Total cost
      * @throws Exception
      */
     public long runStatic(Object... inputs) throws Exception {
         return runStaticNTimes(2, inputs);
     }
 
     public long getMarginOfError(long cost) {
         long len = Long.toString(cost).length();
         len = (len/3)*(len/3) + (len%3);
         if(len < 2) return 10;
         
         return len*50;
     }
 
     private Class<?> changeToPrimitive(Class<?> aClass) {
         if (Character.class.equals(aClass)) {
             return char.class;
         }
         if (Byte.class.equals(aClass)) {
             return byte.class;
         }
         if (Short.class.equals(aClass)) {
             return short.class;
         }
         if (Integer.class.equals(aClass)) {
             return int.class;
         }
         if (Long.class.equals(aClass)) {
             return long.class;
         }
         if (Float.class.equals(aClass)) {
             return float.class;
         }
         if (Double.class.equals(aClass)) {
             return double.class;
         }
         if (Boolean.class.equals(aClass)) {
             return boolean.class;
         }
         if (Void.class.equals(aClass)) {
             return void.class;
         }
         return aClass;
     }
     
 }
