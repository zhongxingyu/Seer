 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.hula.lang.conditional;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 import com.hula.lang.conditional.HulaConditionalParser.parse_return;
 import com.hula.lang.runtime.RuntimeConnector;
 
 /**
 * A utility class to Hula commands supporting conditional logic. Provides validation 
  * and evaluation of conditional expressions at runtime.
  */
 public class ExpressionUtil
 {
 	/**
 	 * Evaluates a conditional expression
 	 * 
 	 * @param expression The expression to evaluate
 	 * @param rc The runtime connector for runtime variables
 	 * @return boolean indicating result
 	 */
 	public static boolean evaluate(String expression, RuntimeConnector rc)
 	{
 		HulaConditionalLexer lex = new HulaConditionalLexer(new ANTLRStringStream(expression));
 		CommonTokenStream tokenStream = new CommonTokenStream(lex);
 		HulaConditionalParser parser = new HulaConditionalParser(tokenStream);
 		parse_return parserResult = null;
 		try
 		{
 			parserResult = parser.parse();
 		}
 		catch (RecognitionException e)
 		{
 			throw new RuntimeException("error evaluating expression [" + expression + "]", e);
 		}
 
 		CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(parserResult.getTree());
 		HulaConditionalTree tree = new HulaConditionalTree(nodeStream);
 		tree.setRuntimeConnector(rc);
 
 		try
 		{
 			Object testResult = tree.test();
 			if (testResult instanceof Boolean)
 			{
 				return ((Boolean) testResult).booleanValue();
 			}
 			throw new RuntimeException("result not boolean [" + testResult + "]");
 		}
 		catch (RecognitionException e)
 		{
 			throw new RuntimeException("error evaluating expression [" + expression + "]", e);
 		}
 
 	}
	
 	/**
 	 * Validates a conditional expression
 	 * 
 	 * @param expression The expression to validate
 	 * @return boolean indicating result
 	 */
 	public static boolean validate(String expression)
 	{
 		HulaConditionalLexer lex = new HulaConditionalLexer(new ANTLRStringStream(expression));
 		CommonTokenStream tokenStream = new CommonTokenStream(lex);
 		HulaConditionalParser parser = new HulaConditionalParser(tokenStream);
 		try
 		{
 			parser.parse();
 		}
 		catch (RecognitionException e)
 		{
 			throw new RuntimeException("error evaluating expression [" + expression + "]", e);
 		}
 		return true;
 	}
 }
