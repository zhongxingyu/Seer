 import java.util.List;
 import java.math.BigDecimal;
 
 // this is a runtime exception so users of the test framework don't have to
 // declare the exception for each and every test, which would be very tedious
 class AssertException extends RuntimeException {
  static final long serialVersionUID = 1;
   AssertException(String msg) {
     super(msg);
   }
 }
 
 // this is the base class for all tests which provides useful utility functions
 public class AbstractTest {
   void assertEqual(Object a, Object b) {
     if(a == null && b == null) return;
     if(a == null || !a.equals(b)) throw new AssertException(String.format("%s wasn't equal to %s", a, b));
   }
 
   void assertEqual(BigDecimal a, BigDecimal b) {
     if(a == null && b == null) return;
     if(a == null || a.compareTo(b) != 0) throw new AssertException(String.format("%s wasn't equal to %s", a, b));
   }
 
   void assertNotEqual(Object a, Object b) {
     if(a == null && b != null) return;
     if(a == null || a.equals(b)) throw new AssertException(String.format("%s was equal to %s", a, b));
   }
 
   void assertIs(Object a, Object b) {
     if(a != b) throw new AssertException(String.format("%s isn't %s (==)", a, b));
   }
 
   void assertIsNot(Object a, Object b) {
     if(a == b) throw new AssertException(String.format("%s isn't %s (==)", a, b));
   }
 
   <T> void assertIn(T a, List<T> b) {
     if(! b.contains(a)) throw new AssertException(String.format("%s is not in %s", a, b));
   }
 
   <T> void assertNotIn(T a, List<T> b) {
     if(b.contains(a)) throw new AssertException(String.format("%s is in %s", a, b));
   }
 
   void assertTrue(boolean cond) {
     if(!cond) throw new AssertException("assertion failed");
   }
 
   void assertIsInstance(Object a, Class cls) {
     if(!cls.isInstance(a)) throw new AssertException(
       String.format("%s is not an instance of %s", a, cls.getName()));
   }
 
   void assertIsNotInstance(Object a, Class cls) {
     if(cls.isInstance(a)) throw new AssertException(
       String.format("%s is an instance of %s", a, cls.getName()));
   }
 }
