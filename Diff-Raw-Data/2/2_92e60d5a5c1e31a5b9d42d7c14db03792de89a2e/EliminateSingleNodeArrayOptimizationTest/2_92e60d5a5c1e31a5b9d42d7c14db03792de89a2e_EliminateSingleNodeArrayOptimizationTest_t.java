 /**
  *    Copyright 2013 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.optimizer;
 
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import org.junit.Test;
 import ru.histone.HistoneException;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class EliminateSingleNodeArrayOptimizationTest extends AbstractOptimizersTest {
 
     @Test
     public void test() throws IOException, HistoneException {
         ArrayNode input = (ArrayNode) getJackson().readTree("[[[\"AAA\",[101,1],\"BBB\"]]]");
         ArrayNode expected = (ArrayNode) getJackson().readTree("[\"AAA\",[101,1],\"BBB\"]");
 
        ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         assertEquals(expected.toString(), ast.toString());
     }
 
 
 }
