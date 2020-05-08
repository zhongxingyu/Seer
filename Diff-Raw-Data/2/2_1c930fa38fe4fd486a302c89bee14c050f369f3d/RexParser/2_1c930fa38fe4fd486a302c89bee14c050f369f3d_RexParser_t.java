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
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.araqne.logdb.AbstractQueryCommandParser;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.query.command.Rex;
 
 public class RexParser extends AbstractQueryCommandParser {
 
 	@Override
 	public String getCommandName() {
 		return "rex";
 	}
 
 	@Override
 	public QueryCommand parse(QueryContext context, String commandString) {
 
 		// extract field names and remove placeholder
 		List<String> names = new ArrayList<String>();
 
 		ParseResult r = QueryTokenizer.parseOptions(context, commandString, "rex".length(), Arrays.asList("field"),
 				getFunctionRegistry());
 		@SuppressWarnings("unchecked")
 		Map<String, String> options = (Map<String, String>) r.value;
 
 		String field = options.get("field");
 		if (field == null)
 			throw new QueryParseException("field-not-found", commandString.length());
 
 		Pattern placeholder = Pattern.compile("\\(\\?<(.*?)>");
 		String regexToken = commandString.substring(r.next);
 		if (!QueryTokenizer.isQuoted(regexToken.trim()))
			throw new QueryParseException("invalid-regex", commandString.length(), regexToken);
 
 		// for later toString convenience
 		String originalRegexToken = regexToken;
 
 		regexToken = QueryTokenizer.removeQuotes(regexToken);
 		regexToken = toNonCapturingGroup(regexToken);
 
 		Matcher matcher = placeholder.matcher(regexToken);
 		while (matcher.find()) {
 			names.add(matcher.group(1));
 		}
 
 		while (true) {
 			matcher = placeholder.matcher(regexToken);
 			if (!matcher.find())
 				break;
 
 			// suppress special meaning of $ and \
 			String quoted = Matcher.quoteReplacement("(");
 			regexToken = matcher.replaceFirst(quoted);
 		}
 
 		Pattern p = Pattern.compile(regexToken);
 		return new Rex(field, originalRegexToken, p, names.toArray(new String[0]));
 	}
 
 	private String toNonCapturingGroup(String s) {
 		StringBuilder sb = new StringBuilder();
 
 		char last2 = ' ';
 		char last = ' ';
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (last2 != '\\' && last == '(' && c != '?')
 				sb.append("?:");
 			sb.append(c);
 			last2 = last;
 			last = c;
 		}
 
 		return sb.toString();
 	}
 
 }
