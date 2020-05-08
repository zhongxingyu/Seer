 package org.jboss.perfrunner;
 
 import java.io.FileNotFoundException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.Description;
 import org.junit.runner.Runner;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.Suite;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 
 /**
  * A runner that creates a suite from a test class. Each member of the suite is
  * a runner that executes the same test method multiple times with varying
  * parameters.
  *
  * @author Jonathan Fuerth <jfuerth@gmail.com>
  */
 public class PerfRunner extends Suite {
 
   /**
    * Runs a single test method once for each possible combination of its varying
    * parameter values.
    *
    * @author Jonathan Fuerth <jfuerth@gmail.com>
    */
   private static class VaryingParametersTestRunner extends BlockJUnit4ClassRunner {
 
     private final PerformanceReportBuilder performanceReportBuilder = new PerformanceReportBuilder();
 
     VaryingParametersTestRunner(Class<?> type) throws InitializationError {
       super(type);
     }
 
     @Override
     protected List<FrameworkMethod> computeTestMethods() {
       List<FrameworkMethod> testMethods = new ArrayList<FrameworkMethod>();
       for (FrameworkMethod targetMethod : getTestClass().getAnnotatedMethods(Test.class)) {
         Method m = targetMethod.getMethod();
         int paramCount = m.getParameterTypes().length;
 
         int[][] values = new int[paramCount][];
         for (int p = 0; p < paramCount; p++) {
           Varying varying = getSoleVaryingAnnotation(targetMethod.getMethod(), p);
           values[p] = valuesOf(varying);
         }
 
         // the pointers array holds indexes into the values[][] array.
         // we use this to compute the cartesian product of the sets of ints stored in values[][]
         int[] pointers = new int[paramCount];
 
         do {
           int[] params = new int[paramCount];
           for (int i = 0; i < paramCount; i++) {
             params[i] = values[i][pointers[i]];
           }
           testMethods.add(new ParameterizedFrameworkMethod(m, params));
         } while (countUp(pointers, values));
       }
       return testMethods;
     }
 
     @Override
     protected void validateTestMethods(List<Throwable> errors) {
       List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Test.class);
       for (FrameworkMethod fm : methods) {
         fm.validatePublicVoid(false, errors);
         Method m = fm.getMethod();
         for (int p = 0; p < m.getParameterTypes().length; p++) {
           try {
             Class<?> ptype = m.getParameterTypes()[p];
 
             if (ptype != Integer.TYPE) {
               throw new InitializationError(
                   "Method " + m.getName() + " parameter " + p + " is of type " + ptype + ", but only int is supported.");
             }
 
             Varying varying = getSoleVaryingAnnotation(m, p); // if more than one @Varying, this will throw InitializationError
             int[] values = valuesOf(varying);
             if (values.length == 0) {
               throw new InitializationError("Method " + m.getName() + " parameter " + p + " has 0 possible variations");
             }
 
           } catch (Exception e) {
             errors.add(e);
           }
         }
       }
     }
 
     @Override
     protected Description describeChild(FrameworkMethod method) {
       ParameterizedFrameworkMethod pmethod = (ParameterizedFrameworkMethod) method;
 
       // collect the method and parameter annotations for the benefit of the reporting listener
       List<Annotation> annotations = new ArrayList<Annotation>();
       Collections.addAll(annotations, method.getAnnotations());
       for (Annotation[] paramAnnotation : method.getMethod().getParameterAnnotations()) {
         Collections.addAll(annotations, paramAnnotation);
       }
 
       return Description.createTestDescription(
           getTestClass().getJavaClass(),
           testName(method) + Arrays.toString(pmethod.getParameters()),
           annotations.toArray(new Annotation[annotations.size()]));
     }
 
     @Override
     public void run(RunNotifier notifier) {
       Description description = getDescription();
       try {
         performanceReportBuilder.testRunStarted(description);
         super.run(notifier);
       } catch (FileNotFoundException e) {
         throw new RuntimeException("Failed to create PerfRunner report output file", e);
       } finally {
         performanceReportBuilder.testRunFinished(null);
       }
     }
 
     // TODO: this is wedged in here. there must be a better way!
     @Override
     protected void runChild(FrameworkMethod method, RunNotifier notifier) {
       Description description = describeChild(method);
       performanceReportBuilder.testStarted(description);
       super.runChild(method, notifier);
       performanceReportBuilder.testFinished(description);
     }
 
     // Utility methods below here.
     // TODO move most of the following into a new ParameterSet class
 
     /**
      * Increments the first value in pointers, rolling it back to 0 and carrying
      * the 1 if pointers[0] == values[0].length. And so on down the line.
      *
      * @param pointers
      *          the array to modify by adding one (with carry) in the first slot
      * @param values
      *          The length of values[i] determines the value at which
      *          pointers[i] wraps back to 0
      * @return true unless the entire pointers array has "rolled over" back to
      *         0.
      */
     private boolean countUp(int[] pointers, int[][] values) {
       int pos = 0;
       boolean carry;
       do {
         carry = false;
         pointers[pos]++;
         if (pointers[pos] >= values[pos].length) {
           pointers[pos] = 0;
           pos++;
           carry = true;
         }
       } while (pos < pointers.length && carry);
       return !carry;
     }
 
     /**
      * Returns the sequence of values that the given Varying instance specifies. For
      * example, {@code @Varying(from=1, to=10, step=2)} produces an array of 5 values:
      * {@code [1, 3, 5, 7, 9]}.
      *
      * @param varying the varying annotation in question
      * @return
      */
     private int[] valuesOf(Varying varying) {
       if (varying.to() < varying.from()) {
         throw new IllegalArgumentException(
             "Illegal varying parameters: to < from (from=" +
                 varying.from() + ", to=" + varying.to() + ")");
       }
       if (varying.step() < 1) {
         throw new IllegalArgumentException(
             "Illegal varying parameters: step < 1 (step=" + varying.step() + ")");
       }
 
       int valueCount = 0;
       for (int v = varying.from(); v <= varying.to(); v += varying.step()) {
         valueCount++;
       }
 
       int vc = valueCount;
       int[] values = new int[vc];
       for (int v = varying.from(), i = 0; v <= varying.to(); v += varying.step(), i++) {
         values[i] = v;
       }
 
       return values;
     }
 
     /**
      * Returns the one and only {@code @Varying} annotation declared for the
      * given parameter. If there are 0 or more than 1 such annotations, an
      * exception is thrown.
      *
      * @param m The method to retrieve the parameter annotation from
      * @param p The parameter index to retrieve the annotation for
      * @return The only varying annotation on the given method parameter
      */
     private Varying getSoleVaryingAnnotation(Method m, int p) {
       Varying foundIt = null;
       for (Annotation atn : m.getParameterAnnotations()[p]) {
         if (atn.annotationType() == Varying.class) {
           if (foundIt != null) {
             throw new AssertionError("Method " + m.getName() + " parameter " + p + " has more than one @Varying annotation");
           }
           foundIt = (Varying) atn;
         }
       }
       if (foundIt != null) {
         return foundIt;
       }
       throw new AssertionError("Method " + m.getName() + " parameter " + p + " is missing its @Varying annotation");
     }
 
   }
 
 
   public PerfRunner(Class<?> klass) throws Throwable {
     super(klass, createParameterizedRunners(klass));
   }
 
   private static List<Runner> createParameterizedRunners(Class<?> klass) throws InitializationError {
     List<Runner> children = new ArrayList<Runner>();
 
     // TODO we want to iterate over the test methods here and add a suite for each method
     // (to give a nice hierarchical separation of each perf test run)
     children.add(new VaryingParametersTestRunner(klass));
 
     return children;
   }
 
 }
