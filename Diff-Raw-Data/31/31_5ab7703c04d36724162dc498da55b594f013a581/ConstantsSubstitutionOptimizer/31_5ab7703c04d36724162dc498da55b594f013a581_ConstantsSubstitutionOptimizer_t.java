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
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import ru.histone.HistoneException;
 import ru.histone.evaluator.nodes.AstNode;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.parser.AstNodeType;
 import ru.histone.utils.Assert;
 
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 public class ConstantsSubstitutionOptimizer extends AbstractASTWalker {
     private Deque<Map<String, Node>> context = new LinkedList<Map<String, Node>>();
 
     public ConstantsSubstitutionOptimizer(NodeFactory nodeFactory, ObjectNode context) {
         super(nodeFactory);
         this.context.push(new HashMap<String, Node>());
         Iterator<Map.Entry<String, JsonNode>> iter = context.fields();
         while (iter.hasNext()) {
             Map.Entry<String, JsonNode> entry = iter.next();
             Node value = nodeFactory.jsonToNode(entry.getValue());
             this.context.getFirst().put(entry.getKey(), value);
         }
     }
 
     @Override
     public void pushContext() {
         context.push(new HashMap<String, Node>());
     }
 
     @Override
     public void popContext() {
         context.pop();
     }
 
     @Override
     public ArrayNode process(ArrayNode ast) throws HistoneException {
         return processAST(ast);
     }
 
     @Override
     protected JsonNode processSelector(ArrayNode ast) throws HistoneException {
         Assert.isTrue(ast.size() == 2);
 
         String name = ast.get(1).get(0).asText();
 
         Node node = searchContext(name);
 
         if (node == null) {
             return super.processSelector(ast);
         } else {
             return super.node2Ast(node);
 //            return nodeFactory.jsonArray(AstNodeType.STRING, nodeFactory.jsonString(node.getAsString().getValue()));
         }
     }
 
     @Override
     protected JsonNode processVariable(ArrayNode ast) throws HistoneException {
         Assert.isTrue(ast.size() == 3);
 
         JsonNode var = ast.get(1);
         String varName = var.asText();
         context.getFirst().remove(varName);
 
         JsonNode preValue = ast.get(2);
         JsonNode valueNode = processAstNode(preValue);
 
         if (valueNode.isArray()) {
             ArrayNode value = (ArrayNode) valueNode;
//            if (var.isTextual() && (isConstant(value) || /*isStatements(value) || */isMapOfConstants(value))) {
                 context.getFirst().put(varName, AstNode.create(value));
//            }
         }
 
         return nodeFactory.jsonArray(AstNodeType.VAR, var, valueNode);
     }
 
     private Node searchContext(String name) {
         for (Map<String, Node> frame : context) {
             if (frame.containsKey(name)) return frame.get(name);
         }
         return null;
     }
 }
