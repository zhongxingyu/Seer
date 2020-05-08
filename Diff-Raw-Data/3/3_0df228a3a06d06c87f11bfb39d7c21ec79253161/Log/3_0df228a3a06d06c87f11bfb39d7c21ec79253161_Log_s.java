 /**
  *    Copyright 2012 MegaFon
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
 package ru.histone.evaluator.functions.node.number;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import ru.histone.evaluator.functions.node.NodeFunction;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.evaluator.nodes.NumberHistoneNode;
 import ru.histone.evaluator.nodes.NumberHistoneNode;
 
 /**
  *  Format a number up to required decimal places.
  */
 public class Log extends NodeFunction<NumberHistoneNode> {
 
     public Log(NodeFactory nodeFactory) {
         super(nodeFactory);
     }
 
     @Override
     public String getName() {
         return "log";
     }
 
     @Override
 	public Node execute(NumberHistoneNode target, Node... args) {
 		BigDecimal value = target.getValue();
 		value = new BigDecimal(Math.log(value.doubleValue())); 
 		return getNodeFactory().number(value);
 	}
 }
