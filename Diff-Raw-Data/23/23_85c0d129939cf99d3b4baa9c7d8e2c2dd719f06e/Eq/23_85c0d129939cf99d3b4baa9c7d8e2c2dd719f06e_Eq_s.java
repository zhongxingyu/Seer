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
 
 import java.util.regex.Pattern;
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.ObjectComparator;
 
 public class Eq extends BinaryExpression {
 	private ObjectComparator cmp = new ObjectComparator();
 	private Pattern p;
 
 	public Eq(Expression lhs, Expression rhs) {
 		super(lhs, rhs);
 
 		if (rhs instanceof StringConstant) {
 			String needle = (String) rhs.eval(null);
			p = tryBuildPattern(needle);
 		}
 	}
 
 	public static Pattern tryBuildPattern2(String s) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("^");
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}'
 					|| c == '|' || c == '\\' || c == '*') {
 				sb.append('\\');
 				sb.append(c);
 			} else {
 				sb.append(c);
 			}
 		}
 		sb.append("$");
 		String quoted = sb.toString();
 		String expanded = quoted.replaceAll("(?<!\\\\\\\\)\\\\\\*", ".*");
 		boolean wildcard = !expanded.equals(quoted);
 		expanded = expanded.replaceAll("\\\\\\\\\\\\\\*", "\\\\*");
 
 		if (wildcard)
 			return Pattern.compile(expanded, Pattern.CASE_INSENSITIVE);
 		return null;
 	}
 
 	public static Pattern tryBuildPattern(String s) {
 		boolean wildcard = false;
 		boolean escape = false;
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("^");
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (c == '\\') {
 				if (escape)
 					sb.append('\\');
 
 				escape = !escape;
 				continue;
 			}
 
 			if (c == '*' && !escape) {
 				wildcard = true;
 				sb.append(".*");
 			} else {
 				sb.append(c);
 			}
 		}
 		sb.append("$");
 
 		if (wildcard)
 			return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
 		return null;
 	}
 
 	@Override
 	public Object eval(LogMap map) {
 		Object l = lhs.eval(map);
 		if (l == null)
 			return false;
 
 		if (p != null) {
 			return p.matcher(l.toString()).find();
 		} else {
 			Object r = rhs.eval(map);
 			if (r == null)
 				return false;
 
 			return cmp.compare(l, r) == 0;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "(" + lhs + " == " + rhs + ")";
 	}
 }
