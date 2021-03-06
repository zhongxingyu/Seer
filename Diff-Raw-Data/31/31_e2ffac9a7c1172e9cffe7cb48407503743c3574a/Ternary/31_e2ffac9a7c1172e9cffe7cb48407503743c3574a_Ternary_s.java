 /*
  * Copyright 2004-2008 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.expression.spel.ast;
 
 import org.antlr.runtime.Token;
 import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.SpelException;
 import org.springframework.expression.spel.ExpressionState;
 
 /**
  * Represents a ternary expression, for example: "someCheck()?true:false".
  * 
  * @author Andy Clement
  */
 public class Ternary extends SpelNode {
 
 	public Ternary(Token payload) {
 		super(payload);
 	}
 
 	@Override
 	public Object getValue(ExpressionState state) throws EvaluationException {
		Object condition = getChild(0).getValue(state);
 		try {
			boolean b = state.toBoolean(condition);
			if (b)
 				return getChild(1).getValue(state);
			else
 				return getChild(2).getValue(state);
		} catch (SpelException see) {
			see.setPosition(getChild(0).getCharPositionInLine());
			throw see;
 		}
 	}
 
 	@Override
 	public String toStringAST() {
 		return new StringBuilder().append(getChild(0).toStringAST()).append(" ? ").append(getChild(1).toStringAST())
 				.append(" : ").append(getChild(2).toStringAST()).toString();
 	}
 
	// TODO 3 should this say TRUE if the left or the right are writable???
 	@Override
 	public boolean isWritable(ExpressionState expressionState) throws SpelException {
 		return false;
 	}
 }
