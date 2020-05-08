 package org.jackie.utils;
 
 /**
  * @author Patrik Beno
  */
 @SuppressWarnings({"ThrowableInstanceNeverThrown", "unchecked"})
 public class Assert {
 
 	static public void doAssert(boolean condition, String msg, Object... args) {
 		if (!condition) {
 			throw new AssertionError(String.format(msg, args));
 		}
 	}
 
 	static public AssertionError assertFailed(Throwable thrown) {
 		return new AssertionError(thrown);
 	}
 
 	static public AssertionError assertFailed(final Throwable thrown, String msg, Object... args) {
 		return new AssertionError(String.format(msg, args)) {{
 				initCause(thrown);
 		}};
 	}
 
 	static public <T> T typecast(Object o, Class<T> expected) {
 		if (o == null) {
 			return null;
 		}
 
 		if (!expected.isAssignableFrom(o.getClass())) {
 			throw new ClassCastException(String.format(
 					"Incompatible types: Expected %s, found: %s",
 					expected.getName(), o.getClass().getName()));
 		}
 		return (T) o;
 	}
 
 	static public AssertionError notYetImplemented() {
 		return new AssertionError("Not Yet Implemented!");
 	}
 
 	static public UnsupportedOperationException unsupported() {
 		return new UnsupportedOperationException();
 	}
 
 	static public AssertionError notYetHandled(final Throwable t) {
 		return new AssertionError(String.format("Not Yet Handled: %s", t.getClass().getName())) {{
 			initCause(t);
 		}};
 	}
 
 	static public AssertionError invariantFailed(String msg, Object ... args) {
 		return new AssertionError(String.format(msg, args));
 	}
 
 	static public AssertionError invariantFailed(Enum e) {
 		return new AssertionError(
 				String.format("Unexpected enum value: %s.%s", e.getClass().getName(), e.name()));
 	}
 
 	static public void notNull(Object obj) {
 		if (obj == null) {
 			throw new AssertionError("Unexpected NULL");
 		}
 	}
 
 	static public void logNotYetImplemented() {
 		Log.warn("%s", notYetImplemented());
 	}
 
 	static public void expected(Object expected, Object found, String msg, Object ... args) {
		if (expected != null && expected.equals(found) || expected == null && found == null) { return; }
 
 		throw assertFailed(null, "Expected %s, found %s. %s", expected, found, String.format(msg, args));
 	}
 }
