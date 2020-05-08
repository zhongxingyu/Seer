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
 *  Returns the natural logarithm (base <i>e</i>) of a <code>double</code> value.
 *  If args[0] is not null return logarithm for base
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
		if (args.length > 1) {
			return getNodeFactory().UNDEFINED;
		}
		BigDecimal base = null;
		if (args.length == 0) {
		} else if (args[0].isNumber()) {
			base = args[0].getAsNumber().getValue();
		} else if (args[0].isString()) {
			try {
				base = new BigDecimal(args[0].getAsString().getValue());
			} catch (Exception e) {
				// if wrong format try to get natural logarithm
			}
		}

 		BigDecimal value = target.getValue();
		if (base == null || base.longValue() <= 0) {
			value = new BigDecimal(Math.log(value.doubleValue()));
		} else {
			value = new BigDecimal(Math.log(value.doubleValue()) / Math.log(base.doubleValue()));
		}
 		return getNodeFactory().number(value);
 	}
 }
