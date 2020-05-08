 package org.jbenchx;
 
 import java.lang.annotation.*;
 import java.lang.reflect.*;
 import java.util.*;
 
 import org.jbenchx.annotations.*;
 import org.jbenchx.monitor.*;
 import org.jbenchx.result.*;
 import org.jbenchx.run.*;
 import org.jbenchx.util.*;
 
 public class BenchmarkRunner {
   
   private final List<Class<?>> fBenchmarks = new ArrayList<Class<?>>();
   
   public void add(Class<?> benchmark) {
     fBenchmarks.add(benchmark);
   }
   
   public IBenchmarkResult run(IBenchmarkContext context) {
     IProgressMonitor progressMonitor = context.getProgressMonitor();
     BenchmarkResult result = new BenchmarkResult(context.getSystemInfo());
     List<BenchmarkTask> benchmarkTasks = findAllBenchmarkTasks(context);
     progressMonitor.init(benchmarkTasks.size(), result);
     for (IBenchmarkTask task: benchmarkTasks) {
       task.run(result, context);
     }
     progressMonitor.finished();
     return result;
   }
   
   private List<BenchmarkTask> findAllBenchmarkTasks(IBenchmarkContext context) {
     List<BenchmarkTask> tasks = new ArrayList<BenchmarkTask>();
     for (Class<?> clazz: fBenchmarks) {
       addBenchmarkTasks(context, tasks, clazz);
     }
     return tasks;
   }
   
   private void addBenchmarkTasks(IBenchmarkContext context, List<BenchmarkTask> tasks, Class<?> clazz) {
     
     Constructor<?>[] constructors = clazz.getConstructors();
     if (constructors.length != 1) {
       throw new BenchmarkClassError(clazz, "Benchmark class must have exactly one constructor!");
     }
     
     Iterable<ParameterizationValues> constructorParameterizations = null;
     Constructor<?> constructor = constructors[0];
     constructorParameterizations = getConstructorArgumentsIterator(constructor);
     
 //    if (!ClassUtil.hasDefaultConstructor(clazz)) {
 //      result.addGeneralError(new BenchmarkClassError(clazz, "No default constructor found!"));
 //    }
     
     Method[] methods = clazz.getMethods();
     Arrays.sort(methods, MethodByNameSorter.INSTANCE);
     for (Method method: methods) {
       
       BenchmarkParameters params = BenchmarkParameters.read(method);
       if (params == null) {
         continue;
       }
       
 //      if (method.getParameterTypes().length > 0) {
 //        result.addGeneralError(new BenchmarkClassError(clazz, "Benchmark method " + method.getName() + " has parameters!"));
 //        continue;
 //      }
       
       params = BenchmarkParameters.merge(context.getDefaultParams(), params);
       boolean singleRun = hasSingleRunAnnotation(method);
       
       Iterable<ParameterizationValues> methodParameterizations = getMethodArgumentsIterator(method);
       for (ParameterizationValues constructorArguments: constructorParameterizations) {
         for (ParameterizationValues methodArguments: methodParameterizations) {
          tasks.add(new BenchmarkTask(clazz, method, params, singleRun, constructorArguments, methodArguments));
         }
       }
       
     }
     
   }
   
   private Iterable<ParameterizationValues> getConstructorArgumentsIterator(Constructor<?> constructor) {
     return getArgumentsIterator(constructor, constructor.getParameterTypes(), constructor.getParameterAnnotations(), constructor.getDeclaringClass());
   }
   
   private Iterable<ParameterizationValues> getMethodArgumentsIterator(Method method) {
     return getArgumentsIterator(method, method.getParameterTypes(), method.getParameterAnnotations(), method.getDeclaringClass());
   }
   
   private Iterable<ParameterizationValues> getArgumentsIterator(AccessibleObject object, Class<?>[] paramTypes, Annotation[][] paramAnnotations,
       Class<?> declaringClass) {
     List<Parameterization> parameterizations = new ArrayList<Parameterization>(paramTypes.length);
     for (int i = 0; i < paramTypes.length; ++i) {
       parameterizations.add(Parameterization.create(declaringClass, object, paramTypes[i], paramAnnotations[i]));
     }
     return new ParameterizationIterable(parameterizations);
   }
   
   private boolean hasSingleRunAnnotation(Method method) {
     return !ClassUtil.findMethodAnnotations(method, SingleRun.class).isEmpty();
   }
   
 }
