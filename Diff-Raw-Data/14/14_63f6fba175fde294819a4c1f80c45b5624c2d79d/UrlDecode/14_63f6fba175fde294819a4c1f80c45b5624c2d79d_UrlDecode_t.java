 /*
  * Copyright 2013 Eediom Inc.
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
 
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * @since 1.6.7
  * @author darkluster
  * 
  */
 public class UrlDecode implements Expression {
	private final Logger logger = LoggerFactory.getLogger(UrlDecode.class);
 	private Expression valueExpr;
 	private String charset;
 
 	public UrlDecode(List<Expression> exprs) {
 		this.valueExpr = exprs.get(0);
 		charset = "utf-8";
 		if (exprs.size() > 1)
 			charset = exprs.get(1).eval(null).toString();
 		try {
 			Charset.forName(charset);
 		} catch (Exception e) {
 			throw new LogQueryParseException("invalid-charset", -1);
 		}
 	}
 
 	@Override
 	public Object eval(LogMap map) {
 		Object value = valueExpr.eval(map);
 		if (value == null)
 			return null;
 
 		try {
 			return URLDecoder.decode(value.toString(), charset);
		} catch (Throwable t) {
			if (logger.isDebugEnabled())
				logger.debug("araqne logdb: cannot decode url [" + value + "]", t);

			return value;
 		}
 	}
 }
