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
 package org.araqne.logdb.query.expr;
 
 import java.util.List;
 
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.Row;
 
 public class Substr extends FunctionExpression {
 	private final Expression valueExpr;
 	private final Expression beginExpr;
 	private Expression endExpr;
 
 	public Substr(QueryContext ctx, List<Expression> exprs) {
 		super("substr", exprs);
 		
 		this.valueExpr = exprs.get(0);
 		this.beginExpr = exprs.get(1);
 
 		if (exprs.size() > 2)
 			this.endExpr = exprs.get(2);
 	}
 
 	@Override
 	public Object eval(Row map) {
 		Object value = valueExpr.eval(map);
 		if (value == null)
 			return null;
 
 		String s = value.toString();
 		int len = s.length();
 		int begin = Integer.parseInt(beginExpr.eval(map).toString());
 		if (begin < 0)
 			begin = len + begin;
 		
 		if (begin < 0 || len <= begin)
 			return null;
 
 		int end = len;
 		if (endExpr != null)
			end = Integer.parseInt(endExpr.eval(map).toString());
 		
 		if (end < 0)
 			end = len + end;
 		
 		if (end < 0 || begin > end)
 			return null;
 
 		return s.substring(begin, end);
 	}
 
 }
