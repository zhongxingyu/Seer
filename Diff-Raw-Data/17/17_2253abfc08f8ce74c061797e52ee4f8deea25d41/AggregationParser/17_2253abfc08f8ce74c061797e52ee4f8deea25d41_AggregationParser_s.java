 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.logdb.FunctionRegistry;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.query.aggregator.AggregationField;
 import org.araqne.logdb.query.aggregator.AggregationFunction;
 import org.araqne.logdb.query.aggregator.Average;
 import org.araqne.logdb.query.aggregator.Count;
 import org.araqne.logdb.query.aggregator.First;
 import org.araqne.logdb.query.aggregator.Last;
 import org.araqne.logdb.query.aggregator.Max;
 import org.araqne.logdb.query.aggregator.Min;
 import org.araqne.logdb.query.aggregator.PerDay;
 import org.araqne.logdb.query.aggregator.PerHour;
 import org.araqne.logdb.query.aggregator.PerMinute;
 import org.araqne.logdb.query.aggregator.PerSecond;
 import org.araqne.logdb.query.aggregator.Range;
 import org.araqne.logdb.query.aggregator.Sum;
 import org.araqne.logdb.query.expr.Expression;
 import org.araqne.logdb.query.expr.Values;
 
 public class AggregationParser {
 	private static final String AS = " as ";
 	private static Map<String, Class<? extends AggregationFunction>> t;
 
 	static {
 		t = new HashMap<String, Class<? extends AggregationFunction>>();
 		t.put("c", Count.class);
 		t.put("count", Count.class);
 		t.put("sum", Sum.class);
 		t.put("avg", Average.class);
 		t.put("first", First.class);
 		t.put("last", Last.class);
 		t.put("max", Max.class);
 		t.put("min", Min.class);
 		t.put("per_day", PerDay.class);
 		t.put("per_hour", PerHour.class);
 		t.put("per_minute", PerMinute.class);
 		t.put("per_second", PerSecond.class);
 		t.put("range", Range.class);
 		t.put("values", Values.class);
 	}
 
 	public static AggregationField parse(QueryContext context, String s,
 			Map<String, Class<? extends AggregationFunction>> funcTable, FunctionRegistry functionRegistry) {
 		// find 'as' keyword
 		String funcPart = s.trim();
 		String alias = null;
 		int p = QueryTokenizer.findKeyword(s, AS);
 		if (p > 0) {
 			funcPart = s.substring(0, p).trim();
			alias = s.substring(p + AS.length());
 		}
 
 		// find aggregation function
 		AggregationFunction func = parseFunc(context, funcTable, funcPart, functionRegistry);
 
 		// build and return
 		AggregationField field = new AggregationField();
 		field.setName(alias != null ? alias : func.toString());
 		field.setFunction(func);
 		return field;
 	}
 
 	public static AggregationField parse(QueryContext context, String s, FunctionRegistry functionRegistry) {
 		return parse(context, s, t, functionRegistry);
 	}
 
 	private static AggregationFunction parseFunc(QueryContext context,
 			Map<String, Class<? extends AggregationFunction>> funcTable, String s, FunctionRegistry functionRegistry) {
 		int p = s.indexOf('(');
 		String funcName = s;
 		String argsToken = "";
 		if (p > 0) {
 			funcName = s.substring(0, p);
 
 			// TODO: check closing parens
 			argsToken = s.substring(p + 1, s.length() - 1);
 		}
 
 		List<String> argTokens = QueryTokenizer.parseByComma(argsToken);
 		List<Expression> exprs = new ArrayList<Expression>();
 
 		for (String argToken : argTokens) {
 			Expression expr = ExpressionParser.parse(context, argToken, functionRegistry);
 			exprs.add(expr);
 		}
 
 		// find function
 		Class<?> c = funcTable.get(funcName);
 		if (c == null)
 			throw new QueryParseException("invalid-aggregation-function", -1, "function name token is [" + funcName + "]");
 
 		try {
 			return (AggregationFunction) c.getConstructors()[0].newInstance(exprs);
 		} catch (Throwable e) {
 			throw new QueryParseException("cannot-create-aggregation-function", -1, e.getMessage());
 		}
 	}
 }
