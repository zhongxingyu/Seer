 package org.jackie.utils;
 
 /**
  * @author Patrik Beno
  */
 @SuppressWarnings({"ThrowableInstanceNeverThrown", "unchecked"})
 public class Assert {
 
 	static public void doAssert(boolean condition, String msg, Object... args) {
 		if (!condition) {
 			throw new AssertionError(msg, args);
 		}
 	}
 
 	static public AssertionError assertFailed(Throwable thrown) {
 		return new AssertionError(thrown);
 	}
 
 	static public AssertionError assertFailed(final Throwable thrown, String msg, Object... args) {
 		return new AssertionError(thrown, msg, args);
 	}
 
 	// todo optimize; make this effectivelly inlinable
 	static public <T> T typecast(Object o, Class<T> expected) {
 		if (o == null) {
 			return null;
 		}
 
 		if (!expected.isAssignableFrom(o.getClass())) {
 			throw new ClassCastException(String.format(
 					"Incompatible types. Expected: %s, found: %s",
 					expected.getName(), o.getClass().getName()));
 		}
 		return (T) o;
 	}
 
 	static public AssertionError notYetImplemented() {
 		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
 		return new AssertionError("Not Yet Implemented! %s", ste);
 	}
 
 	static public UnsupportedOperationException unsupported() {
 		return unsupported("");
 	}
 
 	static public UnsupportedOperationException unsupported(String msg, Object ... args) {
 		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
 		return new UnsupportedOperationException(String.format("%s : %s", ste.toString(), String.format(msg, args)));
 	}
 
 	static public AssertionError unexpected(Throwable thrown) {
		return new AssertionError(thrown, "Unexpected: %s", thrown.getClass());
 	}
 
 	static public AssertionError notYetHandled(Throwable t) {
 		return new AssertionError(t, "Not Yet Handled: %s", t.getClass().getName());
 	}
 
 	static public void logNotYetHandled(Throwable thrown) {
 		Log.warn("Not Yet Handled: at %s", thrown);		
 	}
 
 	static public AssertionError invariantFailed(Throwable thrown, String msg, Object ... args) {
 		return new AssertionError(thrown, msg, args);
 	}
 
 	static public AssertionError invariantFailed(String msg, Object ... args) {
 		return new AssertionError(String.format(msg, args));
 	}
 
 	static public AssertionError invariantFailed(Enum e) {
 		return new AssertionError("Unexpected enum value: %s.%s", e.getClass().getName(), e.name());
 	}
 
 	static public void notNull(Object obj) {
 		if (obj == null) {
 			throw new AssertionError("Unexpected NULL");
 		}
 	}
 
 	static public <T> T NOTNULL(T t, String message, Object ... args) {
 		if (t == null) {
 			throw new AssertionError("Unexpected NULL: %s", String.format(message, args));
 		}
 		return t;
 	}
 
 	static public <T> T NOTNULL(T t) {
 		if (t == null) {
 			throw new AssertionError("Unexpected NULL");
 		}
 		return t;
 	}
 
 	static public void logNotYetImplemented() {
 		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
 		Log.warn("Not Yet Implemented: at %s", ste);
 	}
 
 	static public void expected(Object expected, Object found, String msg, Object ... args) {
 		if (expected != null && expected.equals(found) || expected == null && found == null) { return; }
 
 		throw assertFailed(null, "Expected [%s], found [%s]. %s", expected, found, String.format(msg, args));
 	}
 
 	static public boolean NOT(boolean expression) {
 		return !expression;
 	}
 
 	static class AssertionError extends java.lang.AssertionError {
 		AssertionError(String message, Object ... args) {
 			this(null, message, args);
 		}
 		AssertionError(Throwable thrown) {
 			this(thrown, thrown.getClass().getSimpleName());
 		}
 		AssertionError(Throwable cause, String message, Object ... args) {
 			super(String.format(message, args));
 			initCause(cause);
 		}
 	}
 
 }
