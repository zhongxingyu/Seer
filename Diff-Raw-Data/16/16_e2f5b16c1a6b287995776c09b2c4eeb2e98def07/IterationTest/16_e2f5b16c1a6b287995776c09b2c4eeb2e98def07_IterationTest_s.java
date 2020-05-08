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
 import org.junit.Before;
 import org.junit.Test;
 import see.See;
 import see.tree.Node;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.collect.ImmutableList.of;
 import static com.google.common.collect.Maps.newHashMap;
 import static org.junit.Assert.assertEquals;
 
 public class IterationTest {
     See see = new See();
 
     final List<?> testList = of(new StringBean("nine"), new StringBean("crno"), new StringBean("bka"));
     final Map<String, ?> baseContext = ImmutableMap.of("list", testList, "map", ImmutableMap.of("list", testList));
     Map<String,?> context;
 
     @Before
     public void setUp() throws Exception {
         context = newHashMap(baseContext);
     }
 
     @Test
     public void testForLoop() throws Exception {
        Node<?> tree = see.parseExpressionList("a = 0; for(s : list) a = a + s.length;");
        see.evaluate(tree, context);
 
        assertEquals("11.0", context.get("a").toString());
     }
 
     @Test
     public void testForLoopWithComplexExpressions() throws Exception {
         Node<?> tree = see.parseExpressionList("a = 0; for(c : map.list) { a = a + 1; } a;");
         Object result = see.evaluate(tree, context);
 
         assertEquals(new BigDecimal(3.0), result);
     }
 
     @Test
     public void testForLoopJavascriptSyntax() throws Exception {
         Node<?> tree = see.parseExpressionList("a = 0; for(c in list) { a = a + 1; } a;");
         Object result = see.evaluate(tree, context);
         
         assertEquals(new BigDecimal(3), result);
     }
 
     @Test
     public void testWhileLoop() throws Exception {
        Node<?> tree = see.parseExpressionList("a = 3; b = 3; while(a > 0) { a = a - 1; b = b + 2; }");
        see.evaluate(tree, context);
 
        assertEquals(BigDecimal.ZERO, context.get("a"));
        assertEquals(new BigDecimal(9), context.get("b"));
     }
 
     public static class StringBean {
         private String content;
 
         public StringBean(String content) {
             this.content = content;
         }
         
         public int getLength() {
             return content.length();
         }
     }
 }
