 /*
  * Copyright 2011 Vasily Shiyan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package see.integration;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import org.junit.Test;
 import see.ReactiveSee;
 import see.reactive.Dependency;
 import see.reactive.Signal;
 import see.reactive.VariableSignal;
 import see.reactive.impl.ReactiveFactory;
 import see.tree.Node;
 
 import java.util.Map;
 
 import static com.google.common.collect.ImmutableMap.of;
 import static org.junit.Assert.assertEquals;
 
 public class BindingTest {
     ReactiveFactory reactiveFactory = new ReactiveFactory();
     ReactiveSee see = new ReactiveSee(reactiveFactory);
 
     @Test
     public void testBindings() throws Exception {
         VariableSignal<String> var = reactiveFactory.var("crno");
         PropertyTraversalTest.TestBean bean = new PropertyTraversalTest.TestBean("omg", null);
 
         Map<String, Object> context = of("a", bean, "v", var);
         
         see.eval("a.name <- v", context);
         assertEquals("crno", bean.getName());
 
         var.update("bka");
         assertEquals("bka", bean.getName());
     }
 
     @Test
     public void testMultipleBindings() throws Exception {
         VariableSignal<Integer> a = reactiveFactory.var(1);
         VariableSignal<Integer> b = reactiveFactory.var(2);
         TestBean bean = new TestBean();
 
         Map<String, Object> context = of("a", a, "b", b, "bean", bean);
 
         see.eval("bean.value <- signal(a + b)", context);
         assertEquals("3.0", bean.getValue().toString());
         
         a.update(7);
         assertEquals("9.0", bean.getValue().toString());
 
         b.update(35);
         assertEquals("42.0", bean.getValue().toString());
     }
 
     @Test
     public void testBindingContext() throws Exception {
         VariableSignal<Integer> a = reactiveFactory.var(7);
         TestBean bean = new TestBean();
 
         Map<String, Object> context = Maps.newHashMap(of("a", a, "b", 2, "bean", bean));
 
         see.eval("bean.value <- signal(a + b)", context);
         assertEquals("9.0", bean.getValue().toString());
 
         context.put("b", 42);
         assertEquals("9.0", bean.getValue().toString());
 
         a.update(40);
         assertEquals("42.0", bean.getValue().toString()); // Signal is bound to old b == 42
     }
 
     @Test
     public void testNowBindings() throws Exception {
         VariableSignal<Integer> a = reactiveFactory.var(7);
         TestBean bean = new TestBean();
 
         Map<String, Object> context = of("a", a, "bean", bean);
         Node<Object> tree = see.parseExpressionList("b = a.now; bean.value <- signal(b + 2);");
         see.evaluate(tree, context);
 
         assertEquals("9.0", bean.getValue().toString());
 
         a.update(42);
         assertEquals("9.0", bean.getValue().toString());
     }
 
     @Test
     public void testImplicitSignalCreation() throws Exception {
         VariableSignal<Integer> a = reactiveFactory.var(4);
         TestBean bean = new TestBean();
 
         Map<String, Object> context = of("a", a, "bean", bean);
         see.eval("bean.value <- a + 5", context); // No signal() function
 
         assertEquals("9.0", bean.getValue().toString());
         
         a.update(37);
         assertEquals("42.0", bean.getValue().toString());
     }
 
     @Test
     public void testEagerness() throws Exception {
         Dependency a = reactiveFactory.var(false);
         Dependency b = reactiveFactory.var(true);
 
         Map<String, Object> context = ImmutableMap.<String, Object>of("a", a, "b", b);
         Signal<?> result = (Signal<?>) see.eval("signal(a && b)", context); // b is not evaluated normally
 
         assertEquals(ImmutableSet.of(a, b), result.getDependencies()); // Dependencies should contain b
     }
 
     public static class TestBean {
         private Number value;
 
         public Number getValue() {
             return value;
         }
 
         public void setValue(Number value) {
             this.value = value;
         }
     }
 }
