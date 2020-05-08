 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.regex.Pattern;
 
 import junit.framework.TestCase;
 
 public class SerialDependencyResolver_ValidUseCasesTest extends TestCase
 {
     private SerialDependencyResolver resolver;
     
     @Override
     protected void setUp()
     {
         resolver = new SerialDependencyResolver();
     }
     
     public void testNoModules() throws Exception
     {
         resolver.init(Collections.<ModuleInfo>emptyList());
         
         assertSame(null, resolver.getFreeModule());
     }
     
     public void testSingleModule() throws Exception
     {
         final ModuleInfo module = new ModuleInfo("foo");
         
         resolver.init(Collections.singleton(module));
         
         assertSame(module, resolver.getFreeModule());
         resolver.moduleProcessed(module);
         assertSame(null, resolver.getFreeModule());
     }
     
     public void testTwoLinkedModules() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         module1.addDependency(module2);
         
         resolver.init(Arrays.asList(module1, module2));
         
         assertSame(module2, resolver.getFreeModule());
         resolver.moduleProcessed(module2);
         assertSame(module1, resolver.getFreeModule());
         resolver.moduleProcessed(module1);
         assertSame(null, resolver.getFreeModule());
     }
     
     public void testTwoUnrelatedModules() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         
         resolver.init(Arrays.asList(module1, module2));
         
         final HashSet<ModuleInfo> returnedModules = new HashSet<ModuleInfo>();
         final ModuleInfo m1 = resolver.getFreeModule();
         returnedModules.add(m1);
         resolver.moduleProcessed(m1);
         final ModuleInfo m2 = resolver.getFreeModule();
         returnedModules.add(m2);
         resolver.moduleProcessed(m2);
         assertSame(null, resolver.getFreeModule());
         
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(module1, module2)), returnedModules);
     }
     
     
     public void testThreeModules_Chain() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         module1.addDependency(module2);
         module3.addDependency(module1);
         
         resolver.init(Arrays.asList(module1, module2, module3));
         
         assertSame(module2, resolver.getFreeModule());
         resolver.moduleProcessed(module2);
         assertSame(module1, resolver.getFreeModule());
         resolver.moduleProcessed(module1);
         assertSame(module3, resolver.getFreeModule());
         resolver.moduleProcessed(module3);
         assertSame(null, resolver.getFreeModule());
     }
     
     public void testThreeModules_TwoDependUponOne() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         module1.addDependency(module2);
         module3.addDependency(module2);
         
         resolver.init(Arrays.asList(module1, module2, module3));
 
         final ArrayList<ModuleInfo> order = flushModules(resolver, 3);
         assertTrue(order.contains(module1));
         assertTrue(order.contains(module2));
         assertTrue(order.contains(module3));
         assertTrue(order.indexOf(module2) < order.indexOf(module1));
         assertTrue(order.indexOf(module2) < order.indexOf(module3));
     }
     
     public void testThreeModules_OneDependsUponTwo() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         module2.addDependency(module1);
         module2.addDependency(module3);
         
         resolver.init(Arrays.asList(module1, module2, module3));
 
         final ArrayList<ModuleInfo> order = flushModules(resolver, 3);
         assertTrue(order.contains(module1));
         assertTrue(order.contains(module2));
         assertTrue(order.contains(module3));
         assertTrue(order.indexOf(module1) < order.indexOf(module2));
         assertTrue(order.indexOf(module3) < order.indexOf(module2));
     }
     
     public void testFourModules_Diamond() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         final ModuleInfo module4 = new ModuleInfo("quux");
         module2.addDependency(module1);
         module2.addDependency(module3);
         module1.addDependency(module4);
         module3.addDependency(module4);
         
         resolver.init(Arrays.asList(module1, module2, module3, module4));
 
         final ArrayList<ModuleInfo> order = flushModules(resolver, 4);
         assertTrue(order.contains(module1));
         assertTrue(order.contains(module2));
         assertTrue(order.contains(module3));
         assertTrue(order.contains(module4));
         assertTrue(order.indexOf(module1) < order.indexOf(module2));
         assertTrue(order.indexOf(module3) < order.indexOf(module2));
         assertTrue(order.indexOf(module4) < order.indexOf(module1));
         assertTrue(order.indexOf(module4) < order.indexOf(module3));
     }
     
     public void testFourModules_Branch() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         final ModuleInfo module4 = new ModuleInfo("quux");
         module3.addDependency(module1);
         module3.addDependency(module2);
         module2.addDependency(module4);
         
         resolver.init(Arrays.asList(module1, module2, module3, module4));
 
         final ArrayList<ModuleInfo> order = flushModules(resolver, 4);
         assertTrue(order.contains(module1));
         assertTrue(order.contains(module2));
         assertTrue(order.contains(module3));
         assertTrue(order.contains(module4));
         assertTrue(order.indexOf(module1) < order.indexOf(module3));
         assertTrue(order.indexOf(module2) < order.indexOf(module3));
         assertTrue(order.indexOf(module4) < order.indexOf(module2));
     }
     
     public void testFourModules_ThreeAndSingle() throws Exception
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         final ModuleInfo module4 = new ModuleInfo("quux");
         module3.addDependency(module1);
         module3.addDependency(module2);
         
         resolver.init(Arrays.asList(module1, module2, module3, module4));
 
         final ArrayList<ModuleInfo> order = flushModules(resolver, 4);
         assertTrue(order.contains(module1));
         assertTrue(order.contains(module2));
         assertTrue(order.contains(module3));
         assertTrue(order.contains(module4));
         assertTrue(order.indexOf(module1) < order.indexOf(module3));
         assertTrue(order.indexOf(module2) < order.indexOf(module3));
     }
     
     public void testLoop_TwoModules()
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         module1.addDependency(module2);
         module2.addDependency(module1);
         
         try {
             resolver.init(Arrays.asList(module1, module2));
             fail();
         }
         catch (CyclicDependenciesDetectedException ex) {
             assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                     Pattern.quote("[->foo->bar->]") + '|' + Pattern.quote("[->bar->foo->]") + ")\\.", ex.getMessage()));
         }
     }
     
     public void testLoop_ThreeModules_AllInLoop()
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         module1.addDependency(module3);
         module3.addDependency(module2);
         module2.addDependency(module1);
         
         try {
             resolver.init(Arrays.asList(module1, module2, module3));
             fail();
         }
         catch (CyclicDependenciesDetectedException ex) {
             assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                     Pattern.quote("[->foo->baz->bar->]") + '|' +
                     Pattern.quote("[->baz->bar->foo->]") + '|' +
                     Pattern.quote("[->bar->foo->baz->]") + ")\\.", ex.getMessage()));
         }
     }
     
     public void testLoop_ThreeModules_TwoInLoop()
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         module1.addDependency(module3);
         module3.addDependency(module1);
         
         try {
             resolver.init(Arrays.asList(module1, module2, module3));
             fail();
         }
         catch (CyclicDependenciesDetectedException ex) {
             assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                     Pattern.quote("[->foo->baz->]") + '|' +
                     Pattern.quote("[->baz->foo->]") + ")\\.", ex.getMessage()));
         }
     }
     
     public void testLoop_FourModules_AllInLoop()
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         final ModuleInfo module4 = new ModuleInfo("quux");
         module1.addDependency(module3);
         module3.addDependency(module2);
         module2.addDependency(module4);
         module4.addDependency(module1);
         
         try {
             resolver.init(Arrays.asList(module1, module2, module3, module4));
             fail();
         }
         catch (CyclicDependenciesDetectedException ex) {
             assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                     Pattern.quote("[->foo->baz->bar->quux->]") + '|' +
                     Pattern.quote("[->quux->foo->baz->bar->]") + '|' +
                     Pattern.quote("[->bar->quux->foo->baz->]") + '|' +
                     Pattern.quote("[->baz->bar->quux->foo->]") + ")\\.", ex.getMessage()));
         }
     }
     
     public void testLoop_FourModules_ThreeInLoop()
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         final ModuleInfo module4 = new ModuleInfo("quux");
         module1.addDependency(module3);
         module3.addDependency(module2);
         module2.addDependency(module1);
         
         try {
             resolver.init(Arrays.asList(module1, module2, module3, module4));
             fail();
         }
         catch (CyclicDependenciesDetectedException ex) {
             assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                     Pattern.quote("[->foo->baz->bar->]") + '|' +
                     Pattern.quote("[->bar->foo->baz->]") + '|' +
                     Pattern.quote("[->baz->bar->foo->]") + ")\\.", ex.getMessage()));
         }
     }
     
     public void testLoop_FourModules_TwoLoops()
     {
         final ModuleInfo module1 = new ModuleInfo("foo");
         final ModuleInfo module2 = new ModuleInfo("bar");
         final ModuleInfo module3 = new ModuleInfo("baz");
         final ModuleInfo module4 = new ModuleInfo("quux");
         module1.addDependency(module3);
         module3.addDependency(module1);
         module2.addDependency(module4);
         module4.addDependency(module2);
         
         try {
             resolver.init(Arrays.asList(module1, module2, module3, module4));
             fail();
         }
         catch (CyclicDependenciesDetectedException ex) {
             assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->baz]") + '|' +
                     Pattern.quote("[->baz->foo->]") + '|' +
                     Pattern.quote("[->bar->quux->]") + '|' +
                     Pattern.quote("[->quux->bar->]") + ")\\.", ex.getMessage()));
         }
     }
     
     private static ArrayList<ModuleInfo> flushModules(final SerialDependencyResolver resolver, final int moduleCount)
     {
         final ArrayList<ModuleInfo> result = new ArrayList<ModuleInfo>();
         for (int i = moduleCount; i > 0; --i) {
             final ModuleInfo module = resolver.getFreeModule();
             result.add(module);
             resolver.moduleProcessed(module);
         }
         assertSame(null, resolver.getFreeModule());
         assertEquals(moduleCount, result.size());
         return result;
     }
 }
