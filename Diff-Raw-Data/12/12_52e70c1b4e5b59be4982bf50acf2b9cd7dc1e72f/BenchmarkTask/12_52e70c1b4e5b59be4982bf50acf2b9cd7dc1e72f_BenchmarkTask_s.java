 package org.jbenchx.run;
 
 import java.lang.reflect.*;
 import java.util.*;
 
 import org.jbenchx.*;
 import org.jbenchx.Timer;
 import org.jbenchx.result.*;
 import org.jbenchx.util.*;
 import org.jbenchx.vm.*;
 
 public class BenchmarkTask implements IBenchmarkTask {
   
   private static final double          SQRT2 = Math.sqrt(2);
   
   private final String                 fClassName;
   
   private final String                 fMethodName;
   
   private final BenchmarkParameters    fParams;
   
   private final boolean                fSingleRun;
   
   private final ParameterizationValues fConstructorArguments;
   
   private final ParameterizationValues fMethodArguments;
   
   private final ArrayList<Class<?>>    fMethodArgumentTypes;
   
  public BenchmarkTask(Method method, BenchmarkParameters params, boolean singleRun, ParameterizationValues constructorArguments,
       ParameterizationValues methodArguments) {
    this(method.getDeclaringClass().getName(), method.getName(), new ArrayList<Class<?>>(Arrays.<Class<?>>asList(method.getParameterTypes())),
         params, singleRun, constructorArguments, methodArguments);
   }
   
   public BenchmarkTask(String className, String methodName, List<Class<?>> methodArgumentTypes, BenchmarkParameters params, boolean singleRun,
       ParameterizationValues constructorArguments, ParameterizationValues methodArguments) {
     if (methodArgumentTypes.size() != methodArguments.getValues().size()) {
       throw new IllegalArgumentException("Method argument type count does not match the method arguments count: " + methodArgumentTypes.size()
           + " != " + methodArguments.getValues().size());
     }
     fClassName = className;
     fMethodName = methodName;
     fMethodArgumentTypes = new ArrayList<Class<?>>(methodArgumentTypes);
     fParams = params;
     fSingleRun = singleRun;
     fConstructorArguments = constructorArguments;
     fMethodArguments = methodArguments;
   }
   
   @Override
   public void run(BenchmarkResult result, IBenchmarkContext context) {
     try {
       
       context.getProgressMonitor().started(this);
       
       TaskResult taskResult = internalRun(context);
       
       result.addResult(this, taskResult);
       context.getProgressMonitor().done(this);
       
     } catch (Exception e) {
       result.addResult(this, new TaskResult(fParams, new BenchmarkTimings(), 0, new BenchmarkTaskFailure(this, e)));
       context.getProgressMonitor().failed(this);
     }
   }
   
   private TaskResult internalRun(IBenchmarkContext context) throws Exception {
     long timerGranularity = 10 * TimeUtil.MS;
     long methodInvokeTime = 0;
     SystemInfo systemInfo = context.getSystemInfo();
     if (systemInfo != null) {
       timerGranularity = systemInfo.getTimerGranularity();
       methodInvokeTime = systemInfo.getMethodInvokeTime();
     }
     
     Object benchmark = createInstance();
     Method method = getBenchmarkMethod(benchmark);
     long iterationCount = findIterationCount(benchmark, method, timerGranularity);
     
     BenchmarkTimings timings = new BenchmarkTimings();
     SystemUtil.cleanMemory();
     VmState preState = VmState.getCurrentState();
     long runtimePerIteration = Long.MAX_VALUE;
     do {
       
       GcStats preGcStats = SystemUtil.getGcStats();
       long time = singleRun(benchmark, method, iterationCount);
       GcStats postGcStats = SystemUtil.getGcStats();
       VmState postState = VmState.getCurrentState();
       runtimePerIteration = Math.min(runtimePerIteration, time / iterationCount);
       
       Timing timing = new Timing(time, preGcStats, postGcStats);
       if (preState.equals(postState)) {
         timings.add(timing);
       } else {
         // restart
         timings.clear();
         iterationCount = findIterationCount(benchmark, method, timerGranularity);
         runtimePerIteration = Long.MAX_VALUE;
       }
       context.getProgressMonitor().run(this, timing, VmState.difference(preState, postState));
       preState = postState;
       
     } while (timings.needsMoreRuns(fParams));
     
 //    long avgNs = Math.round(timings.getEstimatedTime() / iterationCount / fDivisor);
 //    System.out.println();
 //    System.out.println(this + ": " + TimeUtil.toString(avgNs));
     TaskResult result = new TaskResult(fParams, timings, iterationCount);
     long minSingleIterationTime = methodInvokeTime * 10;
     if (runtimePerIteration < minSingleIterationTime) {
       result.addWarning(new BenchmarkWarning("Runtime of single iteration too short: " + runtimePerIteration
           + "ns, increase work in single iteration to run at least " + minSingleIterationTime + "ns"));
     }
     return result;
   }
   
   private long singleRun(Object benchmark, Method method, long iterationCount) throws IllegalArgumentException, IllegalAccessException,
       InvocationTargetException {
     Timer timer = new Timer();
     timer.start();
     Object[] arguments = fMethodArguments.getValues().toArray();
     for (long i = 0; i < iterationCount; ++i) {
       method.invoke(benchmark, arguments);
     }
     return timer.stopAndReset();
   }
   
   private long findIterationCount(Object benchmark, Method method, long timerGranularity) throws IllegalAccessException, InvocationTargetException {
     if (fSingleRun) {
       return 1;
     }
     
     Timer timer = new Timer();
     long iterations = 1;
     long time;
     for (;;) {
       
       Object[] arguments = fMethodArguments.getValues().toArray();
       timer.start();
       for (int i = 0; i < iterations; ++i) {
         method.invoke(benchmark, arguments);
       }
       time = timer.stopAndReset();
       if (iterations == 1 && time > fParams.getTargetTimeNs()) {
         break;
       }
       if (time >= fParams.getTargetTimeNs() / SQRT2 && time < fParams.getTargetTimeNs() * SQRT2) {
         break;
       }
       time = Math.max(time, 2 * timerGranularity); // at least two times the timer granularity
       double factor = (1.0 * fParams.getTargetTimeNs()) / time;
       iterations = Math.round(iterations * factor);
       iterations = Math.max(1, iterations);
       
     }
     return iterations;
   }
   
   private Method getBenchmarkMethod(Object benchmark) throws SecurityException, NoSuchMethodException {
     return benchmark.getClass().getMethod(fMethodName, fMethodArgumentTypes.toArray(new Class[fMethodArgumentTypes.size()]));
   }
   
   private Object createInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
       InvocationTargetException {
     ClassLoader classLoader = ClassUtil.createClassLoader();
 //    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     Class<?> clazz = classLoader.loadClass(fClassName);
     Constructor<?> constructor = clazz.getConstructors()[0];
     return constructor.newInstance(fConstructorArguments.getValues().toArray());
   }
   
   @Override
   public String toString() {
     return getName();
   }
   
   @Override
   public String getName() {
     StringBuilder sb = new StringBuilder();
     int lastDot = fClassName.lastIndexOf('.');
     if (lastDot == -1) {
       sb.append(fClassName);
     } else {
       sb.append(fClassName.substring(lastDot + 1));
     }
     appendArguments(sb, fConstructorArguments);
     sb.append('.');
     sb.append(fMethodName);
     appendArguments(sb, fMethodArguments);
     return sb.toString();
   }
   
   private void appendArguments(StringBuilder sb, ParameterizationValues arguments) {
     if (!arguments.hasArguments()) return;
     sb.append('(');
     for (Object argument: arguments.getValues()) {
       sb.append(argument.toString());
       sb.append(',');
     }
     sb.setCharAt(sb.length() - 1, ')');
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((fClassName == null) ? 0 : fClassName.hashCode());
     result = prime * result + ((fConstructorArguments == null) ? 0 : fConstructorArguments.hashCode());
     result = prime * result + ((fMethodArgumentTypes == null) ? 0 : fMethodArgumentTypes.hashCode());
     result = prime * result + ((fMethodArguments == null) ? 0 : fMethodArguments.hashCode());
     result = prime * result + ((fMethodName == null) ? 0 : fMethodName.hashCode());
     result = prime * result + ((fParams == null) ? 0 : fParams.hashCode());
     result = prime * result + (fSingleRun ? 1231 : 1237);
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj) return true;
     if (obj == null) return false;
     if (getClass() != obj.getClass()) return false;
     BenchmarkTask other = (BenchmarkTask)obj;
     if (fClassName == null) {
       if (other.fClassName != null) return false;
     } else if (!fClassName.equals(other.fClassName)) return false;
     if (fConstructorArguments == null) {
       if (other.fConstructorArguments != null) return false;
     } else if (!fConstructorArguments.equals(other.fConstructorArguments)) return false;
     if (fMethodArgumentTypes == null) {
       if (other.fMethodArgumentTypes != null) return false;
     } else if (!fMethodArgumentTypes.equals(other.fMethodArgumentTypes)) return false;
     if (fMethodArguments == null) {
       if (other.fMethodArguments != null) return false;
     } else if (!fMethodArguments.equals(other.fMethodArguments)) return false;
     if (fMethodName == null) {
       if (other.fMethodName != null) return false;
     } else if (!fMethodName.equals(other.fMethodName)) return false;
     if (fParams == null) {
       if (other.fParams != null) return false;
     } else if (!fParams.equals(other.fParams)) return false;
     if (fSingleRun != other.fSingleRun) return false;
     return true;
   }
   
 }
