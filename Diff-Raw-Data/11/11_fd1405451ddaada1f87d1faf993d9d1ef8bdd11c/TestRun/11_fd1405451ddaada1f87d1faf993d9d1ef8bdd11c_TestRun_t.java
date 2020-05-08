 package tbc.supercheck;
 
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * A TestRun object represents the ability to perform tests on one or a set of
  * properties (invariant predicates). A property can be defined in any class,
  * and can be run using the {@link #runOn(Class, String, int)} method of this 
  * class. Alternatively, all of the properties declared in a single class may 
  * be run using the overload {@link #runOn(Class, int)}.
  * 
  * @author Karl Jonathan Ward <karl.j.ward@googlemail.com>
  */
 public class TestRun {
     
     private boolean continuePropAfterFail = false;
     
     private boolean printSuccessRuns      = false;
     
     private Recording recording           = new Recording();
     
     /**
      * Set to true to make TestRun print out details of successful property 
      * tests, not just those that fail. E.g.:
      * 
      * <pre>new TestRun().setVerbose(true).runOn(SomeInvariants.class, 1000);</pre>
      */
     public TestRun setVerbose(boolean b) {
         printSuccessRuns = b;
         return this;
     }
     
     /**
      * Set to true to continue to test a property after it has failed for
      * some data. E.g.:
      * 
      * <pre>new TestRun().setContProp(true).runOn(SomeInvariants.class, 1000);</pre>
      */
     public TestRun setContProp(boolean b) {
         continuePropAfterFail = b;
         return this;
     }
     
     /**
      * Answers a recording of the tests executed by this test run. The order of
      * properties tested is remembered, along with a random seed value for each.
      * A recording can be played back by calling {@link #runRecording(Recording)}.
      * <p>
      * A Recoding may be serialised using the Java Serialization API. In this
      * example the same tests would be executed twice:
      * <pre>
      * // Perform a test run.
      * TestRun testRun = new TestRun();
      * testRun.runOn(SomeInvariants.class, 10000);
      * testRun.runOn(SomeOtherInvariants.class, 10000);
      * 
      * // Serialise the test run's recording.
      * ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(filename));
      * oout.writeObject(new TestRun().getRecording());
      * oout.close();
      * 
      * // Retrieve the recording.
      * ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
      * Recording recording = (Recording) oin.readObject();
      * oin.close();
      * 
      * // Play the recording.
      * TestRun playBackTestRun = new TestRun();
      * playBackTestRun.runRecording(recording);
      * </pre>
      * 
      * @return a Recording of property tests
      */
     public Recording getRecording() {
         return recording;
     }
     
     /**
      * Re-runs the tests remembered by a Recording, in the same order and with
      * the same data. However, if the signatures of any properties in the recording
      * have changed since the recording was made, play back will fail. This is
      * because only the random seeds used to generate data are remembered in the
     * recording - if the arguments to a property have changed, the wrong data
     * will be reproduced.
      */
     public void runRecording(Recording recording) {
         recording.playBack(this);
     }
 	
     /**
      * Run arbitrary data through all the properties defined in a single class.
      * Any method in the class "invariants" whose simple name begins with the
      * prefix "prop_" will be considered as a test property, and will be executed
      * with arbitrary data, "timesForEach" times.
      */
     public void runOn(Class<?> invariants, int timesForEach) throws TestException {
         for (Method m : invariants.getMethods()) {
             if (m.getName().startsWith("prop_")) {
                 runOn(m, timesForEach);
             }
         }
     }
     
     /**
      * Run arbitrary data through a particular property. The property name
      * specified should be the full simple name of the method that represents 
      * the property (including the "prop_" prefix). For example:
      * 
      * <pre>runOn(QuickSortInvariants.class, "prop_idempotent", 10000);</pre>
      */
     public void runOn(Class<?> invariants, String propName, int times) throws TestException {
         Method[] ms = invariants.getDeclaredMethods();
         for (Method m : ms) {
             if (m.getName().equals(propName)) {
                 runOn(m, times);
             }
         }
     }
 
     /**
      * Similar to {@link #runOn(Class, String, int) except properties must be
      * specified by a reflected reference instead of a name.
      */
     public void runOn(Method prop, int times) throws TestException {
         runOn(prop, times, System.currentTimeMillis());
     }
     
     void runOn(Method prop, int times, long seed) throws TestException {
         System.out.print("Running " + prop.getName() + " " + times + " times... ");
 
         Gen gen = new Gen();
         gen.setSeed(seed);
         
         for (int i=0; i<times; i++) {
             int paramCount = prop.getParameterTypes().length;
             Object[] params = new Object[paramCount];
 
             for (int pIdx=0; pIdx<paramCount; pIdx++) {
                 Class<?> paramT = prop.getParameterTypes()[pIdx];
                 params[pIdx] = gen.createArbitraryFor(paramT);
             }
 
             if (!runOn(prop, params)) {
                 System.out.println("\n! Failed on try " + (i+1) + " for params: ");
                 printParamList(params, System.out, "\t");
                 
                 if (!continuePropAfterFail) {
                     /* Record the partial completion */
                     recording.addTestEvent(prop, gen.getSeed(), times);
                     return;
                 }
                 
             } else if (printSuccessRuns) {
                 System.out.println("\n* Passed for params: ");
                 printParamList(params, System.out, "\t");
             }
         }
         
         /* Record the successful completion */
         recording.addTestEvent(prop, gen.getSeed(), times);
 
         System.out.println("success.");
     }
 
     private boolean runOn(Method prop, Object[] params) throws TestException {
         try {
             return (Boolean) prop.invoke(null, params);
         } catch (InvocationTargetException e) {
             throw new TestException(prop.getName() + " threw: " + e.getCause());
         } catch (IllegalAccessException e) {
             throw new TestException(e.toString());
         }
     }
 
     private void printParamList(Object[] ary, PrintStream out, String prefix) {
         for (int i=0; i<ary.length; i++) {
             out.println(prefix + (i+1) + ". " + ary[i]);
         }
     }
     
 }
