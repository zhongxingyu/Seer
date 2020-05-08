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
 
 import java.util.List;
 
 import org.araqne.logdb.AbstractQueryCommandParser;
 import org.araqne.logdb.LookupHandlerRegistry;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.query.command.Lookup;
 
 public class LookupParser extends AbstractQueryCommandParser {
 	private LookupHandlerRegistry registry;
 
 	public LookupParser(LookupHandlerRegistry registry) {
 		this.registry = registry;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "lookup";
 	}
 
 	@Override
 	public QueryCommand parse(QueryContext context, String commandString) {
 		QueryTokens tokens = QueryTokenizer.tokenize(commandString);
 
 		// find OUTPUT token
 		int outputOffset = -1;
 		for (int i = 0; i < tokens.size(); i++) {
 			if (tokens.string(i).equalsIgnoreCase("output")) {
 				outputOffset = i;
 				break;
 			}
 		}
 
 		if (outputOffset == -1)
 			throw new QueryParseException("output-token-not-found", commandString.length());
 
 		List<String> inputTokens = tokens.substrings(2, outputOffset);
 		List<String> outputTokens = tokens.substrings(outputOffset + 1);
 
 		String handlerName = tokens.firstArg();
		if (registry != null && registry.getLookupHandler(handlerName) == null)
			throw new QueryParseException("invalid-lookup-name", -1, handlerName);
 
 		LookupField src = parseLookupField(inputTokens);
 		LookupField dst = parseLookupField(outputTokens);
 
 		Lookup lookup = new Lookup(handlerName, src.first, src.second, dst.first, dst.second);
 		lookup.setLogQueryService(registry);
 		return lookup;
 	}
 
 	private LookupField parseLookupField(List<String> tokens) {
 		LookupField field = new LookupField();
 		if (tokens.size() == 1) {
 			field.first = tokens.get(0);
 			field.second = field.first;
 			return field;
 		}
 
 		if (tokens.size() != 3)
 			throw new QueryParseException("invalid-lookup-field", -1);
 
 		if (!tokens.get(1).equalsIgnoreCase("as"))
 			throw new QueryParseException("as-token-not-found", -1);
 
 		field.first = tokens.get(0);
 		field.second = tokens.get(2);
 		return field;
 	}
 
 	private class LookupField {
 		private String first;
 		private String second;
 	}
 }
