 package se.mockachino;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static se.mockachino.Mockachino.mock;
 import static se.mockachino.Mockachino.*;
 import static se.mockachino.matchers.Matchers.*;
 import static se.mockachino.Settings.*;
 
 public class MockClassTest {
     public static class TestClass {
         public TestClass(int a, String s) {
 
         }
     }
 
     private static class SecretClass {
         private SecretClass(int a, String s) {
 
         }
     }
 
     private static class ExceptionClass {
         public ExceptionClass() {
             throw new RuntimeException("Ouch");
         }
         public ExceptionClass(int a, String s) {
             throw new RuntimeException("Ouch");
         }
         public ExceptionClass(int a, String s, boolean b) {
         }
     }
 
     private static class BrokenClass {
         public BrokenClass() {
             throw new Error("Broken");
         }
     }
 
     @Test
     public void testMockClassWithParams() {
         TestClass mock = mock(TestClass.class);
         assertTrue(mock.toString().startsWith("Mock:TestClass:"));
     }
 
     @Test
     public void testMockPrivateClass() {
         SecretClass mock = mock(SecretClass.class);
         assertTrue(mock.toString().startsWith("Mock:SecretClass:"));
     }
 
     @Test
     public void testException() {
         ExceptionClass mock = mock(ExceptionClass.class);
         assertTrue(mock.toString().startsWith("Mock:ExceptionClass:"));
     }
 
     @Test
     public void testSimpleObjenesis() {
         BrokenClass mock = mock(BrokenClass.class);
         assertTrue(mock.toString().startsWith("Mock:BrokenClass:"));
     }

 }
