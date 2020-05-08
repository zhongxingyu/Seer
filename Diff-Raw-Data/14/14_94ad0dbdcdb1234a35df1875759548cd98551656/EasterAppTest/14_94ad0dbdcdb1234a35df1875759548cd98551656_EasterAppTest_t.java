 package org.wooddog.woodstub;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.wooddog.woodstub.core.WoodStub;
 import org.wooddog.woodstub.core.runtime.Stub;
 import org.wooddog.woodstub.core.runtime.StubFactory;
 import org.wooddog.woodstub.demo.EasterApp;
 
 import java.util.Date;
 
 import static org.junit.Assert.*;
 
 /**
  * Unit test for simple App.
  */
 public class EasterAppTest implements StubFactory, Stub {
     private boolean result;
     private EasterApp easterApp;
 
 
     @Before
     public void setup() {
         System.out.println("SETUP");
     }
 
 
     @Test
     public void testIs12MAJ2010Easter_No() {
        easterApp = new EasterApp();
        WoodStub.setStubFactory(this);

         this.result = false;
         assertFalse(easterApp.isEaster());
     }
 
     @Test
     public void testIs12MAJ2011Easter_Yes() {
        easterApp = new EasterApp();
        WoodStub.setStubFactory(this);

         this.result = true;
         assertTrue(easterApp.isEaster());
     }
 
 
     /**
      * Factory method called by WoodStub for creation of stubs to be used as replacement code for a given code.
      *
      * This method will be called whenever a method somewhere in the JVM is called, if the method should be stubbed the
      * this factory most return a stub Object, in case it shouldn't be stubbed it most return null.
      *
      * Class names is using the low level naming notation which is the fully qualified class name but with / instead of
      * dots.
      *
      * Notation used in the method descriptor is also based on the low level format, each type is a single character and
      * arrays is preceded with a [ for each dimension.
      *
      * sample:
      *   String subSequence(int from, int to)
      *
      * is represented as
      *   (II)Ljava/lang/String
      *
      *
      * base type table:
      *
      * Z   | boolean
      * B   | byte
      * C   | char
      * S   | short
      * I   | int
      * F   | float
      * J   | long
      * D   | double
      * L   | Object
      * V   | void
      * [*  | a 2d array, where the asterix represents one of the above one letter codes.
      * [[* | a 3d array, where the asterix represents one of the above one letter codes.
      *
      *
      * @param source The instance of the object to create a method stub for.
      * @param className The name of the class to create a method stub for.
      * @param methodName The name of the method to create a stub for.
      * @param methodDescriptor A string describing the parameters and return type for the method to be stubbed.
      *
      * @return A stub if the method should be stubbed otherwise null.
      */
     public Stub createStub(Object source, String className, String methodName, String methodDescriptor) {
         System.out.println("STUBBB");
         if ("(Ljava/util/Date;)Z".equals(methodDescriptor)) {
             return this;
         }
 
         return null;
     }
 
     /**
      * this method is invoked as the first one when the stubbed method is invoked.
      *
      * !NOTE PARAMETER NAMES IS ALWAYS NULL IN THIS VERSION!
      *
      * If the method takes no arguments the arrays will be zero sized.
      *
      * @param parameterNames    Contains an array of parameter names, ordered in the same sequence as in the stubbed
      *                          method.
      *
      * @param parameterValues   Contains an array of parameter values, ordered in the same sequence as in the stubbed
      *                          method.
      */
     public void setParameters(String[] parameterNames, Object[] parameterValues) {
     }
 
     /**
      * This method is invoked as the second one, and is meant for holding the assertion logic though not required.
      *
      * Throwing an exception that is not supported by the stubbed method will not directly lead to errors, however
      * it might come as a surprise for the calling method that the method it believes it is talking to suddenly starts
      * to throw new types of exceptions.
      *
      *
      * !NOTE PARAMETER NAMES IS ALWAYS NULL IN THIS VERSION!
      * !           WILL BE FIXED IN THE NEXT ONE           !
      *
      * If the method takes no arguments the arrays will be zero sized.
      */
     public void execute() throws Throwable {
     }
 
     /**
      * This is the last method which will be called, but only if the stubbed method isn't a void method.
      *
      * The returned Object type most always mach the return declaration of the stubbed method.
      *
      * primitive values most be wrapped in their respectively wrappers and most not be null.
      *
      * The Object returned will be parsed on to the calling class and the illusion is complete.
      *
      * @return The value to be returned.
      */
     public Object getResult() {
         return result;
     }
 }

