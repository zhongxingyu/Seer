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
 package org.araqne.logdb.query.command;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.ThreadSafe;
 
 public class Rex extends QueryCommand implements ThreadSafe {
 
 	private final String field;
 	private final Pattern p;
	private final ThreadLocal<Matcher> localMatcher;
 	private final String[] names;
 
 	// for query string generation convenience
 	private String originalRegexToken;
 
 	public Rex(String field, String originalRegexToken, Pattern p, String[] names) {
 		this.field = field;
 		this.p = p;
 		this.names = names;
		this.localMatcher = new ThreadLocal<Matcher>() {
			@Override
			protected Matcher initialValue() {
				return Rex.this.p.matcher("");
			}
		};

 		this.originalRegexToken = originalRegexToken;
 	}
 
 	public String getInputField() {
 		return field;
 	}
 
 	public Pattern getPattern() {
 		return p;
 	}
 
 	public String[] getOutputNames() {
 		return names;
 	}
 
 	@Override
 	public void onPush(Row m) {
 		Object o = m.get(field);
 		if (o == null) {
 			pushPipe(m);
 			return;
 		}
 
 		String s = o.toString();
 
		Matcher matcher = localMatcher.get();
 		matcher.reset(s);
 		if (matcher.find())
 			for (int i = 0; i < matcher.groupCount(); i++)
 				m.put(names[i], matcher.group(i + 1));
 
 		pushPipe(m);
 	}
 
 	@Override
 	public void onPush(RowBatch rowBatch) {
		Matcher matcher = localMatcher.get();
 		if (rowBatch.selectedInUse) {
 			for (int i = 0; i < rowBatch.size; i++) {
 				int p = rowBatch.selected[i];
 				Row row = rowBatch.rows[p];
 
 				Object o = row.get(field);
 				if (o == null)
 					continue;
 
 				String s = o.toString();
 
 				matcher.reset(s);
 				if (matcher.find())
 					for (int g = 0; g < matcher.groupCount(); g++)
 						row.put(names[g], matcher.group(g + 1));
 			}
 		} else {
 			for (Row row : rowBatch.rows) {
 				Object o = row.get(field);
 				if (o == null)
 					continue;
 
 				String s = o.toString();
 
 				matcher.reset(s);
 				if (matcher.find())
 					for (int g = 0; g < matcher.groupCount(); g++)
 						row.put(names[g], matcher.group(g + 1));
 			}
 		}
 
 		pushPipe(rowBatch);
 	}
 
 	@Override
 	public String toString() {
 		return "rex field=" + field + " " + originalRegexToken;
 	}
 }
