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
 
 import com.google.common.collect.Maps;
 import org.junit.Test;
 import see.See;
import see.exceptions.SeeRuntimeException;
 import see.exceptions.NoSuchVariableException;
import see.exceptions.PropagatedException;
 import see.functions.VarArgFunction;
 import see.tree.Node;
 
 import java.math.BigDecimal;
import java.util.NoSuchElementException;
 
 import static com.google.common.collect.ImmutableMap.of;
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.junit.Assert.*;
 
 public class FunctionCreationTest {
     See see = new See();
 
     @Test
     public void testReturnType() throws Exception {
         Object result = see.eval("def(args, @tree 1)", of("args", asList("a", "b")));
         assertThat(result, instanceOf(VarArgFunction.class));
     }
 
     @Test
     public void testResultEvaluation() throws Exception {
         Node<Object> tree = see.parseExpressionList("s = def(args, @tree a + b); s('crn', 'bka');");
 
         Object result = see.evaluate(tree, Maps.newHashMap(of("args", asList("a", "b"))));
         assertEquals("crn" + "bka", result);
     }
 
     @Test
     public void testFullForm() throws Exception {
         Object f = see.eval("function(a, b) { a + b; }");
         assertThat(f, instanceOf(VarArgFunction.class));
     }
 
     @Test
     public void testFullFormDeclaration() throws Exception {
         Node<Object> tree = see.parseExpressionList("s = function(a, b) { a + b; }; s('crn', 'bka');");
         Object result = see.evaluate(tree);
         assertEquals("crn" + "bka", result);
     }
 
     @Test
     public void testRecursion() throws Exception {
         Object f = see.eval("fact = function(n) { if(n <= 0, 1, n*fact(n - 1)); }");
 
         Object result = see.eval("fact(5)", of("fact", f));
         assertEquals(new BigDecimal(120), result);
     }
 
     @Test
     public void testVariableScopes() throws Exception {
         Node<Object> tree = see.parseExpressionList("function() { a = 42; } (); a;");
 
         try {
             see.evaluate(tree);
             fail("Exception expected");
        } catch (PropagatedException e) {
            assertThat(e.getLastCause(), instanceOf(NoSuchVariableException.class));
         }
     }
 
     @Test
     public void testParameterImmutability() throws Exception {
         try {
             see.eval("function(a) { a = 42; } (9)");
             fail("Exception expected");
         } catch (SeeRuntimeException e) {
             assertThat(e.getCause(), instanceOf(UnsupportedOperationException.class));
         }
     }
 
     @Test
     public void testNullParameter() throws Exception {
         see.eval("function(a) { 9; }(null)");
     }
 
     @Test
     public void testShortSyntaxSingleArg() throws Exception {
         Node<?> parsed = see.parseExpression("a => a + 1");
         assertEquals(see.parseExpression("function(a) { a + 1; }"), parsed);
     }
 
     @Test
     public void testShortSyntaxMultipleArgs() throws Exception {
         Node<?> parsed = see.parseExpression("(a, b) => a + b");
         assertEquals(see.parseExpression("function(a, b) { a + b; }"), parsed);
     }
 }
