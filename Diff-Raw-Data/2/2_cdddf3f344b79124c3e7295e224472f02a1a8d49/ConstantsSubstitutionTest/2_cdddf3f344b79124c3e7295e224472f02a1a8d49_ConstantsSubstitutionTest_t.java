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
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.junit.Before;
 import org.junit.Test;
 import ru.histone.Histone;
 import ru.histone.HistoneBuilder;
 import ru.histone.HistoneException;
 import ru.histone.evaluator.nodes.NodeFactory;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class ConstantsSubstitutionTest {
     private Histone histone;
     private NodeFactory nodeFactory;
     private ObjectMapper jackson;
 
     @Before
     public void init() throws HistoneException {
         HistoneBuilder histoneBuilder = new HistoneBuilder();
         histone = histoneBuilder.build();
         nodeFactory = histoneBuilder.getNodeFactory();
         jackson = new ObjectMapper();
     }
 
     @Test
     public void expr_selector() throws IOException, HistoneException {
         String input = "{{var a = 111}}{{a}}{{b}}{{c.test}}{{\"tttt\"}}";
         ArrayNode expectedAST = (ArrayNode) jackson.readTree("[[1001,\"a\",[101,111]],[101,111],[103,\"222\"],[105,[\"c\",\"test\"]],[103,\"tttt\"]]");
         // String inputAST = [[1001,"a",[101,111]],[105,["a"]],[105,["b"]],[105,["c","test"]],[103,"tttt"]]
         ObjectNode context = nodeFactory.jsonObject();
         context.put("b", "222");
         ArrayNode initialAST = histone.parseTemplateToAST(input);
         ArrayNode optimizedAST = histone.optimizeAST(initialAST, context, OptimizationTypes.CONSTANTS_SUBSTITUTION);
 
         assertEquals(expectedAST.toString(), optimizedAST.toString());
     }
 
     @Test
     public void expr_if() throws IOException, HistoneException {
         String input = "{{var b = false}}{{if a}}AAA{{elseif b}}BBB{{/if}}";
         ArrayNode expectedAST = (ArrayNode) jackson.readTree("[[1001,\"b\",[17]],[1000,[[[16],[\"AAA\"]],[[17],[\"BBB\"]]]]]");
         ObjectNode context = nodeFactory.jsonObject();
         context.put("a", true);
         ArrayNode initialAST = histone.parseTemplateToAST(input);
         ArrayNode optimizedAST = histone.optimizeAST(initialAST, context, OptimizationTypes.CONSTANTS_SUBSTITUTION);
 
         assertEquals(expectedAST.toString(), optimizedAST.toString());
     }
     @Test
     public void expr_var() throws IOException, HistoneException {
         String input = "a {{var x = 10}}{{for r in range(1, 10)}}{{var x = x + 10}}{{x}} {{/for}} b";
        ArrayNode expectedAST = (ArrayNode) jackson.readTree("[\"a \",[1001,\"x\",[101,10]],[1002,[\"r\"],[106,null,\"range\",[[101,1],[101,10]]],[[[1001,\"x\",[9,[101,10],[101,10]]],[9,[101,10],[101,10]],\" \"]]],\" b\"]");
         ObjectNode context = nodeFactory.jsonObject();
         context.put("a", true);
         ArrayNode initialAST = histone.parseTemplateToAST(input);
         ArrayNode optimizedAST = histone.optimizeAST(initialAST, context, OptimizationTypes.CONSTANTS_SUBSTITUTION);
 
         assertEquals(expectedAST.toString(), optimizedAST.toString());
     }
 
 }
