 package ahs.test;
 
 import ahs.log.*;
 import ahs.util.*;
 
 import junit.framework.*;
 
 /**
  * <p>
  * I believe JUnit is fundamentally flawed in several of its design choices. Most
  * significantly of these is that it delays reporting of exceptions it catches and errors
  * it detects until normal program termination. This is ridiculous, as it actually
  * severely impedes the detection of errors serious enough to disrupt program flow out of
  * normal bounds that result in timely termination.
  * </p>
  * 
  * <p>
  * Behold, a replacement: this class simply passes errors and exceptions it encounters on
  * to a logger. Exceptions that bubble out of a test stop that test and all subsequent
  * tests; asserts failed do not.
  * </p>
  * 
  * <p>
  * If "confirmation" is enabled, all assertions with a message will emit an INFO-level
  * message when the assertion passes correctly.
  * </p>
  * 
  * @author hash
  * 
  */
 public abstract class TestCase implements Runnable {
 	public TestCase(Logger $log, boolean $enableConfirmation) {
 		this.$log = $log;
 		this.$confirm = $enableConfirmation;
 //		resetFailures();
 	}
 	
 	public void run() {
 		try {
 			runTests();
 		} catch (Exception $e) {
 			$log.error(this.getClass(), "fatal exception",  $e);
 		}
 	}
 	
 	private final Logger		$log;
 	private boolean			$confirm;
 	private static final String	DEF	= "assertion failed";
 	private int			$failures;
 	
 	protected abstract void runTests() throws Exception;
 	
 	
 	
 	
 	
 	////////////////
 	//  BOOLEAN
 	////////////////
 	public boolean assertTrue(boolean $bool) {
 		if (!$bool) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected true != false actual."));
 			return false;
 		} return true;
 	}
 	public boolean assertFalse(boolean $bool) {
 		if ($bool) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected true != false actual."));
 			return false;
 		} return true;
 	}
 	public boolean assertEquals(boolean $expected, boolean $actual) {
 		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		} return true;
 	}
 	public boolean assertEquals(String $message, boolean $expected, boolean $actual) {
 		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion \""+$message+"\" failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		}
 		if ($confirm) $log.info(this.getClass(), "assertion \"" + $message + "\" passed -- expected " + $expected + " == " + $actual + " actual.");
 		return true;
 	}
 	
 	
 	////////////////
 	//  Object
 	////////////////
 	public boolean assertNull(Object $x) {
 		if ($x != null) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected null != "+$x+" actual."));
 			return false;
 		} return true;
 	}
 	
 	public boolean assertEquals(Object $expected, Object $actual) {
		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		} return true;
 	}
 	public boolean assertEquals(String $message, Object $expected, Object $actual) {
		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion \""+$message+"\" failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		}
 		if ($confirm) $log.info(this.getClass(), "assertion \"" + $message + "\" passed -- expected " + $expected + " == " + $actual + " actual.");
 		return true;
 	}
 	
 	public boolean assertInstanceOf(Class<?> $klass, Object $obj) {
 		if ($obj == null) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- null is never an instance of anything, and certainly not "+$klass+"."));
 			return false;
 		}
 		try {
 			$klass.cast($obj);
 			return true;
 		} catch (ClassCastException $e) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- "+$e.getMessage()+"."));
 			return false;
 		}
 	}
 	public boolean assertInstanceOf(String $message, Class<?> $klass, Object $obj) {
 		if ($obj == null) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion \""+$message+"\" failed -- null is never an instance of anything, and certainly not "+$klass+"."));
 			return false;
 		}
 		try {
 			$klass.cast($obj);
 			return true;
 		} catch (ClassCastException $e) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion \""+$message+"\" failed -- "+$e.getMessage()+"."));
 			return false;
 		}
 	}
 	
 	
 	////////////////
 	//  String
 	////////////////
 	public boolean assertEquals(String $expected, String $actual) {
		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		} return true;
 	}
 	public boolean assertEquals(String $message, String $expected, String $actual) {
		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion \""+$message+"\" failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		}
 		if ($confirm) $log.info(this.getClass(), "assertion \"" + $message + "\" passed -- expected " + $expected + " == " + $actual + " actual.");
 		return true;
 	}
 	
 	
 	////////////////
 	//  INT
 	////////////////
 	public boolean assertEquals(int $expected, int $actual) {
 		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		} return true;
 	}
 	public boolean assertEquals(String $message, int $expected, int $actual) {
 		if ($expected != $actual) {
 			$failures++;
 			$log.error(this.getClass(), DEF, new AssertionFailedError("assertion \""+$message+"\" failed -- expected "+$expected+" != "+$actual+" actual."));
 			return false;
 		}
 		if ($confirm) $log.info(this.getClass(), "assertion \"" + $message + "\" passed -- expected " + $expected + " == " + $actual + " actual.");
 		return true;
 	}
 	
 	
 	////////////////
 	//  BYTE
 	////////////////
 	public boolean assertEquals(byte[] $a, byte[] $b) {
 		return assertEquals(Arr.toString($a),Arr.toString($b));
 	}
 	
 	public boolean assertNotEquals(byte[] $a, byte[] $b) {
 		return !assertEquals($a, $b);
 	}
 	
 	
 	
 	public boolean assertEquals(char[] $a, char[] $b) {
 		return assertEquals(Arr.toString($a),Arr.toString($b));
 	}
 	
 	
 	
 	//future work:
 	//   i'd like to implement a breakIfFailed method that throws a Failure exception if any the assertions made within this case (i.e. method) have failed.
 	//   the point here is to provide a balance between making it convenient to get readouts of assertions on multiple states, but also to quit when the developer deems that things have gone wrong enough.
 	//
 	// of course, the problem here is that our current interface doesn't actually give this class knowledge of where cases in a unit begin and end, so it's kinda hard to make counters set and reset sanely.
 	//
 //	public void breakIfFailed() throws AssertionFailedError {
 //		if ($failures > 0) throw new AssertionFailedError("breaking: "+$failures+" failures.");
 //	}
 //	private void resetFailures() {
 //		$failures = 0;
 //	}
 	
 	
 	
 	private static class AssertionFailedError extends Error {
 		public AssertionFailedError() { super(); }
 		public AssertionFailedError(String $arg0) { super($arg0); }
 		public AssertionFailedError(Throwable $arg0) { super($arg0); }
 		public AssertionFailedError(String $arg0, Throwable $arg1) { super($arg0, $arg1); }
 	}
 }
