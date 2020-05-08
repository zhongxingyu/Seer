 /*
  *  Copyright 2009-2010 Mathieu ANCELIN.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.test.circular;
 
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.api.DSInjector;
 import org.junit.Test;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNull;
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Tests of dependency-shot. 
  * 
  * @author Mathieu ANCELIN
  */
 public class CircularTest {
 
     @Test
     public void testCyclicDependency() {
         boolean isCyclic = false;
         Exception ex = null;
         try {
             DSInjector injector = DependencyShot.getInjector();
             BillingService service = injector.getInstance(BillingService.class);
             service.chargeAccountFor(123);
             assertTrue(service.getAccount().getMoney() == (100000 - 123));
         } catch (Exception e) {
             ex = e;
             isCyclic = true;
         }
         assertTrue(isCyclic);
         assertTrue(ex.getMessage().contains("DSCyclicDependencyDetectedException"));
     }
 
     @Test
     public void testCyclicDependencyAllowed() {
         boolean isCyclic = false;
         Exception ex = null;
         try {
             DSInjector injector = DependencyShot.getInjector();
             injector.allowCircularDependencies(true);
             BillingService service = injector.getInstance(BillingService.class);
             service.chargeAccountFor(123);
             assertTrue(service.getAccount().getMoney() == (100000 - 123));
         } catch (Exception e) {
             System.out.println(e);
             ex = e;
             isCyclic = true;
             System.out.println("boooom");
         }
         assertFalse(isCyclic);
         assertNull(ex);
     }
 
     @Test
     public void testNonCyclicDependency() {
         DSInjector injector = DependencyShot.getInjector(new NonCyclicBinder());
         BillingService service = injector.getInstance(BillingService.class);
         service.chargeAccountFor(123);
         assertTrue(service.getAccount().getMoney() == (100000 - 123));
     }
 
     @Test
     public void testCyclicMethod() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector();
         try {
             MethodA a = injector.getInstance(MethodA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             MethodA a = injector.getInstance(MethodA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
     }
 
     @Test
     public void testCyclicConstructor() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector();
         try {
             ConstructorA a = injector.getInstance(ConstructorA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             ConstructorA a = injector.getInstance(ConstructorA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
     }
 
     @Test
     public void testCyclicInterfaceConstructor() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector(new Binder() {
                     @Override
                     public void configureBindings() {
                         bind(InterfaceA.class).to(ConstructorA.class);
                         bind(InterfaceB.class).to(ConstructorB.class);
                     }
                 });
         InterfaceA a = null;
         try {
             a = injector.getInstance(InterfaceA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             a = injector.getInstance(InterfaceA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
 //        System.out.println(a.getB().getA());
 //        System.out.println(a.getB().getA().getB());
 //        assertEquals(a.getB().getA().getValue(), "ConstructorA");
 //        assertEquals(a.getB().getA().getB().getValue(), "ConstructorB");
     }
 
     @Test
     public void testCyclicField() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector();
         try {
             FieldA a = injector.getInstance(FieldA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             FieldA a = injector.getInstance(FieldA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
     }
 
     @Test
     public void testSingletonCyclicMethod() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector();
         try {
             SingletonMethodA a = injector.getInstance(SingletonMethodA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             SingletonMethodA a = injector.getInstance(SingletonMethodA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
     }
 
     @Test
     public void testSingletonCyclicConstructor() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector();
         try {
             SingletonConstructorA a = injector.getInstance(SingletonConstructorA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             SingletonConstructorA a = injector.getInstance(SingletonConstructorA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertTrue(isCyclic);
     }
 
     @Test
     public void testSingletonCyclicInterfaceConstructor() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector(
                 new Binder() {
                     @Override
                     public void configureBindings() {
                         bind(SingletonInterfaceA.class).to(SingletonConstructorA.class);
                         bind(SingletonInterfaceB.class).to(SingletonConstructorB.class);
                     }
                 });
         SingletonInterfaceA a = null;
         try {
              a = injector.getInstance(SingletonInterfaceA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             a = injector.getInstance(SingletonInterfaceA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
 //        System.out.println(a.getB().getA());
 //        System.out.println(a.getB().getA().getB());
 //        assertEquals(a.getB().getA().getValue(), "SingletonConstructorA");
 //        assertEquals(a.getB().getA().getB().getValue(), "SingletonConstructorB");
     }
 
     @Test
     public void testSingletonCyclicField() {
         boolean isCyclic = false;
         DSInjector injector = DependencyShot.getInjector();
         try {
             SingletonFieldA a = injector.getInstance(SingletonFieldA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
         isCyclic = false;
         injector.allowCircularDependencies(true);
         try {
             SingletonFieldA a = injector.getInstance(SingletonFieldA.class);
         } catch (Exception e ) {
             isCyclic = true;
         }
         assertFalse(isCyclic);
     }
 }
