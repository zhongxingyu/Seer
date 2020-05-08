 package org.twodee.speccheck;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.junit.Assert;
 import org.junit.ComparisonFailure;
 import org.junit.Test;
 import org.junit.internal.ArrayComparisonFailure;
 import org.junit.runner.Description;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Request;
 import org.junit.runner.Result;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunListener;
 import org.twodee.speccheck.utilities.SpecCheckZipper;
 
 public class SpecChecker {
   private static final int MAX_SPECCHECK_TESTS_ORDER = 50;
   private static final String tag = "hw";
   private static final String[] filesToZip = {};
 
   public static void main(String[] args) {
     SpecCheck.testAsStudent(TestSuite.class);
   }
 
   public static class TestSuite {
     // --- GENERATED TESTS GO HERE ---
   }
 
   /**
    * A utility class for running JUnit tests and reporting any failures.
    * 
    * @author cjohnson
    */
   static class SpecCheck {
     /**
      * Uninstantiable.
      */
     private SpecCheck() {
     }
 
     /**
      * Run the JUnit tests in the specified SpecChecker class, customizing
      * output for grading.
      * 
      * @param tester
      * SpecChecker class, assumed to contain at least one JUnit test.
      */
     public static void testAsGrader(Class<?> tester) {
       runTestSuite(tester, false);
     }
 
     /**
      * Run the JUnit tests contained in the specified class, customizing output
      * for the student working on an assignment.
      * 
      * @param tester
      * Class containing JUnit tests.
      */
     public static void testAsStudent(Class<?> tester) {
       SpecCheckTestResults results = runTestSuite(tester, true);
       if (results.hasSpecCheckTests() && results.isSpecCompliant() && filesToZip.length > 0) {
         SpecCheckZipper.zip(results.isPerfect(), tag, null, filesToZip);
       }
     }
 
     /**
      * Run the JUnit tests contained in the specified class, customizing output
      * according to the arguments.
      * 
      * @param tester
      * Class containing JUnit tests.
      * @param isVerbose
      * Whether or not to be wordy in diagnostic messages.
      * @return Message indicating how many tests passed.
      */
     private static SpecCheckTestResults runTestSuite(Class<?> tester,
                                                      boolean isVerbose) {
       try {
         return evaluateTests(tester, isVerbose);
       } catch (Error e) {
        return new SpecCheckTestResults("Tests couldn't be run. Did you add JUnit to your project?", 0, 0, 0, 0);
       }
     }
 
     /**
      * Run each @Test in the specified tester class and print the results.
      * 
      * @param tester
      * The class containing the JUnit tests.
      * 
      * @return A message indicating a score and how many tests pass.
      */
     public static SpecCheckTestResults evaluateTests(Class<?> tester,
                                                      boolean isVerbose) {
       JUnitCore core = new JUnitCore();
       Request request = Request.aClass(tester);
       request = request.sortWith(new SpecCheckTestComparator());
       SpecCheckRunListener listener = new SpecCheckRunListener(isVerbose);
       core.addListener(listener);
       core.run(request);
 
       return listener.getResults();
     }
   }
 
   @Retention(RetentionPolicy.RUNTIME)
   @interface SpecCheckTest {
     /**
      * The number of points this test contributes to the student's final score
      * if this test passes.
      */
     int nPoints() default 0;
 
     /**
      * An indicator of priority for this test. A lesser-numbered test runs
      * before a greater-numbered test. Pure interface tests should be given a
      * low number, like 0, so that they are executed first.
      * 
      * @return
      */
     int order() default MAX_SPECCHECK_TESTS_ORDER + 1;
   }
 
   /**
    * This class contains a number of helper methods for ripping apart JUnit data
    * types into usable pieces.
    * 
    * @author cjohnson
    */
   static class SpecCheckUtilities {
     /**
      * Gets the method identified by the given Description.
      * 
      * @param d
      * The description given by JUnit.
      * @return The method
      * @throws SecurityException
      * @throws NoSuchMethodException
      * @throws ClassNotFoundException
      */
     public static Method getMethod(Description d) throws SecurityException, NoSuchMethodException, ClassNotFoundException {
       return Class.forName(getClassName(d)).getDeclaredMethod(getMethodName(d));
     }
 
     /**
      * Gets the name of the class identified by the given Description.
      * 
      * @param d
      * The description given by JUnit.
      * @return The class name
      */
     public static String getClassName(Description d) {
       Matcher matcher = parseDescription(d);
       return matcher.matches() ? matcher.group(2) : d.toString();
     }
 
     /**
      * Gets the name of the method identified by the given Description.
      * 
      * @param d
      * The description given by JUnit.
      * @return The method name
      */
     public static String getMethodName(Description d) {
       Matcher matcher = parseDescription(d);
       if (matcher.matches()) {
         return matcher.group(1);
       } else {
         return null;
       }
     }
 
     /**
      * Parses the JUnit Description and holds the results in a Matcher object.
      * 
      * @param d
      * The description given by JUnit
      * @return A Matcher holding the method and class names.
      */
     private static Matcher parseDescription(Description d) {
       // The description has the form methodName(className).
       return Pattern.compile("(.*)\\((.*)\\)").matcher(d.toString());
     }
 
     /**
      * Gets a description of how the modifiers differ from what was expected/
      * 
      * @param expected
      * The expected modifiers
      * @param actual
      * The actual modifiers observed
      * @return A short description how actual differs from expected
      */
     public static String getModifierDifference(int expected,
                                                int actual) {
       String msg = "";
 
       if (Modifier.isStatic(expected) != Modifier.isStatic(actual)) {
         msg += "It should " + (Modifier.isStatic(actual) ? "not " : "") + "be static. ";
       }
 
       if (Modifier.isPublic(expected) != Modifier.isPublic(actual)) {
         msg += "It should " + (Modifier.isPublic(actual) ? "not " : "") + "be public. ";
       }
 
       if (Modifier.isProtected(expected) != Modifier.isProtected(actual)) {
         msg += "It should " + (Modifier.isProtected(actual) ? "not " : "") + "be protected. ";
       }
 
       if (Modifier.isPrivate(expected) != Modifier.isPrivate(actual)) {
         msg += "It should " + (Modifier.isPrivate(actual) ? "not " : "") + "be private. ";
       }
 
       if (Modifier.isFinal(expected) != Modifier.isFinal(actual)) {
         msg += "It should " + (Modifier.isFinal(actual) ? "not " : "") + "be final. ";
       }
 
       // For interfaces, we don't care if they are marked abstract. TODO: is
       // there
       // a better way to express this?
       if (Modifier.isInterface(expected) != Modifier.isInterface(actual)) {
         msg += "It should " + (Modifier.isInterface(actual) ? "not " : "") + "be an interface. ";
       } else if (Modifier.isAbstract(expected) != Modifier.isAbstract(actual)) {
         msg += "It should " + (Modifier.isAbstract(actual) ? "not " : "") + "be abstract. ";
       }
 
       return msg;
     }
 
     /**
      * Get a comma-separated sequence of stringified class literals (i.e,
      * "String.class, boolean.class") corresponding to the given array of class
      * objects.
      * 
      * @param types
      * Ordered list of classes for which a comma-separated String is created.
      * 
      * @return The comma-separated list of classes.
      */
     public static String getTypesList(Class<?>[] types) {
       String list = "";
       if (types.length > 0) {
         list += types[0].getName() + ".class";
         for (int i = 1; i < types.length; ++i) {
           list += ", " + types[i].getName() + ".class";
         }
       }
       return list;
     }
   }
 
   static class SpecCheckTestResults {
     private String message;
     private int nTestsPassed;
     private int nTests;
     private int nSpecCheckTestsPassed;
     private int nSpecCheckTests;
 
     public SpecCheckTestResults(String message,
                                 int nTestsPassed,
                                 int nTests,
                                 int nSpecCheckTestsPassed,
                                 int nSpecCheckTests) {
       this.message = message;
       this.nTestsPassed = nTestsPassed;
       this.nTests = nTests;
       this.nSpecCheckTestsPassed = nSpecCheckTestsPassed;
       this.nSpecCheckTests = nSpecCheckTests;
     }
 
     public int getSpecCheckTestsCount() {
       return nSpecCheckTests;
     }
 
     public int getSpecCheckTestsPassedCount() {
       return nSpecCheckTestsPassed;
     }
 
     public String getMessage() {
       return message;
     }
 
     public int getPassedCount() {
       return nTestsPassed;
     }
 
     public int getTestCount() {
       return nTests;
     }
 
     public boolean isPerfect() {
       return nTests == nTestsPassed;
     }
 
     public boolean hasSpecCheckTests() {
       return nSpecCheckTests > 0;
     }
 
     public boolean isSpecCompliant() {
       return nSpecCheckTests == nSpecCheckTestsPassed;
     }
   }
 
   /**
    * A custom JUnit RunListener. We offer our own so that we can optionally add
    * points to a student's score when tests pass.
    * 
    * @author cjohnson
    */
   static class SpecCheckRunListener extends RunListener {
     /** A map from a test to points earned for it */
     private HashMap<Description, Integer> testToPoints;
 
     /** The total number of points possible in this test suite */
     private int nPointsPossible;
 
     private boolean isVerbose;
 
     private PrintStream oldOut;
 
     private int nSpecCheckTests;
     private int nSpecCheckTestsFailed;
 
     private SpecCheckTestResults results;
 
     public SpecCheckRunListener(boolean isVerbose) {
       this.isVerbose = isVerbose;
 
     }
 
     @Override
     public void testRunStarted(Description description) throws Exception {
       super.testRunStarted(description);
       testToPoints = new HashMap<Description, Integer>();
       nPointsPossible = 0;
       nSpecCheckTests = 0;
       nSpecCheckTestsFailed = 0;
       oldOut = null;
 
       if (!isVerbose) {
         oldOut = System.out;
         System.setOut(new PrintStream(new OutputStream() {
           public void write(int b) {
           }
         }));
       }
     }
 
     /**
      * Triggered when a new test has been started. If tagged with
      * 
      * @SpecCheckTest, the test's points are added to the total.
      */
     @Override
     public void testStarted(Description description) {
       try {
         Method m = SpecCheckUtilities.getMethod(description);
         SpecCheckTest anno = m.getAnnotation(SpecCheckTest.class);
         if (anno != null) {
           // I put the test/points in the map here, but it may be taken out
           // later if the test ends up failing.
           testToPoints.put(description, anno.nPoints());
           nPointsPossible += anno.nPoints();
           if (anno.order() <= MAX_SPECCHECK_TESTS_ORDER) {
             ++nSpecCheckTests;
           }
         }
       } catch (SecurityException e) {
         e.printStackTrace();
       } catch (NoSuchMethodException e) {
         e.printStackTrace();
       } catch (ClassNotFoundException e) {
         e.printStackTrace();
       }
     }
 
     /**
      * Triggered when a test fails.
      */
     @Override
     public void testFailure(Failure failure) {
       // Earlier we blindly added the test to the points map. Take it out now
       // because those points weren't earned.
       testToPoints.remove(failure.getDescription());
       try {
         Method m = SpecCheckUtilities.getMethod(failure.getDescription());
         SpecCheckTest anno = m.getAnnotation(SpecCheckTest.class);
         if (anno != null && anno.order() <= MAX_SPECCHECK_TESTS_ORDER) {
           ++nSpecCheckTestsFailed;
         }
       } catch (NoSuchMethodException e) {
         e.printStackTrace();
       } catch (ClassNotFoundException e) {
         e.printStackTrace();
       }
     }
 
     /**
      * Gets the number of points earned. This number is only meaningful after
      * the tests have been run.
      * 
      * @return Number of points earned.
      */
     private int getScore() {
       int score = 0;
 
       for (Integer nPoints : testToPoints.values()) {
         score += nPoints;
       }
 
       return score;
     }
 
     /**
      * Gets the number of points possible. This number is only meaningful after
      * the tests have been run.
      * 
      * @return Number of points possible.
      */
     private int getScorePossible() {
       return nPointsPossible;
     }
 
     @Override
     public void testRunFinished(Result result) throws Exception {
       super.testRunFinished(result);
 
       if (!isVerbose) {
         System.setOut(oldOut);
       }
 
       int nTests = result.getRunCount();
       int nTestsFailed = result.getFailureCount();
       final String wrapPattern = "(.{50,}?) ";
 
       String scoreMessage = String.format("%d out of %d tests pass.", nTests - nTestsFailed, nTests);
       if (getScorePossible() > 0) {
         scoreMessage = String.format("You received %d/%d points. %s", getScore(), getScorePossible(), scoreMessage);
       }
       System.out.printf("%s%n%n", scoreMessage);
 
       if (nTestsFailed > 0) {
         for (Failure f : result.getFailures()) {
           System.out.println("PROBLEM: ");
           if (!f.getException().getClass().equals(AssertionError.class) &&
               !f.getException().getClass().equals(ComparisonFailure.class) &&
               !f.getException().getClass().equals(ArrayComparisonFailure.class)) {
             System.out.println("Your code threw an exception, making it impossible to test. You'll have to sleuth out what caused it. Start by finding the line in *your* code where it was first thrown. Look in the following listing for the first reference to your code.".replaceAll(wrapPattern, "$1\n"));
             System.out.println(f.getException().getClass());
             f.getException().printStackTrace(System.out);
           } else {
             System.out.printf("%s%n", f.getException().getLocalizedMessage().replaceAll(wrapPattern, "$1\n"));
           }
           System.out.printf("%n");
         }
 
         System.out.println("If you do not fix these problems, you are deviating from the homework specification and may lose points.".replaceAll(wrapPattern, "$1\n"));
       }
 
       if (getScorePossible() != 0) {
         scoreMessage = "TOTAL: " + getScore() + "/" + getScorePossible();
       }
 
       results = new SpecCheckTestResults(scoreMessage, nTests - nTestsFailed, nTests, nSpecCheckTests - nSpecCheckTestsFailed, nSpecCheckTests);
     }
 
     public SpecCheckTestResults getResults() {
       return results;
     }
   }
 
   /**
    * We want pure interface tests to run before any optional functional tests.
    * To support this, @SpecCheckTest exposes an order parameter. Tests with a
    * lesser order are run before tests with a greater order. This comparator can
    * be used to order two tests. This class is unlikely to be used directly by
    * the instructor.
    * 
    * @author cjohnson
    */
   static class SpecCheckTestComparator implements Comparator<Description> {
     /**
      * Order two SpecCheckTests based on their order parameter.
      * 
      * @param a
      * One test.
      * @param b
      * The other test.
      * @return A negative value if the order of a is less than the order of b, 0
      * if they are the same, and a positive value otherwise.
      */
     @Override
     public int compare(Description a,
                        Description b) {
       Method m1 = null;
       Method m2 = null;
 
       try {
         m1 = SpecCheckUtilities.getMethod(a);
         m2 = SpecCheckUtilities.getMethod(b);
       } catch (SecurityException e) {
         e.printStackTrace();
       } catch (NoSuchMethodException e) {
         e.printStackTrace();
       } catch (ClassNotFoundException e) {
         e.printStackTrace();
       }
 
       SpecCheckTest a1 = m1.getAnnotation(SpecCheckTest.class);
       SpecCheckTest a2 = m2.getAnnotation(SpecCheckTest.class);
 
       if (a1 == null || a2 == null || a1.order() == a2.order()) {
         return 0;
       } else if (a1.order() < a2.order()) {
         return -1;
       } else {
         return 1;
       }
     }
   }
 }
