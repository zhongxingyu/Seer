 /*
  * Copyright 2010 NCHOVY
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
 package org.araqne.log.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class DelimiterParser extends V1LogParser {
 	private final char delimiter;
 	private final String[] columnHeaders;
 	private final String targetField;
 	private final boolean includeTargetField;
 	private final List<FieldDefinition> fieldDefs;
 
 	public DelimiterParser(String delimiter, String[] columnHeaders) {
 		this(delimiter, columnHeaders, "line", false);
 	}
 
 	public DelimiterParser(String delimiter, String[] columnHeaders, String targetField, boolean includeTargetField) {
 		if (delimiter.startsWith("\\u")) {
 			this.delimiter = (char) Integer.parseInt(delimiter.substring(2, 6), 16);
 		} else {
 			this.delimiter = delimiter.charAt(0);
 		}
 		this.columnHeaders = columnHeaders;
 		this.targetField = targetField;
 		this.includeTargetField = includeTargetField;
 
		this.fieldDefs = new ArrayList<FieldDefinition>();
		if (columnHeaders != null)
			for (String c : columnHeaders) {
 				fieldDefs.add(new FieldDefinition(c, "string"));
			}
 	}
 
 	@Override
 	public Map<String, Object> parse(Map<String, Object> params) {
 		String line = (String) params.get(targetField);
 		if (line == null)
 			return params;
 
 		HashMap<String, Object> m = new HashMap<String, Object>(40);
 		if (includeTargetField)
 			m.put(targetField, line);
 
 		int i = 0;
 		int last = 0;
 		while (true) {
 			int p = line.indexOf(delimiter, last);
 
 			String token = null;
 			if (p >= 0)
 				token = line.substring(last, p);
 			else
 				token = line.substring(last);
 
 			if (token.isEmpty())
 				token = null;
 
 			if (columnHeaders != null && i < columnHeaders.length)
 				m.put(columnHeaders[i], token);
 			else
 				m.put("column" + Integer.toString(i), token);
 
 			if (p < 0)
 				break;
 
 			last = p + 1;
 			i++;
 		}
 
 		return m;
 	}
 
 	/**
 	 * @since 2.9.1
 	 */
 	@Override
 	public List<FieldDefinition> getFieldDefinitions() {
 		return fieldDefs;
 	}
 }
