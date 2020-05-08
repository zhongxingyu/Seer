 /***********************************************************************************************************************
  *
  * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 package eu.stratosphere.meteor.expression;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import eu.stratosphere.meteor.MeteorTest;
 import eu.stratosphere.sopremo.EvaluationContext;
 import eu.stratosphere.sopremo.SecondOrderFunctions;
 import eu.stratosphere.sopremo.base.Grouping;
 import eu.stratosphere.sopremo.expressions.ArithmeticExpression;
 import eu.stratosphere.sopremo.expressions.ArithmeticExpression.ArithmeticOperator;
 import eu.stratosphere.sopremo.expressions.ArrayCreation;
 import eu.stratosphere.sopremo.expressions.ConstantExpression;
 import eu.stratosphere.sopremo.expressions.FunctionCall;
 import eu.stratosphere.sopremo.expressions.InputSelection;
 import eu.stratosphere.sopremo.expressions.ObjectCreation;
 import eu.stratosphere.sopremo.function.ExpressionFunction;
 import eu.stratosphere.sopremo.function.FunctionNode;
 import eu.stratosphere.sopremo.function.JavaMethod;
 import eu.stratosphere.sopremo.io.Sink;
 import eu.stratosphere.sopremo.io.Source;
 import eu.stratosphere.sopremo.operator.SopremoPlan;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.JsonUtil;
 
 /**
  * @author Arvid Heise
  */
 public class SecondOrderFunctionTest extends MeteorTest {
 
 	@Test
 	public void testMapWithFunctionDefinition() {
 		final SopremoPlan actualPlan = this.parseScript("square = fn(elem) elem.a * elem.b;\n" +
 			"$input = read from 'input.json';\n" +
 			"$result = group $input by $input.key into { squared: map($input, &square) };\n" +
 			"write $result to 'output.json'; ");
 
 		final SopremoPlan expectedPlan = new SopremoPlan();
 		final EvaluationContext context = expectedPlan.getEvaluationContext();
 		context.getFunctionRegistry().put(SecondOrderFunctions.class);
 		final ExpressionFunction squareFunction = new ExpressionFunction(1,
 			new ArithmeticExpression(JsonUtil.createPath("0", "a"), ArithmeticOperator.MULTIPLICATION,
 				JsonUtil.createPath("0", "b")));
 		final ConstantExpression functionNode = new ConstantExpression(new FunctionNode(squareFunction));
 
 		final Source input = new Source("input.json");
 		final Grouping projection = new Grouping().
 			withInputs(input).
 			withGroupingKey(0, JsonUtil.createPath("0", "key")).
 			withResultProjection(new ObjectCreation(
 				new ObjectCreation.FieldAssignment("squared",
 					new FunctionCall("map", context, new InputSelection(0), functionNode))));
 		final Sink sink = new Sink("output.json").withInputs(projection);
 		expectedPlan.setSinks(sink);
 
 		Assert.assertEquals("unexpectedPlan", expectedPlan, actualPlan);
 	}
 
 	@Test
 	public void testMapWithFunctionImport() throws SecurityException, NoSuchMethodException {
 		final SopremoPlan actualPlan = this
 			.parseScript("testudf = javaudf('" + getClass().getName() + ".udfTest');\n" +
 				"$input = read from 'input.json';\n" +
 				"$result = group $input by $input.key into map($input, &testudf);\n" +
 				"write $result to 'output.json'; ");
 
 		final SopremoPlan expectedPlan = new SopremoPlan();
 		final EvaluationContext context = expectedPlan.getEvaluationContext();
 		context.getFunctionRegistry().put(SecondOrderFunctions.class);
 		final JavaMethod javaMethod = new JavaMethod("testudf");
 		javaMethod.addSignature(getClass().getMethod("udfTest", IJsonNode[].class));
 		final ConstantExpression functionNode = new ConstantExpression(new FunctionNode(javaMethod));
 
 		final Source input = new Source("input.json");
 		final Grouping projection = new Grouping().
 			withInputs(input).
 			withGroupingKey(0, JsonUtil.createPath("0", "key")).
 			withResultProjection(new FunctionCall("map", context, new InputSelection(0), functionNode));
 		final Sink sink = new Sink("output.json").withInputs(projection);
 		expectedPlan.setSinks(sink);
 
 		Assert.assertEquals("unexpectedPlan", expectedPlan, actualPlan);
 	}
 
 	@Test
 	public void testMapWithInlineFunction() {
 		final SopremoPlan actualPlan = this.parseScript(
 			"$input = read from 'input.json';\n" +
 				"$result = group $input by $input.key into [map($input, fn(elem) elem.a * elem.b)];\n" +
 				"write $result to 'output.json'; ");
 
 		final SopremoPlan expectedPlan = new SopremoPlan();
 		final EvaluationContext context = expectedPlan.getEvaluationContext();
 		context.getFunctionRegistry().put(SecondOrderFunctions.class);
 		final ExpressionFunction squareFunction = new ExpressionFunction(1,
 			new ArithmeticExpression(JsonUtil.createPath("0", "a"), ArithmeticOperator.MULTIPLICATION,
 				JsonUtil.createPath("0", "b")));
 		final ConstantExpression functionNode = new ConstantExpression(new FunctionNode(squareFunction));
 
 		final Source input = new Source("input.json");
 		final Grouping projection = new Grouping().
 			withInputs(input).
 			withGroupingKey(0, JsonUtil.createPath("0", "key")).
 			withResultProjection(new ArrayCreation(
 				new FunctionCall("map", context, new InputSelection(0), functionNode)));
 		final Sink sink = new Sink("output.json").withInputs(projection);
 		expectedPlan.setSinks(sink);
 
 		Assert.assertEquals("unexpectedPlan", expectedPlan, actualPlan);
 	}
 
 	public static IJsonNode udfTest(final IJsonNode... nodes) {
 		return nodes[0];
 	}
 }
