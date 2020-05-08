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
 package eu.stratosphere.sopremo;
 
 import junit.framework.Assert;
 import eu.stratosphere.sopremo.aggregation.Aggregation;
 import eu.stratosphere.sopremo.function.SopremoFunction;
 import eu.stratosphere.sopremo.type.IArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.JavaToJsonMapper;
 
 /**
  * @author Arvid Heise
  */
 public class FunctionTest {
 
 	public static void assertAggregate(Object expected, Aggregation function, Object... items) {
 		function.initialize();
 
 		for (Object item : items)
 			function.aggregate(JavaToJsonMapper.INSTANCE.valueToTree(item));
 
 		final IJsonNode result = function.getFinalAggregate();
 		Assert.assertEquals(JavaToJsonMapper.INSTANCE.valueToTree(expected), result);
 	}
 
 	public static void assertReturn(Object expected, SopremoFunction function, Object... items) {
 		final IArrayNode params = (IArrayNode) JavaToJsonMapper.INSTANCE.valueToTree(items);
 		final IJsonNode result = function.call(params);
 		Assert.assertEquals(JavaToJsonMapper.INSTANCE.valueToTree(expected), result);
 	}
 }
