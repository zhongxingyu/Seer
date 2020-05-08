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
 
 import java.text.SimpleDateFormat;
import java.util.Date;
 import java.util.List;
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryParseException;
 
 public class ToDate implements Expression {
 
 	private Expression valueExpr;
 	private String format;
 	private SimpleDateFormat dateFormat;
 
 	public ToDate(List<Expression> exprs) {
 		this.valueExpr = exprs.get(0);
		
		// if there is no format, first argument is in unixtime
		if (exprs.size() == 1)
			return;
		
 		try {
 			this.format = (String) exprs.get(1).eval(null);
 			this.dateFormat = new SimpleDateFormat(format);
 		} catch (IllegalArgumentException e) {
 			throw new LogQueryParseException("invalid-argument", -1, "invalid date format pattern");
 		}
 	}
 
 	@Override
 	public Object eval(LogMap map) {
 		Object value = valueExpr.eval(map);
 		if (value == null)
 			return null;
 
 		try {
 			String s = value.toString();
 			if (s.isEmpty())
 				return null;
			
			if (dateFormat != null)
				return dateFormat.parse(s);
			else
				return new Date(Long.valueOf(s) * 1000);
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "date(" + valueExpr + ", " + format + ")";
 	}
 
 }
