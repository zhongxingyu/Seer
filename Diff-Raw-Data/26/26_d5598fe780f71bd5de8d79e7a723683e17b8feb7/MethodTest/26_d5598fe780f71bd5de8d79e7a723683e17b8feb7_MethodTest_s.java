 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package libcore.java.lang.reflect;
 
 import java.lang.reflect.Method;
 import junit.framework.TestCase;
 
 public final class MethodTest extends TestCase {
     // Check that the VM gives useful detail messages.
     public void test_invokeExceptions() throws Exception {
         Method m = String.class.getMethod("charAt", int.class);
         try {
             m.invoke("hello"); // Wrong number of arguments.
             fail();
         } catch (IllegalArgumentException iae) {
             assertEquals("wrong number of arguments; expected 1, got 0", iae.getMessage());
         }
         try {
             m.invoke("hello", "world"); // Wrong type.
             fail();
         } catch (IllegalArgumentException iae) {
             assertEquals("argument 1 should have type int, got java.lang.String", iae.getMessage());
         }
         try {
             m.invoke("hello", (Object) null); // Null for a primitive argument.
             fail();
         } catch (IllegalArgumentException iae) {
             assertEquals("argument 1 should have type int, got null", iae.getMessage());
         }
         try {
             m.invoke(new Integer(5)); // Wrong type for 'this'.
             fail();
         } catch (IllegalArgumentException iae) {
            assertEquals("expected receiver of type java.lang.String, not java.lang.Integer", iae.getMessage());
         }
         try {
             m.invoke(null); // Null for 'this'.
             fail();
         } catch (NullPointerException npe) {
            assertEquals("expected receiver of type java.lang.String, not null", npe.getMessage());
         }
     }
 
     public void test_getExceptionTypes() throws Exception {
         Method method = MethodTestHelper.class.getMethod("m1", new Class[0]);
         Class[] exceptions = method.getExceptionTypes();
         assertEquals(1, exceptions.length);
         assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
         // Check that corrupting our array doesn't affect other callers.
         exceptions[0] = NullPointerException.class;
         exceptions = method.getExceptionTypes();
         assertEquals(1, exceptions.length);
         assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
     }
 
     public void test_getParameterTypes() throws Exception {
         Class[] expectedParameters = new Class[] { Object.class };
         Method method = MethodTestHelper.class.getMethod("m2", expectedParameters);
         Class[] parameters = method.getParameterTypes();
         assertEquals(1, parameters.length);
         assertEquals(expectedParameters[0], parameters[0]);
         // Check that corrupting our array doesn't affect other callers.
         parameters[0] = String.class;
         parameters = method.getParameterTypes();
         assertEquals(1, parameters.length);
         assertEquals(expectedParameters[0], parameters[0]);
     }
 
     public void testGetMethodWithPrivateMethodAndInterfaceMethod() throws Exception {
         assertEquals(InterfaceA.class, Sub.class.getMethod("a").getDeclaringClass());
     }
 
     public void testGetMethodReturnsIndirectlyImplementedInterface() throws Exception {
         assertEquals(InterfaceA.class, ImplementsC.class.getMethod("a").getDeclaringClass());
         assertEquals(InterfaceA.class, ExtendsImplementsC.class.getMethod("a").getDeclaringClass());
     }
 
     public void testGetDeclaredMethodReturnsIndirectlyImplementedInterface() throws Exception {
         try {
             ImplementsC.class.getDeclaredMethod("a").getDeclaringClass();
             fail();
         } catch (NoSuchMethodException expected) {
         }
         try {
             ExtendsImplementsC.class.getDeclaredMethod("a").getDeclaringClass();
             fail();
         } catch (NoSuchMethodException expected) {
         }
     }
 
     public void testGetMethodWithConstructorName() throws Exception {
         try {
             MethodTestHelper.class.getMethod("<init>");
             fail();
         } catch (NoSuchMethodException expected) {
         }
     }
 
     public void testGetMethodWithNullName() throws Exception {
         try {
             MethodTestHelper.class.getMethod(null);
             fail();
         } catch (NullPointerException expected) {
         }
     }
 
     public void testGetMethodWithNullArgumentsArray() throws Exception {
         Method m1 = MethodTestHelper.class.getMethod("m1", (Class[]) null);
         assertEquals(0, m1.getParameterTypes().length);
     }
 
     public void testGetMethodWithNullArgument() throws Exception {
         try {
             MethodTestHelper.class.getMethod("m2", new Class[] { null });
             fail();
         } catch (NoSuchMethodException expected) {
         }
     }
 
     public void testGetMethodReturnsInheritedStaticMethod() throws Exception {
         Method b = Sub.class.getMethod("b");
         assertEquals(void.class, b.getReturnType());
     }
 
     public void testGetDeclaredMethodReturnsPrivateMethods() throws Exception {
         Method method = Super.class.getDeclaredMethod("a");
         assertEquals(void.class, method.getReturnType());
     }
 
     public void testGetDeclaredMethodDoesNotReturnSuperclassMethods() throws Exception {
         try {
             Sub.class.getDeclaredMethod("a");
             fail();
         } catch (NoSuchMethodException expected) {
         }
     }
 
     public void testGetDeclaredMethodDoesNotReturnImplementedInterfaceMethods() throws Exception {
         try {
             InterfaceB.class.getDeclaredMethod("a");
             fail();
         } catch (NoSuchMethodException expected) {
         }
     }
 
     public static class MethodTestHelper {
         public void m1() throws IndexOutOfBoundsException { }
         public void m2(Object o) { }
     }
 
     public static class Super {
         private void a() {}
         public static void b() {}
     }
     public static interface InterfaceA {
         void a();
     }
     public static abstract class Sub extends Super implements InterfaceA {
     }
 
     public static interface InterfaceB extends InterfaceA {}
     public static interface InterfaceC extends InterfaceB {}
     public static abstract class ImplementsC implements InterfaceC {}
     public static abstract class ExtendsImplementsC extends ImplementsC {}
 }
