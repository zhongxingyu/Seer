 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.test;
 
 import us.exultant.ahs.util.*;
 import java.util.*;
 import java.util.concurrent.*;
 import org.slf4j.*;
 
 /**
  * <p>
  * Extend this class to produce unit testing systems. Like other unit-testing frameworks,
  * this class gives shorthand operations for asserting data matches expectations, and
  * passes errors and exceptions it encounters on to a logger.
  * <ul>
  * <li>WARN-level messages are emitted when an assertion fails.
  * <li>INFO-level messages are emitted at the beginning and end of every unit of the test.
  * <li>DEBUG-level messages are emitted when an assertion passes.
  * <li>ERROR-level messages are emitted if a fatal failure caused the entire case to fail
  * to complete.
  * </ul>
  * </p>
  * 
  * <p>
  * The fundamental unit of testing is a class, {@link Unit}. You specify one
  * {@link Unit#call()} method per unit, and that's where you run your test. You do
  * whatever you need in that method, and call {@code assert[whatever]} methods on whatever
  * data you want to test. Using a class instead of an annotated method approach taken by
  * other unit testing libraries has several advantages: it means control flow can be
  * specified by a simple List, and it means that if several tests want to have shared
  * state, you can express that quite precisely with class hierarchies, or reuse code
  * between some units without exposing it to the entire suite.
  * </p>
  * 
  * <p>
  * Logging is performed by SLF4J.
  * </p>
  * 
  * 
  * <h3>Control flow</h3>
  * 
  * <p>
  * You specify it. The {@link Unit}s in the List returned by the {@link #getUnits()}
  * method is what we'll run. No hokey annotations or reflection, the system just does
  * exactly what you tell it to with a plain old collection. The ordering of execution of
  * tests is clear and respected.
  * </p>
  * 
  * <p>
  * Normally, the entire {@link Unit#call()} method of the Unit will complete, and THEN
  * whether or not that Unit passed or failed will be formally concluded. In other words,
  * you can issue more than one {@code assert[whatever]} in a test Unit, and they will all
  * be executed even if the first one doesn't pass. When one Unit fails, the rest will
  * still be executed.
  * </p>
  * 
  * <p>
  * If you want a test to stop, there are a couple things you can do:
  * <ul>
  * <li>Invoke {@link Unit#breakUnitIfFailed()}. If any assertions made so far failed, the
  * Unit will now stop executing and be reported as failed. Other Units will still run
  * normally.
  * <li>Invoke {@link Unit#breakCaseIfFailed()}. If any assertions made so far failed, the
  * Unit will now stop executing and be reported as failed, AND all other Units will also
  * not be run!
  * <li>Throw an exception. This has pretty much the same effect as
  * {@link Unit#breakCaseIfFailed()} &mdash; this Unit will be reported as failed, and all
  * other Units after this one will not be run. (Unless of course the exception was
  * {@link Unit#expectExceptionType() expected}.)
  * <li>Invoke {@link Unit#breakUnit(String)}. Regardless of any assertions made so far,
  * the Unit will now stop executing and be reported as failed. Other Units will still run
  * normally.
  * <li>Invoke {@link Unit#breakCaseIfFailed()}. Regardless of any assertions made so far,
  * Unit will now stop executing and be reported as failed, AND all other Units will also
  * not be run!
  * </p>
  * 
  * <p>
  * Units of a test will not be multithreaded. If a project's tests benefit from
  * mulithreading, it is the designer that should declare this, and is not for the testing
  * system to presume. If you do want threading, see the notes under the Customizing
  * section.
  * </p>
  * 
  * <p>
  * At the end of test running, the vm will exit (i.e. {@link System#exit(int)} will be
 * invoked) with the codes described above. A forceful exit like this is performed rather
 * than merely returning and letting the calling code decide what to do because in tests
 * with systems that spawn threads I've found this behavior to be more pleasant and
 * reliable. You may override this if you like; see the notes under the Customizing
 * section.
  * </p>
  * 
  * 
  * <h3>Reporting</h3>
  * 
  * <p>
  * By default, you'll get a nice little string of JSON on stdout (and it should be the
  * only thing on stdout, unless some other part of the program you're testing dumped
  * there); that JSON contains simple pass/fail counts.
  * </p>
  * 
  * <p>
  * In order to be easily useful in shell scripts, the program will exit with a status code
  * of 0 if all tests passed; 4 if any tests failed; and 5 if any tests failed so hard that
  * not all of the case ran.
  * </p>
  * 
  * 
  * <h3>Customizing</h3>
  * 
  * <p>
  * To change the reporting performed on exit, override the {@link #succeeded()},
  * {@link #failed()}, and {@link #aborted()}. Or, to just replace the JSON string that
  * provides the default summary report, just override {@link #exitMessage()} (the other
  * three methods call that).
  * </p>
  * 
  * <p>
  * If you want to thread your tests: the whole class does happen to implement
  * {@link Runnable}. The only other caveat is that you'll also want to override the
  * {@link #succeeded()}, {@link #failed()}, and {@link #aborted()} methods to do whatever
  * data gathering you want rather than leaving them with their default behavior of exiting
  * the vm.
  * </p>
  * 
  * 
  * <h3>Why not JUnit? That's standard isn't it?</h3>
  * <p>
  * I believe JUnit is fundamentally flawed in several of its design choices.
  * <ul>
  * <li>Most significantly of these is that any one assert failing causes that entire block
  * of code to abort immediately. Often I find myself wanting to be able to make several
  * asserts to related variables at once, THEN deciding whether or not there's be a failure
  * &mdash; that way you can have the ability to see the value of more than one variable
  * and get a better picture of the state of the whole system when there's a failure, which
  * I think that is a vastly more productive way to operate than cutting off your report
  * the first time something stumbles.
  * <li>JUnit in command-line mode delays reporting of exceptions it catches and errors it
  * detects until normal program termination. This is ridiculous, as it actually severely
  * impedes the detection of errors serious enough to disrupt program flow out of normal
  * bounds that result in timely termination.
  * <li>JUnit presumes that your tests may be executed in any order. This is ridiculous,
  * and just not something I want to worry or wonder about when designing my tests.
  * <li>JUnit presumes that your tests may be executed <i>concurrently</i> in any order.
  * This is so ridiculous I don't even have words to describe my feelings, and just not
  * something I want to worry or wonder about when designing my tests.
  * </ul>
  * If you like JUnit, by all means, go for it and keep using what works for you. This is
  * just a different way of doing things; I build and ship this in a separate module than
  * the core of AHSlib for a reason, and every other part of AHSlib will work happily
  * without this code being available at runtime.
  * </p>
  * 
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public abstract class TestCase implements Runnable {
 	/**
 	 * Sets up a default logger based on the name of the current class.
 	 */
 	public TestCase() {
 		// funny story, by the way: java's inane "cannot refer to an instance method while explicitly invoking a constructor" rule prevents you from writing this constructor as a call to the other one.  but you can still do the same thing by copypastaing extra code.  -.- 
 		this.$log = LoggerFactory.getLogger(getClass());
 	}
 	
 	public TestCase(Logger $log) {
 		this.$log = $log;
 	}
 	
 	/**
 	 * By default, this will always attempt to call {@link System#exit(int)} at the
 	 * end of running tests, exiting with 0 if all tests pass, 4 if any units have
 	 * failed, and 5 if any unit failed catastrophically (i.e. the entire case was not
 	 * completed). This behavior and/or those values can be overridden by overriding
 	 * the {@link #succeeded()}, {@link #failed()}, and {@link #aborted()} methods,
 	 * respectively.
 	 */
 	public synchronized void run() {
 		List<Unit> $units = getUnits();	// list is assumed immutable on pain of death or idiocy
 		
 		$numUnits = $units.size();
 		$numUnitsRun = 0;
 		$numUnitsPassed = 0;
 		$numUnitsFailed = 0;
 		
 		for (int $i = 0; $i < $units.size(); $i++) {
 			Unit $unit = $units.get($i);
 			if ($unit == null) continue;
 			
 			try {
 				resetFailures();
 				
 				$log.info("TEST UNIT "+$unit.getName()+" STARTING...");
 				$numUnitsRun++;
 				$unit.call();
 				if ($unit.expectExceptionType() != null) {
 					$numUnitsFailed++;
 					$log.error("EXPECTED EXCEPTION; TEST CASE ABORTED.");
 					aborted();
 				}
 				if ($unitFailures == 0) {
 					$numUnitsPassed++;
 					$log.info("TEST UNIT "+$unit.getName()+" PASSED SUCCESSFULLY!\n");
 				} else {
 					$numUnitsFailed++;
 					$log.info("TEST UNIT "+$unit.getName()+" FAILED (WITH "+$unitFailures+" FAILURES)!\n");
 				}
 			} catch (AssertionFatal $e) {
 				$numUnitsFailed++;
 				$log.error("FATAL EXCEPTION; TEST CASE ABORTED.", $e);
 				aborted();
 				break;
 			} catch (AssertionFailed $e) {
 				$numUnitsFailed++;
 				$log.error("TEST UNIT "+$unit.getName()+" ABORTED.", $e);
 			} catch (Throwable $e) {
 				if ($unit.expectExceptionType() != null) {
 					// some kind of exception was expected.
 					if ($unit.expectExceptionType().isAssignableFrom($e.getClass())) {
 						// and it was this kind that was expected, so this is good.
 						$numUnitsPassed++;
 						assertInstanceOf($unit.expectExceptionType(), $e);	// generates a normal confirmation message
 						$log.info("TEST UNIT "+$unit.getName()+" PASSED SUCCESSFULLY!\n");
 					} else {
 						// and it wasn't this kind.  this represents fatal failure.
 						$numUnitsFailed++;
 						$log.error("FATAL EXCEPTION; TEST CASE ABORTED.", $e);
 						aborted();
 						break;
 					}
 				} else {
 					// no exception was expected.  any exception represents fatal failure.
 					$numUnitsFailed++;
 					$log.error("FATAL EXCEPTION; TEST CASE ABORTED.", $e);
 					aborted();
 					break;
 				}
 			}
 		}
 		
 		if ($numUnitsFailed > 0)
 			failed();
 		else
 			succeeded();
 	}
 	
 	
 	/**
 	 * <p>
 	 * Called when the entire test case finished with all units passing successfully.
 	 * Default behavior is printing {@link #exitMessage()} to stdout followed by
 	 * forceful termination of the program via {@link System#exit(int)} with an exit
 	 * code of 0.
 	 * </p>
 	 */
 	protected void succeeded() {
 		System.out.println(exitMessage());
 		System.exit(0);
 	}
 	
 	/**
 	 * <p>
 	 * Called when the entire test case finished, but at least one unit did not pass
 	 * successfully. Default behavior is printing {@link #exitMessage()} to stdout
 	 * followed by forceful termination of the program via {@link System#exit(int)}
 	 * with an exit code of 4.
 	 * </p>
 	 */
 	protected void failed() {
 		System.out.println(exitMessage());
 		System.exit(4);
 	}
 	
 	/**
 	 * <p>
 	 * Called when the entire test case is aborted (i.e. a unit throws an unexpected
 	 * exception or AssertionFatal). Default behavior is printing
 	 * {@link #exitMessage()} to stdout followed by forceful termination of the
 	 * program via {@link System#exit(int)} with an exit code of 5.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note that the entire test case is <b>not</b> considered aborted when a single
 	 * unit of the case fails or or aborted, and as such this method will not be
 	 * called in that situation ({@link #failed()} will be).
 	 * </p>
 	 */
 	protected void aborted() {
 		System.out.println(exitMessage());
 		System.exit(5);
 	}
 	
 	protected String exitMessage() {
 		return "{\"#\":\"TESTCASE\",\n"+
 		"         \"numUnits\":"+$numUnits+",\n"+
 		"      \"numUnitsRun\":"+$numUnitsRun+",\n"+
 		"   \"numUnitsPassed\":"+$numUnitsPassed+",\n"+
 		"   \"numUnitsFailed\":"+$numUnitsFailed+"\n"+
 		"}";
 	}
 	
 	protected final Logger	$log;
 	private int		$unitFailures;
 	private int		$numUnits;
 	private int		$numUnitsRun;
 	private int		$numUnitsPassed;
 	private int		$numUnitsFailed;
 	
 	public abstract List<Unit> getUnits();
 	
 	
 
 	/**
 	 * <p>
 	 * Each Unit in a TestCase contains a coherent set of assertions (or just one
 	 * assertion) preceded by code to set up the test. The class name of an instance
 	 * of Unit is used when logging the successful passing of a Unit and so use of
 	 * anonymous subclasses of Unit is not advised.
 	 * </p>
 	 * 
 	 * <p>
 	 * Any object returned by the {@link #call()} method is ignored by TestCase, so
 	 * it's typically appropriate to act as if Unit actually implemented
 	 * <tt>Callable&lt;{@link Void}&gt;</tt>. (The return type of Object is allowed in
 	 * case the client cares to compose their units in odd ways, but doing so is not
 	 * recommended.)
 	 * </p>
 	 */
 	public abstract class Unit implements Callable<Object> {
 		/**
 		 * If this returns null, any exception thrown from the {@link #call()}
 		 * method results in failure of the Unit and aborting of all further Units
 		 * in the entire Case. Otherwise, if this method is overriden to return a
 		 * type, an exception <i>must</i> be thrown from the call method that is
 		 * instanceof that type, or the Unit fails and all further Units in the
 		 * entire Case are aborted.
 		 */
 		public <$T extends Throwable> Class<$T> expectExceptionType() { return null; }
 		// this method often seems to cause warnings about unchecked conversion in subclasses even when the return type is obviously legitimate, but i'm unsure of why.
 		
 		public void breakUnitIfFailed() throws AssertionFailed {
 			if ($unitFailures > 0) throw new AssertionFailed("breaking: "+$unitFailures+" failures.");
 		}
 		public void breakCaseIfFailed() throws AssertionFatal {
 			if ($unitFailures > 0) throw new AssertionFatal("breaking case: "+$unitFailures+" failures.");
 		}
 
 		public void breakUnit(String $message) throws AssertionFailed {
 			throw new AssertionFailed("breaking: "+$message);
 		}
 		public void breakCase(String $message) throws AssertionFatal {
 			throw new AssertionFatal("breaking case: "+$message);
 		}
 		
 		public String getName() {
 			String[] $arrg = Primitives.Patterns.DOT.split(getClass().getCanonicalName());
 			return $arrg[$arrg.length-1];
 		}
 	}
 	
 	
 	
 	protected void resetFailures() {
 		$unitFailures = 0;
 	}
 	
 	// using autoboxing on primitives as much as these message functions do bothers me but it does save me a helluva lot of lines of code here and i am assuming you're not using any assertions inside of terribly tight loops (or if you are, you're eiter not using confirmation or not failing hundreds of thousands of times).
 	// it might be a clarity enhancement to do quotation marks around the actual and expected values depending on type, though, which i don't do right now.
 	static String messageFail(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion failed -- expected " + $expected + " != " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" failed -- expected " + $expected + " != " + $actual + " actual.";
 	}
 	static String messagePass(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion passed -- expected " + $expected + " == " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" passed -- expected " + $expected + " == " + $actual + " actual.";
 	}	// i'm not recycling code in the above two because i think someday i might do some alignment stuff, in which case the above become more complicated cases.
 	static String messageFailNot(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion failed -- unexpected " + $expected + " == " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" failed -- unexpected " + $expected + " == " + $actual + " actual.";
 	}
 	static String messagePassNot(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion passed -- expected " + $expected + " != " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" passed -- expected " + $expected + " != " + $actual + " actual.";
 	}
 	static String messageFail(String $label, String $message) {
 		if ($label == null)
 			return "assertion failed -- " + $message;
 		else
 			return "assertion \"" + $label + "\" failed -- " +$message;
 	}
 	static String messagePass(String $label, String $message) {
 		if ($label == null)
 			return "assertion passed -- " + $message;
 		else
 			return "assertion \"" + $label + "\" passed -- " + $message;
 	}
 	// note that failure messages get wrapped in exceptions and then given to the logger (with a constant message of "assertion failed")
 	//  whereas success messages get passed to the logger as actual messages (with no exception attached).
 	//   this... might be a poor inconsistency, since i could see wanting to be able to report line numbers of successes outloud as well. 
 	
 	
 	
 	
 	
 	////////////////
 	//  BOOLEAN
 	////////////////
 	public boolean assertTrue(boolean $bool) {
 		return assertEquals(null, true, $bool);
 	}
 	public boolean assertFalse(boolean $bool) {
 		return assertEquals(null, false, $bool);
 	}
 	public boolean assertEquals(boolean $expected, boolean $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertTrue(String $label, boolean $bool) {
 		return assertEquals($label, true, $bool);
 	}
 	public boolean assertFalse(String $label, boolean $bool) {
 		return assertEquals($label, false, $bool);
 	}
 	public boolean assertEquals(String $label, boolean $expected, boolean $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(messageFail($label, $expected, $actual), new AssertionFailed());
 			return false;
 		}
 		$log.debug(messagePass($label, $expected, $actual));
 		return true;
 	}
 	
 	
 	////////////////
 	//  Object
 	////////////////
 	public boolean assertSame(Object $expected, Object $actual) {
 		return assertSame(null, $expected, $actual);
 	}
 	public boolean assertSame(String $label, Object $expected, Object $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(messageFail($label, $expected, $actual), new AssertionFailed());
 			return false;
 		}
 		$log.debug(messagePass($label, $expected, $actual));
 		return true;
 	}
 	public boolean assertNotSame(Object $expected, Object $actual) {
 		return assertNotSame(null, $expected, $actual);
 	}
 	public boolean assertNotSame(String $label, Object $expected, Object $actual) {
 		if ($expected == $actual) {
 			$unitFailures++;
 			$log.warn(messageFailNot($label, $expected, $actual), new AssertionFailed());
 			return false;
 		}
 		$log.debug(messagePassNot($label, $expected, $actual));
 		return true;
 	}
 	public boolean assertNull(Object $actual) {
 		return assertSame(null, null, $actual);
 	}
 	public boolean assertNull(String $label, Object $actual) {
 		return assertSame($label, null, $actual);
 	}
 	public boolean assertNotNull(Object $actual) {
 		return assertNotSame(null, null, $actual);
 	}
 	public boolean assertNotNull(String $label, Object $actual) {
 		return assertNotSame($label, null, $actual);
 	}
 	public boolean assertEquals(Object $expected, Object $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, Object $expected, Object $actual) {
 		if (!assertEqualsHelper($expected, $actual)) {
 			$unitFailures++;
 			$log.warn(messageFail($label, $expected, $actual), new AssertionFailed());
 			return false;
 		}
 		$log.debug(messagePass($label, $expected, $actual));
 		return true;
 	}
 	private boolean assertEqualsHelper(Object $expected, Object $actual) {
 		if ($expected == null) return ($actual == null);
 		return $expected.equals($actual);
 	}
 	public boolean assertInstanceOf(Class<?> $klass, Object $obj) {
 		return assertInstanceOf(null, $klass, $obj);
 	}
 	public boolean assertInstanceOf(String $label, Class<?> $klass, Object $obj) {
 		if ($obj == null) {
 			$unitFailures++;
 			$log.warn(messageFail($label, "null is never an instance of anything, and certainly not "+$klass+"."), new AssertionFailed());
 			return false;
 		}
 		try {
 			$klass.cast($obj);
 			$log.debug(messagePass($label, "\""+$obj.getClass().getCanonicalName()+"\" is an instance of \""+$klass.getCanonicalName()+"\""));
 			return true;
 		} catch (ClassCastException $e) {
 			$unitFailures++;
 			$log.warn(messageFail($label, $e.getMessage()+"."), new AssertionFailed());
 			return false;
 		}
 	}
 	
 	
 	////////////////
 	//  String
 	////////////////
 	// there's not actually a dang thing special about these, i just want the api itself to reassure developers that yes, strings can be asserted on and nothing weird happens.
 	public boolean assertEquals(String $expected, String $actual) {
 		return assertEquals(null, (Object)$expected, (Object)$actual);
 	}
 	public boolean assertEquals(String $label, String $expected, String $actual) {
 		return assertEquals($label, (Object)$expected, (Object)$actual);
 	}
 	
 	
 	////////////////
 	//  INT
 	////////////////
 	public boolean assertEquals(int $expected, int $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, int $expected, int $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(messageFail($label, $expected, $actual), new AssertionFailed());
 			return false;
 		}
 		$log.debug(messagePass($label, $expected, $actual));
 		return true;
 	}
 	
 	
 	////////////////
 	//  LONG
 	////////////////
 	public boolean assertEquals(long $expected, long $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, long $expected, long $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(messageFail($label, $expected, $actual), new AssertionFailed());
 			return false;
 		}
 		$log.debug(messagePass($label, $expected, $actual));
 		return true;
 	}
 	
 	
 	////////////////
 	//  BYTE
 	////////////////
 	public boolean assertEquals(byte[] $expected, byte[] $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, byte[] $expected, byte[] $actual) {
 		return assertEquals($label, Strings.toHex($expected), Strings.toHex($actual));
 	}
 	
 	////////////////
 	//  CHAR
 	////////////////
 	public boolean assertEquals(char[] $expected, char[] $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, char[] $expected, char[] $actual) {
 		return assertEquals($label, Arr.toString($expected), Arr.toString($actual));
 	}
 	
 	
 	
 	
 	
 	private static class AssertionFailed extends Error {
 		public AssertionFailed() { super(); }
 		public AssertionFailed(String $arg0) { super($arg0); }
 		public AssertionFailed(Throwable $arg0) { super($arg0); }
 		public AssertionFailed(String $arg0, Throwable $arg1) { super($arg0, $arg1); }
 	}
 	private static class AssertionFatal extends AssertionFailed {
 		public AssertionFatal() { super(); }
 		public AssertionFatal(String $arg0) { super($arg0); }
 		public AssertionFatal(Throwable $arg0) { super($arg0); }
 		public AssertionFatal(String $arg0, Throwable $arg1) { super($arg0, $arg1); }
 	}
 	
 	// Note!  You can not make methods like:
 	//	assertNotEquals(byte[] $a, byte[] $b) {
 	//		return !assertEquals($a, $b);
 	// because they'll still do the failure count and the log messages backwards inside.
 	
 	//future work:
 	//   i think it should be more or less possible to provide an interface to retrofit ahs TestCase to JUnit, which would be handy for folks that like the ability integrate JUnit with eclipse plugins or the like.
 }
